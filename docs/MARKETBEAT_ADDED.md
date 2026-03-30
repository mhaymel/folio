# MarketBeat Added as Quote Provider

## Summary

Added **MarketBeat** (https://www.marketbeat.com/) as a possible source for web scraping stock quotes.

## Changes Made

### 1. `docs/PROJECT.md`
- ✅ Added `MarketBeat` to the list of seed quote providers

### 2. `docs/pages/settings.md`
- ✅ Updated cascade fallback from 10 to **11 sources**
- ✅ Added step 11 with MarketBeat configuration:
  - URL pattern: `https://www.marketbeat.com/stocks/{EXCHANGE}/{TICKER}/`
  - Example: `https://www.marketbeat.com/stocks/NYSE/T/`
  - Currency: USD→EUR
- ✅ Added `marketbeat.csv` config file specification
  - Format: `ISIN;EXCHANGE;TICKER`
  - Required because MarketBeat uses exchange+ticker, not ISIN

### 3. `docs/data-model.md`
- ✅ Added `MarketBeat` to quote provider example

### 4. `docs/EFFORT_ESTIMATION.md`
- ✅ Updated from 7 to **8 quote providers**
- ✅ Added MarketBeat to the list of scrapers in Week 9-10

## Implementation Notes

### MarketBeat Specifics
- **URL Format:** `https://www.marketbeat.com/stocks/{EXCHANGE}/{TICKER}/`
- **Exchange Examples:** NYSE, NASDAQ, AMEX, etc.
- **Ticker Example:** T (AT&T)
- **Currency:** USD (requires conversion to EUR)
- **Config File Needed:** `marketbeat.csv` with format `ISIN;EXCHANGE;TICKER`

### Integration Requirements
1. Create `backend/src/main/resources/marketbeat.csv` mapping file
2. Implement `MarketBeatSource` class following the pattern of existing sources
3. Add MarketBeat to the cascade order in `IsinsQuoteLoader` (step 11)
4. Parse HTML to extract quote value
5. Apply USD→EUR conversion using ECB exchange rate

### Cascade Position
MarketBeat is placed as **step 11** (last in the cascade) because:
- Requires exchange+ticker mapping (additional config overhead)
- USD-only (requires currency conversion)
- Good fallback for US stocks after other sources fail

## Documentation Updated
- ✅ PROJECT.md — Seed data
- ✅ pages/settings.md — Quote system specification
- ✅ data-model.md — Quote provider examples
- ✅ EFFORT_ESTIMATION.md — Implementation effort

Date: 2026-03-30

