import authImage from '../images/authimage.jpeg';

/**
 * The split-screen frame shared by the login and signup pages: form on the
 * left, illustration on the right. Pages supply only the form content.
 */
export default function AuthLayout({ onBackToHome, children }) {
  return (
    <div className="auth-split">
      <section className="auth-split__form">
        <button
          type="button"
          className="brand brand-button auth-split__brand"
          onClick={onBackToHome}
          aria-label="Countdown, back to home"
        >
          Countdown
        </button>
        <div className="auth-split__body">
          <div className="auth-card">{children}</div>
        </div>
      </section>

      <div className="auth-split__visual">
        <img
          src={authImage}
          alt="Illustration of a stressed student surrounded by books, sticky notes and looming deadlines"
          width="736"
          height="981"
        />
      </div>
    </div>
  );
}
