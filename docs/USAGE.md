# Usage guide

A walkthrough of the full system flow through the web interface, from an empty catalog to a session with a waitlist.

The examples assume the Docker setup from the [README](../README.md#quick-start-docker), where the frontend runs at `http://localhost:3000` and the sample users already exist. If you run the frontend with `npm run dev`, it's at `http://localhost:5173` instead, and you need to follow [step 0](#0-prepare-the-users-without-docker-only) first.

## 0. Prepare the users (without Docker only)

There is no sign-up or login yet (planned for v2.0), so users are inserted directly into the database:

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

Copy the returned UUIDs into the `TEST_USERS` list in [`frontend/src/config/testData.ts`](../frontend/src/config/testData.ts). In the same file, empty `KNOWN_SUBJECTS` and `KNOWN_BONDS`, since they point to records from the sample data.

## 1. Choose who is acting

The **"Acting as"** switcher at the top of the screen sets which user performs each action. It stands in for the login, so switch users whenever a step asks you to.

## 2. Create a subject ("1. Subjects" tab)

1. As **any user**, suggest a subject (e.g. "Linear Algebra"). It starts as **Pending**.
2. Switch to the **admin** and click **Approve**. Only approved subjects accept tutors.

## 3. Make someone a tutor ("2. Tutoring bonds" tab)

1. As the **future tutor**, pick the approved subject and click **Request**. The bond starts as **Pending**.
2. As the **admin**, click **Approve**. That user is now a tutor for *that* subject, and the admin becomes the bond's **evaluator**.

The tutor role is per subject: the same user can tutor one subject and be a student in another.

> With the Docker sample data, the tutor already has an approved bond for Calculus I, so you can skip straight to step 4.

## 4. Create a session ("3. Sessions" tab)

1. As the **tutor** (or the bond's evaluator), pick the approved bond.
2. Set the start, end and capacity, and check **"Allow waitlist"** to queue students once it fills up.

Tip: a capacity of 1 with the waitlist on is the quickest way to see the queue in action.

## 5. Enroll students

1. Switch to a **student** and click **Enroll** on the session card. With a free spot, the enrollment is **Confirmed**.
2. Once the session is full, the next students go onto the **waitlist** with their queue position (#1, #2...). Without a waitlist, the API rejects the enrollment with `409`.

## 6. Cancel an enrollment

As **the student themselves**, click **Cancel** on their enrollment. If they were confirmed, the first student in the queue is **promoted** automatically, the queue is renumbered without gaps and the promoted student gets a notification.

## 7. Cancel the session or revoke the bond

- **Cancel session:** only the bond's **evaluator** (the admin who approved it).
- **Revoke bond** (tab 2, as admin): the tutor loses the bond and **all of their future sessions** are cancelled in cascade.

## v1 limitations you'll notice

- The API has no `GET` endpoints yet, so the lists show **only what this screen created** (stored in the browser). Changes the server makes to other records, such as a waitlist promotion or the revocation cascade, happen in the database but don't show up on screen.
- The **"Clear local data"** button in the yellow notice wipes those lists from the browser. It doesn't delete anything from the database.

## Without the frontend

[`http/session.http`](../http/session.http) runs the same sessions and enrollments flow directly against the API with IntelliJ's HTTP Client, including the error cases (`403`, `409`, `400`).
