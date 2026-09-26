A `RecursiveAction` returns nothing, so to count it needs a shared,
thread-safe accumulator. The page's pitfall 1 shows the race on a plain
`int count` and fixes it with an `AtomicInteger`: each base case counts in a
local variable, then updates the atomic counter once with
`addAndGet(localCount)`. (A lost update between separate `get` and `set` calls
cannot be forced by a test, so use `addAndGet` because it is right, not because
a test will catch the alternative.)

Write `CountMatching(array, start, end, threshold, target, counter)`, a
`RecursiveAction` that adds to `counter` the number of elements of
`array[start..end)` equal to `target`, splitting at the midpoint while the
range is longer than `threshold`. The counter may already hold a value: leaves
**add** to it, never overwrite it. A `null` counter is refused by
the constructor with `IllegalArgumentException`.

| array | target | threshold | counter before | counter after |
|---|---|---|---|---|
| `1, 2, 3, 2, 1, 2` | 2 | 6 | 0 | 3 |
| `7, 1, 7, 7, 2, 7, 3, 7` | 7 | 2 | 10 | 15 |
| `1, 2, 3` | 2 | 2 | `null` | `IllegalArgumentException` |
