# Project Description

## What it does

Folio is a personal investment portfolio analysis and visualization tool for tracking securities 
across multiple brokers, calculating gains/losses, dividends.

In the first version KeSt (Austrian capital gains tax) calculation is a stated goal, but it is not yet clear how this will be implemented. Therefore, KeSt is currently out of scope for v1 and will be explicitly deferred to a future version.

## Who uses it
It is a personal tool, just for me. Only one portfolio is managed. There can be multiple users with access to the same portfolio, but there is no concept of multiple portfolios or user-specific portfolios.

## Requirements
- User registration and authentication
- The swagger ui and the rest endpoints should be protected and only accessible for authenticated users
- The application should have a user-friendly interface for managing data
- All data tables in the UI shall have resizable columns
- The application should have a responsive design that works well on both desktop and mobile devices
- The application should have a dark mode and a light mode, with the ability to switch between them
- The application should have proper error handling and display user-friendly error messages
- The application should have proper logging for debugging and monitoring purposes
- The application should have a well-defined data model and use a relational database for persistence
- The application should have a clear separation of concerns between the frontend and backend
- The application should have proper authentication and authorization mechanisms in place to ensure security
- The application should have a production build that can be easily deployed and run on linux and windows
- The application should have API documentation that is easily accessible and up-to-date
- The application should have a clear and modern design, with a focus on usability and accessibility
- The application should have a clean separation of concerns between frontend and backend that allows for scalability and maintainability
- The application should have a well-defined API that allows for easy integration with other systems and services
- The application should have a clear and consistent coding style and follow best practices for software development
- The application should have a comprehensive test suite that covers all major functionality and edge cases

### Data Import

#### Fetching Quotes
The backend fetches periodically quote data via webscrapping. The interval can be changed dynamically via the UI and is stored database in table settings. The quotes are stored in the database and used for performance calculation and display in the UI.

#### Broker Transaction Import
- The system shall import transaction data from **DeGiro** broker CSV exports (see sample: [`docs/samples/Transactions.csv`](docs/samples/Transactions.csv)). The share price stored per transaction shall be derived from the `Wert EUR` column (index 11, total trade value in EUR) divided by the absolute share count, not from the `Kurs` column (index 7), which is denominated in the security's local trading currency and may not be EUR. Rows where `Anzahl` (count) is zero shall be skipped entirely — they represent non-trade entries such as fees or dividends and would cause division by zero in the price formula.
- The system shall import transaction data from **ZERO** broker CSV exports (see sample: [`docs/samples/ZERO-orders-22.03.2026.csv`](docs/samples/ZERO-orders-22.03.2026.csv)).
- The system shall normalize ISINs across all broker sources into a unified transaction model.

#### Account Statement Import
- The system shall import DeGiro account statement CSVs (see sample: [`docs/samples/Account.csv`](docs/samples/Account.csv)) to extract dividend payments.
- The system shall import ZERO account statement CSVs (see sample: [`docs/samples/ZERO-kontoumsaetze-22.03.2026.csv`](docs/samples/ZERO-kontoumsaetze-22.03.2026.csv)) to extract dividend payments. Only rows with status `gebucht` and purpose starting with `Coupons/Dividende` are considered dividends. The ISIN is extracted from the purpose field, and the valuta date is used as the payment date.

#### Dividend File Import
- The system shall read a manually maintained dividend file (`dividende.csv`) containing expected dividend yields per ISIN (see sample: [`docs/samples/dividende.csv`](docs/samples/dividende.csv)).
- For the file upload via UI, the system shall provide a user interface to upload the `dividende.csv` file and validate its format and content before processing.

### Diversification Analysis

#### Branch Diversification
- The system shall categorize each ISIN by industry/branch (see sample: [`docs/samples/branches.csv`](docs/samples/branches.csv)).
- The system shall calculate the total invested amount per branch.
- The system shall output a branch diversification breakdown.

#### Country Diversification
- The system shall categorize each ISIN by country (see sample: [`docs/samples/countries.csv`](docs/samples/countries.csv)).
- The system shall calculate the total invested amount per country.
- The system shall output a country diversification breakdown.

### Data model
- The system shall have a well-defined data model that represents the entities and relationships in the application, such as users, transactions, positions, quotes, branches, countries, etc.
- The data model shall be designed to support the required functionality and performance of the application, with proper normalization and indexing for efficient querying and data integrity.

