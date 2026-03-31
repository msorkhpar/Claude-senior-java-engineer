# 6.7.1. Lightweight Threads for High Concurrency

## Concept Explanation

Virtual threads, introduced as a preview feature in Java 19 and finalized in Java 21 (JEP 444), are lightweight threads
that dramatically reduce the effort of writing, maintaining, and observing high-throughput concurrent applications. They
are the flagship deliverable of **Project Loom**, a long-running OpenJDK initiative aimed at making concurrent
programming in Java simpler and more scalable.

**Real-world analogy**: Think of a restaurant kitchen. Platform threads are like expensive, full-time professional chefs
— you can only afford a limited number of them (typically matching the number of stoves/burners). Virtual threads are
like recipe cards that can be handed to any available chef. When a chef finishes one recipe or is waiting for something
to cook (blocking I/O), they simply pick up the next recipe card. You can have millions of recipe cards without needing
millions of chefs. The chefs (carrier threads) are reused efficiently, and the recipe cards (virtual threads) are
extremely cheap to create.

Before Project Loom, Java developers faced a fundamental tension: the `java.lang.Thread` API was tied to operating
system threads, which are expensive resources. Each platform thread consumes roughly 1 MB of stack memory and requires
OS-level scheduling. This meant applications were limited to thousands of concurrent threads at most, even though modern
servers often need to handle hundreds of thousands or millions of concurrent connections (e.g., web servers, microservices,
database connection handlers).

Virtual threads solve this by decoupling the Java `Thread` abstraction from the underlying OS thread. A virtual thread
is **mounted** onto a platform thread (called a **carrier thread**) only while it is executing CPU work. When a virtual
thread performs a blocking operation (such as I/O, `Thread.sleep()`, or waiting on a lock), it is **unmounted** from the
carrier thread, freeing the carrier to run other virtual threads. This allows millions of virtual threads to coexist,
each representing a unit of concurrent work, without exhausting OS resources.

### 6.7.1.1. Overview of Project Loom and Virtual Threads

Project Loom's core goal is to make the thread-per-request programming model practical at scale. Instead of forcing
developers to use complex asynchronous frameworks (reactive programming, callbacks, CompletableFuture chains), virtual
threads let you write straightforward blocking code that scales just as well as asynchronous code.

Key concepts:

- **Virtual thread**: A lightweight thread managed by the JVM, not the OS. It has its own call stack and can be
  suspended and resumed cheaply.
- **Carrier thread**: A platform (OS) thread that actually executes virtual thread code. The JVM maintains a pool of
  carrier threads (typically backed by `ForkJoinPool.commonPool()`).
- **Mounting/Unmounting**: The process of attaching/detaching a virtual thread to/from a carrier thread. Unmounting
  happens automatically at blocking points.
- **Continuation**: The internal mechanism that allows a virtual thread's stack to be saved and restored. This is what
  makes virtual threads cheap — their stack starts small and grows as needed.

### 6.7.1.2. Creating and Managing Virtual Threads

Java 21 provides several APIs for creating virtual threads:

1. **`Thread.startVirtualThread(Runnable)`** — The simplest way to create and start a virtual thread.
2. **`Thread.ofVirtual().start(Runnable)`** — Builder pattern with more configuration options (naming, etc.).
3. **`Thread.ofVirtual().unstarted(Runnable)`** — Creates a virtual thread without starting it.
4. **`Thread.ofVirtual().factory()`** — Creates a `ThreadFactory` for use with executors.
5. **`Executors.newVirtualThreadPerTaskExecutor()`** — An `ExecutorService` that creates a new virtual thread for
   every submitted task.

## Key Points to Remember

1. **Virtual threads are cheap to create** — Creating a virtual thread is roughly 1000x cheaper than creating a platform
   thread. Their initial stack is only a few hundred bytes, growing on demand.
