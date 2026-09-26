A `Runnable` returns nothing and cannot throw a checked exception. A `Callable<V>`
returns a `V` and may throw any `Exception`. Submitted to an `ExecutorService`, it
gives back a `Future<V>`, and `get()` blocks until the result is there, reporting a
failure as an `ExecutionException` that **wraps** the exception the task threw.
The course's own `SumCallable` sums a range this way.

Write `Totals.total(ExecutorService pool, List<Callable<Integer>> parts)`. It
submits **every** part to `pool` first, then waits for each result, and returns
their sum as a `long`. If a part throws, `total` throws `IllegalStateException`
whose **cause** is the exception that part threw (not the `ExecutionException`
around it).

| parts | result |
|---|---|
| sum of 1..50, sum of 51..100 | `5050` |
| `() -> 7` | `7` |
| a part that throws `IOException("disk gone")` | `IllegalStateException`, cause `IOException("disk gone")` |
