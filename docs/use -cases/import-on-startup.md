# Auto-Import on Startup

## Overview

On application startup, sample data from `docs/samples/` is automatically imported. This runs **once per startup**, **in development mode only** (`spring.profiles.active=dev`). It must never execute in production.

---

## Source Files

### DeGiro

| File | Content |
|------|---------|
| `account.csv` | Received dividends |
| `transactions.csv` | Buy / sell transactions |

### Zero

| File pattern | Content |
|--------------|---------|
| `ZERO-orders*.csv` | Buy / sell transactions |
| `ZERO-kontoumsaetze-*.csv` | Received dividends |

### Reference Data

| File | Content |
|------|---------|
| `countries.csv` | ISIN → country mapping |
| `branches.csv` | ISIN → branch (sector) mapping |
| `tickers.csv` | ISIN, ticker symbol, name |
| `dividends.csv` | ISIN, name, expected dividend amount, currency |

---

## Logging

For each file, two log entries are written at `INFO` level:

1. **Before import** — `Loading <filename>...`
2. **After import** — `Loaded <filename> — N rows imported.`

If a file is not found, a `WARN` entry is written and the file is skipped. Import errors are logged at `ERROR` level.

---

## Constraints

- Runs **only once** per application start.
- Active **only** when the `dev` profile is enabled.
- Must not affect the production database or run during production startup.
