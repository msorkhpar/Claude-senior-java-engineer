The page's `ThreadLocalDemo` (Q3) uses `ThreadLocal` twice: a request id that downstream
code reads without it being passed along, and a `SimpleDateFormat`, which is not
thread-safe, given to each thread as its own copy. It also warns that in a thread pool a
value that is not removed leaks into the next task.

Write `RequestContext`:

- `process(String requestId, Supplier<T> work)` makes `requestId` this thread's current
  request id, runs `work`, and returns its result. When `process` ends, normally or with
  the work's exception (which it lets through), the thread has no request id any more.
- `currentRequestId()` returns this thread's current request id, or `null`.
- `formatter()` returns this thread's own `SimpleDateFormat` for `yyyy-MM-dd`: the same
  object every time on one thread, and a different object on another thread.
- `format(Date)` formats a date with this thread's formatter.

| call | answer |
|---|---|
| `process("req-1", RequestContext::currentRequestId)` | `"req-1"` |
| `currentRequestId()` afterwards | `null` |
| threads A and B inside `process("one", …)` and `process("two", …)` at the same time | A reads `"one"`, B reads `"two"` |
| `process("x", () -> { throw new IllegalStateException(); })` | throws `IllegalStateException`; the id is `null` afterwards |
| `format(<5 March 2024>)` | `"2024-03-05"` |
