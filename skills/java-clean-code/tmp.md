this is just a scratchpad for possible rules to add to the skill, not yet organized or finalized


number of classes, records, interfaces and enums in one 
package should be limited to 10

DiversificationEntry
HoldingDto



Names are searchable

Unreachable code after return/throw/break/continue	Remove the unreachable statements
Empty blocks (catch, if, else) with no side effects	Remove the block and its condition if applicable

R-003q (primitive obsession in fields/constructor params)
Controller endpoints accept raw String
isin/name/ticker/currency/format/sortField/sortDir — they're request params,
but the same obsession appears internally (filter.isin() etc.).



user iterators in for



given when then
all rules applies to java test code

e2e tests, ui tests

limit number of classes, records, interfaces and enums in one package
unit test for each java class
no final at variables, parameters
records, test null checks.
maximale zeilen laenge fest legen

assertThrowsIAE


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


https://github.com/AbsolutelySkilled/AbsolutelySkilled/blob/main/skills/clean-architecture/SKILL.md


https://skills.sh/jkappers/agent-skills/clean-code
Pass 2: Import and Dependency Cleanup
Sort imports by category: standard library, external packages, internal modules. Remove duplicate imports. Consolidate multiple imports from the same module into a single statement.

Follow the project's import convention if one is defined. Otherwise, detect the dominant pattern in the file.

Pass 3: Structure Simplification
Reduce nesting with early returns. Convert deeply nested conditionals into guard clauses that return/throw/continue early.

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
Flatten unnecessary wrappers. Remove single-use wrapper functions that add no abstraction value — functions whose body is a single call to another function with the same arguments.

Collapse single-branch conditionals. An if with no else that wraps the entire function body becomes a guard clause.

Replace nested ternaries. Convert nested ternary/conditional expressions into if/else chains, switch/match statements, or lookup tables.

Pass 4: Redundancy Elimination
Pattern	Resolution
Duplicate logic across branches	Extract to a shared block before/after the conditional
Redundant boolean comparisons (== true, == false)	Use the expression directly or negate it

Repeated string/number literals (3+ occurrences)	Extract to a named constant
Identity transformations (map(x => x), .filter(() => true))	Remove the no-op call
Unnecessary intermediate variables used once and immediately returned	Return the expression directly
Re-assignment to self (x = x)	Remove the statement
Pass 5: Expression Simplification
Simplify boolean logic:

!(!x) → x
a && a → a
if (cond) return true; else return false; → return cond;
x !== null && x !== undefined → use nullish checks when the language supports them
Simplify arithmetic:

x * 1 → x, x + 0 → x, x * 0 → 0
Simplify string operations:

str + "" → str (when already a string)
Consecutive string concatenations → template literals or format strings when the language supports them


https://skills.sh/doubleslashse/claude-marketplace/clean-code
Rule of Three
Only extract duplication after you've seen it THREE times:

First occurrence - just write the code
Second occurrence - note it, consider extraction
Third occurrence - refactor to remove duplication


Premature Abstraction
// BAD: Abstraction for one implementation
public interface IOrderIdGenerator
{
string Generate();
}

public class GuidOrderIdGenerator : IOrderIdGenerator
{
public string Generate() => Guid.NewGuid().ToString();
}

// Registration
services.AddSingleton<IOrderIdGenerator, GuidOrderIdGenerator>();

// GOOD: Direct until you need flexibility
public class Order
{
public string Id { get; } = Guid.NewGuid().ToString();
}

// Add abstraction ONLY when you need a second implementation


YAGNI - You Aren't Gonna Need It
Don't implement something until it is necessary.

Feature Creep
// BAD: Building for hypothetical future requirements
public class UserService
{
public User CreateUser(
string email,
string name,
string? middleName = null,           // No requirement for this
string? suffix = null,                // No requirement for this
string? preferredName = null,         // No requirement for this
bool enableTwoFactor = false,         // No requirement for this
string? backupEmail = null,           // No requirement for this
Dictionary<string, string>? metadata = null)  // "Might need it later"
{
// ...
}
}

// GOOD: Only what's needed now
public class UserService
{
public User CreateUser(string email, string name)
{
return new User
{
Id = Guid.NewGuid(),
Email = email,
Name = name,
CreatedAt = DateTime.UtcNow
};
}
}
Unnecessary Flexibility
// BAD: Configurable everything (but we only use JSON)
public interface ISerializer
{
string Serialize<T>(T obj);
T Deserialize<T>(string data);
}