2. **Virtual threads are best for I/O-bound work** — They shine when tasks spend most of their time waiting (network
   calls, database queries, file I/O). For CPU-bound work, platform threads in a fixed pool remain the better choice.
3. **Virtual threads are daemon threads** — They are always daemon threads and cannot be set to non-daemon. They have
   fixed `NORM_PRIORITY` and `setPriority()` is ignored.
4. **Virtual threads support the full Thread API** — They work with `Thread.sleep()`, `join()`, `interrupt()`,
   thread-local variables, and all standard synchronization primitives.
5. **Do not pool virtual threads** — Unlike platform threads, virtual threads should not be pooled. Create a new one
   for each task. Pooling them defeats their purpose and adds unnecessary complexity.
6. **`Executors.newVirtualThreadPerTaskExecutor()`** is the recommended entry point for most applications.
7. **Thread-local variables work** but can be wasteful if many virtual threads each hold heavy thread-local state.
   Consider `ScopedValue` (preview in Java 21) as a more efficient alternative.
8. **Virtual threads are not faster** — They don't make individual tasks run faster. They improve **throughput** by
   allowing more concurrent tasks to be in flight simultaneously.

## Relevant Java 21 Features

- **JEP 444: Virtual Threads** — Finalized in Java 21 as a standard feature after previewing in Java 19 (JEP 425) and
  Java 20 (JEP 436).
- **JEP 453: Structured Concurrency (Preview)** — Complements virtual threads by treating groups of related tasks as a
  unit, simplifying error handling and cancellation.
- **JEP 446: Scoped Values (Preview)** — Provides an efficient alternative to thread-local variables, particularly
  beneficial with virtual threads where millions of threads may exist.
- **Thread.Builder API** — New builder-style API (`Thread.ofVirtual()`, `Thread.ofPlatform()`) for creating threads
  with a fluent interface.
- **`Thread.isVirtual()`** — New method to check if a thread is virtual.

### Evolution Across Java Versions

| Version  | Feature                        | Status         |
|----------|--------------------------------|----------------|
| Java 19  | Virtual Threads (JEP 425)      | First Preview  |
| Java 20  | Virtual Threads (JEP 436)      | Second Preview |
| Java 21  | Virtual Threads (JEP 444)      | **Final**      |
| Java 21  | Structured Concurrency         | Preview        |
| Java 21  | Scoped Values                  | Preview        |

## Common Pitfalls and How to Avoid Them

1. **Pooling virtual threads**: Developers accustomed to platform thread pools may try to pool virtual threads. This is
   unnecessary and counterproductive.

   ```java
   // WRONG: Don't pool virtual threads
   ExecutorService pool = Executors.newFixedThreadPool(100,
       Thread.ofVirtual().factory());

   // CORRECT: Use virtual-thread-per-task executor
   ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
   ```

2. **Using virtual threads for CPU-bound tasks**: Virtual threads provide no benefit for CPU-intensive work because
   there's no blocking point where they can unmount.

   ```java
   // WRONG: CPU-bound work in virtual threads
   try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
       for (int i = 0; i < 1000; i++) {
           executor.submit(() -> computeIntensiveTask()); // No benefit
       }
   }

   // CORRECT: Use platform thread pool for CPU-bound work
   try (var executor = Executors.newFixedThreadPool(
           Runtime.getRuntime().availableProcessors())) {
       for (int i = 0; i < 1000; i++) {
           executor.submit(() -> computeIntensiveTask());
       }
   }
   ```

3. **Heavy thread-local usage**: If each virtual thread allocates expensive thread-local state, millions of virtual
   threads can consume significant memory.

   ```java
   // PROBLEMATIC: Each of 1 million virtual threads gets its own heavy ThreadLocal
   ThreadLocal<byte[]> buffer = ThreadLocal.withInitial(() -> new byte[1024 * 1024]);

   // BETTER: Use ScopedValue (preview) or pass state explicitly
   ```

