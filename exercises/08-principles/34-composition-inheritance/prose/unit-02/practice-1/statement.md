Instead of a hierarchy of executor subclasses, one for each retry policy, the page composes
a **strategy**: the executor holds a `RetryStrategy` and asks it, after each failed attempt,
whether to try again and how long to wait.

```java
interface RetryStrategy {
    boolean shouldRetry(int attempt, Exception lastException);
    long delayMillis(int attempt);
}
```

The waiting is composed too: the executor is given a `LongConsumer sleeper` and calls it with
each delay, so a test can record the waits instead of sleeping (production code passes a
sleeper that calls `Thread.sleep`).

Write `ResilientExecutor` and `ExponentialBackoff`:

- `new ResilientExecutor(strategy, sleeper)`, then `execute(task)` calls `task.get()`. If it
  throws, the executor asks `strategy.shouldRetry(attempt, e)`, where `attempt` is the attempt
  that just failed (counting from 1) and `e` is the exception it threw; on yes it passes
  `strategy.delayMillis(attempt)` for that same attempt to the sleeper (only when it is above
  0; a delay of 0 is not slept) and tries again. A task that fails twice under a strategy allowing 3 retries with a 7 ms
  delay returns its result after 3 attempts and 2 sleeps of 7;
- the executor uses **the injected strategy** and nothing else to decide: a strategy that
  never retries stops after 1 attempt, one that allows 5 retries survives 5 failures;
- when the strategy says stop, `execute` rethrows **the last exception** the task threw,
  itself, not a wrapper;
- `attempts()` counts every call to a task this executor made, added up over all its
  `execute` calls;
- `ExponentialBackoff(maxRetries, initialDelayMs, maxDelayMs)` retries while
  `attempt <= maxRetries` and waits `initialDelayMs * 2^(attempt - 1)`, capped at
  `maxDelayMs`: with `(5, 100, 1000)` the delays are 100, 200, 400, 800, and 1000 for every
  attempt after that;
- the page's warning: compute the doubling as a shift that is **checked first**, or a large
  attempt overflows. `delayMillis(64)` and `delayMillis(Integer.MAX_VALUE)` are still 1000.
