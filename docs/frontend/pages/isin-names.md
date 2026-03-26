# ISIN Names

> Route: `/isin-names` — ISIN to stock name mappings.

## Use Case

The UI should provide a view that displays all ISINs along with their associated stock names. An ISIN can have multiple names (e.g., different names used across brokers or import files). The data should be fetched from the backend via a REST API endpoint that retrieves ISIN and name data from the `isin` and `isin_name` tables.

---

## REST API

### `GET /api/isin-names`

Returns all ISIN to name mappings, sorted alphabetically by ISIN.

**Response:**
```json
[
  { "isin": "DE000BASF111", "name": "BASF SE" },
  { "isin": "DE000BASF111", "name": "BASF" },
  { "isin": "IE00B4L5Y983", "name": "iShares Core MSCI World ETF" },
  { "isin": "US0378331005", "name": "Apple Inc." }
]
```

**Notes:**
- An ISIN can appear multiple times if it has multiple names.
- Sorted by name ascending, then ISIN ascending.

---

## UI Specification

### Table

| Column | Description | Alignment | Width | MinWidth |
|--------|-------------|-----------|-------|----------|
| ISIN | Stock identifier | left | 140 | 140 |
| Name | Stock name | left | 400 | 200 |

### Features

- Table conventions per [ui.md](ui.md) apply (pagination, sortable, resizable, full width).
- Default sort: name ascending.
- Loading indicator while fetching data.
- Count display: `"N ISIN name mappings"`.

