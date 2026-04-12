ingore the stuff below, this is just a scratchpad 
for the java clean code session

Comments

user iterators in for

never variables without initialization

avoid reassignment of variables, prefer immutability, exceptions for control flow, 

Magic Numbers and Strings

Unused variables and parameters	Remove if safe; prefix with _ when removal changes a public interface
Unreachable code after return/throw/break/continue	Remove the unreachable statements
Commented-out code blocks	Remove entirely. Version control preserves history
Empty blocks (catch, if, else) with no side effects	Remove the block and its condition if applicable
Unused private methods/functions	Remove the definition

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