The page's fourth interview question refactors this toward KISS:

```java
// Before: Over-engineered retry logic
interface RetryStrategy { int getMaxAttempts(); long getDelay(); }
class ExponentialBackoff implements RetryStrategy { ... }
class LinearBackoff implements RetryStrategy { ... }
class RetryExecutor<T> {
    private final RetryStrategy strategy;
    private final List<RetryListener> listeners;
    // ... 100+ lines of code
}
```

Keep only the essential behaviour. Write `executeWithRetry(Supplier<T> action, int maxAttempts)`
in `Retry`, as one loop:

- it calls `action.get()` and returns the first result that does not throw, even when that
  result is `null`;
- an attempt that throws a `RuntimeException` is retried, **up to `maxAttempts` attempts in
  all**: with `maxAttempts` 3, an action that fails twice and then succeeds returns its
  result after exactly 3 calls;
- when every attempt fails, it throws **the last attempt's** exception: that very exception
  object, not a new one wrapping it;
- only `RuntimeException`s are retried. **An `Error`, such as an `AssertionError`, is not
  retried**: it propagates from the first attempt that throws it;
- a `maxAttempts` of 0 or less throws `IllegalArgumentException` without calling the action.

No backoff, no listeners: add them when a real requirement asks for them.
