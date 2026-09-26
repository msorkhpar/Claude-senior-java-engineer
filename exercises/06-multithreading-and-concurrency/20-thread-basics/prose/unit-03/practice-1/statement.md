The page's second path of interruption: a thread that is **running**, not
blocked, is not woken by anything. `interrupt()` only sets its flag, and a
CPU-bound loop must **check** the flag itself, at a natural checkpoint such as
the top of each iteration. It should check with `isInterrupted()`, which leaves
the flag as it is; `Thread.interrupted()` clears it.

Write `Worker.countSteps(IntConsumer step)`:

- Before each step, check whether the current thread has been interrupted. If
  it has, stop.
- Otherwise run step number `n` (1, 2, 3, ...) by calling `step.accept(n)`.
- Return how many steps ran. The interrupt flag must still be set when it
  returns.

| situation | answer |
|---|---|
| another thread interrupts the worker while step 5 runs | `5` |
| the calling thread was interrupted before the call | `0`, and `step` is never called |
| after either | the thread's interrupt flag is still set |
