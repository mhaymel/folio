# Plan: Thin frontend — move all logic to backend

## Goal

The frontend should be a thin rendering layer with as little code as possible. **All** data logic — filtering, sorting, aggregation, deriving filter options — must live in the backend. Every page that displays a sortable/filterable table sends the user's criteria as REST query parameters and renders the response as-is. The DataTable `sortable` prop is enabled for UI affordance only; actual data reordering is always done by the backend.

---

## Current state summary

| Page | Client-side sorting | Client-side filtering | Client-side aggregation/derivation |
|------|--------------------|-----------------------|------------------------------------|
| **Stocks** | DataTable sorts locally | country + branch filter via `useMemo` | Derives unique countries/branches from data |
| **Transactions** | DataTable sorts locally | ISIN + name + depot filter via `useMemo` | Derives unique depots; computes `sumCount` |
| **Countries** | DataTable sorts locally | — | — |
| **Branches** | DataTable sorts locally | — | — |
| **Depots** | DataTable sorts locally | — | — |
| **Currencies** | DataTable sorts locally | — | — |
| **TickerSymbols** | DataTable sorts locally | — | — |
| **IsinNames** | DataTable sorts locally | — | — |
| **Analytics** | DataTable sorts locally | — | Extracts `entries` array via `useMemo` |
| Dashboard | No table sorting | — | Extracts `top5Holdings`/`top5DividendSources` via `useMemo` |
| Settings | No tables | — | Interval validation logic |
| Import | No tables | — | FormData upload handling |

**Backend**: Export endpoints already accept `sortField`/`sortDir` (and filters for stocks/transactions) and apply them via Java streams. The main GET endpoints do **not** accept sort/filter params (except `GET /api/transactions` which has basic exact-match filters).

---

## Implementation plan

### Step 1 — Backend: Add `sortField`/`sortDir` to all list endpoints

Add optional `sortField` and `sortDir` query params to every GET list endpoint. Apply sorting via Java streams (or ORDER BY where applicable) before returning the response. Reuse the comparator logic that already exists in the corresponding export endpoints.

| Endpoint | Supported `sortField` values | Default sort |
|----------|------------------------------|-------------|
| `GET /api/countries` | `name` | `name asc` |
| `GET /api/branches` | `name` | `name asc` |
| `GET /api/depots` | `name` | `name asc` |
| `GET /api/currencies` | `name` | `name asc` |
| `GET /api/ticker-symbols` | `isin`, `tickerSymbol`, `name` | `isin asc` |
| `GET /api/isin-names` | `isin`, `name` | `name asc` |
| `GET /api/analytics/countries` | `name`, `investedAmount`, `percentage` | `investedAmount desc` |
| `GET /api/analytics/branches` | `name`, `investedAmount`, `percentage` | `investedAmount desc` |
| `GET /api/stocks` | `isin`, `name`, `country`, `branch`, `totalShares`, `avgEntryPrice`, `currentQuote`, `performancePercent`, `dividendPerShare`, `estimatedAnnualIncome` | `isin asc` |
| `GET /api/transactions` | `date`, `isin`, `name`, `depot`, `count`, `sharePrice` | `date desc` |

**Refactoring**: Extract the sorting logic from each export endpoint into a shared private method in the service (or controller), so both the list endpoint and the export endpoint call the same code.

#### Tests

- For each endpoint: verify default sort order, explicit `asc`, explicit `desc`, and invalid `sortField` (should fall back to default).

---

### Step 2 — Backend: Add filtering + aggregation to Stocks and Transactions

#### 2a. `GET /api/stocks` — add filter params

| Param | Behaviour |
|-------|-----------|
| `country` | Exact match on country name |
| `branch` | Exact match on branch name |

Reuse the filtering logic from the stock export endpoint.

#### 2b. New endpoint `GET /api/stocks/filters`

Returns distinct filter options derived from current positions:
```json
{ "countries": ["Austria", "Germany", …], "branches": ["Technology", "Finance", …] }
```
Create `StockFiltersDto` record.

#### 2c. `GET /api/transactions` — extend filter params

| Param | Behaviour |
|-------|-----------|
| `isin` | **Partial**, case-insensitive match (LIKE `%value%`) — currently exact |
| `name` | **Partial**, case-insensitive match on joined `isin_name` — currently unsupported |
| `depot` | Exact match on depot name — already supported |
| `fromDate` / `toDate` | Already supported |

#### 2d. Envelope response for `GET /api/transactions`

Wrap the response in:
```json
{
  "transactions": [ … ],
  "totalCount": 50,
  "filteredCount": 12,
  "sumCount": 1234.500
}
```
Create `TransactionResponseDto` record. Compute `sumCount` and counts server-side.

#### 2e. New endpoint `GET /api/transactions/filters`

Returns distinct depot names:
```json
{ "depots": ["DeGiro", "ZERO"] }
```
Create `TransactionFiltersDto` record.

#### Tests

