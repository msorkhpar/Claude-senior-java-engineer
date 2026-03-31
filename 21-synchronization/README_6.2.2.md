# 6.2.2. Volatile Keyword

## Concept Explanation

The `volatile` keyword in Java addresses a critical challenge in multithreaded programming: **memory visibility**.
In modern computer architectures, each CPU core has its own cache. Without coordination, a write to a variable by one
thread may remain in that thread's CPU cache and not be visible to other threads reading from their own caches.

**Real-world analogy**: Imagine a company with multiple offices in different cities. Each office keeps its own local
copy of the company's price list. If headquarters updates the price list, offices might still use their outdated local
copy. `volatile` is like requiring headquarters to update a shared, always-visible central database, and requiring
all offices to always consult that central database — never their local copy.

### The Java Memory Model and Visibility

Without synchronization, the Java Memory Model (JMM) allows threads to keep their own working copies of shared
variables. A write by Thread A may be cached and not flushed to main memory until later. A read by Thread B may use
a cached value rather than reading from main memory.

This can lead to subtle bugs:
- A flag set by one thread may never be seen by another thread (infinite loops)
- A published object may appear partially constructed

### What `volatile` Guarantees

1. **Visibility**: A write to a volatile variable is immediately visible to all threads that subsequently read it.
   The JMM guarantees that:
   - Writing to volatile flushes the new value to main memory
   - Reading volatile always reads from main memory (bypasses caches)

2. **Ordering (partial)**: Volatile introduces happens-before relationships:
   - All actions before writing to volatile X happen-before the write to X
   - The write to volatile X happens-before all actions after reading X

3. **Prohibition of reordering**: The JVM and CPU are NOT allowed to reorder reads/writes to volatile variables
   in ways that would violate visibility.

### What `volatile` Does NOT Guarantee

`volatile` does NOT provide **atomicity** for compound operations. A read-modify-write operation like `count++` is
actually three steps:
1. Read `count` from memory
2. Increment the value
3. Write the new value back

If two threads perform these three steps concurrently, the result is a race condition even if `count` is volatile.
Only single reads and writes to `volatile long` and `volatile double` are guaranteed atomic (unlike non-volatile
64-bit operations which may be non-atomic on 32-bit JVMs).

```java
// NOT thread-safe even with volatile!
volatile int count = 0;
count++; // Read-modify-write: not atomic
```

For compound operations, use `synchronized` or `AtomicInteger`/`AtomicLong`.

### Common Use Cases for `volatile`

1. **Status/flag variables**: A boolean flag checked by one thread and set by another.
2. **Safe publication of immutable objects**: Publish an object reference once it's fully constructed.
3. **Double-checked locking (corrected pattern)**: With volatile, the DCL pattern is safe in Java 5+.
4. **Independent writes from multiple threads**: When writes don't depend on the previous value.

## Key Points to Remember

1. `volatile` ensures **visibility** — all reads see the most recent write.
2. `volatile` does NOT ensure **atomicity** — compound operations like `++` are NOT atomic.
3. `volatile` establishes **happens-before** relationships between reads and writes.
4. Reading and writing a single `volatile` variable is **always atomic** (including `long` and `double`).
5. `volatile` prevents CPU/compiler **instruction reordering** around the volatile access.
6. `volatile` is not a replacement for `synchronized` — use it only when the variable is written by one thread and
   read by others, or when multiple threads write independent values.
7. For compound operations (increment, compare-and-set), use `AtomicXxx` classes or `synchronized`.
8. The double-checked locking pattern for singleton REQUIRES `volatile` to be correct in Java 5+.

## Relevant Java 21 Features

- **VarHandle API (Java 9+)**: `java.lang.invoke.VarHandle` provides more fine-grained memory ordering control,
  allowing `getOpaque`, `getAcquire`, `getRelease`, `getVolatile` semantics. This is lower-level than `volatile`
  but enables optimized concurrent algorithms.

- **Atomic classes in `java.util.concurrent.atomic`**: `AtomicInteger`, `AtomicLong`, `AtomicReference`,
  `LongAdder` (Java 8+) provide atomic compound operations. `LongAdder` is more scalable than `AtomicLong` for
  high-contention increment scenarios.

- **Virtual Threads (JEP 444)**: Virtual threads don't change the behavior of `volatile` — the visibility and
  ordering guarantees still apply. However, `volatile` combined with virtual threads should be used carefully since
  volatile reads/writes add memory barriers.

