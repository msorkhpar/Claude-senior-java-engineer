# 5.2.1. Definition of Happens-before Relationships

## Concept Explanation

The **happens-before** relationship is the cornerstone of the Java Memory Model (JMM), formally defined in the Java
Language Specification (JLS §17.4). It is a partial order over memory operations (reads and writes) that guarantees
**memory visibility**: if action A happens-before action B, then all memory writes performed by A (and all actions
before A) are visible to B and all subsequent actions.

**Real-world analogy**: Think of happens-before like a signed, timestamped contract. If Alice signs a contract before
Bob, and the contract says "all changes Alice made to the shared document are visible when Bob reads it," that is
happens-before. Without such a contract, Bob cannot be sure he will see Alice's changes — the document might be
cached somewhere Bob cannot see. Happens-before is the JVM's "contract" that forces visibility.

The JMM does NOT guarantee that threads execute in real-time order — the processor, compiler, and JVM are all free to
reorder operations for performance. Happens-before is the ONLY mechanism that constrains this reordering.

### The Six Core Happens-before Rules

The JMM defines happens-before through a specific set of rules (JLS §17.4.5):

1. **Program Order Rule** — Each action in a thread happens-before every subsequent action in the same thread.
   Within a single thread, code executes in program order (or appears to — the compiler may reorder if the
   single-thread semantics are preserved).

2. **Monitor Lock Rule** — An unlock of a monitor happens-before every subsequent lock of that same monitor.
   When thread T1 releases a `synchronized` lock and thread T2 acquires the same lock, T2 is guaranteed to see
   all writes T1 performed while holding the lock.

3. **Volatile Variable Rule** — A write to a `volatile` variable happens-before every subsequent read of that
   same variable. This provides visibility but NOT atomicity for compound operations like `i++`.

4. **Thread Start Rule** — A call to `Thread.start()` on a thread happens-before any action in the started thread.
   All writes made before `start()` are visible to the new thread.

5. **Thread Join Rule** — All actions in a thread happen-before any thread successfully returns from `join()` on
   that thread. Whatever the joined thread wrote is visible to the joining thread after `join()` returns.

6. **Transitivity** — If A happens-before B, and B happens-before C, then A happens-before C.
   This rule makes the partial order transitive and allows building chains of happens-before guarantees.

### What Happens Without Happens-before

Without happens-before, the JVM can:
- Cache writes in CPU registers — other threads may never see them
- Reorder instructions for performance (both compiler and CPU reordering)
- Use stale values from CPU caches

This results in **data races**: two threads accessing the same variable where at least one access is a write, and
there is no happens-before ordering between them. A program with a data race has undefined behavior in the JMM.

### The Difference Between Happens-before and Wall-clock Time

Happens-before is about **visibility**, not real-time ordering. If write W happens-before read R, it means R is
guaranteed to see the value written by W. It does NOT mean W physically occurred before R on the clock. Two
operations without happens-before between them are **unordered** — the outcome is non-deterministic.

## Key Points to Remember

1. **Happens-before is about visibility guarantees**, not clock ordering.
2. **Six core rules**: program order, monitor lock, volatile variable, thread start, thread join, transitivity.
3. **Data race definition**: Two conflicting accesses with no happens-before relationship between them.
4. **Transitivity extends guarantees**: If A hb B and B hb C, then writes before A are visible to C.
5. **Single thread always has total order**: all program order rules apply within one thread.
6. **Unsynchronized reads can see any previous write**, not necessarily the most recent one.
7. **Happens-before is NOT causal ordering** — it is a formal constraint on visibility.
8. **The JMM is defined for the Java platform** — native code, JNI, and off-heap memory operate outside it.

## Relevant Java 21 Features

- **Java 5 (JSR-133)**: The current JMM was introduced, fixing the broken double-checked locking idiom and
  clarifying volatile semantics. All modern Java builds on this memory model.
- **Java 7+**: The `ForkJoinPool` framework uses happens-before guarantees through task submission and completion.
- **Java 8+**: `CompletableFuture` chains propagate happens-before through completion callbacks.
- **Java 9+**: `VarHandle` (JEP 193) provides fine-grained memory access modes: plain, opaque, release/acquire, and
  volatile, allowing programmers to choose the exact happens-before strength needed.
- **Java 21 (Virtual Threads, JEP 444)**: Virtual threads obey the same JMM happens-before rules as platform
  threads. `Thread.start()` and `Thread.join()` rules apply identically. Structured concurrency (JEP 453) uses
  `StructuredTaskScope.join()`, which establishes happens-before from all child task completions to the joiner.

## Common Pitfalls and How to Avoid Them

