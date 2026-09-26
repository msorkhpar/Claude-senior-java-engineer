The page calls it the most common JMM bug: a loop `while (running) { … }` over a plain
field. Nothing in the loop writes `running`, so the JIT may hoist the read out of the
loop and turn it into `if (running) { while (true) { … } }`, and a stop requested by
another thread is never seen. The fix is one word: make the flag `volatile`.

Write `Spinner`, the page's stop-flag loop:

- `requestStop()` asks the loop to stop; any thread may call it.
- `stopRequested()` says whether a stop has been requested.
- `runUntilStopped(Runnable step)` checks the flag **before** each step, runs `step` while
  no stop has been requested, and returns how many steps ran.

| scenario | answer |
|---|---|
| the step calls `requestStop()` on its 3rd run | `3` |
| `requestStop()` before `runUntilStopped` | `0`, and the step never runs |
| `stopRequested()` on a new `Spinner` | `false` |
