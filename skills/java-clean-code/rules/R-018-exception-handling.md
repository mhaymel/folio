# Exception Handling Rules

## R-018a

All rules that apply to non-exception classes (naming, design, fields, etc.) also apply to exception classes unless explicitly stated otherwise in this file.

---

## R-018b

Custom exception classes must extend `RuntimeException` and be `final`.

**Bad:**

```java
class OrderException extends Exception {
    OrderException(String message) {
        super(message);
    }
}
```

**Bad:**

```java
class OrderException extends RuntimeException {
    OrderException(String message) {
        super(message);
    }
}
```

**Good:**

```java
final class OrderException extends RuntimeException {
    OrderException(String message) {
        super(message);
    }
}
```

---

## R-018c

Exception class names must end with `Exception`.

**Bad:**

```java
final class InsufficientFunds extends RuntimeException {
    InsufficientFunds(String message) {
        super(message);
    }
}

final class PaymentError extends RuntimeException {
    PaymentError(String message) {
        super(message);
    }
}
```

**Good:**

```java
final class InsufficientFundsException extends RuntimeException {
    InsufficientFundsException(String message) {
        super(message);
    }
}

final class PaymentException extends RuntimeException {
    PaymentException(String message) {
        super(message);
    }
}
```

---

## R-018d

If new exception classes are necessary, they must be unchecked — extend `RuntimeException`, not `Exception`.

The exception class must be `final` (see R-002a).

**Bad:**

```java
final class InsufficientFundsException extends Exception {
    InsufficientFundsException(String message) {
        super(message);
    }
}

// every caller is forced to handle or declare
final class PaymentService {
    void charge(Order order) throws InsufficientFundsException {
        if (order.balance().compareTo(order.total()) < 0) {
            throw new InsufficientFundsException("balance too low");
        }
    }
}
```

**Good:**

```java
final class InsufficientFundsException extends RuntimeException {
    InsufficientFundsException(String message) {
        super(message);
    }
}

final class PaymentService {
    void charge(Order order) {
        if (order.balance().compareTo(order.total()) < 0) {
            throw new InsufficientFundsException("balance too low");
        }
    }
}
```

**Good (with cause preservation):**

```java
final class PaymentGatewayException extends RuntimeException {
    PaymentGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}

final class PaymentService {
    void charge(Order order) {
        try {
            gateway.process(order);
        } catch (IOException exception) {
            throw new PaymentGatewayException("gateway call failed", exception);
        }
    }
}
```

---

## R-018e

It is forbidden to use exceptions for control flow. Exceptions must only signal unexpected, abnormal situations — not replace `if`/`else`, `Optional`, or return values. 

**Rule of thumb:** if the situation is *expected* during normal operation (e.g. "user not found", "empty input", "end of collection"), handle it with regular control flow. Reserve exceptions for truly *exceptional* conditions (e.g. database unreachable, file system corrupt, programming error).

**Bad:**

```java
final class UserService {
    User findOrCreate(UserId id) {
        try {
            return repository.getById(id); // throws when not found
        } catch (UserNotFoundException exception) {
            return createDefaultUser(id); // exception used as if/else
        }
    }
}
```

**Good:**

```java
final class UserService {
    User findOrCreate(UserId id) {
        var user = repository.findById(id); // returns null when not found
        if (user == null) {
            return createDefaultUser(id);
        }
        return user;
    }
}
```

**Bad:**

```java
final class CsvParser {
    boolean isInteger(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException exception) {
            return false; // exception used to test a condition
        }
    }
}
```

**Good:**

```java
final class CsvParser {
    boolean isInteger(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
```

---

## R-018f

Do not return `null` from a `catch` block. 

**Bad:**

```java
final class UserRepository {
    User findById(UserId id) {
        try {
            return database.query(id);
        } catch (SQLException exception) {
            return null; // failure is invisible to the caller
        }
    }
}
```

**Good:**

```java
final class UserRepository {
    User findById(UserId id) {
        try {
            return database.query(id);
        } catch (SQLException exception) {
            throw new DatabaseException("failed to find user", exception);
        }
    }
}
```

---

## R-018g

Do not swallow exceptions silently. Every `catch` block must either rethrow the exception (wrapped or as-is), log it, or perform meaningful recovery. An empty `catch` block or one that only returns a default value without any record of the failure makes errors invisible and turns debugging into guesswork.

**Bad:**

```java
final class ConfigLoader {
    Config load(Path path) {
        try {
            return parseFile(path);
        } catch (IOException exception) {
            // silently swallowed — no log, no rethrow, no trace
        }
        return Config.defaults();
    }
}
```

**Bad:**

```java
final class NotificationService {
    void send(User user) {
        try {
            mailer.send(user.email());
        } catch (MailException exception) {
            // ignored
        }
    }
}
```

**Good (rethrow):**

```java
final class ConfigLoader {
    Config load(Path path) {
        try {
            return parseFile(path);
        } catch (IOException exception) {
            throw new ConfigException("failed to load config", exception);
        }
    }
}
```

**Good (log and recover):**

```java
final class NotificationService {
    void send(User user) {
        try {
            mailer.send(user.email());
        } catch (MailException exception) {
            log.error("failed to send notification to {}", user.email(), exception);
        }
    }
}
```

---

## R-018h

Always preserve the original cause when wrapping exceptions. When catching an exception and throwing a new one, pass the original exception as the `cause` parameter.

**Bad:**

```java
final class UserRepository {
    User findById(UserId id) {
        try {
            return database.query(id);
        } catch (SQLException exception) {
            throw new DatabaseException("failed to find user"); // cause lost
        }
    }
}
```

**Good:**

```java
final class UserRepository {
    User findById(UserId id) {
        try {
            return database.query(id);
        } catch (SQLException exception) {
            throw new DatabaseException("failed to find user", exception);
        }
    }
}
```

---

