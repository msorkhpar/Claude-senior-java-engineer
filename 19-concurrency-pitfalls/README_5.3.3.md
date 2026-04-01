# 5.3.3. Visibility Issues and Proper Synchronization Techniques

## Concept Explanation

A **visibility issue** occurs when one thread writes a value to a shared variable but another thread continues to see a
stale (outdated) value because there is no happens-before relationship between the write and the subsequent read. Under
the Java Memory Model (JMM), without explicit synchronization, the JIT compiler and CPU caches are free to optimize in
ways that prevent updates from being propagated across threads.

**Real-world analogy**: Imagine a team of accountants in different offices, each with a personal whiteboard that mirrors
a central ledger. When accountant A updates the central ledger, accountants B and C keep working from their personal
whiteboards -- they never know the ledger changed because nobody told them to refresh. A visibility issue is exactly
this: one thread updates a variable in main memory, but the other thread's CPU cache still holds the old value, and
without a synchronization action (the equivalent of "refresh your whiteboard"), the reading thread may never see the
update.

### Why Visibility Issues Are Dangerous

1. **Infinite loops**: A thread checking a boolean flag may loop forever because the JIT compiler "hoists" the read out
   of the loop, caching the value in a register. The flag change by another thread is never observed.
2. **Partially constructed objects**: Without proper publication, a thread may see a non-null reference to an object
   whose fields have not been initialized yet (due to instruction reordering).
3. **Stale data**: A thread may compute results based on an outdated value, leading to silently incorrect output that
   passes superficial testing.
4. **Non-reproducibility**: Visibility bugs depend on JIT compilation thresholds, CPU architecture, OS thread scheduling,
   and JVM version. A program may work correctly in interpreted mode and fail in compiled mode (or vice versa).

### How the JMM Addresses Visibility

The Java Memory Model defines **happens-before** relationships that guarantee visibility:

| Mechanism            | Visibility Guarantee                                                                                  |
|----------------------|-------------------------------------------------------------------------------------------------------|
| `volatile` write/read | A write to a volatile variable happens-before every subsequent read of that variable by any thread.   |
| `synchronized` exit/entry | An unlock on a monitor happens-before every subsequent lock on the same monitor.                  |
| `Thread.start()`    | The call to `start()` happens-before any action in the started thread.                                |
| `Thread.join()`     | All actions in a thread happen-before `join()` returns in the calling thread.                         |
| `final` fields       | A final field written in a constructor is visible to all threads after the constructor completes (if safely published). |

## Key Points to Remember

1. Without synchronization, a thread may **never** see a write performed by another thread -- this is not just a matter
   of timing; the JIT may permanently cache the value.
2. `volatile` guarantees visibility of the most recent write for a **single variable** and prevents instruction
   reordering around volatile accesses, but does NOT provide atomicity for compound operations like `i++`.
3. `synchronized` provides **both** mutual exclusion (atomicity) **and** memory visibility. When a thread exits a
   synchronized block, all writes it performed become visible to the next thread entering a synchronized block on the
   same monitor.
4. Piggybacking visibility: writing non-volatile data before a volatile write makes all those writes visible to a thread
   that subsequently reads the volatile variable (volatile "publishes" the preceding writes).
5. Double-checked locking is **broken** without `volatile` on the instance field -- instruction reordering can expose a
   partially constructed object.
6. The Initialization-on-Demand Holder idiom leverages JLS class-loading guarantees for safe, lazy, synchronization-free
   singleton initialization.
7. `final` fields are safely visible to all threads after construction completes -- provided the `this` reference does
   not escape during construction.
8. Java records implicitly declare `final` fields, making them naturally safe for cross-thread sharing (assuming safe
   publication of the record reference itself).

## Relevant Java 21 Features

- **Virtual threads (Project Loom, JEP 444)**: Virtual threads do not change visibility rules. The same JMM
  guarantees apply. However, virtual threads make blocking synchronization (`synchronized`, `ReentrantLock`) much
  cheaper in terms of system resources, since a blocked virtual thread does not consume an OS thread. Note: as of
  Java 21, `synchronized` blocks can pin virtual threads to their carrier; prefer `ReentrantLock` in hot paths on
  virtual threads.
