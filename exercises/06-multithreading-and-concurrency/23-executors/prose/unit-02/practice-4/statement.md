Two ways the page reads results without waiting for the slowest task:

- `invokeAny(tasks)` returns the result of the **first task to succeed**,
  cancels the rest, ignores failures while one task can still succeed, and
  throws `ExecutionException` if all fail. The page uses it to query several
  replicas and keep the fastest answer.
- A `CompletionService` (`ExecutorCompletionService`) hands back each `Future`
  **as it completes**, so a slow first task does not block the fast ones behind
  it.

Write `Replicas`:

- `first(executor, replicas)`: the first successful reply; the replicas still
  running are cancelled; `ExecutionException` if every replica fails.
- `inArrivalOrder(executor, tasks)`: every task's result, in the order the
  tasks **finished**. A failed task's exception comes out as the
  `ExecutionException` from its `get()`.

| call | answer |
|---|---|
| `first(executor, [() -> "r1"])` | `"r1"` |
| `first(executor, [one that throws, () -> "ok"])` | `"ok"` |
| `first(executor, [one that never answers, () -> "fast"])` | `"fast"`, and the slow replica's thread is free again |
| `first(executor, [two that throw])` | throws `ExecutionException` |
| `inArrivalOrder(executor, [a, b, c])` where `c` finishes first and `a` last | `[c, b, a]` |
