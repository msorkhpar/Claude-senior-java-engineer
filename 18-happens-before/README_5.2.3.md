# 5.2.3. Proper Synchronization to Ensure Memory Visibility

## Concept Explanation

Proper synchronization means applying the correct mechanism at the right scope to guarantee that all threads see a
consistent view of shared state. This section brings together the happens-before rules and the synchronization
mechanisms into actionable patterns.

**Real-world analogy**: Proper synchronization is like a well-run restaurant kitchen. Each station (thread) has its
own workspace (thread-local memory). The expediter (memory model) ensures dishes are handed off at well-defined
moments (synchronization points). Without the expediter (unsynchronized code), a waiter might pick up a half-finished
dish or miss an order entirely.

### The Four Core Problems Proper Synchronization Solves

1. **Stale reads (visibility failure)**: Thread A writes `x = 1`, but Thread B still reads `x = 0` because the
   write is stuck in a CPU cache or register. Fix: use `volatile` or `synchronized`.

2. **Data races (atomicity failure)**: Thread A reads, modifies, and writes back, while Thread B does the same
   concurrently. The two operations interleave and produce a wrong result. Fix: use `synchronized` or `AtomicXxx`.

3. **Reordering surprises**: The CPU or JIT compiler reorders instructions for performance. A write that appears
   first in source code may physically execute after another write, breaking the expected causal chain. Fix:
   use `volatile` (full memory barrier) or `VarHandle` with appropriate ordering modes.

4. **Unsafe publication**: An object reference is made visible to other threads before its constructor finishes,
   allowing readers to see partially initialized state. Fix: use `volatile` for the reference, `synchronized`,
   static initializer, or `final` fields.

### Pattern Catalog for Ensuring Memory Visibility

#### Pattern 1: Volatile Flag for Cooperative Cancellation

The most common single-variable publish pattern. A single writer sets a `volatile boolean` flag; multiple readers
poll it. No compounding, no atomicity requirement — `volatile` is exactly right.

```java
class StoppableTask implements Runnable {
    private volatile boolean running = true;

    void stop() { running = false; } // volatile write — visible immediately

    @Override
    public void run() {
        while (running) { // volatile read — always fresh
            doWork();
        }
    }
}
```

#### Pattern 2: Synchronized Counter (Check-then-Act)

When the operation is compound (read + modify + write), `volatile` is insufficient. Use `synchronized` or
`AtomicInteger`.

```java
// synchronized version
class SynchronizedCounter {
    private int count = 0;
    synchronized void increment() { count++; }
    synchronized int get() { return count; }
}

// AtomicInteger version (lock-free, often faster under contention)
class AtomicCounter {
    private final AtomicInteger count = new AtomicInteger();
    void increment() { count.incrementAndGet(); }
    int get() { return count.get(); }
}
```

#### Pattern 3: Safe Publication of Immutable Objects

An immutable object (all `final` fields, no mutation after construction) needs to be safely published exactly once.
After safe publication, readers need zero synchronization.

```java
// Producer
volatile ImmutableConfig config; // volatile for safe publication

void updateConfig(String newHost, int newPort) {
    config = new ImmutableConfig(newHost, newPort); // atomic reference replacement
}

// Consumer — no synchronization needed after reading the reference
void useConfig() {
    ImmutableConfig c = config; // one volatile read
    connect(c.host(), c.port()); // all final fields are visible
}
```

#### Pattern 4: CountDownLatch / CyclicBarrier for Staged Initialization

When multiple threads must complete setup before any consumer reads the results, use a `CountDownLatch`. The
`await()` call provides happens-before from all `countDown()` calls.

```java
CountDownLatch ready = new CountDownLatch(1);
int[] result = {0};

Thread producer = new Thread(() -> {
    result[0] = compute(); // writes result
    ready.countDown();     // happens-before ready.await() returns
});

Thread consumer = new Thread(() -> {
    ready.await();         // blocks until countdown; then all producer writes visible
    System.out.println(result[0]); // guaranteed to see computed result
});
```

#### Pattern 5: Using `java.util.concurrent` Collections

`ConcurrentHashMap`, `CopyOnWriteArrayList`, and `LinkedBlockingQueue` all have well-defined happens-before
guarantees documented in the java.util.concurrent package javadoc. Using these eliminates the need to write your
own synchronization for data structures.

### Choosing the Right Mechanism