#### `ticker_symbol`
Maps a ticker symbol to a security.

| Column | Type        | Constraints      | Description                        |
|--------|-------------|------------------|------------------------------------|
| id     | INTEGER     | PK               | Surrogate primary key              |
| symbol | VARCHAR(20) | UNIQUE, NOT NULL | Ticker symbol (e.g. AAPL, BASF.DE) |

#### `currency`
Reference table of all currencies.

| Column | Type        | Constraints      | Description                    |
|--------|-------------|------------------|--------------------------------|
| id     | INTEGER     | PK               | Surrogate primary key          |
| name   | VARCHAR(3)  | UNIQUE, NOT NULL | Currency code (e.g. EUR, USD)  |

#### `isin`
Central registry of all known securities by ISIN.

| Column | Type         | Constraints | Description             |
|--------|--------------|-------------|-------------------------|
| id     | INTEGER      | PK          | Surrogate primary key   |
| isin   | VARCHAR(12)  | UNIQUE, NOT NULL | Security identifier (e.g. DE000BASF111) |

#### `dividend`
Manually maintained expected annual dividend per ISIN. Sourced from [`docs/samples/dividende.csv`](docs/samples/dividende.csv).

| Column             | Type         | Constraints      | Description                          |
|--------------------|--------------|------------------|--------------------------------------|
| id                 | INTEGER      | PK               | Surrogate primary key                |
| isin_id            | INTEGER      | FK → isin.id     | Reference to the security            |
| currency_id        | INTEGER      | FK → currency.id | Reference to the currency            |
| dividend_per_share | DOUBLE       | NOT NULL         | Expected annual dividend per share   |

#### `depot`
Represents a broker account/depot where securities are held.

| Column | Type         | Constraints      | Description           |
|--------|--------------|------------------|-----------------------|
| id     | INTEGER      | PK               | Surrogate primary key |
| name   | VARCHAR(100) | UNIQUE, NOT NULL | Depot name (e.g. DeGiro, ZERO) |

#### `transaction`
Records a buy or sell transaction for a security.

| Column      | Type      | Constraints      | Description                        |
|-------------|-----------|------------------|------------------------------------|
| id          | INTEGER   | PK               | Surrogate primary key              |
| date        | TIMESTAMP | NOT NULL         | Date and time of the transaction   |
| isin_id     | INTEGER   | FK → isin.id     | Reference to the security          |
| depot_id    | INTEGER   | FK → depot.id    | Reference to the depot             |
| count       | DOUBLE    | NOT NULL         | Number of shares bought or sold    |
| share_price | DOUBLE    | NOT NULL         | Price per share at time of transaction |

#### `country`
Reference table of all countries. Sourced from [`docs/samples/countries.csv`](docs/samples/countries.csv).

| Column | Type         | Constraints   | Description        |
|--------|--------------|---------------|--------------------|
| id     | INTEGER      | PK            | Surrogate primary key |
| name   | VARCHAR(100) | UNIQUE, NOT NULL | Country name (e.g. BRD, USA, Irland) |

#### `branch`
Reference table of all industry branches. Sourced from [`docs/samples/branches.csv`](docs/samples/branches.csv).

| Column | Type         | Constraints      | Description                        |
|--------|--------------|------------------|------------------------------------|
| id     | INTEGER      | PK               | Surrogate primary key              |
| name   | VARCHAR(100) | UNIQUE, NOT NULL | Branch name (e.g. Technology, Energy) |

#### `isin_country`
Maps an ISIN to its country of origin. Sourced from [`docs/samples/countries.csv`](docs/samples/countries.csv).

| Column     | Type    | Constraints              | Description          |
|------------|---------|--------------------------|----------------------|
| isin_id    | INTEGER | PK, FK → isin.id         | Reference to ISIN    |
| country_id | INTEGER | PK, FK → country.id      | Reference to country |

#### `isin_name`
Maps an ISIN to a human-readable security name. An ISIN can have multiple names (e.g. different names used across brokers or import files). Names are only added, never overwritten. A composite unique constraint on `(isin_id, name)` prevents exact duplicate entries.

| Column  | Type         | Constraints      | Description                        |
|---------|--------------|------------------|------------------------------------|
| id      | INTEGER      | PK                       | Surrogate primary key             |
| isin_id | INTEGER      | FK → isin.id             | Reference to the security         |
| name    | VARCHAR(255) | NOT NULL                 | Security name (e.g. "Apple Inc.") |
|         |              | UNIQUE (isin_id, name)   | Prevents duplicate (ISIN, name) pairs |