- **Structured concurrency (JEP 453, preview)**: Encourages hierarchical task management where child tasks complete
  before the parent scope exits. This naturally limits the scope of shared mutable state, reducing visibility pitfalls.
- **Record classes (JEP 395)**: Records are immutable by default (all components are `final`), making them excellent
  candidates for safely shared cross-thread data -- the reference just needs to be safely published (e.g., via
  `volatile`).
- **Scoped values (JEP 446, preview)**: Intended as a modern, immutable replacement for `ThreadLocal` in structured
  concurrency contexts, eliminating the mutable-sharing aspect entirely.
- **Sequenced collections (JEP 431)**: Provide well-defined encounter order, but do not change thread-safety guarantees.
  Concurrent access still requires proper synchronization.

### Evolution of Visibility Mechanisms Across Java Versions

| Version  | Feature                                       | Impact on Visibility                                       |
|----------|-----------------------------------------------|------------------------------------------------------------|
| Java 1.0 | `synchronized`, `volatile` (weak semantics)  | Original visibility model; volatile had limited guarantees |
| Java 5   | JSR-133 (revised JMM)                        | Strengthened volatile to prevent reordering; defined happens-before |
| Java 5   | `java.util.concurrent` package               | `AtomicInteger`, `ConcurrentHashMap`, locks with built-in visibility |
| Java 8   | `StampedLock`                                | Optimistic reads without full lock overhead                |
| Java 9   | `VarHandle`                                  | Fine-grained memory ordering modes (plain, opaque, acquire/release, volatile) |
| Java 16  | Records                                       | Immutable value types with final fields -- inherently safe |
| Java 21  | Virtual threads, Scoped values (preview)     | Cheaper blocking, modern ThreadLocal alternative           |

## Common Pitfalls and How to Avoid Them

### 1. Non-volatile boolean flag (JIT hoisting)

```java
// PROBLEM: The JIT compiler may hoist the read of 'stop' out of the loop,
// causing the worker thread to spin forever even after another thread sets stop = true.
public class BrokenFlag {
    private boolean stop = false; // NOT volatile!

    public void run() {
        while (!stop) { // JIT may compile this to: if (!stop) while(true) {}
            // ... work ...
        }
    }

    public void requestStop() {
        stop = true; // Another thread sets this, but the worker never sees it
    }
}
```

```java
// SOLUTION: Make the flag volatile
public class FixedFlag {
    private volatile boolean stop = false;

    public void run() {
        while (!stop) { // Volatile read on every iteration -- always sees latest value
            // ... work ...
        }
    }

    public void requestStop() {
        stop = true; // Volatile write -- visible to all threads immediately
    }
}
```

### 2. Volatile does NOT provide atomicity for compound operations

```java
// PROBLEM: volatile int with i++ is still racy
public class VolatileCounterBug {
    private volatile int count = 0;

    public void increment() {
        count++; // Read (volatile), add 1, write (volatile) -- 3 non-atomic steps!
    }
    // Two threads each calling increment() 1000x may yield count < 2000
}
```

```java
// SOLUTION: Use AtomicInteger or synchronized
import java.util.concurrent.atomic.AtomicInteger;

public class SafeCounter {
    private final AtomicInteger count = new AtomicInteger(0);

    public void increment() {
        count.incrementAndGet(); // Single atomic CAS operation
    }

    public int getCount() {
        return count.get();
    }
}
```

### 3. Broken double-checked locking (missing volatile)

```java
// PROBLEM: Without volatile, another thread may see a non-null reference
// to a partially constructed object due to instruction reordering.
public class BrokenSingleton {
    private static BrokenSingleton instance; // NOT volatile!

    public static BrokenSingleton getInstance() {
        if (instance == null) {              // First check (no lock)
            synchronized (BrokenSingleton.class) {
                if (instance == null) {      // Second check (with lock)
                    instance = new BrokenSingleton(); // Reordering: reference assigned before constructor finishes!
                }
            }
        }
        return instance; // Another thread may see a half-constructed object
    }
}
```

