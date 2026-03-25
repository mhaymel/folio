# Transactions

> Route: `/transactions` — Sortable, filterable transaction table.

## Use Case

The UI shall provide a view displaying all transactions fetched from the backend in a sortable, resizable table. All rows are loaded at once and filtered client-side.

---

## REST API

### `GET /api/transactions`

| Query Param | Description |
|-------------|-------------|
| `fromDate` | Filter start date |
| `toDate` | Filter end date |
| `isin` | Filter by ISIN |
| `depotId` | Filter by depot |
| `page` | Page number |
| `size` | Page size |
| `sort` | Sort field + direction |

Returns paginated list with ISIN, security name (JOIN `isin_name`), depot, date, count, share price.

---

## UI Specification

### Columns

| Column | Format | Alignment | `width` | `minWidth` |
|--------|--------|-----------|---------|------------|
| Date | `DD-MM-YYYY`; `sortAccessor` returns ISO `YYYY-MM-DD` for chronological sort; default sort descending | left | 105 | 105 |
| ISIN | plain; custom cell with `display:flex; align-items:center; height:100%`; double-click → `setIsinFilter` | left | 140 | 140 |
| Name | plain; custom cell with `display:flex; align-items:center; height:100%`; double-click → `setNameFilter` | left | 240 | 120 |
| Depot | plain | left | 100 | 80 |
| Count | `toLocaleString('de-DE', {minimumFractionDigits:2, maximumFractionDigits:2})` | right | — | 80 |
| Share Price | `toLocaleString('de-DE', {minimumFractionDigits:2, maximumFractionDigits:2})` | right | — | 100 |

### Filtering

- **ISIN filter**: free-text input; case-insensitive partial match updated in real time as the user types (e.g. typing `DE000` shows all transactions whose ISIN contains that substring). A Clear button appears next to the field and resets the filter. Double-clicking an ISIN value in the table copies it to this filter, immediately showing only that ISIN's transactions.
- **Name filter**: free-text input; case-insensitive partial match updated in real time as the user types. A Clear button appears next to the field and resets the filter. Double-clicking a Name value in the table copies it to this filter, immediately showing only transactions with that security name.
- **Depot filter**: dropdown listing all depots present in the loaded data plus an "All depots" option. Selecting a depot restricts the view to that depot's transactions; selecting "All depots" shows all transactions.

**Filter bar:** ISIN `TextInput` (real-time partial, case-insensitive; Clear button; double-click cell fills); Name `TextInput` (same); Depot `Select<string>` (`""` = "All depots" + sorted unique names); Refresh `Button`.

### Loading and Refresh

- A loading indicator (spinner) is displayed while data is being fetched from the backend.
- A Refresh button reloads all transactions from the backend.
- The row count shown above the table reflects the active filter (e.g. "42 of 16140 transactions").
- **Loading:** `ProgressCircle size="small"` + "Loading…".

### Row Count

`"N of M transactions"` filtered / `"M transactions"` unfiltered.

### Pagination

- Default page size: 10 rows per page.
- Page size selector options: 10, 20, 50, 100 rows per page.
- A "Show All / Paginate" toggle switches between paginated and full-table view.
- `DataTablePagination`; `defaultPageSize=10`; options `[10,20,50,100]`.