4. **Forgetting that virtual threads are daemon threads**: If the main thread exits, virtual threads are terminated.

   ```java
   // WRONG: Main thread may exit before virtual threads finish
   for (int i = 0; i < 100; i++) {
       Thread.startVirtualThread(() -> doWork());
   }
   // Program may exit immediately!

   // CORRECT: Wait for completion
   try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
       for (int i = 0; i < 100; i++) {
           executor.submit(() -> doWork());
       }
   } // AutoCloseable: waits for all tasks to complete
   ```

5. **Not handling InterruptedException**: Virtual threads support cooperative cancellation via interruption. Swallowing
   `InterruptedException` without re-interrupting the thread breaks cancellation.

   ```java
   // WRONG: Swallowing interrupt
   try {
       Thread.sleep(1000);
   } catch (InterruptedException e) {
       // Silently ignored - BAD
   }

   // CORRECT: Restore interrupt status
   try {
       Thread.sleep(1000);
   } catch (InterruptedException e) {
       Thread.currentThread().interrupt();
       throw new RuntimeException("Task was interrupted", e);
   }
   ```

## Best Practices and Optimization Techniques

1. **Prefer `Executors.newVirtualThreadPerTaskExecutor()`** over raw `Thread.startVirtualThread()` for task management.
   The executor handles lifecycle and provides a clean `try-with-resources` pattern.

2. **Write blocking code naturally** — The whole point of virtual threads is to write simple, synchronous code that
   scales. Don't add async complexity.

3. **Use `ReentrantLock` instead of `synchronized`** when blocking inside critical sections to avoid carrier thread
   pinning (covered in detail in section 6.7.2).

4. **Name your virtual threads** for debugging and monitoring:
   ```java
   Thread.ofVirtual().name("http-handler-", 0).factory();
   ```

5. **Use structured concurrency** (when stable) to manage groups of virtual threads as a single unit of work.

6. **Monitor with JFR (Java Flight Recorder)** — Virtual thread events are integrated with JFR for observability.

7. **Limit concurrent access to shared resources** using semaphores rather than reducing thread count:
   ```java
   Semaphore dbPool = new Semaphore(20); // Limit to 20 concurrent DB connections
   try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
       for (var request : requests) {
           executor.submit(() -> {
               dbPool.acquire();
               try {
                   queryDatabase(request);
               } finally {
                   dbPool.release();
               }
           });
       }
   }
   ```

## Edge Cases and Their Handling

1. **Interrupting a sleeping virtual thread**: Calling `interrupt()` on a virtual thread that is in `Thread.sleep()`
   causes it to throw `InterruptedException` immediately, just like platform threads.

2. **Joining a virtual thread that never finishes**: Use `Thread.join(timeout)` with a timeout to avoid indefinite
   blocking.

3. **Thread.currentThread() inside a virtual thread**: Returns the virtual thread instance, not the carrier thread.

4. **ThreadGroup for virtual threads**: Virtual threads belong to a fixed "VirtualThreads" thread group. This group
   cannot be changed and does not support the deprecated `ThreadGroup.enumerate()` method meaningfully.

5. **Stack overflow in virtual threads**: Virtual threads have growable stacks, so `StackOverflowError` is still
   possible but the threshold is different from platform threads.

6. **Large number of thread-local variables**: With millions of virtual threads, careless use of thread-locals can lead
   to OutOfMemoryError. Clean up thread-locals or use `ScopedValue` instead.

## Interview-specific Insights

Interviewers frequently ask about virtual threads to gauge a candidate's understanding of modern Java concurrency:

- **"What problem do virtual threads solve?"** — They solve the mismatch between the thread-per-request model and OS
  thread limitations. Know that this is about throughput, not latency.
- **"When should you NOT use virtual threads?"** — CPU-bound tasks, or when you need to control the exact number of
  OS-level threads.
- **"How are virtual threads different from reactive/async frameworks?"** — Virtual threads let you write blocking code
  that scales, avoiding callback hell and complex operator chains.
