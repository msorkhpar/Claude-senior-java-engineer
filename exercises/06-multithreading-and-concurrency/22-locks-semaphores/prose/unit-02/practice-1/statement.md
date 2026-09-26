The page's `ConnectionPool` guards N connections with a `Semaphore(N)`:
`acquire()` takes a permit (waiting while there are none) and then a
connection; `release` puts the connection back and returns the permit. The page
also warns about **permit inflation**: a semaphore enforces no upper bound, so
one release too many silently lets one more thread in. Guard against it in
your own code.

Write `Pool<R>`, built from a list of resources:

- `R acquire()` waits until a resource is free and returns it.
- `R tryAcquire(long timeout, TimeUnit unit)` waits **at most** `timeout`;
  `null` if nothing came free.
- `void release(R resource)` returns a leased resource. A resource that is not
  **currently leased** (released twice, or never handed out by this pool) is
  refused with `IllegalArgumentException` and changes nothing. Leases are
  tracked by **object identity**: a different object that merely `equals` a
  leased one was not leased.
- `int available()` is the number of free resources.

| pool | calls | answer |
|---|---|---|
| `[db-1, db-2]` | `acquire()`, `acquire()`, `available()`; release both; `available()` | `0`, then `2` |
| `[db-1]`, leased | another thread's `acquire()`, then `release(db-1)` | that `acquire()` waits, then returns that same `db-1` object |
| `[db-1]`, leased | `tryAcquire(100_000, MICROSECONDS)` | `null` after about 100 ms |
| `[db-1]`, leased | another thread's `tryAcquire(8, SECONDS)`, then `release(db-1)` | that call waits, then returns `db-1` |
| `[db-1]` | `acquire()`, `release(r)`, `release(r)` | second `release`: `IllegalArgumentException`; `available()` is `1` |
| `[db-1]`, leased | `release(new String("db-1"))` | `IllegalArgumentException`; `available()` is `0` |
