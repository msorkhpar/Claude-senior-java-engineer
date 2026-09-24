# 5.1.2. Shared Memory and Thread-Local Memory

## Concept Explanation

A common way to picture the Java Memory Model divides memory into two regions: **main memory** (shared among all
threads) and **working memory** (thread-local, private to each thread). The pre-Java 5 specification was written in
these terms; the current JMM (JLS §17.4) is defined through happens-before instead and mentions no caches, but the
picture predicts the same failures and is fundamental to diagnosing and preventing concurrency bugs.

**Real-world analogy**: Think of a large city library (main memory) and each citizen's personal notebook (working
memory / CPU cache). When you look up a book in the library, you copy its contents into your notebook. You work from
your notebook until you decide to check the library again for updates or write your changes back. Without a protocol
for how often you update your notebook or post changes back to the library, two citizens working on the same topic
in their notebooks may end up with conflicting, inconsistent views of the truth.

### Main Memory (Heap)
Main memory in the JMM conceptually contains:
- **Instance fields** of objects on the heap
- **Static fields** of classes
- **Array elements**

Main memory is the "source of truth" but is NOT directly accessed by threads during execution. Threads copy values
into their working memory, operate on the copies, and may later flush changes back.

### Working Memory (Thread-Local / CPU Cache)
Each thread has its own working memory containing:
- **Local variables** (method parameters, loop variables, variables declared inside methods)
- **Cached copies** of heap variables the thread has read

**Critical insight**: Local variables are ALWAYS thread-local. They live on the thread's stack frame and are never
shared between threads. There is no JMM visibility concern for local variables. The concerns arise only when sharing
data through heap objects.

### When Does a Thread See Updates From Another Thread?
Without synchronization: **never guaranteed**. The JMM allows a thread to keep a stale cached copy of a heap
variable indefinitely. This is why the JMM visibility rules (volatile, synchronized, etc.) are essential.

### volatile and Main Memory
In the working-memory picture, a `volatile` field behaves as if every read came from main memory and every write went
straight to it. What the JLS actually guarantees (§17.4.4–17.4.5): all volatile accesses fall in one total
synchronization order, a volatile read sees the last write to that field before it in that order, and a volatile write
happens-before every later read of the field — which also makes the writer's earlier writes visible. Real hardware keeps
caches coherent anyway; the JVM implements volatile by restricting compiler reordering and emitting memory barriers,
not by bypassing CPU caches.

## Key Points to Remember

1. **Local variables are always thread-local** — they live on the stack and are never shared. No JMM concern.
2. **Heap variables** (instance fields, static fields, array elements) are potentially shared and subject to JMM rules.
3. Threads operate on **cached copies** of heap variables in their working memory (registers/caches).
4. Without synchronization, there is **no guarantee** of when (or if) one thread's write is visible to another.
5. **volatile** fields behave as if they bypassed the working memory (reads from, writes to main memory); in reality
   they are implemented with memory barriers and compiler restrictions, not by bypassing CPU caches.
6. The working memory concept is **logical** — it maps to CPU caches, registers, and compiler optimizations in
   practice.
7. Even on modern hardware where caches are coherent (x86), the JMM still permits reordering that can cause
   visibility failures without proper synchronization.
8. **long and double** fields without `volatile` may not be atomically read/written on 32-bit JVMs.

## Relevant Java 21 Features

- **VarHandle (Java 9+)**: Provides fine-grained control over memory access modes: plain (no ordering), opaque
  (atomic and coherent per variable, eventually visible, but no ordering with other variables), release/acquire
  (ordering between a release write and the acquire read that sees it, without full sequential consistency), and
  volatile (full JMM volatile semantics). This allows you to use the minimum ordering needed for correctness.
- **Virtual Threads (Java 21)**: Each virtual thread has its own stack (and therefore its own local variables).
  Virtual threads share the heap just like platform threads, so the same JMM rules apply.
- **Sequenced Collections (Java 21)**: `LinkedHashMap.sequencedEntrySet()` etc. — these are thread-unsafe by default.
  Understanding shared vs. thread-local memory helps you use `ConcurrentHashMap` when appropriate.

## Common Pitfalls and How to Avoid Them

