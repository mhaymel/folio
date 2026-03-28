# Gaps & Issues

Open gaps and issues identified between the project specification and the current implementation.

---

### V. Clerk authentication not yet implemented

Clerk is planned for user management and authentication but zero code exists. `SecurityConfig.java` uses a `folio.security.enabled` flag (defaulting to `false`) that permits all requests. No `ClerkJwtFilter`, no `nimbus-jose-jwt` dependency, no `@clerk/clerk-react` in the frontend. No protected routes, no `<ClerkProvider>`, no auth hooks.

### X. `parser/` package not yet extracted

A `parser/` directory exists at `com.folio.parser` but is empty. All CSV parsing (8 import methods for DeGiro transactions, ZERO orders, DeGiro/ZERO account statements, dividends, branches, countries, ticker symbols) lives directly in `ImportService` alongside helper methods (`parseGermanDouble`, `parseCsvLine`, `upsertIsin`, etc.). No broker-specific parser classes have been extracted.