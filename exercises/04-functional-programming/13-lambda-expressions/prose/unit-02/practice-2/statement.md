An expression body returns its value implicitly. A block body needs an
explicit `return`, and the compiler insists that every path through the block
returns a value. Early returns keep such a block flat: handle the special cases
first, then the ordinary one.

Write two static methods in `Normalizer`:

1. `normalizer()` returns a `Function<String, String>` that maps `null` to
   `"NULL"`, text that is empty once trimmed to `"EMPTY"`, and any other text to
   its trimmed, upper-cased form. Trimming means `String.trim()`: it removes
   leading and trailing characters up to U+0020 (spaces, tabs, line breaks and
   other control characters such as U+0001), but not wider Unicode spaces such
   as the em space U+2003, which stay.
2. `normalizeAll(List<String> texts)` applies that function to every entry, in
   order. The list may contain `null` entries.

| input | output |
|---|---|
| `" hello "` | `"HELLO"` |
| `"World"` | `"WORLD"` |
| `null` | `"NULL"` |
| `""` | `"EMPTY"` |

Check the special cases in the right order.
