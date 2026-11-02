import { useEffect, useState } from 'react';
import { useAuth } from './context/AuthContext.jsx';
import LandingPage from './pages/LandingPage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import SignupPage from './pages/SignupPage.jsx';
import TasksPage from './pages/TasksPage.jsx';

export default function App() {
  const { isAuthenticated, loading } = useAuth();
  // Which public page a signed-out visitor is looking at: landing | login | signup
  const [view, setView] = useState('landing');

  // Switching "pages" is just state, so the browser won't reset the scroll
  // position itself (e.g. after scrolling down to the review form).
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [view]);

  if (loading) {
    return <div className="app-loading">Loading…</div>;
  }

  if (isAuthenticated) {
    return <TasksPage />;
  }

  const goHome = () => setView('landing');

  if (view === 'login') {
    return <LoginPage onSwitchToSignup={() => setView('signup')} onBackToHome={goHome} />;
  }

  if (view === 'signup') {
    return <SignupPage onSwitchToLogin={() => setView('login')} onBackToHome={goHome} />;
  }

  return <LandingPage onLogin={() => setView('login')} onSignup={() => setView('signup')} />;
}
