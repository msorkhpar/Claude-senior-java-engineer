# 5.1.3. Synchronization and Memory Consistency

## Concept Explanation

**Memory consistency** describes a program's guarantee about the order in which memory operations are observed by
different threads. Without explicit synchronization, the JMM provides very weak guarantees — a thread may see writes
in any order, or not see them at all. Synchronization primitives (synchronized, volatile, locks) strengthen these
guarantees by establishing happens-before edges.

**Real-world analogy**: Think of a shared Google Doc (main memory) that multiple collaborators (threads) edit
simultaneously. Without version control or collaboration rules (synchronization), two users editing at the same time
may overwrite each other's changes or see stale content. Synchronization is like enforcing a "one editor at a time"
or "save before sharing" policy, ensuring everyone sees a consistent view of the document.

### The Synchronization Contract in Java

Java provides several synchronization mechanisms, each with specific JMM guarantees:

**1. `synchronized` keyword**
- `synchronized(obj) { ... }` acquires the intrinsic monitor of `obj`.
- **On entry**: All cached values in working memory are invalidated; subsequent reads go to main memory.
- **On exit**: All writes made inside the block are flushed to main memory; the monitor is released.
- Establishes a happens-before edge: unlock of monitor M happens-before any subsequent lock of M.
- Provides BOTH visibility and atomicity for the protected region.

**2. `volatile` keyword**
- Volatile write happens-before any subsequent volatile read of the same variable.
- Forces reads to main memory and writes to flush to main memory.
- Provides visibility but NOT atomicity for compound operations (like `i++`).

**3. `java.util.concurrent.locks.Lock` interface (ReentrantLock, etc.)**
- `lock.unlock()` establishes the same happens-before as a monitor release.
- `lock.lock()` establishes the same happens-before as a monitor acquisition.
- More flexible than `synchronized` (try-lock, timed lock, interruptible lock, condition variables).

**4. `java.util.concurrent` atomic classes**
- Operations like `AtomicInteger.compareAndSet()` establish happens-before edges.
- Based on hardware compare-and-swap (CAS) instructions — lock-free, non-blocking.

### Sequentially Consistent vs. Happens-Before Consistency

Java's JMM provides **happens-before consistency**, which is weaker than sequential consistency. In sequentially
consistent execution, all threads observe all operations in a single, consistent total order. In happens-before
consistency, only operations that have an established ordering relationship are guaranteed to be visible in order.
This weaker model allows hardware and compiler optimizations that make Java programs fast.

## Key Points to Remember

1. `synchronized` provides **both** atomicity and visibility; `volatile` provides only visibility.
2. The happens-before edge from a monitor release is to **any subsequent acquisition of the SAME monitor**.
   Synchronizing on different objects provides no cross-thread guarantee.
3. Synchronization does NOT prevent all interleavings — it only establishes ordering guarantees for actions
   connected by happens-before.
4. The **double-checked locking pattern** requires `volatile` on the instance field (since Java 5) to be correct.
5. **Lock contention** is the primary source of synchronization overhead — minimize the scope of synchronized blocks.
6. `ReentrantLock` offers the same memory semantics as `synchronized` plus additional features (try-lock, fair mode).
7. **Condition objects** (`lock.newCondition()`) enable threads to wait for specific state changes efficiently.

## Relevant Java 21 Features

- **Virtual threads and synchronized**: In Java 21, a virtual thread executing inside a `synchronized` block is
  "pinned" to its carrier platform thread (cannot be unmounted). This can reduce the scalability benefit of virtual
  threads. Prefer `ReentrantLock` in code used by virtual threads.
- **VarHandle.compareAndSet()**: Provides CAS semantics with configurable memory ordering (plain, acquire/release,
  volatile), allowing optimized lock-free patterns.
- **StampedLock (Java 8+)**: Provides optimistic read locking — a reader can read without acquiring a full lock,
  then validate that no write occurred. Falls back to a pessimistic read if validation fails. Great for read-heavy
  workloads.
- **Structured Concurrency (JEP 453, Preview in Java 21)**: `StructuredTaskScope` provides a framework where
  task lifetimes are scoped, making happens-before relationships more predictable and explicit.

