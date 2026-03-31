# 5.3.1. Data Races and How to Prevent Them

## Concept Explanation

A **data race** occurs when two or more threads access the same shared memory location concurrently, at least one of
those accesses is a write, and there is no synchronization mechanism coordinating those accesses. Data races are among
the most insidious bugs in concurrent programming because they can be non-deterministic, meaning they may appear only
occasionally and under specific timing conditions that are hard to reproduce.

**Real-world analogy**: Imagine two people simultaneously editing the same Google Doc with no awareness of each other.
One person reads the sentence, thinks about their edit, types a change — but in the meantime, the other person has
already rewritten the same paragraph. When the second person's changes are applied, the first person's earlier work is
lost. Each individual thinks they're working correctly, but the coordination is broken. A data race in software works
exactly the same way.

Under the Java Memory Model (JMM), a data race is specifically defined as:
- Two memory accesses in different threads that access the same variable
- At least one access is a write
- The accesses are not ordered by a happens-before relationship

The JMM makes no guarantees about what value a thread will read from a variable when there is a data race — the result
is essentially undefined behavior (or more precisely, the JMM guarantees only "out-of-thin-air" values for references,
but allows any value for primitives).

### Why Data Races Are Dangerous

1. **Non-determinism**: Bugs appear sporadically, depending on thread scheduling — making them hard to find.
2. **Memory visibility**: Without synchronization, a thread may see a stale value from its local cache.
3. **Instruction reordering**: Compilers and CPUs may reorder instructions for optimization — valid for single-threaded
   execution but can cause surprising results in concurrent code.
4. **Torn reads/writes**: On 64-bit platforms, a `long` or `double` field access may not be atomic on all JVMs,
   causing a read to see half of one write and half of another.

## Key Points to Remember

1. A data race requires: concurrent access + at least one write + no synchronization.
2. Data races can corrupt state even if the code looks logically correct in isolation.
3. `volatile` prevents data races on single read/write operations but not compound operations.
4. `synchronized` prevents data races by establishing mutual exclusion and memory visibility.
5. Immutable objects can never be involved in a data race (their fields never change after construction).
6. `final` fields, when safely published, are immune to data races.
7. Detecting data races: use tools like ThreadSanitizer, Helgrind, or the Java Concurrency Stress (JCStress) framework.
8. Data races on reference types may produce "out-of-thin-air" values in theory — always use synchronization.

## Relevant Java 21 Features

- **Virtual threads (Project Loom)**: Virtual threads make I/O-heavy workloads easier to scale but do NOT automatically
  prevent data races. The same synchronization rules apply.
- **Structured concurrency (JEP 453 preview)**: Encourages task hierarchies that reduce shared-state access between
  sibling tasks.
- **Sequenced collections**: Provide predictable iteration order, reducing certain race-prone patterns.
- **Record classes**: Immutable by design, making them safe for sharing across threads without synchronization for their
  fields (assuming safe publication).
- **Pattern matching for switch**: Enables more expressive code, but concurrent state management requirements remain
  unchanged.

## Common Pitfalls and How to Avoid Them

### 1. Unsynchronized counter increment

```java
// PROBLEM: data race on counter
public class UnsynchronizedCounter {
    private int counter = 0;

    public void increment() {
        counter++; // Read, increment, write — NOT atomic!
    }

    public int getCounter() {
        return counter;
    }
}
// Two threads calling increment() 1000 times each may result in counter < 2000
```

```java
// SOLUTION 1: synchronized method
public class SynchronizedCounter {
    private int counter = 0;

    public synchronized void increment() {
        counter++;
    }

    public synchronized int getCounter() {
        return counter;
    }
}

// SOLUTION 2: AtomicInteger (preferred for simple counters)
public class AtomicCounter {
    private final AtomicInteger counter = new AtomicInteger(0);

    public void increment() {
        counter.incrementAndGet();
    }

    public int getCounter() {
        return counter.get();
    }
}
```

### 2. Race on check-then-act

