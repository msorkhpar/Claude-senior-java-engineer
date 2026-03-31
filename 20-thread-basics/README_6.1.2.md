# 6.1.2. Starting Threads (start() Method)

## Concept Explanation

The `start()` method is the gateway to true concurrent execution in Java. When you call `start()` on a `Thread` object, the JVM performs several critical actions:

1. **Allocates a new OS thread** (or virtual thread for virtual threads) with its own stack.
2. **Transitions the thread from NEW to RUNNABLE state**.
3. **Registers the thread with the JVM thread scheduler**.
4. **Returns immediately** to the calling thread — the caller does not block.
5. **Asynchronously calls `run()`** on the newly created thread.

**Real-world analogy**: Think of hiring a new employee (calling `start()`). Once hired, the employee immediately begins their onboarding and work independently. You (the manager/calling thread) don't wait for them to finish — you continue your own work. The HR department (JVM scheduler) coordinates who works when. In contrast, simply assigning them a task list without hiring them (calling `run()` directly) means you do the work yourself.

### What Happens Internally During `start()`

```
Thread.start()
    ↓
Checks thread state (must be NEW, otherwise throws IllegalThreadStateException)
    ↓
Calls native start0() (JVM native method)
    ↓
OS creates a new thread (or JVM creates a virtual thread)
    ↓
Thread transitions: NEW → RUNNABLE
    ↓
JVM scheduler queues the thread for execution
    ↓
start() returns to caller (non-blocking)
    ↓ (asynchronously, on the new thread)
Thread.run() is invoked
    ↓
Thread transitions: RUNNABLE → TERMINATED after run() completes
```

### `join()` — Waiting for a Thread to Complete

`join()` is the companion to `start()`. It blocks the calling thread until the target thread terminates:

```java
Thread t = new Thread(() -> doWork());
t.start();
t.join(); // Caller blocks here until t finishes
```

`join(long millis)` provides a timeout so you don't wait forever.

## Key Points to Remember

1. **`start()` is non-blocking** — it returns immediately. The new thread runs concurrently with the caller.
2. **`run()` is just a method call** — calling it directly does not start a new thread.
3. **Thread state transitions**: `start()` moves a thread from `NEW` to `RUNNABLE`. You cannot call `start()` on a thread that is not in `NEW` state.
4. **`IllegalThreadStateException`** is thrown if `start()` is called on an already-started thread.
5. **`join()` blocks** the calling thread until the thread completes, with optional timeout.
6. **`Thread.currentThread()`** returns a reference to the currently executing thread.
7. **`Thread.sleep(millis)`** causes the current thread to pause for the specified duration, releasing the CPU but not any held monitors.

## Relevant Java 21 Features

- **`Thread.ofVirtual().start(Runnable)`**: A concise way to start a virtual thread. Returns the started thread.
  ```java
  Thread vt = Thread.ofVirtual().start(() -> handleRequest());
  ```
- **`ExecutorService.newVirtualThreadPerTaskExecutor()`**: Creates a virtual thread for each submitted task. Ideal for high-concurrency I/O servers.
  ```java
  try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
      exec.submit(() -> handleRequest());
  }
  ```
- **Structured Concurrency (Preview)**: Groups related threads so that if one fails, others are cancelled:
  ```java
  try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      Future<String> f1 = scope.fork(() -> fetchFromDB());
      Future<String> f2 = scope.fork(() -> fetchFromCache());
      scope.join().throwIfFailed();
  }
  ```

## Common Pitfalls and How to Avoid Them

1. **Calling `run()` instead of `start()`**:
   ```java
   // WRONG: executes on current thread, no concurrency
   thread.run();

   // CORRECT: creates a new thread
   thread.start();
   ```

2. **Starting a thread twice**:
   ```java
   Thread t = new Thread(() -> doWork());
   t.start();
   t.start(); // Throws IllegalThreadStateException!
   ```

3. **Not joining when the result is needed**:
   ```java
   Thread t = new Thread(() -> results.add(computeValue()));
   t.start();
   // Missing t.join() here — results may be empty when we read it!
   System.out.println(results); // Race condition!
   ```

4. **Blocking the main thread with immediate `join()`**:
   ```java
   // Pointless: effectively sequential execution
   Thread t = new Thread(() -> doWork());
   t.start();
   t.join(); // Blocks main thread — no benefit from threading here
   ```
   Use `join()` only when you actually need to wait for all results before proceeding.

5. **Starting threads without error handling**:
   ```java
   // Better: always set an uncaught exception handler
   thread.setUncaughtExceptionHandler((t, e) -> {
       logger.error("Thread {} failed with exception", t.getName(), e);
   });
   thread.start();
   ```

