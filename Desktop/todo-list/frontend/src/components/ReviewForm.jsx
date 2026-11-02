import { useState } from 'react';
import { NAME_MAX_LENGTH, REVIEW_MAX_LENGTH, prepareReview, sendReview } from '../reviews.js';

// Public by design - it only tells Web3Forms which inbox to forward to.
// Vite reads this at dev-server start / build time, so restart after changing .env.
const ACCESS_KEY = import.meta.env.VITE_WEB3FORMS_ACCESS_KEY;

export default function ReviewForm() {
  const [name, setName] = useState('');
  const [review, setReview] = useState('');
  const [honeypot, setHoneypot] = useState('');
  const [status, setStatus] = useState('idle'); // idle | sending | sent | error
  const [message, setMessage] = useState('');

  async function handleSubmit(event) {
    event.preventDefault();

    const prepared = prepareReview({ name, review, honeypot });

    if (prepared.status === 'invalid') {
      setStatus('error');
      setMessage(prepared.error);
      return;
    }

    if (prepared.status === 'ok') {
      setStatus('sending');
      setMessage('');
      try {
        await sendReview(prepared.review, { accessKey: ACCESS_KEY });
      } catch (err) {
        setStatus('error');
        setMessage(err.message);
        return;
      }
    }

    // A flagged bot submission falls through to the same success state, so it
    // learns nothing from the response - but nothing was actually sent.
    setName('');
    setReview('');
    setHoneypot('');
    setStatus('sent');
    setMessage('Thank you! Your review has been sent.');
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="field">
        <label htmlFor="review-name">Your name</label>
        <input
          id="review-name"
          className="input"
          type="text"
          value={name}
          onChange={(e) => setName(e.target.value)}
          maxLength={NAME_MAX_LENGTH}
          autoComplete="name"
          required
        />
      </div>

      <div className="field">
        <label htmlFor="review-text">Your review</label>
        <textarea
          id="review-text"
          className="input"
          rows={6}
          value={review}
          onChange={(e) => setReview(e.target.value)}
          maxLength={REVIEW_MAX_LENGTH}
          required
        />
      </div>

      {/* Bot trap - see .honeypot in landing.css */}
      <div className="honeypot" aria-hidden="true">
        <label htmlFor="review-website">Leave this field empty</label>
        <input
          id="review-website"
          type="text"
          tabIndex={-1}
          autoComplete="off"
          value={honeypot}
          onChange={(e) => setHoneypot(e.target.value)}
        />
      </div>

      <button className="btn btn-primary btn-lg" type="submit" disabled={status === 'sending'}>
        {status === 'sending' ? 'Sending…' : 'Send review'}
      </button>

      {status === 'error' && (
        <div className="form-error" role="alert">
          {message}
        </div>
      )}
      {status === 'sent' && (
        <div className="form-success" role="status">
          {message}
        </div>
      )}
    </form>
  );
}
