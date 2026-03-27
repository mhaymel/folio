# Gaps & Issues: PROJECT.md and plan.md

## Resolved

| # | Issue | Resolution |
|---|---|---|
| 1 | Missing stock `name` field | `isin_name` table added to PROJECT.md and plan.md Phase 2 schema |
| 4 | `dividend_payment` missing `currency_id` | Column added to PROJECT.md and plan.md Phase 2 schema |
| 7 | Vague column name in plan 4.3 (DeGiro Account.csv) | Exact 0-indexed column table added |
| 8 | Stale open-question reference in plan 4.3 | Replaced with actual storage logic |
| C | KeSt (Austrian tax) scope | Explicitly deferred to a future version in PROJECT.md |
| A1 | Empty `Import ZERO-kontoumsaetze.csv` use case | Content added to PROJECT.md |
| B1 | Dashboard use case missing from PROJECT.md | Dashboard use case added |
| D | Market quotes / open question #2 | Live EUR quotes via `IsinsQuoteLoader` cascade (§5.6 added to plan.md); quote + performance added to stocks endpoint; open question #2 resolved |
| E | `isin_name` UNIQUE constraint | Composite `UNIQUE(isin_id, name)` already in plan.md Phase 2 schema and PROJECT.md data model; insert logic in all parsers already enforces "never overwrite" |
| F | ZERO orders CSV — no name column identified | `Name` column confirmed at index 0; plan.md §4.2 updated with full column table and `isin_name` insert step |
| G | Quote persistence + scheduling not in plan | `isin_quote` table added to schema; §5.6 extended with persistence (upsert after fetch, update `settings.quote.last.fetch.timestamp`) and `@Scheduled` task; seed migration V5 added for default interval |
| H | Quote management endpoints missing | §5.7 added: GET/PUT settings, POST immediate trigger |
| I | Settings page missing from frontend plan | `/settings` route added to §6.2; §6.7 Settings Page Design added |
| J | Stocks page missing quote/performance columns | §6.6 updated with Current Quote and Performance columns |
| K | Seed migration V3/V4 order was inverted in plan text | Fixed: V3=currencies, V4=quote providers, V5=settings |
| L | Dashboard backend endpoint missing from Phase 5 | `§5.8 DashboardController` added: `GET /api/dashboard` returning total portfolio value, stock count, total dividend ratio %, top 5 holdings, top 5 dividend sources, last quote fetch timestamp |
| M | Dashboard frontend page description incomplete (wrong KPIs) | §6.2 Dashboard row updated; §6.8 Dashboard Page Design added with KPI cards, top-5 tables, and last-fetch timestamp |
| N | Open question #5 (KeSt) never marked resolved despite PROJECT.md explicitly deferring it | Marked resolved in plan.md open questions |
| O | Tiny types requirement (PROJECT.md tech pref) absent from plan.md | Added to §1.1: `model/` package note and tiny types paragraph |
| P | `quote/` package missing from §1.1 package structure | Added `quote/` entry to package tree in §1.1 |
| Q | Phase 5 section order wrong: 5.6 and 5.7 appeared before 5.5 in the file | Sections reordered: 5.1 → 5.2 → 5.3 → 5.4 → 5.5 → 5.6 → 5.7 → 5.8 |

---

## Open

### T. No testing phase in plan.md

PROJECT.md requires "a comprehensive test suite that covers all major functionality and edge cases", but plan.md has no phase or section covering testing strategy, test types (unit / integration / e2e), or tooling (JUnit, Mockito, Testcontainers, Vitest, etc.). Needs a Phase 10 (or similar) before implementation begins.

### ~~A. Empty use case section in PROJECT.md~~ — Resolved

`Import dividende.csv` use case content added to PROJECT.md.

### ~~B. Missing use cases in PROJECT.md~~ — Resolved

`Import branches.csv` and `Import countries.csv` use cases added to PROJECT.md.

