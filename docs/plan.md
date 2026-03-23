# Implementation Plan: Folio Portfolio Tracker

## Overview

Full-stack app: Spring Boot backend + React/TypeScript frontend, packaged as a single Docker image.

---

## Phase 1: Project Scaffolding

### 1.0 Root Gradle Setup

A root-level `settings.gradle` is required so IntelliJ recognizes the Gradle project when opened at the repo root (`C:\daten\dev\folio`). It includes the backend via composite build:

```groovy
rootProject.name = 'folio'
includeBuild('backend')
```

### 1.1 Backend (Gradle + Spring Boot)

**Directory:** `backend/`

**`build.gradle` dependencies (as implemented):**
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `springdoc-openapi-starter-webmvc-ui:2.8.5` (Swagger)
- `flyway-core`, `flyway-database-postgresql`
- `postgresql` (runtimeOnly)
- `com.opencsv:opencsv:5.9` (CSV parsing)
- `com.h2database:h2` (runtimeOnly — dev + test)

**Note:** Lombok is NOT used. All Java code is written explicitly. `nimbus-jose-jwt` for Clerk JWT verification is planned but not yet added.

**Package structure (as implemented):**
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
  └── quote/          # IsinsQuoteLoader + per-source quote fetchers (planned, not yet implemented)
```

**Tiny types:** Domain values are wrapped in value types (e.g. `Isin`, `Quote`, `DepotName`) rather than raw `String`/`double`. Each tiny type encapsulates its validation and lives in `model/`.

**`application.yml`:**
```yaml
spring.datasource: Neon PostgreSQL URL (from env var DATABASE_URL)
spring.flyway: enabled, locations=classpath:db/migration
clerk.jwks-uri: https://api.clerk.com/v1/jwks
```

**`application-dev.yml`** (profile: `dev`):
```yaml
spring.datasource:
  url: jdbc:h2:file:./data/folio;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
  driver-class-name: org.h2.Driver
spring.h2.console.enabled: true
spring.jpa.database-platform: org.hibernate.dialect.H2Dialect
```

**`application-test.yml`** (profile: `test`):
```yaml
spring.datasource:
  url: jdbc:h2:mem:folio;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
  driver-class-name: org.h2.Driver
