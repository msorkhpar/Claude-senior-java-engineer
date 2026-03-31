# 5.2.2. Synchronization Actions and Their Impact on Memory Visibility

## Concept Explanation

Three primary language-level mechanisms establish happens-before guarantees across threads in Java:
**synchronized methods/blocks**, **volatile variables**, and **final fields**. Each targets a different use case and
provides a different granularity of guarantee.

### 5.2.2.1. Synchronized Methods and Blocks

The `synchronized` keyword uses a **monitor** (also called an intrinsic lock or object lock). Every Java object has
exactly one monitor. When a thread acquires the monitor (enters a synchronized block), it establishes exclusive
ownership; no other thread can enter any `synchronized` block guarded by the same monitor until the first thread
releases it.

**Monitor Lock Rule (JLS §17.4.5)**: An unlock of monitor M **happens-before** every subsequent lock of M.

This means: all writes a thread performed while holding the lock (and all writes that happened-before while it held
the lock) become visible to the next thread that acquires the same lock.

`synchronized` provides TWO guarantees:
1. **Mutual exclusion (atomicity)**: Only one thread can execute inside the block at a time.
2. **Memory visibility**: On exit, writes are flushed; on entry, the thread refreshes its view of memory.

**Real-world analogy**: A synchronized block is like a shared notebook in a locked cabinet. Whoever has the key
(lock) can update the notebook. When they put the key back (unlock), all their changes are in the notebook. The next
person who picks up the key reads the notebook fresh — seeing all previous changes.

### 5.2.2.2. Volatile Variables

A `volatile` variable provides the **Volatile Variable Rule (JLS §17.4.5)**: A write to volatile field V
**happens-before** every subsequent read of V.

Volatile provides:
1. **Visibility**: Writes are immediately visible to all threads that subsequently read the variable.
2. **Prohibition of caching**: The JVM cannot cache the value in a register or CPU cache.
3. **Limited reordering prevention**: Operations cannot be reordered across a volatile access (full memory barrier).

Volatile does NOT provide:
- **Atomicity** for compound operations: `volatile int i; i++` is still a read-modify-write that requires
  additional synchronization.
- **Mutual exclusion**: Multiple threads can read/write a volatile variable simultaneously.

**Real-world analogy**: A volatile variable is like a whiteboard in an open office that everyone can see. When
someone writes on it, everyone immediately sees the new value. But two people can erase and write at the same
time — there's no turn-taking (no mutual exclusion).

**When to use volatile vs synchronized**:
- Use `volatile` when: one thread writes, others only read; or for a boolean flag; or for a single reference
  publication.
- Use `synchronized` when: the operation involves reading and then writing the same variable (check-then-act),
  or when you need compound operations to be atomic.

### 5.2.2.3. Final Fields

The `final` field guarantee is different from volatile and synchronized — it applies at **object construction time**
and requires no synchronization at use time.

**Final Field Rule (JLS §17.5)**: If object O is properly constructed (the `this` reference does not escape during
construction), then a write to a `final` field of O in O's constructor **happens-before** any subsequent read of that
field in any other thread.

This means: once a reference to an object is safely published (made visible to other threads through a
happens-before mechanism), all `final` fields of that object are guaranteed to be visible with their
initialized values — even without any explicit synchronization at the read site.

**Implications**:
- Immutable objects (all fields `final`) require only safe publication of the reference — no locking at read time.
- The JVM may cache final field values aggressively, knowing they cannot change.
- If an object escapes `this` during construction (e.g., passes `this` to another thread inside the constructor),
  the final field guarantee is void.

**Real-world analogy**: A `final` field is like a birth certificate. Once properly issued (object fully constructed),
it captures the values at the moment of birth and is forever fixed. Anyone who gets a valid copy of the certificate
(safely published reference) can read any field without needing to verify it with anyone else.

## Key Points to Remember

