# Reference Data Pages

> Routes: `/depots`, `/currencies` — Simple alphabetical list views.

## Use Cases


### Show Currencies
The UI should provide a view that displays the currencies, sorted alphabetically. The currencies should be fetched from the backend via a REST API endpoint that retrieves currency data from the database.

### Show Depots
The UI should provide a view that displays the depots, sorted alphabetically. The depots should be fetched from the backend via a REST API endpoint that retrieves depot data from the database.

---

## REST API

### Reference Data Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/depots?sortField=&sortDir=&page=&pageSize=` | Depots, sorted and paginated |
| GET | `/api/currencies?sortField=&sortDir=&page=&pageSize=` | Currencies, sorted and paginated |

Both endpoints accept optional `sortField` (default: `name`), `sortDir` (default: `asc`), `page` (default: `1`), and `pageSize` (default: `10`; `-1` = all) query params. Returns a paginated envelope per [ui.md](ui.md).

---

## UI Specification

### Columns (both pages)

| Column | Alignment | `width` | `minWidth` |
|--------|-----------|---------|------------|
| Name | left | 300 | 200 |

- Table conventions per [ui.md](ui.md) apply (sortable, resizable, full width).
- Default sort: name ascending. Sort changes trigger a re-fetch with `sortField` and `sortDir` query params.

