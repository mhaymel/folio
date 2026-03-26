# Stocks

> Route: `/stocks` — Portfolio positions with live quotes and performance.

## Use Case

The UI should provide a view that displays all stocks (ISINs) currently held in the portfolio, along with relevant details such as ticker symbol, name, country, branch, count, current quote, entry price, and performance. The stocks should be fetched from the backend via a REST API endpoint that retrieves stock data from the database and calculates the current quote and performance based on transactions and current market prices. The UI should allow the user to filter and sort stocks based on different criteria (e.g., country, branch, performance). The UI should also provide a way to refresh the stock data to reflect any changes. Table conventions per [ui.md](ui.md) apply (sortable, resizable, full width).

---

## REST API

### `GET /api/stocks` — Current positions aggregated from transactions

**Calculation:**
- `SUM(count)` per ISIN; keep positions where `SUM(count) > 0`.
- **Avg entry price:** `SUM(count * share_price) / SUM(count)` across **all** transactions (buys positive, sells negative — reduces cost basis proportionally).
- Join with `isin_name`, `isin_country`, `isin_branch`, `dividend`, `isin_quote`.
- Response: ISIN, name, country, branch, total shares, avg entry price, current quote (null if not fetched), performance % (`(current_quote − avg_entry_price) / avg_entry_price * 100`), expected annual dividend, estimated annual income.

---

## UI Specification

### Columns

| Column | width | minWidth |
|--------|-------|----------|
| ISIN | 140 | 140 |
| Name | 240 | 240 |
| Country | 120 | 80 |
| Branch | 160 | 80 |
| Total Shares | — | — |
| Avg Entry Price | — | — |
| Current Quote | — | — |
| Performance (%) | — | — |
| Expected Dividend/Share | — | — |
| Est. Annual Income | — | — |

- Current Quote and Performance show `—` if no quote fetched yet.
- Filter bar: country dropdown, branch dropdown.

