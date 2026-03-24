# Project Description

## What it does

Folio is a personal investment portfolio analysis and visualization tool for tracking securities 
across multiple brokers, calculating gains/losses, dividends.

In the first version KeSt (Austrian capital gains tax) calculation is a stated goal, but it is not yet clear how this will be implemented. Therefore, KeSt is currently out of scope for v1 and will be explicitly deferred to a future version.

## Who uses it
It is a personal tool, just for me. Only one portfolio is managed. There can be multiple users with access to the same portfolio, but there is no concept of multiple portfolios or user-specific portfolios.

## Requirements
- User registration and authentication
- The swagger ui and the rest endpoints should be protected and only accessible for authenticated users
- The application should have a user-friendly interface for managing data
- All data tables in the UI shall have resizable columns
- The application should have a responsive design that works well on both desktop and mobile devices
- The application should have a dark mode and a light mode, with the ability to switch between them
- The application should have proper error handling and display user-friendly error messages
- The application should have proper logging for debugging and monitoring purposes
- The application should have a well-defined data model and use a relational database for persistence
- The application should have a clear separation of concerns between the frontend and backend
- The application should have proper authentication and authorization mechanisms in place to ensure security
- The application should have a production build that can be easily deployed and run on linux and windows
- The application should have API documentation that is easily accessible and up-to-date
- The application should have a clear and modern design, with a focus on usability and accessibility
- The application should have a clean separation of concerns between frontend and backend that allows for scalability and maintainability
- The application should have a well-defined API that allows for easy integration with other systems and services
- The application should have a clear and consistent coding style and follow best practices for software development
- The application should have a comprehensive test suite that covers all major functionality and edge cases

### Data Import

#### Fetching Quotes
The backend fetches periodically quote data via webscrapping. The interval can be changed dynamically via the UI and is stored database in table settings. The quotes are stored in the database and used for performance calculation and display in the UI.

#### Broker Transaction Import
- The system shall import transaction data from **DeGiro** broker CSV exports (see sample: [`docs/samples/Transactions.csv`](docs/samples/Transactions.csv)). The share price stored per transaction shall be derived from the `Wert EUR` column (index 11, total trade value in EUR) divided by the absolute share count, not from the `Kurs` column (index 7), which is denominated in the security's local trading currency and may not be EUR. Rows where `Anzahl` (count) is zero shall be skipped entirely — they represent non-trade entries such as fees or dividends and would cause division by zero in the price formula.
- The system shall import transaction data from **ZERO** broker CSV exports (see sample: [`docs/samples/ZERO-orders-22.03.2026.csv`](docs/samples/ZERO-orders-22.03.2026.csv)).
- The system shall normalize ISINs across all broker sources into a unified transaction model.

#### Account Statement Import
- The system shall import DeGiro account statement CSVs (see sample: [`docs/samples/Account.csv`](docs/samples/Account.csv)) to extract dividend payments.
- The system shall import ZERO account statement CSVs (see sample: [`docs/samples/ZERO-kontoumsaetze-22.03.2026.csv`](docs/samples/ZERO-kontoumsaetze-22.03.2026.csv)) to extract dividend payments. Only rows with status `gebucht` and purpose starting with `Coupons/Dividende` are considered dividends. The ISIN is extracted from the purpose field, and the valuta date is used as the payment date.

#### Dividend File Import
- The system shall read a manually maintained dividend file (`dividende.csv`) containing expected dividend yields per ISIN (see sample: [`docs/samples/dividende.csv`](docs/samples/dividende.csv)).
- For the file upload via UI, the system shall provide a user interface to upload the `dividende.csv` file and validate its format and content before processing.

### Diversification Analysis

#### Branch Diversification
- The system shall categorize each ISIN by industry/branch (see sample: [`docs/samples/branches.csv`](docs/samples/branches.csv)).
- The system shall calculate the total invested amount per branch.
- The system shall output a branch diversification breakdown.

#### Country Diversification
- The system shall categorize each ISIN by country (see sample: [`docs/samples/countries.csv`](docs/samples/countries.csv)).
- The system shall calculate the total invested amount per country.
- The system shall output a country diversification breakdown.

### Data model
- The system shall have a well-defined data model that represents the entities and relationships in the application, such as users, transactions, positions, quotes, branches, countries, etc.
- The data model shall be designed to support the required functionality and performance of the application, with proper normalization and indexing for efficient querying and data integrity.

#### `ticker_symbol`
Maps a ticker symbol to a security.

| Column | Type        | Constraints      | Description                        |
|--------|-------------|------------------|------------------------------------|
| id     | INTEGER     | PK               | Surrogate primary key              |
| symbol | VARCHAR(20) | UNIQUE, NOT NULL | Ticker symbol (e.g. AAPL, BASF.DE) |

#### `currency`
Reference table of all currencies.

| Column | Type        | Constraints      | Description                    |
|--------|-------------|------------------|--------------------------------|
| id     | INTEGER     | PK               | Surrogate primary key          |
| name   | VARCHAR(3)  | UNIQUE, NOT NULL | Currency code (e.g. EUR, USD)  |

#### `isin`
Central registry of all known securities by ISIN.

| Column | Type         | Constraints | Description             |
|--------|--------------|-------------|-------------------------|
| id     | INTEGER      | PK          | Surrogate primary key   |
| isin   | VARCHAR(12)  | UNIQUE, NOT NULL | Security identifier (e.g. DE000BASF111) |

#### `dividend`
Manually maintained expected annual dividend per ISIN. Sourced from [`docs/samples/dividende.csv`](docs/samples/dividende.csv).

| Column             | Type         | Constraints      | Description                          |
|--------------------|--------------|------------------|--------------------------------------|
| id                 | INTEGER      | PK               | Surrogate primary key                |
| isin_id            | INTEGER      | FK → isin.id     | Reference to the security            |
| currency_id        | INTEGER      | FK → currency.id | Reference to the currency            |
| dividend_per_share | DOUBLE       | NOT NULL         | Expected annual dividend per share   |

#### `depot`
Represents a broker account/depot where securities are held.

