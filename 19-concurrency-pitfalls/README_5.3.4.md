# 5.3.4. Best Practices for Safe Concurrent Programming

## Concept Explanation

Writing safe concurrent code is one of the hardest challenges in software engineering. Concurrency bugs are
non-deterministic, difficult to reproduce, and often invisible during testing only to surface catastrophically in
production under load. Rather than trying to debug concurrency issues after the fact, the most effective approach is to
follow proven **best practices** that eliminate entire categories of bugs by design.

**Real-world analogy**: Think of a busy commercial kitchen. The head chef doesn't try to debug why orders are getting
mixed up after the fact -- they establish systems upfront: each station has its own cutting board (thread confinement),
recipes are printed on laminated cards that nobody modifies (immutability), orders flow through a single ticket rail
(message passing via queues), and the walk-in fridge has a one-person-at-a-time rule for inventory changes (minimal
locking). These practices don't make concurrency problems impossible, but they make them rare and localized.

### The Hierarchy of Safe Concurrency Strategies

The following strategies are listed in order of preference -- from the safest and simplest to the most error-prone:

1. **Avoid shared mutable state entirely**: If threads don't share data, there are no concurrency issues. Use message
   passing, task isolation, or functional designs.
2. **Share only immutable state**: Immutable objects (records, final fields) can be shared freely across threads without
   synchronization.
3. **Confine mutable state to a single thread**: Use `ThreadLocal` or stack confinement so that each thread has its own
   copy.
4. **Use high-level concurrent abstractions**: `ConcurrentHashMap`, `BlockingQueue`, `AtomicInteger`, etc. -- these
   classes encapsulate correct synchronization internally.
5. **Use explicit locks with minimal scope**: `ReentrantLock`, `ReadWriteLock`, `StampedLock` -- hold the lock for the
   shortest possible duration.
6. **Use `synchronized` blocks**: The simplest form of mutual exclusion, but coarse-grained and can pin virtual threads.
7. **Roll your own lock-free algorithms**: CAS loops, VarHandle -- only for experts building infrastructure libraries.

## Key Points to Remember

1. **Immutability is the gold standard**: If data cannot be changed after construction, it cannot be corrupted by
   concurrent access. Java records, `final` fields, and `Collections.unmodifiableXxx()` are your primary tools.
2. **Thread confinement eliminates sharing**: `ThreadLocal`, stack-local variables, and task-scoped data ensure no two
   threads ever access the same mutable state.
3. **High-level abstractions handle synchronization for you**: `ConcurrentHashMap.computeIfAbsent()`,
   `BlockingQueue.put()/take()`, `AtomicReference.compareAndSet()` -- use these instead of hand-rolling synchronized
   blocks.
4. **Minimize lock scope**: Perform expensive computations outside the lock. Only hold the lock for the actual state
   mutation.
5. **Prefer `ReentrantLock` over `synchronized` on virtual threads**: `synchronized` blocks can pin virtual threads to
   their carrier OS thread; `ReentrantLock` does not have this limitation (as of Java 21).
6. **Use `ReadWriteLock` for read-heavy workloads**: Multiple readers can proceed concurrently; only writers require
   exclusive access.
7. **Use `CopyOnWriteArrayList` for read-heavy lists with rare writes**: Iteration is always safe (snapshot semantics),
   but writes copy the entire array.
8. **Always clean up `ThreadLocal` in thread pool environments**: Call `remove()` in a `finally` block to prevent memory
   leaks when threads are reused.
9. **Design for testability**: Use `CountDownLatch`, `CyclicBarrier`, and `Awaitility` to write deterministic concurrent
   tests.
10. **Document thread-safety guarantees**: Use `@ThreadSafe`, `@NotThreadSafe`, `@GuardedBy` annotations from the
    jcip-annotations library.

## Relevant Java 21 Features

- **Virtual threads (JEP 444)**: Enable a thread-per-task programming model where blocking is cheap. This reduces the
  need for complex asynchronous, callback-based architectures. However, virtual threads do NOT change the JMM -- all
  synchronization rules still apply. Key consideration: prefer `ReentrantLock` over `synchronized` in hot paths because
  `synchronized` can pin the virtual thread to its carrier.
