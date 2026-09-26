Structured concurrency, a preview in Java 21, ties the lifetime of concurrent
subtasks to one block of code: the block does not finish until every subtask has
finished. Without the preview API the page reaches the same shape with
`Executors.newVirtualThreadPerTaskExecutor()` in a try-with-resources: every task
gets its **own virtual thread**, and `close()` waits for all of them before the
block exits.

Write `Gather.all(List<Callable<T>> tasks)`. It runs every task on its own
virtual thread, waits for all of them, and returns one `Outcome` per task:

- The outcomes are in **task order**, whatever order the tasks finish in.
- A task that returns `v` gives `new Outcome<>(v, null)`.
- A task that throws gives `new Outcome<>(null, message)`, where `message` is
  the **task's own exception's** `getMessage()`, not a wrapper's. One task
  failing does not stop or hide the others.

`Outcome` is the nested record `Gather.Outcome<T>(T value, String error)`.

| tasks | answer |
|---|---|
| `[() -> "a", () -> "b"]` | `[Outcome[value=a, error=null], Outcome[value=b, error=null]]` |
| `[() -> 1, () -> { throw new IllegalStateException("boom"); }, () -> 3]` | `[Outcome[value=1, error=null], Outcome[value=null, error=boom], Outcome[value=3, error=null]]` |
| `[]` | `[]` |
