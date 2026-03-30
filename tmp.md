# Dividend Payments

> Route: `/dividend-payments` — Sortable, filterable dividend payment history.

### UI Conventions

Follow UI conventions in [ui.md](ui.md).

## Use Case

Display all dividend payments received in the portfolio, including the date, amount, currency, stock details (ISIN, name), and depot where the payment was received. This page provides users with a complete history of dividend income across all depots. Users can filter by stock name, ISIN, or depot, sort by any column, and view aggregated totals. Table conventions per [ui.md](ui.md) apply (sortable, resizable, full width).

The page can be accessed from the main menu under the "Dividend Payments" item in the sidebar navigation.

---

## REST API

### `GET /api/dividend-payments` — Dividend payment history

| Query Param | Description |
|-------------|-------------|
| `isin` | Partial, case-insensitive ISIN match (optional) |
| `name` | Partial, case-insensitive stock name match (optional) |
| `depot` | Exact depot name match (optional) |
| `fromDate` | Filter start date (optional, ISO format `YYYY-MM-DD`) |
| `toDate` | Filter end date (optional, ISO format `YYYY-MM-DD`) |
| `sortField` | One of: `timestamp`, `isin`, `name`, `depot`, `value`, `currency` (default: `timestamp`) |
| `sortDir` | `asc` or `desc` (default: `desc`) |
| `page` | Page number, 1-based (default: `1`) |
| `pageSize` | Items per page; one of `[10, 20, 50, 100, -1]`; `-1` = all (default: `10`) |

Returns a paginated envelope with filtered/sorted dividend payments and aggregated totals:
```json
{
  "items": [
    {
      "id": 1,
      "timestamp": "15.03.2026 10:30",
      "isin": "DE000BASF111",
      "name": "BASF SE",
      "depot": "DeGiro",
      "currency": "EUR",
      "value": 34.00
    }
  ],
  "page": 1,
  "pageSize": 10,
  "totalItems": 42,
  "totalPages": 5,
  "sumValue": 1250.50
}
```

- **`timestamp`**: Pre-formatted by backend as `DD.MM.YYYY HH:mm`.
- **`name`**: Stock name resolved via `isin_name`; null if not available.
- **`sumValue`**: Sum of `value` field across all filtered results (before pagination).

### `GET /api/dividend-payments/filters` — Available filter options

Returns distinct depot names from all dividend payments:
```json
{ "depots": ["DeGiro", "ZERO"] }
```

### `GET /api/dividend-payments/export?format={csv|xlsx}` — Export dividend payments

Query params: same as `GET /api/dividend-payments` (filters, sorting).

Returns a CSV or Excel file with all filtered dividend payments (ignores pagination).

