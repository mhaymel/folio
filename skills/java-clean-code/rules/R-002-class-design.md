# Class Design Rules

## R-002a

Classes must be declared as `final` — this prevents unintended subclassing and makes the design explicit.

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

Non-static fields must be private.

**Bad:**

```java
final class UserService {
    final int userId;
    public final String userName; 
}
```

**Good:**

```java
final class UserService {
    private final int userId;
    private final String userName;
}
```

---

## R-002f

Prefer final fields wherever possible.

**Bad:**

```java
final class UserService {
    private int userId;
    private String userName; 
}
```

**Good:**

```java
final class UserService {
    private final int userId;
    private final String userName;
}
```

---

## R-002g

Prefer immutable classes wherever possible.

**Bad:**

```java
final class UserService {
    private final int userId;
    private String userName; 
    private String password;
}
```

**Good:**

```java
final class UserService {
    private final int userId;
    private final String userName;
    private final String password;
}
```

---

## R-002h

Classes must have one primary constructor that initializes all non-static fields.

Secondary constructors are allowed, but they must delegate to the primary 
constructor by using `this(...)`.
They must not initialize fields or contain field-initialization logic. 
Their purpose is to provide default values for omitted parameters.

The primary constructor must declare one parameter for each non-static field.
The primary and secondary constructors may be package-private, private, or public.
When the primary constructor only exists for internal delegation (i.e. some fields
are internal state never set from outside), make the primary constructor `private`.

**Spring note:** When a class has multiple constructors, Spring cannot auto-detect
which one to use for dependency injection. Annotate the secondary (injection)
constructor with `@Autowired` so Spring selects it.


**Bad:**

```java
final class UserService {
    private final int userId;
    private String userName; 
    private String password;

    UserService(int userId, String userName, String password) {
        this.userId = userId;
        this.userName = userName;
        this.password = password;
    }

    UserService(int userId) {
        this.userId = userId;
        this.userName = "User1";
        this.password = "Password1";
    }
}
```

**Good:**

```java
final class UserService {
    private final int userId;
    private String userName;
    private String password;

    private UserService(int userId, String userName, String password) { // primary constructor (private — userName/password are internal state)
        this.userId = userId;
        this.userName = userName;
        this.password = password;
    }

    UserService(int userId) {
        this(userId, "User1", "Password1");
    }
}
```

---

## R-002i

Fields must not be initialized at the point of declaration.

**Bad:**

```java
final class UserService {
    private int userId = 0;
    private final List<String> users = new ArrayList<>();
}
```

**Good:**

```java
final class UserService {
    private int userId;
    private final List<String> users;

    UserService(int userId, List<String> users) { // primary constructor
        this.userId = userId;
        this.users = users;
    }
}
```

---

## R-002j

Constructors must be free of code except for precondition checks.

**Bad:**

```java
final class UserService {
    private final int userId;
    private String userName; 
    private String password;

    UserService(int userId, String userName, String password) {
        this.userId = userId;
        this.userName = userName;
        this.password = password;
        // some additional code
        log.info("UserService created for user: " + userName);
    }
}
```

**Good:**

```java
final class UserService {
    private final int userId;
    private String userName; 
    private String password;

    UserService(int userId, String userName, String password) {
        this.userId = userId;
        this.userName = userName;
        this.password = password;
    }
}
```

---

## R-002k

Classes should not have more than three non-static fields. Introduce new classes or records to group related fields together.

**Bad:**

```java
final class UserService {
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

final class UserService {
    private final int userId;
    private final UserCredentials credentials;
    private final String email;

}
```

---

## R-002l

Inner classes (static and non-static) must not be used. Extract every nested type to its own top-level file. For the Builder pattern, use `<Type>Builder` as the class name (e.g. `IsinBuilder`).

**Bad:**

```java
final class UserService {
    final class UserCredentials {
        private final String userName;
        private final String password;

        UserCredentials(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }
    }
}
```

**Good:**

```java
final class UserCredentials {
    private final String userName;
    private final String password;

    UserCredentials(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }
}

final class UserService {
}
```

---

## R-002m

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

## R-002n

A class must not have unused fields.

**Bad:**

```java
final class UserService {
    private final int userId;
    private final String userName;

    UserService(int userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }
 
    String userName() {
        return userName;
    }
    // userId is not used anywhere in the class
}
```

**Good:**

```java
final class UserService {
    private final String userName;

    UserService(String userName) {
        this.userName = userName;
    }

    String userName() {
        return userName;
    }
}
```

---

## R-002o

A constructor must not have unused parameters.

**Bad:**

```java
final class UserService {
    private final String userName;

    UserService(int userId, String userName) {
        this.userName = userName;
    }
}
```

**Good:**

```java
final class UserService {
    private final String userName;

    UserService(String userName) {
        this.userName = userName;
    }
}
```

---

## R-002p

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

## R-002q

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

## R-002r

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

## R-002s

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

