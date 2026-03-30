# Effort Estimation: Implementing Folio from Scratch

## Executive Summary

**Total Estimated Effort: 6-9 months for an average software engineer**

- **Backend (Spring Boot + PostgreSQL):** 3-4 months
- **Frontend (React + TypeScript):** 2-3 months
- **Integration, Testing, DevOps:** 1-2 months

This assumes a **single developer working full-time** with average experience in Spring Boot, React, and relational databases.

---

## Project Scope Analysis

### Current Implementation Size

| Component | Metric | Count |
|-----------|--------|-------|
| **Backend** | Java source files | 121 files |
| **Backend** | Test files | 25 files |
| **Frontend** | TypeScript/React files | 44 files |
| **Database** | Tables | 14 tables |
| **Pages** | UI screens | 13 pages |
| **Features** | Major features | ~20 features |

### Complexity Indicators

#### Backend Complexity ⭐⭐⭐⭐ (High)
- 14 database tables with foreign key relationships
- Complex CSV parsing for 2 different broker formats (DeGiro, ZERO)
- Web scraping for 8 different quote providers (including MarketBeat)
- Portfolio calculations (aggregations, performance, dividends)
- Server-side filtering, sorting, pagination for all tables
- Export to CSV and Excel
- Flyway database migrations
- Multi-currency support (30 currencies)

#### Frontend Complexity ⭐⭐⭐ (Medium-High)
- 13 distinct pages with sortable, filterable tables
- Server-side pagination, filtering, sorting coordination
- Multi-select filters with sessionStorage persistence
- CSV/Excel export integration
- Responsive design (desktop + mobile)
- Dark/light mode support (planned)
- Strato design system integration (custom component library)

#### Integration Complexity ⭐⭐⭐ (Medium-High)
- Authentication with Clerk (planned, not yet implemented)
- Docker containerization (single image with both frontend + backend)
- H2 for dev/test, PostgreSQL for production
- Cross-origin communication (frontend ↔ backend)

---

## Detailed Effort Breakdown

### Phase 1: Backend Core (6-8 weeks)

#### Week 1-2: Project Setup & Data Model
- **Effort:** 2 weeks
- **Tasks:**
  - Spring Boot project scaffolding
  - Gradle build configuration
  - Database schema design (14 tables)
  - Flyway migration scripts
  - JPA entity classes (14 entities + builders)
  - Repository interfaces (13 repositories)
  - H2/PostgreSQL dual configuration
- **Complexity:** Medium — Requires careful schema design for normalization

#### Week 3-4: CSV Import System
- **Effort:** 2 weeks
- **Tasks:**
  - Generic CSV parsing framework
  - DeGiro transaction parser
  - ZERO transaction parser
  - DeGiro account statement parser (dividends)
  - ZERO account statement parser (dividends)
  - Dividend file parser
  - Country/branch/ticker symbol parsers
  - German decimal number parsing (1.234,56 format)
  - Import validation and error handling
- **Complexity:** High — Two different broker formats with edge cases

#### Week 5-6: Portfolio Calculations
- **Effort:** 2 weeks
- **Tasks:**
  - Stock aggregation (per depot + total)
  - Average entry price calculation
  - Performance calculation (gain/loss %)
  - Dividend income estimation
  - Diversification analysis (country + branch)
  - Top 5 holdings calculation
  - Dashboard KPI calculations
- **Complexity:** Medium-High — Requires financial domain knowledge

#### Week 7-8: REST API Layer
- **Effort:** 2 weeks
- **Tasks:**
  - 13 controller classes
  - DTOs for all endpoints
  - Server-side filtering logic
  - Server-side sorting logic
  - Pagination implementation
  - Query parameter validation
  - Error handling and logging
- **Complexity:** Medium — Repetitive but requires careful design

### Phase 2: Backend Advanced (4-6 weeks)

#### Week 9-10: Web Scraping for Quotes
- **Effort:** 2 weeks
- **Tasks:**
  - 8 quote provider scrapers:
    - JustETF (API + HTML fallback)
    - Onvista
    - FinanzenNet
    - CNBC
    - FondsDiscount (EUR + USD)
    - ComDirect
    - WallstreetOnline
    - MarketBeat
  - Scheduled quote fetching
  - HTML parsing and data extraction
  - Error handling for failed fetches
  - Quote storage and retrieval
