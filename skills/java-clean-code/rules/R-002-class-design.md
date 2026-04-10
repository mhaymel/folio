# Class Design Rules

## R-002a

Classes must be declared as `final`.

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

The primary constructor should declare one parameter for each non-static field.
The primary and secondary constructors may be package-private, private, or public.


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

    UserService(int userId, String userName, String password) { // primary constructor
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

Inner classes (static and non-static) must not be used. Use top-level classes instead.

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