- **StampedLock (Java 8+)**: Offers optimistic read locking, potentially more scalable than ReadWriteLock for
  read-heavy workloads, but more complex. The optimistic read must be validated.

### Evolution Across Java Versions

- **Java 1.0–1.4**: `volatile` provided only visibility, but NOT the reordering prohibition. The double-checked
  locking pattern was broken.
- **Java 5 (JSR 133)**: The Java Memory Model was revised. `volatile` now provides both visibility AND
  establishes happens-before, fixing double-checked locking with `volatile`.
- **Java 9**: VarHandle API introduced, providing fine-grained memory ordering beyond volatile.

## Common Pitfalls and How to Avoid Them

1. **Using `volatile` for compound operations** — does not provide atomicity:

   ```java
   // WRONG: Not thread-safe!
   volatile int count = 0;

   public void increment() {
       count++; // READ-MODIFY-WRITE — not atomic even with volatile!
   }

   // CORRECT: Use AtomicInteger for compound operations
   AtomicInteger count = new AtomicInteger(0);
   public void increment() {
       count.incrementAndGet(); // Atomic
   }

   // OR: Use synchronized
   private int count = 0; // doesn't need to be volatile if always accessed under lock
   public synchronized void increment() {
       count++;
   }
   ```

2. **Not using `volatile` for double-checked locking** — unsafe without it in Java 5+:

   ```java
   // WRONG: Without volatile, a partially-constructed object might be published
   private static Singleton instance;
   public static Singleton getInstance() {
       if (instance == null) {
           synchronized (Singleton.class) {
               if (instance == null) {
                   instance = new Singleton(); // NOT SAFE without volatile
               }
           }
       }
       return instance;
   }

   // CORRECT: Use volatile for double-checked locking
   private static volatile Singleton instance;
   public static Singleton getInstance() {
       if (instance == null) {
           synchronized (Singleton.class) {
               if (instance == null) {
                   instance = new Singleton(); // Safe with volatile
               }
           }
       }
       return instance;
   }
   ```

3. **Confusing `volatile` with `synchronized`** — `volatile` only guards individual reads/writes:

   ```java
   // volatile provides visibility for individual read/write of 'items'
   volatile List<String> items;

   // WRONG: This check-then-act is not atomic
   if (items == null) {         // read (volatile)
       items = new ArrayList<>(); // write (volatile)
   }
   // Another thread could set 'items' between the read and write!

   // CORRECT: Use synchronized for check-then-act
   private List<String> items; // Not volatile; access via synchronized
   public synchronized void initIfNeeded() {
       if (items == null) {
           items = new ArrayList<>();
       }
   }
   ```

4. **Over-using `volatile`** — volatile reads/writes add memory barriers and prevent certain optimizations:

   ```java
   // Unnecessary if only accessed from a single thread
   volatile int localCounter = 0; // Adds overhead for no benefit if single-threaded

   // Only declare volatile when shared between threads
   ```

5. **Assuming `volatile` on a reference makes the object's fields visible**:

   ```java
   volatile Config config; // Only the reference 'config' is volatile
   // config.value is NOT automatically volatile!
   config.value = newValue; // May not be visible to other threads
   ```

## Best Practices and Optimization Techniques

1. **Use `volatile` for flag variables**: The most natural and safe use case.

   ```java
   private volatile boolean running = true;
   public void stop() { running = false; }
   public void run() { while (running) { /* ... */ } }
   ```

2. **Use `AtomicXxx` instead of `volatile` for increment/decrement** operations.

3. **Use `LongAdder` for high-throughput counters** — less contention than `AtomicLong` under high concurrency.

4. **Safe publication pattern** with volatile:

   ```java
   class SafeObject {
       private volatile HelperObject helper;

       public HelperObject getHelper() {
           if (helper == null) {
               synchronized (this) {
                   if (helper == null) {
                       helper = new HelperObject();
                   }
               }
           }
           return helper;
       }
   }
   ```

5. **Use `VarHandle` for advanced, fine-grained memory ordering** when `volatile` is too strict.

6. **Prefer immutability**: Immutable objects don't need `volatile` (after safe publication via a volatile/final
   field).

7. **Document the threading policy**: Add comments explaining why a field is volatile and what invariants it
   maintains.

## Edge Cases and Their Handling

1. **Visibility vs. atomicity for `long` and `double`**: On 32-bit JVMs, reads/writes to 64-bit non-volatile `long`
   and `double` may not be atomic (can see "word tearing"). `volatile` makes them atomic:

   ```java
   volatile long timestamp; // Guaranteed atomic read/write even on 32-bit JVMs
   ```

