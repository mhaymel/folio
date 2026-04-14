# Package and File Naming Rules

## R-015a

Package names must be all lowercase. Do not use uppercase letters, underscores, or hyphens.

**Bad:**

```
com.example.MyPackage
com.example.order_processing
com.example.user-management
```

**Good:**

```
com.example.mypackage
com.example.orderprocessing
com.example.usermanagement
```

---

## R-015b

Package names must be meaningful and reflect the domain or feature they contain. Avoid generic names like `util`, `misc`, `common`, or `general`.

**Bad:**

```
com.example.util
com.example.misc
com.example.stuff
com.example.common
```

**Good:**

```
com.example.pricing
com.example.portfolio
com.example.authentication
```

---

## R-015c

Each package must have a clear, single responsibility. Do not dump unrelated classes into the same package.

**Bad:**

```
com.example.service/
    UserService.java
    InvoiceService.java
    CurrencyConverter.java
    PdfExporter.java
    EmailSender.java
```

**Good:**

```
com.example.user/
    UserService.java
com.example.invoice/
    InvoiceService.java
com.example.currency/
    CurrencyConverter.java
```

---

## R-015d

Do not use plural package names. Use the singular form.

**Bad:**

```
com.example.users
com.example.orders
com.example.invoices
```

**Good:**

```
com.example.user
com.example.order
com.example.invoice
```

---

## R-015e

The Java source file name must exactly match the name of the public (or package-private) top-level type it contains, including case. One top-level type per file.

**Bad:**

```
userService.java        → contains class UserService
UserServiceImpl.java    → contains class UserService
OrderHelper.java        → contains class OrderProcessor and class OrderValidator
```

**Good:**

```
UserService.java        → contains class UserService
OrderProcessor.java     → contains class OrderProcessor
OrderValidator.java     → contains class OrderValidator
```

---

## R-015f

Do not use abbreviations or acronyms in package names unless the abbreviation is universally understood in the domain (e.g. `io`, `sql`). Spell out the full word.

**Bad:**

```
com.example.svc
com.example.mgmt
com.example.auth.usr
```

**Good:**

```
com.example.service
com.example.management
com.example.authentication.user
```

---