## Common Pitfalls and How to Avoid Them

1. **Synchronizing on the wrong object**

   ```java
   // BROKEN: two threads may use different Integer instances (autoboxing!)
   private Integer lock = 42;

   public synchronized void badMethod() { // synchronizes on 'this', not 'lock'
       // ...
   }

   // Also broken: Integer.valueOf(42) may be cached, but relying on this is fragile
   synchronized (lock) { ... }
   ```

   **Fix**: Always use a dedicated, private, final lock object:
   ```java
   private final Object lock = new Object();
   synchronized (lock) { ... }
   ```

2. **Double-checked locking without volatile (pre-Java 5 bug)**

   ```java
   // BROKEN before Java 5: partial construction visible without volatile
   private static Singleton instance;
   public static Singleton getInstance() {
       if (instance == null) {              // First check (no lock)
           synchronized (Singleton.class) {
               if (instance == null) {       // Second check (with lock)
                   instance = new Singleton(); // Object may be partially constructed!
               }
           }
       }
       return instance;
   }
   ```

   **Fix**: Add `volatile`:
   ```java
   private static volatile Singleton instance;
   ```

3. **Locking on mutable references**

   ```java
   // BROKEN: if 'list' reference changes, locking provides no guarantee
   private List<String> list = new ArrayList<>();
   public void add(String item) {
       synchronized (list) { list.add(item); }
   }
   public void replace() {
       list = new ArrayList<>(); // Changes the lock object!
   }
   ```

   **Fix**: Use a separate, final lock object.

4. **Holding locks during I/O or long operations**

   ```java
   // BAD: holds lock while doing network I/O — blocks other threads
   public synchronized void fetchAndStore(String url) throws IOException {
       String data = httpClient.get(url); // may take seconds
       cache.put(url, data);
   }
   ```

   **Fix**: Minimize lock scope — fetch outside the lock, only lock for the store.

## Best Practices and Optimization Techniques

1. **Minimize synchronized block scope**: Lock only what's necessary. Compute results outside the lock,
   then enter the lock just to update state.
2. **Document lock ordering**: If you must acquire multiple locks, always acquire them in the same order
   across all code paths to prevent deadlocks. Document the ordering with comments.
3. **Prefer explicit Lock over synchronized when**: You need try-lock, timed lock, interruptible lock,
   or multiple condition variables.
4. **Use read-write locks for read-heavy workloads**:
   ```java
   ReadWriteLock rwLock = new ReentrantReadWriteLock();
   // Multiple readers can hold the read lock simultaneously
   rwLock.readLock().lock();
   try { /* read */ } finally { rwLock.readLock().unlock(); }
   // Only one writer, exclusive
   rwLock.writeLock().lock();
   try { /* write */ } finally { rwLock.writeLock().unlock(); }
   ```
5. **Use `java.util.concurrent` instead of rolling your own**: `ConcurrentHashMap`, `BlockingQueue`,
   `Semaphore`, etc. are battle-tested and correct.
6. **Test with stress tools**: Use `jcstress` (JVM Concurrency Stress tests) to catch subtle JMM violations
   that only appear under heavy concurrent load.

## Edge Cases and Their Handling

1. **Nested synchronized blocks and deadlock**: Acquiring locks in different orders across threads causes
   deadlock. Example: Thread A holds lock1, waits for lock2; Thread B holds lock2, waits for lock1.
   Prevention: always acquire locks in the same canonical order; use `lock.tryLock(timeout)` as a fallback.

2. **Spurious wakeups**: `Object.wait()` and `Condition.await()` may return without being notified.
   Always wrap `wait()` in a loop that checks the actual condition:
   ```java
   synchronized (lock) {
       while (!conditionMet()) { // not 'if'
           lock.wait();
       }
   }
   ```

3. **Re-entrant locking**: Java's `synchronized` and `ReentrantLock` are reentrant — a thread that already
   holds the lock can re-acquire it without deadlocking. The lock is released when the last release matches
   the first acquisition.

4. **Memory visibility with lock.tryLock()**: Even if `tryLock()` returns false (lock not acquired), any
   successful `lock()` / `unlock()` cycle establishes happens-before. A failed tryLock provides no
   memory ordering guarantee.

