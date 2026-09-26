Parallel streams run on `ForkJoinPool.commonPool()`, which the whole JVM shares. To keep one job off it, submit
the stream to a pool of your own: a parallel stream started from inside a `ForkJoinPool` task runs in that pool.
The pool is then yours to shut down, and `get()` on the submitted task reports a failure as an
`ExecutionException` whose cause is the real exception.

Write `PoolRunner.mapInPool(List<T> items, int parallelism, Function<T, R> f)`. It creates a `ForkJoinPool` with
the given parallelism, runs `f` over `items` with a parallel stream inside that pool, and returns the results in
list order. It shuts the pool down before it returns, whether `f` succeeded or not. If `f` throws an unchecked
exception, `mapInPool` throws an exception of that same type.

| call | answer |
|---|---|
| `mapInPool([1, 2, 3, 4], 2, x -> x * 10)` | `[10, 20, 30, 40]` |
| `mapInPool(["a", "b"], 4, String::toUpperCase)` | `["A", "B"]` |

The tests watch which threads run `f`, which pool they belong to, and what the caller sees when `f` fails.
