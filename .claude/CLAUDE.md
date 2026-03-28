# Folio — Claude Code Instructions

Folio is a personal investment portfolio tracker: Spring Boot backend + React/TypeScript frontend, packaged as a single Docker image.

## Key docs

| File | Contents |
|------|----------|
| `docs/PROJECT.md` | Requirements, tech preferences, scaffolding, running locally, testing, auth, API docs |
| `docs/coding-guidelines.md` | Coding conventions: backend (no Lombok, naming, testing) + frontend (Strato quirks, design) |
| `docs/data-model.md` | Database schema, entity relationships, tiny types |
| `docs/gaps.and.issues.md` | Identified gaps and their resolution status |
| `docs/frontend/pages/ui.md` | General UI rules: formatting, DataTable conventions, export, testing |
| `docs/frontend/pages/*.md` | Per-page specs: routes, API contracts, CSV parsing, UI layout |

Always read the relevant doc section before making changes that touch requirements or architecture.

## Reference project

`/c/mnt/private/dev/depot` is a separate project containing reference implementations of portfolio calculations (performance, gains/losses, dividends, etc.). It is **not** used or imported by Folio, but serves as a starting point and reference for implementing equivalent functionality here. Code quality there is not authoritative — treat it as a source of ideas, not as ground truth.

