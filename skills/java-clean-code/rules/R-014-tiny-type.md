# Tiny Type Rules

## R-014a

Tiny types must be implemented as Java records.

**Bad:**

```java
final class Isin {
    private final String value;

    Isin(String value) {
        this.value = value;
    }

    String value() {
        return value;
    }
}
```

**Good:**

```java
import static com.dynatrace.deus.util.preconditions.Preconditions.requireNotEmpty;

record Isin(String value) {
    Isin {
        requireNotEmpty(value);
    }
}
```

---

## R-014b

A tiny type must wrap exactly one value. If more than one field is needed, it is no longer a tiny type ΓÇö model it as a regular record or class instead.

**Bad:**

```java
record Money(BigDecimal amount, String currency) {
}
```

**Good:**

```java
import static com.dynatrace.deus.util.preconditions.Preconditions.requireNotEmpty;

record Currency(String value) {
    Currency {
        requireNotEmpty(value);
    }
}

record Amount(BigDecimal value) {
}
```

---

## R-014c

The wrapped field must be named `value`.

**Bad:**

```java
record Isin(String isin) {
}

record PortfolioId(long id) {
}
```

**Good:**

```java
import static com.dynatrace.deus.util.preconditions.Preconditions.requireNotEmpty;

record Isin(String value) {
    Isin {
        requireNotEmpty(value);
    }
}

record PortfolioId(long value) {
}
```

---

## R-014d

A tiny type must validate its value in a compact constructor. Reject `null` and any domain-invalid state at construction time. This ensures that once a tiny type instance exists, it is always valid.

**Bad:**

```java
import static com.dynatrace.deus.util.preconditions.Preconditions.requireNotEmpty;

record Isin(String value) {
    Isin {
        requireNotEmpty(value);
    }
}
```

**Good:**

```java
import static com.dynatrace.deus.util.preconditions.Preconditions.requireNotEmpty;

record Isin(String value) {
    Isin {
        requireNotEmpty(value);
    }
}
```

---

## R-014e

A tiny type must not contain business logic. Its only responsibilities are holding a value and validating it at construction. Behavior that operates on the value belongs in a service or domain object, not in the tiny type itself.

**Bad:**

```java
import static com.dynatrace.deus.util.preconditions.Preconditions.requireNotEmpty;

record Isin(String value) {
    Isin {
        requireNotEmpty(value);
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
import static com.dynatrace.deus.util.preconditions.Preconditions.requireNotEmpty;

record Isin(String value) {
    Isin {
        requireNotEmpty(value);
    }
}
```

---

## R-014f

The compact constructor must only contain precondition checks (`if` + `throw`). It must not contain logging, side effects, or value transformation. Store the value exactly as received.

**Bad:**

```java
import static com.dynatrace.deus.util.preconditions.Preconditions.requireNotEmpty;

record Isin(String value) {
    Isin {
        requireNotEmpty(value);
        value = value.toUpperCase().trim();
        log.info("Created ISIN: " + value);
    }
}
```

**Good:**

```java
import static com.dynatrace.deus.util.preconditions.Preconditions.requireNotEmpty;

record Isin(String value) {
    Isin {
        requireNotEmpty(value);
    }
}
```

---

## R-014g

Do not override `equals`, `hashCode`, or `toString` on a tiny type. The record-generated implementations based on the wrapped value are correct and sufficient.

**Bad:**

```java
import static com.dynatrace.deus.util.preconditions.Preconditions.requireNotEmpty;

record Isin(String value) {
    Isin {
        requireNotEmpty(value);
    }

    @Override
    public String toString() {
        return "ISIN[" + value + "]";
    }
}
```

**Good:**

```java
import static com.dynatrace.deus.util.preconditions.Preconditions.requireNotEmpty;

record Isin(String value) {
    Isin {
        requireNotEmpty(value);
    }
}
```

---

## R-014h

A tiny type must not implement any interface. Tiny types are pure value wrappers, not polymorphic abstractions. If polymorphism is needed, use a regular record or class.

**Bad:**

```java
import static com.dynatrace.deus.util.preconditions.Preconditions.requireNotEmpty;

record Isin(String value) implements Identifier {
    Isin {
        requireNotEmpty(value);
    }
}
```

**Good:**

```java
import static com.dynatrace.deus.util.preconditions.Preconditions.requireNotEmpty;

record Isin(String value) {
    Isin {
        requireNotEmpty(value);
    }
}
```

---

## R-014i

Every domain concept that is represented as a primitive (`String`, `int`, `long`, `BigDecimal`, etc.) must have its own dedicated tiny type. Do not reuse the same tiny type for different concepts even if the underlying primitive type is the same.

**Bad:**

```java
import static com.dynatrace.deus.util.preconditions.Preconditions.requireNotEmpty;

record Identifier(String value) {
    Identifier {
        requireNotEmpty(value);
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
import static com.dynatrace.deus.util.preconditions.Preconditions.requireNotEmpty;

record Isin(String value) {
    Isin {
        requireNotEmpty(value);
    }
}

record PortfolioName(String value) {
    PortfolioName {
        requireNotEmpty(value);
    }
}

final class Portfolio {
    private final Isin isin;
    private final PortfolioName name;
}
```

---