| Column | Type         | Constraints      | Description           |
|--------|--------------|------------------|-----------------------|
| id     | INTEGER      | PK               | Surrogate primary key |
| name   | VARCHAR(100) | UNIQUE, NOT NULL | Depot name (e.g. DeGiro, ZERO) |

#### `transaction`
Records a buy or sell transaction for a security.

| Column      | Type      | Constraints      | Description                        |
|-------------|-----------|------------------|------------------------------------|
| id          | INTEGER   | PK               | Surrogate primary key              |
| date        | TIMESTAMP | NOT NULL         | Date and time of the transaction   |
| isin_id     | INTEGER   | FK → isin.id     | Reference to the security          |
| depot_id    | INTEGER   | FK → depot.id    | Reference to the depot             |
| count       | DOUBLE    | NOT NULL         | Number of shares bought or sold    |
| share_price | DOUBLE    | NOT NULL         | Price per share at time of transaction |

#### `country`
Reference table of all countries. Sourced from [`docs/samples/countries.csv`](docs/samples/countries.csv).

| Column | Type         | Constraints   | Description        |
|--------|--------------|---------------|--------------------|
| id     | INTEGER      | PK            | Surrogate primary key |
| name   | VARCHAR(100) | UNIQUE, NOT NULL | Country name (e.g. BRD, USA, Irland) |

#### `branch`
Reference table of all industry branches. Sourced from [`docs/samples/branches.csv`](docs/samples/branches.csv).

| Column | Type         | Constraints      | Description                        |
|--------|--------------|------------------|------------------------------------|
| id     | INTEGER      | PK               | Surrogate primary key              |
| name   | VARCHAR(100) | UNIQUE, NOT NULL | Branch name (e.g. Technology, Energy) |

#### `isin_country`
Maps an ISIN to its country of origin. Sourced from [`docs/samples/countries.csv`](docs/samples/countries.csv).

| Column     | Type    | Constraints              | Description          |
|------------|---------|--------------------------|----------------------|
| isin_id    | INTEGER | PK, FK → isin.id         | Reference to ISIN    |
| country_id | INTEGER | PK, FK → country.id      | Reference to country |

#### `isin_name`
Maps an ISIN to a human-readable security name. An ISIN can have multiple names (e.g. different names used across brokers or import files). Names are only added, never overwritten. A composite unique constraint on `(isin_id, name)` prevents exact duplicate entries.

| Column  | Type         | Constraints      | Description                        |
|---------|--------------|------------------|------------------------------------|
| id      | INTEGER      | PK                       | Surrogate primary key             |
| isin_id | INTEGER      | FK → isin.id             | Reference to the security         |
| name    | VARCHAR(255) | NOT NULL                 | Security name (e.g. "Apple Inc.") |
|         |              | UNIQUE (isin_id, name)   | Prevents duplicate (ISIN, name) pairs |

#### `isin_ticker`
Maps an ISIN to its ticker symbol(s).

| Column           | Type    | Constraints                  | Description               |
|------------------|---------|------------------------------|---------------------------|
| isin_id          | INTEGER | PK, FK → isin.id             | Reference to ISIN         |
| ticker_symbol_id | INTEGER | PK, FK → ticker_symbol.id    | Reference to ticker symbol |

#### `isin_branch`
Maps an ISIN to its industry branch. Sourced from [`docs/samples/branches.csv`](docs/samples/branches.csv).

| Column    | Type    | Constraints          | Description         |
|-----------|---------|----------------------|---------------------|
| isin_id   | INTEGER | PK, FK → isin.id     | Reference to ISIN   |
| branch_id | INTEGER | PK, FK → branch.id   | Reference to branch |

#### `quote_provider`
Reference table of all quote data sources used in the cascading fallback.

| Column | Type         | Constraints      | Description                              |
|--------|--------------|------------------|------------------------------------------|
| id     | INTEGER      | PK               | Surrogate primary key                    |
| name   | VARCHAR(100) | UNIQUE, NOT NULL | Provider name (e.g. `JustETF`, `Onvista`) |

#### `settings`
Key-value store for application configuration (e.g. quote fetch interval).

| Column | Type         | Constraints      | Description                        |
|--------|--------------|------------------|------------------------------------|
| id     | INTEGER      | PK               | Surrogate primary key              |
| key    | VARCHAR(100) | UNIQUE, NOT NULL | Setting name (e.g. `quote.fetch.interval`) |
| value  | VARCHAR(500) | NOT NULL         | Setting value                      |

#### `dividend_payment`
Records an actual dividend payment received for a security in a specific depot.

| Column      | Type             | Constraints      | Description                              |
|-------------|------------------|------------------|------------------------------------------|
| id          | INTEGER          | PK               | Surrogate primary key                    |
| timestamp   | TIMESTAMP        | NOT NULL         | Date and time the dividend was received  |
| isin_id     | INTEGER          | FK → isin.id     | Reference to the security                |
| depot_id    | INTEGER          | FK → depot.id    | Reference to the depot                   |
| currency_id | INTEGER          | FK → currency.id | Reference to the currency                |
| value       | DOUBLE PRECISION | NOT NULL         | Dividend amount received                 |

### Use cases

#### Import Transactions.csv
UI should provide a file upload interface for the user to upload the `Transactions.csv` file exported from DeGiro. The system should parse the CSV file, extract transaction data, and store it in the database according to the defined data model. The system should handle any parsing errors gracefully and provide feedback to the user about the success or failure of the import process. The system should also ensure that ISINs are normalized and mapped correctly to the `isin` table, and that transactions are associated with the correct depot (DeGiro). before the data will be written to the table transactions all entries for the depot DeGiro should be deleted to avoid duplicates and to reflect any changes in the transaction history. 

#### Import ZERO-orders.csv
similar to the import of `Transactions.csv`, the UI should provide a file upload interface for the user to upload the `ZERO-orders-*.csv` file exported from ZERO. The system should parse the CSV file, extract transaction data, and store it in the database according to the defined data model. The system should handle any parsing errors gracefully and provide feedback to the user about the success or failure of the import process. The system should also ensure that ISINs are normalized and mapped correctly to the `isin` table, and that transactions are associated with the correct depot (Trade Republic). before the data will be written to the table transactions all entries for the depot Trade Republic should be deleted to avoid duplicates and to reflect any changes in the transaction history.

