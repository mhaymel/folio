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
| GET | `/api/depots` | All depots, sorted alphabetically |
| GET | `/api/currencies` | All currencies, sorted alphabetically |

---

## UI Specification

- Simple single-column tables displaying the name field.
- Table conventions per [ui.md](ui.md) apply (sortable, resizable, full width).

