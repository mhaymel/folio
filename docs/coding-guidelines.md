# Coding Guidelines

Central coding conventions for the Folio project. All contributors (human and AI) must follow these rules.

---

## General

- **Thin frontend / fat backend** — Keep the frontend as thin as possible. All data logic (filtering, sorting, aggregation, derived values) belongs in the backend. The frontend only renders data and forwards user interactions (filter criteria, sort parameters) to the backend via REST calls. The goal is to minimise the amount of frontend code.
- Keep classes and functions small and focused (single responsibility).
- Prefer composition over inheritance.
- Do **not** use abstract classes; use interfaces (with default methods where appropriate) instead.
- Avoid deep nesting; return early on error/guard conditions.
- Use meaningful, self-documenting names for classes, methods, and variables.
- Remove dead code — do not leave commented-out blocks in the codebase.

---

## Backend (Java 21 / Spring Boot)

### No Lombok

Do **not** use Lombok. Write explicit Java: getters, setters, constructors, manual `Builder` classes (extracted to top-level `<Type>Builder`), `getLogger(ClassName.class)` (via static import, see DES-08).

### Design

| ID | Rule |
|----|------|
| **DES&#8209;02** | Define contracts as interfaces. |
| **DES&#8209;03** | Share reusable logic via helper/utility classes or default interface methods. |
| **DES&#8209;09** | Prefer constructor injection; never use field injection (`@Autowired` on fields). |
| **DES&#8209;10** | Keep controllers thin — business logic belongs in service classes. |
| **DES&#8209;11** | Each REST controller should map to exactly one service. |

### Naming

- Controllers: `*Controller` (e.g. `StockController`).
- Services: `*Service` (e.g. `StockService`).
- Repositories: `*Repository` (e.g. `StockRepository`).
- DTOs: `*Dto` (e.g. `StockDto`).
- Parsed CSV types: `Parsed*` records in the `parser` package (e.g. `ParsedTransaction`). These are immutable value objects produced by CSV parsing, before DB entities are created.
- Use `lowerCamelCase` for methods and variables, `UpperCamelCase` for classes.

### Logging

- Use SLF4J (`org.slf4j.Logger`).
- Log external calls (URLs, provider names) at `INFO` level.
- Log errors and unexpected states at `WARN` or `ERROR`.
- Use `DEBUG` for verbose/diagnostic output only.

### Database / Flyway

- One migration file per schema change, prefixed with `V<number>__` (e.g. `V1__create_schema.sql`).
- Never alter existing, already-applied migration files — add a new migration instead.
- Never use `ddl-auto: create` — all schema changes must go through Flyway migrations.
- Use `snake_case` for all table and column names.

### Error Handling

- Controllers should return proper HTTP status codes (400, 404, 500) — not just 200 with an error body.
- Never swallow exceptions silently; at minimum log at `WARN` level.

### Testing

See [testing.md](testing.md) for all testing conventions, scope, and infrastructure.

Key naming rules:
- Unit test class: `<ClassUnderTest>Test` (e.g. `QuoteFetcherTest`)
- External API integration test class: `<ClassUnderTest>IntegrationTest` (e.g. `QuoteFetcherIntegrationTest`, `IsinToTickerIntegrationTest`)
- Test method: `should<ExpectedBehaviour>[When<Condition>]` — no `test` prefix
- **Do not use separator comments** like `// --- methodToBeTested ---` or `// --- section name ---`. Test method names are self-documenting; let the test structure speak for itself.

---

## Frontend (React / TypeScript / Vite)

### Strato Component API Quirks

- `Paragraph` has no `color` prop — use `style={{ color: 'var(--dt-color-...)' }}`.
- `TextInput` onChange signature: `(value: string, event) => void`.
- `Select` uses `Select.Content` + `Select.Option` children pattern.
- `DataTable` `sortable` prop enables sort indicator arrows but the component also sorts rows client-side by default. Since all sorting is server-side, the frontend must ensure column header clicks only trigger a backend request — the DataTable must not reorder data on its own.
- `DataTable` sorting cycles through three states by default (asc → desc → unsorted). Since server-side sorting always requires an active sort, the `onSortByChange` handler must treat the empty-array (removal) state as a direction toggle so that only asc ↔ desc is possible.
- Use Strato `Select` (not native `<select>`) for all dropdown controls, including the page-size selector in the pagination bar.
- Install packages with `--legacy-peer-deps` (required by `@dynatrace-sdk/*` peer deps).

### Design

- Use functional components with hooks — no class components.
- Keep page components in `src/pages/`, reusable UI pieces in `src/components/`.
- All API calls go through `src/api/client.ts`.
- Shared TypeScript types live in `src/types/index.ts`.

### Naming

- Components and pages: `UpperCamelCase` (e.g. `Stocks.tsx`).
- Hooks and utilities: `lowerCamelCase` (e.g. `useFetch`).
- CSS / style files: match the component name (e.g. `App.css`).

### Types

- Prefer explicit TypeScript interfaces/types over `any`.
- Export shared DTOs from `src/types/index.ts` to keep a single source of truth.

### Formatting & Linting

- Follow the project ESLint configuration (`eslint.config.js`).
- Use single quotes, no semicolons (or whichever style the existing config enforces — stay consistent).

---

## Documentation

- Feature requirements go into the appropriate `docs/pages/*.md` file or `docs/PROJECT.md`.
- Data model changes must be reflected in `docs/data-model.md`.
- Coding conventions go into this file (`docs/coding-guidelines.md`).