- **Complexity:** High — Web scraping is fragile and site-dependent

#### Week 11-12: Export System
- **Effort:** 2 weeks
- **Tasks:**
  - CSV export for all tables
  - Excel export (Apache POI integration)
  - Export respects filters and sorting
  - Proper file naming and headers
  - Multi-sheet Excel exports
- **Complexity:** Medium — Library integration work

#### Week 13-14: Testing & Refinement
- **Effort:** 2 weeks
- **Tasks:**
  - Unit tests for services (25+ test classes)
  - Integration tests for controllers
  - CSV parser tests with sample data
  - Portfolio calculation tests
  - Test data fixtures
- **Complexity:** Medium — Comprehensive coverage needed

### Phase 3: Frontend (8-12 weeks)

#### Week 15-16: Project Setup & Layout
- **Effort:** 2 weeks
- **Tasks:**
  - Vite + React + TypeScript setup
  - Strato design system integration
  - Routing (React Router)
  - Layout component (sidebar navigation)
  - Axios client configuration
  - Dark/light mode support
- **Complexity:** Medium — Strato has learning curve

#### Week 17-20: Core Pages (4 weeks)
- **Effort:** 4 weeks (~3 days per page)
- **Tasks:**
  - Dashboard (KPIs + Top 5 tables)
  - Transactions (filterable table + date range)
  - Stocks (aggregated view)
  - Stocks per Depot (grouped view)
  - Dividend Payments (new feature)
- **Complexity:** Medium-High — Each page has filters, sorting, pagination

#### Week 21-23: Reference Data Pages (3 weeks)
- **Effort:** 3 weeks (~2-3 days per page)
- **Tasks:**
  - Countries
  - Branches
  - Depots
  - Currencies
  - Ticker Symbols
  - ISIN Names
  - Analytics (Country/Branch charts)
- **Complexity:** Medium — Simpler than core pages

#### Week 24-25: Import & Settings (2 weeks)
- **Effort:** 2 weeks
- **Tasks:**
  - File upload UI (8 different import types)
  - Import status indicators
  - Settings page (quote fetch interval)
  - Quote fetch trigger
- **Complexity:** Medium — File handling complexity

#### Week 26: Frontend Testing
- **Effort:** 1 week
- **Tasks:**
  - Vitest unit tests for components
  - Playwright E2E tests
  - Accessibility testing
- **Complexity:** Medium

### Phase 4: Integration & DevOps (4-8 weeks)

#### Week 27-28: Authentication
- **Effort:** 2 weeks
- **Tasks:**
  - Clerk integration (frontend + backend)
  - Protected routes
  - JWT token validation
  - User session management
- **Complexity:** Medium — Following Clerk documentation

#### Week 29-30: Docker & Deployment
- **Effort:** 2 weeks
- **Tasks:**
  - Multi-stage Dockerfile
  - Docker Compose for local dev
  - Production build optimization
  - PostgreSQL connection configuration
  - Environment variable management
- **Complexity:** Medium

#### Week 31-32: Polish & Documentation
- **Effort:** 2 weeks
- **Tasks:**
  - Error message improvements
  - Loading states and spinners
  - Mobile responsive fixes
  - API documentation (Swagger)
  - User documentation
- **Complexity:** Low-Medium

#### Week 33-34: Production Deployment & Monitoring
- **Effort:** 2 weeks
- **Tasks:**
  - Neon PostgreSQL setup
  - Production deployment
  - Logging configuration
  - Monitoring setup
  - Bug fixing from production
- **Complexity:** Medium

---

## Skill Requirements

### Required Skills
- ✅ **Java 21** — Core language proficiency
- ✅ **Spring Boot 3** — Framework fundamentals
- ✅ **JPA/Hibernate** — ORM and database mapping
- ✅ **React 18** — Modern React with hooks
- ✅ **TypeScript** — Type system and patterns
- ✅ **SQL** — Schema design and queries
- ✅ **REST API design** — API patterns and conventions
- ✅ **Git** — Version control

### Nice-to-Have Skills (Accelerators)
- ⚡ **Flyway** — Database migrations
- ⚡ **Gradle** — Build tooling
- ⚡ **Vite** — Frontend bundler
- ⚡ **Web scraping** — HTML parsing, HTTP clients
- ⚡ **Apache POI** — Excel file generation
- ⚡ **Docker** — Containerization
- ⚡ **PostgreSQL** — Database specifics

