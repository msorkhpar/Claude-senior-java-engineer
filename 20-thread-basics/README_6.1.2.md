# 6.1.2. Starting Threads (start() Method)

The nightly settlement report computes four regional totals, each on its own thread. One morning
it said APAC had settled nothing. The job had exited with code 0; the only trace was a stack trace
in its stderr:

```text
Exception in thread "region-APAC" java.lang.ArithmeticException: / by zero
```

The code that started the threads waited for them inside a `try` that caught runtime exceptions,
and never saw the failure. `start()` hands the work to another thread and returns; the result, the
failure and the news that the thread has finished come back only if the caller collects them.

The examples use two helpers: `total(region)`, a few milliseconds of arithmetic returning a total in
cents, and `fetchTotal(region)`, which waits 200 ms like a call to a region's ledger service. Every
output comes from Java 21.

## start() Doesn't Wait for the New Thread to Do Anything

Four region threads are started and the totals printed on the next line:

```java
Long[] totals = new Long[REGIONS.size()];
List<Thread> workers = new ArrayList<>();
for (int i = 0; i < REGIONS.size(); i++) {
    int slot = i;
    Thread t = new Thread(() -> totals[slot] = total(REGIONS.get(slot)), "region-" + REGIONS.get(i));
    t.start();
    workers.add(t);
}
System.out.println("right after start(): " + Arrays.toString(totals));
for (Thread t : workers) t.join();
System.out.println("after join():        " + Arrays.toString(totals));
```

```text
right after start(): [null, null, null, null]
after join():        [999000000, 999000000, 999000000, 999000000]
```

`start()` makes the thread runnable and returns without waiting for `run()` to begin: the scheduler
decides when the new thread gets a CPU. Reading the array before the joins is a race.

Nor does the new thread reliably get going first. This program counts the runs in which the worker
appended `w` before the caller appended `m`:

```java
StringBuffer log = new StringBuffer();
Thread t = new Thread(() -> log.append('w'));
t.start();
log.append('m');
t.join();
if (log.charAt(0) == 'w') workerFirst++;
```

```text
on a 20-core machine:               worker first in 1 of 10000 runs
the same, pinned to one core:       worker first in 1410 of 10000 runs
```

Pinned to one core (`taskset -c 0`), the worker went first in about one run in seven. Nothing
orders the two threads after `start()`; the order you measure belongs to the machine you measured
on.

> **Virtual threads are daemon threads**, and the JVM exits once only daemon threads are left
> (unit 6.1.1). Returning from `main` cancels nothing, but if no non-daemon thread is left, the
> JVM's exit ends the virtual thread wherever it is. The report is never written:
>
> ```java
> Thread p = Thread.ofPlatform().unstarted(() -> { });
> Thread v = Thread.ofVirtual().unstarted(() -> { });
> System.out.println("platform daemon: " + p.isDaemon() + ", virtual daemon: " + v.isDaemon());
> Thread.ofVirtual().start(() -> {
>     fetchTotal("EMEA");
>     System.out.println("virtual thread wrote the report");
> });
> System.out.println("main returns");
> ```
>
> ```text
> platform daemon: false, virtual daemon: true
> main returns
> ```

## Results Come Back at join(): Start Every Worker, Then Join Every One

`t.join()` blocks the caller until `t` has terminated. Where the joins go decides whether the
threads save any time. Four ledger calls of 200 ms, joined two ways:

```java
for (String region : REGIONS) {
    Thread t = new Thread(() -> fetchTotal(region));
    t.start();
    t.join();                                   // waits for this region before starting the next
}
```

```java
List<Thread> workers = new ArrayList<>();
for (String region : REGIONS) {
    Thread t = new Thread(() -> fetchTotal(region));
    t.start();
    workers.add(t);
}
for (Thread t : workers) t.join();
```

```text
start, join, start, join: 804 ms
start all, then join all: 201 ms
```

The first loop runs the threads one after another, as slowly as no threads at all. The second has
all four calls waiting at once and takes as long as the slowest. Joining in start order is fine: if
the first thread is the slowest, the later joins return at once.

The plain `Long[]` was safe to print after the joins, with no `volatile` or lock, because threads
see each other's writes in an agreed order only where a happens-before rule says so, and the
memory-consistency rules of `java.util.concurrent` give two for threads:

- "A call to `start` on a thread *happens-before* any action in the started thread": what the
  caller wrote before `start()` is there when `run()` begins.
- "All actions in a thread *happen-before* any other thread successfully returns from a `join` on
  that thread": what the thread wrote is there when `join()` returns.

While both threads run, a plain field written by one and read by the other has no guarantee.

### Spot the Problem: A Done Flag Instead of join()

Instead of joining, a reader waits for a flag the worker sets when done:

```java
static boolean done;                                // plain field, no volatile
static long result;

Thread worker = new Thread(() -> {
    long r = 0;
    for (int i = 0; i < 300; i++) r += total("EMEA");   // about a second of work
    result = r;
    done = true;
});
Thread reader = new Thread(() -> {
    while (!done) { }                           // wait for the worker
    System.out.println("reader saw the result: " + result);
});
reader.setDaemon(true);
worker.start();
reader.start();
long t0 = System.nanoTime();
worker.join();
System.out.printf("worker finished after %d ms, done = %s%n", (System.nanoTime() - t0) / 1_000_000, done);
reader.join(2_000);
System.out.println("reader still waiting 2 s later: " + reader.isAlive());
```