1. **Reading a stale value from working memory**

   ```java
   // BROKEN: 'running' may be cached in Thread B's working memory as 'true' forever
   class BrokenFlag {
       private boolean running = true;

       void stop() { running = false; } // Thread A

       void work() { // Thread B
           while (running) { /* may spin forever */ }
       }
   }
   ```

   **Fix**: Use `volatile boolean running = true;` to force main-memory reads.

2. **Assuming writes propagate immediately**

   ```java
   // BROKEN: Thread B may see counter as 0 even after Thread A increments it
   class BrokenCounter {
       private int counter = 0;

       void increment() { counter++; } // Thread A
       int get() { return counter; }   // Thread B
   }
   ```

   **Fix**: Use `AtomicInteger` or `synchronized` methods.

3. **Treating local variables as shared** (false concern)

   ```java
   // FINE: local variable 'sum' is on Thread A's stack — no sharing possible
   public void processLocally(List<Integer> data) {
       int sum = 0; // local — thread-local by definition
       for (int v : data) sum += v;
       System.out.println(sum);
   }
   ```

4. **Unsafe publication through non-volatile fields**

   ```java
   // BROKEN: other threads may see helper as null or partially constructed
   static Helper helper;
   static void init() { helper = new Helper(); }
   ```

   **Fix**: Use `volatile static Helper helper;` or `static final Helper helper = new Helper();`.

## Best Practices and Optimization Techniques

1. **Minimize shared mutable state**: The fewer heap variables shared between threads, the fewer JMM concerns.
   Use thread-local data via `ThreadLocal<T>` or pass data through method parameters instead of shared fields.
2. **Use `ThreadLocal` for per-thread state**: `ThreadLocal<T>` gives each thread its own copy of a variable,
   eliminating sharing entirely:
   ```java
   private static final ThreadLocal<SimpleDateFormat> formatter =
       ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
   ```
3. **Prefer final fields for immutable data**: Final fields get special JMM publication guarantees at zero runtime
   cost. Design immutable value objects to maximize thread safety without synchronization.
4. **Use `volatile` conservatively**: `volatile` has a cost (prevents certain compiler optimizations, requires memory
   barriers). Use it only where visibility across threads is truly needed.
5. **Use `AtomicReference` for CAS-based publication**: When you need to atomically update an object reference,
   `AtomicReference.compareAndSet()` provides both atomicity and visibility.

## Edge Cases and Their Handling

1. **ThreadLocal and memory leaks**: `ThreadLocal` values are kept alive as long as the thread is alive. In thread
   pools (where threads are long-lived and reused), forgetting to call `threadLocal.remove()` can cause memory leaks
   and incorrect state sharing between tasks.
2. **Inheritance of thread-local values**: `InheritableThreadLocal` copies the parent thread's values into a child
   thread at creation time. Changes after creation are not shared. Useful for propagating context (e.g., security
   context) to spawned threads, but can cause subtle bugs if not understood.
3. **Long/double atomicity**: Even though modern 64-bit JVMs implement 64-bit atomic loads and stores, the JMM
   specification does NOT require it for non-volatile long/double. Always use `volatile long` or `AtomicLong` for
   shared 64-bit values if you need guaranteed atomicity.
4. **Array elements**: Each element of a shared array is a separate heap variable. A volatile array reference does
   NOT make the elements volatile — you need `AtomicIntegerArray`, `AtomicLongArray`, or explicit synchronization
   for volatile-like semantics on array elements.

## Interview-specific Insights

Interviewers focus on:
- The **conceptual distinction** between stack (thread-local) and heap (shared) memory
- Why you **cannot rely on implicit visibility** of heap writes between threads
- How **volatile** solves the visibility problem and at what cost
- Understanding of **ThreadLocal** and its proper usage and pitfalls
- Common mistakes like caching stale values or unsafe publication

Common tricky questions:
- "If a variable is on the heap, does every thread always see the latest value?" (No — without synchronization, no)
- "Can two threads share a local variable?" (No — each thread running the method has its own copy on its own stack;
  the object a local variable refers to can still be shared)
- "Is `volatile` sufficient for `i++`?" (No — `i++` is a compound read-modify-write operation)

## Interview Q&A Section

**Q1: What is the difference between heap memory and stack memory in the context of thread safety?**

