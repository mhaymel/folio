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
- The system shall import transaction data from **DeGiro** broker CSV exports (see sample: [`docs/samples/Transactions.csv`](docs/samples/Transactions.csv)). use euro values.
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
similar to the import of `Transactions.csv`, the UI should provide a file upload interface for the user to upload the `ZERO-orders-*.csv` file exported from Trade Republic. The system should parse the CSV file, extract transaction data, and store it in the database according to the defined data model. The system should handle any parsing errors gracefully and provide feedback to the user about the success or failure of the import process. The system should also ensure that ISINs are normalized and mapped correctly to the `isin` table, and that transactions are associated with the correct depot (Trade Republic). before the data will be written to the table transactions all entries for the depot Trade Republic should be deleted to avoid duplicates and to reflect any changes in the transaction history.

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
The UI should provide a view that displays all transactions in a tabular format, showing relevant details such as date, ISIN, depot, count, share price, name etc. The transactions should be fetched from the backend via a REST API endpoint that retrieves transaction data from the database. The UI should allow the user to filter and sort transactions based on different criteria (e.g. date range, ISIN, depot). The UI should also provide a way to refresh the transaction data to reflect any newly imported transactions.

#### show countries
the UI should provide a view that displays the countries. sorted alphabetically. The countries should be fetched from the backend via a REST API endpoint that retrieves country data from the database.

#### import countries.csv
the UI should provide a file upload interface for the user to upload the `countries.csv` file, which contains the mapping of ISINs to their respective countries. The system should parse the CSV file, extract the country mapping data, and store it in the database according to the defined data model. The system should handle any parsing errors gracefully and provide feedback to the user about the success or failure of the import process. The system should also ensure that ISINs are normalized and mapped correctly to the `isin` table, and that the country mapping is associated with the correct ISINs in the `isin_country` table. If an isin already has a country mapping, it should be updated with the new value from the CSV file. if an isin does not have a country mapping yet, a new entry should be created in the `isin_country` table. if an isin from the CSV file does not exist in the `isin` table, it should be added to the `isin` table and then mapped to the country in the `isin_country` table.

#### show branches
the UI should provide a view that displays the branches. sorted alphabetically. The branches should be fetched from the backend via a REST API endpoint that retrieves branch data from the database.

#### import branches.csv
the UI should provide a file upload interface for the user to upload the `branches.csv` file, which contains the mapping of ISINs to their respective industry branches. The system should parse the CSV file, extract the branch mapping data, and store it in the database according to the defined data model. The system should handle any parsing errors gracefully and provide feedback to the user about the success or failure of the import process. The system should also ensure that ISINs are normalized and mapped correctly to the `isin` table, and that the branch mapping is associated with the correct ISINs in the `isin_branch` table. If an isin already has a branch mapping, it should be updated with the new value from the CSV file. if an isin does not have a branch mapping yet, a new entry should be created in the `isin_branch` table. if an isin from the CSV file does not exist in the `isin` table, it should be added to the `isin` table and then mapped to the branch in the `isin_branch` table. 

#### country diversification breakdown
the UI should provide a view that shows the country diversification breakdown, which is calculated by the backend based on the transactions and the country mapping of ISINs. The backend should provide a REST API endpoint that calculates and returns the country diversification breakdown, which includes the total invested amount per country and the percentage of the total portfolio invested in each country. The UI should display this information in a clear and visually appealing way, such as a pie chart or a bar chart or a donutchart.

#### branch diversification breakdown
similar to the country diversification breakdown, the UI should provide a view that shows the branch diversification breakdown, which is calculated by the backend based on the transactions and the branch mapping of ISINs. The backend should provide a REST API endpoint that calculates and returns the branch diversification breakdown, which includes the total invested amount per branch and the percentage of the total portfolio invested in each branch. The UI should display this information in a clear and visually appealing way, such as a pie chart or a bar chart or a donutchart.

#### show securities
the UI should provide a view that displays all securities (ISINs) that are currently held in the portfolio, along with relevant details such as ticker symbol, name, country, branch, count, current quote, entry price, performance etc. The securities should be fetched from the backend via a REST API endpoint that retrieves security data from the database and calculates the current quote and performance based on the transactions and the current market prices. The UI should allow the user to filter and sort securities based on different criteria (e.g. country, branch, performance). The UI should also provide a way to refresh the security data to reflect any changes in the transactions or market prices.

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
- react frontend with typescript
- frontend should be a single page application that consumes the backend rest api
- frontend should be responsive and work well on both desktop and mobile devices
- frontend should have a clean and modern design, with a focus on usability and accessibility
- Dynatrace Strato design system should be used as design library for the frontend (https://developer.dynatrace.com/design/about-strato-design-system/)
- frontend should have a dark mode and a light mode, with the ability to switch between them


### build tools
- gradle for the backend
- vite for the frontend
- result of the production build should be a single docker image containing both frontend and backend that can be run on linux and windows