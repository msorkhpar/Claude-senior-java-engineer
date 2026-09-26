The page's batch execution: `invokeAll(tasks)` submits every task, waits until
all are done and returns their `Future`s **in the same order as the input**.
`invokeAll(tasks, timeout, unit)` also cancels the tasks that have not finished
when the time runs out. The page's best practices recommend that timed form, so
one slow task cannot hold up the whole batch.

Write `Gather.all(executor, tasks, timeout, unit, fallback)`, returning one
result per task, in the order of `tasks`:

- the task's result if it finished;
- `fallback` if the task threw, or if it was still running at the deadline and
  so was cancelled.

| `tasks` | `all(executor, tasks, 300, MILLISECONDS, -1)` |
|---|---|
| `() -> 1`, `() -> 4`, `() -> 9` | `[1, 4, 9]` |
| three tasks where the last finishes first and the first finishes last | their results in task order |
| `() -> 1`, one that throws, `() -> 9` | `[1, -1, 9]` |
| `() -> 1`, one that never finishes | `[1, -1]`, and the unfinished task is cancelled, freeing its thread |
