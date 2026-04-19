# Method Return Value & Body Rules

## R-014a

Return early to reduce nesting. Do not use `else` after `return` or `throw`.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

record Money(BigDecimal amount, Currency currency) {
    Money {
        requireNonNull(amount);
        requireNonNull(currency);
    }
}

final class DiscountService {
    private static final BigDecimal PREMIUM_DISCOUNT_RATE = BigDecimal.valueOf(0.2);
    private static final BigDecimal LARGE_ORDER_DISCOUNT_RATE = BigDecimal.valueOf(0.1);
    private static final BigDecimal LARGE_ORDER_THRESHOLD = BigDecimal.valueOf(100);

    Money computeDiscount(Order order) {
        if (order.isPremium()) {
            return order.price().multiply(PREMIUM_DISCOUNT_RATE);
        } else {
            if (order.price().amount().compareTo(LARGE_ORDER_THRESHOLD) > 0) {
                return order.price().multiply(LARGE_ORDER_DISCOUNT_RATE);
            } else {
                return order.price().zero();
            }
        }
    }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

record Money(BigDecimal amount, Currency currency) {
    Money {
        requireNonNull(amount);
        requireNonNull(currency);
    }
}

final class DiscountService {
    private static final BigDecimal PREMIUM_DISCOUNT_RATE = BigDecimal.valueOf(0.2);
    private static final BigDecimal LARGE_ORDER_DISCOUNT_RATE = BigDecimal.valueOf(0.1);
    private static final BigDecimal LARGE_ORDER_THRESHOLD = BigDecimal.valueOf(100);

    Money computeDiscount(Order order) {
        if (order.isPremium()) {
            return order.price().multiply(PREMIUM_DISCOUNT_RATE);
        }
        if (order.price().amount().compareTo(LARGE_ORDER_THRESHOLD) > 0) {
            return order.price().multiply(LARGE_ORDER_DISCOUNT_RATE);
        }
        return order.price().zero();
    }
}
```

---

## R-014b

Public methods that may not produce a result must return `Optional`.  

**Exception:** `Optional` produces an additional object. If object churn 
matters `null` can be used as a return value. In this case a comment should 
be added why Optional was not used and the method name should make it clear 
that `null` is a possible return value.

**Bad:**

```java
public final class UserRepository {
    public User findByEmail(Email email) {
        // returns null when not found
    }
}
```

**Good:**

```java
// public because callers outside this package rely on the repository contract;
// the rule demands a public method, which requires a public class (R-002b exception).
public final class UserRepository {
    public Optional<User> findByEmail(Email email) {
        // returns Optional.empty() when not found
    }
}
```

---

## R-014c

Package private methods which may not produce a result can either return an `Optional` or `null`.

**Good (returning `null`):**

```java
final class UserRepository {
    User findByEmail(Email email) {
        // returns null when not found
    }
}
```

**Good (returning `Optional`):**

```java
final class UserRepository {
    Optional<User> findByEmail(Email email) {
        // returns Optional.empty() when not found
    }
}
```

---

## R-014d

Private methods must not return `Optional`.

**Bad:**

```java
final class UserService {
    private Optional<User> lookupUser(Email email) {
        // ...
    }

    Optional<User> findByEmail(Email email) {
        return lookupUser(email);
    }
}
```

**Good:**

```java
final class UserService {
    private User lookupUser(Email email) {
        // returns null when not found
    }

    Optional<User> findByEmail(Email email) {
        return Optional.ofNullable(lookupUser(email));
    }
}
```

---

## R-014e

Use the primitive-specialized Optional types `OptionalInt`, `OptionalLong`, and `OptionalDouble` instead of their boxed counterparts `Optional<Integer>`, `Optional<Long>`, and `Optional<Double>`.

**Bad:**

```java
final class PortfolioService {
    Optional<Integer> countPositions(PortfolioId portfolioId) {
        // unnecessary boxing: int → Integer → Optional<Integer>
    }

    Optional<Long> totalVolume(PortfolioId portfolioId) {
        // unnecessary boxing: long → Long → Optional<Long>
    }

