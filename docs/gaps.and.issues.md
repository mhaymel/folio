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

### T. No testing phase in plan.md

PROJECT.md requires "a comprehensive test suite that covers all major functionality and edge cases", but plan.md has no phase or section covering testing strategy, test types (unit / integration / e2e), or tooling (JUnit, Mockito, Testcontainers, Vitest, etc.). Needs a Phase 10 (or similar) before implementation begins.

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

### V. Clerk authentication not yet implemented

Plan §3 describes a full Clerk JWT filter (`ClerkJwtFilter.java`) and `nimbus-jose-jwt` dependency. In the actual code, `SecurityConfig.java` uses a `folio.security.enabled` flag (defaulting to `false`) that simply permits all requests. `@clerk/clerk-react` is not installed in the frontend. No `ClerkJwtFilter` exists.

### W. `quote/` package (IsinsQuoteLoader) not yet implemented

Plan §5.6 describes a full cascading quote fetcher across 10 sources with its own `quote/` package, config CSV files, `@Scheduled` task, and `isin_quote` upsert logic. None of this exists in the codebase. `QuoteController` endpoints exist but the quote-fetching implementation is absent.

### X. `parser/` package not yet extracted

Plan §1.1 lists a `parser/` package for broker-specific CSV parsers. In the actual code, CSV parsing is implemented inside `ImportService`. The extraction into separate parser classes per broker has not been done.

### Y. Lombok removed — plan.md was updated

Lombok is listed in the original plan §1.1 but is not in `build.gradle` and is not used anywhere. Plan §1.1 has been corrected to reflect this.

### Z. Wrong Strato package name in plan.md — corrected

Plan §1.2 listed `@dynatrace/strato-design-system` which does not exist as an NPM package. The actual package is `@dynatrace/strato-components`. Plan §1.2 and PROJECT.md have been corrected.

---

## Resolved (continued)

| # | Issue | Resolution |
|---|---|---|
| V1 | plan.md §1.1 listed Lombok | Removed from plan; confirmed not in build.gradle or any source file |
| V2 | plan.md §1.2 wrong Strato package name | Corrected to `@dynatrace/strato-components`; PROJECT.md updated accordingly |
| V3 | plan.md §6.1 described top nav bar | Updated to reflect Page + Page.Sidebar + Page.Main layout now implemented |
| R | `avg_entry_price` calculation undefined | Defined as `SUM(count * share_price) / SUM(count)` across **all** transactions (buys positive, sells negative) — mirrors `IsinTransactions.entryPrice()`. Implemented in `PortfolioService` JPQL and native SQL; plan.md §5.4 and §5.5 updated. |
| S | `isin_country` / `isin_branch` 1:1 vs 1:N | Clarified as 1:1 in PROJECT.md (each ISIN has exactly one country and one branch; mapping replaced on re-import). ImportService already implements delete+insert. V6 migration adds `UNIQUE(isin_id)` to enforce the constraint at DB level; plan.md §2 and §4.6/§4.7 updated. |
| U | ZERO depot name inconsistency | PROJECT.md use case corrected to use "ZERO" throughout (was erroneously "Trade Republic" in one use case). |

---

### AA. `show depots` use case — frontend missing

PROJECT.md defines a `show depots` use case. The backend `/api/depots` endpoint was implemented in `ReferenceDataController`. However, the frontend had no `/depots` route, no `Depots` page, and no sidebar nav entry. — **Resolved 2026-03-23**: `Depots.tsx` page created; `/depots` route added to `App.tsx`; "Depots" nav item added to `Layout.tsx`.

### AB. `show currencies` use case — backend and frontend missing

PROJECT.md defines a `show currencies` use case. Neither the backend endpoint `/api/currencies` nor the frontend page existed. The use case was also absent from plan.md §5.2 and §6.2. — **Resolved 2026-03-23**: `findAllByOrderByNameAsc()` added to `CurrencyRepository`; `GET /api/currencies` added to `ReferenceDataController`; `Currency` type added to `types/index.ts`; `Currencies.tsx` page created; `/currencies` route added to `App.tsx`; "Currencies" nav item added to `Layout.tsx`; plan.md §5.2 and §6.2 updated.

---

## Summary

Items N, O, P, Q, R, S, U, V1, V2, V3, AA, AB resolved and applied to plan.md / PROJECT.md. Open items T, V, W, X remain requiring decisions or implementation work.

> Last reviewed: 2026-03-23 — `show depots` frontend and `show currencies` (backend + frontend) implemented; plan.md §5.2 and §6.2 updated; Clerk and quote fetcher gaps remain open.