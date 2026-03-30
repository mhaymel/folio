# Data Model

The system shall have a well-defined data model that represents the entities and relationships in the application, such as users, transactions, positions, quotes, branches, countries, etc.

The data model shall be designed to support the required functionality and performance of the application, with proper normalization and indexing for efficient querying and data integrity.

---

## `ticker_symbol`
Maps a ticker symbol to a stock.

| Column  | Type        | Constraints              | Description                        |
|---------|-------------|---------------------------|------------------------------------|
| id      | INTEGER     | PK                       | Surrogate primary key              |
| isin_id | INTEGER     | FK → isin.id, UNIQUE     | Reference to the stock          |
| symbol  | VARCHAR(20) | UNIQUE, NOT NULL         | Ticker symbol (e.g. AAPL, BASF.DE) |

## `currency`
Reference table of all currencies.

| Column | Type        | Constraints      | Description                    |
|--------|-------------|------------------|--------------------------------|
| id     | INTEGER     | PK               | Surrogate primary key          |
| name   | VARCHAR(3)  | UNIQUE, NOT NULL | Currency code (e.g. EUR, USD)  |

## `isin`
Central registry of all known stocks by ISIN.

| Column | Type         | Constraints | Description             |
|--------|--------------|-------------|-------------------------|
| id     | INTEGER      | PK          | Surrogate primary key   |
| isin   | VARCHAR(12)  | UNIQUE, NOT NULL | Stock identifier (e.g. DE000BASF111) |

## `dividend`
Manually maintained expected annual dividend per ISIN. Sourced from [`docs/samples/dividende.csv`](samples/dividende.csv).

| Column             | Type         | Constraints      | Description                          |
|--------------------|--------------|------------------|--------------------------------------|
| id                 | INTEGER      | PK               | Surrogate primary key                |
| isin_id            | INTEGER      | FK → isin.id     | Reference to the stock            |
| currency_id        | INTEGER      | FK → currency.id | Reference to the currency            |
| dividend_per_share | DOUBLE       | NOT NULL         | Expected annual dividend per share   |

## `depot`
Represents a broker account/depot where stocks are held.

| Column | Type         | Constraints      | Description           |
|--------|--------------|------------------|-----------------------|
| id     | INTEGER      | PK               | Surrogate primary key |
| name   | VARCHAR(100) | UNIQUE, NOT NULL | Depot name (e.g. DeGiro, ZERO) |

## `transaction`
Records a buy or sell transaction for a stock.

| Column      | Type      | Constraints      | Description                        |
|-------------|-----------|------------------|------------------------------------|
| id          | INTEGER   | PK               | Surrogate primary key              |
| date        | TIMESTAMP | NOT NULL         | Date and time of the transaction   |
| isin_id     | INTEGER   | FK → isin.id     | Reference to the stock          |
| depot_id    | INTEGER   | FK → depot.id    | Reference to the depot             |
| count       | DOUBLE    | NOT NULL         | Number of shares bought or sold    |
| share_price | DOUBLE    | NOT NULL         | Price per share at time of transaction |

## `country`
Reference table of all countries. Sourced from [`docs/samples/countries.csv`](samples/countries.csv).

| Column | Type         | Constraints   | Description        |
|--------|--------------|---------------|--------------------|
| id     | INTEGER      | PK            | Surrogate primary key |
| name   | VARCHAR(100) | UNIQUE, NOT NULL | Country name (e.g. BRD, USA, Irland) |

## `branch`
Reference table of all industry branches. Sourced from [`docs/samples/branches.csv`](samples/branches.csv).

| Column | Type         | Constraints      | Description                        |
|--------|--------------|------------------|------------------------------------|
| id     | INTEGER      | PK               | Surrogate primary key              |
| name   | VARCHAR(100) | UNIQUE, NOT NULL | Branch name (e.g. Technology, Energy) |

## `isin_country`
Maps an ISIN to its country of origin. Sourced from [`docs/samples/countries.csv`](samples/countries.csv).

