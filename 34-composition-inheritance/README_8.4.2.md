# 8.4.2. Applying Composition over Inheritance in Concurrent Programming

## Concept Explanation

In concurrent programming, the choice between composition and inheritance has amplified consequences. Inheritance creates hidden dependencies between classes, and when multiple threads interact with an inheritance hierarchy, those hidden dependencies become potential sources of race conditions, deadlocks, and data corruption. Composition, by contrast, makes dependencies explicit, boundaries clear, and synchronization points visible.

**Real-world analogy**: Imagine a restaurant kitchen. The inheritance approach is like having a "head chef" who does everything -- cooking, plating, cleaning -- and apprentices who override specific steps. If two apprentices try to work simultaneously, they clash because they share the head chef's workspace and tools implicitly. The composition approach is like having independent stations: a grill station, a prep station, a plating station. Each station has its own tools and workspace. Multiple cooks can work simultaneously because the boundaries are clear, and coordination happens through explicit handoffs (passing plates), not implicit shared state.

### 8.4.2.1 Favoring Object Composition for Flexibility

In concurrent systems, flexibility means being able to swap, configure, and combine behaviors without restructuring class hierarchies. Composition achieves this by injecting behavior objects (strategies, policies, handlers) that can be changed at construction time or even at runtime.

For example, a retry mechanism for network calls can be composed with different retry strategies (fixed delay, exponential backoff, no retry) without creating a hierarchy of executor subclasses. Each strategy is a separate, testable component.

### 8.4.2.2 Avoiding Deep Inheritance Hierarchies

Deep hierarchies like `Thread -> WorkerThread -> RetryableWorkerThread -> LoggingRetryableWorkerThread` are particularly dangerous in concurrent code because:
- Each level may add its own synchronization, leading to nested locks and deadlock risks.
- Overriding a method in a subclass may inadvertently bypass synchronization added in a parent class.
- Testing requires instantiating the entire hierarchy, making unit testing difficult.

The alternative is to use the decorator pattern: start with a base implementation and wrap it with independent decorators for logging, retrying, synchronization, etc. Each decorator has a single responsibility and can be applied in any combination.

## Key Points to Remember

- Composition makes synchronization boundaries explicit -- each composed component manages its own thread safety.
- Inheritance can silently break synchronization when a subclass overrides a synchronized method without maintaining the lock contract.
- Decorators (composition-based wrappers) allow adding cross-cutting concerns (logging, retries, metrics, synchronization) independently.
- Composed objects can be tested in isolation, even in concurrent scenarios, because they do not depend on a superclass's internal threading behavior.
- Constructor injection of composed strategies makes concurrency policies configurable without code changes.
- Deep inheritance hierarchies with synchronized methods create lock ordering risks that are difficult to reason about.
- Virtual threads (Java 21) work naturally with composed task runners because each decorator simply delegates without blocking platform threads.

## Relevant Java 21 Features

- **Virtual threads (JEP 444)**: Composition pairs well with virtual threads. Composed task runners can be executed on virtual threads without concern about blocking platform threads. Each decorator in a composition chain runs on the same virtual thread, keeping the execution model simple.
- **Structured concurrency (JEP 453, incubator)**: Encourages scoped, composable concurrency patterns rather than deep thread hierarchies.
- **Records as strategy objects**: Immutable records can serve as lightweight strategy implementations that are inherently thread-safe.
- **Sealed interfaces for strategy types**: Sealed interfaces can define a closed set of strategy implementations, combining the benefits of restricted inheritance with composition-based usage.

## Common Pitfalls and How to Avoid Them

1. **Breaking synchronization through inheritance**

   When a subclass overrides a synchronized method without maintaining the synchronization contract, the thread safety of the parent class is silently broken:

   ```java
   // PROBLEM: Subclass breaks parent's synchronization
   class SafeCounter {
       protected int count = 0;
       public synchronized void increment() { count++; }
       public synchronized int getCount() { return count; }
   }

   class ExtendedCounter extends SafeCounter {
       @Override
       public void increment() { // NOT synchronized! Breaks thread safety
           count++;
           // Additional logic
       }
   }
   ```

   **Fix**: Use composition to add behavior while preserving the original synchronization:

   ```java
   class ExtendedCounter implements Counter {
       private final SafeCounter delegate;  // Thread-safe by its own contract

       public void increment() {
           delegate.increment();  // Delegates to synchronized method
           // Additional logic (in its own synchronization scope if needed)
       }
   }
   ```