- **Structured concurrency (JEP 453, preview)**: Provides a `StructuredTaskScope` that manages the lifecycle of subtasks
  as a group. When the scope completes, all subtasks are guaranteed to have finished. This eliminates many patterns where
  shared mutable state was used to coordinate between tasks.
- **Scoped values (JEP 446, preview)**: A modern, immutable alternative to `ThreadLocal` designed for structured
  concurrency. A scoped value is set once, inherited by child tasks, and automatically cleaned up when the scope exits.
  Eliminates `ThreadLocal` memory leak risks.
- **Record classes (JEP 395)**: Immutable by default (all components are `final`). Combined with a `volatile` reference,
  records enable lock-free atomic state replacement -- create a new record for each state change and publish it via a
  volatile write.
- **Sequenced collections (JEP 431)**: Provide well-defined encounter order for collections. Not thread-safe by default,
  but useful with concurrent wrappers.

### Evolution of Concurrency Best Practices Across Java Versions

| Version  | Feature                                       | Best Practice Impact                                        |
|----------|-----------------------------------------------|-------------------------------------------------------------|
| Java 1.0 | `synchronized`, `wait()/notify()`            | Only tool available; error-prone, coarse-grained            |
| Java 5   | `java.util.concurrent` (JSR-166)             | ConcurrentHashMap, BlockingQueue, Executors, AtomicXxx -- paradigm shift |
| Java 5   | JSR-133 (revised JMM)                        | Formalized happens-before; made volatile and final reliable |
| Java 7   | Fork/Join framework                           | Divide-and-conquer parallelism                              |
| Java 8   | CompletableFuture, StampedLock, LongAdder    | Async composition, optimistic reads, high-throughput counters |
| Java 9   | VarHandle, reactive Flows                    | Fine-grained memory ordering, reactive streams              |
| Java 16  | Records                                       | Immutable value types -- safe for sharing by default        |
| Java 21  | Virtual threads, structured concurrency      | Thread-per-task model, reduced need for thread pools         |

## Common Pitfalls and How to Avoid Them

### 1. Using mutable shared state when immutability would suffice

```java
// PROBLEM: Mutable configuration object shared between threads
public class MutableConfig {
    private String host;       // Can be changed by any thread at any time
    private int port;
    private boolean ssl;

    // Getters and setters -- no synchronization!
    public void setHost(String host) { this.host = host; }
    public String getHost() { return host; }
    // ... reader may see inconsistent state (new host, old port)
}
```

```java
// SOLUTION: Immutable record + volatile reference for atomic replacement
public record Config(String host, int port, boolean ssl) {
    public Config {
        Objects.requireNonNull(host);
        if (port < 0 || port > 65535) throw new IllegalArgumentException("Invalid port");
    }
    public Config withHost(String newHost) { return new Config(newHost, port, ssl); }
}

public class ConfigManager {
    private volatile Config config; // Atomic reference swap

    public ConfigManager(Config initial) { this.config = Objects.requireNonNull(initial); }
    public void update(Config c) { this.config = Objects.requireNonNull(c); }
    public Config get() { return config; } // Always a consistent, fully constructed snapshot
}
```

### 2. Forgetting to clean up ThreadLocal in thread pools

```java
// PROBLEM: ThreadLocal leak in a thread pool
public class LeakyHandler {
    private static final ThreadLocal<UserSession> session = new ThreadLocal<>();

    public void handle(Request req) {
        session.set(loadSession(req));
        process();
        // MISSING: session.remove() -- the thread is returned to the pool with stale session data!
    }
}
```

```java
// SOLUTION: Always remove in a finally block
public class SafeHandler {
    private static final ThreadLocal<UserSession> session = new ThreadLocal<>();

    public void handle(Request req) {
        session.set(loadSession(req));
        try {
            process();
        } finally {
            session.remove(); // Critical: prevents leak and stale data on reused threads
        }
    }
}
```

### 3. Holding locks during expensive operations

```java
// PROBLEM: Lock held during expensive computation -- starves other threads
public class WideLock {
    private final Object lock = new Object();
    private final List<String> results = new ArrayList<>();

    public void processAndStore(String input) {
        synchronized (lock) {
            String result = expensiveComputation(input); // 500ms under lock!
            results.add(result);
        }
    }
}
```

