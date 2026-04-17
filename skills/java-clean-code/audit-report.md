# java-clean-code Rule Audit Report

## Summary
~160 Good blocks audited across R-001 through R-019 (and R-999). ~30 Good blocks have clear cross-rule violations. Several Bad blocks have incidental issues and a handful of rule files have internal text problems worth flagging.

## Good-block violations

| File | Sub-rule illustrated | Violates | Note |
|------|---------------------|----------|------|
| R-002-class-design.md | R-002c (`AdminUser extends User` vs interface) | R-011f | Interface `Identifiable` declares `long getId()` and the class implements `public long getId()` — non-boolean getter must not use `get` prefix |
| R-002-class-design.md | R-002k (extract inner class) | R-007a | Extracted `UserCredentials` is an immutable class with only `final` fields — should be a record |
| R-002-class-design.md | R-002o (parameter object) | R-007f | `record OrderRequest(String product, int quantity, BigDecimal price)` — Object components not `requireNonNull`-validated |
| R-002-class-design.md | R-002s (tiny types) | R-007f, R-014d | `record Isin(String value)`, `record PortfolioName(String value)`, `record Money(BigDecimal amount, Currency currency)` — no compact ctor / `requireNonNull` validation |
| R-003-class-field.md | (implicit R-003f/g/h/i/j/k examples) | R-002f | Several Good snippets show fields without a primary constructor; taken literally this breaks R-002f. (Treatable as elision, see Rule-text issues below.) |
| R-005-interface-design.md | R-005d (interface signatures) | R-011b | Good signature includes `Map<UserId, User> byId()` — getter-style method name is not a verb |
| R-006-record-naming.md | R-006a / R-006b / R-006c | R-007f | `record UserProfile(String name, String email)`, `record OrderDto(String id)`, `record UserRegistration(...)`, `record PaymentRequest(...)`, `record CustomerAddress(...)`, `record InvoiceSummary(...)`, `record LoginCredentials(...)` — all have Object components with no `requireNonNull` |
| R-006-record-naming.md | R-006d | R-014d, R-007f | `record Account(String value)` looks like a tiny type but has no validation; `value` String not null-checked |
| R-007-record-design.md | R-007a / R-007b / R-007c / R-007e | R-007f | `record UserSummary(long id, String name)`, `record Credentials(String userName, String password)` — Object components without `requireNonNull` (self-inconsistent given R-007f is in the same file) |
| R-007-record-design.md | R-007d | — (syntax) | Good block is not valid Java: `final record UserService { private final int userId; ... }` uses class-style field declarations inside a record and `final record` is tautological — see Rule-text issues |
| R-007-record-design.md | R-007i (primitives over boxed) | R-011g / R-003k | `record UserSummary(long id, int age, boolean active)` — boolean component becomes accessor `active()` which lacks the required `is/has/can/...` prefix |
| R-007-record-design.md | R-007i (with tiny types variant) | R-007f, R-011g | `record UserSummary(UserId id, int age, boolean active)` — `UserId id` not null-checked; same `active` prefix issue |
| R-007-record-design.md | R-007j | R-007d | Good `Portfolio` has 4 components (`name`, `stocks`, `weights`, `report`) — R-007d advises at most 3 |
| R-009-imports.md | R-009a (no unused imports) | R-003c | `private final List<String> names = List.of("Alice");` — field initialized at point of declaration |
| R-010-programming-by-contract.md | R-010b (no message arg) | R-011d | Class `ExampleGood` has method `void m(String name)` — single-letter method name |
| R-011-method-naming.md | R-011f (no `get` prefix) | R-002f, R-007a | `final class User { private final String name; String name() {…} }` — one final field, no primary ctor, should arguably be a record |
| R-012-method-code.md | R-012b (no empty blocks) | R-016d | `catch (ValidationException e)` — single-letter catch variable; other R-018 examples consistently use `exception` |
| R-013-method-design.md | R-013c (early return) | R-999d / R-016k | Magic literals `0.2`, `100`, `0.1` in discount math |
| R-013-method-design.md | R-013e (no output params) | R-999d | Magic literal `0.9` |
| R-013-method-design.md | R-013g (pkg-private null return) | — (compile error) | The Good block declares two methods with the same name and parameter list (`User findByEmail(Email)` and `Optional<User> findByEmail(Email)`) which cannot coexist — see Rule-text issues |
| R-013-method-design.md | R-013o (no static methods) | R-999d | Magic literal `0.19` |
| R-013-method-design.md | R-013q (0–1 params) | R-007f, R-007d, R-013r | `record OrderRequest(String product, int quantity, BigDecimal price, String currency)` — 4 Object/primitive components, no `requireNonNull`; conflicts with R-007d (≤3) |
| R-013-method-design.md | R-013r (no primitive obsession) | R-007f, R-014d | `record Isin(String value)` and `record Money(BigDecimal amount, Currency currency)` — no compact-ctor validation |
| R-013-method-design.md | R-013t (no setters) | R-007a | `final class User { private final Name name; private final Email email; … }` — two final fields; prefer record |
| R-013-method-design.md | R-013u (getters only return) | R-011f | Good block keeps `List<Position> getPositions() { return positions; }` — non-boolean getter with `get` prefix |
| R-013-method-design.md | R-013w (extract predicates) | R-013q | `void process(User user, Session session, Order order)` has 3 parameters; R-013q caps at 0–1 |
| R-014-tiny-type.md | R-014b (one value) | R-007f, R-014d | `record Amount(BigDecimal value)` — no `requireNonNull`, no validation in compact ctor |
| R-014-tiny-type.md | R-014c (field named `value`) | R-014d | `record PortfolioId(long value) {}` has no compact ctor at all — primitive, but R-014d still expects rejection of domain-invalid state |
| R-019-concurrency.md | R-019a (immutability) | R-007h | `PriceSnapshot` compact ctor has `requireNonNull(prices); prices = Map.copyOf(prices);` as two statements instead of the composed `prices = Map.copyOf(requireNonNull(prices));` form R-007h mandates |
| R-019-concurrency.md | R-019b (atomics) | R-002f, R-002j | `FetchTracker()` is a no-arg ctor that internally instantiates `AtomicBoolean`/`AtomicLong` — primary ctor should take one param per field (R-002f) and must contain only precondition checks (R-002j). Also contradicts R-002i: since the fields are internal state, the primary ctor should be private with a public no-arg secondary ctor delegating to it |
| R-019-concurrency.md | R-019c (concurrent collections) | R-002f, R-002j, R-013q | `QuoteRegistry()` no-arg ctor instantiates `ConcurrentHashMap` internally; same R-002f / R-002j issue. `void put(Isin isin, Quote quote)` has 2 parameters (R-013q caps at 0–1) |
| R-019-concurrency.md | R-019d (dedicated lock) | R-002f, R-002j | `ExportService()` no-arg ctor instantiates `StringBuilder` and `Object` internally — same R-002f / R-002j issue |
| R-019-concurrency.md | R-019f (shutdown) | R-999d / R-016k | Magic literal `10` in `awaitTermination(10, TimeUnit.SECONDS)` |
| R-019-concurrency.md | R-019i (no wait/notify) | R-002f, R-002j | `JobCoordinator()` no-arg ctor instantiates `CountDownLatch(1)` internally — R-002f/R-002j |
| R-019-concurrency.md | R-019j (static-holder idiom) | R-002k, R-013o | `private static final class Holder` is a nested class (R-002k forbids inner classes); `static ExchangeRates get()` is a static method (R-013o forbids static methods). See Rule-text issues — R-019j and R-002k/R-013o are in direct conflict |
| R-019-concurrency.md | R-019j (instance-level lazy) | R-002f, R-002j | `QuoteClient()` no-arg ctor instantiates `AtomicReference<>()` internally — R-002f/R-002j |