6. **`join()` without timeout can deadlock**:
   ```java
   // Risky: waits forever if thread never terminates
   t.join();

   // Safer: timeout-bounded join
   t.join(5000); // Wait at most 5 seconds
   if (t.isAlive()) {
       t.interrupt(); // Request cancellation
   }
   ```

## Best Practices and Optimization Techniques

1. **Use thread pools instead of bare threads** in production:
   ```java
   ExecutorService executor = Executors.newFixedThreadPool(10);
   executor.submit(() -> doWork());
   ```

2. **Set `UncaughtExceptionHandler`** to catch thread failures silently dropped otherwise.

3. **Use `CountDownLatch` or `CyclicBarrier`** for coordinating groups of threads rather than chaining multiple `join()` calls.

4. **Prefer `CompletableFuture`** for chaining async operations with result propagation:
   ```java
   CompletableFuture.supplyAsync(() -> fetchData())
       .thenApply(data -> process(data))
       .thenAccept(result -> display(result));
   ```

5. **Use virtual threads for I/O-bound work** (Java 21+):
   ```java
   try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
       IntStream.range(0, 10_000).forEach(i ->
           executor.submit(() -> makeHttpCall(i)));
   }
   ```

## Edge Cases and Their Handling

1. **Thread starts but `run()` throws an unchecked exception**: The thread terminates with `TERMINATED` state. The exception is passed to the `UncaughtExceptionHandler`. If none is set, it prints to `System.err`.

2. **Thread starts before the `Runnable` is fully initialized**: Avoid passing partially constructed objects to threads. Use factory methods or builder patterns.

3. **`start()` on a thread whose `run()` is a no-op**: The thread transitions through all states instantly and terminates immediately. This is valid but wastes resources.

4. **`join()` on a thread that hasn't started**: `join()` returns immediately because an unstarted thread is considered "done" (it has never run).

5. **`join()` timeout of 0**: `join(0)` is equivalent to `join()` — it waits indefinitely.

## Interview-specific Insights

Interviewers focus on:
- The exact sequence of events that occur when `start()` is called.
- The distinction between `start()` and `run()`.
- When and how to use `join()`, and the risks of not using it.
- Thread state transitions triggered by `start()` and `join()`.
- Error handling strategies for background threads.

Tricky questions:
- "If I call `thread.join()` immediately after `thread.start()`, is that the same as calling `thread.run()`?"
- "What happens to a thread's exceptions if no `UncaughtExceptionHandler` is set?"
- "Can you start a `Thread` from within its own `run()` method to restart it?"

## Interview Q&A Section

**Q1: Walk me through exactly what happens when `thread.start()` is called.**

```text
A1: When thread.start() is called, the following sequence occurs:

1. The JVM checks that the thread is in NEW state. If it's in any other state, IllegalThreadStateException is thrown.

2. The thread's internal state is changed from NEW to RUNNABLE.

3. The native start0() method is called, which requests the OS to create a new OS thread (for platform threads) or the JVM to create a virtual thread.

4. The OS thread is registered with the JVM thread scheduler.

5. start() returns to the caller — this is non-blocking. The calling thread continues immediately.

6. Asynchronously: when the scheduler gives the new thread CPU time, it begins executing thread.run(). For a Thread subclass, this calls the overridden run(). For a Thread with a Runnable target, it calls target.run().

7. When run() completes (or throws an unchecked exception), the thread transitions to TERMINATED state.
```

```java
public class StartSequenceDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread t = new Thread(() -> {
            System.out.println("2. New thread runs: " + Thread.currentThread().getName());
        });

        System.out.println("1. Before start() - state: " + t.getState()); // NEW
        t.start();
        System.out.println("3. After start() returns - main continues immediately");
        t.join(); // wait for t to finish
        System.out.println("4. After join() - thread state: " + t.getState()); // TERMINATED
    }
}
```

**Q2: What is the difference between `thread.join()`, `thread.join(millis)`, and `Thread.sleep(millis)`?**

```text
A2:
thread.join():
- Blocks the CALLING thread until the TARGET thread terminates
- InterruptedException is thrown if the calling thread is interrupted while waiting
- join(0) waits indefinitely (same as join())

thread.join(long millis):
- Blocks the CALLING thread for at most 'millis' milliseconds or until the target thread terminates
- Returns even if the thread hasn't finished (check t.isAlive() afterwards)
- join(millis, nanos) provides nanosecond precision

Thread.sleep(long millis):
- Blocks the CURRENT thread (the thread calling sleep()) for the specified duration
- Does not release any held monitors/locks
- Can be used anywhere, not tied to another thread's lifecycle
- A sleeping thread can be woken early by interrupt()

Key difference: join() is about waiting for another thread; sleep() is about the current thread pausing.
```

