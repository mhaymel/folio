# Java Clean Code Rule Examples — Audit Report

## Summary

~180 Good blocks audited across 25 rule files, 5 failing. 1 Bad block flagged as incidentally muddy.

## Good-block violations

| File | Sub-rule illustrated | Violates | Note |
|------|---------------------|----------|------|
| R-014-method-body.md | R-014k (Portfolio getter purity) | R-007a | `final class Portfolio` has one `final List<Position>` field and only the getter `positions()` — exactly the pattern R-007a says must be a record. R-014 has no `Note on examples` exempting class-as-record, unlike R-002, R-003, R-011. Should be `record Portfolio(List<Position> positions) { Portfolio { positions = List.copyOf(requireNonNull(positions)); } }`. |
| R-014-method-body.md | R-014o Good (eager) (QuoteClient) | R-007a | `final class QuoteClient` with one `final HttpClient` field, a constructor, and a getter `httpClient()`. Pure data carrier — should be `record QuoteClient(HttpClient httpClient)`. Same missing `Note on examples` issue as R-014k. |
| R-021-concurrency.md | R-021j Good (instance-level lazy value) (QuoteClient) | R-014o | The block is structurally identical to R-014o's "Bad (lazy without evidence)" block — wraps `HttpClient` in `AtomicReference` and resolves on first call, but lacks the comment-with-measurement that R-014o requires. Either add the same evidence comment used in R-014o's "Good (lazy, with recorded evidence)" block, or replace this Good with the R-014o "Good (eager)" pattern. |
| R-013-method-visibility.md | R-013c Good (private static helper) (TaxCalculator.applyRate) | R-015f | `private static BigDecimal applyRate(BigDecimal amount)` takes a raw `BigDecimal` for what is conceptually a money amount. R-015f forbids primitive obsession in single parameters representing a domain concept. Borderline — the helper exists to manipulate `Money`'s inner `amount`, so introducing a tiny type would be awkward; but as written the example shows the forbidden pattern. |
| R-999-not-categorized.md | R-999e Good (DataRecord) | R-007a | `final class DataRecord` has three `final` fields (`Instant generationTimestamp`, `Instant modificationTimestamp`, `Quantity quantity`) and no methods — a textbook record candidate. Should be `record DataRecord(Instant generationTimestamp, Instant modificationTimestamp, Quantity quantity) { … }`. |

## Bad-block incidental issues

| File | Sub-rule illustrated | Incidental issue | Note |
|------|---------------------|------------------|------|
| R-021-concurrency.md | R-021j Bad (ExchangeRates DCL singleton) | R-013c, R-003l | The Bad block also contains a non-private static method (`static ExchangeRates get()`) and a non-final static field (`private static ExchangeRates instance`). These are intrinsic to the singleton-DCL pattern the rule is targeting, but a reader may walk away thinking R-021j is about static singletons rather than DCL. Consider an instance-scoped DCL example (like R-014o's lazy variant) instead, so the only thing that's wrong is the locking pattern. |

## Rule-text issues

- **R-018l Good (second example)** — the `// when the variable is unnecessary` block illustrates an unused **parameter** (`process(Order order)` with `// order intentionally unused`), not an unused local variable. R-018l's scope is local variables; the relevant rule for unused parameters is R-015c. Either move this example to R-015c or replace it with an unused-local-variable case.
- **R-007a Good (UserSummary)** uses `record UserSummary(UserId id, Name name)`. Per R-003p, a field of a domain type should be either lowercased-first-letter (`userId`) or a meaningful role name. `id` is conventional for an entity's own identifier, but the sub-rule does not explicitly call out this exception, which makes it ambiguous whether `UserId id` complies. Consider an explicit clarification in R-003p (or rename the component to `userId`).

## Clean files

- R-001-class-naming.md — all blocks ok
- R-002-class-design.md — all blocks ok
- R-003-class-field.md — all blocks ok
- R-004-interface-naming.md — all blocks ok
- R-005-interface-design.md — all blocks ok
- R-006-record-naming.md — all blocks ok
- R-007-record-design.md — all blocks ok
- R-008-enum.md — all blocks ok
- R-009-imports.md — all blocks ok
- R-010-programming-by-contract.md — all blocks ok
- R-011-method-naming.md — all blocks ok
- R-012-method-code.md — all blocks ok
- R-015-method-parameter.md — all blocks ok
- R-016-tiny-type.md — all blocks ok
- R-017-package-and-file-naming.md — no Java code blocks (file-naming examples only)
- R-018-local-variable.md — all blocks ok (rule-text issue noted above)
- R-019-comment.md — all blocks ok
- R-020-exception-handling.md — all blocks ok
- R-022-unit-test.md — all blocks ok
- R-023-stream-lambdas.md — all blocks ok
- R-024-logging.md — all blocks ok
