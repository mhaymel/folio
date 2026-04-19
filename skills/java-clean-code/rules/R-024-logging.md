# Logging Rules

## R-024a

Use SLF4J for all logging. Each class that logs declares one logger as a `private static final` field named `LOG`, obtained via a static import of `LoggerFactory.getLogger`. `System.out`, `System.err`, `Throwable.printStackTrace`, and `java.util.logging` are forbidden.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

final class OrderService {
    private final OrderSubmitter submitter;

    OrderService(OrderSubmitter submitter) {
        this.submitter = requireNonNull(submitter);
    }

    void placeOrder(OrderRequest request) {
        System.out.println("placing order " + request);
        try {
            submitter.submit(request);
        } catch (SubmitException e) {
            e.printStackTrace();
        }
    }
}
```

**Good:**

```java
import org.slf4j.Logger;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

final class OrderService {
    private static final Logger LOG = getLogger(OrderService.class);

    private final OrderSubmitter submitter;

    OrderService(OrderSubmitter submitter) {
        this.submitter = requireNonNull(submitter);
    }

    void placeOrder(OrderRequest request) {
        LOG.info("placing order {}", request);
        try {
            submitter.submit(request);
        } catch (SubmitException e) {
            LOG.error("failed to submit order {}", request, e);
        }
    }
}
```

---

## R-024b

Use `{}` placeholders for log arguments. String concatenation and 
`String.format` are forbidden in log calls.

**Bad:**

```java
LOG.debug("loaded user " + user.id() + " from " + source);
LOG.info(String.format("processed %d orders in %d ms", count, durationMs));
```

**Good:**

```java
LOG.debug("loaded user {} from {}", user.id(), source);
LOG.info("processed {} orders in {} ms", count, durationMs);
```

---

## R-024c

When logging an exception, pass the `Throwable` as the last argument. Do not concatenate `exception.getMessage()` into the message, and do not add a `{}` placeholder for the exception — SLF4J detects a trailing `Throwable` and renders the full stack trace.

**Bad:**

```java
catch (IOException e) {
    LOG.error("failed to read config: " + e.getMessage());
}

catch (IOException e) {
    LOG.error("failed to read config {}", path, e.getMessage());
}
```

**Good:**

```java
catch (IOException e) {
    LOG.error("failed to read config {}", path, e);
}
```

---

## R-024d

Do not log sensitive data: passwords, tokens, API keys, session 
identifiers, full card numbers, or personal data covered by privacy 
policy. Log a stable non-sensitive identifier instead (e.g. a user id, 
a hashed token prefix, a correlation id).

**Bad:**

```java
LOG.info("authenticated user {} with password {}", email, password);
LOG.debug("calling provider with token {}", apiToken);
```

**Good:**

```java
LOG.info("authenticated user {}", userId);
LOG.debug("calling provider for user {}", userId);
```

---

## R-024e

Do not log-and-throw. Either log the failure and handle it, or throw 
and let the caller decide. 

**Bad:**

```java
try {
    repository.save(order);
} catch (PersistenceException e) {
    LOG.error("failed to save order {}", order, e);
    throw e;
}
```

**Good (log once, at the outermost handler that can act on it):**

```java
try {
    repository.save(order);
} catch (PersistenceException e) {
    throw new OrderSaveException(order.id(), e);
}
```

---

## R-024f

Match the log level to the severity of the event:

- `ERROR` — a failure the operator must act on. The affected request or job did not complete.
- `WARN`  — a recoverable anomaly or a degraded path. Processing continued.
- `INFO`  — significant business events: startup, shutdown, a completed transaction.
- `DEBUG` — diagnostic detail for developers. Off in production by default.
- `TRACE` — fine-grained step-by-step detail used only while actively debugging.

Do not use `ERROR` for events the system already handled, and do not use `INFO` for per-iteration chatter.

**Bad:**

```java
LOG.error("cache miss for key {}", key);          // normal, not a failure
LOG.info("entering loop iteration {}", index);    // diagnostic noise
LOG.warn("application started on port {}", port); // significant event, not a warning
```

**Good:**

```java
LOG.debug("cache miss for key {}", key);
LOG.trace("entering loop iteration {}", index);
LOG.info("application started on port {}", port);
```

---

## R-024g

Guard a log statement with `isDebugEnabled()` / `isTraceEnabled()` only when building an argument is itself expensive — a large serialisation, a database round-trip, an external call. A plain field read or `toString` is not expensive and does not need a guard; SLF4J already skips placeholder substitution when the level is disabled.

This mirrors the measurement-or-nothing rule for lazy loading in [R-014o](R-014-method-body.md#r-014o): only pay the guard's cost when the saving is real.

**Bad (needless guard on a cheap call):**

```java
if (LOG.isDebugEnabled()) {
    LOG.debug("loaded user {}", user.id());
}
```

**Bad (expensive call left unguarded):**

```java
LOG.debug("portfolio snapshot: {}", renderFullSnapshot(portfolio));
```

**Good:**

```java
LOG.debug("loaded user {}", user.id());

if (LOG.isDebugEnabled()) {
    LOG.debug("portfolio snapshot: {}", renderFullSnapshot(portfolio));
}
```

---
