## Interface Design Rules

### R-005a
Interfaces must be package-private by default

Make interfaces public only if they must be used outside of the package.

**Bad:**

```java
public interface UserRepository {
    User findById(UserId id);
}
```

**Good:**

```java
interface UserRepository {
    User findById(UserId id);
}
```

---

### R-005b
Interfaces must follow single responsibility.Each interface 
should model one clear capability.

**Bad:**

```java
interface UserOperations {
    User findById(UserId id);
    void sendResetEmail(Email email);
    void exportUsersCsv();
}
```

**Good:**

```java
interface UserRepository {
    User findById(UserId id);
}

interface PasswordResetNotifier {
    void sendResetEmail(Email email);
}
```

---

### R-005c
Interfaces should stay small and cohesive.Prefer focused 
interfaces with a small number of related methods.

**Bad:**

```java
interface OrderService {
    void createOrder(Order order);
    void cancelOrder(OrderId orderId);
    Order findOrder(OrderId orderId);
    List<Order> findAllOrders();
    void sendOrderEmail(OrderId orderId);
    void archiveOrder(OrderId orderId);
}
```

**Good:**

```java
interface OrderRepository {
    void save(Order order);
    Order findById(OrderId orderId);
}

interface OrderCancellation {
    void cancel(OrderId orderId);
}
```

---

### R-005d
Interface method signatures must use interface types, not concrete implementations.
Interface method parameter and return types must use the most general interface that 
fits the usage. A concrete implementation class must not appear in an interface 
signature when an interface exposes the contract the method actually needs. 
An interface describes a contract.

 This applies to every type, not just collections. Common examples:

| Use (interface) | Not (concrete) |
|----|----|
| `List`, `Set`, `Map`, `Collection`, `Iterable`, `Queue`, `Deque` | `ArrayList`, `LinkedList`, `HashSet`, `LinkedHashSet`, `TreeSet`, `HashMap`, `LinkedHashMap`, `TreeMap`, `ArrayDeque` |
| `Executor`, `ExecutorService`, `ScheduledExecutorService` | `ThreadPoolExecutor`, `ScheduledThreadPoolExecutor` |
| `InputStream`, `OutputStream`, `Reader`, `Writer` | `FileInputStream`, `BufferedReader`, `PrintWriter`, `ByteArrayOutputStream` |
| `Path` | `File` |
| `Clock` | `SystemClock` / custom concrete clock |

**Bad:**

```java
interface UserRepository {
    ArrayList<User> findAll();
    void saveAll(HashSet<User> users);
    HashMap<UserId, User> indexById();
    void exportTo(FileOutputStream out);
    void scheduleCleanup(ScheduledThreadPoolExecutor executor);
}
```

**Good:**

```java
interface UserRepository {
    List<User> findAll();
    void saveAll(Set<User> users);
    Map<UserId, User> indexById();
    void exportTo(OutputStream out);
    void scheduleCleanup(ScheduledExecutorService executor);
}
```

---

### R-005e
Interface methods must have zero or one parameter. Group related parameters into a
record or — when the parameters are of the same type and represent a collection of
similar elements — a `List`. This is the interface-scoped companion 
to [R-013q](R-013-method-design.md#r-013q).

**Bad:**

```java
interface OrderService {
    void placeOrder(Product product, int quantity, BigDecimal price);
    void notifyUsers(User user1, User user2, User user3);
}
```

**Good:**

```java
interface OrderService {
    void placeOrder(OrderRequest request);
    void notifyUsers(List<User> users);
}
```

---

### R-005f
Parameter of interface `default` methods must not be reassigned. `default` methods 
in interfaces have a body, so the parameter-reassignment prohibition 
from [R-013y](R-013-method-design.md#r-013y) applies to them as well. 
Treat the parameter as effectively final and introduce a new local variable 
with a descriptive name when a different value is needed.

**Bad:**

```java
interface PriceService {
    default Money applyDiscount(Money price) {
        price = price.subtract(defaultDiscount());
        return price;
    }
}
```

**Good:**

```java
interface PriceService {
    default Money applyDiscount(Money price) {
        Money discountedPrice = price.subtract(defaultDiscount());
        return discountedPrice;
    }
}
```

---

