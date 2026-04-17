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
record UserSummary(long id, String name) {
}
```

---

## R-007b

Records must be package-private by default. Make records public only if they must be accessed outside.

**Bad:**

```java
public record UserSummary(long id, String name) {
}
```

**Good:**

```java
record UserSummary(long id, String name) {
}
```

---

## R-007c

Nested records must not be used. Use top-level records instead.

Java records nested inside a class are implicitly `static`, but nesting them still increases coupling and hides types. Declare them at the top level.

**Bad:**

```java
final class UserService {
    record Credentials(String userName, String password) {
    }
}
```

**Good:**

```java
record Credentials(String userName, String password) {
}

final class UserService {
}
```

## R-007d

Consider keeping the number of fields in a record to rather low eg. 3 fields. If a record has more than 3 fields, it may indicate that the record is modeling too much data and should be split into multiple records.

**Bad:**

```java
final record UserService {
    private final int userId;
    private final String userName;
    private final String password;
    private final String email;
}
```

**Good:**

```java
record UserCredentials(String userName, String password) {
}

final record UserService {
    private final int userId;
    private final UserCredentials credentials;
    private final String email;

}
```

---

## R-007e

Records do not need builders; use the canonical constructor directly.

**Bad:**

```java
record UserSummary(long id, String name) {
}

final class UserSummaryBuilder {
    private long id;
    private String name;

    UserSummaryBuilder id(long id) { this.id = id; return this; }
    UserSummaryBuilder name(String name) { this.name = name; return this; }
    UserSummary build() { return new UserSummary(id, name); }
}
```

**Good:**

```java
record UserSummary(long id, String name) {
}

// usage:
var summary = new UserSummary(1L, "Alice");
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

record UserSummary(String name, Instant createdAt) {
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
record Portfolio(String name, List<String> stocks) {
}
```

**Good:**

```java
record Portfolio(String name, List<String> stocks) {
    Portfolio {
        requireNonNull(name);
        stocks = List.copyOf(requireNonNull(stocks));
    }
}
```

**Bad:**

```java
record Snapshot(String label, Date takenAt) {
}
```

**Good:**

```java
record Snapshot(String label, Instant takenAt) {
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

Boxed primitive types (`Integer`, `Long`, `Short`, `Byte`, `Float`, `Double`, `Boolean`, `Character`) must not be used as record components. Use the corresponding primitive (`int`, `long`, etc.) instead. Boxed types introduce unnecessary allocation, allow accidental `null` values, and hide the intent that the component holds a simple scalar. If a domain meaning is attached to the value, wrap the primitive in a dedicated tiny type (see R-014).

**Bad:**

```java
record UserSummary(Long id, Integer age, Boolean active) {
}
```

**Good:**

```java
record UserSummary(long id, int age, boolean active) {
}
```

**Good (with tiny types):**

```java
record UserId(long value) {
}

record UserSummary(UserId id, int age, boolean active) {
}
```

---

