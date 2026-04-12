# Folio — Claude Code Instructions

Folio is a personal investment portfolio tracker: Spring Boot backend + React/TypeScript frontend, packaged as a single Docker image.

## Key docs

| File | Contents |
|------|----------|
| `docs/README.md` | Documentation structure overview, link patterns, maintenance guidelines |
| `docs/PROJECT.md` | Requirements, tech preferences, scaffolding, running locally, testing, auth, API docs, page index |
| `docs/coding-guidelines.md` | Coding conventions: backend (no Lombok, naming, testing) + frontend (Strato quirks, design) |
| `docs/data-model.md` | Database schema, entity relationships, tiny types |
| `docs/ui.md` | General UI rules: formatting, DataTable conventions, export, testing, pagination, state management |
| `docs/plan.md` | Thin frontend implementation plan: backend sorting, filtering, pagination |
| `docs/gaps.and.issues.md` | Identified gaps and their resolution status |
| `docs/pages/*.md` | Per-page specs: routes, API contracts, CSV parsing, UI layout (13 pages) |

Always read the relevant doc section before making changes that touch requirements or architecture.

## Skills

**Before writing or modifying any Java file**, read all rule files in `skills/java-clean-code/rules/` and ensure the 
generated code complies with every sub-rule. This applies to all Java work — new files, 
edits, refactors, and reviews — not only when explicitly asked for a "clean code" pass.

## Reference project

`/c/mnt/private/dev/depot` is a separate project containing reference implementations of portfolio calculations (performance, gains/losses, dividends, etc.). It is **not** used or imported by Folio, but serves as a starting point and reference for implementing equivalent functionality here. Code quality there is not authoritative — treat it as a source of ideas, not as ground truth.

