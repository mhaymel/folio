# Branches

> Route: `/branches` — Alphabetical list of branches.

## Use Case

The UI should provide a view that displays the branches, sorted alphabetically. The branches should be fetched from the backend via a REST API endpoint that retrieves branch data from the database.

---

## REST API

### `GET /api/branches`

Returns all branches, sorted alphabetically.

**Response:**
```json
[
  { "id": 1, "name": "Energy" },
  { "id": 2, "name": "Technology" },
  { "id": 3, "name": "Healthcare" }
]
```

---

## UI Specification

- Single-column table displaying branch names.
- Table conventions per [ui.md](ui.md) apply (sortable, resizable, full width).
- Data sorted alphabetically by name.

