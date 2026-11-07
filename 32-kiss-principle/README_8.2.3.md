# 8.2.3. Benefits of KISS for Concurrency

A box office sells 500 seats per show. Its ticket counter keeps two numbers, seats `free` and
seats `held` in a basket, and a health check pages the on-call engineer whenever
`free + held` isn't 500. A few times a week it fires, with 499 or 501. Nobody can reproduce it, and the
counter's concurrency test passes. The counter was written lock-free, for speed:

```java
/** A box office's ticket counter. Every ticket is either free or held: free + held == capacity. */
public interface Tickets {
    boolean hold();
    void release();
    /** What the health check reads: free + held. */
    int total();
}
```

```java
import java.util.concurrent.atomic.AtomicInteger;

/** Two atomic counters and no lock: hold() never blocks. */
public final class LockFreeTickets implements Tickets {
    private final AtomicInteger free;
    private final AtomicInteger held = new AtomicInteger();
    private final Runnable midHold;                    // a test hook; () -> {} in production

    public LockFreeTickets(int capacity, Runnable midHold) {
        this.free = new AtomicInteger(capacity);
        this.midHold = midHold;
    }

    @Override public boolean hold() {
        int f;
        do {
            f = free.get();
            if (f == 0) return false;
        } while (!free.compareAndSet(f, f - 1));
        midHold.run();
        held.incrementAndGet();
        return true;
    }

    @Override public void release() {
        held.decrementAndGet();
        free.incrementAndGet();
    }

    @Override public int total() {
        return free.get() + held.get();
    }
}
```

The simple version has two `int` fields and the same three methods, each `synchronized`:

```java
/** Both counts behind one monitor: after construction, every read and write of free and held holds its lock. */
public final class SyncTickets implements Tickets {
    private int free;
    private int held;
    private final Runnable midHold;

    public SyncTickets(int capacity, Runnable midHold) {
        this.free = capacity;
        this.midHold = midHold;
    }

    @Override public synchronized boolean hold() {
        if (free == 0) return false;
        free--;
        midHold.run();
        held++;
        return true;
    }

    @Override public synchronized void release() {
        held--;
        free++;
    }

    @Override public synchronized int total() {
        return free + held;
    }
}
```

## A Tool That Runs Every Schedule of a Small Model

A test runs the threads in whatever order the machine picks. To see every order, we model each
method as a list of **atomic steps**, actions no other thread can interleave with, and let a
small explorer try every schedule: at each point, any thread that isn't finished may take its
next step.

```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Runs every schedule of a few threads over a small shared state and counts what happened. */
public final class Schedules {

    /** One atomic step of one thread. Returns the thread's next step, or -1 when it is done. */
    public interface Step {
        int run(int[] shared, int[] local);
    }

    private final List<Step[]> threads = new ArrayList<>();
    private final Set<String> states = new HashSet<>();
    private long schedules, broken;

    public Schedules thread(Step... steps) { threads.add(steps); return this; }

    /** shared[2] counts wrong sums seen; a schedule is broken if any step saw one. */
    public void explore(String name, int... shared) {
        int n = threads.size();
        int[] pcs = new int[n];
        dfs(shared, pcs, new int[n][2]);
        System.out.printf("%-30s %,9d schedules %,6d states, wrong sum in %,d%n",
                name, schedules, states.size(), broken);
    }

    private void dfs(int[] shared, int[] pcs, int[][] locals) {
        states.add(Arrays.toString(shared) + Arrays.toString(pcs) + Arrays.deepToString(locals));
        boolean anyLeft = false;
        for (int t = 0; t < pcs.length; t++) {
            if (pcs[t] < 0) continue;
            anyLeft = true;
            int[] sh = shared.clone();
            int[] pc = pcs.clone();
            int[][] loc = new int[locals.length][];
            for (int i = 0; i < locals.length; i++) loc[i] = locals[i].clone();
            pc[t] = threads.get(t)[pcs[t]].run(sh, loc[t]);
            dfs(sh, pc, loc);
        }
        if (!anyLeft) {
            schedules++;
            if (shared[2] > 0) broken++;
        }
    }
}
```