## Bad-block incidental issues

| File | Sub-rule illustrated | Incidental issue | Note |
|------|---------------------|-------------------|------|
| R-013-method-design.md | R-013f (Optional return) | R-002b | `public final class UserRepository` is public where `R-013f`'s teaching point has nothing to do with class visibility; the matching Good block removes `public` without comment, which conflates two lessons |
| R-019-concurrency.md | R-019a (immutability Bad) | R-002f, R-002j | The Bad `PriceCache` also has a no-arg ctor initializing `HashMap`/`Object` internally; this is fine for illustrating the "lock-based" alternative but the Good block rewrites the constructor shape, not just the sync strategy |

## Clean files
- R-001-class-naming.md — all Good blocks ok
- R-004-interface-naming.md — all Good blocks ok
- R-008-enum.md — all Good blocks ok
- R-015-package-and-file-naming.md — all Good blocks ok
- R-016-local-variable.md — all Good blocks ok
- R-017-comment.md — all Good blocks ok
- R-018-exception-handling.md — all Good blocks ok
- R-999-not-categorized.md — all Good blocks ok

## Rule-text issues

- **R-007d**: the Good and Bad Java snippets use invalid record syntax (`final record UserService { private final int userId; … }`). Records take components in the header, not `private final` field declarations, and `final record` is tautological since records are implicitly final.
- **R-013g**: the Good block declares two methods with identical signatures (`User findByEmail(Email)` and `Optional<User> findByEmail(Email)`). They cannot coexist — the example needs to show two alternative implementations, not both at once.
- **R-014d**: the Good and Bad blocks are textually identical, so the rule cannot be illustrated by contrast.
- **R-019j vs R-002k / R-013o**: the static-holder idiom (nested `Holder` class + `static get()`) is the Good example for R-019j, but R-002k forbids inner classes and R-013o forbids static methods. An explicit exception clause is needed in R-002k/R-013o (or R-019j must propose a non-static alternative).
- **R-019b vs R-002f / R-002i / R-002j**: the "atomics" Good example uses a no-arg constructor to initialize internal `AtomicBoolean`/`AtomicLong` fields. R-002f (primary ctor declares one param per field), R-002i (private primary ctor for internal state), and R-002j (no code beyond precondition checks) together disallow this shape. Many Good blocks in R-019 share this pattern — the rule file needs either an exception for owned mutable primitives or rewritten examples that use a private primary ctor + secondary no-arg ctor. The same concern applies to R-019c / R-019d / R-019i / R-019j (instance-level).
- **R-013q vs constructors**: R-013q mandates "zero or one parameter" for methods. R-013s explicitly extends R-013 rules to "regular methods, constructors, and record canonical constructors." Together these conflict with R-002f, R-002o, and most multi-field class/record Good blocks throughout the skill. If R-013q is intended to apply only to non-constructor methods, the rule should say so explicitly.
