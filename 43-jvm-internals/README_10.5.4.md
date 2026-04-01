# 10.5.4. JVM Performance Tuning and Monitoring

## Concept Explanation

JVM performance tuning and monitoring is the art and science of configuring the JVM and observing its behavior to
achieve optimal application performance. It encompasses garbage collection tuning, memory configuration, thread
management, and using monitoring tools to identify bottlenecks.

**Real-world analogy**: Think of the JVM as a high-performance race car. The **heap size** is the fuel tank -- too small
and you run out mid-race (OutOfMemoryError), too large and the car is heavy and slow (long GC pauses). **GC tuning** is
like choosing the right tire compound -- soft tires (low-latency GC like ZGC) give great grip but wear out faster, hard
tires (throughput-oriented GC like Parallel) last longer but have less grip. **Monitoring** is the telemetry dashboard
that shows engine temperature, tire pressure, and fuel level in real time. A good mechanic (performance engineer) reads
the telemetry and makes adjustments accordingly.

### Key Performance Areas

#### 1. Memory Configuration

- **Heap sizing**: `-Xms` (initial) and `-Xmx` (maximum) set heap boundaries.
- **Metaspace**: `-XX:MetaspaceSize` and `-XX:MaxMetaspaceSize` control class metadata memory.
- **Stack sizing**: `-Xss` sets per-thread stack size.
- **Direct memory**: `-XX:MaxDirectMemorySize` for NIO direct buffers.

#### 2. Garbage Collection Tuning

The JVM offers several garbage collectors, each with different trade-offs:

| GC | Best For | Flags | Pause Behavior |
|-----|----------|-------|----------------|
| Serial GC | Small apps, single core | `-XX:+UseSerialGC` | Long STW pauses |
| Parallel GC | Throughput | `-XX:+UseParallelGC` | Moderate STW pauses |
| G1 GC (default) | Balanced | `-XX:+UseG1GC` | Predictable pauses |
| ZGC | Low latency | `-XX:+UseZGC` | Sub-millisecond pauses |
| Shenandoah | Low latency | `-XX:+UseShenandoahGC` | Sub-millisecond pauses |

#### 3. Monitoring Tools

- **jcmd**: Multi-purpose JVM diagnostic tool
- **jstat**: JVM statistics monitoring
- **jmap**: Memory map and heap dump generation
- **jstack**: Thread dump generation
- **JFR (Java Flight Recorder)**: Low-overhead production profiling
- **JMC (Java Mission Control)**: GUI analysis tool for JFR recordings
- **VisualVM**: All-in-one monitoring and troubleshooting tool

#### 4. Performance Diagnostics

- **Thread analysis**: Detecting deadlocks, thread starvation, and contention
- **Memory analysis**: Identifying memory leaks, large object allocation patterns
- **CPU profiling**: Finding hotspots and optimization opportunities
- **I/O analysis**: Detecting blocking I/O and suboptimal access patterns

## Key Points to Remember

1. **G1 GC is the default** since Java 9. Use ZGC for ultra-low latency requirements.
2. **Don't set `-Xms` equal to `-Xmx`** unless you have a specific reason -- it prevents the JVM from returning
   unused memory to the OS.
3. **JFR has near-zero overhead** and is safe for production use. Always consider enabling it.
4. **Thread dumps** (`jstack` or `jcmd Thread.print`) are the primary tool for diagnosing hangs and deadlocks.
5. **GC logs** should always be enabled in production: `-Xlog:gc*:file=gc.log:time,level,tags`.
6. The **80/20 rule** applies: 80% of performance issues come from application code, not JVM tuning.
7. Always **measure before tuning** -- premature optimization of JVM flags is counterproductive.
8. **Heap dumps** (`jmap -dump` or `jcmd GC.heap_dump`) are essential for diagnosing memory leaks.

## Relevant Java 21 Features

- **ZGC Generational mode**: `-XX:+UseZGC -XX:+ZGenerational` (default in Java 21) adds generational collection to
  ZGC for better throughput alongside low latency.
