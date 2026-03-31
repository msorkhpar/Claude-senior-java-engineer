# 8.3.2. Applying DRY in Concurrent Programming

## Concept Explanation

Concurrent programming involves boilerplate-heavy patterns -- acquiring locks, managing executor lifecycles, handling retries, caching results thread-safely. When these patterns are copy-pasted across classes, any bug fix or improvement must be applied in every copy. DRY in concurrency means **extracting reusable patterns** so the locking strategy, retry logic, or executor lifecycle is defined once and shared everywhere.

**Real-world analogy**: Imagine every office in a building has its own independent security system with its own keypad code. When the building manager changes the access policy, every office must be reconfigured individually -- and forgetting one creates a security hole. DRY says: use one centralized access control system that every office relies on.

This section covers two sub-topics:
- **8.3.2.1. Extracting reusable concurrency patterns** -- encapsulating lock/try/finally, parallel execution, retry logic into reusable utilities.
- **8.3.2.2. Avoiding duplication of synchronization logic** -- ensuring all code paths use the same synchronization mechanism rather than each class implementing its own.

## Key Points to Remember

- The lock-try-finally pattern is the most frequently duplicated concurrency code -- extract it into a `LockExecutor`.
- ExecutorService lifecycle management (create, submit, shutdown, awaitTermination) is another common duplication target.
- Retry logic for transient failures should live in a single `RetryExecutor`, not be pasted into every call site.
- Thread-safe caching (using `ConcurrentHashMap.computeIfAbsent`) should be encapsulated in a reusable cache class.
- When duplicated synchronization logic diverges (e.g., one copy uses `synchronized`, another uses `ReentrantLock`), subtle concurrency bugs emerge.

## Relevant Java 21 Features

- **Virtual threads (JEP 444)**: Simplify executor management -- `Executors.newVirtualThreadPerTaskExecutor()` reduces the need for custom thread pool boilerplate.
- **Structured concurrency (JEP 453, preview)**: `StructuredTaskScope` eliminates duplicated fork-join-cancel patterns.
- **Records**: Ideal for immutable results returned from reusable concurrent utilities.
- **`try-with-resources`**: ExecutorService now implements `AutoCloseable` in Java 21, reducing shutdown boilerplate.

## Common Pitfalls and How to Avoid Them

1. **Duplicated lock-try-finally across methods**
   ```java
   // VIOLATION: Same pattern in deposit(), withdraw(), getBalance()
   lock.lock();
   try { balance += amount; return balance; }
   finally { lock.unlock(); }
   ```
   **Solution**: Extract into a `LockExecutor.withLock(Supplier)` utility method.

2. **Inconsistent executor shutdown patterns**
   ```java
   // Copy A: executor.shutdown(); executor.awaitTermination(5, SECONDS);
   // Copy B: executor.shutdownNow(); // forgot awaitTermination!
   ```
   **Solution**: Centralize into a `ParallelComputation.executeAll()` method.

3. **Duplicated retry logic with different error handling**
   ```java
   // ServiceA retries 3 times, ServiceB retries 5 times, ServiceC has no delay
   ```
   **Solution**: Use a configurable `RetryExecutor(maxRetries, delayMs)`.

4. **Rolling your own cache in every service**
   ```java
   // Each service creates its own ConcurrentHashMap + computeIfAbsent
   ```
   **Solution**: Use a reusable `ThreadSafeCache<K,V>` class.

## Best Practices and Optimization Techniques

1. **Parameterize the varying part**: Use `Supplier<T>`, `Callable<T>`, and `Runnable` to inject behavior into reusable lock/retry/execution wrappers.
2. **Make utilities immutable and thread-safe**: `LockExecutor` and `RetryExecutor` should be safe to share across threads.
3. **Use `try-with-resources` for executors in Java 21**: `ExecutorService` is `AutoCloseable`.
4. **Prefer `invokeAll` over manual future collection**: Reduces boilerplate and handles exceptions consistently.
5. **Keep retry configuration external**: Pass `maxRetries` and `delay` as constructor parameters, not hard-coded values.

## Edge Cases and Their Handling

1. **Null actions**: `LockExecutor.withLock(null)` should throw `NullPointerException` immediately, not after acquiring the lock.
2. **Zero retries**: `RetryExecutor(0, 0)` should execute exactly once with no retry.
3. **Exceptions in locked actions**: The lock must be released even if the action throws -- the `finally` block in the reusable utility guarantees this.
4. **Empty task lists**: `ParallelComputation.executeAll(emptyList, 4)` should return an empty list, not throw.
5. **Negative thread counts**: Should throw `IllegalArgumentException` with a clear message.

## Interview-specific Insights

Interviewers look for:
- Whether you recognize the lock-try-finally pattern as a DRY opportunity
- Your ability to use higher-order functions (`Supplier`, `Callable`) to parameterize concurrent utilities
- Understanding of why inconsistent synchronization is dangerous
- Awareness of Java 21 features (virtual threads, structured concurrency) that reduce boilerplate

Tricky questions:
- "How would you refactor three classes that each have their own ReentrantLock management?" (Extract a `LockExecutor`.)
- "What happens if your shared retry utility has a bug?" (Single point of failure -- but also a single point of fix.)

## Interview Q&A Section

**Q1: How would you eliminate duplicated lock-try-finally boilerplate in Java?**

```text
A1: Create a reusable LockExecutor class that accepts a Lock and provides withLock(Supplier<T>)
and withLockRun(Runnable) methods. The lock acquisition, try block, and finally-unlock pattern
is written exactly once. All callers pass their business logic as a lambda.

Benefits:
1. The locking pattern is correct in one place -- no risk of forgetting unlock.
2. Changing lock type (e.g., to ReadWriteLock) only requires modifying LockExecutor.
3. The calling code focuses on business logic, not concurrency mechanics.
```

