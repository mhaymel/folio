# Allgemeines

## R-001a

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