1. `synchronized` provides BOTH mutual exclusion AND memory visibility; it is the strongest guarantee.
2. `volatile` provides visibility and ordering but NO mutual exclusion; `i++` on a volatile is NOT atomic.
3. `final` fields provide visibility after safe publication without any runtime synchronization overhead.
4. All three mechanisms are based on different happens-before rules, each with different costs and use cases.
5. A `volatile` write acts as a "store fence" and a `volatile` read acts as a "load fence" at the CPU level.
6. Synchronized blocks that use DIFFERENT locks provide NO mutual exclusion and NO happens-before between them.
7. Final field guarantee is conditional: the object must be **properly constructed** (no `this` escape).
8. `volatile` on an object reference does NOT make the object's fields volatile — only the reference itself.

## Relevant Java 21 Features

- **Java 5 (JSR-133)**: Fixed the volatile semantics to include full memory barrier semantics. Pre-Java 5 volatile
  only prevented CPU caching, not compiler reordering. This is why double-checked locking is only safe in Java 5+.
- **Java 9 (VarHandle, JEP 193)**: `java.lang.invoke.VarHandle` provides fine-grained memory ordering:
  - `PLAIN`: no ordering guarantees (like unsynchronized access)
  - `OPAQUE`: prohibits tearing (for long/double) and some reordering, but no cross-thread guarantees
  - `ACQUIRE`/`RELEASE`: one-sided memory barriers — `ACQUIRE` for reads, `RELEASE` for writes (lighter than volatile)
  - `VOLATILE`: full volatile semantics
- **Java 21 (Virtual Threads)**: Virtual threads use the same synchronization mechanisms. However, a `synchronized`
  block in a virtual thread that blocks will pin the virtual thread to its carrier platform thread. Prefer
  `java.util.concurrent.locks.ReentrantLock` in virtual-thread-heavy code for better scalability.
- **Java 21 (Structured Concurrency, JEP 453)**: `StructuredTaskScope.join()` provides happens-before from all
  child task results to the joining thread, offering a safer abstraction than raw `Thread.join()`.

## Common Pitfalls and How to Avoid Them

1. **Using different monitors for the same data** — synchronized on different objects provides NO happens-before.

   ```java
   // Broken: Two threads lock different objects — no mutual exclusion, no visibility
   class BrokenSynchronization {
       private int value = 0;

       synchronized void write(int v) { value = v; }       // uses 'this' as monitor

       void read() {
           synchronized (new Object()) { // different lock!
               System.out.println(value); // no happens-before from write()
           }
       }
   }
   ```

   ```java
   // Fix: Use the same monitor for all operations on shared state
   class CorrectSynchronization {
       private int value = 0;
       private final Object lock = new Object();

       void write(int v) { synchronized(lock) { value = v; } }
       void read() { synchronized(lock) { System.out.println(value); } }
   }
   ```

2. **Assuming volatile makes compound operations atomic** — `volatile int i; i++` is read-modify-write, NOT atomic.

   ```java
   // Broken: concurrent i++ on volatile still has race condition
   class BrokenVolatileCounter {
       volatile int count = 0;
       void increment() { count++; } // read, increment, write — NOT atomic!
   }
   ```

   ```java
   // Fix: Use AtomicInteger for atomic compound operations
   class CorrectAtomicCounter {
       AtomicInteger count = new AtomicInteger(0);
       void increment() { count.incrementAndGet(); } // atomic
   }
   ```

3. **Publishing objects with final fields before construction is complete (`this` escape)**

   ```java
   // Broken: this escapes the constructor — final field guarantee is void
   class UnsafePublication {
       static UnsafePublication instance;
       final int value;

       UnsafePublication() {
           instance = this; // 'this' escapes BEFORE value is assigned!
           value = 42;
       }
   }
   // Another thread reading instance.value might see 0 (default), not 42!
   ```

   ```java
   // Fix: Never let 'this' escape during construction
   class SafePublication {
       final int value;
       static volatile SafePublication instance; // volatile for safe publication

       SafePublication() {
           value = 42; // assign all fields first
           // do NOT pass 'this' to any other thread here
       }

       static void publish() {
           instance = new SafePublication(); // safe publication via volatile
       }
   }
   ```

