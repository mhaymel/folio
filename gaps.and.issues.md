# Gaps & Issues: PROJECT.md and plan.md

## Resolved

| # | Issue | Resolution |
|---|---|---|
| 1 | Missing security `name` field | `isin_name` table added to PROJECT.md and plan.md Phase 2 schema |
| 4 | `dividend_payment` missing `currency_id` | Column added to PROJECT.md and plan.md Phase 2 schema |
| 7 | Vague column name in plan 4.3 (DeGiro Account.csv) | Exact 0-indexed column table added |
| 8 | Stale open-question reference in plan 4.3 | Replaced with actual storage logic |
| C | KeSt (Austrian tax) scope | Explicitly deferred to a future version in PROJECT.md |
| A1 | Empty `Import ZERO-kontoumsaetze.csv` use case | Content added to PROJECT.md |
| B1 | Dashboard use case missing from PROJECT.md | Dashboard use case added |
| D | Market quotes / open question #2 | Live EUR quotes via `IsinsQuoteLoader` cascade (§5.6 added to plan.md); quote + performance added to securities endpoint; open question #2 resolved |
| E | `isin_name` UNIQUE constraint | Composite `UNIQUE(isin_id, name)` already in plan.md Phase 2 schema and PROJECT.md data model; insert logic in all parsers already enforces "never overwrite" |
| F | ZERO orders CSV — no name column identified | `Name` column confirmed at index 0; plan.md §4.2 updated with full column table and `isin_name` insert step |
| G | Quote persistence + scheduling not in plan | `isin_quote` table added to schema; §5.6 extended with persistence (upsert after fetch, update `settings.quote.last.fetch.timestamp`) and `@Scheduled` task; seed migration V5 added for default interval |
| H | Quote management endpoints missing | §5.7 added: GET/PUT settings, POST immediate trigger |
| I | Settings page missing from frontend plan | `/settings` route added to §6.2; §6.7 Settings Page Design added |
| J | Securities page missing quote/performance columns | §6.6 updated with Current Quote and Performance columns |
| K | Seed migration V3/V4 order was inverted in plan text | Fixed: V3=currencies, V4=quote providers, V5=settings |
| L | Dashboard backend endpoint missing from Phase 5 | `§5.8 DashboardController` added: `GET /api/dashboard` returning total portfolio value, security count, total dividend ratio %, top 5 holdings, top 5 dividend sources, last quote fetch timestamp |
| M | Dashboard frontend page description incomplete (wrong KPIs) | §6.2 Dashboard row updated; §6.8 Dashboard Page Design added with KPI cards, top-5 tables, and last-fetch timestamp |
| N | Open question #5 (KeSt) never marked resolved despite PROJECT.md explicitly deferring it | Marked resolved in plan.md open questions |
| O | Tiny types requirement (PROJECT.md tech pref) absent from plan.md | Added to §1.1: `model/` package note and tiny types paragraph |
| P | `quote/` package missing from §1.1 package structure | Added `quote/` entry to package tree in §1.1 |
| Q | Phase 5 section order wrong: 5.6 and 5.7 appeared before 5.5 in the file | Sections reordered: 5.1 → 5.2 → 5.3 → 5.4 → 5.5 → 5.6 → 5.7 → 5.8 |

---

## Open

### R. `avg_entry_price` calculation undefined

Plan §5.4 and §5.8 reference `avg_entry_price` without defining how it is computed. Specifically: when shares are sold, does the average entry price reset, stay fixed, or use a FIFO/LIFO method? The current formula implied by the plan is `SUM(count * share_price) / SUM(count)` across all transactions (buys positive, sells negative), but this is not stated. Needs a decision before implementing the securities and dashboard endpoints.

### S. `isin_country` / `isin_branch` schema vs. import use case mismatch

The Phase 2 schema uses a composite-PK join table for both `isin_country` and `isin_branch`, which allows an ISIN to be mapped to **multiple** countries or branches. However, the PROJECT.md import use cases say "if an ISIN already has a country/branch mapping, it should be **updated**" — implying 1:1. The analytics calculations also implicitly assume one country per ISIN. Needs clarification: is the relationship 1:1 (UNIQUE on `isin_id`) or 1:N? If 1:1, the schema and upsert logic must be updated.

### T. No testing phase in plan.md

PROJECT.md requires "a comprehensive test suite that covers all major functionality and edge cases", but plan.md has no phase or section covering testing strategy, test types (unit / integration / e2e), or tooling (JUnit, Mockito, Testcontainers, Vitest, etc.). Needs a Phase 10 (or similar) before implementation begins.

### U. ZERO depot name inconsistency in PROJECT.md

PROJECT.md "Import ZERO-orders.csv" use case refers to the depot as **"Trade Republic"**, while the data model, all other use cases, and plan.md consistently use **"ZERO"**. The seeded depot name in V2 migration is `ZERO`. The PROJECT.md use case text should be treated as a prose error; plan.md is authoritative. Recommend correcting the PROJECT.md use case wording to avoid confusion during implementation.

### ~~A. Empty use case section in PROJECT.md~~ — Resolved

`Import dividende.csv` use case content added to PROJECT.md.

### ~~B. Missing use cases in PROJECT.md~~ — Resolved

`Import branches.csv` and `Import countries.csv` use cases added to PROJECT.md.

### ~~D. Market quotes and performance — open question #2~~ — Resolved

Live EUR quotes via `IsinsQuoteLoader` cascade fallback (§5.6 added to plan.md). Securities endpoint updated to return current quote and performance %. Open question #2 resolved.

### ~~E. `isin_name` UNIQUE constraint not reflected in PROJECT.md~~ — Resolved

Both plan.md Phase 2 schema and PROJECT.md data model already reflect composite `UNIQUE(isin_id, name)`. All parser sections enforce "never overwrite" insert logic.

### ~~F. ZERO orders CSV — no name/product column identified~~ — Resolved

`Name` column confirmed at index 0 in the sample file. Plan §4.2 updated with full 0-indexed column table and `isin_name` insert step.

---

## Summary

Items N, O, P, Q resolved and applied to plan.md. Four open items remain (R, S, T, U) requiring decisions before or during implementation.

> Last reviewed: 2026-03-22 — full cross-check of PROJECT.md vs plan.md: section ordering fixed, tiny types added, quote package added, KeSt resolved; open items R/S/T/U documented.