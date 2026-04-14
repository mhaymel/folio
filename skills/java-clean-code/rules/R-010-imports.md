## Import Rules

## R-010a

No unused imports. Remove all unused imports from Java files. IDEs (IntelliJ, VS Code) provide quick-fix commands to clean imports — use them before committing code.

**Bad:**

```java
import java.util.List;
import java.util.Map;

final class UserService {
    private final List<String> names = List.of("Alice");
}
```

**Good:**

```java
import java.util.List;

final class UserService {
    private final List<String> names = List.of("Alice");
}
```

---

## R-010b

No wildcard imports (`import java.util.*;`). Always use explicit, fully-qualified imports. This makes dependencies clear and avoids name collisions. IDEs can auto-organize imports — use them.

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

## R-010c

Use static imports for static method calls where unambiguous (e.g. `getLogger(…)` instead of `LoggerFactory.getLogger(…)`, `requireNonNull(…)` instead of `Objects.requireNonNull(…)`). When two static-imported names collide (e.g. `List.of` vs `Map.of`), keep one qualified to avoid ambiguity.

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