4. **Synchronizing on non-final fields** — If the reference used as a monitor changes, threads may use different monitors.

   ```java
   // Broken: lock object reference can change
   class BrokenLock {
       private Object lock = new Object(); // NOT final!

       void doWork() {
           synchronized (lock) { ... }
       }

       void changeLock() { lock = new Object(); } // now other threads use a different lock
   }
   ```

   ```java
   // Fix: Always use final references as monitors
   class CorrectLock {
       private final Object lock = new Object(); // final!
       void doWork() { synchronized (lock) { ... } }
   }
   ```

5. **Volatile reference, non-volatile object fields** — Volatile on a reference does NOT make the object's contents volatile.

   ```java
   // Broken: config reference is volatile, but config fields are not
   class Config { int timeout = 30; String host = "localhost"; }

   class Server {
       volatile Config config = new Config();

       void updateConfig() {
           config.timeout = 60;  // NOT guaranteed visible via volatile!
           config.host = "newhost";
       }
   }
   ```

   ```java
   // Fix: Replace the whole object (immutable update pattern)
   class Server {
       volatile Config config = new Config(30, "localhost");

       void updateConfig() {
           config = new Config(60, "newhost"); // replace reference atomically
           // All readers who read the new reference see fully constructed object
       }
   }
   ```

## Best Practices and Optimization Techniques

1. **Prefer `java.util.concurrent` over raw `synchronized`** for most use cases: `ReentrantLock`, `ReadWriteLock`,
   `Semaphore`, `CountDownLatch`, `CyclicBarrier`, and concurrent collections provide correct, well-tested, and
   often faster implementations.

2. **Use the narrowest synchronization scope**: Lock only the minimum amount of code necessary. Long synchronized
   blocks reduce concurrency and can cause contention.

3. **Immutable objects eliminate synchronization needs**: If an object's state never changes after construction
   and all fields are `final`, you only need to publish it safely once. Subsequent reads are free of synchronization.

4. **Use `volatile` for single-variable state flags**: A `volatile boolean running = true` for a stop-flag, or a
   `volatile` reference for safe publication, is idiomatic and efficient.

5. **VarHandle ACQUIRE/RELEASE for performance-critical lock-free algorithms**: If you need one-sided barriers
   (producer writes with RELEASE mode, consumer reads with ACQUIRE mode), `VarHandle` provides this lighter-weight
   alternative to full volatile.

6. **Document which lock protects which field** using `@GuardedBy("lockName")` annotation.

## Edge Cases and Their Handling

1. **`synchronized` on `null` throws `NullPointerException`** — ensure the monitor reference is non-null.

2. **Volatile `long` and `double` are guaranteed atomic** — non-volatile `long`/`double` writes may be split
   into two 32-bit writes on 32-bit JVMs, allowing tearing. `volatile` prevents this.

3. **`final` fields in deserialization** — Java serialization bypasses constructors when reading objects.
   `final` field guarantees do not apply automatically during deserialization. Use `readObject()` carefully.

4. **Static `synchronized` methods** — These use the `Class` object as the monitor, not an instance.
   `static synchronized` and `instance synchronized` methods on the same class do NOT share a monitor.

5. **`synchronized` blocks and exceptions** — The monitor is always released when a `synchronized` block exits,
   even if an exception is thrown. This prevents deadlocks from exceptions.

## Interview-specific Insights

Interviewers focus on:
- The distinction between `volatile` (visibility, no atomicity) and `synchronized` (visibility + atomicity).
- The `final` field guarantee and what "proper construction" means (no `this` escape).
- When to use which mechanism: single-variable publish → `volatile`, compound operations → `synchronized`/`Atomic`.
- Understanding that `volatile int i; i++` is NOT atomic.
- The `VarHandle` ACQUIRE/RELEASE pattern for performance-critical lock-free code.

**Common tricky questions**:
- "Is `volatile` sufficient for a counter shared across threads?" (No, `i++` needs `AtomicInteger` or `synchronized`.)
- "What happens if you synchronize on two different objects?" (No mutual exclusion, no happens-before between them.)
- "Can you use `final` fields without `synchronized`?" (Yes, if safely published — this is the basis of immutable objects.)
- "What does Java 9's `VarHandle` add over `volatile`?" (Fine-grained ordering modes for lock-free algorithms.)

## Interview Q&A Section

