A method reference passes its argument on unchanged: `Integer::parseInt` cannot trim
`"  42  "` first. The page's fix is a separate step, `map(String::trim)`, before it.
A reference cannot be negated with `!` either; `Predicate.not(String::isBlank)` can.

Write `NumberCleaner.clean(List<List<String>> batches)`. It reads every entry of every
batch, skips `null` and blank entries, parses the rest as integers (ignoring spaces
around them), removes repeated numbers and returns them **highest first**, by value.
The page's version writes every step that only calls a method as a method reference
(`Collection::stream` flattens the batches in `flatMap`).

| batches | result |
|---|---|
| `[["3", "1"], ["2", "3"]]` | `[3, 2, 1]` |
| `[["  42  ", " 7 "], ["  100  "]]` | `[100, 42, 7]` |
| `[["5", null, "", "   ", "6"]]` | `[6, 5]` |

A number can be written more than one way.
