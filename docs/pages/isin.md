# ISINs

> Route: `/isins` — all ISINs known to the system.

Follow UI conventions in [ui.md](../ui.md) and testing conventions in [testing.md](../testing.md).

---

## Overview

Lists every ISIN stored in the database, together with its associated name, ticker symbol, country, and branch. An ISIN enters the system when a transaction referencing it is imported. ISINs with no transactions are never created.

---

## REST API

### `GET /api/isins`

| Query Param | Description |
|-------------|-------------|
| `isin` | Filter by ISIN (substring, case-insensitive) |
| `tickerSymbol` | Filter by ticker symbol (substring, case-insensitive) |
| `name` | Filter by name (substring, case-insensitive) |
| `country` | Filter by country — comma-separated list for multi-select |
| `branch` | Filter by branch — comma-separated list for multi-select |
| `sortField` | `isin`, `tickerSymbol`, `name`, `country`, `branch` (default: `isin`) |
| `sortDir` | `asc` or `desc` (default: `asc`) |
| `page` | Page number, 1-based (default: `1`) |
| `pageSize` | Items per page; one of `[10, 20, 50, 100, -1]`; `-1` = all (default: `10`) |

Returns a paginated envelope per [ui.md](../ui.md):

```json
{
  "items": [
    {
      "isin": "DE000BASF111",
      "tickerSymbol": "BASF.DE",
      "name": "BASF SE",
      "country": "Germany",
      "branch": "Chemicals"
    }
  ],
  "page": 1,
  "pageSize": 10,
  "totalItems": 42,
  "totalPages": 5
}
```

**Notes:**
- Each row represents one ISIN. If an ISIN has multiple names, the first is used. If it has multiple ticker symbols, the first is used.
- `tickerSymbol`, `name`, `country`, and `branch` are `null` when not set.

---

### `GET /api/isins/filters`

Returns distinct filter option values for the multi-select dropdowns.

```json
{
  "countries": ["Austria", "Germany", "USA"],
  "branches":  ["Chemicals", "Finance", "Technology"]
}
```

---

### `GET /api/isins/export`

Exports the current filtered/sorted result as CSV or Excel.

| Query Param | Description |
|-------------|-------------|
| `format` | `csv` or `excel` (default: `csv`) |
| `isin` | Same filter params as the main endpoint |
| `tickerSymbol` | |
| `name` | |
| `country` | |
| `branch` | |
| `sortField` | |
| `sortDir` | |

---

## UI Specification

### Filters

| Control | Type | Param |
|---------|------|-------|
| ISIN | Text input | `isin` |
| Ticker Symbol | Text input | `tickerSymbol` |
| Name | Text input | `name` |
| Country | Multi-select dropdown | `country` |
| Branch | Multi-select dropdown | `branch` |
| Clear button | Resets all filters | — |
| Refresh button | Reloads data and filter options | — |

### Table

| Column | Alignment | Width | Min width |
|--------|-----------|-------|-----------|
| ISIN | left | 140 | 140 |
| Ticker Symbol | left | 100 | 80 |
| Name | left | 240 | 120 |
| Country | left | 120 | 80 |
| Branch | left | 160 | 80 |

### Features

- Table conventions per [ui.md](../ui.md) apply: sortable, resizable, full-width, server-side pagination.
- Default sort: ISIN ascending.
- Count display: `"N ISINs"`.
- Export buttons (CSV / Excel).
- Show All / Paginate toggle.
- Double-click on ISIN cell sets the ISIN filter. Double-click on Name cell sets the name filter.
- ISIN column renders an `IsinCell` component with quick-filter icon.
