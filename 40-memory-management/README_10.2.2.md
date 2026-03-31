# 10.2.2. Garbage Collection Algorithms

## Concept Explanation

Garbage collection (GC) is the process by which the JVM automatically reclaims memory occupied by objects that are no
longer reachable from the application. Understanding GC algorithms is critical for senior engineers because GC behavior
directly impacts application latency, throughput, and memory efficiency.

**Real-world analogy**: Imagine a library (the heap) where patrons (threads) borrow books (objects). Over time, some
books are returned and shelved, but others are left on tables with no one reading them. The librarian (garbage collector)
periodically walks through the library, identifies books that no one is using (unreachable objects), and puts them back
on the shelf (frees memory). Different librarians use different strategies: some do a quick scan of recently placed books
(minor GC), while others do a full inventory of the entire library (major GC).

### Mark-and-Sweep Algorithm

The foundational GC algorithm. It works in two phases:

1. **Mark Phase**: Starting from GC roots (local variables, static fields, active threads, JNI references), the
   collector traverses the object graph and marks every reachable object.
2. **Sweep Phase**: The collector scans the heap and frees memory occupied by unmarked (unreachable) objects.

The key insight is that circular references between unreachable objects are still collected, because the mark phase only
follows references starting from roots.

### Mark-and-Compact

An improvement over mark-and-sweep that adds a **compaction** phase. After sweeping, surviving objects are moved together
to eliminate memory fragmentation. This makes future allocation faster (bump-pointer allocation) but the compaction step
is expensive.

### Generational Garbage Collection

Based on the **weak generational hypothesis**: most objects die young. The heap is divided into generations:

- **Young Generation**: Where new objects are allocated
    - **Eden Space**: Initial allocation area
    - **Survivor Spaces (S0, S1)**: Objects that survive a young GC are copied here
- **Old Generation (Tenured)**: Objects that survive multiple young GCs are promoted here
- **Metaspace** (since Java 8): Stores class metadata (replaced PermGen)

**Minor GC** collects the young generation (fast, frequent). **Major GC** (or Full GC) collects the entire heap
(slow, infrequent).

### Modern GC Implementations

Java 21 includes several garbage collectors:

| Collector     | Type           | Key Characteristic                              |
|---------------|----------------|------------------------------------------------|
| Serial GC     | Stop-the-world | Single-threaded, for small heaps (<100MB)       |
| Parallel GC   | Stop-the-world | Multi-threaded, maximizes throughput             |
| G1 GC         | Mostly concurrent | Region-based, balances latency and throughput (default since Java 9) |
| ZGC           | Concurrent     | Sub-millisecond pauses, scales to TB heaps      |
| Shenandoah    | Concurrent     | Low-pause, concurrent compaction                 |

## Key Points to Remember

1. **GC roots** include: local variables in active threads, static fields, JNI references, active monitor locks, and
   class objects loaded by system classloader.
2. **Mark-and-sweep** correctly handles circular references -- only reachability from roots matters.
3. **Generational GC** exploits the fact that most objects are short-lived, making young generation collection very fast.
4. **Minor GC** is typically 10-100x faster than Major GC because the young generation is small.
5. **Stop-the-world pauses** freeze all application threads during GC; modern collectors minimize these.
6. **G1 GC** is the default collector since Java 9 and divides the heap into equal-sized regions rather than
   contiguous generations.
7. **ZGC** (Java 21) achieves sub-millisecond pause times regardless of heap size, using colored pointers and
   load barriers.
8. **`System.gc()`** is a suggestion, not a command -- the JVM may ignore it.
9. **Finalization** (`finalize()`) is deprecated; use `Cleaner` or `PhantomReference` for cleanup.
10. **GC tuning** should be data-driven: measure with GC logs, JFR, or monitoring tools before changing defaults.

## Relevant Java 21 Features

- **Generational ZGC (JEP 439)**: ZGC now supports generational collection, combining ultra-low pause times with the
  throughput benefits of generational collection. Enabled with `-XX:+UseZGC -XX:+ZGenerational` (default in future
  releases).
