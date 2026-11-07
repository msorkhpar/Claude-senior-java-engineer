# 6.7.2. Differences Between Virtual Threads and Platform Threads

An order service runs its requests on `Executors.newFixedThreadPool(200)`. On Java 21 we change
that one line to `Executors.newVirtualThreadPerTaskExecutor()`, the tests pass, and it ships.
During the next peak the database refuses connections, the dashboard's "busy workers" gauge reads
0, and one node stops answering for a second at a time while its CPU sits idle.

Virtual threads implement the same `Thread` API as platform threads (unit 6.7.1), so the code
compiles and runs; where the two kinds of thread differ, it can misbehave. Every output below is
from Java 21 on a machine with 20 CPUs.

## A Blocked Virtual Thread Gives Its Carrier Back; a Blocked Platform Thread Keeps Its OS Thread

A platform thread is an OS thread: while it blocks, the OS thread blocks with it. A virtual thread
runs on a platform thread, its **carrier**, only while it computes. When it blocks in `sleep`, a
`java.util.concurrent` lock or queue, or a socket read, it **unmounts**, and the carrier runs
another virtual thread. Unit 6.7.1 shows how, what a waiting virtual thread costs, and why a
`ThreadLocal` stops being a cache.

## A Fixed Pool Was Also a Limit: Put a Semaphore in Front of What Is Scarce

A database has 10 connections and a query takes 20 ms. A caller that finds none free waits 20 ms
for one, then gets an error, as a connection pool does when its timeout runs out. "In flight"
counts the callers inside `query()`, holding a connection or waiting for one. 1,000 tasks query it, on a pool of 10,
on virtual threads, and on virtual threads that first take a permit from a `Semaphore(10)`:

```java
executor.submit(() -> {
    permits.acquire();
    try {
        db.query();
    } finally {
        permits.release();
    }
    return null;
});
```

```text
pool of 10:                  peak in flight   10, refused   0, 2062 ms
virtual, one per task:       peak in flight 1000, refused 990,   60 ms
virtual + Semaphore(10):     peak in flight   10, refused   0, 2085 ms
```

`newFixedThreadPool(10)` did two jobs: it ran the tasks, and it kept more than 10 of them from
reaching the database at once. Nobody wrote the second job down, and the migration removed it. A
real pool's timeout is longer than our 20 ms, but once requests arrive faster than the connections
can serve them for longer than that timeout, callers fail the same way, and their retries arrive on
top of the next requests.

A task waiting in `acquire()` is a parked virtual thread, an object on the heap rather than an OS
thread, so 990 of them waiting are cheap. Size the semaphore to the resource (its connections, its
concurrency limit), and take a permit for each call, not for each task: a task that answers from a
cache, or the part of a task that formats the result, never touches the database and has no reason
to hold a permit.

`acquire()` goes before the `try`. If it throws `InterruptedException`, no permit was taken, yet a
`finally` around it would release one. `Semaphore` doesn't check: its javadoc says a thread that
releases a permit need not have acquired it, so the limit silently rises by one.

> **Pooling virtual threads is not the fix.** `Executors.newFixedThreadPool(10,
> Thread.ofVirtual().factory())` brings the limit back, but for every task, including the ones that
> never reach the database.

<!-- practice 1 -->

## On Java 21, Blocking Inside synchronized Holds the Carrier

A token cache refreshes an expired token with a 1-second call to the identity provider:

```java
final class SyncTokens {
    private String token;
    synchronized String token() throws InterruptedException {
        if (token == null) {
            Thread.sleep(1_000);                         // the call to the identity provider
            token = "t-1";
        }
        return token;
    }
}
```

We run with `-Djdk.virtualThreadScheduler.parallelism=4`, so there are four carriers. Four requests
call `token()` at once; then we start one unrelated virtual thread and time how long it takes to
run. The second line uses the same cache with a `ReentrantLock` locked and unlocked around the
same body:

```text
synchronized:  an unrelated task waited  944 ms to start
ReentrantLock: an unrelated task waited    0 ms to start
```

