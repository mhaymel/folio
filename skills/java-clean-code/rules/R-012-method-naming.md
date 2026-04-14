# Method Naming Rules

## R-012a

Method names must use `lowerCamelCase`.

**Bad:**

```java
final class OrderService {
    void ProcessOrder(Order order) {
    }

    void save_user(User user) {
    }
}
```

**Good:**

```java
final class OrderService {
    void processOrder(Order order) {
    }

    void saveUser(User user) {
    }
}
```

---

## R-012b

Method names must be a verb or verb phrase that describes the action performed.

**Bad:**

```java
final class OrderService {
    void order(Order order) {
    }

    String username() {
    }
}
```

**Good:**

```java
final class OrderService {
    void placeOrder(Order order) {
    }

    String findUsername() {
    }
}
```

---

## R-012c

Method names must be **meaningful** and clearly describe what the method does — a reader should immediately understand the method's purpose from its name alone.

**Bad:**

```java
final class OrderService {
    void handle(Order order) {
    }

    void doStuff() {
    }
}
```

**Good:**

```java
final class OrderService {
    void validateOrder(Order order) {
    }

    void sendConfirmationEmail() {
    }
}
```

---

## R-012d

Method names must be **more than one character** long. Single-letter method names like `a`, `x`, `f` are forbidden.

**Bad:**

```java
final class MathService {
    int a(int x) {
        return x * 2;
    }
}
```

**Good:**

```java
final class MathService {
    int doubleValue(int value) {
        return value * 2;
    }
}
```

---

## R-012e

Do not use underscores in method names. Use `lowerCamelCase` exclusively. This applies to all methods, including unit test methods.

**Bad:**

```java
final class OrderService {
    void process_order(Order order) {
    }
}

final class OrderServiceTest {
    void should_process_order() {
    }
}
```

**Good:**

```java
final class OrderService {
    void processOrder(Order order) {
    }
}

final class OrderServiceTest {
    void shouldProcessOrder() {
    }
}
```

---

## R-012f

Getter-style methods for non-boolean properties must not use the `get` prefix. Use the property name directly. This follows the convention established by Java records.

**Bad:**

```java
final class User {
    private final String name;

    String getName() {
        return name;
    }
}
```

**Good:**

```java
final class User {
    private final String name;

    String name() {
        return name;
    }
}
```

---

## R-012g

Boolean query (predicate) methods must start with one of the prefixes `is`, `has`, `can`, `should`, or `contains`. Choose the prefix that best expresses the question being asked:

- `is` — state or identity checks (e.g. `isEmpty()`, `isCancelled()`)
- `has` — ownership or presence checks (e.g. `hasPermission(User user)`, `hasItems()`)
- `can` — capability or permission checks (e.g. `canRetry()`, `canAccess()`)
- `should` — policy or recommendation checks (e.g. `shouldNotify()`, `shouldRetry()`)
- `contains` — containment checks (e.g. `containsKey(Key key)`, `containsValue(Value value)`)

**Bad:**

```java
final class Order {
    boolean cancelled() {
        return status == Status.CANCELLED;
    }

    boolean items() {
        return !items.isEmpty();
    }

    boolean retry() {
        return retryCount < MAX_RETRIES;
    }

    boolean notify() {
        return preferences.emailEnabled();
    }

    boolean key(String key) {
        return map.containsKey(key);
    }
}
```

**Good:**

```java
final class Order {
    boolean isCancelled() {
        return status == Status.CANCELLED;
    }

    boolean hasItems() {
        return !items.isEmpty();
    }

    boolean canRetry() {
        return retryCount < MAX_RETRIES;
    }

    boolean shouldNotify() {
        return preferences.emailEnabled();
    }

    boolean containsKey(String key) {
        return map.containsKey(key);
    }
}
```

---

