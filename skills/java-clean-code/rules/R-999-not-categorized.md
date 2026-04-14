# Uncategorized Rules

## R-999a

Purpose: a container for rules that do not fit existing categories. Rules will be moved to the fitting once the category is created.

---

## R-999b

Do not duplicate existing rules. Before adding an uncategorized rule, search the `rules/` folder to ensure the same guidance doesn't already exist in another file.

**Bad:** adding a rule about `lowerCamelCase` to R-999 while `R-004-class-field.md` and `R-012-method-naming.md` already cover naming.

**Good:** add a short cross-reference to the existing rule and explain why the new rule is different (if it truly is).

---

## R-999c

All identifiers — class names, interface names, record names, enum names, method names, field names, parameter names, local variable names, and package names — must be in English. Do not use German or any other non-English language.

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

## R-999c

Magic values are forbidden. A magic value is a literal value that appears in code without explanation. Use named constants instead of magic values. Exceptions are trivial literals that like  `0`, `1`, `-1`, ""

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
