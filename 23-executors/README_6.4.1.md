# 6.4.1. ExecutorService Interface and Implementations

## Concept Explanation

The `ExecutorService` is the cornerstone of Java's high-level concurrency framework, introduced in Java 5 as part of the `java.util.concurrent` package. It decouples **task submission** from **task execution**, allowing developers to focus on defining work without worrying about low-level thread management.

**Real-world analogy**: Think of an `ExecutorService` as a restaurant kitchen. You (the caller) submit orders (tasks) to the kitchen manager (the executor). The kitchen has a fixed number of chefs (threads in the pool). When all chefs are busy, new orders wait in a queue. The kitchen manager decides which chef handles which order, when to hire temporary staff (scale up threads), and when to let idle staff go home (reclaim threads). You never directly tell a chef to cook -- you simply submit the order and wait for the result.

Before Java 5, developers had to manually create and manage `Thread` objects, handle thread lifecycle, and build their own task queues. This was error-prone and led to resource leaks, unbounded thread creation, and complex shutdown logic. The `ExecutorService` framework solves all of these problems with a clean abstraction.

### The Hierarchy

```text
Executor                          (root interface: single execute(Runnable) method)
  └── ExecutorService             (adds submit(), invokeAll(), shutdown(), etc.)
        └── ScheduledExecutorService   (adds schedule(), scheduleAtFixedRate(), etc.)
```

### Key Implementations

| Factory Method | Implementation | Behavior |
|---|---|---|
| `Executors.newFixedThreadPool(n)` | `ThreadPoolExecutor` | Fixed number of threads; unbounded queue |
| `Executors.newCachedThreadPool()` | `ThreadPoolExecutor` | Creates threads on demand; reuses idle threads (60s timeout) |
| `Executors.newSingleThreadExecutor()` | Wrapper around `ThreadPoolExecutor` | Single thread; guarantees sequential execution |
| `Executors.newScheduledThreadPool(n)` | `ScheduledThreadPoolExecutor` | Fixed pool for delayed/periodic tasks |
| `Executors.newVirtualThreadPerTaskExecutor()` | (Java 21+) | One virtual thread per task; ideal for I/O-bound workloads |

### ThreadPoolExecutor Configuration

`ThreadPoolExecutor` is the most configurable implementation. Its constructor accepts:

```java
ThreadPoolExecutor(
    int corePoolSize,          // threads kept alive even when idle
    int maximumPoolSize,       // upper bound on threads
    long keepAliveTime,        // idle time before non-core threads are reclaimed
    TimeUnit unit,             // time unit for keepAliveTime
    BlockingQueue<Runnable> workQueue, // queue holding pending tasks
    ThreadFactory threadFactory,       // factory for creating new threads
    RejectedExecutionHandler handler   // policy when queue and pool are full
)
```

**Task submission flow:**
1. If fewer than `corePoolSize` threads are running, a new thread is created for the task.
2. If core threads are busy, the task is added to the `workQueue`.
3. If the queue is full and fewer than `maximumPoolSize` threads exist, a new thread is created.
4. If the queue is full and `maximumPoolSize` threads are running, the `RejectedExecutionHandler` is invoked.

### execute() vs submit()

| Aspect | `execute(Runnable)` | `submit(Runnable/Callable)` |
|---|---|---|
| Return type | `void` | `Future<?>` or `Future<T>` |
| Exception handling | Exceptions propagate to thread's `UncaughtExceptionHandler` | Exceptions are captured in the `Future` |
| Result retrieval | Not possible | Via `Future.get()` |
| Defined in | `Executor` interface | `ExecutorService` interface |

## Key Points to Remember

1. Always shut down an `ExecutorService` when done to prevent resource leaks.
2. `Executors.newFixedThreadPool(n)` uses an unbounded `LinkedBlockingQueue` -- in production, prefer creating `ThreadPoolExecutor` directly with a bounded queue and a rejection policy.
3. `execute()` swallows exceptions silently from the caller's perspective; `submit()` captures them in the `Future`.
4. `Executors.newCachedThreadPool()` can create an unlimited number of threads -- dangerous for high-throughput systems.
5. `Executors.newSingleThreadExecutor()` guarantees task ordering (FIFO).
6. Calling `shutdown()` stops accepting new tasks but completes pending ones; `shutdownNow()` interrupts running tasks and returns un-started ones.
7. Always use `awaitTermination()` after `shutdown()` to wait for clean completion.
8. Custom `ThreadFactory` implementations let you name threads, set daemon status, and configure priority -- invaluable for debugging.
9. `ThreadPoolExecutor` exposes hooks: `beforeExecute()`, `afterExecute()`, and `terminated()` for monitoring and logging.