1. **Assuming sequential consistency without synchronization** — Threads do not automatically see each other's writes.

   ```java
   // Broken: no happens-before between writer and reader threads
   class BrokenFlag {
       boolean ready = false;
       int data = 0;

       void writer() {
           data = 42;
           ready = true; // might be reordered before data = 42!
       }

       void reader() {
           while (!ready) ; // might loop forever due to caching
           System.out.println(data); // might print 0!
       }
   }
   ```

   ```java
   // Fix: use volatile (for single writes/reads) or synchronized
   class FixedFlag {
       volatile boolean ready = false;
       volatile int data = 0;

       void writer() { data = 42; ready = true; }
       void reader() { while (!ready) ; System.out.println(data); }
   }
   ```

2. **Thinking program order rule applies across threads** — The program order rule is per-thread only.

   ```java
   // Broken assumption: T2 will always see x=1 because T1 wrote it "first"
   int x = 0;
   // Thread T1:
   x = 1;           // No synchronization
   // Thread T2:
   System.out.println(x); // May print 0
   ```

   ```java
   // Fix: use synchronization to create cross-thread happens-before
   volatile int x = 0;
   // T1: x = 1;  (volatile write)
   // T2: System.out.println(x);  (volatile read sees volatile write)
   ```

3. **Forgetting transitivity when chaining guarantees** — A write before a `volatile` write IS visible after the
   subsequent `volatile` read, due to transitivity.

   ```java
   // This IS correct (transitivity in action):
   volatile int flag = 0;
   int data = 0;
   // T1: data = 42; flag = 1;  (data write hb flag write, by program order)
   // T2: if (flag == 1) { read data; } // data write hb flag write hb flag read hb data read
   // So T2 sees data=42 when it sees flag=1
   ```

4. **Confusing happens-before with liveness** — Happens-before only guarantees visibility IF the ordering occurs.
   A reader might never see a write if the ordering never happens (e.g., if the writer thread never runs).

## Best Practices and Optimization Techniques

1. **Use the weakest synchronization that provides the guarantee you need**: volatile for single variable
   visibility, synchronized for compound operations, `VarHandle` for lock-free algorithms.

2. **Document your synchronization strategy** with comments: state which variable is the "synchronization point"
   and which writes are made visible through it.

3. **Lean on higher-level abstractions** (`java.util.concurrent`, `CompletableFuture`, `Executors`) that already
   provide well-defined happens-before guarantees rather than rolling your own.

4. **Design for single-writer** wherever possible: a single writer publishing to multiple readers via volatile
   requires only the volatile variable rule, no locking needed.

5. **Use `@GuardedBy` annotations** (from `jcip-annotations` or JSR-305) to document which lock protects which field,
   making happens-before relationships explicit in the code.

## Edge Cases and Their Handling

1. **64-bit long/double writes are not atomic** on 32-bit JVMs without `volatile`. Use `volatile long` or
   `AtomicLong` if multiple threads access the same `long` or `double` variable.

2. **Array element visibility**: Declaring an array reference `volatile` does NOT make element reads/writes volatile.
   Use `AtomicIntegerArray`, `AtomicReferenceArray`, or a `VarHandle` for volatile array element access.

3. **Happens-before with interruption**: A call to `Thread.interrupt()` happens-before the interrupted thread
   detecting the interruption (via `isInterrupted()`, `interrupted()`, or `InterruptedException`).

4. **Static initializer happens-before**: A class's static initializer block (or static field initializations) in
   the class definition happens-before the first access to any static field or method of that class in any thread.
   This underpins the Initialization-on-Demand Holder (IODH) idiom.

5. **Default field values**: A write of the default value (0, false, null) to every field happens-before any action
   in any thread. This is why uninitialized fields always appear as their zero-value to all threads.

## Interview-specific Insights

Interviewers at senior level will probe:

- Whether you can state the six happens-before rules from memory and explain each.
- Whether you understand why the JMM allows reordering (performance) and how happens-before constrains it.
- Whether you can identify data races in code snippets and explain the fix.
- Transitivity — can you trace a chain of happens-before relationships to prove a write is visible?
- The difference between visibility (happens-before) and atomicity (synchronized, Atomic classes).

**Common tricky questions**:
- "If T1 writes x and then T2 reads x, is T2 guaranteed to see T1's write?" (No, only if there is a happens-before.)
- "Does the program order rule create happens-before between threads?" (No, only within a single thread.)
- "Can you have data races on volatile variables?" (No — a volatile write always happens-before a volatile read of
  the same variable, so volatile accesses are always ordered.)

## Interview Q&A Section

**Q1: What is the happens-before relationship in the Java Memory Model and why does it exist?**

