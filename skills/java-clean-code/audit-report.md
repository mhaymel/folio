# Audit Report: java-clean-code rule examples

## Summary

~200 Good blocks audited across 25 rule files, **19 failing**. **3 Bad blocks** flagged as muddy.

The dominant failure modes are:

1. **Primitive obsession in records and fields** (R-016i / R-002s): `long id`, `int quantity`, `String name`, `BigDecimal amount` appear as domain-concept fields without tiny-type wrappers in several Good blocks.
2. **Logger mis-use in non-logging rules**: examples outside `R-024` frequently write `log.info("x: " + value)` — lowercase `log`, string concatenation — violating R-024a/b.
3. **Single-letter catch variables**: `catch (IOException e)` appears in R-024's own Good blocks even though R-018d/e and the rest of the skill use `exception`.

## Good-block violations

| File | Sub-rule illustrated | Violates | Note |
|------|---------------------|----------|------|
| R-002-class-design.md | R-002o (parameter-object for many args) | R-016i / R-002s | `record OrderRequest(Product product, int quantity, Money price)` — `int quantity` is a domain concept; should be a `Quantity` tiny type. R-016b's own Good block introduces `Amount(BigDecimal value)`, so the same treatment is expected for quantity. |
| R-007-record-design.md | R-007a (prefer records to immutable classes) | R-016i | `record UserSummary(long id, Name name)` — `long id` is a domain concept (`UserId`). R-007i's "Good (with tiny types)" block shows the expected `UserId(long value)` wrapper. |
| R-007-record-design.md | R-007b (records package-private by default) | R-016i | Same `long id` issue carried over from R-007a. |
| R-007-record-design.md | R-007d (record ≤3 fields) | R-016i, R-006b | `record UserService(int userId, UserCredentials credentials, Email email)` — `int userId` raw primitive for a domain concept; and the record name `UserService` is a service-sounding name, not a data-model noun. |
| R-007-record-design.md | R-007e (no builders) | R-016i | `record UserSummary(long id, Name name)` — same `long id` issue. |
| R-007-record-design.md | R-007i (no boxed primitives) | R-016i | First Good block `record UserSummary(long id, int age, boolean isActive)` leaves `id` and `age` as raw primitives for domain concepts. The "Good (with tiny types)" block that follows shows the stricter form — the first Good is arguably weak rather than compliant. |
| R-014-method-body.md | R-014g (no unreachable code) | R-024a, R-024b, R-016i | `log.info("charged: " + amount);` uses lowercase `log` (must be `LOG`), string concatenation in a log call (must use `{}`), and `BigDecimal charge(Order order)` returns a raw `BigDecimal` for money (should be `Money`). |
| R-018-local-variable.md | R-018h "try block exception" | R-024a, R-024b | `log.error("request failed with response: " + response, exception)` — lowercase `log` and string concatenation in a log call. |
| R-018-local-variable.md | R-018l "Or, when the variable is unnecessary" | R-015c | `@Override public void process(Order order) { doSomething(); }` — `order` is an unused parameter, which R-015c forbids outright. The illustrative conflict with R-018l should be called out (see Rule-text issues below). |
| R-018-local-variable.md | R-018m (declare locals near first use) | R-015e, R-016i, R-018k / R-999d | `sendInvoice(total, currency, hasDiscount)` is a 3-parameter method call (R-015e forbids methods with >1 parameter); `String currency` carries primitive obsession (R-016i); `BigDecimal.valueOf(100)` is a magic literal (R-018k / R-999d). |
| R-019-comment.md | R-019d (framework-requirement comment) | R-015f / R-016i | `void setName(String name)` takes a raw `String` for a domain concept. The earlier R-014j framework-exception example already uses `Name name` — R-019d should match. |
| R-020-exception-handling.md | R-020g "Good (log and recover)" | R-024a | `log.error("failed to send notification to {}", user.email(), exception)` — lowercase `log` where R-024a requires `LOG`. |
| R-021-concurrency.md | R-021a (prefer immutability over locks) | R-002j, R-002i | `PriceCache(PriceSnapshot initial) { this.snapshot = new AtomicReference<>(requireNonNull(initial)); }` does transformation in the constructor (R-002j forbids code beyond precondition checks) and exposes the primary constructor even though `AtomicReference` is internal state (R-002i requires a private primary with a secondary wrapper — the pattern used correctly in the R-021b FetchTracker Good block and in R-021i JobCoordinator). |
| R-023-stream-lambdas.md | R-023c (streams must be pure) | R-024a, R-024b | `log.info("processing " + order)` — lowercase `log` and string concatenation in a log call. |
| R-024-logging.md | R-024a (LOG field, SLF4J) | R-018d, R-018e | `catch (SubmitException e)` uses the single-letter abbreviation `e`; R-018d forbids single-letter locals and the rest of the skill uses `exception`. Notably this appears in the R-024 file itself. |
| R-024-logging.md | R-024c (pass Throwable last) | R-018d, R-018e | Same `catch (IOException e)` single-letter catch variable. |
| R-024-logging.md | R-024e (do not log-and-throw) | R-018d, R-018e | Same `catch (PersistenceException e)` single-letter catch variable. |
| R-999-not-categorized.md | R-999e (pronounceable names) | R-016i, R-007g | `DataRecord` has `private final String quantity` (raw primitive for a domain concept — R-016i) and two `Date` fields (mutable type — R-007g applies to records, and R-002e's immutability preference discourages `Date` here too; prefer `Instant`). |
| R-999-not-categorized.md | R-999f (searchable names) | R-018l | Inside the `for` loop `int orderTotal = orders.get(i).total();` is assigned but never read — R-018l forbids unused locals. |

## Bad-block incidental issues

| File | Sub-rule illustrated | Incidental issue | Note |
|------|---------------------|----------|------|
| R-014-method-body.md | R-014g Bad | R-024a, R-024b | The Bad block introduces additional issues (lowercase `log`, string concat) that are unrelated to "dead code after return". A reader may confuse which defect the example is teaching. |
| R-018-local-variable.md | R-018h Bad ("try block exception") | R-024a, R-024b | Same logger issues as the Good block — the Bad example also writes `log.error("..." + response, exception)`. The lesson is about variable declaration location, but the log statement muddies it. |
| R-023-stream-lambdas.md | R-023c Bad | R-024a, R-024b | Bad block has `log.info("processing " + order)` and `log.debug("seeing: " + order)` in R-023f — the logging mistakes are incidental to "streams must be pure" and "no peek". |

## Clean files

- R-001-class-naming.md — all blocks ok
- R-003-class-field.md — all blocks ok (Good blocks elide constructors, consistent with the file's note that classes are illustrative of field-scoped rules)
- R-004-interface-naming.md — all blocks ok
- R-005-interface-design.md — all blocks ok
- R-006-record-naming.md — all blocks ok
- R-008-enum.md — all blocks ok
- R-009-imports.md — all blocks ok
- R-010-programming-by-contract.md — all blocks ok
- R-011-method-naming.md — all blocks ok
- R-012-method-code.md — all blocks ok
- R-013-method-visibility.md — all blocks ok
- R-015-method-parameter.md — all blocks ok
- R-016-tiny-type.md — all blocks ok
- R-017-package-and-file-naming.md — all blocks ok (naming-only, no code)
- R-022-unit-test.md — all blocks ok
- R-999-not-categorized.md — R-999c, R-999d, R-999g clean; R-999e and R-999f flagged above

## Rule-text issues

- **R-018l vs. R-015c conflict.** R-018l's "Or, when the variable is unnecessary" Good block keeps an unused method parameter (`public void process(Order order)` where `order` is intentionally unused) with a comment explaining intent. R-015c says a method must not have an unused parameter — no exception carved out. The two rules give conflicting guidance for the same snippet; R-018l should either point out the `@Override`-under-an-interface case where the parameter is imposed by the supertype, or R-015c should note the override exception. As-written, the Good block of R-018l violates R-015c.

- **Money / Amount primitive-obsession pattern.** Several Good blocks across files (R-002o, R-002s, R-006b, R-012a, R-014a, R-015b) use `record Money(BigDecimal amount, Currency currency)` with a raw `BigDecimal` amount. R-016b's Good block defines `record Amount(BigDecimal value)` and composes `Money(Amount amount, Currency currency)`. The two patterns are mutually inconsistent: either `BigDecimal amount` inside `Money` is acceptable (because `Money` is itself the domain tiny type) and R-016b's example is over-engineered, or R-016i applies recursively and the other files need to be updated. The rule-set should pick one.

- **R-005d vs. R-005c.** R-005d's Good block defines a `UserRepository` interface with five methods covering save, export, and scheduling — stretching R-005b/R-005c's single-responsibility and small-cohesive guidance. The example teaches "use interface types in signatures" well, but at the cost of violating the sibling rules.