### ~~D. Market quotes and performance — open question #2~~ — Resolved

Live EUR quotes via `IsinsQuoteLoader` cascade fallback (§5.6 added to plan.md). Stocks endpoint updated to return current quote and performance %. Open question #2 resolved.

### ~~E. `isin_name` UNIQUE constraint not reflected in PROJECT.md~~ — Resolved

Both plan.md Phase 2 schema and PROJECT.md data model already reflect composite `UNIQUE(isin_id, name)`. All parser sections enforce "never overwrite" insert logic.

### ~~F. ZERO orders CSV — no name/product column identified~~ — Resolved

`Name` column confirmed at index 0 in the sample file. Plan §4.2 updated with full 0-indexed column table and `isin_name` insert step.

---

### V. Clerk authentication not yet implemented

Plan §3 describes a full Clerk JWT filter (`ClerkJwtFilter.java`) and `nimbus-jose-jwt` dependency. In the actual code, `SecurityConfig.java` uses a `folio.security.enabled` flag (defaulting to `false`) that simply permits all requests. `@clerk/clerk-react` is not installed in the frontend. No `ClerkJwtFilter` exists.

### ~~W. `quote/` package (IsinsQuoteLoader) not yet implemented~~ — Resolved

Plan §5.6 describes a full cascading quote fetcher across 10 sources with its own `quote/` package, config CSV files, `@Scheduled` task, and `isin_quote` upsert logic. — **Resolved 2026-03-25**: Full `quote/` package implemented with `IsinsQuoteLoader`, 10 cascade sources, `QuoteService` with scheduling and persistence. See gap AO for details.

### X. `parser/` package not yet extracted

Plan §1.1 lists a `parser/` package for broker-specific CSV parsers. In the actual code, CSV parsing is implemented inside `ImportService`. The extraction into separate parser classes per broker has not been done.

### Y. Lombok removed — plan.md was updated

Lombok is listed in the original plan §1.1 but is not in `build.gradle` and is not used anywhere. Plan §1.1 has been corrected to reflect this.

### Z. Wrong Strato package name in plan.md — corrected

Plan §1.2 listed `@dynatrace/strato-design-system` which does not exist as an NPM package. The actual package is `@dynatrace/strato-components`. Plan §1.2 and PROJECT.md have been corrected.

---

## Resolved (continued)

| # | Issue | Resolution |
|---|---|---|
| V1 | plan.md §1.1 listed Lombok | Removed from plan; confirmed not in build.gradle or any source file |
| V2 | plan.md §1.2 wrong Strato package name | Corrected to `@dynatrace/strato-components`; PROJECT.md updated accordingly |
| V3 | plan.md §6.1 described top nav bar | Updated to reflect Page + Page.Sidebar + Page.Main layout now implemented |
| R | `avg_entry_price` calculation undefined | Defined as `SUM(count * share_price) / SUM(count)` across **all** transactions (buys positive, sells negative) — mirrors `IsinTransactions.entryPrice()`. Implemented in `PortfolioService` JPQL and native SQL; plan.md §5.4 and §5.5 updated. |
| S | `isin_country` / `isin_branch` 1:1 vs 1:N | Clarified as 1:1 in PROJECT.md (each ISIN has exactly one country and one branch; mapping replaced on re-import). ImportService already implements delete+insert. V6 migration adds `UNIQUE(isin_id)` to enforce the constraint at DB level; plan.md §2 and §4.6/§4.7 updated. |
| U | ZERO depot name inconsistency | PROJECT.md use case corrected to use "ZERO" throughout (was erroneously "Trade Republic" in one use case). |

---

### AA. `show depots` use case — frontend missing

PROJECT.md defines a `show depots` use case. The backend `/api/depots` endpoint was implemented in `ReferenceDataController`. However, the frontend had no `/depots` route, no `Depots` page, and no sidebar nav entry. — **Resolved 2026-03-23**: `Depots.tsx` page created; `/depots` route added to `App.tsx`; "Depots" nav item added to `Layout.tsx`.