```java
// SOLUTION: Compute outside the lock; only hold lock for the state mutation
public class NarrowLock {
    private final ReentrantLock lock = new ReentrantLock();
    private final List<String> results = new ArrayList<>();

    public void processAndStore(String input) {
        String result = expensiveComputation(input); // Outside the lock
        lock.lock();
        try {
            results.add(result); // Only the insertion is under the lock
        } finally {
            lock.unlock();
        }
    }
}
```

### 4. Using HashMap in concurrent contexts instead of ConcurrentHashMap

```java
// PROBLEM: HashMap is not thread-safe
public class UnsafeRegistry {
    private final Map<String, String> services = new HashMap<>();

    public void register(String name, String endpoint) {
        if (!services.containsKey(name)) { // Check-then-act race!
            services.put(name, endpoint);
        }
    }
}
```

```java
// SOLUTION: ConcurrentHashMap with atomic operations
public class SafeRegistry {
    private final ConcurrentHashMap<String, String> services = new ConcurrentHashMap<>();

    public boolean register(String name, String endpoint) {
        return services.putIfAbsent(name, endpoint) == null; // Atomic check-and-insert
    }
}
```

### 5. Using synchronized instead of ReentrantLock with virtual threads

```java
// PROBLEM: synchronized pins virtual threads to carrier threads
public class PinnedVirtualThread {
    private final Object lock = new Object();

    public void doWork() {
        synchronized (lock) {     // Pins the virtual thread!
            blockingIoOperation(); // Carrier thread is blocked, reducing scalability
        }
    }
}
```

```java
// SOLUTION: Use ReentrantLock -- does not pin virtual threads
public class UnpinnedVirtualThread {
    private final ReentrantLock lock = new ReentrantLock();

    public void doWork() {
        lock.lock();
        try {
            blockingIoOperation(); // Virtual thread can unmount from carrier
        } finally {
            lock.unlock();
        }
    }
}
```

## Best Practices and Optimization Techniques

1. **Design for immutability first**: Start with records and `final` fields. Only introduce mutability when profiling
   proves it necessary. An immutable object shared via a `volatile` reference provides atomic state replacement without
   locks.

2. **Use the producer-consumer pattern for inter-thread communication**: `BlockingQueue` decouples producers from
   consumers, handles all synchronization internally, and provides natural flow control via bounded capacity.

3. **Prefer `ConcurrentHashMap.computeIfAbsent()` over manual check-then-act**: The `computeIfAbsent` method is atomic
   -- the mapping function is called at most once per key, even under concurrent access.

4. **Use `ReadWriteLock` when reads vastly outnumber writes**: Allows unlimited concurrent readers with exclusive writer
   access. Consider `StampedLock` for even better read performance via optimistic reads.

5. **Return defensive copies from synchronized methods**: When a method returns a collection or mutable object, return a
   copy to prevent callers from modifying the internal state outside the lock.

6. **Use `CopyOnWriteArrayList` for event listener registries**: Listeners are read (iterated) frequently during event
   notification but modified rarely (registration/deregistration). `CopyOnWriteArrayList` is ideal for this pattern.

7. **Prefer task-based concurrency over thread-based**: Use `ExecutorService`, `CompletableFuture`, or structured
   concurrency instead of manually creating and managing threads. This separates the "what" (the task) from the "how"
   (the threading model).

8. **Test concurrent code with stress tests**: Use `@RepeatedTest`, `CountDownLatch` for synchronization barriers, and
   `Awaitility` for assertion polling. Run with many threads to expose races.

9. **Profile before optimizing**: Don't use complex lock-free algorithms unless profiling shows lock contention is a
   bottleneck. `synchronized` and `ReentrantLock` are correct and sufficient for most applications.

10. **Document thread-safety contracts**: Every class that is accessed by multiple threads should have a clear
    thread-safety specification: is it thread-safe, conditionally thread-safe, or not thread-safe?

## Edge Cases and Their Handling

### 1. ThreadLocal with virtual threads

Virtual threads are cheap to create and typically short-lived. Using `ThreadLocal` with virtual threads can lead to
excessive memory usage because each virtual thread gets its own copy. Prefer `ScopedValue` (preview in Java 21) for
structured concurrency, or pass context explicitly.