## Interview-specific Insights

Interviewers test deep understanding of:
- **Happens-before through synchronized**: Which writes are visible after acquiring a lock?
- **Double-checked locking**: Why it was broken, why `volatile` fixes it
- **Deadlock prevention**: How to avoid, detect, and recover from deadlocks
- **synchronized vs. ReentrantLock**: When to choose each
- **Condition variables**: How to use `wait/notify/notifyAll` correctly

Common tricky questions:
- "Can synchronized guarantee ordering without any write in the synchronized block?" (Yes — the lock itself
  establishes happens-before for all prior writes)
- "Is it safe to use `HashMap` inside a synchronized block?" (Yes — synchronized protects access to HashMap)
- "Why is `notifyAll()` preferred over `notify()`?" (notify() wakes one thread, which may not be the right one;
  notifyAll() wakes all and lets them re-check conditions)

## Interview Q&A Section

**Q1: What is the difference between `synchronized` and `volatile` in terms of memory consistency?**

```text
A1: Both synchronized and volatile establish happens-before edges, but they differ significantly:

VOLATILE:
- Guarantees: Visibility only. A volatile write happens-before any subsequent volatile read of the same field.
- Does NOT guarantee atomicity for compound operations (read-modify-write like i++ is still non-atomic).
- Lighter weight: Just memory barrier instructions, no thread blocking.
- Use when: A single variable needs to be visible across threads and access to it doesn't require atomicity
  of compound operations (e.g., a boolean flag, a reference to an immutable object).

SYNCHRONIZED:
- Guarantees: Both visibility AND atomicity for the protected region.
- On entry: Thread's working memory is invalidated (reads go to main memory).
- On exit: Writes are flushed to main memory; monitor is released.
- Mutual exclusion: Only one thread can hold a monitor at a time — other threads block.
- Use when: You need to protect compound operations (check-then-act, read-modify-write, multi-step updates).

Rule of thumb:
- Is the operation a single read or write to a reference or primitive (not long/double compound op)?
  → volatile may suffice
- Does the operation involve multiple steps that must appear atomic? → synchronized
```

```java
public class SyncVsVolatileDemo {
    // volatile: safe for single read/write, NOT for i++
    private volatile int volatileCounter = 0;

    // synchronized: safe for compound operations
    private int syncCounter = 0;

    // WRONG: volatile doesn't make i++ atomic
    public void wrongIncrement() {
        volatileCounter++; // read-modify-write: still non-atomic!
    }

    // CORRECT: synchronized protects the compound operation
    public synchronized void correctIncrement() {
        syncCounter++; // atomic under the lock
    }

    // CORRECT: AtomicInteger for lock-free compound ops
    private final java.util.concurrent.atomic.AtomicInteger atomicCounter =
        new java.util.concurrent.atomic.AtomicInteger(0);

    public void atomicIncrement() {
        atomicCounter.incrementAndGet(); // CAS-based, no blocking
    }
}
```

**Q2: Explain the double-checked locking pattern and why it requires volatile.**

```text
A2: Double-checked locking (DCL) is an optimization of the lazy initialization pattern. The naive version:

    if (instance == null) {
        instance = new Singleton();
    }

is not thread-safe. Wrapping in synchronized:

    synchronized (Singleton.class) {
        if (instance == null) {
            instance = new Singleton();
        }
    }

is thread-safe but acquires the lock on every call. The DCL optimization checks outside the lock first:

    if (instance == null) {                    // First check, no lock
        synchronized (Singleton.class) {
            if (instance == null) {             // Second check, with lock
                instance = new Singleton();
            }
        }
    }

Why was this broken before Java 5?
Object construction in Java compiles to roughly:
1. Allocate memory for the object
2. Initialize the object fields (call constructor)
3. Assign the reference to 'instance'

Steps 2 and 3 can be REORDERED by the compiler/JIT. Thread A may assign the reference (step 3) before
completing construction (step 2). Thread B sees instance != null (first check passes) and returns a
partially constructed object.

volatile fix (Java 5+): Declaring 'instance' volatile prevents the reordering of steps 2 and 3.
The volatile write of 'instance' happens-before any subsequent volatile read of 'instance', ensuring
Thread B sees the fully constructed object.
```

