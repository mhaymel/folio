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
- `R-010-method.md`
- `R-011-tiny-type.md`

---

## Procedure

Follow these steps **in order** when applying clean code rules:

1. **Load all rule files.** Use the `read_file` tool to load every file listed above. Do not rely on memory or summaries.
2. **Collect all Java source files.** Use `file_search` with `**/*.java` to find every Java file under `src/main/java`. Do **not** limit this to a subset — every file must be checked.
3. **Read every Java file.** Use `read_file` on each file. Do not skip any file.
4. **Check every file against every applicable sub-rule.** For each Java file determine its type (class, record, enum, interface) and check **every** sub-rule (a, b, c, …) from every applicable rule file. All rule files R-008 and R-009 apply to all types. Do not skip any sub-rule.
5. **Report violations per file.** For each file, list every sub-rule that is violated. If a file passes all rules, state that explicitly. Present the results as a table: File | Violation | Sub-rule.
6. **Fix all violations.** Apply changes for every violation found. After fixing a file, use `get_errors` to verify it compiles.
7. **Compile and run tests.** Run `gradlew :backend:compileJava` and `gradlew :backend:test`. All must pass.
8. **Verify completeness.** Re-read every file that was modified and confirm no sub-rule was missed. If a fix introduced a new violation (e.g. a new class was created), check that new file against all rules too.