    Optional<Double> averageReturn(PortfolioId portfolioId) {
        // unnecessary boxing: double → Double → Optional<Double>
    }
}
```

**Good:**

```java
final class PortfolioService {
    OptionalInt countPositions(PortfolioId portfolioId) {
        // no boxing, returns OptionalInt.of(count) or OptionalInt.empty()
    }

    OptionalLong totalVolume(PortfolioId portfolioId) {
        // no boxing, returns OptionalLong.of(volume) or OptionalLong.empty()
    }

    OptionalDouble averageReturn(PortfolioId portfolioId) {
        // no boxing, returns OptionalDouble.of(avg) or OptionalDouble.empty()
    }
}
```

---

## R-014f

Avoid deeply nested code. A method must not exceed two levels of nesting 
(relative to the method body). Extract inner logic into private methods 
to flatten the structure.

**Bad:**

```java
final class OrderProcessor {
    void process(List<Order> orders) {
        for (Order order : orders) {
            if (!order.isCancelled()) {
                for (Item item : order.items()) {
                    if (item.isInStock()) {
                        ship(item);
                    }
                }
            }
        }
    }
}
```

**Good:**

```java
final class OrderProcessor {
    void process(List<Order> orders) {
        for (Order order : orders) {
            processOrder(order);
        }
    }

    private void processOrder(Order order) {
        if (order.isCancelled()) {
            return;
        }
        for (Item item : order.items()) {
            shipIfInStock(item);
        }
    }

    private void shipIfInStock(Item item) {
        if (item.isInStock()) {
            ship(item);
        }
    }
}
```

---

## R-014g

Methods must not contain dead or unreachable code. The compiler rejects
code after a direct `return`, `throw`, or `break`, but it cannot detect
code after a helper that always throws — remove those lines manually.

**Bad:**

```java
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

final class PaymentService {
    private static final Logger LOG = getLogger(PaymentService.class);

    void charge(Order order) {
        rejectBecauseDisabled();
        LOG.info("charged: {}", order); // dead — helper above always throws
    }

    private void rejectBecauseDisabled() {
        throw new IllegalStateException("charges disabled");
    }
}
```

**Good:**

```java
final class PaymentService {
    void charge(Order order) {
        rejectBecauseDisabled();
    }

    private void rejectBecauseDisabled() {
        throw new IllegalStateException("charges disabled");
    }
}
```

---

## R-014h

Methods must do one thing. If a method does more than one thing, extract the separate concerns into their own methods.

**Bad:**

```java
final class UserService {
    void validateAndSave(User user) {
        if (user.name().isBlank()) {
            throw new IllegalArgumentException("name is blank");
        }
        repository.save(user);
        emailService.sendWelcome(user);
    }
}
```

**Good:**

```java
final class UserService {
    void register(User user) {
        validate(user);
        save(user);
        notifyWelcome(user);
    }

    private void validate(User user) {
        if (user.name().isBlank()) {
            throw new IllegalArgumentException("name is blank");
        }
    }

    private void save(User user) {
        repository.save(user);
    }

    private void notifyWelcome(User user) {
        emailService.sendWelcome(user);
    }
}
```

---

## R-014i

Keep methods short -  aim for at most 15 lines of logic (excluding blank lines and braces). Long methods are hard to read and test. Extract helper methods instead.

**Bad:**

```java
final class ReportService {
    String generateReport(List<Order> orders) {
        var total = BigDecimal.ZERO;
        var count = 0;
        for (Order order : orders) {
            if (!order.isCancelled()) {
                total = total.add(order.amount());
                count++;
            }
        }
        var average = count > 0 ? total.divide(BigDecimal.valueOf(count)) : BigDecimal.ZERO;
        var sb = new StringBuilder();
        sb.append("Total: ").append(total);
        sb.append("\nCount: ").append(count);
        sb.append("\nAverage: ").append(average);
        return sb.toString();
    }
}
```

**Good:**

```java
final class ReportService {
    String generateReport(List<Order> orders) {
        var summary = summarize(orders);
        return formatReport(summary);
    }

    private OrderSummary summarize(List<Order> orders) {
        // ...
    }

