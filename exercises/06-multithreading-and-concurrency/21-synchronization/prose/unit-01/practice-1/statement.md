The page's counter protects `count++` with `synchronized`, and prefers a
**dedicated, private lock object** to `synchronized` methods, because a method
lock is `this`, and any outside code can take `this` too. It also warns that
`synchronized (new Object())` protects nothing, since every call gets its own
lock.

Write `Balance`, a balance many threads may change:

- `Balance(int initial)` starts the balance.
- `int update(IntUnaryOperator change)` applies `change` to the balance and
  returns the new balance. Only one update runs at a time: a second update
  **waits** until the first has finished.
- `int get()` returns the balance.
- Code outside the class that does `synchronized (balance) { ... }` must not be
  able to stall `update`.
- If `change` throws, the exception reaches the caller, the balance keeps its
  old value, and the next update, from any thread, still runs.

| calls, from `new Balance(10)` | answer |
|---|---|
| `update(x -> x + 5)` | `15` |
| `update(x -> x * 2)`, then `get()` | `30`, `30` |
| `update(x -> { throw new IllegalStateException(); })` | throws `IllegalStateException`; `get()` is still `30` |
| another thread holds `synchronized (balance)`; this thread calls `update(x -> x + 1)` | returns `31` without waiting for it |
