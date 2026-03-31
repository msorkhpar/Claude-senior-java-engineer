# 6.4.2. Callable and Future for Returning Results

## Concept Explanation

While `Runnable` has been part of Java since version 1.0, it has a fundamental limitation: it cannot return a result or throw a checked exception. Java 5 introduced `Callable<V>` and `Future<V>` to address these gaps, enabling asynchronous computation with result retrieval and proper error handling.

**Real-world analogy**: Think of `Callable` and `Future` like ordering food at a busy restaurant's counter. When you place your order (submit a `Callable`), the cashier hands you a receipt with a number (a `Future`). You can go sit down and do other things. When your order is ready, you present your receipt to pick up the food (`Future.get()`). If the kitchen had a problem with your order (exception), you find out when you try to pick it up. You can also decide to cancel your order (`Future.cancel()`) or check whether it's ready yet (`Future.isDone()`).

### Callable vs Runnable

```java
// Runnable: no return value, no checked exceptions
@FunctionalInterface
public interface Runnable {
    void run();
}

// Callable: returns a value and can throw checked exceptions
@FunctionalInterface
public interface Callable<V> {
    V call() throws Exception;
}
```

### Future Interface

`Future<V>` represents the result of an asynchronous computation. It provides methods to:
- **Wait for the result**: `get()` blocks until the result is available
- **Wait with a timeout**: `get(long timeout, TimeUnit unit)` blocks for at most the specified time
- **Check completion**: `isDone()` returns `true` if the computation is complete
- **Cancel the computation**: `cancel(boolean mayInterruptIfRunning)`
- **Check cancellation**: `isCancelled()` returns `true` if the task was cancelled

### Batch Execution

`ExecutorService` provides two powerful batch methods:
- **`invokeAll(Collection<Callable>)`**: Submits all tasks and blocks until ALL complete. Returns a list of `Future` objects in the same order as the input.
- **`invokeAny(Collection<Callable>)`**: Submits all tasks and returns the result of the FIRST one to complete successfully. Remaining tasks are cancelled.

### CompletionService

`CompletionService` wraps an `ExecutorService` and provides a way to process results in **completion order** rather than submission order, using an internal `BlockingQueue` of completed `Future` objects.

## Key Points to Remember

1. `Callable<V>` is a functional interface, so it can be expressed as a lambda expression.
2. `Future.get()` is a blocking call -- it will block the calling thread until the result is available.
3. `Future.get(timeout, unit)` throws `TimeoutException` if the result is not available within the specified time.
4. If a `Callable` throws an exception, it is wrapped in `ExecutionException` and rethrown by `Future.get()`. Use `getCause()` to access the original exception.
5. `Future.cancel(true)` sends an interrupt to the running thread; `cancel(false)` only prevents the task from starting if it hasn't already.
6. Calling `get()` on a cancelled `Future` throws `CancellationException`.
7. `invokeAll()` blocks until all tasks complete; `invokeAny()` blocks until the first task completes.
8. `invokeAll()` with a timeout cancels tasks that have not completed when time expires.
9. `invokeAny()` throws `ExecutionException` if ALL tasks fail; it only needs ONE to succeed.
10. `CompletionService` is useful when you want to process results as they become available, rather than waiting for the slowest task.

## Relevant Java 21 Features

- **CompletableFuture**: While `Future` is the basic building block, `CompletableFuture` (Java 8+) extends `Future` with a rich API for chaining, combining, and composing asynchronous operations without blocking. `Future.get()` is blocking; `CompletableFuture.thenApply()` is non-blocking.
- **Virtual Threads**: With virtual threads (Java 21), blocking on `Future.get()` is much less costly because virtual threads are cheap to park and resume. This reduces the need for complex non-blocking patterns in many cases.
- **Structured Concurrency (Preview, JEP 462)**: `StructuredTaskScope` provides a structured way to fork multiple tasks and join them, with built-in cancellation and error propagation, serving as a modern alternative to manual `invokeAll()` patterns.

```java
// CompletableFuture (non-blocking chain)
CompletableFuture.supplyAsync(() -> fetchData())
    .thenApply(data -> process(data))
    .thenAccept(result -> saveResult(result))
    .exceptionally(ex -> { log.error("Failed", ex); return null; });

// Structured Concurrency (Java 21 preview)
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    Subtask<String> user = scope.fork(() -> fetchUser());
    Subtask<Integer> order = scope.fork(() -> fetchOrder());
    scope.join();
    scope.throwIfFailed();
    return new Response(user.get(), order.get());
}
```

