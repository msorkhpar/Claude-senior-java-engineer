`Future.cancel(true)` does not kill a running task: it **interrupts** the
executor thread that runs it. The task's own code must notice, by checking the
flag or by letting a blocking call throw `InterruptedException`, and stop.
"Future.cancel() does NOT guarantee the task stops immediately."

Write `Jobs.countingTask(int limit, long pauseMillis, IntConsumer step)`. It
returns a `Callable<Integer>` that:

- runs `step.accept(i)` for `i = 1, 2, ..., limit`, sleeping `pauseMillis`
  milliseconds after each step when `pauseMillis > 0`, and returns how many
  steps ran;
- stops early, returning the steps run so far, as soon as its thread is
  interrupted, whether the interrupt arrives during a step or during a sleep.
  It restores the interrupt flag when a sleep was interrupted.

| situation | result |
|---|---|
| `countingTask(10, 0, noop)` submitted and left alone | `get()` is `10` |
| a task with no limit, cancelled with `cancel(true)` while it runs | it stops; the single-thread executor then runs the next task |
| a task with a 60 s pause, cancelled while asleep | it stops at once; the executor runs the next task |
