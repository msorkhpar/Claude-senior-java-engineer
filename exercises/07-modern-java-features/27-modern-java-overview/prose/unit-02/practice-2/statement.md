Java 11's `String.lines()` returns a `Stream<String>` of lines, splitting on
`\n`, `\r\n` **and** `\r`, which `split("\n")` does not. The page's edge cases:
`lines()` on an empty string has **no** lines, and a trailing line terminator
does **not** add an empty last line. `String.repeat(n)` builds padding without
a loop (`"x".repeat(0)` is `""`).

Write `Listing.number(String text)`. It returns each line of `text` as
`"<n>: <line>"`, numbering from 1. Blank lines in the middle are kept and
numbered. When there are ten or more lines, **right-align** the numbers:
pad each number on the left with spaces to the width of the largest one.

| text | answer |
|---|---|
| `"a\nb"` | `["1: a", "2: b"]` |
| `"a\n\nb"` | `["1: a", "2: ", "3: b"]` |
| `"a\r\nb\rc\n"` | `["1: a", "2: b", "3: c"]` |
| `"a\n\n"` | `["1: a", "2: "]` |
| `""` | `[]` |
| ten lines `"l1\n...\nl10"` | `[" 1: l1", " 2: l2", ..., "10: l10"]` |
| a hundred lines | `["  1: l1", ..., " 10: l10", ..., "100: l100"]` |
