The page's pitfalls: `future.get()` without a timeout can block forever, and a
`get(timeout, unit)` that times out leaves the task **running in the
background** unless you cancel it. Its interview answer shows the fix,
`computeWithTimeout`: wait with a timeout, and on a timeout call
`future.cancel(true)` and return a fallback.

Write `Fallback.within(executor, task, timeout, unit, fallback)`:

- submit `task` to `executor` and wait at most `timeout` for its result;
- on a timeout: cancel the task, interrupting it, and return `fallback`;
- if the task failed: throw a `RuntimeException` whose **cause is the exception
  the task threw**;
- if the calling thread is interrupted while it waits: cancel the task, restore
  the interrupt flag and return `fallback`.

| `task` (on a single-thread executor) | `within(executor, task, 200, MILLISECONDS, "stale")` |
|---|---|
| `() -> "fresh"` | `"fresh"` |
| a task that never finishes | `"stale"`, and the executor's thread is free again |
| `() -> { throw new IOException("down"); }` | throws a `RuntimeException` with cause that `IOException` |
| any task, called from a thread that is already interrupted | `"stale"`, and the thread is still interrupted |
