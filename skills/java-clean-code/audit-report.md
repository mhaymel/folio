# java-clean-code audit report

## Summary

All issues from the previous audit have been resolved. ~140 Good blocks across 20 rule files now comply with every sub-rule they illustrate.

## Resolved Good-block violations

| File | Fix applied |
|------|-------------|
| R-007-record-design.md (R-007a) | Introduced `Name` tiny type; `UserSummary` now takes `Name` instead of raw `String`. |
| R-011-method-naming.md (R-011f) | `User` class now uses `Name` tiny type instead of raw `String` field. |
| R-013-method-design.md (R-013w) | Replaced `getToken()`/`getExpiry()`/`getTotal()`/`getDestination()` with property-style accessors; renamed `qualifiesForFreeShipping` → `isEligibleForFreeShipping` (approved `is` prefix). |
| R-013-method-design.md (R-013y) | Reduced to one parameter (`Money price`); discount factor lifted to a class-level constant. |
| R-017-comment.md (R-017a) | Replaced two-param `add(int a, int b)` with single-param `doubleValue(int value)`. |
| R-017-comment.md (R-017c) | Replaced `priceWithTax(BigDecimal, BigDecimal)` with `computeTotalWithTax(Invoice)` returning `Money`. |
| R-017-comment.md (R-017e) | Moved `cache` field initialization out of the declaration into the constructor. |
| R-019-concurrency.md (R-019a) | Introduced `Price` tiny type; renamed `price(Isin)` → `findPrice(Isin)`. |
| R-019-concurrency.md (R-019b) | Replaced `long now` parameter with `Instant now`; field type switched to `AtomicReference<Instant>`. |
| R-019-concurrency.md (R-019c) | Renamed `get(Isin)` → `quote(Isin)` to avoid the forbidden `get` prefix. |
| R-019-concurrency.md (R-019j) | Renamed `client()` → `httpClient()` to match the field name (R-011f). |
| R-020-unit-test.md (R-020g) | Second Good test now uses `// given / when` combined marker (see new R-020d exception). |
| R-999-not-categorized.md (R-999g) | Added compact canonical constructors with `requireNonNull` checks to both Bad and Good records. |

## Resolved rule-text issues

| Rule | Fix applied |
|------|-------------|
| R-014 (all sub-rules) | Replaced `com.dynatrace.deus.util.preconditions.Preconditions.requireNotEmpty` with `java.util.Objects.requireNonNull` throughout. |
| R-011g / R-013w | Unified predicate prefix list to `is`, `has`, `can`, `should`, `was`, `contains`. R-013w now cross-references R-011g. |
| R-013q | Added explicit scope note: applies to methods only; constructors and record canonical constructors are exempt. |
| R-020d | Added exception: tests with no distinct action may combine `// given / when` into a single marker. |
| R-003m | Added note clarifying that `public static final` constants are exempt from the tiny-type rules (R-002s, R-013r, R-014i). |

## Clean files

All 21 rule files now pass cross-rule compliance:

- R-001-class-naming.md
- R-002-class-design.md
- R-003-class-field.md
- R-004-interface-naming.md
- R-005-interface-design.md
- R-006-record-naming.md
- R-007-record-design.md
- R-008-enum.md
- R-009-imports.md
- R-010-programming-by-contract.md
- R-011-method-naming.md
- R-012-method-code.md
- R-013-method-design.md
- R-014-tiny-type.md
- R-015-package-and-file-naming.md
- R-016-local-variable.md
- R-017-comment.md
- R-018-exception-handling.md
- R-019-concurrency.md
- R-020-unit-test.md
- R-999-not-categorized.md
