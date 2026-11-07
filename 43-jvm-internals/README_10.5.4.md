# 10.5.4. JVM Performance Tuning and Monitoring

The order service runs in a container with a 1 GB memory limit and no JVM flags. Some mornings its
requests time out while the health check still answers, and on Fridays the pod restarts with
`OOMKilled`. You have a shell next to the process and the JDK's own tools: `jcmd`, the Flight
Recorder and the `java` launcher. Each of them prints a number that is easy to misread.

Every run here is Java 21 (Temurin 21.0.12) on one Linux machine. In the outputs, `<pid>` and
`<nid>` replace process and thread ids, and `...` marks lines left out. GC logs are unit 10.2.2,
JIT logs unit 10.5.3.

## GC.heap_info Counts Garbage as Used; Live Data Is What a Full Collection Leaves

`jcmd <pid> <command>` attaches to a running JVM started by the same user and runs a diagnostic
command inside it; `jcmd` alone lists the JVMs it can see. This program keeps 64 MB, throws
300 MB away, then idles:

```java
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/** Holds 64 MB of live data, drops 300 MB of garbage, then idles. */
public class Catalog {
    static final List<byte[]> cache = new ArrayList<>();   // live: 1,024 arrays of 64 KB
    static long checksum;

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 1024; i++) cache.add(new byte[64 << 10]);
        for (int i = 0; i < 4800; i++) {                    // 300 MB in 64 KB arrays, each dropped at once
            byte[] page = new byte[64 << 10];
            checksum += page.length;
        }
        Files.writeString(Path.of(args[0]), Long.toString(ProcessHandle.current().pid()));
        System.out.println("ready, cache " + cache.size() / 16 + " MB");
        new CountDownLatch(1).await();                      // idle until killed
    }
}
```

We start it with `java -Xms1g -Xmx1g -Xlog:gc:file=gc.log Catalog`. Pause here: how much of the
heap does it report as used?

```text
$ jcmd <pid> GC.heap_info
<pid>:
 garbage-first heap   total 1048576K, used 216433K [0x00000000c0000000, 0x0000000100000000)
  region size 1024K, 142 young (145408K), 0 survivors (0K)
 Metaspace       used 602K, committed 768K, reserved 1114112K
  class space    used 53K, committed 128K, reserved 1048576K
```

211 MB, three times what the program holds: "used" counts every byte in the regions the
collector has handed out, including the 142 young regions full of arrays nobody references. A
class histogram, first with `-all`, then without, and the GC log:

```text
$ jcmd <pid> GC.class_histogram -all
 num     #instances         #bytes  class name (module)
-------------------------------------------------------
   1:         11921      206317672  [B (java.base@21.0.12.1)
...
$ jcmd <pid> GC.class_histogram
   1:          9176       67482112  [B (java.base@21.0.12.1)
...
$ jcmd <pid> GC.heap_info
 garbage-first heap   total 1048576K, used 66807K [0x00000000c0000000, 0x0000000100000000)
```

```text
[0.528s][info][gc] GC(5) Pause Full (Heap Inspection Initiated GC) 211M->65M(1024M) 5.282ms
```

The plain histogram counts only live objects. To find them it ran a stop-the-world full
collection, and `jmap -histo:live`, run on the same program, logged the same line. It took 5 ms on
211 MB; on a heap of tens of gigabytes it can stop the service for seconds. `-all` collects
nothing and counts garbage too.

So a high "used" figure is not a leak. A leak is the number after a full collection (`211M->65M`
here) climbing from one collection to the next.

## A Thread Dump Names a Deadlock Cycle, Not a Thread Stuck Behind a Holder That Waits

Unit 5.3.4 finds a deadlock from inside with `ThreadMXBean.findDeadlockedThreads()`. From
outside, `jcmd <pid> Thread.print` (the dump `jstack` prints) shows every thread's stack and
the locks in it. Two warehouse threads take the same two monitors in opposite orders:

```java
static void restock() {
    synchronized (stock) {
        stock.units += 5;
        await(bothHoldOne);
        synchronized (prices) { prices.cents -= 100; }   // reprice after a restock
    }
}

static void reprice() {
    synchronized (prices) {
        prices.cents += 100;
        await(bothHoldOne);
        if (hang) waitForFeed();                         // the supplier's feed never loads
        else synchronized (stock) { stock.units -= 1; }  // reserve one unit at the new price
    }
}
```

With `hang` false, the dump ends with a deadlock section:

```text
Found one Java-level deadlock:
=============================
"restock":
  waiting to lock monitor 0x00007cb5340010f0 (object 0x00000005ab8170e0, a Warehouse$PriceList),
  which is held by "reprice"

"reprice":
  waiting to lock monitor 0x00007cb538000f60 (object 0x00000005ab816750, a Warehouse$Stock),
  which is held by "restock"
...
Found 1 deadlock.
```

With `hang` true, `reprice` holds the price list while it waits on a `CountDownLatch` that nobody
counts down. `restock` is just as stuck, but the dump has no deadlock section, because there is no
cycle: `reprice` waits for a latch, not a lock. Find the holder by matching the address `restock`
waits for to a `- locked` line:

```text
"restock" #28 [<nid>] prio=5 ... waiting for monitor entry  [0x0000756ec26fe000]
   java.lang.Thread.State: BLOCKED (on object monitor)
	at Warehouse.restock(Warehouse.java:23)
	- waiting to lock <0x00000005ab8170e0> (a Warehouse$PriceList)
	- locked <0x00000005ab816750> (a Warehouse$Stock)
...
"reprice" #29 [<nid>] prio=5 ... waiting on condition  [0x0000756ec25fe000]
   java.lang.Thread.State: WAITING (parking)
	at jdk.internal.misc.Unsafe.park(java.base@21.0.12.1/Native Method)
	- parking to wait for  <0x00000005ab817320> (a java.util.concurrent.CountDownLatch$Sync)
...
	at Warehouse.waitForFeed(Warehouse.java:38)
	at Warehouse.reprice(Warehouse.java:31)
	- locked <0x00000005ab8170e0> (a Warehouse$PriceList)
```

In production the latch is a database call or an HTTP request without a timeout, and fifty
request threads are `BLOCKED` on `<0x...70e0>`. Follow each `BLOCKED` thread's address to its
`- locked` line, then read what the holder waits for. Match the address, not the class: two price
lists are two locks.

A thread keeps its monitors while it parks, sleeps or reads a socket. Only `Object.wait()` gives
up one, the monitor it waits on, and the dump still lists that one as `- locked`, below a
`- waiting on` line with the same address. That line is not a lock the thread holds; its other
`- locked` lines are. A hand-rolled spin lock shows no owner at all (unit 8.2.3).

<!-- practice 1 -->

## A JFR Allocation Sample Stands for All the Bytes Since the Previous One

The JDK Flight Recorder (JFR) records events inside the JVM to a file that the `jfr` tool reads.
Start it with the program (`-XX:StartFlightRecording`) or on a running one
(`jcmd <pid> JFR.start`, then `JFR.dump`). This invoice renderer runs for two seconds, beside a
`customerName` lookup with a small cache, and prints what its thread allocated
(`ThreadMXBean.getCurrentThreadAllocatedBytes`; the number in brackets only keeps the result in use):

```java
static String renderInvoice(int id) {
    String html = "<table>";
    for (int line = 0; line < 40; line++) {
        html += "<tr><td>item " + line + "</td><td>" + (id * line % 997) + "</td></tr>";
    }
    return html + "</table>";
}
```

```text
$ java -XX:StartFlightRecording:filename=invoices.jfr Invoices
invoices: 1,136,100, allocated by main: 36,187 MB (0)
$ jfr view allocation-by-site invoices.jfr
Method                                                           Allocation Pressure
---------------------------------------------------------------- -------------------
jdk.internal.misc.Unsafe.allocateUninitializedArray(Class, int)               99.56%
java.lang.StringConcatHelper.newString(byte[], long)                           0.34%
...
```

The view groups by the top frame, which is the JDK's own allocation helper. One event shows the
rest of the stack:

```text
$ jfr print --stack-depth 6 --events jdk.ObjectAllocationSample invoices.jfr
jdk.ObjectAllocationSample {
  startTime = 01:12:25.222 (2026-11-07)
  objectClass = byte[] (classLoader = bootstrap)
  weight = 655.9 kB
  eventThread = "main" (javaThreadId = 1)
  stackTrace = [
    jdk.internal.misc.Unsafe.allocateUninitializedArray0(Class, int) line: 1387
    jdk.internal.misc.Unsafe.allocateUninitializedArray(Class, int) line: 1380
    java.lang.StringConcatHelper.newArray(long) line: 511
    java.lang.StringConcatHelper.newArrayWithSuffix(String, long) line: 489
    Invoices.renderInvoice(int) line: 16
    Invoices.main(String[]) line: 26
  ]
}
```