**Q1: What is the difference between `volatile` and `synchronized`?**

```text
A1: Both establish happens-before guarantees, but they differ significantly:

VOLATILE:
- Rule: A volatile write happens-before every subsequent volatile read of the same variable.
- Visibility: Yes — all threads see the latest write.
- Atomicity: Only for SINGLE reads/writes. Compound operations (i++) are NOT atomic.
- Mutual exclusion: No — multiple threads can access simultaneously.
- Overhead: Low — a memory barrier (fence instruction), no OS-level blocking.
- Use case: Single-variable state flags, safe publication of immutable objects.

SYNCHRONIZED:
- Rule: Unlock of monitor M happens-before subsequent lock of M.
- Visibility: Yes — on entry, fresh view of memory; on exit, writes flushed.
- Atomicity: Yes — only one thread in the block at a time.
- Mutual exclusion: Yes — exclusive access within the block.
- Overhead: Higher — potential thread blocking, OS kernel involvement for contention.
- Use case: Compound operations, check-then-act, read-modify-write.
```

```java
// volatile: visible but NOT atomic for compound ops
class VolatileExample {
    volatile boolean flag = false;      // OK: single write/read
    volatile int counter = 0;

    void setFlag() { flag = true; }     // atomic single write
    boolean checkFlag() { return flag; } // atomic single read

    void increment() { counter++; }     // NOT ATOMIC: read, +1, write (race!)
}

// synchronized: visible AND atomic
class SynchronizedExample {
    private int counter = 0;

    synchronized void increment() { counter++; } // atomic: one thread at a time
    synchronized int get() { return counter; }    // atomic: fresh read
}

// Correct volatile + atomic: use AtomicInteger
class AtomicExample {
    volatile boolean flag = false;                 // volatile for boolean flag
    AtomicInteger counter = new AtomicInteger(0); // atomic for counter

    void setFlag() { flag = true; }
    void increment() { counter.incrementAndGet(); }
}
```

---

**Q2: What does the final field guarantee provide and when does it NOT apply?**

```text
A2: The final field guarantee (JLS §17.5): A write to a final field in an object's constructor
happens-before any subsequent read of that final field in another thread, provided the object
is PROPERLY CONSTRUCTED.

"Properly constructed" means: the 'this' reference does not escape during the constructor.
If any code in the constructor passes 'this' to another thread (directly or indirectly), the
guarantee is void, and the reading thread may see the default (zero) value for the final field.

When the guarantee DOES apply (object properly constructed, reference safely published):
- Reading threads can access all final fields without synchronization.
- The JVM may aggressively cache final field values.
- This is the foundation of immutable objects in Java.

When the guarantee does NOT apply:
1. 'this' escapes during construction.
2. Deserialization (bypasses constructors — use readObject() to re-establish invariants).
3. Reflection bypassing final (setAccessible + Field.set on a final field after construction).

This is why immutable objects like String, Integer, and record types are safe to share across
threads without additional synchronization — all their fields are final and properly set.
```

```java
// Correct: Immutable object with final fields — no synchronization needed at read sites
public final class ImmutablePoint {
    private final int x;
    private final int y;

    public ImmutablePoint(int x, int y) {
        this.x = x; // (W1) — constructor write
        this.y = y; // (W2) — constructor write
        // 'this' does NOT escape here — guarantee is intact
    }

    public int getX() { return x; } // no synchronization needed
    public int getY() { return y; } // no synchronization needed
}

// Broken: 'this' escapes — final field guarantee VOID
public class EscapingThis {
    static EscapingThis instance;
    final int value;

    EscapingThis() {
        instance = this; // ESCAPES 'this' before value is assigned!
        value = 42;      // Reader may see 0 for value!
    }
}
```

---

**Q3: What is "safe publication" and how do synchronized, volatile, and final fields achieve it?**

