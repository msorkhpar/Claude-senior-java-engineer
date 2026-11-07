# 8.5.3. Choosing Between Fail-Fast and Fail-Safe Iterators

A price-alert service threw `ConcurrentModificationException` in production: one thread was
walking the `ArrayList` of alert listeners while a user's dashboard subscribed. The fix that
shipped made every shared list a `CopyOnWriteArrayList`, the audit log included, which takes one
append per request, 2,000 a second at peak, and passes 100,000 events within the first minute.
Since then CPU and GC time climb the longer the service runs. The end-of-day stock report walks a `ConcurrentHashMap`; it has never thrown, and most
nights its total is off by a few units.

Each of the three needs a different kind of iterator. How fail-fast, snapshot and weakly
consistent iterators behave is unit 6.5.3, and unit 8.5.2 goes further into weakly consistent
walks. Every output is from Java 21 on one machine.

## Once Another Thread Writes, a Fail-Fast Walk Fails at Random; a Small Listener List Needs a Snapshot

Twenty listeners, one thread dispatching ticks to them, and a dashboard on another thread that
subscribes and unsubscribes in a loop. The same code runs with an `ArrayList`, then with a
`CopyOnWriteArrayList`:

```java
for (int i = 0; i < 20; i++) listeners.add(tick -> { });
Consumer<String> dashboard = tick -> { };
AtomicBoolean stop = new AtomicBoolean();
Thread ui = new Thread(() -> {                    // a dashboard opens and closes
    while (!stop.get()) { listeners.add(dashboard); listeners.remove(dashboard); }
});
ui.setDaemon(true);
ui.start();
int failed = 0, partial = 0;
Map<String, Integer> kinds = new TreeMap<>();
for (int d = 0; d < 100_000; d++) {
    int called = 0;
    try {
        for (Consumer<String> l : listeners) { l.accept("tick"); called++; }
    } catch (RuntimeException e) {
        failed++;
        kinds.merge(e.getClass().getSimpleName(), 1, Integer::sum);
        if (called > 0) partial++;                 // some listeners already had the tick
    }
}
```

```text
ArrayList            95,090 of 100,000 dispatches threw {ConcurrentModificationException=95087, NoSuchElementException=1, NullPointerException=2}; 86,890 after some listeners had the tick
CopyOnWriteArrayList      0 of 100,000 dispatches threw {}; 0 after some listeners had the tick
```

This dashboard changes the list without pause; in production it subscribed a few times an hour,
and the exception came about once a week. Most failed dispatches had already reached some
listeners: a retry sends them the tick twice, and giving up never sends it to the rest. Not every
failure was a `ConcurrentModificationException` either: the iterator handed back a `null`, which
the call turned into the `NullPointerException`, and once found no next element after `hasNext()`
had promised one. The counter and the array are plain fields, and nothing makes the dispatching
thread see another thread's writes in order, so the check unit 6.5.3 calls best-effort promises
nothing here. Over 16 runs, between 39 % and 99.7 % of dispatches threw. Fail-fast fits a
collection one thread owns (unit 5.3.4), where its exception reports a bug in the loop.

The copy-on-write list passed every run. A subscribe copies 20 references, and a dispatch walks
the array that was current when it began, without a lock. A `synchronizedList` walked under its
own lock (unit 6.5.2) is also correct, but then every subscribe waits for the whole dispatch in
progress, every remaining listener included.

<!-- practice 1 -->

## Every Copy-on-Write Write Copies the Whole Array, So Its Cost Grows with the List

The audit log got the same list type. This program keeps a list at size n and replaces one
element per write, on one thread, with nothing competing; for comparison it also times a locked
`ArrayList` and a `ConcurrentLinkedQueue` held at the same size:

```java
// i + 1_000 is never a cached Integer, so every set stores a new object and must copy
double[] c = median(i -> cow.set(i % n, i + 1_000), writes);
double[] s = median(i -> sync.set(i % n, i + 1_000), writes);
double[] q = median(i -> { clq.offer(i + 1_000); clq.poll(); }, writes);
```

`median` runs three warm-up rounds, then five timed ones, and keeps the middle one; allocation
comes from `ThreadMXBean.getThreadAllocatedBytes`. There is no JMH here, so read the times as
orders of magnitude on one machine. One of three runs:

```text
      n | COW set     | COW bytes/write | synchronizedList set | CLQ offer+poll
     10 |       19 ns |              72 |                10 ns |          15 ns
    100 |       25 ns |             432 |                11 ns |          19 ns
  1,000 |      160 ns |           4,032 |                11 ns |          16 ns
 10,000 |    2,263 ns |          40,032 |                11 ns |          15 ns
100,000 |   21,044 ns |         400,032 |                13 ns |          16 ns
```

