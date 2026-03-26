# Dashboard

> Route: `/` — Landing page with portfolio KPIs.

## Use Case

The UI should provide a dashboard as the landing page, giving a quick overview of the portfolio. It should display:
- **Total portfolio value**: the sum of (total shares × avg entry price) across all open positions.
- **Number of different stocks**: the count of distinct ISINs currently held.
- **Total dividend ratio**: the estimated annual dividend income divided by the total portfolio value, expressed as a percentage.
- **Top 5 holdings**: the five positions with the highest invested value (total shares × avg entry price), showing stock name, ISIN, and invested amount.
- **Top 5 dividend sources**: the five stocks with the highest estimated annual dividend income (shares × dividend per share), showing stock name, ISIN, and estimated annual income.

The UI shows date and time of the last successful quote fetch, so the user can see how up-to-date the displayed quotes are.

The data should be fetched from the backend via a dedicated REST API endpoint.

---

## REST API

### `GET /api/dashboard` — Portfolio summary

**Calculation:**
- **Total portfolio value:** `SUM(avg_entry_price * total_shares)` across open positions.
- **Stock count:** distinct ISINs with `SUM(count) > 0`.
- **Total dividend ratio:** `SUM(shares * dividend_per_share) / total_portfolio_value * 100`.
- **Top 5 holdings:** highest `avg_entry_price * total_shares`; fields: ISIN, name, invested amount.
- **Top 5 dividend sources:** highest `shares * dividend_per_share`; fields: ISIN, name, estimated annual income.
- **Last quote fetch:** `settings` key `quote.last.fetch.timestamp`; null if not set.

**Response:**
```json
{
  "totalPortfolioValue": 12345.67,
  "stockCount": 23,
  "totalDividendRatio": 3.14,
  "top5Holdings": [{ "isin": "IE00B4L5Y983", "name": "iShares Core MSCI World ETF", "investedAmount": 4500.00 }],
  "top5DividendSources": [{ "isin": "DE000BASF111", "name": "BASF SE", "estimatedAnnualIncome": 150.00 }],
  "lastQuoteFetchAt": "2026-03-22T14:30:00"
}
```

---

## UI Specification

- **KPI row:** Total Portfolio Value (EUR), Number of Stocks, Total Dividend Ratio (%).
- **Top 5 Holdings:** table with ISIN, Stock Name, Invested Amount (EUR).
- **Top 5 Dividend Sources:** table with ISIN, Stock Name, Est. Annual Income (EUR).
- **Last Quote Fetch:** timestamp formatted per [ui.md](ui.md) (e.g., "Last updated: 22.03.2026 14:30"); `—` if not yet fetched.
- Data from `GET /api/dashboard`.
- Table conventions per [ui.md](ui.md) apply (sortable, resizable, full width).