    private String formatReport(OrderSummary summary) {
        // ...
    }
}
```

---

## R-014j

Setter methods are forbidden. Objects should be immutable -  set all state through the constructor. Mutable setters make objects harder to reason about, break thread-safety, and invite temporal coupling (the caller must remember to call setters in the right order).

**Exception:** setters are allowed only when technically unavoidable, e.g. when a framework requires them for deserialization (Jackson without `@JsonCreator`, JPA entities, etc.). In those cases, keep the setter package-private and document why it exists.

**Bad:**

```java
final class User {
    private Name name;
    private Email email;

    void setName(Name name) {
        this.name = name;
    }

    void setEmail(Email email) {
        this.email = email;
    }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

record User(Name name, Email email) {
    User {
        requireNonNull(name);
        requireNonNull(email);
    }
}
```

**Good (framework exception -  JPA entity):**

```java
import static java.util.Objects.requireNonNull;

final class User {
    private Name name;

    // required by JPA for reflective instantiation
    void setName(Name name) {
        this.name = requireNonNull(name);
    }
}
```

---

## R-014k

Getter methods must only return a value. They must not modify the state of the object and must not perform expensive or long-running operations.

**Bad:**

```java
final class Portfolio {
    private final List<Position> positions;
    private boolean accessed;

    List<Position> positions() {
        accessed = true; // mutates state
        return positions;
    }
}
```

**Good:**

```java
final class Portfolio {
    private final List<Position> positions;

    List<Position> positions() {
        return positions;
    }
}
```

---

## R-014l

Unused private methods are forbidden. A method is unused if it is not referenced anywhere in the codebase and also
not used by reflection (e.g. Spring controller endpoints, JPA entity lifecycle methods, etc.). 

**Bad:**

```java
final class ReportService {
    void generate() {
        // implementation...
    }

    // unused private helper -  should be removed
    private void formatAsCsv() {
        // leftover from previous implementation, not referenced anywhere
    }
}
```

**Good:**

```java
final class ReportService {
    void generate() {
        // implementation...
    }

    // unused helper removed; no dead code remains
}
```

---

## R-014m

Extract boolean conditions into private predicate methods (e.g. `isLoggedIn()`, `hasPermission()`, `isExpired()`). These methods serve as self-documenting code, replacing inline comments and improving readability. The method name **is** the comment.

Use one of the predicate prefixes listed in [R-011g](R-011-method-naming.md#r-011g) (`is`, `has`, `can`, `should`, `was`, `contains`). Keep each predicate focused on a single condition. Visibility must be `private` (or package-private if reused within the package).

**Bad:**

```java
final class OrderService {
    void process(OrderContext context) {
        // check if user is logged in and session is still valid
        if (context.user().token() != null && !context.user().token().value().isBlank()
                && context.session().expiry().isAfter(Instant.now())) {
            // check if order is eligible for free shipping
            if (context.order().total().value().compareTo(FREE_SHIPPING_THRESHOLD) >= 0
                    && context.order().destination().isInland()) {
                // ...
            }
        }
    }
}
```

**Good:**

```java
final class OrderService {
    void process(OrderContext context) {
        if (isLoggedIn(context.user()) && isSessionValid(context.session())) {
            if (isEligibleForFreeShipping(context.order())) {
                // ...
            }
        }
    }

    private boolean isLoggedIn(User user) {
        return user.token() != null && !user.token().value().isBlank();
    }

    private boolean isSessionValid(Session session) {
        return session.expiry().isAfter(Instant.now());
    }