The sampled array holds part of one invoice, at most a few kilobytes, yet its `weight` is
655.9 kB. JFR doesn't record every
allocation: the default settings take about 150 samples a second, and a sample's `weight` is what
its thread allocated since its previous sample. This program sums the weights by the first frame
outside the JDK, with the `jdk.jfr.consumer` API:

```java
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedFrame;
import jdk.jfr.consumer.RecordingFile;

/** Sums jdk.ObjectAllocationSample by the first frame outside the JDK. */
public class AllocBy {
    public static void main(String[] args) throws Exception {
        Map<String, long[]> byMethod = new TreeMap<>();         // method -> {samples, bytes}
        for (RecordedEvent e : RecordingFile.readAllEvents(Path.of(args[0]))) {
            if (!e.getEventType().getName().equals("jdk.ObjectAllocationSample")) continue;
            if (!e.getThread("eventThread").getJavaName().equals("main")) continue;
            String site = "(no application frame)";
            for (RecordedFrame f : e.getStackTrace().getFrames()) {
                String type = f.getMethod().getType().getName();
                if (!type.startsWith("java.") && !type.startsWith("jdk.") && !type.startsWith("sun.")) {
                    site = type + "." + f.getMethod().getName();
                    break;
                }
            }
            long[] sum = byMethod.computeIfAbsent(site, k -> new long[2]);
            sum[0]++;
            sum[1] += e.getLong("weight");
        }
        byMethod.forEach((site, sum) ->
                System.out.printf("%-26s %4d samples %,8d MB%n", site, sum[0], sum[1] >> 20));
    }
}
```

```text
(no application frame)        1 samples       20 MB
Invoices.customerName         1 samples        3 MB
Invoices.renderInvoice      304 samples   36,153 MB
```

304 samples stand for 36,153 MB, and the thread's own counter says 36,187 MB. The sampler takes
a fixed number of samples per second, so a count of samples follows time spent, not bytes. Here
the `+=` copies the whole string on every line, and the fix is a `StringBuilder`.

The split between methods is rougher than the total. In a worker alternating two methods, one
allocating 5,287 MB in large `StringBuilder` arrays and one 865 MB in small strings, JFR charged
the second 199 to 293 MB across five recordings. Read per-method figures as a ranking.

<!-- practice 2 -->

## JFR Lock Events Name the Lock, the Waiter and the Previous Owner, Above a Threshold

Four threads post to one ledger for two seconds, each post holding its monitor for 1 ms:

```java
synchronized void post(long cents) {
    long until = System.nanoTime() + 1_000_000;          // 1 ms of work under the lock
    while (System.nanoTime() < until) balance += cents % 3;
}
```

```text
posts: 1,995 in 2 s by 4 threads
$ jfr view contention-by-thread ledger.jfr
Thread                                               Count    Avg    P90   Max.
---------------------------------------------------- ----- ------ ------ ------
poster-0                                                 2 668 ms 953 ms 953 ms
poster-1                                                 3 575 ms 689 ms 689 ms
poster-2                                                 5 321 ms 547 ms 547 ms
poster-3                                                 5 259 ms 421 ms 421 ms
$ jfr print --stack-depth 2 --events jdk.JavaMonitorEnter ledger.jfr
jdk.JavaMonitorEnter {
  startTime = 01:12:27.846 (2026-11-07)
  duration = 154 ms
  monitorClass = Ledger (classLoader = app)
  previousOwner = "poster-0" (javaThreadId = 32)
  address = 0x732F3C000F60
  eventThread = "poster-3" (javaThreadId = 35)
  stackTrace = [
    Ledger.post(long) line: 9
...
```

1,995 posts of 1 ms in 2 s: four threads did the work of one. Together they waited about six
seconds, in single waits of up to 953 ms for a lock never held longer than 1 ms, so here the
monitor did not hand the lock over in arrival order. Each event names the lock's class, the thread
that waited, the thread that held it before, and the line that asked.

By default JFR records a monitor wait only from 20 ms up; with
`-XX:StartFlightRecording:jdk.JavaMonitorEnter#threshold=0ms,...` the same program recorded 36
waits instead of 15. And a `ReentrantLock` is not a monitor: the same ledger with one recorded no
`jdk.JavaMonitorEnter` events, and its waits were `jdk.ThreadPark` events with
`parkedClass = java.util.concurrent.locks.ReentrantLock$NonfairSync`.