```text
A3: Safe publication means making an object reference visible to other threads in a way that
guarantees they also see the object's fully initialized state. Without safe publication, a
thread might see the object reference but read uninitialized (default) field values.

The four safe publication idioms in Java:
1. Initializing an object reference from a static initializer (class loading lock).
2. Storing a reference in a volatile field (volatile rule).
3. Storing a reference in a field guarded by a lock (monitor lock rule).
4. Storing a reference in a final field (final field rule).

Unsound publication (data race on the reference):
   // Thread 1:  obj = new MyObject(); // reference stored without synchronization
   // Thread 2:  if (obj != null) obj.use(); // may see partially initialized obj

All four safe publication mechanisms ensure that when Thread 2 sees the reference,
it also sees all the object's fields in their initialized state.
```

```java
// Safe publication via volatile
class SafePublicationViaVolatile {
    volatile MyObject obj; // volatile reference

    void publish() {
        MyObject local = new MyObject(42, "data"); // construct first
        obj = local; // volatile write — safe publication
    }

    void consume() {
        MyObject ref = obj; // volatile read
        if (ref != null) ref.use(); // sees fully initialized object
    }
}

// Safe publication via final field (immutable objects)
final class ImmutableConfig {
    final String host;
    final int port;

    ImmutableConfig(String host, int port) {
        this.host = host; // final write in constructor
        this.port = port;
    }
}
// Once ImmutableConfig is safely published (via volatile/synchronized/static init),
// all threads can read host and port without synchronization.

// Safe publication via static initializer
class SafePublicationViaStatic {
    private static final MyObject SINGLETON = new MyObject(42, "data");
    // static initializer hb any thread's first access to SINGLETON

    static MyObject getInstance() { return SINGLETON; }
}
```

---

**Q4: Why is double-checked locking (DCL) only safe in Java 5+ with volatile?**

```text
A4: Double-checked locking is an optimization for lazy initialization that attempts to avoid
synchronization on every getInstance() call. The classic (broken) version:

    if (instance == null) {          // first check (no lock)
        synchronized (MyClass.class) {
            if (instance == null) {  // second check (with lock)
                instance = new MyClass(); // 3 operations: alloc, init, assign
            }
        }
    }
    return instance;

The problem: without volatile, the JVM can reorder the three operations of 'new MyClass()'.
Specifically, it may assign the reference to 'instance' BEFORE finishing initialization.
Thread B doing the first check could see instance != null and return a partially initialized object.

In Java 5+, making 'instance' volatile fixes this:
- The volatile write of 'instance' acts as a full memory barrier.
- No store of any field can be reordered to appear after the volatile write.
- Thread B's volatile read of 'instance' creates happens-before from Thread A's write,
  ensuring Thread B sees the fully initialized object.

Before Java 5, even volatile was broken in this scenario due to a JMM bug (JSR-133 fixed it).
```

```java
// Java 5+ DCL — CORRECT with volatile
public class DCLSingleton {
    private static volatile DCLSingleton instance; // VOLATILE is essential

    private final int value;

    private DCLSingleton() {
        value = 42;
    }

    public static DCLSingleton getInstance() {
        if (instance == null) {                    // first check (no lock, fast path)
            synchronized (DCLSingleton.class) {
                if (instance == null) {            // second check (under lock)
                    instance = new DCLSingleton(); // volatile write — full barrier
                }
            }
        }
        return instance; // volatile read — sees fully initialized object
    }

    public int getValue() { return value; }
}

// Better alternative: Initialization-on-Demand Holder (no volatile needed)
public class IoDHSingleton {
    private IoDHSingleton() {}

    private static class Holder {
        static final IoDHSingleton INSTANCE = new IoDHSingleton();
    }

    public static IoDHSingleton getInstance() {
        return Holder.INSTANCE; // class init hb this read
    }
}
```

---

**Q5: How does Java 9's VarHandle improve on volatile for performance-critical code?**

