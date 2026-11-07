# 6.2.2. Volatile Keyword

The scheduler's `stop()` did nothing until someone added `volatile` to its `running` flag. The fix
worked, so the same pull request made three more fields volatile: the request counter, and the
host and the port of the upstream database. A week later the dashboard counted 3.1 million
requests for an hour in which the load balancer counted 3.4 million, and the database team asked
why a few connections a day arrived at the new host on the old port.

`volatile` did what it promises in all four fields: a read of a volatile field sees the latest write
to that field, together with whatever the writer wrote before it. It doesn't merge two steps or two
fields. Units 5.2.2 and 5.3.3 give the memory-model rules behind this. In the code you write, that
comes to five rules, one per section heading below.

Every output below comes from Java 21 (HotSpot, amd64). Several examples hold a thread on a
`CountDownLatch` where the scheduler could pause it, so an interleaving that happens once in
thousands of runs under load happens on every run.

## Without volatile, a Reader May Never See the Write: the JIT May Read a Plain Field Once

A worker counts batches until another thread sets `stop`. The field is plain:

```java
class PlainWorker {
    boolean stop;                                  // plain field
    long batches;
    void run() { while (!stop) { batches++; } }
}
```

`main` starts the worker, waits 500 ms, sets `stop = true`, and gives the worker a second to end. A
second worker is the same class with `volatile boolean stop`. Does the first one end?

```text
plain field:    stop set, still running 1 s later: true
volatile field: stop set, still running 1 s later: false (ended after 0 ms)
```

Three runs of three printed the same. With `-Xint`, which turns the JIT compiler off:

```text
plain field:    stop set, still running 1 s later: false
volatile field: stop set, still running 1 s later: false (ended after 0 ms)
```

No cache is hiding the write: the hardware keeps caches coherent, and the interpreted loop sees it.
The JIT compiled the loop, found nothing in it that writes `stop` or synchronizes with another
thread, and read the field once, before the loop, as if it were written `if (!stop) while (true)
batches++;`. It doesn't always do that (a call it can't inline in the loop usually keeps it
reading), but the memory model allows it: a read is guaranteed to see another thread's write only if
the write happens-before the read (unit 5.2.1), and nothing orders a plain write in one thread
before a plain read in another.

`volatile` takes that freedom away. The `java.util.concurrent` package documentation states the
rule: "A write to a volatile field happens-before every subsequent read of that same field." The JIT
must read the field on every iteration.

Whether it hangs depends on when the JIT compiles the loop. Stopped 1 ms after it started instead of
500, the plain worker ended in 3 runs of 10; in the other seven, the JIT had most likely compiled
the loop already. A loop that turns more slowly is compiled later, so a test that sets the flag
early can pass while the warmed-up service hangs. In production that hangs the shutdown: the deploy
sets the flag, waits out its timeout, and kills the process.

Any happens-before edge fixes it: an `AtomicBoolean`, or a getter and a setter both `synchronized`
on the same object (unit 6.2.1). A task on a pool should also check its interrupt flag, because
`shutdownNow()` interrupts the thread and never touches a field of yours (unit 6.1.3).

## A Volatile Write Publishes What Was Written Before It, Not What Comes After

The writer's earlier writes happen-before its volatile write, because they come first in its program
order, and the volatile write happens-before the read that sees it. So a reader that sees the new
value also sees everything the writer wrote before it. That is how one volatile reference publishes
a whole object: build it, then assign it.

A nightly job publishes its report through `static volatile Report latest`, where `Report` has two
plain fields, `rows` and `totalCents`. A dashboard thread reads `latest` whenever someone opens the
page. Two versions of the job (the latches make the timing repeatable: `published` lets the
dashboard read once `latest` is assigned, and `loadRows` holds until the dashboard has looked):

```java
// publish, then fill
Report r = new Report();
latest = r;                                    // volatile write
published.countDown();
r.rows = loadRows(readerLooked);               // the slow query
r.totalCents = 9_877_150L;

// fill, then publish
Report r2 = new Report();
r2.rows = 1_204;
r2.totalCents = 9_877_150L;
latest = r2;                                   // volatile write, after the fields
published2.countDown();
```

In both, the dashboard reads `latest` after it is assigned and while the query would run:

```text
publish, then fill: the dashboard reads rows=0, totalCents=0
fill, then publish: the dashboard reads rows=1204, totalCents=9877150
```

The first job published an empty report, and every page view during the query showed zeros. Its
later writes may never reach another thread at all: no volatile write follows them, so nothing
publishes them, and a reader may see them, not see them, or see one and not the other.