## Without -Xmx the Heap Is a Quarter of the Memory the JVM Sees

The JVM sizes the heap from the memory it believes the machine has. In a container that is the
container's limit (`UseContainerSupport` is on by default). `-XX:MaxRAM` sets the number by hand,
which is how these runs stand in for containers of each size. A script ran
`java <flags> -XX:+PrintFlagsFinal -version` for each line and kept four flags:

```text
-XX:MaxRAM=128m                             MaxHeapSize     64 MB  InitialHeapSize     8 MB  G1
-XX:MaxRAM=256m                             MaxHeapSize    126 MB  InitialHeapSize     8 MB  G1
-XX:MaxRAM=400m                             MaxHeapSize    126 MB  InitialHeapSize     8 MB  G1
-XX:MaxRAM=512m                             MaxHeapSize    128 MB  InitialHeapSize     8 MB  G1
-XX:MaxRAM=1g                               MaxHeapSize    256 MB  InitialHeapSize    16 MB  G1
-XX:MaxRAM=4g                               MaxHeapSize   1024 MB  InitialHeapSize    64 MB  G1
-XX:MaxRAM=16g                              MaxHeapSize   4096 MB  InitialHeapSize   256 MB  G1
-XX:MaxRAM=1g -XX:MaxRAMPercentage=75       MaxHeapSize    768 MB  InitialHeapSize    16 MB  G1
-XX:MaxRAM=1g -Xmx800m                      MaxHeapSize    800 MB  InitialHeapSize    16 MB  G1
-XX:MaxRAM=4g -XX:ActiveProcessorCount=1    MaxHeapSize   1024 MB  InitialHeapSize    64 MB  Serial
(no flags)                                  MaxHeapSize  10176 MB  InitialHeapSize   640 MB  G1
```

With no flags in a 1 GB container the order service gets a 256 MB heap, because
`MaxRAMPercentage` defaults to 25. Between about 250 and 500 MB the heap stays at a floor of
126 MB; below that, `MinRAMPercentage` (50) applies. `-XX:MaxRAMPercentage=75` gives 768 MB and
follows the limit when the container is resized; `-Xmx` overrides both. The initial heap is 1/64
of the memory, at least 8 MB, and `-Xms` sets it.

The collector is chosen at startup too: with one CPU the JVM picked Serial instead of G1.

> **`MaxRAM` doesn't stand in for everything.** It changes the heap sizing, but not the collector
> choice: with `-XX:MaxRAM=1g` the JVM still picked G1. HotSpot's source picks G1 only with at
> least two CPUs and about 1.8 GB of physical memory, so a real 1 GB container may run Serial. We
> didn't run a container here; check with `-XX:+PrintFlagsFinal` inside yours.

## The Process Is Bigger Than the Heap

`-Xmx` limits the heap only. Thread stacks, class metadata, compiled code, the collector's own
structures and direct buffers live outside it, and Native Memory Tracking (NMT) counts them. A
gateway with a 256 MB heap, 200 waiting threads and 128 MB of `ByteBuffer.allocateDirect`, started
with `-Xms256m -Xmx256m -XX:NativeMemoryTracking=summary`:

```text
$ jcmd <pid> VM.native_memory summary scale=MB
Total: reserved=2030MB, committed=500MB
...
-                 Java Heap (reserved=256MB, committed=256MB)
...
-                     Class (reserved=1024MB, committed=0MB)
...
-                    Thread (reserved=228MB, committed=23MB)
                            (threads #227)
...
-                      Code (reserved=242MB, committed=7MB)
...
-                        GC (reserved=68MB, committed=68MB)
...
-                     Other (reserved=128MB, committed=128MB)
...
-                 Metaspace (reserved=64MB, committed=1MB)
...
```

The process committed 500 MB for a 256 MB heap; the direct buffers are the 128 MB under `Other`.
"Reserved" is address space and costs nothing. "Committed" is memory the process may touch; the
kernel charges each page to the container once it is touched, so plan the limit for it. Put this
gateway in a 512 MB container with `MaxRAMPercentage=75`: once the heap grows to 384 MB, the rest
needs 244 MB more, and the kernel kills the process (`OOMKilled`). There is no `OutOfMemoryError` and no heap dump, because the JVM never
ran out of heap. Size the heap as the limit minus what NMT shows for the rest.

