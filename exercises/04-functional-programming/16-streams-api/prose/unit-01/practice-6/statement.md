`Stream.iterate(seed, next)` makes an infinite stream. It is safe only because
streams are lazy: a `limit()` or a short-circuiting terminal operation decides how
much of it is ever produced.

Write `Squares.firstSquaresAbove(int threshold, int count)`. Starting from the
natural numbers 1, 2, 3, ... (an infinite `Stream.iterate`), it returns the first
`count` squares that are **greater than** `threshold`, in increasing order.
`threshold` may be negative; `count` is never negative, and the squares asked for
stay well inside `int`.

| threshold | count | answer |
|---|---|---|
| `0` | `4` | `[1, 4, 9, 16]` |
| `10` | `3` | `[16, 25, 36]` |
| `0` | `0` | `[]` |
| `-5` | `3` | `[1, 4, 9]` |

Where the bound sits in the pipeline decides what it counts.