- **Deprecation of finalize()**: `Object.finalize()` is deprecated for removal. Use `java.lang.ref.Cleaner` instead.
- **Improved G1 region pinning**: G1 can now pin regions during JNI critical sections, avoiding full GC pauses.
- **Compact Object Headers (Lilliput)**: Experimental support for reducing object header size, directly reducing GC
  pressure by making objects smaller.
- **GC logging unification**: All collectors use the unified GC logging framework (`-Xlog:gc*`).

## Common Pitfalls and How to Avoid Them

1. **Calling `System.gc()` in production code**

   ```java
   // Problem: Triggering full GC causes stop-the-world pause
   public void clearCache() {
       cache.clear();
       System.gc(); // DON'T DO THIS -- causes unpredictable pauses
   }
   
   // Fix: Let the GC manage itself; clear references instead
   public void clearCache() {
       cache.clear(); // sufficient -- GC will collect when needed
   }
   ```

2. **Relying on `finalize()` for resource cleanup**

   ```java
   // Problem: finalize() is unreliable, deprecated, and causes GC issues
   public class OldStyle {
       @Override
       protected void finalize() throws Throwable {
           closeNativeResource(); // may never be called!
       }
   }
   
   // Fix: Use AutoCloseable + try-with-resources, or Cleaner
   public class ModernStyle implements AutoCloseable {
       private static final Cleaner cleaner = Cleaner.create();
       private final Cleaner.Cleanable cleanable;
       
       public ModernStyle() {
           cleanable = cleaner.register(this, () -> {
               // cleanup action -- runs after object becomes phantom-reachable
           });
       }
       
       @Override
       public void close() {
           cleanable.clean(); // explicit cleanup
       }
   }
   ```

3. **Ignoring GC overhead in throughput calculations**

   ```java
   // Problem: Measuring only application time, ignoring GC pauses
   long start = System.nanoTime();
   processData(hugeDataset);
   long elapsed = System.nanoTime() - start; // includes GC pauses!
   
   // Fix: Use JFR or GC logs to separate application time from GC time
   // Run with: -Xlog:gc*:file=gc.log:time,uptime,level,tags
   ```

4. **Creating too many short-lived large objects**

   ```java
   // Problem: Large objects bypass Eden and go directly to Old Gen
   public void processItems(List<Item> items) {
       for (Item item : items) {
           byte[] buffer = new byte[10_000_000]; // 10MB per iteration!
           process(item, buffer);
       }
   }
   
   // Fix: Reuse buffers
   public void processItems(List<Item> items) {
       byte[] buffer = new byte[10_000_000]; // allocate once
       for (Item item : items) {
           Arrays.fill(buffer, (byte) 0);
           process(item, buffer);
       }
   }
   ```

5. **Premature GC tuning without measurement**

   ```java
   // Problem: Blindly setting GC flags without understanding the workload
   // -XX:+UseG1GC -XX:MaxGCPauseMillis=10 -XX:G1HeapRegionSize=32m
   
   // Fix: Measure first, tune second
   // 1. Enable GC logging: -Xlog:gc*:file=gc.log
   // 2. Analyze with tools: GCViewer, gceasy.io, JFR
   // 3. Identify whether latency or throughput is the priority
   // 4. Make targeted changes based on data
   ```

## Best Practices and Optimization Techniques

1. **Start with defaults** -- G1 GC is well-tuned for most workloads. Only switch collectors if you have specific
   latency or throughput requirements.
2. **Enable GC logging in production**: `-Xlog:gc*:file=gc.log:time,uptime,level,tags:filecount=5,filesize=100m`
3. **Set heap size explicitly**: `-Xms` and `-Xmx` should be equal in production to avoid heap resizing pauses.
4. **Prefer object reuse** in hot paths: buffer pools, `StringBuilder` reuse, flyweight pattern.
5. **Avoid premature promotion**: If objects are promoted too quickly, tune `-XX:MaxTenuringThreshold`.
6. **Monitor GC metrics**: Track GC pause time, frequency, and throughput ratio using JMX or Prometheus exporters.
7. **Use ZGC** for latency-sensitive applications requiring sub-millisecond pauses.
8. **Use Parallel GC** for batch processing jobs where throughput matters more than latency.
9. **Size the young generation** appropriately: too small causes frequent minor GCs; too large causes long minor GC
   pauses.
