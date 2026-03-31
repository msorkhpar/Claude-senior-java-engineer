# 6.7.2. Differences Between Virtual Threads and Platform Threads

## Concept Explanation

Understanding the differences between virtual threads and platform threads is essential for choosing the right
concurrency strategy in Java 21+. While both implement the `java.lang.Thread` API, they differ fundamentally in how
they are scheduled, how they consume resources, and how they behave under blocking and synchronization.

**Real-world analogy**: Platform threads are like dedicated employees sitting at individual desks — each employee
occupies a desk (OS thread/stack) full-time, whether they are actively working or waiting for a phone call. Virtual
threads are like tasks on a shared task board — workers (carrier threads) pick up tasks, work on them until they hit a
waiting point (blocking I/O), then put the task back on the board and pick up another one. You can have millions of tasks
on the board without needing millions of desks.

### 6.7.2.1. Scalability and Resource Consumption

The most dramatic difference between virtual and platform threads is their resource footprint:

| Aspect                  | Platform Thread          | Virtual Thread                    |
|-------------------------|--------------------------|-----------------------------------|
| Stack memory            | ~1 MB (fixed, pre-allocated) | Starts at ~200-300 bytes, grows on demand |
| OS thread mapping       | 1:1 (one OS thread each) | M:N (many virtual to few carrier) |
| Creation cost           | Expensive (kernel allocation) | Cheap (JVM-managed object)       |
| Max concurrent threads  | Thousands (OS-limited)   | Millions (JVM-limited)            |
| Scheduling              | OS scheduler             | JVM scheduler (ForkJoinPool)      |
| Context switch          | Expensive (kernel mode)  | Cheap (user-mode stack swap)      |

For I/O-bound workloads, this difference is transformative. A traditional web server with a fixed thread pool of 200
platform threads can handle at most 200 concurrent requests. With virtual threads, the same server can handle hundreds of
thousands of concurrent requests because each request gets its own virtual thread, and the JVM efficiently multiplexes
them onto a small number of carrier threads.

### 6.7.2.2. Blocking and Synchronization Behavior

When a platform thread blocks (e.g., on `Thread.sleep()`, network I/O, `Lock.lock()`), the underlying OS thread is
blocked and cannot be used for anything else. This wastes a valuable resource.

When a virtual thread blocks, it **unmounts** from its carrier thread. The carrier thread is freed to execute other
virtual threads. When the blocking operation completes, the virtual thread is **remounted** onto an available carrier
(possibly a different one).

However, there is an important exception: **pinning**. A virtual thread becomes **pinned** to its carrier thread when
it enters a `synchronized` block or method. If the virtual thread blocks while inside a `synchronized` section, the
carrier thread is also blocked, reducing the pool of available carriers. This can cause throughput degradation if many
virtual threads contend on synchronized blocks with blocking operations inside.

The solution is to replace `synchronized` with `ReentrantLock` in code paths where virtual threads may block while
holding the lock.

### 6.7.2.3. Compatibility with Existing Code and Libraries

Virtual threads are designed to be a drop-in replacement for platform threads in most scenarios:

- **Full Thread API compatibility**: `sleep()`, `join()`, `interrupt()`, `isAlive()`, thread-local variables all work.
- **Executor framework**: Virtual threads work with `ExecutorService`, `Future`, `Callable`, `CompletableFuture`.
- **Synchronization**: `CountDownLatch`, `CyclicBarrier`, `Semaphore`, `BlockingQueue`, `ReentrantLock` all work.
- **I/O**: `java.nio` channels, `java.net.Socket`, and `java.io` streams are virtual-thread-friendly in Java 21.

