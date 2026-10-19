# Sistema de Gestão de Monitoria Acadêmica

Sistema para gerenciar grupos de estudo e sessões de monitoria acadêmica, com controle de vínculos de monitoria aprovados por administrador, sessões com vagas limitadas e lista de espera, e cascata automática de cancelamento quando um vínculo é revogado. API REST em Spring Boot + interface web em React.

Projeto pessoal de portfólio, construído com foco em modelagem de domínio real, regras de negócio com máquinas de estado e autorização baseada em contexto (não em papéis fixos de usuário).

## Stack

**Backend**
- Java 21
- Spring Boot (Web, Data JPA, Validation)
- Hibernate / JPA
- PostgreSQL
- Lombok
- JUnit 5 + Mockito

**Frontend** (pasta [`frontend`](./frontend))
- React 19 + TypeScript
- Vite

## Como rodar

### Backend

Pré-requisitos: Java 21 e um banco PostgreSQL com o schema de [`docs/database_schema_reference.md`](./docs/database_schema_reference.md) (o Hibernate roda com `ddl-auto=validate` e não cria tabelas).

Defina as variáveis de ambiente da conexão:

| Variável | Exemplo |
|---|---|
| `DB_HOST` | `ep-xxxx.us-east-2.aws.neon.tech` |
| `DB_NAME` | `neondb` |
| `DB_USERNAME` | usuário do banco |
| `DB_PASSWORD` | senha do banco |

```bash
./mvnw spring-boot:run   # sobe a API em http://localhost:8080
./mvnw test              # roda os testes de unidade
```

### Frontend

Com a API rodando:

```bash
cd frontend
npm install
npm run dev              # abre em http://localhost:5173
```

Detalhes no [README do frontend](./frontend/README.md).

## Endpoints

| Método | Rota | Ação |
|---|---|---|
| `POST` | `/subjects` | sugerir matéria |
| `PATCH` | `/subjects/{id}/approve` · `/reject` | aprovar / rejeitar matéria |
| `POST` | `/tutoring-bonds` | pedir vínculo de monitoria |
| `PATCH` | `/tutoring-bonds/{id}/approve` · `/reject` · `/revoke` | aprovar / rejeitar / revogar vínculo |
| `POST` | `/sessions` | criar sessão |
| `PATCH` | `/sessions/{id}/cancel` | cancelar sessão |
| `POST` | `/sessions/{id}/enrollments` | matricular |
| `PATCH` | `/enrollments/{id}/cancel` | cancelar matrícula |

Códigos de resposta: **201** ao criar, **200** ao alterar, **400** requisição inválida, **403** sem permissão, **404** não encontrado, **409** conflito com o estado atual. Erros inesperados devolvem um 500 genérico, sem detalhes internos. Exemplos executáveis em [`http/`](./http).

## O problema

Grupos de estudo e monitorias acadêmicas costumam ser organizados de forma informal (planilhas, grupos de WhatsApp), sem controle real de vagas, aprovação de quem pode dar monitoria em qual matéria, ou histórico de quem participou do quê. Este sistema formaliza esse fluxo:

- Qualquer usuário pode propor uma matéria nova, sujeita à aprovação de um administrador
- Usuários se tornam monitores de uma matéria específica através de um vínculo de monitoria (aprovado por admin)
- Monitores criam sessões com vagas limitadas; estudantes se matriculam, com lista de espera opcional e promoção automática quando uma vaga é liberada
- Papéis são dinâmicos: o mesmo usuário pode ser monitor em uma matéria e estudante em outra

## Decisões de design

- **Papéis dinâmicos**: não existe um campo fixo de "role" no usuário — o papel de monitor é inferido pela existência de um vínculo de monitoria aprovado para aquela matéria específica
- **Sessões referenciam o vínculo de monitoria, não o usuário diretamente**, o que torna a cascata de revogação estruturalmente simples: revogar um vínculo cancela automaticamente todas as sessões futuras associadas a ele
- **Nenhuma exclusão física de dados** — todas as ações destrutivas (desativar usuário, revogar vínculo, cancelar sessão) são reversíveis por natureza, preservando histórico
- **Chaves primárias em UUID** e **status representados como ENUM nativo do PostgreSQL**, priorizando robustez sobre simplicidade
- **userUuid recebido explicitamente no corpo das requisições por enquanto** (pendência técnica): o ideal seria extraído de um token de autenticação; isso será ajustado quando a segurança for implementada

## Diagramas de domínio

O domínio foi modelado por completo antes de qualquer linha de código. Diagramas disponíveis em [`docs/diagrams`](./docs/diagrams):

- **Diagrama ER** — entidades do banco de dados e seus relacionamentos
- **Diagrama de classes** — classes da aplicação (entidades + services) com atributos e métodos
- **Fluxos de negócio** — passo a passo dos 8 principais fluxos do sistema (aprovação de matéria, aprovação de vínculo, criação de sessão, matrícula, cancelamentos, revogação, desativação de usuário)

## Status atual

✅ **Versão 1.1 concluída** (backend + frontend). Próximo passo: v2.0.