10. **Profile before optimizing**: Use JFR (`-XX:StartFlightRecording`) or async-profiler to identify actual allocation
    hot spots.

## Edge Cases and Their Handling

1. **GC during class unloading**: Classes can be unloaded during a full GC if their classloader is unreachable. This
   affects Metaspace usage.
2. **Humongous objects in G1**: Objects larger than half a G1 region are "humongous" and allocated specially. They skip
   the young generation entirely.
3. **GC with off-heap memory**: Direct ByteBuffers (`ByteBuffer.allocateDirect()`) use off-heap memory not tracked by
   the GC. A Cleaner frees them when the ByteBuffer object is collected.
4. **Concurrent mode failure**: In G1/CMS, if the old generation fills up before concurrent collection completes, a
   full stop-the-world GC is triggered.
5. **JNI and GC roots**: Objects passed to native code via JNI become GC roots and cannot be collected until released.
6. **Soft reference clearing timing**: The JVM has discretion on when to clear soft references. Under memory pressure,
   all soft references may be cleared simultaneously, causing a "cache stampede."

## Interview-specific Insights

Interviewers expect senior candidates to:

- **Explain the mark-and-sweep algorithm** step by step, including how circular references are handled
- **Describe generational GC** and the weak generational hypothesis
- **Compare G1, ZGC, and Parallel GC** with trade-offs for different workloads
- **Discuss GC tuning** -- what flags they have used and why
- **Identify GC-related performance issues** in code samples (excessive allocation, memory leaks)
- **Know when NOT to tune** -- understanding that defaults work well for most cases