Known compatibility considerations:
- **`synchronized` keyword**: Causes pinning (see above). Replace with `ReentrantLock` where needed.
- **Native methods/JNI**: Virtual threads are pinned during native method execution.
- **`Thread.setPriority()`**: Silently ignored on virtual threads.
- **`Thread.setDaemon(false)`**: Throws `IllegalArgumentException` on virtual threads.
- **`ThreadGroup`**: Virtual threads always belong to the "VirtualThreads" thread group.
- **Thread-local variables**: Work but can be memory-wasteful at scale.

## Key Points to Remember

1. **Virtual threads unmount on blocking; platform threads do not** — This is the key architectural difference that
   enables scalability.
2. **Pinning occurs with `synchronized`** — Use `ReentrantLock` instead to avoid pinning virtual threads.
3. **Virtual threads are always daemon threads** — They cannot be set to non-daemon.
4. **`setPriority()` is silently ignored** — Virtual threads always run at `NORM_PRIORITY`.
5. **Do not pool virtual threads** — They are designed to be created per-task, not reused.
6. **Platform threads are still preferred for CPU-bound work** — Virtual threads provide no benefit when threads
   rarely block.
7. **Virtual threads work with most existing Java APIs** — Including `ExecutorService`, `Future`,
   `CompletableFuture`, `CountDownLatch`, `BlockingQueue`, etc.
8. **Memory footprint differs dramatically** — Platform threads: ~1 MB each; virtual threads: a few hundred bytes
   initially, growing as needed.
9. **Context switching is cheaper** — Virtual thread context switches happen in user mode (JVM), not kernel mode (OS).
10. **Native methods/JNI cause pinning** — Virtual threads cannot unmount during native method execution.

## Relevant Java 21 Features

- **JEP 444: Virtual Threads** — The implementation is based on `Continuation` (internal API) and uses a dedicated
  `ForkJoinPool` for scheduling.
- **Improved `synchronized` behavior** — Java 24 (JEP 491) removes pinning for `synchronized` blocks, but in Java 21,
  pinning is still a concern.
- **JFR events for virtual threads** — `jdk.VirtualThreadStart`, `jdk.VirtualThreadEnd`, `jdk.VirtualThreadPinned`
  events for monitoring.
- **`-Djdk.tracePinnedThreads=full|short`** — JVM flag to detect pinning at runtime (prints stack trace when a virtual
  thread is pinned).

### Monitoring Pinning

Java 21 provides a system property to help detect pinning:

```bash
# Print stack trace when a virtual thread is pinned
java -Djdk.tracePinnedThreads=full MyApp

# Print shorter output
java -Djdk.tracePinnedThreads=short MyApp
```

## Common Pitfalls and How to Avoid Them

1. **Using `synchronized` blocks with blocking operations inside** — This pins the virtual thread to its carrier,
   reducing concurrency.

   ```java
   // PROBLEMATIC: Pinning - carrier thread is blocked
   synchronized (lock) {
       Thread.sleep(1000); // Virtual thread is pinned during sleep
       result = fetchFromDatabase();
   }

   // BETTER: Use ReentrantLock - no pinning
   ReentrantLock lock = new ReentrantLock();
   lock.lock();
   try {
       Thread.sleep(1000); // Virtual thread unmounts normally
       result = fetchFromDatabase();
   } finally {
       lock.unlock();
   }
   ```

2. **Assuming virtual threads make code faster** — Virtual threads improve throughput (more concurrent tasks), not
   latency (individual task speed).

   ```java
   // MISCONCEPTION: This single task won't run faster on a virtual thread
   Thread.startVirtualThread(() -> {
       // CPU-bound computation takes the same time on virtual or platform thread
       double result = computePI(1_000_000);
   });

   // CORRECT USE: Many I/O-bound tasks benefit from virtual threads
   try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
       for (var url : urls) {
           executor.submit(() -> fetchUrl(url)); // Each blocks on I/O
       }
   } // Handles thousands of concurrent I/O operations efficiently
   ```