#### `isin_ticker`
Maps an ISIN to its ticker symbol(s).

| Column           | Type    | Constraints                  | Description               |
|------------------|---------|------------------------------|---------------------------|
| isin_id          | INTEGER | PK, FK → isin.id             | Reference to ISIN         |
| ticker_symbol_id | INTEGER | PK, FK → ticker_symbol.id    | Reference to ticker symbol |

#### `isin_branch`
Maps an ISIN to its industry branch. Sourced from [`docs/samples/branches.csv`](docs/samples/branches.csv).

| Column    | Type    | Constraints          | Description         |
|-----------|---------|----------------------|---------------------|
| isin_id   | INTEGER | PK, FK → isin.id     | Reference to ISIN   |
| branch_id | INTEGER | PK, FK → branch.id   | Reference to branch |

#### `quote_provider`
Reference table of all quote data sources used in the cascading fallback.

| Column | Type         | Constraints      | Description                              |
|--------|--------------|------------------|------------------------------------------|
| id     | INTEGER      | PK               | Surrogate primary key                    |
| name   | VARCHAR(100) | UNIQUE, NOT NULL | Provider name (e.g. `JustETF`, `Onvista`) |

#### `settings`
Key-value store for application configuration (e.g. quote fetch interval).

| Column | Type         | Constraints      | Description                        |
|--------|--------------|------------------|------------------------------------|
| id     | INTEGER      | PK               | Surrogate primary key              |
| key    | VARCHAR(100) | UNIQUE, NOT NULL | Setting name (e.g. `quote.fetch.interval`) |
| value  | VARCHAR(500) | NOT NULL         | Setting value                      |

#### `dividend_payment`
Records an actual dividend payment received for a security in a specific depot.

| Column      | Type             | Constraints      | Description                              |
|-------------|------------------|------------------|------------------------------------------|
| id          | INTEGER          | PK               | Surrogate primary key                    |
| timestamp   | TIMESTAMP        | NOT NULL         | Date and time the dividend was received  |
| isin_id     | INTEGER          | FK → isin.id     | Reference to the security                |
| depot_id    | INTEGER          | FK → depot.id    | Reference to the depot                   |
| currency_id | INTEGER          | FK → currency.id | Reference to the currency                |
| value       | DOUBLE PRECISION | NOT NULL         | Dividend amount received                 |

---

## Page Specifications

Detailed use cases, API contracts, CSV parsing specs, and UI specifications for each page are maintained in separate documents:

| Page | Document | Route(s) |
|------|----------|----------|
| Dashboard | [pages/dashboard.md](pages/dashboard.md) | `/` |
| Transactions | [pages/transactions.md](pages/transactions.md) | `/transactions` |
| Securities | [pages/securities.md](pages/securities.md) | `/securities` |
| Import | [pages/import.md](pages/import.md) | `/import` |
| Analytics | [pages/analytics.md](pages/analytics.md) | `/analytics/countries`, `/analytics/branches` |
| Settings & Quotes | [pages/settings.md](pages/settings.md) | `/settings` |
| Reference Data | [pages/reference-data.md](pages/reference-data.md) | `/countries`, `/branches`, `/depots`, `/currencies` |

---

## Tech preferences

### Architecture
- clean separation of concerns between the frontend and backend
- as persistence layer, a relational database should be used https://neon.com/
- as database migration tool, flyway should be used https://flywaydb.org/
- H2 (in PostgreSQL compatibility mode) is used as the database for local/dev and testing profiles; Neon PostgreSQL is used for production
- use tiny types for domain entities (e.g. `Isin`, `Quote`, `Depot`) to encapsulate validation and domain logic, rather than using primitive types like `String` or `double` directly in the codebase

