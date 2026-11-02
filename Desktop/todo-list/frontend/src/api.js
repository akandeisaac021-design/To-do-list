const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    // Required for the session cookie to be sent/received cross-origin
    // (frontend on :5173, backend on :8080) - matches the backend's CORS
    // config, which only allows credentials for an explicit origin.
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });

  if (response.status === 204) {
    return null;
  }

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    // Backend's GlobalExceptionHandler always returns { message: "..." }
    // on error responses - fall back to the status code if that's missing
    // for some reason (e.g. a network-level failure with no JSON body).
    throw new Error(data?.message || `Request failed (${response.status})`);
  }

  return data;
}

export const authApi = {
  signup: (username, password) =>
    request('/api/auth/signup', { method: 'POST', body: JSON.stringify({ username, password }) }),
  login: (username, password) =>
    request('/api/auth/login', { method: 'POST', body: JSON.stringify({ username, password }) }),
  logout: () => request('/api/auth/logout', { method: 'POST' }),
  me: () => request('/api/auth/me'),
};

export const taskApi = {
  list: () => request('/api/tasks'),
  create: (task) => request('/api/tasks', { method: 'POST', body: JSON.stringify(task) }),
  complete: (id) => request(`/api/tasks/${id}/complete`, { method: 'PATCH' }),
  remove: (id) => request(`/api/tasks/${id}`, { method: 'DELETE' }),
};
