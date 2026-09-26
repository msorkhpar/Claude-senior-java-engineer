Since Java 9, a try-with-resources statement can manage a resource that already exists, as long as
the variable holding it is final or effectively final: `try (source) { ... }`.

Write `Drain.readAll(LineSource source)`. The source is already open when you get it. Read lines
with `readLine()` until it returns `null`, return them in order, and close the source when done.

| lines in the source | answer |
|---|---|
| `"a"`, `"b"` | `["a", "b"]` |
| none | `[]` |

The caller handed over the source, so closing it is now your job on every path, and every
`IOException`, from reading or from closing, reaches the caller.