#### User management and authentication
clerk (https://clerk.com/) should be used for user management and authentication.

### Backend
- java backend and spring boot 
- backend should support a rest api for the frontend to consume
- swagger ui and openapi should be used for API documentation
- backend should have a well-defined data model and use a relational database for persistence
- backend should have proper error handling and logging
- backend should be designed with security in mind, with proper authentication and authorization mechanisms in place

### Frontend
- React 18 + TypeScript frontend, built with Vite
- Single-page application consuming the backend REST API via Axios
- Responsive design using the Strato `Page` + `Page.Sidebar` + `Page.Main` layout
- **Strato design system:** `@dynatrace/strato-components` v3.1.1 with `@dynatrace/strato-design-tokens` for CSS custom properties
  - Design tokens are injected via JS in `main.tsx` (CSS import not available)
  - `AppRoot` from `@dynatrace/strato-components/core` wraps the app
  - Navigation lives in `Page.Sidebar`; the `Page` component handles responsive collapse to a drawer
  - Components imported from subpackage paths: `/layouts`, `/buttons`, `/forms`, `/tables`, `/typography`
- Dark mode and light mode with the ability to switch between them (planned; tokens support it via `@media prefers-color-scheme`)
- Clerk authentication planned (`@clerk/clerk-react`) — not yet integrated


### build tools
- gradle for the backend
- vite for the frontend
- result of the production build should be a single docker image containing both frontend and backend that can be run on linux and windows

---

## Project Scaffolding

### Root Gradle Setup

A root-level `settings.gradle` is required so IntelliJ recognizes the Gradle project when opened at the repo root. It includes the backend via composite build:

```groovy
rootProject.name = 'folio'
includeBuild('backend')
```

### Backend (Spring Boot + Gradle)

**`build.gradle` dependencies:**
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `springdoc-openapi-starter-webmvc-ui:2.8.5`
- `flyway-core`, `flyway-database-postgresql`
- `postgresql` (runtimeOnly)
- `com.opencsv:opencsv:5.9`
- `com.h2database:h2` (runtimeOnly — dev + test)

**Note:** Lombok is NOT used. `nimbus-jose-jwt` for Clerk JWT verification is planned but not yet added.

**Package structure:**
```
com.folio
  ├── config/         # Security, OpenAPI, CORS config
  ├── controller/     # REST controllers
  ├── service/        # Business logic (ImportService, PortfolioService)
  ├── repository/     # JPA repositories
  ├── model/          # JPA entities (explicit Java, no Lombok)
  ├── dto/            # Request/Response DTOs
  ├── exception/      # Global exception handler
  ├── parser/         # CSV parsers per broker (planned, not yet extracted)
  └── quote/          # IsinsQuoteLoader + per-source fetchers (planned, not yet implemented)
```

**Tiny types:** Domain values wrapped in value types (`Isin`, `Quote`, `DepotName`) rather than raw `String`/`double`. Each tiny type lives in `model/`.

**`application.yml`:**
```yaml
spring.datasource: Neon PostgreSQL URL (from env var DATABASE_URL)
spring.flyway: enabled, locations=classpath:db/migration
clerk.jwks-uri: https://api.clerk.com/v1/jwks
```

**`application-dev.yml`:**
```yaml
spring.datasource:
  url: jdbc:h2:file:./data/folio;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
  driver-class-name: org.h2.Driver
spring.h2.console.enabled: true
spring.jpa.database-platform: org.hibernate.dialect.H2Dialect
```

**`application-test.yml`:**
```yaml
spring.datasource:
  url: jdbc:h2:mem:folio;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
  driver-class-name: org.h2.Driver
spring.jpa.database-platform: org.hibernate.dialect.H2Dialect
```

### Frontend (Vite + React + TypeScript)

**`package.json` dependencies:**
- `react@18.3.1`, `react-dom`, `react-router-dom@7.13.1`
- `@dynatrace/strato-components@3.1.1`
- `@dynatrace/strato-design-tokens@1.3.1`
- `@dynatrace/strato-icons@2.1.0`
- `@dynatrace-sdk/*` (peer deps; install with `--legacy-peer-deps`)
- `recharts@3.8.0`
- `axios@1.13.6`

**Note:** `@clerk/clerk-react` is planned but not yet installed.

**Directory structure:**
```
src/
  ├── api/            # Axios client (client.ts — baseURL hardcoded for dev, no auth yet)
  ├── components/     # Layout.tsx (Page + Sidebar + Main)
  ├── pages/          # Route-level pages
  └── types/          # TypeScript interfaces (index.ts)
```

**Global table convention:** All `DataTable` instances use the `resizable` prop.

**Strato integration pattern:**
- `AppRoot` from `@dynatrace/strato-components/core` wraps the entire app in `main.tsx`
- Design tokens injected via JS (CSS import not available)
- Layout: `Page` + `Page.Header` + `Page.Sidebar` + `Page.Main`
- `AppHeader` inside `Page.Header` for the title bar
- Sidebar contains nav links via `react-router-dom`
- Imports use subpackage paths: `/layouts`, `/buttons`, `/forms`, `/tables`, `/typography`

### Docker

Multi-stage `Dockerfile`:

```dockerfile
# Stage 1: Build frontend
FROM node:20-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/ .
RUN npm ci && npm run build

# Stage 2: Build backend
FROM eclipse-temurin:21-jdk AS backend
WORKDIR /app
COPY backend/ .
COPY --from=frontend /app/frontend/dist src/main/resources/static
RUN ./gradlew bootJar -x test

# Stage 3: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Runtime environment variables:** `DATABASE_URL`, `CLERK_PUBLISHABLE_KEY`, `CLERK_SECRET_KEY`.

---

## Database Schema (Flyway)

**`V1__create_schema.sql`** — creates all tables:

```
currency         (id, name VARCHAR(3) UNIQUE NOT NULL)
isin             (id, isin VARCHAR(12) UNIQUE NOT NULL)
isin_name        (id, isin_id FK, name VARCHAR(255) NOT NULL, UNIQUE(isin_id, name))
ticker_symbol    (id, symbol VARCHAR(20) UNIQUE NOT NULL)
isin_ticker      (isin_id FK, ticker_symbol_id FK — composite PK)
country          (id, name VARCHAR(100) UNIQUE NOT NULL)
branch           (id, name VARCHAR(100) UNIQUE NOT NULL)
isin_country     (isin_id FK, country_id FK — composite PK; UNIQUE(isin_id) enforced by V6)
isin_branch      (isin_id FK, branch_id FK — composite PK; UNIQUE(isin_id) enforced by V6)
depot            (id, name VARCHAR(100) UNIQUE NOT NULL)
transaction      (id, date TIMESTAMP, isin_id FK, depot_id FK, count DOUBLE PRECISION, share_price DOUBLE PRECISION)
dividend         (id, isin_id FK, currency_id FK, dividend_per_share DOUBLE PRECISION)
dividend_payment (id, timestamp TIMESTAMP, isin_id FK, depot_id FK, currency_id FK, value DOUBLE PRECISION)
quote_provider   (id, name VARCHAR(100) UNIQUE NOT NULL)
isin_quote       (id, isin_id FK UNIQUE, quote_provider_id FK, value DOUBLE PRECISION NOT NULL, fetched_at TIMESTAMP NOT NULL)
settings         (id, key VARCHAR(100) UNIQUE NOT NULL, value VARCHAR(500) NOT NULL)
```

**`V2__seed_depots.sql`** — inserts `DeGiro` and `ZERO` depot records.

**`V3__seed_currencies.sql`:**
```sql
INSERT INTO currency (name) VALUES
('AUD'), ('CAD'), ('EUR'), ('GBP'), ('USD'), ('SGD'), ('NOK'), ('PLN'),
('CNY'), ('IDR'), ('ZAR'), ('MXN'), ('HUF'), ('ILS'), ('DKK'), ('CZK'),
('THB'), ('SEK'), ('JPY'), ('BRL'), ('RON'), ('CHF'), ('ISK'), ('TRY'),
('HKD'), ('INR'), ('KRW'), ('MYR'), ('NZD'), ('PHP');
```

**`V4__seed_quote_providers.sql`:**
```sql
INSERT INTO quote_provider (name) VALUES
('JustETF'), ('Onvista'), ('FinanzenNet'), ('CNBC'),
('FondsDiscount'), ('ComDirect'), ('WallstreetOnline');
```

**`V5__seed_settings.sql`:**
```sql
INSERT INTO settings (key, value) VALUES ('quote.fetch.interval.minutes', '60');
```

**`V6__enforce_single_country_branch.sql`:**
```sql
ALTER TABLE isin_country ADD CONSTRAINT uq_isin_country_isin UNIQUE (isin_id);
ALTER TABLE isin_branch  ADD CONSTRAINT uq_isin_branch_isin  UNIQUE (isin_id);
```

---

## Authentication (Clerk)

### Backend

- `SecurityConfig.java`: stateless JWT filter on all `/api/**` endpoints.
- `ClerkJwtFilter.java`: fetches Clerk JWKS URI, validates `Authorization: Bearer <token>`, extracts `sub` claim.
- Swagger UI (`/swagger-ui/**`, `/v3/api-docs/**`) also protected; users authorize via the `Authorize` button.
- Currently disabled via `folio.security.enabled=false` (permits all requests); `ClerkJwtFilter` not yet implemented.

### Frontend

- Wrap app in `<ClerkProvider publishableKey={...}>`.
- `<SignIn />` / `<SignUp />` pages at `/sign-in`, `/sign-up`.
- `useAuth()` hook: attach JWT to all API requests via Axios interceptor.
- Protected routes: redirect to `/sign-in` if unauthenticated.
- `@clerk/clerk-react` not yet installed.

---

## Frontend Routes

| Route | Page | Description |
|-------|------|-------------|
| `/` | Dashboard | KPI cards + top-5 holdings/dividend sources + last quote fetch |
| `/transactions` | Transactions | Sortable/filterable table |
| `/securities` | Securities | Portfolio positions with live quotes and performance |
| `/countries` | Countries | Alphabetical list |
| `/branches` | Branches | Alphabetical list |
| `/depots` | Depots | Alphabetical list |
| `/currencies` | Currencies | Alphabetical list |
| `/analytics/countries` | Country Diversification | Donut chart + detail table |
| `/analytics/branches` | Branch Diversification | Donut chart + detail table |
| `/import` | Import | File upload section per data type |
| `/settings` | Settings | Quote fetch interval selector + manual trigger |

### App Layout

- `Page.Header` → `AppHeader` with app title "Folio" and logo link.
- `Page.Sidebar` → navigation links; active link highlighted; collapses to drawer below 640px (Strato default).
- `Page.Main` → routed content via `<Outlet />`.
- **Planned (not yet implemented):** dark/light mode toggle; Clerk `<UserButton />` in header; auth redirect.

---

## Testing

PROJECT.md requires "a comprehensive test suite that covers all major functionality and edge cases." The testing strategy and tooling are not yet defined. The following is planned but not yet implemented:

**Backend (JUnit 5 + Spring Boot Test):**
- Unit tests for CSV parsers (each broker format), domain services (portfolio calculation, import logic), and tiny types.
- Integration tests for REST controllers using `@WebMvcTest` and for repositories using `@DataJpaTest` with H2 in PostgreSQL mode.
- Test profile uses `application-test.yml` (in-memory H2).

**Frontend (Vitest + React Testing Library):**
- Unit tests for utility functions (formatters, filters).
- Component tests for pages with mocked API responses.

---

## Error Handling & Logging

**Backend:**
- `GlobalExceptionHandler` (`@RestControllerAdvice`): maps exceptions to `{ error, message, timestamp }` JSON.
- CSV import: errors collected per row, returned in import response — partial success allowed.
- SLF4J + Logback: structured logging at INFO/WARN/ERROR.

**Frontend:**
- Axios interceptor: 401 → redirect to `/sign-in`; 4xx/5xx → show Strato notification/toast.
- File type validation before upload (client-side).

---

## API Documentation

- SpringDoc generates OpenAPI 3 spec at `/v3/api-docs`.
- Swagger UI at `/swagger-ui/index.html`.
- Configured with Clerk JWT bearer auth scheme (users paste token in `Authorize` dialog).
- All controllers annotated with `@Tag`, `@Operation`, `@ApiResponse`.

---

## Production Build

### Gradle + Vite Integration

`build.gradle` custom tasks:
1. `npmInstall`: exec `npm ci` in `frontend/`
2. `buildFrontend`: exec `npm run build`; depends on `npmInstall`
3. `copyFrontend`: copy `frontend/dist/` → `backend/src/main/resources/static/`; depends on `buildFrontend`
4. `bootJar` depends on `copyFrontend`

### SPA Fallback

Catch-all `ResourceHttpRequestHandler`: all non-`/api/**` routes serve `index.html` (enables client-side routing).

---

## Implementation Order

1. Project scaffolding (backend + frontend skeletons, Docker stub)
2. Database schema + Flyway migrations
3. Clerk authentication (backend JWT filter + frontend auth flow)
4. CSV parsers (DeGiro transactions → ZERO orders → reference files)
5. REST API (import endpoints first, then query/analytics)
6. Frontend pages (import page first to load data, then transactions, securities, analytics)
7. Error handling & logging polish
8. Swagger/OpenAPI annotations
9. Docker build + end-to-end production test
