`Files.lines(path)` reads a file lazily, line by line, but the stream it returns holds
an open file handle. It is `AutoCloseable`, and it must be closed, which is what
try-with-resources is for.

Write two methods in `LineCounter`:

1. `countNonBlank(Stream<String> lines)` returns how many of the lines are not blank
   (blank as `String.isBlank()` means it: empty, or only whitespace, Unicode spaces
   such as U+2003 included),
   and **closes** the stream it was given before returning, even when counting
   fails with an exception.
2. `countNonBlank(Path file)` returns the same count for the lines of a UTF-8 text
   file, decoded as UTF-8. `Files.lines` reads it lazily; its stream should be closed
   too, although the tests can only watch the closing in method 1.

| lines | answer |
|---|---|
| `["hello", "", "world", "!"]` | `3` |
| `[]` (or an empty file) | `0` |

"Blank" is wider than "empty", and a stream you were handed to consume is yours to
close.
