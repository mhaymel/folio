# Class Field Rules

## R-004a

Non-static fields must be private.

**Bad:**

```java
final class UserService {
    final UserId userId;
    public final UserName userName; 
}
```

**Good:**

```java
final class UserService {
    private final UserId userId;
    private final UserName userName;
}
```

---

## R-004b

Prefer final fields wherever possible. If all fields are final, consider using a record instead of a class.

**Bad:**

```java
final class UserService {
    private UserId userId;
    private UserName userName; 
}
```

**Good:**

```java
final class UserService {
    private final UserId userId;
    private final UserName userName;
}
```

---

## R-004c

Fields must not be initialized at the point of declaration. Initialize fields in the primary constructor instead (see [R-003f](R-003-class-design.md#r-003f)).

**Bad:**

```java
final class UserService {
    private UserId userId = new UserId(0);
    private final List<UserName> users = new ArrayList<>();
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

final class UserService {
    private UserId userId;
    private final List<UserName> users;

    UserService(UserId userId, List<UserName> users) { // primary constructor
        this.userId = requireNonNull(userId);
        this.users = requireNonNull(users);
    }
}
```

---

## R-004d

Classes must not have more than three non-static fields.
Introduce new classes or records to group related fields together.

**Bad:**

```java
final class UserService {
    private final UserId userId;
    private final UserName userName;
    private final Password password;
    private final Email email;
}
```

**Good:**

```java
record UserCredentials(UserName userName, Password password) {
}

final class UserService {
    private final UserId userId;
    private final UserCredentials credentials;
    private final Email email;

}
```

---

## R-004e

A class must not have unused fields.

**Bad:**

```java
final class UserService {
    private final UserId userId;
    private final UserName userName;

    UserService(UserId userId, UserName userName) {
        this.userId = requireNonNull(userId);
        this.userName = requireNonNull(userName);
    }
 
    UserName userName() {
        return userName;
    }
    // userId is not used anywhere in the class
}
```

**Good:**

```java
final class UserService {
    private final UserName userName;

    UserService(UserName userName) {
        this.userName = requireNonNull(userName);
    }

    UserName userName() {
        return userName;
    }
}
```

---

## R-004f


Non-static field names must use `lowerCamelCase`.

**Bad:**

```java
final class UserService {
    private final UserName UserName;
    private final TotalPrice TOTAL_PRICE;
}
```

**Good:**

```java
final class UserService {
    private final UserName userName;
    private final TotalPrice totalPrice;
}
```

---

## R-004g

Do not use underscores or prefixes in field names. No Hungarian notation, no `m_`, no `_` prefix or suffix.

**Bad:**

```java
final class UserService {
    private final UserName m_userName;
    private final UserId _userId;
    private final UserName strName;
}
```

**Good:**

```java
final class UserService {
    private final UserName userName;
    private final UserId userId;
    private final UserName name;
}
```

---

## R-004h

Field names must be **meaningful** and clearly describe what they hold — a reader should understand the field's purpose without looking at surrounding code.

**Bad:**

```java
final class OrderService {
    private final CustomerName s;
    private final RetryCount val;
    private final Object data;
}
```

**Good:**

```java
final class OrderService {
    private final CustomerName customerName;
    private final RetryCount retryCount;
    private final PaymentGateway paymentGateway;
}
```

---

## R-004i

Field names must be **more than one character** long. Single-letter names like `x`, `s`, `a` are forbidden.

**Bad:**

```java
final class OrderService {
    private final OrderCount n;
    private final CustomerName s;
}
```

**Good:**

```java
final class OrderService {
    private final OrderCount orderCount;
    private final CustomerName customerName;
}
```

---

## R-004j

Do not use abbreviations or acronyms in field names unless they are universally understood (e.g. `id`, `url`). Spell out the full word.

**Bad:**

```java
final class InvoiceService {
    private final Quantity qty;
    private final Configuration cfg;
    private final PortfolioManager mgr;
}
```

**Good:**

```java
final class InvoiceService {
    private final Quantity quantity;
    private final Configuration configuration;
    private final PortfolioManager manager;
}
```

---

## R-004k

Boolean fields must start with `is`, `has`, `can`, `should`, or `contains`.

**Bad:**

```java
final class UserService {
    private final boolean active;
    private final boolean permission;
    private final boolean retry;
}
```

**Good:**

```java
final class UserService {
    private final boolean isActive;
    private final boolean hasPermission;
    private final boolean canRetry;
}
```

---

## R-004l

Static fields must be `private static final`. Mutable static state (`static` without `final`) is forbidden — it introduces hidden global state, breaks thread safety, and makes testing unreliable.

**Bad:**

```java
final class UserService {
    private static int instanceCount = 0;
    static String defaultName = "Guest";
}
```

**Good:**

```java
final class UserService {
    private static final int MAX_RETRIES = 3;
    private static final String DEFAULT_NAME = "Guest";
}
```

---

## R-004m

The number of `public static final` fields (constants) in a class is not limited.

**Good:**

```java
final class HttpStatus {
    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int NOT_FOUND = 404;
    public static final int INTERNAL_SERVER_ERROR = 500;
}
```

---

## R-004n


Static final fields (constants) must use `UPPER_SNAKE_CASE` — all uppercase letters with words separated by underscores. This is the only place where uppercase letters and underscores are allowed in field names.

**Bad:**

```java
final class UserService {
    private static final int maxRetries = 3;
    private static final String defaultName = "Guest";
    private static final long TimeoutMs = 5000L;
}
```

**Good:**

```java
final class UserService {
    private static final int MAX_RETRIES = 3;
    private static final String DEFAULT_NAME = "Guest";
    private static final long TIMEOUT_MS = 5000L;
}
```

f**Note:** `UPPER_SNAKE_CASE` applies only to `static final` fields. Non-static fields must always use `lowerCamelCase` ([R-004f](#r-004f)).

