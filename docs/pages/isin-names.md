# ISIN Names

> Route: `/isin-names` — ISIN to stock name mappings.

### Conventions

Follow UI conventions in [ui.md](../ui.md). Follow testing conventions in [testing.md](../testing.md).

## Use Case

The UI should provide a view that displays all ISINs along with their associated stock names. An ISIN can have multiple names (e.g., different names used across brokers or import files). The data should be fetched from the backend via a REST API endpoint that retrieves ISIN and name data from the `isin` and `isin_name` tables.

---

## REST API

### `GET /api/isin-names`

| Query Param | Description |
|-------------|-------------|
| `sortField` | Sort field: `isin`, `name` (default: `name`) |
| `sortDir` | `asc` or `desc` (default: `asc`) |
| `page` | Page number, 1-based (default: `1`) |
| `pageSize` | Items per page; one of `[10, 20, 50, 100, -1]`; `-1` = all (default: `10`) |

Returns a paginated envelope per [ui.md](../ui.md):
```json
{
  "items": [
    { "isin": "DE000BASF111", "name": "BASF SE" },
    { "isin": "DE000BASF111", "name": "BASF" },
    { "isin": "IE00B4L5Y983", "name": "iShares Core MSCI World ETF" },
    { "isin": "US0378331005", "name": "Apple Inc." }
  ],
  "page": 1,
  "pageSize": 10,
  "totalItems": 4,
  "totalPages": 1
}
```

**Notes:**
- An ISIN can appear multiple times if it has multiple names.
- Sorted by name ascending, then ISIN ascending.

---

## UI Specification

### Table

| Column | Alignment | `width` | `minWidth` |
|--------|-----------|---------|------------|
| ISIN | left | 140 | 140 |
| Name | left | 400 | 200 |

### Features

- Table conventions per [ui.md](../ui.md) apply (pagination, sortable, resizable, full width).
- Default sort: name ascending. Sort changes trigger a re-fetch with `sortField` and `sortDir` query params.
- Loading indicator while fetching data.
- Count display: `"N ISIN name mappings"`.