public class JsonSerializer : ISerializer { }
public class XmlSerializer : ISerializer { }
public class YamlSerializer : ISerializer { }
public class BinarySerializer : ISerializer { }
public class MessagePackSerializer : ISerializer { }

public class SerializerFactory
{
public ISerializer Create(string format) => // ...
}

// GOOD: Use what you need
public static class JsonHelper
{
private static readonly JsonSerializerOptions Options = new()
{
PropertyNamingPolicy = JsonNamingPolicy.CamelCase
};

    public static string Serialize<T>(T obj) =>
        System.Text.Json.JsonSerializer.Serialize(obj, Options);

    public static T? Deserialize<T>(string json) =>
        System.Text.Json.JsonSerializer.Deserialize<T>(json, Options);
}
Unused Abstractions
// BAD: Interface with single implementation, no plans for others
public interface IEmailSender
{
Task SendAsync(string to, string subject, string body);
}

public class SmtpEmailSender : IEmailSender
{
public Task SendAsync(string to, string subject, string body)
{
// Only implementation we'll ever have
}
}

// GOOD: Just use the class directly
public class EmailSender
{
public async Task SendAsync(string to, string subject, string body)
{
// ...
}
}

// Add interface WHEN you actually need a second implementation
Premature Optimization
// BAD: Caching before measuring
public class ProductService
{
private readonly IMemoryCache _cache;
private readonly IDistributedCache _distributedCache;
private readonly IProductRepository _repository;

    public async Task<Product?> GetByIdAsync(int id)
    {
        var cacheKey = $"product_{id}";

        // Check L1 cache
        if (_cache.TryGetValue(cacheKey, out Product? product))
            return product;

        // Check L2 cache
        var cached = await _distributedCache.GetStringAsync(cacheKey);
        if (cached != null)
        {
            product = JsonSerializer.Deserialize<Product>(cached);
            _cache.Set(cacheKey, product, TimeSpan.FromMinutes(5));
            return product;
        }

        // Database fallback
        product = await _repository.GetByIdAsync(id);
        if (product != null)
        {
            var serialized = JsonSerializer.Serialize(product);
            await _distributedCache.SetStringAsync(cacheKey, serialized);
            _cache.Set(cacheKey, product, TimeSpan.FromMinutes(5));
        }

        return product;
    }
}

// GOOD: Start simple, optimize when needed
public class ProductService
{
private readonly IProductRepository _repository;

    public Task<Product?> GetByIdAsync(int id) =>
        _repository.GetByIdAsync(id);
}

// Add caching AFTER you've identified it as a bottleneck


https://skills.sh/doubleslashse/claude-marketplace/clean-code
Functions
Small (< 20 lines preferred)
Do one thing
One level of abstraction
Few parameters (< 3 preferred)
No side effects
Command/Query separation


Error Handling
Exceptions, not error codes
Specific exception types
No empty catch blocks
Fail fast principle


Avoid Mental Mapping
// BAD
for (int i = 0; i < users.Length; i++)
for (int j = 0; j < users[i].Orders.Length; j++)
Process(users[i].Orders[j]);

// GOOD
foreach (var user in users)
foreach (var order in user.Orders)
Process(order);




Newspaper Metaphor
Headline (class name)
Synopsis (public interface)
Details (private implementation)
Supporting details (utility methods)

https://github.com/grndlvl/software-patterns/blob/HEAD/.claude/skills/clean-code/practices/functions.md

"Functions should do one thing. They should do it well. They should do it only." — Robert C. Martin

https://github.com/grndlvl/software-patterns/blob/HEAD/.claude/skills/clean-code/practices/comments.md
Comments
Core Principle
"The proper use of comments is to compensate for our failure to express ourselves in code." — Robert C. Martin

Comments are, at best, a necessary evil. The best comment is the one you found a way not to write.

https://github.com/grndlvl/software-patterns/blob/HEAD/.claude/skills/clean-code/practices/code-smells.md