Common interview traps:
- "How does GC handle circular references?" (It does, because mark-and-sweep is root-based)
- "What does `System.gc()` do?" (It's a hint; the JVM may ignore it)
- "Is `finalize()` reliable for cleanup?" (No; it's deprecated and non-deterministic)
- "Which GC should I use?" (Depends on requirements -- latency vs throughput vs footprint)

## Interview Q&A Section

**Q1: Explain how the Mark-and-Sweep garbage collection algorithm works.**

```text
A1: Mark-and-Sweep is the foundational GC algorithm that works in two phases:

MARK PHASE:
1. Start from GC roots (thread stacks, static fields, JNI refs, etc.)
2. Traverse the object graph, following all references from each root
3. Mark every object reached as "alive" (set a mark bit)
4. If an object has already been marked, skip it (handles circular references)
5. After traversal, all reachable objects are marked

SWEEP PHASE:
1. Scan the entire heap linearly
2. For each object:
   - If marked: clear the mark bit (reset for next cycle), keep the object
   - If NOT marked: the object is unreachable garbage, free its memory
3. After sweeping, freed memory is available for new allocations

Handling circular references:
Mark-and-Sweep does NOT use reference counting. It starts from roots and follows
reachability. If objects A and B reference each other but neither is reachable from
any root, they are both unmarked and collected. This is a key advantage over
reference-counting GC (used in Python, Objective-C).

Drawback: Basic mark-and-sweep causes memory fragmentation because freed objects
leave gaps. This is addressed by mark-and-compact (moving surviving objects together)
or by copying collectors (used in young generation).
```

```java
// Simulation of mark-and-sweep
public class MarkAndSweepSimulation {
    static class Node {
        String name;
        List<Node> refs = new ArrayList<>();
        boolean marked = false;
    }

    // Mark phase: DFS from roots
    static void mark(List<Node> roots) {
        for (Node root : roots) {
            markDFS(root);
        }
    }

    static void markDFS(Node node) {
        if (node == null || node.marked) return;
        node.marked = true;
        for (Node ref : node.refs) {
            markDFS(ref);
        }
    }

    // Sweep phase: collect unmarked
    static List<Node> sweep(List<Node> allNodes) {
        List<Node> garbage = new ArrayList<>();
        for (Node node : allNodes) {
            if (!node.marked) {
                garbage.add(node); // unreachable -- free this
            } else {
                node.marked = false; // reset for next cycle
            }
        }
        return garbage;
    }
}
```

**Q2: What is generational garbage collection and why is it effective?**

```text
A2: Generational GC divides the heap into generations based on object age:

YOUNG GENERATION (typically 1/3 of heap):
- Eden Space: Where new objects are allocated (bump-pointer allocation = fast)
- Survivor Spaces (S0, S1): Hold objects that survived at least one minor GC
- Collection: Minor GC (fast, frequent, uses copying collector)

OLD GENERATION / TENURED (typically 2/3 of heap):
- Holds long-lived objects promoted from young generation
- Collection: Major GC (slow, infrequent, uses mark-sweep-compact or similar)

METASPACE (off-heap since Java 8):
- Stores class metadata, method bytecode, constant pools
- Grows dynamically (no fixed PermGen limit)

Why it works -- the Weak Generational Hypothesis:
Research shows that the vast majority (~90-95%) of objects die young. By collecting
the young generation frequently, the GC can reclaim most garbage quickly without
scanning the entire heap.

Object lifecycle:
1. Object allocated in Eden
2. First minor GC: if alive, copied to Survivor S0
3. Second minor GC: if alive, copied from S0 to S1 (or vice versa)
4. After N minor GCs (MaxTenuringThreshold, default 15): promoted to Old Gen
5. Objects in Old Gen are collected during Major GC

Performance impact:
- Minor GC: ~10ms (young gen is small, most objects are dead)
- Major GC: ~100ms-1s+ (entire heap, more live objects to process)
- This is why reducing object promotion to Old Gen is important
```

```java
// Simulating generational promotion
public class GenerationalDemo {
    enum Generation { EDEN, SURVIVOR, OLD }

    record GCObject(String id, Generation gen, int age) {
        GCObject promote() {
            return switch (gen) {
                case EDEN -> new GCObject(id, Generation.SURVIVOR, age + 1);
                case SURVIVOR -> age >= 3
                    ? new GCObject(id, Generation.OLD, age + 1)
                    : new GCObject(id, Generation.SURVIVOR, age + 1);
                case OLD -> new GCObject(id, Generation.OLD, age + 1);
            };
        }
    }

    public static void main(String[] args) {
        GCObject obj = new GCObject("myObj", Generation.EDEN, 0);
        System.out.println(obj); // EDEN, age 0

        // Simulate 5 minor GCs
        for (int i = 0; i < 5; i++) {
            obj = obj.promote();
            System.out.println(obj); // Tracks promotion through generations
        }
        // After 5 promotions: OLD generation, age 5
    }
}
```

**Q3: Compare G1 GC, ZGC, and Parallel GC. When would you use each?**

```text
A3: Each collector is designed for different workload characteristics:

PARALLEL GC (-XX:+UseParallelGC):
- Strategy: Stop-the-world with multiple GC threads
- Pause times: 100ms-1s+ (proportional to heap size)
- Throughput: Highest (minimal CPU overhead for GC bookkeeping)
- Use when: Batch processing, offline analytics, where throughput matters
  and pauses are acceptable (e.g., Spark jobs, ETL pipelines)
- Heap range: Any size, but pauses grow with heap

G1 GC (-XX:+UseG1GC) -- DEFAULT since Java 9:
- Strategy: Region-based, partially concurrent
- Pause times: 10-200ms (configurable target via -XX:MaxGCPauseMillis)
- Throughput: Good (slight overhead vs Parallel for concurrent phases)
- Use when: General-purpose workloads needing balanced latency and throughput
  (e.g., web servers, microservices, most production applications)
- Heap range: 4GB-64GB is the sweet spot
- Key feature: Predictable pauses via region selection

ZGC (-XX:+UseZGC):
- Strategy: Fully concurrent (no stop-the-world for most phases)
- Pause times: <1ms regardless of heap size
- Throughput: Slightly lower due to concurrent overhead and load barriers
- Use when: Latency-critical applications (e.g., trading systems, real-time
  APIs, game servers) or very large heaps (hundreds of GB to TBs)
- Heap range: Any size, designed for multi-terabyte heaps
- Key feature: Colored pointers + load barriers for concurrent relocation

Decision framework:
1. Need sub-ms latency? → ZGC
2. Need max throughput for batch jobs? → Parallel GC
3. General purpose / not sure? → G1 GC (default, well-tuned)
4. Low memory footprint? → Serial GC (-XX:+UseSerialGC) for small heaps
```

```java
// JVM flags for each collector:
// Parallel GC (max throughput):
//   java -XX:+UseParallelGC -Xms4g -Xmx4g -jar app.jar

// G1 GC (balanced, default):
//   java -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xms4g -Xmx4g -jar app.jar

// ZGC (ultra-low latency):
//   java -XX:+UseZGC -Xms4g -Xmx4g -jar app.jar

// Monitoring GC behavior programmatically:
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

public class GCMonitorExample {
    public static void printGCInfo() {
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            System.out.printf("Collector: %s%n", gc.getName());
            System.out.printf("  Collection count: %d%n", gc.getCollectionCount());
            System.out.printf("  Collection time:  %d ms%n", gc.getCollectionTime());
            System.out.printf("  Memory pools:     %s%n",
                    String.join(", ", gc.getMemoryPoolNames()));
        }
    }
}
```

**Q4: How does the JVM determine which objects are garbage? What are GC roots?**

```text
A4: The JVM uses reachability analysis (not reference counting) to determine
which objects are garbage. Starting from a set of "GC roots," the collector
traverses the entire object graph. Any object not reachable from a root is garbage.

GC Roots in Java:
1. Local variables and parameters in active thread stacks
   - Every running thread's stack frames contain references
2. Static fields of loaded classes
   - Class<T> objects themselves are roots (loaded by system classloader)
3. Active Java threads (Thread objects themselves)
4. JNI references
   - Local and global references created by native code
5. Synchronized monitors
   - Objects used as locks in synchronized blocks
6. JVM internal references
   - Basic type Class objects, system classloader, etc.

Reachability states:
1. Strongly reachable: reachable from a root via strong references
2. Softly reachable: only reachable via soft references
3. Weakly reachable: only reachable via weak references
4. Phantom reachable: finalized, only tracked by phantom reference
5. Unreachable: not reachable at all -- eligible for collection

Why NOT reference counting:
Reference counting cannot handle circular references without additional
cycle detection. Java's mark-and-sweep approach handles cycles naturally.
Python uses reference counting WITH cycle detection as a supplement.

Important: An object can transition from reachable to unreachable mid-method
if the JIT compiler determines the variable is no longer used (even before
it goes out of scope). This is called "aggressive reachability analysis."
```

```java
// Demonstrating different reachability scenarios
public class GCRootsDemo {

    static Object staticRoot = new Object(); // GC root: static field

    public void methodWithRoots() {
        Object localRoot = new Object();     // GC root: local variable
        Object[] array = new Object[2];      // GC root: local variable
        array[0] = new Object();             // reachable via localRoot -> array -> [0]
        
        // At this point, all objects are reachable
        
        array[0] = null;  // Object formerly at array[0] is now unreachable
        // GC can collect it even though 'array' is still alive
        
        localRoot = null;  // Now 'array' and its contents are unreachable
        // (assuming no other references exist)
    }

    // Circular reference example -- both collected
    public void circularReference() {
        Object a = new Object(); // temporarily reachable
        Object b = new Object(); // temporarily reachable
        // Simulate: a.ref = b; b.ref = a; (conceptually)
        
        a = null;
        b = null;
        // Both objects are unreachable despite referencing each other
        // Mark-and-sweep will collect both
    }
}
```

**Q5: What GC tuning flags should a senior engineer know, and when to use them?**

```text
A5: Here are the most important GC tuning flags, categorized by purpose:

HEAP SIZING (most impactful):
- -Xms / -Xmx: Initial and maximum heap size (set equal in production)
- -XX:NewRatio: Ratio of Old to Young gen (default 2, meaning 1/3 Young)
- -XX:MaxMetaspaceSize: Limit metaspace growth

GC SELECTION:
- -XX:+UseG1GC: G1 collector (default since Java 9)
- -XX:+UseZGC: ZGC for ultra-low latency
- -XX:+UseParallelGC: Parallel for max throughput
- -XX:+UseShenandoahGC: Shenandoah for low latency (OpenJDK)

G1-SPECIFIC:
- -XX:MaxGCPauseMillis=200: Target pause time (default 200ms)
- -XX:G1HeapRegionSize: Region size (1MB-32MB, auto-calculated)
- -XX:InitiatingHeapOccupancyPercent: When to start concurrent marking (default 45%)

GENERATIONAL:
- -XX:MaxTenuringThreshold: How many minor GCs before promoting to Old (default 15)
- -XX:SurvivorRatio: Eden to Survivor ratio (default 8)

LOGGING (essential for diagnosis):
- -Xlog:gc*:file=gc.log:time,uptime,level,tags

MONITORING:
- -XX:+HeapDumpOnOutOfMemoryError: Auto heap dump on OOM
- -XX:HeapDumpPath=/path/to/dump: Where to save heap dumps

Tuning workflow:
1. Start with defaults and enable GC logging
2. Run under production-like load
3. Analyze logs with tools (GCViewer, gceasy.io, JFR)
4. Identify whether issue is throughput, latency, or footprint
5. Make ONE change at a time and re-measure
6. Document every change and its measured impact
```

```java
// Programmatic heap monitoring for adaptive behavior
import java.lang.management.*;

public class GCTuningAwareness {
    
    public static void monitorAndReport() {
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memBean.getHeapMemoryUsage();
        
        double usedPercent = (double) heap.getUsed() / heap.getMax() * 100;
        System.out.printf("Heap: %dMB used / %dMB max (%.1f%%)%n",
                heap.getUsed() / (1024 * 1024),
                heap.getMax() / (1024 * 1024),
                usedPercent);
        
        // List all GC activity
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            System.out.printf("GC [%s]: count=%d, time=%dms%n",
                    gc.getName(), gc.getCollectionCount(), gc.getCollectionTime());
        }
        
        // Memory pool details (Eden, Survivor, Old Gen)
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (pool.getType() == MemoryType.HEAP) {
                MemoryUsage usage = pool.getUsage();
                System.out.printf("Pool [%s]: %dKB used%n",
                        pool.getName(), usage.getUsed() / 1024);
            }
        }
    }
}
```

**Q6: How does G1 GC work internally? What makes it different from previous collectors?**

```text
A6: G1 (Garbage First) GC is a region-based, partially concurrent collector that
aims to provide predictable pause times while maintaining good throughput.

Architecture:
- Divides the heap into equal-sized regions (1MB-32MB, typically 2048 regions)
- Each region can be Eden, Survivor, Old, or Humongous (large objects)
- No fixed contiguous generation boundaries

Collection phases:
1. Young-only phase:
   - Concurrent marking: identifies regions with most garbage
   - Uses SATB (Snapshot At The Beginning) to track reference changes
   
2. Space reclamation phase:
   - Collects young AND selected old regions (ones with most garbage)
   - "Garbage First" = prioritizes regions with the most reclaimable space
   - Copies live objects to new regions, freeing entire old regions

Key innovations:
1. Predictable pauses: G1 selects WHICH regions to collect based on time budget
   (-XX:MaxGCPauseMillis). It won't collect more regions than it can within the target.
2. Region-based: No need for contiguous generations. Any region can change role.
3. Remembered sets: Track cross-region references so G1 doesn't need to scan
   the entire old generation when collecting young regions.
4. Mixed collections: Can collect old regions alongside young regions incrementally,
   spreading the work across multiple pauses.

What makes it better than previous collectors:
- vs Parallel GC: G1 offers predictable pauses (Parallel has no pause target)
- vs CMS: G1 does compaction (CMS doesn't, leading to fragmentation)
- vs Serial: G1 uses multiple threads and concurrent phases

Limitations:
- Higher memory footprint than Parallel (remembered sets overhead, ~5-20%)
- Concurrent marking uses CPU that could go to application
- Not ideal for very small heaps (<1GB) or max-throughput batch workloads
```

```java
// G1 GC configuration example for a web service
// java -XX:+UseG1GC \
//      -Xms4g -Xmx4g \
//      -XX:MaxGCPauseMillis=100 \
//      -XX:G1HeapRegionSize=4m \
//      -XX:InitiatingHeapOccupancyPercent=40 \
//      -Xlog:gc*:file=gc.log:time,uptime \
//      -jar webservice.jar

// Detecting G1 at runtime
public class G1Detection {
    public static String detectCollector() {
        return ManagementFactory.getGarbageCollectorMXBeans()
                .stream()
                .map(GarbageCollectorMXBean::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("unknown");
        // Example output: "G1 Young Generation, G1 Old Generation"
        // or: "ZGC" for ZGC
    }
    
    public static boolean isG1() {
        return ManagementFactory.getGarbageCollectorMXBeans()
                .stream()
                .anyMatch(gc -> gc.getName().contains("G1"));
    }
}
```

**Q7: What is the difference between `System.gc()`, `Runtime.gc()`, and actual garbage collection?**

```text
A7: This is an important distinction that shows understanding of GC internals:

System.gc() and Runtime.getRuntime().gc():
- Both are equivalent -- System.gc() internally calls Runtime.gc()
- They are REQUESTS (hints) to the JVM to perform garbage collection
- The JVM is free to ignore these requests entirely
- When honored, they typically trigger a FULL GC (expensive!)
- Can be disabled entirely with -XX:+DisableExplicitGC

Actual Garbage Collection:
- Triggered automatically by the JVM based on internal heuristics
- The GC decides WHEN, WHAT, and HOW to collect
- Minor GC: triggered when Eden space is full
- Major GC: triggered when Old Gen is nearly full or promotion fails
- The GC algorithm determines which objects to collect and how

Why System.gc() is almost always wrong:
1. It triggers a FULL GC, causing a long stop-the-world pause
2. The JVM's heuristics are better at timing GC than application code
3. It disrupts the GC's ability to optimize its own scheduling
4. In production, -XX:+DisableExplicitGC is often set

Legitimate uses of System.gc() (rare):
1. Testing and benchmarking (before measuring to establish baseline)
2. Before entering a latency-critical section (to reduce chance of GC during it)
3. After bulk loading data (to compact before steady-state operation)

Even in these cases, consider alternatives:
- For benchmarking: use JMH which handles warmup and GC properly
- For latency: switch to ZGC for sub-ms pauses
- For bulk load: tune NewSize so objects are collected in minor GCs
```

```java
// Demonstrating System.gc() behavior
public class SystemGcDemo {
    
    // BAD: Using System.gc() for resource management
    public void badPractice() {
        // Don't do this -- it's not reliable
        Object obj = new Object();
        obj = null;
        System.gc(); // may or may not collect the object
    }
    
    // Legitimate use: benchmarking setup
    public long benchmarkAllocations(int count) {
        System.gc(); // try to start from clean state
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        
        Runtime rt = Runtime.getRuntime();
        long before = rt.totalMemory() - rt.freeMemory();
        
        Object[] objects = new Object[count];
        for (int i = 0; i < count; i++) {
            objects[i] = new byte[1024];
        }
        
        long after = rt.totalMemory() - rt.freeMemory();
        return after - before;
    }
    
    // Better: use JMX for monitoring instead of System.gc()
    public static void monitorWithoutForcing() {
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage usage = memBean.getHeapMemoryUsage();
        
        System.out.printf("Used: %dMB, Committed: %dMB, Max: %dMB%n",
                usage.getUsed() / (1024*1024),
                usage.getCommitted() / (1024*1024),
                usage.getMax() / (1024*1024));
    }
}
```

## Code Examples

- Test: [GarbageCollectionTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/memorymanagement/GarbageCollectionTest.java)
- Source: [GarbageCollection.java](src/main/java/com/github/msorkhpar/claudejavatutor/memorymanagement/GarbageCollection.java)