```java
// Acceptable: ThreadLocal with platform threads (long-lived, pooled)
private static final ThreadLocal<DateFormat> FORMAT =
    ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

// Problematic: ThreadLocal with millions of virtual threads
// Each virtual thread allocates its own DateFormat -- millions of instances!

// Better: Use ScopedValue (Java 21 preview) or pass context as a parameter
```

### 2. CopyOnWriteArrayList with frequent writes

`CopyOnWriteArrayList` copies the entire internal array on every mutation. If writes are frequent, this causes excessive
garbage and poor performance. Use it only when reads vastly outnumber writes (e.g., listener registries,
configuration lists).

```java
// Good: Read-heavy, write-rare (event listeners)
private final List<EventListener> listeners = new CopyOnWriteArrayList<>();

// Bad: Write-heavy (log buffer, metrics collection)
// Use ConcurrentLinkedQueue or a synchronized List instead
private final Queue<LogEntry> logBuffer = new ConcurrentLinkedQueue<>();
```

### 3. ReadWriteLock fairness and writer starvation

The default `ReentrantReadWriteLock` is non-fair, which can cause writer starvation under high read contention (readers
keep acquiring the read lock, preventing writers from ever acquiring the write lock).

```java
// Non-fair (default): risk of writer starvation under high read load
var rwLock = new ReentrantReadWriteLock();

// Fair: readers and writers are served in order -- prevents starvation but lower throughput
var fairRwLock = new ReentrantReadWriteLock(true);
```

### 4. Defensive copies under locks

Returning a reference to an internal collection from a synchronized method allows the caller to modify the collection
outside the lock, bypassing thread safety.

```java
// UNSAFE: Caller gets a direct reference to internal state
public synchronized List<String> getItems() {
    return items; // Caller can modify 'items' without holding the lock!
}

// SAFE: Return a snapshot (defensive copy)
public List<String> getItems() {
    lock.lock();
    try {
        return new ArrayList<>(items); // Caller gets an independent copy
    } finally {
        lock.unlock();
    }
}
```

### 5. Bounded BlockingQueue and backpressure

When using a bounded `BlockingQueue`, the producer blocks when the queue is full. This provides natural backpressure but
can cause deadlocks if the producer and consumer share the same thread pool with a limited number of threads.

```java
// RISK: If all pool threads are producers waiting on a full queue, no consumer can run
ExecutorService pool = Executors.newFixedThreadPool(4);
BlockingQueue<Task> queue = new ArrayBlockingQueue<>(10);

// SAFER: Use separate pools for producers and consumers, or use offer() with timeout
boolean offered = queue.offer(task, 1, TimeUnit.SECONDS);
if (!offered) {
    // Handle backpressure: drop, log, or apply different strategy
}
```

## Interview-specific Insights

Interviewers focus on:

- **Design-level thinking**: Can you design a system that minimizes shared mutable state? Do you reach for immutability,
  thread confinement, or message passing before reaching for locks?
- **Knowledge of `java.util.concurrent`**: Can you explain when to use `ConcurrentHashMap` vs. `Collections.synchronizedMap()`?
  When to use `BlockingQueue` vs. a shared list with locks?
- **Lock granularity**: Do you understand why minimizing lock scope improves throughput? Can you refactor a wide-locked
  method into a narrow-locked one?
- **Virtual thread awareness**: Do you know that `synchronized` pins virtual threads and that `ReentrantLock` does not?
- **Testing concurrent code**: How do you write tests that exercise concurrent behavior deterministically?
- **Trade-off analysis**: Can you articulate the trade-offs between `CopyOnWriteArrayList` (safe iteration, expensive
  writes) and `Collections.synchronizedList()` (cheap writes, unsafe iteration)?

Common tricky questions:

- "How would you make this class thread-safe?" -- Start with immutability, then confinement, then concurrent collections,
  then locks. Don't jump straight to `synchronized`.
- "What's wrong with `Collections.synchronizedMap()` + iteration?" -- The map's methods are individually synchronized,
  but iterating is not atomic. You need to synchronize on the map during iteration.
- "When would you NOT use `ConcurrentHashMap`?" -- When you need a consistent view of the entire map (e.g., size() +
  iteration as one atomic operation), or when you need a sorted concurrent map (use `ConcurrentSkipListMap`).
