# Record Naming Rules

## R-005a

Record names must start with an uppercase letter and use CamelCase.

**Bad:**

```java
record userprofile(String name, String email) { }
record orderDTO(String id) { }
```

**Good:**

```java
record UserProfile(String name, String email) { }
record OrderDto(String id) { }
```

---

## R-005b

Record names must be **nouns** that describe the data model, not actions.

**Bad:**

```java
record ValidateUser(String name, String email) { }
record ProcessPayment(String id, BigDecimal amount) { }
```

**Good:**

```java
record UserRegistration(String name, String email) { }
record PaymentRequest(String id, BigDecimal amount) { }
```

---

## R-005c

Record names must be **meaningful** and describe what data the record represents.

**Bad:**

```java
record Data(String value) { }
record Thing(String a, String b) { }
record Info(String text) { }
```

**Good:**

```java
record CustomerAddress(String street, String city) { }
record InvoiceSummary(String invoiceId, BigDecimal total) { }
record LoginCredentials(String username, String password) { }
```

---

## R-005d

Record names must be more than one character long. Single-letter names are forbidden.

**Bad:**

```java
record A(String value) { }
record X(int id) { }
```

**Good:**

```java
record Account(String value) { }
record XmlEntry(int id) { }
```

