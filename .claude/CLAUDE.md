# Folio — Claude Code Instructions

Folio is a personal investment portfolio tracker: Spring Boot backend + React/TypeScript frontend, packaged as a single Docker image.

## Key docs

| File | Contents |
|------|----------|
| `docs/PROJECT.md` | Full requirements, data model, use cases, tech preferences |
| `docs/plan.md` | Phase-by-phase implementation plan with detailed specs |
| `docs/gaps.and.issues.md` | All identified gaps and their resolution status |

Always read the relevant doc section before making changes that touch requirements or architecture.

## Reference project

`/c/mnt/private/dev/depot` is a separate project containing reference implementations of portfolio calculations (performance, gains/losses, dividends, etc.). It is **not** used or imported by Folio, but serves as a starting point and reference for implementing equivalent functionality here. Code quality there is not authoritative — treat it as a source of ideas, not as ground truth.

## Project structure

```
backend/   — Spring Boot (Gradle), Java 21
frontend/  — React 18 + TypeScript, Vite, Strato design system
docs/      — Requirements and implementation docs
```

## Backend rules

- **No Lombok** — write explicit Java: getters, setters, constructors, manual `Builder` inner classes, `LoggerFactory.getLogger(ClassName.class)`.
- Database: H2 in-memory (`jdbc:h2:mem:`) for `dev` and `test` profiles; Neon PostgreSQL for production.
- Flyway for all schema changes — never use `ddl-auto: create`.
- Tiny types for domain values (`Isin`, `Quote`, `DepotName` etc.) — no raw `String`/`double` in domain logic.
- Security is currently disabled via `folio.security.enabled=false`; Clerk JWT integration is planned but not yet implemented.

## Frontend rules

- **Strato design system** (`@dynatrace/strato-components@3.1.1`) for all UI components.
  - Tokens injected via JS in `main.tsx` — no CSS import.
  - `AppRoot` from `/core` wraps the app.
  - Layout: `Page` + `Page.Header` + `Page.Sidebar` + `Page.Main`.
  - Imports use subpackage paths: `/layouts`, `/buttons`, `/forms`, `/tables`, `/typography`, `/content`.
  - `Paragraph` has no `color` prop — use `style={{ color: 'var(--dt-color-...)' }}`.
  - `TextInput` onChange: `(value: string, event) => void`.
  - `Select` uses `Select.Content` + `Select.Option` children pattern.
- **All `DataTable` instances must have `resizable`** — global requirement.
- Install packages with `--legacy-peer-deps` (required by `@dynatrace-sdk/*` peer deps).
- API client (`src/api/client.ts`): Axios with `baseURL: 'http://localhost:8080/api'`.

## Running locally

```bash
# Backend (dev profile, H2 in-memory)
cd backend && ./gradlew bootRun

# Frontend
cd frontend && npm run dev   # http://localhost:5173
```

## Number and date formatting (frontend)

- Prices, counts, monetary values: `toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 })` — comma as decimal separator (e.g. `123,45`).
- Dates: ISO `YYYY-MM-DD` via `isoString.substring(0, 10)` — avoids timezone shifts from `new Date()`.

## German decimal parsing (backend)

`ImportService.parseDouble`: if `,` or `~` is present in the value, remove all `.` first (thousands separator), then replace `,`/`~` with `.`. This handles German locale numbers like `1.234,56` → `1234.56`.