- [x] Modelagem de domínio completa (diagramas ER, classes, fluxos de negócio)
- [x] Schema de banco de dados desenhado e documentado
- [x] Schema implementado em PostgreSQL (Neon, banco na nuvem)
- [x] Projeto Spring Boot criado e configurado (Maven, Java 21, Web/JPA/PostgreSQL Driver/Validation/DevTools)
- [x] Conexão da aplicação com o banco funcionando (aplicação sobe e conecta com sucesso)
- [x] Lombok configurado
- [x] Todas as 6 entidades JPA mapeadas e validadas contra o schema (User, Subject, TutoringBond, Session, Enrollment, Notification)
- [x] Repositories criados para todas as entidades
- [x] Fluxo de Subject completo (Service + Controller + tratamento de erros) — testado via HTTP Client e mesclado na main
- [x] GlobalExceptionHandler centralizando o tratamento de exceções com códigos HTTP apropriados
- [x] TutoringBondService completo: requestTutoringBond, approveTutoringBond, rejectTutoringBond e revokeTutoringBond, com validação de status e exceções específicas (TutoringBondNotFoundException, TutoringBondAlreadyExistsException, InvalidTutoringBondStatusException)
- [x] Testes de unidade do TutoringBondService (JUnit 5 + Mockito, `@ExtendWith(MockitoExtension.class)`, nomes de teste em inglês no padrão `should...When...`), cobrindo cenários de sucesso e de erro de approve, reject e revoke
- [x] Controller de TutoringBond, com exceções mapeadas no GlobalExceptionHandler
- [x] Exceções de Session e Enrollment criadas e mapeadas com códigos HTTP apropriados (400, 403, 404, 409): SessionNotFoundException, EnrollmentNotFoundException, SessionFullException, EnrollmentAlreadyExistsException, InvalidSessionStatusException, InvalidEnrollmentStatusException, UserWithoutPermissionException, InvalidTimeException
- [x] SessionService iniciado: `createSession` implementado (valida horário de fim após o início antes de consultar o banco, exige vínculo APPROVED, restrito ao requester ou evaluator do vínculo, cria a sessão via construtor da entidade)
- [x] Testes de unidade do `createSession`
- [x] `cancelSession` (apenas o avaliador do vínculo) + testes
- [x] Cascata de revogação: revogar um vínculo cancela suas sessões futuras e ativas (`cancelFutureSessions` com derived query, `revokeTutoringBond` transacional) + testes
- [x] `enroll` (bloqueio de duplicidade, confirmação ou lista de espera, sessão lotada) + testes
- [x] `cancelEnrollment` (só o próprio aluno cancela; promoção automática do primeiro da lista de espera, reordenação da fila sem buracos e notificação `SPOT_PROMOTED` ao promovido, tudo em `@Transactional`) + testes
- [x] Controllers de Session e Enrollment (`SessionController` para sessões e matrículas, `EnrollmentController` para cancelar matrícula, seguindo aninhamento raso de rotas) + `http/session.http` cobrindo o fluxo completo contra o banco real
- [x] Senha fora das respostas da API (`@JsonIgnore` no `passwordHash`, como solução provisória até os DTOs de resposta)
- [x] **Versão 1.0** fechada com tag e release no GitHub
- [x] Frontend em React + TypeScript + Vite, com telas de matérias, vínculos e sessões/matrículas, e CORS configurado no backend
- [x] Dois ciclos de testes manuais de ponta a ponta pelo frontend, que revelaram e motivaram as correções abaixo
- [x] Matéria com nome repetido → 409 (antes: 500 com SQL exposto na resposta)
- [x] Tratamento genérico de erros: conflitos de integridade → 409, erros inesperados → 500 genérico com log, sem vazar detalhes internos
- [x] Vínculo de monitoria só em matéria aprovada → 409
- [x] Endpoints de criação devolvendo 201 Created
- [x] Validação de entrada nos DTOs com Bean Validation (`@NotNull`, `@NotBlank`, `@Size`, `@Min` + `@Valid`) → 400
- [x] Testes de unidade do `SubjectService` e do `requestTutoringBond` (61 testes no total)
- [x] **Versão 1.1**: frontend + correções de validação e tratamento de erros
- [ ] Segurança/autenticação (para extrair o usuário logado em vez de recebê-lo no corpo da requisição) — v2.0
- [ ] Papéis de usuário (admin): hoje `User` não tem papel, então as ações "de admin" não verificam nada e o cancelamento de matrícula por admin ficou de fora — v2.0
- [ ] Endpoints `GET` (listar matérias, vínculos, sessões e matrículas) — v2.0
- [ ] DTOs de resposta no lugar das entidades JPA (substituem o `@JsonIgnore` provisório) — v2.0
- [ ] Cancelar uma sessão (ou revogar o vínculo) deve cancelar também as matrículas dela e notificar os alunos; hoje elas ficam `CONFIRMED`/`WAITLISTED` — v2.0
- [ ] Impedir que o monitor se matricule na própria sessão — v2.0
- [ ] Normalizar nomes de matéria (maiúsculas e acentos) antes de checar duplicidade — v2.0
- [ ] Concorrência na matrícula: dois alunos disputando a última vaga ao mesmo tempo podem ser confirmados juntos (exige lock no banco) — v2.0
- [ ] Separar banco de desenvolvimento/testes do banco "real" — v2.0
- [ ] Dockerização da aplicação (planejada para o final do projeto, junto com autenticação) — v2.0

### Convenções de processo adotadas

- **Testes logo após cada service**: testes de unidade (JUnit/Mockito) são escritos assim que um service é concluído, antes de avançar para o próximo — não ficam acumulados para o final do projeto
- **Docker no final**: a conteinerização da aplicação fica para uma das últimas etapas, depois de toda a lógica de negócio, autenticação e testes estarem prontos

## Stack de infraestrutura

- **Banco de dados:** PostgreSQL hospedado no [Neon](https://neon.com) (plano gratuito), escolhido para não depender de uma máquina específica
- **Configuração de conexão:** via variáveis de ambiente (host, nome do banco, usuário, senha), nunca commitadas no repositório
- **Lombok:** usado para reduzir código repetitivo (getters, setters, construtores) nas entidades e services
- **HTTP Client (IntelliJ Ultimate):** usado para testar os endpoints REST, com arquivos `.http` versionados na pasta `http/`
- **Node.js + Vite:** ambiente de desenvolvimento do frontend