2. **Volatile array reference vs. volatile array elements**: The volatile applies to the reference, not the
   elements. Use `AtomicIntegerArray` for volatile array elements:

   ```java
   volatile int[] arr = new int[10];
   arr[0] = 42; // NOT volatile — this write may not be visible to other threads

   // Use AtomicIntegerArray for element-level volatile semantics
   AtomicIntegerArray atomicArr = new AtomicIntegerArray(10);
   atomicArr.set(0, 42); // Volatile semantics for elements
   ```

3. **Volatile in final fields**: Declared `final` fields in Java 5+ have special visibility guarantees when
   properly constructed (without `this` escaping). `volatile` is not needed for `final` fields.

4. **Happens-before chain**: Volatile creates happens-before guarantees across threads, not just the volatile
   variable itself:

   ```java
   // Thread 1
   data = "hello";   // Not volatile, but...
   flag = true;      // volatile write — happens-after writing data

   // Thread 2
   if (flag) {       // volatile read
       use(data);    // Safe! JMM guarantees data is visible here
   }
   ```

5. **Spurious `volatile` writes**: Writing to a volatile field even when the value hasn't changed still acts
   as a full memory barrier and notifies all threads. Don't write volatile fields in tight loops unnecessarily.

## Interview-specific Insights

Interviewers focus on:

- The difference between `volatile`, `synchronized`, and `Atomic` classes
- Whether `volatile` provides atomicity (it does NOT for compound ops)
- The double-checked locking pattern and why `volatile` is required
- The happens-before relationship established by volatile
- When to use `volatile` vs. `synchronized` vs. `AtomicInteger`
- Memory visibility without synchronization (the classic "flag variable" infinite loop bug)
- Visibility of volatile reference vs. fields of the referenced object

Classic interview trick question: "Is `volatile int count; count++` thread-safe?" (Answer: No!)

Tricky topics:
- 64-bit variables (`long`, `double`) and word tearing on 32-bit JVMs
- The JMM happens-before chain through volatile: non-volatile variables written before a volatile write ARE
  visible after reading the volatile variable
- Why double-checked locking was broken pre-Java 5

## Interview Q&A Section

**Q1: What problem does the `volatile` keyword solve?**

```text
A1: volatile solves the memory visibility problem in multithreaded programs.

Without volatile: Each CPU core has its own cache. The JVM allows threads to keep their own
"working copy" of shared variables. A write by Thread A may sit in A's CPU cache and never
be flushed to main memory, so Thread B reading from its own cache always sees the stale value.

Classic bug:
  Thread A: while (!shutdown) { doWork(); }   // May never see the update
  Thread B: shutdown = true;                  // Written to B's cache only?

With volatile: Writes are immediately visible to all threads. All reads go to main memory.
The JMM prohibits instruction reordering around volatile accesses.

volatile does NOT solve atomicity — compound operations like count++ are still not atomic
even if count is declared volatile. For those, use synchronized or AtomicInteger.
```

```java
// Classic visibility bug — fixed with volatile
public class VisibilityDemo {
    // Without volatile: Thread reading 'running' might never see it become false
    private volatile boolean running = true;

    public void stop() {
        running = false; // Immediately visible to all threads with volatile
    }

    public void work() {
        while (running) {  // Always reads fresh value from main memory
            // do work
        }
        System.out.println("Stopped");
    }
}
```

**Q2: Does `volatile` provide atomicity? When should you use `AtomicInteger` instead?**

```text
A2: volatile does NOT provide atomicity for compound operations.

A compound operation like count++ involves three steps:
1. Read the current value
2. Increment it
3. Write the new value back

If two threads execute these three steps concurrently, both may read the same value, both
increment it to the same next value, and both write that same value — losing one increment.
This is a race condition, and volatile does NOT prevent it.

volatile IS atomic for:
- Single reads and writes of any type (including long and double)
- Reading a volatile reference

Use AtomicInteger (or other Atomic classes) when:
- Multiple threads increment/decrement a counter
- You need compare-and-set semantics
- You need compound atomic operations (getAndIncrement, compareAndSet)

Use synchronized when:
- Multiple variables must be updated atomically as a group
- You need check-then-act atomicity
- You need to coordinate access across multiple operations
```