The same rule makes double-checked locking work (unit 5.3.3): the constructor's writes come before
the volatile write of the instance field, so a thread that sees the reference sees a finished
object. If `latest` were a plain field, even the second job would promise nothing: nothing would
order its field writes before another thread's read of the reference (unit 5.1.4 shows what may then
be reordered).

<!-- practice 4 -->

## volatile Makes Each Read and Each Write Atomic, Never a Read Followed by a Write

Back to the request counter. Three counters, each hit by four threads 1,000,000 times: a
`volatile int` with `count++`, an `AtomicInteger` with `incrementAndGet()`, and a plain `int` whose
`count++` sits in a `synchronized` method.

```text
volatile int, count++:          2,528,129
AtomicInteger.incrementAndGet: 4,000,000
synchronized count++:          4,000,000
```

Two more runs gave 2,611,352 and 2,854,038 for the volatile counter. `count++` is three actions:
read the field, add one, write the field. `volatile` makes the read see the latest write and the
write visible to the next read, and nothing more. Two threads that both read 41,207 both write
41,208, and one request is lost.

Atomicity of single accesses matters on its own for `long` and `double`. The JLS (§17.7) allows a
plain 64-bit write to be done as two 32-bit writes, and `VarHandle`'s javadoc promises plain reads
and writes to be "bitwise atomic only for references and for primitive values of at most 32 bits". A
reader that falls between the two halves gets a value nobody wrote. A `volatile long` is read and
written whole. On 64-bit HotSpot, 100,000,000 reads of a plain `long` that another thread kept
writing found no torn value; the risk is on 32-bit JVMs and in what the specification allows.

### Spot the Problem: a Top-Up That Undoes a Call

A tenant's API quota stands at 500 calls. Billing tops it up by 1,000 while a request thread takes
one call. The top-up runs in its own thread: it counts down `computing` once it has read the quota,
then waits in `await(otherDone)` until the request thread has finished:

```java
static volatile long quotaVolatile = 500;

Thread topUp = new Thread(() -> {
    long read = quotaVolatile;
    computing.countDown();
    await(otherDone);                          // the top-up is computing...
    quotaVolatile = read + 1000;
});
```

Meanwhile the request thread runs `quotaVolatile = quotaVolatile - 1;` and counts down `otherDone`.
What is the quota afterwards, and what should it be?

```text
volatile read, then write:  quota = 1500 (expected 1499)
```

The top-up wrote a value computed from 500, so the call taken in between came back. Each access was
atomic and visible; the gap between the top-up's read and its write was not.

The fix makes the read, the computation and the write one step. An atomic class does it with
compare-and-set: it writes the new value only if the field still holds the value the computation
started from, and otherwise runs the computation again on the new value. The same hold with
`AtomicLong.updateAndGet` (the rule counts its runs and holds on its first one, for the demo), while
the request thread calls `quotaAtomic.decrementAndGet()`:

```java
LongUnaryOperator plus1000 = q -> {
    if (ruleRuns.incrementAndGet() == 1) { computing2.countDown(); await(otherDone2); }
    return q + 1000;
};
Thread topUp2 = new Thread(() -> quotaAtomic.updateAndGet(plus1000));
```

```text
AtomicLong.updateAndGet:    quota = 1499, the rule ran 2 times
```

The rule's first result was computed from 500, the compare-and-set found 499, and the rule ran again
on 499. That is why `updateAndGet`'s javadoc asks for "a side-effect-free function, since it may be
re-applied": a rule that sends a message sends it twice under contention. When the computation has
side effects, take a lock instead: a `synchronized` method makes the second thread wait for the
first, so nothing is computed twice.

Two near-fixes come up in review, and both leave the gap open:

- **Check again before writing.** `if (quotaVolatile == read) quotaVolatile = computed;` is still
  a read followed by a write: another thread can write between the check and the assignment. When
  the check fails, the update is dropped, which loses it as surely as overwriting would. Only
  `compareAndSet` makes the check and the write one action.
- **Turn the rule into a delta.** `addAndGet(computed - read)` is atomic, but it applies a
  difference computed from an old value to a new one. For an addition that gives the right answer;
  for "double the quota" or "cap it at 10,000" it doesn't, because those rules depend on the value
  they apply to.

A check-then-act on one field has the same gap. A service that keeps the highest latency it has seen
with `if (micros > peak) peak = micros;` can lose its true peak to a lower value written a moment
later; `peak.accumulateAndGet(micros, Math::max)` on an `AtomicLong` makes it one step. For a
counter many threads hit at once, `LongAdder` (unit 5.3.2) loses nothing and contends less than
`AtomicLong`.

