# Missing & Incomplete Requirements

Analysis of requirements from the project specification that are **not yet implemented** or **partially implemented** in the current codebase.

---

## 1. Authentication & Security

### 1.1 Clerk Authentication — Not Implemented

- **Spec:** Clerk (`@clerk/clerk-react`, `nimbus-jose-jwt`) shall be used for user management and authentication.
- **Status:** Zero authentication code exists. `SecurityConfig.java` uses a `folio.security.enabled` flag (defaulting to `false`) that permits all requests. No `ClerkJwtFilter`, no `nimbus-jose-jwt` dependency, no `@clerk/clerk-react` in the frontend.
- **Impact:** All of the following are missing:
  - User registration
  - Login / logout flow
  - `<ClerkProvider>` wrapping the frontend app
  - Protected routes in the frontend
  - JWT verification on backend API endpoints
  - Swagger UI protection (spec: "The swagger ui and the rest endpoints should be protected and only accessible for authenticated users")

> **Ref:** `PROJECT.md` → Requirements, Tech preferences → User management and authentication

---

## 2. Frontend — UI Gaps

### 2.1 Row Count Display — "N of M" Format Missing

- **Spec (ui.md):** Filtered: `"N of M items"` (e.g. `"12 of 50 stocks"`). Unfiltered: `"M items"`.
- **Status:**
  - **Transactions:** Always shows `"{filteredCount} transactions"` — never displays the unfiltered total in the `"N of M"` format. The backend `TransactionPaginatedResponseDto` has `filteredCount` but no separate `totalCount` (total count without filters applied), so the "N of M" pattern is impossible with the current API response.
  - **Stocks:** Always shows `"{table.totalItems} stocks"` — no "N of M" format when filters are active.
  - **Other pages** (Countries, Branches, Depots, Currencies, TickerSymbols, IsinNames, Analytics): These have no filters, so `"M items"` format is correct as-is.

> **Ref:** `ui.md` → Row Count Display; `transactions.md` → Summary; `stocks.md`

### 2.2 Transactions — Missing Refresh Button

- **Spec (transactions.md):** "A Refresh button reloads all transactions from the backend."
- **Status:** The Stocks page has a Refresh button, but the Transactions page does not.

> **Ref:** `transactions.md` → Loading and Refresh

### 2.3 Transactions — Missing Per-Filter Clear Buttons

- **Spec (transactions.md):** "A Clear button appears next to the [ISIN] field and resets the filter." Same for Name filter.
- **Status:** There is a single "Clear" button that resets all filters simultaneously. No individual clear buttons next to ISIN or Name filter inputs.

> **Ref:** `transactions.md` → Filtering

### 2.4 Debounce on Text Filter Inputs — Not Implemented

- **Spec (plan.md Step 5):** "Debounce ISIN/name text input (300 ms) to avoid excessive requests on every keystroke."
- **Status:** Both Transactions and Stocks pages trigger an API refetch immediately on every keystroke in the ISIN and Name text inputs. No debounce logic exists.

> **Ref:** `plan.md` → Step 5

### 2.5 Dashboard — Missing Column Widths and MinWidths

- **Spec (dashboard.md):** Top 5 Holdings and Top 5 Dividend Sources tables shall have specific `width`/`minWidth` per column (e.g. ISIN: 140/140, Name: 240/200, Amount: 160/120).
- **Status:** `Dashboard.tsx` columns do not specify `width` or `minWidth` properties.

> **Ref:** `dashboard.md` → UI Specification

### 2.6 Stocks — Column Header "Count" Should Be "Total Shares"

- **Spec (stocks.md):** Column header is `"Total Shares"`.
- **Status:** `Stocks.tsx` uses `header: 'Count'` for this column. The sort field is also `count` instead of `totalShares` as specified.

> **Ref:** `stocks.md` → Columns

### 2.7 Ticker Symbols — Row Count Label Mismatch

- **Spec (ticker-symbols.md):** Count display: `"N ticker symbol mappings"`.
- **Status:** Displays `"N ticker symbols"` (`itemLabel="ticker symbols"`).

> **Ref:** `ticker-symbols.md` → Features

### 2.8 ISIN Names — Row Count Label Mismatch

- **Spec (isin-names.md):** Count display: `"N ISIN name mappings"`.
- **Status:** Displays `"N ISIN names"` (`itemLabel="ISIN names"`).

> **Ref:** `isin-names.md` → Features

### 2.9 Dark Mode / Light Mode Toggle — Not Implemented

- **Spec (PROJECT.md):** "Dark mode and light mode with the ability to switch between them."
- **Status:** Dark mode only works via the OS-level `prefers-color-scheme` CSS media query. There is no user-facing toggle switch to manually switch between dark and light mode.

> **Ref:** `PROJECT.md` → Requirements; `PROJECT.md` → Frontend tech preferences

### 2.10 Transactions — `fromDate`/`toDate` Filters Not Exposed in UI

- **Spec (transactions.md):** API supports `fromDate` and `toDate` query params.
- **Status:** The backend endpoint accepts `fromDate`/`toDate`, but the Transactions UI does not expose any date range filter controls. Users cannot filter transactions by date.

> **Ref:** `transactions.md` → REST API

---

## 3. Backend — API & Logic Gaps

### 3.1 Transactions — Missing `totalCount` (Unfiltered) in Response

- **Spec (plan.md Step 2d):** The envelope response should include `totalCount` (count of all transactions regardless of filters) alongside `filteredCount` (count after filtering).
- **Status:** `TransactionController` computes `filteredCount = data.size()` (after filtering), but never computes the total unfiltered count. The `totalItems` in the paginated envelope equals the filtered count. The "N of M" row count display is therefore impossible.