```text
A1: The happens-before (hb) relationship is a formal partial order defined by the Java Memory Model (JMM, JLS §17.4)
over memory operations. If action A happens-before action B, then all memory writes performed by A (and by all
actions that happen-before A) are guaranteed to be visible to B.

It exists because modern hardware and compilers aggressively reorder memory operations for performance:
- CPUs have multi-level caches — a write to a register may not be flushed to main memory.
- Compilers reorder instructions within optimization passes.
- The JIT compiler in the JVM reorders bytecode to native instructions.

Without happens-before, two threads accessing shared state would have undefined behavior. The happens-before
relationship is the contract that says "at this point, thread B is guaranteed to see what thread A wrote."

This is fundamentally different from wall-clock time. Happens-before is about visibility guarantees, not about
which instruction physically executed first on the CPU.
```

```java
// Example: Thread.start() creates happens-before
public class ThreadStartHappensBefore {
    private int sharedValue = 0;

    public void demonstrate() throws InterruptedException {
        sharedValue = 42; // Write BEFORE start() — guaranteed visible in new thread

        Thread t = new Thread(() -> {
            // Thread.start() happens-before this code
            // So sharedValue is guaranteed to be 42 here
            System.out.println("Value: " + sharedValue); // Always prints 42
        });

        t.start();
        t.join(); // join() happens-before code after it
        // sharedValue is still 42 (thread only read it)
    }
}
```

---

**Q2: Can you list and explain the six core happens-before rules?**

```text
A2: The six core rules are:

1. Program Order Rule: Each action in a thread happens-before every subsequent action in that same thread.
   Ensures single-threaded programs appear to execute in program order.

2. Monitor Lock Rule: An unlock of monitor M happens-before every subsequent lock of M.
   This is the basis of synchronized — releasing a lock ensures visibility of all writes to the acquirer.

3. Volatile Variable Rule: A write to volatile field V happens-before every subsequent read of V.
   Provides visibility for single variable changes without locking.

4. Thread Start Rule: Thread.start() on thread T happens-before any action in T.
   All writes before start() are visible to the new thread.

5. Thread Join Rule: All actions in thread T happen-before Thread.join(T) returns successfully.
   All writes made by T are visible to the thread that called join() after join() returns.

6. Transitivity: If A hb B and B hb C, then A hb C.
   Allows chaining guarantees across multiple synchronization points.
```

```java
public class HappensBeforeRulesDemo {
    volatile int vFlag = 0;
    int data = 0;

    // Demonstrates rules 1, 3, and 6 (transitivity)
    void writer() {
        data = 100;    // (W1) — rule 1: W1 hb W2 within writer thread
        vFlag = 1;     // (W2) — rule 3: W2 hb any subsequent read of vFlag
    }

    void reader() {
        if (vFlag == 1) { // (R2) — rule 3: W2 hb R2
            // By transitivity (rule 6): W1 hb W2 hb R2, so W1 hb R2
            // Therefore data is guaranteed to be 100 here
            System.out.println(data); // Always 100 when vFlag==1
        }
    }

    // Demonstrates rule 4 (Thread Start)
    void demonstrateStartRule() throws Exception {
        int localValue = 99;
        Thread t = new Thread(() -> System.out.println(localValue)); // sees 99
        t.start(); // start() hb thread body
        t.join();  // rule 5 (Thread Join): thread body hb after join
    }
}
```

---

**Q3: What is a data race and how does it relate to happens-before?**

```text
A3: A data race occurs when:
  1. Two or more threads access the same variable concurrently.
  2. At least one of those accesses is a write.
  3. There is NO happens-before relationship between the conflicting accesses.

A program with a data race has undefined behavior in the JMM — the reading thread may see
any value, including stale values, zero, or even values that were never written. This is not a
JVM bug; it is explicitly permitted behavior under the JMM.

Happens-before eliminates data races by imposing an order between conflicting accesses.
If write W happens-before read R, R is guaranteed to see the value written by W (or a later write).

Common data race example: unsynchronized counter (++i is read-modify-write, three operations,
not atomic). Without synchronization, concurrent threads racing on ++ produce incorrect counts.
```

```java
// DATA RACE — undefined behavior
class DataRaceExample {
    int counter = 0; // no synchronization

    void increment() { counter++; } // read-modify-write, not atomic
    int get() { return counter; }
}

// FIX 1: synchronized — monitor lock rule creates happens-before
class SynchronizedCounter {
    private int counter = 0;

    synchronized void increment() { counter++; }
    synchronized int get() { return counter; }
}

// FIX 2: AtomicInteger — uses CAS (compare-and-swap), lock-free
class AtomicCounter {
    private final AtomicInteger counter = new AtomicInteger(0);

    void increment() { counter.incrementAndGet(); }
    int get() { return counter.get(); }
}
```

---

**Q4: How does transitivity work in practice? Give an example.**

