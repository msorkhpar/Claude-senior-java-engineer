# 6.1.1. Creating Threads (Runnable Interface and Thread Class)

## Concept Explanation

In Java, a thread is a lightweight unit of execution that runs concurrently within a process. The JVM supports multithreading natively, allowing multiple threads to run in parallel (on multi-core systems) or time-shared (on single-core systems).

There are two fundamental ways to create a thread in Java:

1. **Extending the `Thread` class**: Override the `run()` method in a subclass.
2. **Implementing the `Runnable` interface**: Implement the `run()` method and pass the `Runnable` to a `Thread` constructor.

**Real-world analogy**: Think of a restaurant kitchen. The kitchen (JVM process) has multiple chefs (threads). Each chef follows a recipe (Runnable/task). Some chefs specialize in certain dishes (subclassing Thread), but the most flexible arrangement is having general-purpose chefs who receive recipe cards (Runnable tasks). The restaurant manager (Thread scheduler) decides which chef cooks what and when.

### Method 1: Extending Thread

```java
public class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("Running in thread: " + Thread.currentThread().getName());
    }
}

MyThread t = new MyThread();
t.start();
```

### Method 2: Implementing Runnable

```java
public class MyTask implements Runnable {
    @Override
    public void run() {
        System.out.println("Running in thread: " + Thread.currentThread().getName());
    }
}

Thread t = new Thread(new MyTask());
t.start();
```

### Method 3: Lambda Expression (Java 8+)

Since `Runnable` is a functional interface, it can be expressed as a lambda:

```java
Thread t = new Thread(() -> System.out.println("Thread running!"));
t.start();
```

### Method 4: Virtual Threads (Java 21)

```java
Thread virtualThread = Thread.ofVirtual().start(() -> System.out.println("Virtual thread!"));
```

## Key Points to Remember

1. **`Runnable` is preferred over `Thread` subclassing** because it separates task logic from thread management and supports composition (a class can implement `Runnable` and extend another class).
2. **`run()` vs `start()`**: Calling `run()` executes the method on the current thread (no new thread). Calling `start()` creates a new OS-level thread and then invokes `run()` on it.
3. **`Runnable` is a functional interface** with a single abstract method `void run()`, so lambdas work seamlessly.
4. **Thread naming**: Threads get default names like `Thread-0`, `Thread-1`. You can set a custom name via the constructor or `setName()`.
5. **Daemon threads**: Threads can be daemon (background) threads. The JVM exits when all non-daemon threads complete.
6. **Thread priority**: Ranges from `Thread.MIN_PRIORITY` (1) to `Thread.MAX_PRIORITY` (10). Default is `Thread.NORM_PRIORITY` (5).

## Relevant Java 21 Features

- **Virtual Threads (Project Loom - JEP 444)**: Java 21 introduced stable virtual threads — lightweight threads managed by the JVM rather than OS threads. They are ideal for I/O-bound tasks.
  ```java
  // Platform thread
  Thread platform = Thread.ofPlatform().name("my-platform-thread").start(() -> doWork());

  // Virtual thread
  Thread virtual = Thread.ofVirtual().name("my-virtual-thread").start(() -> doWork());
  ```
- **`Thread.Builder` API**: Java 21 provides a builder pattern for thread creation:
  ```java
  Thread t = Thread.ofPlatform()
      .name("worker", 0)
      .daemon(false)
      .priority(Thread.NORM_PRIORITY)
      .start(() -> doWork());
  ```
- **Structured Concurrency (JEP 453 - Preview)**: A higher-level API for managing related threads as a unit.

## Common Pitfalls and How to Avoid Them

1. **Calling `run()` instead of `start()`**:
   ```java
   // Wrong: runs on the current thread, no concurrency
   thread.run();

   // Correct: creates a new thread
   thread.start();
   ```

2. **Subclassing Thread when Runnable suffices**:
   ```java
   // Problematic: limits inheritance, tightly couples task and thread
   class MyTask extends Thread { ... }

   // Better: separates concerns
   class MyTask implements Runnable { ... }
   Thread t = new Thread(new MyTask());
   ```