## Relevant Java 21 Features

- **Virtual Threads (JEP 444)**: `Executors.newVirtualThreadPerTaskExecutor()` creates a new virtual thread for every submitted task. Virtual threads are lightweight (managed by the JVM, not the OS) and are ideal for I/O-bound workloads where thousands of concurrent tasks are common. Unlike platform threads, virtual threads have negligible creation cost.
- **AutoCloseable ExecutorService**: Since Java 19, `ExecutorService` extends `AutoCloseable`, enabling try-with-resources patterns that automatically call `shutdown()` and `awaitTermination()`.
- **Structured Concurrency (Preview, JEP 462)**: Introduces `StructuredTaskScope` for managing groups of related tasks as a unit, simplifying error handling and cancellation in concurrent code.

```java
// Java 21 - try-with-resources with ExecutorService
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> processRequest(request));
}
// executor is automatically shut down here
```

## Common Pitfalls and How to Avoid Them

1. **Never forgetting to shut down the executor**
   ```java
   // BAD: Executor runs forever, application never exits
   ExecutorService executor = Executors.newFixedThreadPool(4);
   executor.submit(() -> doWork());
   // Missing shutdown!
   
   // GOOD: Always shut down
   ExecutorService executor = Executors.newFixedThreadPool(4);
   try {
       executor.submit(() -> doWork());
   } finally {
       executor.shutdown();
       executor.awaitTermination(30, TimeUnit.SECONDS);
   }
   
   // BEST (Java 21+): Use try-with-resources
   try (var executor = Executors.newFixedThreadPool(4)) {
       executor.submit(() -> doWork());
   }
   ```

2. **Using unbounded queues in production**
   ```java
   // BAD: Unbounded queue can cause OutOfMemoryError
   ExecutorService executor = Executors.newFixedThreadPool(4);
   
   // GOOD: Use bounded queue with rejection policy
   ThreadPoolExecutor executor = new ThreadPoolExecutor(
       4, 8, 60L, TimeUnit.SECONDS,
       new ArrayBlockingQueue<>(1000),
       new ThreadPoolExecutor.CallerRunsPolicy()
   );
   ```

3. **Catching exceptions from execute()**
   ```java
   // BAD: Exception is silently lost
   executor.execute(() -> {
       throw new RuntimeException("Oops!");
   });
   
   // GOOD: Use submit() and check the Future
   Future<?> future = executor.submit(() -> {
       throw new RuntimeException("Oops!");
   });
   try {
       future.get();
   } catch (ExecutionException e) {
       log.error("Task failed", e.getCause());
   }
   ```

4. **Using Executors.newCachedThreadPool() without limits**
   ```java
   // BAD: Can create thousands of threads under load
   ExecutorService executor = Executors.newCachedThreadPool();
   
   // GOOD: Configure max threads explicitly
   ThreadPoolExecutor executor = new ThreadPoolExecutor(
       0, 100, 60L, TimeUnit.SECONDS,
       new SynchronousQueue<>()
   );
   ```

5. **Not naming threads**
   ```java
   // BAD: Thread dumps show "pool-1-thread-1" -- unhelpful
   ExecutorService executor = Executors.newFixedThreadPool(4);
   
   // GOOD: Custom ThreadFactory with meaningful names
   ThreadFactory factory = r -> {
       Thread t = new Thread(r, "order-processor-" + counter.incrementAndGet());
       t.setDaemon(true);
       return t;
   };
   ExecutorService executor = Executors.newFixedThreadPool(4, factory);
   ```

## Best Practices and Optimization Techniques

1. **Size the pool appropriately**: For CPU-bound tasks, use `Runtime.getRuntime().availableProcessors()` threads. For I/O-bound tasks, use a larger pool or virtual threads.
2. **Use bounded queues with rejection policies** in production to prevent memory exhaustion.
3. **Prefer `submit()` over `execute()`** to get a `Future` for error checking.
4. **Name your threads** using a custom `ThreadFactory` -- this makes thread dumps readable.
5. **Set daemon threads** for background work that should not prevent JVM shutdown.
6. **Monitor pool metrics**: `ThreadPoolExecutor` exposes `getActiveCount()`, `getCompletedTaskCount()`, `getTaskCount()`, `getQueue().size()` for observability.
7. **Use the two-phase shutdown idiom**:
   ```java
   executor.shutdown();
   if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
       executor.shutdownNow();
       if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
           System.err.println("Pool did not terminate");
       }
   }
   ```
