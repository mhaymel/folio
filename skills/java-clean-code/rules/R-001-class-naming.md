# Class Naming Rules

## R-001a

Class names must start with an uppercase letter and use CamelCase.

**Bad:**

```java
final class orderprocessor { }
final class userRepository { }
```

**Good:**

```java
final class OrderProcessor { }
final class UserRepository { }
```

---

## R-001b

Class names must be **nouns** (e.g., `UserRepository`, `PaymentService`), not verbs or actions.

**Bad:**

```java
final class ValidateUser { }
final class DoThings { }
```

**Good:**

```java
final class UserValidator { }
final class DataProcessor { }
```

---

## R-001c

Class names must be **meaningful** and clearly describe the class's single responsibility — a reader should immediately understand what the class does from its name alone.

**Bad:**

```java
final class Thing { }
final class Stuff { }
final class Helper { }
```

**Good:**

```java
final class InvoiceGenerator { }
final class CustomerRepository { }
final class OrderValidator { }
```

---

## R-001d

Class names must be **more than one character** long. Single-letter class names like `A`, `B`, `X` are forbidden.

**Bad:**

```java
final class A { }
final class D { }
final class X { }
```

**Good:**

```java
final class Application { }
final class Database { }
final class XMLParser { }
```

---

## R-001e

Underscores are forbidden in class names.

**Bad:**

```java
final class Order_Processor { }
final class User_Repository { }
```

**Good:**

```java
final class OrderProcessor { }
final class UserRepository { }
```

