import landingImage from '../images/LandingPageimage.jpeg';
import Navbar from '../components/Navbar.jsx';
import ReviewForm from '../components/ReviewForm.jsx';

export default function LandingPage({ onLogin, onSignup }) {
  return (
    <>
      <Navbar onLogin={onLogin} onSignup={onSignup} />

      <main>
        <section className="hero" aria-labelledby="hero-title">
          <div className="hero__copy">
            <h1 id="hero-title">Know what&rsquo;s due next.</h1>
            <p className="hero__lede">
              Add a task and a deadline. Your list keeps the soonest one on top, and anything
              overdue turns red so it can&rsquo;t slip past you.
            </p>
            <div className="hero__actions">
              <button type="button" className="btn btn-primary btn-lg" onClick={onSignup}>
                Get started
              </button>
              <button type="button" className="btn btn-secondary btn-lg" onClick={onLogin}>
                Log in
              </button>
            </div>
          </div>

          <div className="hero__visual">
            <img
              src={landingImage}
              alt="Illustration of a focused student in headphones studying with a pen and tablet"
              width="735"
              height="890"
            />
          </div>
        </section>

        <section id="reviews" className="reviews" aria-labelledby="reviews-title">
          <div className="reviews__inner">
            <h2 id="reviews-title">Leave a review</h2>
            <p className="reviews__intro">
              Tried Countdown? Tell us what worked and what didn&rsquo;t. Your review is emailed
              straight to us.
            </p>
            <ReviewForm />
          </div>
        </section>
      </main>
    </>
  );
}
