A `RecursiveAction`'s `compute()` returns **nothing**: the page's actions change
an array **in place**. Its Q2 splits with `invokeAll(left, right)`, which forks
both subtasks and waits for both, and its pitfalls ask for **strictly disjoint**
ranges `[start, mid)` and `[mid, end)`. Its best practices validate input **in
the constructor**, not in `compute()`.

Write `ScaleAction(array, start, end, threshold, factor, offset, probe)`, a
`RecursiveAction` that turns every `array[i]` of `[start, end)` into
`array[i] * factor + offset`. While the range is longer than `threshold`, split
it at the midpoint and run both halves **at the same time** (the page does it
with `invokeAll(left, right)`). In each base case,
call `probe.leaf(start, end)` once. A `null` array is
refused by the constructor with `IllegalArgumentException`.

| array | range | threshold | factor, offset | array afterwards |
|---|---|---|---|---|
| `1, 2, 3, 4` | `[0, 4)` | 4 | `2, 1` | `3, 5, 7, 9` |
| `1, 2, ..., 10` | `[1, 9)` | 3 | `10, 0` | `1, 20, 30, 40, 50, 60, 70, 80, 90, 10` |
| `1, 2, ..., 10` | `[0, 10)` | 3 | `1, 0` | leaves `[0,2) [2,5) [5,7) [7,10)` |
| `null` | `[0, 0)` | 1 | `1, 0` | `IllegalArgumentException` |

The tests also check that the two leaves of one split can be inside
`probe.leaf` **at the same time**, in a pool of two workers.