```java
// PROBLEM: Race condition between check and action
public class RaceOnCheckThenAct {
    private Map<String, String> cache = new HashMap<>();

    // Thread A and Thread B can both see key absent and both insert
    public String getOrCompute(String key) {
        if (!cache.containsKey(key)) {       // check
            cache.put(key, compute(key));     // act
        }
        return cache.get(key);
    }
}

// SOLUTION: ConcurrentHashMap.computeIfAbsent
public class SafeCheckThenAct {
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public String getOrCompute(String key) {
        return cache.computeIfAbsent(key, this::compute);
    }
}
```

### 3. Long and double tearing

```java
// PROBLEM: long writes may not be atomic on 32-bit JVMs
private long sharedLong = 0;

// Thread 1: writes upper 32 bits, then lower 32 bits
// Thread 2: reads in between — sees a "torn" value

// SOLUTION: use volatile for longs and doubles
private volatile long sharedLong = 0;
// Or use AtomicLong
private final AtomicLong sharedLong = new AtomicLong(0);
```

### 4. Sharing mutable collections without synchronization

```java
// PROBLEM: ArrayList is not thread-safe
private List<String> sharedList = new ArrayList<>();

// SOLUTION 1: Use CopyOnWriteArrayList for read-heavy workloads
private List<String> sharedList = new CopyOnWriteArrayList<>();

// SOLUTION 2: Collections.synchronizedList
private List<String> sharedList = Collections.synchronizedList(new ArrayList<>());

// SOLUTION 3: ConcurrentLinkedQueue for FIFO access
private Queue<String> sharedQueue = new ConcurrentLinkedQueue<>();
```

## Best Practices and Optimization Techniques

1. **Prefer immutability**: Use `final` fields and immutable value objects (records, etc.) to eliminate mutable shared state.
2. **Confine state to a single thread**: Thread-local storage (`ThreadLocal`) ensures each thread has its own copy.
3. **Use higher-level abstractions**: `java.util.concurrent` classes (`AtomicLong`, `ConcurrentHashMap`, `BlockingQueue`) are designed for concurrent use.
4. **Minimize the scope of shared mutable state**: The less data is shared, the fewer potential races.
5. **Use static analysis**: Tools like SpotBugs (FindBugs successor), Checker Framework, and IntelliJ IDEA can detect potential data races at compile time.
6. **Write tests with stress frameworks**: JCStress (`@Arbiter`, `@State`, `@Actor`) or JUnit `@RepeatedTest` with multiple threads can expose races.
7. **Document thread-safety guarantees**: Annotate classes with `@ThreadSafe`, `@NotThreadSafe`, or `@GuardedBy`.

## Edge Cases and Their Handling

### 1. Safe publication of immutable objects

Even immutable objects can be involved in a data race if they are not *safely published*. A safely published object is
one whose reference is made visible to other threads only after construction is complete.

```java
// UNSAFE: Another thread may see the reference before construction is done
private MyImmutable obj;

public void init() {
    obj = new MyImmutable(42); // May be partially constructed when another thread reads obj
}

// SAFE: volatile ensures the write to obj is visible only after the object is fully constructed
private volatile MyImmutable obj;
```

### 2. Static initializer safety

Static initializers run within a class-loading lock, so they are inherently thread-safe. Static fields initialized in
a static initializer or at declaration are safely published.

```java
// Safe — initialized in static initializer
public class SafeSingleton {
    private static final SafeSingleton INSTANCE = new SafeSingleton();

    public static SafeSingleton getInstance() {
        return INSTANCE; // Always visible, always fully constructed
    }
}
```

### 3. Null check races

```java
// PROBLEM: Double-checked locking without volatile is broken
private MyObject instance;

public MyObject getInstance() {
    if (instance == null) {                 // First check (no lock)
        synchronized (this) {
            if (instance == null) {         // Second check (with lock)
                instance = new MyObject();  // May be seen as partially constructed without volatile!
            }
        }
    }
    return instance;
}

// SOLUTION: use volatile for the field
private volatile MyObject instance;
```