```text
A4: Transitivity (rule 6) allows happens-before chains: if A hb B and B hb C, then A hb C.
This is crucial because it means non-volatile writes CAN be made visible through a volatile
write/read pair — you don't need to make every variable volatile, just the "publication" variable.

The canonical pattern: write data fields (1), write volatile flag (2), read volatile flag (3),
read data fields (4). By transitivity: (1) hb (2) hb (3) hb (4), so (1) hb (4).

This is why the "volatile publication idiom" works: publish an object reference via a volatile
field, and all fields of the published object (set before the volatile write) are visible to
any thread that reads the volatile reference.
```

```java
public class TransitivityDemo {
    private int x = 0;
    private int y = 0;
    private volatile int ready = 0;

    // Thread 1 calls this
    public void producer() {
        x = 10;      // (A) — program order: A hb B
        y = 20;      // (B) — program order: B hb C
        ready = 1;   // (C) — volatile write
    }

    // Thread 2 calls this
    public void consumer() {
        if (ready == 1) { // (D) — volatile read: C hb D (volatile rule)
            // By transitivity: A hb B hb C hb D
            // So A hb D and B hb D
            System.out.println(x); // guaranteed to be 10
            System.out.println(y); // guaranteed to be 20
        }
    }
}
```

---

**Q5: Does the program order rule create happens-before between two threads? Why or why not?**

```text
A5: No. The program order rule is strictly within a single thread. It says that each action in
thread T happens-before every subsequent action in thread T — it says nothing about other threads.

Two threads running simultaneously have NO inherent happens-before relationship with each other.
Their operations are concurrent and unordered. Thread T2 cannot assume it sees any particular
value written by T1 unless there is an explicit synchronization action (volatile, synchronized,
Thread.start/join, etc.) that creates a cross-thread happens-before.

This is one of the most common misconceptions. Programmers who have only written single-threaded
code assume that "the code runs in order" — but in multi-threaded code, each thread has its own
program order, and these orders are independent.

The only happens-before between threads comes from the other five rules (monitor lock, volatile,
thread start, thread join, and transitivity of those four).
```

```java
// Common misconception — no cross-thread happens-before
class NoHappensBeforeAcrossThreads {
    int shared = 0;

    void run() throws InterruptedException {
        Thread writer = new Thread(() -> { shared = 42; });
        Thread reader = new Thread(() -> {
            // Does NOT have happens-before from writer!
            // May see 0 or 42 — undefined behavior (data race)
            System.out.println(shared);
        });

        writer.start();
        reader.start();
        writer.join();
        reader.join();
    }
}

// Correct: Thread.start() creates happens-before
class CorrectOrdering {
    int shared = 0;

    void run() throws InterruptedException {
        shared = 42; // Before start() — start rule guarantees visibility

        Thread reader = new Thread(() -> {
            System.out.println(shared); // Always sees 42
        });

        reader.start(); // start() hb thread body
        reader.join();
    }
}
```

---

**Q6: How does the static initializer happens-before guarantee underpin the Initialization-on-Demand Holder pattern?**

```text
A6: The JMM guarantees that a class's static initializer (the <clinit> method) happens-before
the first access to any static field or method of that class by any thread. The JVM ensures
this using internal locking during class loading (the "initialization lock").

The Initialization-on-Demand Holder (IODH) idiom exploits this guarantee to create a
thread-safe, lazily initialized singleton without any synchronized blocks in the getInstance()
method. The Holder inner class is not loaded until getInstance() is called, at which point the
JVM's class loading lock ensures that the static initializer of Holder happens-before any
thread reads the INSTANCE field.

This is more efficient than double-checked locking and works correctly on all Java versions.
```

```java
public class SingletonIODH {
    // Private constructor prevents direct instantiation
    private SingletonIODH() {
        // initialization...
    }

    // Inner class is loaded only when getInstance() is first called
    private static final class Holder {
        // Static initializer runs under JVM class-loading lock
        // JMM guarantees: static init hb any subsequent access to INSTANCE
        static final SingletonIODH INSTANCE = new SingletonIODH();
    }

    // No synchronization needed — guaranteed thread-safe by JMM
    public static SingletonIODH getInstance() {
        return Holder.INSTANCE; // class load + static init hb this read
    }

    // Usage
    public static void main(String[] args) {
        SingletonIODH s1 = SingletonIODH.getInstance();
        SingletonIODH s2 = SingletonIODH.getInstance();
        System.out.println(s1 == s2); // true — same instance
    }
}
```

## Code Examples

- Source: [HappensBeforeDefinition.java](src/main/java/com/github/msorkhpar/claudejavatutor/happensbefore/HappensBeforeDefinition.java)
- Test: [HappensBeforeDefinitionTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/happensbefore/HappensBeforeDefinitionTest.java)