3. **Starting the same Thread instance twice**:
   ```java
   Thread t = new Thread(() -> doWork());
   t.start();
   t.start(); // Throws IllegalThreadStateException!
   ```

4. **Forgetting to handle `InterruptedException`**:
   ```java
   // Wrong: swallowing the interrupt
   try { Thread.sleep(1000); } catch (InterruptedException e) { /* ignored */ }

   // Correct: restore the interrupt status
   try {
       Thread.sleep(1000);
   } catch (InterruptedException e) {
       Thread.currentThread().interrupt();
       // handle gracefully
   }
   ```

5. **Using anonymous `Thread` subclasses when lambdas are cleaner**:
   ```java
   // Verbose
   new Thread(new Runnable() {
       @Override public void run() { doWork(); }
   }).start();

   // Clean
   new Thread(() -> doWork()).start();
   ```

## Best Practices and Optimization Techniques

1. **Prefer `Runnable` (or `Callable`) over `Thread` subclassing** for better separation of concerns.
2. **Use thread pools** (via `ExecutorService`) instead of creating raw threads for production workloads. Raw thread creation is expensive.
3. **Name your threads** — it makes debugging thread dumps significantly easier:
   ```java
   Thread t = new Thread(task, "payment-processor-1");
   ```
4. **Use virtual threads for I/O-bound tasks** (Java 21+): They scale to millions without exhausting OS thread resources.
5. **Set daemon status appropriately**: Background tasks that should not prevent JVM shutdown should be daemon threads.
6. **Avoid thread creation in tight loops**: Prefer thread pools or virtual threads managed by `ExecutorService`.

## Edge Cases and Their Handling

1. **Null `Runnable`**: Passing `null` to `Thread(Runnable)` is allowed — the thread will run `Thread.run()` which calls nothing (since it checks for a null target).
2. **Starting a thread from within a constructor**: Dangerous — the object might not be fully initialized when the thread starts running. Use factory methods instead.
3. **Thread name conflicts**: Java allows multiple threads with the same name. Use unique naming conventions.
4. **Priority hints**: Thread priority is a hint to the OS scheduler, not a guarantee. Do not rely on priority for correctness.
5. **Stack overflow in thread**: Each thread has its own stack. Deep recursion can cause `StackOverflowError` in the thread without affecting others.

## Interview-specific Insights

Interviewers frequently ask about:
- The difference between `Runnable` and `Thread`, and why `Runnable` is preferred.
- What happens if you call `run()` directly instead of `start()`.
- How `Callable` differs from `Runnable` (returns a value, can throw checked exceptions).
- The role of virtual threads in Java 21 and how they differ from platform threads.
- Thread naming and why it matters in production.

Tricky questions to expect:
- "Can you start a thread twice? What happens?"
- "Why is extending Thread considered a design smell?"
- "What is the difference between a daemon thread and a non-daemon thread?"

## Interview Q&A Section

**Q1: What is the difference between implementing `Runnable` and extending `Thread` for creating threads?**

```text
A1: Both approaches achieve concurrent execution, but they differ significantly:

1. INHERITANCE: Extending Thread uses up Java's single-inheritance slot, preventing the class from extending any other class. Implementing Runnable leaves inheritance available.

2. SEPARATION OF CONCERNS: Runnable separates the task (what to do) from the execution mechanism (how/when to run it). This follows the Single Responsibility Principle.

3. REUSABILITY: A Runnable can be submitted to thread pools, scheduled executors, or virtual thread builders. An extended Thread is tightly coupled to its execution context.

4. FLEXIBILITY: The same Runnable instance can be shared across multiple threads (if thread-safe) or wrapped in different thread types (platform, virtual, daemon).

5. LAMBDA-FRIENDLY: Since Runnable is a functional interface, it works with lambda expressions. Thread subclasses cannot.

Verdict: Always prefer Runnable (or Callable) over Thread subclassing unless you have a specific reason to customize Thread behavior.
```

