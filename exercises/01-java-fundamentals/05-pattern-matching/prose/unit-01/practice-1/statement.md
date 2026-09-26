`obj instanceof Type` is `true` when `obj` is a `Type` or any subtype of it, and `false` for
`null`. So a chain of checks tests the most specific type first: every `Integer` is also a
`Number`. It works with arrays too, and an `int[]` is not an `Object[]`.

Write `kind(Object obj)` in `Kinds`:

| obj | result |
|---|---|
| an `Integer` | `"integer"` |
| any other `Number` (`Long`, `Double`, ...) | `"number"` |
| a `CharSequence` (`String`, `StringBuilder`, ...) | `"text"` |
| an `int[]` | `"int array"` |
| an `Object[]` (`String[]` included) | `"object array"` |
| `null` | `"null"` |
| anything else | `"other"` |