### AB. `show currencies` use case — backend and frontend missing

PROJECT.md defines a `show currencies` use case. Neither the backend endpoint `/api/currencies` nor the frontend page existed. The use case was also absent from plan.md §5.2 and §6.2. — **Resolved 2026-03-23**: `findAllByOrderByNameAsc()` added to `CurrencyRepository`; `GET /api/currencies` added to `ReferenceDataController`; `Currency` type added to `types/index.ts`; `Currencies.tsx` page created; `/currencies` route added to `App.tsx`; "Currencies" nav item added to `Layout.tsx`; plan.md §5.2 and §6.2 updated.

---

## Summary

### AC. Stocks table — columns not resizable, several columns clip content

The Stocks table had no resizable columns; ISIN, Name, Country, and Branch columns were too narrow. — **Resolved 2026-03-24**: `resizable` prop added to `DataTable`; column widths set: ISIN `140/140`, Name `240/240`, Country `120/80`, Branch `160/80`; PROJECT.md and plan.md §6.6 updated.

---

## Summary

### AE. All tables — columns not resizable (except Stocks)

All `DataTable` instances except Stocks were missing the `resizable` prop. — **Resolved 2026-03-24**: `resizable` added to all `DataTable` usages in Analytics, Branches, Countries, Currencies, Depots, Dashboard (both tables), and Transactions; global table convention documented in plan.md §1.2; requirement added to PROJECT.md.

### AD. Transactions page — no loading indicator, no pagination

The Transactions page fetched all rows with no visual feedback during load and displayed them in a single unbroken table. — **Resolved 2026-03-24**: `ProgressCircle` loading indicator added; `DataTablePagination` child added with `defaultPageSize={10}` and `pageSizeOptions={[10, 20, 50, 100]}`; "Show All / Paginate" toggle button added for viewing all rows; PROJECT.md and plan.md §6.5 updated.

---

## Summary

### AF. Transactions page — column widths, date format, number format, real-time filter, depot dropdown

Several requirements were missing from the Transactions page implementation: ISO date format, per-column width/minWidth, count formatted to ≤4 decimal places, ISIN real-time client-side partial filter with Clear button, depot filter as dynamic dropdown instead of free-text. — **Resolved 2026-03-24**: all rows now fetched at once; client-side filtering by ISIN (real-time, case-insensitive, partial) and depot (Select dropdown populated from loaded data); date displayed as `YYYY-MM-DD`; count uses 0–4 decimal places; column widths/minWidths set per spec; PROJECT.md use case rewritten with structured table; plan.md §6.5 updated.

---

### AH. DeGiro transaction import — share price taken from wrong column

`ImportService.importDegiroTransactions` read the share price from column 7 (`Kurs`), which is denominated in the stock's local trading currency (e.g. USD for US stocks). For non-EUR stocks this stores a non-EUR price, causing incorrect portfolio valuations. Comparison with the depot reference project (`TransactionFromDegiro` / `SharePrice`) revealed the correct approach: derive the EUR price from column 11 (`Wert EUR`) as `abs(Wert EUR) / abs(Anzahl)`. — **Resolved 2026-03-24**: `ImportService` changed to read index 11 and compute `Math.abs(eurValue) / Math.abs(count)`; PROJECT.md and plan.md §4.1 updated with column table and derivation rationale.

### AG. Transactions — Count column format clarified to exactly 2 decimal places

Count was formatted with 0–4 decimal places (`min:0, max:4`). Requirement clarified to exactly 2 decimal places matching Share Price format. — **Resolved 2026-03-24**: `fmtCount` changed to `{minimumFractionDigits:2, maximumFractionDigits:2}`; PROJECT.md column table and plan.md §6.5 updated. — **Updated 2026-03-26**: Count changed to 3 decimal places (`minimumFractionDigits:3, maximumFractionDigits:3`); negative values shown in red, positive in green via custom cell renderer.