8. **Consider virtual threads** (Java 21+) for I/O-bound workloads with many concurrent tasks.

## Edge Cases and Their Handling

1. **Submitting tasks after shutdown**: Throws `RejectedExecutionException`. Always check `isShutdown()` before submitting if tasks arrive asynchronously.
2. **Thread pool with zero tasks**: `shutdown()` + `awaitTermination()` returns immediately.
3. **InterruptedException during awaitTermination**: Re-interrupt the thread and call `shutdownNow()` as a fallback.
4. **Queue full with CallerRunsPolicy**: The submitting thread runs the task itself, which provides natural backpressure but can block the caller.
5. **Single-thread executor failure**: If the single thread dies due to an uncaught exception, the executor creates a replacement thread automatically.
6. **Null tasks**: Submitting `null` to `execute()` or `submit()` throws `NullPointerException`.

## Interview-specific Insights

Interviewers commonly focus on:
- The difference between `execute()` and `submit()` and when to use each.
- How `ThreadPoolExecutor` decides whether to create a new thread or queue a task.
- The four built-in rejection policies and their trade-offs.
- Why `Executors.newFixedThreadPool()` and `newCachedThreadPool()` are discouraged in production (Alibaba coding guidelines, SonarQube rules).
- Proper shutdown patterns and what happens to pending tasks.
- How virtual threads change the concurrency landscape.
- Thread safety of shared state accessed by tasks.

Tricky questions to expect:
- "What happens if a task submitted via `execute()` throws an exception?"
- "Why might `Executors.newCachedThreadPool()` cause an `OutOfMemoryError`?"
- "How does `CallerRunsPolicy` provide backpressure?"
- "What is the difference between `shutdown()` and `shutdownNow()`?"

## Interview Q&A Section

**Q1: What is the difference between `execute()` and `submit()` in `ExecutorService`?**

```text
A1: execute() and submit() both allow you to run tasks asynchronously, but they differ in several important ways:

1. Return type: execute() returns void. submit() returns a Future<?> or Future<T>.
2. Exception handling: With execute(), uncaught exceptions propagate to the thread's
   UncaughtExceptionHandler and the thread may die. With submit(), exceptions are
   captured inside the returned Future -- they are only thrown when you call Future.get().
3. Interface origin: execute(Runnable) is defined in the Executor interface.
   submit() is defined in ExecutorService and has three overloads:
   submit(Runnable), submit(Callable<T>), and submit(Runnable, T).
4. Task types: execute() only accepts Runnable. submit() accepts both Runnable and Callable.

Best practice: Prefer submit() in production code because it gives you a handle to check
for exceptions and task completion. Use execute() only for fire-and-forget scenarios where
you truly don't care about the result or failure.
```

```java
ExecutorService executor = Executors.newFixedThreadPool(2);

// execute() - fire and forget, exception lost
executor.execute(() -> {
    System.out.println("Running via execute()");
    // Any exception here is NOT visible to the caller
});

// submit() - returns Future, exception captured
Future<?> future = executor.submit(() -> {
    System.out.println("Running via submit()");
    throw new RuntimeException("Error!");
});

try {
    future.get(); // ExecutionException wrapping RuntimeException
} catch (ExecutionException e) {
    System.out.println("Caught: " + e.getCause().getMessage());
}

executor.shutdown();
```

**Q2: How does ThreadPoolExecutor decide whether to create a new thread or queue a task?**

```text
A2: ThreadPoolExecutor follows a specific algorithm when a new task is submitted:

1. If fewer than corePoolSize threads are running, a NEW thread is always created,
   even if other core threads are idle.
2. If corePoolSize or more threads are running, the task is placed into the workQueue.
3. If the workQueue is full AND fewer than maximumPoolSize threads exist, a new thread
   is created to handle the task.
4. If the workQueue is full AND maximumPoolSize threads are already running, the
   RejectedExecutionHandler is invoked.

This means:
- Core threads are always created first (up to corePoolSize)
- Queue is filled next
- Extra threads (beyond core) are only created when the queue is FULL
- If everything is at capacity, the rejection policy kicks in

This is counter-intuitive: increasing maximumPoolSize does NOT help if the queue is
unbounded, because the queue will never be full and extra threads will never be created.
That's why newFixedThreadPool(n) has core == max with an unbounded queue.
```

