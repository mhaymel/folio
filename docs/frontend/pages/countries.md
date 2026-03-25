# Countries

> Route: `/countries` — Alphabetical list of countries.

## Use Case

The UI should provide a view that displays the countries, sorted alphabetically. The countries should be fetched from the backend via a REST API endpoint that retrieves country data from the database.

---

## REST API

### `GET /api/countries`

Returns all countries, sorted alphabetically.

**Response:**
```json
[
  { "id": 1, "name": "BRD" },
  { "id": 2, "name": "Irland" },
  { "id": 3, "name": "USA" }
]
```

---

## UI Specification

- Single-column table displaying country names.
- Table conventions per [ui.md](ui.md) apply (sortable, resizable, full width).
- Data sorted alphabetically by name.