```java
// SOLUTION: Declare instance as volatile
public class CorrectSingleton {
    private static volatile CorrectSingleton instance;

    public static CorrectSingleton getInstance() {
        CorrectSingleton result = instance; // Local variable avoids double volatile read
        if (result == null) {
            synchronized (CorrectSingleton.class) {
                result = instance;
                if (result == null) {
                    result = new CorrectSingleton();
                    instance = result; // Volatile write ensures full construction before publication
                }
            }
        }
        return result;
    }
}
```

### 4. Unsafe publication of mutable objects

```java
// PROBLEM: Publishing an object reference without synchronization.
// The reader may see the reference as non-null but fields as default values.
public class UnsafePublication {
    private Holder holder; // NOT volatile

    public void initialize() {
        holder = new Holder(42); // Object may be partially visible to other threads
    }

    public Holder getHolder() {
        return holder; // Reader may see holder != null but holder.value == 0
    }
}
```

```java
// SOLUTION: Use volatile for the reference, or use final fields in the published object.
public class SafePublication {
    private volatile Holder holder;

    public void initialize() {
        holder = new Holder(42); // Volatile write ensures full construction is visible
    }

    public Holder getHolder() {
        return holder; // Volatile read -- if non-null, all fields are fully initialized
    }
}
```

### 5. Forgetting that synchronized visibility applies only to the same monitor

```java
// PROBLEM: Reader and writer synchronize on DIFFERENT monitors -- no visibility guarantee!
public class WrongMonitor {
    private int data = 0;
    private final Object writeLock = new Object();
    private final Object readLock = new Object();

    public void write(int value) {
        synchronized (writeLock) { data = value; }
    }

    public int read() {
        synchronized (readLock) { return data; } // Different monitor -- data may be stale!
    }
}
```

```java
// SOLUTION: Use the SAME monitor for both reader and writer
public class SameMonitor {
    private int data = 0;
    private final Object lock = new Object();

    public void write(int value) {
        synchronized (lock) { data = value; }
    }

    public int read() {
        synchronized (lock) { return data; } // Same monitor -- visibility guaranteed
    }
}
```

## Best Practices and Optimization Techniques

1. **Default to immutability**: Use `final` fields, Java records, and unmodifiable collections. Immutable objects require
   no synchronization for their internal state -- only the reference needs safe publication.
2. **Prefer `volatile` for simple flags and single-variable state**: If the only operation is a read or a write (not a
   compound read-modify-write), `volatile` is sufficient and cheaper than `synchronized`.
3. **Use `synchronized` when multiple variables must be consistent together**: Synchronized blocks provide both atomicity
   and visibility across all variables accessed within the block.
4. **Piggyback visibility on a single volatile write**: Write all non-volatile data first, then write a volatile flag.
   Readers poll the volatile flag; once they see the flag change, all prior writes are guaranteed visible. This technique
   reduces the number of volatile fields needed.
5. **Prefer the Initialization-on-Demand Holder idiom for singletons**: It provides lazy initialization, thread safety,
   and zero synchronization overhead on the fast path, all guaranteed by the JLS.
6. **Use `VarHandle` for advanced scenarios**: When you need fine-grained control over memory ordering (e.g.,
   acquire/release semantics instead of full volatile), `VarHandle` provides access modes that can be more efficient.
7. **Avoid publishing `this` during construction**: If a constructor registers the object as a listener or starts a
   thread, other threads may see a partially constructed object.
8. **Prefer `ReentrantLock` over `synchronized` with virtual threads**: On Java 21, `synchronized` blocks can pin
   virtual threads to their carrier OS thread; `ReentrantLock` does not have this limitation.

## Edge Cases and Their Handling

### 1. JIT compiler hoisting and the -Xint flag

In interpreted mode (`-Xint`), visibility bugs may not manifest because the JVM reads from main memory on every access.
Once the JIT compiles the method, it may hoist loop-invariant reads into registers. This means a bug can appear only
after the application has been running for minutes (once the JIT kicks in).

**Handling**: Never rely on interpreted-mode testing for concurrency correctness. Use volatile or synchronized even if
the bug doesn't appear in testing.

### 2. Final fields and the `this` escape problem

```java
// UNSAFE: The constructor publishes 'this' before construction is complete
public class ThisEscape {
    private final int value;

    public ThisEscape(EventSource source) {
        source.registerListener(e -> onEvent(e)); // 'this' escapes via the lambda!
        value = 42; // Another thread calling onEvent() may see value == 0
    }

    private void onEvent(Event e) {
        System.out.println(value); // May print 0!
    }
}
```

