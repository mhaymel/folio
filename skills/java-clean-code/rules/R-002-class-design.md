## Class Design Rules

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

Classes must be package-private. Make classes public only if they must be accessed outside of the package.

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
    public abstract void process(Data data);
}

final class UserDataProcessor extends Processor {
    @Override
    public void process(Data data) {
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

Methods must be package-private by default. Make methods public only if they must be accessed outside.

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
Non-static fields must be private

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

## R-002f

Prefer final fields in classes wherever possible

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

Prefer immutable classes wherever possible

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

Classes should have only one primary constructor. The primary initialize all fields. There can be more than one secondary constructor, but they must delegate to the primary constructor. Fields must not be initialized in secondary constructors.
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
        this.userName = "User1;
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

    UserService(int userId, String userName, String password) { //primary constructor
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

    UserService(int userId, List<String> users) { //primary constructor
        this.userId = userId;
        this.users = users;
    }
}
```

---

## R-002h

Constructors must be code free, except for precondition checks.

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

## R-002i

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

## R-002j

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

## R-002k

enums must not be declared as inner types. Declare enums at the top level. 

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