A **schedule** is one complete order of every thread's steps; a **state** is the shared values
plus each thread's position and locals.

## Each Step Other Threads Can Run Between Multiplies the Schedules: 6 for synchronized, 1,124 for Lock-Free

The model has 4 seats (`CAP`), and `shared` holds `{free, held, wrong sums seen}`. The lock-free
`hold()` is three steps, because another thread can run between any two of them, and the check
is two:

```java
static final Schedules.Step[] SPLIT_HOLD = {
    (sh, l) -> { l[0] = sh[0]; return l[0] == 0 ? -1 : 1; },                // f = free.get()
    (sh, l) -> { if (sh[0] != l[0]) return 0; sh[0] = l[0] - 1; return 2; }, // free.compareAndSet(f, f - 1)
    (sh, l) -> { sh[1]++; return -1; }                                        // held.incrementAndGet()
};
static final Schedules.Step[] SPLIT_CHECK = {
    (sh, l) -> { l[0] = sh[0]; return 1; },                                   // free.get()
    (sh, l) -> { if (l[0] + sh[1] != CAP) sh[2]++; return -1; }               // held.get(), compare
};
```

In `SyncTickets` no other thread runs inside a method, so each method is one step. A third
version, `SnapshotTickets` (one of unit 8.2.2's constructs), keeps both counts in one immutable `record Count(int free, int held)`
and swaps it with one `compareAndSet` on an `AtomicReference<Count>`: its `hold()` is two steps
(read the record, swap in a new one or retry; the model uses a version number for the
reference) and its check is one read. One check runs against one, two and three holders (every
output on this page is from Java 21 on a 20-thread x86-64 machine):

```text
lock-free,    hold x1 + check         10 schedules     18 states, wrong sum in 3
synchronized, hold x1 + check          2 schedules      4 states, wrong sum in 0
record,       hold x1 + check          3 schedules      6 states, wrong sum in 0
lock-free,    hold x2 + check      1,124 schedules    178 states, wrong sum in 520
synchronized, hold x2 + check          6 schedules      8 states, wrong sum in 0
record,       hold x2 + check         38 schedules     28 states, wrong sum in 0
lock-free,    hold x3 + check  2,750,676 schedules  2,406 states, wrong sum in 1,527,084
synchronized, hold x3 + check         24 schedules     16 states, wrong sum in 0
record,       hold x3 + check      5,766 schedules    196 states, wrong sum in 0
```

With one step per method, three threads have 3! = 6 orders. Each extra step multiplies that:
steps of 3, 3 and 2 alone give 8!/(3!·3!·2!) = 560 orders, and each failed `compareAndSet` adds
a retry: the lock-free version has 1,124 schedules for two
holds and a check, and 2.75 million for three.

520 of the 1,124 lock-free schedules break the invariant, in both directions. A check that
reads `free` after a hold took a seat, and `held` before the hold counted it, sees 3 of 4; one
that reads `free` before and `held` after sees 5. Each `AtomicInteger` is correct on its own.
The invariant spans two of them, and nothing makes the pair change at once.

## A Stress Test Samples the Likely Schedules, So It Seldom Hits the Broken Ones

The counter's concurrency test runs two holds and a check, released together by a latch, and the check must see 500. It ran 10,000 times per version, five times
over:

```text
lock-free  test shape: failed 39 of 10,000
synced     test shape: failed 0 of 10,000
record     test shape: failed 0 of 10,000
lock-free  test shape: failed 36 of 10,000
synced     test shape: failed 0 of 10,000
record     test shape: failed 0 of 10,000
lock-free  test shape: failed 33 of 10,000
synced     test shape: failed 0 of 10,000
record     test shape: failed 0 of 10,000
lock-free  test shape: failed 18 of 10,000
synced     test shape: failed 0 of 10,000
record     test shape: failed 0 of 10,000
lock-free  test shape: failed 20 of 10,000
synced     test shape: failed 0 of 10,000
record     test shape: failed 0 of 10,000
```

The model says 520 of 1,124 schedules are broken; the test fails about 3 times in 1,000. The
schedules aren't equally likely. The gap between `compareAndSet` and `incrementAndGet` is a few
instructions, and the threads leave the latch at slightly different moments, so most runs
execute one call after another. A test run once per build fails about one build in 340.

The zeros for the other two versions show only that these runs met no broken schedule. That
none exists comes from the model, which tried every schedule of four seats and up to four
threads, taking each step as atomic and each write as visible at once: what the lock and the
`AtomicReference` provide.

## In the Simple Design the Race Has No Schedule, and Its Correctness Argument Is One Sentence

A race the model finds can be forced in a real test by pausing one thread at the dangerous
point, as unit 5.3.1 does; a pause after any step is a schedule the real system can produce. Here the `midHold` hook pauses `hold()` between its two writes, and
the health check runs meanwhile:

```java
Tickets tickets = make.apply(() -> {             // runs between the two writes
    paused.countDown();
    try { resume.await(5, TimeUnit.SECONDS); } catch (InterruptedException e) { throw new IllegalStateException(e); }
});
ExecutorService pool = Executors.newFixedThreadPool(2);
Future<Boolean> hold = pool.submit(tickets::hold);
paused.await(5, TimeUnit.SECONDS);               // the holder is now between the writes
Future<Integer> check = pool.submit(tickets::total);
try {
    System.out.println(name + ": check returned " + check.get(200, TimeUnit.MILLISECONDS)
            + " while hold() was paused");
} catch (TimeoutException e) {
    System.out.println(name + ": check still waiting after 200 ms");
}
resume.countDown();
hold.get(5, TimeUnit.SECONDS);
System.out.println(name + ": that check returned " + check.get(5, TimeUnit.SECONDS)
        + "; a new check after hold() returns " + tickets.total());
```

```text
lock-free: check returned 499 while hold() was paused
lock-free: that check returned 499; a new check after hold() returns 500
synchronized: check still waiting after 200 ms
synchronized: that check returned 500; a new check after hold() returns 500
```

The lock-free check returns 499 every time it meets a paused hold; only a check made after the
hold sees 500. The synchronized check can't
run until the hold is done: the paused thread holds the monitor, and `total()` needs it.

The correctness argument for `SyncTickets` is one sentence: after the constructor, every read
and write of `free` and `held` happens while holding this object's monitor, so no thread sees
one changed without the other. A reviewer checks it by finding every use of the two fields: all
of them are in three `synchronized` methods. For `SnapshotTickets` it is also
one sentence: the state is one immutable record, and every change replaces it with one
`compareAndSet`, so a reader gets the old record or the new one, never half of each. The
lock-free version has no such sentence. Its argument would have to cover every point between
two atomic calls, and the first one already fails.

<!-- practice 1 -->

## A Thread Dump Names a Monitor's Holder; a Hand-Rolled Lock Shows Only Busy Threads

Suppose the team fixes the torn read with a spin lock instead of `synchronized`, because "it makes no
kernel call, so it's cheaper than `synchronized`". `SpinTickets` has the two `int` fields of
`SyncTickets` and wraps every method in this lock, with `unlock()` in a `finally`:

```java
private final AtomicBoolean busy = new AtomicBoolean();

private void lock() {
    while (!busy.compareAndSet(false, true)) Thread.onSpinWait();
}

private void unlock() {
    busy.set(false);
}
```

By the same one-sentence argument it is correct, and the stress test agrees: 0 failures in five
runs of 10,000. Then one holder stalls inside `hold()`, with the `midHold` hook standing for an
audit write to a disk that stopped answering, while another counter thread and the health
check call in. Printed through unit 6.1.4's `Dump` helper (state, the lock waited for and its
holder, CPU used in a window of about 200 ms):