#### Import Account.csv
The UI should provide a file upload interface for the user to upload the `Account.csv` file exported from DeGiro. The system should parse the CSV file and extract dividend payment records. Only rows where the description field equals `"Dividende"` are processed. Each valid row is stored as a `dividend_payment` entry associated with the DeGiro depot, using the valuta date as the payment timestamp and recording the currency. The system should handle parsing errors gracefully and provide feedback about the success or failure of the import. Before writing new records, all existing `dividend_payment` entries for the DeGiro depot should be deleted to avoid duplicates.

#### Import ZERO-kontoumsaetze.csv
The ui should provide a file upload interface for the user to upload the `ZERO-kontoumsaetze-*.csv`. THe data should be parsed and dividend payment records should be extracted and writen to table `dividend_payment`. as depot the value for ZERO should be used. before the records are stored to the table, all existing `dividend_payment` entries for the ZERO depot should be deleted to avoid duplicates and to reflect any changes in the transaction history.

#### Import dividende.csv
The UI should provide a file upload interface for the user to upload the `dividende.csv` file, which contains the expected annual dividend per share for each ISIN. The system should parse the CSV file, extract the dividend data, and store it in the database according to the defined data model. The system should handle any parsing errors gracefully and provide feedback to the user about the success or failure of the import process. The system should also ensure that ISINs are normalized and mapped correctly to the `isin` table, and that dividend data is associated with the correct ISINs in the `dividend` table. If an ISIN from the CSV file does not exist in the `isin` table, it should be added to the `isin` table before storing the dividend data.

#### Dashboard
The UI should provide a dashboard as the landing page, giving a quick overview of the portfolio. It should display:
- **Total portfolio value**: the sum of (total shares × avg entry price) across all open positions.
- **Number of different securities**: the count of distinct ISINs currently held.
- **Total dividend ratio**: the estimated annual dividend income divided by the total portfolio value, expressed as a percentage.
- **Top 5 holdings**: the five positions with the highest invested value (total shares × avg entry price), showing security name, ISIN, and invested amount.
- **Top 5 dividend sources**: the five securities with the highest estimated annual dividend income (shares × dividend per share), showing security name, ISIN, and estimated annual income.
the UI shows date and time of the last successful quote fetch, so the user can see how up-to-date the displayed quotes are.S

The data should be fetched from the backend via a dedicated REST API endpoint.

#### Show transactions

The UI shall provide a view displaying all transactions fetched from the backend in a sortable, resizable table. All rows are loaded at once and filtered client-side.

**Columns:**

| Column | Format                                                                       | Alignment | Min Width |
|--------|------------------------------------------------------------------------------|-----------|-----------|
| Date | `DD-MM-YYYY`; sortable (sort key: ISO `YYYY-MM-DD` for correct chronological order); default sort: descending (newest first) | left | 105 px |
| ISIN | plain text                                                                   | left | 140 px |
| Name | plain text                                                                   | left | 120 px |
| Depot | plain text                                                                   | left | 80 px |
| Count | exactly 2 decimal places; comma as decimal separator (e.g. `100,00`, `0,12`) | right | 80 px |
| Share Price | exactly 2 decimal places; comma as decimal separator (e.g. `123,45`)         | right | 100 px |

**Filtering:**
- **ISIN filter**: free-text input; case-insensitive partial match updated in real time as the user types (e.g. typing `DE000` shows all transactions whose ISIN contains that substring). A Clear button appears next to the field and resets the filter. Double-clicking an ISIN value in the table copies it to this filter, immediately showing only that ISIN's transactions.
- **Name filter**: free-text input; case-insensitive partial match updated in real time as the user types. A Clear button appears next to the field and resets the filter. Double-clicking a Name value in the table copies it to this filter, immediately showing only transactions with that security name.
- **Depot filter**: dropdown listing all depots present in the loaded data plus an "All depots" option. Selecting a depot restricts the view to that depot's transactions; selecting "All depots" shows all transactions.

**Loading and refresh:**
- A loading indicator (spinner) is displayed while data is being fetched from the backend.
- A Refresh button reloads all transactions from the backend.
- The row count shown above the table reflects the active filter (e.g. "42 of 16140 transactions").

**Pagination:**
- Default page size: 10 rows per page.
- Page size selector options: 10, 20, 50, 100 rows per page.
- A "Show All / Paginate" toggle switches between paginated and full-table view.

#### show countries
the UI should provide a view that displays the countries. sorted alphabetically. The countries should be fetched from the backend via a REST API endpoint that retrieves country data from the database.

#### import countries.csv
the UI should provide a file upload interface for the user to upload the `countries.csv` file, which contains the mapping of ISINs to their respective countries. The system should parse the CSV file, extract the country mapping data, and store it in the database according to the defined data model. The system should handle any parsing errors gracefully and provide feedback to the user about the success or failure of the import process. The system should also ensure that ISINs are normalized and mapped correctly to the `isin` table, and that the country mapping is associated with the correct ISINs in the `isin_country` table. If an isin already has a country mapping, it should be updated with the new value from the CSV file. if an isin does not have a country mapping yet, a new entry should be created in the `isin_country` table. if an isin from the CSV file does not exist in the `isin` table, it should be added to the `isin` table and then mapped to the country in the `isin_country` table.

#### show branches
the UI should provide a view that displays the branches. sorted alphabetically. The branches should be fetched from the backend via a REST API endpoint that retrieves branch data from the database.

#### show currencies
the UI should provide a view that displays the currencies. sorted alphabetically. The currencies should be fetched from the backend via a REST API endpoint that retrieves currency data from the database.

#### show depots
the UI should provide a view that displays the depots. sorted alphabetically. The depots should be fetched from the backend via a REST API endpoint that retrieves depot data from the database.

