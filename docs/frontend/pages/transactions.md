# Transactions

> Route: `/transactions` — Sortable, filterable transaction table.


### UI Conventions

follow ui conventions in [ui.md](ui.md)

## Use Case

- The UI shall provide a view displaying transactions fetched from the backend.
- Filtering, sorting, and aggregation are performed server-side; the frontend sends filter criteria and sort parameters as query params and renders the response.
- Table conventions per [ui.md](ui.md) apply.

---

## REST API

### `GET /api/transactions`

| Query Param | Description |
|-------------|-------------|
| `isin` | Partial, case-insensitive ISIN match (optional) |
| `name` | Partial, case-insensitive name match (optional) |
| `depot` | Exact depot name match (optional) |
| `fromDate` | Filter start date (optional) |
| `toDate` | Filter end date (optional) |
| `sortField` | One of: `date`, `isin`, `name`, `depot`, `count`, `sharePrice` (default: `date`) |
| `sortDir` | `asc` or `desc` (default: `desc`) |
| `page` | Page number, 1-based (default: `1`) |
| `pageSize` | Items per page; one of `[10, 20, 50, 100, -1]`; `-1` = all (default: `10`) |

Returns a paginated envelope with the filtered/sorted list, counts, and aggregation:
```json
{
  "items": [ … ],
  "page": 1,
  "pageSize": 10,
  "totalItems": 50,
  "totalPages": 5,
  "filteredCount": 50,
  "totalCount": 120,
  "sumCount": 1234.500
}
```
- `filteredCount`: number of transactions matching the current filters (before pagination).
- `totalCount`: total number of transactions regardless of any filters. Required for the "N of M" row count display (see [ui.md](ui.md)).

### `GET /api/transactions/filters` — Available filter options

Returns distinct depot names from all transactions:
```json
{ "depots": ["DeGiro", "ZERO"] }
```

---

## UI Specification

### Columns

| Column | Format | Alignment | `width` | `minWidth` |
|--------|--------|-----------|---------|------------|
| Date | Pre-formatted by backend as `DD-MM-YYYY`; rendered as-is; default sort descending | centre | 105 | 105 |
| ISIN | Double-click → `setIsinFilter` | left | 140 | 140 |
| Name | Double-click → `setNameFilter` | left | 240 | 120 |
| Depot | Plain text | left | 100 | 80 |
| Count | 3 decimal places; negative red, positive green | right | 100 | 80 |
| Share Price | Number format per [ui.md](ui.md) | right | 110 | 100 |

### Summary
- **Row count:** When filters are active, display `"N of M transactions"` (e.g. `"12 of 50 transactions"`) where N = `filteredCount` and M = `totalCount`. When no filters are active, display `"M transactions"` (e.g. `"50 transactions"`).
- Display the sum of the Count column reflecting current filters, formatted with 3 decimal places in German locale (e.g. `"Sum count: 1.234,500"`).

### Filtering

- **ISIN filter**: free-text input; case-insensitive partial match updated in real time as the user types (e.g. typing `DE000` shows all transactions whose ISIN contains that substring). A Clear button appears next to the field and resets the filter. Double-clicking an ISIN value in the table copies it to this filter, immediately showing only that ISIN's transactions.
- **Name filter**: free-text input; case-insensitive partial match updated in real time as the user types. A Clear button appears next to the field and resets the filter. Double-clicking a Name value in the table copies it to this filter, immediately showing only transactions with that stock name.
- **Depot filter**: multi-select dropdown populated from `GET /api/transactions/filters`. The user can select one or more depots; when none are selected all depots are shown. Multiple values are sent as a comma-separated string (e.g. `depot=DeGiro,ZERO`). The backend splits on comma and filters using set membership.
- **Date range filter**: `fromDate` and `toDate` date inputs allow the user to filter transactions to a specific date range. Both are optional. When set, they are sent as query params to the backend.

**Filter bar:** ISIN `TextInput` (real-time partial, case-insensitive, 300 ms debounce; Clear button; double-click cell fills); Name `TextInput` (same); Depot `MultiSelect` (multi-select dropdown; options from `/api/transactions/filters`); From Date `DateInput`; To Date `DateInput`; Refresh `Button`.

**State preservation:** Filter values, sort state, and pagination are preserved in `sessionStorage` when navigating away and back (see [ui.md](ui.md)).

**Debounce:** ISIN and Name text inputs shall be debounced (300 ms) per [ui.md](ui.md) to avoid excessive requests on every keystroke.

All filter values and sort state are sent as query params to `GET /api/transactions`; the backend returns the filtered, sorted data and the sum of count.

### Loading and Refresh

- A loading indicator (spinner) is displayed while data is being fetched from the backend.
- **Filter inputs must remain mounted during loading.** The loading spinner replaces only the table/data area, not the filter bar. This ensures the user can continue typing in filter fields without losing focus.
- A Refresh button reloads all transactions from the backend.
- **Loading:** `ProgressCircle size="small"` + "Loading…".

### Name Filter — All Transactions for Matching Stocks

When filtering by name, the result must include **all** transactions (both buy and sell) for any stock whose name matches the filter. The backend must not exclude sell transactions (negative count) just because name resolution returns null for some transactions. Implementation: collect all ISIN codes whose resolved name matches the filter, then include all transactions with those ISIN codes.

