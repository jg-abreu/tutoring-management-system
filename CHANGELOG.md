# Changelog

All notable changes to this project are documented here.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project uses [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [1.2.0] - 2026-10-20

### Added
- Docker Compose setup that runs PostgreSQL (with schema and sample data), the API and the frontend with a single command.
- GitHub Actions CI running the backend test suite against PostgreSQL and the frontend lint and build.
- MIT license.

### Changed
- The whole project (docs, UI text, comments and sample data) is now in English.
- README rewritten; the step-by-step walkthrough moved to [`docs/USAGE.md`](docs/USAGE.md).

### Fixed
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

[Unreleased]: https://github.com/jg-abreu/tutoring-management-system/compare/v1.2...HEAD
[1.2.0]: https://github.com/jg-abreu/tutoring-management-system/compare/v1.1...v1.2
[1.1.0]: https://github.com/jg-abreu/tutoring-management-system/compare/v1.0...v1.1
[1.0.0]: https://github.com/jg-abreu/tutoring-management-system/releases/tag/v1.0