#### import branches.csv
the UI should provide a file upload interface for the user to upload the `branches.csv` file, which contains the mapping of ISINs to their respective industry branches. The system should parse the CSV file, extract the branch mapping data, and store it in the database according to the defined data model. The system should handle any parsing errors gracefully and provide feedback to the user about the success or failure of the import process. The system should also ensure that ISINs are normalized and mapped correctly to the `isin` table, and that the branch mapping is associated with the correct ISINs in the `isin_branch` table. If an isin already has a branch mapping, it should be updated with the new value from the CSV file. if an isin does not have a branch mapping yet, a new entry should be created in the `isin_branch` table. if an isin from the CSV file does not exist in the `isin` table, it should be added to the `isin` table and then mapped to the branch in the `isin_branch` table. 

#### country diversification breakdown
the UI should provide a view that shows the country diversification breakdown, which is calculated by the backend based on the transactions and the country mapping of ISINs. The backend should provide a REST API endpoint that calculates and returns the country diversification breakdown, which includes the total invested amount per country and the percentage of the total portfolio invested in each country. The UI should display this information in a clear and visually appealing way, such as a pie chart or a bar chart or a donutchart.

#### branch diversification breakdown
similar to the country diversification breakdown, the UI should provide a view that shows the branch diversification breakdown, which is calculated by the backend based on the transactions and the branch mapping of ISINs. The backend should provide a REST API endpoint that calculates and returns the branch diversification breakdown, which includes the total invested amount per branch and the percentage of the total portfolio invested in each branch. The UI should display this information in a clear and visually appealing way, such as a pie chart or a bar chart or a donutchart.

#### show securities
the UI should provide a view that displays all securities (ISINs) that are currently held in the portfolio, along with relevant details such as ticker symbol, name, country, branch, count, current quote, entry price, performance etc. The securities should be fetched from the backend via a REST API endpoint that retrieves security data from the database and calculates the current quote and performance based on the transactions and the current market prices. The UI should allow the user to filter and sort securities based on different criteria (e.g. country, branch, performance). The UI should also provide a way to refresh the security data to reflect any changes in the transactions or market prices. The table columns shall be resizable by the user. The ISIN column shall have a fixed minimum width wide enough to display a full 12-character ISIN without clipping. The Name column shall be wide enough for typical security names. The Country and Branch columns shall be wide enough to display their values without clipping.

#### change quote fetch interval
the UI should provide a way for the user to change the interval at which the backend fetches live quotes for the securities. This could be implemented as a settings page where the user can select from predefined intervals (e.g. every 15 minutes, every hour, every 4 hours) or enter a custom interval. The selected interval should be stored in the database in table settings and used by the backend to schedule the quote fetching task. The UI should also provide feedback to the user about the current quote fetch interval and any changes made to it. REST API endpoint should be provided to update the quote fetch interval in the backend. the ui should also provide a way to trigger an immediate quote fetch, in case the user wants to update the quotes right away without waiting for the next scheduled fetch. the UI should show the date and time of the last successful quote fetch, so the user can see how up-to-date the displayed quotes are

### Quote Data System

The backend fetches live EUR-denominated prices for a set of ISINs using a **cascading fallback** approach across up to 10 sources. The central orchestrator is `IsinsQuoteLoader`. Each source is tried in order; ISINs successfully resolved are removed from the remaining set before the next source is attempted.

#### Config Files (bundled as backend resources)

Three sources require a pre-configured URL path per ISIN, stored as semicolon-delimited CSV files in `src/main/resources/`:

| File | Format | Used by |
|---|---|---|
| `finanzennet.csv` | `ISIN;relative-url-path` | Finanzen.net source |
| `onvista.csv` | `ISIN;relative-url-path` | Onvista source |
| `wallstreetonline.csv` | `ISIN;relative-url-path` | WallstreetOnline source |
| `isin.symbol.csv` | `ISIN;TICKER;Company Name` | CNBC source |

If an ISIN has no entry in the relevant config file, that source is skipped for that ISIN.

#### Cascade Fallback Order

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

#### Currency Conversion

- USD→EUR conversion uses the ECB online XML exchange rate feed (with static fallback) for steps 4 and 8.
- WallstreetOnline (step 10) uses a hardcoded factor of `0.86` for USD instead of the dynamic rate.

#### Error Handling

- Each source returns `Optional.empty()` on any failure (HTTP non-200, parse error, missing config entry, network error).
- ISINs with no successful quote from any source are simply absent from the result — no error is thrown.
- All decimal values use comma→dot normalization before parsing.


## Tech preferences

### Architecture
- clean separation of concerns between the frontend and backend
- as persistence layer, a relational database should be used https://neon.com/
- as database migration tool, flyway should be used https://flywaydb.org/
- H2 (in PostgreSQL compatibility mode) is used as the database for local/dev and testing profiles; Neon PostgreSQL is used for production
- use tiny types for domain entities (e.g. `Isin`, `Quote`, `Depot`) to encapsulate validation and domain logic, rather than using primitive types like `String` or `double` directly in the codebase

