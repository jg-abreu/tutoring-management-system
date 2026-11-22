# AGENTS.md

Project rules for Codex in this repository. Machine-specific instructions live in `CLAUDE.local.md` (git-ignored).

## Project

Tutoring Management System: an academic tutoring platform where admins approve who can tutor each subject, tutors open sessions with limited spots, and students enroll, with an automatic waitlist that promotes the next student when a spot opens up. Built as a portfolio project and treated with production seriousness (tests, versioning, technical debt).

One repository with both parts: the API at the root and the React client in `frontend/`.

- **API stack:** Java 21, Spring Boot 4.1 (Web MVC, Data JPA, Validation, Security), Hibernate, Lombok, PostgreSQL 17, JUnit 5 + Mockito, Maven wrapper.
- **Client stack (`frontend/`):** React 19, TypeScript, Vite, oxlint. No UI, state or HTTP libraries: native `fetch`, Context API and plain CSS.
- **Infrastructure:** Docker Compose (PostgreSQL + API + client served by nginx, which forwards `/api/` to the API), GitHub Actions CI with one job per part.
- **Current version:** v2.0 released and live at https://tutoring.joaoabreu.dev. The open work lives in the issues and in the "Tutoring Management" GitHub Project.

## Workflow: spec-driven development

This project follows an AI-first, spec-driven workflow: the coding assistant drafts and implements, João approves the intent and reviews the result. Every feature or fix goes through these stages, and **no code is written before the spec and the plan are approved**.

1. **Spec.** Write the specification in the GitHub issue (or in `docs/specs/<feature>.md` for larger features): the problem, the business rules, the acceptance criteria and the error cases with their status codes. For the client, also the behavior on screen and how each API error is shown. João approves or edits it.
2. **Plan.** Comment on the issue with the files to change, the endpoints, any schema change and the test scenarios (or, for the client, how the change will be checked in the browser). João approves or edits it.
3. **Tasks.** Split the approved plan into small tasks, as a checklist in the issue. Each task becomes one commit.
4. **Implement.** For each task in the API, write the tests from the acceptance criteria first, then the code, then run `./mvnw test`. In the client, write the code, run `npm run lint` and `npm run build` in `frontend/` and check the flow in the browser against the API. Keep new code free of explanatory comments.
5. **Explain.** After each task, explain the implementation outside the code: what changed, why it was done this way and which alternative was discarded.
6. **Review.** Open the PR with `Closes #N`. CI must be green. João reviews the code against the spec and merges.

If the implementation shows that the spec is wrong or incomplete, stop, propose the change to the spec and wait for João before continuing. The spec is updated first, and the code follows.

## Division of responsibilities

| Area | Who does the work | Codex's role |
|---|---|---|
| Specs, plans and tasks | Codex drafts, João approves | Writes them in the issue and waits for approval before coding |
| Business rules, architecture and trade-offs | João | Presents the options with pros and cons; the final decision is João's |
| Back-end (production code, tests, schema and seed, `.http` files) | Codex | Implements and tests in small tasks |
| Front-end (`frontend/`) | Codex | Implements and validates (lint, build, browser check) |
| Docker and CI | Codex | Implements and validates (build, `docker compose up`, tests) |
| Git and GitHub (branches, commits, messages, push, PR description and opening) | Codex | Creates the branch, commits (including files João wrote), pushes and opens the PR |
| Review and merge | João | Codex **never merges or approves a PR** |

When a decision is needed, ask **one question at a time**. Use concepts and tools João already knows; ask before introducing unfamiliar ones, including new npm dependencies.

## Project structure

```
src/main/java/com/joaoguilherme/tutoringmanagementsystem/
  controller/   REST endpoints, one controller per resource
  service/      business rules and state transitions
  model/        JPA entities and status enums
  repository/   Spring Data repositories (derived queries)
  dto/          validated request bodies
  exception/    domain exceptions + GlobalExceptionHandler
  config/       CORS
src/test/java/...           service unit tests (JUnit 5 + Mockito) and the context test
docker/postgres/init/       01-schema.sql and 02-seed.sql, loaded by Docker Compose and CI (and by the demo's daily reset)
deploy/                     live demo at tutoring.joaoabreu.dev: production compose, nginx, reset timer, README
docs/                       schema notes, design diagrams, specs/, migrations/, client usage guide (USAGE.md)
http/                       IntelliJ HTTP Client request collections
frontend/                   React client
  src/api/                  central HTTP client (client.ts) and one function per API endpoint
  src/components/           reusable components
  src/context/              shared state (logged-in user, known records)
  src/features/             one folder per screen
  src/hooks/                shared hooks
  src/lib/                  date and time zone formatting
  Dockerfile                Vite build served by nginx
  default.conf.template     nginx config; API_URL is filled in when the container starts
.github/workflows/ci.yml    backend tests against PostgreSQL, and client lint and build
```

