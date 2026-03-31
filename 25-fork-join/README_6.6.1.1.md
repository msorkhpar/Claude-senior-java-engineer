# 6.6.1.1. Parallelizing Tasks with ForkJoinPool

## Concept Explanation

`ForkJoinPool` is a specialized `ExecutorService` implementation designed for fork/join-style parallelism. Unlike a
traditional thread pool (`ThreadPoolExecutor`), which uses a shared task queue, `ForkJoinPool` gives each worker thread
its own double-ended work queue (deque) and employs a **work-stealing** algorithm to keep all threads busy.

**Real-world analogy**: Consider a restaurant kitchen with multiple chefs. In a traditional kitchen (thread pool), orders
go to a single queue and chefs pick from it. In a fork/join kitchen, each chef has their own prep station with a stack of
sub-tasks. If a chef finishes their stack early, they walk over to a busy chef's station and take a task from the bottom
of that chef's pile. This ensures no chef is idle while work remains.

### How ForkJoinPool Works

1. A large task is submitted to the pool.
2. The task's `compute()` method decides whether the input is small enough to process sequentially.
3. If not, it splits into subtasks: **forks** one and **computes** the other inline.
4. The forked subtask is placed on the current thread's deque.
5. Other threads may **steal** it from the deque's opposite end.
6. Results are combined when `join()` is called.

### The Common Pool

Java provides a JVM-wide singleton: `ForkJoinPool.commonPool()`. It is used by:

- `parallelStream()`
- `CompletableFuture.supplyAsync()` (when no executor is specified)
- Any code that calls `ForkJoinPool.commonPool()`

Its default parallelism is `Runtime.getRuntime().availableProcessors() - 1`.

## Key Points to Remember

1. `ForkJoinPool.commonPool()` is a shared, JVM-wide pool -- contention is possible.
2. Create a custom `ForkJoinPool` when you need isolation or different parallelism.
3. Always shut down custom pools to release resources (`pool.shutdown()`).
4. Use `pool.invoke(task)` for synchronous execution, `pool.submit(task)` for async.
5. `invoke()` blocks until the task completes and returns the result.
6. `submit()` returns a `ForkJoinTask<V>` (which is a `Future<V>`).
7. The common pool's parallelism can be overridden with `-Djava.util.concurrent.ForkJoinPool.common.parallelism=N`.
8. `ForkJoinPool` is **not suitable for blocking I/O** -- use `ManagedBlocker` or a different pool.

## Relevant Java 21 Features

- **Virtual threads**: For I/O-bound concurrency, virtual threads (`Thread.ofVirtual()`) are preferred over
  `ForkJoinPool`. Fork/Join remains ideal for CPU-bound decomposition.
- **Structured Concurrency (preview)**: `StructuredTaskScope` provides fork/join semantics for task lifecycle
  management, but targets a different use case than data-parallel decomposition.
- The common pool parallelism default was refined in JDK 19+ for containers with CPU limits.

## Common Pitfalls and How to Avoid Them

1. **Saturating the common pool with blocking operations**

   ```java
   // Problem: blocking call inside parallel stream
   list.parallelStream().map(url -> {
       return httpClient.send(request, handler); // blocks a common pool thread
   }).collect(toList());
   ```

   **Fix**: Use a custom pool or virtual threads for I/O:

   ```java
   ForkJoinPool customPool = new ForkJoinPool(16);
   customPool.submit(() ->
       list.parallelStream().map(url -> httpClient.send(request, handler)).collect(toList())
   ).join();
   ```

2. **Forgetting to shut down custom pools**

   ```java
   ForkJoinPool pool = new ForkJoinPool(4);
   long result = pool.invoke(task);
   // pool leaks threads!
   ```

   **Fix**: Use try-finally:

   ```java
   ForkJoinPool pool = new ForkJoinPool(4);
   try {
       long result = pool.invoke(task);
   } finally {
       pool.shutdown();
   }
   ```

3. **Assuming parallelism equals thread count**: The pool size can grow beyond parallelism when `ManagedBlocker` is used.

4. **Using ForkJoinPool for inherently sequential problems**: Not all problems benefit from parallelism. The overhead of
   task creation and context switching may exceed the sequential cost for small inputs.

## Best Practices and Optimization Techniques

1. Use the **common pool** for general-purpose parallel work unless you need isolation.
2. Always **shut down** custom pools in a `finally` block.
3. Set parallelism based on **CPU cores** for compute-bound work; higher for mixed workloads.
4. Use `invoke()` when you need the result immediately; `submit()` + `join()` for deferred results.
5. Profile to verify that parallelism provides a measurable speedup over sequential execution.
6. Use `-Djava.util.concurrent.ForkJoinPool.common.parallelism` to tune the common pool globally.

## Edge Cases and Their Handling

1. **Null input arrays**: Return a default value (e.g., 0) without submitting to the pool.
2. **Empty input arrays**: Same as null -- handle before pool submission.
3. **Single-element input**: Compute directly; no benefit from forking.
4. **Parallelism = 1**: The pool operates with a single worker thread plus the submitting thread.
5. **Thread interruption**: Handle `InterruptedException` properly when using `awaitTermination()`.

## Interview-specific Insights

- Be ready to explain work-stealing with a whiteboard diagram showing per-thread deques.
- Know the difference between `invoke()`, `submit()`, and `execute()`.
- Understand why the common pool is shared with parallel streams and the implications.
- Be able to articulate when Fork/Join is preferable to `ExecutorService` or virtual threads.

## Interview Q&A Section

**Q1: How do you create and use a ForkJoinPool?**

