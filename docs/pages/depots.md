# Depots

> Route: `/depots` — Alphabetical list of depots.

### Conventions

Follow UI conventions in [ui.md](../ui.md). Follow testing conventions in [testing.md](../testing.md).

## Use Case

Display all depots (brokerage accounts) stored in the system, sorted alphabetically. Depots are fetched from the backend via a REST API endpoint. The page provides a simple single-column table view with sorting, pagination, and export capabilities. Table conventions per [ui.md](../ui.md) apply (sortable, resizable, full width).

The page can be accessed from the main menu under the "Depots" item in the sidebar navigation.

---

## REST API

### `GET /api/depots` — Depot list

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
    { "id": 1, "name": "DeGiro" },
    { "id": 2, "name": "ZERO" }
  ],
  "page": 1,
  "pageSize": 10,
  "totalItems": 2,
  "totalPages": 1
}
```

- **`name`**: Depot name (VARCHAR(100), unique).

### `GET /api/depots/export?format={csv|xlsx}` — Export depots

Query params: same as `GET /api/depots` (sorting only, no filters).

Returns a CSV or Excel file with all depots (ignores pagination).

**Headers:**
- `Content-Type`: `text/csv; charset=UTF-8` or `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- `Content-Disposition`: `attachment; filename="depots.{csv|xlsx}"`

---

## UI Specification

### Columns

| Column | Alignment | `width` | `minWidth` |
|--------|-----------|---------|------------|
| Depot | left | 300 | 200 |

### Summary

- **Row count:** Display `"N depots"` (e.g., `"2 depots"`).

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

- **Entity:** `Depot` (see [`depot` table](../data-model.md#depot): `id`, `name` VARCHAR(100) UNIQUE).
- **Controller:** `DepotController` with endpoints:
  - `GET /api/depots` (paginated, sorted)
  - `GET /api/depots/export`
- **Repository:** `DepotRepository extends JpaRepository<Depot, Integer>` with `findAllByOrderByNameAsc()`.
- **No DTO needed** — entity is returned directly.
- **Export:** Uses generic `ExportService` with a single "Depot" column.

### Frontend

- **Page:** `Depots.tsx` at route `/depots`.
- **Hook:** Uses `useServerTable` hook with endpoint `/depots`, defaultSortField `name`, defaultSortDir `asc`.
- **Components:** Reuses `ExportButtons`, `PaginationControls`.
- **Navigation:** "Depots" menu item in `Layout.tsx` sidebar.