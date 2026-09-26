`Files.lines(path)` reads a file lazily, line by line, but the stream it returns holds
an open file handle. It is `AutoCloseable`, and it must be closed, which is what
try-with-resources is for.

Write two methods in `LineCounter`:

1. `countNonBlank(Stream<String> lines)` returns how many of the lines are not blank,
   and **closes** the stream it was given before returning, even when counting
   fails with an exception.
2. `countNonBlank(Path file)` returns the same count for the lines of a UTF-8 text
   file (`Files.lines` reads it lazily; close what you open).

| lines | answer |
|---|---|
| `["hello", "", "world", "!"]` | `3` |
| `[]` (or an empty file) | `0` |

"Blank" is wider than "empty", and a stream you were handed to consume is yours to
close.
