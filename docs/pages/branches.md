# Branches

> Route: `/branches` — Alphabetical list of branches.

## Use Case

The UI should provide a view that displays the branches, sorted alphabetically. The branches should be fetched from the backend via a REST API endpoint that retrieves branch data from the database.

---

## REST API

### `GET /api/branches`

| Query Param | Description |
|-------------|-------------|
| `sortField` | Sort field (default: `name`) |
| `sortDir` | `asc` or `desc` (default: `asc`) |
| `page` | Page number, 1-based (default: `1`) |
| `pageSize` | Items per page; one of `[10, 20, 50, 100, -1]`; `-1` = all (default: `10`) |

Returns a paginated envelope per [ui.md](../ui.md):
```json
{
  "items": [
    { "id": 1, "name": "Energy" },
    { "id": 2, "name": "Technology" },
    { "id": 3, "name": "Healthcare" }
  ],
  "page": 1,
  "pageSize": 10,
  "totalItems": 3,
  "totalPages": 1
}
```

---

## UI Specification

### Columns

| Column | Alignment | `width` | `minWidth` |
|--------|-----------|---------|------------|
| Name | left | 300 | 200 |

- Table conventions per [ui.md](../ui.md) apply (sortable, resizable, full width).
- Default sort: name ascending. Sort changes trigger a re-fetch with `sortField` and `sortDir` query params.

