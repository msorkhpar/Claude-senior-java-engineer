# 5.1.4. Reordering and Optimization by the Compiler and Runtime

## Concept Explanation

One of the most counter-intuitive aspects of concurrent programming is that the code you write is NOT necessarily
executed in the order you write it. Modern computing systems apply aggressive reordering optimizations at three
levels: the **compiler**, the **JIT runtime**, and the **CPU hardware**. The Java Memory Model explicitly permits
these reorderings — subject only to the constraint that they must not change the behavior of single-threaded code
and must respect happens-before relationships.

**Real-world analogy**: Think of a chef (the JVM) given a recipe (your source code). The chef is free to prepare
ingredients in any order they prefer — chopping vegetables while the oven preheats, for example — as long as the
final dish comes out the same. This freedom to reorder lets the chef work efficiently. But if two chefs are sharing
the kitchen (multi-threaded), they need explicit coordination rules (synchronization) to prevent one chef from
using ingredients the other hasn't finished preparing yet.

### Three Levels of Reordering

**1. Compiler reordering**: The Java compiler (javac) and JIT compiler (HotSpot C1/C2) may reorder instructions
within a method to:
- Better utilize CPU pipelines
- Reduce register pressure
- Enable constant folding, loop unrolling, inlining

Example: Two independent assignments may be swapped if neither depends on the other.

**2. JIT/Runtime reordering**: The JIT compiler performs more aggressive transformations like:
- Hoisting invariant computations out of loops
- Eliminating redundant reads (caching a value in a register)
- Dead code elimination

**3. CPU reordering**: Modern CPUs (especially weakly-ordered architectures like ARM, POWER) have sophisticated
out-of-order execution pipelines and write buffers that may reorder memory operations at the hardware level.
x86 CPUs provide a "total store order" (TSO) model that prevents many reorderings, which is why JMM bugs that
appear on ARM may not appear on x86.

### What the JMM Allows and Forbids

The JMM allows any reordering that does NOT:
1. Change the behavior as observed by the thread performing the operations (within-thread semantics).
2. Violate established happens-before relationships.

In practice, this means:
- **Without synchronization**: Threads may observe operations from other threads in almost any order.
- **With volatile**: Writes before a volatile write cannot be reordered to occur after it; reads after a
  volatile read cannot be reordered to occur before it.
- **With synchronized**: Operations inside a synchronized block cannot be moved outside it.

### Volatile and Reordering Prevention

The JMM's volatile semantics act as **memory barriers**:
- A volatile write prevents any preceding write from being moved after the volatile write.
- A volatile read prevents any following read from being moved before the volatile read.
- This creates a "fence" in the instruction stream that the compiler and hardware must respect.

## Key Points to Remember

1. **Three sources of reordering**: Java compiler (javac), JIT compiler (HotSpot), and CPU hardware.
2. **Reordering is legal** within single-threaded code — the JMM only constrains cross-thread visibility.
3. **volatile** creates memory barriers that prevent certain reorderings; it is the primary tool for
   controlling reordering without full mutual exclusion.
4. **synchronized** prevents all reorderings from crossing the block boundaries (acts as full barrier).
5. **Hoisting reads out of loops** is a common JIT optimization that causes the "infinite loop" bug
   with non-volatile flags.
6. **x86 is strongly ordered** (TSO) — bugs caused by reordering on ARM or POWER may not appear on x86,
   making them hard to reproduce in development.
7. **final fields** prevent their construction-time writes from being reordered after constructor completion,
   enabling safe publication.
8. **`@Contended` annotation** (JDK internal) prevents false sharing by padding fields that are accessed
   by different threads frequently.

## Relevant Java 21 Features

- **VarHandle memory access modes (Java 9+)**: Fine-grained reordering control:
  - `plain`: No ordering — compiler is free to reorder
  - `opaque`: No reordering within the same thread, but no cross-thread guarantee
  - `release/acquire`: Establish directed happens-before (like unlock/lock)
  - `volatile`: Full two-directional ordering (strongest)
  - This allows using the minimum barrier strength needed, improving performance in critical paths.