2. **Deadlocks from nested synchronization in deep hierarchies**

   When a parent and child class both synchronize on different monitors, method calls that traverse the hierarchy can deadlock:

   ```java
   // PROBLEM: Parent locks on 'this', child locks on 'childLock'
   class Parent {
       synchronized void doWork() { /* ... */ }
   }

   class Child extends Parent {
       private final Object childLock = new Object();
       @Override
       synchronized void doWork() { // locks 'this'
           synchronized (childLock) {  // then locks childLock
               super.doWork();
           }
       }
   }
   ```

   **Fix**: Compose independent components with clear, non-overlapping lock scopes.

3. **Tight coupling to Thread class through inheritance**

   Extending `Thread` directly couples your logic to the threading mechanism:

   ```java
   // PROBLEM: Inheriting from Thread
   class MyWorker extends Thread {
       @Override
       public void run() { /* worker logic */ }
   }
   ```

   **Fix**: Implement `Runnable` or `Callable` and compose with an executor:

   ```java
   class MyWorker implements Runnable {
       @Override
       public void run() { /* worker logic */ }
   }
   // Usage: executor.submit(new MyWorker());
   ```

4. **Overriding methods that have implicit concurrency contracts**

   Methods in concurrent classes (e.g., `ConcurrentHashMap.computeIfAbsent`) have specific atomicity guarantees. Overriding them in subclasses can violate those guarantees.

   **Fix**: Wrap concurrent classes with composition rather than extending them.

## Best Practices and Optimization Techniques

1. **Compose retry strategies as objects**: Define retry behavior as a strategy interface with implementations (fixed, exponential, no-retry). Inject the strategy into executors rather than creating executor subclasses.
2. **Use the decorator pattern for cross-cutting concerns**: Layer logging, metrics, synchronization, and error handling as independent decorators around a core implementation.
3. **Inject concurrency policies through composition**: Thread pool configuration, timeout policies, and backpressure strategies should be composed, not hardcoded in base classes.
4. **Keep composed components stateless when possible**: Stateless components are inherently thread-safe and can be shared freely.
5. **Use `CopyOnWriteArrayList` for composed listener lists**: When composing observable behavior in concurrent contexts, use thread-safe collections for observer/listener lists.
6. **Prefer immutable composed objects**: Immutable strategy objects, formatters, and validators eliminate the need for synchronization entirely.

## Edge Cases and Their Handling

1. **Null strategies/delegates**: Always validate composed objects with `Objects.requireNonNull()` in constructors.
2. **Exception propagation through decorators**: Each decorator must decide whether to catch, wrap, or propagate exceptions. Document this behavior clearly.
3. **Decorator ordering**: The order of decorator application matters (e.g., logging before synchronization vs. after). Make ordering explicit in factory methods.
4. **Thread-local state in decorators**: Decorators that maintain thread-local state must ensure proper cleanup, especially with virtual threads that may be reused.
5. **Shutdown and resource cleanup**: Composed components that hold resources (executors, connections) need explicit lifecycle management. Consider implementing `AutoCloseable`.

## Interview-specific Insights

Interviewers testing concurrent design often ask candidates to:
- Refactor a thread-unsafe inheritance hierarchy into a composition-based design.
- Explain why extending `Thread` is considered bad practice compared to implementing `Runnable`.
- Design a retry mechanism using the Strategy pattern (composition) rather than subclassing.
- Identify deadlock risks in inheritance hierarchies with synchronized methods.
- Explain how decorators maintain thread safety when layered.

Key tip: When discussing this topic in an interview, start with the problem (fragile base class in concurrent contexts), show the inheritance-based solution and its flaws, then demonstrate the composition-based solution. This shows both understanding and the ability to refactor.

## Interview Q&A Section

**Q1: Why is extending Thread considered bad practice? What should you do instead?**

```text
A1: Extending Thread is considered bad practice for several reasons:

1. It uses up your single inheritance slot. If MyWorker extends Thread, it
   cannot extend any other class.
2. It tightly couples the task logic to the threading mechanism. The task
   cannot be reused with different execution models (thread pools, virtual
   threads, single-threaded executor).
3. It conflates "what to do" with "how to run it." The Runnable interface
   separates these concerns.
4. Thread is a heavy class with many methods (setPriority, setDaemon, etc.)
   that are irrelevant to the task logic.

Instead, implement Runnable or Callable and submit tasks to an ExecutorService.
This is composition: the task (what) is composed with the executor (how).
With Java 21 virtual threads, this becomes even more important because you
want tasks to be lightweight and executor-agnostic.
```