- Stocks: filter combinations, empty results, filter-options endpoint.
- Transactions: partial ISIN match, name match, combined filters, envelope fields, filters endpoint.

---

### Step 3 — Frontend: Remove client-side sorting from all table pages

For each page that has a DataTable with sort state (`sortField`/`sortDir`):

**Countries, Branches, Depots, Currencies** (single-column pages):
1. When the user changes sort direction, re-fetch from the backend with `?sortField=name&sortDir=…`.
2. Remove `useMemo(() => data, [data])` — assign API response directly.
3. Disable DataTable's client-side sort behaviour (data arrives pre-sorted).

**TickerSymbols, IsinNames** (multi-column pages):
1. Same pattern — re-fetch on sort change with the selected `sortField` and `sortDir`.
2. Remove trivial `useMemo`.

**Analytics**:
1. Re-fetch `GET /api/analytics/{type}?sortField=…&sortDir=…` on sort change.
2. Remove `useMemo(() => data?.entries ?? [], [data])` — use response directly.

#### Tests

- Update each `.test.tsx` to verify that sort changes trigger a new API call with the correct query params.

---

### Step 4 — Frontend: Remove client-side filtering & aggregation from Stocks

1. On mount and when filters/sort change, call `GET /api/stocks?country=…&branch=…&sortField=…&sortDir=…` and render the response directly.
2. Fetch filter options from `GET /api/stocks/filters` on mount; populate dropdowns.
3. Remove the `useMemo` blocks that derive unique countries/branches and filter the stock list.
4. Update `Stocks.test.tsx`.

---

### Step 5 — Frontend: Remove client-side filtering & aggregation from Transactions

1. On mount and when filters/sort change, call `GET /api/transactions?isin=…&name=…&depot=…&sortField=…&sortDir=…` and render `transactions` from the envelope.
2. Use `sumCount`, `totalCount`, `filteredCount` from the response for the summary — remove the `reduce`.
3. Fetch depot options from `GET /api/transactions/filters` on mount; populate dropdown.
4. Remove the `useMemo` blocks for filtering, aggregation, and depot derivation.
5. Debounce ISIN/name text input (300 ms) to avoid excessive requests on every keystroke.
6. Update `Transactions.test.tsx`.

---

### Step 6 — Frontend: Simplify Dashboard and remaining pages

**Dashboard**: Remove `useMemo(() => data?.top5Holdings ?? [], [data])` and similar — access fields directly from the response object (e.g. `data?.top5Holdings`). No API change needed.

**Settings / Import**: Already minimal; no changes required.

---

### Step 7 — Consolidate backend sort/filter logic

After all endpoints accept sort/filter params:
1. Extract shared sorting utility (e.g. a generic `SortHelper.sort(list, sortField, sortDir, fieldMap)`) to avoid duplicating comparator logic across controllers.
2. Ensure each list endpoint and its corresponding export endpoint share the exact same filter+sort implementation.
3. Remove any duplicated comparator code from export methods.

---

### Step 8 — Backend: Date formatting

Move date and date-time formatting from the frontend to the backend so the frontend receives pre-formatted strings and renders them as-is.

#### 8a. `TransactionDto` — format `date` field

Change `TransactionDto.date` from `LocalDateTime` to `String`. The service formats the date as `DD-MM-YYYY` (e.g. `28-03-2026`) before returning it. The ISO date (`YYYY-MM-DD`) is no longer sent to the frontend.

- **Sorting still works** because the backend sorts by the underlying `LocalDateTime` column before formatting.
- The frontend `fmtDate` / `isoDate` helper functions become dead code.

#### 8b. `DashboardDto` — format `lastQuoteFetchAt` field

Change `DashboardDto.lastQuoteFetchAt` from `LocalDateTime` to `String`. The service formats it as `DD.MM.YYYY HH:mm` (e.g. `22.03.2026 14:30`) before returning. Null stays null.

#### 8c. `QuoteSettingsDto` — format `lastFetchAt` field

Same treatment: format `lastFetchAt` as `DD.MM.YYYY HH:mm`.

#### 8d. Export endpoints

Export endpoints already format dates server-side with `DateTimeFormatter.ofPattern("dd-MM-yyyy")` — no change needed. Verify the format matches the new list-endpoint format for consistency.

#### 8e. Frontend cleanup

Remove all date-formatting helper functions (`fmtDate`, `isoDate`, `sortAccessor` for dates). Render the string received from the backend directly. Remove the `sortAccessor` column config from the Date column in Transactions — backend sorting makes it unnecessary.

#### Tests

- Backend: verify formatted date strings in list-endpoint responses.
- Frontend: update test expectations to match pre-formatted strings.

---

### Step 9 — Backend: Server-side pagination for all list endpoints

Add `page` and `pageSize` query params to every GET list endpoint that returns a table. The backend slices the (already sorted and filtered) result list and returns a paginated envelope.

#### 9a. Pagination envelope