```java
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicVsVolatileDemo {

    // volatile int — NOT safe for count++
    private volatile int volatileCount = 0;

    // AtomicInteger — safe for all operations
    private final AtomicInteger atomicCount = new AtomicInteger(0);

    // synchronized — safe for all operations
    private int syncCount = 0;

    // UNSAFE: Race condition even with volatile
    public void unsafeIncrement() {
        volatileCount++; // read-modify-write is not atomic!
    }

    // SAFE: Atomic increment
    public void safeAtomicIncrement() {
        atomicCount.incrementAndGet(); // Atomic operation
    }

    // SAFE: Synchronized increment
    public synchronized void safeSyncIncrement() {
        syncCount++;
    }

    // volatile IS safe for simple assignment
    private volatile boolean initialized = false;
    public void setInitialized() {
        initialized = true; // Safe: single write
    }
    public boolean isInitialized() {
        return initialized; // Safe: single read
    }
}
```

**Q3: Explain the double-checked locking pattern and why `volatile` is required.**

```text
A3: Double-checked locking (DCL) is a pattern for lazy initialization of a singleton that
attempts to avoid the cost of synchronization after the object is initialized.

The naive implementation:
  if (instance == null) {        // Check 1 (no lock)
      synchronized (Cls.class) {
          if (instance == null) { // Check 2 (with lock)
              instance = new Cls();
          }
      }
  }

Why this was BROKEN before Java 5 (JSR 133):
  new Cls() is not an atomic operation. The JVM allocates memory, initializes fields,
  and then assigns the reference. Without volatile, the JVM/CPU can reorder these steps.
  It may write the reference to 'instance' BEFORE finishing field initialization.
  Thread B reads a non-null but partially-constructed instance!

Why volatile FIXES it:
  volatile establishes a happens-before relationship. The write to 'instance' (volatile)
  happens-after all the writes done during new Cls(). Thread B reading 'instance'
  (volatile read) sees all those prior writes. Object is fully constructed.
```

```java
public class DoubleCheckedLocking {

    // CORRECT: volatile required for safe DCL in Java 5+
    private static volatile DoubleCheckedLocking instance;

    private final String data;

    private DoubleCheckedLocking() {
        this.data = "initialized";
    }

    public static DoubleCheckedLocking getInstance() {
        if (instance == null) {           // First check (no synchronization)
            synchronized (DoubleCheckedLocking.class) {
                if (instance == null) {   // Second check (under lock)
                    instance = new DoubleCheckedLocking(); // volatile write
                }
            }
        }
        return instance; // volatile read
    }

    // ALTERNATIVE: Prefer initialization-on-demand holder idiom (no volatile needed)
    // Thread-safe and lazy by virtue of class loading semantics
    private static class Holder {
        private static final DoubleCheckedLocking INSTANCE = new DoubleCheckedLocking();
    }

    public static DoubleCheckedLocking getInstanceViaHolder() {
        return Holder.INSTANCE;
    }
}
```

**Q4: What happens-before guarantees does `volatile` provide beyond just the volatile variable itself?**

```text
A4: This is a subtle but important aspect of the JMM happens-before rules.

The rule is: a volatile write to variable X happens-before a subsequent volatile read of X.

Because happens-before is transitive, this means:
  - All actions performed by Thread A BEFORE writing to volatile X are visible to Thread B
    AFTER Thread B reads volatile X.

This "piggyback" effect means you can safely publish non-volatile state through a volatile
variable, as long as:
  1. All state is written before the volatile write
  2. The volatile variable is read before accessing that state

This is called the "volatile as a synchronization action" or "piggybacking on volatile."
```

```java
public class VolatileHappensBeforeDemo {
    private int nonVolatileData;  // NOT volatile
    private String nonVolatileMessage; // NOT volatile
    private volatile boolean published = false; // volatile flag

    // Writer thread
    public void publish(int data, String message) {
        nonVolatileData = data;        // Step 1: write non-volatile state
        nonVolatileMessage = message;  // Step 2: write non-volatile state
        published = true;              // Step 3: volatile write — happens-after steps 1 & 2
    }

    // Reader thread — called after publish() is visible
    public void consume() {
        if (published) {  // volatile read
            // JMM guarantees: because we read 'published' = true (volatile),
            // we see all writes that happened-before the volatile write of 'published'.
            // nonVolatileData and nonVolatileMessage are correctly visible!
            System.out.println(nonVolatileData + " " + nonVolatileMessage);
        }
    }
}
```

**Q5: What is the difference between `volatile`, `synchronized`, and `AtomicInteger` in terms of memory semantics and performance?**

