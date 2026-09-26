The page's shutdown idiom has two phases. `shutdown()` stops new work but lets
running and queued tasks finish, and `awaitTermination()` waits for them. If
they do not finish in time, `shutdownNow()` interrupts the running tasks and
returns the ones that never started, and a second `awaitTermination()` waits
again. Its edge cases add: if the waiting thread is interrupted, fall back to
`shutdownNow()` and re-interrupt the thread.

Write `Shutdown.close(executor, timeout, unit)`, returning the tasks that never
started (an empty list when none were dropped):

- phase 1: `shutdown()`, then wait up to `timeout` for termination;
- phase 2, only if phase 1 timed out: `shutdownNow()`, then wait up to `timeout`
  once more, and return (a task that ignores its interrupt may still be running);
- if the calling thread is interrupted while it waits: `shutdownNow()`, restore
  its interrupt flag, and return what `shutdownNow()` returned.

| a single-thread executor holding | `close(executor, t, unit)` returns | and |
|---|---|---|
| one finished task | `[]` | the executor is terminated |
| a running task that ends soon, and two queued tasks | `[]` | all three tasks ran |
| a running task that never ends, and one queued task | the queued task | the running task was interrupted, and the executor is terminated when `close` returns |
| a running task that never ends, the caller then interrupted | | the caller's interrupt flag is set, the executor terminates |
