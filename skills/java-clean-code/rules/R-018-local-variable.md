# Local Variable Rules

## R-018a

Local variable names must use `lowerCamelCase`.

**Bad:**

```java
String UserName = "Alice";
BigDecimal PRICE = BigDecimal.TEN;
```

**Good:**

```java
String userName = "Alice";
BigDecimal price = BigDecimal.TEN;
```

---

## R-018b

Do not use underscores in local variable names.

**Bad:**

```java
String user_name = "Alice";
int total_count = 0;
BigDecimal net_price = BigDecimal.TEN;
```

**Good:**

```java
String userName = "Alice";
int totalCount = 0;
BigDecimal netPrice = BigDecimal.TEN;
```

---

## R-018c

Local variable names must be meaningful and describe what they hold — a reader should understand the variable's purpose without looking at surrounding code.

**Bad:**

```java
User x = repository.findByEmail(email);
LocalDate d = invoice.dueDate();
BigDecimal tmp = order.totalAmount();
```

**Good:**

```java
User user = repository.findByEmail(email);
LocalDate dueDate = invoice.dueDate();
BigDecimal totalAmount = order.totalAmount();
```

---

## R-018d

Local variable names must be more than one character long. Single-letter names like `x`, `s`, `a` are forbidden.

**Exceptions:**
- loop variables in classic `for` loops may use `i`, `j`, or `k`
- catch-clause variables may use the single letter `e` (e.g. `catch (IOException e)`)

**Bad:**

```java
User u = repository.findById(id);
Status s = order.status();
```

**Good:**

```java
User user = repository.findById(id);
Status status = order.status();
```

**Good (loop exception):**

```java
for (int i = 0; i < items.size(); i++) {
    process(items.get(i));
}
```

**Good (catch exception):**

```java
try {
    repository.save(order);
} catch (PersistenceException e) {
    LOG.error("failed to save order", e);
}
```

---

## R-018e

Do not use abbreviations or acronyms in variable names unless they are universally understood (e.g. `id`, `url`). Spell out the full word.

**Bad:**

```java
int qty = order.quantity();
Configuration cfg = loadConfiguration();
BigDecimal amt = invoice.amount();
PortfolioManager mgr = new PortfolioManager();
```

**Good:**

```java
int quantity = order.quantity();
Configuration configuration = loadConfiguration();
BigDecimal amount = invoice.amount();
PortfolioManager manager = new PortfolioManager();
```

---

## R-018f

Do not prefix or suffix variable names with type information (Hungarian notation). The type is already visible from the declaration.

**Bad:**

```java
String strName = "Alice";
List<Order> orderList = repository.findAll();
int iCount = 0;
```

**Good:**

```java
String name = "Alice";
List<Order> orders = repository.findAll();
int count = 0;
```

---

## R-018g

Boolean local variables must start with `is`, `has`, `can`, `should`, or `contains` — consistent with boolean method naming (R-011g).

**Bad:**

```java
boolean valid = order.isValid();
boolean permission = user.hasPermission();
boolean retry = retryCount < MAX_RETRIES;
```

**Good:**

```java
boolean isValid = order.isValid();
boolean hasPermission = user.hasPermission();
boolean canRetry = retryCount < MAX_RETRIES;
```

---

## R-018h

Local variables must be initialized at the point of declaration. 
Do not declare a variable and assign it later.

**Exception:** in loops or `try` blocks where the variable needs to be declared before the block.

**Bad:**

```java
String name;
BigDecimal total;

name = user.name();
total = order.totalAmount();
```

**Good:**

```java
String name = user.name();
BigDecimal total = order.totalAmount();
```

**Good (try block exception):**

```java
Response response = null;
try {
    response = client.send(request);
    process(response);
} catch (IOException exception) {
    LOG.error("request failed with response: {}", response, exception);
}
```

**Good (loop exception):**

```java
String line;
while ((line = reader.readLine()) != null) {
    process(line);
}
```

---

## R-018i

Local variables must not be reassigned. Treat every local variable as effectively final. If a different value is needed, create a new variable with a descriptive name.

**Exceptions:** reassignment is allowed for:
- loop variables in classic `for` loops (e.g. `i++`)
- accumulators and counters (e.g. `count++`, `total = total.add(amount)`)
- line-by-line or chunk-by-chunk I/O reading (e.g. `while ((line = reader.readLine()) != null)`)

**Bad:**

```java
BigDecimal price = order.basePrice();
price = price.multiply(taxRate);
price = price.subtract(discount);
```

**Good:**

```java
BigDecimal basePrice = order.basePrice();
BigDecimal priceWithTax = basePrice.multiply(taxRate);
BigDecimal finalPrice = priceWithTax.subtract(discount);
```

**Good (loop variable):**

```java
for (int i = 0; i < orders.size(); i++) {
    process(orders.get(i));
}
```

**Good (accumulator):**

```java
BigDecimal total = BigDecimal.ZERO;
for (Order order : orders) {
    total = total.add(order.amount());
}
```

**Good (I/O reading):**

```java
String line;
while ((line = reader.readLine()) != null) {
    process(line);
}
```

---

## R-018j