- **"What is a carrier thread?"** — Be prepared to explain the mounting/unmounting mechanism.
- **"Are virtual threads a replacement for platform threads?"** — No, they complement them. Platform threads are still
  needed for CPU-bound work and certain low-level scenarios.

Tricky questions to expect:
- "Can you set a virtual thread to non-daemon?" (No, `setDaemon(false)` throws `IllegalArgumentException`)
- "Does `setPriority()` work on virtual threads?" (No, it is silently ignored; priority is always `NORM_PRIORITY`)
- "What happens to thread-locals in virtual threads?" (They work but can be wasteful at scale)

## Interview Q&A Section

**Q1: What are virtual threads and why were they introduced in Java 21?**

```text
A1: Virtual threads are lightweight threads managed by the JVM rather than the operating system.
They were introduced to solve a fundamental scalability problem in Java: the thread-per-request
programming model is simple and natural, but OS threads are expensive resources. Each OS thread
consumes about 1 MB of stack memory and involves kernel-level scheduling overhead, limiting
applications to a few thousand concurrent threads.

Virtual threads decouple the Java Thread abstraction from OS threads. A virtual thread is
mounted onto a carrier (platform) thread only while it executes CPU work. When it blocks
(I/O, sleep, lock contention), it unmounts, freeing the carrier for other virtual threads.
This means you can have millions of concurrent virtual threads without proportional OS resource
consumption.

The key benefit is throughput improvement for I/O-bound workloads (web servers, microservices,
database-intensive applications) without requiring developers to adopt complex asynchronous
programming models like reactive streams or callback chains.
```

```java
// Before virtual threads: limited by thread pool size
ExecutorService pool = Executors.newFixedThreadPool(200);
for (var request : requests) {
    pool.submit(() -> handleRequest(request)); // Max 200 concurrent
}

// With virtual threads: unlimited concurrency
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (var request : requests) {
        executor.submit(() -> handleRequest(request)); // Millions possible
    }
}
```

**Q2: How do you create virtual threads in Java 21?**

```text
A2: Java 21 provides several ways to create virtual threads:

1. Thread.startVirtualThread(Runnable) - Simplest; creates and starts immediately
2. Thread.ofVirtual().start(Runnable) - Builder pattern; allows setting name, etc.
3. Thread.ofVirtual().unstarted(Runnable) - Creates without starting
4. Thread.ofVirtual().factory() - Creates a ThreadFactory for use with executors
5. Executors.newVirtualThreadPerTaskExecutor() - Best for most applications

The recommended approach for production code is using
Executors.newVirtualThreadPerTaskExecutor() because:
- It provides lifecycle management via ExecutorService
- It supports try-with-resources for clean shutdown
- It integrates with existing Callable/Future patterns
- It creates one virtual thread per submitted task
```

```java
// Method 1: Simplest
Thread vt1 = Thread.startVirtualThread(() -> System.out.println("Hello"));

// Method 2: Builder with name
Thread vt2 = Thread.ofVirtual()
        .name("my-virtual-thread")
        .start(() -> System.out.println("Named thread"));

// Method 3: Unstarted
Thread vt3 = Thread.ofVirtual().unstarted(() -> System.out.println("Not yet"));
vt3.start(); // Start when ready

// Method 4: Factory for custom executors
ThreadFactory factory = Thread.ofVirtual().name("worker-", 0).factory();
ExecutorService executor = Executors.newThreadPerTaskExecutor(factory);

// Method 5: Recommended for most use cases
try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
    Future<String> future = exec.submit(() -> {
        Thread.sleep(100);
        return "result";
    });
    System.out.println(future.get());
}
```

**Q3: What is the difference between `Thread.startVirtualThread()` and `Executors.newVirtualThreadPerTaskExecutor()`?**

