# Class Design Rules

> **Note on examples:** the Good blocks in this file deliberately use classes to illustrate class-scoped rules. In practice, a `final class` whose fields are all `final` and which has no behavior beyond getters should be a record — see [R-007a](R-007-record-design.md#r-007a).

## R-002a

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

## R-002b

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

## R-002c

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

## R-002d

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

## R-002e

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

## R-002f

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

## R-002g

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

## R-002h

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

## R-002i

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

## R-002j

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

## R-002k

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

record UserCredentials(UserName userName, Password password) {
    UserCredentials {
        requireNonNull(userName);
        requireNonNull(password);
    }
}

final class UserService {
}
```

---

## R-002l

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

## R-002m

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

## R-002n

Do not inherit from concrete classes. Only extend interfaces (or implement them).

**Bad:**

```java
final class AdminUser extends User {
}
```

**Good:**

```java
interface Identifiable {
    long id();
}

final class AdminUser implements Identifiable {
    @Override
    public long id() {
        return id;
    }
}
```

---

## R-002o

Methods must have one or none parameters. If more are needed, 
introduce a parameter object or rethink the design.

**Bad:**

```java
final class OrderService {
    void createOrder(String product, int quantity, BigDecimal price, String currency) {
    }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

record OrderRequest(Product product, int quantity, Money price) {
    OrderRequest {
        requireNonNull(product);
        requireNonNull(price);
    }
}

final class OrderService {
    void createOrder(OrderRequest request) {
        requireNonNull(request);
    }
}
```

---

## R-002p

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


## R-002r

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

## R-002s

Avoid primitive obsession in fields and constructor parameters. 
When a field represents a domain concept, use a dedicated 
tiny type (record) instead of a raw primitive (`String`, `int`, 
`long`, `BigDecimal`, etc.). 

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
    Isin {
        requireNonNull(value);
    }
}

record PortfolioName(String value) {
    PortfolioName {
        requireNonNull(value);
    }
}

record Money(BigDecimal amount, Currency currency) {
    Money {
        requireNonNull(amount);
        requireNonNull(currency);
    }
}

record Portfolio(Isin isin, PortfolioName name, Money marketValue) {
    Portfolio {
        requireNonNull(isin);
        requireNonNull(name);
        requireNonNull(marketValue);
    }
}
```

---

## R-002t

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

