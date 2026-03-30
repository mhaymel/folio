# Ticker Symbols

> Route: `/ticker-symbols` — ISIN to ticker symbol mappings.

### Conventions

Follow UI conventions in [ui.md](../ui.md). Follow testing conventions in [testing.md](../testing.md).

## Use Case

The UI should provide a view that displays all ISINs along with their associated ticker symbols and stock names. This allows users to see the ticker symbol mappings for stocks in their portfolio. The data should be fetched from the backend via a REST API endpoint that retrieves ISIN, ticker symbol, and name data from the database by joining the `isin`, `ticker_symbol`, `isin_ticker`, and `isin_name` tables.

---

## REST API

### `GET /api/ticker-symbols`

| Query Param | Description |
|-------------|-------------|
| `sortField` | Sort field: `isin`, `tickerSymbol`, `name` (default: `isin`) |
| `sortDir` | `asc` or `desc` (default: `asc`) |
| `page` | Page number, 1-based (default: `1`) |
| `pageSize` | Items per page; one of `[10, 20, 50, 100, -1]`; `-1` = all (default: `10`) |

Returns a paginated envelope per [ui.md](../ui.md):
```json
{
  "items": [
    { "isin": "DE000BASF111", "tickerSymbol": "BASF.DE", "name": "BASF SE" },
    { "isin": "IE00B4L5Y983", "tickerSymbol": "IWDA.AS", "name": "iShares Core MSCI World ETF" },
    { "isin": "US0378331005", "tickerSymbol": "AAPL", "name": "Apple Inc." }
  ],
  "page": 1,
  "pageSize": 10,
  "totalItems": 3,
  "totalPages": 1
}
```

**Notes:**
- An ISIN can have multiple ticker symbols (appears as multiple rows).
- An ISIN can have multiple names; the API returns the first name found.
- ISINs without ticker symbol mappings are excluded from the response.

---

## UI Specification

### Table

Two-column table displaying ISIN, ticker symbol, and stock name.

| Column | Alignment | `width` | `minWidth` |
|--------|-----------|---------|------------|
| ISIN | left | 140 | 140 |
| Ticker Symbol | left | 140 | 100 |
| Name | left | 300 | 200 |

### Features

- Table conventions per [ui.md](../ui.md) apply (sortable, resizable, full width).
- Default sort: ISIN ascending. Sort changes trigger a re-fetch with `sortField` and `sortDir` query params.
- Loading indicator while fetching data.
- Count display: `"N ticker symbol mappings"`.

