# Method Visibility Rules

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

Non-private `static` methods are forbidden. Use instance methods instead.

**Exception:** `private static` methods are allowed. They are 
invisible outside the class, so they cannot be called or mocked 
from elsewhere and introduce no hidden coupling. Use them for 
pure helpers that do not depend on instance state — marking such 
a helper `static` documents that independence and lets the compiler 
enforce it.

**Bad:**

```java
final class TaxCalculator {
    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.19);

    static Money calculateTax(Money price) {
        // ...
    }
}
```

**Good:**

```java
final class TaxCalculator {
    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.19);

    Money calculateTax(Money price) {
        // ...
    }
}
```

**Good (private static helper):**

```java
final class TaxCalculator {
    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.19);

    Money calculateTax(Money price) {
        return new Money(applyRate(price.amount()), price.currency());
    }

    private static BigDecimal applyRate(BigDecimal amount) {
        return amount.multiply(TAX_RATE);
    }
}
```

---