> **Ref:** `plan.md` → Step 2d; `ui.md` → Row Count Display

### 3.2 Stocks — Sort Field `totalShares` Not Supported

- **Spec (stocks.md):** `sortField` accepts `totalShares`.
- **Status:** The `StockController.SORT_FIELDS` map uses key `"count"` (mapped to `StockDto::getCount`), not `"totalShares"`. A client sending `?sortField=totalShares` would get the default sort instead.

> **Ref:** `stocks.md` → REST API

### 3.3 Settings — Interval Update Request Body Mismatch

- **Spec:** `PUT /api/quotes/settings/interval` accepts `@RequestBody Map<String, Integer>` with key `"intervalMinutes"`.
- **Status:** The backend expects `intervalMinutes` in the request body, but `Settings.tsx` sends `minutes` as a **query parameter**: `api.put('/quotes/settings/interval', null, { params: { minutes } })`. The interval save likely fails silently or is ignored.

> **Ref:** `settings.md` → REST API; `Settings.tsx`

### 3.4 Production Database Configuration — Incomplete

- **Spec (PROJECT.md):** Neon PostgreSQL for production, configured via `DATABASE_URL` env var.
- **Status:** `application.yml` only has `spring.profiles.active: dev`. There is no production profile (`application-prod.yml`) with Neon PostgreSQL datasource URL, and no `DATABASE_URL` env var binding.

> **Ref:** `PROJECT.md` → Tech preferences → Architecture

---

## 4. Build & Deployment

### 4.1 Docker Image — Not Implemented

- **Spec (PROJECT.md):** "Result of the production build should be a single docker image containing both frontend and backend that can be run on linux and windows."
- **Status:** No `Dockerfile` exists anywhere in the project.

> **Ref:** `PROJECT.md` → Build tools

---

## 5. Testing Gaps

### 5.1 Missing Frontend Page Tests

- **Spec (ui.md):** "Each page and shared component shall have a corresponding `.test.tsx` file."
- **Status:** The following pages have **no** test files:
  - `Analytics.tsx`
  - `Branches.tsx`
  - `Currencies.tsx`
  - `Depots.tsx`
  - `TickerSymbols.tsx`
  - `IsinNames.tsx`
  - `Import.tsx`

  Existing tests: `Countries.test.tsx`, `Dashboard.test.tsx`, `Settings.test.tsx`, `Stocks.test.tsx`, `Transactions.test.tsx`, `ExportButtons.test.tsx`, `Layout.test.tsx`, `PaginationControls.test.tsx`.

> **Ref:** `ui.md` → Testing → UI Unit / Component Tests

### 5.2 Missing `ServerTable.test.tsx`

- **Spec:** Shared components shall have corresponding test files.
- **Status:** `ServerTable.tsx` has no test file.

> **Ref:** `ui.md` → Testing

---

## 6. Architecture & Code Quality

### 6.1 Tiny Types — Partially Implemented

- **Spec (PROJECT.md):** "Use tiny types for domain entities (e.g. `Isin`, `Quote`, `Depot`) to encapsulate validation and domain logic, rather than using primitive types."
- **Status:** The `com.folio.domain` package contains:
  - `IsinCode` — wraps a validated ISIN string
  - `TickerCode` — wraps a ticker symbol string
  - `Quote` — record holding `Amount amount` (price + `Currency`) and `Instant timestamp`; produced by `QuoteFetcher`
  - `Amount` — record holding `double value` and `Currency`

  No tiny types yet for `Depot`, `DepotName`, or other domain values. Services and controllers still use raw `String` and `double` for most domain values.

> **Ref:** `PROJECT.md` → Tech preferences → Architecture

### 6.2 CSV Parsing Logic in ImportService

- **Spec (gaps.and.issues.md):** "CSV line parsing logic (`parseGermanDouble`, `parseCsvLine`) still lives in `ImportService`."
- **Status:** Already documented as a known gap. The `parser/` package exists but parsing utility methods have not been extracted from `ImportService`.

> **Ref:** `gaps.and.issues.md` → X. parser/ package

---

## Summary Table

| # | Area | Gap | Severity |
|---|------|-----|----------|
| 1.1 | Security | Clerk authentication not implemented | High |
| 2.1 | Frontend | Row count "N of M" format missing | Medium |
| 2.2 | Frontend | Transactions missing Refresh button | Low |
| 2.3 | Frontend | Missing per-filter Clear buttons on Transactions | Low |
| 2.4 | Frontend | No debounce on text filter inputs | Medium |
| 2.5 | Frontend | Dashboard table column widths not set | Low |
| 2.6 | Frontend | Stocks column header "Count" should be "Total Shares" | Low |
| 2.7 | Frontend | Ticker Symbols row count label mismatch | Low |
| 2.8 | Frontend | ISIN Names row count label mismatch | Low |
| 2.9 | Frontend | No dark/light mode toggle switch | Medium |
| 2.10 | Frontend | Transactions date range filter not in UI | Medium |
| 3.1 | Backend | Transactions missing `totalCount` in response | Medium |
| 3.2 | Backend | Stocks sort field `totalShares` not supported | Low |
| 3.3 | Backend | Settings interval update request body mismatch (bug) | High |
| 3.4 | Backend | No production database configuration | High |
| 4.1 | DevOps | No Dockerfile for production build | High |
| 5.1 | Testing | 7 frontend pages missing test files | Medium |
| 5.2 | Testing | ServerTable component missing test file | Low |
| 6.1 | Architecture | Tiny types partially implemented (`IsinCode`, `TickerCode`, `Quote`, `Amount` done; `Depot` etc. missing) | Low |
| 6.2 | Architecture | CSV parsing logic not extracted from ImportService | Low |

