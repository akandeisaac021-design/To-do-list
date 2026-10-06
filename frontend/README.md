# Todo List App (Frontend)

React + Vite + JavaScript frontend for the to-do list app.

## Auth model

The backend uses session-cookie auth, not JWT: `POST /api/auth/login` sets a
`JSESSIONID` cookie, and every later request just needs to include it
(`credentials: 'include'` on every `fetch` call in `src/api.js` handles
this). There's no token stored in localStorage, and nothing to attach to
headers manually.

Because there's no client-visible token, the app can't tell on page load
whether you're already logged in - it has to ask. `AuthContext` calls
`GET /api/auth/me` on mount; a `200` means there's a valid session, a `401`
means there isn't. That's the `loading` state you see briefly on first load.

## Setup

```
npm install
cp .env.example .env
```

Edit `.env` if your backend isn't on `http://localhost:8080`.

```
npm run dev
```

**Before this will actually work:** the backend needs two things added -
a `GET /api/auth/me` endpoint and CORS config allowing this frontend's
origin with credentials. See the separate `backend-additions` files if you
haven't added those yet - without CORS + `allowCredentials`, the browser
will silently block the session cookie from being sent cross-origin, and
requests to `/api/tasks` will keep coming back 401 even right after a
successful login.

## Design

The visual identity is built around the deadline itself: every deadline and
timestamp is set in a monospace numeral face (IBM Plex Mono), so it reads
like a clock rather than a form field. Expired tasks get a solid red left
border and bold red text - no badges or icons, just weight and color doing
the work.

The palette was checked against WCAG AA (4.5:1 for normal text): the muted
gray used for deadlines, labels, and the username display is `#6b6459`,
which clears 4.5:1 on both the white surface and the cream page background.
An earlier lighter gray here (`#8a8275`) only hit 3.80:1 and would have
technically failed accessibility contrast requirements on real body text.

## How it fits together

- `src/api.js` - all backend calls. `credentials: 'include'` on every
  request is what carries the session cookie; there's no token to attach.
- `src/context/AuthContext.jsx` - holds the logged-in user, exposes
  `login`, `signup`, `logout`. Note `signup` calls `login` internally right
  after - signup alone doesn't establish a session on the backend.
- `src/dateUtils.js` - `isExpired()` computes overdue status client-side
  from `deadline` + `status`, since the backend's task response doesn't
  include a pre-computed `expired` flag.
- `src/pages/TasksPage.jsx` - main view; the backend already returns tasks
  sorted soonest-deadline-first, so the frontend only re-sorts locally
  after adding a new one.

## Not yet built

- Editing an existing task's title/deadline (no `PUT`/`PATCH` for this on
  the backend yet - currently you'd delete and re-add)
- Per-task configurable reminder timing / email reminders (not on the
  backend yet - noted as an open item from the original planning)
- Loading skeletons, offline handling, deploy config (Vercel env vars,
  `ALLOWED_ORIGIN` for the deployed backend) - all still open once this is
  ready to leave localhost
