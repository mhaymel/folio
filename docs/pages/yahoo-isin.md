# Yahoo ISIN

> Route: `/yahoo-isin` — look up ticker symbols from Yahoo Finance for all held portfolio ISINs.

### Conventions

Follow UI conventions in [ui.md](../ui.md). Follow testing conventions in [testing.md](../testing.md).

---

## Use Case

For all ISINs the user currently holds (positive transaction sum), a ticker symbol is looked up via the Yahoo Finance search API. Results are **not persisted** — they are displayed only for the current session.

Two tables are shown after the fetch:

1. **With ticker** — ISINs for which Yahoo returned a ticker symbol.
2. **Without ticker** — ISINs for which no ticker symbol was found.

Tables are empty on mount and only populated after the user clicks **Fetch from Yahoo**.

---

## REST API

### `POST /api/yahoo-isin/fetch`

Queries the Yahoo Finance search API for each held ISIN. No data is written to the database.

**Request body:** none

**Response `200 OK`:**
```json
{
  "withTicker": [
    { "isin": "FR0000120628", "tickerSymbol": "CS.PA", "name": "AXA SA" }
  ],
  "withoutTicker": [
    { "isin": "XX0000000000", "name": "Unknown Corp" }
  ]
}
```

Both lists are sorted by ISIN ascending.

---

## UI Specification

### Buttons

- **Fetch from Yahoo** — triggers `POST /api/yahoo-isin/fetch`, shows spinner while in flight.
- **Clear** — resets both tables to empty and clears the status message.

Status message after fetch: `"N found, M not found."`

### Table — With Ticker

| Column | Alignment | `width` | `minWidth` |
|--------|-----------|---------|------------|
| ISIN | left | 140 | 140 |
| Ticker Symbol | left | 140 | 100 |
| Name | left | 300 | 200 |

### Table — Without Ticker

| Column | Alignment | `width` | `minWidth` |
|--------|-----------|---------|------------|
| ISIN | left | 140 | 140 |
| Name | left | 300 | 200 |

### Features

- Both tables hidden until fetch completes.
- Sortable, resizable, full width.
- No pagination (client-side data, results displayed in full).
- No export (data is not persisted).