```text
worker finished after 910 ms, done = true
reader still waiting 2 s later: true
```

The main thread joined the worker and sees `done = true`. The reader never does; it spins at
100 % of a core. No rule connects the worker's write of `done` to
the reader's reads, so the JIT compiler may read the field once and loop on that value forever, as
it did here.

Wait for a thread's work with `join()`; a signal while it keeps running needs a `volatile` field
(unit 6.2.2) or a `CountDownLatch`. `isAlive()`
is a hand-over too: by JLS 17.4.4, a thread that sees it return `false` also sees everything the
finished thread wrote.

<!-- practice 1 -->

## join(millis) Returns Without Saying Why, So Ask isAlive()

A plain `join()` on a ledger call that hangs waits forever; `join(millis)` bounds the wait:

```java
Thread stuck = new Thread(() -> {
    try {
        Thread.sleep(5_000);                    // a ledger call that hangs
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}, "region-APAC");
stuck.setDaemon(true);
stuck.start();
long t0 = System.nanoTime();
stuck.join(300);
System.out.printf("join(300) returned after %d ms, isAlive() = %s%n",
        (System.nanoTime() - t0) / 1_000_000, stuck.isAlive());
boolean ended = stuck.join(Duration.ofMillis(300));
System.out.println("join(Duration.ofMillis(300)) = " + ended);
```

```text
join(300) returned after 300 ms, isAlive() = true
join(Duration.ofMillis(300)) = false
```

`join(300)` returned the way it returns when the thread ends: no exception, no value. Ask
`isAlive()` afterwards, or code reads APAC's empty slot and reports it as a total. Since Java 19,
`join(Duration)` returns `true` only if the thread has terminated. Either way the thread is still
working; only your wait stopped (unit 6.1.3 shows how to stop it).

**`join(0)` waits forever.** The javadoc: "A timeout of 0 means to wait forever." It bites code
computing the time left on a deadline: 0.6 ms left rounds down to 0, and `slow` runs 400 ms:

```java
long deadline = System.nanoTime() + 600_000;                   // 0.6 ms left
long remainingMs = TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime());
long t0 = System.nanoTime();
slow.join(remainingMs);
```

```text
join(0) with 0.6 ms left returned after 400 ms
```

On a hung thread, a shutdown waits forever exactly when its budget has run out. Once the deadline
has passed, the negative remaining time makes `join(millis)` throw `IllegalArgumentException`.
`join(Duration)` has neither edge: zero or a negative duration only checks whether the thread has
terminated.

**A thread that was never started looks finished.** `join(millis)` "returns immediately, without
waiting, if the thread has not been started":

```text
join(1000) on a thread never started: returned after 0 ms, isAlive() = false, state NEW
join(Duration.ofSeconds(1)): java.lang.IllegalThreadStateException: Thread not started
```

`isAlive()` is `false` both before `start()` and after the end. A helper handed a `Thread` it
didn't start can't tell which unless it checks for `Thread.State.NEW` (unit 6.1.4).

<!-- practice 2 -->

## An Exception Ends Only Its Own Thread: Hand the Failure Over

The report from the start, with APAC's exchange rate missing and the joins inside a `try`:

```java
long[] totals = new long[REGIONS.size()];
List<Thread> workers = new ArrayList<>();
for (int i = 0; i < REGIONS.size(); i++) {
    int slot = i;
    String region = REGIONS.get(i);
    Thread t = new Thread(() -> {
        int rate = region.equals("APAC") ? 0 : 100;       // a missing exchange rate
        totals[slot] = total(region) / rate;
    }, "region-" + region);
    t.start();
    workers.add(t);
}
try {
    for (Thread t : workers) t.join();
    System.out.println("all joined, totals " + Arrays.toString(totals));
} catch (RuntimeException e) {
    System.out.println("the caller caught " + e);
}
```

```text
Exception in thread "region-APAC" java.lang.ArithmeticException: / by zero
	at Examples.lambda$crash$9(Examples.java:166)
	at java.base/java.lang.Thread.run(Thread.java:1583)
all joined, totals [9990000, 0, 9990000, 9990000]
```

An exception travels up the stack of the thread that threw it, and a started thread's stack begins
at `Thread.run()`. The caller's frames are on another stack, so no `catch` of the caller is in the
way. When the exception leaves `run()`, the JVM hands it to the thread's `UncaughtExceptionHandler`
(with none set, the default handler, or a stack trace on `System.err`). Then the thread terminates
like any other: `join()` returned normally, the `catch` never ran, and APAC's slot kept its 0.

The thread has to hand the failure over like a result, written where the caller reads after
`join()`: catch it at the top of the thread's work, or set a handler before `start()`:

```java
Throwable[] failures = new Throwable[1];
Thread t = new Thread(() -> {
    throw new ArithmeticException("no exchange rate for APAC");
}, "region-APAC");
t.setUncaughtExceptionHandler((thread, e) -> {
    failures[0] = e;
    System.out.println("handler runs on " + Thread.currentThread().getName() + ", alive: " + thread.isAlive());
});
t.start();
t.join();
System.out.println("after join(): " + failures[0]);
```

```text
handler runs on region-APAC, alive: true
after join(): java.lang.ArithmeticException: no exchange rate for APAC
```

The handler runs on the failing thread while it is still alive, "when a thread is about to
terminate due to an uncaught exception" (the javadoc). Its write is one of that thread's actions, so
`join()` hands it over like the totals.

If you catch inside the thread instead, catch `Throwable`. An `AssertionError` or a
`StackOverflowError` is not an `Exception`, and a `catch (Exception e)` lets it through to stderr.

<!-- practice 3 -->

## A Thread Runs Once: Run the Work Again on a New Thread

A region thread has ended or died, and its work has to run again. The obvious call is `start()`:

```java
Runnable job = () -> {
    fetchTotal("EMEA");
    System.out.println("EMEA fetched on " + Thread.currentThread().getName());
};
Thread t = new Thread(job, "region-EMEA");
t.start();
try {
    t.start();
} catch (IllegalThreadStateException e) {
    System.out.println("start() while " + t.getState() + ": " + e);
}
t.join();
try {
    t.start();
} catch (IllegalThreadStateException e) {
    System.out.println("start() when " + t.getState() + ": " + e);
}
Thread again = new Thread(job, t.getName());
again.start();
again.join();
```

```text
start() while RUNNABLE: java.lang.IllegalThreadStateException
EMEA fetched on region-EMEA
start() when TERMINATED: java.lang.IllegalThreadStateException
EMEA fetched on region-EMEA
```

A `Thread` goes from `NEW` to `TERMINATED` once (unit 6.1.4), and `start()` is legal only in `NEW`.
`Thread` doesn't give its `Runnable` back, so keep your own reference and create a new `Thread` for each run, with the old name
for readable thread dumps.

A supervisor that calls `start()` on a dead consumer gets `IllegalThreadStateException` in its own
thread and, unless it catches it, dies too; no consumer is restarted after that.

Nor is the thread's state the test for a dead worker. A consumer settles one payment and finds its
queue empty:

```text
settled payment-1
queue empty: state WAITING, isAlive() true
```

It is parked in `take()`, alive. A supervisor that restarts every worker that isn't `RUNNABLE`
starts another consumer on the same queue each time the queue runs dry. Ask `isAlive()` instead.

<!-- practice 4 -->

<!-- practice 5 -->

## Summary

- **`start()` doesn't wait for the new thread to do anything**, and no order between the two
  threads holds.
- **Results come back at `join()`: start every worker, then join every one.** `start()`, `join()`
  and an `isAlive()` that returns `false` are the hand-over points; a plain `done` flag is not.
- **`join(millis)` returns without saying why.** Ask `isAlive()`, or use `join(Duration)`. `join(0)`
  waits forever, and a never-started thread looks finished.
- **An exception ends only its own thread.** Hand the failure over by catching `Throwable` in the
  thread or with an `UncaughtExceptionHandler`, and read it after `join()`.
- **A `Thread` runs once.** Restart work on a new `Thread`, and decide that the old one is dead
  with `isAlive()`, not with its state.

## Reference

| Call | Effect (Java 21) |
|---|---|
| `t.start()` | starts `t` and returns at once; `IllegalThreadStateException` unless `t` is `NEW` |
| `t.join(millis)` | waits at most `millis` ms and returns nothing either way; `0` waits forever; a negative value throws `IllegalArgumentException`; returns at once if `t` was never started |
| `t.join(Duration)` (Java 19+) | waits at most that long and returns `true` if `t` has terminated; zero or negative only checks; `IllegalThreadStateException` if `t` was never started |
| `t.isAlive()` | `true` from `start()` until the thread terminates (after any uncaught-exception handler has run); `false` before `start()` too |

## Challenges

These have no single answer.

1. Measure start-all-then-join-all for 1,000 regions with platform and with virtual threads. Where
   does each run out?
2. Write `joinAll(List<Thread> threads, Duration budget)` sharing one deadline and returning the
   threads still alive. What should it report for a thread that ends just after its join timed out?
3. Design the smallest object that carries a result, a failure or nothing (it hung) to the caller,
   then compare it with `Future` in unit 6.4.2.

## Design Note: Why a Thread Hands Nothing Back

`run()` returns `void` and declares no checked exception, so the work has no typed way to return
anything; where the result goes, how the failure travels and how long to wait are left to you.
`ExecutorService` and `Future` (unit 6.4.2) package the three: `get()` returns the result, throws the
failure wrapped in an `ExecutionException`, and takes a timeout.

## Code Examples

- Source: [ThreadStarting.java](src/main/java/com/github/msorkhpar/claudejavatutor/threadbasics/ThreadStarting.java)
- Test: [ThreadStartingTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/threadbasics/ThreadStartingTest.java)