```text
A1: In the JMM's conceptual model:

STACK MEMORY (thread-local):
- Each thread has its own stack. Method calls push frames onto the stack, each containing local variables,
  parameters, and return values.
- Stack memory is NEVER shared between threads (each thread has its own stack).
- Therefore, local variables are inherently thread-safe — no synchronization needed.

HEAP MEMORY (shared):
- All objects and their fields live on the heap, which is shared among all threads.
- Instance fields, static fields, and array elements are all on the heap.
- Because the heap is shared, concurrent access requires proper synchronization to ensure correct visibility.

A common interview mistake is to confuse "on the heap" with "thread-safe." Just because an object is on the heap
doesn't make it thread-safe — it means it CAN be shared and therefore MUST be protected if actually shared.

Object references (the variables pointing to objects) can live on either the stack (if local) or the heap (if
they are fields of another object). The object's FIELDS always live on the heap.
```

```java
public class StackVsHeapDemo {
    private int sharedField = 0; // Heap — shared, requires synchronization

    public void threadSafeMethod() {
        int localVar = 10;       // Stack — thread-local, no concern
        String localStr = "hi";  // Stack reference — but the String object is on heap
        // localStr is still effectively thread-local here (not shared with other threads)

        sharedField = localVar;  // Writing to heap — potential visibility issue
    }
}
```

**Q2: How does `volatile` affect thread-local working memory?**

```text
A2: When a field is declared volatile, the JMM mandates:

In the working-memory picture:
WRITE: a write to a volatile field cannot stay in the thread's working memory — it behaves as if written to main
memory right away.
READ: a read of a volatile field cannot be served from a stale cached copy — it behaves as if read from main memory.

What the JLS actually specifies (and what the picture approximates): volatile accesses are totally ordered, each
volatile read sees the last write to that field before it in that order, and a volatile write happens-before any
subsequent volatile read of the same variable. This means all writes the writer made before the volatile write
are also visible to the reading thread. The JLS says nothing about caches or main memory.

Non-volatile reads: The JVM is free to cache the value in the thread's working memory and return the
cached copy on subsequent reads — even if another thread has updated main memory.

Cost of volatile: Volatile reads/writes are more expensive than non-volatile because they need memory barriers
(on x86, a volatile write is followed by a locked instruction or mfence; volatile reads are almost free) and
prevent certain compiler/JIT optimizations — not because they bypass CPU caches, which stay coherent. They are
usually cheaper than synchronized blocks.
```

```java
public class VolatileWorkingMemoryDemo {
    private volatile boolean flag = false;
    private int data = 0;

    public void writer() {
        data = 42;        // (1) may stay in writer's working memory temporarily
        flag = true;      // (2) volatile write: publishes (1) too — (1) cannot move after (2)
                          // happens-before any subsequent read of 'flag'
    }

    public void reader() {
        if (flag) {       // (3) volatile read: sees latest main memory value
            // By happens-before: (2) happened-before (3)
            // By program order: (1) happened-before (2)
            // Therefore: reader sees data = 42 (guaranteed)
            System.out.println(data);
        }
    }
}
```

**Q3: What is `ThreadLocal` and when should you use it?**

```text
A3: ThreadLocal<T> is a Java utility class that provides per-thread storage — each thread that accesses
a ThreadLocal variable gets its own independently initialized copy of the variable.

Key characteristics:
- Eliminates sharing by giving each thread its own copy
- The value is associated with the thread, not the ThreadLocal object
- Automatically garbage-collected when the thread terminates (unless using thread pools)

When to use:
1. Per-thread state that must not be shared (e.g., SimpleDateFormat instances, database connections)
2. Propagating context through call stacks without method signature changes (e.g., user session, transaction ID)
3. Per-thread caching (e.g., reusing expensive-to-create objects across method calls within a single thread)

When NOT to use:
1. When you actually want to share state between threads (use concurrent collections instead)
2. In thread pools without calling remove() — can cause memory leaks and state pollution between tasks
3. As a substitute for proper design (hiding shared state in ThreadLocal is often a design smell)
```

