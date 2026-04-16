---
name: java-clean-code
description: A skill to provide guidelines and best practices for writing clean and maintainable Java code.
---

# Java Clean Code Skill

Clean code is code that is easy to read and understand, maintainable, and extensible by any human developer, not just the original author. 

This skill provides guidelines and best practices for writing clean java code.

## When to Use

- User says "clean code", "clean this code" / "refactor" / "improve readability"
- Code review focusing on maintainability
- Reducing complexity
- Improving naming
- Refactoring
- Improve code quality

---

## Rules


Rules are organized by category in the `rules/` folder:

- [R-001-class-naming](rules/R-001-class-naming.md)
- [R-002-class-design](rules/R-002-class-design.md)
- [R-003-class-field](rules/R-003-class-field.md)
- [R-004-interface-naming](rules/R-004-interface-naming.md)
- [R-005-interface-design](rules/R-005-interface-design.md)
- [R-006-record-naming](rules/R-006-record-naming.md)
- [R-007-record-design](rules/R-007-record-design.md)
- [R-008-enum](rules/R-008-enum.md)
- [R-009-imports](rules/R-009-imports.md)
- [R-010-programming-by-contract](rules/R-010-programming-by-contract.md)
- [R-011-method-naming](rules/R-011-method-naming.md)
- [R-012-method-code](rules/R-012-method-code.md)
- [R-013-method-design](rules/R-013-method-design.md)
- [R-014-tiny-type](rules/R-014-tiny-type.md)
- [R-015-package-and-file-naming](rules/R-015-package-and-file-naming.md)
- [R-016-local-variable](rules/R-016-local-variable.md)
- [R-017-comment](rules/R-017-comment.md)
- [R-018-exception-handling](rules/R-018-exception-handling.md)
- [R-999-not-categorized](rules/R-999-not-categorized.md)

---

## Procedure

Follow these steps **in order** when applying clean code rules:

1. **Load all rule files.** Use the `read_file` tool to load every file listed above. Do not rely on memory or summaries.
2. **Collect all Java source files.** Use `file_search` with `**/*.java` to find every Java file under `src/main/java`. Do **not** limit this to a subset — every file must be checked.
3. **Read every Java file.** Use `read_file` on each file. Do not skip any file.
4. **Check every file against every applicable sub-rule.** For each Java file determine its type (class, record, enum, interface) and check **every** sub-rule (a, b, c, …) from every applicable rule file. All rule files R-001, R-010 and R-011 apply to all types. Do not skip any sub-rule.
5. **Report violations per file.** For each file, list every sub-rule that is violated. If a file passes all rules, state that explicitly. Present the results as a table: File | Violation | Sub-rule.
6. **Fix all violations.** Apply changes for every violation found. After fixing a file, use `get_errors` to verify it compiles.
7. **Compile and run tests.** Run `gradlew :backend:compileJava` and `gradlew :backend:test`. All must pass.
8. **Verify completeness.** Re-read every file that was modified and confirm no sub-rule was missed. If a fix introduced a new violation (e.g. a new class was created), check that new file against all rules too.

## Data provenance & credits

This is only for a human reader to understand where the content in this skill comes from. The AI does not need to know this and must ignore this section when applying the rules.
T
his section documents where the content in this skill comes.

- clean code by https://en.wikipedia.org/wiki/Robert_C._Martin
- public Java clean-code best-practices
- mix of internal engineering guidelines
- personal experience and opinions of Markus Heumel
- generated with heavy use of GitHub copilot and claude code
- 