```java
// Inferior: Thread subclassing
class DataLoader extends Thread {
    @Override
    public void run() {
        loadData();
    }
}

// Superior: Runnable
class DataLoader implements Runnable {
    @Override
    public void run() {
        loadData();
    }
}

// Usage: can submit to any executor
ExecutorService pool = Executors.newFixedThreadPool(4);
pool.submit(new DataLoader());

// Lambda version
pool.submit(() -> loadData());
```

**Q2: What happens if you call `thread.run()` instead of `thread.start()`?**

```text
A2: Calling run() directly does NOT create a new thread. The run() method executes synchronously on the CALLING thread, just like any ordinary method call.

- thread.start(): Creates a new OS-level thread, registers it with the JVM thread scheduler, and then asynchronously calls run() on the new thread. Returns immediately to the caller.
- thread.run(): Simply invokes the run() method on the current thread. No new thread is created. The entire execution happens sequentially.

This is one of the most common beginner mistakes. It explains why a program might appear to work correctly during testing (because the logic in run() executes) but doesn't gain any concurrency benefit.
```

```java
public class RunVsStart {
    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            System.out.println("Executing in: " + Thread.currentThread().getName());
        });

        // Calling run(): prints "Executing in: main"
        t.run();

        // Calling start(): prints "Executing in: Thread-0" (new thread)
        t.start();
    }
}
```

**Q3: Can you start a thread more than once? What exception is thrown?**

```text
A3: No. Once a Thread has been started (even if it has already finished), calling start() again throws an IllegalThreadStateException.

This is because Thread has an internal state machine. Once the thread transitions from NEW to RUNNABLE (via start()), it can never return to NEW. Even after TERMINATED, the thread cannot be restarted.

The solution is to create a new Thread instance each time, or better yet, use a thread pool which manages thread reuse transparently.
```

```java
Thread t = new Thread(() -> System.out.println("Hello"));
t.start();
try {
    t.join(); // wait for it to finish
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}

// This throws IllegalThreadStateException
try {
    t.start(); // ERROR!
} catch (IllegalThreadStateException e) {
    System.out.println("Cannot restart a thread: " + e.getMessage());
}

// Correct: create a new thread instance
Thread t2 = new Thread(() -> System.out.println("Hello again"));
t2.start();
```

**Q4: How do virtual threads in Java 21 differ from platform threads when it comes to creation?**

```text
A4: Virtual threads (Project Loom, JEP 444, stable in Java 21) represent a paradigm shift:

PLATFORM THREADS:
- Mapped 1:1 to OS threads
- Expensive to create (~1MB stack by default)
- Context switching is a kernel-level operation
- Typical JVM: ~thousands of platform threads maximum

VIRTUAL THREADS:
- Managed entirely by the JVM, multiplexed onto a small pool of carrier (OS) threads
- Very cheap to create (a few hundred bytes of stack initially, grows dynamically)
- Blocking a virtual thread only parks it — the carrier thread picks up another virtual thread
- Typical JVM: millions of virtual threads possible

Creation syntax:
- Thread.ofVirtual().start(task)  // Virtual thread
- Thread.ofPlatform().start(task)  // Platform thread (explicit)
- Executors.newVirtualThreadPerTaskExecutor()  // Pool that creates one virtual thread per task

When to use virtual threads:
- I/O-bound tasks (network calls, database queries, file I/O)
- High-concurrency servers handling many simultaneous connections

When to stick with platform threads:
- CPU-intensive tasks that need true parallelism
- Tasks requiring thread-local state that assumes 1:1 OS thread mapping
```

```java
// Platform thread (old way)
Thread platform = new Thread(() -> processRequest());
platform.start();

// Virtual thread (Java 21)
Thread virtual = Thread.ofVirtual().name("handler-1").start(() -> processRequest());

// Virtual thread per task executor (recommended for servers)
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 1_000_000; i++) {
        executor.submit(() -> handleRequest());
    }
} // auto-closes and awaits all tasks
```

