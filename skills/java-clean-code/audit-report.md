# java-clean-code Audit Report

## Summary

~190 Good blocks audited across 21 rule files. 12 Good blocks have cross-rule violations worth flagging. 0 Bad blocks flagged as muddy.

## Good-block violations

| File | Sub-rule illustrated | Violates | Note |
|------|---------------------|----------|------|
| R-007-record-design.md | R-007d (UserCredentials / UserService) | R-002s, R-013r, R-014i | `UserCredentials(String userName, String password)` uses raw `String` for domain concepts. Should wrap in `Username` / `Password` tiny types. |
| R-009-imports.md | R-009a (UserService) | R-002s, R-013r, R-014i | `List<String> names` is primitive obsession — should be `List<Name>` since `Name` is the domain concept being held. |
| R-011-method-naming.md | R-011b (OrderService) | R-013r | `String findUsername()` returns raw `String`; a `Username` tiny type would match the convention used in R-011f and elsewhere. |
| R-012-method-code.md | R-012a (PaymentService.refund) | R-013j | `refund(Invoice invoice)` never uses `invoice` — throws immediately. Unused parameter should be removed, or the method should actually use it. |
| R-016-local-variable.md | R-016m (InvoiceService) | R-013q, R-013r | `sendInvoice(total, currency, hasDiscount)` passes three parameters (violates R-013q one-parameter rule) and the locals are raw `BigDecimal`/`String` (R-013r). |
| R-019-concurrency.md | R-019a (PriceCache) | R-002f, R-002i, R-002j | Primary constructor takes `PriceSnapshot initial` but the sole field is `AtomicReference<PriceSnapshot> snapshot` — parameter type ≠ field type violates R-002f's one-param-per-field. The `new AtomicReference<>(...)` call is construction code beyond a precondition check (R-002j). Should follow the R-002i pattern used in R-019b/c/d/j: private primary ctor taking the `AtomicReference`, public no-arg ctor that delegates with `this(new AtomicReference<>(requireNonNull(initial)))`. |
| R-019-concurrency.md | R-019g (QuoteFetcher, both Good variants) | R-019h | Both Good blocks use `Thread.sleep(timeout.toMillis())`. R-019h forbids `Thread.sleep` outside tests and event-driven back-off. The example is teaching interrupt handling, so the call site is a bit baked in — but readers will learn that `Thread.sleep` is acceptable in production service code. Consider switching the example to a `BlockingQueue.poll(timeout, ...)` or `Future.get(timeout, ...)` call that also throws `InterruptedException`. |
| R-019-concurrency.md | R-019j (QuoteClient) | R-013u | `httpClient()` is declared as a getter but mutates state via `updateAndGet` (installs an `HttpClient` on first call) and `HttpClient.newHttpClient()` is an expensive allocation. R-013u forbids both mutation and expensive work in getters. The rule itself (lazy init via `AtomicReference`) is sound, but calling the method `httpClient()` frames it as a getter. Renaming to `getOrCreateHttpClient()` / `resolveHttpClient()` would signal the lazy-init contract and remove the cross-rule conflict. |
| R-999-not-categorized.md | R-999f (OrderService.process) | R-016l | `int orderTotal = orders.get(i).total();` is assigned but never read — unused local. R-016l forbids unused local variables; the example is meant to demonstrate searchable names, not dead code, but currently illustrates both. |

### Cross-rule issue: R-007a and the many final-class examples

Many Good blocks in R-002 and R-003 (and a few in R-010, R-011, R-013) show a `final class` whose every non-static field is `final` and which has no methods beyond a constructor / getter. Taken on its own, each of those Good blocks violates R-007a ("Prefer records to immutable classes").

Examples: R-002e, R-002f, R-002h, R-002j, R-002m, R-002r, R-003a, R-003b, R-003e, R-003f, R-003g, R-003h, R-003i, R-003j, R-003k, R-011f, R-013u, R-999e.

This is a structural tension, not a fixable per-example bug: R-002/R-003 teach rules that are scoped to classes, so showing the immutable version as a record would defeat the lesson. A reasonable mitigation would be a one-sentence note at the top of R-002 / R-003 explaining that the examples deliberately use classes to illustrate class-level rules, and that in practice such shapes should be records per R-007a.

## Bad-block incidental issues

No Bad blocks flagged. Each Bad block isolates the rule it illustrates without incidentally teaching other anti-patterns in confusing ways.

## Rule-text issues

| File | Sub-rule | Issue |
|------|----------|-------|
| R-013-method-design.md | R-013y | "Treat every the parameter as effectively final" — stray word ("the"). Minor typo. |
| R-013-method-design.md | R-013y | "Method and constructor parameter must not be reassigned" — should be "parameters" (plural) to match the rest of the file. |
| R-013-method-design.md | R-013r | "method parameter" (singular) used twice where the rule covers all parameters; consistency suggests "method parameters". |
| R-019-concurrency.md | R-019g | The rule forbids swallowing `InterruptedException`, but its Good examples use `Thread.sleep` which R-019h forbids outside tests. Consider switching the illustrative blocking call to one that doesn't conflict with R-019h (e.g. `BlockingQueue.poll(timeout, ...)` or `Future.get(timeout, ...)`). |
| R-019-concurrency.md | R-019j | The rule's Good example demonstrates lazy initialization via `updateAndGet`, but the method is named `httpClient()` — a getter name. R-013u forbids mutation and expensive work in getters. Either rename to signal lazy init (e.g. `resolveHttpClient()`) or add a note that R-013u does not apply to lazy-init methods that reach a stable fixed point after the first call. |

## Clean files

- R-001-class-naming.md — all Good blocks ok
- R-004-interface-naming.md — all Good blocks ok
- R-005-interface-design.md — all Good blocks ok
- R-006-record-naming.md — all Good blocks ok
- R-008-enum.md — all Good blocks ok
- R-010-programming-by-contract.md — all Good blocks ok
- R-014-tiny-type.md — all Good blocks ok
- R-015-package-and-file-naming.md — no Java code blocks; package examples ok
- R-017-comment.md — all Good blocks ok
- R-018-exception-handling.md — all Good blocks ok
- R-020-unit-test.md — all Good blocks ok
