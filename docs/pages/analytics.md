# Analytics — Diversification Breakdown

> Routes: `/analytics/countries`, `/analytics/branches` — Donut charts + detail tables.

## Use Cases

### Country Diversification Breakdown
The UI should provide a view that shows the country diversification breakdown, which is calculated by the backend based on the transactions and the country mapping of ISINs. The backend should provide a REST API endpoint that calculates and returns the country diversification breakdown, which includes the total invested amount per country and the percentage of the total portfolio invested in each country. The UI should display this information in a clear and visually appealing way, such as a pie chart or a bar chart or a donut chart.

### Branch Diversification Breakdown
Similar to the country diversification breakdown, the UI should provide a view that shows the branch diversification breakdown, which is calculated by the backend based on the transactions and the branch mapping of ISINs. The backend should provide a REST API endpoint that calculates and returns the branch diversification breakdown, which includes the total invested amount per branch and the percentage of the total portfolio invested in each branch. The UI should display this information in a clear and visually appealing way, such as a pie chart or a bar chart or a donut chart.

---

## REST API

### Analytics Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/analytics/countries?sortField=&sortDir=&page=&pageSize=` | Total invested per country + % of portfolio |
| GET | `/api/analytics/branches?sortField=&sortDir=&page=&pageSize=` | Total invested per branch + % of portfolio |

Both endpoints accept optional `sortField` (`name`, `investedAmount`, `percentage`; default: `investedAmount`), `sortDir` (default: `desc`), `page` (default: `1`), and `pageSize` (default: `10`; `-1` = all) query params. Returns a paginated envelope per [ui.md](../ui.md).

**Calculation:** `invested = SUM(count * share_price)` per open position; group by country/branch via `isin_country`/`isin_branch`; compute % of total.

---

## UI Specification

- Donut chart (Recharts `PieChart`) with legend.
- Detail table below the chart. Country and branch pages share the same layout pattern.

### Columns

| Column | Alignment | `width` | `minWidth` |
|--------|-----------|---------|------------|
| Name | left | 240 | 200 |
| Invested Amount (EUR) | right | 160 | 120 |
| Percentage (%) | right | 120 | 80 |

- Table conventions per [ui.md](../ui.md) apply (sortable, resizable, full width).
- Default sort: invested amount descending. Sort changes trigger a re-fetch with `sortField` and `sortDir` query params.