| Requirement | Mechanism |
|---|---|
| Single variable, one writer, multiple readers | `volatile` |
| Counter: concurrent increments/decrements | `AtomicInteger` / `AtomicLong` |
| Compound check-then-act on multiple variables | `synchronized` block |
| Object construction safety | `final` fields + safe publication |
| Read-heavy, write-rare map/set/list | `CopyOnWriteArrayList`, `ConcurrentHashMap` |
| Producer-consumer queue | `LinkedBlockingQueue`, `ArrayBlockingQueue` |
| Staged multi-thread completion | `CountDownLatch`, `CyclicBarrier` |
| Async results | `CompletableFuture`, `Future` |
| Lock-free custom algorithms | `VarHandle` with RELEASE/ACQUIRE |

## Key Points to Remember

1. **Default state is no visibility guarantee** — all shared mutable state needs explicit synchronization.
2. **Use the weakest synchronization that is correct**: `volatile` < `AtomicXxx` < `synchronized` < `ReentrantLock`.
3. **Every shared mutable variable must be consistently protected by the same mechanism/lock**.
4. **`java.util.concurrent` abstractions are preferred over rolling your own** — they are correct, tested, and optimized.
5. **Thread confinement (no sharing) is the strongest safety guarantee** — if state is never shared, it needs no sync.
6. **Stack confinement** (local variables inside a method, not passed anywhere) is always thread-safe.
7. **Immutable shared state** needs only safe publication — once published, reads are free.
8. **Stateless classes** (no instance fields) are always thread-safe.

## Relevant Java 21 Features

- **Java 5+**: `java.util.concurrent` package — the gold standard for concurrent programming in Java.
- **Java 8**: `CompletableFuture` — provides happens-before through `thenApply`, `thenCompose`, etc.
- **Java 9**: `VarHandle` for fine-grained memory ordering in lock-free algorithms.
- **Java 16+**: `record` types are naturally immutable (all fields are `final` and `private`) — ideal for safe publication.
- **Java 21 (Structured Concurrency, JEP 453)**: `StructuredTaskScope` provides happens-before from all child task
  completions to the joiner, making safe multi-threaded result aggregation straightforward.
- **Java 21 (Virtual Threads, JEP 444)**: Prefer `ReentrantLock` over `synchronized` in virtual-thread code to
  avoid pinning the carrier thread.

## Common Pitfalls and How to Avoid Them

1. **Partial synchronization** — Protecting writes but not reads (or vice versa) creates a data race.

   ```java
   // Broken: write is synchronized, read is not
   class PartialSync {
       private int value;
       synchronized void write(int v) { value = v; }
       int read() { return value; } // unsynchronized — data race!
   }
   ```

   ```java
   // Fix: synchronize all accesses consistently
   class FullSync {
       private int value;
       synchronized void write(int v) { value = v; }
       synchronized int read() { return value; } // same lock, full visibility
   }
   ```

2. **Using thread-unsafe collections for shared state** — `ArrayList`, `HashMap`, `HashSet` are NOT thread-safe.

   ```java
   // Broken: ArrayList shared across threads without synchronization
   List<String> shared = new ArrayList<>();
   executor.submit(() -> shared.add("item")); // may corrupt internal array
   ```

   ```java
   // Fix: use thread-safe alternatives
   List<String> safe = new CopyOnWriteArrayList<>();           // or
   List<String> synced = Collections.synchronizedList(new ArrayList<>()); // or
   BlockingQueue<String> queue = new LinkedBlockingQueue<>();
   ```

3. **Locking but on different objects** — see 5.2.2 pitfall #1.

4. **Forgetting to declare volatile for publication** — A reference stored without synchronization may be seen
   partially (or not at all) by other threads.

   ```java
   // Broken: reference publication without happens-before
   class BrokenPublisher {
       MyObject obj;  // not volatile
       void init() { obj = new MyObject(); } // Thread A
       void use()  { obj.doWork(); }          // Thread B may see null or uninitialized
   }
   ```

   ```java
   // Fix: volatile for the reference
   class SafePublisher {
       volatile MyObject obj;
       void init() { obj = new MyObject(); }  // volatile write
       void use()  { MyObject o = obj; if (o != null) o.doWork(); } // volatile read
   }
   ```

