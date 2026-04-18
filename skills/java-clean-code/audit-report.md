# java-clean-code audit report

## Summary

~140 Good blocks audited across 20 rule files; 22 failing. 2 Bad blocks flagged as muddy.

Dominant failure modes:
- Method names that are nouns, violating R-011b (verb phrase).
- Good blocks that use 2+ method parameters, violating R-013q.
- Record/class Good blocks that expose raw `String`/`BigDecimal`/`long`/`int` for domain concepts, violating R-002s / R-013r / R-014i.
- Single-field records named with `id`/`isin`/etc. components instead of the mandatory `value` component (R-014c).
- Test Good blocks missing `// given // when // then` markers (R-020d).
- Use of `getXxx()` accessors in a Good example that post-dates the R-011f prohibition.

## Good-block violations

| File | Sub-rule illustrated | Violates | Note |
|------|---------------------|----------|------|
| R-006-record-naming.md | R-006a `UserProfile`, `OrderDto` | R-002s, R-013r, R-014i | record components `String name`, `String email`, `String id` represent domain concepts but use raw `String`; should wrap in tiny types |
| R-006-record-naming.md | R-006b `UserRegistration`, `PaymentRequest` | R-002s, R-013r, R-014i | `String name`, `String email`, `String id`, `BigDecimal amount` — raw primitives for domain concepts |
| R-006-record-naming.md | R-006c `CustomerAddress`, `InvoiceSummary`, `LoginCredentials` | R-002s, R-013r, R-014i | all components declared as `String`/`BigDecimal` rather than domain-specific tiny types |
| R-006-record-naming.md | R-006d `XmlEntry(int id)` | R-014c | tiny type component must be named `value`, not `id` |
| R-007-record-design.md | R-007a `record UserSummary(long id, String name)` | R-002s, R-013r, R-014i | `String name` is a domain concept; should be `Name` tiny type |
| R-007-record-design.md | R-007b, R-007c, R-007e, R-007f, R-007g, R-007h, R-007i, R-007j | R-002s, R-013r, R-014i | every Good example declares `String name`, `String label`, `String userName`, `String password`, `List<String> stocks`, `Map<String, Double>` etc. instead of tiny types |
| R-007-record-design.md | R-007i Good (with tiny types) `record UserSummary(UserId id, int age, boolean isActive)` | R-014i | `int age` still primitive; should be `Age` tiny type |
| R-010-programming-by-contract.md | R-010b `class Example`, `greet(String name)` | R-001c, R-013r | `Example` is a generic/meaningless class name (same family as `Thing`, `Helper`); `String name` is primitive obsession |
| R-010-programming-by-contract.md | R-010c `UserProcessor.normalizeName(String name)` | R-013r | `String name` parameter and `String` return are primitive obsession |
| R-011-method-naming.md | R-011f `User.name()` | R-013r | field `String name` is primitive obsession; should be `Name` tiny type |
| R-011-method-naming.md | R-011g `Order.containsKey(String key)` | R-013r | `String key` parameter is primitive obsession when `key` represents a domain concept |
| R-012-method-code.md | R-012a `BigDecimal refund(Invoice invoice)` | R-013r | returns raw `BigDecimal`; should return `Money` |
| R-012-method-code.md | R-012d Good (switch) `String label(Status status)` | R-011b | method name `label` is a noun, not a verb phrase; should be `toLabel`/`formatLabel` |
| R-013-method-design.md | R-013c `discount(Order order)` | R-011b, R-013r | method name `discount` is a noun; returns raw `BigDecimal` |
| R-013-method-design.md | R-013e `discountedPrice(Order order)` | R-011b, R-013r | method name `discountedPrice` is a noun; returns raw `BigDecimal` |
| R-013-method-design.md | R-013w `isLoggedIn`, `isSessionValid`, `qualifiesForFreeShipping` | R-011f, R-011g | example bodies call `user.getToken()`, `session.getExpiry()`, `order.getTotal()`, `order.getDestination()` — R-011f forbids `get` prefix; predicate `qualifiesForFreeShipping` does not start with any of the mandated prefixes `is`/`has`/`can`/`should`/`contains` |
| R-013-method-design.md | R-013y `applyDiscount(Money price, Discount discount)` | R-013q | method has **two** parameters; R-013q says exactly zero or one. Group into a record. |
| R-017-comment.md | R-017a `Calculator.add(int a, int b)` | R-013q, R-013r, R-999f | two parameters; both raw `int`; both single-letter names |
| R-017-comment.md | R-017c `priceWithTax(BigDecimal price, BigDecimal taxRate)` | R-011b, R-013q, R-013r | method name `priceWithTax` is a noun; two parameters; both raw `BigDecimal` |
| R-017-comment.md | R-017e TODO example | R-003c | `private final Map<String, Value> cache = new ConcurrentHashMap<>();` initializes a field at the point of declaration — R-003c forbids this |
| R-019-concurrency.md | R-019a `PriceSnapshot.price(Isin isin)` and `PriceCache.price(Isin isin)` | R-011b, R-013r | method name `price` is a noun; returns raw `BigDecimal` in a `Map<Isin, BigDecimal>` |
| R-019-concurrency.md | R-019b `FetchTracker.tryStart(long now)` | R-013r | parameter `long now` is primitive obsession for a timestamp (should be `Instant` or a dedicated tiny type) |
| R-019-concurrency.md | R-019c `QuoteRegistry.get(Isin isin)` | R-011f | returns `Quote` (a non-boolean property-style lookup); the generic `get` name is the exact getter-style prefix R-011f discourages — prefer `quote(Isin isin)` or `find(Isin isin)` |
| R-019-concurrency.md | R-019j `QuoteClient.client()` | R-011f | method exposes the `httpClient` field; should be named `httpClient()` (R-011f: "Use the property name directly") rather than `client()` |
| R-020-unit-test.md | R-020g Good (both test methods `shouldAddStock`, `shouldStartEmpty`) | R-020d | test method bodies lack the mandatory `// given` / `// when` / `// then` comment markers |
| R-999-not-categorized.md | R-999g `record TickerSymbolFilter(String isinFragment, String tickerSymbolFragment, String nameFragment)` | R-007f | record has three object (non-primitive) components but no compact canonical constructor with `requireNonNull` checks |