**Handling**: Use a static factory method that constructs the object first, then registers it.

### 3. 64-bit value tearing for long and double

On 32-bit JVMs, reads and writes of `long` and `double` fields may not be atomic -- a reader may see the upper 32 bits
from one write and the lower 32 bits from another. Declaring the field `volatile` guarantees atomic reads and writes.

```java
// UNSAFE on 32-bit JVMs
private long timestamp; // may exhibit torn reads/writes

// SAFE
private volatile long timestamp; // atomic read/write guaranteed
```

### 4. Out-of-order writes with lazy initialization

The JVM may reorder the steps of object construction. For example, `instance = new Foo()` involves:
1. Allocate memory
2. Initialize fields
3. Assign reference to `instance`

Without volatile, the JVM may reorder steps 2 and 3, allowing another thread to see a non-null `instance` with
uninitialized fields.

## Interview-specific Insights

Interviewers focus on:

- The distinction between **visibility** and **atomicity** -- they are orthogonal concerns. `volatile` provides
  visibility but not atomicity for compound operations; `synchronized` provides both.
- **Happens-before** rules and which actions establish them (volatile read/write, synchronized entry/exit,
  Thread.start/join, final fields).
- Why double-checked locking is broken without volatile and what instruction reordering scenario causes the bug.
- The **piggybacking** technique: how writing to a volatile variable "publishes" all prior non-volatile writes.
- Whether `final` fields are truly safe across threads (yes, with safe publication and no `this` escape).
- Real-world scenarios where visibility bugs manifest only under load or after JIT compilation.

Common tricky questions:

- "Can a thread spin forever on a non-volatile boolean flag?" -- Yes, the JIT may hoist the read out of the loop.
- "Does `volatile` make `i++` thread-safe?" -- No, only the individual read and write are visible, but the compound
  operation is not atomic.
- "What is the difference between `volatile` and `synchronized` for a single variable?" -- volatile provides visibility
  and ordering; synchronized provides visibility, ordering, AND mutual exclusion (atomicity).
- "Is the Holder idiom better than double-checked locking?" -- For singletons, yes -- it is simpler, requires no
  volatile field, and leverages JLS class-initialization guarantees.

## Interview Q&A Section

**Q1: What is a visibility issue in concurrent programming, and why does it happen?**

```text
A1: A visibility issue occurs when a write performed by one thread is not seen by another thread.
This happens because of the hardware memory architecture: each CPU core has its own cache, and
without an explicit synchronization action, the JVM and CPU are free to serve reads from the
local cache rather than main memory.

The Java Memory Model (JMM) defines "happens-before" relationships that guarantee visibility.
Without such a relationship between a write and a subsequent read:
- The reading thread may see a stale value from its cache indefinitely.
- The JIT compiler may optimize by hoisting a field read out of a loop, caching it in a CPU
  register, and never re-reading from memory.
- The CPU may reorder instructions, causing writes to appear in a different order to other threads.

Key mechanisms that establish happens-before (and thus guarantee visibility):
1. volatile write -> volatile read (on the same variable)
2. synchronized block exit -> synchronized block entry (on the same monitor)
3. Thread.start() -> first action in the started thread
4. Last action in a thread -> Thread.join() return
5. Writing a final field in a constructor -> reading that field after construction (with safe
   publication)
```

```java
// Demonstrating a visibility issue and its fix
public class VisibilityDemo {
    // BROKEN: worker may never see stop = true
    private boolean stop = false;

    public void brokenWorker() {
        // JIT may compile this as: if (!stop) while(true) {}
        while (!stop) {
            // ... work ...
        }
    }

    // FIXED: volatile ensures every read fetches the latest value
    private volatile boolean safeStop = false;

    public void safeWorker() {
        while (!safeStop) { // Volatile read on every iteration
            // ... work ...
        }
    }

    public void requestStop() {
        safeStop = true; // Volatile write, immediately visible to all threads
    }
}
```

**Q2: How does piggybacking on a volatile write provide visibility for non-volatile variables?**