## Conventions

- `spring.jpa.hibernate.ddl-auto=validate` on purpose: the schema is written by hand (`docs/database_schema_reference.md` and `docker/postgres/init/01-schema.sql`), and Hibernate must not change it. A schema change updates both files in the same task.
- Unit tests right after each service, before moving on: `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`, test names in English as `should...When...`.
- Exceptions are thrown in the service and handled in `GlobalExceptionHandler` with `handle<ExceptionName>` methods. **400** = the request itself is invalid; **409** = valid request that conflicts with the current state; **404** = not found; **403** = not allowed. Unexpected errors return a generic 500, always logged and never echoing `ex.getMessage()`.
- Input validation with Bean Validation on the DTOs plus `@Valid` before each `@RequestBody`.
- Endpoints that create a resource use `@ResponseStatus(HttpStatus.CREATED)`.
- Entities have a constructor with the required fields plus `@NoArgsConstructor` (required by JPA).
- Shallow nested routes: creating uses the parent (`POST /sessions/{id}/enrollments`); acting on an existing item uses only its id (`PATCH /enrollments/{id}/cancel`).
- Nothing is physically deleted: cancelling, rejecting and revoking are status changes.

Client (`frontend/`):

- All HTTP calls go through `src/api/client.ts`; screens never call `fetch` directly.
- API types in `src/api/types.ts` mirror the API responses; a change to a response updates the type in the same PR.
- Error messages shown to the user come from the API response (`ApiError`); the client does not duplicate business rules, the API decides.
- One folder per screen in `src/features/`; shared pieces move to `components/` or `hooks/` only when a second screen needs them.
- Plain CSS with the variables in `index.css`; no CSS framework.

## Git workflow

- **No direct commits to `main`**: every change, documentation included, goes through a PR.
- One branch per issue, created from an up-to-date `main` (`git pull` first), prefixed: `feature/`, `fix/`, `test/`, `docs/`, `chore/`, `ci/`. A change that touches the API and the client goes in one branch and one PR.
- Small commits in English using Conventional Commits (`feat:`, `fix:`, `test:`, `docs:`, `refactor:`, `style:`, `chore:`, `ci:`), one per task.
- Before each commit, check `git status` and `git diff` and add only the task's files; never `git add .`.
- Never add AI attribution to commits or PRs: no `Co-Authored-By` trailers, "Generated with" lines or session links.
- PR description in English: what changed, why, how to test, the definition-of-done checklist and `Closes #N`, so the GitHub Project moves the card to Done on merge.
- Work happens on two machines: `git pull` at the start of a session; commit and push everything at the end.

## Keeping CLAUDE.md and AGENTS.md in sync

`CLAUDE.md` and `AGENTS.md` hold the same project rules, one file per coding assistant. They must stay identical except for the title line and the assistant's name.

- Any change to the project rules goes into **both files in the same commit**.
- Machine-specific instructions (vault path, end-of-day and handoff steps) do not go here; they live in the git-ignored `CLAUDE.local.md`.
- Before committing, check that only the title line differs (both sides are normalized to the same assistant name):

  ```bash
  n='s/Claude Code/Codex/g;s/Claude/Codex/g'; diff <(sed "$n" CLAUDE.md) <(sed "$n" AGENTS.md)
  ```

## Definition of done (per issue)

- [ ] The spec and the plan were approved by João before the code
- [ ] Every acceptance criterion in the spec is covered by a test (API) or was checked in the browser against the API (client)
- [ ] `./mvnw test` passes, `npm run lint` and `npm run build` pass in `frontend/`, and CI is green
- [ ] Schema changes are reflected in `01-schema.sql` and `docs/database_schema_reference.md`
- [ ] `http/` requests and the README (API table, Roadmap, client screens and commands) reflect the change
- [ ] No secret in the code, tests, logs or commit
- [ ] João has reviewed the code against the spec

## Commands

```bash
docker compose up -d db         # PostgreSQL with schema and sample data; the default configuration points to it
./mvnw test                     # full backend suite
./mvnw spring-boot:run          # API on http://localhost:8080 (servers use SPRING_PROFILES_ACTIVE=prod)
docker compose up --build       # PostgreSQL + API on http://localhost:8080 + client on http://localhost:3000

cd frontend
npm install
npm run dev                     # client on http://localhost:5173 (API on http://localhost:8080)
npm run lint && npm run build
```

On Windows PowerShell use `.\mvnw.cmd`. Stop the API in IntelliJ before switching branches: DevTools restarts mid-switch and the instance ends up without controllers, answering 404 to everything.

## Session context

Use only Google Drive for Second Brain notes; the retired Obsidian app and MCP are not session prerequisites. Read the original `CLAUDE.local.md` for the local Drive path, then the transition, universal rules and project Status. Update Status and Log at an authorized session close, and the transition when passing the baton. Keep historical entries intact.