5. **Spinning on non-volatile flags** — Without `volatile`, the JIT may hoist the flag read out of the loop,
   creating an infinite loop even after the flag is set by another thread.

   ```java
   // Broken: JIT may optimize away the loop condition check
   class BrokenStop {
       boolean stop = false; // NOT volatile
       void run() { while (!stop) { } } // JIT may compile to: if (!stop) while(true);
       void stop() { stop = true; }
   }
   ```

   ```java
   // Fix: volatile prevents hoisting
   class CorrectStop {
       volatile boolean stop = false;
       void run() { while (!stop) { } } // re-reads stop every iteration
       void stop() { stop = true; }
   }
   ```

## Best Practices and Optimization Techniques

1. **Thread confinement first**: Design components so that mutable state is owned by a single thread. Use message
   passing or immutable data to communicate between threads.

2. **Prefer immutability**: Once initialized, immutable objects can be freely shared. Use `record` types, final
   classes, and builder patterns to construct immutable objects.

3. **Use `java.util.concurrent` building blocks**: `CountDownLatch`, `CyclicBarrier`, `Phaser`, `Semaphore`,
   `CompletableFuture`, and concurrent collections are well-tested and handle all happens-before correctly.

4. **Minimize lock scope**: Hold locks for the shortest time possible. Move non-critical computation outside the
   synchronized block.

5. **Lock ordering to prevent deadlocks**: Always acquire multiple locks in a consistent order (e.g., alphabetical
   by lock name). Alternatively, use `tryLock()` with a timeout.

6. **Prefer `ReentrantReadWriteLock` for read-heavy workloads**: Multiple readers can hold the read lock
   simultaneously; writers get exclusive access. Dramatically reduces contention vs `synchronized`.

7. **Test concurrent code**: Use `CountDownLatch`/`CyclicBarrier` to synchronize test threads to a start line,
   then verify results. Use stress-testing tools like JCStress or consciously run tests with many threads.

## Edge Cases and Their Handling

1. **Interrupted threads and synchronization**: When a thread waiting on `wait()`, `sleep()`, `join()`, or a lock
   is interrupted, it throws `InterruptedException`. Always restore the interrupt flag or re-throw.

2. **AtomicReference.compareAndSet**: CAS (Compare-And-Set) operations in `AtomicReference` behave as volatile
   reads and writes — they establish happens-before.

3. **ThreadLocal — no sharing needed**: `ThreadLocal` gives each thread its own copy of a variable. No
   synchronization needed if the value is never shared. Perfect for per-thread contexts (e.g., database connections).

4. **Fork/Join task stealing**: When `ForkJoinTask` B steals work from A, the JMM guarantees that A's writes before
   the fork are visible to B. B's writes are visible to A after `join()`.

5. **CompletableFuture composition**: Each stage in a `CompletableFuture` chain establishes happens-before from the
   completion of one stage to the start of the next. Results flow safely through the chain.

## Interview-specific Insights

Interviewers look for:
- Ability to identify the correct synchronization mechanism for a given scenario.
- Knowledge of what `java.util.concurrent` offers and why to prefer it over raw `synchronized`.
- Understanding of thread confinement, immutability, and safe publication as alternatives to runtime locking.
- Ability to reason about and fix common visibility bugs (non-volatile flags, partial sync, etc.).
- Knowledge of deadlock causes and how lock ordering prevents them.

**Common tricky questions**:
- "How do you stop a thread safely?" (Volatile flag or interrupt protocol — never `Thread.stop()`.)
- "Is `Collections.synchronizedList` completely thread-safe?" (For single operations yes; compound operations
  like iterate-then-modify still need external synchronization.)
- "What is the difference between `ConcurrentHashMap` and `Collections.synchronizedMap`?" (`ConcurrentHashMap`
  uses fine-grained locking/CAS, allowing concurrent reads and segmented writes; `synchronizedMap` wraps every
  method in `synchronized(this)` — one lock for all.)
- "When would you use `ReadWriteLock`?" (When reads are frequent and writes are rare — read lock allows concurrent
  readers; write lock is exclusive.)

## Interview Q&A Section

**Q1: How do you safely stop a thread in Java?**

```text
A1: The correct way to stop a thread is cooperative cancellation:
1. Use a volatile boolean flag — the thread checks it periodically and exits when set.
2. Use Thread.interrupt() — the thread checks isInterrupted() or catches InterruptedException.

NEVER use Thread.stop() — it was deprecated in Java 1.1 because it releases all monitors held
by the thread, potentially leaving objects in inconsistent state.

The volatile flag approach:
- Works for threads in a computation loop.
- Must be volatile to prevent JIT from caching the flag value.

The interrupt approach:
- Also wakes threads blocked in wait(), sleep(), or blocking I/O.
- The thread must cooperate: check isInterrupted() in loops, or catch InterruptedException.
- Best practice: restore the interrupt flag with Thread.currentThread().interrupt() if you
  catch InterruptedException but cannot propagate it.
```