```text
A2: The JMM guarantees that all writes performed BEFORE a volatile write are visible to any
thread that subsequently performs a volatile read of the same variable. This is because:

1. Program order: within a single thread, actions are ordered by program order.
2. Volatile write happens-before volatile read: a volatile write to variable V happens-before
   every subsequent volatile read of V by any thread.
3. Transitivity: if A happens-before B, and B happens-before C, then A happens-before C.

By combining these rules:
- Thread 1 writes non-volatile fields (data = 42, label = "hello") -- these happen-before
  the volatile write (program order).
- Thread 1 performs a volatile write (published = true).
- Thread 2 performs a volatile read (reads published == true) -- the volatile write
  happens-before this read.
- By transitivity, all of Thread 1's prior writes (data, label) happen-before Thread 2's
  volatile read.

This means Thread 2 is guaranteed to see data = 42 and label = "hello" once it sees
published == true. This technique is called "piggybacking" because the non-volatile writes
piggyback on the volatile write's visibility guarantee.

This is how java.util.concurrent internally works -- many classes use a single volatile field
or CAS operation to "publish" multiple state changes at once.
```

```java
public class PiggybackingExample {
    private int data;
    private String label;
    private volatile boolean published = false; // The "gate" variable

    // Writer thread
    public void publish(int d, String l) {
        data = d;           // Non-volatile write (1)
        label = l;          // Non-volatile write (2)
        published = true;   // Volatile write -- publishes (1) and (2) to all readers
    }

    // Reader thread
    public void read() {
        if (published) {    // Volatile read -- if true, all prior writes are visible
            // Safe to read: data and label are guaranteed to be the published values
            System.out.println(data + ": " + label);
        }
    }
}
```

**Q3: Why is double-checked locking broken without volatile, and what is the correct implementation?**

```text
A3: Double-checked locking attempts to avoid the overhead of synchronization on the fast path
by checking if the instance is null before acquiring the lock. The problem without volatile is
instruction reordering.

When the JVM executes: instance = new Singleton()
This is actually three operations:
  1. Allocate memory for the object
  2. Call the constructor (initialize fields)
  3. Assign the reference to 'instance'

The JVM is allowed to reorder steps 2 and 3. If it assigns the reference (step 3) before
calling the constructor (step 2), another thread performing the first null check will see
instance != null and return a reference to a partially constructed object.

The fix: declare instance as volatile. A volatile write has a "store-store barrier" that
prevents reordering -- the constructor must complete (step 2) before the reference is
published (step 3). Additionally, the volatile read in the first null check ensures the
reader sees a fully constructed object.

An even better alternative for singletons is the Initialization-on-Demand Holder idiom,
which relies on JLS class initialization guarantees (Section 12.4.2): the inner Holder class
is loaded and initialized exactly once, lazily, and thread-safely -- with zero volatile or
synchronized overhead on the fast path.
```

```java
// BROKEN double-checked locking (no volatile)
class BrokenDCL {
    private static BrokenDCL instance; // Bug: not volatile

    public static BrokenDCL getInstance() {
        if (instance == null) {
            synchronized (BrokenDCL.class) {
                if (instance == null) {
                    instance = new BrokenDCL(); // Reordering may expose partial object
                }
            }
        }
        return instance;
    }
}

// CORRECT double-checked locking (volatile)
class CorrectDCL {
    private static volatile CorrectDCL instance;

    public static CorrectDCL getInstance() {
        CorrectDCL result = instance; // Single volatile read
        if (result == null) {
            synchronized (CorrectDCL.class) {
                result = instance;
                if (result == null) {
                    result = new CorrectDCL();
                    instance = result; // Volatile write after full construction
                }
            }
        }
        return result;
    }
}

// BEST: Initialization-on-Demand Holder idiom
class HolderSingleton {
    private HolderSingleton() {}

    private static class Holder {
        static final HolderSingleton INSTANCE = new HolderSingleton();
    }

    public static HolderSingleton getInstance() {
        return Holder.INSTANCE; // Class loading guarantees thread safety
    }
}
```

**Q4: What is the difference between `volatile` and `synchronized` for ensuring visibility?**

