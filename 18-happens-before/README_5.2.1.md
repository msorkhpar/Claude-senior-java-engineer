# 5.2.1. Definition of Happens-before Relationships

A request thread in the pricing service logs `waiting for rates` at 09:00:00.150. The warm-up
thread logs `rates loaded` at 09:00:00.204 and sets the plain field `warmedUp = true`. Thirty
seconds later the health check kills the pod, and the thread dump shows the request thread still
in its `while (!warmedUp)` loop. The log puts the write first. Why does the read never see it?

The Java Memory Model answers with one relation, happens-before, and the clock is not part of it.
Four rules follow, one per heading.

## A Read Is Guaranteed to See a Write Only If the Write Happens-Before It

The `java.util.concurrent` package javadoc states the rule from JLS §17.4.5: "The results of a
write by one thread are guaranteed to be visible to a read by another thread only if the write
operation *happens-before* the read operation." When every write to a variable is ordered against
a read by happens-before, the read sees the last of them. When nothing orders a write before the
read, the read may return it, or another value, including the initial zero. A pair of actions that
the model orders across two threads is called an edge.

A reconciliation worker adds matched amounts to a plain field. The main thread wrote the cutoff
before starting it, and reads the subtotal twice: after a `join` that ran out of time, and after
one that saw the worker end.

```java
import java.time.Duration;
import java.util.concurrent.CountDownLatch;

/** Start and join edges: what the main thread may read from a worker, and when. */
public class JoinBudget {
    static long cutoffCents;      // plain: written by main before start()
    static long subtotalCents;    // plain: written by the worker, read by main

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch lastBatch = new CountDownLatch(1);
        cutoffCents = 50_000;                                   // before start(): the worker sees it
        Thread worker = new Thread(() -> {
            long[] amounts = {12_000, 30_000, 45_000, 61_000, 8_000};
            for (int i = 0; i < amounts.length; i++) {
                if (i == 3) {                                   // the last batch arrives late
                    try { lastBatch.await(); } catch (InterruptedException e) { return; }
                }
                if (amounts[i] <= cutoffCents) subtotalCents += amounts[i];
            }
        }, "shard-1");
        worker.start();

        boolean ended = worker.join(Duration.ofMillis(200));
        System.out.println("join(200 ms) returned " + ended + ", alive: " + worker.isAlive()
                + ", subtotal read: " + subtotalCents);

        lastBatch.countDown();
        ended = worker.join(Duration.ofSeconds(5));
        System.out.println("join(5 s)   returned " + ended + ", alive: " + worker.isAlive()
                + ", subtotal read: " + subtotalCents);
    }
}
```

```text
join(200 ms) returned false, alive: true, subtotal read: 87000
join(5 s)   returned true, alive: false, subtotal read: 95000
```

Three runs printed this; only the second line is guaranteed. A thread's actions
happen-before another thread's return from `join` only when the join returns because the thread
has ended. The first `join` returned `false`, so nothing orders the worker's additions before the
first read. That read is a data race: two accesses to one field, one a write, not ordered by happens-before
(unit 5.3.1). It printed 87,000 here; the model also allows 0, 12,000 or 42,000, the earlier
values of the field (and, for a plain `long`, a torn value, §17.7, which 64-bit HotSpot doesn't
produce). The worker's reads of `cutoffCents` are ordered,
because the write came before `start()`.

Inside one thread, each action happens-before every later action in program order. So the main
thread's last read, which follows its successful `join`, is covered by that `join`.

In production the race looks like a correct number. A nightly report that reads a shard's subtotal
after its join timed out prints a total, the total matches no rerun, and nobody can tell which
shard was half-done.

<!-- practice 1 -->

## Four Pairs of Actions Connect Two Threads, and Transitivity Chains Them

Program order stops at the thread. Between two threads, the package javadoc lists these pairs:

- an unlock of a monitor, and every later lock **of that same monitor**;
- a write to a volatile field, and every later read **of that same field**;
- a call to `start()` on a thread, and every action **in the started thread**;
- every action in a thread, and the return of `join()` **on that thread**, once it has ended.

Each pair names one object on both sides. A lock on another monitor, a read of another volatile
field, or a write made after `start()` gives no edge (unit 5.1.3 shows the lock case). Happens-before
is also transitive: if A happens-before B and B happens-before C, then A happens-before C.

A loader thread fills a rates map and then sets a volatile flag. A converter waits for the flag and
uses the map. The main thread joins the converter only:

```java
import java.util.HashMap;
import java.util.Map;

/** Plain writes carried across three threads by a volatile edge and then a join edge. */
public class Chain {
    static Map<String, Long> rates;          // plain, written by the loader
    static volatile boolean ratesReady;      // volatile, written last by the loader
    static long converted;                   // plain, written by the converter

    public static void main(String[] args) throws InterruptedException {
        Thread loader = new Thread(() -> {
            Map<String, Long> m = new HashMap<>();
            m.put("EUR", 108L);
            m.put("GBP", 127L);
            rates = m;                       // (1) plain write
            ratesReady = true;               // (2) volatile write
        });
        Thread converter = new Thread(() -> {
            while (!ratesReady) Thread.onSpinWait();   // (3) volatile read that sees (2)
            converted = 2_500 * rates.get("GBP");      // (4) plain read of (1), plain write
        });
        loader.start();
        converter.start();
        converter.join();                    // (5) join on the converter only
        System.out.println("converted: " + converted + ", rates seen by main: " + rates);
    }
}
```

