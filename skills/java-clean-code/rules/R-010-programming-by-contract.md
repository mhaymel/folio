# Programming by Contract

## R-010a

All parameters to public methods and constructors must be checked for nullity using `requireNonNull`. 
Use `requireNonNull` with one parameter only — no message string.

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

## R-010b

Do not pass a message string to `requireNonNull`. Use the single-parameter form only.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

final class Example {
    void greet(String name) {
        requireNonNull(name, "name must not be null");
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

final class Greeter {
    void greet(Name name) {
        requireNonNull(name);
    }
}
```

---

## R-010c

`requireNonNull` on parameters of private methods is forbidden.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

final class UserProcessor {
    private String normalizeName(String name) {
        // forbidden: private method performing requireNonNull
        name = requireNonNull(name);
        return name.trim();
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

final class UserProcessor {
    private final Name name;

    UserProcessor(Name name) {
        // validate at public boundary
        this.name = requireNonNull(name);
    }

    private Name normalizeName(Name name) {
        // private helper assumes callers validated nullity
        return new Name(name.value().trim());
    }
}
```

---



