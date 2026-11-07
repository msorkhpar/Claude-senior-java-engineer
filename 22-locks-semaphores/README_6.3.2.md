# 6.3.2. Semaphores

The invoice service calls a tax API whose contract allows eight concurrent calls per client; a
client that keeps going over is blocked for an hour. The team put the call behind a
`Semaphore(8)`.

Two incidents followed. In the first, the service stopped answering: 200 request threads were
parked in `Semaphore.acquire()`, `availablePermits()` read 0, and the tax API saw no traffic from
us. In the second, the provider blocked us for going over the limit, while our gauge showed
`availablePermits()` at 13 on a semaphore created with 8.

Both had one cause: a semaphore is a counter that doesn't know who took a permit and believes
every `release()` it is given.

## A Tool That Counts Who Gets In

`Gate` stands in for the tax API: it counts the callers inside `call()`, remembers the peak, and
can time out its first few calls. `Gate.load` starts n callers, waits two seconds, and prints
what happened.

```java
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/** A stand-in for the tax API: counts the callers inside it and remembers the most at once. */
public final class Gate {
    private final AtomicInteger inside = new AtomicInteger();
    private final AtomicInteger peak = new AtomicInteger();
    private final AtomicInteger failuresLeft;
    private final long millis;

    /** Each call takes {@code millis}; the first {@code failures} calls time out instead. */
    public Gate(long millis, int failures) {
        this.millis = millis;
        this.failuresLeft = new AtomicInteger(failures);
    }

    public String call(String invoice) throws IOException, InterruptedException {
        peak.accumulateAndGet(inside.incrementAndGet(), Math::max);
        try {
            Thread.sleep(millis);
            if (failuresLeft.getAndDecrement() > 0) throw new IOException("read timed out");
            return "tax for " + invoice;
        } finally {
            inside.decrementAndGet();
        }
    }

    public int peak() {
        return peak.get();
    }

    /** One request thread's way through the semaphore to the gate. */
    public interface Caller {
        void call(String invoice) throws Exception;
    }

    /** Starts n callers at once, gives them 2 s to finish, and prints what happened. */
    public static void load(String name, int n, Semaphore permits, Gate gate, Caller caller)
            throws InterruptedException {
        List<Thread> callers = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String invoice = "INV-" + (1000 + i);
            callers.add(Thread.ofPlatform().daemon().start(() -> {
                try {
                    caller.call(invoice);
                } catch (Exception e) {
                    // a refused or failed call; the caller's code decides what that costs
                }
            }));
        }
        long deadline = System.nanoTime() + 2_000_000_000L;
        for (Thread t : callers) t.join(Math.max(1, (deadline - System.nanoTime()) / 1_000_000));
        long waiting = callers.stream().filter(Thread::isAlive).count();
        System.out.printf("%-16s peak inside %d, permits afterwards %d, callers still waiting %d%n",
                name, gate.peak(), permits.availablePermits(), waiting);
    }
}
```

Every output in this unit comes from Java 21.

## A Semaphore Counts Permits: It Limits How Many Threads Are Inside, Not Which Ones

`new Semaphore(3)` holds three permits. `acquire()` takes one, and waits while there are none;
`release()` puts one back and wakes a waiting thread. That number is the semaphore's whole state.
Twelve callers through three permits to a 50 ms call:

```java
Semaphore permits = new Semaphore(3);
Gate api = new Gate(50, 0);
Gate.load("acquire/release", 12, permits, api, invoice -> {
    permits.acquire();
    try {
        api.call(invoice);
    } finally {
        permits.release();
    }
});
```

```text
acquire/release  peak inside 3, permits afterwards 3, callers still waiting 0
```

Two things are often read into the count, and neither holds.

**It doesn't make the guarded code exclusive.** With three permits, three threads run the code
between `acquire()` and `release()` at once. Six threads each increment a shared `long` 200,000
times inside the semaphore:

```java
permits.acquireUninterruptibly();
try {
    count++;
} finally {
    permits.release();
}
```

```text
Semaphore(3): count = 992280 (expected 1200000)
Semaphore(1): count = 1200000 (expected 1200000)
```

Only a single permit gives mutual exclusion. With more, the shared state needs its own
protection, which is why the module's `ResourcePool` keeps its free connections in a
`ConcurrentLinkedQueue`.