```java
// Approach 1: volatile flag
class StoppableWorker implements Runnable {
    private volatile boolean cancelled = false;

    public void cancel() { cancelled = true; }

    @Override
    public void run() {
        while (!cancelled) { // re-reads volatile every iteration
            processNextItem();
        }
        cleanup();
    }
}

// Approach 2: interrupt protocol (handles blocking operations)
class InterruptibleWorker implements Runnable {
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                doWork();
                Thread.sleep(100); // throws InterruptedException if interrupted
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // restore interrupt status
        } finally {
            cleanup();
        }
    }
}

// Usage
Thread worker = new Thread(new InterruptibleWorker());
worker.start();
// ... later ...
worker.interrupt(); // signal cancellation
worker.join();      // wait for clean exit
```

---

**Q2: What is thread confinement and when should you use it?**

```text
A2: Thread confinement means restricting access to mutable state so that only ONE thread ever
accesses it at a time. No sharing means no synchronization needed.

Three forms of thread confinement:
1. Ad-hoc confinement: The programmer ensures by convention that only one thread accesses the
   state. Fragile — relies on discipline, not enforced by the language.

2. Stack confinement: Local variables inside a method are on the thread's stack and cannot be
   accessed by other threads (unless you pass references elsewhere). This is the safest form.

3. ThreadLocal<T>: Each thread gets its own independent copy of the variable. Used for
   per-thread resources like database connections, SimpleDateFormat instances, or request contexts.

When to use:
- When the state is naturally per-thread (e.g., a JDBC connection, a random number generator seed).
- When you want to avoid synchronization overhead entirely.
- When implementing thread pools where each task has its own state.

Limitations:
- ThreadLocal values are tied to thread lifetime; in thread pools, they persist across tasks
  unless explicitly cleared (threadLocal.remove()).
- Can cause memory leaks if ThreadLocal references are held in ClassLoader hierarchies.
```

```java
// Stack confinement — naturally safe
public List<String> processItems(List<String> input) {
    List<String> result = new ArrayList<>(); // stack-confined (local variable)
    for (String item : input) {
        result.add(item.trim().toUpperCase()); // no synchronization needed
    }
    return result; // escapes here, but as a local reference, not shared
}

// ThreadLocal — per-thread copy
class DateParser {
    // SimpleDateFormat is NOT thread-safe; ThreadLocal gives each thread its own copy
    private static final ThreadLocal<SimpleDateFormat> FORMAT =
        ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    public Date parse(String date) throws ParseException {
        return FORMAT.get().parse(date); // each thread uses its own formatter
    }

    // IMPORTANT: in thread pools, remove when done to prevent leaks
    public void cleanup() { FORMAT.remove(); }
}

// In a web framework context (Servlet per-request context)
class RequestContext {
    private static final ThreadLocal<String> currentUserId = new ThreadLocal<>();

    public static void setUserId(String id) { currentUserId.set(id); }
    public static String getUserId() { return currentUserId.get(); }
    public static void clear() { currentUserId.remove(); } // call in finally block
}
```

---

**Q3: What is the difference between ConcurrentHashMap and Collections.synchronizedMap?**

```text
A3: Both provide thread-safe Map implementations, but they differ significantly in performance
and granularity.

Collections.synchronizedMap(map):
- Wraps every method in synchronized(this) — one big lock for the entire map.
- All concurrent operations are serialized — one thread at a time.
- Simple iteration (entrySet().iterator()) is NOT thread-safe — you must synchronize externally.
- Better when you need to iterate or perform compound operations under a single lock.

ConcurrentHashMap:
- Uses fine-grained locking (Java 7: 16 segments; Java 8+: CAS + per-bucket synchronization).
- Multiple threads can read and write to different buckets concurrently.
- Reads (get) are non-blocking — no locking at all.
- Provides atomic compound operations: putIfAbsent, computeIfAbsent, merge, compute.
- Iteration (entrySet().iterator()) is weakly consistent — sees state at some point during iteration, no ConcurrentModificationException.
- Higher throughput under concurrent access.

Rule of thumb: Prefer ConcurrentHashMap for high-concurrency scenarios. Use synchronizedMap
only when you need to wrap an existing non-thread-safe map or need external locking for
compound operations involving the map.
```

