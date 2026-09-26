A `finally` block runs after the `try` block however it ends, which makes it the classic place for
cleanup such as closing a reader. (Try-with-resources, on the next page, replaces this pattern; here
you write it by hand.)

Write `FirstLine.read(Reader source)`. It wraps `source` in a `BufferedReader`, returns its first
line (as `readLine()` gives it), and always closes the reader, which closes `source` too.

| source text | answer |
|---|---|
| `"first\nsecond"` | `"first"` |
| `"only"` | `"only"` |

"Always" includes the run where `readLine()` throws an `IOException`, which must still reach the
caller. Keep `readLine()`'s own answer for a source with no lines at all.