On Java 21 a virtual thread can't unmount while it holds a monitor, or while it waits to enter
one. It is **pinned**: its carrier blocks with it. The first request holds the monitor and sleeps
on its carrier; the other three wait to enter on theirs. All four carriers are taken, so nothing
else on the JVM runs until the refresh returns, although the CPU is idle. With `ReentrantLock`, the holder unmounts while it sleeps, the three waiters
park, and the unrelated task starts at once.

The scheduler adds no carrier for this kind of pinning, so the unrelated task waited until the
refresh returned. It does add one while a virtual thread is in `Object.wait()`, which pins too, so
waiting on a monitor costs an extra OS thread rather than a stall.

On Java 21, use a `java.util.concurrent` lock where code blocks while holding the lock (a network
call, a query, a `sleep`). A `synchronized` block that only updates memory pins its carrier for
microseconds and can stay. To find the blocking ones, run with `-Djdk.tracePinnedThreads=short`.
It prints the stack where a pinned virtual thread blocked:

```text
VirtualThread[#28]/runnable@ForkJoinPool-1-worker-1 reason:MONITOR
    SyncTokens.token(Pinning.java:42) <== monitors:1
```

It names the holder only; the three threads waiting to enter don't appear. In production, a JFR
recording does the same with the `jdk.VirtualThreadPinned` event, for each pinned block of 20 ms
or more.

> **Java 24 and later differ.** JEP 491 lets a virtual thread unmount inside `synchronized` and
> while it waits to enter a monitor. On Java 25 the `synchronized` cache's unrelated task waits
> 4 ms and this program records no `jdk.VirtualThreadPinned` event. The `jdk.tracePinnedThreads`
> property is gone from the JDK's source and prints nothing; the JFR event remains for what still
> pins, such as a native method on the stack.

## A Virtual Thread Is Always a Daemon, at Normal Priority

Code written for platform threads sets a thread's daemon status and priority. A virtual thread
rejects the first and ignores the second:

```java
Thread flusher = Thread.ofVirtual().name("flusher").unstarted(() -> { });
System.out.println("daemon: " + flusher.isDaemon() + ", group: " + flusher.getThreadGroup().getName());
flusher.setPriority(Thread.MAX_PRIORITY);
System.out.println("priority after setPriority(10): " + flusher.getPriority());
try {
    flusher.setDaemon(false);
} catch (IllegalArgumentException e) {
    System.out.println("setDaemon(false): " + e);
}
Thread.ofVirtual().start(() -> {
    try { Thread.sleep(100); } catch (InterruptedException e) { return; }
    System.out.println("flushed");                    // never printed: main returns first
});
System.out.println("main returns");
```

```text
daemon: true, group: VirtualThreads
priority after setPriority(10): 5
setDaemon(false): java.lang.IllegalArgumentException: 'false' not legal for virtual threads
main returns
```

The JVM exits when its last non-daemon thread ends, and virtual threads never hold it up: the
flush never ran. A virtual thread's priority is fixed at `NORM_PRIORITY`, `setPriority` with a
value from 1 to 10 is ignored without an error, and every virtual thread is in the `VirtualThreads`
group. `Thread.ofVirtual()` has no `daemon()` or `priority()` method, so builder code can't even
ask; only a setter called on a thread you were handed fails, at run time.

A thread that must keep the JVM alive until it finishes, such as a log flusher on shutdown, is a
platform thread.

<!-- practice 3 -->

## Thread Listings Leave Virtual Threads Out

The "busy workers" gauge counted the pool's threads that were running a request. After the
migration it reads 0. Here 1,000 virtual threads, named `order-0`, `order-1`, … by
`Thread.ofVirtual().name("order-", 0).factory()`, all wait on a latch while we look for them:

```java
long listed = Thread.getAllStackTraces().keySet().stream()
        .filter(t -> t.getName().startsWith("order-")).count();
```

```text
order- threads in Thread.getAllStackTraces(): 0
ThreadMXBean.getThreadCount():               26
order- threads in jcmd Thread.dump_to_file:  1000
```

`Thread.getAllStackTraces()` returns "all live platform threads", in its javadoc's words, and
`ThreadMXBean` "does not support monitoring or management of virtual threads". Anything built on those APIs sees an idle
service: a gauge, a watchdog that looks for a request stuck for a minute, a health check that
counts workers.

