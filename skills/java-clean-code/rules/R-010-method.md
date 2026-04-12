# Method Rules

## R-010a

Method names must use `lowerCamelCase`.

**Bad:**

```java
final class OrderService {
    void ProcessOrder(Order order) {
    }

    void save_user(User user) {
    }
}
```

**Good:**

```java
final class OrderService {
    void processOrder(Order order) {
    }

    void saveUser(User user) {
    }
}
```

---

## R-010b

Method names must be a verb or verb phrase that describes the action performed.

**Bad:**

```java
final class OrderService {
    void order(Order order) {
    }

    String username() {
    }
}
```

**Good:**

```java
final class OrderService {
    void placeOrder(Order order) {
    }

    String findUsername() {
    }
}
```

---

## R-010c

Method names must be **meaningful** and clearly describe what the method does — a reader should immediately understand the method's purpose from its name alone.

**Bad:**

```java
final class OrderService {
    void handle(Order order) {
    }

    void doStuff() {
    }
}
```

**Good:**

```java
final class OrderService {
    void validateOrder(Order order) {
    }

    void sendConfirmationEmail() {
    }
}
```

---

## R-010d

Method names must be **more than one character** long. Single-letter method names like `a`, `x`, `f` are forbidden.

**Bad:**

```java
final class MathService {
    int a(int x) {
        return x * 2;
    }
}
```

**Good:**

```java
final class MathService {
    int doubleValue(int value) {
        return value * 2;
    }
}
```

---

## R-010e

Do not use underscores in method names. Use `lowerCamelCase` exclusively. This applies to all methods, including unit test methods.

**Bad:**

```java
final class OrderService {
    void process_order(Order order) {
    }
}

final class OrderServiceTest {
    void should_process_order() {
    }
}
```

**Good:**

```java
final class OrderService {
    void processOrder(Order order) {
    }
}

final class OrderServiceTest {
    void shouldProcessOrder() {
    }
}
```

---

## R-010f

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

## R-010g

`protected` methods are forbidden. Use package-private or `private` visibility instead. Since inheritance from concrete classes is not allowed (see R-002s), `protected` serves no purpose.

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

## R-010h

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

## R-010i

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

## R-010j

Do not use output parameters — do not modify an object passed as a parameter to communicate a result. Return a value instead.

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

## R-010k

Prefer returning `Optional` over returning `null`. Returning `null` forces callers to remember null-checks and leads to `NullPointerException`s. Use `Optional` for methods that may not produce a result.

**Bad:**

```java
final class UserRepository {
    User findByEmail(String email) {
        // returns null when not found
    }
}
```

**Good:**

```java
final class UserRepository {
    Optional<User> findByEmail(String email) {
        // returns Optional.empty() when not found
    }
}
```

---

## R-010l

A method must not have unused parameters. Remove any parameter that is not referenced in the method body.

**Bad:**

```java
final class OrderService {
    void cancel(Order order, String reason) {
        order.markCancelled();
    }
}
```

**Good:**

```java
final class OrderService {
    void cancel(Order order) {
        order.markCancelled();
    }
}
```

---

## R-010m

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

## R-010n

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

## R-010o

Getter-style methods for non-boolean properties must not use the `get` prefix. Use the property name directly. This follows the convention established by Java records.

**Bad:**

```java
final class User {
    private final String name;

    String getName() {
        return name;
    }
}
```

**Good:**

```java
final class User {
    private final String name;

    String name() {
        return name;
    }
}
```

---

## R-010p

Boolean query methods must use an `is` or `has` prefix.

**Bad:**

```java
final class Order {
    boolean cancelled() {
        return status == Status.CANCELLED;
    }

    boolean items() {
        return !items.isEmpty();
    }
}
```

**Good:**

```java
final class Order {
    boolean isCancelled() {
        return status == Status.CANCELLED;
    }

    boolean hasItems() {
        return !items.isEmpty();
    }
}
```

---

## R-010q

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

## R-010r

Keep methods short — aim for at most 15 lines of logic (excluding blank lines and braces). Long methods are hard to read and test. Extract helper methods instead.

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

## R-010s

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

## R-010t

Private methods must not return `Optional`. Use `null` internally 
and convert to `Optional` at the public API boundary. `Optional` is designed 
for public return types to signal "may be absent" to callers — inside a class, 
`null` is simpler and avoids unnecessary wrapping.

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

## R-010u

`Optional` must not be used as a method parameter. `Optional` was designed 
for return types to signal that a result may be absent — not for inputs. 
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

## R-010v

A method must have **zero or one** parameter. Multiple parameters are hard to read, 
easy to swap by accident, and signal that the method is doing too much. 
Group related parameters into a record, an object, or — when the parameters are of 
the same type and represent a collection of similar elements — a `List`.

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

## R-010w

Avoid primitive obsession in method parameters. Do not pass raw primitive types (`String`, `int`, `long`, `BigDecimal`, etc.) when the value represents a domain concept. Wrap it in a dedicated tiny type (record) instead. This prevents accidental misuse, makes the API self-documenting, and lets the compiler catch mistakes that primitive types cannot. Although R-010v already limits methods to one parameter, this rule still applies: even a single `String` that represents an ISIN, email address, or currency code should be a typed wrapper.

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