**It doesn't limit a rate.** Suppose the contract had said "ten requests per second". Ten permits
in front of a 5 ms call, with forty threads calling for one second:

```text
Semaphore(10), 5 ms calls: 1989 calls in one second, peak inside 10
```

Each permit came back when its call ended and was taken again at once. Permits bound how many
calls run at once, not how many start per second. A rate limit needs a clock: permits that come
back on a schedule, not when the call ends.

## A Semaphore Has No Owner and Trusts Every Release: Release Exactly What You Acquired

A `ReentrantLock` knows which thread holds it (unit 6.3.1) and refuses an `unlock()` from any
other thread. A semaphore records only the count:

```java
ReentrantLock lock = new ReentrantLock();
lock.lock();
Thread.ofPlatform().start(() -> {
    try {
        lock.unlock();
    } catch (IllegalMonitorStateException e) {
        System.out.println("lock.unlock() from another thread: " + e.getClass().getSimpleName());
    }
}).join();

Semaphore one = new Semaphore(1);
one.acquire();
Thread.ofPlatform().start(one::release).join();
System.out.println("one.release() from another thread: permits = " + one.availablePermits());

one.acquire();
System.out.println("same thread, second tryAcquire(1, SECONDS): " + one.tryAcquire(1, TimeUnit.SECONDS));
```

```text
lock.unlock() from another thread: IllegalMonitorStateException
one.release() from another thread: permits = 1
same thread, second tryAcquire(1, SECONDS): false
```

So `Semaphore(1)` is not a lock. It isn't reentrant: a thread that holds the permit and asks
again waits for itself, with a plain `acquire()` for ever. And any thread may give a permit back,
whether or not it took one. A mistake doesn't throw; it moves the count, and the count is your
limit.

**A permit that isn't given back is gone.** The tax call without a `finally`, through a
`Semaphore(3)` to `new Gate(50, 3)`, whose first three calls time out:

```java
Gate.load("no finally", 8, permits, api, invoice -> {
    permits.acquire();
    api.call(invoice);
    permits.release();
});
```

```text
no finally       peak inside 3, permits afterwards 0, callers still waiting 5
```

The three timeouts took the three permits with them, and every later caller waits in `acquire()` for
ever. This is the first incident, and only a restart brings the permits back.

**A release that didn't follow an acquire adds a permit.** A refusal path written in a hurry, run
in three waves of twelve callers against the same `Semaphore(3)`:

```java
try {
    if (!permits.tryAcquire()) {
        refused.incrementAndGet();
        throw new RejectedExecutionException("tax API busy");
    }
    api.call(invoice);
} finally {
    permits.release();
}
```

```text
wave 1           peak inside 7, permits afterwards 8, callers still waiting 0
wave 2           peak inside 10, permits afterwards 10, callers still waiting 0
wave 3           peak inside 11, permits afterwards 11, callers still waiting 0
refused in total: 8
```

Every refused caller gave back a permit it never took, raising the limit by one. This is the
second incident. Refusals happen when the semaphore is full, when the provider is busiest, so
the limit grew under the most load until the provider blocked us.

The module's `SemaphoreUsage.ResourcePool` (copied into a class `Module`; the second argument is
fairness) drifts the same way: `release(resource)` requeues the connection and adds a permit
without checking. A retry path gives one connection back twice:

```java
Module.ResourcePool<String> pool = new Module.ResourcePool<>(new ArrayDeque<>(List.of("conn-1", "conn-2")), false);
String c = pool.acquire();
pool.release(c);
pool.release(c);                               // a retry path gives it back a second time
System.out.println("availableCount() = " + pool.availableCount());
System.out.println("three acquires: " + pool.acquire() + ", " + pool.acquire() + ", " + pool.acquire());
```

```text
availableCount() = 3
three acquires: conn-2, conn-1, conn-1
```

A pool of two now has three permits, and two callers hold `conn-1` at once. A pool that must
survive its callers' mistakes records what it handed out and refuses anything that isn't out.

So release in a `finally`, and only on the path where the acquire succeeded. The acquire goes
before the `try`:

```java
permits.acquire();                             // if this throws, there is nothing to give back
try {
    return api.call(invoice);
} finally {
    permits.release();
}
```

With `tryAcquire()`, the `try` starts after the refusal:

```java
if (!permits.tryAcquire()) throw new RejectedExecutionException("tax API busy");
try {
    return api.call(invoice);
} finally {
    permits.release();
}
```

The same timeouts and waves through these two versions:

```text
fixed, 3 timeouts peak inside 3, permits afterwards 3, callers still waiting 0
refuse, wave 1   peak inside 3, permits afterwards 3, callers still waiting 0
refuse, wave 2   peak inside 3, permits afterwards 3, callers still waiting 0
refuse, wave 3   peak inside 3, permits afterwards 3, callers still waiting 0
```

### Spot the Problem: A finally That Gives Back a Permit the Thread Never Got

This version passes review: the release is in a `finally`, and nothing leaks when the call
throws.

```java
static String quote(Semaphore permits, Gate api, String invoice) throws Exception {
    try {
        permits.acquire();                         // (1)
        return api.call(invoice);
    } finally {
        permits.release();                         // (2)
    }
}
```

What happens when a request is cancelled while its thread still waits at (1)?

If cancelling interrupts the thread, as `Future.cancel(true)` does, `acquire()` throws
`InterruptedException` without taking a permit, and the `finally` at (2) gives one back anyway.
Two callers hold the permits of a `Semaphore(2)`, five more queue behind them, and the five
waiters are interrupted:

```text
Semaphore(2), 5 waiters interrupted: permits afterwards = 7
```

Each cancelled waiter adds a permit. Waiters are cancelled when the downstream is slow, so this bug
too raises the limit when the provider is struggling. With `acquire()` above the `try`, the count
holds:

```text
fixed, 5 waiters interrupted: permits afterwards = 2
```

<!-- practice 1 -->

## When the Permits Run Out, Choose: Wait, Wait a Bounded Time, or Refuse

One afternoon the tax API slows from 50 ms to two seconds a call. Twenty requests arrive together,
and three permits are free. What happens to the other seventeen, one second in, depends on which
call takes the permit:

```java
new P("acquire()", s -> { s.acquire(); return true; }),
new P("tryAcquire(200 ms)", s -> s.tryAcquire(200, TimeUnit.MILLISECONDS)),
new P("tryAcquire()", Semaphore::tryAcquire)
```

```text
acquire()           after 1 s:  0 refused (last at    0 ms), 20 threads still tied up
tryAcquire(200 ms)  after 1 s: 17 refused (last at  203 ms),  3 threads still tied up
tryAcquire()        after 1 s: 17 refused (last at    1 ms),  3 threads still tied up
```

`acquire()` waits as long as it takes, and every waiting thread comes from the server's request
pool. With a pool of 200 and invoices arriving steadily, the pool fills within seconds, and
requests that never touch tax, the health check among them, have no thread to run on.
`tryAcquire(200, MILLISECONDS)` waits a bounded time and then returns `false`; `tryAcquire()`
decides at once. Both freed the refused threads to answer "try again later".

Choose by the caller. A user waiting for a page can afford a short wait; a batch job that will
retry anyway should be refused at once. An unbounded `acquire()` fits only where waiting is the
point, as with the signals in the final section, or the release is sure to come soon.

Two shortcuts look like `tryAcquire()` and aren't.

**Checking `availablePermits()` first.** `if (permits.availablePermits() > 0) permits.acquire();`
reads the count, and by the time `acquire()` runs another thread may have taken the permit.
Twenty threads make 100 calls each through `Semaphore(2)`, skipping the call when the check sees
no permit:

```text
2,000 calls, Semaphore(2): saw a free permit 245, of those then waited in acquire() over 1 ms 7, skipped 1755
```

Seven calls saw a free permit and then waited anyway. `availablePermits()` is a snapshot for a
gauge, never a decision; `tryAcquire()` checks and takes in one atomic step.

**Expecting `tryAcquire()` to respect a fair semaphore.** `new Semaphore(n, true)` hands permits
to waiting threads in arrival order. `tryAcquire()` ignores that order, as its javadoc says: it
takes a permit that is free at that moment, even with threads queued. A fair semaphore with one
thread queued gets a release, and the main thread tries at once:

```text
fair semaphore, one thread queued, permit released:
  tryAcquire() took it first in 762 of 1000 runs
  tryAcquire(0, SECONDS) took it first in 0 of 1000 runs
```

