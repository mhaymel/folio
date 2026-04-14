ingore the stuff below, this is just a scratchpad for the java clean code session








Unreachable code after return/throw/break/continue	Remove the unreachable statements
Commented-out code blocks	Remove entirely. Version control preserves history
Empty blocks (catch, if, else) with no side effects	Remove the block and its condition if applicable



user iterators in for

no nulls, no optionals as fields or parameters, no final at variables and parameters, tiny types for domain concepts (e.g. stock name),

given when then
all rules applies to java test code
tiny type for stock name
e2e tests, ui tests

limit number of classes, records, interfaces and enums in one package
unit test for each java class
no final at variables, parameters
records, test null checks.
maximale zeilen laenge fest legen

assertThrowsIAE

do not use fully qualified names

Optionals nicht als parameter, nicht als fields.

https://skills.sh/?q=clean+code

https://github.com/sickn33/antigravity-awesome-skills/blob/main/skills/clean-code/SKILL.md
This skill embodies the principles of "Clean Code" by Robert C. Martin (Uncle Bob). 
Use it to transform "code that works" into "code that is clean."

https://github.com/decebals/claude-code-java/blob/main/.claude/skills/clean-code/SKILL.md
https://github.com/decebals/claude-code-java/blob/main/.claude/skills/solid-principles/SKILL.md

https://skills.sh/jkappers/agent-skills/clean-code
Reduce nesting with early returns. Convert deeply nested conditionals into guard clauses that return/throw/continue early.

https://github.com/grndlvl/software-patterns/tree/main/.claude/skills/clean-code/practices




https://skills.sh/hainamchung/agent-assistant/clean-code







