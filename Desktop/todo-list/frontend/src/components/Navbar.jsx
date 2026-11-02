export default function Navbar({ onLogin, onSignup }) {
  return (
    <header className="navbar">
      <nav className="navbar__inner" aria-label="Main">
        <span className="brand">Countdown</span>

        <div className="navbar__links">
          <a className="navbar__anchor" href="#reviews">
            Leave a review
          </a>
          <button type="button" className="btn btn-secondary" onClick={onLogin}>
            Log in
          </button>
          <button type="button" className="btn btn-primary" onClick={onSignup}>
            Sign up
          </button>
        </div>
      </nav>
    </header>
  );
}