From 1,000 elements on, the copy-on-write time and allocation grow tenfold with each tenfold
size, because every mutative operation makes "a fresh copy of the underlying array" (its
javadoc): here 4 bytes per element, plus the array header and the boxed value. The other two stay flat. Between runs the times moved by up
to a factor of three (2.2 to 7.3 µs at 10,000); the bytes didn't move.

For the audit log at 100,000 events, each append copies 400 KB. At 2,000 requests a second that
is 800 MB of garbage a second and about 4 % of a core spent copying, and both grow with every
event appended. By the same table, a subscribe to the 20-listener list costs about 20 ns and
100 bytes.

> **Measuring it yourself.** `set` skips the copy when the new element is the same object as the
> old one, and `Integer.valueOf` returns cached objects for -128 to 127. Our first version wrote
> `i`, so each round stored the same cached objects for 0 to 127 into the same slots again, and
> the 100,000 row showed 374,430 bytes instead of 400,032. Hence the `i + 1_000`.

## A Weakly Consistent Walk Fits Work on One Element at a Time, Not a Total Across Elements

The log needs an append that doesn't depend on its size, and an export that doesn't hold up
requests. The log starts with 100,000 events; four threads append while an export walks it once:

```java
Queue<Event> clq = new ConcurrentLinkedQueue<>();
run("ConcurrentLinkedQueue", clq, () -> {
    for (Event e : clq) written += e.seq() % 7;          // stands in for writing a line
});
List<Event> sync = Collections.synchronizedList(new ArrayList<>());
run("synchronizedList, walk under lock", sync, () -> {
    synchronized (sync) { for (Event e : sync) written += e.seq() % 7; }
});
```

```text
ConcurrentLinkedQueue              export took   7 ms,    38,798 appends while it ran
synchronizedList, walk under lock  export took   6 ms,       108 appends while it ran
```

The weakly consistent walk took no lock and threw nothing while the requests went on appending.
Under the list's lock they waited for the whole walk; the 108 landed just before it took the lock
or just after it let go. An export that writes each line to a file holds that lock as long as the
file takes. Each event stands on its own, so it doesn't matter whether one appended mid-walk makes
this export.

The stock report is different. Stock moves between warehouses as a debit and a credit, so the
total never changes, and the report adds up every warehouse. Each `merge` on a
`ConcurrentHashMap` is atomic, and the walk takes no lock:

```java
public void move(String from, String to, int qty) {
    map.merge(from, -qty, Integer::sum);
    map.merge(to, qty, Integer::sum);
}
public long total() {
    long t = 0;
    for (int units : map.values()) t += units;
    return t;
}
```

One thread moves stock as fast as it can, and a report runs every 10 ms for a second:

```text
live         100 warehouses:  98 of 100 reports wrong, a report takes  0.03 ms | 14,612,998 moves
live     100,000 warehouses: 100 of 100 reports wrong, a report takes  4.31 ms |  4,466,428 moves
```

Almost every report was wrong, and none threw. The walk reads each warehouse at a different
moment, so a report that reads one side of a move before it and the other side after it counts
the units twice or not at all (unit 6.5.3 showed the same with account balances). Atomic
single-key updates don't help: the report needs every warehouse at one moment, and no walk over a
structure that changes under it gives that.

<!-- practice 2 -->

## A Total That Must Add Up Needs a Snapshot or a Lock, and Size Decides Which

A snapshot gives the report one moment: each move builds a new map and publishes it in one atomic
step, and the report walks whichever map was current when it began.

```java
public void move(String from, String to, int qty) {
    current.updateAndGet(m -> {
        Map<String, Integer> next = new HashMap<>(m);
        next.merge(from, -qty, Integer::sum);
        next.merge(to, qty, Integer::sum);
        return Collections.unmodifiableMap(next);
    });
}
public long total() {
    long t = 0;
    for (int units : current.get().values()) t += units;
    return t;
}
```

So does a lock: moves take the write lock of a `ReentrantReadWriteLock`, and the report walks a
plain `HashMap` under the read lock, so no move lands mid-walk. The same run with both:

```text
snapshot     100 warehouses:   0 of 100 reports wrong, a report takes  0.08 ms |    890,483 moves
RW lock      100 warehouses:   0 of 100 reports wrong, a report takes  0.07 ms | 11,714,501 moves
snapshot 100,000 warehouses:   0 of 100 reports wrong, a report takes  3.08 ms |        366 moves
RW lock  100,000 warehouses:   0 of 100 reports wrong, a report takes  2.03 ms |  3,387,651 moves
```

Every report added up. The snapshot copies the whole map on every move: with 100 warehouses the
mover managed about a million moves in the second, against 14 million on the live map, and with
100,000 warehouses it managed 366. The lock lets moves run between reports and stops them for the
length of each walk: 2 ms per report at 100,000 warehouses, a fifth of the second at a hundred
reports a second. Size and write rate decide between them.

A lock that only `move` takes leaves the report walking a map that changes under it. A mutable map
kept in the `AtomicReference` and changed in place is the live map again: `get()` hands every
reader that one map, and nothing is copied.

<!-- practice 3 -->

## Summary

- **Once another thread writes, a fail-fast walk fails at random**, and not only with
  `ConcurrentModificationException`; a dispatch that fails has often reached some listeners
  already. Fail-fast fits data one thread owns; a small, rarely written list that threads share
  fits a `CopyOnWriteArrayList`.
- **Every copy-on-write write copies the whole array.** Time and garbage per write grow with the
  list: about 20 µs and 400 KB at 100,000 elements here, so a large or write-heavy list doesn't
  fit.
- **A weakly consistent walk fits work on one element at a time.** It never blocks writers, and a
  total it adds up can be one the collection never held.
- **A total that must add up needs a snapshot or a lock.** The snapshot charges each write a copy,
  so it fits small or rarely written data; the lock makes writers wait out each read.

## Reference

**Choosing (each row shows where on this page or in which unit it was run):**

| The data | One read must | Choose | Its iterator | Shown in |
|---|---|---|---|---|
| owned by one thread | see each element | `ArrayList`, `HashMap` | fail-fast; the exception is a bug in the loop | unit 6.5.3 |
| shared, small, read far more than written | see each element | `CopyOnWriteArrayList` | snapshot | the listener run; the write-cost table |
| shared, large or written often | handle each element on its own | `ConcurrentLinkedQueue` (`ConcurrentHashMap` for a map, unit 8.5.2) | weakly consistent | the write-cost table; the log-export run |
| shared, small or written rarely | agree across elements | an unmodifiable map replaced through an `AtomicReference` | fail-fast, over a map nobody changes | the stock run, `snapshot` |
| shared, large and written often | agree across elements | `HashMap` under a `ReentrantReadWriteLock` | fail-fast, walked under the read lock | the stock run, `RW lock` |

**Copy-on-write writes (Java 21):**

| Fact | Source |
|---|---|
| `add`, `set`, `remove` and every other mutative operation copy the array | `CopyOnWriteArrayList` javadoc |
| `set` of the object already at that index doesn't copy | the side note's first run: 374,430 instead of 400,032 bytes per write |
| a copy of 10,000 elements allocated 40,032 bytes: 4 per element, 16 of array header, 16 of the boxed value | the write-cost table |

## Challenges

1. Each websocket session registers a listener, so the list reaches 5,000, with 200 connects and
   disconnects a second. Estimate from the write-cost table what a `CopyOnWriteArrayList` costs
   there. What would you use instead, and what would a dispatch then see?
2. The stock report runs once a night over 2 million items, while moves arrive 5,000 times a
   second. Neither a copy per move nor a lock held for the whole walk is acceptable. What else
   gives the report one moment?
3. Run the write-cost measurement with four writer threads. Which column changes most, and why?

## Design Note: Three Iteration Contracts and What Each Gives Up

The JDK could have given every collection one iterator. It gives three, and each gives up
something different. Fail-fast costs one counter and one comparison per step, and gives up
correctness once a second thread writes. A snapshot iterator gives readers one moment without a
lock, and makes every write copy everything. A weakly consistent iterator makes neither readers
nor writers wait, and gives up the moment: a read that spans elements can mix before and after.

None of the three gives a lock-free reader one moment of a structure that writers change in place
without copying it, which is why the consistent report above cost either a copy or a lock. Unit
8.5.1 uses "fail fast" in a wider sense, checking arguments in constructors and refusing bad
configuration.

## Code Examples

- Test: [IteratorChoiceGuideTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/failfastsafe/IteratorChoiceGuideTest.java)
- Source: [IteratorChoiceGuide.java](src/main/java/com/github/msorkhpar/claudejavatutor/failfastsafe/IteratorChoiceGuide.java)
