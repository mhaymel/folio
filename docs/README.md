# Documentation Structure Overview

## Current Structure (as of 2026-03-30)

```
docs/
├── PROJECT.md                          # Main project overview and page index
├── plan.md                             # Implementation plan (thin frontend)
├── data-model.md                       # Database schema and entities
├── ui.md                               # General UI requirements and conventions
├── testing.md                          # Testing requirements and conventions
├── coding-guidelines.md                # Java/React/TS coding standards
├── gaps.and.issues.md                  # Known issues and missing implementations
├── missing.md                          # Feature gaps
├── UI_MD_MOVED.md                      # Historical: ui.md move summary
├── DIVIDEND_PAYMENTS_ADDED.md          # Historical: dividend payments spec added
├── PAGES_DIRECTORY_MOVED.md            # Historical: pages directory move summary
├── pages/                              # Page specifications (14 files)
│   ├── analytics.md
│   ├── branches.md
│   ├── countries.md
│   ├── currencies.md
│   ├── dashboard.md
│   ├── depots.md
│   ├── dividend-payments.md
│   ├── import.md
│   ├── isin-names.md
│   ├── settings.md
│   ├── stocks.md
│   ├── stocks-per-depot.md
│   ├── ticker-symbols.md
│   └── transactions.md
└── samples/                            # Sample CSV files for testing
    ├── Account.csv
    ├── branches.csv
    ├── countries.csv
    ├── dividende.csv
    ├── ticker-symbol.csv
    ├── Transactions.csv
    ├── ZERO-kontoumsaetze-22.03.2026.csv
    ├── ZERO-orders-22.03.2026.csv
    └── ZERO-orders-27.03.2026.csv
```

## Document Types

### Root-Level Documentation
- **PROJECT.md** — Central index linking all page specs, tech stack, requirements
- **plan.md** — Step-by-step backend refactoring plan (thin frontend)
- **data-model.md** — Entity schemas, relationships, database design
- **ui.md** — UI conventions (applies to all pages)
- **testing.md** — Testing conventions (backend, frontend, E2E)
- **coding-guidelines.md** — Java/React/TypeScript standards
- **gaps.and.issues.md** — Tracked issues and implementation gaps
- **missing.md** — Feature gaps

### Page Specifications (`pages/`)
Each file describes a single UI page with:
- Use case
- REST API contract (endpoints, query params, response format)
- UI specification (columns, filters, interactions)
- Implementation notes

### Historical Summaries
- **UI_MD_MOVED.md** — Documents the move of ui.md from pages/ to root
- **DIVIDEND_PAYMENTS_ADDED.md** — Documents the addition of dividend-payments.md spec
- **PAGES_DIRECTORY_MOVED.md** — Documents the move of pages/ from frontend/pages/ to root

## Link Patterns

### From page specs to ui.md
```markdown
[ui.md](../ui.md)
```
All page specs are in `docs/pages/`, so they reference ui.md one level up.

### From root docs to page specs
```markdown
[pages/dashboard.md](pages/dashboard.md)
```
Root-level docs reference page specs in the pages/ subdirectory.

### From root docs to other root docs
```markdown
[ui.md](ui.md)
```
Same-directory references.

## Adding a New Page Specification

1. Create `docs/pages/<page-name>.md` following the template from existing pages
2. Add an entry to the "Page Specifications" table in `docs/PROJECT.md`
3. Use `[ui.md](../ui.md)` to reference general UI requirements
4. Use `[testing.md](../testing.md)` to reference testing requirements
5. Add route, REST API, UI spec, and implementation notes sections

## Maintenance

- Never edit historical summary documents (UI_MD_MOVED.md, etc.) — they document past changes
- Update PROJECT.md when adding/removing pages
- Update data-model.md when changing database schema
- Update coding-guidelines.md when establishing new conventions
- Keep gaps.and.issues.md current as issues are resolved

