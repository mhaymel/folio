# java-clean-code Rule Audit Report

## Summary

~160 Good blocks audited, 11 failing. 2 Bad blocks flagged as muddy. 6 rule-text issues noted.

## Good-block violations

| File | Sub-rule illustrated | Violates | Note |
|------|---------------------|----------|------|
| R-003-class-field.md | R-003c Good (UserService) | R-003b | `private UserId userId` is not declared `final`; could be final since ctor assigns once via `requireNonNull` |
| R-003-class-field.md | R-003d Good (UserCredentials) | R-007f | record `UserCredentials(UserName userName, Password password)` has no compact ctor with `requireNonNull(userName); requireNonNull(password);` |
| R-005-interface-design.md | R-005e Good (notifyUsers) | R-013r | `notifyUsers(List<String> users)` — should be `List<Email>` since emails are a domain concept (aligns with R-013q Good list example) |
| R-007-record-design.md | R-007d Good (UserService record) | R-006b, R-006c | record named `UserService` — not a data-model noun; should be something like `UserAccount` or `UserRegistration` |
| R-010-programming-by-contract.md | R-010b Good (ExampleGood) | R-001c | class name `ExampleGood` is not meaningful (does not describe responsibility) |
| R-013-method-design.md | R-013f Good (UserRepository) | R-013a | `public Optional<User> findByEmail(Email email)` in pkg-private class — the class was demoted from public but `public` stayed on the method |
| R-013-method-design.md | R-013y Good (applyDiscount) | R-013q | `applyDiscount(Money price, Discount discount)` has 2 parameters; should be a single grouping record or overload |
| R-017-comment.md | R-017a Good (Calculator.add) | R-013q, R-999f | `int add(int a, int b)` has 2 params; `a`/`b` are single-letter unsearchable names |
| R-017-comment.md | R-017c Good (priceWithTax) | R-013q | `priceWithTax(BigDecimal price, BigDecimal taxRate)` has 2 parameters |
| R-017-comment.md | R-017e Good (cache field) | R-003c | `private final Map<String, Value> cache = new ConcurrentHashMap<>();` initializes a field at declaration instead of in the primary constructor |
| R-019-concurrency.md | R-019j Good (static-holder) | R-002k, R-013o | `private static final class Holder` is an inner class (R-002k forbids inner classes); `static ExchangeRates get()` is a static method (R-013o forbids static methods) |

## Bad-block incidental issues

| File | Sub-rule illustrated | Incidental issue | Note |
|------|---------------------|------------------|------|
| R-013-method-design.md | R-013y Bad (applyDiscount) | R-013q | 2 parameters muddies whether the example teaches parameter reassignment or parameter count |
| R-017-comment.md | R-017a Bad (Calculator.add) | R-013q, R-999f | same 2-param + single-letter names as the Good block; reader can't tell which aspect the rule is about |

## Rule-text issues

- **R-014d** — Bad and Good blocks are literally identical (`record Isin(String value) { Isin { requireNotEmpty(value); } }`). The example does not demonstrate a contrast. The Bad should show a record without validation (or without the compact ctor altogether) so the Good's validation is the distinguishing feature.
- **R-016k Good (local constants)** — uses `final int STATUS_PENDING = 5;` and `final String ROLE_ADMIN = "A";` as local variables. This directly contradicts R-016j ("Local variables must not be declared `final`"). Either drop `final` from the locals in R-016k, or teach class-level constants only.
- **R-019j Good (static-holder)** — the static-holder idiom structurally requires an inner static class and a static accessor method, which R-002k and R-013o explicitly forbid. The rule and its example cannot both be correct as written; add an explicit exception to R-002k/R-013o for this idiom or remove the static-holder Good block and rely on the `AtomicReference.updateAndGet` variant.
- **R-003n** — line 376 has a stray leading `f` immediately before `**Note:**`.
- **R-002-class-design.md** — line 410 has a stray `ma` after the closing fence of R-002k's Good block.
- **Mojibake (`ΓÇö` for `—`, `ΓåÆ` for arrow)** still present in: R-001c line 45, R-002i Good comment (line 316), R-003h line 207, R-003n line 354, R-015e lines 105/107/113/115 (`ΓåÆ`). A previous pass cleaned R-010 but not the rest.

## Clean files

- R-001-class-naming.md — all blocks ok (mojibake aside)
- R-002-class-design.md — all blocks ok (mojibake + stray `ma` aside)
- R-004-interface-naming.md — all blocks ok
- R-006-record-naming.md — all blocks ok
- R-008-enum.md — all blocks ok
- R-009-imports.md — all blocks ok
- R-011-method-naming.md — all blocks ok
- R-012-method-code.md — all blocks ok
- R-015-package-and-file-naming.md — all blocks ok (mojibake aside; no Java code)
- R-018-exception-handling.md — all blocks ok
- R-020-unit-test.md — all blocks ok
- R-999-not-categorized.md — all blocks ok
