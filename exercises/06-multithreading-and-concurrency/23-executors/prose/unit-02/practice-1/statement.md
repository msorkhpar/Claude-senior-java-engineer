The page's interview answer lists what `Future.get()` can throw:
`ExecutionException` when the task failed (the real exception is its
`getCause()`, even a checked one), `CancellationException` when the task was
cancelled, `TimeoutException` from `get(timeout, unit)`, and
`InterruptedException` when the waiting thread is interrupted. Its pitfalls add:
always wait with a timeout, and handle the cause, not the wrapper.

Write `Outcomes.describe(future, timeout, unit)`, which waits at most `timeout`
for the future and returns one line:

- `"value: " + value` when the task returned;
- `"failed: " + SimpleClassName + ": " + message` of the exception **the task
  threw**;
- `"cancelled"` when the task was cancelled;
- `"timed out"` when it did not finish in time.

An interrupt of the waiting thread is not an outcome: let the
`InterruptedException` out.

| the future's task | `describe(future, 1, SECONDS)` |
|---|---|
| returned `42` | `"value: 42"` |
| threw `new IOException("File not found")` | `"failed: IOException: File not found"` |
| was cancelled before it ran | `"cancelled"` |
| finishes while `describe` is waiting (timeout 5 s) | `"value: late"` |
| never finishes (timeout 50 ms) | `"timed out"` |
| never finishes, and the calling thread is interrupted | throws `InterruptedException` |
