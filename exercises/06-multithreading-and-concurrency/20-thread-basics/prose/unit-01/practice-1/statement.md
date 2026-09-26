A `Thread` object is only a description of work until something makes it run.
The page's first pitfall is calling `run()` where `start()` was meant: `run()`
is an ordinary method call on the **current** thread, while `start()` creates a
new thread, returns at once, and runs the task there. The page also asks you to
name your threads, so thread dumps and logs say who is doing what.

Write `Launch.launch(String name, Runnable task)`. It creates a platform thread
named `name` for `task`, makes the task run **on that new thread**, and returns
the thread **without waiting** for the task to finish.

| call | what the test sees |
|---|---|
| `launch("payment-processor-1", counter::incrementAndGet)`, then `join` | the counter is `1`; the thread's name is `payment-processor-1` |
| `launch("worker-7", () -> seen.set(Thread.currentThread()))`, then `join` | `seen` is the returned thread, not the caller |
| `launch("slow", taskThatWaitsForAGate)` | returns while the task still waits for the gate |