```text
A5: All three provide visibility guarantees, but they differ in scope and cost:

volatile:
  - Guarantees: visibility of single variable, partial ordering (happens-before for that variable)
  - Does NOT guarantee: atomicity of compound operations
  - Performance: Lowest overhead — a memory barrier, no thread context switching
  - Use when: One writer, multiple readers of a simple flag or reference

synchronized:
  - Guarantees: mutual exclusion, full memory visibility of ALL variables in the block
  - Guarantees: atomicity of the entire synchronized block
  - Performance: Higher overhead — involves lock acquisition, potential thread blocking
  - Use when: Multiple writers, compound operations, or invariants spanning multiple variables

AtomicInteger/AtomicLong/etc.:
  - Guarantees: atomicity of specific operations (getAndIncrement, compareAndSet, etc.)
  - Uses: hardware-level CAS (Compare-And-Swap) instruction — lock-free
  - Performance: Medium — no lock, but CAS may spin under high contention
  - Use when: High-concurrency counters, accumulators, or CAS-based algorithms

For very high-write-throughput counters: prefer LongAdder (Java 8+) over AtomicLong.
LongAdder distributes the count across multiple cells, reducing contention.
```

```java
import java.util.concurrent.atomic.*;

public class MemorySemanticsComparison {
    // volatile: visibility only, compound ops unsafe
    private volatile boolean flag = false;

    // synchronized: full mutual exclusion + visibility
    private int syncCounter = 0;
    public synchronized void syncIncrement() { syncCounter++; }

    // AtomicInteger: CAS-based atomic compound operations
    private final AtomicInteger atomicCounter = new AtomicInteger(0);
    public void atomicIncrement() { atomicCounter.incrementAndGet(); }

    // LongAdder: high-throughput counter with less contention
    private final LongAdder throughputCounter = new LongAdder();
    public void highThroughputIncrement() { throughputCounter.increment(); }
    public long getThroughputCount() { return throughputCounter.sum(); }

    // AtomicReference: volatile semantics for object references
    private final AtomicReference<String> config = new AtomicReference<>("default");
    public boolean updateConfig(String expected, String newValue) {
        return config.compareAndSet(expected, newValue); // Atomic CAS
    }
}
```

**Q6: Can you explain a scenario where using `volatile` alone is insufficient and might lead to a subtle bug?**

```text
A6: A classic scenario is the "check-then-act" pattern. Even with volatile, if two threads
both check a condition and then act on it, the check and the act are not performed atomically.

Example: A volatile lazy-initialization check:
  if (resource == null) {       // Two threads might both see null
      resource = new Resource(); // Both create a new instance — bug!
  }

Another example: Compound conditional update:
  if (counter < MAX) {
      counter++; // Read of 'counter' and write to 'counter' are separate — race!
  }

Another scenario: Volatile array reference but non-volatile element writes.
  volatile int[] scores = new int[10];
  scores[5] = 42; // The reference 'scores' is volatile but element 5 is NOT!

The key insight: volatile provides sequential consistency for the specific variable,
not for sequences of operations involving that variable.
Solution: Use synchronized blocks or AtomicXxx for compound operations.
```

```java
import java.util.concurrent.atomic.AtomicReference;

public class VolatileInsufficientDemo {

    // PROBLEM 1: Check-then-act race condition
    private volatile Object resource = null;

    // UNSAFE even with volatile!
    public Object getResourceUnsafe() {
        if (resource == null) {           // Thread A and B both see null
            resource = new Object();      // Both create — one is lost!
        }
        return resource;
    }

    // SAFE: Use synchronized for check-then-act
    public synchronized Object getResourceSafe() {
        if (resource == null) {
            resource = new Object();      // Only one thread can be here
        }
        return resource;
    }

    // PROBLEM 2: Volatile reference doesn't make array contents volatile
    private volatile int[] scores = new int[10];

    public void updateScoreUnsafe(int index, int value) {
        scores[index] = value;  // NOT a volatile write! Other threads may not see this.
    }

    // SAFE: Use AtomicIntegerArray for element-level volatile semantics
    private final java.util.concurrent.atomic.AtomicIntegerArray atomicScores =
            new java.util.concurrent.atomic.AtomicIntegerArray(10);

    public void updateScoreSafe(int index, int value) {
        atomicScores.set(index, value);  // Volatile semantics for elements
    }
}
```

## Code Examples

- Implementation: [VolatileKeyword.java](src/main/java/com/github/msorkhpar/claudejavatutor/synchronization/VolatileKeyword.java)
- Test: [VolatileKeywordTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/synchronization/VolatileKeywordTest.java)
