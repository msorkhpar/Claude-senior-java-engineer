Streams do not need a collection behind them. `Stream.iterate(seed, hasNext, next)`
(Java 9) runs like a `for` loop and stops by itself; `Stream.generate(supplier)` is
endless and needs a `limit()`; `IntStream.range(a, b)` excludes `b` while
`IntStream.rangeClosed(a, b)` includes it.

Write three methods in `Sequences`:

1. `powersBelow(int bound)` returns the powers of two 1, 2, 4, ... that are
   **less than** `bound` (the three-argument `Stream.iterate` fits; `bound` is at
   most `1 << 20`).
2. `repeated(String text, int times)` returns `text` repeated `times` times
   (`Stream.generate` with a `limit` fits; `times` is never negative).
3. `labels(String prefix, int count)` returns `prefix + 1`, `prefix + 2`, ... up to
   `prefix + count`.

| call | answer |
|---|---|
| `powersBelow(100)` | `[1, 2, 4, 8, 16, 32, 64]` |
| `powersBelow(1)` | `[]` |
| `repeated("ab", 3)` | `"ababab"` |
| `labels("row", 3)` | `["row1", "row2", "row3"]` |
| `labels("row", 0)` | `[]` |

Mind which end of each sequence is included.