## Common Pitfalls and How to Avoid Them

1. **Blocking the main thread indefinitely with `Future.get()`**
   ```java
   // BAD: Blocks forever if task hangs
   String result = future.get();
   
   // GOOD: Always use a timeout
   String result = future.get(30, TimeUnit.SECONDS);
   ```

2. **Not checking for exceptions in submitted tasks**
   ```java
   // BAD: Exception is silently swallowed
   Future<?> future = executor.submit(() -> {
       throw new RuntimeException("Error!");
   });
   // future is never inspected
   
   // GOOD: Always check the Future
   try {
       future.get(5, TimeUnit.SECONDS);
   } catch (ExecutionException e) {
       log.error("Task failed", e.getCause());
   }
   ```

3. **Confusing `ExecutionException` with the actual cause**
   ```java
   // BAD: Catching the wrapper instead of the cause
   try {
       future.get();
   } catch (ExecutionException e) {
       throw e; // This wraps the real exception
   }
   
   // GOOD: Unwrap the cause
   try {
       future.get();
   } catch (ExecutionException e) {
       Throwable cause = e.getCause();
       if (cause instanceof IOException ioe) {
           handleIOException(ioe);
       } else {
           throw new RuntimeException("Unexpected failure", cause);
       }
   }
   ```

4. **Forgetting to cancel the Future on timeout**
   ```java
   // BAD: Task continues running after timeout
   try {
       result = future.get(5, TimeUnit.SECONDS);
   } catch (TimeoutException e) {
       // Task is still running in the background!
   }
   
   // GOOD: Cancel the task on timeout
   try {
       result = future.get(5, TimeUnit.SECONDS);
   } catch (TimeoutException e) {
       future.cancel(true); // interrupt the running task
       result = defaultValue;
   }
   ```

5. **Using `invokeAll()` when you only need the first result**
   ```java
   // BAD: Waits for ALL tasks even though you only need the fastest
   List<Future<String>> futures = executor.invokeAll(tasks);
   String result = futures.get(0).get();
   
   // GOOD: Use invokeAny() for first-to-complete semantics
   String result = executor.invokeAny(tasks);
   ```

## Best Practices and Optimization Techniques

1. **Always set timeouts** on `Future.get()` to prevent indefinite blocking.
2. **Use `CompletionService`** when processing results as they arrive is important (avoids head-of-line blocking).
3. **Prefer `Callable` over `Runnable`** when you need a return value or need to propagate checked exceptions.
4. **Use `invokeAny()`** for redundant computation patterns (e.g., querying multiple replicas and using the first response).
5. **Unwrap `ExecutionException`** to handle the root cause, not the wrapper.
6. **Cancel tasks you no longer need** to free up thread pool resources.
7. **Consider `CompletableFuture`** for complex async workflows with chaining, composition, and non-blocking callbacks.
8. **Implement retry logic** with exponential backoff for transient failures.
9. **Use `invokeAll(tasks, timeout, unit)`** to prevent long-running tasks from blocking the entire batch.

## Edge Cases and Their Handling

1. **Empty task list**: `invokeAll(emptyList)` returns an empty list immediately. `invokeAny(emptyList)` throws `IllegalArgumentException`.
2. **All tasks fail in `invokeAny()`**: Throws `ExecutionException` with one of the task's exceptions.
3. **`Future.get()` after `cancel()`**: Throws `CancellationException`, regardless of the `mayInterruptIfRunning` flag.
4. **Interrupted thread calling `Future.get()`**: `InterruptedException` is thrown. You must decide whether to re-interrupt and propagate, or retry.
5. **Task returning `null`**: `Future.get()` returns `null`. This is valid but can be confusing -- consider using `Optional<V>` as the Callable's return type.
6. **Double `get()` call**: `Future.get()` is idempotent -- calling it multiple times returns the same result (or throws the same exception).
7. **`cancel(false)` vs `cancel(true)`**: `cancel(false)` only prevents an un-started task from running. If the task has already started, it continues. `cancel(true)` also sends an interrupt, but the task must check `Thread.interrupted()` or catch `InterruptedException` to cooperatively stop.

## Interview-specific Insights

Interviewers commonly focus on:
- The difference between `Callable` and `Runnable` and why `Callable` was introduced.
- How exceptions are propagated through `Future.get()`.
- The blocking nature of `Future.get()` and how to mitigate it.
- The semantics of `cancel()` and its interaction with `get()`.
- `invokeAll()` vs `invokeAny()` and their use cases.
- Progression from `Future` to `CompletableFuture` and what problems each solves.
- How to implement patterns like timeout, retry, and parallel fan-out/fan-in.