3. **Treating virtual thread thread-group as configurable** — Virtual threads always belong to the "VirtualThreads"
   group.

   ```java
   // This does NOT change the virtual thread's group
   ThreadGroup myGroup = new ThreadGroup("my-group");
   // Thread.ofVirtual() does not accept a ThreadGroup parameter
   Thread vt = Thread.ofVirtual().unstarted(() -> {});
   System.out.println(vt.getThreadGroup().getName()); // "VirtualThreads"
   ```

4. **Using `Thread.getAllStackTraces()` to monitor virtual threads** — This method does not include virtual threads.
   Use JFR or thread dumps via `jcmd` instead.

   ```java
   // WRONG: Does not show virtual threads
   Map<Thread, StackTraceElement[]> all = Thread.getAllStackTraces();

   // CORRECT: Use jcmd for thread dumps that include virtual threads
   // jcmd <pid> Thread.dump_to_file -format=json output.json
   ```

5. **Comparing performance using small numbers of tasks** — Virtual threads show their advantage only when the number
   of concurrent tasks significantly exceeds the number of available platform threads.

   ```java
   // WRONG benchmark: 10 tasks - no visible difference
   // CORRECT benchmark: 10,000+ I/O-bound tasks to see the difference
   ```

## Best Practices and Optimization Techniques

1. **Replace `synchronized` with `ReentrantLock`** in code that will run on virtual threads, especially if blocking
   operations occur inside the critical section.

2. **Use `-Djdk.tracePinnedThreads=short`** during development and testing to detect pinning early.

3. **Use semaphores to limit resource access**, not thread pools:
   ```java
   Semaphore dbSemaphore = new Semaphore(20);
   try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
       for (var query : queries) {
           executor.submit(() -> {
               dbSemaphore.acquire();
               try {
                   return executeQuery(query);
               } finally {
                   dbSemaphore.release();
               }
           });
       }
   }
   ```

4. **Profile with JFR** to understand virtual thread behavior in production:
   ```bash
   java -XX:StartFlightRecording=filename=recording.jfr,duration=60s MyApp
   ```

5. **Migrate gradually** — Start by replacing `Executors.newFixedThreadPool()` with
   `Executors.newVirtualThreadPerTaskExecutor()` for I/O-bound services.

6. **Test with realistic load** — Virtual thread benefits are most visible under high concurrency. Test with thousands
   of concurrent tasks to validate throughput improvements.

7. **Avoid deep synchronized nesting** — Deeply nested synchronized blocks increase the risk and duration of pinning.

## Edge Cases and Their Handling

1. **Virtual thread joining itself**: Calling `join()` on the current virtual thread throws
   `IllegalStateException` (deadlock detection), same as platform threads.

2. **Exhausting carrier threads due to pinning**: If all carrier threads are pinned inside `synchronized` blocks, the
   JVM may add temporary carrier threads (up to `jdk.virtualThreadScheduler.maxPoolSize`) to maintain progress.

3. **Thread.sleep(0)**: On virtual threads, `Thread.sleep(0)` yields the virtual thread, potentially allowing other
   virtual threads to run on the carrier. On platform threads, it may or may not yield.

4. **Virtual threads and `Object.wait()`**: Virtual threads can call `wait()` on monitors. However, if the monitor was
   entered via `synchronized`, the virtual thread is pinned while waiting.

5. **`InheritableThreadLocal` with virtual threads**: Works as expected — child virtual threads inherit values from
   the parent. But be cautious of memory consumption at scale.

6. **Creating virtual threads inside `synchronized` blocks**: The virtual thread itself is not pinned just because it
   was created inside a synchronized block; it only pins when it enters its own synchronized block.

## Interview-specific Insights

This topic is a favorite in Java interviews for senior engineers because it tests deep understanding of concurrency:

- **"How do virtual threads achieve scalability?"** — Explain the mounting/unmounting mechanism, not just "they're
  lightweight."
- **"What is thread pinning?"** — Critical to demonstrate knowledge of the `synchronized` limitation and the
  `ReentrantLock` solution.
