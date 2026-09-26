The page's Q5: parallel streams run in the **common** `ForkJoinPool`, shared by
the whole JVM, so a long-running parallel stream can starve every other one.
To run a parallel stream in a pool of your own, **submit the stream operation
as a task** to that pool and join it: the stream's work then happens on that
pool's workers. As with any custom pool, shut it down afterwards.

Write `PoolStream.sum(values, parallelism, f)`. It creates a new
`ForkJoinPool` of the given parallelism, runs a **parallel** stream over
`values` that applies `f` to each value and sums the results, inside that pool,
and shuts the pool down before returning, whether `f` succeeded or threw (its exception reaches the caller).

| values | parallelism | f | answer |
|---|---|---|---|
| `1, 2, 3` | 3 | `x -> x * x` | `14` |
| `1, 2, 3, 4` | 3 | records its thread, returns `x` | `10`; every recorded thread is a worker of a pool of parallelism 3 that is not the common pool |
| `5, 7` | 3 | waits until the other value is being mapped too | `12` |