```text
A4: Both volatile and synchronized establish happens-before relationships, but they differ in
scope, guarantees, and performance:

volatile:
- Applies to a SINGLE variable.
- Guarantees visibility: every read sees the latest write by any thread.
- Guarantees ordering: prevents reordering of reads/writes around the volatile access.
- Does NOT provide mutual exclusion: two threads can read and write simultaneously.
- Does NOT provide atomicity for compound operations (read-modify-write like i++).
- Lightweight: no lock acquisition; just a memory barrier.
- Works well for simple flags, state variables, and single-reference publication.

synchronized:
- Applies to ALL variables accessed within the synchronized block.
- Guarantees visibility: when a thread exits a synchronized block, all writes become visible
  to the next thread entering a synchronized block on the SAME monitor.
- Guarantees ordering: instructions cannot be reordered across synchronized block boundaries.
- Provides mutual exclusion (atomicity): only one thread can hold the monitor at a time.
- Heavier: involves lock acquisition/release, potential thread blocking and context switches.
- Essential when multiple variables must be consistent together or compound operations are needed.

When to use each:
- Use volatile for simple flags (stop, ready), single-reference publication, and when you
  only need visibility (no compound operations).
- Use synchronized when you need atomicity for compound operations, consistency across
  multiple variables, or when the critical section involves non-trivial logic.
- Consider ReentrantLock as an alternative to synchronized when you need tryLock(), timed
  locking, or better behavior with virtual threads (avoids carrier pinning).
```

```java
public class VolatileVsSynchronized {
    // Use case for volatile: simple flag
    private volatile boolean shutdown = false;

    public void requestShutdown() {
        shutdown = true; // Volatile write -- visible immediately
    }

    public void workerLoop() {
        while (!shutdown) { // Volatile read -- always fresh
            doWork();
        }
    }

    // Use case for synchronized: compound state update
    private int balance = 0;
    private int transactionCount = 0;

    public synchronized void deposit(int amount) {
        balance += amount;       // Must be atomic with transactionCount update
        transactionCount++;      // Both fields always consistent
    }

    public synchronized int[] getState() {
        return new int[]{balance, transactionCount}; // Consistent snapshot
    }

    private void doWork() { /* ... */ }
}
```

**Q5: How do `final` fields provide visibility guarantees, and what is safe publication?**

```text
A5: The JMM provides a special guarantee for final fields (JLS 17.5): if a field is declared
final and is properly initialized in the constructor, then any thread that obtains a reference
to the fully constructed object is guaranteed to see the correct value of the final field --
WITHOUT any additional synchronization.

This works because the JVM inserts a "freeze" action at the end of the constructor for objects
with final fields. This freeze ensures that the writes to final fields are visible before the
object reference is published.

However, this guarantee has a critical prerequisite: SAFE PUBLICATION. The object must be
published only after its constructor has completed. If the 'this' reference escapes during
construction (e.g., by passing it to another thread, registering as a listener, or storing it
in a static field within the constructor), the guarantee is void.

Safe publication mechanisms:
1. Storing the reference in a volatile field
2. Storing the reference in a field guarded by synchronized
3. Storing the reference via CAS (AtomicReference.compareAndSet)
4. Static initializer (class loading provides the happens-before)
5. Storing in a final field of another properly constructed object

Java records are ideal for visibility because:
- All components are implicitly final
- The canonical constructor runs to completion before the reference is available
- Combined with a volatile reference, records provide a lock-free, visibility-safe pattern
  for sharing immutable snapshots across threads.
```

```java
// Immutable class with final fields -- safe for cross-thread sharing
public final class ImmutableConfig {
    private final String host;
    private final int port;

    public ImmutableConfig(String host, int port) {
        this.host = host;
        this.port = port;
        // After constructor completes, final fields are "frozen" and visible to all threads
    }

    public String getHost() { return host; }
    public int getPort() { return port; }
}

// Java record -- implicitly final fields, same guarantees
public record Config(String host, int port) {}

// Safe publication via volatile reference
public class ConfigHolder {
    private volatile Config config; // volatile ensures reference is safely published

    public void update(Config newConfig) {
        this.config = newConfig; // volatile write after record is fully constructed
    }

    public Config getConfig() {
        return config; // volatile read -- if non-null, all final fields are visible
    }
}

// UNSAFE: 'this' escape during construction -- breaks final field guarantee
public class UnsafeThis {
    private final int value;

    public UnsafeThis(List<UnsafeThis> registry) {
        registry.add(this); // 'this' escapes before constructor finishes!
        value = 42;         // Another thread reading from registry may see value == 0
    }
}
```