| Column     | Type    | Constraints              | Description          |
|------------|---------|--------------------------|----------------------|
| isin_id    | INTEGER | PK, FK → isin.id         | Reference to ISIN    |
| country_id | INTEGER | PK, FK → country.id      | Reference to country |

## `isin_name`
Maps an ISIN to a human-readable stock name. An ISIN can have multiple names (e.g. different names used across brokers or import files). Names are only added, never overwritten. A composite unique constraint on `(isin_id, name)` prevents exact duplicate entries.

| Column  | Type         | Constraints      | Description                        |
|---------|--------------|------------------|------------------------------------|
| id      | INTEGER      | PK                       | Surrogate primary key             |
| isin_id | INTEGER      | FK → isin.id             | Reference to the stock         |
| name    | VARCHAR(255) | NOT NULL                 | Stock name (e.g. "Apple Inc.") |
|         |              | UNIQUE (isin_id, name)   | Prevents duplicate (ISIN, name) pairs |

## `isin_ticker`
Maps an ISIN to its ticker symbol(s).

| Column           | Type    | Constraints                  | Description               |
|------------------|---------|------------------------------|---------------------------|
| isin_id          | INTEGER | PK, FK → isin.id             | Reference to ISIN         |
| ticker_symbol_id | INTEGER | PK, FK → ticker_symbol.id    | Reference to ticker symbol |

## `isin_branch`
Maps an ISIN to its industry branch. Sourced from [`docs/samples/branches.csv`](samples/branches.csv).

| Column    | Type    | Constraints          | Description         |
|-----------|---------|----------------------|---------------------|
| isin_id   | INTEGER | PK, FK → isin.id     | Reference to ISIN   |
| branch_id | INTEGER | PK, FK → branch.id   | Reference to branch |

## `quote_provider`
Reference table of all quote data sources used in the cascading fallback.

| Column | Type         | Constraints      | Description                              |
|--------|--------------|------------------|------------------------------------------|
| id     | INTEGER      | PK               | Surrogate primary key                    |
| name   | VARCHAR(100) | UNIQUE, NOT NULL | Provider name (e.g. `JustETF`, `Onvista`, `MarketBeat`) |

## `settings`
Key-value store for application configuration (e.g. quote fetch interval).

| Column | Type         | Constraints      | Description                        |
|--------|--------------|------------------|------------------------------------|
| id     | INTEGER      | PK               | Surrogate primary key              |
| key    | VARCHAR(100) | UNIQUE, NOT NULL | Setting name (e.g. `quote.fetch.interval`) |
| value  | VARCHAR(500) | NOT NULL         | Setting value                      |

## `dividend_payment`
Records an actual dividend payment received for a stock in a specific depot.

| Column      | Type             | Constraints      | Description                              |
|-------------|------------------|------------------|------------------------------------------|
| id          | INTEGER          | PK               | Surrogate primary key                    |
| timestamp   | TIMESTAMP        | NOT NULL         | Date and time the dividend was received  |
| isin_id     | INTEGER          | FK → isin.id     | Reference to the stock                |
| depot_id    | INTEGER          | FK → depot.id    | Reference to the depot                   |
| currency_id | INTEGER          | FK → currency.id | Reference to the currency                |
| value       | DOUBLE PRECISION | NOT NULL         | Dividend amount received                 |

## `isin_quote`
Stores the latest fetched quote for a stock. One row per ISIN (upserted on each fetch).

| Column            | Type             | Constraints              | Description                              |
|-------------------|------------------|--------------------------|------------------------------------------|
| id                | INTEGER          | PK                       | Surrogate primary key                    |
| isin_id           | INTEGER          | FK → isin.id, UNIQUE     | Reference to the stock                |
| quote_provider_id | INTEGER          | FK → quote_provider.id   | Provider that successfully fetched the quote |
| value             | DOUBLE PRECISION | NOT NULL                 | Quote price in EUR                       |
| fetched_at        | TIMESTAMP        | NOT NULL                 | When the quote was fetched               |