- **JFR enhancements**: New event types for virtual threads, structured concurrency, and better native memory tracking.
- **Virtual thread monitoring**: New diagnostic capabilities for virtual threads via `jcmd` and JFR.
- **Unified logging**: `-Xlog` provides a single, consistent framework for all JVM logging including GC.
- **Container awareness**: JVM automatically detects container memory and CPU limits (Docker, Kubernetes).

## Common Pitfalls and How to Avoid Them

1. **Over-tuning GC parameters**: Adding too many GC flags can prevent the JVM's adaptive sizing from working correctly.
   ```bash
   # BAD: Over-specified GC tuning
   java -XX:NewRatio=3 -XX:SurvivorRatio=8 -XX:MaxTenuringThreshold=15 \
        -XX:ParallelGCThreads=8 -XX:ConcGCThreads=4 ...

   # GOOD: Let the JVM adapt, set only what's needed
   java -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xmx4g MyApp
   ```
   **Solution**: Start with minimal tuning. Set the GC type, heap size, and pause time goal. Let the JVM adapt the rest.

2. **Ignoring GC logs in production**:
   ```bash
   # Always enable GC logging -- it has negligible overhead
   java -Xlog:gc*:file=gc.log:time,level,tags:filecount=5,filesize=100m MyApp
   ```
   **Solution**: Enable GC logging in all environments. It's the first place to look when performance degrades.

3. **Using jmap -histo in production with Full GC**:
   ```bash
   # BAD: Forces a Full GC in production
   jmap -histo:live <pid>

   # GOOD: Get histogram without GC
   jmap -histo <pid>

   # BETTER: Use jcmd
   jcmd <pid> GC.class_histogram
   ```
   **Solution**: Use `jcmd` instead of `jmap` when possible. Be aware that `:live` triggers a full GC.

4. **Setting heap too large**: Larger heaps mean longer GC pauses (especially with G1 and Parallel GC).
   ```bash
   # 32GB heap with G1 can have multi-second pauses
   java -Xmx32g -XX:+UseG1GC MyApp

   # Consider ZGC for large heaps
   java -Xmx32g -XX:+UseZGC MyApp
   ```
   **Solution**: For heaps larger than 8GB, strongly consider ZGC or Shenandoah.

## Best Practices and Optimization Techniques

1. **Start with defaults**: Modern JVMs have excellent defaults. Only tune when measurements indicate a need.
2. **Enable monitoring from day one**:
   ```bash
   java -Xlog:gc*:file=gc.log:time,level,tags \
        -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/path/to/dumps \
        -XX:StartFlightRecording=duration=0,maxsize=500m,dumponexit=true,filename=app.jfr \
        MyApp
   ```
3. **Use container-aware settings** in Docker/Kubernetes:
   ```bash
   # JVM automatically detects container limits since Java 10
   # Fine-tune with:
   java -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 MyApp
   ```
4. **Profile before tuning**: Use JFR + JMC or async-profiler to find real bottlenecks.
5. **GC pause time goals**: Prefer `-XX:MaxGCPauseMillis=N` over manual generation sizing.
6. **Monitor key metrics**: GC pause time, GC frequency, heap usage after GC, allocation rate, thread count.

## Edge Cases and Their Handling

1. **OutOfMemoryError: GC Overhead Limit Exceeded**: The JVM spent more than 98% of time in GC recovering less than 2%
   of heap. This indicates a memory leak or insufficient heap.
2. **OutOfMemoryError: Direct buffer memory**: NIO direct buffers exhausted. Tune with
   `-XX:MaxDirectMemorySize` and ensure buffers are properly released.
3. **Container memory kills (OOMKilled)**: The JVM uses more memory than just the heap (Metaspace, thread stacks,
   direct memory, native memory). Set container limits higher than `-Xmx` by at least 20-30%.
4. **Thread stack overflow**: Deep recursion or large local variable arrays. Tune with `-Xss` (default 512KB-1MB).
5. **Slow startup**: Use CDS (`-Xshare:on`), AOT compilation (GraalVM native image), or reduce classpath scanning.

## Interview-specific Insights

Interviewers often focus on:

- Knowledge of GC algorithms and when to use each
- Understanding heap layout (Young/Old generation, Eden/Survivor spaces)
- Ability to diagnose memory leaks using heap dumps
- Familiarity with monitoring tools (JFR, jcmd, jstack)
- Experience with performance tuning in production
- Understanding of the trade-offs between latency and throughput

Common tricky questions:
- "How would you diagnose a memory leak in a production Java application?"
- "What GC would you choose for a high-frequency trading application? Why?"
- "Your Java service in Kubernetes keeps getting OOMKilled. What would you investigate?"
- "How do you find the cause of occasional latency spikes?"

## Interview Q&A Section

**Q1: How would you diagnose a memory leak in a production Java application?**

```text
A1: Memory leak diagnosis follows a systematic approach:

Step 1: Detect the symptom
- GC logs show increasing heap usage after Full GC over time
- Application becomes slow or unresponsive
- OutOfMemoryError: Java heap space

Step 2: Gather evidence
- Enable GC logging: -Xlog:gc*:file=gc.log:time,level,tags
- Monitor heap usage trend: jstat -gcutil <pid> 1000
- Check heap histogram: jcmd <pid> GC.class_histogram

Step 3: Capture heap dump
- jcmd <pid> GC.heap_dump /path/to/dump.hprof
- Or automatically: -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/path/

Step 4: Analyze heap dump
- Open in Eclipse MAT (Memory Analyzer Tool) or VisualVM
- Look for:
  a. Leak Suspects Report (automated analysis)
  b. Dominator tree (largest retained objects)
  c. Histogram sorted by retained size
  d. GC Roots paths for suspicious objects

Step 5: Identify the root cause
- Common causes:
  a. Unbounded caches (Map without eviction)
  b. Listeners/callbacks not deregistered
  c. ThreadLocal variables not cleaned up
  d. Static collections growing unboundedly
  e. Unclosed resources (streams, connections)
  f. ClassLoader leaks in web applications

Step 6: Fix and verify
- Fix the code to properly release references
- Monitor with GC logs to confirm heap usage stabilizes
```

```java
// Common memory leak patterns and fixes
public class MemoryLeakPatterns {
    // LEAK: Unbounded cache
    private static final Map<String, byte[]> cache = new HashMap<>();

    public void leakyCache(String key, byte[] data) {
        cache.put(key, data); // Never evicted!
    }

    // FIX: Use bounded cache or WeakHashMap
    private static final Map<String, byte[]> boundedCache =
        new LinkedHashMap<>(100, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
                return size() > 1000; // Evict when over 1000 entries
            }
        };

    // LEAK: ThreadLocal not cleaned up
    private static final ThreadLocal<List<String>> threadData = new ThreadLocal<>();

    public void leakyThreadLocal() {
        threadData.set(new ArrayList<>()); // Never removed!
    }

    // FIX: Always clean up ThreadLocal in finally
    public void fixedThreadLocal() {
        try {
            threadData.set(new ArrayList<>());
            // ... use threadData
        } finally {
            threadData.remove(); // Always clean up
        }
    }

    // LEAK: Listener not deregistered
    interface DataListener { void onData(String data); }

    private final List<DataListener> listeners = new ArrayList<>();

    public void addListener(DataListener listener) {
        listeners.add(listener); // Holds strong reference forever
    }

    // FIX: Provide removeListener() and use WeakReference if appropriate
    public void removeListener(DataListener listener) {
        listeners.remove(listener);
    }
}
```

**Q2: Compare G1 GC, ZGC, and Shenandoah. When would you choose each?**