https://skills.sh/booklib-ai/skills/clean-code-reviewer
2. Functions (Ch. 3)
   Small: Functions should be small. Then smaller than that. Rarely should a function be 20 lines. Blocks within if, else, and while should be one line — probably a function call.
   Do one thing: A function should do one thing, do it well, and do it only. If you can extract a meaningfully named function from it, it's doing more than one thing.
   One level of abstraction per function: Don't mix high-level intent (getHtml()) with low-level details (PathParser.render(pagePath)). Read code like a top-down narrative: each function leads to the next level of abstraction (the Stepdown Rule).
   Switch statements: By their nature, switches do N things. Bury them in an Abstract Factory (or a registry/map from type → handler) that uses polymorphism. The switch appears once, hidden behind the factory interface; all callers see only the abstraction. Tolerate a switch only if it appears exactly once, creates polymorphic objects, and is invisible to the rest of the system. A switch on a type tag (PaymentType.CREDIT, PaymentType.PAYPAL) that keeps growing as new types are added is the classic OCP violation — the solution is not an enum enrichment but a polymorphic type hierarchy constructed by a factory.
   Descriptive names: A long descriptive name is better than a short enigmatic name. A long descriptive name is better than a long descriptive comment. Be consistent in naming: includeSetupAndTeardownPages, includeSetupPages, includeSuiteSetupPage.
   Function arguments:
   Zero (niladic) is best, one (monadic) is fine, two (dyadic) is harder, three (triadic) — needs strong justification. More than three: extract into an argument object.
   Common monadic forms: asking a question about the arg (isFileExists(file)), transforming the arg (fileOpen(name) → InputStream), or an event (no output, passwordAttemptFailedNtimes(attempts)).
   Flag arguments are ugly (F3): Passing a boolean loudly declares the function does more than one thing. Split into two functions.
   Dyadic: writeField(name) is clearer than writeField(outputStream, name). Consider making outputStream a member variable.
   Argument objects: When a function needs 2–3+ args, consider wrapping them. makeCircle(double x, double y, double radius) → makeCircle(Point center, double radius).- No side effects: A function named checkPassword shouldn't also initialize a session. That's a temporal coupling hidden as a side effect.
   G31 — Hidden Temporal Couplings: When callers must invoke methods in a specific order to get correct behaviour, that ordering constraint must be visible in the API — not buried in a comment. One approach: each step returns an intermediate result type that the next step requires as its argument, so the compiler enforces the sequence. builder.addHeader(title, date) returning a HeaderAdded that is the required argument to .addBody(records) is impossible to call out of order; a void method with a "call me third" comment is not.
   Output arguments: appendFooter(s) — is s being appended to, or is s the thing being appended? Output arguments are counterintuitive (F2). In OO: report.appendFooter(). The object-oriented fix is always the same: the object should own its own state. A ReportBuilder that takes a List<String> from callers should instead maintain that list internally and expose methods that mutate it — callers accumulate state by calling methods, not by passing a shared buffer around.
   Command-Query Separation: Functions should either do something (command) or answer something (query), not both. if (set("username", "unclebob")) is confusing.
   Prefer exceptions to error codes: Error codes force nested if chains and violate command-query separation. Extract try/catch bodies into their own functions. Error handling is one thing (a function that handles errors should do nothing else).
   DRY: Duplication is the root of all evil in software. Duplication may be the source of many other principles (Codd's database normal forms, OO, structured programming are all strategies for eliminating duplication).
   Guard clauses / early returns flatten nesting: Deeply nested positive conditionals are a readability smell. Invert conditions to exit early — the happy path becomes the linear path. If the deepest if block is where the real work happens, that's a sign the function needs guard clauses.



https://skills.sh/pproenca/dot-skills/uncle-bob-clean-code-best-practices
Quick Reference
1. Meaningful Names (CRITICAL)
   name-intention-revealing - Use names that reveal intent
   name-avoid-disinformation - Avoid misleading names
   name-meaningful-distinctions - Make meaningful distinctions
   name-pronounceable - Use pronounceable names
   name-searchable - Use searchable names
   name-avoid-encodings - Avoid encodings in names
   name-class-noun - Use noun phrases for class names
   name-method-verb - Use verb phrases for method names
2. Functions (CRITICAL)
   func-small - Keep functions small
   func-one-thing - Functions should do one thing
   func-abstraction-level - Maintain one level of abstraction
   func-minimize-arguments - Minimize function arguments
   func-no-side-effects - Avoid side effects
   func-command-query-separation - Separate commands from queries
   func-prefer-exceptions - Prefer exceptions to error codes
   func-dry - Do not repeat yourself
3. Comments (HIGH)
   cmt-express-in-code - Express yourself in code, not comments
   cmt-explain-intent - Use comments to explain intent
   cmt-avoid-redundant - Avoid redundant comments
   cmt-avoid-commented-out-code - Delete commented-out code
   cmt-warning-consequences - Use warning comments for consequences
4. Formatting (HIGH)
   fmt-vertical-formatting - Use vertical formatting for readability
   fmt-horizontal-alignment - Avoid horizontal alignment
   fmt-team-rules - Follow team formatting rules
   fmt-indentation - Respect indentation rules
5. Objects and Data Structures (MEDIUM-HIGH)
   obj-data-abstraction - Hide data behind abstractions
   obj-data-object-asymmetry - Understand data/object anti-symmetry
   obj-law-of-demeter - Follow the Law of Demeter
   obj-avoid-hybrids - Avoid hybrid data-object structures
   obj-dto - Use DTOs for data transfer
6. Error Handling (MEDIUM-HIGH)
   err-use-exceptions - Use exceptions instead of return codes
   err-write-try-catch-first - Write try-catch-finally first
   err-provide-context - Provide context with exceptions
   err-define-by-caller-needs - Define exceptions by caller needs
   err-avoid-null - Avoid returning and passing null
7. Unit Tests (MEDIUM)
   test-first-law - Follow the three laws of TDD
   test-keep-clean - Keep tests clean
   test-one-assert - One assert per test
   test-first-principles - Follow FIRST principles
   test-build-operate-check - Use Build-Operate-Check pattern
8. Classes and Systems (MEDIUM)
   class-small - Keep classes small
   class-cohesion - Maintain class cohesion
   class-organize-for-change - Organize classes for change
   class-isolate-from-change - Isolate classes from change
   class-separate-concerns - Separate construction from use


https://skills.sh/andrewgleave/skills/cleanse
1. Flatten control flow
   Nested conditionals are the single biggest readability killer. Flatten them.

Convert if/else chains to early returns and guard clauses. Put the bail-out condition at the top, happy path below.
When a function has a single meaningful path wrapped in a conditional, invert the condition and return early.
If an else block just returns or continues, remove the else — the early return already handles it.


3. Simplify logic
   Collapse trivial if/else into a single expression where the language supports it (ternary, nil-coalescing, pattern matching).
   Replace boolean flags that track state across a loop with early exits or functional transforms (map, filter, first).
   Inline one-use helpers that are short enough to read in place. Don't extract helpers for one-time operations — three similar lines is better than a premature abstraction.

Move declarations close to usage
Variables defined at the top of a function but not used until line 40 force the reader to hold them in mental memory. Move declarations to just before first use. This makes extraction easier later.

6. Break up long functions
   If a function is doing multiple distinct things sequentially, split it — each piece should do one thing and be readable on its own. Apply the same passes above to each extracted function. If extracting would require threading many parameters or the logic is deeply interleaved, don't force it — flag it as a readability concern instead.


https://skills.sh/d-o-hub/rust-self-learning-memory/clean-code-developer
Testability
Dependencies easily mocked
Pure functions where possible
Clear inputs and outputs
Testable in isolation


https://skills.sh/site/skills.volces.com/clean-code
NEVER Do
NEVER add comments that restate the code — if the code needs a comment to explain what it does, rename things until it doesn't
NEVER create abstractions for fewer than 3 use cases — premature abstraction is worse than duplication
NEVER leave commented-out code in the codebase — delete it; version control exists for history
NEVER write functions longer than 20 lines — extract sub-functions until each does one thing
NEVER nest deeper than 2 levels — use guard clauses, early returns, or extract functions
NEVER use magic numbers or strings — define named constants with clear semantics
NEVER edit a file without checking what depends on it — broken imports and missing updates are the most common source of bugs in multi-file changes
NEVER leave a task with failing lint or type checks — fix all errors before marking complete


Further Reading
This skill is based on Robert C. Martin's seminal guide to software craftsmanship:

"Clean Code: A Handbook of Agile Software Craftsmanship" by Robert C. Martin
"The Clean Coder: A Code of Conduct for Professional Programmers" by Robert C. Martin
"Clean Architecture: A Craftsman's Guide to Software Structure and Design" by Robert C. Martin
"Refactoring: Improving the Design of Existing Code" by Martin Fowler