Tricky questions:
- "What happens if you call `Future.get()` on a cancelled Future?"
- "What is the difference between `cancel(true)` and `cancel(false)`?"
- "How would you implement a timeout for a batch of tasks?"
- "When would you use `invokeAny()` over `invokeAll()`?"

## Interview Q&A Section

**Q1: What is the difference between `Callable` and `Runnable`?**

```text
A1: Callable and Runnable are both functional interfaces for defining tasks,
but they differ in two key ways:

1. Return value:
   - Runnable.run() returns void
   - Callable.call() returns a value of type V (generic)

2. Exception handling:
   - Runnable.run() cannot throw checked exceptions
   - Callable.call() can throw any Exception, including checked exceptions

3. Usage with ExecutorService:
   - Runnable can be used with both execute() and submit()
   - Callable can only be used with submit() (returns Future<V>)

When Callable was introduced in Java 5, it solved the long-standing problem
of getting results back from asynchronous tasks without resorting to shared
mutable state (like using AtomicReference or CountDownLatch with Runnable).
```

```java
ExecutorService executor = Executors.newFixedThreadPool(2);

// Runnable - no return value, no checked exceptions
Runnable runnable = () -> System.out.println("Hello from Runnable");
executor.execute(runnable);

// Callable - returns a value, can throw checked exceptions
Callable<Integer> callable = () -> {
    // Can throw checked exceptions here
    return 42;
};
Future<Integer> future = executor.submit(callable);
int result = future.get(); // 42

executor.shutdown();
```

**Q2: How does exception handling work with `Future.get()`?**

```text
A2: When a Callable (or Runnable submitted via submit()) throws an exception,
it is captured by the Future and rethrown when get() is called:

1. RuntimeException thrown in task -> wrapped in ExecutionException -> thrown by get()
2. Checked Exception thrown in task -> wrapped in ExecutionException -> thrown by get()
3. Error thrown in task -> wrapped in ExecutionException -> thrown by get()

Future.get() can throw three types of exceptions:
- ExecutionException: The task failed. Use getCause() to get the original exception.
- InterruptedException: The waiting thread was interrupted.
- CancellationException: The task was cancelled before or during execution.
- TimeoutException: (only for get(timeout, unit)) The timeout expired.

The original exception is always accessible via ExecutionException.getCause().
You should use instanceof or pattern matching to handle different failure types.

Important: If you never call get(), the exception is silently lost! This is a
common source of bugs -- always inspect the Future for tasks that might fail.
```

```java
ExecutorService executor = Executors.newSingleThreadExecutor();

// Task that throws a checked exception
Future<String> future = executor.submit(() -> {
    throw new java.io.IOException("File not found");
});

try {
    String result = future.get();
} catch (ExecutionException e) {
    Throwable cause = e.getCause();
    // cause is the original IOException
    System.out.println("Cause type: " + cause.getClass().getSimpleName());
    System.out.println("Message: " + cause.getMessage());

    // Pattern matching (Java 21)
    switch (cause) {
        case java.io.IOException ioe -> System.out.println("IO error: " + ioe.getMessage());
        case IllegalArgumentException iae -> System.out.println("Bad arg: " + iae.getMessage());
        default -> throw new RuntimeException("Unexpected", cause);
    }
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}

executor.shutdown();
```

**Q3: What is the difference between `cancel(true)` and `cancel(false)`?**

```text
A3: Future.cancel(boolean mayInterruptIfRunning) attempts to cancel the task:

cancel(false):
- If the task hasn't started yet: prevents it from running (returns true)
- If the task is already running: does nothing (returns false)
- If the task is already done/cancelled: returns false

cancel(true):
- If the task hasn't started yet: prevents it from running (returns true)
- If the task is already running: sends an interrupt to the executing thread (returns true)
- If the task is already done/cancelled: returns false

Key subtlety: cancel(true) sends an interrupt, but it doesn't guarantee the task stops.
The task must cooperatively check for interruption:
- Check Thread.currentThread().isInterrupted()
- Catch InterruptedException from blocking operations (sleep, wait, I/O)

After cancellation (regardless of the flag):
- isDone() returns true
- isCancelled() returns true
- get() throws CancellationException

A task that ignores interrupts will continue running even after cancel(true),
but the Future will report it as cancelled and get() will throw CancellationException.
```

