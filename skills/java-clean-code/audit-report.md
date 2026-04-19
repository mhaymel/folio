# java-clean-code cross-rule audit

Scope: every rule file in `skills/java-clean-code/rules/` (~190 Good blocks).

## Summary

~190 Good blocks audited, **8 failing**. **0 Bad blocks flagged as muddy.**

Most failures are cross-rule drift: an example hand-written to teach rule X incidentally
violates rule Y from another file. Snippets that are merely stripped (missing imports,
elided bodies, undeclared external symbols) are not flagged.

## Good-block violations

| File | Sub-rule illustrated | Violates | Note |
|------|---------------------|----------|------|
| R-009-imports.md | R-009a (Good) | R-002s, R-003p | `private final List<String> names;` — `String` for a name is primitive obsession; should be `List<Name>` with a `Name` tiny type. The rule is about imports, so the field type is incidental. |
| R-011-method-naming.md | R-011f (Good) | R-007a | `final class User { private final Name name; Name name() { return name; } }` has only final fields and a getter — must be a record per R-007a. R-011 lacks the "classes used to illustrate" disclaimer that R-002 and R-003 carry. |
| R-012-method-code.md | R-012b (Good) | R-024a | Catch block calls `logWarning("Optional payment validation failed", exception);` — this bypasses SLF4J. R-024a mandates `LOG.warn(...)` via a `private static final Logger LOG = getLogger(...)`. |
| R-021-concurrency.md | R-021d (Good) | R-003o | `private final StringBuilder buffer;` — StringBuilder is a concrete class; R-003o requires the most general interface unless concrete-only behavior is needed and documented. No such documentation here. |
| R-021-concurrency.md | R-021a (Good) | R-014b / R-016 boundary | `record PriceSnapshot(Map<Isin, Money> prices)` defines a `Money findPrice(Isin isin)` method that returns `null` when the key is missing. Record methods default to package-private, so R-014b (public → Optional) does not apply, but the return can be null without a documenting comment (`// returns null when not found`). Also borderline under R-016b/e (wraps exactly one value) — see Rule-text issues. |
| R-023-stream-lambdas.md | R-023a (Good) | R-023c | `orders.forEach(this::process);` — `process` is a void method and therefore side-effecting; R-023c requires a plain `for-each` loop for side effects, not `Stream.forEach` / `Iterable.forEach`. The method-reference preference is correct, but the example places it in a context that R-023c forbids. |
| R-999-not-categorized.md | R-999e (Good) | R-002e, R-007g (by analogy) | `DataRecord` stores `Date generationTimestamp;` and `Date modificationTimestamp;` — `Date` is mutable and is explicitly called out as forbidden in R-007g's note ("`List<Date>` is not [immutable]"). Should be `Instant`. R-002e ("prefer immutable classes") applies. |
| R-999-not-categorized.md | R-999f (Good) | R-018l | Inside the `for` loop: `int orderTotal = orders.get(i).total();` — `orderTotal` is assigned and never read. R-018l forbids unused local variables. The rule teaches searchable names, but the snippet needs at least a `sum += orderTotal;` or similar to make the variable live. |

## Bad-block incidental issues

None worth flagging. Every Bad block teaches its sub-rule cleanly; any additional
violations present (raw `String` fields, missing constructors, undeclared imports)
are either the point of the Bad block or clearly subordinate to it.

## Rule-text issues

- **R-016b vs. R-016e — "one value" is ambiguous.** R-016b says "A tiny type must wrap
  exactly one value", which by a literal reading makes any single-component record
  (including `record Snapshot(Map<Isin, Money> prices)`) a tiny type and therefore
  subject to R-016e ("no business logic"). Every example in R-016, however, wraps a
  *primitive-shaped* value (`String`, `long`, `int`, `BigDecimal`). R-021a's `PriceSnapshot`
  record has a single Map component plus a `findPrice` lookup method. Either R-016b
  should be tightened to "wraps exactly one primitive value" (so collection-wrapping
  records are not tiny types), or R-021a's Good block should be rewritten to not look
  like a tiny type. As written, R-021a either violates R-016e or proves that R-016 does
  not really mean "exactly one value" in the general sense.

## Clean files

- R-001-class-naming.md — all blocks ok
- R-002-class-design.md — all blocks ok
- R-003-class-field.md — all blocks ok (field-scope snippets legitimately omit constructors)
- R-004-interface-naming.md — all blocks ok
- R-005-interface-design.md — all blocks ok
- R-006-record-naming.md — all blocks ok
- R-007-record-design.md — all blocks ok
- R-008-enum.md — all blocks ok
- R-010-programming-by-contract.md — all blocks ok
- R-013-method-visibility.md — all blocks ok
- R-014-method-body.md — all blocks ok
- R-015-method-parameter.md — all blocks ok
- R-016-tiny-type.md — all blocks ok
- R-017-package-and-file-naming.md — all blocks ok (text, not Java)
- R-018-local-variable.md — all blocks ok
- R-019-comment.md — all blocks ok
- R-020-exception-handling.md — all blocks ok
- R-022-unit-test.md — all blocks ok (test literals like `"US0378331005"` are allowed per R-018k's test-context exception)
- R-024-logging.md — all blocks ok
