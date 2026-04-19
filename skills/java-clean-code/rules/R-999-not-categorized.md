# Uncategorized Rules

## R-999a

Purpose: a container for rules that do not fit existing categories. 
Rules will be moved to the fitting once the category is created.

---

## R-999b

Do not duplicate existing rules. Before adding an uncategorized rule, 
search the `rules/` folder to ensure the same guidance doesn't already 
exist in another file.

**Bad:** adding a rule about `lowerCamelCase` to R-999 while `R-003-class-field.md` and `R-011-method-naming.md` already cover naming.

**Good:** add a short cross-reference to the existing rule and explain why the new rule is different (if it truly is).

---

## R-999c

All identifiers — class names, interface names, record names, enum names, method names, 
field names, parameter names, local variable names, and package names — must be in English. 
Do not use German or any other non-English language.

**Bad:**

```java
final class BenutzerService {
    private final BenutzerName benutzerName;

    BenutzerService(BenutzerName benutzerName) {
        this.benutzerName = requireNonNull(benutzerName);
    }

    void speichern() {
    }
}
```

**Good:**

```java
final class UserService {
    private final UserName userName;

    UserService(UserName userName) {
        this.userName = requireNonNull(userName);
    }

    void save() {
    }
}
```
---

## R-999d

Magic values are forbidden. A magic value is a literal value that 
appears in code without explanation. Use named constants instead of 
magic values. Exceptions are trivial literals that like  `0`, `1`, `-1`, ""

**Bad:**

```java
final class OrderService {
    void retry() {
        if (retries > 3) { // what is 3?
            // ...
        }
    }

    void timeout() {
        client.call(5000); // what is 5000 (ms)?
    }
}
```

**Good:**

```java
final class OrderService {
    private static final int MAX_RETRIES = 3;
    private static final int TIMEOUT_MS = 5_000;

    void retry() {
        if (retries > MAX_RETRIES) { // clear intent
            // ...
        }
    }

    void timeout() {
        client.call(TIMEOUT_MS);
    }
}
```

---

## R-999e

All identifiers must be pronounceable.

**Bad:**

```java
final class DtaRcrd {
    private final Instant genymdhms;
    private final Instant modymdhms;
    private final int pszqint;
}
```

**Good:**

```java
record Quantity(int value) {
}

final class DataRecord {
    private final Instant generationTimestamp;
    private final Instant modificationTimestamp;
    private final Quantity quantity;
}
```

---

## R-999f

All identifiers must be searchable. Single-letter names and very 
short abbreviations are hard to grep for.

**Exception:** loop variables `i`, `j`, `k` in classic `for` loops are acceptable.

**Bad:**

```java
final class OrderService {
    int process() {
        int d = 0;         // days? discount? delta? unsearchable
        String s = "EUR";  // currency? status? unsearchable
        for (int i = 0; i < orders.size(); i++) {
            int t = orders.get(i).total(); // what is t?
            d += t;
        }
        return d;
    }
}
```

**Good:**

```java
final class OrderService {
    int process() {
        int totalAmount = 0;
        String currencyCode = "EUR";
        for (int i = 0; i < orders.size(); i++) {
            int orderTotal = orders.get(i).total();
            totalAmount += orderTotal;
        }
        return totalAmount;
    }
}
```

---

## R-999g

Do not name a variable, parameter, or record component after a domain
type when it does not represent an instance of that domain type. If a 
tiny type (e.g. `Isin`, `TickerSymbol`) exists for a concept, a `String` 
variable holding something other than a valid instance of that concept 
— such as a search term, partial match, or filter fragment — must use a 
distinct name that reflects its actual purpose (e.g. `isinFragment`, 
`tickerSymbolFragment`, `nameFragment`).

**Bad:**

```java
import static java.util.Objects.requireNonNull;

// "isin" suggests the field holds a valid Isin, but it is actually a partial search string
record TickerSymbolFilter(String isin, String tickerSymbol, String name) {
    TickerSymbolFilter {
        requireNonNull(isin);
        requireNonNull(tickerSymbol);
        requireNonNull(name);
    }
}
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

record IsinFragment(String value) {
    IsinFragment {
        requireNonNull(value);
    }
}

record TickerSymbolFragment(String value) {
    TickerSymbolFragment {
        requireNonNull(value);
    }
}

record NameFragment(String value) {
    NameFragment {
        requireNonNull(value);
    }
}

// Names make clear these are search fragments, not domain objects
record TickerSymbolFilter(IsinFragment isinFragment, TickerSymbolFragment tickerSymbolFragment, NameFragment nameFragment) {
    TickerSymbolFilter {
        requireNonNull(isinFragment);
        requireNonNull(tickerSymbolFragment);
        requireNonNull(nameFragment);
    }
}
```

---
