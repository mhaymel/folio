# Programming by Contract

## R-009a

All parameters to public methods and constructors must be checked for nullity using `requireNonNull`. Use `requireNonNull` with one parameter only — no message string.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

final class UserService {
    private final UserRepository repository;

    UserService(UserRepository repository) {
        this.repository = repository;
    }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

final class UserService {
    private final UserRepository repository;

    UserService(UserRepository repository) {
        this.repository = requireNonNull(repository);
    }
}
```

---

## R-009b

Do not pass a message string to `requireNonNull`. Use the single-parameter form only.

**Bad:**

```java
requireNonNull(name, "name must not be null");
```

**Good:**

```java
requireNonNull(name);
```

---