- "What are the risks of ThreadLocal with virtual threads?" -- Memory overhead (one copy per virtual thread, potentially
  millions), and the lifetime of virtual threads may not align with the expected lifecycle of ThreadLocal values.

## Interview Q&A Section

**Q1: What is the hierarchy of strategies for safe concurrent programming, and which should you prefer?**

```text
A1: The strategies, from most preferred to least, are:

1. No shared mutable state: The safest option. Use message passing (BlockingQueue), task
   isolation, or a purely functional design where each task receives its input and produces
   its output without side effects.

2. Shared immutable state: Use records, final fields, or unmodifiable collections. Immutable
   objects can be shared freely across threads with zero synchronization -- only the reference
   needs safe publication (e.g., via volatile).

3. Thread-confined mutable state: Use ThreadLocal or stack confinement so each thread has its
   own copy of the data. No sharing = no race conditions.

4. High-level concurrent abstractions: ConcurrentHashMap, BlockingQueue, AtomicInteger, etc.
   These classes handle synchronization internally and are thoroughly tested.

5. Explicit locks (ReentrantLock, ReadWriteLock): Use when you need fine-grained control over
   locking behavior (tryLock, timed locking, read-write separation).

6. synchronized: Simplest locking mechanism but coarse-grained. Pins virtual threads. Use for
   simple, low-contention cases.

7. Lock-free algorithms (CAS, VarHandle): Expert-level only. Very error-prone. Only justified
   for infrastructure libraries with extreme performance requirements.

The key insight interviewers look for: the higher you stay in this hierarchy, the fewer
concurrency bugs you'll encounter. Most production code should use strategies 1-4.
```

```java
// Strategy 1: No shared state -- tasks communicate via a queue
BlockingQueue<Task> queue = new ArrayBlockingQueue<>(100);

// Producer thread
queue.put(new Task("process-data"));

// Consumer thread
Task task = queue.take(); // No shared mutable state between producer and consumer

// Strategy 2: Shared immutable state via record + volatile
record AppConfig(String host, int port) {}
volatile AppConfig config = new AppConfig("localhost", 8080);

// Strategy 3: Thread-confined state via ThreadLocal
ThreadLocal<SimpleDateFormat> formatter =
    ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

// Strategy 4: High-level abstraction
ConcurrentHashMap<String, String> registry = new ConcurrentHashMap<>();
registry.putIfAbsent("service-a", "http://host:8080"); // Atomic operation
```

**Q2: Why should you prefer immutable objects for concurrent programming, and how do you implement them in Java 21?**

```text
A2: Immutable objects are the strongest tool against concurrency bugs because they eliminate the
root cause: shared mutable state. If an object's state cannot change after construction, then:

- No data race is possible (no writes to race on).
- No visibility issue is possible (final fields have JMM guarantees).
- No atomicity violation is possible (there are no compound state updates).
- No lock is needed for reading the object's state.
- The object can be freely shared, cached, and passed between threads.

In Java 21, the best way to create immutable objects is with records:

1. Records automatically generate final fields for all components.
2. The compact constructor runs validation before fields are assigned.
3. Records generate equals(), hashCode(), and toString() automatically.
4. The "with" pattern (withXxx methods) creates new instances for modifications.

For atomic state replacement, combine a record with a volatile reference:
- Create a new record instance for each state change.
- Assign it to a volatile field -- the volatile write ensures the new state is fully
  visible to all threads.
- Readers always see a consistent, fully constructed snapshot.

This pattern is lock-free, simple, and correct by construction. It's used extensively in
java.util.concurrent internals (e.g., ConcurrentHashMap uses immutable Node objects).
```

```java
// Immutable configuration record with validation
public record DatabaseConfig(String url, String user, int poolSize) {
    public DatabaseConfig {
        Objects.requireNonNull(url, "url must not be null");
        Objects.requireNonNull(user, "user must not be null");
        if (poolSize < 1) throw new IllegalArgumentException("poolSize must be >= 1");
    }

    // "With" pattern: creates a new instance, leaving the original unchanged
    public DatabaseConfig withPoolSize(int newPoolSize) {
        return new DatabaseConfig(url, user, newPoolSize);
    }
}

// Thread-safe config manager using volatile reference to immutable record
public class ConfigManager {
    private volatile DatabaseConfig config;

    public ConfigManager(DatabaseConfig initial) {
        this.config = Objects.requireNonNull(initial);
    }

    public void updateConfig(DatabaseConfig newConfig) {
        this.config = Objects.requireNonNull(newConfig); // Atomic volatile write
    }

    public DatabaseConfig getConfig() {
        return config; // Atomic volatile read -- always a consistent snapshot
    }
}
```