```java
ExecutorService executor = Executors.newSingleThreadExecutor();

// Task that cooperatively checks for interruption
Future<?> future = executor.submit(() -> {
    while (!Thread.currentThread().isInterrupted()) {
        // Do work...
        try {
            Thread.sleep(100); // Throws InterruptedException if interrupted
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupt status
            break; // Exit gracefully
        }
    }
    System.out.println("Task stopped cooperatively");
    return null;
});

Thread.sleep(250);
boolean cancelled = future.cancel(true); // Interrupts the running thread
System.out.println("Cancel result: " + cancelled);    // true
System.out.println("Is cancelled: " + future.isCancelled()); // true
System.out.println("Is done: " + future.isDone());           // true

try {
    future.get(); // Throws CancellationException
} catch (CancellationException e) {
    System.out.println("Cannot get result of cancelled task");
}

executor.shutdown();
```

**Q4: What is the difference between `invokeAll()` and `invokeAny()`?**

```text
A4: Both are batch execution methods on ExecutorService, but they serve different purposes:

invokeAll(Collection<Callable<T>>):
- Submits all tasks and BLOCKS until ALL are complete
- Returns List<Future<T>> in the same order as input
- Every Future in the result list will have isDone() == true
- Tasks that threw exceptions have ExecutionException in their Future
- With timeout: tasks not finished are cancelled
- Use case: parallel fan-out where you need ALL results (e.g., scatter-gather)

invokeAny(Collection<Callable<T>>):
- Submits all tasks and BLOCKS until the FIRST succeeds
- Returns the result of the first successfully completed task (not a Future)
- Remaining tasks are cancelled automatically
- If ALL tasks fail, throws ExecutionException
- With timeout: throws TimeoutException if no task completes in time
- Use case: redundant computation, querying replicas for fastest response

Key differences:
| Aspect          | invokeAll()                  | invokeAny()              |
|-----------------|------------------------------|--------------------------|
| Returns         | List<Future<T>>              | T (direct result)        |
| Waits for       | ALL tasks                    | FIRST success            |
| Failed tasks    | Future contains exception    | Ignored (if at least one succeeds) |
| Cancellation    | Only with timeout variant    | Remaining tasks cancelled |
| Empty input     | Returns empty list           | Throws IllegalArgumentException |
```

```java
ExecutorService executor = Executors.newFixedThreadPool(3);
List<Callable<String>> tasks = List.of(
    () -> { Thread.sleep(300); return "slow"; },
    () -> { Thread.sleep(100); return "fast"; },
    () -> { Thread.sleep(200); return "medium"; }
);

// invokeAll - waits for ALL tasks
List<Future<String>> allResults = executor.invokeAll(tasks);
for (Future<String> f : allResults) {
    System.out.println(f.get()); // slow, fast, medium (in submission order)
}

// invokeAny - returns FIRST successful result
String fastest = executor.invokeAny(tasks);
System.out.println("Fastest: " + fastest); // "fast" (completed in 100ms)

executor.shutdown();
```

**Q5: What is `CompletionService` and when should you use it?**

```text
A5: CompletionService is an interface (implemented by ExecutorCompletionService)
that decouples task submission from result retrieval by maintaining an internal
queue of completed Future objects.

Problem it solves:
With a list of Future objects, if you iterate in order and call get() on each,
you block on the first one even if later tasks have already completed. This is
called "head-of-line blocking."

How it works:
1. You submit tasks via completionService.submit(callable)
2. As tasks complete, their Futures are placed into an internal BlockingQueue
3. You call take() (blocking) or poll() (non-blocking) to get the next completed Future
4. Results come back in COMPLETION ORDER, not submission order

Use cases:
- Processing results as soon as they are available (e.g., rendering web page
  components as each loads)
- Implementing timeout for a batch: process whatever completes within the deadline
- Load balancing: feed results to downstream processing as they arrive

CompletionService vs invokeAll:
- invokeAll blocks until ALL tasks complete, then you process results
- CompletionService lets you process results INCREMENTALLY as they complete
```

```java
ExecutorService executor = Executors.newFixedThreadPool(3);
CompletionService<String> cs = new ExecutorCompletionService<>(executor);

// Submit tasks with varying durations
cs.submit(() -> { Thread.sleep(300); return "task-3 (300ms)"; });
cs.submit(() -> { Thread.sleep(100); return "task-1 (100ms)"; });
cs.submit(() -> { Thread.sleep(200); return "task-2 (200ms)"; });

// Process results in completion order (fastest first)
for (int i = 0; i < 3; i++) {
    Future<String> completed = cs.take(); // blocks for next completed task
    System.out.println("Completed: " + completed.get());
}
// Output order: task-1 (100ms), task-2 (200ms), task-3 (300ms)

// With timeout - process whatever completes within 150ms
cs.submit(() -> { Thread.sleep(50); return "fast"; });
cs.submit(() -> { Thread.sleep(500); return "slow"; });

Future<String> result = cs.poll(150, TimeUnit.MILLISECONDS);
if (result != null) {
    System.out.println("Got: " + result.get()); // "fast"
}

executor.shutdown();
```