    private boolean isEligibleForFreeShipping(Order order) {
        return order.total().value().compareTo(FREE_SHIPPING_THRESHOLD) >= 0
                && order.destination().isInland();
    }
}
```

---

## R-014n

Method and constructor parameters, as well as method return types, must use the most general interface that fits the usage. A concrete implementation class must not appear in a method or constructor signature when an interface exposes the contract actually needed. Pinning a signature to a concrete class leaks an implementation decision, blocks callers from passing alternative implementations, and couples the API to a single class hierarchy.

This applies to every type, not just collections. Common examples:

| Use (interface) | Not (concrete) |
|----|----|
| `List`, `Set`, `Map`, `Collection`, `Iterable`, `Queue`, `Deque` | `ArrayList`, `LinkedList`, `HashSet`, `LinkedHashSet`, `TreeSet`, `HashMap`, `LinkedHashMap`, `TreeMap`, `ArrayDeque` |
| `Executor`, `ExecutorService`, `ScheduledExecutorService` | `ThreadPoolExecutor`, `ScheduledThreadPoolExecutor` |
| `InputStream`, `OutputStream`, `Reader`, `Writer` | `FileInputStream`, `BufferedReader`, `PrintWriter`, `ByteArrayOutputStream` |
| `Path` | `File` |
| `Clock` | concrete clock |
| Your own interface (e.g. `UserRepository`) | Its implementation (`JpaUserRepository`) |

Only use the concrete type when behavior available **only** on that concrete type is actually required.

**Bad:**

```java
final class ReportService {
    private final ArrayList<Row> rows;
    private final ThreadPoolExecutor executor;

    ReportService(ArrayList<Row> rows, ThreadPoolExecutor executor) {
        this.rows = requireNonNull(rows);
        this.executor = requireNonNull(executor);
    }

    ArrayList<Row> activeRows() {
        return rows;
    }

    void addAll(HashSet<Row> newRows) {
        rows.addAll(newRows);
    }

    void writeTo(FileOutputStream out) {
        // ...
    }
}
```

**Good:**

```java
final class ReportService {
    private final List<Row> rows;
    private final Executor executor;

    ReportService(List<Row> rows, Executor executor) {
        this.rows = requireNonNull(rows);
        this.executor = requireNonNull(executor);
    }

    List<Row> activeRows() {
        return rows;
    }

    void addAll(Set<Row> newRows) {
        rows.addAll(newRows);
    }

    void writeTo(OutputStream out) {
        // ...
    }
}
```

---

## R-014o

Lazy loading is forbidden by default. Initialize every dependency eagerly in the primary 
constructor so a fully constructed object is always ready to use. Lazy initialization adds 
complexity (thread-safety, null handling, first-call latency spikes) and hides startup cost 
from the caller.

**Exception:** lazy loading is allowed only after profiling or benchmarking evidence shows 
that eager initialization has a measurable and unacceptable cost (startup time, memory, or 
an unused expensive resource). When the exception applies, a comment is required that names 
the measurement, ticket, or incident that justifies it — without that evidence the optimization 
is speculative.

**Bad (lazy without evidence):**

```java
import static java.net.http.HttpClient.newHttpClient;

final class QuoteClient {
    private final AtomicReference<HttpClient> httpClient;

    private QuoteClient(AtomicReference<HttpClient> httpClient) {
        this.httpClient = requireNonNull(httpClient);
    }

    QuoteClient() {
        this(new AtomicReference<>());
    }

    HttpClient resolveHttpClient() {
        return httpClient.updateAndGet(this::reuseOrCreate);
    }

    private HttpClient reuseOrCreate(HttpClient existing) {
        return existing != null ? existing : newHttpClient();
    }
}
```

**Good (eager):**

```java
final class QuoteClient {
    private final HttpClient httpClient;

    QuoteClient(HttpClient httpClient) {
        this.httpClient = requireNonNull(httpClient);
    }

    HttpClient httpClient() {
        return httpClient;
    }
}
```

**Good (lazy, with recorded evidence):**

```java
import static java.net.http.HttpClient.newHttpClient;

final class QuoteClient {
    // Lazy: startup profiling (PROJ-4321, trace 2026-03-12) showed
    // newHttpClient() adds 180 ms to cold start for a client unused on
    // 40% of request paths. Initialize on first resolve instead.
    private final AtomicReference<HttpClient> httpClient;

    private QuoteClient(AtomicReference<HttpClient> httpClient) {
        this.httpClient = requireNonNull(httpClient);
    }

    QuoteClient() {
        this(new AtomicReference<>());
    }

    HttpClient resolveHttpClient() {
        return httpClient.updateAndGet(this::reuseOrCreate);
    }

    private HttpClient reuseOrCreate(HttpClient existing) {
        return existing != null ? existing : newHttpClient();
    }
}
```

---
