# 5.1.1. Overview of the Java Memory Model

## Concept Explanation

The Java Memory Model (JMM), formally specified in Chapter 17 of the Java Language Specification, defines the rules
governing how threads interact through memory. It answers a deceptively simple question: "If thread A writes a value,
when can thread B read it?" Without the JMM, every platform—x86, ARM, SPARC—could provide different answers, making
concurrent Java programs non-portable.

**Real-world analogy**: Imagine a team of software engineers (threads) sharing a whiteboard (main memory). The JMM
governs the rules of this collaboration: when can one engineer see what another has written? Can one engineer read a
stale note that has already been updated? The JMM provides the rulebook that all engineers and the JVM must follow.

The JMM was introduced in Java 1.0 but had well-known flaws. Java 5 (JSR-133) introduced a corrected and
comprehensive JMM that remains in effect today. At its core, the JMM defines:

1. **Main Memory**: The shared memory area visible to all threads. This conceptually maps to heap memory where objects
   and static fields live.
2. **Working Memory (CPU Cache / Registers)**: Each thread has its own working memory — a thread-local view of the data
   it is currently using. This conceptually maps to CPU registers and caches.
3. **Happens-Before Relationships**: A partial order over all actions in a program. If action X happens-before action Y,
   then X's effects are visible to Y. The JMM defines which actions establish happens-before edges.

Without understanding the JMM, you may write code that:
- Works perfectly in single-threaded tests but fails intermittently in production
- Behaves correctly on your development machine (x86) but fails on ARM servers
- Passes a code review but is silently broken by compiler or JIT optimizations

Key actions defined by the JMM include: reads and writes to variables, lock acquisitions and releases, thread starts,
and thread joins. Each of these actions may or may not create happens-before edges between threads.

## Key Points to Remember

1. The JMM is about **visibility** and **ordering**, not just atomicity.
2. Threads may work with **stale cached copies** of variables unless proper synchronization is used.
3. The **happens-before** relationship is the primary mechanism for reasoning about memory visibility.
4. The JMM was significantly revised in **Java 5 (JSR-133)** to fix the broken double-checked locking pattern and
   related bugs.
5. **Volatile reads/writes**, **synchronized blocks**, **thread start/join**, and **Lock operations** all establish
   happens-before edges.
6. The JMM is an **abstraction**—it describes what the JVM must guarantee, not how hardware implements it.
7. **Memory consistency errors** (reading stale values) are the most common JMM-related bugs.
8. The JMM applies to both **heap variables** and **class-level (static) variables** — NOT to local variables, which
   are always thread-local.

## Relevant Java 21 Features

- **Virtual Threads (JEP 444)**: Java 21 makes virtual threads standard. Virtual threads follow the same JMM rules
  as platform threads. Synchronized blocks work correctly with virtual threads, though `ReentrantLock` is preferred
  since virtual threads can be pinned to a carrier thread inside `synchronized`.
- **Structured Concurrency (JEP 453, Preview)**: Provides a higher-level model where the lifetimes of subtasks are
  scoped to a structured block, making happens-before relationships more predictable.
- **VarHandle API (Java 9+)**: Fine-grained memory-ordering operations (plain, opaque, release/acquire, volatile)
  that expose the JMM's ordering guarantees directly without requiring full `synchronized` blocks.
- **StampedLock**: An alternative to `synchronized` and `ReentrantLock` that provides optimistic reads, useful when
  understanding JMM visibility implications.

## Common Pitfalls and How to Avoid Them

1. **Assuming variables are visible across threads without synchronization**

   ```java
   // BROKEN: stopRequested may never be seen as true by the background thread
   private boolean stopRequested = false;

   public void runBroken() {
       Thread t = new Thread(() -> {
           while (!stopRequested) { /* spin */ }
       });
       t.start();
       Thread.sleep(100);
       stopRequested = true; // may not be visible to t
   }
   ```

   **Fix**: Declare `stopRequested` as `volatile` or use `synchronized`:
   ```java
   private volatile boolean stopRequested = false;
   ```