```java
// synchronizedMap: external lock required for compound operations
Map<String, Integer> syncMap = Collections.synchronizedMap(new HashMap<>());

// Compound check-then-act: must synchronize externally
synchronized (syncMap) { // MUST synchronize manually for compound ops
    if (!syncMap.containsKey("key")) {
        syncMap.put("key", 1);
    }
}

// Iteration: must synchronize on the map itself
synchronized (syncMap) {
    for (Map.Entry<String, Integer> entry : syncMap.entrySet()) {
        System.out.println(entry.getKey() + "=" + entry.getValue());
    }
}

// ConcurrentHashMap: atomic compound operations built in
Map<String, Integer> concMap = new ConcurrentHashMap<>();

// Atomic putIfAbsent — no external lock needed
concMap.putIfAbsent("key", 1);

// Atomic computeIfAbsent — create value only if key absent
concMap.computeIfAbsent("counter", k -> 0);

// Atomic merge — combine existing and new value
concMap.merge("count", 1, Integer::sum); // increment atomically

// Weakly-consistent iteration — no ConcurrentModificationException
for (Map.Entry<String, Integer> entry : concMap.entrySet()) {
    System.out.println(entry.getKey() + "=" + entry.getValue());
}
```

---

**Q4: How do you use CountDownLatch and CyclicBarrier to coordinate threads?**

```text
A4: Both are synchronization utilities in java.util.concurrent that allow threads to wait for
each other at specific points. They differ in reusability and direction of waiting.

CountDownLatch:
- One-shot barrier: initialized with a count; decremented by countDown(), awaited by await().
- The latch CANNOT be reset; use for one-time events (initialization complete, signal to start).
- Happens-before: all countDown() calls hb await() returning.
- Use cases: start signal (count=1, start when latch opens), wait for N tasks to complete.

CyclicBarrier:
- Reusable: automatically resets after all parties reach the barrier.
- All parties call await(); the last arrival triggers the optional barrier action.
- Happens-before: each barrier action hb the start of the next phase.
- Use cases: iterative algorithms where all threads must complete phase N before starting N+1.
```

```java
// CountDownLatch: parallel task initialization then start
class ParallelInit {
    void run() throws InterruptedException {
        int workers = 4;
        CountDownLatch initDone = new CountDownLatch(workers); // count = 4
        CountDownLatch startSignal = new CountDownLatch(1);    // count = 1

        for (int i = 0; i < workers; i++) {
            new Thread(() -> {
                try {
                    initializeWorker();      // do setup
                    initDone.countDown();    // signal: I am ready
                    startSignal.await();     // wait for start signal
                    doWork();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        initDone.await();        // wait until all workers are initialized
        System.out.println("All workers ready, starting...");
        startSignal.countDown(); // release all workers simultaneously
    }
}

// CyclicBarrier: phased computation
class PhasedComputation {
    private final CyclicBarrier barrier;
    private final int[][] data;

    PhasedComputation(int threads, int[][] data) {
        this.data = data;
        this.barrier = new CyclicBarrier(threads, () -> {
            // Barrier action: runs once all threads arrive, before next phase
            System.out.println("Phase complete, starting next...");
        });
    }

    void runPhase(int threadId) throws Exception {
        for (int phase = 0; phase < 3; phase++) {
            processPartition(threadId, phase); // do phase N work
            barrier.await(); // wait for all threads to finish phase N
            // barrier action has run; all writes from phase N are visible
            // safe to read other threads' phase N results in phase N+1
        }
    }
}
```

---

**Q5: What is the safe publication of objects and why is it important?**

```text
A5: Safe publication means making an object reference visible to other threads in a way that
guarantees they see the object's complete, fully initialized state.

Without safe publication, the JIT compiler and CPU can reorder:
1. The writes to the object's fields (inside the constructor).
2. The write of the reference to the shared variable.

A reading thread may see the reference (non-null) before the fields are initialized,
causing it to observe partially initialized state — a very subtle and hard-to-reproduce bug.

The four safe publication mechanisms:
1. Static initializer: class loading lock guarantees initialization hb first access.
2. volatile field for the reference: volatile write of reference hb volatile reads.
3. final field: constructor writes to final fields hb any subsequent reads.
4. synchronized: monitor lock rule ensures visibility.

Unsafely published objects: using a plain (non-volatile, non-final, non-static, non-synchronized)
field to share an object reference between threads. The reading thread may see null or a
partially initialized object.

Best practice: prefer immutable objects (all final fields) + safe publication of the reference.
Once published, they require ZERO synchronization on the read path.
```

