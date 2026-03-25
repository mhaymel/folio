# Securities

> Route: `/securities` — Portfolio positions with live quotes and performance.

## Use Case

The UI should provide a view that displays all securities (ISINs) that are currently held in the portfolio, along with relevant details such as ticker symbol, name, country, branch, count, current quote, entry price, performance etc. The securities should be fetched from the backend via a REST API endpoint that retrieves security data from the database and calculates the current quote and performance based on the transactions and the current market prices. The UI should allow the user to filter and sort securities based on different criteria (e.g. country, branch, performance). The UI should also provide a way to refresh the security data to reflect any changes in the transactions or market prices. The table columns shall be resizable by the user. The ISIN column shall have a fixed minimum width wide enough to display a full 12-character ISIN without clipping. The Name column shall be wide enough for typical security names. The Country and Branch columns shall be wide enough to display their values without clipping.

---

## REST API

### `GET /api/securities` — Current positions aggregated from transactions

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
- Sortable + resizable (`resizable` prop).

