Java 11 added `isBlank()`, `strip()`, `stripLeading()` and `stripTrailing()` to
`String`. The page's key distinction: `trim()` treats only characters up to
`U+0020` as whitespace, while `strip()` uses `Character.isWhitespace`, so it
also removes Unicode spaces such as EN QUAD (`U+2000`) and EM SPACE (`U+2003`).
`isBlank()` is the modern form of `trim().isEmpty()`, with the same Unicode
awareness.

Write `Fields.clean(List<String> raw)`. It returns the values of `raw`, in
order, with **all surrounding whitespace removed, Unicode spaces included**,
and leaves out every value that is **blank**: empty, or made only of
whitespace of any kind.

| raw | answer |
|---|---|
| `[" a ", "b\t", "   ", "\t\n"]` | `["a", "b"]` |
| `[" Hello "]` | `["Hello"]` |
| `["  ", "x"]` | `["x"]` |
| `[]` | `[]` |
