## Record Design Rules

## R-007a

Prefer records to immutable classes.

**Bad:**

```java
final class UserSummary {
    private final long id;
    private final String name;
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

record UserSummary(long id, String name) {
    UserSummary {
        requireNonNull(name);
    }
}
```

---

## R-007b

Records must be package-private by default. Make records public only if they must be accessed outside.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

public record UserSummary(long id, String name) {
    UserSummary {
        requireNonNull(name);
    }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

record Name(String value) {
    Name {
        requireNonNull(value);
    }
}

record UserSummary(long id, Name name) {
    UserSummary {
        requireNonNull(name);
    }
}
```

---

## R-007c

Nested records are forbidden. Use top-level records instead.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

final class UserService {
    record Credentials(String userName, String password) {
        Credentials {
            requireNonNull(userName);
            requireNonNull(password);
        }
    }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

record Username(String value) {
    Username {
        requireNonNull(value);
    }
}

record Password(String value) {
    Password {
        requireNonNull(value);
    }
}

record Credentials(Username userName, Password password) {
    Credentials {
        requireNonNull(userName);
        requireNonNull(password);
    }
}

final class UserService {
}
```

## R-007d

Keep record fields to at most 3. Split larger records into smaller ones.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

record UserAccount(int userId, String userName, String password, Email email) {
    UserAccount {
        requireNonNull(userName);
        requireNonNull(password);
        requireNonNull(email);
    }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

record UserCredentials(String userName, String password) {
    UserCredentials {
        requireNonNull(userName);
        requireNonNull(password);
    }
}

record UserService(int userId, UserCredentials credentials, Email email) {
    UserService {
        requireNonNull(credentials);
        requireNonNull(email);
    }
}
```

---

## R-007e

Records do not need builders; use the canonical constructor directly.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

record UserSummary(long id, String name) {
    UserSummary {
        requireNonNull(name);
    }
}

final class UserSummaryBuilder {
    private long id;
    private String name;

    UserSummaryBuilder id(long id) { this.id = id; return this; }
    UserSummaryBuilder name(String name) { this.name = requireNonNull(name); return this; }
    UserSummary build() { return new UserSummary(id, name); }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

record Name(String value) {
    Name {
        requireNonNull(value);
    }
}

record UserSummary(long id, Name name) {
    UserSummary {
        requireNonNull(name);
    }
}

// usage:
var summary = new UserSummary(1L, new Name("Alice"));
```

---

## R-007f

Object parameters in records must not be null. Validate non-null constraints in the compact canonical constructor using `Objects.requireNonNull`.

**Bad:**

```java
record UserSummary(String name, Instant createdAt) {
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

record Name(String value) {
    Name {
        requireNonNull(value);
    }
}

record UserSummary(Name name, Instant createdAt) {
    UserSummary {
        requireNonNull(name);
        requireNonNull(createdAt);
    }
}
```

---

## R-007g

All components of a record must themselves be immutable. 
Using mutable types such as `List`, `Map`, `Set`, `Date`, or arrays breaks 
the immutability guarantee of the record. 
Use unmodifiable or immutable alternatives instead, and defensively copy 
in the compact canonical constructor.

> **Note:** This rule applies recursively. `List.copyOf()` makes the list 
> itself unmodifiable, but if the elements inside it are mutable objects, 
> those elements can still be mutated. Every component, and every element 
> within a component, must be immutable (e.g. `List<String>` is safe 
> because `String` is immutable; `List<Date>` is not).

**Bad:**

```java
import static java.util.Objects.requireNonNull;

record Portfolio(String name, List<String> stocks) {
    Portfolio {
        requireNonNull(name);
        requireNonNull(stocks);
    }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

record PortfolioName(String value) {
    PortfolioName {
        requireNonNull(value);
    }
}

record StockSymbol(String value) {
    StockSymbol {
        requireNonNull(value);
    }
}

record Portfolio(PortfolioName name, List<StockSymbol> stocks) {
    Portfolio {
        requireNonNull(name);
        stocks = List.copyOf(requireNonNull(stocks));
    }
}
```

**Bad:**

```java
import static java.util.Objects.requireNonNull;

record Snapshot(String label, Date takenAt) {
    Snapshot {
        requireNonNull(label);
        requireNonNull(takenAt);
    }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

record Label(String value) {
    Label {
        requireNonNull(value);
    }
}

record Snapshot(Label label, Instant takenAt) {
    Snapshot {
        requireNonNull(label);
        requireNonNull(takenAt);
    }
}
```

---

## R-007h

Compose null-checks and defensive copies into a single expression rather than writing them as separate statements. Pipe the component through `requireNonNull` directly into `List.copyOf` (or any other wrapper). The result reads as a single transformation — "normalize this component" — instead of two sequentially coupled side-effects on the same name.

**Bad:**

```java
record DashboardLists(List<HoldingDto> top5Holdings, List<DividendSourceDto> top5DividendSources) {
    DashboardLists {
        requireNonNull(top5Holdings);
        requireNonNull(top5DividendSources);
        top5Holdings = List.copyOf(top5Holdings);
        top5DividendSources = List.copyOf(top5DividendSources);
    }
}
```

**Good:**

```java
record DashboardLists(List<HoldingDto> top5Holdings, List<DividendSourceDto> top5DividendSources) {
    DashboardLists {
        top5Holdings = List.copyOf(requireNonNull(top5Holdings));
        top5DividendSources = List.copyOf(requireNonNull(top5DividendSources));
    }
}
```

---

## R-007i

Boxed primitive types (`Integer`, `Long`, `Short`, `Byte`, `Float`, `Double`, `Boolean`, `Character`) must not be used as record components. Use the corresponding primitive (`int`, `long`, etc.) instead.

**Bad:**

```java
record UserSummary(Long id, Integer age, Boolean active) {
}
```

**Good:**

```java
record UserSummary(long id, int age, boolean isActive) {
}
```

**Good (with tiny types):**

```java
import static java.util.Objects.requireNonNull;

record UserId(long value) {
}

record Age(int value) {
}

record UserSummary(UserId id, Age age, boolean isActive) {
    UserSummary {
        requireNonNull(id);
        requireNonNull(age);
    }
}
```

---

## R-007j

Declare record components with the most general interface. Never use a concrete type in the header. For mutable inputs, combine with R-007g and copy through an unmodifiable wrapper.

| Use (interface) | Not (concrete) |
|----|----|
| `List`, `Set`, `Map`, `Collection`, `Iterable` | `ArrayList`, `HashSet`, `HashMap`, `LinkedHashMap`, `TreeMap` |
| `Executor`, `ExecutorService` | `ThreadPoolExecutor` |
| `InputStream`, `Reader` | `FileInputStream`, `BufferedReader` |
| `Path` | `File` |
| `Clock` | concrete clock |

**Bad:**

```java
import static java.util.Objects.requireNonNull;

record Portfolio(String name,
                 ArrayList<String> stocks,
                 HashMap<String, Double> weights) {
    Portfolio {
        requireNonNull(name);
        stocks = List.copyOf(requireNonNull(stocks));
        weights = Map.copyOf(requireNonNull(weights));
    }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

record PortfolioName(String value) {
    PortfolioName {
        requireNonNull(value);
    }
}

record StockSymbol(String value) {
    StockSymbol {
        requireNonNull(value);
    }
}

record Weight(double value) {
}

record Portfolio(PortfolioName name,
                 List<StockSymbol> stocks,
                 Map<StockSymbol, Weight> weights) {
    Portfolio {
        requireNonNull(name);
        stocks = List.copyOf(requireNonNull(stocks));
        weights = Map.copyOf(requireNonNull(weights));
    }
}
```

---

