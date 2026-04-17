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

### R-005d: Interface method signatures must use interface types, not concrete implementations

Interface method parameters and return types must use the most general interface that fits the usage. 
A concrete implementation class must not appear in an interface signature when an interface exposes 
the contract the method actually needs. An interface describes a contract.

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
    HashMap<UserId, User> byId();
    void exportTo(FileOutputStream out);
    void scheduleCleanup(ScheduledThreadPoolExecutor executor);
}
```

**Good:**

```java
interface UserRepository {
    List<User> findAll();
    void saveAll(Set<User> users);
    Map<UserId, User> byId();
    void exportTo(OutputStream out);
    void scheduleCleanup(ScheduledExecutorService executor);
}
```

---