```text
spin lock:
counter-1      WAITING        cpu   0 ms  waits for CountDownLatch$Sync
counter-2      RUNNABLE       cpu 216 ms
health-check   RUNNABLE       cpu 217 ms
synchronized:
counter-1      WAITING        cpu   0 ms  waits for CountDownLatch$Sync
counter-2      BLOCKED        cpu   0 ms  waits for SyncTickets held by counter-1
health-check   BLOCKED        cpu   0 ms  waits for SyncTickets held by counter-1
```

With the spin lock, the waiting threads look like threads at work: `RUNNABLE`, a core each, no
lock and no owner. With `synchronized` they are `BLOCKED`, and the line names the thread to look
at. `jcmd <pid> Thread.print` says the same. In this excerpt the first two threads are from the
spin-lock run and the last two from the synchronized run; `...` marks left-out frames:

```text
"counter-2" #29 [1205223] daemon prio=5 os_prio=0 cpu=3245.94ms elapsed=3.25s tid=0x000071a4c0144520 nid=1205223 runnable  [0x000071a48e2fd000]
   java.lang.Thread.State: RUNNABLE
	at SpinTickets.lock(SpinTickets.java:16)
	at SpinTickets.hold(SpinTickets.java:24)

"counter-1" #28 [1205222] daemon prio=5 os_prio=0 cpu=0.42ms elapsed=3.25s tid=0x000071a4c0143280 nid=1205222 waiting on condition  [0x000071a48e3fe000]
   java.lang.Thread.State: WAITING (parking)
...
	at Stall.lambda$stall$0(Stall.java:11)
	at Stall$$Lambda/0x000071a440001800.run(Unknown Source)
	at SpinTickets.hold(SpinTickets.java:28)

"counter-2" #32 [1205227] daemon prio=5 os_prio=0 cpu=0.16ms elapsed=3.02s tid=0x000071a4c019bea0 nid=1205227 waiting for monitor entry  [0x000071a48defe000]
   java.lang.Thread.State: BLOCKED (on object monitor)
	at SyncTickets.hold(SyncTickets.java:13)
	- waiting to lock <0x00000005abc39070> (a SyncTickets)

"counter-1" #31 [1205226] daemon prio=5 os_prio=0 cpu=0.15ms elapsed=3.02s tid=0x000071a4c019adf0 nid=1205226 waiting on condition  [0x000071a48dffe000]
   java.lang.Thread.State: WAITING (parking)
...
	at Stall.lambda$stall$0(Stall.java:11)
	at Stall$$Lambda/0x000071a440001800.run(Unknown Source)
	at SyncTickets.hold(SyncTickets.java:15)
	- locked <0x00000005abc39070> (a SyncTickets)
```

