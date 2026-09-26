The page's `submit(task)` gives back a `ForkJoinTask`, which is a `Future`, so
you can wait for it **with a bound**: `get(timeout, unit)`. The module's own
`ForkJoinPoolBasics.sumWithTimeout` does exactly this. `get` reports a task's
failure wrapped in an `ExecutionException`, and on a timeout it throws
`TimeoutException`. The page's edge cases also ask you to handle thread
**interruption** properly.

Write `Deadline.within(task, parallelism, timeoutMillis)`:

- run `task` in a **new** pool of the given parallelism and wait at most
  `timeoutMillis` for it;
- in time: return `Optional.of(result)`;
- too late: cancel the task and return `Optional.empty()`;
- the task failed: throw `IllegalStateException` whose cause is the task's
  exception;
- always finish with `shutdownNow()`, which also **interrupts** a worker that is
  still running a task (a cancelled `ForkJoinTask` is not interrupted by
  `cancel(true)`).

| task | timeout | answer |
|---|---|---|
| returns `42` | 5000 ms | `Optional[42]` |
| waits on a latch that is never opened | 200 ms | `Optional.empty`, and the waiting task gets an `InterruptedException` |
| throws `IllegalArgumentException("bad input")` | 5000 ms | `IllegalStateException`, whose root cause says `"bad input"` |
