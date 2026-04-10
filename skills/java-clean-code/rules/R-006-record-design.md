## Record Design Rules

## R-006a

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

## R-006b

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

## R-006c

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

## R-006d

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

## R-006e

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

## R-006f

Object parameters in records must not be null. Validate non-null constraints in the compact canonical constructor using `Objects.requireNonNull`.

**Bad:**

```java
record UserSummary(Long id, String name) {
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

record UserSummary(Long id, String name) {
    UserSummary {
        requireNonNull(id);
        requireNonNull(name);
    }
}
```

---