---

### AI. DeGiro import — count=0 rows cause NaN share price

DeGiro `Transactions.csv` contains rows where `Anzahl` (count) is `0,00` — these represent non-trade entries such as fees or dividends. The share price formula `abs(Wert EUR) / abs(count)` divides by zero, producing `NaN` (when `Wert EUR` is also 0) or `Infinity`. These rows appeared as `NaN` in the Transactions view. The depot reference project (`TransactionFromDegiro`) handles this explicitly by returning `Optional.empty()` when count is zero. — **Resolved 2026-03-24**: `ImportService.importDegiroTransactions` now skips any row where `count == 0` immediately after parsing `Anzahl`. PROJECT.md and plan.md §4.1 updated to document the skip rule.

---

### AJ. Transactions — Date column too narrow; no default sort order

The Date column had `minWidth: 105` which caused `YYYY-MM-DD` dates to clip. No default sort order was defined, so rows appeared in arbitrary backend order. — **Resolved 2026-03-24**: `minWidth` increased to `120`; default sort set to descending by date (newest first). PROJECT.md and plan.md §6.5 updated.

---

Items N, O, P, Q, R, S, U, V1, V2, V3, AA, AB, AC, AD, AE, AF, AG, AH, AI, AJ resolved and applied to plan.md / PROJECT.md. Open items T, V, X remain requiring decisions or implementation work.

### AN. Dashboard DataTables missing `resizable` prop

AE states `resizable` was added to all DataTable usages including Dashboard. However, `Dashboard.tsx` still has `<DataTable ... fullWidth />` without `resizable` on both the holdings and dividend-sources tables. The global convention in PROJECT.md requires it on all DataTable instances. — **Resolved 2026-03-25**: `resizable` prop added to both `DataTable` components in `Dashboard.tsx`.

### AL. Transactions — ISIN cell top-aligned; name filter missing

ISIN cells were top-aligned because the custom cell renderer bypasses Strato's `DataTableDefaultCell` centering wrapper. No name filter existed. — **Resolved 2026-03-24**: custom cell spans now use `display:flex; align-items:center; height:100%` to restore vertical centering; same style applied to the new Name cell renderer. Name `TextInput` filter added (real-time partial case-insensitive match with Clear button); double-clicking a Name cell sets the name filter. `filteredTxns` memo updated to include name match. PROJECT.md and plan.md §6.5 updated.

---

### AK. Transactions — double-click ISIN to filter

No way to quickly filter to a single ISIN from the table itself; users had to type the ISIN manually. — **Resolved 2026-03-24**: double-clicking an ISIN cell in the Transactions table copies the ISIN into the filter input, immediately narrowing the view to that ISIN's transactions. Column definition moved into component via `useMemo`; custom `cell` renderer wraps the ISIN value in a `<span onDoubleClick>` with `cursor: pointer` and a tooltip. PROJECT.md and plan.md §6.5 updated.

---

### AM. Dashboard — "Top 5 Holdings" and "Top 5 Dividend Sources" tables not using full width

Both `DataTable` instances on the Dashboard page were missing the `fullWidth` prop, causing them to render at content width instead of stretching to the available page width. All other pages (Stocks, Transactions, Analytics, Branches, Countries, Currencies, Depots) already used `fullWidth`. — **Resolved 2026-03-25**: `fullWidth` prop added to both `DataTable` components in `Dashboard.tsx`.

---

### AO. `quote/` package (IsinsQuoteLoader) not implemented