## Bad-block incidental issues

| File | Sub-rule illustrated | Incidental issue | Note |
|------|---------------------|------------------|------|
| R-019-concurrency.md | R-019g Bad `QuoteFetcher.await(Duration timeout)` | R-019h | the Bad block calls `Thread.sleep(timeout.toMillis())`, which is also what the Good blocks below do — the Bad block is teaching the interrupt-handling issue, not R-019h, but a reader comparing Bad→Good may confuse the lesson |
| R-013-method-design.md | R-013y Bad `applyDiscount(Money price, Discount discount)` | R-013q | Bad already has 2 parameters (the rule being taught is parameter reassignment, but the 2-param shape means the example also violates R-013q — a reader may conflate the two lessons) |

## Rule-text issues

- **R-014 preconditions import.** R-014 Good examples use `requireNotEmpty` from `com.dynatrace.deus.util.preconditions.Preconditions`. That utility is not defined anywhere in this skill and its existence is implicit. A reader outside the Dynatrace codebase cannot apply the example verbatim. Either ship a sample implementation or change the examples to use `requireNonNull` + an inline blank check.
- **R-011g vs R-013w prefix list.** R-011g lists `is, has, can, should, contains`. R-013w lists `is, has, can, should, was`. `contains` vs `was` are inconsistent between the two rule files. The R-013w Good example also uses `qualifiesForFreeShipping`, which matches *neither* list.
- **R-013q vs constructors.** R-013q says "a method must have zero or one parameter," but R-002f mandates that the primary constructor takes one parameter per field, and R-003d allows up to three fields. That implies constructors are exempt from R-013q. R-013s explicitly clarifies the opposite scope ("applies to all method-like declarations"). R-013q should state its scope explicitly (methods only, or methods + constructors) to remove ambiguity.
- **R-020g missing Given/When/Then.** Both Good blocks in R-020g omit the markers that R-020d makes mandatory. Either R-020g Good blocks should add the markers, or R-020d should note that trivial tests may omit them.
- **R-003m `public static final int OK = 200;`.** Numeric HTTP codes are domain concepts that other rules (R-002s, R-013r, R-014i) would push toward a tiny type. R-003m reads as a blanket exception for "constants", which conflicts with the tiny-type rules. Clarify whether `public static final int` constants are a carve-out from R-014i.

## Clean files

- R-001-class-naming.md — all blocks ok
- R-002-class-design.md — all blocks ok (3-field + 3-param constructors sit at the R-003d / R-002f limits, not over; empty classes in `R-002k` / `R-002l` Good blocks accepted as stripped-down illustrations of the extraction)
- R-003-class-field.md — all blocks ok (missing constructors in several field-focused Good blocks accepted as stripped-down illustrations)
- R-004-interface-naming.md — all blocks ok
- R-005-interface-design.md — all blocks ok
- R-008-enum.md — all blocks ok
- R-009-imports.md — all blocks ok
- R-014-tiny-type.md — all blocks ok (primitive wrapping is the point of tiny types)
- R-015-package-and-file-naming.md — all blocks ok (file uses prose/text examples, no Java blocks needing cross-check)
- R-016-local-variable.md — all blocks ok (raw `BigDecimal`/`String` locals accepted — these examples are about variable *naming*, not type choice)
- R-018-exception-handling.md — all blocks ok (custom exceptions using `String message` accepted as conventional for `RuntimeException` subclasses)
