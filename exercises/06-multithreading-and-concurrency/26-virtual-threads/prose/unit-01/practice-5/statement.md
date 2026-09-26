Thread-local variables work on virtual threads: each virtual thread has its own
copy of a `ThreadLocal` value. With a million virtual threads, though, every
value left behind is memory held for nothing, so the page's rule is to
`remove()` the value in a `finally` block when the work is done.

Write `RequestContext`, which holds "the user of the current request" per thread:

- `runAs(String user, Callable<T> work)` makes `user` the current user of the
  **calling thread**, runs `work` there and returns its result. When it returns,
  **or throws**, the calling thread has no current user again. An exception from
  `work` is passed on unchanged.
- `current()` returns the current user of the calling thread, or `null` outside
  `runAs`.
- Threads never see each other's user.

| call | answer |
|---|---|
| `runAs("ann", RequestContext::current)` | `"ann"` |
| `runAs("ann", ...)` on one virtual thread while `runAs("bob", ...)` runs on another | each work sees its own user |
| `current()` after `runAs("ann", ...)` has returned | `null` |
| `runAs("ann", () -> { throw new IOException(); })` | throws the `IOException`; then `current()` is `null` |
