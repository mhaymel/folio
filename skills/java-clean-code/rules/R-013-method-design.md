# Method Design Rules

## R-013a

Methods must be package-private by default. Only make a method `public` if it must be accessed from outside the package (e.g. interface implementations or Spring controller endpoints).

**Bad:**

```java
final class UserService {
    public void saveUser(User user) {
    }
}
```

**Good:**

```java
final class UserService {
    void saveUser(User user) {
    }
}
```

---

## R-013b

`protected` methods are forbidden. Use package-private or `private` visibility instead. Since inheritance from concrete classes is not allowed (see R-002n), `protected` serves no purpose.

**Bad:**

```java
final class OrderService {
    protected void processOrder(Order order) {
    }
}
```

**Good:**

```java
final class OrderService {
    void processOrder(Order order) {
    }
}
```

---

## R-013c

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
            return order.amount().multiply(PREMIUM_DISCOUNT_RATE);
        } else {
            if (order.amount().value().compareTo(LARGE_ORDER_THRESHOLD) > 0) {
                return order.amount().multiply(LARGE_ORDER_DISCOUNT_RATE);
            } else {
                return order.amount().zero();
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
            return order.amount().multiply(PREMIUM_DISCOUNT_RATE);
        }
        if (order.amount().value().compareTo(LARGE_ORDER_THRESHOLD) > 0) {
            return order.amount().multiply(LARGE_ORDER_DISCOUNT_RATE);
        }
        return order.amount().zero();
    }
}
```

---

## R-013d

Avoid boolean parameters (flag arguments). A boolean parameter signals that the method does two different things. Split it into two separate methods with intention-revealing names.

**Bad:**

```java
final class UserService {
    void setActive(boolean isActive) {
        if (isActive) {
            enable();
        } else {
            disable();
        }
    }
}
```

**Good:**

```java
final class UserService {
    void activate() {
        enable();
    }

    void deactivate() {
        disable();
    }
}
```

---

## R-013e

Do not use output parameters -  do not modify an object passed as a parameter to communicate a result. Return a value instead.

**Bad:**

```java
final class PriceCalculator {
    private static final BigDecimal DISCOUNT_MULTIPLIER = BigDecimal.valueOf(0.9);