`jcmd <pid> Thread.dump_to_file -format=json <file>` does write every virtual thread, with its
stack. For a number on a dashboard, count the work yourself, in the code that starts and ends it.

<!-- practice 2 -->

## Summary

- **A blocked virtual thread unmounts and frees its carrier.** Waiting tasks no longer need an OS
  thread each.
- **A fixed pool was also a limit on what the tasks call.** Without it, a `Semaphore` sized to the
  resource keeps the database, or the API, from being flooded.
- **On Java 21, blocking inside `synchronized` pins the carrier,** and enough pinned carriers stop
  every virtual thread in the JVM. Use a `ReentrantLock` where code blocks while holding the lock;
  Java 24 and later no longer pin there.
- **A virtual thread is always a daemon at `NORM_PRIORITY`.** A thread that must keep the JVM alive
  is a platform thread.
- **Thread listings and `ThreadMXBean` show platform threads only.** Count virtual work yourself,
  and use `jcmd` to see the threads.

## Reference

**Platform and virtual threads side by side (Java 21):**

| | Platform thread | Virtual thread |
|---|---|---|
| Runs on | its own OS thread | a carrier: a platform thread of a `ForkJoinPool`, one per available processor by default (`-Djdk.virtualThreadScheduler.parallelism`) |
| Blocked in `sleep`, `ReentrantLock.lock`, `BlockingQueue.take`, a socket read | holds its OS thread | unmounts; the carrier runs other virtual threads |
| Blocked inside `synchronized`, or waiting to enter it | holds its OS thread | pinned: holds its carrier, and no carrier is added (not on JDK 24+) |
| `Object.wait()` | holds its OS thread | pinned, but the scheduler adds a carrier while it waits (measured; not on JDK 24+) |
| `isDaemon()` | as set; by default that of the thread that created it | always `true`; `setDaemon(false)` throws `IllegalArgumentException` |
| `getPriority()` | as set | always `NORM_PRIORITY` (5); `setPriority` ignored |
| `getThreadGroup()` | as set | `VirtualThreads` |
| `Thread.getAllStackTraces()`, `ThreadMXBean` | listed | not listed |
| `jcmd <pid> Thread.dump_to_file -format=json` | listed | listed |

**Finding pinning:**

| Tool | Java 21 | Java 24 and later |
|---|---|---|
| `-Djdk.tracePinnedThreads=short` or `=full` | prints the stack where a pinned virtual thread blocked | removed; prints nothing |
| JFR event `jdk.VirtualThreadPinned` | a pinned block of 20 ms or more (default threshold) | only for what still pins, such as native frames |

## Challenges

1. Your service calls three downstream systems with limits of 50 connections, 200 requests per
   second and "be gentle". Which of them does a `Semaphore` protect, and what does the second one
   need instead?
2. A third-party client library you can't change uses `synchronized` around its network calls.
   On Java 21, what can you do, and what would you measure to decide whether it matters?
3. List the places in a service you know that read `Thread.getAllStackTraces()`, a thread pool's
   `getActiveCount()` or a thread's name to report health. What does each report after a move to
   virtual threads?

## Design Note: One Thread Class for Two Kinds of Thread

The JDK could have given virtual threads a type of their own. It made them `Thread`s instead, so
that `ThreadLocal`, `Thread.currentThread()`, thread dumps, debuggers and twenty years of blocking
code work on them unchanged. That is why the migration at the top of this unit was one line.

The cost is that the differences don't show in the types. A pool-sized limit, a `ThreadLocal` cache
(unit 6.7.1) and a `synchronized` block around a network call all compile and pass tests that run ten tasks. They
show up under load, which is where the migration was meant to help.

## Code Examples

- Test: [VirtualVsPlatformThreadsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/virtualthreads/VirtualVsPlatformThreadsTest.java)
- Source: [VirtualVsPlatformThreads.java](src/main/java/com/github/msorkhpar/claudejavatutor/virtualthreads/VirtualVsPlatformThreads.java)
