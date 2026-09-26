The page's robust task uses **both** cancellation tools: a `volatile boolean`
flag, which a CPU-bound section can check cheaply, and `interrupt()`, which is
the only thing that wakes a thread blocked in `sleep()`. "A volatile flag alone
cannot wake a sleeping thread." Cleanup goes in a `finally` block so it runs
however the task ends.

Write `Ticker`, a `Runnable` that ticks until it is stopped:

- `Ticker(Runnable tick, long pauseMillis, Runnable cleanup)`.
- `run()` repeats: run `tick`, then sleep `pauseMillis`. It stops when it has
  been cancelled or its thread has been interrupted (an interrupt that wakes the
  sleep counts). When it stops, `cleanup` runs exactly once, and `run()` returns
  with the interrupt flag restored if an interrupt stopped it.
- `cancel()` may be called from any thread. It sets a `boolean` field named
  `cancelled`, which must be `volatile`, **and** interrupts the thread running
  the ticker, if one is running.

| situation | result |
|---|---|
| ticking with a 1 ms pause; another thread calls `cancel()` | the thread ends |
| asleep in a 60 s pause; another thread calls `cancel()` | the thread ends at once |
| asleep in a 60 s pause; another thread calls `thread.interrupt()` | the thread ends at once; `cleanup` ran once |
