# Database schema — Academic Tutoring Management System

My notes on the schema, written before a single line of Java. I built this up slowly, testing each CREATE TABLE directly in pgAdmin before moving on to the next table.

## Decisions I made before starting

- **UUID primary keys**, not BIGSERIAL. It takes more work to set up in Hibernate later, but it's the pattern that shows up most in job postings, so it was worth practicing.
- **Status as a native Postgres ENUM**, not a loose VARCHAR. It's harder to change later, but the database guarantees on its own that nobody inserts an invalid status — I prefer that robustness.
- Every table gets `created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`. I didn't repeat this table by table below.
- No real deletes — cancel, revoke, deactivate. Nobody disappears from the database.

---

## users

I had to rename it from `user` to `users` because `user` is a reserved word in Postgres (I ran straight into that when creating it).

| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK |
| email | VARCHAR(255) | UNIQUE, NOT NULL |
| password_hash | VARCHAR(60) | NOT NULL |
| active | BOOLEAN | NOT NULL, DEFAULT TRUE |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() |

`password_hash` stores the hash (bcrypt), not the plain-text password — I'd obviously never store a password directly, but it's good to note why the length is 60.

---

## subject

| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK |
| name | VARCHAR(255) | UNIQUE, NOT NULL |
| status | subject_status (PENDING, APPROVED, REJECTED) | NOT NULL, DEFAULT 'PENDING' |
| creator_id | UUID | FK → users.id, NOT NULL |
| evaluator_id | UUID | FK → users.id, nullable |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() |

`evaluator_id` is nullable because when a subject is created (status PENDING) nobody has evaluated it yet. It only gets filled in when an admin approves or rejects it.

---

## tutoring_bond

| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK |
| status | bond_status (PENDING, APPROVED, REJECTED, REVOKED) | NOT NULL, DEFAULT 'PENDING' |
| requester_id | UUID | FK → users.id, NOT NULL |
| evaluator_id | UUID | FK → users.id, nullable |
| subject_id | UUID | FK → subject.id, NOT NULL |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() |

An extra rule I created as a partial unique index:

```
requester_id + subject_id can only repeat if the status is NOT PENDING or APPROVED
```

This keeps someone from requesting two pending (or already approved) bonds for the same subject — say my request was rejected, I can try again later, but I can't have two "live" requests for the same subject at the same time.

I noticed this table solves the many-to-many between user (tutor) and subject on its own — I didn't need a separate join table, because the bond has a life of its own (status, who evaluated it, etc.).

---

## session

| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK |
| start_time | TIMESTAMPTZ | NOT NULL |
| end_time | TIMESTAMPTZ | NOT NULL |
| spots | INT | NOT NULL, CHECK (spots >= 1) |
| allows_waitlist | BOOLEAN | NOT NULL, DEFAULT FALSE |
| status | session_status (ACTIVE, CANCELLED) | NOT NULL, DEFAULT 'ACTIVE' |
| bond_id | UUID | FK → tutoring_bond.id, NOT NULL |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() |

There's also a `CHECK (end_time > start_time)`, so a session can't end before it starts.

Important point: `session` points to `bond_id`, not directly to the tutor user. That was on purpose — this way, if a bond is revoked, I can find every affected session just by filtering on `bond_id`, without cross-referencing user and subject every time.

---

## enrollment

| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK |
| status | enrollment_status (CONFIRMED, WAITLISTED, CANCELLED, REJECTED) | NOT NULL |
| queue_position | INT | nullable, CHECK (queue_position >= 1) |
| session_id | UUID | FK → session.id, NOT NULL |
| student_id | UUID | FK → users.id, NOT NULL |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() |

`status` here **has no DEFAULT** on purpose — deciding whether an enrollment starts as CONFIRMED or WAITLISTED depends on counting how many spots are already taken, and the database can't do that on its own. The `EnrollmentService` decides it at creation time.

`queue_position` only matters when the status is WAITLISTED. Otherwise it stays null — and the `>= 1` CHECK lets null through just fine, because a comparison with NULL evaluates to "unknown" and Postgres doesn't block it (that surprised me when I learned it).

---

## notification

| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK |
| event_type | notification_event_type (SESSION_CANCELLED, SPOT_PROMOTED, BOND_APPROVED, BOND_REJECTED, BOND_REVOKED, SUBJECT_STATUS_CHANGED, USER_DEACTIVATED) | NOT NULL |
| message | TEXT | NOT NULL |
| receiver_id | UUID | FK → users.id, NOT NULL |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() |

I made a mistake creating this enum — I forgot `SESSION_CANCELLED` on the first try and had to use `ALTER TYPE ... ADD VALUE` to fix it afterwards. Good to know it can be fixed without dropping everything.

---

## Indexes I created beyond the automatic ones

Postgres only creates indexes on its own for PKs and UNIQUE constraints — not for foreign keys, those are manual. I added indexes on the columns I expect to be queried most day to day:

- `enrollment.session_id` — to list who is enrolled in a session
- `enrollment.student_id` — to list a student's history
- `tutoring_bond.subject_id` — to list the tutors of a subject
- `notification.receiver_id` — to list someone's notifications

I left out the other FKs (like `session.bond_id`, `subject.creator_id`) because I don't see them being queried that often. If things get slow later, I can always add them.

---

## All foreign keys, summarized

- `subject.creator_id` → `users.id`
- `subject.evaluator_id` → `users.id`
- `tutoring_bond.requester_id` → `users.id`
- `tutoring_bond.evaluator_id` → `users.id`
- `tutoring_bond.subject_id` → `subject.id`
- `session.bond_id` → `tutoring_bond.id`
- `enrollment.session_id` → `session.id`
- `enrollment.student_id` → `users.id`
- `notification.receiver_id` → `users.id`

---

## Next step

Set up the Spring Boot project and create the JPA entities matching this schema.