- **"Would you use virtual threads for a CPU-bound computation pipeline?"** — Demonstrate understanding that virtual
  threads are for I/O-bound workloads.
- **"How do you monitor virtual threads in production?"** — Mention JFR events, `jcmd` thread dumps, and the
  `-Djdk.tracePinnedThreads` flag.

Whiteboard coding approaches:
- Be prepared to sketch the mounting/unmounting lifecycle of a virtual thread on a carrier thread
- Know how to refactor `synchronized` to `ReentrantLock` for virtual thread compatibility
- Demonstrate migrating a `newFixedThreadPool` to `newVirtualThreadPerTaskExecutor`

## Interview Q&A Section

**Q1: What are the key differences between virtual threads and platform threads?**

```text
A1: The differences span several dimensions:

1. Resource consumption:
   - Platform threads: ~1 MB stack, one OS thread each
   - Virtual threads: ~200-300 bytes initially, share a pool of carrier threads

2. Scheduling:
   - Platform threads: Scheduled by the OS kernel
   - Virtual threads: Scheduled by the JVM (ForkJoinPool-based scheduler)

3. Blocking behavior:
   - Platform threads: OS thread is consumed while blocked
   - Virtual threads: Unmount from carrier thread when blocked, freeing it

4. Creation cost:
   - Platform threads: Expensive (OS-level allocation)
   - Virtual threads: Cheap (JVM-managed, like creating an object)

5. Properties:
   - Platform threads: Configurable daemon status and priority
   - Virtual threads: Always daemon, fixed NORM_PRIORITY

6. Scalability:
   - Platform threads: Limited to thousands
   - Virtual threads: Scales to millions

7. Best use case:
   - Platform threads: CPU-bound work
   - Virtual threads: I/O-bound work with high concurrency
```

```java
// Platform thread: expensive, limited scalability
Thread platform = Thread.ofPlatform()
        .name("platform-worker")
        .daemon(false)                    // Can be non-daemon
        .priority(Thread.MAX_PRIORITY)    // Priority respected
        .start(() -> doWork());
System.out.println(platform.isVirtual()); // false
System.out.println(platform.isDaemon());  // false

// Virtual thread: cheap, massive scalability
Thread virtual = Thread.ofVirtual()
        .name("virtual-worker")
        // .daemon(false) would throw IllegalArgumentException
        // .priority(MAX) would be silently ignored
        .start(() -> doWork());
System.out.println(virtual.isVirtual()); // true
System.out.println(virtual.isDaemon());  // always true
```

**Q2: What is thread pinning and how do you avoid it?**

```text
A2: Thread pinning occurs when a virtual thread cannot unmount from its carrier
thread at a blocking point. This happens in two scenarios:

1. When a virtual thread blocks inside a synchronized block or method
2. When a virtual thread executes a native method (JNI)

Why is pinning problematic?
When a virtual thread is pinned, its carrier (platform) thread is also blocked.
Since there are typically only as many carrier threads as CPU cores, pinning
reduces the effective concurrency. If all carrier threads are pinned, the system
can stall — new virtual threads cannot make progress until a carrier is freed.

How to detect pinning:
- Use -Djdk.tracePinnedThreads=full at runtime
- Use JFR events (jdk.VirtualThreadPinned)
- Profile under load to identify contention

How to avoid pinning:
- Replace synchronized blocks with ReentrantLock
- Keep synchronized blocks short and non-blocking
- Avoid blocking I/O inside synchronized sections
- In Java 24+, JEP 491 eliminates synchronized pinning

Note: Pinning for short, non-blocking synchronized sections is usually harmless.
The concern is when blocking operations (sleep, I/O, lock wait) happen inside
synchronized blocks.
```

