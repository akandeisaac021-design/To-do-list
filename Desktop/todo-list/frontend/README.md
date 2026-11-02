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

Edit `.env` if your backend isn't on `http://localhost:8080`, and add your
Web3Forms access key so the landing page's review form works (see
"Review form" below).

```
npm run dev      # dev server
npm test         # unit tests (Node's built-in runner - no extra dependencies)
npm run build    # production build
```

**Before this will actually work:** the backend needs two things added -
a `GET /api/auth/me` endpoint and CORS config allowing this frontend's
origin with credentials. See the separate `backend-additions` files if you
haven't added those yet - without CORS + `allowCredentials`, the browser
will silently block the session cookie from being sent cross-origin, and
requests to `/api/tasks` will keep coming back 401 even right after a
successful login.

## Pages

Signed-out visitors see one of three views (plain React state in `App.jsx`,
no router):

- **Landing** (`LandingPage`) - a navbar, an introduction, and the review
  form. The navbar's "Log in" / "Sign up" buttons lead to the two pages below.
- **Login** and **Signup** - the page is split into two equal vertical halves:
  the form on the left, an illustration on the right. Both pages share one
  layout component (`AuthLayout`), so the split lives in a single place.
  Under 860px wide the illustration is hidden and the form takes the full width.

Signed-in users see `TasksPage`.

## Review form

The landing page's form emails reviews to your inbox using
[Web3Forms](https://web3forms.com), called straight from the browser over
HTTPS.

**Why not send the email from the Spring backend?** That would use SMTP, and
Render's free web services block outbound SMTP ports (25, 465, 587), so it
would work on your laptop and then silently time out once deployed. (Web3Forms
also doesn't allow server-to-server calls on its free plan - it has to be the
browser.)

**One-time setup**

1. Go to https://web3forms.com, enter the address that should receive reviews,
   and the access key is emailed to you. Free plan, no card.
2. Put it in `.env`: `VITE_WEB3FORMS_ACCESS_KEY=<your key>`, then restart
   `npm run dev` (Vite only reads env vars at startup).
3. On Vercel: Project Settings -> Environment Variables, add the same name, and
   redeploy (it is baked in at build time).

The key is **public by design** - it only says where to forward submissions,
and is visible in the built JavaScript. The worst a stranger can do with it is
send you spam reviews, and Web3Forms filters those server-side. The form also
has a hidden honeypot field that quietly discards submissions from simple bots.

Without a key the form shows "The review form is not set up yet" and logs the
variable name to the browser console.

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

Form fields are a separate rule: WCAG 1.4.11 asks for 3:1 on the outline of an
input, and the light divider color (`#e4ddd0`, 1.35:1) doesn't get close - the
fields were close to invisible. Inputs now use `--border-input: #8a8275`
(3.80:1 on white, 3.55:1 on the cream background). `--border` stays light and
is only for decorative dividers.

Text sizes are defined once as a rem-based scale in `styles/base.css`
(`--fs-sm` 16px up to `--fs-hero`), so they follow the visitor's browser
font-size setting and are easy to adjust in one place.

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

- `src/reviews.js` - the review form's logic as plain functions
  (`prepareReview` trims and validates, `sendReview` calls Web3Forms). No React
  or Vite imports, which is what lets `src/reviews.test.js` run under plain
  `node --test` with no test framework installed.
- `src/components/AuthLayout.jsx` - the shared split-screen frame for login and
  signup.
- `src/styles/` - `base.css` (tokens, type scale, shared buttons/fields/
  messages), then one file per area: `auth.css`, `landing.css`, `tasks.css`.
  `index.css` just imports them.

**Image filenames are case-sensitive on Vercel.** The imports in
`AuthLayout.jsx` and `LandingPage.jsx` must match `src/images/authimage.jpeg`
and `src/images/LandingPageimage.jpeg` exactly (note the capital L), or the
build works on Windows/macOS and fails on deploy.

## Not yet built

- Editing an existing task's title/deadline (no `PUT`/`PATCH` for this on
  the backend yet - currently you'd delete and re-add)
- A router: switching between landing/login/signup is state, so the browser's
  Back button leaves the site instead of returning to the previous page, and
  there are no shareable `/login` URLs
- Per-task configurable reminder timing / email reminders (not on the
  backend yet - noted as an open item from the original planning)
- Loading skeletons, offline handling, deploy config (Vercel env vars,
  `ALLOWED_ORIGIN` for the deployed backend) - all still open once this is
  ready to leave localhost