https://skills.sh/decebals/claude-code-java/clean-code
Avoid Flag Arguments
// BAD: Boolean flag changes behavior❌
public void sendMessage(String message, boolean isUrgent) {
if (isUrgent) {
// send immediately
} else {
// queue for later
}
}
// GOOD: Separate methods✅
public void sendUrgentMessage(String message) {
// send immediately
}
public void queueMessage(String message) {
// queue for later
}
--------------------------------------------
Good Comments
// GOOD: Explain WHY, not WHAT✅
// Retry with exponential backoff to avoid overwhelming the server
// during high load periods (see incident #1234)
for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
Thread.sleep((long) Math.pow(2, attempt) * 1000);
// ...
}
// TODO: Replace with Redis cache after infrastructure upgrade (Q2 2026)
private Map<String, User> userCache = new ConcurrentHashMap<>();
// WARNING: Order matters! Discounts must be applied before tax calculation
applyDiscounts(order);
calculateTax(order);
--------------------------------------------
Let Code Speak
// BAD: Comment explaining bad code❌
// Check if the user is an admin or has special permission
// and the action is allowed for their role
if ((user.getRole() == 1 || user.getRole() == 2) &&
(action == 3 || action == 4 || action == 7)) {
// ...
}
// GOOD: Self-documenting code✅
if (user.hasAdminPrivileges() && action.isAllowedFor(user.getRole())) {
// ...
}
--------------------------------------------
Magic Numbers
// BAD❌
if (user.getAge() >= 18) { }
if (order.getTotal() > 100) { }
Thread.sleep(86400000);
// GOOD✅
private static final int ADULT_AGE = 18;
private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("100");
private static final long ONE_DAY_MS = TimeUnit.DAYS.toMillis(1);
if (user.getAge() >= ADULT_AGE) { }
if (order.getTotal().compareTo(FREE_SHIPPING_THRESHOLD) > 0) { }
Thread.sleep(ONE_DAY_MS);
--------------------------------------------
Primitive Obsession
// BAD: Primitives everywhere❌
public void createUser(String email, String phone, String zipCode) {
// No validation, easy to mix up parameters
}
createUser("12345", "john@email.com", "555-1234"); // Wrong order, compiles!
// GOOD: Value objects✅
public record Email(String value) {
public Email {
if (!value.contains("@")) {
throw new IllegalArgumentException("Invalid email");
}
}
}
--------------------------------------------
Guard Clauses
// BAD: Deeply nested❌
public void processOrder(Order order) {
if (order != null) {
if (order.isValid()) {
if (order.hasItems()) {
// actual logic buried here
}
}
}
}
// GOOD: Guard clauses✅
public void processOrder(Order order) {
if (order == null) return;
if (!order.isValid()) return;
if (!order.hasItems()) return;
// actual logic at top level
}
--------------------------------------------
Functions / Methods
Keep Functions Small
// BAD: 50+ line method doing multiple things❌
public void processOrder(Order order) {
// validate order (10 lines)
// calculate totals (15 lines)
// apply discounts (10 lines)
// update inventory (10 lines)
// send notifications (10 lines)
// ... and more
}
// GOOD: Small, focused methods✅
public void processOrder(Order order) {
validateOrder(order);
calculateTotals(order);
applyDiscounts(order);
updateInventory(order);
sendNotifications(order);
}
--------------------------------------------
Single Level of Abstraction
// BAD: Mixed abstraction levels❌
public void processOrder(Order order) {
validateOrder(order); // High level
// Low level mixed in
BigDecimal total = BigDecimal.ZERO;
for (OrderItem item : order.getItems()) {
total = total.add(item.getPrice().multiply(
BigDecimal.valueOf(item.getQuantity())));
}
sendEmail(order); // High level again
}
// GOOD: Consistent abstraction level✅
public void processOrder(Order order) {
validateOrder(order);
calculateTotal(order);
sendConfirmation(order);
}
private BigDecimal calculateTotal(Order order) {
return order.getItems().stream()
.map(item -> item.getPrice().multiply(
BigDecimal.valueOf(item.getQuantity())))
.reduce(BigDecimal.ZERO, BigDecimal::add);
}
==================================================================
https://github.com/AbsolutelySkilled/AbsolutelySkilled/blob/main/skills/clean-
code/references
Large Class (God Object)
Symptom: ..., too many methods,
--------------------------------------------
Commented-Out Code
# Bad: dead code hiding in comments
# total = calculate_legacy_total(order)
total = calculate_total(order)
Delete it. Git has history.
--------------------------------------------
Use pronounceable names
If you can't say it out loud, it's hard to discuss in code reviews and meetings.
// Bad
Date genymdhms; // generation year-month-day-hour-minute-second
// Good
Date generationTimestamp;
--------------------------------------------
Constants
Use SCREAMING_SNAKE_CASE. The name
--------------------------------------------
Naming consistency
Pick one word per concept
Choose one synonym and use it everywhere. Don't mix:
fetch / retrieve / get / load - pick one
create / make / build / generate - pick one
controller / manager / handler - pick one
Use domain language
Use the terms your business domain uses (Domain-Driven Design's "ubiquitous
language").
If the business says "policy," don't call it "rule" in code.
--------------------------------------------
SOLID Principles
--------------------------------------------
O - Open/Closed Principle (OCP)
--------------------------------------------
Liskov Substitution Principle (LSP)
--------------------------------------------
Interface Segregation Principle (ISP)
==================================================
https://github.com/jkappers/agent-skills/blob/main/skills/clean-code/SKILL.md
Empty blocks (catch, if, else) with no side effects
--------------------------------------------
Structure Simplification
Reduce nesting with early returns. Convert deeply nested conditionals into guard
clauses that return/throw/continue early.
# Before
def process(item):
if item is not None:
if item.is_valid():
if item.status == "active":
return handle(item)
return None
# After
def process(item):
if item is None:
return None
if not item.is_valid():
return None
if item.status != "active":
return None
return handle(item)
--------------------------------------------
Redundant boolean comparisons (== true, == false)
===================================================
https://github.com/grndlvl/software-patterns/blob/main/.claude/skills/clean-
code/SKILL.md
When This Skill Activates
This skill automatically activates when you:
Review code for quality or maintainability
Need guidance on naming conventions
Discuss refactoring or code smells
Apply SOLID principles
Improve code readability or structure
Write or review unit tests
https://github.com/grndlvl/software-patterns/blob/main/.claude/skills/clean-
code/practices/code-smells.md
Code Smells
https://github.com/grndlvl/software-patterns/blob/main/.claude/skills/clean-
code/practices/comments.md
comments
https://github.com/grndlvl/software-patterns/blob/main/.claude/skills/clean-
code/practices/error-handling.md
error handling and Exceptions

avoid mocking