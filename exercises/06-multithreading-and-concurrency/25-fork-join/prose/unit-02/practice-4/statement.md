The page warns that `ForkJoinPool` is **not suitable for blocking** calls: a
worker that blocks is a worker lost to the pool. Its answer is
`ForkJoinPool.ManagedBlocker`: you wrap the blocking call in a blocker with
`block()` and `isReleasable()`, and call `ForkJoinPool.managedBlock(blocker)`.
The pool can then **compensate** by starting an extra thread while yours is
blocked (so the pool can grow beyond its parallelism).

Write `LatchWaiter.await(latch)`: it returns once `latch` has counted down to
zero, and does its waiting through `ForkJoinPool.managedBlock`.
`isReleasable()` is true when the count is already `0`, and `block()` waits
with `latch.await()`. An interrupt while waiting ends `await` with the
`InterruptedException`.

| situation | answer |
|---|---|
| the latch is already at 0 | returns at once |
| a pool of parallelism **1**: task A calls `await` on a latch of count 1, then task B of the same pool counts it down | A returns: B got a thread although A was blocked |
| a thread waits in `await` and is interrupted | `await` throws `InterruptedException` |

With a plain `latch.await()` in the second row, the pool's only worker would
sit blocked in A, and B would never run.
