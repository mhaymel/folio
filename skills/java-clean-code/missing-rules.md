# Missing Clean Code Rules

> Generated: 2026-04-16

## 1. Exception Handling → new `R-018-exception-handling.md`

- ~~Custom exception classes must extend `RuntimeException` and be `final`~~ → R-018b ✅
- ~~Exception class names must end with `Exception`~~ → R-018c ✅
- ~~Prefer unchecked (runtime) exceptions over checked exceptions~~ → R-018d ✅
- ~~Do not use exceptions for control flow~~ → R-018e ✅
- ~~Do not return `null` from a `catch` block~~ → R-018f ✅
- ~~Do not swallow exceptions silently~~ → R-018g ✅
- ~~Always preserve the original cause when wrapping exceptions (`new XException(message, cause)`)~~ → R-018h ✅

## 2. Testing Conventions → new `R-019-testing.md`

- Test methods must follow a consistent naming pattern (e.g. `shouldDoXWhenY`)
- One logical assertion per test method (or one behavior)
- Structure tests as Arrange–Act–Assert (or Given–When–Then) with blank-line separation
- Tests must be independent — no shared mutable state between tests
- No logic (if/else, loops, try/catch) inside test methods
- Use `@ParameterizedTest` for data-driven variations instead of duplicating test methods
- Test class name must be `<ClassUnderTest>Test`
- No `System.out.println` in tests — use assertions
- Prefer asserting on domain objects / tiny types over raw primitives
- Test data must be constructed inline or via factory methods, not loaded from shared fixtures

## 3. Logging → new `R-020-logging.md`

- Use SLF4J (`Logger` / `LoggerFactory`) — no `System.out.println` or `System.err.println`
- Use parameterized logging (`log.info("User {} logged in", userId)`) — no string concatenation
- Log at the appropriate level: `error` for failures, `warn` for recoverable issues, `info` for business events, `debug`/`trace` for diagnostics
- Do not log sensitive data (passwords, tokens, PII)
- Do not log inside tight loops

## 4. Collections and Streams → new `R-021-collection-and-stream.md`

- Return empty collections instead of `null` (`List.of()`, `Map.of()`)
- Return unmodifiable collections from public methods (`List.copyOf()`, `Collections.unmodifiableList()`)
- No raw types — always use generics (`List<String>`, not `List`)
- Prefer `List.of()` / `Map.of()` over `new ArrayList<>()` for immutable collections
- Prefer method references over lambdas when the lambda only delegates (`this::process` instead of `x -> process(x)`)
- Stream pipelines must be short and readable — extract complex intermediate operations into private methods
- No side effects inside `map()`, `filter()`, or `flatMap()` — use `forEach()` or `peek()` (for debugging only)
- Prefer `Stream.toList()` (Java 16+) over `collect(Collectors.toList())`

## 5. Null Safety Beyond Contracts → add to `R-010-programming-by-contract.md`

- Never return `null` for collections — return `List.of()` / `Map.of()`
- Use `@Nullable` annotation when null is a valid value at a package-private boundary
- Prefer `Optional` (for public API) over returning `null` (already in R-013f/g, but no general "no null returns" rule)

## 6. Resource Management → new `R-022-resource-management.md`

- Always use try-with-resources for `AutoCloseable` / `Closeable` resources
- Do not call `.close()` manually in a `finally` block
- Streams on I/O sources (e.g. `Files.lines()`) must be used inside try-with-resources

## 7. Switch Statements and Expressions → add to `R-012-method-code.md`

- Prefer switch expressions (Java 14+) over switch statements
- Switches must be exhaustive — cover all enum constants or include a `default`
- No fall-through — every branch must `break`, `return`, `throw`, or use arrow syntax (`->`)
- Avoid switch on `String` when an enum is more appropriate

## 8. Type Casting and `instanceof` → add to `R-012-method-code.md`

- Avoid explicit type casts — prefer polymorphism or generics
- Use pattern matching for `instanceof` (Java 16+): `if (obj instanceof String s)` instead of cast-after-check
- Do not use `instanceof` followed by cast — let the pattern variable handle it

## 9. Class Member Ordering → add to `R-002-class-design.md`

- Enforce a consistent ordering: static fields → instance fields → constructors → public methods → package-private methods → private methods

## 10. Lambdas and Functional Style → add to `R-012-method-code.md`

- Lambdas must be short (1–3 lines) — extract longer lambdas to a private method
- Prefer method references (`this::process`) when the lambda is a simple delegation
- Do not use lambdas with side effects in functional operations (`map`, `filter`)

## 11. Annotation Usage → add to `R-002-class-design.md`

- `@Override` is mandatory on every overriding method
- Do not create custom annotations unless absolutely necessary
- Place annotations on their own line, above the method/field

## 12. String Handling → add to `R-012-method-code.md`

- Do not use string concatenation in loops — use `StringBuilder` or `String.join()`
- Prefer text blocks (Java 15+) for multi-line strings
- Use `String.isEmpty()` or `String.isBlank()` instead of comparing with `""` or checking `.length() == 0`

## 13. Dependency Injection / Spring → new `R-026-dependency-injection.md`

- Prefer constructor injection — no `@Autowired` on fields
- Do not use `@Autowired` when the class has a single constructor (Spring infers it)
- Injected dependencies must be `private final` fields

## 14. Equality and Comparison → add to `R-002-class-design.md`

- Do not override `equals` / `hashCode` unless you have a clear domain reason — prefer records
- If you override `equals`, you must override `hashCode` (and vice versa)
- Use `Objects.equals()` for null-safe comparison
- Compare enums with `==`, not `.equals()`

## 15. Concurrency → new `R-028-concurrency.md`

- Prefer immutable objects to avoid synchronization issues
- Do not use `synchronized` blocks unless you document why
- Shared mutable state must be justified and documented
- Prefer `java.util.concurrent` utilities over low-level `wait`/`notify`

---

## Summary

| # | Topic | Target File |
|---|-------|-------------|
| 1 | Exception handling | **new** `R-018-exception-handling.md` |
| 2 | Testing conventions | **new** `R-019-testing.md` |
| 3 | Logging | **new** `R-020-logging.md` |
| 4 | Collections & streams | **new** `R-021-collection-and-stream.md` |
| 5 | Null safety (beyond requireNonNull) | `R-010-programming-by-contract.md` |
| 6 | Resource management (try-with-resources) | **new** `R-022-resource-management.md` |
| 7 | Switch statements / expressions | `R-012-method-code.md` |
| 8 | Type casting / instanceof | `R-012-method-code.md` |
| 9 | Class member ordering | `R-002-class-design.md` |
| 10 | Lambdas / functional style | `R-012-method-code.md` |
| 11 | Annotation usage (`@Override`, placement) | `R-002-class-design.md` |
| 12 | String handling | `R-012-method-code.md` |
| 13 | Dependency injection / Spring | **new** `R-026-dependency-injection.md` |
| 14 | Equality & comparison | `R-002-class-design.md` |
| 15 | Concurrency | **new** `R-028-concurrency.md` |

