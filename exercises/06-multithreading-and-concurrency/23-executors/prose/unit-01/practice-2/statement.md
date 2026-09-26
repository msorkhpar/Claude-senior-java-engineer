The page's pitfall: an exception thrown by a task run with `execute()` never
reaches the caller, and one thrown by a task run with `submit()` is kept inside
the returned `Future`, so if nobody calls `get()` it is lost. The page also says
`ThreadPoolExecutor` exposes hooks, `beforeExecute()`, `afterExecute()` and
`terminated()`, for monitoring.

Write `MonitoredPool`, a `ThreadPoolExecutor` of `threads` threads and an
unbounded queue, that uses those hooks to keep a record:

- `List<String> failures()` holds the message of every task that failed, whether
  it was run with `execute()` or `submit()`. For a submitted task, report the
  exception the task threw, not the `ExecutionException` around it.
- `int successes()` counts the tasks that ended without an exception.
- `boolean finished()` is `true` once the pool has terminated, and `false` before.

The record is written from the pool's worker threads, so keep it thread-safe; the tests run one worker at
a time, so they check the record's contents, not its thread safety.

| tasks on `new MonitoredPool(1)`, then `shutdown()` and wait | `failures()` | `successes()` |
|---|---|---|
| `execute` one that returns, `execute` one that throws `new IllegalStateException("boom")` | `["boom"]` | `1` |
| `submit` one that throws `new IllegalStateException("bad")`, `submit` one that returns | `["bad"]` | `1` |
| `submit` a `Callable` that throws `new IOException("disk")` | `["disk"]` | `0` |
| a task still running after `shutdown()` | `finished()` is `false` until it ends | |
