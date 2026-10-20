-- Sample data for anyone running the system with Docker.
-- The UUIDs are fixed and match frontend/src/config/testData.ts
-- and the test users table in the README. There is no authentication yet:
-- the API receives the user's UUID in the request body.
-- Everyone's password is "senha123" (bcrypt hash), kept for when login exists.

INSERT INTO users (id, email, password_hash) VALUES
    ('2c53ba13-53cc-4c79-a4f2-31cfbcceb898', 'admin@tutoring.dev',    '$2a$10$sCjVLw/LecIPwooVmD8dJuNMF.eEiHpRaq0sDXkgb6VWm7qGq1KVy'),
    ('899b90d5-673e-4137-abd2-1606d73f9b0e', 'tutor@tutoring.dev',    '$2a$10$sCjVLw/LecIPwooVmD8dJuNMF.eEiHpRaq0sDXkgb6VWm7qGq1KVy'),
    ('451c540f-a05f-4ead-a6d3-89528391d8ad', 'student1@tutoring.dev', '$2a$10$sCjVLw/LecIPwooVmD8dJuNMF.eEiHpRaq0sDXkgb6VWm7qGq1KVy'),
    ('fb0e9fd2-7181-495c-922b-dd4c7e5baf2c', 'student2@tutoring.dev', '$2a$10$sCjVLw/LecIPwooVmD8dJuNMF.eEiHpRaq0sDXkgb6VWm7qGq1KVy'),
    ('b5578fc3-17cd-4277-aec8-407227991e1f', 'student3@tutoring.dev', '$2a$10$sCjVLw/LecIPwooVmD8dJuNMF.eEiHpRaq0sDXkgb6VWm7qGq1KVy');

-- Subjects already approved by the admin (suggested by the tutor)
INSERT INTO subject (id, name, status, creator_id, evaluator_id) VALUES
    ('a4ce163a-0660-4262-9b5c-15f457fba955', 'Calculus I',
        'APPROVED', '899b90d5-673e-4137-abd2-1606d73f9b0e', '2c53ba13-53cc-4c79-a4f2-31cfbcceb898'),
    ('ef56bc00-2be6-4a12-af88-9f809de94ba5', 'Algorithms and Data Structures',
        'APPROVED', '899b90d5-673e-4137-abd2-1606d73f9b0e', '2c53ba13-53cc-4c79-a4f2-31cfbcceb898');

-- Approved bond: the tutor can create Calculus I sessions
INSERT INTO tutoring_bond (id, status, requester_id, evaluator_id, subject_id) VALUES
    ('0128d64d-7818-4eec-9b8e-a55e8421ab1f', 'APPROVED',
        '899b90d5-673e-4137-abd2-1606d73f9b0e', '2c53ba13-53cc-4c79-a4f2-31cfbcceb898',
        'a4ce163a-0660-4262-9b5c-15f457fba955');