```java
// Unsafe publication — data race on 'obj' reference
class UnsafePublisher {
    Object obj;                         // plain field, no synchronization

    void publish() { obj = new Object(); }  // Thread A writes
    void consume() { obj.hashCode(); }      // Thread B reads — MAY SEE null!
}

// Safe publication pattern 1: volatile reference
class SafePublisherVolatile {
    volatile Object obj;                     // volatile reference

    void publish() { obj = new Object(); }   // volatile write: hb any subsequent read
    void consume() {
        Object ref = obj;                    // volatile read
        if (ref != null) ref.hashCode();     // safe — sees initialized object
    }
}

// Safe publication pattern 2: synchronized
class SafePublisherSynchronized {
    Object obj;

    synchronized void publish() { obj = new Object(); } // lock hb subsequent lock
    synchronized void consume() { if (obj != null) obj.hashCode(); }
}

// Safe publication pattern 3: final + immutable + static
class SafePublisherStatic {
    // Static initializer: class loading lock ensures this is safe
    private static final Object SHARED = new Object();

    static Object get() { return SHARED; } // always safely initialized
}

// Safe publication pattern 4: final field (best for immutable objects)
final class SafeImmutable {
    final String name;
    final int value;

    SafeImmutable(String name, int value) {
        this.name = name;
        this.value = value;
    }
    // Once reference is safely published (via any of patterns 1-3),
    // name and value are always visible without additional synchronization.
}
```

---

**Q6: How do you reason about thread safety of a class from first principles?**

```text
A6: A systematic framework for analyzing thread safety:

Step 1 — Identify shared mutable state.
  - Instance fields accessed from multiple threads.
  - Static fields. Mutable objects reachable from those fields.
  - Exclude: local variables (stack-confined), ThreadLocal, immutable fields.

Step 2 — Identify the invariants that must hold.
  - E.g., for a BoundedList: size >= 0 && size <= capacity.
  - Invariants that span multiple fields need atomic enforcement.

Step 3 — Check that every access to shared mutable state is properly synchronized.
  - Every read AND write must be protected by the same lock (or volatile, Atomic, etc.).
  - Partial protection (sync writes, unsync reads) still creates data races.

Step 4 — Check for compound actions that need atomicity.
  - check-then-act (if(x==null) x = new Foo())
  - read-modify-write (x++)
  - These require a single synchronized block, not separate synchronized methods.

Step 5 — Check for safe publication.
  - How does the object reference reach other threads?
  - Is it via volatile field, static init, synchronized, or final field?

Step 6 — Check for liveness issues.
  - Could deadlock occur (two threads waiting for each other's locks)?
  - Could starvation occur (a thread never gets the lock)?
  - Could livelock occur (threads keep retrying but make no progress)?
```

```java
// Full example — analyzing and fixing a thread-safety issue
// BROKEN: not thread-safe
class UnsafeStack<T> {
    private Object[] elements;   // Step 1: shared mutable state
    private int size = 0;        // Step 1: shared mutable state

    @SuppressWarnings("unchecked")
    T pop() {
        // Step 4: compound action — check size THEN access element
        // Without atomicity: another thread may pop between our check and access
        if (size == 0) throw new EmptyStackException();
        T result = (T) elements[--size]; // read-modify-write on size
        elements[size] = null;
        return result;
    }

    void push(T item) {
        ensureCapacity();
        elements[size++] = item; // read-modify-write on size
    }
}

// FIXED: synchronized methods ensure atomicity + visibility
class SafeStack<T> {
    private Object[] elements = new Object[16];
    private int size = 0;

    @SuppressWarnings("unchecked")
    synchronized T pop() {
        if (size == 0) throw new EmptyStackException();
        T result = (T) elements[--size];
        elements[size] = null;
        return result;
    }

    synchronized void push(T item) {
        ensureCapacity();
        elements[size++] = item;
    }

    synchronized int size() { return size; }

    private void ensureCapacity() {
        if (elements.length == size)
            elements = Arrays.copyOf(elements, size * 2);
    }
}
```

## Code Examples

- Source: [MemoryVisibility.java](src/main/java/com/github/msorkhpar/claudejavatutor/happensbefore/MemoryVisibility.java)
- Test: [MemoryVisibilityTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/happensbefore/MemoryVisibilityTest.java)
