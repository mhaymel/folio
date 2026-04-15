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
final class DiscountService {
    BigDecimal discount(Order order) {
        if (order.isPremium()) {
            return order.amount().multiply(BigDecimal.valueOf(0.2));
        } else {
            if (order.amount().compareTo(BigDecimal.valueOf(100)) > 0) {
                return order.amount().multiply(BigDecimal.valueOf(0.1));
            } else {
                return BigDecimal.ZERO;
            }
        }
    }
}
```

**Good:**

```java
final class DiscountService {
    BigDecimal discount(Order order) {
        if (order.isPremium()) {
            return order.amount().multiply(BigDecimal.valueOf(0.2));
        }
        if (order.amount().compareTo(BigDecimal.valueOf(100)) > 0) {
            return order.amount().multiply(BigDecimal.valueOf(0.1));
        }
        return BigDecimal.ZERO;
    }
}
```

---

## R-013d

Avoid boolean parameters (flag arguments). A boolean parameter signals that the method does two different things. Split it into two separate methods with intention-revealing names.

**Bad:**

```java
final class InvoiceService {
    void send(Invoice invoice, boolean withCopy) {
        mailer.send(invoice);
        if (withCopy) {
            archiver.archive(invoice);
        }
    }
}
```

**Good:**

```java
final class InvoiceService {
    void send(Invoice invoice) {
        mailer.send(invoice);
    }

    void sendAndArchive(Invoice invoice) {
        send(invoice);
        archiver.archive(invoice);
    }
}
```

---

## R-013e

Do not use output parameters ΓÇö do not modify an object passed as a parameter to communicate a result. Return a value instead.

**Bad:**

```java
final class PriceCalculator {
    void applyDiscount(Order order) {
        order.setPrice(order.price().multiply(BigDecimal.valueOf(0.9)));
    }
}
```

**Good:**

```java
final class PriceCalculator {
    BigDecimal discountedPrice(BigDecimal price) {
        return price.multiply(BigDecimal.valueOf(0.9));
    }
}
```

---

## R-013f

Public methods that that may not produce a result must return `Optional`.  

**Exception:** `Optional` produces an additional object. If object churn matters `null` can be used as a return value. In this case a comment should be added why Optional was not used and the method name should make it clear that `null` is a possible return value.

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
final class UserRepository {
    public Optional<User> findByEmail(Email email) {
        // returns Optional.empty() when not found
    }
}
```

---

## R-013g

Package private methods which may not produce a result can either return an `Optional` or `null`.

**Good:**

```java
final class UserRepository {
    User findByEmail(Email email) {
        // returns null when not found
    }

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
    private Optional<User> lookupUser(String email) {
        // ...
    }

    Optional<User> findByEmail(String email) {
        return lookupUser(email);
    }
}
```

**Good:**

```java
final class UserService {
    private User lookupUser(String email) {
        // returns null when not found
    }

    Optional<User> findByEmail(String email) {
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

Keep methods short ΓÇö aim for at most 15 lines of logic (excluding blank lines and braces). Long methods are hard to read and test. Extract helper methods instead.

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
    static BigDecimal calculateTax(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(0.19));
    }
}
```

**Good:**

```java
final class TaxCalculator {
    BigDecimal calculateTax(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(0.19));
    }
}
```

---

## R-013p

`Optional` must not be used as a method parameter. `Optional` was designed 
for return types to signal that a result may be absent ΓÇö not for inputs. 
An `Optional` parameter forces every caller to wrap values and makes the method harder to read. Use method overloading or pass `null` with a `@Nullable` annotation instead.

**Bad:**

```java
final class UserService {
    List<User> findUsers(Optional<String> nameFilter) {
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

    List<User> findUsers(String nameFilter) {
        return repository.findByName(nameFilter);
    }
}
```

---

## R-013q

A method must have **zero or one** parameter. Multiple parameters are hard to read, 
easy to swap by accident, and signal that the method is doing too much. 
Group related parameters into a record, an object, or ΓÇö when the parameters are of 
the same type and represent a collection of similar elements ΓÇö a `List`.

**Bad:**

```java
final class OrderService {
    void placeOrder(String product, int quantity, BigDecimal price, String currency) {
        // ...
    }
}

final class NotificationService {
    void notifyUsers(String user1, String user2, String user3) {
        // ...
    }
}
```

**Good:**

```java
record OrderRequest(String product, int quantity, BigDecimal price, String currency) {
}

final class OrderService {
    void placeOrder(OrderRequest request) {
        // ...
    }
}

final class NotificationService {
    void notifyUsers(List<String> users) {
        // ...
    }
}
```

---

## R-013r

Avoid primitive obsession in method parameters. Do not pass raw primitive types (`String`, `int`, `long`, `BigDecimal`, etc.) when the value represents a domain concept. Wrap it in a dedicated tiny type (record) instead. This prevents accidental misuse, makes the API self-documenting, and lets the compiler catch mistakes that primitive types cannot. Although R-013q already limits methods to one parameter, this rule still applies: even a single `String` that represents an ISIN, email address, or currency code should be a typed wrapper.

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
record Isin(String value) {
}

final class PortfolioService {
    void addPosition(Isin isin) {
        // ...
    }
}

record Money(BigDecimal amount, Currency currency) {
}

final class PaymentService {
    Money convert(Money money) {
        // ...
    }
}
```

---

## R-013s

Method parameters must not be declared `final`.

Declaring a parameter `final` is unnecessary noise: it does not change the public API and clutters signatures. If immutability is desired for intermediate values, prefer using local `final` variables inside the method body. Keep parameter lists concise and free of modifiers.

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

Setter methods are forbidden. Objects should be immutable ΓÇö set all state through the constructor. Mutable setters make objects harder to reason about, break thread-safety, and invite temporal coupling (the caller must remember to call setters in the right order).

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

final class User {
    private final Name name;
    private final Email email;

    User(Name name, Email email) {
        this.name = requireNonNull(name);
        this.email = requireNonNull(email);
    }
}
```

**Good (framework exception ΓÇö JPA entity):**

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

    List<Position> getPositions() {
        accessed = true; // mutates state
        return positions;
    }
}
```

**Good:**

```java
final class Portfolio {
    private final List<Position> positions;

    List<Position> getPositions() {
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

    // unused private helper ΓÇö should be removed
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

