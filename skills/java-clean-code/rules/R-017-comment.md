# Comments

## R-017a

Commented-out code is forbidden and must be removed.

**Bad:**

```java
final class Calculator {
    int add(int a, int b) {
        return a + b;
    }

    // old implementation ΓÇö commented out, left behind
    // private int addOld(int a, int b) {
    //     // complex legacy behavior
    //     return computeLegacy(a, b);
    // }
}
```

**Good:**

```java
final class Calculator {
    int add(int a, int b) {
        return a + b;
    }
}
```

---

## R-017b

If a comment merely restates code, refactor the code to be self-documenting.

**Bad:**

```java
final class User {
    // increment login attempts by one
    void increaseAttempts() {
        attempts = attempts + 1; // comment repeats the code
    }
}
```

**Good:**

```java
final class User {
    void incrementLoginAttempts() {
        attempts = attempts + 1;
    }
}
```

---

## R-017c

Avoid comments that explain obviously readable code. instead prefer clearer identifiers and small methods.

**Bad:**

```java
// compute the total price including tax
BigDecimal total = price.add(price.multiply(taxRate));
```

**Good:**

```java
BigDecimal total = priceWithTax(price, taxRate);

private BigDecimal priceWithTax(BigDecimal price, BigDecimal taxRate) {
    return price.add(price.multiply(taxRate));
}
```

---

## R-017d

Document non-obvious uses such as reflective access, framework requirements, or suppression of tools. When a method or field must remain for framework reasons, add a short comment explaining why and, if applicable, reference the framework or a ticket.

**Example (framework exception):**

```java
final class UserEntity {
    private String name;

    // Required by JPA for reflective instantiation
    @SuppressWarnings("unused")
    void setName(String name) {
        this.name = name;
    }
}
```

---

## R-017e

Use TODO and WARNING comments sparingly and include context and tracking information (ticket id or rationale). TODOs should be actionable and short-lived.

**Bad:**

```java
// TODO: fix later
void risky() {
    // ...
}
```

**Good:**

```java
// TODO(PROJ-1234): replace this in-memory cache with Redis to support clustering
private final Map<String, Value> cache = new ConcurrentHashMap<>();
```