2. **Check-then-act without synchronization**

   ```java
   // BROKEN: another thread may update between the check and the act
   if (!map.containsKey(key)) {
       map.put(key, computeValue(key));
   }
   ```

   **Fix**: Use `ConcurrentHashMap.computeIfAbsent()` or explicit synchronization.

3. **Publishing objects without synchronization**

   ```java
   // BROKEN: another thread may see a partially constructed object
   public class UnsafePublisher {
       static Helper helper;
       static void initialize() { helper = new Helper(); }
   }
   ```

   **Fix**: Use `volatile`, `final` fields, or synchronization for safe publication.

4. **Over-trusting unit tests** — JMM bugs are timing-dependent and may not manifest in tests run on specific hardware.
   Use tools like `jcstress` for stress-testing concurrency correctness.

## Best Practices and Optimization Techniques

1. **Prefer immutability**: Immutable objects are inherently thread-safe because they are safely published via final
   fields and their state never changes.
2. **Use `volatile` for simple flags**: For single boolean flags or reference publication, `volatile` is sufficient and
   has lower overhead than `synchronized`.
3. **Use `java.util.concurrent` classes**: `AtomicInteger`, `ConcurrentHashMap`, `CountDownLatch`, etc. are built with
   correct JMM semantics and save you from rolling your own synchronization.
4. **Document thread-safety contracts**: Use `@GuardedBy`, `@ThreadSafe`, and `@NotThreadSafe` annotations
   (from `com.google.code.findbugs:jsr305`) to communicate intent.
5. **Use the minimum synchronization needed**: Over-synchronization kills performance; under-synchronization causes
   bugs. Use `volatile` when only visibility is needed, `synchronized` when atomicity is also required.
6. **Understand happens-before before using `volatile`**: Volatile guarantees visibility of the volatile variable
   itself and all writes made before the volatile write (happens-before "piggybacking").

## Edge Cases and Their Handling

1. **64-bit long and double reads/writes**: Without `volatile`, reads and writes to `long` and `double` fields are
   NOT atomic on 32-bit JVMs — they may be split into two 32-bit operations. Modern 64-bit JVMs guarantee atomicity,
   but the JMM does not. Always use `volatile` or `AtomicLong` for shared `long`/`double` values.
2. **Final fields**: Writing to final fields in a constructor creates a happens-before edge with any read of those
   fields after the constructor completes — but ONLY if the object reference is not published (leaked) before the
   constructor finishes.
3. **Static initializers**: Class initialization is guaranteed to be thread-safe by the JVM (via the class loading
   lock). Static final fields set during class initialization are visible to all threads without additional
   synchronization.
4. **Lazy initialization with volatile**: The double-checked locking pattern requires both `volatile` and `synchronized`
   to be correct (since Java 5).

## Interview-specific Insights

Interviewers expect senior engineers to:

- **Explain the JMM in plain English**: "What happens when thread A writes to a variable — when does thread B see it?"
- **Know happens-before rules from memory**: volatile, synchronized, thread start/join, final fields
- **Identify JMM bugs in code snippets**: spotting missing `volatile`, unsafe publication, double-checked locking errors
- **Distinguish atomicity, visibility, and ordering**: These are three separate concerns and require different
  solutions
- **Know the history**: Why JSR-133 (Java 5) was needed; what was broken before

