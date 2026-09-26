The page's `TimedCounter` shows what `synchronized` cannot do: `tryLock()` gives
up at once when the lock is taken, `tryLock(time, unit)` gives up after a
timeout, and the diagnostics `hasQueuedThreads()` and `getQueueLength()` tell
you whether anyone is waiting. The page's pitfall: `tryLock()` returns a
`boolean`, and a caller that ignores it enters the critical section without
the lock.

Write `TimedCounter`, a counter guarded by the `ReentrantLock` it is given:

- `boolean tryIncrement()` adds one only if it gets the lock **without
  waiting**; it returns whether it counted.
- `boolean increment(long timeout, TimeUnit unit)` waits **at most** `timeout`
  (in the given `unit`) for the lock; it returns whether it counted.
  An interrupt while it waits ends it with `InterruptedException`.
- `long count()` returns the count.
- `boolean isContended()` is `true` when at least one thread is **waiting** for
  the lock (not merely when the lock is held).

The lock is reentrant: a thread that already holds it gets it again at once. Every path that takes the lock should release it, and reading the count under
the lock too keeps the read consistent with the writes.

| situation | call | answer |
|---|---|---|
| lock free | `tryIncrement()`, `increment(1, SECONDS)`, `count()` | `true`, `true`, `2` |
| another thread holds the lock | `tryIncrement()` | `false` at once; nothing counted |
| another thread holds the lock | `increment(100_000, MICROSECONDS)` | `false` after about 100 ms |
| another thread holds it, then releases it | `increment(5, SECONDS)` | `true` |
| another thread holds it, nobody waits | `isContended()` | `false` |
| another thread holds it, a third waits | `isContended()` | `true` |
