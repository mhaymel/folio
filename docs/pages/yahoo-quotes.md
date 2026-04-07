# Yahoo Quotes

> Route: `/yahoo-quotes` — fetch and display Yahoo Finance quotes for held portfolio ISINs.

### Conventions

Follow UI conventions in [ui.md](../ui.md). Follow testing conventions in [testing.md](../testing.md).

---

## Use Case

Fetches current prices from **Yahoo Finance** for all ISINs the user currently holds (positive transaction sum). The fetch is ticker-symbol driven: for each held ISIN the first ticker from `isin_ticker` is used to call the Yahoo Finance API. ISINs without a ticker symbol cannot be priced this way.

Two tables are shown:

1. **With quote** — held ISINs that have an entry in `isin_quote` (price, currency, provider, fetched timestamp).
2. **Without quote** — held ISINs that have no quote yet (includes those with no ticker symbol).

Both tables are **empty on first visit** and only load after the **Fetch Quotes** button is clicked. Once loaded, the data (including the fetch status message) **persists across navigation** via `sessionStorage` — returning to the page restores the previous results without a new fetch. Clicking **Fetch Quotes** clears both tables and the status message, then fetches fresh data.

---

## REST API

### `POST /api/yahoo-quotes/fetch`

Fetches prices from Yahoo Finance for all held ISINs that have a ticker symbol in `isin_ticker`. Results are upserted into `isin_quote` with provider name `"Yahoo Finance"`. ISINs with no ticker symbol are skipped. Prices ≤ 0 returned by Yahoo are discarded (handled by `QuoteFetcher`).

**Request body:** none

**Response `200 OK`:**
```json
{
  "total": 15,
  "fetched": 12,
  "noTicker": 2,
  "noQuote": 1
}
```

| Field | Description |
|-------|-------------|
| `total` | Total held ISINs |
| `fetched` | Quotes successfully fetched and saved |
| `noTicker` | ISINs skipped because no ticker symbol is registered |
| `noQuote` | ISINs with a ticker symbol for which Yahoo returned no price |

---

### `GET /api/yahoo-quotes/with-quote`

Returns held ISINs that have a current quote.

| Query Param | Description |
|-------------|-------------|
| `sortField` | `isin`, `name`, `tickerSymbol`, `price`, `currency`, `provider`, `fetchedAt` (default: `isin`) |
| `sortDir` | `asc` or `desc` (default: `asc`) |
| `page` | 1-based page number (default: `1`) |
| `pageSize` | One of `[10, 20, 50, 100, -1]`; `-1` = all (default: `10`) |
| `isin` | Filter by ISIN (substring, case-insensitive) |
| `name` | Filter by name (substring, case-insensitive) |
| `ticker` | Filter by ticker symbol (substring, case-insensitive) |
| `currency` | Filter by currency code (substring, case-insensitive) |

Returns a paginated envelope per [ui.md](../ui.md):
```json
{
  "items": [
    {
      "isin": "IE00B4L5Y983",
      "name": "iShares Core MSCI World ETF",
      "tickerSymbol": "IWDA.AS",
      "price": 89.42,
      "currency": "EUR",
      "provider": "Yahoo Finance",
      "fetchedAt": "05.04.2026 14:32"
    }
  ],
  "page": 1, "pageSize": 10, "totalItems": 1, "totalPages": 1
}
```

**Notes:**
- `tickerSymbol` — first ticker from `isin_ticker`; `null` if none.
- `name` — first name from `isin_name`; `null` if none.
- `price` — raw value stored in `isin_quote`; currency code in `currency` field.
- `currency` — 3-letter code from Yahoo response (e.g. `EUR`, `USD`); `null` if not available.
- `fetchedAt` formatted `dd.MM.yyyy HH:mm`.

---

### `GET /api/yahoo-quotes/without-quote`

Returns held ISINs with no entry in `isin_quote`.

| Query Param | Description |
|-------------|-------------|
| `sortField` | `isin`, `name`, `tickerSymbol` (default: `isin`) |
| `sortDir` | `asc` or `desc` (default: `asc`) |
| `page` | 1-based page number (default: `1`) |
| `pageSize` | One of `[10, 20, 50, 100, -1]`; `-1` = all (default: `10`) |
| `isin` | Filter by ISIN (substring, case-insensitive) |
| `name` | Filter by name (substring, case-insensitive) |
| `ticker` | Filter by ticker symbol (substring, case-insensitive) |

```json
{
  "items": [
    { "isin": "DE000BASF111", "name": "BASF SE", "tickerSymbol": null }
  ],
  "page": 1, "pageSize": 10, "totalItems": 1, "totalPages": 1
}
```

---

## UI Specification

### Fetch Button

- Label: **Fetch Quotes**
- Placed above both tables.
- Triggers `POST /api/yahoo-quotes/fetch`.
- Shows loading state while in flight; both tables also show loading.
- On success: `"12 of 15 quotes fetched. 2 no ticker, 1 no quote."` (or simpler: `"12 quotes fetched."`).
- On error: shows error message.
- After completion: both tables reload.

### Table — With Quote

| Column | Alignment | `width` | `minWidth` |
|--------|-----------|---------|------------|
| ISIN | left | 140 | 140 |
| Name | left | 260 | 160 |
| Ticker | left | 80 | 60 |
| Price | right | 100 | 80 |
| Currency | left | 80 | 60 |
| Provider | left | 140 | 100 |
| Fetched At | left | 160 | 120 |

Count display: `"N held ISINs with quote"`.

Filters: ISIN, Name, Ticker, Currency (text inputs, substring match).

### Table — Without Quote

| Column | Alignment | `width` | `minWidth` |
|--------|-----------|---------|------------|
| ISIN | left | 140 | 140 |
| Name | left | 300 | 200 |
| Ticker | left | 100 | 60 |

Count display: `"N held ISINs without quote"`.

Filters: ISIN, Name, Ticker (text inputs, substring match).

### Features

- Table conventions per [ui.md](../ui.md) apply (sortable, resizable, pagination, full width, export).
- Both tables are **empty on first visit** — no data is loaded until the user clicks Fetch Quotes. On return visits the previous results are restored from `sessionStorage`.
- Price displayed with 2 decimal places (German locale format).
- Prices ≤ 0 from Yahoo are discarded by `QuoteFetcher` before storage.
