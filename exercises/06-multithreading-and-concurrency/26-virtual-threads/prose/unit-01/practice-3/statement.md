Virtual threads are too cheap to pool, so a pool is no longer how you protect a
scarce resource such as 20 database connections. The page's answer: give every
task its own virtual thread and make each task take a permit from a `Semaphore`
around the scarce part, giving it back in `finally`.

Write `Throttle.runAll(List<Callable<T>> tasks, Semaphore permits)`:

- Each task runs on its **own virtual thread**, and all of them are started
  at once.
- A task runs only while it holds one permit of `permits`, so at most as many
  tasks run at a time as the semaphore has permits. The others wait on the
  semaphore.
- The permit goes back when the task ends, **also when it throws**.
- It returns the results in task order, after every task has finished. If a
  task throws, `runAll` throws an `ExecutionException` whose cause is that
  exception.

| tasks | permits | answer |
|---|---|---|
| `[() -> 1, () -> 2, () -> 3]` | `new Semaphore(2)` | `[1, 2, 3]` |
| 8 tasks that block until released | `new Semaphore(3)` | while blocked: 3 tasks running, 5 waiting for a permit |
| `[() -> { throw new IllegalStateException(); }]` | `new Semaphore(2)` | `ExecutionException`; the semaphore has 2 permits again |