<!-- practice 3 -->

## Summary

- **`GC.heap_info` counts garbage as used.** Live data is what a full collection leaves, and a
  plain `GC.class_histogram` forces that collection; `-all` doesn't.
- **A thread dump names a deadlock cycle, not every stuck thread.** Follow a `BLOCKED` thread's
  address to its `- locked` line and read what the holder waits for.
- **A JFR allocation sample stands for all the bytes since the previous one.** Sum `weight`, and
  look past the JDK's frames to your own.
- **JFR lock events name the lock, the waiter and the previous owner,** for monitor waits of 20 ms
  or more by default; `ReentrantLock` waits are `jdk.ThreadPark`.
- **Without `-Xmx` the heap is 25 % of the memory the JVM sees,** which in a container is its
  limit; `MaxRAMPercentage` changes the share.
- **The process is bigger than the heap.** The container limit must hold the heap plus what NMT
  shows for stacks, code, the collector and direct buffers.

## Reference

| Command (Java 21) | What it does |
|---|---|
| `jcmd` | lists the JVMs this user can attach to |
| `jcmd <pid> GC.heap_info` | heap capacity and used bytes, garbage included; no collection |
| `jcmd <pid> GC.class_histogram` | live objects per class; runs a full GC first (so does `jmap -histo:live`) |
| `jcmd <pid> GC.class_histogram -all` | all objects per class, garbage included; no collection |
| `jcmd <pid> Thread.print` | every thread's stack and locks; a deadlock section for a cycle of monitors or of `ReentrantLock`s |
| `jcmd <pid> JFR.start name=r`, `JFR.dump name=r filename=f.jfr`, `JFR.stop name=r` | start, save and stop a recording on a running JVM |
| `jcmd <pid> VM.native_memory summary` | committed memory by area; needs `-XX:NativeMemoryTracking=summary` at startup |
| `jfr view <view> f.jfr` | a table such as `allocation-by-site`, `contention-by-class`, `contention-by-thread` |
| `jfr print --events <type> f.jfr` | every event of a type, with its fields and stack |

| Event | Records | Default setting |
|---|---|---|
| `jdk.ObjectAllocationSample` | a sampled allocation; `weight` is the bytes since the thread's previous sample | 150 samples/s |
| `jdk.JavaMonitorEnter` | a wait to enter a `synchronized` block: `monitorClass`, `previousOwner`, `duration` | waits of 20 ms or more |
| `jdk.ThreadPark` | a park, such as a wait for a `ReentrantLock` or a latch: `parkedClass`, `duration` | 20 ms or more |

| Flag | Default | Effect |
|---|---|---|
| `-Xmx`, `-Xms` | from the percentages below | maximum and initial heap |
| `-XX:MaxRAMPercentage` | 25 | maximum heap as a share of the memory the JVM sees |
| `-XX:MinRAMPercentage` | 50 | the share used instead when that memory is small |
| `-XX:InitialRAMPercentage` | 1.5625 | initial heap as a share of that memory |
| `-XX:MaxRAM` | the machine's or container's memory | the memory the JVM assumes |

## Challenges

1. Your service's heap after each full collection grows by 30 MB a day. Which `jcmd` commands, run a
   day apart, tell you which class grows, and what does running them cost the service?
2. A recording has no `jdk.JavaMonitorEnter` events, yet throughput stops growing beyond four
   threads. What else in the recording can tell you why, and which setting do you change first?
3. A team runs 40 pods with `-Xmx` at 90 % of the limit. Using NMT on one pod, work out the limit
   they need, and what they gain or lose by switching to `MaxRAMPercentage`.

## Design Note: Why the Recorder Samples

A profiler that recorded every allocation would slow the program it measures, so JFR samples:
about 150 allocations a second, each weighted by the bytes it stands for, and lock waits only past
a threshold. That keeps a recording cheap enough to leave running in production. The price is that every number is an estimate, and anything below
the threshold or between samples is missing. A total built from weights comes close (36,153 MB
against 36,187 MB above); a count of samples doesn't. `jcmd` makes the opposite trade: its answers
are exact, but some of them, such as a plain class histogram, stop the program to get them.

## Code Examples

- Test: [JvmPerformanceTuningTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/jvminternals/JvmPerformanceTuningTest.java)
- Source: [JvmPerformanceTuning.java](src/main/java/com/github/msorkhpar/claudejavatutor/jvminternals/JvmPerformanceTuning.java)
