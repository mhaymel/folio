# Method Code Quality Rules

## R-012a

Unreachable code is forbidden. Remove all statements that cannot be executed because they come after `return`, `throw`, `break`, or `continue`.

**Bad:**

```java
final class PaymentService {
    void processPayment(Invoice invoice) {
        if (invoice.isPaid()) {
            return;
        }
        // Code below is unreachable when invoice is paid
        invoice.setStatus("pending");
        saveToDatabase(invoice);
    }
}
```

**Good:**

```java
final class PaymentService {
    void processPayment(Invoice invoice) {
        if (invoice.isPaid()) {
            return;
        }
        invoice.setStatus("pending");
        saveToDatabase(invoice);
    }
}
```

---

## R-012b

Empty blocks (catch, if, else, try, etc.) with no side effects are forbidden. Remove the block and its condition if applicable, or add meaningful logic. If intentionally empty (e.g., catching an exception that should be ignored), document why with a comment.

**Bad:**

```java
final class PaymentProcessor {
    void process(Payment payment) {
        try {
            validatePayment(payment);
        } catch (ValidationException e) {
            // empty catch block — silently ignores errors
        }

        if (payment.isOptional()) {
        } else {
            processPayment(payment);
        }
    }
}
```

**Good:**

```java
final class PaymentProcessor {
    void process(Payment payment) {
        try {
            validatePayment(payment);
        } catch (ValidationException e) {
            // Validation errors for optional payments are acceptable
            // and should not prevent processing
            logWarning("Optional payment validation failed", e);
        }

        if (!payment.isOptional()) {
            processPayment(payment);
        }
    }
}
```

---

## R-012c

Do not use `continue`. Extract the loop body to a private method and use `return` instead of `continue`.

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

## R-012d

`break` is forbidden. In a loop extract body to a private method and use `return` instead of `break`.

**Exception:** `break` is allowed in `switch` statements/expressions, but you must still handle the `default` case and avoid fall-through.

**Bad:**

```java
final class OrderSearcher {
    Order findFirst(List<Order> orders) {
        Order result = null;
        for (Order order : orders) {
            if (order.isPending()) {
                result = order;
                break;
            }
        }
        return result;
    }
}
```

**Good:**

```java
final class OrderSearcher {
    Order findFirst(List<Order> orders) {
        for (Order order : orders) {
            if (order.isPending()) {
                return order;
            }
        }
        return null;
    }
}
```

**Good (switch exception):**

```java
final class StatusMapper {
    String label(Status status) {
        switch (status) {
            case PENDING:
                return "Pending";
            case SHIPPED:
                return "Shipped";
            default:
                break;
        }
        return "Unknown";
    }
}
```

---

## R-012e

`return`, `break`, or `continue` inside a `finally` are forbidden.

**Bad:**

```java
final class ResourceLoader {
    String load(Path path) {
        try {
            return readFile(path);
        } finally {
            return ""; // silently swallows any exception from readFile
        }
    }
}
```

**Bad:**

```java
final class BatchProcessor {
    void process(List<Order> orders) {
        for (Order order : orders) {
            try {
                handle(order);
            } finally {
                continue; // silently swallows any exception from handle
            }
        }
    }
}
```

**Good:**

```java
final class ResourceLoader {
    String load(Path path) {
        try {
            return readFile(path);
        } finally {
            cleanup();
        }
    }
}
```

---
