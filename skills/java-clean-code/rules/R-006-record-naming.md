# Record Naming Rules

## R-006a

Record names must start with an uppercase letter and use CamelCase.

**Bad:**

```java
record userprofile(String name, String email) { }
record orderDTO(String id) { }
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

record UserProfile(String name, String email) {
    UserProfile {
        requireNonNull(name);
        requireNonNull(email);
    }
}

record OrderDto(String id) {
    OrderDto {
        requireNonNull(id);
    }
}
```

---

## R-006b

Record names must be **nouns** that describe the data model, not actions.

**Bad:**

```java
record ValidateUser(String name, String email) { }
record ProcessPayment(String id, BigDecimal amount) { }
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

record UserRegistration(String name, String email) {
    UserRegistration {
        requireNonNull(name);
        requireNonNull(email);
    }
}

record PaymentRequest(String id, BigDecimal amount) {
    PaymentRequest {
        requireNonNull(id);
        requireNonNull(amount);
    }
}
```

---

## R-006c

Record names must be **meaningful** and describe what data the record represents.

**Bad:**

```java
record Data(String value) { }
record Thing(String a, String b) { }
record Info(String text) { }
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

record CustomerAddress(String street, String city) {
    CustomerAddress {
        requireNonNull(street);
        requireNonNull(city);
    }
}

record InvoiceSummary(String invoiceId, BigDecimal total) {
    InvoiceSummary {
        requireNonNull(invoiceId);
        requireNonNull(total);
    }
}

record LoginCredentials(String username, String password) {
    LoginCredentials {
        requireNonNull(username);
        requireNonNull(password);
    }
}
```

---

## R-006d

Record names must be more than one character long. Single-letter names are forbidden.

**Bad:**

```java
record A(String value) { }
record X(int id) { }
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

record Account(String value) {
    Account {
        requireNonNull(value);
    }
}

record XmlEntry(int id) {
}
```

