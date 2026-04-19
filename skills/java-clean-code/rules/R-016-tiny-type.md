# Tiny Type Rules

## R-016a

Tiny types must be implemented as Java records.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

final class Isin {
    private final String value;

    Isin(String value) {
        this.value = requireNonNull(value);
    }

    String value() {
        return value;
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
```

---

## R-016b

A tiny type must wrap exactly one value. If more than one field is needed, 
it is no longer a tiny type - model it as a regular record or class instead.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

record Money(BigDecimal amount, String currency) {
    Money {
        requireNonNull(amount);
        requireNonNull(currency);
    }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

record Currency(String value) {
    Currency {
        requireNonNull(value);
    }
}

record Amount(BigDecimal value) {
    Amount {
        requireNonNull(value);
    }
}

record Money(Amount amount, Currency currency) {
    Money {
        requireNonNull(amount);
        requireNonNull(currency);
    }
}
```

---

## R-016c

The wrapped field must be named `value`.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

record Isin(String isin) {
    Isin {
        requireNonNull(isin);
    }
}

record PortfolioId(long id) {
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

record PortfolioId(long value) {
}
```

---

## R-016d

A tiny type must validate its value in a compact constructor. Reject `null` 
and any domain-invalid state at construction time. This ensures that once 
a tiny type instance exists, it is always valid.

**Bad:**

```java
record Isin(String value) {
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
```

---

## R-016e

A tiny type must not contain business logic. Its only responsibilities are holding a value and validating it at construction. Behavior that operates on the value belongs in a service or domain object, not in the tiny type itself.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

record Isin(String value) {
    Isin {
        requireNonNull(value);
    }

    String exchange() {
        return value.substring(0, 2);
    }

    boolean isUsListed() {
        return value.startsWith("US");
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
```

---

## R-016f

The compact constructor must only contain precondition checks (`if` + `throw`). It must not contain logging, side effects, or value transformation. Store the value exactly as received.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

record Isin(String value) {
    Isin {
        requireNonNull(value);
        value = value.toUpperCase().trim();
        log.info("Created ISIN: " + value);
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
```

---

## R-016g

Do not override `equals`, `hashCode`, or `toString` on a tiny type. The record-generated implementations based on the wrapped value are correct and sufficient.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

record Isin(String value) {
    Isin {
        requireNonNull(value);
    }

    @Override
    public String toString() {
        return "ISIN[" + value + "]";
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
```

---

## R-016h

A tiny type must not implement any interface. Tiny types are pure value wrappers, not polymorphic abstractions. If polymorphism is needed, use a regular record or class.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

record Isin(String value) implements Identifier {
    Isin {
        requireNonNull(value);
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
```

---

## R-016i

Every domain concept that is represented as a primitive (`String`, `int`, `long`, `BigDecimal`, etc.) must have its own dedicated tiny type. Do not reuse the same tiny type for different concepts even if the underlying primitive type is the same.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

record Identifier(String value) {
    Identifier {
        requireNonNull(value);
    }
}

// used for both ISIN and portfolio name
final class Portfolio {
    private final Identifier isin;
    private final Identifier name;
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

record PortfolioName(String value) {
    PortfolioName {
        requireNonNull(value);
    }
}

final class Portfolio {
    private final Isin isin;
    private final PortfolioName name;
}
```

---