spring.jpa.database-platform: org.hibernate.dialect.H2Dialect
```

**Additional `build.gradle` dependency:**
- `com.h2database:h2` (scope: `runtimeOnly`, active for `dev` and `test` profiles only)

### 1.2 Frontend (Vite + React + TypeScript)

**Directory:** `frontend/`

**`package.json` dependencies (as installed):**
- `react@18.3.1`, `react-dom`, `react-router-dom@7.13.1`
- `@dynatrace/strato-components@3.1.1` (Strato UI components)
- `@dynatrace/strato-design-tokens@1.3.1` (CSS custom properties injected via JS in `main.tsx`)
- `@dynatrace/strato-icons@2.1.0`
- `@dynatrace-sdk/*` (peer deps required by strato-components, installed with `--legacy-peer-deps`)
- `recharts@3.8.0` (charts)
- `axios@1.13.6`

**Note:** `@clerk/clerk-react` is planned but not yet installed. Clerk authentication (Phase 3) is not yet implemented.

**Strato integration pattern:**
- `AppRoot` from `@dynatrace/strato-components/core` wraps the entire app in `main.tsx`
- Design tokens injected as CSS custom properties via JS (CSS import not available — not in package exports)
- Layout uses `Page` + `Page.Header` + `Page.Sidebar` + `Page.Main` from `@dynatrace/strato-components/layouts`
- `AppHeader` inside `Page.Header` for the app title bar
- Sidebar contains nav links; clicking navigates to the corresponding page via `react-router-dom`
- Component imports use subpackage paths: `@dynatrace/strato-components/layouts`, `/buttons`, `/forms`, `/tables`, `/typography`

**Directory structure:**
```
src/
  ├── api/            # Axios client (client.ts — baseURL hardcoded for dev, no auth yet)
  ├── components/     # Layout.tsx (Page + Sidebar + Main)
  ├── pages/          # Route-level pages
  └── types/          # TypeScript interfaces (index.ts)
```

### 1.3 Docker

**Multi-stage `Dockerfile`:**
1. Stage 1 (`node`): Build Vite frontend → `dist/`
2. Stage 2 (`gradle`): Build Spring Boot JAR, copy frontend `dist/` into `src/main/resources/static/`
3. Stage 3 (`eclipse-temurin:21-jre`): Run JAR

---

## Phase 2: Database Schema (Flyway)

**`V1__create_schema.sql`** — creates all tables:

```
currency         (id, name VARCHAR(3) UNIQUE NOT NULL)
isin             (id, isin VARCHAR(12) UNIQUE NOT NULL)
isin_name        (id, isin_id FK, name VARCHAR(255) NOT NULL, UNIQUE(isin_id, name))
ticker_symbol    (id, symbol VARCHAR(20) UNIQUE NOT NULL)
isin_ticker      (isin_id FK, ticker_symbol_id FK — composite PK)
country          (id, name VARCHAR(100) UNIQUE NOT NULL)
branch           (id, name VARCHAR(100) UNIQUE NOT NULL)
isin_country     (isin_id FK, country_id FK — composite PK; UNIQUE(isin_id) — 1:1, added by V6)
isin_branch      (isin_id FK, branch_id FK — composite PK; UNIQUE(isin_id) — 1:1, added by V6)
depot            (id, name VARCHAR(100) UNIQUE NOT NULL)
transaction      (id, date TIMESTAMP, isin_id FK, depot_id FK, count DOUBLE PRECISION, share_price DOUBLE PRECISION)
dividend         (id, isin_id FK, currency_id FK, dividend_per_share DOUBLE PRECISION)
dividend_payment (id, timestamp TIMESTAMP, isin_id FK, depot_id FK, currency_id FK, value DOUBLE PRECISION)
quote_provider   (id, name VARCHAR(100) UNIQUE NOT NULL)
isin_quote       (id, isin_id FK UNIQUE, quote_provider_id FK, value DOUBLE PRECISION NOT NULL, fetched_at TIMESTAMP NOT NULL)
settings         (id, key VARCHAR(100) UNIQUE NOT NULL, value VARCHAR(500) NOT NULL)
```

**`V2__seed_depots.sql`** — inserts `DeGiro` and `ZERO` depot records.

**`V3__seed_currencies.sql`** — inserts all known currencies:
```sql
INSERT INTO currency (name) VALUES
('AUD'), ('CAD'), ('EUR'), ('GBP'), ('USD'), ('SGD'), ('NOK'), ('PLN'),
('CNY'), ('IDR'), ('ZAR'), ('MXN'), ('HUF'), ('ILS'), ('DKK'), ('CZK'),
('THB'), ('SEK'), ('JPY'), ('BRL'), ('RON'), ('CHF'), ('ISK'), ('TRY'),
('HKD'), ('INR'), ('KRW'), ('MYR'), ('NZD'), ('PHP');
```

**`V4__seed_quote_providers.sql`** — inserts all active quote providers in cascade order:
```sql
INSERT INTO quote_provider (name) VALUES
('JustETF'),
('Onvista'),
('FinanzenNet'),
('CNBC'),
('FondsDiscount'),
('ComDirect'),
('WallstreetOnline');
```

**`V6__enforce_single_country_branch.sql`** — adds `UNIQUE(isin_id)` constraints to `isin_country` and `isin_branch` to enforce the 1:1 relationship at the database level:
```sql
ALTER TABLE isin_country ADD CONSTRAINT uq_isin_country_isin UNIQUE (isin_id);
ALTER TABLE isin_branch  ADD CONSTRAINT uq_isin_branch_isin  UNIQUE (isin_id);
```

**`V5__seed_settings.sql`** — inserts default application settings:
```sql
INSERT INTO settings (key, value) VALUES
('quote.fetch.interval.minutes', '60');
```

---

## Phase 3: Authentication (Clerk)

### 3.1 Backend Security Config

- `SecurityConfig.java`: Stateless JWT filter on all `/api/**` endpoints (including Swagger UI).
- `ClerkJwtFilter.java`: Fetches Clerk's JWKS URI, validates incoming `Authorization: Bearer <token>`, extracts `sub` claim.
- Swagger UI (`/swagger-ui/**`, `/v3/api-docs/**`): also protected — user must authorize via `Authorize` button with their JWT.

### 3.2 Frontend Auth

- Wrap app in `<ClerkProvider publishableKey={...}>`.
- `<SignIn />` / `<SignUp />` pages at `/sign-in`, `/sign-up`.
- `useAuth()` hook: attach JWT to all API requests via Axios interceptor.
- Protected routes: redirect to `/sign-in` if unauthenticated.

---

## Phase 4: CSV Parsing

### 4.1 DeGiro `Transactions.csv`

**Format:** comma-separated, first row is header.
**Relevant columns:** `Datum` (DD-MM-YYYY), `Uhrzeit`, `ISIN`, `Anzahl` (positive=buy, negative=sell), `Kurs` (price per share).

**Parsing logic:**
1. Skip header row.
2. Parse `Datum` + `Uhrzeit` → `LocalDateTime`.
3. Parse `Anzahl` and `Kurs` as `double` (replace `,` with `.` for decimal).
4. Upsert ISIN into `isin` table.
5. Insert `Product` column (security name) into `isin_name` if this (isin_id, name) pair does not already exist — never overwrite existing names.
6. Map to depot `DeGiro`.
7. Before insert: **DELETE all transactions where `depot_id = DeGiro`**.
8. Bulk insert all parsed rows.

### 4.2 ZERO `ZERO-orders-*.csv`

**Format:** semicolon-separated, first row is header.
**Relevant columns (0-indexed):**

| Index | Column | Description |
|-------|--------|-------------|
| 0 | Name | Security name — insert into `isin_name` |
| 1 | ISIN | Security identifier |
| 5 | Status | Filter: must equal `"ausgeführt"` |
| 12 | Richtung | `"Kauf"` = buy (positive count), `"Verkauf"` = sell (negative count) |
| 16 | Ausführung Datum | Execution date (DD.MM.YYYY) |
| 17 | Ausführung Zeit | Execution time |
| 18 | Ausführung Kurs | Price per share — replace `,` with `.` before parsing |
| 19 | Anzahl ausgeführt | Number of shares executed |

**Parsing logic:**
1. Skip header row.
2. **Filter:** only rows with `Status` (index 5) = `"ausgeführt"`.
3. Parse `Ausführung Datum` (index 16) + `Ausführung Zeit` (index 17) → `LocalDateTime`.
4. `Richtung` (index 12) = `"Kauf"` → positive count; `"Verkauf"` → negative count.
5. Upsert ISIN (index 1) into `isin` table.
6. Insert `Name` (index 0) into `isin_name` if this (isin_id, name) pair does not already exist — never overwrite existing names.
7. Map to depot `ZERO`.
8. Before insert: **DELETE all transactions where `depot_id = ZERO`**.
9. Bulk insert.

### 4.3 DeGiro `Account.csv` (dividends)

**Format:** comma-separated.
**Relevant columns (0-indexed):**

| Index | Column | Description |
|-------|--------|-------------|
| 2 | Valuta | Payment date — used as `timestamp` |
| 4 | ISIN | Security identifier |
| 5 | Beschreibung | Filter: must equal `"Dividende"` exactly |
| 7 | Currency | Currency code (e.g. EUR, USD) |
| 8 | Änderung | Dividend amount — decimal separator is `~`, replace with `.` before parsing |

**Parsing logic:**
1. Skip rows where `line.length < 9`.
2. Filter rows where index 5 (`Beschreibung`) equals `"Dividende"` exactly (this excludes `"Dividendensteuer"` and other entries).
3. Parse index 2 (`Valuta`) as `LocalDateTime` → `timestamp`.
4. Parse index 4 as ISIN; upsert into `isin` table.
5. Parse index 7 as currency code; upsert into `currency` table.
6. Parse index 8 as dividend amount: replace `~` with `.`, then parse as `double`.
7. Insert into `dividend_payment` (depot = DeGiro).

### 4.4 ZERO `ZERO-kontoumsaetze-*.csv` (dividends)

**Format:** semicolon-separated.
**Columns (0-indexed):** `[0] Datum`, `[1] Valuta`, `[2] Betrag`, `[3] Betrag storniert`, `[4] Status`, `[5] Verwendungszweck`, `[6] IBAN`.

**Parsing logic:**
1. Filter rows with `Status` (index 4) = `"gebucht"`.
2. Filter rows where `Verwendungszweck` (index 5) **starts with** `"Coupons/Dividende"`.
3. Extract ISIN from `Verwendungszweck`: find `"ISIN "`, take the next 12 characters.
4. Use `Valuta` (index 1) as the payment date.
5. Parse `Betrag` (index 2) as dividend amount — replace `,` with `.` before parsing as double.
6. Currency is implicitly EUR (ZERO kontoumsaetze has no currency column); upsert `EUR` into `currency` table.
7. Upsert ISIN into `isin` table.
8. Before insert: **DELETE all `dividend_payment` entries where `depot_id = ZERO`**.
9. Insert into `dividend_payment` (depot = ZERO, currency = EUR).

### 4.5 `dividende.csv`

**Format:** semicolon-separated: `ISIN;Name;Currency;DividendPerShare`

**Parsing logic:**
1. Upsert ISIN into `isin` table.
2. Insert `Name` field into `isin_name` if this (isin_id, name) pair does not already exist — never overwrite existing names.
3. Upsert currency into `currency` table.
4. Replace all rows in `dividend` table on each import.

### 4.6 `branches.csv`

**Format:** semicolon-separated: `ISIN;Name;Branch`

**Parsing logic:**
1. Upsert ISIN into `isin` table.
2. Insert `Name` field into `isin_name` if this (isin_id, name) pair does not already exist — never overwrite existing names.
3. Upsert branch name into `branch`.
4. Replace branch mapping for this ISIN (1:1): DELETE existing row in `isin_branch` where `isin_id` matches, then INSERT the new row.

### 4.7 `countries.csv`

**Format:** semicolon-separated: `ISIN;Name;Country`

**Parsing logic:**
1. Upsert ISIN into `isin` table.
2. Insert `Name` field into `isin_name` if this (isin_id, name) pair does not already exist — never overwrite existing names.
3. Upsert country name into `country`.
4. Replace country mapping for this ISIN (1:1): DELETE existing row in `isin_country` where `isin_id` matches, then INSERT the new row.

---

## Phase 5: Backend REST API

All endpoints under `/api/**`, secured with Clerk JWT.

### 5.1 Import Endpoints (`ImportController`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/import/degiro/transactions` | Upload DeGiro Transactions.csv |
| POST | `/api/import/degiro/account` | Upload DeGiro Account.csv |
| POST | `/api/import/zero/orders` | Upload ZERO orders CSV |
| POST | `/api/import/zero/account` | Upload ZERO kontoumsaetze CSV |
| POST | `/api/import/dividends` | Upload dividende.csv |
| POST | `/api/import/branches` | Upload branches.csv |
| POST | `/api/import/countries` | Upload countries.csv |

All return `{ success: boolean, imported: int, errors: string[] }`.

### 5.2 Reference Data Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/countries` | All countries, sorted alphabetically |
| GET | `/api/branches` | All branches, sorted alphabetically |
| GET | `/api/depots` | All depots, sorted alphabetically |
| GET | `/api/currencies` | All currencies, sorted alphabetically |

### 5.3 Transactions Endpoint

| Method | Path | Query Params |
|--------|------|--------------|
| GET | `/api/transactions` | `fromDate`, `toDate`, `isin`, `depotId`, `page`, `size`, `sort` |

Returns paginated transaction list with ISIN, security name (JOIN to `isin_name`), depot, date, count, share price.

### 5.4 Securities Endpoint

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/securities` | Current positions aggregated from transactions |

**Backend calculation:**
- Aggregate `SUM(count)` per `isin_id` across all transactions.
- Filter positions where `SUM(count) > 0` (still held).
- **Avg entry price formula:** `SUM(count * share_price) / SUM(count)` across **all** transactions for that ISIN (buys contribute positive values, sells contribute negative — mirrors `IsinTransactions.entryPrice()`). Sells proportionally reduce the cost basis rather than being excluded.
- Join with `isin_name`, `isin_country`, `isin_branch`, `dividend`, `isin_quote`.
- Return: ISIN, name (from `isin_name`), country, branch, total shares, avg entry price, current quote (from `isin_quote.value`, null if not yet fetched), performance % (`(current_quote - avg_entry_price) / avg_entry_price * 100`), expected annual dividend, estimated annual income.

### 5.5 Analytics Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/analytics/countries` | Total invested per country + % of portfolio |
| GET | `/api/analytics/branches` | Total invested per branch + % of portfolio |

**Calculation logic:**
1. For each open position: `invested = SUM(count * share_price)` (same weighted formula as avg entry price × total shares, computed directly as the sum).
2. Group by country (or branch) via `isin_country` (or `isin_branch`).
3. Sum invested per group, compute percentage of total.

### 5.6 Quote Fetcher (`IsinsQuoteLoader`)

Live EUR prices are resolved via a cascading fallback across 10 sources. Implemented in the `quote` package.

**Config files** (bundled in `src/main/resources/`):

| File | Format | Purpose |
|---|---|---|
| `finanzennet.csv` | `ISIN;path` | URL path for Finanzen.net |
| `onvista.csv` | `ISIN;path` | URL path for Onvista |
| `wallstreetonline.csv` | `ISIN;path` | URL path for WallstreetOnline |
| `isin.symbol.csv` | `ISIN;TICKER;Name` | Ticker symbol for CNBC |

**Cascade order** (each step only processes ISINs not yet resolved):

| Step | Class | URL | Parsing | Currency |
|---|---|---|---|---|
| 1 | `JustEtfRestQuote` | `.../api/etfs/{ISIN}/quote?locale=de&currency=EUR` | Gson JSON: `latestQuote.raw` | EUR |
| 2 | `OnvistaQuote` | `.../onvista.de/{path}` | HTML string search: `<span> <!-- -->EUR\|USD</span>` | EUR or USD as-is |
| 3 | `FinanzenNetQuote` | `.../finanzen.net/{path}` | HTML: `snapshot__value` span + currency check | EUR only |
| 4 | `CnbcQuote` | `.../cnbc.com/quotes/{TICKER}` | HTML: `QuoteStrip-lastPrice` span | USD→EUR (ECB rate) |
| 5 | `JustEtfQuote` | `.../justetf.com/at/etf-profile.html?isin={ISIN}` | HTML: `div.val` → `EUR` span → next span | EUR |
| 6 | `JustEtfRestQuote` | Same as step 1 | Same as step 1 (retry) | EUR |
| 7 | `FondsDiscountDeQuoteEUR` | `.../fondsdiscount.de/fonds/etf/{ISIN}/` | HTML: `course-number` span → `EUR` marker | EUR |
| 8 | `FondsDiscountDeQuoteUSD` | Same URL as step 7 | Same span → `USD` marker | USD→EUR (ECB rate) |
| 9 | `ComDirectQuote` | `.../comdirect.de/inf/zertifikate/{ISIN}` | HTML: `text-size--xxlarge` span | EUR |
| 10 | `WallstreetOnlineQuote` | `.../wallstreet-online.de/{path}` | jsoup CSS: `.quoteValue span` + `div.quote_currency` | EUR or USD (hardcoded ×0.86) |

**HTTP conventions (steps 1–9):** Java `HttpClient` GET, no custom headers. Non-200 or exception → `Optional.empty()`. Step 10 uses jsoup's HTTP client.

**Currency exchange rate:** `CurrencyExchangeRate` reads ECB XML feed at startup with static fallback. Used for USD→EUR in steps 4 and 8. Step 10 uses a hardcoded `×0.86` factor instead.

**Disabled source (do not implement):** Lemon Markets batch API — API token expired, commented out.

**Return value:** `List<IsinQuote>` (ISIN + EUR price). ISINs with no successful quote are absent from the list (no error thrown).

**Persistence:** After each fetch run, upsert results into `isin_quote` (one row per ISIN; `quote_provider_id` set to the provider that succeeded; `fetched_at` = current timestamp). Update `settings` key `quote.last.fetch.timestamp` to the current timestamp after a successful batch.

**Scheduling:** A `@Scheduled` Spring task reads the fetch interval from `settings` (`quote.fetch.interval.minutes`) and triggers `IsinsQuoteLoader` for all ISINs currently held (`SUM(count) > 0`). The interval is re-read from the DB before each run so UI changes take effect without restart. Enable scheduling with `@EnableScheduling` on the application class.

### 5.7 Quote Management Endpoints (`QuoteController`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/quotes/settings` | Returns current fetch interval (minutes) and last fetch timestamp |
| PUT | `/api/quotes/settings/interval` | Updates `quote.fetch.interval.minutes` in `settings` table |
| POST | `/api/quotes/fetch` | Triggers an immediate quote fetch for all held ISINs |

`GET /api/quotes/settings` response:
```json
{ "intervalMinutes": 60, "lastFetchAt": "2026-03-22T14:30:00" }
```
`lastFetchAt` is null if no fetch has run yet.

### 5.8 Dashboard Endpoint (`DashboardController`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/dashboard` | Portfolio summary for the dashboard |

**Backend calculation:**
- **Total portfolio value**: `SUM(avg_entry_price * total_shares)` across all open positions (`SUM(count) > 0`).
- **Security count**: count of distinct ISINs with open positions.
- **Total dividend ratio**: `SUM(shares * dividend_per_share) / total_portfolio_value * 100` (as %). ISINs without a `dividend` entry contribute 0.
- **Top 5 holdings**: five open positions with the highest `avg_entry_price * total_shares`; return ISIN, name (from `isin_name`), and invested amount.
- **Top 5 dividend sources**: five ISINs with the highest `shares * dividend_per_share`; return ISIN, name, and estimated annual income.
- **Last quote fetch**: read `settings` key `quote.last.fetch.timestamp`; null if not yet set.

Response structure:
```json
{
  "totalPortfolioValue": 12345.67,
  "securityCount": 23,
  "totalDividendRatio": 3.14,
  "top5Holdings": [
    { "isin": "IE00B4L5Y983", "name": "iShares Core MSCI World ETF", "investedAmount": 4500.00 }
  ],
  "top5DividendSources": [
    { "isin": "DE000BASF111", "name": "BASF SE", "estimatedAnnualIncome": 150.00 }
  ],
  "lastQuoteFetchAt": "2026-03-22T14:30:00"
}
```

---

## Phase 6: Frontend Pages & Components

### 6.1 App Layout

Layout is implemented using the Strato `Page` component (`@dynatrace/strato-components/layouts`):

- `Page.Header` contains `AppHeader` with the app title ("Folio") and logo link.
- `Page.Sidebar` contains the navigation links. Clicking a link navigates to the corresponding route; the active link is highlighted.
- `Page.Main` contains the routed page content (`<Outlet />`), wrapped in a `.page-content` div for padding.
- The sidebar collapses to a drawer on narrow screens (Strato default breakpoint: 640px).

**Planned but not yet implemented:**
- Dark/light mode toggle (requires Strato theme switching; preference stored in `localStorage`)
- Clerk `<UserButton />` in the header
- Auth redirect on unauthenticated access

### 6.2 Pages

| Route | Page | Description |
|-------|------|-------------|
| `/` | Dashboard | KPI cards: total portfolio value, security count, total dividend ratio; top 5 holdings; top 5 dividend sources; last quote fetch timestamp |
| `/transactions` | Transactions | Sortable/filterable table |
| `/securities` | Securities | Portfolio positions table with live quotes and performance |
| `/countries` | Countries | Alphabetical list |
| `/branches` | Branches | Alphabetical list |
| `/depots` | Depots | Alphabetical list |
| `/currencies` | Currencies | Alphabetical list |
| `/analytics/countries` | Country Diversification | Donut chart + detail table |
| `/analytics/branches` | Branch Diversification | Donut chart + detail table |
| `/import` | Import | File upload section per data type |
| `/settings` | Settings | Quote fetch interval selector + manual trigger |

### 6.3 Import Page Design

- One card/section per import type, grouped by broker.
- Each card: description, `<input type="file" />`, upload button, status indicator (idle / loading / success with row count / error with messages).

### 6.4 Analytics Page Design

- Donut chart (Recharts `PieChart`) with legend.
- Detail table below: name, invested amount (EUR), percentage.
- Country and branch pages share the same layout pattern.

### 6.5 Transactions Page Design

- Table columns: Date, ISIN, Security Name, Depot, Count, Share Price.
- Filter bar: date range picker, ISIN text input, depot dropdown.
- Pagination controls.
- Refresh button.

### 6.6 Securities Page Design

- Table columns: ISIN, Name, Country, Branch, Total Shares, Avg Entry Price, Current Quote, Performance (%), Expected Dividend/Share, Est. Annual Income.
- Current Quote and Performance show `—` if no quote has been fetched yet for that ISIN.
- Filter bar: country dropdown, branch dropdown.
- Sortable columns.

### 6.7 Settings Page Design

- Fetch interval selector: dropdown with predefined options (15 min, 30 min, 1 h, 4 h, 12 h, 24 h) plus custom input.
- "Save interval" button — calls `PUT /api/quotes/settings/interval`.
- "Fetch now" button — calls `POST /api/quotes/fetch`; shows loading spinner while running.
- Last fetch timestamp displayed below the controls.

### 6.8 Dashboard Page Design

- **KPI row**: three cards — Total Portfolio Value (EUR), Number of Securities (count), Total Dividend Ratio (%).
- **Top 5 Holdings**: table with columns ISIN, Security Name, Invested Amount (EUR).
- **Top 5 Dividend Sources**: table with columns ISIN, Security Name, Est. Annual Income (EUR).
- **Last Quote Fetch**: timestamp displayed below the KPI row (e.g. "Last updated: 22.03.2026 14:30"). Shows `—` if no fetch has run yet.
- Data fetched from `GET /api/dashboard`.

---

## Phase 7: Error Handling & Logging

**Backend:**
- `GlobalExceptionHandler` (`@RestControllerAdvice`): maps exceptions to `{ error, message, timestamp }` JSON.
- CSV import errors collected per row and returned in import response — partial success allowed.
- SLF4J + Logback: structured logging at INFO/WARN/ERROR.

**Frontend:**
- Axios interceptor: 401 → redirect to `/sign-in`; 4xx/5xx → show Strato notification/toast.
- File type validation before upload (client-side).

---

## Phase 8: API Documentation

- SpringDoc generates OpenAPI 3 spec at `/v3/api-docs`.
- Swagger UI at `/swagger-ui/index.html`.
- Swagger UI configured with Clerk JWT bearer auth scheme (users paste token in `Authorize` dialog).
- All controllers annotated with `@Tag`, `@Operation`, `@ApiResponse`.

---

## Phase 9: Production Build

### 9.1 Gradle + Vite Integration

`build.gradle` custom tasks:
1. `npmInstall`: exec `npm ci` in `frontend/`
2. `buildFrontend`: exec `npm run build` in `frontend/`; depends on `npmInstall`
3. `copyFrontend`: copy `frontend/dist/` → `backend/src/main/resources/static/`; depends on `buildFrontend`
4. `bootJar` depends on `copyFrontend`

### 9.2 Spring Boot SPA Fallback

Configure a catch-all `ResourceHttpRequestHandler` so all non-`/api/**` routes serve `index.html`, enabling client-side routing.

### 9.3 Dockerfile

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

## Implementation Order (Recommended Sequence)

1. Phase 1 — Project scaffolding (backend + frontend skeletons, Docker stub)
2. Phase 2 — Database schema + Flyway migrations
3. Phase 3 — Clerk authentication (backend JWT filter + frontend auth flow)
4. Phase 4 — CSV parsers (DeGiro transactions first, then ZERO, then reference files)
5. Phase 5 — REST API (import endpoints first, then query/analytics endpoints)
6. Phase 6 — Frontend pages (import page first to load data, then transactions, securities, analytics)
7. Phase 7 — Error handling & logging polish
8. Phase 8 — Swagger/OpenAPI annotations
9. Phase 9 — Docker build + end-to-end production test

---

## Open Questions to Resolve During Implementation

1. ~~**Received dividends storage**: Does Account.csv / ZERO-kontoumsaetze dividend data need a dedicated `received_dividend` table, or is it reporting-only?~~ → Resolved: `dividend_payment` table added.
2. ~~**Market quotes**: Is live market price integration (e.g., Yahoo Finance API) in scope for the initial version, or show avg entry price only?~~ → Resolved: live EUR quotes are fetched via `IsinsQuoteLoader` cascading fallback (see §5.6). Current quote and performance % are included in the securities endpoint response.
3. ~~**ZERO kontoumsaetze dividend pattern**: Exact `Verwendungszweck` format for dividends needs verification from a more complete sample file.~~ → Resolved: filter `Status = "gebucht"` + `Verwendungszweck` starts with `"Coupons/Dividende"`; ISIN extracted as 12 chars after `"ISIN "`; date = Valuta column.
4. ~~**Transaction currency**: DeGiro provides both local currency and EUR value — store original currency + exchange rate, or normalize everything to EUR?~~ → Resolved: store EUR value only.
5. ~~**Austrian KeSt tax calculation**: Mentioned as a project goal — is this in scope for the initial version?~~ → Resolved: explicitly out of scope for v1; deferred to a future version.