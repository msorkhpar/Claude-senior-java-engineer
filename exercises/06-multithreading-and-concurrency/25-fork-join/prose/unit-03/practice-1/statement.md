The page's Q1 walks through `SumTask extends RecursiveTask<Long>`:

1. **Base case**: if `end - start <= threshold`, sum the range sequentially.
2. Otherwise split at `mid = start + (end - start) / 2` and run the two
   halves **at the same time**, then return the two sums added. The page's way
   is to fork the left subtask, compute the right one in the current thread and
   join the left last.

Its edge cases add: use `long` for sums of large `int` arrays.

Write `SumTask(array, start, end, threshold, probe)`. It sums
`array[start..end)`. In each base case, before summing, call
`probe.leaf(start, end)` once with the leaf's range (the tests use it to see how
you split).

| array | range | threshold | answer | leaves |
|---|---|---|---|---|
| `1..10` | `[0, 10)` | 3 | `55` | `[0,2) [2,5) [5,7) [7,10)` |
| `1..10` | `[2, 5)` | 3 | `12` | `[2,5)` |
| four times `Integer.MAX_VALUE` | `[0, 4)` | 2 | `8589934588` | `[0,2) [2,4)` |
| `1, 2, 3, 4` | `[0, 4)` | 4 | `10` | `[0,4)` |

The halves really run in parallel: the tests check that the two leaves of a
split can be inside `probe.leaf` **at the same time**, in a pool of two workers.
Summing both halves in the current thread one after the other, or joining the
forked half before computing the other, runs them one at a time and fails that
check.