```java
public class DoubleCheckedLocking {
    // CRITICAL: volatile prevents construction reordering
    private static volatile DoubleCheckedLocking instance;

    private final int value;

    private DoubleCheckedLocking() {
        this.value = expensiveInit();
    }

    private int expensiveInit() {
        // simulate expensive initialization
        return 42;
    }

    public static DoubleCheckedLocking getInstance() {
        if (instance == null) {                    // First check (no lock, fast path)
            synchronized (DoubleCheckedLocking.class) {
                if (instance == null) {             // Second check (with lock)
                    instance = new DoubleCheckedLocking(); // volatile write
                }
            }
        }
        return instance; // volatile read
    }

    // Alternative: Initialization-on-demand holder (preferred — no volatile needed)
    static class SingletonHolder {
        static final DoubleCheckedLocking INSTANCE = new DoubleCheckedLocking();
    }

    public static DoubleCheckedLocking getInstanceSafe() {
        return SingletonHolder.INSTANCE; // class loading is thread-safe
    }

    public int getValue() { return value; }
}
```

**Q3: How do you prevent deadlock in Java?**

```text
A3: Deadlock occurs when two or more threads are permanently blocked, each waiting for a lock held by another.
Four conditions must ALL be true for deadlock (Coffman conditions):
1. Mutual exclusion: locks are non-shareable
2. Hold and wait: threads hold locks while waiting for others
3. No preemption: locks cannot be forcibly released
4. Circular wait: threads form a cycle of waiting

Prevention strategies:
1. LOCK ORDERING: Always acquire multiple locks in the same predefined order across all threads.
   If Thread A acquires lock1 then lock2, ensure Thread B does the same. Document the order.

2. LOCK TIMEOUT (tryLock): Use ReentrantLock.tryLock(timeout) — if a lock can't be acquired within
   the timeout, release all held locks and retry. This breaks the hold-and-wait condition.

3. REDUCE LOCK SCOPE: Minimize what happens inside a locked region. Avoid calling external code,
   I/O operations, or other potentially-locking methods while holding a lock.

4. USE LOCK-FREE STRUCTURES: AtomicInteger, ConcurrentHashMap, etc. avoid locks entirely.

5. STRUCTURED LOCKING: Use java.util.concurrent utilities (BlockingQueue, Semaphore, etc.) that
   are designed to avoid deadlock.
```

```java
public class DeadlockPrevention {
    private final Object lockA = new Object();
    private final Object lockB = new Object();

    // DEADLOCK RISK: inconsistent lock ordering
    public void riskyMethod1() {
        synchronized (lockA) {
            synchronized (lockB) { /* work */ }
        }
    }

    public void riskyMethod2() {
        synchronized (lockB) {    // different order!
            synchronized (lockA) { /* work */ }
        }
    }

    // SAFE: consistent lock ordering (always lockA before lockB)
    public void safeMethod1() {
        synchronized (lockA) {
            synchronized (lockB) { /* work */ }
        }
    }

    public void safeMethod2() {
        synchronized (lockA) {   // same order as safeMethod1
            synchronized (lockB) { /* work */ }
        }
    }

    // SAFE: tryLock with timeout
    private final java.util.concurrent.locks.ReentrantLock rlockA =
        new java.util.concurrent.locks.ReentrantLock();
    private final java.util.concurrent.locks.ReentrantLock rlockB =
        new java.util.concurrent.locks.ReentrantLock();

    public boolean tryAcquireBoth() throws InterruptedException {
        if (rlockA.tryLock(100, java.util.concurrent.TimeUnit.MILLISECONDS)) {
            try {
                if (rlockB.tryLock(100, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                    try {
                        // do work
                        return true;
                    } finally { rlockB.unlock(); }
                }
            } finally { rlockA.unlock(); }
        }
        return false; // back off and retry
    }
}
```

**Q4: What is a memory barrier and how does Java use them?**