Local variables must not be declared `final`. Since local variables must not be reassigned (R-018i), adding `final` is redundant noise.

**Bad:**

```java
final String name = user.name();
final BigDecimal total = order.totalAmount();
final List<Order> orders = repository.findAll();
```

**Good:**

```java
String name = user.name();
BigDecimal total = order.totalAmount();
List<Order> orders = repository.findAll();
```

---

## R-018k

Avoid magic literals in expressions. Do not use unexplained numeric 
or string literals directly in expressions — give them a descriptive 
name with a local `final` variable or use a class-level constant/tiny type 
when appropriate.

**Bad:**

```java
if (status == 5) { // what is 5?
    handlePending();
}
items.forEach(item -> process(item, "A")); // what is "A"?
```

**Good (class-level constants):**

```java
private static final int STATUS_PENDING = 5;
private static final String ROLE_ADMIN = "A";
// used throughout the class
```

Exceptions and notes:
- Trivial literals that are self-explanatory (e.g. `0`, `1`, `-1`) or loop indices (`i`, `j`) are typically allowed.
- Prefer tiny types (records) for domain values (e.g. `CurrencyCode`, `ProductCode`) rather than raw string literals.
- In tests, it's acceptable to use literals that make the test intent clear; prefer well-named variables when the literal's meaning is non-obvious.

---

## R-018l

Unused local variables must be removed. A variable is unused if it is declared but never read after its declaration or assigned a value that is never read.

**Bad:**

```java
void process(Order order) {
    int count = order.items().size(); // never used
    doSomething();
}
```

**Good:**

```java
void process(Order order) {
    int itemCount = order.items().size();
    if (itemCount == 0) return;
    doSomething();
}
```

Or, when the variable is unnecessary:

```java
@Override
public void process(Order order) {
    // order intentionally unused
    doSomething();
}
```

Exceptions and intentional cases:
- Intentional unused locals are extremely rare. Prefer removing the variable; if a local is kept to documents intent, add a short comment explaining why.
- Do not keep unused locals as placeholders; version control preserves history.

Enforcement and notes:
- Enable compiler/IDE warnings for unused locals (most IDEs and `javac -Xlint` detect this).
- Use static analysis tools (PMD/SpotBugs) to detect and fail on unused local variables in CI.

---

## R-018m

Declare local variables as close as possible to their first usage.
Do not declare all variables at the top of a method.

**Bad:**

```java
import static java.util.Objects.requireNonNull;

record Invoice(Money total, Currency currency, boolean hasDiscount) {
    Invoice {
        requireNonNull(total);
        requireNonNull(currency);
    }
}

final class InvoiceService {
    private static final BigDecimal DISCOUNT_THRESHOLD = BigDecimal.valueOf(100);

    void process(List<Order> orders) {
        Money total = calculateTotal(orders);
        Currency currency = orders.get(0).currency();
        boolean hasDiscount = total.amount().compareTo(DISCOUNT_THRESHOLD) > 0;

        // ... 10 lines of unrelated logic that does not use total, currency, or hasDiscount ...

        sendInvoice(new Invoice(total, currency, hasDiscount));
    }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

record Invoice(Money total, Currency currency, boolean hasDiscount) {
    Invoice {
        requireNonNull(total);
        requireNonNull(currency);
    }
}

final class InvoiceService {
    private static final BigDecimal DISCOUNT_THRESHOLD = BigDecimal.valueOf(100);

    void process(List<Order> orders) {
        // ... 10 lines of unrelated logic ...

        Money total = calculateTotal(orders);
        Currency currency = orders.get(0).currency();
        boolean hasDiscount = total.amount().compareTo(DISCOUNT_THRESHOLD) > 0;
        sendInvoice(new Invoice(total, currency, hasDiscount));
    }
}
```

---

## R-018n

A local variable of a domain type must be named either (a) by lowercasing the first
letter of the type name — `Currency currency`, `Isin isin`, `OrderId orderId` — or
(b) by a role name that adds semantic meaning beyond the type itself — `Money price`,
`Money total`, `User customer`. Mere synonyms of the type (`Isin identifier`,
`Currency denomination`, `Money value`, `Money amount`) are forbidden — `amount`
is reserved for the `BigDecimal` component *inside* `Money` (`money.amount()`),
so `Money amount` shadows that component and obscures the distinction between
the whole and the part. Use a qualified variant
(e.g. `primaryIsin`, `fallbackIsin`) only when two locals of the same type coexist
in the same scope and need disambiguation. This is the local-scoped companion to
[R-003p](R-003-class-field.md#r-003p).

**Bad:**

```java
void settle(Order order) {
    Isin identifier = order.isin();
    Currency denomination = order.currency();
    Money value = order.total();
    // ...
}
```

**Good:**

```java
void settle(Order order) {
    Isin isin = order.isin();
    Currency currency = order.currency();
    Money price = order.total();
    // ...
}
```

**Good (disambiguation when two locals share a type):**

```java
void reconcile(Trade trade) {
    Isin primaryIsin = trade.primaryLeg().isin();
    Isin fallbackIsin = trade.fallbackLeg().isin();
    // ...
}
```

---
