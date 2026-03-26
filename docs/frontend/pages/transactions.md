# Transactions

> Route: `/transactions` — Sortable, filterable transaction table.


### UI Conventions

follow ui conventions in [ui.md](ui.md)

## Use Case

- The UI shall provide a view displaying all transactions fetched from the backend. 
- All rows are loaded at once and filtered client-side. 
- Table conventions per [ui.md](ui.md) apply.

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

Returns paginated list with ISIN, stock name (JOIN `isin_name`), depot, date, count, share price.

---

## UI Specification

### Columns

| Column | Format | Alignment | `width` | `minWidth` |
|--------|--------|-----------|---------|------------|
| Date | Date format per [ui.md](ui.md); `sortAccessor` returns ISO format for chronological sort; default sort descending | left | 105 | 105 |
| ISIN | plain; custom cell with `display:flex; align-items:center; height:100%`; double-click → `setIsinFilter` | left | 140 | 140 |
| Name | plain; custom cell with `display:flex; align-items:center; height:100%`; double-click → `setNameFilter` | left | 240 | 120 |
| Depot | plain | left | 100 | 80 |
| Count | Number format per [ui.md](ui.md) with 3 decimal places; negative values red, positive values green | right | — | 80 |
| Share Price | Number format per [ui.md](ui.md) | right | — | 100 |

### Summary 
- Display sum of count of transactions reflecting current filters, e.g. "42 transactions" or "12 of 50 transactions" if a filter is active.

### Filtering

- **ISIN filter**: free-text input; case-insensitive partial match updated in real time as the user types (e.g. typing `DE000` shows all transactions whose ISIN contains that substring). A Clear button appears next to the field and resets the filter. Double-clicking an ISIN value in the table copies it to this filter, immediately showing only that ISIN's transactions.
- **Name filter**: free-text input; case-insensitive partial match updated in real time as the user types. A Clear button appears next to the field and resets the filter. Double-clicking a Name value in the table copies it to this filter, immediately showing only transactions with that stock name.
- **Depot filter**: dropdown listing all depots present in the loaded data plus an "All depots" option. Selecting a depot restricts the view to that depot's transactions; selecting "All depots" shows all transactions.

**Filter bar:** ISIN `TextInput` (real-time partial, case-insensitive; Clear button; double-click cell fills); Name `TextInput` (same); Depot `Select<string>` (`""` = "All depots" + sorted unique names); Refresh `Button`.

### Loading and Refresh

- A loading indicator (spinner) is displayed while data is being fetched from the backend.
- A Refresh button reloads all transactions from the backend.
- **Loading:** `ProgressCircle size="small"` + "Loading…".

