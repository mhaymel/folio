# Class Design Rules

## R-003a

Classes must be declared as `final`.

**Exception:** classes that Spring needs to CGLIB-proxy must **not** be `final`. This includes classes annotated with `@Configuration`, `@SpringBootApplication`, and any Spring bean whose methods are annotated with `@Transactional` (or other AOP-proxied annotations such as `@Cacheable`, `@Async`, etc.).

**Bad:**

```java
class UserService {
    void saveUser(User user) {
        // ...
    }
}
```

**Good:**

```java
final class UserService {
    void saveUser(User user) {
        // ...
    }
}
```

---

## R-003b

Classes must be package-private. Make classes public only if they must be accessed outside the package.

**Bad:**

```java
public final class UserService {
}
```

**Good:**

```java
final class UserService {
}
```

---

## R-003c

Classes must not be abstract. Use interfaces and composition instead.

**Bad:**

```java
abstract class DataProcessor {
    abstract void process(Data data);
}

final class UserDataProcessor extends DataProcessor {
    @Override
    void process(Data data) {
        // implementation
    }
}
```

**Good:**

```java
interface Processor {
    void process(Data data);
}

final class UserDataProcessor implements Processor {
    @Override
    public void process(Data data) {
        // implementation
    }
}
```

---

## R-003d

Methods must be package-private by default. Make methods public only if they must be accessed outside the package.

**Bad:**

```java
final class UserService {
    public void saveUser(User user) { 
    }
}
```

**Good:**

```java
final class UserService {
    void saveUser(User user) {
    }
}
```

---

## R-003e

Prefer immutable classes wherever possible.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

final class UserService {
    private final UserId userId;
    private UserName userName; 
    private Password password;

    UserService(UserId userId, UserName userName, Password password) {
        this.userId = requireNonNull(userId);
        this.userName = requireNonNull(userName);
        this.password = requireNonNull(password);
    }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

final class UserService {
    private final UserId userId;
    private final UserName userName;
    private final Password password;

    UserService(UserId userId, UserName userName, Password password) {
        this.userId = requireNonNull(userId);
        this.userName = requireNonNull(userName);
        this.password = requireNonNull(password);
    }
}
```

---

## R-003f

Classes must have one primary constructor that initializes all non-static fields. The primary constructor must declare one parameter for each non-static field.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

final class UserService {
    private final UserId userId;
    private final UserName userName;

    UserService(UserName userName) {
        this.userId = new UserId(0);
        this.userName = requireNonNull(userName);
    }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

final class UserService {
    private final UserId userId;
    private final UserName userName;

    UserService(UserId userId, UserName userName) {
        this.userId = requireNonNull(userId);
        this.userName = requireNonNull(userName);
    }
}
```

---

## R-003g

Secondary constructors must delegate to the primary constructor by using `this(...)`. They must not initialize fields or contain field-initialization logic. Their purpose is to provide default values for omitted parameters.

**Spring note:** When a class has multiple constructors, Spring cannot auto-detect which one to use for dependency injection. Annotate the secondary (injection) constructor with `@Autowired` so Spring selects it.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

final class UserService {
    private final UserId userId;
    private final UserName userName; 
    private final Password password;

    UserService(UserId userId, UserName userName, Password password) {
        this.userId = requireNonNull(userId);
        this.userName = requireNonNull(userName);
        this.password = requireNonNull(password);
    }

    UserService(UserId userId) {
        this.userId = requireNonNull(userId);
        this.userName = new UserName("User1");
        this.password = new Password("Password1");
    }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

final class UserService {
    private final UserId userId;
    private final UserName userName;
    private final Password password;

    private UserService(UserId userId, UserName userName, Password password) {
        this.userId = requireNonNull(userId);
        this.userName = requireNonNull(userName);
        this.password = requireNonNull(password);
    }

    UserService(UserId userId) {
        this(userId, new UserName("User1"), new Password("Password1"));
    }
}
```

---

## R-003h

A secondary constructor must not have more parameters than the primary constructor.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

final class UserService {
    private final UserName userName;

    private UserService(UserName userName) {
        this.userName = requireNonNull(userName);
    }

    UserService(UserName userName, Password password) { // more params than primary
        this(userName);
    }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

final class UserService {
    private final UserName userName;

    UserService(UserName userName) {
        this.userName = requireNonNull(userName);
    }
}
```

---

## R-003i

When the primary constructor only exists for internal delegation (i.e. some fields are internal state never set from outside), make the primary constructor `private`.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

final class UserService {
    private final UserId userId;
    private final UserName userName;

    UserService(UserId userId, UserName userName) { // exposes internal state
        this.userId = requireNonNull(userId);
        this.userName = requireNonNull(userName);
    }

    UserService(UserId userId) {
        this(userId, new UserName("User1"));
    }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

final class UserService {
    private final UserId userId;
    private final UserName userName;

    private UserService(UserId userId, UserName userName) { // private — userName is internal state
        this.userId = requireNonNull(userId);
        this.userName = requireNonNull(userName);
    }

    UserService(UserId userId) {
        this(userId, new UserName("User1"));
    }
}
```

---

## R-003j

Constructors must be free of code except for precondition checks.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

final class UserService {
    private final UserId userId;
    private final UserName userName; 
    private final Password password;

    UserService(UserId userId, UserName userName, Password password) {
        this.userId = requireNonNull(userId);
        this.userName = requireNonNull(userName);
        this.password = requireNonNull(password);
        // some additional code
        log.info("UserService created for user: " + userName);
    }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

final class UserService {
    private final UserId userId;
    private final UserName userName; 
    private final Password password;

    UserService(UserId userId, UserName userName, Password password) {
        this.userId = requireNonNull(userId);
        this.userName = requireNonNull(userName);
        this.password = requireNonNull(password);
    }
}
```

---

## R-003k

Inner classes (static and non-static) must not be used. Extract every nested type to its own top-level file.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

final class UserService {
    final class UserCredentials {
        private final UserName userName;
        private final Password password;