**Headers:**
- `Content-Type`: `text/csv; charset=UTF-8` or `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- `Content-Disposition`: `attachment; filename="dividend-payments.{csv|xlsx}"`

---

## UI Specification

### Columns

| Column | Format | Alignment | `width` | `minWidth` |
|--------|--------|-----------|---------|------------|
| Date | Pre-formatted by backend as `DD.MM.YYYY HH:mm`; default sort descending | center | 140 | 140 |
| ISIN | Double-click → `setIsinFilter` | left | 140 | 140 |
| Name | Double-click → `setNameFilter`; display `—` if null | left | 240 | 120 |
| Depot | Double-click → `setDepotFilter` | left | 100 | 80 |
| Currency | Plain text | center | 80 | 60 |
| Amount | Number format per [ui.md](ui.md); 2 decimal places | right | 110 | 100 |

### Summary

- **Row count:** Display `"N dividend payments"` (e.g., `"42 dividend payments"`).
- **Sum of amounts:** Display the sum of the Amount column reflecting current filters, formatted with 2 decimal places in German locale (e.g., `"Total: 1.250,50 EUR"`). Note: If multiple currencies are present in filtered results, display separate sums per currency (e.g., `"Total: 1.000,00 EUR | 250,00 USD"`).

### Filtering

- **ISIN filter**: Free-text input; case-insensitive partial match updated in real time with 300 ms debounce. Clear button resets the filter. Double-clicking an ISIN value in the table copies it to this filter.
- **Name filter**: Free-text input; case-insensitive partial match updated in real time with 300 ms debounce. Clear button resets the filter. Double-clicking a Name value in the table copies it to this filter.
- **Depot filter**: Multi-select dropdown populated from `GET /api/dividend-payments/filters`. Multiple selections sent as comma-separated string (e.g., `depot=DeGiro,ZERO`).
- **Date range filter**: `fromDate` and `toDate` date inputs allow filtering to a specific date range. Both are optional.

**Filter bar:** ISIN `TextInput` (real-time partial, case-insensitive, 300 ms debounce; Clear button; double-click cell fills); Name `TextInput` (same); Depot `MultiSelect`; From Date `DateInput`; To Date `DateInput`; Refresh `Button`.

**State preservation:** Filter values, sort state, and pagination are preserved in `sessionStorage` when navigating away and back (see [ui.md](ui.md)).

### Export

- **Export buttons** (CSV and Excel) positioned top-right, next to "Show All" button.
- Export respects current filters and sorting but ignores pagination (exports all filtered results).

### Loading and Refresh

- A loading indicator (spinner) is displayed while data is being fetched.
- **Filter inputs must remain mounted during loading.** The loading spinner replaces only the table/data area, not the filter bar.
- A Refresh button reloads all dividend payments and filter options from the backend.
- **Loading:** `ProgressCircle size="small"` + "Loading…".

### Pagination

- Pagination controls at the bottom of the table (per [ui.md](ui.md)).
- Page size selector with options: 10, 20, 50, 100, Show All.
- "Show All" button toggles between paginated view and showing all results.

### Interactions

- **Double-click on ISIN cell:** Copies ISIN to ISIN filter and triggers re-fetch.
- **Double-click on Name cell:** Copies name to Name filter and triggers re-fetch.
- **Double-click on Depot cell:** Adds depot to Depot multi-select filter and triggers re-fetch.

---

## Implementation Notes

### Backend

- **Entity:** `DividendPayment` (already exists: `id`, `timestamp`, `isin_id`, `depot_id`, `currency_id`, `value`).
- **DTO:** Create `DividendPaymentDto` with fields: `id`, `timestamp` (formatted string), `isin`, `name`, `depot`, `currency`, `value`.
- **Controller:** `DividendPaymentController` with endpoints:
  - `GET /api/dividend-payments` (paginated, filtered, sorted)
  - `GET /api/dividend-payments/filters`
  - `GET /api/dividend-payments/export`
- **Service:** `DividendPaymentService` to query `DividendPayment` repository, join with `Isin`, `IsinName`, `Depot`, `Currency`, apply filters, sort, paginate, and calculate `sumValue`.
- **Repository:** `DividendPaymentRepository extends JpaRepository<DividendPayment, Integer>`.

### Frontend

- **Page:** `DividendPayments.tsx` at route `/dividend-payments`.
- **Hook:** Use `useServerTable` hook (same pattern as Transactions, Stocks pages).
- **Components:** Reuse `ExportButtons`, `PaginationControls`, `MultiSelect` from existing pages.
- **Navigation:** Add "Dividend Payments" menu item to `Layout.tsx` sidebar.

### Currency Handling

- If all filtered dividend payments use the same currency, display single total (e.g., `"Total: 1.250,50 EUR"`).
- If multiple currencies are present, group by currency and display separate totals (e.g., `"Total: 1.000,00 EUR | 250,00 USD"`).
- The backend should return `sumValue` as a single number for simplicity; frontend can fetch currency distribution if needed, or backend can return a map of `sumValueByCurrency`.

**Recommendation for MVP:** Display a single total assuming all dividends are in the same currency (EUR). Add multi-currency support in a future iteration if needed.
