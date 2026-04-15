## Interface Design Rules

### R-005a: Interfaces must be package-private by default

Make interfaces public only if they must be used outside of the package.

**Bad:**

```java
public interface UserRepository {
    User findById(long id);
}
```

**Good:**

```java
interface UserRepository {
    User findById(long id);
}
```

---

### R-005b: Interfaces must follow single responsibility

Each interface should model one clear capability.

**Bad:**

```java
interface UserOperations {
    User findById(long id);
    void sendResetEmail(String email);
    void exportUsersCsv();
}
```

**Good:**

```java
interface UserRepository {
    User findById(long id);
}

interface PasswordResetNotifier {
    void sendResetEmail(String email);
}
```

---

### R-005c: Interfaces should stay small and cohesive

Prefer focused interfaces with a small number of related methods.

**Bad:**

```java
interface OrderService {
    void createOrder(Order order);
    void cancelOrder(long orderId);
    Order findOrder(long orderId);
    List<Order> findAllOrders();
    void sendOrderEmail(long orderId);
    void archiveOrder(long orderId);
}
```

**Good:**

```java
interface OrderRepository {
    void save(Order order);
    Order findById(long orderId);
}

interface OrderCancellation {
    void cancel(long orderId);
}
```

---