**Q3: When and how should you use the producer-consumer pattern with BlockingQueue?**

```text
A3: The producer-consumer pattern is ideal when:
- One or more threads produce work items and one or more threads consume them.
- You need to decouple the rate of production from the rate of consumption.
- You want natural flow control (backpressure) when consumers are slower than producers.

BlockingQueue implementations handle all synchronization internally:
- put() blocks if the queue is full (backpressure).
- take() blocks if the queue is empty (wait for work).
- offer(timeout) and poll(timeout) provide non-blocking alternatives with timeouts.

Choosing the right BlockingQueue:
- ArrayBlockingQueue: Bounded, array-backed. Best for fixed-capacity buffers. Fair ordering
  possible.
- LinkedBlockingQueue: Optionally bounded, linked-node based. Better throughput under high
  contention (separate locks for head and tail).
- PriorityBlockingQueue: Unbounded, priority-ordered. Consumers always get the highest-priority
  item.
- SynchronousQueue: Zero-capacity -- each put() blocks until a corresponding take() (direct
  handoff). Used by Executors.newCachedThreadPool().

Benefits:
- Eliminates shared mutable state between producer and consumer.
- Built-in thread safety -- no external synchronization needed.
- Natural backpressure via bounded capacity.
- Clean shutdown via poison pill pattern or Thread.interrupt().
```

```java
public class ProducerConsumerExample {
    private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(50);
    private volatile boolean running = true;

    // Producer: generates items and puts them on the queue
    public void produce() throws InterruptedException {
        int count = 0;
        while (running) {
            String item = "item-" + count++;
            queue.put(item); // Blocks if queue is full (backpressure)
        }
    }

    // Consumer: takes items from the queue and processes them
    public void consume() throws InterruptedException {
        while (running || !queue.isEmpty()) {
            String item = queue.poll(1, TimeUnit.SECONDS); // Timeout avoids infinite block
            if (item != null) {
                process(item);
            }
        }
    }

    // Graceful shutdown
    public void shutdown() {
        running = false;
    }

    private void process(String item) {
        System.out.println(Thread.currentThread().getName() + " processed: " + item);
    }
}
```

**Q4: How do ReadWriteLock and CopyOnWriteArrayList optimize for read-heavy workloads?**

```text
A4: Both ReadWriteLock and CopyOnWriteArrayList optimize for the common scenario where reads
vastly outnumber writes, but they use fundamentally different strategies:

ReadWriteLock (ReentrantReadWriteLock):
- Multiple threads can hold the read lock simultaneously -- reads proceed in parallel.
- Only one thread can hold the write lock, and it requires exclusive access (no concurrent
  readers or writers).
- Good for: caches, lookup tables, configuration stores where data changes occasionally.
- Trade-off: Writers must wait for all readers to release; readers must wait for writers.
- Variant: StampedLock (Java 8+) supports optimistic reads -- the reader doesn't acquire a
  lock at all; it just checks afterward whether a write occurred and retries if so.

CopyOnWriteArrayList:
- All mutations (add, remove, set) create a NEW internal array, copying all existing elements.
- Iterators operate on a snapshot of the array at the time the iterator was created.
- Reads never block and never throw ConcurrentModificationException.
- Good for: event listener registries, infrequently-updated lists iterated by many threads.
- Trade-off: Writes are O(n) because they copy the entire array. Not suitable for large lists
  with frequent writes.

When to use which:
- ReadWriteLock: Data is too large to copy on every write, or writes happen at moderate
  frequency. Access is key-based (maps, caches).
- CopyOnWriteArrayList: List is small, writes are rare, and safe iteration is critical (e.g.,
  event listeners, observer pattern).
```

