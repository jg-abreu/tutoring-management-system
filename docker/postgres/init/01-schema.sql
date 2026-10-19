-- Schema do banco, igual ao descrito em docs/database_schema_reference.md.
-- O Postgres executa este arquivo sozinho na primeira subida do container
-- (só quando o volume de dados está vazio). O Hibernate roda com
-- ddl-auto=validate e apenas confere que tudo isto existe.

-- ENUMs
CREATE TYPE subject_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED');
CREATE TYPE bond_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'REVOKED');
CREATE TYPE session_status AS ENUM ('ACTIVE', 'CANCELLED');
CREATE TYPE enrollment_status AS ENUM ('CONFIRMED', 'WAITLISTED', 'CANCELLED', 'REJECTED');
CREATE TYPE notification_event_type AS ENUM (
    'SESSION_CANCELLED',
    'SPOT_PROMOTED',
    'BOND_APPROVED',
    'BOND_REJECTED',
    'BOND_REVOKED',
    'SUBJECT_STATUS_CHANGED',
    'USER_DEACTIVATED'
);

-- Tabelas, na ordem das foreign keys
CREATE TABLE users (
    id            UUID PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(60)  NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE subject (
    id           UUID PRIMARY KEY,
    name         VARCHAR(255)   NOT NULL UNIQUE,
    status       subject_status NOT NULL DEFAULT 'PENDING',
    creator_id   UUID           NOT NULL REFERENCES users (id),
    evaluator_id UUID           REFERENCES users (id),
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TABLE tutoring_bond (
    id           UUID PRIMARY KEY,
    status       bond_status NOT NULL DEFAULT 'PENDING',
    requester_id UUID        NOT NULL REFERENCES users (id),
    evaluator_id UUID        REFERENCES users (id),
    subject_id   UUID        NOT NULL REFERENCES subject (id),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE session (
    id              UUID PRIMARY KEY,
    start_time      TIMESTAMPTZ    NOT NULL,
    end_time        TIMESTAMPTZ    NOT NULL,
    spots           INT            NOT NULL CHECK (spots >= 1),
    allows_waitlist BOOLEAN        NOT NULL DEFAULT FALSE,
    status          session_status NOT NULL DEFAULT 'ACTIVE',
    bond_id         UUID           NOT NULL REFERENCES tutoring_bond (id),
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CHECK (end_time > start_time)
);

CREATE TABLE enrollment (
    id             UUID PRIMARY KEY,
    status         enrollment_status NOT NULL,
    queue_position INT               CHECK (queue_position >= 1),
    session_id     UUID              NOT NULL REFERENCES session (id),
    student_id     UUID              NOT NULL REFERENCES users (id),
    created_at     TIMESTAMPTZ       NOT NULL DEFAULT NOW()
);

CREATE TABLE notification (
    id          UUID PRIMARY KEY,
    event_type  notification_event_type NOT NULL,
    message     TEXT                    NOT NULL,
    receiver_id UUID                    NOT NULL REFERENCES users (id),
    created_at  TIMESTAMPTZ             NOT NULL DEFAULT NOW()
);

-- Índices
CREATE INDEX idx_enrollment_session_id ON enrollment (session_id);
CREATE INDEX idx_enrollment_student_id ON enrollment (student_id);
CREATE INDEX idx_tutoring_bond_subject_id ON tutoring_bond (subject_id);
CREATE INDEX idx_notification_receiver_id ON notification (receiver_id);

-- Um mesmo monitor não pode ter dois vínculos "vivos" (PENDING ou APPROVED)
-- para a mesma matéria; rejeitados e revogados podem se repetir.
CREATE UNIQUE INDEX uq_tutoring_bond_active_requester_subject
    ON tutoring_bond (requester_id, subject_id)
    WHERE status IN ('PENDING', 'APPROVED');