**Q5: What is a daemon thread, and when should you use one?**

```text
A5: A daemon thread is a background thread that does not prevent the JVM from exiting. When all non-daemon (user) threads finish, the JVM shuts down even if daemon threads are still running.

Key characteristics:
- Set before start(): thread.setDaemon(true) must be called before thread.start()
- Inherits daemon status from parent: if the creating thread is daemon, child threads are daemon by default
- JVM shutdown interrupts daemon threads abruptly (no finally block guarantees)

Use cases for daemon threads:
- Garbage collection (JVM's GC threads are daemon)
- Background monitoring / health checks
- Log flushing in the background
- Housekeeping tasks that should not block JVM exit

Caution: Never use daemon threads for tasks requiring guaranteed cleanup (file I/O flush, database transaction commit), because they may be killed abruptly when the JVM exits.
```

```java
Thread monitor = new Thread(() -> {
    while (true) {
        try {
            collectMetrics();
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            break;
        }
    }
});
monitor.setDaemon(true); // Must be set BEFORE start()
monitor.setName("metrics-monitor");
monitor.start();
// JVM can now exit even if monitor is sleeping
```

**Q6: What is `Callable` and how does it differ from `Runnable`?**

```text
A6: Callable<V> is a functional interface (java.util.concurrent) similar to Runnable but with two key differences:

1. RETURN VALUE: Callable<V>.call() returns a value of type V. Runnable.run() returns void.
2. CHECKED EXCEPTIONS: Callable.call() declares "throws Exception", allowing checked exceptions to propagate. Runnable.run() cannot throw checked exceptions.

Callable is used with ExecutorService.submit(), which returns a Future<V> that can retrieve the result asynchronously.

Runnable is used for fire-and-forget tasks where the result is not needed or is communicated through shared state.
```

```java
// Runnable: no return value
Runnable task = () -> System.out.println("Done");
ExecutorService executor = Executors.newSingleThreadExecutor();
Future<?> future1 = executor.submit(task);

// Callable: returns a value
Callable<Integer> computation = () -> {
    Thread.sleep(100); // can throw checked exception
    return 42;
};
Future<Integer> future2 = executor.submit(computation);

try {
    Integer result = future2.get(); // blocks until result is available
    System.out.println("Result: " + result); // 42
} catch (InterruptedException | ExecutionException e) {
    Thread.currentThread().interrupt();
}
executor.shutdown();
```

**Q7: How do you set a thread name, and why does it matter?**

```text
A7: Thread names can be set via:
1. Thread constructor: new Thread(runnable, "my-thread-name")
2. setName() method: thread.setName("worker-1")
3. Thread.Builder: Thread.ofPlatform().name("prefix-", 0).start(...)

Why naming matters:
1. DEBUGGING: Thread dumps (via jstack, VisualVM, etc.) show thread names. Named threads make it immediately obvious which component owns which thread.
2. MONITORING: JMX, profilers, and APMs display thread names in metrics.
3. LOGGING: Many logging frameworks can include the thread name in log output (via %t in log patterns), making it easy to trace request flows.
4. CODE READABILITY: Named threads make the code's intent clearer.

Convention: Use descriptive names that indicate purpose — e.g., "order-processor-1", "db-connection-health-check", "kafka-consumer-0".
```

```java
// Naming at construction
Thread worker = new Thread(() -> processOrders(), "order-processor-1");

// Naming with builder (auto-incrementing suffix)
Thread.Builder.OfPlatform builder = Thread.ofPlatform().name("db-worker-", 0);
Thread t1 = builder.start(() -> queryDatabase()); // "db-worker-0"
Thread t2 = builder.start(() -> queryDatabase()); // "db-worker-1"

// In production: thread name appears in log output
// [order-processor-1] INFO OrderService - Processing order 12345
```

## Code Examples

- Source: [ThreadCreation.java](src/main/java/com/github/msorkhpar/claudejavatutor/threadbasics/ThreadCreation.java)
- Test: [ThreadCreationTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/threadbasics/ThreadCreationTest.java)
