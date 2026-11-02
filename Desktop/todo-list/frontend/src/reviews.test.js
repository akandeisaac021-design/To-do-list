import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import { prepareReview, sendReview, WEB3FORMS_ENDPOINT } from './reviews.js';

describe('prepareReview', () => {
  it('trims surrounding whitespace from name and review', () => {
    const result = prepareReview({ name: '  Ada  ', review: '  Loved it.  ', honeypot: '' });

    assert.deepEqual(result, { status: 'ok', review: { name: 'Ada', review: 'Loved it.' } });
  });

  it('rejects a name that is empty or only whitespace', () => {
    const result = prepareReview({ name: '   ', review: 'Fine.', honeypot: '' });

    assert.equal(result.status, 'invalid');
    assert.match(result.error, /name/i);
  });

  it('rejects a review that is empty or only whitespace', () => {
    const result = prepareReview({ name: 'Ada', review: '\n  \t', honeypot: '' });

    assert.equal(result.status, 'invalid');
    assert.match(result.error, /review/i);
  });

  it('reports the missing name first when both fields are blank', () => {
    const result = prepareReview({ name: '', review: '', honeypot: '' });

    assert.equal(result.status, 'invalid');
    assert.match(result.error, /name/i);
  });

  it('flags the submission as spam when the hidden honeypot field is filled in', () => {
    const result = prepareReview({ name: 'Ada', review: 'Loved it.', honeypot: 'http://spam.example' });

    assert.deepEqual(result, { status: 'spam' });
  });
});

describe('sendReview', () => {
  const review = { name: 'Ada', review: 'Loved it.' };

  function fakeFetch(response) {
    const calls = [];
    const fetchFn = async (url, options) => {
      calls.push({ url, options });
      return response;
    };
    return { fetchFn, calls };
  }

  it('POSTs the review as JSON to Web3Forms along with the access key', async () => {
    const { fetchFn, calls } = fakeFetch({ ok: true, json: async () => ({ success: true }) });

    await sendReview(review, { accessKey: 'key-123', fetchFn });

    assert.equal(calls.length, 1);
    assert.equal(calls[0].url, WEB3FORMS_ENDPOINT);
    assert.equal(calls[0].options.method, 'POST');
    assert.equal(calls[0].options.headers['Content-Type'], 'application/json');
    assert.equal(calls[0].options.headers.Accept, 'application/json');

    const body = JSON.parse(calls[0].options.body);
    assert.equal(body.access_key, 'key-123');
    assert.equal(body.name, 'Ada');
    assert.equal(body.review, 'Loved it.');
    assert.ok(body.subject, 'an email subject is set so reviews are easy to spot in the inbox');
  });

  it('does not send cookies to the third-party endpoint', async () => {
    const { fetchFn, calls } = fakeFetch({ ok: true, json: async () => ({ success: true }) });

    await sendReview(review, { accessKey: 'key-123', fetchFn });

    assert.notEqual(calls[0].options.credentials, 'include');
  });

  it('rejects with the service message when Web3Forms reports a failure', async () => {
    const { fetchFn } = fakeFetch({
      ok: false,
      json: async () => ({ success: false, message: 'Invalid access key' }),
    });

    await assert.rejects(sendReview(review, { accessKey: 'bad', fetchFn }), {
      message: 'Invalid access key',
    });
  });

  it('rejects with a generic message when the response body is not JSON', async () => {
    const { fetchFn } = fakeFetch({
      ok: false,
      json: async () => {
        throw new SyntaxError('Unexpected token <');
      },
    });

    await assert.rejects(sendReview(review, { accessKey: 'key-123', fetchFn }), /could not send/i);
  });

  it('rejects with a friendly message when the network request itself fails', async () => {
    const fetchFn = async () => {
      throw new TypeError('Failed to fetch');
    };

    await assert.rejects(sendReview(review, { accessKey: 'key-123', fetchFn }), /could not send/i);
  });

  it('rejects without making any request when no access key is configured', async () => {
    const { fetchFn, calls } = fakeFetch({ ok: true, json: async () => ({ success: true }) });

    await assert.rejects(sendReview(review, { accessKey: '', fetchFn }), /not set up/i);
    assert.equal(calls.length, 0);
  });
});
