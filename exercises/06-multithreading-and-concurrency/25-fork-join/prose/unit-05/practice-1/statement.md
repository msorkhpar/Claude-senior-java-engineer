The page's threshold heuristic is **`N / (parallelism * 4)` with a minimum**:
enough tasks for good load balancing without the overhead of tiny ones. Its Q1
reads the parallelism from the pool and takes `Math.max(minimum, N / (parallelism * 4))`.

Write `AdaptiveSum.sum(pool, array, floor, probe)`: it sums `array` with a
`RecursiveTask` **run in `pool`**, whose threshold is

    Math.max(floor, array.length / (pool.getParallelism() * 4))

A range of at most that many elements is summed sequentially; a longer one is
split at the midpoint into two subtasks. In each base
case, before summing, call `probe.leaf(start, end)` once.

| pool parallelism | array | floor | threshold | answer | leaves |
|---|---|---|---|---|---|
| 2 | `1..100` | 10 | 12 | `5050` | 12 leaves |
| 2 | 160 ones | 5 | 20 | `160` | 8 leaves of 20 |
| 2 | 40 ones | 16 | 16 | `40` | 4 leaves of 10 |
| 5 | 160 ones | 5 | 8 | `160` | 32 leaves of 5 |
