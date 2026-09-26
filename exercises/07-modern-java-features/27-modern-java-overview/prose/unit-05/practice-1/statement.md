Java 21's pattern matching for `switch` (JEP 441) switches on any object with
type patterns (`case Integer i`), guards (`case Integer i when i < 0`) and
`case null`. The page's pitfalls: without a `case null`, a `null` selector
throws `NullPointerException`, because **`default` does not match `null`**; and
the first matching case wins, so the compiler rejects a plain `case Integer i`
placed before a guarded `case Integer i when ...`.

Write `Values.format(Object obj)` as one `switch` expression:

| obj | answer |
|---|---|
| `null` | `"null"` |
| an `Integer` below 0 | `"negative integer " + i` |
| any other `Integer` | `"non-negative integer " + i` |
| a blank `String` (empty, or only whitespace, Unicode spaces included) | `"blank string"` |
| any other `String` | `"string " + s` |
| an empty `List` | `"empty list"` |
| any other `List` | `"list of " + size` |
| anything else | `"other: " + obj.getClass().getSimpleName()` |

For example, `format(-5)` is `"negative integer -5"`, `format(0)` is
`"non-negative integer 0"`, `format("  ")` is `"blank string"` and
`format(2.5)` is `"other: Double"`. Only a `List` has list rows: a `Set` is
"anything else".