```java
// PROBLEMATIC: Pinning due to synchronized + blocking
public class PinningExample {
    private final Object lock = new Object();

    public void problematic() {
        synchronized (lock) {
            // Virtual thread is PINNED here
            try {
                Thread.sleep(1000);          // Carrier thread blocked!
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            fetchFromDatabase();             // Carrier thread still blocked!
        }
    }
}

// FIXED: Using ReentrantLock avoids pinning
public class NoPinningExample {
    private final ReentrantLock lock = new ReentrantLock();

    public void fixed() {
        lock.lock();
        try {
            // Virtual thread can unmount here
            try {
                Thread.sleep(1000);          // Carrier thread freed!
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            fetchFromDatabase();             // Carrier thread freed!
        } finally {
            lock.unlock();
        }
    }
}
```

**Q3: When should you use platform threads instead of virtual threads?**

```text
A3: Platform threads remain the better choice in several scenarios:

1. CPU-bound computation:
   Virtual threads provide no benefit for CPU-intensive tasks because there are
   no blocking points where unmounting can occur. A fixed pool of platform threads
   sized to the number of CPU cores is optimal.

2. When you need thread priority control:
   Virtual threads ignore setPriority(). If your application relies on thread
   priorities for scheduling, use platform threads.

3. When you need non-daemon threads:
   Virtual threads are always daemon threads. If you need threads that prevent
   JVM shutdown, use platform threads.

4. Low-concurrency scenarios:
   If your application never has more than a few dozen concurrent tasks, the
   overhead difference between platform and virtual threads is negligible.

5. Heavy ThreadLocal usage with no migration path:
   If your codebase relies heavily on expensive ThreadLocal values and you
   cannot refactor, platform threads may be more memory-efficient since you
   have fewer of them.

6. Latency-sensitive real-time applications:
   The JVM's virtual thread scheduler may introduce non-deterministic scheduling
   delays. OS-level thread scheduling with real-time priorities may be needed.

Rule of thumb: Use virtual threads for I/O-bound work, platform threads for
CPU-bound work.
```

```java
// CPU-bound: Use platform thread pool
ExecutorService cpuPool = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors());
try {
    List<Future<Double>> results = cpuPool.invokeAll(
        IntStream.range(0, 1000)
            .mapToObj(i -> (Callable<Double>) () -> computePI(1_000_000))
            .toList()
    );
} finally {
    cpuPool.shutdown();
}

// I/O-bound: Use virtual threads
try (var ioExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
    List<Future<String>> results = ioExecutor.invokeAll(
        urls.stream()
            .map(url -> (Callable<String>) () -> fetchUrl(url))
            .toList()
    );
}
```

**Q4: How do virtual threads work with existing concurrency utilities?**

```text
A4: Virtual threads are designed to be fully compatible with the existing
java.util.concurrent utilities. This backward compatibility was a deliberate
design goal of Project Loom.

Compatible utilities include:
- ExecutorService, Future, Callable - Submit tasks and get results
- CompletableFuture - Compose async operations
- CountDownLatch, CyclicBarrier - Coordinate threads
- Semaphore - Control access to resources
- ReentrantLock, ReadWriteLock - Mutual exclusion (preferred over synchronized)
- BlockingQueue - Producer-consumer patterns
- ConcurrentHashMap - Concurrent data structures
- AtomicInteger, AtomicReference - Lock-free operations

The only utility that behaves differently is the synchronized keyword, which
causes pinning. All java.util.concurrent utilities are virtual-thread-friendly.

Migration strategy:
1. Replace Executors.newFixedThreadPool() with newVirtualThreadPerTaskExecutor()
2. Replace synchronized with ReentrantLock where blocking occurs inside
3. Replace ThreadLocal with ScopedValue where possible (preview)
4. Keep everything else the same
```