```java
// BAD: Inheriting from Thread
class DataProcessor extends Thread {
    private final List<String> data;
    DataProcessor(List<String> data) { this.data = data; }

    @Override
    public void run() {
        data.forEach(item -> System.out.println(item.toUpperCase()));
    }
}
new DataProcessor(data).start(); // Tied to platform threads

// GOOD: Composition with Runnable + Executor
class DataProcessor implements Runnable {
    private final List<String> data;
    DataProcessor(List<String> data) { this.data = data; }

    @Override
    public void run() {
        data.forEach(item -> System.out.println(item.toUpperCase()));
    }
}
// Can run on platform threads, virtual threads, or single-threaded executor
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(new DataProcessor(data));
}
```

**Q2: How do you add retry logic to a concurrent task without inheritance?**

```text
A2: Use the Strategy pattern via composition. Define a RetryStrategy interface
that encapsulates the retry policy (max retries, delay calculation, which
exceptions to retry). Then compose this strategy into a ResilientExecutor
that applies the retry logic around any task.

Benefits over inheritance:
- Retry strategies can be tested independently.
- Different retry strategies can be swapped at runtime or configuration time.
- The executor does not need to know the details of the retry policy.
- New retry strategies (circuit breaker, jitter, etc.) can be added without
  modifying the executor class.

This approach follows both Composition over Inheritance and the Open/Closed
Principle.
```

```java
// Strategy interface for retry behavior
interface RetryStrategy {
    boolean shouldRetry(int attempt, Exception lastException);
    long delayMillis(int attempt);
}

// Concrete strategies
class FixedRetryStrategy implements RetryStrategy {
    private final int maxRetries;
    private final long delayMs;
    // ... implementation
}

class ExponentialBackoffStrategy implements RetryStrategy {
    private final int maxRetries;
    private final long initialDelayMs;
    // ... implementation with exponential delay calculation
}

// Executor composes the strategy
class ResilientExecutor {
    private final RetryStrategy retryStrategy;

    ResilientExecutor(RetryStrategy strategy) {
        this.retryStrategy = Objects.requireNonNull(strategy);
    }

    <T> T execute(Supplier<T> task) throws Exception {
        int attempt = 0;
        Exception lastException = null;
        while (true) {
            attempt++;
            try {
                return task.get();
            } catch (Exception e) {
                lastException = e;
                if (!retryStrategy.shouldRetry(attempt, e)) throw e;
                Thread.sleep(retryStrategy.delayMillis(attempt));
            }
        }
    }
}
```

**Q3: How do decorators help avoid deep inheritance hierarchies in concurrent code?**

```text
A3: In concurrent code, requirements like logging, synchronization, retrying,
and metrics often lead to class explosion through inheritance:
  BaseTask -> LoggingTask -> SynchronizedLoggingTask -> RetryingSynchronizedLoggingTask

This hierarchy has several problems:
- Each combination requires a new class (N features = 2^N classes).
- Synchronization in parent classes can conflict with child classes.
- Testing requires the entire hierarchy.

Decorators solve this by wrapping a base implementation with single-purpose
wrappers:
  SynchronizedTaskRunner(LoggingTaskRunner(UpperCaseRunner))

Each decorator:
- Implements the same interface as the base.
- Holds a reference to the delegate (composition).
- Adds one concern (logging OR synchronization OR retrying).
- Can be applied in any combination and order.

For concurrency specifically:
- SynchronizedTaskRunner adds a lock around the delegate call.
- LoggingTaskRunner records start/end/error without affecting threading.
- The base runner focuses purely on business logic.
```

```java
// Base interface
interface TaskRunner {
    String run(String input) throws Exception;
}

// Base implementation: pure business logic
class UpperCaseRunner implements TaskRunner {
    @Override
    public String run(String input) { return input.toUpperCase(); }
}

// Decorator 1: Logging (no synchronization concern)
class LoggingTaskRunner implements TaskRunner {
    private final TaskRunner delegate;
    private final List<String> logs = new CopyOnWriteArrayList<>();

    LoggingTaskRunner(TaskRunner delegate) { this.delegate = delegate; }

    @Override
    public String run(String input) throws Exception {
        logs.add("START: " + input);
        String result = delegate.run(input);
        logs.add("SUCCESS: " + result);
        return result;
    }
}

// Decorator 2: Thread safety
class SynchronizedTaskRunner implements TaskRunner {
    private final TaskRunner delegate;
    private final ReentrantLock lock = new ReentrantLock();

    SynchronizedTaskRunner(TaskRunner delegate) { this.delegate = delegate; }

    @Override
    public String run(String input) throws Exception {
        lock.lock();
        try { return delegate.run(input); }
        finally { lock.unlock(); }
    }
}

// Compose freely:
TaskRunner runner = new SynchronizedTaskRunner(
    new LoggingTaskRunner(
        new UpperCaseRunner()
    )
);
```