**Q6: What is the Initialization-on-Demand Holder idiom and why is it preferred for singletons?**

```text
A6: The Initialization-on-Demand Holder (also called the "Bill Pugh Singleton" or "Holder idiom")
is a pattern for lazy, thread-safe singleton initialization that requires no explicit
synchronization and no volatile fields.

How it works:
1. The singleton instance is stored as a static final field of a private static inner class (the
   "Holder").
2. The Holder class is not loaded until it is referenced for the first time -- which happens only
   when getInstance() is called.
3. The JLS (Section 12.4.2) guarantees that class initialization is performed exactly once, is
   thread-safe (the JVM acquires a lock during initialization), and all static fields are visible
   to all threads after initialization.

Advantages over double-checked locking:
- Simpler: no volatile, no synchronized, no local variable tricks.
- Correct by construction: relies on JLS guarantees, not subtle memory model reasoning.
- Zero overhead on the fast path: no volatile read, no lock check.
- Lazy: the Holder class (and thus the singleton) is not initialized until first use.

Limitations:
- Only works for singletons (one instance per class loader).
- Cannot pass constructor arguments (the instance is created statically).
- Cannot handle initialization failures gracefully (the class is marked as erroneous and
  subsequent calls throw ExceptionInInitializerError or NoClassDefFoundError).
```

```java
// Initialization-on-Demand Holder idiom
public class HolderIdiom {
    private HolderIdiom() {
        // Private constructor -- prevents external instantiation
    }

    // Inner class is loaded only when getInstance() is first called
    private static class Holder {
        static final HolderIdiom INSTANCE = new HolderIdiom();
        // JLS guarantees: this initialization is thread-safe and happens exactly once
    }

    public static HolderIdiom getInstance() {
        return Holder.INSTANCE; // Triggers Holder class loading on first call
    }
}

// For comparison: Enum singleton (another safe approach, but eager)
public enum EnumSingleton {
    INSTANCE;

    public void doSomething() {
        // Enum constants are initialized during class loading -- thread-safe and serialization-safe
    }
}
```

**Q7: Can you have a visibility issue with `synchronized` if you use different monitors?**

```text
A7: Yes! The happens-before guarantee of synchronized applies ONLY when both the writer and the
reader synchronize on the SAME monitor object. If they use different monitors, there is no
happens-before relationship between them, and the reader may see stale data.

This is a common mistake in practice:
1. Using different lock objects for reading and writing the same shared state.
2. Synchronizing on a method (which uses 'this') in one thread and on a different object in
   another thread.
3. Synchronizing on a non-final field that may be reassigned -- the two threads may end up
   locking different objects.

Rules to avoid this pitfall:
- Always synchronize on the SAME lock object for all accesses to a given shared variable.
- Declare lock objects as private final to prevent reassignment.
- Prefer 'private final Object lock = new Object()' over synchronizing on 'this' -- this
  prevents external code from accidentally synchronizing on the same monitor.
- Document which lock protects which state (use @GuardedBy annotations).
```

```java
// BROKEN: Different monitors for reader and writer
class BrokenMonitors {
    private int value = 0;
    private final Object writeLock = new Object();
    private final Object readLock = new Object();

    public void write(int v) {
        synchronized (writeLock) { value = v; } // Locks writeLock
    }

    public int read() {
        synchronized (readLock) { return value; } // Locks readLock -- no happens-before!
    }
}

// CORRECT: Same monitor for reader and writer
class CorrectMonitors {
    private int value = 0;
    private final Object lock = new Object(); // Single lock, private and final

    public void write(int v) {
        synchronized (lock) { value = v; }
    }

    public int read() {
        synchronized (lock) { return value; } // Same lock -- visibility guaranteed
    }
}
```

## Code Examples

- Source: [VisibilityIssue.java](src/main/java/com/github/msorkhpar/claudejavatutor/concurrencypitfalls/VisibilityIssue.java)
- Test: [VisibilityIssueTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/concurrencypitfalls/VisibilityIssueTest.java)
