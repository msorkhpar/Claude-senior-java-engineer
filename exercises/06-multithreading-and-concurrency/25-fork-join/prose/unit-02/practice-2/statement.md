The page contrasts the ways to hand a task to a pool: `invoke(task)` **blocks**
until the task is done, while `submit(task)` returns a `ForkJoinTask<V>` (a
`Future`) **at once**, and you call `join()` or `get()` later to collect the
result.

Write `Batch.runAll(pool, tasks)`. It hands **every** task to **`pool`** with
`submit` before it waits for any of them, then joins them and returns their
results in the order of the list.

| tasks, pool of parallelism 2 | answer |
|---|---|
| a task returning `"alpha"`, then one returning `"beta"` once alpha is done | `["alpha", "beta"]` |
| two tasks that each wait until the other has started | both finish: `["left", "right"]` |
| three tasks that note the pool they run in | all three ran in `pool` |
| an empty list | `[]` |

Calling `pool.invoke(task)` for each task in turn would run them one after the
other: a task that needs another one running beside it would then never finish.

Note that `task.fork()` called from a thread outside any pool pushes the task to
the **common** pool, not to `pool`.
