import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { authApi } from '../api.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  // Starts true: with session-cookie auth there's nothing in localStorage
  // to check synchronously, so the app genuinely doesn't know if you're
  // logged in until /api/auth/me answers.
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    authApi
      .me()
      .then(setUser)
      .catch(() => setUser(null)) // 401 just means "not logged in", not an error to surface
      .finally(() => setLoading(false));
  }, []);

  const login = useCallback(async (username, password) => {
    const loggedInUser = await authApi.login(username, password);
    setUser(loggedInUser);
  }, []);

  const signup = useCallback(
    async (username, password) => {
      // Signup doesn't establish a session on its own (only /login does) -
      // matches the backend, where AuthController.signup never touches
      // HttpSession.
      await authApi.signup(username, password);
      await login(username, password);
    },
    [login],
  );

  const logout = useCallback(async () => {
    await authApi.logout();
    setUser(null);
  }, []);

  const value = {
    user,
    isAuthenticated: Boolean(user),
    loading,
    login,
    signup,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