```java
// Demonstrating the task submission flow
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    2,                          // corePoolSize
    4,                          // maximumPoolSize
    60L, TimeUnit.SECONDS,      // keepAliveTime
    new ArrayBlockingQueue<>(2), // bounded queue of capacity 2
    new ThreadPoolExecutor.AbortPolicy()
);

// Task 1: core thread 1 created (pool: 1, queue: 0)
// Task 2: core thread 2 created (pool: 2, queue: 0)
// Task 3: queued (pool: 2, queue: 1)
// Task 4: queued (pool: 2, queue: 2) -- queue is now FULL
// Task 5: new thread created (pool: 3, queue: 2)
// Task 6: new thread created (pool: 4, queue: 2) -- max reached
// Task 7: RejectedExecutionException! (pool full, queue full)
```

**Q3: What are the four built-in rejection policies, and when would you use each?**

```text
A3: When a ThreadPoolExecutor cannot accept a task (queue full, max threads reached),
it invokes the configured RejectedExecutionHandler. Java provides four built-in policies:

1. AbortPolicy (default): Throws RejectedExecutionException immediately.
   Use when: You want to know immediately that the system is overloaded and handle
   it in the calling code.

2. CallerRunsPolicy: The calling thread runs the task itself.
   Use when: You want natural backpressure. This slows down the producer,
   preventing it from submitting more tasks while the pool is saturated.
   Great for scenarios where slowing down is preferable to losing tasks.

3. DiscardPolicy: Silently drops the rejected task.
   Use when: Losing occasional tasks is acceptable (e.g., non-critical telemetry).
   Dangerous because failures are silent.

4. DiscardOldestPolicy: Discards the oldest task in the queue, then retries submission.
   Use when: Newer tasks are more valuable than older ones (e.g., real-time data feeds).

You can also implement a custom policy by implementing RejectedExecutionHandler.
Common custom policies: log and discard, retry with backoff, persist to a database.
```

```java
// AbortPolicy (default) - throws exception
var abortExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(1), new ThreadPoolExecutor.AbortPolicy());

// CallerRunsPolicy - calling thread executes the task
var callerRunsExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(1), new ThreadPoolExecutor.CallerRunsPolicy());

// Custom rejection handler
RejectedExecutionHandler customHandler = (task, executor) -> {
    System.err.println("Task rejected! Queue size: " + executor.getQueue().size());
    // Could log, persist, retry, etc.
};
var customExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(1), customHandler);
```

**Q4: Why are `Executors.newFixedThreadPool()` and `newCachedThreadPool()` discouraged in production?**

```text
A4: Both factory methods create executors with potentially dangerous defaults:

newFixedThreadPool(n):
- Uses an UNBOUNDED LinkedBlockingQueue
- If tasks are produced faster than consumed, the queue grows without limit
- Can cause OutOfMemoryError as millions of tasks queue up
- No backpressure mechanism

newCachedThreadPool():
- Uses a SynchronousQueue (zero capacity) with maximumPoolSize = Integer.MAX_VALUE
- Creates a new thread for every task when all existing threads are busy
- Under sustained load, can create thousands of OS threads
- Each platform thread consumes ~1MB of stack memory
- Can cause OutOfMemoryError or severe context-switching overhead

The recommended approach is to create ThreadPoolExecutor directly with:
- A BOUNDED queue (e.g., ArrayBlockingQueue with a known capacity)
- A reasonable maximumPoolSize
- An appropriate rejection policy
- A custom ThreadFactory for naming

Many companies (e.g., Alibaba) and static analysis tools (SonarQube) flag direct
use of Executors factory methods as a code smell.
```

```java
// Instead of Executors.newFixedThreadPool(10):
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    10,                               // core pool size
    10,                               // max pool size (same as core for fixed behavior)
    0L, TimeUnit.SECONDS,             // no timeout for core threads
    new ArrayBlockingQueue<>(500),    // bounded queue
    r -> {
        Thread t = new Thread(r, "api-worker-" + counter.incrementAndGet());
        t.setDaemon(true);
        return t;
    },
    new ThreadPoolExecutor.CallerRunsPolicy()  // backpressure
);
```

**Q5: What is the proper way to shut down an `ExecutorService`?**

```text
A5: The recommended approach is the two-phase shutdown pattern from the Java documentation:

Phase 1 - Graceful shutdown:
  1. Call shutdown() to stop accepting new tasks.
  2. Call awaitTermination() with a reasonable timeout.
  3. This allows currently executing and queued tasks to complete.

Phase 2 - Forced shutdown (if graceful fails):
  1. Call shutdownNow() to interrupt running tasks and drain the queue.
  2. Call awaitTermination() again.
  3. Log a warning if tasks still haven't terminated.

Key differences:
- shutdown(): No new tasks accepted; running/queued tasks finish normally.
- shutdownNow(): Attempts to interrupt running tasks; returns list of un-started tasks.
- isShutdown(): Returns true after shutdown() is called.
- isTerminated(): Returns true only after ALL tasks have completed post-shutdown.

Since Java 19, ExecutorService implements AutoCloseable, so you can use
try-with-resources which calls shutdown() + awaitTermination() automatically.
```