```text
A4: A memory barrier (also called a memory fence) is a hardware instruction that prevents the CPU from
reordering memory operations across the barrier. Different CPUs have different barriers:
- Load barrier: no load can be moved before this point
- Store barrier: no store can be moved after this point
- Full barrier: both load and store ordering enforced

Java abstracts barriers through its higher-level synchronization primitives:
- volatile write: inserts a StoreStore barrier before + StoreLoad barrier after
- volatile read: inserts a LoadLoad barrier after + LoadStore barrier after
- synchronized exit (monitor release): inserts a StoreStore + StoreLoad barrier
- synchronized entry (monitor acquire): inserts a LoadLoad + LoadStore barrier

Programmers generally don't need to think about hardware barriers — they think in terms of
happens-before relationships. But understanding barriers helps explain WHY certain patterns work
and WHY certain optimizations are forbidden by the JMM.

VarHandle (Java 9+) exposes explicit memory ordering:
- VarHandle.get() / set() — no barrier (plain access)
- VarHandle.getOpaque() / setOpaque() — prevents reordering within thread
- VarHandle.getAcquire() / setRelease() — release/acquire semantics (like mutex unlock/lock)
- VarHandle.getVolatile() / setVolatile() — full volatile semantics
```

```java
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class MemoryBarrierDemo {
    private int value;
    private volatile int volatileFlag;

    // VarHandle for fine-grained memory ordering
    private static final VarHandle VALUE_HANDLE;
    static {
        try {
            VALUE_HANDLE = MethodHandles.lookup()
                .findVarHandle(MemoryBarrierDemo.class, "value", int.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // Release semantics: all writes before this are visible to threads that
    // do an acquire read of the same variable
    public void setWithRelease(int v) {
        value = v;
        VALUE_HANDLE.setRelease(this, v); // release barrier
    }

    // Acquire semantics: see all writes made before the corresponding release
    public int getWithAcquire() {
        return (int) VALUE_HANDLE.getAcquire(this); // acquire barrier
    }

    // Full volatile semantics (strongest guarantee)
    public void setVolatile(int v) {
        VALUE_HANDLE.setVolatile(this, v);
    }
}
```

**Q5: What is the Initialization-on-Demand Holder idiom and why is it preferred over DCL?**

```text
A5: The Initialization-on-Demand Holder (IODH) idiom exploits the JVM's class loading mechanism to
achieve lazy, thread-safe initialization without explicit synchronization or volatile:

public class Singleton {
    private Singleton() { }

    private static class Holder {
        static final Singleton INSTANCE = new Singleton();
    }

    public static Singleton getInstance() {
        return Holder.INSTANCE;
    }
}

Why it works:
1. The JVM guarantees that class initialization (static initializers) is thread-safe — the JVM uses
   a class initialization lock to ensure only one thread initializes a class.
2. The Holder class is NOT loaded until the first call to getInstance(). This provides lazy initialization.
3. Once Holder is initialized, the static final INSTANCE is safely published to all threads via the
   class initialization happens-before guarantee.
4. No volatile needed — the class loading lock provides the ordering guarantee.

Why it's preferred over DCL:
1. Simpler code — no volatile, no double-check
2. Correct by construction — relies on well-specified JVM behavior
3. No risk of mis-applying the pattern incorrectly
4. Works on all Java versions (not just Java 5+)
```

```java
public class InitializationOnDemandHolder {
    private final int expensiveValue;

    private InitializationOnDemandHolder() {
        // Expensive initialization — runs only once, lazily, thread-safely
        this.expensiveValue = computeExpensiveValue();
    }

    private int computeExpensiveValue() {
        return 42; // imagine expensive computation here
    }

    // Inner holder class — loaded lazily on first access to getInstance()
    private static class Holder {
        // Class initialization lock ensures thread safety
        static final InitializationOnDemandHolder INSTANCE =
            new InitializationOnDemandHolder();
    }

    public static InitializationOnDemandHolder getInstance() {
        return Holder.INSTANCE; // Holder class initialized lazily here
    }

    public int getValue() { return expensiveValue; }
}
```

## Code Examples

- Source: [MemoryConsistency.java](src/main/java/com/github/msorkhpar/claudejavatutor/javamemorymodel/MemoryConsistency.java)
- Test: [MemoryConsistencyTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/javamemorymodel/MemoryConsistencyTest.java)
