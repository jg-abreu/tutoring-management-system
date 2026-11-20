# Tutoring Management

[![CI](https://github.com/jg-abreu/tutoring-management/actions/workflows/ci.yml/badge.svg)](https://github.com/jg-abreu/tutoring-management/actions/workflows/ci.yml)
![React 19](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)
![TypeScript 6](https://img.shields.io/badge/TypeScript-6-3178C6?logo=typescript&logoColor=white)
![Vite 8](https://img.shields.io/badge/Vite-8-646CFF?logo=vite&logoColor=white)

Web client for the [Tutoring Management API](https://github.com/jg-abreu/tutoring-management-api). It covers the whole flow in the browser: suggesting and approving subjects, requesting and evaluating tutoring bonds, creating and cancelling sessions, enrolling students and cancelling enrollments (with a waitlist).

<p align="center">
  <picture>
    <source media="(prefers-color-scheme: light)" srcset="docs/images/session-waitlist-light.png">
    <img src="docs/images/session-waitlist.png" alt="A Calculus I session with capacity 2 shown as a seat map: two seats taken by student1 and student2, and student3 first on the waitlist" width="720">
  </picture>
</p>

The business rules, the database and the tests live in the API repository; this client exists to demo them.

## Stack

- **React 19** + **TypeScript**
- **Vite** (dev server and build)
- **oxlint** (linting)
- No UI, state or HTTP libraries: native `fetch`, Context API and plain CSS
- Same visual identity as [joaoabreu.dev](https://joaoabreu.dev): its color tokens, Inter and JetBrains Mono (through Fontsource, bundled with the build), dark and light themes with a toggle that remembers the choice

## Running

Both options need the API running on `http://localhost:8080`. The quickest way is Docker, in the API repository:

```bash
git clone https://github.com/jg-abreu/tutoring-management-api.git
cd tutoring-management-api
docker compose up --build
```

### With Node.js (development)

Prerequisite: Node.js 20+.

```bash
npm install
npm run dev
```

Open `http://localhost:5173`. The API already allows this origin via CORS.

To point to a different API, create a `.env.local` file with:

```
VITE_API_URL=http://other-host:8080
```

### With Docker

```bash
docker compose up --build
```

Open `http://localhost:3000`. nginx serves the build and forwards `/api/` to the API, so the browser sees a single origin. The API address comes from the `API_URL` environment variable (default `http://host.docker.internal:8080`, the API on your machine); if another program uses port 8080 on your machine, point it elsewhere, for example `API_URL=http://other-host:8080 docker compose up --build`.

### Logging in

The client opens on a login screen. The sample users from the API's seed all use the password `senha123`, and picking one in the sample users list fills in the form. The token is kept in memory only, so reloading the page logs out; a `401` from the API (expired or invalid token) also brings back the login screen. Approve, reject and revoke only show for admins; the role comes from the login response, and the API still refuses those actions with `403` for anyone else.

### Commands

| Command | What it does |
|---|---|
| `npm run dev` | dev server with hot reload |
| `npm run build` | type check + production build into `dist/` |
| `npm run lint` | lint with oxlint |
| `npm run preview` | serves the production build locally |

## Structure

```
src/
├── api/          central HTTP client and one function per API endpoint
├── components/   reusable components (header, sidebar, theme toggle, seat map, status badge, messages)
├── config/       the sample users listed on the login screen
├── context/      shared state (session: token and current user; subjects, bonds, sessions and enrollments loaded from the API)
├── features/     one folder per screen: login, subjects, bonds, sessions
├── hooks/        useLocalStorage (theme and current section), useApiAction
└── lib/          date and time zone formatting, reading the user id from the token
```

The [usage guide](docs/USAGE.md) walks through every flow step by step, from an empty catalog to a session with a waitlist.

## Known limitations

- **Lists reload after your own actions, not on their own:** what another user changes shows up after your next action or a page reload. There is no polling or push.
- **Every list is read in full:** the API has no pagination yet, which is fine for the sample data.

## History

This client started in the `frontend/` folder of the API repository and moved here with its commit history.