```text
A1: There are two main approaches:

1. Use the common pool (recommended for most cases):
   ForkJoinPool.commonPool().invoke(task);

2. Create a custom pool:
   ForkJoinPool pool = new ForkJoinPool(parallelism);
   try {
       result = pool.invoke(task);
   } finally {
       pool.shutdown();
   }

The common pool is shared JVM-wide and used by parallel streams.
A custom pool provides isolation and configurable parallelism.
```

```java
// Common pool
long sum = ForkJoinPool.commonPool().invoke(new SumTask(array, 0, array.length));

// Custom pool with 4 threads
ForkJoinPool customPool = new ForkJoinPool(4);
try {
    long sum = customPool.invoke(new SumTask(array, 0, array.length));
} finally {
    customPool.shutdown();
}
```

**Q2: What is the difference between invoke(), submit(), and execute() in ForkJoinPool?**

```text
A2: All three submit tasks to the pool, but differ in blocking behavior and return type:

- invoke(task): Blocks until the task completes, returns the result directly.
  Equivalent to submit(task).join(). Best for synchronous usage.

- submit(task): Returns a ForkJoinTask<V> (a Future) immediately without blocking.
  Call .join() or .get() later to retrieve the result. Best for asynchronous usage.

- execute(task): Returns void immediately, like submit() but with no way to get
  the result. Best for fire-and-forget RecursiveAction tasks.

All three methods will throw if the task encounters an exception, but invoke()
throws immediately while submit()/execute() defer the exception to join()/get().
```

```java
ForkJoinPool pool = ForkJoinPool.commonPool();
SumTask task = new SumTask(array, 0, array.length);

// invoke -- blocks and returns result
long result = pool.invoke(task);

// submit -- async, get result later
ForkJoinTask<Long> future = pool.submit(new SumTask(array, 0, array.length));
long result2 = future.join(); // blocks here

// execute -- fire and forget
pool.execute(new IncrementAction(array, 1));
```

**Q3: How do you run a parallel stream in a custom ForkJoinPool?**

```text
A3: By default, parallel streams use the common ForkJoinPool. To use a custom pool,
submit the stream operation as a task to your custom pool:

  ForkJoinPool customPool = new ForkJoinPool(parallelism);
  customPool.submit(() -> myStream.parallel().operation()).join();

This is useful when:
- You want to isolate parallel stream work from the common pool
- You need different parallelism than the common pool provides
- You want to avoid starving other parallel operations

Caveat: This technique is a practical pattern but not officially guaranteed by the
specification. It works reliably in all current JDK implementations.
```

```java
ForkJoinPool customPool = new ForkJoinPool(8);
try {
    long sum = customPool.submit(() ->
        IntStream.range(0, array.length)
            .parallel()
            .mapToLong(i -> array[i])
            .sum()
    ).join();
} finally {
    customPool.shutdown();
}
```

**Q4: What is ManagedBlocker and when should you use it?**

```text
A4: ManagedBlocker is an interface in ForkJoinPool that allows blocking operations
to cooperate with the pool's thread management. When a thread is about to block,
the pool can compensate by creating additional threads to maintain parallelism.

Implement ManagedBlocker when you must perform a blocking operation inside a
ForkJoinPool task (e.g., waiting on a lock, I/O, or a condition). Without it,
the blocked thread reduces effective parallelism.

Methods to implement:
- block(): Performs the blocking operation
- isReleasable(): Returns true if blocking is no longer necessary

Usage: ForkJoinPool.managedBlock(blocker);

However, for I/O-bound work, virtual threads are a better choice in Java 21+.
```

```java
class MyBlocker implements ForkJoinPool.ManagedBlocker {
    private volatile boolean done = false;
    private int result;

    @Override
    public boolean block() throws InterruptedException {
        // Perform blocking operation
        result = someBlockingCall();
        done = true;
        return true;
    }

    @Override
    public boolean isReleasable() {
        return done;
    }
}

// Usage inside a ForkJoinTask
MyBlocker blocker = new MyBlocker();
ForkJoinPool.managedBlock(blocker);
```

**Q5: What are the key configuration parameters of ForkJoinPool?**

```text
A5: ForkJoinPool has several configuration options:

1. parallelism: Number of worker threads (default: availableProcessors() - 1
   for the common pool). This controls the maximum number of threads actively
   executing tasks.

2. ForkJoinPool.ForkJoinWorkerThreadFactory: Factory for creating worker threads.
   Allows custom thread naming, priority, or daemon status.

3. UncaughtExceptionHandler: Handler for exceptions thrown by worker threads
   that are not caught by task code.

4. asyncMode: If true, tasks are processed in FIFO order instead of LIFO.
   Useful for event-style tasks that are never joined. Default is false.

Common pool can be configured via system properties:
- java.util.concurrent.ForkJoinPool.common.parallelism
- java.util.concurrent.ForkJoinPool.common.threadFactory
- java.util.concurrent.ForkJoinPool.common.exceptionHandler
```

```java
// Custom ForkJoinPool with full configuration
ForkJoinPool pool = new ForkJoinPool(
    4,                                          // parallelism
    ForkJoinPool.defaultForkJoinWorkerThreadFactory, // thread factory
    null,                                       // exception handler
    false                                       // asyncMode (LIFO)
);

// Query pool statistics
System.out.println("Parallelism: " + pool.getParallelism());
System.out.println("Pool size: " + pool.getPoolSize());
System.out.println("Active threads: " + pool.getActiveThreadCount());
System.out.println("Steal count: " + pool.getStealCount());
System.out.println("Queued tasks: " + pool.getQueuedTaskCount());
```

## Code Examples

- Source: [ForkJoinPoolBasics.java](src/main/java/com/github/msorkhpar/claudejavatutor/forkjoin/ForkJoinPoolBasics.java)
- Test: [ForkJoinPoolBasicsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/forkjoin/ForkJoinPoolBasicsTest.java)
