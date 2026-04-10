---
name: java-clean-code
description: A skill to provide guidelines and best practices for writing clean and maintainable Java code.
---

# Clean Code Skill

Write readable, maintainable code following Clean Code principles.

## When to Use

- User says "clean code", """clean this code" / "refactor" / "improve readability"
- Code review focusing on maintainability
- Reducing complexity
- Improving naming
- Refactoring
- Improve code quality

---

## Rules

Rules are organized by category in the `rules/` folder:

- `R-001-class-naming.md`
- `R-002-class-design.md`
- `R-003-interface-naming.md`
- `R-004-interface-design.md`
- `R-005-record-naming.md`
- `R-006-record-design.md`
- `R-007-enum-naming.md`
- `R-008-imports.md`
- `R-009-programming-by-contract.md`

---

## Procedure

Follow these steps **in order** when applying clean code rules:

1. **Load all rule files.** Use the `read_file` tool to load every file listed above. Do not rely on memory or summaries.
2. **Identify applicable rule files.** Determine which rule files apply to the code under review (e.g. R-001/R-002 for classes, R-005/R-006 for records, R-007 for enums, R-008 for imports, R-009 for preconditions).
3. **Walk through every sub-rule.** For each applicable rule file, check **every** sub-rule (a, b, c, …) against the code. Do not skip any.
4. **Report each sub-rule result.** For each sub-rule, state whether it passes or is violated.
5. **Fix all violations.** Apply changes for every violation found.
6. **Verify completeness.** After fixing, re-read the file and confirm no sub-rule was missed.