- **jdk.internal.vm.annotation.Contended**: Available as `@jdk.internal.vm.annotation.Contended`
  (JDK internal) or via JVM flags (`-XX:-RestrictContended`). Pads fields to prevent false sharing
  in high-performance concurrent code.
- **JEP 352 — Non-Volatile Mapped Byte Buffers**: Exposes NVM (non-volatile memory) access with explicit
  store ordering, relevant for persistence layers.

## Common Pitfalls and How to Avoid Them

1. **Loop flag not read from main memory (most common JMM bug)**

   ```java
   // BROKEN: JIT may hoist the 'running' read out of the loop
   // After hoisting: if (!running) { /* skipped */ } else { while(true) { /* infinite */ } }
   private boolean running = true;

   public void spinLoop() {
       while (running) { /* JIT may cache 'running' = true in a register */ }
   }
   ```

   **Fix**:
   ```java
   private volatile boolean running = true;
   ```

2. **Unsafe publication — reference visible before object is fully initialized**

   ```java
   // BROKEN: another thread may see a non-null 'data' but with data[0] = 0
   static int[] data;
   static void init() {
       data = new int[10]; // (1) reference stored
       data[0] = 42;       // (2) element set — may be reordered BEFORE (1)?
       // Actually: (2) cannot appear to other threads until after (1) with reordering
       // BUT: (2) may be reordered with OTHER prior operations
   }
   ```

   **Better example of unsafe publication**:
   ```java
   // BROKEN: 'config' reference may be seen without config.value being set
   static Config config;
   static void setConfig() {
       Config c = new Config();
       c.value = 42;    // write to field
       config = c;      // store of reference — may appear before c.value = 42 to other threads
   }
   ```

   **Fix**: Use volatile for `config`, or make `Config.value` final.

3. **Assuming x86 test results mean no JMM bug**

   ```java
   // "Works on my machine" (x86) but broken on ARM
   // x86's TSO model prevents most store-load reorderings that ARM permits
   // Always write code that is correct per the JMM spec, not per x86 behavior
   ```

4. **Misusing `@Contended` / false sharing**

   ```java
   // Adjacent fields in a class may share a cache line (typically 64 bytes)
   // Thread A writing field1 and Thread B writing field2 cause cache line bouncing
   class Counter {
       volatile long field1; // Thread A
       volatile long field2; // Thread B — shares cache line with field1!
   }
   ```

   **Fix**: Pad fields or use separate objects:
   ```java
   // Using separate objects prevents false sharing
   AtomicLong counter1 = new AtomicLong();
   AtomicLong counter2 = new AtomicLong();
   ```

## Best Practices and Optimization Techniques

1. **Understand that `volatile` is the reordering solution for simple visibility**: For scenarios where
   you only need to prevent the compiler from caching a variable in a register (e.g., a stop flag), `volatile`
   is sufficient and lightweight.
2. **Use `final` to anchor construction**: If all relevant state is set in a constructor and stored in final
   fields, the JMM guarantees no reordering will be visible to properly-publishing code.
3. **Prefer design over barriers**: Immutable objects, message-passing (BlockingQueue), and thread confinement
   eliminate the need for barriers altogether — they're safer and often faster.
4. **Profile before optimizing barriers**: Removing volatile or relaxing synchronization for performance is
   only justified if profiling shows it is a bottleneck. Premature optimization of synchronization is a
   major source of bugs.
5. **Use `LockSupport.fullFence()` for explicit barriers in library code**: `VarHandle.fullFence()` or
   `Unsafe.fullFence()` insert full memory barriers for cases where higher-level primitives don't fit.
6. **Consider cache line alignment for high-frequency counters**: In performance-critical code (e.g., Disruptor
   pattern), aligning frequently-written fields to separate cache lines prevents false sharing.

## Edge Cases and Their Handling

1. **Compiler hoisting of loop conditions**: The JIT may hoist a non-volatile field read out of a loop body,
   converting `while(flag)` into `if(flag) { while(true) }`. This is correct per the JMM if flag is not
   volatile. The fix is always `volatile boolean flag`.

