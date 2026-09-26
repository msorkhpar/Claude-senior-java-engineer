Since Java 9, a try-with-resources statement can manage a resource that already exists, as long as
the variable holding it is final or effectively final: `try (source) { ... }`.

Write `Drain.readAll(LineSource source)`. The source is already open when you get it. Read lines
with `readLine()` until it returns `null`, return them in order, and close the source when done.

| lines in the source | answer |
|---|---|
| `"a"`, `"b"` | `["a", "b"]` |
| `"a"`, `""`, `"b"` | `["a", "", "b"]` (an empty line is still a line) |
| none | `[]` |

The caller handed over the source, so closing it is now your job on every path, whatever a read throws,
and every exception, from reading or from closing, reaches the caller. When a read and the close both
fail, the read's exception is the one thrown, with the close's added to it as suppressed, as
try-with-resources does.