```java
// Two-phase shutdown pattern
ExecutorService executor = Executors.newFixedThreadPool(4);
try {
    // ... submit tasks ...
} finally {
    executor.shutdown(); // Phase 1: stop accepting new tasks
    try {
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            executor.shutdownNow(); // Phase 2: interrupt running tasks
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                System.err.println("Executor did not terminate!");
            }
        }
    } catch (InterruptedException e) {
        executor.shutdownNow(); // Re-cancel if current thread interrupted
        Thread.currentThread().interrupt(); // Preserve interrupt status
    }
}

// Java 21+ - simpler with try-with-resources
try (var executor = Executors.newFixedThreadPool(4)) {
    executor.submit(() -> processRequest());
} // shutdown() + awaitTermination() called automatically
```

**Q6: How do virtual threads (Java 21) change the way we use ExecutorService?**

```text
A6: Virtual threads fundamentally change the concurrency model:

Before virtual threads (platform threads):
- Each thread maps 1:1 to an OS thread
- Creating thousands of threads is expensive (~1MB stack per thread)
- Thread pool sizing was a critical performance tuning knob
- I/O-bound tasks would block an expensive OS thread

With virtual threads:
- Virtual threads are managed by the JVM, not the OS
- Creating millions of virtual threads is feasible (a few hundred bytes each)
- The JVM multiplexes virtual threads onto a small pool of carrier (platform) threads
- When a virtual thread blocks on I/O, its carrier thread is freed for other work
- Thread pool sizing becomes less important for I/O-bound workloads

Key implications for ExecutorService:
- Executors.newVirtualThreadPerTaskExecutor() creates one virtual thread per task
- No thread pool needed -- each task gets its own lightweight thread
- Ideal for I/O-bound workloads (HTTP requests, database queries, file I/O)
- NOT beneficial for CPU-bound workloads (virtual threads share carrier threads)
- ThreadLocal should be avoided with virtual threads (use ScopedValue instead)

Virtual threads do NOT make your code faster -- they make it more scalable by
allowing many more concurrent tasks without exhausting OS resources.
```

```java
// Platform threads - limited by OS resources
try (var executor = Executors.newFixedThreadPool(200)) {
    for (int i = 0; i < 10_000; i++) {
        executor.submit(() -> {
            // Only 200 requests in-flight at a time
            return httpClient.send(request, BodyHandlers.ofString());
        });
    }
}

// Virtual threads - one per task, no pool sizing needed
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 10_000; i++) {
        executor.submit(() -> {
            // All 10,000 requests can be in-flight simultaneously
            return httpClient.send(request, BodyHandlers.ofString());
        });
    }
}
```

**Q7: What is CallerRunsPolicy and how does it provide backpressure?**

```text
A7: CallerRunsPolicy is a RejectedExecutionHandler that runs the rejected task
in the calling thread (the thread that called execute() or submit()).

How it provides backpressure:
1. When the pool and queue are full, the rejected task runs in the producer thread
2. While the producer thread is busy running the task, it cannot submit more tasks
3. This naturally slows down the producer, giving the pool time to drain
4. Once the task completes, the producer can submit new tasks again

This creates a self-regulating system:
- Under light load: Tasks go to the pool normally
- Under moderate load: Tasks queue up
- Under heavy load: The producer slows down by running tasks itself

Advantages:
- No tasks are lost (unlike DiscardPolicy)
- No exceptions thrown (unlike AbortPolicy)
- Natural backpressure without explicit rate limiting

Caveats:
- If the caller is a single-threaded event loop (e.g., Netty), blocking it
  can stall the entire application
- The caller thread changes, which affects ThreadLocal values
- Performance characteristics change under load (the caller's other work is delayed)
```

```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    2, 2, 0L, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(2),
    new ThreadPoolExecutor.CallerRunsPolicy()
);

// If pool (2 threads) and queue (2 slots) are all busy:
// This task runs in the MAIN thread, slowing down submission
executor.execute(() -> {
    System.out.println("Running in: " + Thread.currentThread().getName());
    // Could print "main" if pool is saturated!
});
```

## Code Examples

- Source: [ExecutorServiceDemo.java](src/main/java/com/github/msorkhpar/claudejavatutor/executors/ExecutorServiceDemo.java)
- Test: [ExecutorServiceDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/executors/ExecutorServiceDemoTest.java)
