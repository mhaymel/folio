# Yahoo ISIN

> Route: `/yahoo-isin` — look up ticker symbols from Yahoo Finance for all currently held ISINs.

Follow UI conventions in [ui.md](../ui.md) and testing conventions in [testing.md](../testing.md).

---

## Overview

For every ISIN the user currently holds (positive transaction sum), a ticker symbol is resolved via the Yahoo Finance search API. Results are displayed in up to three tables and can optionally be persisted to the database.

The same ticker symbol may be returned for more than one ISIN (e.g. a share listed on multiple exchanges). Each ISIN may have at most one ticker entry.

---

## Tables

All tables are hidden on mount and populated only after **Fetch from Yahoo** is clicked.

| # | Table | Shown when |
|---|-------|-----------|
| 1 | **With Ticker** | Always (after fetch) |
| 2 | **Without Ticker** | Always (after fetch) |
| 3 | **Duplicate Tickers** | At least one ticker symbol maps to more than one ISIN |

---

## REST API

### `POST /api/yahoo-isin/fetch`

Resolves ticker symbols for all held ISINs. Nothing is written to the database.

**Request body:** none

**Response `200 OK`:**
```json
{
  "withTicker":       [{ "isin": "FR0000120628", "tickerSymbol": "CS.PA", "name": "AXA SA" }],
  "withoutTicker":    [{ "isin": "XX0000000000", "name": "Unknown Corp" }],
  "duplicateTickers": [
    { "isin": "US1234567890", "tickerSymbol": "AMCR", "name": "Amcor A" },
    { "isin": "AU0000123456", "tickerSymbol": "AMCR", "name": "Amcor B" }
  ]
}
```

- `withTicker` and `withoutTicker` are sorted by ISIN ascending.
- `duplicateTickers` is computed server-side from `withTicker`: any ticker symbol appearing for more than one ISIN is included, sorted by ticker symbol then ISIN.

---

### `POST /api/yahoo-isin/save`

Persists the `withTicker` results to the database. For each entry:

- If the ISIN already has a ticker entry → update the symbol.
- If no entry exists for the ISIN → create a new one.

**Request body:** the `withTicker` array from the fetch response.

**Response `200 OK`:**
```json
{ "created": 12, "updated": 3 }
```

---

## UI

### Buttons

| Button | Behaviour |
|--------|-----------|
| **Fetch from Yahoo** | Calls `POST /api/yahoo-isin/fetch`. Disabled while loading or saving. Shows spinner. |
| **Save** | Calls `POST /api/yahoo-isin/save` with the current With Ticker results. Enabled only after a successful fetch. Disabled while loading or saving. |
| **Clear** | Clears all tables and the status message. |

**Status messages**

- After fetch: `"N found, M not found."`
- After save: `"N created, M updated."`

### Error Handling

If the save request fails, a modal is shown with the title **"Save failed"** and the message **"Ticker symbols could not be saved. Please try again."** The Save button remains enabled so the user can retry without refetching.

### Table Columns

**With Ticker**

| Column | Alignment | Width | Min width |
|--------|-----------|-------|-----------|
| ISIN | left | 140 | 140 |
| Ticker Symbol | left | 140 | 100 |
| Name | left | 300 | 200 |

**Without Ticker**

| Column | Alignment | Width | Min width |
|--------|-----------|-------|-----------|
| ISIN | left | 140 | 140 |
| Name | left | 300 | 200 |

**Duplicate Tickers** *(only shown when duplicates exist)*

| Column | Alignment | Width | Min width |
|--------|-----------|-------|-----------|
| Ticker Symbol | left | 140 | 100 |
| ISIN | left | 140 | 140 |
| Name | left | 300 | 200 |

### General

- All tables are sortable, resizable, and full-width.
- Client-side pagination on all tables.
- No export (use Save to persist data).
- **Results persist across navigation.** Fetch results are stored in a React context above the router and survive page changes. Data is cleared only when **Clear** is clicked or a new fetch is performed.
