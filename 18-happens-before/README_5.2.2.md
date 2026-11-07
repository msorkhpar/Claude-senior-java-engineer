# 5.2.2. Synchronization Actions and Their Impact on Memory Visibility

Two pull requests wait for review. In the first, a parser thread fills a plain `HashMap` and puts it
on a `BlockingQueue`, and a writer thread takes it and writes it to the database. Nothing in either
class is `volatile` or `synchronized`. In the second, an integration test starts a cache warmer,
calls `Thread.sleep(200)`, and then reads the cache, a plain `HashMap` field the warmer fills. That
test has passed every night for a year.

Only the first is correct. A read is guaranteed to see another thread's write only if the write
happens-before the read (unit 5.2.1); the queue creates that order and the sleep doesn't. Four
rules, one per heading below, say which calls create it. Every output is from Java 21 (Temurin
21.0.12, HotSpot, amd64).

## A java.util.concurrent Hand-off Is a Happens-before Edge, and It Carries Plain Data

The JLS defines a few edges itself: program order, monitor unlock to lock, a volatile write to a
read of it, `Thread.start` and `Thread.join` (unit 5.2.1). The `java.util.concurrent` classes add
their own, and most classes state them in a javadoc paragraph headed "Memory consistency effects".
`BlockingQueue`, in the JDK 21 source:

> Memory consistency effects: As with other concurrent collections, actions in a thread prior to
> placing an object into a `BlockingQueue` *happen-before* actions subsequent to the access or
> removal of that element from the `BlockingQueue` in another thread.

"Actions prior to placing" means every write the producer made before the `put`, to any object, not
only the element. This program passes data through five such hand-offs:

```java
class Totals { String region; long orders; long cents; }   // plain fields only
```

```java
ExecutorService pool = Executors.newFixedThreadPool(2);

// submit -> the task: written before submit, read by the task
Totals request = new Totals();
request.region = "eu-west";
Future<String> seenByTask = pool.submit(() -> request.region);
// the task -> Future.get(): written by the task, read after get()
Future<Totals> computed = pool.submit(() -> {
    Totals t = new Totals();
    t.region = "us-east"; t.orders = 1_204; t.cents = 9_877_300;
    return t;
});
Totals t = computed.get();
System.out.println("submit -> task:       task read region=" + seenByTask.get());
System.out.println("task -> Future.get(): " + t.region + " " + t.orders + " orders");

// put -> take: a plain HashMap built by the producer
BlockingQueue<Map<String, Long>> queue = new ArrayBlockingQueue<>(1);
pool.execute(() -> {
    Map<String, Long> byRegion = new HashMap<>();
    byRegion.put("ap-south", 77L);
    try { queue.put(byRegion); } catch (InterruptedException e) { }
});
System.out.println("put -> take:          " + queue.take());
```