```java
public class LockExecutor {
    private final Lock lock;
    public LockExecutor(Lock lock) { this.lock = lock; }

    public <T> T withLock(Supplier<T> action) {
        lock.lock();
        try { return action.get(); }
        finally { lock.unlock(); }
    }
}

// Usage: no duplicated lock boilerplate
class Account {
    private final LockExecutor locker = new LockExecutor(new ReentrantLock());
    private int balance = 0;

    public int deposit(int amount) {
        return locker.withLock(() -> { balance += amount; return balance; });
    }
    public int getBalance() {
        return locker.withLock(() -> balance);
    }
}
```

**Q2: Why is duplicated executor lifecycle management dangerous?**

```text
A2: ExecutorService requires careful lifecycle management: create, submit tasks, shutdown,
and awaitTermination. When this pattern is duplicated:

1. One copy might forget shutdown() -- causing thread leaks.
2. Another might use shutdownNow() instead of shutdown() -- canceling running tasks.
3. A third might skip awaitTermination() -- returning results before tasks complete.

By extracting a reusable ParallelComputation utility, the lifecycle is correct everywhere.
In Java 21, ExecutorService implements AutoCloseable, so try-with-resources handles shutdown
automatically -- an even DRYer approach.
```

```java
// DRY: Reusable parallel execution utility
public class ParallelComputation {
    public <T> List<T> executeAll(List<Callable<T>> tasks, int threadCount)
            throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        try {
            List<Future<T>> futures = executor.invokeAll(tasks);
            List<T> results = new ArrayList<>();
            for (Future<T> f : futures) {
                try { results.add(f.get()); }
                catch (ExecutionException e) { throw new RuntimeException(e); }
            }
            return results;
        } finally {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
```

**Q3: How does a reusable retry pattern improve concurrent systems?**

```text
A3: Concurrent systems frequently encounter transient failures (network timeouts,
lock contention, temporary resource unavailability). A reusable RetryExecutor:

1. Centralizes retry count and delay configuration -- no magic numbers scattered around.
2. Ensures consistent behavior -- all retries use the same backoff strategy.
3. Makes testing easier -- inject a RetryExecutor with 0 retries for fast tests.
4. Handles edge cases correctly -- what if maxRetries is 0? What if the delay is 0?

Without DRY, each caller implements its own retry loop, often with subtle differences
in error handling or delay logic.
```

```java
public class RetryExecutor {
    private final int maxRetries;
    private final long retryDelayMs;

    public RetryExecutor(int maxRetries, long retryDelayMs) {
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
    }

    public <T> T executeWithRetry(Callable<T> action) throws Exception {
        Exception lastException = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try { return action.call(); }
            catch (Exception e) {
                lastException = e;
                if (attempt < maxRetries && retryDelayMs > 0) Thread.sleep(retryDelayMs);
            }
        }
        throw lastException;
    }
}
```

**Q4: How does ConcurrentHashMap.computeIfAbsent help with DRY caching?**

```text
A4: Many services need thread-safe caching -- compute a value on first access and return
the cached value on subsequent calls. Without DRY, each service creates its own
ConcurrentHashMap and calls computeIfAbsent directly. With DRY, a reusable
ThreadSafeCache<K,V> encapsulates this:

1. Thread-safe by construction -- ConcurrentHashMap guarantees atomicity.
2. Consistent API -- getOrCompute, get, invalidate, clear.
3. Single place to add features like TTL, max size, or metrics.

This is the same idea behind Guava's LoadingCache or Caffeine, but at a simpler scale.
```

```java
public class ThreadSafeCache<K, V> {
    private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();

    public V getOrCompute(K key, Function<K, V> computeFunction) {
        return cache.computeIfAbsent(key, computeFunction);
    }

    public Optional<V> get(K key) {
        return Optional.ofNullable(cache.get(key));
    }

    public void invalidate(K key) { cache.remove(key); }
    public int size() { return cache.size(); }
    public void clear() { cache.clear(); }
}
```

**Q5: How does Java 21's structured concurrency reduce duplicated fork-join patterns?**

```text
A5: Before structured concurrency, the fork-join pattern was duplicated everywhere:
create executor, submit tasks, collect futures, handle exceptions, shutdown.
Java 21's StructuredTaskScope (preview) provides a single, reusable abstraction:

1. StructuredTaskScope.ShutdownOnFailure -- cancel all if one fails.
2. StructuredTaskScope.ShutdownOnSuccess -- return first result.
3. Automatic lifecycle -- try-with-resources handles shutdown.
4. Child threads are bounded to parent scope -- no orphaned threads.

This is DRY at the framework level: the platform provides the correct fork-join
pattern so developers don't have to rewrite it in every service.
```

```java
// Java 21 structured concurrency (preview) -- DRY fork-join
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    Subtask<String> user = scope.fork(() -> fetchUser(id));
    Subtask<String> order = scope.fork(() -> fetchOrder(id));
    scope.join().throwIfFailed();
    return new UserOrder(user.get(), order.get());
}
// No manual executor creation, shutdown, or future handling!
```

## Code Examples

- Test: [DryConcurrencyPatternsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/dryprinciple/DryConcurrencyPatternsTest.java)
- Source: [DryConcurrencyPatterns.java](src/main/java/com/github/msorkhpar/claudejavatutor/dryprinciple/DryConcurrencyPatterns.java)