Common tricky interview questions:
- "Is `i++` atomic?" (No — it's read-modify-write, three separate operations)
- "Can `volatile` replace `synchronized`?" (Only for visibility; not for compound actions like check-then-act)
- "Why was double-checked locking broken before Java 5?" (partial construction / reordering without `volatile`)

## Interview Q&A Section

**Q1: What is the Java Memory Model and why does it exist?**

```text
A1: The Java Memory Model (JMM) is a specification (Java Language Specification, Chapter 17) that defines the rules
governing how threads in a Java program interact through shared memory.

It exists because:
1. Modern hardware uses caches, registers, and out-of-order execution — so writes by one CPU core are not
   immediately visible to another.
2. Compilers and JIT optimizers reorder instructions for performance.
3. Without a well-defined model, programs that work on one hardware/JVM combination could silently produce incorrect
   results on another.

The JMM abstracts over all of this hardware complexity and provides one portable set of rules for Java programmers:
"As long as you synchronize correctly using the primitives defined by the JMM, your program will behave correctly
on every JVM and hardware platform."

Before JSR-133 (Java 5), the JMM had known bugs — for example, the double-checked locking pattern was broken
because compilers were allowed to reorder object construction with the store of the reference. JSR-133 fixed this
and provided a cleaner happens-before model.
```

```java
// JMM in action: visibility without synchronization
public class JmmVisibilityDemo {
    private boolean running = true; // NOT volatile

    public void stop() {
        running = false; // Thread A writes
    }

    public void run() {
        // Thread B might spin forever — it may see a cached 'true'
        while (running) {
            // work
        }
    }
    // Fix: declare running as volatile
}
```

**Q2: What is a happens-before relationship?**

```text
A2: A happens-before relationship is a partial order over actions in a Java program. If action X happens-before
action Y, then:
- All memory writes performed by X (and everything that happened-before X) are visible to Y.
- X appears to execute before Y.

Happens-before edges are established by:
1. Program order: Within a single thread, action A that appears before action B in code happens-before B.
2. Monitor lock: An unlock of a monitor happens-before every subsequent lock of that same monitor.
3. Volatile write: A write to a volatile field happens-before every subsequent read of that field.
4. Thread start: Thread.start() happens-before any action in the started thread.
5. Thread join: All actions in a thread happen-before Thread.join() returns.
6. Final fields: The write of a final field in a constructor happens-before any external read of the object,
   assuming the reference is not leaked before the constructor completes.

Happens-before is TRANSITIVE: if X happens-before Y and Y happens-before Z, then X happens-before Z.
```

```java
public class HappensBeforeDemo {
    private int value = 0;
    private volatile boolean ready = false;

    // Thread A
    public void writer() {
        value = 42;          // (1)
        ready = true;        // (2) volatile write
    }

    // Thread B
    public void reader() {
        if (ready) {         // (3) volatile read — happens-after (2)
            // (2) happens-after (1) by program order
            // (3) happens-after (2) by volatile
            // Therefore: (1) happens-before (3)
            System.out.println(value); // Guaranteed to print 42
        }
    }
}
```

**Q3: What is the difference between atomicity, visibility, and ordering in the context of the JMM?**

```text
A3: These are three distinct concerns:

1. ATOMICITY: An operation is atomic if it appears to happen as a single, indivisible unit.
   - int read/write: atomic by JMM spec
   - long/double read/write: NOT guaranteed atomic (may be split into two 32-bit ops) without volatile
   - i++: NOT atomic — it is read, increment, write (three ops)
   - Solution: synchronized blocks, AtomicInteger, AtomicLong, etc.

2. VISIBILITY: When thread A writes a value, will thread B see the updated value or a stale cached copy?
   - Without synchronization: NO guarantee
   - volatile: guarantees the write is immediately flushed to main memory and subsequent reads see the updated value
   - synchronized: on exit, all writes are flushed to main memory; on entry, the working memory is refreshed

3. ORDERING: Can the compiler/JIT/CPU reorder instructions?
   - Without synchronization: YES — compilers and CPUs reorder instructions aggressively
   - volatile: establishes happens-before ordering; writes before a volatile write cannot be moved after it
   - synchronized: provides sequentially consistent ordering within the protected region

Senior engineers understand that different problems need different tools:
- Only atomicity? → AtomicInteger
- Only visibility? → volatile
- Both atomicity and visibility? → synchronized / explicit Lock
```

```java
public class ThreeConcernsDemo {
    // Visibility issue only
    private volatile boolean flag = false;

    // Atomicity issue only (on 32-bit JVM)
    private volatile long largeCounter = 0L; // volatile fixes split-word reads

    // Both atomicity and visibility
    private int compound = 0;

    public synchronized void incrementCompound() {
        compound++; // read-modify-write: needs synchronized for atomicity + visibility
    }

    // Better: use AtomicInteger for lock-free atomicity
    private final java.util.concurrent.atomic.AtomicInteger atomic =
            new java.util.concurrent.atomic.AtomicInteger(0);

    public void incrementAtomic() {
        atomic.incrementAndGet(); // atomic + visible
    }
}
```

**Q4: What happens-before rules does the JMM define for synchronized blocks?**

```text
A4: For synchronized blocks, the JMM provides two critical rules:

1. UNLOCK → LOCK (monitor release happens-before subsequent acquisition):
   When thread A releases a monitor (exits synchronized block), all writes made by thread A while holding that
   monitor are flushed to main memory. When thread B subsequently acquires the same monitor, thread B's working
   memory is invalidated and it refreshes from main memory. Therefore, thread B sees all writes made by thread A.

2. Within a thread, all actions within a synchronized block have program-order happens-before edges between them.

By transitivity, this means:
- All writes by thread A inside synchronized(lock){...}
- are visible to thread B after thread B enters synchronized(lock){...}

This provides BOTH visibility and atomicity for the protected code region.

Important subtlety: The happens-before edge is only established for threads that synchronize on the SAME monitor
object. Synchronizing on different objects provides no cross-thread visibility guarantees.
```

```java
public class SynchronizedHappensBefore {
    private final Object lock = new Object();
    private int sharedValue = 0;
    private String sharedName = null;

    public void writer() {
        synchronized (lock) {
            sharedValue = 100;    // (1)
            sharedName = "Alice"; // (2)
        } // monitor release (3): flushes (1) and (2)
    }

    public void reader() {
        synchronized (lock) { // monitor acquire (4): invalidates cache
            // (4) happens-after (3) by monitor rule
            // (3) happens-after (1) and (2) by program order
            // Therefore: reader sees sharedValue=100, sharedName="Alice"
            System.out.println(sharedValue + " " + sharedName);
        }
    }
}
```

**Q5: How does the JMM handle final fields and safe publication?**

```text
A5: The JMM provides a special guarantee for final fields:

FINAL FIELD RULE: A write to a final field in a constructor, and a write to an object referenced by a final field
in a constructor, happens-before the first read of that final field outside the constructor, provided the reference
to the object is not published (leaked) before the constructor completes.

This means:
- An object whose ALL mutable state is set through final fields is inherently thread-safe without additional
  synchronization, assuming no reference escape from the constructor.
- This is why immutable classes (all fields final, no mutation after construction) are the gold standard for
  thread safety.

Safe publication methods (ways to make an object's reference visible to other threads safely):
1. Store it in a static field during class initialization (static initializers are thread-safe)
2. Store it in a volatile field or AtomicReference
3. Store it in a field guarded by a lock (synchronized setter)
4. Store it in a final field (then publish the holder object)

Unsafe publication:
- Storing in a plain non-volatile, non-final, non-synchronized field allows other threads to see a
  partially constructed object.
```

```java
// Safe publication via final fields
public final class ImmutablePoint {
    private final int x;
    private final int y;

    public ImmutablePoint(int x, int y) {
        this.x = x;
        this.y = y;
        // Constructor completes — final field guarantee kicks in
    }

    public int getX() { return x; }
    public int getY() { return y; }
    // No synchronization needed — final fields ensure visibility
}

// Safe publication via volatile
public class SafePublisher {
    private volatile ImmutablePoint point;

    public void publish(int x, int y) {
        point = new ImmutablePoint(x, y); // volatile write
    }

    public ImmutablePoint get() {
        return point; // volatile read — sees latest write
    }
}
```

## Code Examples

- Source: [JmmOverview.java](src/main/java/com/github/msorkhpar/claudejavatutor/javamemorymodel/JmmOverview.java)
- Test: [JmmOverviewTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/javamemorymodel/JmmOverviewTest.java)