**Q6: How would you implement a timeout for an asynchronous operation using `Future`?**

```text
A6: There are several approaches to implementing timeouts with Future:

1. Future.get(timeout, unit): The simplest approach. Blocks for at most the
   specified time, then throws TimeoutException.

2. invokeAll(tasks, timeout, unit): For batch operations. Tasks not completed
   within the timeout are cancelled.

3. invokeAny(tasks, timeout, unit): For first-result semantics with a deadline.

4. ScheduledExecutorService: Schedule a cancellation task that runs after the timeout.

Best practice pattern:
- Call get(timeout, unit) wrapped in try-catch
- On TimeoutException, cancel the Future with interrupt
- Return a default value or rethrow

Important considerations:
- Always cancel the Future on timeout to free pool resources
- Remember that cancel(true) only sends an interrupt -- the task must cooperate
- For strict timeouts, consider wrapping the task to check elapsed time internally
- Virtual threads (Java 21) make blocking on get() less expensive
```

```java
ExecutorService executor = Executors.newSingleThreadExecutor();

// Pattern 1: Simple timeout with fallback
public <T> T computeWithTimeout(Callable<T> task, long timeoutMs, T fallback) {
    Future<T> future = executor.submit(task);
    try {
        return future.get(timeoutMs, TimeUnit.MILLISECONDS);
    } catch (TimeoutException e) {
        future.cancel(true); // Stop the task
        return fallback;
    } catch (ExecutionException e) {
        throw new RuntimeException("Task failed", e.getCause());
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        future.cancel(true);
        return fallback;
    }
}

// Pattern 2: Scheduled cancellation
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
Future<String> future = executor.submit(() -> {
    Thread.sleep(5000);
    return "result";
});

// Auto-cancel after 1 second
scheduler.schedule(() -> future.cancel(true), 1, TimeUnit.SECONDS);
```

**Q7: What is the retry pattern with `Callable` and `Future`?**

```text
A7: The retry pattern re-submits a failed Callable up to a maximum number of attempts.
This is useful for transient failures like network timeouts or temporary service
unavailability.

Key design decisions:
1. Max retries: How many times to retry (usually 2-3)
2. Backoff strategy: How long to wait between retries
   - Fixed delay: sleep(1000ms) between retries
   - Exponential backoff: sleep(1000ms, 2000ms, 4000ms, ...)
   - Jitter: Add randomness to prevent thundering herd
3. Retryable exceptions: Not all exceptions should be retried
   (e.g., retry IOException but not IllegalArgumentException)
4. Timeout per attempt: Each attempt should have its own timeout

Implementation considerations:
- Use a new Future for each retry (don't reuse the old one)
- Log each retry attempt for debugging
- Have a circuit breaker to stop retrying if the system is consistently failing
- Consider using a library like Resilience4j for production retry logic
```

```java
public <T> T executeWithRetry(ExecutorService executor, Callable<T> task,
                               int maxRetries, long delayMs) throws Exception {
    Exception lastException = null;

    for (int attempt = 0; attempt <= maxRetries; attempt++) {
        try {
            Future<T> future = executor.submit(task);
            return future.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            lastException = (Exception) e.getCause();
            System.out.printf("Attempt %d failed: %s%n", attempt + 1, lastException.getMessage());

            if (attempt < maxRetries) {
                // Exponential backoff with jitter
                long backoff = delayMs * (1L << attempt);
                long jitter = (long) (backoff * 0.2 * Math.random());
                Thread.sleep(backoff + jitter);
            }
        } catch (TimeoutException e) {
            lastException = e;
            System.out.printf("Attempt %d timed out%n", attempt + 1);
        }
    }

    throw new RuntimeException("All " + (maxRetries + 1) + " attempts failed", lastException);
}
```

## Code Examples

- Source: [CallableFutureDemo.java](src/main/java/com/github/msorkhpar/claudejavatutor/executors/CallableFutureDemo.java)
- Test: [CallableFutureDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/executors/CallableFutureDemoTest.java)
