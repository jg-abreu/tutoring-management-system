# Frontend — Tutoring Management System

Web interface for the academic tutoring API. It covers the whole system flow in the browser: suggesting and approving subjects, requesting and evaluating tutoring bonds, creating and cancelling sessions, enrolling students and cancelling enrollments (with a waitlist).

## Stack

- **React 19** + **TypeScript**
- **Vite** (dev server and build)
- **oxlint** (linting)
- No UI, state or HTTP libraries: native `fetch`, Context API, plain CSS with variables and automatic dark mode

## Running

Prerequisites: Node.js 20+ and the API running at `http://localhost:8080` (see the root README).

```bash
npm install
npm run dev
```

Open `http://localhost:5173`. The API already allows this origin via CORS (`CorsConfig`).

To point to a different API, create a `.env.local` file with:

```
VITE_API_URL=http://other-host:8080
```

Other commands:

| Command | What it does |
|---|---|
| `npm run build` | type check + production build into `dist/` |
| `npm run lint` | lint with oxlint |
| `npm run preview` | serves the production build locally |

## Structure

```
src/
├── api/          central HTTP client and one function per API endpoint
├── components/   reusable components (status badge, messages, user switcher)
├── config/       temporary test data (users and database records)
├── context/      shared state (current user, known records)
├── features/     one folder per screen: subjects, bonds, sessions
├── hooks/        useLocalStorage, useApiAction
└── lib/          date and time zone formatting
```

## Known limitations (planned for v2.0)

- **No login:** the "Acting as" switcher picks the user who performs each action; the user list is hardcoded in `src/config/testData.ts`, pointing to records in the development database.
- **The API has no `GET` endpoints yet:** the lists only show what was created or changed from the screen itself (stored in `localStorage`). Changes the server makes to other records — such as automatic waitlist promotion or cascading cancellation — don't show up until there is a way to query them.
- **No user roles:** the approve/reject buttons show up for every user; the API is the one that decides.
