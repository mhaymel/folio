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

| ID | Rule                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
|----|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **DES&#8209;01** | Do **not** use abstract classes.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| **DES&#8209;02** | Define contracts as interfaces.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| **DES&#8209;03** | Share reusable logic via helper/utility classes or default interface methods.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| **DES&#8209;04** | **All classes must be `final`** — prevents unintended subclassing and makes the design explicit. (JPA entities included; switch `FetchType.LAZY` to `EAGER` where needed so Hibernate does not require proxy subclasses.) **Exception:** classes that Spring needs to CGLIB-proxy must **not** be `final`. This includes classes annotated with `@Configuration`, `@SpringBootApplication`, and any Spring bean (service, controller, etc.) whose methods are annotated with `@Transactional` (or other AOP-proxied annotations such as `@Cacheable`, `@Async`, etc.). |
| **DES&#8209;05** | Do **not** inherit from concrete classes. Only extend interfaces (or implement them).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| **DES&#8209;06** | Do **not** create inner classes (including static nested classes and inner records). Extract every nested type to its own top-level file. For the Builder pattern, use `<Type>Builder` as the class name (e.g. `IsinBuilder`).                                                                                                                                                                                                                                                                                                                                         |
| **DES&#8209;07** | **Java records** are encouraged for simple, immutable value objects (e.g. parsed CSV rows). Each record must be in its own top-level file — never defined inside another class. Records do not need builders; use the canonical constructor directly. The constructor parameter limit (max 3) does not apply to records.                                                                                                                                                                                                                                               |
| **DES&#8209;08** | **Use static imports** for static method calls where unambiguous (e.g. `getLogger(…)` instead of `LoggerFactory.getLogger(…)`, `requireNonNull(…)` instead of `Objects.requireNonNull(…)`). When two static-imported names collide (e.g. `List.of` vs `Map.of`), keep one qualified to avoid ambiguity.                                                                                                                                                                                                                                                                |
| **DES&#8209;09** | Prefer constructor injection; never use field injection (`@Autowired` on fields).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| **DES&#8209;10** | Keep controllers thin — business logic belongs in service classes.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| **DES&#8209;11** | Each REST controller should map to exactly one service.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| **DES&#8209;12** | All parameters to public methods and constructors must be checked for nullity (`requireNonNull`) and throw `IllegalArgumentException` if invalid. Use `requireNonNull` with one parameter only — no message string.                                                                                                                                                                                                                                                                                                                                                    |
| **DES&#8209;13** | **Constructors** must have **at most 3 parameters**. Keep constructor bodies free of logic — only parameter validation (precondition checks) is allowed.                                                                                                                                                                                                                                                                                                                                                                                                               |
| **DES&#8209;14** | **Methods** must have **at most 3 parameters**; prefer 0 or 1. If more are needed, introduce a parameter object or rethink the design.                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| **DES&#8209;15** | **Do not use underscores in method names.** Use `lowerCamelCase` exclusively. This applies to all methods, including unit test methods. For tests, write `shouldAcceptNonEmptyArray()` not `should_accept_non_empty_array()`.                                                                                                                                                                                                                                                                                                                                          |
| **DES&#8209;16** | **No unused imports.** Remove all unused imports from Java files. IDEs (IntelliJ, VS Code) provide quick-fix commands to clean imports — use them before committing code.                                                                                                                                                                                                                                                                                                                                                                                              |
| **DES&#8209;17** | **No wildcard imports** (`import java.util.*;`). Always use explicit, fully-qualified imports. This makes dependencies clear and avoids name collisions. IDEs can auto-organize imports — use them.                                                                                                                                                                                                                                                                                                                                                                    |
| **DES&#8209;18** | **Do not use `continue` or `break` in loops.** Extract the loop body to a private method and use `return` instead of `continue`. Do not invert the condition; do not add nesting. |

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