### Learning Curve Items (Add Time)
- 📚 **Strato Design System** — ~1 week to learn (proprietary Dynatrace UI library)
- 📚 **Financial domain knowledge** — Portfolio calculations, ISIN codes, dividends (~1-2 weeks)
- 📚 **Broker CSV formats** — Understanding DeGiro/ZERO exports (~3-4 days)

---

## Risk Factors & Uncertainty

### High Risk (Could add 2-4 weeks each)
1. **Web scraping fragility** — Quote provider websites change frequently
2. **Browser automation** — Some sites may require Selenium/Playwright
3. **Edge cases in CSV parsing** — Broker formats have quirks
4. **Authentication complexity** — Clerk integration may have surprises

### Medium Risk (Could add 1-2 weeks each)
1. **Performance issues** — Large transaction datasets may need optimization
2. **Cross-browser compatibility** — Strato components may behave differently
3. **PostgreSQL dialect differences** — Subtle differences from H2
4. **Mobile responsive design** — Complex tables on small screens

### Low Risk (Manageable)
1. **German locale number parsing** — Well-specified
2. **Export functionality** — Libraries are mature
3. **Basic CRUD operations** — Straightforward

---

## Assumptions

This estimate assumes:
- ✅ Developer has **intermediate experience** with Spring Boot and React (2+ years)
- ✅ Developer works **full-time** (40 hours/week)
- ✅ Requirements are **stable** (docs don't change mid-development)
- ✅ **No major blockers** (library conflicts, infrastructure issues)
- ✅ Developer has **access to sample CSV files** for testing
- ✅ **Strato design system documentation** is available

---

## Effort by Experience Level

| Experience Level | Total Time | Notes |
|------------------|------------|-------|
| **Junior (0-2 years)** | 9-12 months | Needs time to learn Spring Boot ecosystem, financial domain |
| **Mid-level (2-5 years)** | 6-9 months | **This estimate** — Has Spring Boot + React experience |
| **Senior (5+ years)** | 4-6 months | Familiar with similar systems, fewer blockers |
| **Expert (10+ years)** | 3-4 months | Deep expertise, knows shortcuts and patterns |

---

## Comparison to Actual Implementation

Based on the current codebase metrics:
- **121 Java files** — Suggests ~3-4 months of backend work ✅
- **25 test files** — Indicates good test coverage (~20% test ratio) ✅
- **44 TypeScript/React files** — Suggests ~2-3 months of frontend work ✅
- **13 pages with detailed specs** — Well-documented requirements ✅

**Estimated vs. Actual:** The current implementation appears to align with a **6-9 month effort** for a mid-level developer working full-time.

---

## Recommendations for Implementation

### To Reduce Time
1. **Start with simplified quote fetching** — Use a single provider initially
2. **Defer authentication** — Build with open endpoints first
3. **Use mock data** — Start with sample CSVs, add real broker parsing later
4. **Minimal viable UI** — Build basic tables first, add filters/sorting incrementally
5. **Leverage AI coding assistants** — Claude/GitHub Copilot can accelerate boilerplate

### To Improve Quality
1. **Write tests early** — Don't defer testing to the end
2. **Set up CI/CD from day 1** — Automated testing saves time
3. **Use the detailed specifications** — Don't reinvent the wheel
4. **Follow the coding guidelines** — Consistency reduces debugging time
5. **Get early feedback** — Deploy a demo early and iterate

---

## Conclusion

**For an average software engineer (2-5 years experience):**
- **Realistic estimate:** 6-9 months full-time
- **Optimistic (with AI assistance):** 4-6 months
- **Conservative (with learning curve):** 9-12 months

The project is **moderately complex** but well-documented. The combination of financial domain knowledge, web scraping, multi-broker CSV parsing, and a custom UI library (Strato) adds significant complexity beyond a typical CRUD application.

**Key success factors:**
1. Understanding the financial domain (portfolios, dividends, ISINs)
2. Careful CSV parsing with robust error handling
3. Efficient server-side filtering/sorting/pagination
4. Responsive UI with good UX for complex data tables
5. Reliable web scraping that handles failures gracefully

The detailed specifications in `docs/` significantly reduce risk and uncertainty.

