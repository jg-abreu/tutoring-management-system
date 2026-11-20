# Usage guide

A walkthrough of the full system flow through the web interface, from an empty catalog to a session with a waitlist.

The examples assume the API runs with Docker from the [API repository](https://github.com/jg-abreu/tutoring-management-api#quick-start-docker), so the sample users already exist, and this client runs as described in the [README](../README.md#running): `http://localhost:5173` with `npm run dev` or `http://localhost:3000` with Docker. If the API uses another database, follow [step 0](#0-prepare-the-users-without-docker-only) first.

## 0. Prepare the users (without Docker only)

There is no sign-up yet, so users are inserted directly into the API's database. The hash below is the bcrypt hash of `senha123`, the same one the Docker sample data uses:

```sql
INSERT INTO users (id, email, password_hash, role)
VALUES
    (gen_random_uuid(), 'tutor@test.com',    '$2a$10$sCjVLw/LecIPwooVmD8dJuNMF.eEiHpRaq0sDXkgb6VWm7qGq1KVy', 'USER'),
    (gen_random_uuid(), 'admin@test.com',    '$2a$10$sCjVLw/LecIPwooVmD8dJuNMF.eEiHpRaq0sDXkgb6VWm7qGq1KVy', 'ADMIN'),
    (gen_random_uuid(), 'student1@test.com', '$2a$10$sCjVLw/LecIPwooVmD8dJuNMF.eEiHpRaq0sDXkgb6VWm7qGq1KVy', 'USER'),
    (gen_random_uuid(), 'student2@test.com', '$2a$10$sCjVLw/LecIPwooVmD8dJuNMF.eEiHpRaq0sDXkgb6VWm7qGq1KVy', 'USER'),
    (gen_random_uuid(), 'student3@test.com', '$2a$10$sCjVLw/LecIPwooVmD8dJuNMF.eEiHpRaq0sDXkgb6VWm7qGq1KVy', 'USER')
RETURNING id, email;
```

Copy the returned UUIDs and emails into the `TEST_USERS` list in [`src/config/testData.ts`](../src/config/testData.ts), so the login screen fills them in and the lists show names. In the same file, empty `KNOWN_SUBJECTS` and `KNOWN_BONDS`, since they point to records from the sample data.

## 1. Log in

Log in with a user's email and password (`senha123` for the sample users); the buttons under the form fill them in. The header shows who is logged in and their role (`ADMIN` or `USER`). Whenever a step asks for another user, click **Log out** and log in as that user.

The session lives only in memory: reloading the page, or a token that expires after 2 hours, brings back the login screen.

## 2. Create a subject ("Subjects" in the sidebar)

1. As **any user**, suggest a subject (e.g. "Linear Algebra"). It starts as **Pending**.
2. Switch to the **admin** and click **Approve**. Only approved subjects accept tutors.

## 3. Make someone a tutor ("Tutoring bonds" in the sidebar)

1. As the **future tutor**, pick the approved subject and click **Request**. The bond starts as **Pending**.
2. As the **admin**, click **Approve**. That user is now a tutor for *that* subject, and the admin becomes the bond's **evaluator**.

The tutor role is per subject: the same user can tutor one subject and be a student in another.

> With the Docker sample data, the tutor already has an approved bond for Calculus I, so you can skip straight to step 4.

## 4. Create a session ("Sessions" in the sidebar)

1. As the **tutor** (or the bond's evaluator), pick the approved bond.
2. Set the start, end and capacity, and check **"Allow waitlist"** to queue students once it fills up.

Tip: a capacity of 1 with the waitlist on is the quickest way to see the queue in action.

## 5. Enroll students

1. Switch to a **student** and click **Enroll** on the session. With a free spot, the enrollment is **Confirmed** and takes a seat in the seat map.
2. Once the session is full, the next students go onto the **waitlist** with their queue position (#1, #2...). Without a waitlist, the API rejects the enrollment with `409`.

## 6. Cancel an enrollment

As **the student themselves**, click **Cancel** on their enrollment. If they were confirmed, the first student in the queue is **promoted** automatically, the queue is renumbered without gaps and the promoted student gets a notification.

## 7. Cancel the session or revoke the bond

- **Cancel session:** only the bond's **evaluator** (the admin who approved it).
- **Revoke bond** (Tutoring bonds, as admin): the tutor loses the bond and **all of their future sessions** are cancelled in cascade.

## v1 limitations you'll notice

- The API has no `GET` endpoints yet, so the lists show **only what this screen created** (stored in the browser). Changes the server makes to other records, such as a waitlist promotion or the revocation cascade, happen in the database but don't show up on screen.
- The **"Clear local data"** button in the note at the top of each screen wipes those lists from the browser. It doesn't delete anything from the database.

## Without this client

In the API repository, [`http/session.http`](https://github.com/jg-abreu/tutoring-management-api/blob/main/http/session.http) runs the same sessions and enrollments flow directly against the API with IntelliJ's HTTP Client, including the error cases (`403`, `409`, `400`).