2. **Instruction scheduling around object construction**: The JIT may reorder the store of the object reference
   and the stores of its fields. This is why the broken double-checked locking pattern can return partially
   constructed objects. Using volatile on the reference prevents this reordering.

3. **Processor write buffers**: Writes may sit in a CPU write buffer before reaching the cache/memory. A
   `StoreLoad` barrier (the most expensive barrier) forces the write buffer to drain. This is what volatile
   writes on x86 translate to — a `mfence` or `lock xchg` instruction.

4. **False sharing in arrays**: Adjacent array elements may share a cache line. If multiple threads write
   to adjacent elements, they will contend on the same cache line even though they're writing different
   data. Solution: use padding or access patterns that spread writes across cache lines.

## Interview-specific Insights

Interviewers focus on:
- **Understanding that code doesn't always execute in written order** — this is a mindset shift for many developers
- **Practical impact**: The infinite loop from hoisted non-volatile flag is a classic question
- **Why volatile works**: Understanding memory barriers, not just "volatile means visible"
- **Platform differences**: x86 vs. ARM behavior and why JMM correctness matters regardless
- **False sharing**: A performance concern that demonstrates understanding of cache architecture

Common tricky questions:
- "Write a correct, lazily initialized singleton without using volatile" (Initialization-on-Demand Holder)
- "Can a correctly synchronized program still experience performance degradation from reordering?" (Yes —
  false sharing causes cache line bouncing even without correctness issues)
- "What is the cost of a volatile write on x86?" (A store with `lock` prefix or `mfence` — significantly
  more expensive than a regular store)

## Interview Q&A Section

**Q1: What is instruction reordering and why does the JVM permit it?**

```text
A1: Instruction reordering is the act of executing operations in a different order than they appear in source code.

WHY IT'S PERMITTED:
Modern computing systems have multiple levels of optimization:
1. The Java compiler (javac) may reorder bytecode instructions.
2. The JIT compiler (HotSpot) applies aggressive optimizations: inlining, loop unrolling, register allocation,
   common subexpression elimination — all of which may change the execution order.
3. The CPU hardware has out-of-order execution engines, write buffers, and store-load reordering capabilities
   that change the apparent execution order from the perspective of other CPUs.

WHY IT'S SAFE FOR SINGLE-THREADED CODE:
The JMM guarantees that within a single thread, the behavior is AS IF all operations executed in program order.
Reorderings are not visible to the executing thread — they only become visible to OTHER threads.

WHY IT MATTERS FOR MULTI-THREADED CODE:
When two threads share data without synchronization, one thread may observe the effects of another thread's
operations in a different order than they appear in the source code. This can lead to correctness bugs that
are extremely hard to reproduce and debug.
```

```java
public class ReorderingDemo {
    int a = 0;
    int b = 0;

    // Thread A executes this:
    public void writerA() {
        a = 1;  // (1)
        b = 1;  // (2)
        // (1) and (2) may be reordered by the JIT or CPU
        // Thread B may see b=1 before a=1, or see a=1 and b=0 simultaneously
    }

    // Thread B executes this:
    public void readerB() {
        int rb = b;  // (3) reads b
        int ra = a;  // (4) reads a
        // Without synchronization, Thread B may see rb=1, ra=0
        // (b updated, a not yet — due to reordering of (1) and (2))
    }

    // Fix: declare both a and b as volatile
    // Or use synchronized blocks
    // Or redesign to avoid shared mutable state
}
```

**Q2: How does `volatile` prevent reordering?**

```text
A2: volatile establishes two categories of reordering restrictions (memory barriers):

WRITE BARRIER (before volatile write):
- No write that appears BEFORE the volatile write in program order can be reordered to appear AFTER it.
- Called a "release" fence or "StoreStore + StoreLoad" barrier.
- Effect: All prior stores are flushed and visible before the volatile store.

READ BARRIER (after volatile read):
- No read that appears AFTER the volatile read in program order can be reordered to appear BEFORE it.
- Called an "acquire" fence or "LoadLoad + LoadStore" barrier.
- Effect: All subsequent loads read from the latest values in main memory.

COMBINED EFFECT (happens-before):
- A volatile write X happens-before a subsequent volatile read Y of the same variable.
- All writes before X are also visible to the thread doing Y (by transitivity).

This is why the "writer sets data before volatile flag, reader checks volatile flag before reading data"
pattern works correctly for communication between threads.
```

