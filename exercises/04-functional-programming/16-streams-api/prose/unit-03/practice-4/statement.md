`takeWhile(p)` (Java 9) passes elements while `p` holds and stops for good at the
first one that fails; `dropWhile(p)` skips elements while `p` holds and then passes
that element and **everything after it**. `filter(p)`, by contrast, tests every
element on its own.

A sensor warms up: its first readings are below a threshold. Write two methods in
`Readings`:

1. `warmup(List<Integer> readings, int threshold)` returns the readings before the
   first one that is at or above `threshold`.
2. `afterWarmup(List<Integer> readings, int threshold)` returns that first reading at
   or above `threshold` and every reading after it.

| readings | threshold | warmup | afterWarmup |
|---|---|---|---|
| `[1, 2, 3, 4, 5, 6]` | `4` | `[1, 2, 3]` | `[4, 5, 6]` |
| `[1, 2, 5, 3, 4]` | `5` | `[1, 2]` | `[5, 3, 4]` |
| `[7, 1, 2]` | `5` | `[]` | `[7, 1, 2]` |

The two lists always put the readings back together, in order.