```java
// All of these work correctly with virtual threads:

// 1. CountDownLatch
CountDownLatch latch = new CountDownLatch(100);
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 100; i++) {
        executor.submit(() -> {
            doWork();
            latch.countDown();
        });
    }
}
latch.await();

// 2. Semaphore to limit resource access
Semaphore sem = new Semaphore(10);
Thread.startVirtualThread(() -> {
    sem.acquire();
    try {
        accessLimitedResource();
    } finally {
        sem.release();
    }
});

// 3. BlockingQueue for producer-consumer
BlockingQueue<String> queue = new LinkedBlockingQueue<>();
Thread.startVirtualThread(() -> queue.put("item"));
Thread.startVirtualThread(() -> {
    String item = queue.take(); // Unmounts while waiting
    process(item);
});

// 4. CompletableFuture with virtual thread executor
var executor = Executors.newVirtualThreadPerTaskExecutor();
CompletableFuture.supplyAsync(() -> fetchData(), executor)
        .thenApply(data -> transform(data))
        .thenAccept(result -> save(result));
```

**Q5: How do you monitor and debug virtual threads in production?**

```text
A5: Virtual threads require different monitoring strategies than platform threads
because traditional tools may not display them:

1. Thread.getAllStackTraces() does NOT include virtual threads
   - Use jcmd <pid> Thread.dump_to_file -format=json output.json instead
   - This produces a JSON thread dump that includes virtual threads

2. Java Flight Recorder (JFR) events:
   - jdk.VirtualThreadStart: When a virtual thread starts
   - jdk.VirtualThreadEnd: When a virtual thread ends
   - jdk.VirtualThreadPinned: When a virtual thread is pinned (critical!)
   - jdk.VirtualThreadSubmitFailed: When scheduling fails

3. Pinning detection:
   - -Djdk.tracePinnedThreads=full prints stack traces on pinning
   - -Djdk.tracePinnedThreads=short prints shorter output
   - Essential during development and testing

4. JMX/MBeans:
   - Standard thread MBeans track platform threads only
   - Use JFR for virtual thread metrics

5. Logging:
   - Name your virtual threads (Thread.ofVirtual().name("prefix-", 0))
   - Use MDC (Mapped Diagnostic Context) for request tracking

Best practices:
- Enable JFR recording in production
- Use pinning detection during development
- Monitor carrier thread pool utilization
- Track virtual thread creation rate and active count
```

```java
// Named virtual threads for better debugging
ThreadFactory factory = Thread.ofVirtual()
        .name("order-processor-", 0)
        .factory();

try (var executor = Executors.newThreadPerTaskExecutor(factory)) {
    // Threads will be named order-processor-0, order-processor-1, etc.
    executor.submit(() -> processOrder(order));
}

// Detecting pinning during development
// Run with: java -Djdk.tracePinnedThreads=full MyApp
// Output when pinning occurs:
// Thread[#23,VirtualThread-unparker,5,main]
//     java.base/java.lang.VirtualThread$VThreadContinuation.onPinned(...)
//     at MyClass.synchronizedMethod(MyClass.java:42) <== monitor held

// JFR recording for production monitoring
// java -XX:StartFlightRecording=filename=vt.jfr,duration=60s,
//       settings=profile MyApp
```

**Q6: What happens when you migrate an existing application from platform threads to virtual threads?**

```text
A6: Migration is generally straightforward but requires attention to several areas:

Step 1: Replace thread pool creation
- Change Executors.newFixedThreadPool(N) to
  Executors.newVirtualThreadPerTaskExecutor()
- Change Executors.newCachedThreadPool() to
  Executors.newVirtualThreadPerTaskExecutor()

Step 2: Identify and fix synchronized pinning
- Search for synchronized blocks that contain blocking operations
- Replace with ReentrantLock
- Use -Djdk.tracePinnedThreads=full to find remaining issues

Step 3: Audit ThreadLocal usage
- Check for expensive ThreadLocal initial values
- Consider ScopedValue (preview) for new code
- Ensure ThreadLocal.remove() is called appropriately

Step 4: Test under load
- Virtual threads may expose hidden concurrency bugs
- Higher concurrency means more contention on shared resources
- Database connection pools may need Semaphore-based throttling

Step 5: Monitor
- Set up JFR recording
- Track pinning events
- Monitor memory usage (more concurrent tasks = more objects in flight)

Common surprises during migration:
- Database connection pools exhausted (too many concurrent queries)
- Higher memory usage due to more concurrent in-flight requests
- Existing race conditions becoming more visible
- synchronized-based third-party libraries causing pinning
```