**Q4: What problems arise when overriding synchronized methods in subclasses?**

```text
A4: Overriding synchronized methods in subclasses creates several problems:

1. Missing synchronization: If the subclass forgets the 'synchronized'
   keyword, the thread safety contract of the parent is silently broken.
   The subclass method runs without holding the monitor.

2. Different lock semantics: The parent may synchronize on 'this', but the
   subclass might add synchronization on a different object, creating
   potential for deadlocks when both locks are held.

3. Extended critical sections: A subclass might add additional logic inside
   the synchronized method, holding the lock longer than intended and
   reducing throughput.

4. Super calls within locks: Calling super.method() from a subclass's
   synchronized method means the lock is already held. If the super method
   also tries to acquire the same lock, Java's reentrant locking prevents
   deadlock, but it may not prevent logical errors.

5. Invisible lock ordering: When a hierarchy has multiple synchronized
   methods that call each other, the lock ordering becomes implicit and
   fragile.

Composition avoids ALL of these issues because each component manages its
own synchronization independently.
```

```java
// PROBLEM: Subclass breaks thread safety
class ThreadSafeStack<E> {
    private final List<E> items = new ArrayList<>();

    public synchronized void push(E item) { items.add(item); }
    public synchronized E pop() { return items.remove(items.size() - 1); }
    public synchronized int size() { return items.size(); }
}

// Subclass forgets synchronized -- BREAKS thread safety
class LoggingStack<E> extends ThreadSafeStack<E> {
    @Override
    public void push(E item) {  // NOT synchronized!
        System.out.println("Pushing: " + item);
        super.push(item);  // super IS synchronized, but the println is not
    }
}

// SOLUTION: Composition
class LoggingStackWrapper<E> implements Stack<E> {
    private final Stack<E> delegate;  // Can be any thread-safe Stack

    @Override
    public void push(E item) {
        System.out.println("Pushing: " + item);
        delegate.push(item);  // Thread safety is delegate's responsibility
    }
}
```

**Q5: How do composition and virtual threads (Java 21) work together?**

```text
A5: Virtual threads and composition are natural partners because:

1. Virtual threads are lightweight, so creating a virtual thread per task
   is feasible. Composed task runners (decorators) add minimal overhead
   per virtual thread.

2. Virtual threads encourage a "task as a unit of work" model. Composition
   allows building these tasks from small, reusable components (strategies,
   formatters, validators) rather than inheriting from a monolithic base task.

3. Virtual threads unmount from platform threads during blocking operations.
   Composed decorators that delegate through the chain naturally support this
   because each decorator simply calls the next, and if the innermost
   operation blocks, the virtual thread unmounts cleanly.

4. With structured concurrency (JEP 453), composed subtasks can be managed
   as a group. Each subtask is a composed pipeline that runs on its own
   virtual thread.

5. Composition avoids synchronized blocks (which pin virtual threads to
   platform threads). Instead, use ReentrantLock in decorators, which
   virtual threads handle efficiently.
```

```java
// Composed pipeline executed on virtual threads
var pipeline = new ProcessingPipeline<String>()
    .addStage(String::trim)
    .addStage(String::toUpperCase)
    .addStage(s -> s + " [processed]");

// Each item runs on its own virtual thread
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    List<Future<String>> futures = items.stream()
        .map(item -> executor.submit(() -> pipeline.execute(item)))
        .toList();

    for (var future : futures) {
        System.out.println(future.get());
    }
}

// Decorator-based runner on virtual threads
TaskRunner runner = new SynchronizedTaskRunner(
    new LoggingTaskRunner(new UpperCaseRunner())
);

try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> runner.run("hello"));
    executor.submit(() -> runner.run("world"));
}
```

## Code Examples

- Source: [CompositionInConcurrency.java](src/main/java/com/github/msorkhpar/claudejavatutor/compositioninheritance/CompositionInConcurrency.java)
- Test: [CompositionInConcurrencyTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/compositioninheritance/CompositionInConcurrencyTest.java)