```java
// ReadWriteLock: thread-safe cache with concurrent reads
public class RWLockCache<K, V> {
    private final Map<K, V> data = new HashMap<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public V get(K key) {
        rwLock.readLock().lock();   // Multiple readers can enter simultaneously
        try {
            return data.get(key);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public void put(K key, V value) {
        rwLock.writeLock().lock();  // Exclusive access
        try {
            data.put(key, value);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public Map<K, V> snapshot() {
        rwLock.readLock().lock();
        try {
            return new HashMap<>(data); // Defensive copy under read lock
        } finally {
            rwLock.readLock().unlock();
        }
    }
}

// CopyOnWriteArrayList: safe iteration for event listeners
public class EventBus {
    private final List<EventListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(EventListener l) { listeners.add(l); }       // Copies array
    public void removeListener(EventListener l) { listeners.remove(l); } // Copies array

    public void fireEvent(Event e) {
        // Safe: iterates over a snapshot -- no ConcurrentModificationException
        for (EventListener l : listeners) {
            l.onEvent(e);
        }
    }
}
```

**Q5: Why does `synchronized` pin virtual threads, and what should you use instead?**

```text
A5: In Java 21, virtual threads are multiplexed onto a small pool of platform (carrier) threads.
When a virtual thread blocks on an I/O operation or a ReentrantLock, it can "unmount" from its
carrier thread, freeing the carrier to run other virtual threads. This is what makes virtual
threads lightweight and scalable.

However, when a virtual thread enters a synchronized block (or a synchronized method), it
"pins" itself to the carrier thread. While pinned:
- The virtual thread cannot unmount.
- The carrier thread is blocked along with the virtual thread.
- Other virtual threads waiting for a carrier thread must wait.

This reduces the scalability advantage of virtual threads. Under high contention, pinning can
effectively turn virtual threads into platform threads in terms of resource consumption.

Why does this happen? The JVM uses monitor-based locking for synchronized blocks, which is
tightly integrated with the OS thread. Unmounting a virtual thread while holding a monitor
would require complex bookkeeping that is not yet implemented.

The solution: use ReentrantLock instead of synchronized in code paths that run on virtual
threads. ReentrantLock uses AbstractQueuedSynchronizer (AQS), which supports parking and
unparking virtual threads without pinning.

Note: This is a known limitation that the JVM team is working to address in future versions.
In the meantime, the practical advice is:
1. For new code on virtual threads: use ReentrantLock.
2. For existing code: monitor for pinning using -Djdk.tracePinnedThreads=short or full.
3. Short, non-blocking synchronized blocks are usually fine -- pinning is only problematic
   when combined with blocking operations inside the synchronized block.
```

```java
// PROBLEM: synchronized pins the virtual thread
public class PinningExample {
    private final Object lock = new Object();

    public void handleRequest() {
        synchronized (lock) {              // Virtual thread is pinned here
            String data = fetchFromDb();    // Blocking I/O while pinned -- carrier is stuck!
            processData(data);
        }
    }
}

// SOLUTION: ReentrantLock allows virtual thread to unmount
public class NonPinningExample {
    private final ReentrantLock lock = new ReentrantLock();

    public void handleRequest() {
        lock.lock();
        try {
            String data = fetchFromDb();   // Virtual thread can unmount during I/O
            processData(data);
        } finally {
            lock.unlock();
        }
    }
}

// Detecting pinning: run with -Djdk.tracePinnedThreads=short
// Output shows stack traces where virtual threads are pinned
```

**Q6: How do you minimize lock scope, and why is it important?**

```text
A6: Lock scope refers to the amount of code executed while holding a lock. Minimizing lock scope
means holding the lock for the shortest possible duration -- only for the actual state mutation,
not for any preparatory computation or I/O.

Why it matters:
1. Throughput: While one thread holds a lock, all other threads waiting for that lock are
   blocked. The longer the lock is held, the more time threads spend waiting.
2. Latency: Under contention, the time a thread waits for a lock directly impacts request
   latency.
3. Deadlock risk: The longer a lock is held, the more likely it is that the holding thread
   will need another lock -- increasing deadlock risk.
4. Virtual thread pinning: With synchronized blocks on virtual threads, a wider scope means
   longer pinning.

Technique: Compute outside, mutate inside.
1. Perform all expensive computations (parsing, network calls, serialization) BEFORE acquiring
   the lock.
2. Acquire the lock.
3. Perform only the minimal state mutation.
4. Release the lock.
5. Perform any post-processing after releasing the lock.

This pattern can improve throughput by orders of magnitude under high contention.
```