The latch and the map hand-offs have the same shape (the whole program is `Handoffs.java` in the
unit's examples):

```text
submit -> task:       task read region=eu-west
task -> Future.get(): us-east 1204 orders
countDown -> await:   parts 500 + 1000
put -> take:          {ap-south=77}
map put -> get:       sa-east 12 orders
```

A correct run proves nothing on its own, since a racy program usually prints the right values too.
These are guaranteed because the javadoc promises each edge, on every JVM and CPU; the Reference
quotes the sentence for each class.

The library keeps the promise with the JLS's own edges: an `ArrayBlockingQueue`'s `put` and `take`
lock the same `ReentrantLock`, and a latch's count is a volatile field that `countDown` changes by
compare-and-set. The javadoc sentence is the contract, and the implementation behind it can change
between releases.

So the objects you pass through a concurrent structure need no `volatile` fields and no locks of
their own, as long as one thread at a time uses each of them. A `volatile` added "for safety" to
every field of a message class blocks some compiler optimizations, and suggests the class is shared
when it isn't.

## The Edge Covers Writes Made Before the Release, Not After It

The edge runs from the `put` to the `take`. A write the producer makes after its `put` has no edge
to the consumer, which may see it whole, in part, or not at all.

An audit recorder collects events into lists of 100 and hands each full list to a sender thread.
To save allocations, it clears the list and reuses it:

```java
List<Integer> batch = new ArrayList<>();
for (int e = 0; e < events; e++) {
    batch.add(e);
    if (batch.size() == batchSize) {
        outbox.put(batch);
        if (reuse) batch.clear();            // reuse the list for the next batch
        else batch = new ArrayList<>();      // start a new list
    }
}
```

The sender takes each batch, counts its events, and catches anything the iteration throws. Three
runs of each, with a million events:

```text
new    recorded 1,000,000, shipped 1,000,000, distinct 1,000,000, batches that threw 0
reuse  recorded 1,000,000, shipped 325,343, distinct 195,779, batches that threw 6,301
       {ConcurrentModificationException=6252, NoSuchElementException=49}
new    recorded 1,000,000, shipped 1,000,000, distinct 1,000,000, batches that threw 0
reuse  recorded 1,000,000, shipped 194,595, distinct 111,828, batches that threw 7,638
       {ConcurrentModificationException=7598, NoSuchElementException=40}
new    recorded 1,000,000, shipped 1,000,000, distinct 1,000,000, batches that threw 0
reuse  recorded 1,000,000, shipped 90,098, distinct 52,828, batches that threw 8,558
       {ConcurrentModificationException=8553, NoSuchElementException=5}
```

With reuse, only 5 % to 20 % of the distinct events reached the sender, many of them more than
once, and most of the 10,000 batches threw while being iterated. After each `put`, the recorder and
the sender shared one `ArrayList` with no edge between them, and an `ArrayList` isn't built for two
threads.

The same mistake has other shapes:

- `latch.countDown()` before the result is stored, so a waiter can wake and read the old value;
- fields of a task's input set after `executor.submit(task)`, so the task can start before them;
- a mutable value put in a `ConcurrentHashMap` and then changed in place, so a reader that got it
  can see the change, part of it, or nothing.

Hand over the list and start a new one, or put a copy.

<!-- practice 1 -->

## Only an Acquire That Succeeds, on the Same Object, Gets the Edge

On the consumer's side, the edge reaches the call that actually received the hand-off: the
`take` that removed the element, the `get` that returned the result, the `await` that returned
because the count reached zero. A call that gave up got nothing.

A service waits up to 100 ms for its rate limits to load and then reads them. `loaded` is a
`CountDownLatch(1)` that the loader counts down after it writes the plain field `limits`. The
loader is slow today; here it is held on a second latch, so the timeout always expires first:

```java
boolean ok = loaded.await(100, TimeUnit.MILLISECONDS);   // returns false: count is still 1
System.out.println("await returned " + ok + ", limits = " + limits);
```

```text
await returned false, limits = null
after the load: await returned true, limits = {checkout=200}
```

`await` returned `false` on the timeout, and the code read `limits` anyway. It got `null` because
the loader hadn't written yet; had the loader been halfway through, no rule says which of its writes
the reader would see. The `CountDownLatch` javadoc grants the edge to
"actions following a successful return from a corresponding `await()`", and a return of `false` is
not one. The same goes for a `Future.get(timeout, unit)` that throws `TimeoutException`, a `poll()`
that returns `null`, and a `ConcurrentHashMap.get` that returns `null`. In the map's words, an
update has a happens-before relation "with any (non-null) retrieval for that key reporting the
updated value".

An unlock publishes to later locks of that monitor, not of any monitor. A reader that locks
something the writer never locks has no edge, and when that object never leaves the thread, the
JIT may remove the lock entirely. This reader spins on a plain `stop` flag that `main` sets after
500 ms. Its loop body, `body`, enters a block on a fresh object (`turns` only counts the turns):

```java
static boolean stop;                              // plain field, written by main
// ...
case "lock-new-object" -> () -> { synchronized (new Object()) { } };
// ...
while (!stop) { body.run(); turns[0]++; }
```

```text
lock-new-object   still spinning 1 s after stop = true
```

In all three runs the loop was still spinning a second later. One run each with
`-XX:-EliminateLocks`, `-XX:-DoEscapeAnalysis` and `-Xint` ended it. Turning off lock elimination
or escape analysis is enough, so the likely story is that escape analysis found the object
unreachable from other threads and the lock was removed. That leaves the empty spin of unit 6.2.2,
where the JIT hoists the read of `stop` out of the loop. A reader that locks a shared object the
writer never takes ended on this JVM, but the JLS gives it no edge either. Writer and reader must
use the same lock (unit 5.1.3).

<!-- practice 2 -->

## Sleep, Yield and println Create No Edge, Even When the Loop Ends

A worker spins on a plain `stop` flag and never stops, because the JIT hoisted the read out of the
loop (unit 6.2.2). Someone adds a log line inside the loop to see what is going on, and the hang is
gone. The log line didn't fix it.

Here is the same spin with seven loop bodies, each in a fresh JVM, three runs each; every body gave
the same answer all three times:

```java
Runnable body = switch (variant) {
    case "empty" -> () -> { };
    case "sleep" -> () -> { try { Thread.sleep(1); } catch (InterruptedException e) { } };
    case "yield" -> Thread::yield;
    case "onSpinWait" -> Thread::onSpinWait;
    case "println" -> () -> log.println("waiting");
    case "lock-new-object" -> () -> { synchronized (new Object()) { } };
    case "lock-reader-only" -> () -> { synchronized (sharedLock) { } };
    default -> throw new IllegalArgumentException(variant);
};
```

`log` is a `PrintStream` (the class of `System.out`) writing to `OutputStream.nullOutputStream()`,
so the run prints nothing.

```text
empty             still spinning 1 s after stop = true
sleep             ended
yield             ended
onSpinWait        ended
println           ended
lock-new-object   still spinning 1 s after stop = true
lock-reader-only  ended
```

Five bodies made the loop end, and none of them creates an edge from the writer. JLS 17.3 says
that `Thread.sleep` and `Thread.yield` have no synchronization semantics: the compiler need not
write register-held values back to memory before them, nor reload them after. A JIT that kept
`stop` in a register across the sleep would be correct, and the loop would hang. That is allowed by
the JLS and was not observed here: HotSpot read the field again on every turn of these loops,
which is a property of this compiler, not a promise.

`println` ended the loop because it takes a lock. A lock acquire keeps the JIT from moving a later
read of `stop` above it, so the loop reads the field after every line. The writer never takes that
lock, so the JLS gives no edge, and another JVM or a change to `PrintStream`'s locking may bring
the hang back. Removing the log line brings it back for certain (unit 5.3.3 covers diagnosing
such bugs).

> **JDK versions differ.** In JDK 21, `PrintStream.println` takes the stream's internal lock, a
> `ReentrantLock`, when the stream's class is exactly `PrintStream`; a subclass, or
> `-Djdk.io.useMonitors=true`, takes `synchronized (this)` instead. A run with that property ended
> the loop too.

A delay fails on its own terms too. Suppose the nightly test from the introduction sleeps 200 ms
because the warmer usually needs 50. On a loaded CI machine the warmer needs 300, and the test
reads an empty cache. Even when the warmer has finished in time, no edge connects its writes to the
test's reads, as with the `sleep` loop above.

Wait on the hand-off the warmer already offers, its `Future` or a latch it counts down after its
last write. The test then waits exactly as long as the warmer needs.

<!-- practice 3 -->

## Summary

- **A `java.util.concurrent` hand-off is a happens-before edge.** Everything written before
  `submit`, `countDown`, `put` or a map `put`, and everything a task writes, is visible after the
  matching task start, `await`, `take`, map `get` or `Future.get`, so the data passed needs no
  `volatile` or lock of its own.
- **The edge covers writes before the release, not after it.** Reusing a list after `put` delivered
  only 5 % to 20 % of a million distinct events; do every write, then release, then let go.
- **Only an acquire that succeeds, on the same object, gets the edge.** An `await` that returned
  `false`, a map `get` that returned `null`, a `Future.get` that timed out, and a lock on another
  object give nothing.
- **Sleep, yield and println create no edge.** Their loops ended on HotSpot because of how it
  compiles them; the JLS allows them to hang, and a sleep doesn't guarantee the other thread has
  finished.

## Reference

**Memory consistency effects, in the words of the JDK 21 javadoc:**

| Class | Edge |
|---|---|
| `Executor` | "Actions in a thread prior to submitting a `Runnable` object to an `Executor` *happen-before* its execution begins, perhaps in another thread." |
| `ExecutorService` | "Actions in a thread prior to the submission of a `Runnable` or `Callable` task to an `ExecutorService` *happen-before* any actions taken by that task, which in turn *happen-before* the result is retrieved via `Future.get()`." |
| `Future` | "Actions taken by the asynchronous computation *happen-before* actions following the corresponding `Future.get()` in another thread." |
| `CountDownLatch` | "Until the count reaches zero, actions in a thread prior to calling `countDown()` *happen-before* actions following a successful return from a corresponding `await()` in another thread." |
| `BlockingQueue` | "actions in a thread prior to placing an object into a `BlockingQueue` *happen-before* actions subsequent to the access or removal of that element from the `BlockingQueue` in another thread." |
| `ConcurrentMap` | "actions in a thread prior to placing an object into a `ConcurrentMap` as a key or value *happen-before* actions subsequent to the access or removal of that object from the `ConcurrentMap` in another thread." |
| `ConcurrentHashMap` | "an update operation for a given key bears a *happens-before* relation with any (non-null) retrieval for that key reporting the updated value." |
| `Semaphore` | "Actions in a thread prior to calling a "release" method such as `release()` *happen-before* actions following a successful "acquire" method such as `acquire()` in another thread." |
| `CyclicBarrier` | "Actions in a thread prior to calling `await()` *happen-before* actions that are part of the barrier action, which in turn *happen-before* actions following a successful return from the corresponding `await()` in other threads." |

The `java.util.concurrent` package summary ("Memory Consistency Properties") lists the same edges
for every concurrent collection, and adds, among others, `Lock.unlock` to `Lock.lock`,
`Exchanger` and `Phaser`.

**No edge from the writer (spin loop on a plain field, Java 21, HotSpot amd64, three runs each):**

| In the reader's loop | The JLS | Observed here |
|---|---|---|
| nothing | no edge | still spinning (ends under `-Xint`, unit 6.2.2) |
| `Thread.sleep(1)`, `Thread.yield()` | no synchronization semantics (JLS 17.3) | ended |
| `Thread.onSpinWait()` | no edge | ended |
| `println` on a `PrintStream` | a lock the writer never takes: no edge | ended |
| `synchronized (new Object())` | a lock nobody else takes: no edge | still spinning; ended with `-XX:-EliminateLocks`, `-XX:-DoEscapeAnalysis` or `-Xint` (one run each) |
| `synchronized` on a shared object the writer doesn't lock | no edge | ended |

## Challenges

1. A batch recorder allocates a new list for every 100 events, and profiling says the allocation
   costs 3 % of CPU. How would you reuse buffers without a race, and what would the pool of
   buffers itself need?
2. Your class hands results from a worker to callers through a `volatile` field and a
   `CountDownLatch`. Write the "Memory consistency effects" paragraph for its javadoc. What can't
   you promise?
3. Find a test in your codebase that waits with `Thread.sleep`. What hand-off could it wait on
   instead, and how long does the test take after the change?

## Design Note: The Library's Edges Are a Contract in Prose

Everything `java.util.concurrent` promises is built from the JLS's few edges and stated in the
javadoc, class by class. The language stays small, and a new class can add an edge without a change
to the JLS.

The cost is that the compiler checks none of it. A call that looks like synchronization (`sleep`,
`println`, a lock nobody else takes) compiles as well as one that is, and on HotSpot it often
behaves the same. Whether a hand-off is safe is decided by a sentence in the javadoc, so the check
is to read that sentence, and a class of your own that hands data between threads owes its callers
the same sentence.

## Code Examples

- Source: [SynchronizationActions.java](src/main/java/com/github/msorkhpar/claudejavatutor/happensbefore/SynchronizationActions.java)
- Test: [SynchronizationActionsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/happensbefore/SynchronizationActionsTest.java)
