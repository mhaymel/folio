## Import Rules

## R-009a

No unused imports. Remove all unused imports from Java files. 

**Bad:**

```java
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

record Name(String value) {
    Name {
        requireNonNull(value);
    }
}

final class UserService {
    private final List<Name> names;

    UserService(List<Name> names) {
        this.names = requireNonNull(names);
    }
}
```

**Good:**

```java
import java.util.List;

import static java.util.Objects.requireNonNull;

record Name(String value) {
    Name {
        requireNonNull(value);
    }
}

final class UserService {
    private final List<Name> names;

    UserService(List<Name> names) {
        this.names = requireNonNull(names);
    }
}
```

---

## R-009b

No wildcard imports (`import java.util.*;`). 
Always use explicit, fully-qualified imports. 

**Bad:**

```java
import java.util.*;
```

**Good:**

```java
import java.util.List;
import java.util.Map;
```

---

## R-009c

Use static imports for static method calls where unambiguous (e.g. `getLogger(...)` instead of `LoggerFactory.getLogger(...)`, `requireNonNull(...)` instead of `Objects.requireNonNull(...)`). When two static-imported names collide (e.g. `List.of` vs `Map.of`), keep one qualified to avoid ambiguity.

**Bad:**

```java
import org.slf4j.LoggerFactory;

final class UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
}
```

**Good:**

```java
import static org.slf4j.LoggerFactory.getLogger;

final class UserService {
    private static final Logger LOG = getLogger(UserService.class);
}
```

---