<!-- practice 1 -->

## volatile Covers One Field: Values That Must Agree Are Swapped as One Immutable Object

The upstream address from the opening was two volatile fields, and a reload wrote them one after
the other:

```java
final class TwoFields {
    volatile String host = "db-a.example.com";
    volatile int port = 5432;

    void reload(String newHost, int newPort, Runnable between) {
        host = newHost;
        between.run();                             // the reload thread is descheduled here
        port = newPort;
    }

    String target() { return host + ":" + port; }
}
```

A reload to `db-b.example.com:6432` is held after its first write, and a request reads the target:

```text
two volatile fields: a request during the reload connects to db-b.example.com:5432
two volatile fields: a request after the reload connects to  db-b.example.com:6432
```

Without the hold, one thread flipping the address between the two upstreams as fast as it could, and
another reading it 10 million times, found the mix 31,062 times in one run and 20,445 in the next. A
reader can come in between two writes, and `host` and `port` are two writes.

So put the values that must agree into one immutable object, build it completely, and publish it
with one volatile write:

```java
final class OneReference {
    record Upstream(String host, int port) {}

    volatile Upstream upstream = new Upstream("db-a.example.com", 5432);

    void reload(String newHost, int newPort, Runnable between) {
        between.run();                             // descheduled at the same point
        upstream = new Upstream(newHost, newPort);
    }

    String target() {
        Upstream u = upstream;
        return u.host() + ":" + u.port();
    }
}
```

```text
one volatile record:  a request during the reload connects to db-a.example.com:5432
one volatile record:  a request after the reload connects to  db-b.example.com:6432
```

In the stress run the record was mixed 0 times in 10 million reads, in both runs. The record's
fields are `final`, so the object can't change after it is published, and the publication rule above
covers its construction. A lock taken by both the reload and the read would keep the pair together
too, but every request then takes that lock, and if the reload reads its configuration while holding
it, every request waits for the read.

Volatile stops at what the field points to. With `volatile Upstream upstream` holding a mutable
class, `upstream.port = 6432` is a plain write to a plain field: the volatile read of `upstream`
that comes before it orders nothing for other threads. In a `volatile int[] a`, `a[3] = 42` is a
plain write too. `AtomicIntegerArray`, `AtomicLongArray` and `AtomicReferenceArray` give volatile
access to each element.

If several threads replace the object based on its current value, that is a read followed by a write
again: use `AtomicReference.updateAndGet`, or a lock. Unit 5.3.4 builds a configuration store that
way.

<!-- practice 2 -->

## Every Read of a volatile Field Can See a New Value: Read It Once per Decision

A volatile field can change between any two reads of it, so code that reads it twice and expects the
same answer has a race. The primary database is drained by setting `volatile Upstream primary` to
`null`, and requests fall back while it is. Two ways of writing the request:

```java
static String readTwice() {
    if (primary != null) return primary.host();   // two reads of the field
    return "fallback";
}

static String readOnce() {
    Upstream p = primary;                          // one read, then only the local
    if (p != null) return p.host();
    return "fallback";
}
```

Another thread drains and restores the primary in a loop while a request thread calls each version
10 million times:

```text
check the field, then use the field: 172,849 NullPointerExceptions in 10,000,000 requests
copy to a local, check, use:         0 NullPointerExceptions in 10,000,000 requests
```

A second run gave 59,745 and 0. The null check was true when it ran; the second read came a moment
later and found the drain. A local variable belongs to one thread, so once the value is copied into
it, no other thread can change it.

The module's `DoubleCheckedLazy` (in `VolatileKeyword.java`, linked at the end) builds its value
from a `supplier` on first use. Its `getInstance()` reads the volatile `instance` into `local` and
returns `local`, so the value it checked is the value it returns:

```java
T local = instance; // volatile read — may avoid second volatile read below
if (local == null) {
    synchronized (this) {
        local = instance;
        if (local == null) {
            local = supplier.get();
            instance = local; // volatile write
        }
    }
}
return local;
```

A decision that spans several steps needs the same. A batch of writes that must all reach one
replica reads the current replica once, before the first write. Reading the field for every write
sends the first half of the batch to the old replica and the rest to the new one when a failover
lands in the middle.

<!-- practice 3 -->

## Summary

- **Without `volatile`, a reader may never see the write.** The JIT may read a plain field once
  and loop on that value; the stop flag is set and the worker never stops, and whether a test
  shows it depends on when the loop was compiled.
- **A volatile write publishes what was written before it.** Build the object, then assign it.
  Fields filled in after the assignment are not published; readers may see them empty or half-filled.