The main thread never reads `ratesReady` and never joins the loader. Is its read of
`rates` guaranteed to see the map?

```text
converted: 317500, rates seen by main: {EUR=108, GBP=127}
```

It is, and not because the run printed it. The chain: (1) comes before (2) in the loader's
program order, (2) happens-before (3) by the volatile rule, (3) comes before the converter's last
action, and that action happens-before the return of (5) by the join rule. By transitivity, (1)
happens-before every read after (5). The `put` calls come before (1), so the map's contents arrive
too.

Remove `volatile` from `ratesReady` and the link from (2) to (3) is gone, which breaks the chain.
The main thread's read of `rates` becomes a data race, and so does the converter's; the converter
may also spin forever, and the `join` with it.

> **The JLS lists a few more.** §17.4.4 also orders an `interrupt()` before any point where a
> thread finds that the target was interrupted, a thread's last action before another thread finds it ended through
> `isAlive()` returning `false`, and the default zero of every field before every thread's first
> action. The `java.util.concurrent` classes add their own edges (unit 5.2.2).

## Happens-Before Is Not Time: a Write Seconds Earlier Can Stay Invisible

A cut-down version of the opening problem. A request thread loops until a plain `warmedUp` flag
is set; the main thread plays the warm-up thread, sets the flag 200 ms in, and then gives the
request thread two seconds to finish:

```java
static boolean warmedUp;
static long spins;

Thread request = new Thread(() -> {
    while (!warmedUp) spins++;
    System.out.printf("request thread saw the flag at %4d ms%n", ms(t0));
});
request.setDaemon(true);         // a reader that never sees the flag mustn't keep the JVM up
request.start();

Thread.sleep(200);
warmedUp = true;
System.out.printf("warm-up wrote the flag at   %4d ms%n", ms(t0));

request.join(2_000);
System.out.printf("at %4d ms the request thread is %s%n", ms(t0),
        request.isAlive() ? "still waiting" : "done");
```

```text
warm-up wrote the flag at    204 ms
at 2229 ms the request thread is still waiting
```

All five runs printed this. With `static volatile boolean warmedUp`:

```text
warm-up wrote the flag at    204 ms
request thread saw the flag at  204 ms
at  215 ms the request thread is done
```

The reads ran for two seconds after the write, yet no edge orders the write before any of them, so
every one may return `false`, forever. Here the JIT most likely hoisted the read out of the loop
(unit 6.2.2 shows how), but the model allows the same result from any cause. Waiting longer, sleeping in either
thread, or comparing log timestamps creates no edge (unit 5.2.2 covers `Thread.sleep`).

The volatile run is guaranteed for a narrower reason than "it was later". A volatile write
happens-before the reads of that field that come after it in the synchronization order (JLS
§17.4.4: one total order over all locks, unlocks and volatile accesses), and a read that returns
the new value is after it. A read that still returned `false` carries nothing from the writer.

Happens-before never makes a thread wait, either. It says what a read may see, given the order in
which things ran; it doesn't make the writer run first. Waiting is the job of a loop on a volatile
field, a `join`, or a `java.util.concurrent` class.

<!-- practice 3 -->

## An Edge Carries Every Write Made Before It, Only to Code After Its Other End

Transitivity means you don't need `volatile` on every field you hand to another thread. Write the
plain fields first, then make one write that forms an edge; a reader on the other end of that edge
sees all of them. This is called piggybacking, and the JDK relies on it. `FutureTask`, behind
`ThreadPoolExecutor.submit()`, keeps its result in a plain field (JDK 21 source):

```java
private Object outcome; // non-volatile, protected by state reads/writes
```

`set(v)` writes `outcome` and then writes the volatile `state`; `get()` reads `state` first and
`outcome` after it. (`set` uses `VarHandle.setRelease` for that last write, a weaker mode than a
volatile write that still keeps the `outcome` write before it; its guarantee is defined by
`VarHandle`, not by JLS §17.) Unit 6.2.2 shows the writer's side going wrong: a report published
before it was filled.

### Spot the Problem: Reading the Payload Before the Flag

One thread hands a million payloads to another through a plain `payload` field and a volatile
sequence number. The writer waits for the reader's acknowledgement in the volatile `taken`, writes `payload`, then `seq`.
Two readers wait for each handoff:

```java
long p;
if (order.startsWith("payload")) {
    do { p = payload; } while (seq != i);   // reads payload, then checks seq
} else {
    while (seq != i) Thread.onSpinWait();   // checks seq, then reads payload
    p = payload;
}
if (p != i * 100L) stale[0]++;
taken = i;
```