The entire quote fetching system described in settings.md was missing: no `quote/` package, no cascade fallback sources, no `@Scheduled` task, no `isin_quote` upsert logic. `QuoteController.triggerFetch()` returned a stub response. — **Resolved 2026-03-25**: Full implementation added:
- `QuoteSource` interface + `AbstractHtmlQuoteSource` base class with HTTP client and decimal parsing utilities.
- `EcbExchangeRateProvider` for USD→EUR conversion via ECB XML feed (with 1-hour cache and static fallback).
- `CsvConfigLoader` for loading ISIN→path config CSVs from classpath.
- 10 cascade sources in `quote/sources/`: `JustEtfApiSource` (step 1), `OnvistaSource` (2), `FinanzenNetSource` (3), `CnbcSource` (4), `JustEtfHtmlSource` (5), `JustEtfApiRetrySource` (6), `FondsDiscountEurSource` (7), `FondsDiscountUsdSource` (8), `ComDirectSource` (9), `WallstreetOnlineSource` (10).
- `IsinsQuoteLoader` orchestrator: tries each source in `@Order` priority; resolved ISINs removed before next source.
- `QuoteService`: `@Scheduled(fixedDelay=60000)` task checks configured interval and triggers fetch; `triggerFetch()` fetches all held ISINs, upserts into `isin_quote`, updates `settings.quote.last.fetch.timestamp`.
- `QuoteController.triggerFetch()` wired to `QuoteService`; returns `{ status, fetchedCount }`.
- Empty config CSV placeholders created: `finanzennet.csv`, `onvista.csv`, `wallstreetonline.csv`, `isin.symbol.csv`.
- Settings page updated to show fetched count and refresh last-fetch timestamp after fetch.

### AP. Stocks page — missing country/branch filter dropdowns

The stocks spec (stocks.md) requires a filter bar with country and branch dropdowns, but `Stocks.tsx` had no filters. — **Resolved 2026-03-25**: Country and Branch `Select` dropdowns added, populated from loaded data. Loading indicator (`ProgressCircle`) added. Refresh button added. Filtered count displayed in heading (e.g. "42 of 50").

### AQ. Depots endpoint — not sorted alphabetically

The reference-data spec requires all reference lists sorted alphabetically. `GET /api/depots` used `findAll()` (unsorted). — **Resolved 2026-03-25**: `findAllByOrderByNameAsc()` added to `DepotRepository`; `ReferenceDataController.getDepots()` updated.

### AR. SPA fallback — client-side routing not supported by backend

Opening a frontend route directly (e.g. `/transactions`) returned a 404 from the backend because no catch-all handler existed to serve `index.html` for non-API routes. — **Resolved 2026-03-25**: `SpaWebConfig.java` added: `WebMvcConfigurer` with `PathResourceResolver` that serves `classpath:/static/index.html` for any request not matching a real static file.

---

### AS. Ticker symbol support — data model, import, and display page

The data model defined `ticker_symbol` and `isin_ticker` tables, and import.md specified a `POST /api/import/ticker-symbols` endpoint and `ticker_symbol.csv` parsing, but none of this was implemented. A new `/ticker-symbols` page was specified in `ticker-symbols.md` but had no backend or frontend code. — **Resolved 2026-03-25**:
- `V7__ticker_symbol_add_isin_id.sql` migration adds `isin_id` FK column to `ticker_symbol`.
- `TickerSymbol` JPA model + `TickerSymbolRepository` created.
- `ImportService.importTickerSymbols()`: parses `ISIN;Name;TickerSymbol` CSV, upserts ISIN/name/ticker, creates `isin_ticker` junction mapping.
- `ImportController`: `POST /api/import/ticker-symbols` endpoint added.
- `TickerSymbolDto` + `TickerSymbolController`: `GET /api/ticker-symbols` returns ISIN/ticker/name via native SQL join.
- Frontend: `TickerSymbols.tsx` page (3-column sortable table), route `/ticker-symbols`, nav entry, import card.

### AT. Dashboard — timestamp format, label text, missing "—" for null