The address after `waiting to lock` matches one `- locked` line, in the stack of the thread that
holds the monitor, and that stack shows where it is stuck. The spin lock's holder has
`SpinTickets.hold` in its stack and no `- locked` line: to find it, you read every stack in the
dump for a thread inside `SpinTickets` but not in `lock()`. An `AtomicBoolean` records only
`true`, not who set it, so the deadlock detector of unit 5.3.4 can't see a spin lock either.

A monitor also releases itself: a `synchronized` method or block that ends with an exception
gives up the lock (JLS §8.4.3.6, §14.19). A hand-written lock is released only where every
caller wrote the `finally`. Two methods throw while holding a lock, and another thread then tries
to get in:

```java
static void viaMonitor() {
    synchronized (monitor) {
        throw new IllegalStateException("audit write failed");
    }
}

static void viaLockWithoutFinally() {
    lock.lock();
    if (true) throw new IllegalStateException("audit write failed");
    lock.unlock();
}
```

```text
after the throw, another thread entered the synchronized block: true
after the throw, another thread got the ReentrantLock within 1 s: false
```

<!-- practice 2 -->

## Simplicity Costs Throughput Under Contention: Measure It Against the Load You Have

The argument for the lock-free counter was speed. Here each thread holds and releases a seat in
a loop for one second, after a one-second warm-up, and the numbers are pairs per second, all
threads together:

```text
 1 threads: two atomics  58,584,886  spin  55,380,569  synchronized  48,537,410  record  67,967,383
 4 threads: two atomics   5,376,873  spin   2,252,025  synchronized   5,921,973  record   4,791,133
16 threads: two atomics   9,789,987  spin   1,129,521  synchronized   4,210,137  record   1,975,598
```