```java
public class VolatileReorderingDemo {
    private int result = 0;      // non-volatile — write may be reordered
    private volatile boolean ready = false; // volatile — barrier

    // Thread A
    public void produce() {
        result = 42;     // (1) non-volatile write
        ready = true;    // (2) VOLATILE WRITE — creates StoreStore barrier before here
                         // (1) is guaranteed to happen-before (2)
                         // (2) is flushed to main memory before the volatile store completes
    }

    // Thread B
    public void consume() {
        while (!ready) { } // (3) VOLATILE READ — creates LoadLoad barrier after here
                           // (2) happens-before (3) by volatile semantics
                           // All writes before (2) are visible after (3)
        System.out.println(result); // (4) guaranteed to see result = 42
                                    // because (1) hb (2) hb (3) hb (4)
    }
}
```

**Q3: What is false sharing and how does it affect performance in concurrent Java programs?**

```text
A3: False sharing occurs when two or more threads frequently write to different variables that happen to
reside on the same CPU cache line (typically 64 bytes).

HOW IT HAPPENS:
CPUs manage cache in units called cache lines (64 bytes on modern x86/ARM). When Thread A writes to a
variable on cache line X, the cache coherency protocol (e.g., MESI) invalidates ALL copies of cache line X
on all other CPUs. When Thread B then accesses ITS variable (which happens to be on the same cache line),
it must fetch the cache line again from main memory or another CPU's cache.

This constant invalidation and re-fetching is the "false sharing" — the threads are NOT logically sharing
data, but are forced to share at the cache line level due to physical proximity.

IMPACT:
- Can reduce multi-threaded performance by 10-100x compared to the sequential version
- Shows up as high cache miss rates in profiling tools (like Linux perf or JFR)
- Classic example: multiple counters in an array, one per thread

SOLUTIONS:
1. Padding: Add enough padding fields around hot variables to push them to separate cache lines.
2. @Contended: JDK-internal annotation that pads the annotated field to its own cache line.
3. Separate objects: Each thread works with its own object, rather than adjacent fields/array elements.
4. LongAdder: Uses cell-based padding internally to avoid false sharing across concurrent increments.
```

```java
public class FalseSharingDemo {
    // BAD: adjacent longs likely share a 64-byte cache line
    // Two threads writing counter1 and counter2 simultaneously cause contention
    static class FalselyShared {
        volatile long counter1 = 0; // Thread A writes here
        volatile long counter2 = 0; // Thread B writes here — shares cache line!
    }

    // GOOD: LongAdder handles padding internally
    java.util.concurrent.atomic.LongAdder adder1 = new java.util.concurrent.atomic.LongAdder();
    java.util.concurrent.atomic.LongAdder adder2 = new java.util.concurrent.atomic.LongAdder();

    // GOOD: padding via separate fields (manual, 64-byte cache line)
    static class PaddedCounter {
        long p1, p2, p3, p4, p5, p6, p7;  // 7 * 8 = 56 bytes padding before
        volatile long counter = 0;          // 8 bytes — own cache line
        long q1, q2, q3, q4, q5, q6, q7;  // 7 * 8 = 56 bytes padding after
    }

    // GOOD in JDK: @jdk.internal.vm.annotation.Contended
    // (requires --add-opens and -XX:-RestrictContended JVM flags)
    static class ContendedCounter {
        // @jdk.internal.vm.annotation.Contended
        volatile long counter = 0; // would be padded to own cache line with annotation
    }
}
```

**Q4: Describe a concrete scenario where instruction reordering caused a real bug.**