    void applyDiscount(Order order) {
        order.setPrice(order.price().multiply(DISCOUNT_MULTIPLIER));
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

final class PriceCalculator {
    private static final BigDecimal DISCOUNT_MULTIPLIER = BigDecimal.valueOf(0.9);

    Money computeDiscountedPrice(Order order) {
        return order.price().multiply(DISCOUNT_MULTIPLIER);
    }
}
```

---

## R-013f

Public methods that that may not produce a result must return `Optional`.  

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
public final class UserRepository {
    public Optional<User> findByEmail(Email email) {
        // returns Optional.empty() when not found
    }
}
```

---

## R-013g

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

## R-013h

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

## R-013i

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

## R-013j

A method must not have an unused parameter.

**Bad:**

```java
final class OrderService {
    void cancel(Order order) {
        doSomething();
    }
}
```

**Good:**

```java
final class OrderService {
    void cancel() {
        doSomething();
    }
}
```

---

## R-013k

Avoid deeply nested code. A method must not exceed two levels of nesting (relative to the method body). Extract inner logic into private methods to flatten the structure.

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

## R-013l

Methods must not contain dead or unreachable code. Remove any code after an unconditional `return`, `throw`, or `break`.

**Bad:**

```java
final class PaymentService {
    BigDecimal charge(Order order) {
        var amount = order.totalAmount();
        return amount;
        log.info("charged: " + amount); // unreachable
    }
}
```

**Good:**

```java
final class PaymentService {
    BigDecimal charge(Order order) {
        var amount = order.totalAmount();
        log.info("charged: " + amount);
        return amount;
    }
}
```

---

## R-013m

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

## R-013n

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

## R-013o

`static` methods are forbidden. Static methods cannot be overridden, are hard to mock in tests, and create hidden coupling. Use instance methods instead.

**Bad:**

```java
final class TaxCalculator {
    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.19);

    static Money calculateTax(Money amount) {
        // ...
    }
}
```

**Good:**

```java
final class TaxCalculator {
    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.19);

    Money calculateTax(Money amount) {
        // ...
    }
}
```

---

## R-013p

`Optional` must not be used as a method parameter. `Optional` was designed 
for return types to signal that a result may be absent -  not for inputs. 
An `Optional` parameter forces every caller to wrap values and makes the method harder to read. Use method overloading or pass `null` with a `@Nullable` annotation instead.

**Bad:**

```java
final class UserService {
    List<User> findUsers(Optional<NameFilter> nameFilter) {
        if (nameFilter.isPresent()) {
            return repository.findByName(nameFilter.get());
        }
        return repository.findAll();
    }
}
```

**Good:**

```java
final class UserService {
    List<User> findUsers() {
        return repository.findAll();
    }

    List<User> findUsers(NameFilter nameFilter) {
        return repository.findByName(nameFilter);
    }
}
```

---

## R-013q

A method must have **zero or one** parameter. Group related 
parameters into a record or — when the parameters are of the same 
type and represent a collection of similar elements — a List. This 
is the method-scoped companion to R-005e.

**Scope:** this rule applies to regular methods only. Constructors and 
record canonical constructors are exempt — their parameter count is 
bounded by the field count (see [R-002f](R-002-class-design.md#r-002f) 
and [R-003d](R-003-class-field.md#r-003d)).

**Bad (group into a record):**

```java
final class OrderService {
    void placeOrder(Product product, Quantity quantity, Price price) {
        // ...
    }
}
```

**Good (group into a record):**

```java
import static java.util.Objects.requireNonNull;

record OrderRequest(Product product, Quantity quantity, Price price) {
    OrderRequest {
        requireNonNull(product);
        requireNonNull(quantity);
        requireNonNull(price);
    }
}

final class OrderService {
    void placeOrder(OrderRequest request) {
        // ...
    }
}
```

**Bad (group into a list):**

```java
final class NotificationService {
    void notifyUsers(Email user1, Email user2, Email user3) {
        // ...
    }
}
```

**Good (group into a list):**

```java
final class NotificationService {
    void notifyUsers(List<Email> users) {
        // ...
    }
}
```

---

## R-013r

Avoid primitive obsession in method parameter. Do not pass raw primitive 
types (`String`, `int`, `long`, `BigDecimal`, etc.) when the value represents a 
domain concept. Wrap it in a dedicated tiny type (record) instead. Although R-013q 
already limits methods to one parameter, this rule still applies: even a single `String` 
that represents an ISIN, email address, or currency code should be a typed wrapper.

**Bad:**

```java
final class PortfolioService {
    void addPosition(String isin) {
        // is this an ISIN? A ticker? A name?
    }
}

final class PaymentService {
    BigDecimal convert(BigDecimal amount) {
        // amount in which currency?
    }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

record Isin(String value) {
    Isin {
        requireNonNull(value);
    }
}

final class PortfolioService {
    void addPosition(Isin isin) {
        // ...
    }
}

record Money(BigDecimal amount, Currency currency) {
    Money {
        requireNonNull(amount);
        requireNonNull(currency);
    }
}

final class PaymentService {
    Money convert(Money money) {
        // ...
    }
}
```

---

## R-013s

Method parameter must not be declared `final`.

**Bad:**

```java
final class UserService {
    void saveUser(final User user) {
        // do something
    }
}
```

**Good:**

```java
final class UserService {
    void saveUser(User user) {
        // do something
    }
}
```

This rule applies to all method-like declarations (regular methods, constructors, and record canonical constructors). Parameter annotations (e.g. `@Nullable`) are allowed; only the `final` keyword on parameters is forbidden.

---

## R-013t

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

## R-013u

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


## R-013v

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

## R-013w

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

## R-013x

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

## R-013y

Method and constructor parameter must not be reassigned. Treat every 
the parameter as effectively final. If a different value is needed, 
introduce a new local variable with a descriptive name. This is the 
parameter-scoped companion to [R-016i](R-016-local-variable.md#r-016i).

**Note:** Reassignment *inside a compact canonical constructor* of a 
record is an exception — it is the idiomatic way to apply defensive copies 
(e.g. `stocks = List.copyOf(requireNonNull(stocks));`, see R-007g and R-007h).

**Bad:**

```java
final class PriceService {
    private static final BigDecimal DISCOUNT_MULTIPLIER = BigDecimal.valueOf(0.9);

    Money applyDiscount(Money price) {
        price = price.multiply(DISCOUNT_MULTIPLIER);
        return price;
    }
}
```

**Good:**

```java
final class PriceService {
    private static final BigDecimal DISCOUNT_MULTIPLIER = BigDecimal.valueOf(0.9);

    Money applyDiscount(Money price) {
        Money discountedPrice = price.multiply(DISCOUNT_MULTIPLIER);
        return discountedPrice;
    }
}
```

---
