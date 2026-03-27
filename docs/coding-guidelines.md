# Coding Guidelines

Central coding conventions for the Folio project. All contributors (human and AI) must follow these rules.

---

## General

- Keep classes and functions small and focused (single responsibility).
- Prefer composition over inheritance.
- Do **not** use abstract classes; use interfaces (with default methods where appropriate) instead.
- Avoid deep nesting; return early on error/guard conditions.
- Use meaningful, self-documenting names for classes, methods, and variables.
- Remove dead code — do not leave commented-out blocks in the codebase.

---

## Backend (Java 21 / Spring Boot)

### Design

- Do **not** use abstract classes. Define contracts as interfaces; share reusable logic via helper/utility classes or default interface methods.
- **All classes must be `final`** — prevents unintended subclassing and makes the design explicit. (JPA entities included; switch `FetchType.LAZY` to `EAGER` where needed so Hibernate does not require proxy subclasses.) **Exception:** classes that Spring needs to CGLIB-proxy must **not** be `final`. This includes classes annotated with `@Configuration`, `@SpringBootApplication`, and any Spring bean (service, controller, etc.) whose methods are annotated with `@Transactional` (or other AOP-proxied annotations such as `@Cacheable`, `@Async`, etc.).
- Do **not** inherit from concrete classes. Only extend interfaces (or implement them).
- Do **not** create inner classes (including static nested classes). Extract every nested type to its own top-level file. For the Builder pattern, use `<Type>Builder` as the class name (e.g. `IsinBuilder`).
- **Use static imports** for static method calls where unambiguous (e.g. `getLogger(…)` instead of `LoggerFactory.getLogger(…)`, `requireNonNull(…)` instead of `Objects.requireNonNull(…)`). When two static-imported names collide (e.g. `List.of` vs `Map.of`), keep one qualified to avoid ambiguity.
- Prefer constructor injection; never use field injection (`@Autowired` on fields).
- Keep controllers thin — business logic belongs in service classes.
- Each REST controller should map to exactly one service.
- All parameters to public methods and constructors must be checked for nullity (e.g. `Objects.requireNonNull`) and throw `IllegalArgumentException` if invalid.
- **Constructors** must have **at most 3 parameters**. Keep constructor bodies free of logic — only parameter validation (precondition checks) is allowed.
- **Methods** must have **at most 3 parameters**; prefer 0 or 1. If more are needed, introduce a parameter object or rethink the design.

### Naming

- Controllers: `*Controller` (e.g. `StockController`).
- Services: `*Service` (e.g. `StockService`).
- Repositories: `*Repository` (e.g. `StockRepository`).
- DTOs: `*Dto` (e.g. `StockDto`).
- Use `lowerCamelCase` for methods and variables, `UpperCamelCase` for classes.

### Logging

- Use SLF4J (`org.slf4j.Logger`).
- Log external calls (URLs, provider names) at `INFO` level.
- Log errors and unexpected states at `WARN` or `ERROR`.
- Use `DEBUG` for verbose/diagnostic output only.

### Database / Flyway

- One migration file per schema change, prefixed with `V<number>__` (e.g. `V1__create_schema.sql`).
- Never alter existing, already-applied migration files — add a new migration instead.
- Use `snake_case` for all table and column names.

### Error Handling

- Controllers should return proper HTTP status codes (400, 404, 500) — not just 200 with an error body.
- Never swallow exceptions silently; at minimum log at `WARN` level.

### Testing

- Use **JUnit 5** and **Mockito** for unit tests.
- Every test method must follow the **Given / When / Then** structure, marked with line comments:
  ```java
  @Test
  void shouldReturnEmptyWhenIsinNotFound() {
      // given
      var service = new StockService(mockRepo);
      when(mockRepo.findByIsin("XX")).thenReturn(Optional.empty());

      // when
      var result = service.findByIsin("XX");

      // then
      assertThat(result).isEmpty();
  }
  ```
- Test class naming: `<ClassUnderTest>Test` (e.g. `StockServiceTest`).
- Test method naming: `should<ExpectedBehaviour>[When<Condition>]` — no `test` prefix.
- One logical assertion per test (multiple `assertThat` calls are fine if they verify the same concept).
- Use **AssertJ** (`assertThat`) for assertions — do not use JUnit's `assertEquals` / `assertTrue`.
- Mock external dependencies (repositories, HTTP clients) — never hit a real database or network in unit tests.
- Keep tests independent — no shared mutable state between test methods.
- **Every Java class must have a corresponding test class** (e.g. `StockService` → `StockServiceTest`). The test class must cover **all public methods and constructors**.

---

## Frontend (React / TypeScript / Vite)

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

- Feature requirements go into the appropriate `docs/frontend/pages/*.md` file or `docs/PROJECT.md`.
- Data model changes must be reflected in `docs/data-model.md`.
- Coding conventions go into this file (`docs/coding-guidelines.md`).