Both check the volatile `seq`, and both stop at the right handoff. Which one can return the
previous payload?

```text
payload, then seq: stale payloads: 52,317 of 1,000,000
seq, then payload: stale payloads: 0 of 1,000,000
```

Two more runs gave 8,324 and 5,756 for the first reader and 0 for the second. The first reader read
`payload`, the writer then wrote both fields, and the reader's check of `seq` saw the new number and
accepted the payload it had read before. Its payload read comes before its volatile read in program
order, so the edge ends after it and carries nothing to it. No reordering is needed to explain the
result: the reader's own code read the value too early.

The writer makes every plain write before the write that forms the edge.
The reader reads the plain fields only after the read, lock or `join` that completes the edge, and
after checking that it saw the value it waited for. In an order-handoff queue, the first reader
ships an order with the previous order's amount, between once in 20 and once in 175 handoffs in
these runs, and quite possibly never in a test that hands over ten.

<!-- practice 2 -->

## Summary

- **A read is guaranteed to see a write only if the write happens-before it.** Without an edge the
  read may return an older value or zero, and a data race prints a plausible number, such as a
  subtotal read after a `join` that timed out.
- **Four pairs of actions connect two threads, and transitivity chains them.** Unlock and lock of one
  monitor, write and read of one volatile field, `start()`, and `join()` on an ended thread; a
  chain carries a write to threads that never took part in its first edge.
- **Happens-before is not time.** A plain write made seconds earlier can stay invisible forever,
  and waiting, sleeping or timestamps create no edge.
- **An edge carries every write made before it, only to code after its other end.** Plain fields
  ride on one volatile write, as `FutureTask`'s result does; a reader that reads them before its
  volatile read gets stale values.

## Reference

**The edges between threads** (package javadoc of `java.util.concurrent`, "Memory Consistency
Properties"):

| From | Happens-before | Condition |
|---|---|---|
| each action in a thread | every later action in that thread's program order | same thread only |
| unlock of a monitor (end of a `synchronized` block or method) | every subsequent lock of that same monitor | same monitor |
| write to a volatile field | every subsequent read of that same field | same field |
| `t.start()` | any action in `t` | covers writes made before the call |
| all actions in `t` | another thread's successful return from `t.join()` | `t` has ended |
| A happens-before B, B happens-before C | A happens-before C | transitivity |

**`join` and the edge** (`Thread` javadoc; ran on Java 21):

| Call | Returns when | Edge from `t`'s actions |
|---|---|---|
| `t.join()` | `t` has ended | yes |
| `t.join(Duration d)` | `t` ended (`true`) or `d` ran out (`false`); `d` ≤ 0 only tests, doesn't wait; throws `IllegalThreadStateException` if `t` wasn't started | only if it returned `true` |
| `t.join(long millis)` | `t` ended or the time ran out; returns nothing; `join(0)` waits forever; returns at once if `t` wasn't started | only if `t` has ended |

**What gives no edge** (each one ran, or follows from the first table):

| Situation | Why |
|---|---|
| a plain write, then a read in another thread seconds later | no pair in the table connects them |
| a write made after `t.start()`, read in `t` | the start edge covers writes before the call |
| a read of a plain field before the volatile read that sees the flag | the edge ends at the volatile read |
| a volatile read of field `b` after a write of field `a` | the volatile rule pairs one field |
| a volatile read that returned the old value | it isn't after the write in the synchronization order |

## Challenges

1. A cache is rebuilt on a scheduler thread and swapped into a `volatile` reference. A request
   reads the reference twice in one method. Trace the edges for each read, and decide whether the
   two reads can disagree and whether that matters.
2. Take a class you own that hands data between threads. For each field, name the edge that
   publishes it. How many fields turn out to have none, and how would a test find them?
3. `FutureTask` gets by with one volatile field and a release write. Write a one-slot mailbox the
   same way, then argue from the edges, not from a test, that a reader can't get a stale payload.

## Design Note: Why the Model Is a Partial Order, Not a Clock

The memory model could have said "a read sees the latest write by time". That rule would force
every plain read and write to reach memory in order, and forbid the JIT from keeping a field in a
register or moving a load out of a loop. Every program would pay for the few fields that threads
share.

Java 5 (JSR-133) chose edges instead. Between synchronization actions, the JIT and the processor
may reorder and cache freely, as long as each thread alone behaves as written; only the actions in the first table constrain them, and only
where they appear. A program in which every conflicting pair of accesses is ordered by these edges
behaves as if its threads took turns (unit 5.1.1).

The price is that correctness can't be checked by running the code: the racy read in the first
section printed a plausible subtotal, and the payload race varied ninefold between runs and may
never show on a different processor or JIT. The argument has to
be made from the edges, which is why a reviewer asks "what publishes this field?" and not "did the
test pass?".

## Code Examples

- Source: [HappensBeforeDefinition.java](src/main/java/com/github/msorkhpar/claudejavatutor/happensbefore/HappensBeforeDefinition.java)
- Test: [HappensBeforeDefinitionTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/happensbefore/HappensBeforeDefinitionTest.java)
