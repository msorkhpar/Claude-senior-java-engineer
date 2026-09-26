The page's pitfall: ServiceA retries 3 times, ServiceB retries 5 times, and ServiceC has no
delay, each with its own loop. Its fix is one configurable `RetryExecutor`, whose retry
count and delay are passed to its constructor rather than hard-coded:

```java
public <T> T executeWithRetry(Callable<T> action) throws Exception {
    Exception lastException = null;
    for (int attempt = 0; attempt <= maxRetries; attempt++) { ... }
    throw lastException;
}
```

Complete `RetryExecutor`:

- the constructor refuses a negative `maxRetries` or a negative `retryDelayMs` with
  `IllegalArgumentException`;
- `executeWithRetry(action)` calls the action and returns its first successful result.
  After a failure it retries, up to `maxRetries` more times, sleeping `retryDelayMs`
  milliseconds between tries (never after the last one) when that is more than zero. Any `Exception`
  is a failure, checked or not. **Zero retries runs the action exactly once.**
  When every try fails, it throws the **last** failure, the very exception object the last
  try threw.

Examples:

- an action that fails twice and then returns `"ok"` gives `"ok"` with
  `new RetryExecutor(3, 0)`, after 3 calls;
- with `new RetryExecutor(0, 0)`, a working action is called once, and a failing one is
  called once and its exception reaches the caller;
- with `new RetryExecutor(2, 0)` and an action that always fails, the action is called 3
  times, and the exception thrown is the one from the third call;
- `new RetryExecutor(-1, 0)` throws `IllegalArgumentException`.