- **`volatile` makes each access atomic, never a read followed by a write.** `count++`, a top-up
  and a check-then-act lose updates on a volatile field; an atomic class's `updateAndGet` or
  `compareAndSet`, or a lock, makes them one step.
- **`volatile` covers one field.** Values that must agree go into one immutable object behind one
  volatile reference, replaced whole; two volatile fields give a new host with an old port.
- **Every read can see a new value.** Copy the field into a local once per decision; checking the
  field and then reading it again can throw a `NullPointerException` after a null check.

## Reference

**What each tool gives a shared field:**

| | Each read sees the latest write | Earlier writes published with it | Read-modify-write is one step | Several fields change together |
|---|---|---|---|---|
| plain field | not guaranteed | not guaranteed | no | no |
| `volatile` field | yes | yes, the writes made before the volatile write | no | no; put them in one immutable object |
| `AtomicInteger`, `AtomicLong`, `AtomicReference` | yes | yes | yes: `incrementAndGet`, `updateAndGet`, `accumulateAndGet`, `compareAndSet` | only as one immutable object in an `AtomicReference` |
| `synchronized`, the same monitor for every access | yes | yes | yes | yes |

**Facts (Java 21):**

| Situation | What happens |
|---|---|
| `private final volatile int limit = 10;` | does not compile: `illegal combination of modifiers: final and volatile` |
| a plain `long` or `double` shared between threads | `VarHandle`'s javadoc: plain accesses are "bitwise atomic only for references and for primitive values of at most 32 bits", so a 64-bit value may be torn; on 64-bit HotSpot, 100,000,000 reads of a plain `long` that another thread kept writing found 0 torn values |
| a `volatile long` or `volatile double` | each read and each write is atomic |
| `volatile int[] a`; `a[i] = 1` | only the reference is volatile; the element write is plain |
| `AtomicIntegerArray`, `AtomicLongArray`, `AtomicReferenceArray` | "notable in providing volatile access semantics for their array elements" (package javadoc) |
| `updateAndGet`, `accumulateAndGet` under contention | the function "may be re-applied"; keep it free of side effects |
| a `volatile` reference to a mutable object | a write to one of the object's fields is a plain write |
| `VarHandle` access modes `getOpaque`, `getAcquire`/`setRelease` | weaker orderings than volatile, defined in `VarHandle`'s javadoc; only volatile operations are "totally ordered with respect to each other" |

## Challenges

1. A feature-flag client polls every 30 seconds and keeps three flags in three volatile booleans.
   A request reads all three. List the combinations a request can see during a poll, then decide
   whether that matters for these flags and what you would change if it did.
2. A metrics registry keeps a `volatile Map<String, Long>` and replaces it with an updated copy on
   every increment. Measure how many increments per second it sustains with 1, 4 and 16 writer
   threads, against a `ConcurrentHashMap<String, LongAdder>`. Where does copy-on-write stop being
   the right tool?
3. A status page reads `lastHeartbeat`, a `long` of epoch milliseconds written by a heartbeat
   thread. What can go wrong without `volatile`, on which JVMs, and which test, if any, would show
   it?
4. A rate limiter has two volatile fields, `tokens` and `lastRefill`. Rewrite it once with a lock
   and once with an `AtomicReference` to a record. Which reads more clearly, and which would you
   trust under review?

## Design Note: Why Java 5 Made volatile Publish Other Writes

Before Java 5, a volatile access was ordered only against other volatile accesses. A plain write
made before `ready = true` could still be moved after it, so a reader could see half-written data
behind a flag that said it was ready. Double-checked locking was broken even with `volatile` on the
instance field.

JSR 133 rewrote the memory model for Java 5 and gave volatile the meaning this unit relies on. In
the package documentation's words, reads and writes of volatile fields "have similar memory
consistency effects as entering and exiting monitors, but do not entail mutual exclusion locking." A
volatile write publishes everything before it, like releasing a lock; a volatile read that sees it
receives all of it, like taking the lock.

The price is that the JIT and the processor lose freedom around every volatile access. No earlier
read or write can be moved below a volatile write, and no later one above a volatile read, so a
volatile field in a hot loop can cost more than its own load or store. So declare only the fields
that are shared, and build a whole object before one volatile write instead of making each of its
fields volatile. Java 9's `VarHandle` added weaker modes for the rare code that needs less than this
and can prove it.

## Code Examples

- Implementation: [VolatileKeyword.java](src/main/java/com/github/msorkhpar/claudejavatutor/synchronization/VolatileKeyword.java)
- Test: [VolatileKeywordTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/synchronization/VolatileKeywordTest.java)