```java
// Before migration: fixed thread pool
ExecutorService executor = Executors.newFixedThreadPool(200);
try {
    for (var request : requests) {
        executor.submit(() -> {
            synchronized (this) {           // Potential pinning
                var data = fetchFromDB();   // Blocking I/O inside sync
                return process(data);
            }
        });
    }
} finally {
    executor.shutdown();
}

// After migration: virtual threads + ReentrantLock
ReentrantLock lock = new ReentrantLock();
Semaphore dbLimit = new Semaphore(50); // Protect DB pool

try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (var request : requests) {
        executor.submit(() -> {
            lock.lock();
            try {
                dbLimit.acquire();
                try {
                    var data = fetchFromDB();
                    return process(data);
                } finally {
                    dbLimit.release();
                }
            } finally {
                lock.unlock();
            }
        });
    }
} // AutoCloseable: waits for all tasks
```

**Q7: How does the virtual thread scheduler work internally?**

```text
A7: The virtual thread scheduler is built on a work-stealing ForkJoinPool:

Architecture:
- A dedicated ForkJoinPool serves as the carrier thread pool
- Default parallelism = Runtime.getRuntime().availableProcessors()
- Configurable via -Djdk.virtualThreadScheduler.parallelism=N
- Max pool size via -Djdk.virtualThreadScheduler.maxPoolSize=N (default 256)

Scheduling lifecycle:
1. When a virtual thread is started, it is submitted to the ForkJoinPool
2. A carrier thread picks it up and executes it (mounting)
3. When the virtual thread hits a blocking point:
   a. The JVM saves the virtual thread's stack (continuation)
   b. The virtual thread unmounts from the carrier
   c. The carrier thread picks up another runnable virtual thread
4. When the blocking operation completes (I/O ready, sleep elapsed, etc.):
   a. The virtual thread is re-submitted to the scheduler
   b. Any available carrier thread can resume it (remounting)
   c. The continuation is restored and execution continues

Key implementation details:
- Virtual threads use Continuation objects (internal API) to save/restore stack
- The stack is stored on the heap, not on the native thread stack
- Work-stealing ensures carrier threads are utilized efficiently
- The scheduler is not configurable beyond parallelism/maxPoolSize

Performance characteristics:
- Mounting/unmounting is ~100-1000x cheaper than OS context switch
- Stack storage is heap-allocated and GC-managed
- No kernel transitions for virtual thread scheduling
```

```java
// The scheduler can be configured via system properties
// Default: parallelism = available processors
// -Djdk.virtualThreadScheduler.parallelism=8
// -Djdk.virtualThreadScheduler.maxPoolSize=256

// You can observe the scheduling by looking at thread names
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 50; i++) {
        final int id = i;
        executor.submit(() -> {
            // toString() shows carrier: VirtualThread[#N]/ForkJoinPool-1-worker-M
            System.out.printf("Task %d on: %s%n", id, Thread.currentThread());

            try {
                Thread.sleep(100); // Unmount happens here
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // After sleep, may be on a different carrier
            System.out.printf("Task %d resumed on: %s%n", id, Thread.currentThread());
        });
    }
}
```

## Code Examples

- Test: [VirtualVsPlatformThreadsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/virtualthreads/VirtualVsPlatformThreadsTest.java)
- Source: [VirtualVsPlatformThreads.java](src/main/java/com/github/msorkhpar/claudejavatutor/virtualthreads/VirtualVsPlatformThreads.java)
