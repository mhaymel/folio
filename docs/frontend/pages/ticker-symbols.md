# Ticker Symbols

> Route: `/ticker-symbols` — ISIN to ticker symbol mappings.

## Use Case

The UI should provide a view that displays all ISINs along with their associated ticker symbols and stock names. This allows users to see the ticker symbol mappings for stocks in their portfolio. The data should be fetched from the backend via a REST API endpoint that retrieves ISIN, ticker symbol, and name data from the database by joining the `isin`, `ticker_symbol`, `isin_ticker`, and `isin_name` tables.

---

## REST API

### `GET /api/ticker-symbols`

Returns all ISIN to ticker symbol mappings with stock names, sorted alphabetically by ISIN.

**Response:**
```json
[
  { "isin": "DE000BASF111", "tickerSymbol": "BASF.DE", "name": "BASF SE" },
  { "isin": "IE00B4L5Y983", "tickerSymbol": "IWDA.AS", "name": "iShares Core MSCI World ETF" },
  { "isin": "US0378331005", "tickerSymbol": "AAPL", "name": "Apple Inc." }
]
```

**Notes:**
- An ISIN can have multiple ticker symbols (appears as multiple rows).
- An ISIN can have multiple names; the API returns the first name found.
- ISINs without ticker symbol mappings are excluded from the response.

---

## UI Specification

### Table

Two-column table displaying ISIN, ticker symbol, and stock name.

| Column | Description | Alignment | Width | MinWidth |
|--------|-------------|-----------|-------|----------|
| ISIN | Stock identifier | left | 140 | 140 |
| Ticker Symbol | Ticker symbol | left | 140 | 100 |
| Name | Stock name | left | 300 | 200 |

### Features

- Table conventions per [ui.md](ui.md) apply (sortable, resizable, full width).
- Data sorted alphabetically by ISIN by default.
- Loading indicator while fetching data.
- Count display: `"N ticker symbol mappings"`.

