# Settings & Quote Management

> Route: `/settings` — Quote fetch interval selector + manual trigger.

## Use Case

The UI should provide a way for the user to change the interval at which the backend fetches live quotes for the securities. This could be implemented as a settings page where the user can select from predefined intervals (e.g. every 15 minutes, every hour, every 4 hours) or enter a custom interval. The selected interval should be stored in the database in table settings and used by the backend to schedule the quote fetching task. The UI should also provide feedback to the user about the current quote fetch interval and any changes made to it. REST API endpoint should be provided to update the quote fetch interval in the backend. The UI should also provide a way to trigger an immediate quote fetch, in case the user wants to update the quotes right away without waiting for the next scheduled fetch. The UI should show the date and time of the last successful quote fetch, so the user can see how up-to-date the displayed quotes are.

---

## Quote Data System

The backend fetches live EUR-denominated prices for a set of ISINs using a **cascading fallback** approach across up to 10 sources. The central orchestrator is `IsinsQuoteLoader`. Each source is tried in order; ISINs successfully resolved are removed from the remaining set before the next source is attempted.

### Config Files (bundled as backend resources)

Three sources require a pre-configured URL path per ISIN, stored as semicolon-delimited CSV files in `src/main/resources/`:

| File | Format | Used by |
|---|---|---|
| `finanzennet.csv` | `ISIN;relative-url-path` | Finanzen.net source |
| `onvista.csv` | `ISIN;relative-url-path` | Onvista source |
| `wallstreetonline.csv` | `ISIN;relative-url-path` | WallstreetOnline source |
| `isin.symbol.csv` | `ISIN;TICKER;Company Name` | CNBC source |

If an ISIN has no entry in the relevant config file, that source is skipped for that ISIN.

### Cascade Fallback Order

| Step | Source | URL Pattern | Currency |
|---|---|---|---|
| 1 | JustETF REST API | `https://www.justetf.com/api/etfs/{ISIN}/quote?locale=de&currency=EUR` | EUR (JSON API) |
| 2 | Onvista HTML | `https://www.onvista.de/{path}` (from `onvista.csv`) | EUR or USD |
| 3 | Finanzen.net HTML | `https://www.finanzen.net/{path}` (from `finanzennet.csv`) | EUR only |
| 4 | CNBC HTML | `https://www.cnbc.com/quotes/{TICKER}` (from `isin.symbol.csv`) | USD→EUR |
| 5 | JustETF HTML | `https://www.justetf.com/at/etf-profile.html?isin={ISIN}` | EUR |
| 6 | JustETF REST API | Same as step 1 (retry) | EUR |
| 7 | FondsDiscount.de EUR | `https://www.fondsdiscount.de/fonds/etf/{ISIN}/` | EUR |
| 8 | FondsDiscount.de USD | `https://www.fondsdiscount.de/fonds/etf/{ISIN}/` | USD→EUR |
| 9 | ComDirect HTML | `https://www.comdirect.de/inf/zertifikate/{ISIN}` | EUR |
| 10 | WallstreetOnline | `https://www.wallstreet-online.de/{path}` (from `wallstreetonline.csv`) | EUR or USD (auto-detect) |

### Currency Conversion

- USD→EUR conversion uses the ECB online XML exchange rate feed (with static fallback) for steps 4 and 8.
- WallstreetOnline (step 10) uses a hardcoded factor of `0.86` for USD instead of the dynamic rate.

### Error Handling

- Each source returns `Optional.empty()` on any failure (HTTP non-200, parse error, missing config entry, network error).
- ISINs with no successful quote from any source are simply absent from the result — no error is thrown.
- All decimal values use comma→dot normalization before parsing.

---

## Quote Fetcher Scheduling & Persistence

- **Scheduling:** `@Scheduled` Spring task reads `settings.quote.fetch.interval.minutes` before each run (so UI changes take effect without restart). Runs for all ISINs where `SUM(count) > 0`. Requires `@EnableScheduling` on the application class.
- **Persistence:** After each fetch batch, upsert results into `isin_quote` (one row per ISIN; `quote_provider_id` = provider that succeeded; `fetched_at` = now). Update `settings` key `quote.last.fetch.timestamp`.
- **Disabled source:** Lemon Markets batch API — API token expired; do not implement.

---

## REST API

### Quote Management Endpoints (`QuoteController`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/quotes/settings` | Current fetch interval + last fetch timestamp |
| PUT | `/api/quotes/settings/interval` | Update `quote.fetch.interval.minutes` |
| POST | `/api/quotes/fetch` | Trigger immediate fetch for all held ISINs |

`GET /api/quotes/settings` response: `{ "intervalMinutes": 60, "lastFetchAt": "2026-03-22T14:30:00" }` (`lastFetchAt` null if no fetch yet).

---

## UI Specification

- Fetch interval: dropdown (15 min, 30 min, 1 h, 4 h, 12 h, 24 h) + custom input.
- "Save interval" → `PUT /api/quotes/settings/interval`.
- "Fetch now" → `POST /api/quotes/fetch`; loading spinner while running.
- Last fetch timestamp displayed below controls.

