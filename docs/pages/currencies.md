# Currencies

> Route: `/currencies` — Alphabetical list of currencies.

### Conventions

Follow UI conventions in [ui.md](../ui.md). Follow testing conventions in [testing.md](../testing.md).

## Use Case

Display all currencies stored in the system, sorted alphabetically. Currencies are fetched from the backend via a REST API endpoint. The page provides a simple single-column table view with sorting, pagination, and export capabilities. Table conventions per [ui.md](../ui.md) apply (sortable, resizable, full width).

The page can be accessed from the main menu under the "Currencies" item in the sidebar navigation.

---

## REST API

### `GET /api/currencies` — Currency list

| Query Param | Description |
|-------------|-------------|
| `sortField` | Only `name` supported (default: `name`) |
| `sortDir` | `asc` or `desc` (default: `asc`) |
| `page` | Page number, 1-based (default: `1`) |
| `pageSize` | Items per page; one of `[10, 20, 50, 100, -1]`; `-1` = all (default: `10`) |

Returns a paginated envelope:
```json
{
  "items": [
    { "id": 1, "name": "EUR" },
    { "id": 2, "name": "USD" }
  ],
  "page": 1,
  "pageSize": 10,
  "totalItems": 31,
  "totalPages": 4
}
```

- **`name`**: ISO 4217 currency code (VARCHAR(3), unique).

### `GET /api/currencies/export?format={csv|xlsx}` — Export currencies

Query params: same as `GET /api/currencies` (sorting only, no filters).

Returns a CSV or Excel file with all currencies (ignores pagination).

**Headers:**
- `Content-Type`: `text/csv; charset=UTF-8` or `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- `Content-Disposition`: `attachment; filename="currencies.{csv|xlsx}"`

---

## UI Specification

### Columns

| Column | Alignment | `width` | `minWidth` |
|--------|-----------|---------|------------|
| Currency | left | 300 | 200 |

### Summary

- **Row count:** Display `"N currencies"` (e.g., `"31 currencies"`).

### Export

- **Export buttons** (CSV and Excel) positioned top-right, next to "Show All" button.
- Export respects current sorting but ignores pagination (exports all results).

### Loading and Refresh

- A loading indicator (spinner) is displayed while data is being fetched.
- **Loading:** `ProgressCircle size="small"` + "Loading...".

### Pagination

- Pagination controls at the bottom of the table (per [ui.md](../ui.md)).
- Page size selector with options: 10, 20, 50, 100, Show All.
- "Show All" button toggles between paginated view and showing all results.

---

## Implementation Notes

### Backend

- **Entity:** `Currency` (see [`currency` table](../data-model.md#currency): `id`, `name` VARCHAR(3) UNIQUE).
- **Controller:** `CurrencyController` with endpoints:
  - `GET /api/currencies` (paginated, sorted)
  - `GET /api/currencies/export`
- **Repository:** `CurrencyRepository extends JpaRepository<Currency, Integer>` with `findAllByOrderByNameAsc()`.
- **No DTO needed** — entity is returned directly.
- **Export:** Uses generic `ExportService` with a single "Currency" column.

### Frontend

- **Page:** `Currencies.tsx` at route `/currencies`.
- **Hook:** Uses `useServerTable` hook with endpoint `/currencies`, defaultSortField `name`, defaultSortDir `asc`.
- **Components:** Reuses `ExportButtons`, `PaginationControls`.
- **Navigation:** "Currencies" menu item in `Layout.tsx` sidebar.