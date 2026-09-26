The page's `ForkJoinPool.commonPool()` is shared by the whole JVM. When you need
**isolation or a different parallelism**, you create your own pool, and then you
must **shut it down**: the page's pitfall 2 shows a custom pool that "leaks
threads" and fixes it with `try`/`finally`. The page also says `invoke(task)`
blocks until the task completes and returns its result, and that `invoke()`
throws the task's exception to the caller.

Write `IsolatedPool.run(task, parallelism)`:

- it runs `task` in a **new** `ForkJoinPool` with the given parallelism and
  returns the task's result;
- the pool is shut down before `run` returns, **also when the task throws**, an exception or an `Error`;
- a `RuntimeException` thrown by the task reaches the caller as that type
  (for example an `IllegalStateException`), not wrapped in another exception.

| task | parallelism | answer |
|---|---|---|
| returns `42` | `2` | `42`, and the task saw a pool of parallelism `2` that is not the common pool |
| returns `7` | `3` | `7`, in a pool of parallelism `3`; afterwards that pool `isShutdown()` |
| throws `IllegalStateException("boom")` | `2` | `run` throws `IllegalStateException`; afterwards the pool `isShutdown()` |