```java
public class ThreadLocalDemo {
    // Each thread gets its own SimpleDateFormat (not thread-safe otherwise)
    private static final ThreadLocal<SimpleDateFormat> dateFormat =
        ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    // Context propagation
    private static final ThreadLocal<String> requestId = new ThreadLocal<>();

    public static void processRequest(String id) {
        requestId.set(id); // set per-thread context
        try {
            doWork(); // downstream calls can read requestId without passing it as a param
        } finally {
            requestId.remove(); // CRITICAL in thread pools to prevent stale state
        }
    }

    private static void doWork() {
        String id = requestId.get(); // reads THIS thread's value
        System.out.println("Processing request: " + id);
    }

    public static String formatDate(java.util.Date date) {
        return dateFormat.get().format(date); // thread-safe per-thread SDF
    }
}
```

**Q4: Can two threads safely read from a shared object without synchronization?**

```text
A4: Yes, with conditions:

SAFE (no synchronization needed):
1. The shared object is IMMUTABLE (all fields final, no mutating methods). Final fields get JMM
   publication guarantees after construction completes.
2. The object was safely published (via volatile, synchronized, final field, or static initializer)
   and is never mutated after publication.
3. Concurrent reads from java.util.concurrent classes designed for concurrent access (e.g., ConcurrentHashMap).

UNSAFE:
- If one thread is writing while another is reading (even if both are "just reading a field"), and the
  field is not volatile/synchronized, the reading thread may see a stale value.
- If the object was published unsafely (via a plain non-volatile field), even concurrent reads may see
  a partially constructed object.

The JMM allows compilers to cache values aggressively. Even if the hardware cache is coherent, the
compiler may have placed the value in a register and never re-read from memory.
```

```java
public class ConcurrentReadDemo {
    // SAFE: immutable, safely published via final field
    private static final ImmutableConfig CONFIG = new ImmutableConfig("host", 8080);

    // UNSAFE: mutable, not safely published
    private static MutableConfig mutableConfig;

    static class ImmutableConfig {
        private final String host; // final — JMM guaranteed
        private final int port;

        ImmutableConfig(String host, int port) {
            this.host = host;
            this.port = port;
        }
        public String getHost() { return host; }
        public int getPort() { return port; }
    }

    static class MutableConfig {
        String host;
        int port;
    }

    // Multiple threads can safely read CONFIG concurrently
    public static String getHost() { return CONFIG.getHost(); }

    // Multiple threads reading mutableConfig: UNSAFE without synchronization
    public static String getMutableHost() { return mutableConfig.host; }
}
```

**Q5: What is the danger of `long` and `double` fields in multi-threaded code?**

```text
A5: The JMM specification (JLS 17.7) states that writes to non-volatile long and double values are
NOT required to be atomic. On 32-bit JVMs or 32-bit data buses, a 64-bit write may be split into
two 32-bit writes. If one thread is writing and another is reading concurrently, the reader might
see a "half-written" value — the high 32 bits from the old value and the low 32 bits from the new
value (or vice versa), resulting in a completely bogus number.

In practice:
- Modern 64-bit JVMs (which are now the overwhelming majority) DO perform atomic 64-bit reads and writes,
  so this is rarely seen in practice.
- However, the JMM specification does NOT guarantee it, so technically any non-volatile long/double
  access may tear on an implementation that uses this freedom — and such an implementation still conforms
  to the specification.
- For correctness and portability: always use volatile long/double or AtomicLong/AtomicReference for
  shared 64-bit values.

Note: volatile long and double ARE guaranteed to be atomically read/written per the JMM spec.
```

```java
public class LongDoubleAtomicityDemo {
    // RISKY: on 32-bit JVMs, read/write may not be atomic
    private long riskyLong = 0L;
    private double riskyDouble = 0.0;

    // SAFE: volatile guarantees atomic 64-bit read/write
    private volatile long safeLong = 0L;
    private volatile double safeDouble = 0.0;

    // BEST: AtomicLong provides atomic CAS and increment operations
    private final java.util.concurrent.atomic.AtomicLong atomicLong =
        new java.util.concurrent.atomic.AtomicLong(0L);

    public void demonstrateSafe() {
        // Multiple threads can safely read/write these
        safeLong = Long.MAX_VALUE;
        long read = safeLong; // guaranteed to be atomic read

        atomicLong.incrementAndGet(); // atomic increment (CAS-based)
    }
}
```

## Code Examples

- Source: [SharedMemory.java](src/main/java/com/github/msorkhpar/claudejavatutor/javamemorymodel/SharedMemory.java)
- Test: [SharedMemoryTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/javamemorymodel/SharedMemoryTest.java)
