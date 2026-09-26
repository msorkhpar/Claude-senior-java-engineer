The page's golden rule: release a `ReentrantLock` in a `finally` block, or one
exception leaves it held for ever. And unlike `synchronized` or `lock()`,
`lockInterruptibly()` lets a thread that is **waiting** for the lock be
cancelled with `Thread.interrupt()`: it leaves with `InterruptedException` and
does not get the lock.

Write `Exclusive.run(Runnable action)`, which runs `action` while holding the
`ReentrantLock` the object was built with:

- It waits for the lock, but a caller interrupted **while it waits** leaves
  with `InterruptedException` and does not run the action.
- The lock is released after the action, whether the action returns or throws
  anything at all, an exception or an `Error`; what it throws reaches the
  caller unchanged.

| situation | call | answer |
|---|---|---|
| lock free | `run(action)` | `action` runs holding the lock; the lock is free afterwards |
| another thread holds the lock; the caller is interrupted while it waits | `run(action)` | throws `InterruptedException`; `action` never runs |
| lock free; `action` throws `IllegalStateException` | `run(action)` | throws that `IllegalStateException`; the lock is free |
