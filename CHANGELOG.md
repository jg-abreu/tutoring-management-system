# Changelog

All notable changes to this project are documented here.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project uses [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Removed

- References to the Neon database, which is retired: the demo runs on its own database on the VM and development uses the Docker database.

### Changed

- The React client is back in `frontend/` with its commit history, and the repository is named `tutoring-management-system` again. Docker Compose starts PostgreSQL, the API and the client, and CI runs the backend suite and the client's lint and build. The `tutoring-management` repository is archived.

## [2.0.0] - 2026-11-20

A public demo at [tutoring.joaoabreu.dev](https://tutoring.joaoabreu.dev), with authentication, roles and read endpoints.

### Added
- Live demo at https://tutoring.joaoabreu.dev on the portfolio's server: production Docker Compose, nginx site with per-visitor rate limits, and a daily reset to the sample data at 06:00 UTC ([`deploy/README.md`](deploy/README.md)).
- `GET` endpoints: `/subjects` and `/subjects/{id}`, `/tutoring-bonds` and `/tutoring-bonds/{id}` (admins see every bond, other users only their own), `/sessions` and `/sessions/{id}`, `/sessions/{id}/enrollments` (confirmed, then the waitlist by position, then the rest) and `/enrollments` (the logged-in user's). `?status=` filters subjects, bonds and sessions.
- Cancelling a session, or revoking a bond for each of its future sessions, now also cancels the active enrollments and sends each student a `SESSION_CANCELLED` notification. Existing databases: [`docs/migrations/2026-11-20-cancel-enrollments-of-cancelled-sessions.sql`](docs/migrations/2026-11-20-cancel-enrollments-of-cancelled-sessions.sql).
- `prod` profile: the database and `JWT_SECRET` come only from environment variables, with no defaults.
- User roles: `users.role` (`ADMIN` or `USER`, default `USER`). Only admins approve or reject subjects and approve, reject or revoke tutoring bonds; other users get `403`, checked before the record is loaded. The login response includes `role`. Existing databases need [`docs/migrations/2026-11-20-add-user-role.sql`](docs/migrations/2026-11-20-add-user-role.sql).
- JWT authentication: `POST /auth/login` returns a token valid for 2 hours, and every other route requires `Authorization: Bearer <token>` (`401` otherwise). The signing key comes from the `JWT_SECRET` environment variable.
- MockMvc tests for login, token validation and the current user on every endpoint.
- `http/auth.http` logs in every sample user and stores their tokens for the other request files.

### Changed
- **Breaking:** responses are explicit records instead of serialized JPA entities, and a user inside a response is only `{ id, email }`. Before, every nested user carried Spring Security fields such as `authorities` and `enabled`.
- The default configuration points to the Docker Compose PostgreSQL with development-only values, so `./mvnw test` and `./mvnw spring-boot:run` need no environment variables.
- **Breaking:** request bodies no longer carry `userUuid`, `adminUuid` or `requesterUuid`; the acting user comes from the token, and the `PATCH` actions and enrollment take no body.
- CORS is now applied by Spring Security and allows the `Authorization` header.
- The React frontend moved to its own repository, [tutoring-management](https://github.com/jg-abreu/tutoring-management), with its commit history; this repository is now the API only and was renamed to `tutoring-management-api`. Docker Compose starts PostgreSQL and the API, CI runs only the backend suite, and the usage guide moved with the client.

### Fixed
- A tutor could enroll in their own session; it now answers `409` with `Tutors cannot enroll in their own session`.

## [1.2.0] - 2026-10-20

### Added
- Docker Compose setup that runs PostgreSQL (with schema and sample data), the API and the frontend with a single command.
- GitHub Actions CI running the backend test suite against PostgreSQL and the frontend lint and build.
- MIT license.

### Changed
- The whole project (docs, UI text, comments and sample data) is now in English.
- README rewritten; the step-by-step walkthrough moved to [`docs/USAGE.md`](docs/USAGE.md). It now opens with a screenshot of a full session using the waitlist.

### Fixed
- Actions in the Docker frontend failed with `403 Invalid CORS request`: nginx now forwards the original `Host` header with its port, so requests through the `/api` proxy are same-origin.
- Maven wrapper (`mvnw`) is now executable on Linux and macOS.
- `http/subject.http` used `PUT` for rejecting a subject; the endpoint is `PATCH`.

## [1.1.0] - 2026-10-18

### Added
- React + TypeScript frontend covering subjects, tutoring bonds, sessions and enrollments.
- CORS configuration for the frontend dev server.

### Fixed
- Suggesting a subject with an existing name returns `409` instead of a `500` that exposed SQL.
- Integrity conflicts return `409` and unexpected errors return a generic `500`, without leaking internals.
- Tutoring bonds can only be requested for approved subjects (`409` otherwise).
- Create endpoints return `201 Created`.
- Request bodies are validated with Bean Validation and return `400` when invalid.

## [1.0.0] - 2026-10-18

### Added
- Domain model: users, subjects, tutoring bonds, sessions, enrollments and notifications, mapped with JPA over a PostgreSQL schema using UUID keys and native enums.
- Subject workflow: suggest, approve and reject.
- Tutoring bond workflow: request, approve, reject and revoke, with duplicate-bond protection.
- Sessions with limited spots, restricted to the bond's requester or evaluator; cancellation restricted to the evaluator.
- Enrollment with optional waitlist, automatic promotion of the first waitlisted student on cancellation, gap-free queue renumbering and a notification to the promoted student.
- Revocation cascade: revoking a bond cancels its future active sessions.
- Centralized error handling mapping domain exceptions to `400`, `403`, `404` and `409`.
- Unit tests with JUnit 5 and Mockito for every service.

[Unreleased]: https://github.com/jg-abreu/tutoring-management-system/compare/v2.0...HEAD
[2.0.0]: https://github.com/jg-abreu/tutoring-management-system/compare/v1.2...v2.0
[1.2.0]: https://github.com/jg-abreu/tutoring-management-system/compare/v1.1...v1.2
[1.1.0]: https://github.com/jg-abreu/tutoring-management-system/compare/v1.0...v1.1
[1.0.0]: https://github.com/jg-abreu/tutoring-management-system/releases/tag/v1.0