```text
A3: Both create virtual threads, but they serve different purposes:

Thread.startVirtualThread(Runnable):
- Creates and immediately starts a single virtual thread
- Returns the Thread object directly
- No built-in lifecycle management
- You must manually join() or track the thread
- Best for ad-hoc, fire-and-forget tasks

Executors.newVirtualThreadPerTaskExecutor():
- Creates an ExecutorService that spawns a new virtual thread per task
- Returns an ExecutorService with full lifecycle support
- Supports submit(Callable), submit(Runnable), and invokeAll()
- Implements AutoCloseable (close() waits for all tasks)
- Best for structured task management in production code

Key insight: The executor approach is preferred because it integrates with
try-with-resources, ensuring all tasks complete before proceeding. The raw
Thread approach requires manual coordination with join().
```

```java
// Raw approach: must manage threads manually
List<Thread> threads = new ArrayList<>();
for (int i = 0; i < 1000; i++) {
    threads.add(Thread.startVirtualThread(() -> doWork()));
}
for (Thread t : threads) {
    t.join(); // Must join each thread
}

// Executor approach: clean lifecycle management
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    List<Future<Result>> futures = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
        futures.add(executor.submit(() -> doWork()));
    }
    for (Future<Result> f : futures) {
        Result result = f.get(); // Get results, handle exceptions
    }
} // close() blocks until all tasks are done
```

**Q4: Should you pool virtual threads like you pool platform threads?**

```text
A4: No, you should never pool virtual threads. This is one of the most common
misconceptions when developers first encounter virtual threads.

Why pooling platform threads makes sense:
- Platform threads are expensive to create (~1 MB stack, OS-level allocation)
- Reusing them amortizes creation cost
- Pool size limits resource consumption

Why pooling virtual threads is counterproductive:
- Virtual threads are extremely cheap to create (a few hundred bytes initially)
- Their purpose is one-per-task concurrency without resource exhaustion
- Putting them in a fixed pool reintroduces the exact bottleneck they eliminate
- A fixed pool of virtual threads behaves identically to a fixed pool of
  platform threads — you lose all benefits

If you need to limit concurrency (e.g., to protect a database connection pool),
use a Semaphore to throttle access to the resource, not a thread pool to
throttle the threads.
```

```java
// WRONG: Pooling virtual threads (defeats the purpose)
ExecutorService pool = Executors.newFixedThreadPool(50,
        Thread.ofVirtual().factory());

// CORRECT: Virtual thread per task
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

// To limit concurrent resource access, use Semaphore:
Semaphore connectionLimit = new Semaphore(50);
try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
    for (var request : requests) {
        exec.submit(() -> {
            connectionLimit.acquire();
            try {
                return queryDatabase(request);
            } finally {
                connectionLimit.release();
            }
        });
    }
}
```

**Q5: How does interruption work with virtual threads?**

```text
A5: Virtual threads fully support the cooperative interruption mechanism, just
like platform threads. The key difference is that interrupting a virtual thread
is even more important because virtual threads are designed for high-concurrency
scenarios where timely cancellation is critical.

How it works:
1. Call thread.interrupt() to set the interrupt flag
2. If the thread is in a blocking operation (sleep, I/O, wait), it throws
   InterruptedException immediately
3. If the thread is running, it must check Thread.interrupted() or
   isInterrupted() periodically

Best practices:
- Always handle InterruptedException properly — either propagate it or
  re-interrupt the current thread
- Design long-running virtual thread tasks with interrupt checks
- Use structured concurrency (when stable) for coordinated cancellation of
  task groups

Virtual threads respond to interrupt at all standard blocking points:
Thread.sleep(), Object.wait(), BlockingQueue.take(), Lock.lockInterruptibly(),
I/O operations via java.nio channels, etc.
```

