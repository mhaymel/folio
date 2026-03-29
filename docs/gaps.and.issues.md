# Gaps & Issues

Open gaps and issues identified between the project specification and the current implementation.

---

### V. Clerk authentication not yet implemented

Clerk is planned for user management and authentication but zero code exists. `SecurityConfig.java` uses a `folio.security.enabled` flag (defaulting to `false`) that permits all requests. No `ClerkJwtFilter`, no `nimbus-jose-jwt` dependency, no `@clerk/clerk-react` in the frontend. No protected routes, no `<ClerkProvider>`, no auth hooks.

### VI. Row Count Display — "N of M" Format Missing

- **Spec ([ui.md](frontend/pages/ui.md)):** Filtered: `"N of M items"`. Unfiltered: `"M items"`.
- **Transactions:** Always shows `"{filteredCount} transactions"` — never displays the unfiltered total. The backend `TransactionPaginatedResponseDto` has `filteredCount` but no `totalCount`, so the "N of M" pattern is impossible with the current API response.
- **Stocks:** Always shows `"{table.totalItems} stocks"` — no "N of M" format when filters are active.
- **Resolution:** Backend must add `totalCount` (unfiltered) to paginated responses. Frontend must display "N of M" when filters are active.

### VII. Transactions — Missing Refresh Button

- **Spec ([transactions.md](frontend/pages/transactions.md)):** "A Refresh button reloads all transactions from the backend."
- **Status:** The Stocks page has a Refresh button, but the Transactions page does not.

### VIII. Debounce on Text Filter Inputs — Not Implemented

- **Spec ([ui.md](frontend/pages/ui.md)):** "All free-text filter inputs shall be debounced with a 300 ms delay."
- **Status:** Both Transactions and Stocks pages trigger an API refetch immediately on every keystroke in the ISIN and Name text inputs. No debounce logic exists.

### IX. Dashboard — Missing Column Widths and MinWidths

- **Spec ([dashboard.md](frontend/pages/dashboard.md)):** Top 5 Holdings and Top 5 Dividend Sources tables shall have specific `width`/`minWidth` per column (e.g. ISIN: 140/140, Name: 240/200, Amount: 160/120).
- **Status:** `Dashboard.tsx` columns do not specify `width` or `minWidth` properties.

### X. Stocks — Column Header "Count" Should Be "Total Shares"

- **Spec ([stocks.md](frontend/pages/stocks.md)):** Column header is `"Total Shares"`.
- **Status:** `Stocks.tsx` uses `header: 'Count'` for this column. The sort field is also `count` instead of `totalShares` as specified.

### XI. Transactions — fromDate/toDate Filters Not Exposed in UI

- **Spec ([transactions.md](frontend/pages/transactions.md)):** API supports `fromDate` and `toDate` query params. UI shall expose date range filter controls.
- **Status:** The backend endpoint accepts `fromDate`/`toDate`, but the Transactions UI does not expose any date range filter controls.

### XII. Transactions — Missing `totalCount` (Unfiltered) in Response

- **Spec ([transactions.md](frontend/pages/transactions.md)):** The envelope response should include `totalCount` (count of all transactions regardless of filters) alongside `filteredCount`.
- **Status:** `TransactionController` computes `filteredCount = data.size()` but never computes the total unfiltered count. The "N of M" row count display is therefore impossible.

### XIII. Missing Test Files

- **Spec ([ui.md](frontend/pages/ui.md)):** Each page and shared component shall have a corresponding `.test.tsx` file.
- **Status:** The following pages have no test files: `Analytics.tsx`, `Branches.tsx`, `Currencies.tsx`, `Depots.tsx`, `TickerSymbols.tsx`, `IsinNames.tsx`, `Import.tsx`. Missing `ServerTable.test.tsx` for the shared `ServerTable` component.

### X. `parser/` package — intermediate types extracted

The `parser/` package contains intermediate value objects (`ParsedTransaction`, `ParsedDividendPayment`, `ParsedDividend`, `ParsedBranch`, `ParsedCountry`, `ParsedTickerSymbol`) used by `ImportService` to separate file reading from database writes. CSV line parsing logic (`parseGermanDouble`, `parseCsvLine`) still lives in `ImportService`.