```java
Thread worker = new Thread(() -> {
    try {
        Thread.sleep(2000); // worker sleeps for 2 seconds
        System.out.println("Worker done");
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
});

worker.start();

// Wait for worker, but at most 1 second
worker.join(1000);

if (worker.isAlive()) {
    System.out.println("Worker still running after 1 second timeout");
    worker.interrupt();
} else {
    System.out.println("Worker finished within timeout");
}
```

**Q3: How does `UncaughtExceptionHandler` work with thread `start()`?**

```text
A3: When a thread's run() method throws an unchecked exception (RuntimeException or Error), the thread terminates. The exception propagates to the UncaughtExceptionHandler.

There are three levels:
1. Thread-specific handler: set via thread.setUncaughtExceptionHandler(handler)
2. Thread group handler: ThreadGroup.uncaughtException()
3. Default handler: Thread.setDefaultUncaughtExceptionHandler(handler) — applies to all threads without a specific handler

If no handler handles it, the JVM prints the stack trace to System.err and the thread terminates silently from the application's perspective — no notification to the parent thread!

This is why setting an UncaughtExceptionHandler is critical for production code.
```

```java
Thread t = new Thread(() -> {
    throw new RuntimeException("Something went wrong!");
});

t.setUncaughtExceptionHandler((thread, throwable) -> {
    System.err.println("Thread " + thread.getName() + " failed: " + throwable.getMessage());
    // log, alert, restart, etc.
});

t.start();
t.join();
// Without the handler, the exception would be silently lost (just printed to stderr)
```

**Q4: Why can't you call `start()` on a thread more than once, even after it finishes?**

```text
A4: The Thread class has an internal integer field called 'threadStatus' (in OpenJDK). Once start() is called, this field is changed from 0 (NEW) to a non-zero value.

The start() method checks this field at the very beginning:
- If threadStatus != 0, it throws IllegalThreadStateException immediately.
- There is no way to reset threadStatus back to 0 — it's a one-way door.

This design is intentional: a Thread object represents a single execution lifecycle. After termination, the thread's resources (stack, etc.) are released. Restarting would require re-allocating these resources in an uncontrolled way.

The solution is to create a new Thread instance (or use a thread pool which handles this transparently).
```

```java
Thread t = new Thread(() -> System.out.println("Running"));

System.out.println("State: " + t.getState()); // NEW
t.start();
t.join();
System.out.println("State: " + t.getState()); // TERMINATED

try {
    t.start(); // Throws IllegalThreadStateException
} catch (IllegalThreadStateException e) {
    System.out.println("Cannot restart: " + e);
}

// Solution: create a new instance
Thread t2 = new Thread(() -> System.out.println("Running again"));
t2.start();
```

**Q5: What is the relationship between `Thread.start()` and the happens-before relationship in the Java Memory Model?**

```text
A5: The Java Memory Model (JMM) specifies that thread.start() establishes a happens-before relationship:

"All actions in a thread A that happen before thread A calls thread B.start() are visible to thread B."

This means:
- Any writes made by the parent thread BEFORE calling start() are guaranteed to be visible to the child thread when it starts running.
- Without this guarantee, the child thread might see stale values in memory due to caching or compiler reordering.

Similarly, thread.join() establishes:
"All actions in thread B are visible to thread A after thread A.join(thread B) returns successfully."

These guarantees allow safe transfer of data between threads via start()/join() without additional synchronization, as long as no concurrent writes happen during the child thread's execution.
```

```java
public class HappensBeforeDemo {
    private int value = 0; // No volatile needed here because of start() HB relationship

    public void demonstrate() throws InterruptedException {
        value = 42; // Write happens before start()

        Thread t = new Thread(() -> {
            // Guaranteed to see value = 42, not 0
            System.out.println("Child sees: " + value);
        });

        t.start(); // start() creates HB: write of 42 HB all actions in t

        t.join(); // join() creates HB: all of t's actions HB anything after join()

        // Safe to read result here after join()
    }
}
```

## Code Examples

- Source: [ThreadStarting.java](src/main/java/com/github/msorkhpar/claudejavatutor/threadbasics/ThreadStarting.java)
- Test: [ThreadStartingTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/threadbasics/ThreadStartingTest.java)
