# Academic Tutoring Management System

A system for managing study groups and academic tutoring sessions, with admin-approved tutoring bonds, sessions with limited spots and a waitlist, and automatic cascading cancellation when a bond is revoked. REST API in Spring Boot + web interface in React.

Personal portfolio project, built with a focus on real domain modeling, business rules driven by state machines, and context-based authorization (rather than fixed user roles).

## Stack

**Backend**
- Java 21
- Spring Boot (Web, Data JPA, Validation)
- Hibernate / JPA
- PostgreSQL
- Lombok
- JUnit 5 + Mockito

**Frontend** (in the [`frontend`](./frontend) folder)
- React 19 + TypeScript
- Vite

## Running

### Running with Docker (recommended for trying it out)

All you need is [Docker Desktop](https://www.docker.com/products/docker-desktop/). Compose starts PostgreSQL (already loaded with the schema and sample data), the API and the frontend:

```bash
git clone https://github.com/jg-abreu/tutoring-management-system.git
cd tutoring-management-system
docker compose up --build
```

The first startup takes a few minutes (pulling images and building). After that:

| What | Address | How to access it |
|---|---|---|
| Frontend | http://localhost:3000 | in the browser (this is where you test it) |
| API | http://localhost:8080 | through an HTTP client (the [`http/`](./http) files, Postman, curl). The API has no `GET` endpoints yet, so opening it in the browser shows a 404, and that's expected |
| PostgreSQL | `localhost:5433`, database/user/password `tutoring` | through a database client (pgAdmin, DBeaver, IntelliJ); it doesn't open in the browser |

The data lives in a Docker volume and survives `docker compose down`. To go back to the initial state (schema + seed again): `docker compose down -v` and then `docker compose up`.

**Test users.** There is no login yet: the frontend has an **"Acting as"** switcher, and the API receives the user's UUID in the request body. The seed ([`docker/postgres/init/02-seed.sql`](./docker/postgres/init/02-seed.sql)) creates:

| In the switcher | Email | Role | UUID |
|---|---|---|---|
| admin | `admin@tutoring.dev` | Admin | `2c53ba13-53cc-4c79-a4f2-31cfbcceb898` |
| jgabreuteste | `tutor@tutoring.dev` | Calculus I tutor | `899b90d5-673e-4137-abd2-1606d73f9b0e` |
| student1 | `student1@tutoring.dev` | Student | `451c540f-a05f-4ead-a6d3-89528391d8ad` |
| student2 | `student2@tutoring.dev` | Student | `fb0e9fd2-7181-495c-922b-dd4c7e5baf2c` |
| student3 | `student3@tutoring.dev` | Student | `b5578fc3-17cd-4277-aec8-407227991e1f` |

The approved subjects **Calculus I** (`a4ce163a-0660-4262-9b5c-15f457fba955`) and **Algorithms and Data Structures** (`ef56bc00-2be6-4a12-af88-9f809de94ba5`) also already exist, along with the tutor's **approved** bond for Calculus I (`0128d64d-7818-4eec-9b8e-a55e8421ab1f`, evaluated by the admin). That lets you jump straight to step 4 of the [step-by-step guide](#how-to-use-step-by-step): create a session with few spots and a waitlist, and enroll the three students.

### Backend

Prerequisites: Java 21 and a PostgreSQL database with the schema from [`docs/database_schema_reference.md`](./docs/database_schema_reference.md) (Hibernate runs with `ddl-auto=validate` and doesn't create tables).

Set the connection environment variables:

| Variable | Example |
|---|---|
| `DB_HOST` | `ep-xxxx.us-east-2.aws.neon.tech` |
| `DB_NAME` | `neondb` |
| `DB_USERNAME` | database user |
| `DB_PASSWORD` | database password |

```bash
./mvnw spring-boot:run   # starts the API at http://localhost:8080
./mvnw test              # runs the unit tests
```

### Frontend

With the API running:

```bash
cd frontend
npm install
npm run dev              # opens at http://localhost:5173
```

More details in the [frontend README](./frontend/README.md).

## How to use (step by step)

The full system flow, from scratch to a session with a waitlist, done through the frontend at `http://localhost:5173`.

### 0. Prepare the users (first time only, on a fresh database)

> If you're running with Docker, skip this step: the users already come from the seed and the frontend is at `http://localhost:3000`.

There is no sign-up or login yet (planned for v2.0), so users are inserted directly into the database. Run this on your PostgreSQL:

```sql
INSERT INTO users (id, email, password_hash)
VALUES
    (gen_random_uuid(), 'tutor@test.com',    'no-login-in-v1'),
    (gen_random_uuid(), 'admin@test.com',    'no-login-in-v1'),
    (gen_random_uuid(), 'student1@test.com', 'no-login-in-v1'),
    (gen_random_uuid(), 'student2@test.com', 'no-login-in-v1'),
    (gen_random_uuid(), 'student3@test.com', 'no-login-in-v1')
RETURNING id, email;
```

Copy the returned UUIDs into the `TEST_USERS` list in [`frontend/src/config/testData.ts`](./frontend/src/config/testData.ts). In that same file, empty `KNOWN_SUBJECTS` and `KNOWN_BONDS`: they point to records in the author's development database.

### 1. Choose who is acting

At the top of the screen, the **"Acting as"** switcher sets which user performs each action (it stands in for the login). Switch users whenever a step asks you to.

### 2. Create a subject ("1. Subjects" tab)

1. As **any user**, suggest a subject (e.g. "Linear Algebra"). It starts as **Pending**.
2. Switch to the **admin** and click **Approve**. Only approved subjects accept tutors.

### 3. Make someone a tutor ("2. Tutoring bonds" tab)

1. As the **future tutor**, pick the approved subject and click **Request**. The bond starts as **Pending**.
2. As the **admin**, click **Approve**. From then on, that user is a tutor for *that* subject, and the admin who approved it becomes the bond's **evaluator**.

The tutor role is per subject: the same user can be a tutor in one subject and a student in another.

### 4. Create a session ("3. Sessions" tab)

1. As the **tutor** (or the bond's evaluator), pick the approved bond.
2. Set the start, end and capacity, and check **"Allow waitlist"** if you want a queue once it fills up.

### 5. Enroll students

1. Switch to a **student** and click **Enroll** on the session card. With a free spot, the enrollment is **Confirmed**.
2. Once the session is full, the next students go onto the **waitlist**, with their position in the queue (#1, #2...). Without a waitlist, the API rejects the enrollment.

### 6. Cancel an enrollment

As **the student themselves**, click **Cancel** on their enrollment. If they were confirmed, the first person in the queue is **promoted** automatically, the queue is renumbered and the promoted student gets a notification.

### 7. Cancel the session or revoke the bond

- **Cancel session:** only the bond's **evaluator** (the admin who approved it).
- **Revoke bond** (tab 2, as admin): the tutor loses the bond and **all of their future sessions** are cancelled in cascade.

### Who can do what

| Action | Who |
|---|---|
| Suggest a subject | any user |
| Approve/reject subject and bond, revoke bond | admin (in v1 there are no roles yet: the API accepts any existing user) |
| Request a tutoring bond | any user, on an approved subject |
| Create a session | the tutor (requester) or the evaluator of the approved bond |
| Cancel a session | only the bond's evaluator |
| Enroll | any user, in an active session |
| Cancel an enrollment | only the student themselves |

### v1 limitations you'll notice

- The API has no `GET` endpoints yet, so the lists show **only what the screen itself created** (stored in the browser). Changes the server makes to other records, such as waitlist promotion and the revocation cascade, don't show up on screen, even though they happen in the database.
- The **"Clear local data"** button, in the yellow notice, wipes those lists from the browser. It doesn't delete anything from the database.

**Without the frontend:** the [`http/session.http`](./http/session.http) file runs the same sessions and enrollments flow directly against the API, using IntelliJ's HTTP Client.

## Endpoints

| Method | Route | Action |
|---|---|---|
| `POST` | `/subjects` | suggest a subject |
| `PATCH` | `/subjects/{id}/approve` · `/reject` | approve / reject a subject |
| `POST` | `/tutoring-bonds` | request a tutoring bond |
| `PATCH` | `/tutoring-bonds/{id}/approve` · `/reject` · `/revoke` | approve / reject / revoke a bond |
| `POST` | `/sessions` | create a session |
| `PATCH` | `/sessions/{id}/cancel` | cancel a session |
| `POST` | `/sessions/{id}/enrollments` | enroll |
| `PATCH` | `/enrollments/{id}/cancel` | cancel an enrollment |

Response codes: **201** on create, **200** on update, **400** invalid request, **403** not allowed, **404** not found, **409** conflict with the current state. Unexpected errors return a generic 500, with no internal details. Runnable examples in [`http/`](./http).

## The problem

Study groups and academic tutoring are usually organized informally (spreadsheets, WhatsApp groups), with no real control over spots, approval of who can tutor which subject, or a history of who took part in what. This system formalizes that flow:

- Any user can propose a new subject, subject to an administrator's approval
- Users become tutors for a specific subject through a tutoring bond (approved by an admin)
- Tutors create sessions with limited spots; students enroll, with an optional waitlist and automatic promotion when a spot opens up
- Roles are dynamic: the same user can be a tutor in one subject and a student in another

## Design decisions

- **Dynamic roles**: there is no fixed "role" field on the user — the tutor role is inferred from the existence of an approved tutoring bond for that specific subject
- **Sessions reference the tutoring bond, not the user directly**, which makes the revocation cascade structurally simple: revoking a bond automatically cancels all future sessions tied to it
- **No physical deletion of data** — every destructive action (deactivating a user, revoking a bond, cancelling a session) is reversible by nature, preserving history
- **UUID primary keys** and **statuses as native PostgreSQL ENUMs**, favoring robustness over simplicity
- **userUuid received explicitly in the request body for now** (technical debt): ideally it would be extracted from an authentication token; this will be changed once security is implemented

## Domain diagrams

The domain was fully modeled before writing any code. Diagrams are available in [`docs/diagrams`](./docs/diagrams):

- **ER diagram** — database entities and their relationships
- **Class diagram** — application classes (entities + services) with attributes and methods
- **Business flows** — step by step of the 8 main system flows (subject approval, bond approval, session creation, enrollment, cancellations, revocation, user deactivation)

## Current status

✅ **Version 1.1 complete** (backend + frontend), now runnable with Docker Compose. Next step: v2.0.

- [x] Complete domain modeling (ER, class and business flow diagrams)
- [x] Database schema designed and documented
- [x] Schema implemented in PostgreSQL (Neon, cloud database)
- [x] Spring Boot project created and configured (Maven, Java 21, Web/JPA/PostgreSQL Driver/Validation/DevTools)
- [x] Application connected to the database (the app starts and connects successfully)
- [x] Lombok configured
- [x] All 6 JPA entities mapped and validated against the schema (User, Subject, TutoringBond, Session, Enrollment, Notification)
- [x] Repositories created for every entity
- [x] Complete Subject flow (Service + Controller + error handling) — tested via HTTP Client and merged into main
- [x] GlobalExceptionHandler centralizing exception handling with the appropriate HTTP codes
- [x] Complete TutoringBondService: requestTutoringBond, approveTutoringBond, rejectTutoringBond and revokeTutoringBond, with status validation and specific exceptions (TutoringBondNotFoundException, TutoringBondAlreadyExistsException, InvalidTutoringBondStatusException)
- [x] TutoringBondService unit tests (JUnit 5 + Mockito, `@ExtendWith(MockitoExtension.class)`, test names following the `should...When...` pattern), covering success and error scenarios for approve, reject and revoke
- [x] TutoringBond controller, with exceptions mapped in the GlobalExceptionHandler
- [x] Session and Enrollment exceptions created and mapped to the appropriate HTTP codes (400, 403, 404, 409): SessionNotFoundException, EnrollmentNotFoundException, SessionFullException, EnrollmentAlreadyExistsException, InvalidSessionStatusException, InvalidEnrollmentStatusException, UserWithoutPermissionException, InvalidTimeException
- [x] SessionService started: `createSession` implemented (validates that the end time is after the start before hitting the database, requires an APPROVED bond, restricted to the bond's requester or evaluator, creates the session through the entity constructor)
- [x] `createSession` unit tests
- [x] `cancelSession` (bond evaluator only) + tests
- [x] Revocation cascade: revoking a bond cancels its future active sessions (`cancelFutureSessions` with a derived query, transactional `revokeTutoringBond`) + tests
- [x] `enroll` (duplicate blocking, confirmation or waitlist, full session) + tests
- [x] `cancelEnrollment` (only the student themselves can cancel; automatic promotion of the first waitlisted student, gap-free queue reordering and a `SPOT_PROMOTED` notification to the promoted student, all inside `@Transactional`) + tests
- [x] Session and Enrollment controllers (`SessionController` for sessions and enrollments, `EnrollmentController` for cancelling enrollments, following shallow route nesting) + `http/session.http` covering the full flow against the real database
- [x] Password left out of API responses (`@JsonIgnore` on `passwordHash`, as a temporary fix until response DTOs exist)
- [x] **Version 1.0** closed with a tag and release on GitHub
- [x] Frontend in React + TypeScript + Vite, with subject, bond and session/enrollment screens, and CORS configured on the backend
- [x] Two rounds of end-to-end manual testing through the frontend, which revealed and motivated the fixes below
- [x] Subject with a duplicate name → 409 (before: 500 with SQL exposed in the response)
- [x] Generic error handling: integrity conflicts → 409, unexpected errors → generic 500 with logging, without leaking internal details
- [x] Tutoring bond only on an approved subject → 409
- [x] Create endpoints returning 201 Created
- [x] Input validation on DTOs with Bean Validation (`@NotNull`, `@NotBlank`, `@Size`, `@Min` + `@Valid`) → 400
- [x] Unit tests for `SubjectService` and `requestTutoringBond` (61 tests in total)
- [x] **Version 1.1**: frontend + validation and error handling fixes
- [x] Docker Compose to run the full system (PostgreSQL with schema and sample data, API and frontend) with a single `docker compose up`, tested from a clean clone
- [ ] Security/authentication (to extract the logged-in user instead of receiving it in the request body) — v2.0
- [ ] User roles (admin): `User` has no role today, so the "admin" actions don't check anything, and admin cancellation of enrollments was left out — v2.0
- [ ] `GET` endpoints (list subjects, bonds, sessions and enrollments) — v2.0
- [ ] Response DTOs instead of JPA entities (replacing the temporary `@JsonIgnore`) — v2.0
- [ ] Cancelling a session (or revoking the bond) should also cancel its enrollments and notify the students; today they stay `CONFIRMED`/`WAITLISTED` — v2.0
- [ ] Prevent the tutor from enrolling in their own session — v2.0
- [ ] Normalize subject names (case and accents) before checking for duplicates — v2.0
- [ ] Enrollment concurrency: two students competing for the last spot at the same time can both be confirmed (requires a database lock) — v2.0
- [ ] Separate the development/test database from the "real" database — v2.0

### Process conventions

- **Tests right after each service**: unit tests (JUnit/Mockito) are written as soon as a service is done, before moving on to the next one — they don't pile up until the end of the project
- **Docker for testers**: Docker Compose was brought forward (before authentication) so recruiters and devs can run the system without setting up a database; day-to-day development still happens on Neon

## Infrastructure stack

- **Database:** PostgreSQL hosted on [Neon](https://neon.com) (free tier), chosen so it doesn't depend on a specific machine
- **Connection configuration:** through environment variables (host, database name, user, password), never committed to the repository
- **Lombok:** used to cut down boilerplate (getters, setters, constructors) in entities and services
- **HTTP Client (IntelliJ Ultimate):** used to test the REST endpoints, with `.http` files versioned in the `http/` folder
- **Node.js + Vite:** frontend development environment
- **Docker Compose:** PostgreSQL 17 with schema and seed loaded on first startup (`docker/postgres/init/`), API in a two-stage image (Maven → JRE 21) and frontend served by nginx, which forwards `/api` to the API (same origin, no CORS)
