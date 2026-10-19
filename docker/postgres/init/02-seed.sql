-- Dados de exemplo para quem roda o sistema com Docker.
-- Os UUIDs são fixos e são os mesmos de frontend/src/config/testData.ts
-- e da tabela de usuários de teste do README. Ainda não há autenticação:
-- a API recebe o UUID do usuário no corpo da requisição.
-- A senha de todos é "senha123" (hash bcrypt), guardada para quando o login existir.

INSERT INTO users (id, email, password_hash) VALUES
    ('2c53ba13-53cc-4c79-a4f2-31cfbcceb898', 'admin@monitoria.dev',   '$2a$10$sCjVLw/LecIPwooVmD8dJuNMF.eEiHpRaq0sDXkgb6VWm7qGq1KVy'),
    ('899b90d5-673e-4137-abd2-1606d73f9b0e', 'monitor@monitoria.dev', '$2a$10$sCjVLw/LecIPwooVmD8dJuNMF.eEiHpRaq0sDXkgb6VWm7qGq1KVy'),
    ('451c540f-a05f-4ead-a6d3-89528391d8ad', 'aluno1@monitoria.dev',  '$2a$10$sCjVLw/LecIPwooVmD8dJuNMF.eEiHpRaq0sDXkgb6VWm7qGq1KVy'),
    ('fb0e9fd2-7181-495c-922b-dd4c7e5baf2c', 'aluno2@monitoria.dev',  '$2a$10$sCjVLw/LecIPwooVmD8dJuNMF.eEiHpRaq0sDXkgb6VWm7qGq1KVy'),
    ('b5578fc3-17cd-4277-aec8-407227991e1f', 'aluno3@monitoria.dev',  '$2a$10$sCjVLw/LecIPwooVmD8dJuNMF.eEiHpRaq0sDXkgb6VWm7qGq1KVy');

-- Matérias já aprovadas pelo admin (sugeridas pelo monitor)
INSERT INTO subject (id, name, status, creator_id, evaluator_id) VALUES
    ('a4ce163a-0660-4262-9b5c-15f457fba955', 'Cálculo I',
        'APPROVED', '899b90d5-673e-4137-abd2-1606d73f9b0e', '2c53ba13-53cc-4c79-a4f2-31cfbcceb898'),
    ('ef56bc00-2be6-4a12-af88-9f809de94ba5', 'Algoritmos e Estruturas de Dados',
        'APPROVED', '899b90d5-673e-4137-abd2-1606d73f9b0e', '2c53ba13-53cc-4c79-a4f2-31cfbcceb898');

-- Vínculo aprovado: o monitor pode criar sessões de Cálculo I
INSERT INTO tutoring_bond (id, status, requester_id, evaluator_id, subject_id) VALUES
    ('0128d64d-7818-4eec-9b8e-a55e8421ab1f', 'APPROVED',
        '899b90d5-673e-4137-abd2-1606d73f9b0e', '2c53ba13-53cc-4c79-a4f2-31cfbcceb898',
        'a4ce163a-0660-4262-9b5c-15f457fba955');