#### User management and authentication
clerk (https://clerk.com/) should be used for user management and authentication.

### Backend
- java backend and spring boot 
- backend should support a rest api for the frontend to consume
- swagger ui and openapi should be used for API documentation
- backend should have a well-defined data model and use a relational database for persistence
- backend should have proper error handling and logging
- backend should be designed with security in mind, with proper authentication and authorization mechanisms in place

### Frontend
- React 18 + TypeScript frontend, built with Vite
- Single-page application consuming the backend REST API via Axios
- Responsive design using the Strato `Page` + `Page.Sidebar` + `Page.Main` layout
- **Strato design system:** `@dynatrace/strato-components` v3.1.1 with `@dynatrace/strato-design-tokens` for CSS custom properties
  - Design tokens are injected via JS in `main.tsx` (CSS import not available)
  - `AppRoot` from `@dynatrace/strato-components/core` wraps the app
  - Navigation lives in `Page.Sidebar`; the `Page` component handles responsive collapse to a drawer
  - Components imported from subpackage paths: `/layouts`, `/buttons`, `/forms`, `/tables`, `/typography`
- Dark mode and light mode with the ability to switch between them (planned; tokens support it via `@media prefers-color-scheme`)
- Clerk authentication planned (`@clerk/clerk-react`) — not yet integrated


### build tools
- gradle for the backend
- vite for the frontend
- result of the production build should be a single docker image containing both frontend and backend that can be run on linux and windows

---

## Project Scaffolding

### Root Gradle Setup

A root-level `settings.gradle` is required so IntelliJ recognizes the Gradle project when opened at the repo root. It includes the backend via composite build:

```groovy
rootProject.name = 'folio'
includeBuild('backend')
```

### Backend (Spring Boot + Gradle)

**`build.gradle` dependencies:**
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `springdoc-openapi-starter-webmvc-ui:2.8.5`
- `flyway-core`, `flyway-database-postgresql`
- `postgresql` (runtimeOnly)
- `com.opencsv:opencsv:5.9`
- `com.h2database:h2` (runtimeOnly — dev + test)

**Note:** Lombok is NOT used. `nimbus-jose-jwt` for Clerk JWT verification is planned but not yet added.

**Package structure:**
```
com.folio
  ├── config/         # Security, OpenAPI, CORS config
  ├── controller/     # REST controllers
  ├── service/        # Business logic (ImportService, PortfolioService)
  ├── repository/     # JPA repositories
  ├── model/          # JPA entities (explicit Java, no Lombok)
  ├── dto/            # Request/Response DTOs
  ├── exception/      # Global exception handler
  ├── parser/         # CSV parsers per broker (planned, not yet extracted)
  └── quote/          # IsinsQuoteLoader + per-source fetchers (planned, not yet implemented)
```

**Tiny types:** Domain values wrapped in value types (`Isin`, `Quote`, `DepotName`) rather than raw `String`/`double`. Each tiny type lives in `model/`.

**`application.yml`:**
```yaml
spring.datasource: Neon PostgreSQL URL (from env var DATABASE_URL)
spring.flyway: enabled, locations=classpath:db/migration
clerk.jwks-uri: https://api.clerk.com/v1/jwks
```

**`application-dev.yml`:**
```yaml
spring.datasource:
  url: jdbc:h2:file:./data/folio;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
  driver-class-name: org.h2.Driver
spring.h2.console.enabled: true
spring.jpa.database-platform: org.hibernate.dialect.H2Dialect
```

**`application-test.yml`:**
```yaml
spring.datasource:
  url: jdbc:h2:mem:folio;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
  driver-class-name: org.h2.Driver
spring.jpa.database-platform: org.hibernate.dialect.H2Dialect
```

### Frontend (Vite + React + TypeScript)

**`package.json` dependencies:**
- `react@18.3.1`, `react-dom`, `react-router-dom@7.13.1`
- `@dynatrace/strato-components@3.1.1`
- `@dynatrace/strato-design-tokens@1.3.1`
- `@dynatrace/strato-icons@2.1.0`
- `@dynatrace-sdk/*` (peer deps; install with `--legacy-peer-deps`)
- `recharts@3.8.0`
- `axios@1.13.6`

**Note:** `@clerk/clerk-react` is planned but not yet installed.

**Directory structure:**
```
src/
  ├── api/            # Axios client (client.ts — baseURL hardcoded for dev, no auth yet)
  ├── components/     # Layout.tsx (Page + Sidebar + Main)
  ├── pages/          # Route-level pages
  └── types/          # TypeScript interfaces (index.ts)
```

**Global table convention:** All `DataTable` instances use the `resizable` prop.

**Strato integration pattern:**
- `AppRoot` from `@dynatrace/strato-components/core` wraps the entire app in `main.tsx`
- Design tokens injected via JS (CSS import not available)
- Layout: `Page` + `Page.Header` + `Page.Sidebar` + `Page.Main`
- `AppHeader` inside `Page.Header` for the title bar
- Sidebar contains nav links via `react-router-dom`
- Imports use subpackage paths: `/layouts`, `/buttons`, `/forms`, `/tables`, `/typography`

### Docker

Multi-stage `Dockerfile`:

```dockerfile
# Stage 1: Build frontend
FROM node:20-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/ .
RUN npm ci && npm run build

# Stage 2: Build backend
FROM eclipse-temurin:21-jdk AS backend
WORKDIR /app
COPY backend/ .
COPY --from=frontend /app/frontend/dist src/main/resources/static
RUN ./gradlew bootJar -x test

# Stage 3: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Runtime environment variables:** `DATABASE_URL`, `CLERK_PUBLISHABLE_KEY`, `CLERK_SECRET_KEY`.

---

## Database Schema (Flyway)

**`V1__create_schema.sql`** — creates all tables:

```
currency         (id, name VARCHAR(3) UNIQUE NOT NULL)
isin             (id, isin VARCHAR(12) UNIQUE NOT NULL)
isin_name        (id, isin_id FK, name VARCHAR(255) NOT NULL, UNIQUE(isin_id, name))
ticker_symbol    (id, symbol VARCHAR(20) UNIQUE NOT NULL)
isin_ticker      (isin_id FK, ticker_symbol_id FK — composite PK)
country          (id, name VARCHAR(100) UNIQUE NOT NULL)
branch           (id, name VARCHAR(100) UNIQUE NOT NULL)
isin_country     (isin_id FK, country_id FK — composite PK; UNIQUE(isin_id) enforced by V6)
isin_branch      (isin_id FK, branch_id FK — composite PK; UNIQUE(isin_id) enforced by V6)
depot            (id, name VARCHAR(100) UNIQUE NOT NULL)
transaction      (id, date TIMESTAMP, isin_id FK, depot_id FK, count DOUBLE PRECISION, share_price DOUBLE PRECISION)
dividend         (id, isin_id FK, currency_id FK, dividend_per_share DOUBLE PRECISION)
dividend_payment (id, timestamp TIMESTAMP, isin_id FK, depot_id FK, currency_id FK, value DOUBLE PRECISION)
quote_provider   (id, name VARCHAR(100) UNIQUE NOT NULL)
isin_quote       (id, isin_id FK UNIQUE, quote_provider_id FK, value DOUBLE PRECISION NOT NULL, fetched_at TIMESTAMP NOT NULL)
settings         (id, key VARCHAR(100) UNIQUE NOT NULL, value VARCHAR(500) NOT NULL)
```

**`V2__seed_depots.sql`** — inserts `DeGiro` and `ZERO` depot records.

**`V3__seed_currencies.sql`:**
```sql
INSERT INTO currency (name) VALUES
('AUD'), ('CAD'), ('EUR'), ('GBP'), ('USD'), ('SGD'), ('NOK'), ('PLN'),
('CNY'), ('IDR'), ('ZAR'), ('MXN'), ('HUF'), ('ILS'), ('DKK'), ('CZK'),
('THB'), ('SEK'), ('JPY'), ('BRL'), ('RON'), ('CHF'), ('ISK'), ('TRY'),
('HKD'), ('INR'), ('KRW'), ('MYR'), ('NZD'), ('PHP');
```

**`V4__seed_quote_providers.sql`:**
```sql
INSERT INTO quote_provider (name) VALUES
('JustETF'), ('Onvista'), ('FinanzenNet'), ('CNBC'),
('FondsDiscount'), ('ComDirect'), ('WallstreetOnline');
```

**`V5__seed_settings.sql`:**
```sql
INSERT INTO settings (key, value) VALUES ('quote.fetch.interval.minutes', '60');
```

**`V6__enforce_single_country_branch.sql`:**
```sql
ALTER TABLE isin_country ADD CONSTRAINT uq_isin_country_isin UNIQUE (isin_id);
ALTER TABLE isin_branch  ADD CONSTRAINT uq_isin_branch_isin  UNIQUE (isin_id);
```

---

## Authentication (Clerk)

### Backend

- `SecurityConfig.java`: stateless JWT filter on all `/api/**` endpoints.
- `ClerkJwtFilter.java`: fetches Clerk JWKS URI, validates `Authorization: Bearer <token>`, extracts `sub` claim.
- Swagger UI (`/swagger-ui/**`, `/v3/api-docs/**`) also protected; users authorize via the `Authorize` button.
- Currently disabled via `folio.security.enabled=false` (permits all requests); `ClerkJwtFilter` not yet implemented.

### Frontend

- Wrap app in `<ClerkProvider publishableKey={...}>`.
- `<SignIn />` / `<SignUp />` pages at `/sign-in`, `/sign-up`.
- `useAuth()` hook: attach JWT to all API requests via Axios interceptor.
- Protected routes: redirect to `/sign-in` if unauthenticated.
- `@clerk/clerk-react` not yet installed.

---

## CSV Parsing Specifications

### DeGiro `Transactions.csv`

**Format:** comma-separated; first row is header.

| Index | Column | Description |
|-------|--------|-------------|
| 0 | Datum | Trade date `DD-MM-YYYY` |
| 1 | Uhrzeit | Trade time `HH:mm` |
| 2 | Produkt | Security name → `isin_name` |
| 3 | ISIN | Security identifier |
| 6 | Anzahl | Share count (positive = buy, negative = sell) |
| 11 | Wert EUR | Total trade value in EUR (negative for buys) |

**Share price:** `abs(Wert EUR) / abs(Anzahl)`. Column 7 (`Kurs`) is **not used** — it is in local trading currency.

**Parsing logic:**
1. Skip header row.
2. Parse `Datum` + `Uhrzeit` → `LocalDateTime`.
3. Parse `Anzahl` (German decimal format). **Skip row if `Anzahl == 0`** — zero-count rows are non-trade entries (fees, dividends) and would cause division by zero.
4. Parse `Wert EUR` (German decimal format).
5. `share_price = Math.abs(eurValue) / Math.abs(count)`.
6. Upsert ISIN into `isin`.
7. Insert `Produkt` into `isin_name` if `(isin_id, name)` pair not yet present.
8. Depot = `DeGiro`. DELETE all transactions for DeGiro, then bulk insert.

### ZERO `ZERO-orders-*.csv`

**Format:** semicolon-separated; first row is header.

| Index | Column | Description |
|-------|--------|-------------|
| 0 | Name | Security name → `isin_name` |
| 1 | ISIN | Security identifier |
| 5 | Status | Filter: must equal `"ausgeführt"` |
| 12 | Richtung | `"Kauf"` = buy (positive), `"Verkauf"` = sell (negative) |
| 16 | Ausführung Datum | Execution date `DD.MM.YYYY` |
| 17 | Ausführung Zeit | Execution time |
| 18 | Ausführung Kurs | Price per share |
| 19 | Anzahl ausgeführt | Shares executed |

**Parsing logic:**
1. Skip header. Filter: `Status` (index 5) = `"ausgeführt"`.
2. Parse date (16) + time (17) → `LocalDateTime`.
3. `Richtung` (12): `"Kauf"` → positive; `"Verkauf"` → negative count.
4. Upsert ISIN (1) into `isin`. Insert `Name` (0) into `isin_name` if pair not yet present.
5. Depot = `ZERO`. DELETE all transactions for ZERO, then bulk insert.

### DeGiro `Account.csv` (dividends)

**Format:** comma-separated.

| Index | Column | Description |
|-------|--------|-------------|
| 2 | Valuta | Payment date → `timestamp` |
| 4 | ISIN | Security identifier |
| 5 | Beschreibung | Filter: must equal `"Dividende"` exactly |
| 7 | Currency | Currency code |
| 8 | Änderung | Dividend amount (decimal separator `~`) |

**Parsing logic:**
1. Skip rows where `line.length < 9`. Filter: index 5 equals `"Dividende"` exactly.
2. Parse index 2 → `LocalDateTime`. Upsert ISIN (4) → `isin`. Upsert currency (7) → `currency`.
3. Parse amount (8): replace `~` with `.`. Insert into `dividend_payment` (depot = DeGiro).

### ZERO `ZERO-kontoumsaetze-*.csv` (dividends)

**Format:** semicolon-separated.
**Columns:** `[0] Datum`, `[1] Valuta`, `[2] Betrag`, `[3] Betrag storniert`, `[4] Status`, `[5] Verwendungszweck`, `[6] IBAN`.

**Parsing logic:**
1. Filter: `Status` (4) = `"gebucht"` AND `Verwendungszweck` (5) starts with `"Coupons/Dividende"`.
2. Extract ISIN: find `"ISIN "`, take next 12 characters. Use `Valuta` (1) as payment date.
3. Parse `Betrag` (2): replace `,` with `.`. Currency = EUR (no currency column; upsert `EUR`).
4. DELETE all `dividend_payment` for ZERO, then insert.

### `dividende.csv`

**Format:** `ISIN;Name;Currency;DividendPerShare`

1. Upsert ISIN → `isin`. Insert `Name` → `isin_name` if pair not yet present.
2. Upsert currency → `currency`. Replace all rows in `dividend` table on each import.

### `branches.csv`

**Format:** `ISIN;Name;Branch`

1. Upsert ISIN → `isin`. Insert `Name` → `isin_name` if pair not yet present.
2. Upsert branch → `branch`. Replace branch mapping (1:1): DELETE existing `isin_branch` row, INSERT new.

### `countries.csv`

**Format:** `ISIN;Name;Country`

1. Upsert ISIN → `isin`. Insert `Name` → `isin_name` if pair not yet present.
2. Upsert country → `country`. Replace country mapping (1:1): DELETE existing `isin_country` row, INSERT new.

---

## Backend REST API

All endpoints under `/api/**`, secured with Clerk JWT.

### Import Endpoints (`ImportController`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/import/degiro/transactions` | Upload DeGiro Transactions.csv |
| POST | `/api/import/degiro/account` | Upload DeGiro Account.csv |
| POST | `/api/import/zero/orders` | Upload ZERO orders CSV |
| POST | `/api/import/zero/account` | Upload ZERO kontoumsaetze CSV |
| POST | `/api/import/dividends` | Upload dividende.csv |
| POST | `/api/import/branches` | Upload branches.csv |
| POST | `/api/import/countries` | Upload countries.csv |

All return `{ success: boolean, imported: int, errors: string[] }`.

### Reference Data Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/countries` | All countries, sorted alphabetically |
| GET | `/api/branches` | All branches, sorted alphabetically |
| GET | `/api/depots` | All depots, sorted alphabetically |
| GET | `/api/currencies` | All currencies, sorted alphabetically |

### Transactions Endpoint

| Method | Path | Query Params |
|--------|------|--------------|
| GET | `/api/transactions` | `fromDate`, `toDate`, `isin`, `depotId`, `page`, `size`, `sort` |

Returns paginated list with ISIN, security name (JOIN `isin_name`), depot, date, count, share price.

### Securities Endpoint

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/securities` | Current positions aggregated from transactions |

**Calculation:**
- `SUM(count)` per ISIN; keep positions where `SUM(count) > 0`.
- **Avg entry price:** `SUM(count * share_price) / SUM(count)` across **all** transactions (buys positive, sells negative — reduces cost basis proportionally).
- Join with `isin_name`, `isin_country`, `isin_branch`, `dividend`, `isin_quote`.
- Response: ISIN, name, country, branch, total shares, avg entry price, current quote (null if not fetched), performance % (`(current_quote − avg_entry_price) / avg_entry_price * 100`), expected annual dividend, estimated annual income.

### Analytics Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/analytics/countries` | Total invested per country + % of portfolio |
| GET | `/api/analytics/branches` | Total invested per branch + % of portfolio |

**Calculation:** `invested = SUM(count * share_price)` per open position; group by country/branch via `isin_country`/`isin_branch`; compute % of total.

### Quote Management Endpoints (`QuoteController`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/quotes/settings` | Current fetch interval + last fetch timestamp |
| PUT | `/api/quotes/settings/interval` | Update `quote.fetch.interval.minutes` |
| POST | `/api/quotes/fetch` | Trigger immediate fetch for all held ISINs |

`GET /api/quotes/settings` response: `{ "intervalMinutes": 60, "lastFetchAt": "2026-03-22T14:30:00" }` (`lastFetchAt` null if no fetch yet).

### Dashboard Endpoint (`DashboardController`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/dashboard` | Portfolio summary |

**Calculation:**
- **Total portfolio value:** `SUM(avg_entry_price * total_shares)` across open positions.
- **Security count:** distinct ISINs with `SUM(count) > 0`.
- **Total dividend ratio:** `SUM(shares * dividend_per_share) / total_portfolio_value * 100`.
- **Top 5 holdings:** highest `avg_entry_price * total_shares`; fields: ISIN, name, invested amount.
- **Top 5 dividend sources:** highest `shares * dividend_per_share`; fields: ISIN, name, estimated annual income.
- **Last quote fetch:** `settings` key `quote.last.fetch.timestamp`; null if not set.

Response:
```json
{
  "totalPortfolioValue": 12345.67,
  "securityCount": 23,
  "totalDividendRatio": 3.14,
  "top5Holdings": [{ "isin": "IE00B4L5Y983", "name": "iShares Core MSCI World ETF", "investedAmount": 4500.00 }],
  "top5DividendSources": [{ "isin": "DE000BASF111", "name": "BASF SE", "estimatedAnnualIncome": 150.00 }],
  "lastQuoteFetchAt": "2026-03-22T14:30:00"
}
```

---

## Quote Fetcher Scheduling & Persistence

- **Scheduling:** `@Scheduled` Spring task reads `settings.quote.fetch.interval.minutes` before each run (so UI changes take effect without restart). Runs for all ISINs where `SUM(count) > 0`. Requires `@EnableScheduling` on the application class.
- **Persistence:** After each fetch batch, upsert results into `isin_quote` (one row per ISIN; `quote_provider_id` = provider that succeeded; `fetched_at` = now). Update `settings` key `quote.last.fetch.timestamp`.
- **Disabled source:** Lemon Markets batch API — API token expired; do not implement.

---

## Frontend Pages & Routes

### Routes

| Route | Page | Description |
|-------|------|-------------|
| `/` | Dashboard | KPI cards + top-5 holdings/dividend sources + last quote fetch |
| `/transactions` | Transactions | Sortable/filterable table |
| `/securities` | Securities | Portfolio positions with live quotes and performance |
| `/countries` | Countries | Alphabetical list |
| `/branches` | Branches | Alphabetical list |
| `/depots` | Depots | Alphabetical list |
| `/currencies` | Currencies | Alphabetical list |
| `/analytics/countries` | Country Diversification | Donut chart + detail table |
| `/analytics/branches` | Branch Diversification | Donut chart + detail table |
| `/import` | Import | File upload section per data type |
| `/settings` | Settings | Quote fetch interval selector + manual trigger |

### App Layout

- `Page.Header` → `AppHeader` with app title "Folio" and logo link.
- `Page.Sidebar` → navigation links; active link highlighted; collapses to drawer below 640px (Strato default).
- `Page.Main` → routed content via `<Outlet />`.
- **Planned (not yet implemented):** dark/light mode toggle; Clerk `<UserButton />` in header; auth redirect.

### Import Page

- One card/section per import type, grouped by broker.
- Each card: description, `<input type="file" />`, upload button, status indicator (idle / loading / success with row count / error with messages).

### Analytics Pages

- Donut chart (Recharts `PieChart`) with legend.
- Detail table below: name, invested amount (EUR), percentage.
- Country and branch pages share the same layout pattern.

### Transactions Page

All rows fetched once at load; filtering entirely client-side.

**Columns:**

| Column | Format | Alignment | `width` | `minWidth` |
|--------|--------|-----------|---------|------------|
| Date | `DD-MM-YYYY`; `sortAccessor` returns ISO `YYYY-MM-DD` for chronological sort; default sort descending | left | 105 | 105 |
| ISIN | plain; custom cell with `display:flex; align-items:center; height:100%`; double-click → `setIsinFilter` | left | 140 | 140 |
| Name | plain; custom cell with `display:flex; align-items:center; height:100%`; double-click → `setNameFilter` | left | 240 | 120 |
| Depot | plain | left | 100 | 80 |
| Count | `toLocaleString('de-DE', {minimumFractionDigits:2, maximumFractionDigits:2})` | right | — | 80 |
| Share Price | `toLocaleString('de-DE', {minimumFractionDigits:2, maximumFractionDigits:2})` | right | — | 100 |

**Filter bar:** ISIN `TextInput` (real-time partial, case-insensitive; Clear button; double-click cell fills); Name `TextInput` (same); Depot `Select<string>` (`""` = "All depots" + sorted unique names); Refresh `Button`.

**Row count:** `"N of M transactions"` filtered / `"M transactions"` unfiltered.

**Pagination:** `DataTablePagination`; `defaultPageSize=10`; options `[10,20,50,100]`; "Show All / Paginate" toggle.

**Loading:** `ProgressCircle size="small"` + "Loading…".

### Securities Page

- Columns: ISIN (140/140), Name (240/240), Country (120/80), Branch (160/80), Total Shares, Avg Entry Price, Current Quote, Performance (%), Expected Dividend/Share, Est. Annual Income.
- Current Quote and Performance show `—` if no quote fetched yet.
- Filter bar: country dropdown, branch dropdown. Sortable + resizable (`resizable` prop).

### Settings Page

- Fetch interval: dropdown (15 min, 30 min, 1 h, 4 h, 12 h, 24 h) + custom input.
- "Save interval" → `PUT /api/quotes/settings/interval`.
- "Fetch now" → `POST /api/quotes/fetch`; loading spinner while running.
- Last fetch timestamp displayed below controls.

### Dashboard Page

- **KPI row:** Total Portfolio Value (EUR), Number of Securities, Total Dividend Ratio (%).
- **Top 5 Holdings:** table — ISIN, Security Name, Invested Amount (EUR).
- **Top 5 Dividend Sources:** table — ISIN, Security Name, Est. Annual Income (EUR).
- **Last Quote Fetch:** timestamp (e.g. "Last updated: 22.03.2026 14:30"); `—` if not yet fetched.
- Data from `GET /api/dashboard`.

---

## Error Handling & Logging

**Backend:**
- `GlobalExceptionHandler` (`@RestControllerAdvice`): maps exceptions to `{ error, message, timestamp }` JSON.
- CSV import: errors collected per row, returned in import response — partial success allowed.
- SLF4J + Logback: structured logging at INFO/WARN/ERROR.

**Frontend:**
- Axios interceptor: 401 → redirect to `/sign-in`; 4xx/5xx → show Strato notification/toast.
- File type validation before upload (client-side).

---

## API Documentation

- SpringDoc generates OpenAPI 3 spec at `/v3/api-docs`.
- Swagger UI at `/swagger-ui/index.html`.
- Configured with Clerk JWT bearer auth scheme (users paste token in `Authorize` dialog).
- All controllers annotated with `@Tag`, `@Operation`, `@ApiResponse`.

---

## Production Build

### Gradle + Vite Integration

`build.gradle` custom tasks:
1. `npmInstall`: exec `npm ci` in `frontend/`
2. `buildFrontend`: exec `npm run build`; depends on `npmInstall`
3. `copyFrontend`: copy `frontend/dist/` → `backend/src/main/resources/static/`; depends on `buildFrontend`
4. `bootJar` depends on `copyFrontend`

### SPA Fallback

Catch-all `ResourceHttpRequestHandler`: all non-`/api/**` routes serve `index.html` (enables client-side routing).

---

## Implementation Order

1. Project scaffolding (backend + frontend skeletons, Docker stub)
2. Database schema + Flyway migrations
3. Clerk authentication (backend JWT filter + frontend auth flow)
4. CSV parsers (DeGiro transactions → ZERO orders → reference files)
5. REST API (import endpoints first, then query/analytics)
6. Frontend pages (import page first to load data, then transactions, securities, analytics)
7. Error handling & logging polish
8. Swagger/OpenAPI annotations
9. Docker build + end-to-end production test