All paginated endpoints return:
```json
{
  "items": [ … ],
  "page": 1,
  "pageSize": 10,
  "totalItems": 142,
  "totalPages": 15
}
```

Create a generic `PaginatedResponseDto<T>` record with fields: `items`, `page`, `pageSize`, `totalItems`, `totalPages`.

#### 9b. Query parameters

| Param | Type | Default | Validation |
|-------|------|---------|------------|
| `page` | int | `1` | ≥ 1; values < 1 treated as 1 |
| `pageSize` | int | `10` | One of `[10, 20, 50, 100, -1]`; `-1` means "all" (no slicing); invalid values fall back to `10` |

When `pageSize = -1`, the endpoint returns all items in a single page (`page: 1`, `totalPages: 1`, `pageSize: -1`). This supports the frontend "Show All" toggle without a separate code path.

#### 9c. Endpoints to paginate

| Endpoint | `items` type | Additional envelope fields |
|----------|-------------|---------------------------|
| `GET /api/countries` | `CountryDto[]` | — |
| `GET /api/branches` | `BranchDto[]` | — |
| `GET /api/depots` | `DepotDto[]` | — |
| `GET /api/currencies` | `CurrencyDto[]` | — |
| `GET /api/ticker-symbols` | `TickerSymbolDto[]` | — |
| `GET /api/isin-names` | `IsinNameDto[]` | — |
| `GET /api/analytics/countries` | `AnalyticsEntryDto[]` | — |
| `GET /api/analytics/branches` | `AnalyticsEntryDto[]` | — |
| `GET /api/stocks` | `StockDto[]` | — |
| `GET /api/transactions` | `TransactionDto[]` | `sumCount`, `totalCount`, `filteredCount` (existing fields move into the envelope alongside `items`, `page`, etc.) |

**`GET /api/transactions` envelope** merges pagination with the existing aggregation fields:
```json
{
  "items": [ … ],
  "page": 1,
  "pageSize": 10,
  "totalItems": 50,
  "totalPages": 5,
  "filteredCount": 50,
  "sumCount": 1234.500
}
```
`totalItems` replaces the old `filteredCount` for pagination purposes. `filteredCount` and `sumCount` are computed over the full filtered set (before slicing).

#### 9d. Pagination utility

Create a shared `PaginationHelper` class with a static method:
```java
static <T> PaginatedResponseDto<T> paginate(List<T> items, int page, int pageSize)
```
This slices the list and computes `totalPages`. Each service calls this after sorting/filtering.

#### Tests

- For each endpoint: verify default pagination (page 1, size 10), explicit page/size, `pageSize=-1` returns all, out-of-range page returns empty items, correct `totalItems`/`totalPages`.
- Transactions: verify `sumCount` and `filteredCount` are computed over full filtered set, not just the current page.

---

### Step 10 — Frontend: Consume paginated responses

#### 10a. Update API response types

Replace bare array types (e.g. `CountryDto[]`) with `PaginatedResponse<CountryDto>`:
```typescript
interface PaginatedResponse<T> {
  items: T[];
  page: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
}
```

#### 10b. Update each table page

For every page with a DataTable:
1. Track `page` and `pageSize` in component state (defaults: `page=1`, `pageSize=10`).
2. Send `page` and `pageSize` as query params alongside existing sort/filter params.
3. Remove the Strato `<DataTablePagination>` component — pagination is now server-driven.
4. Render a custom pagination bar below the table showing: page navigation (previous/next/first/last), current page indicator (`"Page 1 of 15"`), page-size selector (`[10, 20, 50, 100]`), and the "Show All / Paginate" toggle.
5. "Show All" sends `pageSize=-1`; "Paginate" resets to `pageSize=10, page=1`.
6. When filters or sort change, reset `page` to `1`.

#### 10c. Row count display

Use `totalItems` from the envelope for the row count display:
- Unfiltered: `"142 stocks"`
- Filtered: `"12 of 142 stocks"` (for endpoints that have both filtered and total counts)

#### 10d. Transactions-specific

Use `sumCount` from the paginated envelope (computed over full filtered set). Remove the client-side `reduce` if still present.

#### Tests

- Update each `.test.tsx` to mock the paginated envelope structure.
- Verify page changes trigger API calls with correct `page`/`pageSize` params.
- Verify page resets to 1 when filters or sort change.

---

### Step 11 — Cleanup & verification

1. Remove dead frontend code: unused `useMemo` hooks, unused imports, unused types, `sortAccessor` functions, `fmtDate`/`isoDate` helpers, client-side `DataTablePagination` imports.
2. Run full backend test suite (`./gradlew :backend:build`).
3. Run full frontend test suite (`npm test`).
4. Run E2E tests (`npm run test:e2e`).
5. Manual smoke test: verify every table page sorts and paginates via backend, dates display correctly, Stocks/Transactions filter correctly, export matches filters and sort order, "Show All" works.

---

## Out of scope

- **Database-level sorting for stocks/analytics**: These are computed from aggregated data in memory. Java-stream sorting is sufficient.