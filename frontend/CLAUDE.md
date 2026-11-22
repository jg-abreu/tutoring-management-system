# CLAUDE.md

Project rules for Claude Code in this repository. Machine-specific instructions live in `CLAUDE.local.md` (git-ignored).

## Project

Tutoring Management: the React client for the [Tutoring Management API](https://github.com/jg-abreu/tutoring-management-api), an academic tutoring platform where admins approve who can tutor each subject, tutors open sessions with limited spots, and students enroll, with an automatic waitlist. The business rules, database and backend tests live in the API repository; this client exists to demo them. Built as a portfolio project and treated with production seriousness.

- **Stack:** React 19, TypeScript, Vite, oxlint. No UI, state or HTTP libraries: native `fetch`, Context API and plain CSS.
- **Infrastructure:** Docker (Vite build served by nginx, which forwards `/api/` to the API), GitHub Actions CI.
- **Current version:** follows the API's v2.0. The open work lives in the issues and in the "Tutoring Management System - v2.0" GitHub Project, shared with the API.

## Workflow: spec-driven development

This project follows an AI-first, spec-driven workflow: the coding assistant drafts and implements, João approves the intent and reviews the result. Every feature or fix goes through these stages, and **no code is written before the spec and the plan are approved**.

1. **Spec.** Write the specification in the GitHub issue: the problem, the behavior on screen, the acceptance criteria and how each API error is shown. João approves or edits it.
2. **Plan.** Comment on the issue with the files to change, the API endpoints used and how the change will be checked. João approves or edits it.
3. **Tasks.** Split the approved plan into small tasks, as a checklist in the issue. Each task becomes one commit.
4. **Implement.** For each task, write the code, then run `npm run lint` and `npm run build` and check the flow in the browser against the API. Keep new code free of explanatory comments.
5. **Explain.** After each task, explain the implementation outside the code: what changed, why it was done this way and which alternative was discarded.
6. **Review.** Open the PR with `Closes #N`. CI must be green. João reviews the result against the spec and merges.

If the implementation shows that the spec is wrong or incomplete, or that the API needs a change, stop, propose the change and wait for João before continuing. API changes go through an issue in the API repository.

## Division of responsibilities

| Area | Who does the work | Claude's role |
|---|---|---|
| Specs, plans and tasks | Claude drafts, João approves | Writes them in the issue and waits for approval before coding |
| Product behavior and trade-offs | João | Presents the options with pros and cons; the final decision is João's |
| Front-end code, Docker and CI | Claude | Implements and validates (lint, build, browser check) |
| Git and GitHub (branches, commits, messages, push, PR description and opening) | Claude | Creates the branch, commits, pushes and opens the PR |
| Review and merge | João | Claude **never merges or approves a PR** |

When a decision is needed, ask **one question at a time**. Use concepts and tools João already knows; ask before introducing unfamiliar ones, including new npm dependencies.

## Project structure

```
src/
  api/          central HTTP client (client.ts) and one function per API endpoint
  components/   reusable components
  config/       temporary test data matching the API's sample data
  context/      shared state (current user, known records)
  features/     one folder per screen: subjects, bonds, sessions
  hooks/        useLocalStorage, useApiAction
  lib/          date and time zone formatting
default.conf.template   nginx config; API_URL is filled in when the container starts
.github/workflows/ci.yml   lint and build
```

## Conventions

- All HTTP calls go through `src/api/client.ts`; screens never call `fetch` directly.
- API types in `src/api/types.ts` mirror the API responses; when the API changes a response, update the type in the same task.
- Error messages shown to the user come from the API response (`ApiError`); the client does not duplicate business rules, the API decides.
- One folder per screen in `src/features/`; shared pieces move to `components/` or `hooks/` only when a second screen needs them.
- Plain CSS with the variables in `index.css`; no CSS framework.

## Git workflow

- **No direct commits to `main`**: every change, documentation included, goes through a PR.
- One branch per issue, created from an up-to-date `main` (`git pull` first), prefixed: `feature/`, `fix/`, `docs/`, `chore/`, `ci/`.
- Small commits in English using Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`, `style:`, `chore:`, `ci:`), one per task.
- Before each commit, check `git status` and `git diff` and add only the task's files; never `git add .`.
- Never add AI attribution to commits or PRs: no `Co-Authored-By` trailers, "Generated with" lines or session links.
- PR description in English: what changed, why, how to test, the definition-of-done checklist and `Closes #N`, so the GitHub Project moves the card to Done on merge.
- Work happens on two machines: `git pull` at the start of a session; commit and push everything at the end.

## Keeping CLAUDE.md and AGENTS.md in sync

`CLAUDE.md` and `AGENTS.md` hold the same project rules, one file per coding assistant. They must stay identical except for the title line and the assistant's name.

- Any change to the project rules goes into **both files in the same commit**.
- Machine-specific instructions do not go here; they live in the git-ignored `CLAUDE.local.md`.
- Before committing, check that only the title line differs (both sides are normalized to the same assistant name):

  ```bash
  n='s/Claude Code/Codex/g;s/Claude/Codex/g'; diff <(sed "$n" CLAUDE.md) <(sed "$n" AGENTS.md)
  ```

## Definition of done (per issue)

- [ ] The spec and the plan were approved by João before the code
- [ ] Every acceptance criterion in the spec was checked in the browser against the API
- [ ] `npm run lint` and `npm run build` pass and CI is green
- [ ] The README reflects new screens, commands or configuration
- [ ] No secret in the code, logs or commit
- [ ] João has reviewed the result against the spec

## Commands

```bash
npm install
npm run dev                     # dev server on http://localhost:5173 (API on http://localhost:8080)
npm run lint && npm run build
docker compose up --build       # nginx on http://localhost:3000, API on the host
```

The API runs from its own repository: `docker compose up --build` in `tutoring-management-api` starts PostgreSQL and the API with sample data.
