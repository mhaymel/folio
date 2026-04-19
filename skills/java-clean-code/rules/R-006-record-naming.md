# Record Naming Rules

## R-006a

Record names must start with an uppercase letter and use CamelCase.

**Bad:**

```java
record userprofile(String name, String email) { }
record orderDTO(String id, String customerName) { }
```

**Good:**

```java
import static java.util.Objects.requireNonNull;

record Name(String value) {
    Name {
        requireNonNull(value);
    }
}

record Email(String value) {
    Email {
        requireNonNull(value);
    }
}

record OrderId(String value) {
    OrderId {
        requireNonNull(value);
    }
}

record UserProfile(Name name, Email email) {
    UserProfile {
        requireNonNull(name);
        requireNonNull(email);
    }
}

record OrderDto(OrderId id, Name customerName) {
    OrderDto {
        requireNonNull(id);
        requireNonNull(customerName);
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

record Name(String value) {
    Name {
        requireNonNull(value);
    }
}

record Email(String value) {
    Email {
        requireNonNull(value);
    }
}

record PaymentId(String value) {
    PaymentId {
        requireNonNull(value);
    }
}

record Money(BigDecimal amount, Currency currency) {
    Money {
        requireNonNull(amount);
        requireNonNull(currency);
    }
}

record UserRegistration(Name name, Email email) {
    UserRegistration {
        requireNonNull(name);
        requireNonNull(email);
    }
}

record PaymentRequest(PaymentId id, Money total) {
    PaymentRequest {
        requireNonNull(id);
        requireNonNull(total);
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

record Street(String value) {
    Street {
        requireNonNull(value);
    }
}

record City(String value) {
    City {
        requireNonNull(value);
    }
}

record InvoiceId(String value) {
    InvoiceId {
        requireNonNull(value);
    }
}

record Money(BigDecimal amount, Currency currency) {
    Money {
        requireNonNull(amount);
        requireNonNull(currency);
    }
}

record Username(String value) {
    Username {
        requireNonNull(value);
    }
}

record Password(String value) {
    Password {
        requireNonNull(value);
    }
}

record CustomerAddress(Street street, City city) {
    CustomerAddress {
        requireNonNull(street);
        requireNonNull(city);
    }
}

record InvoiceSummary(InvoiceId invoiceId, Money total) {
    InvoiceSummary {
        requireNonNull(invoiceId);
        requireNonNull(total);
    }
}

record LoginCredentials(Username username, Password password) {
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

record XmlEntry(int value) {
}
```

