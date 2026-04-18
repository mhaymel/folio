---
description: Audit the java-clean-code skill's rule examples for cross-rule compliance.
argument-hint: "[rule-id]  (optional — e.g. R-019 to scope the audit to one rule file)"
---

# Audit java-clean-code examples

Goal: every **Good** code block in `skills/java-clean-code/rules/*.md` must comply with **every** sub-rule across **every** rule file — not just the sub-rule it illustrates. Every **Bad** code block must violate the sub-rule it illustrates and should not incidentally violate other sub-rules in ways that muddy the lesson.

This audit exists because rule examples are hand-written in isolation and drift out of compliance with rules introduced in other files (e.g. a concurrency example that forgets R-002f's primary-constructor rule).

## Scope

- If `$ARGUMENTS` is empty → audit every file in `skills/java-clean-code/rules/`.
- If `$ARGUMENTS` is a rule id like `R-019` → audit only `skills/java-clean-code/rules/R-019-*.md`.

## Procedure

1. **Load all rule files.** Read every file under `skills/java-clean-code/rules/` with the Read tool. Do not rely on memory. You need the full text of every sub-rule to do the cross-check.

2. **Enumerate code blocks in the audit scope.** For each rule file in scope, list every fenced ```java block with:
   - its rule file and sub-rule (e.g. `R-019b`)
   - its label (`Bad`, `Good`, `Good (restore the flag)`, etc.)
   - the type(s) it declares: class / record / enum / interface / tiny type

3. **Determine applicable rules per block.** Derive applicability from the topic of each rule file (read from its title/heading, not its filename or ID), then match against what the snippet declares:

   - **Universal** (apply to every block): class naming, imports, programming-by-contract, method naming, package/file naming, comments.
   - **Type-specific** (apply only when the block declares that type): class-design and class-field rules for classes; record-naming and record-design rules for records; enum rules for enums; interface-naming and interface-design rules for interfaces; tiny-type rules for tiny types.
   - **Content-driven** (apply when the snippet contains the relevant construct): method-code and method-design rules when methods are present; local-variable rules when local variables are used; exception-handling rules when `throw`/`catch`/`try` is present; concurrency rules when threads, locks, atomics, or executors are used; unit-test rules when test classes/methods are present.

   Sub-rule IDs are only known after reading the files — do not hardcode them here, as rule file names and IDs may change.

   Snippets are *illustrative*. Ignore violations that only exist because the snippet is stripped down — missing package declarations, omitted imports, elided method bodies, abbreviated classes with no companion files. Only flag a violation when the snippet as written, taken on its own, breaks a sub-rule.

4. **Check each Good block against every applicable sub-rule.** For each (block × sub-rule) pair, record: `ok` or `fail`, with a short note citing the offending line when `fail`.

5. **Check each Bad block lightly.** Confirm it violates the sub-rule it illustrates. Flag only *incidental* violations that would confuse the reader about what the example is teaching.

6. **Emit one report.** Do not fix anything. The output is a single markdown report with this structure:

   ```
   ## Summary
   <N> Good blocks audited, <M> failing. <K> Bad blocks flagged as muddy.

   ## Good-block violations

   | File | Sub-rule illustrated | Violates | Note |
   |------|---------------------|----------|------|
   | R-019-concurrency.md | R-019b (FetchTracker) | R-002f, R-002i | no-arg ctor initializes internal AtomicBoolean/AtomicLong fields; primary ctor must take one param per field and be private |

   ## Bad-block incidental issues (optional)

   | File | Sub-rule illustrated | Incidental issue | Note |

   ## Clean files
   - R-001-class-naming.md — all blocks ok
   - ...
   ```

7. **Stop after the report.** Do not modify any rule file. The user will decide which violations to fix and whether to run a follow-up pass.

## Notes

- Be strict but not pedantic. A `Bad` block intentionally breaks rules — don't pile on.
- If two sub-rules conflict for a given snippet, call that out in the report rather than silently picking one.
- If a rule file itself is ambiguous (a sub-rule you can't check without guessing), note it under a **Rule-text issues** section at the end of the report rather than inventing a verdict.