```java
// Creating a virtual thread that respects interruption
Thread worker = Thread.startVirtualThread(() -> {
    while (!Thread.currentThread().isInterrupted()) {
        try {
            // Simulate work with blocking I/O
            String data = fetchFromNetwork();
            process(data);
        } catch (InterruptedException e) {
            // Restore interrupt status and exit gracefully
            Thread.currentThread().interrupt();
            System.out.println("Worker interrupted, cleaning up...");
            break;
        }
    }
});

// Later, cancel the work
Thread.sleep(5000);
worker.interrupt(); // Signals the virtual thread to stop
worker.join(1000);  // Wait for it to finish (with timeout)
```

**Q6: What is the relationship between virtual threads and the ForkJoinPool?**

```text
A6: Virtual threads use a ForkJoinPool as their scheduler (carrier thread pool).
By default, the JVM creates a dedicated ForkJoinPool for scheduling virtual
threads, separate from the common pool used by parallel streams.

Key details:
- The default carrier pool size equals the number of available processors
  (Runtime.getRuntime().availableProcessors())
- This can be configured via: -Djdk.virtualThreadScheduler.parallelism=N
- The maximum pool size can be set via: -Djdk.virtualThreadScheduler.maxPoolSize=N
- Virtual threads are automatically scheduled onto carrier threads from this pool

The carrier pool is an implementation detail that developers generally don't need
to worry about. The JVM handles mounting and unmounting virtual threads onto
carrier threads transparently.

When a virtual thread blocks (I/O, sleep, lock wait), it unmounts from its
carrier thread, allowing the carrier to pick up another runnable virtual thread.
When the blocking operation completes, the virtual thread is scheduled to resume
on any available carrier thread (not necessarily the same one).
```

```java
// Virtual threads use a ForkJoinPool as their carrier pool
// The parallelism defaults to available processors
// You can observe this through thread names:

try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 100; i++) {
        executor.submit(() -> {
            // The carrier thread name is visible in the virtual thread's toString()
            System.out.println(Thread.currentThread());
            // Output like: VirtualThread[#23]/runnable@ForkJoinPool-1-worker-3
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}
```

**Q7: Can you use thread-local variables with virtual threads?**

```text
A7: Yes, thread-local variables work with virtual threads, but they should be
used carefully. Each virtual thread gets its own copy of a ThreadLocal value,
just like platform threads.

The concern is scale: if you have 1 million virtual threads and each one
initializes a ThreadLocal with a 1 KB buffer, that's 1 GB of memory just for
thread-local storage. With platform threads, you might have had 200 threads,
consuming only 200 KB.

Java 21 introduces ScopedValue (preview) as a more efficient alternative:
- ScopedValues are immutable and automatically cleaned up
- They are inherited by child threads efficiently
- No need for explicit remove() calls
- Better memory efficiency with virtual threads

Best practices for thread-locals with virtual threads:
1. Avoid large or expensive ThreadLocal values
2. Always call ThreadLocal.remove() when done
3. Consider using ScopedValue for new code
4. Audit existing thread-local usage before migrating to virtual threads
```

```java
// ThreadLocal works but be mindful of memory at scale
ThreadLocal<SimpleDateFormat> formatter = ThreadLocal.withInitial(
        () -> new SimpleDateFormat("yyyy-MM-dd"));

try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 1_000_000; i++) {
        executor.submit(() -> {
            try {
                String date = formatter.get().format(new java.util.Date());
                process(date);
            } finally {
                formatter.remove(); // Important! Clean up to avoid leaks
            }
        });
    }
}

// Better approach with ScopedValue (Preview in Java 21)
// static final ScopedValue<String> USER = ScopedValue.newInstance();
// ScopedValue.where(USER, "alice").run(() -> {
//     System.out.println(USER.get()); // "alice"
// });
```

## Code Examples

- Test: [VirtualThreadBasicsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/virtualthreads/VirtualThreadBasicsTest.java)
- Source: [VirtualThreadBasics.java](src/main/java/com/github/msorkhpar/claudejavatutor/virtualthreads/VirtualThreadBasics.java)
