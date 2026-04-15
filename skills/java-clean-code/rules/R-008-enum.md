# Enum Rules

## R-008a

Enum names must start with an uppercase letter and use CamelCase.

**Bad:**

```java
enum orderStatus { }
enum paymentType { }
```

**Good:**

```java
enum OrderStatus { }
enum PaymentType { }
```

---

## R-008b

Enum names must be **nouns** that describe the category of values, not verbs or actions.

**Bad:**

```java
enum ProcessPayment { }
enum ValidateStatus { }
```

**Good:**

```java
enum PaymentMethod { }
enum ValidationStatus { }
```

---

## R-008c

Enum names must be **meaningful** and clearly describe what the set of constants represents.

**Bad:**

```java
enum Type { }
enum Kind { }
enum Flag { }
```

**Good:**

```java
enum InvoiceStatus { }
enum ShippingMethod { }
enum UserRole { }
```

---

## R-008d

Enum names must be more than one character long. Single-letter names are forbidden.

**Bad:**

```java
enum A { }
enum S { }
```

**Good:**

```java
enum Availability { }
enum Severity { }
```

---

## R-008e

Enum constants must be written in UPPER_SNAKE_CASE.

**Bad:**

```java
enum OrderStatus {
    pending,
    InProgress,
    shipped
}
```

**Good:**

```java
enum OrderStatus {
    PENDING,
    IN_PROGRESS,
    SHIPPED
}
```

