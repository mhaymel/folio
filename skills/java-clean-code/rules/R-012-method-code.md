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


