`acquire(n)` is not `n` calls to `acquire()`. The page: it waits until all `n`
permits are free **at the same moment** and takes them together, so a waiting
thread never sits on a partial set that another thread needs; and it is
either fully acquired or not at all, even when interrupted. `tryAcquire(n)` is
the same without waiting. The page's `MultiPermitExample` shows the risky
one-at-a-time version.

Write `Leases`, which hands out I/O buffers from a fixed stock, backed by a
`Semaphore`:

- `void lease(int n)` waits until `n` buffers are free and takes them all at
  once. A caller interrupted while it waits leaves with `InterruptedException`
  and holds nothing.
- `boolean tryLease(int n)` takes `n` buffers only if all `n` are free right
  now, and otherwise takes **none** and returns `false`.
- `void giveBack(int n)` returns `n` buffers.
- `int available()` is the number of free buffers.

| stock | calls | answer |
|---|---|---|
| 4 | `lease(3)`, `available()`, `giveBack(3)`, `tryLease(2)`, `available()` | `1`, `true`, `2` |
| 4 | `lease(2)`; another thread's `lease(3)` waits; `available()` | `2` (the waiter holds none) |
| 4 | `lease(2)`, `tryLease(3)`, `available()` | `false`, `2` |
| 4 | `lease(2)`; another thread waits in `lease(3)` and is interrupted | it throws `InterruptedException`; `available()` is `2` |
