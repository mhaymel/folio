# Stocks

> Route: `/stocks` — Portfolio positions aggregated across all depots, with live quotes and performance.

### Conventions

Follow UI conventions in [ui.md](../ui.md). Follow testing conventions in [testing.md](../testing.md).

## Use Case

Display all stocks (ISINs) currently held in the portfolio, aggregated across all depots. Unlike the [Stocks per Depot](stocks-per-depot.md) page which shows one row per ISIN per depot, this page shows one row per ISIN with counts and costs summed across all depots. This provides an overview of total holdings regardless of which depot they are held in. Stocks are fetched from the backend via a REST API endpoint that aggregates transaction data per ISIN (across all depots), and calculates current quote and performance based on market prices. The user can filter by ISIN, name, country, and branch, sort by any column, and refresh the data. Table conventions per [ui.md](../ui.md) apply (sortable, resizable, full width).

The page can be accessed from the main menu under the "Stocks" item in the sidebar navigation.

---

## REST API

### `GET /api/stocks` — Current positions aggregated across all depots

| Query Param | Description |
|-------------|-------------|
| `isin` | Filter by partial ISIN, case-insensitive (optional) |
| `name` | Filter by partial name, case-insensitive (optional) |
| `country` | Filter by exact country name (optional) |
| `branch` | Filter by exact branch name (optional) |
| `sortField` | One of: `isin`, `name`, `country`, `branch`, `count`, `avgEntryPrice`, `currentQuote`, `performancePercent`, `dividendPerShare`, `estimatedAnnualIncome` (default: `isin`) |
| `sortDir` | `asc` or `desc` (default: `asc`) |
| `page` | Page number, 1-based (default: `1`) |
| `pageSize` | Items per page; one of `[10, 20, 50, 100, -1]`; `-1` = all (default: `10`) |

Returns a paginated envelope per [ui.md](../ui.md), extended with `sumCount` (sum of the count field across all filtered results):
```json
{
  "items": [ … ],
  "page": 1,
  "pageSize": 10,
  "totalItems": 42,
  "totalPages": 5,
  "sumCount": 1234.50
}
```

### `GET /api/stocks/filters` — Available filter options

Returns distinct countries and branches from current positions (no depots — this page aggregates across all depots):
```json
{ "countries": ["Austria", "Germany", …], "branches": ["Technology", "Finance", …], "depots": [] }
```

**Calculation:**
- `SUM(count)` per ISIN (across all depots); keep positions where `SUM(count) > 0`.
- **Count:** Sum of all transaction counts (buys positive, sells negative) for the ISIN across all depots.
- **Avg entry price:** `SUM(count * share_price) / SUM(count)` across all transactions for the ISIN across all depots.
- Join with `isin_name`, `isin_country`, `isin_branch`, `dividend`, `isin_quote`.
- Response: ISIN, name, country, branch, count, avg entry price, current quote (null if not fetched), performance % (`(current_quote − avg_entry_price) / avg_entry_price * 100`), expected annual dividend, estimated annual income.

---

## UI Specification

### Filters

| Filter | Type | Behaviour |
|--------|------|-----------|
| ISIN | Text input | Partial, case-insensitive match |
| Name | Text input | Partial, case-insensitive match |
| Country | Multi-select dropdown | Comma-separated exact match; options from `GET /api/stocks/filters` |
| Branch | Multi-select dropdown | Comma-separated exact match; options from `GET /api/stocks/filters` |

Note: There is no depot filter on this page since stocks are aggregated across all depots.

All dropdown filters support multiple selections. When multiple values are selected, they are sent as a comma-separated string (e.g. `country=Germany,USA`). The backend splits on comma and filters using set membership.

- A **Clear** button resets all filters.
- A **Refresh** button reloads both data and filter options.
- Double-clicking a cell in the ISIN or Name column copies its value into the corresponding filter input and triggers a re-fetch.

### Columns

Column order places Country and Branch after Current Quote:

| Column | Alignment | `width` | `minWidth` |
|--------|-----------|---------|------------|
| ISIN | left | 140 | 140 | `IsinCell` with copy + filter icons (see [ui.md](../ui.md)) |
| Name | left | 240 | 240 |
| Count | right | 100 | 80 |
| Avg Entry Price | right | 120 | 100 |
| Current Quote | right | 110 | 100 |
| Country | left | 120 | 80 |
| Branch | left | 160 | 80 |
| Performance (%) | right | 120 | 100 |
| Expected Dividend/Share | right | 160 | 130 |
| Est. Annual Income | right | 140 | 110 |

Note: There is no Depot column on this page since stocks are aggregated across all depots.

- **ISIN cell:** Uses the shared `IsinCell` component with copy-to-clipboard and filter-by-ISIN icons (see [ui.md](../ui.md) § ISIN Column Conventions).
- Current Quote and Performance show `—` if no quote has been fetched yet.
- **Sum of Count:** The sum of the Count column (reflecting current filters) shall be displayed at the top of the table, next to the row count, formatted with 2 decimal places in German locale. The `GET /api/stocks` response includes `sumCount` for this purpose.
- **Debounce:** ISIN and Name text inputs shall be debounced (300 ms) per [ui.md](../ui.md) to avoid excessive requests on every keystroke.
- All filter values and sort state are sent as query params to `GET /api/stocks`; the backend returns the filtered, sorted data.
- **State preservation:** Filter values, sort state, and pagination are preserved in `sessionStorage` when navigating away and back (see [ui.md](../ui.md)).
