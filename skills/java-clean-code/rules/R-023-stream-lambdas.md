# Stream and Lambda Rules

## R-023a

Prefer a method reference to an equivalent lambda. A lambda that does 
nothing but delegate to a named method is just noise around the method reference.

**Bad:**

```java
List<String> upper = names.stream()
        .map(name -> name.toUpperCase())
        .toList();

List<Receipt> receipts = orders.stream()
        .map(order -> toReceipt(order))
        .toList();
```

**Good:**

```java
List<String> upper = names.stream()
        .map(String::toUpperCase)
        .toList();

List<Receipt> receipts = orders.stream()
        .map(this::toReceipt)
        .toList();
```

---

## R-023b

Lambda bodies must be a single expression. If more than one statement 
is needed, extract the body to a private method and use a method reference 
(see [R-023a](#r-023a)).

**Bad:**

```java
List<Order> settled = orders.stream()
        .map(order -> {
            Money total = order.total();
            Money withTax = total.multiply(TAX_RATE);
            return order.withTotal(withTax);
        })
        .toList();
```

**Good:**

```java
final class OrderService {
    List<Order> settled(List<Order> orders) {
        return orders.stream()
                .map(OrderService::applyTax)
                .toList();
    }

    private static Order applyTax(Order order) {
        Money withTax = order.total().multiply(TAX_RATE);
        return order.withTotal(withTax);
    }
}
```

---

## R-023c

Streams and lambdas must be pure: no mutation of captured state, no I/O, 
no logging. A stream pipeline transforms and collects — it does not perform 
side effects. When side effects are genuinely required (e.g. iterating to 
call a void method), use a plain for-each loop instead of `forEach`.

**Bad:**

```java
List<Order> active = new ArrayList<>();
orders.stream()
        .filter(Order::isActive)
        .forEach(active::add); // mutates external list

orders.forEach(order -> LOG.info("processing {}", order)); // I/O in pipeline
```

**Good:**

```java
List<Order> active = orders.stream()
        .filter(Order::isActive)
        .toList();

for (Order order : orders) {
    LOG.info("processing {}", order);
}
```

---

## R-023d

When collecting a stream into a `List`, `Set`, or `Map` that is stored — as a record component, a field, or a return value — use `Collectors.toUnmodifiableList`, `toUnmodifiableSet`, or `toUnmodifiableMap`. This keeps the result immutable and consistent with [R-007g](R-007-record-design.md#r-007g).

**Bad:**

```java
final class PortfolioService {
    PortfolioSnapshot snapshotFrom(List<Order> orders) {
        List<Position> positions = orders.stream()
                .map(Order::position)
                .collect(Collectors.toList()); // mutable
        return new PortfolioSnapshot(positions);
    }
}
```

**Good:**

```java
final class PortfolioService {
    PortfolioSnapshot snapshotFrom(List<Order> orders) {
        List<Position> positions = orders.stream()
                .map(Order::position)
                .collect(Collectors.toUnmodifiableList());
        return new PortfolioSnapshot(positions);
    }
}
```

---

## R-023e

Parallel streams are forbidden by default. 

**Exception:** parallel streams are allowed only after profiling or benchmarking evidence 
shows a measurable and reproducible speed-up on realistic input. When the exception applies, 
a comment is required that names the measurement, ticket, or incident that justifies it. 
This mirrors the measurement-or-nothing rule for lazy loading in [R-014o](R-014-method-body.md#r-014o).

**Bad (speculative parallelism):**

```java
Money total = orders.parallelStream()
        .map(Order::total)
        .reduce(Money.ZERO, Money::add);
```

**Good (sequential):**

```java
Money total = orders.stream()
        .map(Order::total)
        .reduce(Money.ZERO, Money::add);
```

**Good (parallel, with recorded evidence):**

```java
// Parallel: benchmark (PROJ-5120, JMH 2026-02-18) showed a 3.4× speed-up
// on the nightly 2M-row valuation batch. Single-threaded baseline took 11 s.
Money total = positions.parallelStream()
        .map(Position::marketValue)
        .reduce(Money.ZERO, Money::add);
```

---

## R-023f

`peek()` is forbidden. 

**Bad:**

```java
List<Order> active = orders.stream()
        .peek(order -> log.debug("seeing: " + order))
        .filter(Order::isActive)
        .toList();
```

**Good:**

```java
List<Order> active = orders.stream()
        .filter(Order::isActive)
        .toList();
```

---

## R-023g

Prefer `Optional.map`, `flatMap`, `orElse`, `orElseThrow`, and `ifPresent` over `isPresent()` + `get()`. The chain style keeps the presence check and the usage together, removes the risk of calling `get()` on an empty `Optional`, and makes the fallback explicit.

**Bad:**

```java
Optional<User> maybeUser = repository.findByEmail(email);
if (maybeUser.isPresent()) {
    return maybeUser.get().displayName();
}
return "guest";
```

**Good:**

```java
return repository.findByEmail(email)
        .map(User::displayName)
        .orElse("guest");
```

---

## R-023h

Extract complex filter predicates to a private method and use a method reference. An inline predicate lambda with multiple conditions is hard to name, test, and reuse — a named method makes intent explicit and keeps the pipeline readable.

**Bad:**

```java
List<Order> result = orders.stream()
        .filter(o -> o.customer().country().isEu() && o.total().value().signum() > 0)
        .toList();
```

**Good:**

```java
final class OrderService {
    List<Order> findEuOrdersWithPositiveTotal(List<Order> orders) {
        return orders.stream()
                .filter(OrderService::isEuOrderWithPositiveTotal)
                .toList();
    }

    private static boolean isEuOrderWithPositiveTotal(Order order) {
        return order.customer().country().isEu() && order.total().value().signum() > 0;
    }
}
```

---
