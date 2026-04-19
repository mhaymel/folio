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

Rule files state the rule and show **Bad** / **Good** examples. They typically do **not** explain *why* something is bad or *why* something is good. A rationale is included only when it is necessary to understand the transformation from Bad to Good.

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
- [R-013-method-visibility](rules/R-013-method-visibility.md)
- [R-014-method-body](rules/R-014-method-body.md)
- [R-015-method-parameter](rules/R-015-method-parameter.md)
- [R-016-tiny-type](rules/R-016-tiny-type.md)
- [R-017-package-and-file-naming](rules/R-017-package-and-file-naming.md)
- [R-018-local-variable](rules/R-018-local-variable.md)
- [R-019-comment](rules/R-019-comment.md)
- [R-020-exception-handling](rules/R-020-exception-handling.md)
- [R-021-concurrency](rules/R-021-concurrency.md)
- [R-022-unit-test](rules/R-022-unit-test.md)
- [R-023-stream-lambdas](rules/R-023-stream-lambdas.md)
- [R-024-logging](rules/R-024-logging.md)
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
8. **Self-review (mandatory gate — do not skip).** Before declaring the task complete, produce an explicit evidence matrix. This step exists because rules read at step 1 are often forgotten by the time fixes are applied, and newly-created types (records, helper classes) are the most common miss.

   For **every** file that was created or modified in step 6 — including files created *as a side effect* of a fix (new records, extracted classes, new tiny types):

   1. Re-read the file with `read_file` (do not rely on your memory of what you wrote).
   2. Determine its type: class, record, enum, interface.
   3. Walk every applicable rule file and every sub-rule (a, b, c, …). Rules that apply to all types (R-001, R-009, R-010, R-011, R-017, R-019) must be checked for every file; type-specific rules (R-002/R-003 for classes, R-006/R-007 for records, R-008 for enums, R-004/R-005 for interfaces, R-016 for tiny types) only for matching types.
   4. Emit one row per (file × sub-rule) as a markdown table:

      | File | Rule | Sub-rule | Verdict | Note |
      |------|------|----------|---------|------|
      | `IsinNameDto.java` | R-007 | f (requireNonNull in compact ctor) | ok | `requireNonNull(isin); requireNonNull(name);` present |
      | `IsinNameDto.java` | R-007 | g (components immutable) | ok | `Isin` and `String` are immutable |

   5. If any verdict is `fail`, return to step 6 and fix it, then repeat step 8 for the affected files. Do **not** proceed past step 8 with any `fail` rows.
   6. New files created during fixing must be added to the matrix — the set of files to review grows until no new files appear.

   The skill is only complete when the matrix contains every modified file × every applicable sub-rule, and every verdict is `ok`.

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
