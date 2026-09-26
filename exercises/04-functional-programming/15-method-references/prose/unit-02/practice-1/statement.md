A **static** method reference (`Integer::parseInt`, `Math::abs`, `Objects::nonNull`)
passes the interface's arguments straight to a static method. An **unbound** one
(`String::trim`, `String::isEmpty`) calls the method on the argument itself. Both look
like `ClassName::method`; which one you have depends on how the method is declared.

Write two methods in `Prices`, as pipelines of method references:

1. `List<Integer> parse(List<String> raw)` parses every usable entry, in order. `null`
   entries and entries that are empty once trimmed are skipped; spaces around a number
   are ignored.
2. `List<Integer> magnitudes(List<Integer> values)` returns each value's absolute value.

| call | result |
|---|---|
| `parse(["42", "7"])` | `[42, 7]` |
| `parse(["  42  ", null, "   ", "100"])` | `[42, 100]` |
| `magnitudes([-5, 3])` | `[5, 3]` |

The order of the steps matters: think about what each reference needs to be true of
its argument before it runs.
