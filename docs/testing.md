# Testing Requirements

This document defines the testing conventions that apply across the entire project. Page-specific test requirements (e.g., `pages/transactions.md`) take precedence when they conflict with these general rules.

---

## Backend Testing (JUnit 5 + Spring Boot Test)

### Unit Tests

- **Framework:** JUnit 5 + Mockito.
- **Assertions:** Use **AssertJ** (`assertThat`) exclusively — do not use JUnit's `assertEquals` / `assertTrue`.
- **Structure:** Every test method must follow **Given / When / Then**, marked with line comments:
  ```java
  @Test
  void shouldReturnEmptyWhenIsinNotFound() {
      // given
      var service = new StockService(mockRepo);
      when(mockRepo.findByIsin("XX")).thenReturn(Optional.empty());

      // when
      var result = service.findByIsin("XX");

      // then
      assertThat(result).isEmpty();
  }
  ```
- **Naming:**
  - Test class: `<ClassUnderTest>Test` (e.g., `StockServiceTest`).
  - Test method: `should<ExpectedBehaviour>[When<Condition>]` — no `test` prefix.
- **Class modifiers:** Test classes must be **package-private and `final`** — no `public` modifier.
- **Coverage:** Every Java class must have a corresponding test class covering all public methods and constructors.
- **Isolation:** Mock external dependencies (repositories, HTTP clients). Keep tests independent — no shared mutable state between test methods.
- **One concept per test:** One logical assertion per test (multiple `assertThat` calls are fine if they verify the same concept).

### REST API Integration Tests

- **Annotations:** `@SpringBootTest` + `@AutoConfigureMockMvc` + `@ActiveProfiles("test")`.
- **Database:** H2 in-memory database in PostgreSQL compatibility mode, configured in `application-test.yml`:
  ```yaml
  spring:
    datasource:
      url: jdbc:h2:mem:folio;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH
      driver-class-name: org.h2.Driver
    jpa:
      database-platform: org.hibernate.dialect.H2Dialect
      hibernate:
        ddl-auto: validate
    flyway:
      enabled: true
      locations: classpath:db/migration
  folio:
    security:
      enabled: false
  ```
- **Pattern:** Inject `MockMvc` via `@Autowired`. Use `MockMvcRequestBuilders.get()` with `MockMvcResultMatchers` for status codes and JSON path assertions.
- **Scope per controller test:**
  - Default endpoint with expected response structure.
  - All optional query parameters (filters, sorting, pagination).
  - Export endpoints (CSV and Excel) with correct content types and headers.
  - Edge cases (empty results, invalid input).
- **Flyway migrations** run automatically against H2 to create the schema.

### Existing Test Classes