## Interview-specific Insights

Interviewers often probe:

- The formal definition of a data race (concurrent, unsynchronized, at least one write)
- The difference between a **data race** and a **race condition** — they are NOT synonyms. A data race is a formal JMM
  violation; a race condition is a logical bug where outcome depends on timing (a race condition can exist without a data
  race, e.g., in properly synchronized code with TOCTOU issues).
- Whether `volatile` prevents all data races (it prevents races on single reads/writes but not compound operations)
- How immutability prevents data races
- Real-world examples of data races in production systems

Common tricky questions:
- "Can you have a race condition without a data race?" — Yes! Check-then-act under synchronized is a logical race but
  not a JMM data race.
- "Is `i++` on a volatile int atomic?" — No! volatile only ensures visibility, not atomicity of compound operations.
- "What happens if two threads write to different fields of the same object without synchronization?" — They may still
  cause data races because Java does not guarantee field-level atomicity for long/double, and compiler/CPU reordering
  can still cause issues.

## Interview Q&A Section

**Q1: What is a data race, and how does it differ from a race condition?**

```text
A1: A data race is a precise technical term defined by the Java Memory Model (JMM): it occurs when
two threads access the same variable concurrently, at least one access is a write, and the
accesses are not ordered by a happens-before relationship. The JMM makes no guarantees about the
outcome — results are undefined.

A race condition is a broader term for any situation where the correctness of a program depends on
the relative timing or interleaving of threads. A race condition can exist even in fully
synchronized code — for example, a check-then-act sequence where the state may change between
the check and the action, even if each individual operation is synchronized.

Key distinction:
- All data races are a form of race condition, but not all race conditions are data races.
- A data race = JMM violation (undefined behavior).
- A race condition = logical correctness issue that may or may not involve a JMM violation.

Example of a race condition without a data race:
  synchronized(lock) { if (!set.contains(x)) set.add(x); }
  // Each operation is synchronized, but between the check and the add, another thread may add x.
```

```java
// Data race example
class DataRaceExample {
    private int value = 0; // No synchronization

    // Thread 1 calls this
    public void writer() {
        value = 42; // Write without synchronization — data race!
    }

    // Thread 2 calls this
    public int reader() {
        return value; // Read without synchronization — data race!
    }
}

// Race condition without data race
class RaceConditionWithoutDataRace {
    private final Set<String> items = Collections.synchronizedSet(new HashSet<>());

    public void addIfAbsent(String item) {
        // Each operation is thread-safe, but the compound operation is not:
        if (!items.contains(item)) {   // Thread A checks, sees absent
            // Thread B also checks, also sees absent
            items.add(item);           // Both threads insert — logical bug!
        }
        // Fix: use ConcurrentHashMap or synchronize the entire block
    }
}
```

**Q2: How does the Java Memory Model guarantee freedom from data races?**

```text
A2: The Java Memory Model (JMM), specified in the Java Language Specification Chapter 17, defines
the conditions under which a memory access in one thread is guaranteed to be visible to another
thread. The key concept is the "happens-before" relationship.

A happens-before relationship between action A and action B means that the effects of A are
guaranteed to be visible to B. Sources of happens-before in Java include:

1. Program order: Within a single thread, each action happens-before the next one.
2. Monitor lock: An unlock on a monitor happens-before every subsequent lock on that monitor.
3. Volatile: A write to a volatile variable happens-before every subsequent read of that variable.
4. Thread start: Thread.start() happens-before any action in the started thread.
5. Thread join: All actions in a thread happen-before Thread.join() returns.
6. Object initialization: Default initialization happens-before any program action.

If two conflicting accesses (one write, any order) are NOT ordered by happens-before, they form a
data race. The JMM only guarantees safe, predictable behavior for programs that are "correctly
synchronized" — meaning they have no data races.
```