On a fair semaphore, `tryAcquire(0, TimeUnit.SECONDS)` refuses when another thread is queued
ahead of it, so it keeps the order.

<!-- practice 2 -->

<!-- practice 5 -->

## acquire(n) Takes All n Permits at Once or None

The video service's GPU runs at most four encoder sessions, guarded by `Semaphore(4)`. A 4K
transcode needs three. Taking them one at a time looks the same until two 4K jobs start together.
A `CyclicBarrier` of two forces the unlucky interleaving: each job has two sessions before either
asks for its third.

```java
sessions.acquire();
sessions.acquire();
bothHaveTwo.await();                    // both jobs got this far
sessions.acquire();
try { Thread.sleep(50); } finally { sessions.release(3); }
```

```text
acquire() x3: after 1 s, job A alive true, job B alive true, free sessions 0
acquire(3):  both jobs done (2) in 101 ms, free sessions 4
```

Each job holds two sessions and waits for one the other holds, so neither finishes. Without the
barrier this happens only when the timing goes wrong, so it passes tests and shows up under load.
`acquire(3)` waits until three permits are free at once and takes them together.

All or none holds for interrupts too. A job waiting in `acquire(3)` while two sessions are free
holds none of them, and leaves holding none:

```text
while acquire(3) waits: free sessions 2
acquire(3) interrupted: InterruptedException
after the interrupt: free sessions 2
```

`tryAcquire(3)` is the same without the wait: three, or none and `false`. Give them back with
`release(3)`.

### A Waiting acquire(n) Can Be Overtaken

All or none has a price on a non-fair semaphore, which `new Semaphore(4)` is. All four sessions
are busy and the 4K job waits for three. Two sessions finish, and a 1-session job arrives:

```java
sessions.acquire(4);                        // all four sessions busy
Thread big = Thread.ofPlatform().daemon().start(() -> {
    try { sessions.acquire(3); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
});
waitUntil(() -> sessions.getQueueLength() == 1);
sessions.release(2);                        // two finish; the 4K job still needs a third
Thread small = Thread.ofPlatform().daemon().start(() -> {
    try { sessions.acquire(1); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
});
small.join(500);
```

```text
non-fair: a 1-session job arriving now got a session: true, free sessions 1
fair:     a 1-session job arriving now got a session: false, free sessions 2
```

On the non-fair semaphore the small job took one of the two sessions the 4K job was waiting for.
With a steady stream of small jobs the free count may seldom reach three, and the big job can wait a
very long time. A fair semaphore queues the small job behind the big one, at the cost of two
sessions idle until a third frees up. When big requests must not starve, that trade is usually worth
it.

<!-- practice 3 -->

## A Semaphore Keeps a Release Nobody Waited For, Which Makes It a Signal

The ownerless count also makes a semaphore a signal between threads. On startup the service must not
take traffic until three caches have loaded, each on its own thread. Each loader releases a permit
of a `Semaphore(0)` when done, and the starter waits for three. One loader finishes before the
starter waits at all:

```java
Semaphore loaded = new Semaphore(0);
loaded.release();                               // the currency cache was quick
System.out.println("released before anyone waits: availablePermits() = " + loaded.availablePermits());
Thread.ofPlatform().start(loaded::release);     // the tax-rate cache
Thread.ofPlatform().start(loaded::release);     // the customer cache
boolean all = loaded.tryAcquire(3, 5, TimeUnit.SECONDS);
System.out.println("acquire 3: " + all);
```

```text
released before anyone waits: availablePermits() = 1
acquire 3: true
```

The early release raised the count to 1, and the starter's `tryAcquire(3, ...)` counted it. The
`Semaphore` javadoc also states that actions before a `release()` happen-before actions after a
successful acquire in another thread (unit 5.2.1), so the starter sees what the loaders wrote.

**A negative start needs one more release than it seems to.** `new Semaphore(-2)` is legal, and is
sometimes used so that a plain `acquire()` waits for several releases. But an acquire needs a
count of at least 1:

```text
new Semaphore(-2): releases before tryAcquire() succeeded = 3
```

To wait for N releases with `acquire()`, start at `1 - N`, or start at 0 and call `acquire(N)`.

**Leftover permits carry over.** The same semaphore is reused for the hourly reload. In round 1 a
fourth, optional cache also signals, so four releases meet an `acquire(3)`:

```text
round 1 done, permits left over: 1
round 2, two caches loaded, tryAcquire(3, 200 ms): true
```

Round 2 started with round 1's leftover permit, so it let traffic in with two of its three caches
loaded. Give each round its own semaphore, or use a `CountDownLatch`, whose count can't be reset.

<!-- practice 4 -->

## Summary

- **A semaphore counts permits, nothing more.** With N above 1, N threads run the guarded code
  together, so shared state needs its own protection; permits bound concurrency, not a rate.
- **A semaphore has no owner and trusts every release.** A missed release shrinks the limit until
  the service stalls; a release on a path that never acquired (a refusal, or an interrupted
  `acquire()` inside the `try`) raises it. Acquire before the `try`, release in its `finally`.
- **When permits run out, the acquire call decides.** `acquire()` ties up a thread while the
  dependency is slow; `tryAcquire(timeout)` and `tryAcquire()` give it back. Checking
  `availablePermits()` first is a race, and `tryAcquire()` jumps a fair semaphore's queue.
- **`acquire(n)` takes all n permits or none.** n single acquires can deadlock two jobs; on a
  non-fair semaphore a waiting `acquire(n)` can be overtaken by smaller requests.
- **A release with no one waiting is kept.** That makes `Semaphore(0)` a signal that can't be
  missed, and leftover permits carry over to the next use.

## Reference

**Methods of `java.util.concurrent.Semaphore` (Java 21):**

| Call | What it does |
|---|---|
| `acquire()`, `acquire(n)` | waits until 1 or n permits are free and takes them together; throws `InterruptedException` if interrupted while waiting, or on entry if the flag is already set, even with permits free; holds nothing after the throw |
| `acquireUninterruptibly()`, `acquireUninterruptibly(n)` | waits through interrupts; the interrupt flag is still set when it returns |
| `tryAcquire()`, `tryAcquire(n)` | takes the permits now or returns `false`; takes a free permit even if threads are queued on a fair semaphore; doesn't look at the interrupt flag |
| `tryAcquire(timeout, unit)`, `tryAcquire(n, timeout, unit)` | waits at most the timeout, then returns `false`; honours fairness; throws `InterruptedException` like `acquire()` |
| `release()`, `release(n)` | adds 1 or n permits; no check of who calls it |
| `availablePermits()` | the current count, a snapshot |
| `hasQueuedThreads()`, `getQueueLength()` | whether threads are waiting, and an estimate of how many |

**Edge cases (Java 21):**

| Situation | What happens |
|---|---|
| a thread parked in `acquire()` | `WAITING` (`TIMED_WAITING` in a timed `tryAcquire`), never `BLOCKED`; a thread dump shows no owner |
| a virtual thread in `acquire()` | parks like any other waiter; 10,000 of them waited on `Semaphore(0)` and all ran after `release(10_000)` |

## Challenges

These have no single answer. `Gate` helps with the last one.

1. The provider allows eight concurrent calls per client, and the service runs as three
   instances. How many permits does each instance get, and what happens during a rolling deploy
   when four instances run for a minute?
2. Build a "ten requests per second" limiter. Compare a semaphore that a scheduler refills every
   second with one whose permits come back a second after each call starts. Which allows a burst
   at the edge of a second, and how large?
3. With virtual threads (unit 6.7.1) a waiting request costs little, and 10,000 of them can wait
   in `acquire()`. Does the argument for `tryAcquire` in this unit still hold? What does the user
   at the other end of a waiting request see?

## Design Note: Why a Semaphore Checks No Owner

A lock answers "who is inside?", and a semaphore answers "how many more may come in?". Java's
`Semaphore` keeps only the number on purpose: the thread that frees a permit need not be the one
that took it. That lets a loader signal a starter, or a callback on another
thread give back a request's permit.

The cost is that misuse shows up weeks later, as a hung service or a blocked client, with nothing
in the logs pointing to the line. So in application code, put a semaphore inside a class that
knows what it handed out, such as a gateway with one `call` method that acquires and releases in
one place, and don't pass the raw semaphore around.

## Code Examples

- Source: [SemaphoreUsage.java](src/main/java/com/github/msorkhpar/claudejavatutor/lockssemaphores/SemaphoreUsage.java)
- Test: [SemaphoreUsageTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/lockssemaphores/SemaphoreUsageTest.java)