- **Unit tests:** Tiny types (`IsinCodeTest`), DTOs (`DashboardDtoTest`, `StockDtoTest`, `TransactionDtoTest`, `TransactionFilterTest`), models (`IsinTest`, `TransactionTest`, `DividendTest`, `DividendPaymentTest`, `IsinQuoteTest`), services (`ExportServiceTest`), quote system (`IsinsQuoteLoaderTest`, `QuoteFetchHelperTest`), external-API clients (`IsinToTickerTest`, `QuoteFetcherTest`).
- **External API integration tests:** See [External API Integration Tests](#external-api-integration-tests) section below.
- **REST API integration tests:** All 12 controllers tested against H2 in PostgreSQL mode:
  - `ReferenceDataControllerTest` — depots, currencies, countries, branches (GET + CSV/Excel export)
  - `DashboardControllerTest` — dashboard structure, empty portfolio, holdings/dividends export
  - `TransactionControllerTest` — transaction list, optional filters, date filters, export with sort
  - `StocksControllerTest` — aggregated positions list, export with country/branch filters and sorting
  - `StocksPerDepotControllerTest` — positions per depot, export with country/branch/depot filters and sorting
  - `AnalyticsControllerTest` — country/branch diversification structure, export with sort
  - `QuoteControllerTest` — settings GET/PUT, enable/disable toggle, interval validation, trigger fetch
  - `ImportControllerTest` — branches/countries/dividends/ticker-symbols CSV import, empty/invalid input handling
  - `IsinNameControllerTest`, `TickerSymbolControllerTest` — GET + export
  - `ImportToQueryIntegrationTest` — end-to-end pipeline: import reference data → verify via query endpoints
  - `DividendPaymentControllerTest` — dividend payment list, filters, date filters, export with sort

### Running Backend Tests

```bash
cd backend && ./gradlew test
```

---

## External API Integration Tests

These tests call **real external services** over the network — no mocking, no in-memory database. They verify that the full HTTP round-trip, JSON parsing, and domain mapping work against live endpoints.

### Conventions

- Class name: `<ClassUnderTest>IntegrationTest` (e.g. `QuoteFetcherIntegrationTest`).
- Instantiate the class under test with its **public no-arg constructor** — no Spring context.
- Print the result to stdout (e.g. `System.out.printf(...)`) so the actual live value is visible in the test output.
- Assertions must be **value-agnostic** (e.g. `isGreaterThan(0)`, `isNotBlank()`, `isPresent()`) since live values change constantly. The only exceptions are structural invariants like currency code format or timestamp being non-null.
- One test per meaningful live scenario; always include an "unknown / invalid input → empty" case.
- These tests are **not** run in CI by default — they require a live network connection and may be rate-limited.

### Existing Integration Test Classes

| Class | Package | External Service | What it verifies |
|-------|---------|-----------------|-----------------|
| `WallStreetOnlineTest` | `com.test` | wallstreet-online.de | HTML quote scraping returns price `> 0` |
| `IsinToTickerIntegrationTest` | `com.test` | api.openfigi.com | Single + batch ISIN → ticker lookup; invalid ISIN → empty |
| `QuoteFetcherIntegrationTest` | `com.folio.quote.yahoo` | query1.finance.yahoo.com | `regularMarketPrice`, `currency`, `regularMarketTime` parsed into `Quote`; unknown ticker → empty |

---

## Frontend Testing (Vitest + React Testing Library)

### Configuration

- **Runner:** Vitest with `jsdom` environment, configured in `vitest.config.ts`.
- **Setup file:** `src/test/setup.tsx` — mocks all Strato design system components (`@dynatrace/strato-components/*`) with lightweight HTML equivalents so tests run without the full Strato runtime.
- **Utilities:** `src/test/test-utils.tsx` — provides `renderWithRouter()` for components that need React Router context.

### Strato Component Mocks

All Strato components are mocked globally in `setup.tsx`. Key mocks include:

| Strato Component | Mock Behaviour |
|-----------------|---------------|
| `AppRoot` | Renders children directly |
| `Flex`, `Surface` | `<div>` with `data-testid` |
| `Heading` | Renders appropriate `<h1>`–`<h6>` tag |
| `Button` | `<button>` with `onClick` |
| `TextInput` | `<input type="text">` with `onChange` |
| `Select` / `Select.Option` | `<select>` / `<option>` |
| `DataTable` | `<table>` rendering columns and rows, with sort simulation |
| `ProgressCircle` | `<div role="progressbar">` |
| `TimeframeSelector` | `<div>` with presets, value display, and clear button |
| `Modal` | Conditional `<div role="dialog">` |

When adding new Strato components to pages, add corresponding mocks to `setup.tsx`.

### API Mocking

- Mock the API client module: `vi.mock('../api/client', () => ({ default: { get: vi.fn() } }))`.
- Use `vi.mocked(api.get).mockImplementation(...)` to return different responses based on URL.
- Mock child components that are not under test (e.g., `ExportButtons`) to isolate the component.

### Test Scope per Page

Each page `.test.tsx` file must cover:

| Category | What to verify |
|----------|---------------|
| Loading state | `ProgressCircle` / `role="progressbar"` renders while API call is pending |
| Column headers | All column headers present with correct text |
| Data rendering | Mock data appears in the table |
| Row count | Summary text shows correct count (e.g., `"42 transactions"`) |
| Aggregations | Sums, totals, or other computed values display correctly |
| Default API call | First fetch uses correct default sort, pagination, and filter params |
| Filter inputs | Typing in filter fields triggers API call with correct params |
| Multi-select filters | Dropdown renders options; selecting sends comma-separated values |
| Clear button | Resets all filters and triggers re-fetch with defaults |
| Refresh button | Present and functional |
| Page-size selector | Renders with correct options (10, 20, 50, 100); changing triggers re-fetch |
| Column alignment | `data-alignment` attributes match spec |
| Export buttons | `ExportButtons` component renders |
| Session storage | Filters persist to and restore from `sessionStorage` |

### Conventions

- Use `renderWithRouter(<Component />)` for all page components.
- Use `waitFor(() => { expect(...) })` for async assertions after API calls resolve.
- Use `userEvent.setup()` for user interactions (typing, clicking), not `fireEvent`.
- Clear `sessionStorage` in `beforeEach` to ensure test isolation.
- One `describe` block per component, with individual `it` blocks per behaviour.

### Existing Test Files

- `App.test.tsx` — route-to-component mapping for all 13 routes
- `Layout.test.tsx` — sidebar navigation rendering, active item highlighting, click and keyboard navigation
- `ExportButtons.test.tsx` — CSV/Excel download URL construction, parameter forwarding, empty param omission
- `Dashboard.test.tsx` — KPI card rendering, top-5 tables, last-updated timestamp formatting, null timestamp handling
- `Countries.test.tsx` — loading indicator, data rendering, pagination toggle, export buttons
- `Stocks.test.tsx` — loading indicator, column headers (no depot), data rows, filter dropdowns (no depot), API call verification
- `StocksPerDepot.test.tsx` — loading indicator, column headers (with depot), data rows, filter dropdowns (with depot), API call verification
- `Settings.test.tsx` — loading state, enabled/disabled text, timestamp formatting, Fetch Now trigger
- `DividendPayments.test.tsx` — loading, columns, data, filters, TimeframeSelector, sessionStorage, export

### Running Frontend Unit Tests

```bash
cd frontend && npm test          # single run
cd frontend && npm run test:watch # watch mode
```

---

## End-to-End Tests (Playwright)

### Configuration

- **Runner:** Playwright with Chromium, configured in `playwright.config.ts`.
- **Test directory:** `frontend/e2e/`.
- **Base URL:** `http://localhost:5173` (Vite dev server).
- **Web server:** Playwright auto-starts the frontend dev server via `webServer` config.
- **Prerequisites:** Backend must be running separately.

### Test Structure

- **`navigation.spec.ts`** — Sidebar navigation: all nav items render, clicking navigates to correct page, active item highlighting, app header.
- **`pages.spec.ts`** — Per-page structure tests grouped by `test.describe`:
  - Page heading renders.
  - Key controls are visible (filter inputs, dropdowns, buttons).
  - Export buttons present.
  - Show All / Refresh buttons present.

### Scope per Page

Each page should have a `test.describe` block in `pages.spec.ts` verifying:

- Page heading via `page.locator('h1')`.
- Filter inputs and dropdowns are visible.
- Export buttons (CSV / Excel) are visible.
- Show All and Refresh buttons are visible.

### Conventions

- Use `page.goto('/route')` + `expect(locator).toBeVisible()` pattern.
- Prefer `page.getByText()`, `page.getByPlaceholder()`, `page.locator()` for element selection.
- Group tests by page using `test.describe`.
- Tests run against a live frontend + backend — not mocked.

### Existing Test Files

- `navigation.spec.ts` — page loads, sidebar items, inter-page navigation, active item highlighting, app header
- `pages.spec.ts` — Dashboard KPIs and sections, Countries/Stocks/Settings/Import/Analytics/DividendPayments page rendering

### Running E2E Tests

```bash
cd frontend && npm run test:e2e      # headless
cd frontend && npm run test:e2e:ui   # interactive UI mode
```

---

## General Principles

- Every new page or component requires corresponding tests at all three levels (unit, component, E2E).
- Tests must be deterministic and independent — no reliance on execution order or shared state.
- Prefer testing observable behaviour (what the user sees) over implementation details.
- Keep test data minimal but representative — include edge cases (null values, empty lists, boundary numbers).
- Verify a seamless experience across major browsers and device types.
- Validate that the interface is intuitive through usability testing.