```java
// Demonstrating happens-before through volatile
class HappensBeforeDemo {
    private int data = 0;
    private volatile boolean ready = false; // Flag to establish happens-before

    public void writer() {
        data = 42;        // Action A
        ready = true;     // Action B — volatile write establishes happens-before
    }

    public void reader() {
        while (!ready) {} // Waits for volatile read
        // ready = true happens-before this volatile read
        // data = 42 happens-before ready = true (program order)
        // Therefore data = 42 happens-before this point — data is 42 here
        System.out.println(data); // Guaranteed to print 42
    }
}
```

**Q3: Why is `i++` not atomic even on a volatile variable?**

```text
A3: The `volatile` keyword guarantees two things:
1. Visibility: A write to a volatile variable is immediately visible to all threads.
2. No reordering: Reads and writes of volatile variables are not reordered with other memory operations.

However, `volatile` does NOT make compound operations like `i++` atomic. The `i++` operation
decomposes into THREE separate steps:
1. Read the current value of i
2. Increment the value
3. Write the new value back

Even with volatile, a context switch can happen between any of these steps. If Thread A reads the
value (step 1), Thread B also reads the same value (step 1), both increment (step 2), and both
write back (step 3), the final result is an increment of 1 rather than 2.

To make increments atomic, use AtomicInteger.incrementAndGet() which uses CPU-level CAS
(Compare-And-Swap) instructions to ensure atomicity without locking.
```

```java
class VolatileVsAtomic {
    private volatile int volatileCounter = 0;
    private final AtomicInteger atomicCounter = new AtomicInteger(0);

    // NOT thread-safe! Despite volatile
    public void unsafeIncrement() {
        volatileCounter++; // Read + modify + write — 3 non-atomic steps!
    }

    // Thread-safe: CAS-based atomic increment
    public void safeIncrement() {
        atomicCounter.incrementAndGet(); // Single atomic operation
    }

    // Demonstrating the problem
    public static void main(String[] args) throws InterruptedException {
        var example = new VolatileVsAtomic();
        var threads = new ArrayList<Thread>();

        for (int i = 0; i < 1000; i++) {
            threads.add(Thread.ofVirtual().start(example::unsafeIncrement));
        }
        for (var t : threads) t.join();

        // volatileCounter is likely < 1000 due to data race
        System.out.println("Volatile (likely wrong): " + example.volatileCounter);
        System.out.println("Atomic (always correct): " + example.atomicCounter.get());
    }
}
```

**Q4: How can you detect data races in a Java application?**

```text
A4: Several tools and techniques exist for detecting data races in Java:

1. ThreadSanitizer (TSan): A dynamic analysis tool available via native agent. Detects data races
   at runtime by tracking all memory accesses. Available for HotSpot JVM.

2. JCStress (Java Concurrency Stress): A specialized framework for writing correctness tests for
   concurrent code. Tests can reveal races that are very hard to reproduce with regular unit tests.

3. Helgrind / DRD: Valgrind tools that detect data races (mainly for native code, less common for JVM).

4. SpotBugs with FindSecBugs: Static analysis that can detect some classes of thread-safety bugs.

5. IntelliJ IDEA / Eclipse: IDEs can warn about common concurrent programming mistakes.

6. Code review: Manual review using annotations like @GuardedBy, @ThreadSafe from the
   jcip-annotations library makes threading requirements explicit.

7. Stress testing: Running tests with many threads and many iterations can expose races that
   probabilistic analysis would miss.

For production use, structured concurrency, immutability, and high-level concurrent primitives
(java.util.concurrent) are the best preventative measures.
```

```java
// Example of a stress test that can expose data races
class DataRaceStressTest {
    private int sharedCounter = 0;

    // Run this repeatedly with many threads to detect the race
    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.RepeatedTest(10)
    void detectRaceWithRepetition() throws InterruptedException {
        sharedCounter = 0;
        int threadCount = 100;
        int incrementsPerThread = 1000;

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            threads.add(Thread.ofVirtual().start(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    sharedCounter++; // Race!
                }
            }));
        }
        for (Thread t : threads) t.join();

        int expected = threadCount * incrementsPerThread;
        // This assertion will FAIL intermittently due to the data race
        // Org.assertj.core.api.Assertions.assertThat(sharedCounter).isEqualTo(expected);
        System.out.println("Expected: " + expected + ", Actual: " + sharedCounter);
    }
}
```

