// Logic for the landing page's review form, kept free of React and Vite so it
// can be unit-tested under plain Node (see reviews.test.js).
//
// Reviews are emailed by Web3Forms (https://web3forms.com), called straight
// from the browser over HTTPS. They deliberately do NOT go through the Spring
// backend: sending mail from there would need SMTP, and Render's free tier
// blocks outbound SMTP ports (25/465/587). The access key is designed to be
// public - it only identifies which inbox the submissions are forwarded to.

export const WEB3FORMS_ENDPOINT = 'https://api.web3forms.com/submit';

export const NAME_MAX_LENGTH = 80;
export const REVIEW_MAX_LENGTH = 1000;

const EMAIL_SUBJECT = 'New Countdown review';
const SEND_FAILED = 'Could not send your review. Please try again.';
const NOT_CONFIGURED = 'The review form is not set up yet.';

/**
 * Normalises raw form input and decides whether it should be sent.
 *
 * @returns {{status: 'ok', review: {name: string, review: string}}
 *          | {status: 'invalid', error: string}
 *          | {status: 'spam'}}
 */
export function prepareReview({ name, review, honeypot }) {
  // Real visitors never see the honeypot field, so anything in it means a
  // bot filled in every input it could find.
  if (honeypot) {
    return { status: 'spam' };
  }

  const cleanName = name.trim();
  const cleanReview = review.trim();

  if (!cleanName) {
    return { status: 'invalid', error: 'Please enter your name.' };
  }
  if (!cleanReview) {
    return { status: 'invalid', error: 'Please write your review.' };
  }

  return { status: 'ok', review: { name: cleanName, review: cleanReview } };
}

/**
 * Emails a review via Web3Forms. Resolves on success; rejects with an Error
 * whose message is safe to show the visitor.
 *
 * `fetchFn` is injectable so tests don't need the network.
 */
export async function sendReview({ name, review }, { accessKey, fetchFn = globalThis.fetch }) {
  if (!accessKey) {
    console.error(
      'VITE_WEB3FORMS_ACCESS_KEY is not set. See "Review form" in the README for how to get one.',
    );
    throw new Error(NOT_CONFIGURED);
  }

  let response;
  try {
    response = await fetchFn(WEB3FORMS_ENDPOINT, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
      body: JSON.stringify({ access_key: accessKey, subject: EMAIL_SUBJECT, name, review }),
    });
  } catch {
    throw new Error(SEND_FAILED);
  }

  const data = await response.json().catch(() => null);

  if (!response.ok || !data?.success) {
    throw new Error(data?.message || SEND_FAILED);
  }
}