Dashboard spec (`dashboard.md`) requires the last-fetch timestamp formatted as `DD.MM.YYYY HH:mm` with label "Last updated:" and `—` when null. Implementation used `toLocaleString('de-DE')` (wrong format), label "Last quote fetch:", and hid the line entirely when null. Dashboard tables were also missing the `sortable` prop. — **Resolved 2026-03-27**: Timestamp format fixed to `DD.MM.YYYY HH:mm`, label changed to "Last updated:", `—` shown when null, `sortable` added to both DataTables.

### AU. Stocks — column headers abbreviated

The stocks spec (`stocks.md`) defines full column headers (e.g. "Total Shares", "Avg Entry Price", "Current Quote", "Performance (%)", "Expected Dividend/Share", "Est. Annual Income"). The implementation used abbreviated headers ("Shares", "Avg Price", "Quote", "Perf %", "Div/Share", "Est. Income"). — **Resolved 2026-03-27**: All Stocks column headers updated to match spec exactly.

### AV. Settings — missing custom interval input, timestamp format

The settings spec (`settings.md`) requires a custom interval input alongside the dropdown. Implementation had only predefined intervals with no custom option. The last-fetch timestamp also used the wrong format. — **Resolved 2026-03-27**: "Custom" option added to interval dropdown with a `TextInput` for custom minutes. Timestamp format changed to `DD.MM.YYYY HH:mm`, displays `—` when null. Label changed to "Fetch Interval".

### AW. IsinNames — missing secondary ISIN sort in frontend

The ISIN names spec (`isin-names.md`) requires "sorted by name ascending, then ISIN ascending". Frontend only set `defaultSortBy` with name ascending, missing the ISIN secondary sort. — **Resolved 2026-03-27**: Secondary `{ id: 'isin', desc: false }` added to `defaultSortBy`.

### AX. Transactions — summary displayed extra count sum info

The transactions spec (`transactions.md`) requires the summary to show `"N transactions"` or `"N of M transactions"`. Implementation additionally displayed a count sum (total shares) which was not in the spec. — **Resolved 2026-03-27**: Extra count sum removed from summary display; unused `filteredCountSum` and `totalCountSum` memo computations removed.

### AY. Import — error display limited to 3 errors

Import page only displayed the first 3 error messages (`.slice(0, 3)`). The spec requires feedback about all errors. — **Resolved 2026-03-27**: `.slice(0, 3)` removed, all errors are now shown.

### AZ. Analytics SQL — missing spaces in query string concatenation

`PortfolioService.getDiversification()` used Java text block concatenation that produced `JOINisin_country` and `JOINcountry` (missing spaces). The analytics endpoints returned 500 errors. — **Resolved 2026-03-27**: SQL rewritten using plain string concatenation with explicit spaces.

### T. Testing — REST API integration tests added

PROJECT.md requires "a comprehensive test suite that covers all major functionality and edge cases." No REST API tests existed. — **Resolved 2026-03-27**: Integration tests added for all 12 REST controllers using `@SpringBootTest` + `@AutoConfigureMockMvc` with H2 test profile:
- `ReferenceDataControllerTest`: depots, currencies, countries, branches (GET + export)
- `DashboardControllerTest`: dashboard structure, empty portfolio, export
- `TransactionControllerTest`: empty list, filters, date filters, export
- `StockControllerTest`: empty positions, export with filters/sort
- `AnalyticsControllerTest`: diversification structure, export
- `QuoteControllerTest`: settings CRUD, enable/disable, interval validation, trigger fetch
- `ImportControllerTest`: branches/countries/dividends/ticker-symbols import, empty CSV, invalid format
- `IsinNameControllerTest` + `TickerSymbolControllerTest`: GET + export
- `ImportToQueryIntegrationTest`: end-to-end pipeline (import countries/branches/dividends/ticker-symbols → verify via query endpoints)

> Last reviewed: 2026-03-27 — Gaps AT–AZ, T resolved. Open items: V (Clerk auth), X (parser/ extraction).