**Q5: What is the relationship between immutability and data races?**

```text
A5: Immutable objects are inherently thread-safe because their state never changes after
construction. If an object's fields cannot be written after it's constructed, there can be no
concurrent write — and without a concurrent write, there can be no data race.

Java provides two mechanisms for immutability:
1. final fields: A final field can only be assigned once (in the constructor or at declaration).
   After construction, if the object is safely published, the final field's value is guaranteed to
   be visible to all threads without any additional synchronization.
2. Records: Java record classes (Java 16+) generate final fields for all components, making them
   naturally immutable.

"Safe publication" is essential even for immutable objects: the reference to the object must be
made visible to other threads through a properly synchronized channel (volatile field, concurrent
collection, synchronized block, etc.).

Immutability is the gold standard for concurrent programming because it:
- Eliminates data races by definition
- Eliminates the need for synchronization on the object's state
- Makes reasoning about concurrent code simpler
- Enables free sharing across threads
```

```java
// Immutable class — safe for multi-threaded use without synchronization
public final class ImmutablePoint {
    private final int x;
    private final int y;

    public ImmutablePoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public ImmutablePoint translate(int dx, int dy) {
        return new ImmutablePoint(x + dx, y + dy); // Returns new instance
    }
}

// Java record — implicitly immutable
public record ImmutablePointRecord(int x, int y) {
    public ImmutablePointRecord translate(int dx, int dy) {
        return new ImmutablePointRecord(x + dx, y + dy);
    }
}

// Safe publication using volatile
class SafePublisher {
    private volatile ImmutablePoint point; // volatile ensures safe publication

    public void setPoint(ImmutablePoint p) {
        this.point = p; // Safe — volatile write
    }

    public ImmutablePoint getPoint() {
        return this.point; // Safe — volatile read
    }
}
```

**Q6: How does ThreadLocal help prevent data races?**

```text
A6: ThreadLocal provides each thread with its own independent copy of a variable, completely
eliminating shared mutable state. Since no data is shared between threads, no synchronization is
needed, and data races become impossible on ThreadLocal variables.

Use cases for ThreadLocal:
1. Per-thread state in web frameworks (Spring stores request context in ThreadLocal)
2. Database connections per thread (JDBC connection pooling)
3. Per-thread formatters (SimpleDateFormat is not thread-safe; ThreadLocal makes it safe)
4. User session data in servlet containers

However, ThreadLocal has risks:
1. Memory leaks in thread pool environments: If threads are recycled, old ThreadLocal values may
   persist. Always call ThreadLocal.remove() when done.
2. Can obscure dependencies: Passing values implicitly through ThreadLocal makes code harder to test.
3. Not suitable for parallel tasks that need to share results.
```

```java
class ThreadLocalExample {
    // Each thread gets its own SimpleDateFormat (not thread-safe if shared)
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT =
        ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    // Each thread gets its own request context
    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();

    public String formatDate(Date date) {
        return DATE_FORMAT.get().format(date); // No synchronization needed!
    }

    public void processRequest(String requestId) {
        REQUEST_ID.set(requestId);
        try {
            doWork(); // doWork can access REQUEST_ID.get() without parameters
        } finally {
            REQUEST_ID.remove(); // Critical in thread pools — prevents memory leaks!
        }
    }

    private void doWork() {
        String id = REQUEST_ID.get(); // Access the per-thread request context
        System.out.println("Processing request: " + id);
    }
}
```

## Code Examples

- Source: [DataRace.java](src/main/java/com/github/msorkhpar/claudejavatutor/concurrencypitfalls/DataRace.java)
- Test: [DataRaceTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/concurrencypitfalls/DataRaceTest.java)