```text
A2: Each garbage collector is designed for different workload characteristics:

G1 GC (Garbage-First):
- Default since Java 9
- Divides heap into equal-sized regions (1-32MB each)
- Targets configurable pause time: -XX:MaxGCPauseMillis (default 200ms)
- Mixed collections: collects both young and old regions
- Best for: General-purpose workloads, heaps 4-16GB, pause times 100-500ms
- Limitation: Pauses scale with heap size (though better than Parallel GC)

ZGC (Z Garbage Collector):
- Production-ready since Java 15
- Generational mode default in Java 21 (-XX:+ZGenerational)
- Pause times < 1ms regardless of heap size (even multi-TB heaps)
- Uses colored pointers and load barriers
- Concurrent almost entirely (marking, relocation, reference processing)
- Best for: Low-latency applications, large heaps (16GB+), latency-sensitive services
- Limitation: Slightly lower throughput than G1 for small heaps

Shenandoah:
- Not available in Oracle JDK (OpenJDK only)
- Similar goals to ZGC: sub-millisecond pauses
- Uses Brooks pointers and load/store barriers
- Concurrent compaction
- Best for: Low-latency needs on OpenJDK, similar use cases to ZGC
- Limitation: Higher CPU overhead, not in Oracle JDK

Decision matrix:
- General web app with 4-8GB heap: G1 GC (default)
- High-frequency trading / real-time: ZGC
- Microservice with strict latency SLA: ZGC
- Batch processing / data pipelines: Parallel GC or G1
- Large heap (32GB+): ZGC
- Containerized with limited CPU: G1 (lower CPU overhead)
```

```java
// Demonstrating GC monitoring programmatically
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

public class GcMonitoringDemo {
    public static void printGcInfo() {
        // List all garbage collectors
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            System.out.printf("GC Name: %s, Collections: %d, Time: %dms%n",
                gc.getName(), gc.getCollectionCount(), gc.getCollectionTime());
        }

        // Memory usage
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memBean.getHeapMemoryUsage();
        System.out.printf("Heap: used=%dMB, committed=%dMB, max=%dMB%n",
            heapUsage.getUsed() / 1024 / 1024,
            heapUsage.getCommitted() / 1024 / 1024,
            heapUsage.getMax() / 1024 / 1024);

        MemoryUsage nonHeapUsage = memBean.getNonHeapMemoryUsage();
        System.out.printf("Non-Heap: used=%dMB, committed=%dMB%n",
            nonHeapUsage.getUsed() / 1024 / 1024,
            nonHeapUsage.getCommitted() / 1024 / 1024);
    }
}
```

**Q3: Your Java service in Kubernetes keeps getting OOMKilled. What would you investigate?**

```text
A3: OOMKilled in Kubernetes means the container exceeded its memory limit. The JVM uses
more memory than just the heap, so -Xmx alone doesn't determine total memory usage.

JVM Memory Components:
1. Heap (-Xmx): Java objects and arrays
2. Metaspace: Class metadata, method data
3. Thread stacks: -Xss * number_of_threads
4. Code cache: JIT-compiled code (~240MB default reserved)
5. Direct byte buffers: NIO allocations
6. Native memory: JNI, native libraries, internal JVM structures
7. GC overhead: GC data structures (card tables, marking bitmaps)

Investigation steps:
1. Check container memory limit vs. JVM memory settings:
   kubectl describe pod <pod>
   Compare spec.containers[].resources.limits.memory with -Xmx

2. Calculate total JVM memory:
   Total ≈ Heap + Metaspace + ThreadStacks + CodeCache + DirectMemory + GCOverhead
   Rule of thumb: Set container limit to 1.5x-2x of -Xmx

3. Enable Native Memory Tracking:
   -XX:NativeMemoryTracking=summary
   jcmd <pid> VM.native_memory summary

4. Check thread count:
   jcmd <pid> Thread.print | grep -c "nid="
   Each thread uses -Xss (default 1MB) of native memory

5. Use container-aware settings:
   java -XX:MaxRAMPercentage=75.0 (use 75% of container limit for heap)

Fixes:
- Increase container memory limit
- Reduce -Xmx to leave room for non-heap memory
- Use -XX:MaxRAMPercentage instead of -Xmx
- Reduce thread count (use virtual threads!)
- Limit Metaspace: -XX:MaxMetaspaceSize
- Limit code cache: -XX:ReservedCodeCacheSize
- Limit direct memory: -XX:MaxDirectMemorySize
```

