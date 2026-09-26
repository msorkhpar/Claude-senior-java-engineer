An expression body returns its value implicitly. A **block body** needs an
explicit `return`, and the compiler insists that every path through the block
returns a value. Early returns keep such a block flat: handle the special cases
first, then the ordinary one.

Write two static methods in `Normalizer`:

1. `normalizer()` returns a `Function<String, String>` that maps `null` to
   `"NULL"`, text that is empty once trimmed to `"EMPTY"`, and any other text to
   its trimmed, upper-cased form.
2. `normalizeAll(List<String> texts)` applies that function to every entry, in
   order. The list may contain `null` entries.

| input | output |
|---|---|
| `" hello "` | `"HELLO"` |
| `"World"` | `"WORLD"` |
| `null` | `"NULL"` |
| `""` | `"EMPTY"` |

Check the special cases in the right order.
