# Reference Data Pages

> Routes: `/countries`, `/branches`, `/depots`, `/currencies` — Simple alphabetical list views.

## Use Cases

### Show Countries
The UI should provide a view that displays the countries, sorted alphabetically. The countries should be fetched from the backend via a REST API endpoint that retrieves country data from the database.

### Show Branches
The UI should provide a view that displays the branches, sorted alphabetically. The branches should be fetched from the backend via a REST API endpoint that retrieves branch data from the database.

### Show Currencies
The UI should provide a view that displays the currencies, sorted alphabetically. The currencies should be fetched from the backend via a REST API endpoint that retrieves currency data from the database.

### Show Depots
The UI should provide a view that displays the depots, sorted alphabetically. The depots should be fetched from the backend via a REST API endpoint that retrieves depot data from the database.

---

## REST API

### Reference Data Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/countries` | All countries, sorted alphabetically |
| GET | `/api/branches` | All branches, sorted alphabetically |
| GET | `/api/depots` | All depots, sorted alphabetically |
| GET | `/api/currencies` | All currencies, sorted alphabetically |

