Since Java 21 a switch can match patterns: `case String s ->` matches any `String` and binds
it to `s`, and a `when` guard narrows a case further: `case Integer i when i > 0 ->`. The
more specific case is written first, and `case null` handles a `null` selector.

Write `describe(Object obj)` in `Describe` as one switch expression, following the page's
own example:

| obj | result |
|---|---|
| a `String` | `"String of length "` + its length |
| an `Integer` above 0 | `"Positive integer"` |
| any other `Integer` | `"Non-positive integer"` |
| a `List` | `"List with "` + its size + `" elements"` |
| `null` | `"Null input"` |
| anything else | `"Something else"` |

Examples: `describe("hello")` is `"String of length 5"`, `describe(0)` is
`"Non-positive integer"`, and `describe(List.of(1, 2))` is `"List with 2 elements"`.