That is one of three runs. At 16 threads the two atomics made 9.7 to 9.8 million pairs a second
and `synchronized` 4.1 to 7.6 million: the broken version was 1.3 to 2.4 times as fast. In all
three runs, at 4 and 16 threads, the spin lock written to be cheaper was the slowest, and the
record was slower than `synchronized`, probably because every failed `compareAndSet` throws
away a new record and tries again.

A counter that takes 2,000 holds a second at its busiest uses at most about a two-thousandth
of what `synchronized` handled here. As in unit 8.2.1, the stated load decides, and here the
simple design meets it with that much room. A loop that does
nothing but take a lock overstates contention; a real request spends most of its time
elsewhere.

## Summary

- **Each step other threads can run between multiplies the schedules.** Two holds and a check
  have 6 schedules when each method is one step, and 1,124 in the lock-free counter, 520 of them
  broken.
- **A stress test samples the likely schedules.** It failed about 3 times in 1,000 against a
  design the model shows is wrong in 520 of its 1,124 schedules.
- **In the simple design the race has no schedule.** A paused `hold()` makes the lock-free check
  return 499 every time; the synchronized check waits and returns 500. Its correctness argument
  is one sentence a reviewer can check.
- **A dump names a monitor's holder.** Threads waiting on a spin lock show as `RUNNABLE` and burn
  CPU, with no owner and no `- locked` line.
- **The simple design costs throughput under contention.** Measure it against the load you
  have before trading its properties for speed.

## Reference

**The ticket counter, two holds and a check (model with 4 seats; Java 21 runs):**

| Version | Steps per `hold()` / check | Schedules | Broken | Test shape, failures in 10,000 | A waiting thread in a dump |
|---|---|---|---|---|---|
| two `AtomicInteger`s | 3 / 2 | 1,124 | 520 | 18 to 39 | never waits |
| `synchronized` | 1 / 1 | 6 | 0 | 0 | `BLOCKED`, holder named |
| `AtomicReference<Count>` | 2 / 1 | 38 | 0 | 0 | never waits |
| spin lock on `AtomicBoolean` | not modelled | | | | `RUNNABLE`, no holder, uses CPU |

**Who `ThreadMXBean.getThreadInfo` names as the holder (Java 21):**

| Waiting in | State | `getLockOwnerName()` |
|---|---|---|
| `synchronized` | `BLOCKED` | the holder |
| `ReentrantLock.lock()` | `WAITING` | the holder |
| `ReentrantReadWriteLock` read lock while the write lock is held | `WAITING` | the writer |
| a spin on `AtomicBoolean.compareAndSet` | `RUNNABLE` | `null` |

## Challenges

1. Add `release()` to the model of the lock-free counter. How many schedules do one hold, one
   release and one check have, and which wrong sums can the check see?
2. The box office adds a third count, `sold`. Write the correctness sentence for a
   `synchronized` version and for a record version. Which one still fits in a sentence if a
   sale must also update a revenue total kept in another object?
3. A service is stuck, and the dump shows 40 threads `RUNNABLE` in one class's `acquire()`,
   none owning anything. How do you find the holder, and what would you change so the next dump
   tells you?
4. Rerun the throughput loop with a few microseconds of work outside the lock in each
   iteration. At what amount of work does the difference between the versions stop mattering?

## Design Note: Why the JVM Knows Who Holds a Monitor

Every Java object can be locked, and the JVM implements that lock itself, so it can always tell
which thread owns a monitor. That is what lets a dump print
`- locked` and `waiting to lock` with matching addresses, lets `ThreadMXBean` name an owner, and
lets the deadlock detector find a cycle. The `java.util.concurrent` locks keep their owner in
`AbstractOwnableSynchronizer`, whose javadoc says the value is there to help "monitor access and
provide diagnostics". No dump, `ThreadMXBean` call or deadlock check can name the holder of a
lock built from an `AtomicBoolean`.

The price is taking the JDK's semantics as they are: a monitor has no timeout and no `tryLock`,
and a `ReentrantLock` needs a `finally` in every caller.

## Code Examples

- Test: [KissBenefitsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/kissprinciple/KissBenefitsTest.java)
- Source: [KissBenefits.java](src/main/java/com/github/msorkhpar/claudejavatutor/kissprinciple/KissBenefits.java)