```text
A4: The most famous real-world JMM reordering bug is the broken double-checked locking pattern (pre-Java 5):

    static Singleton instance;
    static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton(); // BUG IS HERE
                }
            }
        }
        return instance;
    }

The object construction `instance = new Singleton()` conceptually involves three steps:
1. Allocate memory for Singleton object
2. Call Singleton() constructor to initialize fields
3. Store the reference in 'instance'

The JIT/CPU is allowed to reorder steps 2 and 3:
- Thread A executes steps 1 and 3 (allocates memory, stores reference) but hasn't run the constructor yet
- Thread B enters getInstance(), sees instance != null (first check passes)
- Thread B returns the reference and starts using the UNINITIALIZED object
- Thread A finally runs the constructor — but Thread B already saw stale/default values

This was a real bug in production code before Java 5. The fix (volatile instance) was documented in
JSR-133 (2004) and implemented in Java 5 (2004).

A modern equivalent scenario: unsafe publication of a mutable configuration object in a microservice.
A background thread updates a config reference while request handler threads read it — without volatile,
handlers may see a stale config or a partially initialized one.
```

```java
// The canonical broken DCL (illustrative — do NOT use)
public class BrokenDCL {
    private static BrokenDCL instance; // NOT volatile — broken!

    private int initialized = 0;

    public BrokenDCL() {
        initialized = 1; // constructor writes may be reordered with reference store
    }

    // Thread B may get a BrokenDCL with initialized == 0
    public static BrokenDCL getBrokenInstance() {
        if (instance == null) {
            synchronized (BrokenDCL.class) {
                if (instance == null) {
                    instance = new BrokenDCL();
                }
            }
        }
        return instance;
    }
}

// The correct version
public class CorrectDCL {
    private static volatile CorrectDCL instance; // volatile prevents reordering

    private int initialized = 0;

    public CorrectDCL() {
        initialized = 1;
    }

    public static CorrectDCL getInstance() {
        if (instance == null) {
            synchronized (CorrectDCL.class) {
                if (instance == null) {
                    instance = new CorrectDCL(); // volatile write — full barrier
                }
            }
        }
        return instance;
    }

    public int getInitialized() { return initialized; }
}
```

**Q5: How does the JVM prevent reordering within a synchronized block?**

```text
A5: The JVM implements synchronized blocks using memory barrier instructions at the entry and exit points
of the block:

ON ENTRY (monitor acquire):
- A LoadLoad barrier prevents any reads inside the block from being reordered BEFORE the lock acquire.
- A LoadStore barrier prevents any writes inside the block from being reordered BEFORE the lock acquire.
- Combined effect: The thread refreshes its view of all shared memory on entry.

ON EXIT (monitor release):
- A StoreStore barrier prevents any writes inside the block from being reordered AFTER the lock release.
- A StoreLoad barrier prevents any loads inside the block from being reordered AFTER the lock release.
- Combined effect: All writes made inside the block are flushed to main memory and visible to the next
  thread that acquires the same monitor.

This is the "synchronization order" guarantee: there is a total order over all lock/unlock operations in
a program, and each unlock happens-before the next lock of the same monitor in this total order.

From a practical perspective, the JVM may translate monitor exit to a `mfence` or `lock addl` instruction
on x86, which is an expensive full barrier. This is why minimizing lock contention (using fine-grained locks,
lock-free data structures, or immutable objects) is important for performance.
```

```java
public class SynchronizedBarrierDemo {
    private int shared1 = 0;
    private int shared2 = 0;
    private final Object monitor = new Object();

    public void writer() {
        int local = computeExpensive(); // may execute outside synchronized
        synchronized (monitor) {        // ENTRY: LoadLoad + LoadStore barrier
            shared1 = local;            // (1) cannot move before entry
            shared2 = local * 2;        // (2) cannot move before entry
        }                               // EXIT: StoreStore + StoreLoad barrier
        // (1) and (2) are flushed to main memory on exit
        // They cannot be moved after exit
    }

    public int reader() {
        int result;
        synchronized (monitor) {    // ENTRY: refreshes working memory from main memory
            result = shared1 + shared2; // sees writes from writer() if lock was released first
        }                           // EXIT
        return result;
    }

    private int computeExpensive() { return 21; }
}
```

## Code Examples

- Source: [InstructionReordering.java](src/main/java/com/github/msorkhpar/claudejavatutor/javamemorymodel/InstructionReordering.java)
- Test: [InstructionReorderingTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/javamemorymodel/InstructionReorderingTest.java)