```java
// Native Memory Tracking demonstration
// Run with: java -XX:NativeMemoryTracking=summary NativeMemoryDemo
// Then: jcmd <pid> VM.native_memory summary

public class NativeMemoryDemo {
    public static void printMemoryBreakdown() {
        Runtime runtime = Runtime.getRuntime();

        // Heap information
        long maxHeap = runtime.maxMemory();
        long totalHeap = runtime.totalMemory();
        long freeHeap = runtime.freeMemory();
        long usedHeap = totalHeap - freeHeap;

        System.out.printf("=== Heap Memory ===%n");
        System.out.printf("Max:  %d MB%n", maxHeap / 1024 / 1024);
        System.out.printf("Used: %d MB%n", usedHeap / 1024 / 1024);
        System.out.printf("Free: %d MB%n", freeHeap / 1024 / 1024);

        // Thread count (each uses ~1MB stack by default)
        int threadCount = Thread.activeCount();
        System.out.printf("%n=== Threads ===%n");
        System.out.printf("Active threads: %d%n", threadCount);
        System.out.printf("Estimated stack memory: ~%d MB%n", threadCount);

        // Available processors (important for container awareness)
        System.out.printf("%n=== CPU ===%n");
        System.out.printf("Available processors: %d%n",
            runtime.availableProcessors());
    }

    public static void main(String[] args) {
        printMemoryBreakdown();
    }
}
```

**Q4: How do you use Java Flight Recorder (JFR) for performance analysis?**

```text
A4: Java Flight Recorder (JFR) is a profiling and diagnostics framework built into the
JVM since Java 11 (open-sourced). It has extremely low overhead (<2%) and is safe for
production use.

Starting JFR:

1. At application startup:
   java -XX:StartFlightRecording=duration=60s,filename=recording.jfr MyApp

2. Continuous recording (ring buffer):
   java -XX:StartFlightRecording=maxsize=500m,maxage=24h,
        dumponexit=true,filename=app.jfr MyApp

3. Attaching to running JVM:
   jcmd <pid> JFR.start duration=60s filename=recording.jfr
   jcmd <pid> JFR.dump filename=snapshot.jfr
   jcmd <pid> JFR.stop

Key event categories:
- jdk.CPULoad: CPU utilization
- jdk.GCPausePhase: GC pause details
- jdk.ObjectAllocationInNewTLAB: Allocation hot spots
- jdk.JavaMonitorEnter: Lock contention
- jdk.ThreadPark: Thread parking (waiting)
- jdk.ExecutionSample: Method profiling (CPU sampling)
- jdk.FileRead/FileWrite: I/O operations
- jdk.SocketRead/SocketWrite: Network operations
- jdk.Compilation: JIT compilation events
- jdk.VirtualThreadStart/End: Virtual thread lifecycle (Java 21)

Analyzing JFR recordings:
- JDK Mission Control (JMC): GUI tool for visualizing JFR data
- jfr tool: CLI for printing/summarizing recordings
  jfr summary recording.jfr
  jfr print --events jdk.GCPausePhase recording.jfr
- Programmatic API: JFR streaming API (Java 14+) for real-time monitoring

Best practices:
- Always have continuous recording running in production
- Use custom JFR events for application-specific metrics
- Configure event thresholds to focus on significant events
- Combine JFR with GC logs for comprehensive analysis
```

```java
// Creating custom JFR events (Java 14+)
import jdk.jfr.*;

@Name("com.example.DatabaseQuery")
@Label("Database Query")
@Category({"Application", "Database"})
@StackTrace(true)
public class DatabaseQueryEvent extends jdk.jfr.Event {
    @Label("SQL Query")
    String query;

    @Label("Duration")
    @Timespan(Timespan.MILLISECONDS)
    long durationMs;

    @Label("Row Count")
    int rowCount;

    @Label("Success")
    boolean success;
}

// Usage:
// DatabaseQueryEvent event = new DatabaseQueryEvent();
// event.begin();
// try {
//     ResultSet rs = stmt.executeQuery(sql);
//     event.query = sql;
//     event.rowCount = countRows(rs);
//     event.success = true;
// } catch (SQLException e) {
//     event.success = false;
// } finally {
//     event.durationMs = ...;
//     event.commit();
// }
```