```text
A5: VarHandle (java.lang.invoke.VarHandle, JEP 193) provides four memory ordering modes that
allow programmers to choose the exact memory barrier strength needed:

1. PLAIN (setPlain/getPlain): No ordering guarantees. Same as non-volatile field access.
   Use: within a single thread or with external synchronization.

2. OPAQUE (setOpaque/getOpaque): Prevents tearing (atomic reads/writes of any type).
   Coherent within a thread but no cross-thread guarantees. Use: progress indicators.

3. RELEASE/ACQUIRE (setRelease/getAcquire): One-sided barriers.
   - setRelease: All prior stores are visible before this store. (Producer side.)
   - getAcquire: This load happens-before all subsequent loads. (Consumer side.)
   Use: lock-free publish-subscribe patterns. Lighter than full volatile.

4. VOLATILE (setVolatile/getVolatile): Full bidirectional memory barrier.
   Equivalent to volatile keyword. Use: when full happens-before is needed.

RELEASE/ACQUIRE is the key addition — it allows a producer to publish with RELEASE and a
consumer to read with ACQUIRE, establishing happens-before with less overhead than volatile
(which is always a full bidirectional barrier).

This is important in lock-free data structures (queues, stacks) where the producer only needs
to ensure its writes are visible before the reference, and the consumer only needs to ensure
it reads everything after acquiring the reference.
```

```java
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

// VarHandle RELEASE/ACQUIRE for producer-consumer
class VarHandleDemo {
    private int data = 0;
    private int published = 0;

    private static final VarHandle PUBLISHED;
    static {
        try {
            PUBLISHED = MethodHandles.lookup()
                .findVarHandle(VarHandleDemo.class, "published", int.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // Producer: use RELEASE to ensure data is visible before published
    void produce(int value) {
        data = value;                              // plain write of data
        PUBLISHED.setRelease(this, 1);             // RELEASE: data write hb this write
    }

    // Consumer: use ACQUIRE to ensure all subsequent reads see the producer's writes
    int consume() {
        if ((int) PUBLISHED.getAcquire(this) == 1) { // ACQUIRE: this read hb subsequent reads
            return data; // guaranteed to see the value written by producer
        }
        return -1;
    }
}
```

---

**Q6: What are the synchronization implications when using virtual threads in Java 21?**

```text
A6: Virtual threads (JEP 444) are lightweight threads managed by the JVM and scheduled on
platform threads. They obey the same JMM happens-before rules as platform threads:
- Thread.start() on a virtual thread hb any action in that virtual thread.
- All actions in a virtual thread hb Thread.join() returning.
- volatile, synchronized, and final fields work identically.

The KEY CAVEAT: synchronized blocks in virtual threads can "pin" the virtual thread to its
carrier platform thread, preventing the JVM from unmounting it when it blocks.
This eliminates the scalability benefit of virtual threads.

If a virtual thread blocks inside a synchronized block (e.g., waiting for I/O), the carrier
platform thread is also blocked — defeating the purpose of virtual threads.

Recommendation:
- Use java.util.concurrent.locks.ReentrantLock (or ReadWriteLock, StampedLock) instead of
  synchronized for I/O-bound operations in virtual-thread-heavy code. These locks do not pin.
- Reserve synchronized for very short critical sections where blocking is highly unlikely.
- The JVM team is working to remove the pinning limitation in a future release.

Java 21's StructuredTaskScope provides a higher-level API with automatic happens-before
from all child tasks to the joiner, making concurrent result collection safe.
```

```java
// Preferred pattern for virtual threads: ReentrantLock instead of synchronized
import java.util.concurrent.locks.ReentrantLock;

class VirtualThreadFriendlyCounter {
    private final ReentrantLock lock = new ReentrantLock();
    private int count = 0;

    void increment() {
        lock.lock(); // does NOT pin virtual thread on block
        try {
            count++;
        } finally {
            lock.unlock();
        }
    }

    int get() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }
}

// Structured concurrency — happens-before from all tasks to the joiner
import java.util.concurrent.StructuredTaskScope;

class StructuredDemo {
    int fetchData() throws InterruptedException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var f1 = scope.fork(() -> compute(1)); // fork tasks
            var f2 = scope.fork(() -> compute(2));
            scope.join(); // join() hb any reads of f1.get(), f2.get()
            scope.throwIfFailed();
            return f1.get() + f2.get(); // safely sees all task results
        }
    }

    private int compute(int x) { return x * 10; }
}
```

## Code Examples

- Source: [SynchronizationActions.java](src/main/java/com/github/msorkhpar/claudejavatutor/happensbefore/SynchronizationActions.java)
- Test: [SynchronizationActionsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/happensbefore/SynchronizationActionsTest.java)
