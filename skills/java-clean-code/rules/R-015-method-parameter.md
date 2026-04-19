# Method Parameter Rules

## R-015a

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

## R-015b

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

## R-015c

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

## R-015d

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

## R-015e

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

## R-015f

Avoid primitive obsession in the method parameter. When the single parameter
a method takes ([R-015e](#r-015e)) represents a domain concept, it must not 
be a raw primitive (`String`, `int`, `long`, `BigDecimal`, etc.) — wrap it
in a dedicated tiny type (record, see [R-016](R-016-tiny-type.md)).

The one-parameter limit from R-015e and the tiny-type requirement here are 
complementary: R-015e keeps the parameter list short; R-015f makes sure that 
single parameter is self-describing. A lone `String` parameter hides what the
value actually is (ISIN? ticker? email? currency code?), and the fact that 
there is only one of it does not make it any less ambiguous.

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

## R-015g

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

## R-015h

Method and constructor parameters must not be reassigned. Treat the 
parameters as effectively final. If a different value is needed, 
introduce a new local variable with a descriptive name. This is the 
parameter-scoped companion to [R-018i](R-018-local-variable.md#r-018i).

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

## R-015i

A parameter of a domain type must be named either (a) by lowercasing the first
letter of the type name — `Currency currency`, `Isin isin`, `OrderId orderId` — or
(b) by a role name that adds semantic meaning beyond the type itself — `Money price`,
`Money total`, `User customer`. Mere synonyms of the type (`Isin identifier`,
`Currency denomination`, `Money value`, `Money amount`) are forbidden — `amount`
is reserved for the `BigDecimal` component *inside* `Money` (`money.amount()`),
so `Money amount` shadows that component and obscures the distinction between
the whole and the part. Use a qualified variant
(e.g. `sourceCurrency`, `targetCurrency`) only when two parameters of the same
type coexist and need disambiguation. This is the parameter-scoped companion
to [R-003p](R-003-class-field.md#r-003p).

**Bad:**

```java
final class TradeService {
    void save(Isin identifier) {
        // ...
    }
}
```

**Good:**

```java
final class TradeService {
    void save(Isin isin) {
        // ...
    }
}
```

**Good (disambiguation — constructor parameters are exempt from [R-015e](#r-015e)):**

```java
import static java.util.Objects.requireNonNull;

record ConversionRequest(Money total, Currency sourceCurrency, Currency targetCurrency) {
    ConversionRequest {
        requireNonNull(total);
        requireNonNull(sourceCurrency);
        requireNonNull(targetCurrency);
    }
}
```

---
