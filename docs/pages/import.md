# Import

> Route: `/import` — File upload section per data type.

## Use Cases

### Import DeGiro Transactions.csv
UI should provide a file upload interface for the user to upload the `Transactions.csv` file exported from DeGiro. The system should parse the CSV file, extract transaction data, and store it in the database according to the defined data model. The system should handle any parsing errors gracefully and provide feedback to the user about the success or failure of the import process. The system should also ensure that ISINs are normalized and mapped correctly to the `isin` table, and that transactions are associated with the correct depot (DeGiro). Before the data will be written to the table transactions all entries for the depot DeGiro should be deleted to avoid duplicates and to reflect any changes in the transaction history.

### Import ZERO-orders.csv
Similar to the import of `Transactions.csv`, the UI should provide a file upload interface for the user to upload the `ZERO-orders-*.csv` file exported from ZERO. The system should parse the CSV file, extract transaction data, and store it in the database according to the defined data model. The system should handle any parsing errors gracefully and provide feedback to the user about the success or failure of the import process. The system should also ensure that ISINs are normalized and mapped correctly to the `isin` table, and that transactions are associated with the correct depot (ZERO). Before the data will be written to the table transactions all entries for the depot ZERO should be deleted to avoid duplicates and to reflect any changes in the transaction history.

### Import DeGiro Account.csv
The UI should provide a file upload interface for the user to upload the `Account.csv` file exported from DeGiro. The system should parse the CSV file and extract dividend payment records. Only rows where the description field equals `"Dividende"` are processed. Each valid row is stored as a `dividend_payment` entry associated with the DeGiro depot, using the valuta date as the payment timestamp and recording the currency. The system should handle parsing errors gracefully and provide feedback about the success or failure of the import. Before writing new records, all existing `dividend_payment` entries for the DeGiro depot should be deleted to avoid duplicates.

### Import ZERO-kontoumsaetze.csv
The UI should provide a file upload interface for the user to upload the `ZERO-kontoumsaetze-*.csv`. The data should be parsed and dividend payment records should be extracted and written to table `dividend_payment`. As depot the value for ZERO should be used. Before the records are stored to the table, all existing `dividend_payment` entries for the ZERO depot should be deleted to avoid duplicates and to reflect any changes in the transaction history.

### Import dividende.csv
The UI should provide a file upload interface for the user to upload the `dividende.csv` file, which contains the expected annual dividend per share for each ISIN. The system should parse the CSV file, extract the dividend data, and store it in the database according to the defined data model. The system should handle any parsing errors gracefully and provide feedback to the user about the success or failure of the import process. The system should also ensure that ISINs are normalized and mapped correctly to the `isin` table, and that dividend data is associated with the correct ISINs in the `dividend` table. If an ISIN from the CSV file does not exist in the `isin` table, it should be added to the `isin` table before storing the dividend data.

### Import countries.csv
The UI should provide a file upload interface for the user to upload the `countries.csv` file, which contains the mapping of ISINs to their respective countries. The system should parse the CSV file, extract the country mapping data, and store it in the database according to the defined data model. The system should handle any parsing errors gracefully and provide feedback to the user about the success or failure of the import process. The system should also ensure that ISINs are normalized and mapped correctly to the `isin` table, and that the country mapping is associated with the correct ISINs in the `isin_country` table. If an ISIN already has a country mapping, it should be updated with the new value from the CSV file. If an ISIN does not have a country mapping yet, a new entry should be created in the `isin_country` table. If an ISIN from the CSV file does not exist in the `isin` table, it should be added to the `isin` table and then mapped to the country in the `isin_country` table.

### Import branches.csv
The UI should provide a file upload interface for the user to upload the `branches.csv` file, which contains the mapping of ISINs to their respective industry branches. The system should parse the CSV file, extract the branch mapping data, and store it in the database according to the defined data model. The system should handle any parsing errors gracefully and provide feedback to the user about the success or failure of the import process. The system should also ensure that ISINs are normalized and mapped correctly to the `isin` table, and that the branch mapping is associated with the correct ISINs in the `isin_branch` table. If an ISIN already has a branch mapping, it should be updated with the new value from the CSV file. If an ISIN does not have a branch mapping yet, a new entry should be created in the `isin_branch` table. If an ISIN from the CSV file does not exist in the `isin` table, it should be added to the `isin` table and then mapped to the branch in the `isin_branch` table.

---

## REST API

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

## UI Specification

- One card/section per import type, grouped by broker.
- Each card: description, `<input type="file" />`, upload button, status indicator (idle / loading / success with row count / error with messages).