        UserCredentials(UserName userName, Password password) {
            this.userName = requireNonNull(userName);
            this.password = requireNonNull(password);
        }
    }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

final class UserCredentials {
    private final UserName userName;
    private final Password password;

    UserCredentials(UserName userName, Password password) {
        this.userName = requireNonNull(userName);
        this.password = requireNonNull(password);
    }
}

final class UserService {
}
```

---

## R-003l

Enums must not be declared as inner types. Declare enums at the top level.

**Bad:**

```java
final class UserService {
    enum UserRole {
        ADMIN,
        USER,
        GUEST
    }
}
```

**Good:**

```java
enum UserRole {
    ADMIN,
    USER,
    GUEST
}

final class UserService {
}
```

---

## R-003m

A constructor must not have unused parameters.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

final class UserService {
    private final UserName userName;

    UserService(UserId userId, UserName userName) {
        this.userName = requireNonNull(userName);
    }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

final class UserService {
    private final UserName userName;

    UserService(UserName userName) {
        this.userName = requireNonNull(userName);
    }
}
```

---

## R-003n

Do not inherit from concrete classes. Only extend interfaces (or implement them).

**Bad:**

```java
final class AdminUser extends User {
}
```

**Good:**

```java
interface Identifiable {
    long getId();
}

final class AdminUser implements Identifiable {
    @Override
    public long getId() {
        return id;
    }
}
```

---

## R-003o

Methods must have at most 3 parameters; prefer 0 or 1. If more are needed, introduce a parameter object or rethink the design.

**Bad:**

```java
final class OrderService {
    void createOrder(String product, int quantity, BigDecimal price, String currency) {
    }
}
```

**Good:**

```java
record OrderRequest(String product, int quantity, BigDecimal price) {
}

final class OrderService {
    void createOrder(OrderRequest request) {
    }
}
```

---

## R-003p

Do not use underscores in method names. Use `lowerCamelCase` exclusively. This applies to all methods, including unit test methods.

**Bad:**

```java
final class UserValidatorTest {
    void should_accept_non_empty_array() {
    }
}
```

**Good:**

```java
final class UserValidatorTest {
    void shouldAcceptNonEmptyArray() {
    }
}
```

---

## R-003q

Do not use `continue` or `break` in loops. Extract the loop body to a private method and use `return` instead of `continue`. Do not invert the condition; do not add nesting.

**Bad:**

```java
final class OrderProcessor {
    void processOrders(List<Order> orders) {
        for (Order order : orders) {
            if (order.isCancelled()) {
                continue;
            }
            ship(order);
        }
    }
}
```

**Good:**

```java
final class OrderProcessor {
    void processOrders(List<Order> orders) {
        for (Order order : orders) {
            processOrder(order);
        }
    }

    private void processOrder(Order order) {
        if (order.isCancelled()) {
            return;
        }
        ship(order);
    }
}
```

---

## R-003r

Builders are forbidden. Use the constructor directly.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

final class UserService {
    private final UserId userId;
    private final UserName userName;

    UserService(UserId userId, UserName userName) {
        this.userId = requireNonNull(userId);
        this.userName = requireNonNull(userName);
    }
}

final class UserServiceBuilder {
    private UserId userId;
    private UserName userName;

    UserServiceBuilder userId(UserId userId) { this.userId = userId; return this; }
    UserServiceBuilder userName(UserName userName) { this.userName = userName; return this; }
    UserService build() { return new UserService(userId, userName); }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

final class UserService {
    private final UserId userId;
    private final UserName userName;

    UserService(UserId userId, UserName userName) {
        this.userId = requireNonNull(userId);
        this.userName = requireNonNull(userName);
    }
}

// usage:
var service = new UserService(new UserId(1), new UserName("Alice"));
```

---

## R-003s

Avoid primitive obsession in fields and constructor parameters. When a field represents a domain concept, use a dedicated tiny type (record) instead of a raw primitive (`String`, `int`, `long`, `BigDecimal`, etc.). This makes the code self-documenting, prevents accidental misuse (e.g. swapping two `String` fields), and pushes validation into the type itself.

**Bad:**

```java
final class Portfolio {
    private final String isin;
    private final String name;
    private final BigDecimal marketValue;

    Portfolio(String isin, String name, BigDecimal marketValue) {
        this.isin = isin;
        this.name = name;
        this.marketValue = marketValue;
    }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

record Isin(String value) {
}

record PortfolioName(String value) {
}

record Money(BigDecimal amount, Currency currency) {
}

final class Portfolio {
    private final Isin isin;
    private final PortfolioName name;
    private final Money marketValue;

    Portfolio(Isin isin, PortfolioName name, Money marketValue) {
        this.isin = requireNonNull(isin);
        this.name = requireNonNull(name);
        this.marketValue = requireNonNull(marketValue);
    }
}
```

---

## R-003t

A class without fields is allowed when the class exists solely to implement an interface or to group behavior. Such classes do not need a constructor.

**Good:**

```java
interface OrderValidator {
    boolean isValid(Order order);
}

final class DefaultOrderValidator implements OrderValidator {
    @Override
    public boolean isValid(Order order) {
        return order.totalAmount().compareTo(BigDecimal.ZERO) > 0;
    }
}
```

---

