Pattern matching for `instanceof` tests a type and binds a variable in one step:
`obj instanceof String s`. The page's scope rules: with `&&`, the variable is in
scope on the right-hand side, so `obj instanceof Integer i && i > 0` is safe; and
`null instanceof String s` is simply `false`, never an exception.

Write `Describe.describe(Object obj)`. The page's way to write it is a chain of
`instanceof` patterns rather than casts. The rows are checked in this order:

| obj | answer |
|---|---|
| a `String` longer than 10 characters | `"long string: " + s` |
| any other `String` | `"string of length " + s.length()` |
| an `Integer` greater than 0 | `"positive integer " + i` |
| any other `Integer` | `"integer " + i` |
| a `List` of anything | `"list of " + list.size()` |
| `null` | `"null"` |
| anything else | `"other: " + obj.getClass().getSimpleName()` |

For example, `describe("hi")` is `"string of length 2"`, `describe("hello, world")`
is `"long string: hello, world"`, `describe(0)` is `"integer 0"`, and
`describe(2.5)` is `"other: Double"`. Only `String`, `Integer` and `List` have rows
of their own: a `Long` or a `Set` is "anything else".
