The page's pitfall 2 is **joining too early**:

    left.fork();
    long leftResult = left.join(); // blocks immediately!
    long rightResult = right.compute();

This is "sequential execution disguised as fork/join": the current thread waits
for the left half before it even starts the right one. The fix is to fork
first, compute the other subtask, then join, so **both halves are in progress
together**. (Forking both halves and joining both, the page's other
anti-pattern, also keeps both in progress; its cost is an extra fork per split,
which no test can see, so it is not graded here.)

Write `DotProduct(a, b, start, end, threshold, probe)`, a
`RecursiveTask<Long>` returning the sum of `a[i] * b[i]` for `i` in
`[start, end)` (as a `long`). Split at the midpoint while the range is longer
than `threshold`, never joining the forked half before the other half has been
computed. In each base case, call
`probe.leaf(start, end)` once. Each product `a[i] * b[i]` may exceed the `int` range. Arrays of different lengths are
refused by the constructor with `IllegalArgumentException`.

| a | b | threshold | answer |
|---|---|---|---|
| `1, 2, 3` | `4, 5, 6` | 1 | `32` |
| `1, 2, 3, 4` | `1, 1, 1, 1` | 2 | `10`, and the two leaves are in `probe.leaf` at the same time |
| `1, 2` | `1, 2, 3` | 1 | `IllegalArgumentException` |