```java
// BEFORE: Wide lock scope -- entire computation under lock
public class WideLockScope {
    private final ReentrantLock lock = new ReentrantLock();
    private final List<String> results = new ArrayList<>();

    public void processAndStore(String rawInput) {
        lock.lock();
        try {
            // Expensive parsing and validation -- 100ms
            String validated = validate(rawInput);
            String transformed = transform(validated);
            String formatted = format(transformed);
            results.add(formatted);  // Only this line needs the lock!
        } finally {
            lock.unlock();
        }
    }
}

// AFTER: Minimal lock scope -- only state mutation under lock
public class NarrowLockScope {
    private final ReentrantLock lock = new ReentrantLock();
    private final List<String> results = new ArrayList<>();

    public void processAndStore(String rawInput) {
        // All computation outside the lock -- threads work in parallel
        String validated = validate(rawInput);
        String transformed = transform(validated);
        String formatted = format(transformed);

        // Only the state mutation is under the lock
        lock.lock();
        try {
            results.add(formatted);
        } finally {
            lock.unlock();
        }
    }
}
```

**Q7: How do you test concurrent code effectively?**

```text
A7: Testing concurrent code is inherently challenging because bugs are non-deterministic.
However, several techniques can expose concurrency issues with high probability:

1. Stress testing with @RepeatedTest: Run the same test many times to increase the chance
   of triggering a race condition. Use a high iteration count (50-100).

2. CountDownLatch for synchronization barriers: Use a latch to ensure all threads start
   their work simultaneously, maximizing contention:
   - Create a CountDownLatch(1) as a "start gate."
   - All threads await the latch.
   - Release the latch to start all threads at once.

3. Awaitility for asynchronous assertions: Poll a condition repeatedly with a timeout
   instead of using Thread.sleep() (which is flaky and slow).

4. Assertions on invariants: After all threads complete, verify that invariants hold
   (e.g., counter == expected, no duplicates, no lost entries).

5. CopyOnWriteArrayList for collecting per-thread results: Each thread adds its result to
   a COWAL; after all threads finish, assert on the collected results.

6. AtomicInteger for error counting: Increment an AtomicInteger in each thread when an
   unexpected condition is detected; assert it's zero at the end.

7. JCStress: For infrastructure code, use the Java Concurrency Stress framework, which
   explores thread interleavings far more aggressively than JUnit.

Best practices:
- Never use Thread.sleep() as a synchronization mechanism in tests.
- Always set timeouts on joins and awaits to prevent tests from hanging.
- Use virtual threads (Thread.ofVirtual()) for lightweight test threads.
```

```java
@RepeatedTest(10) // Run multiple times to expose races
void concurrentIncrementsMustNotLoseUpdates() throws InterruptedException {
    var counter = new AtomicInteger(0);
    int threads = 100;
    int incrementsPerThread = 1000;
    int expected = threads * incrementsPerThread;

    CountDownLatch startGate = new CountDownLatch(1);   // All threads start together
    CountDownLatch doneLatch = new CountDownLatch(threads);

    for (int i = 0; i < threads; i++) {
        Thread.ofVirtual().start(() -> {
            try {
                startGate.await(); // Wait until all threads are ready
                for (int j = 0; j < incrementsPerThread; j++) {
                    counter.incrementAndGet();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        });
    }

    startGate.countDown();  // Release all threads simultaneously
    boolean completed = doneLatch.await(10, TimeUnit.SECONDS);

    assertThat(completed).isTrue();
    assertThat(counter.get()).isEqualTo(expected);
}

// Using Awaitility for async assertions
@Test
void eventuallyConverges() {
    var flag = new AtomicBoolean(false);
    Thread.ofVirtual().start(() -> {
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        flag.set(true);
    });

    await().atMost(Duration.ofSeconds(2))
           .untilTrue(flag); // Polls until true or timeout
}
```

## Code Examples

- Source: [ConcurrencyBestPractices.java](src/main/java/com/github/msorkhpar/claudejavatutor/concurrencypitfalls/ConcurrencyBestPractices.java)
- Test: [ConcurrencyBestPracticesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/concurrencypitfalls/ConcurrencyBestPracticesTest.java)
