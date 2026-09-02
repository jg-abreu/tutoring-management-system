# Schema do banco — Sistema de Monitoria Acadêmica

Minhas anotações do schema, feitas antes de escrever qualquer linha de Java. Fui montando isso com calma, testando cada CREATE TABLE direto no pgAdmin antes de seguir pra próxima tabela.

## Decisões que tomei antes de começar

- **PK em UUID**, não BIGSERIAL. Dá mais trabalho de configurar depois no Hibernate, mas é o padrão que mais aparece em vaga de emprego, então valeu a pena praticar.
- **Status como ENUM nativo do Postgres**, não VARCHAR solto. Mais rígido de mudar depois, mas o banco garante sozinho que ninguém insere um status inválido — gosto mais dessa robustez.
- Toda tabela leva `created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`. Não fiquei repetindo isso tabela por tabela lá embaixo.
- Nada de deletar de verdade — cancelar, revogar, desativar. Ninguém some do banco.

---

## users

Tive que renomear de `user` pra `users` porque `user` é palavra reservada no Postgres (dei de cara com isso na hora de criar).

| Coluna | Tipo | Restrições |
|---|---|---|
| id | UUID | PK |
| email | VARCHAR(255) | UNIQUE, NOT NULL |
| password_hash | VARCHAR(60) | NOT NULL |
| active | BOOLEAN | NOT NULL, DEFAULT TRUE |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() |

`password_hash` guarda o hash (bcrypt), não a senha em texto puro — nunca ia guardar senha direto, óbvio, mas bom deixar anotado o porquê do tamanho 60.

---

## subject

| Coluna | Tipo | Restrições |
|---|---|---|
| id | UUID | PK |
| name | VARCHAR(255) | UNIQUE, NOT NULL |
| status | subject_status (PENDING, APPROVED, REJECTED) | NOT NULL, DEFAULT 'PENDING' |
| creator_id | UUID | FK → users.id, NOT NULL |
| evaluator_id | UUID | FK → users.id, pode ser nulo |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() |

`evaluator_id` fica nulo porque no momento em que a matéria é criada (status PENDING) ainda não tem ninguém avaliando ela. Só preenche quando um admin aprova ou rejeita.

---

## tutoring_bond

| Coluna | Tipo | Restrições |
|---|---|---|
| id | UUID | PK |
| status | bond_status (PENDING, APPROVED, REJECTED, REVOKED) | NOT NULL, DEFAULT 'PENDING' |
| requester_id | UUID | FK → users.id, NOT NULL |
| evaluator_id | UUID | FK → users.id, pode ser nulo |
| subject_id | UUID | FK → subject.id, NOT NULL |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() |

Regra extra que criei como índice único parcial:

```
requester_id + subject_id só pode se repetir se o status NÃO for PENDING ou APPROVED
```

Isso evita alguém pedir dois vínculos pendentes (ou já aprovados) pra mesma matéria — tipo, se rejeitaram meu pedido, posso tentar de novo depois, mas não posso ter dois pedidos "vivos" ao mesmo tempo pra mesma matéria.

Percebi que essa tabela resolve sozinha o muitos-pra-muitos entre usuário (monitor) e matéria — não precisei criar uma tabela de associação separada, porque o vínculo já tem vida própria (status, quem avaliou, etc).

---

## session

| Coluna | Tipo | Restrições |
|---|---|---|
| id | UUID | PK |
| start_time | TIMESTAMPTZ | NOT NULL |
| end_time | TIMESTAMPTZ | NOT NULL |
| spots | INT | NOT NULL, CHECK (spots >= 1) |
| allows_waitlist | BOOLEAN | NOT NULL, DEFAULT FALSE |
| status | session_status (ACTIVE, CANCELLED) | NOT NULL, DEFAULT 'ACTIVE' |
| bond_id | UUID | FK → tutoring_bond.id, NOT NULL |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() |

Tem um `CHECK (end_time > start_time)` também, pra sessão não terminar antes de começar.

Ponto importante: `session` aponta pro `bond_id`, não direto pro usuário monitor. Foi proposital — assim, se um vínculo é revogado, dá pra achar todas as sessões afetadas só filtrando por `bond_id`, sem precisar ficar cruzando informação de usuário com matéria toda vez.

---

## enrollment

| Coluna | Tipo | Restrições |
|---|---|---|
| id | UUID | PK |
| status | enrollment_status (CONFIRMED, WAITLISTED, CANCELLED, REJECTED) | NOT NULL |
| queue_position | INT | pode ser nulo, CHECK (queue_position >= 1) |
| session_id | UUID | FK → session.id, NOT NULL |
| student_id | UUID | FK → users.id, NOT NULL |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() |

`status` aqui **não tem DEFAULT** de propósito — decidir se a matrícula nasce CONFIRMED ou WAITLISTED depende de contar quantas vagas já foram ocupadas, e isso o banco não sabe fazer sozinho. Vai ser o `EnrollmentService` que decide isso na hora de criar.

`queue_position` só importa quando é WAITLISTED. Nos outros casos fica nulo — e o CHECK `>= 1` deixa passar nulo numa boa, porque comparação com NULL vira "desconhecido" e o Postgres não bloqueia isso (isso me surpreendeu quando aprendi).

---

## notification

| Coluna | Tipo | Restrições |
|---|---|---|
| id | UUID | PK |
| event_type | notification_event_type (SESSION_CANCELLED, SPOT_PROMOTED, BOND_APPROVED, BOND_REJECTED, BOND_REVOKED, SUBJECT_STATUS_CHANGED, USER_DEACTIVATED) | NOT NULL |
| message | TEXT | NOT NULL |
| receiver_id | UUID | FK → users.id, NOT NULL |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() |

Errei na hora de criar esse enum — esqueci o `SESSION_CANCELLED` na primeira tentativa e tive que usar `ALTER TYPE ... ADD VALUE` pra corrigir depois. Bom saber que dá pra consertar sem apagar tudo de novo.

---

## Índices que criei além dos automáticos

O Postgres cria índice sozinho só pra PK e UNIQUE — pra foreign key não, isso é manual. Botei índice nas colunas que imagino que vão ser mais consultadas no dia a dia:

- `enrollment.session_id` — pra listar quem tá matriculado numa sessão
- `enrollment.student_id` — pra listar o histórico de um estudante
- `tutoring_bond.subject_id` — pra listar os monitores de uma matéria
- `notification.receiver_id` — pra listar as notificações de alguém

Deixei de fora os outros FKs (tipo `session.bond_id`, `subject.creator_id`) porque não imagino eles sendo consultados com tanta frequência. Se sentir lentidão depois, sempre dá pra adicionar.

---

## Todas as foreign keys, resumidas

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

## Próximo passo

Configurar o projeto Spring Boot e criar as entidades JPA batendo com esse schema.
