The page's Java 21 feature: `Executors.newVirtualThreadPerTaskExecutor()` starts
a new virtual thread for every task, so there is no pool size to tune, and
try-with-resources shuts the executor down and waits for its tasks. The page
shows it running many I/O-bound requests at once.

Write `FanOut.all(tasks)`: run every task on its own virtual thread, all at the
same time, wait for them, and return their results **in the order of `tasks`**.
If a task fails, throw the exception that task threw (for the first failed task
in list order), once every task has ended: no task is left running. (What a task that throws an `Error`
leads to is not graded.)

| `tasks` | `all(tasks)` |
|---|---|
| `() -> "a"`, `() -> "b"`, `() -> "c"` | `["a", "b", "c"]` |
| a first task that finishes only after the second one has | `["first", "second"]` |
| three tasks answering `Thread.currentThread().isVirtual()` | `[true, true, true]` |
| 50 tasks that each wait until all 50 have started | every task sees the other 49 |
| `() -> "a"`, one that throws `new IOException("down")`, then one that throws `new IOException("later")` sooner | throws the `IOException("down")` |
