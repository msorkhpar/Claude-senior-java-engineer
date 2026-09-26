The page's intrinsic locks are **reentrant**: the JVM keeps a hold count per
lock, a thread that already holds the lock can take it again, and the lock is
free only when the count is back to zero. A waiting thread is woken through the
monitor: `wait()` **in a loop**, since a woken thread may find the mutex taken again,
and `notifyAll()` when the state changes.

Write `ReentrantMutex`, a reentrant lock of your own, built on one monitor
(`synchronized`, `wait()`, `notifyAll()`):

- `void lock()` takes the mutex, waiting while **another** thread holds it. The
  thread that holds it may lock it again; each lock adds one to the hold count.
- `boolean tryLock()` does the same without waiting: `false` if another thread
  holds it.
- `void unlock()` takes one from the hold count; at zero the mutex is free and
  waiting threads are woken. A thread that does not hold the mutex gets an
  `IllegalMonitorStateException`.
- `int holdCount()` is the owner's hold count, `0` when free.
- `boolean isHeldByCurrentThread()`.

| calls | answer |
|---|---|
| `lock()`, `holdCount()` | `1` |
| `lock()`, `lock()`, `unlock()`; another thread's `tryLock()` | `false` |
| then `unlock()`; another thread's `tryLock()` | `true` |
| `unlock()` on a mutex this thread does not hold | `IllegalMonitorStateException` |
| held; two other threads call `lock()`, then `unlock()` | one of them takes it; the other keeps waiting |