**Q5: What JVM flags should every production Java application have?**

```text
A5: Here are the essential JVM flags for production applications:

Memory Configuration:
- -Xmx<size>: Maximum heap size (or -XX:MaxRAMPercentage for containers)
- -Xms<size>: Initial heap size (set to -Xmx for predictable performance)
- -XX:MaxMetaspaceSize=256m: Prevent unbounded Metaspace growth

GC Configuration:
- -XX:+UseG1GC (default) or -XX:+UseZGC for low latency
- -XX:MaxGCPauseMillis=200: Target GC pause time for G1

GC Logging (critical for diagnostics):
- -Xlog:gc*:file=gc.log:time,level,tags:filecount=5,filesize=100m

Error Handling:
- -XX:+HeapDumpOnOutOfMemoryError: Auto-capture heap dump on OOM
- -XX:HeapDumpPath=/path/to/dumps: Where to store heap dumps
- -XX:+ExitOnOutOfMemoryError: Exit on OOM (better for containers)
  or -XX:+CrashOnOutOfMemoryError: Generate core dump on OOM

Monitoring:
- -XX:StartFlightRecording=maxsize=500m,dumponexit=true,filename=app.jfr
- -XX:NativeMemoryTracking=summary (slight overhead, enable when needed)

Container-specific:
- -XX:MaxRAMPercentage=75.0: Use percentage of container memory
- -XX:ActiveProcessorCount=N: Override CPU detection if needed

Example production command:
java -Xmx4g -Xms4g \
     -XX:+UseG1GC -XX:MaxGCPauseMillis=200 \
     -XX:MaxMetaspaceSize=256m \
     -Xlog:gc*:file=/var/log/app/gc.log:time,level,tags:filecount=5,filesize=100m \
     -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/log/app/ \
     -XX:+ExitOnOutOfMemoryError \
     -XX:StartFlightRecording=maxsize=500m,dumponexit=true,filename=/var/log/app/app.jfr \
     -jar myapp.jar
```

```java
// Programmatic JVM monitoring
import java.lang.management.*;
import java.util.List;

public class JvmMonitoring {
    public static void printFullDiagnostics() {
        // Runtime info
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        System.out.println("JVM: " + runtime.getVmName() + " " + runtime.getVmVersion());
        System.out.println("Uptime: " + runtime.getUptime() / 1000 + "s");
        System.out.println("JVM Args: " + runtime.getInputArguments());

        // Memory
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memory.getHeapMemoryUsage();
        System.out.printf("Heap: used=%dMB, max=%dMB (%.1f%%)%n",
            heap.getUsed() / 1048576, heap.getMax() / 1048576,
            100.0 * heap.getUsed() / heap.getMax());

        // GC stats
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            System.out.printf("GC [%s]: count=%d, time=%dms%n",
                gc.getName(), gc.getCollectionCount(), gc.getCollectionTime());
        }

        // Thread info
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        System.out.println("Threads: " + threads.getThreadCount());
        System.out.println("Peak threads: " + threads.getPeakThreadCount());

        // Deadlock detection
        long[] deadlocked = threads.findDeadlockedThreads();
        if (deadlocked != null) {
            System.out.println("DEADLOCK DETECTED! Thread IDs: ");
            for (long id : deadlocked) {
                ThreadInfo info = threads.getThreadInfo(id);
                System.out.println("  " + info.getThreadName());
            }
        } else {
            System.out.println("No deadlocks detected.");
        }

        // Memory pools
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            MemoryUsage usage = pool.getUsage();
            System.out.printf("Pool [%s]: used=%dMB, max=%dMB%n",
                pool.getName(),
                usage.getUsed() / 1048576,
                usage.getMax() > 0 ? usage.getMax() / 1048576 : -1);
        }
    }
}
```

## Code Examples

- Test: [JvmPerformanceTuningTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/jvminternals/JvmPerformanceTuningTest.java)
- Source: [JvmPerformanceTuning.java](src/main/java/com/github/msorkhpar/claudejavatutor/jvminternals/JvmPerformanceTuning.java)
