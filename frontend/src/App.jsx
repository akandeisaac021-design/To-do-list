import { useState } from 'react';
import { useAuth } from './context/AuthContext.jsx';
import LoginPage from './pages/LoginPage.jsx';
import SignupPage from './pages/SignupPage.jsx';
import TasksPage from './pages/TasksPage.jsx';

export default function App() {
  const { isAuthenticated, loading } = useAuth();
  const [authView, setAuthView] = useState('login');

  if (loading) {
    return <div className="app-loading">Loading…</div>;
  }

  if (!isAuthenticated) {
    return authView === 'login' ? (
      <LoginPage onSwitchToSignup={() => setAuthView('signup')} />
    ) : (
      <SignupPage onSwitchToLogin={() => setAuthView('login')} />
    );
  }

  return <TasksPage />;
}
