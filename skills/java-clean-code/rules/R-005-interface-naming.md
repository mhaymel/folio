# Interface Naming Rules

## R-005a

Interface names must start with an uppercase letter and use CamelCase.

**Bad:**

```java
interface paymentprocessor { }
interface userRepository { }
```

**Good:**

```java
interface PaymentProcessor { }
interface UserRepository { }
```

---

## R-005b

Interface names must be **nouns** or **capability adjectives** that describe a contract (e.g., `PaymentGateway`, `Runnable`), not vague actions.

**Bad:**

```java
interface DoPayment { }
interface MakeStuff { }
```

**Good:**

```java
interface PaymentGateway { }
interface Runnable { }
```

---

## R-005c

Do not prefix interface names with `I` (e.g., `IUserService`). Name the contract directly.

**Bad:**

```java
interface IUserService { }
interface IDataRepository { }
```

**Good:**

```java
interface UserService { }
interface DataRepository { }
```

---

## R-005d

Interface names must be **meaningful** and clearly describe the behavior promised by the contract.

**Bad:**

```java
interface Helper { }
interface Thing { }
interface Manager { }
```

**Good:**

```java
interface InvoiceGenerator { }
interface CustomerRepository { }
interface OrderValidator { }
```

