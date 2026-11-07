# 5.1.3. Synchronization and Memory Consistency

A trading desk's price book keeps a bid and an ask for each instrument. Every write goes through
one `synchronized` method, so two quotes never interleave. The endpoint that reports the spread
reads both fields without the lock: a read can't break anything. Twice a
day, monitoring alerts on a negative spread: a bid above the ask, which no quote ever had.

A lock keeps other threads that take the same lock out of the block, and it hands everything the
releasing thread wrote to the next thread that takes it. A thread that doesn't take the lock gets
neither.

Every output below comes from Java 21 (HotSpot, x86_64, 20 cores), one JVM per run.

## Releasing a Lock Publishes Every Write Made Before It, to the Next Thread That Takes the Same Lock

A worker spins until another thread stops it. `stop()` sets the flag under a lock; `isStopped()`
reads it without one, because a single boolean read looks harmless:

```java
static class Worker {
    private final Object lock = new Object();
    private boolean stopped;                           // plain field
    long work;

    void stop() {
        synchronized (lock) { stopped = true; }
    }

    boolean isStopped() {
        return stopped;                                // skips the lock
    }

    void run() {
        while (!isStopped()) work++;
    }
}
```

`main` starts the worker, waits 500 ms, calls `stop()` and waits up to a second. The second version
locks in `isStopped()` too:

```java
boolean isStopped() { synchronized (lock) { return stopped; } }
```

```text
skips the lock   still running 1 s after stop(): true
same lock        still running 1 s after stop(): false (ended after 0 ms)
```

In ten runs each, the first worker never stopped and the second always did. With `-Xint`, which
turns the JIT compiler off, the first worker stopped too. The JIT compiled the loop, saw nothing in
it that synchronizes with another thread, and most likely read `stopped` once (unit 6.2.2 shows the
same with no lock at all).

The second version works because of a *happens-before edge* (JLS §17.4.5): an unlock of a monitor
happens-before every later lock of that same monitor. Everything the writer did before its unlock,
in program order, comes before the unlock; everything the reader does after its lock comes after,
inside its block or after it. So the reader that takes the lock after `stop()` released it sees
`stopped = true`, and it also sees every other write the stopping thread made before releasing the
lock, inside the block or earlier, to any field, static or not. Nothing is published to a memory
that every thread reads: a thread that takes no lock is promised none of it. A thread can fill in a
report with plain writes, then set `ready = true` in a `synchronized` block, and a reader that sees
`ready` under the same lock sees the whole report.

In the first version the reader never locks, so no edge connects the two threads, and the memory
model lets the read return the old value forever. A deploy that sets the flag then waits out its
timeout and kills the process mid-write.

`ReentrantLock` and every other `Lock` give the same edge: the `Lock` javadoc requires "the same
memory synchronization semantics as provided by the built-in monitor lock".

## A Lock on a Different Object Gives No Edge

Two more versions of `isStopped()`. Both writer and reader say `synchronized`, but the reader locks
another object (ten runs each gave the same as this one):

```java
boolean isStopped() { synchronized (readLock) { return stopped; } }        // another lock object
boolean isStopped() { synchronized (new Object()) { return stopped; } }    // a new object each call
```

```text
another lock     still running 1 s after stop(): false (ended after 0 ms)
new Object()     still running 1 s after stop(): true
```

The edge exists only between an unlock and a later lock **of the same monitor**. With `readLock`,
nothing in the memory model orders `stop()`'s write before the read. The worker stopped because of
how this JVM compiled the loop, not because of a rule: a hang is allowed by the JLS, not observed
here.

No other thread can ever lock the `new Object()`, so its lock can order nothing, and the JIT
removed it, after which it could read the flag once. With escape analysis off
(`-XX:-DoEscapeAnalysis`) or lock elimination off (`-XX:-EliminateLocks`), the same worker stopped
at once.

It usually reaches production as `synchronized void setLimit(int)`, which locks `this`, next to
`static synchronized int limit()`, which locks the `Class` object, over a `static` field. Review
sees the keyword on both. The `Class` object is a different monitor from every instance, even when
the class has only one. Unit 6.2.1 shows the same mistake losing increments. Name the lock that
guards each field, and take that lock in every method that touches it.

<!-- practice 3 -->

## A Reader That Skips the Lock Can See Two Fields That Were Never Written Together

### Spot the Problem: the Price Book

One instrument of the price book from the opening:

```java
static class PriceBook {
    private final Object lock = new Object();
    private long bid = 10_000, ask = 10_001;           // cents; the spread is never negative

    void quote(long newBid, long newAsk) {
        synchronized (lock) {
            bid = newBid;
            ask = newAsk;
        }
    }

    long spreadUnlocked() {
        return ask - bid;                              // "a read can't break anything"
    }

    long spread() {
        synchronized (lock) {
            return ask - bid;
        }
    }
}
```

One thread alternates between `quote(20_000, 20_001)` and `quote(10_000, 10_001)` as fast as it can.
Every quote it writes has a spread of 1. Another thread reads the spread 10 million times:

```text
unlocked read  1,016 negative and 1,071 too wide in 10,000,000 reads; lowest spread -9999
locked read    0 negative and 0 too wide in 10,000,000 reads; lowest spread 1
```

"Too wide" is the other mix, a new ask with an old bid. A second run found 908 negative spreads; a
third found none, so a test that runs it once can pass. The locked reader found none in three runs.

The lock excludes only threads that take it. The unlocked reader can run while the writer is between
`bid = newBid` and `ask = newAsk`: it reads the old ask, 10,001, and the new bid, 20,000, and
reports a spread of -9,999. Each write was made under the lock; the reader still saw a pair that no
single quote ever had. A reader that saw both fields from before the quote, however stale, would
still see a spread of 1; the mix comes from reading between the two writes.

The same holds for any invariant over two fields, a start before an end or a host with its port:
the writer's lock keeps it only for readers that take that lock. A trade priced off a negative
spread goes through with nothing logged.

Read under the same lock as the writes, as `spread()` does. If readers are many and the lock becomes
contended, put the pair in one immutable object behind one `volatile` reference (unit 6.2.2), or use
a read-write lock (unit 6.3.1). Reading one field without the lock is no safer: it has the
visibility problem of the first section, and the JLS even allows a plain `long` to be read half old,
half new (unit 5.1.1).

<!-- practice 1 -->

## Double-Checked Locking Reads Without the Lock, So Its Field Must Be volatile

A service creates its database connection on first use, and wants later calls not to lock:

```java
static class Conn {
    String host;                                 // plain, non-final fields
    int port;
    Conn(String host, int port) { this.host = host; this.port = port; }
}

static class Lazy {                              // double-checked locking, field not volatile
    private Conn conn;

    Conn get() {
        if (conn == null) {                      // first check, no lock
            synchronized (this) {
                if (conn == null) conn = new Conn("db-1.example.com", 5432);
            }
        }
        return conn;
    }
}
```

Which line reads shared state without the lock? The first check. Once `conn` is set, a caller never
enters the block, so it never locks after the creating thread unlocked, and no edge connects them.
It can see `conn` non-null and still read the fields the constructor wrote as `null` and `0`. The
constructor runs before the assignment in the creating thread's program order, but only a thread
with an edge to that thread is promised that order (unit 5.1.4 shows what the compiler and the
processor may reorder).

Two programs, 50 million reads each. The first calls `get()` on fresh `Lazy` objects right after
another thread has assigned `conn`. The second publishes new `Conn` objects through a plain write
outside any lock, and reads them with no ordering, like the first check:

```text
plain field   : 50,000,000 calls to get(), 0 returned an unfinished connection, 0 returned null
50,000,000 references read without a lock, 197,066 of them to an unfinished object (host null 0, port 0 197,065)
```

Through `Lazy`, three runs found no unfinished connection: allowed by the JLS, not observed here.
The plain publication found connections with `port` still 0 in all three runs (40,277 to 290,600 per
run), and none with the C2 compiler off (`-XX:TieredStopAtLevel=1`), so most likely the JIT moved
the constructor's store of `port` after the publishing store. The constructor still runs first in
the creating thread; what moved is when another thread can see each store. Nothing in the JLS stops
it doing the same inside `Lazy`.

Make the field `volatile`. A volatile write happens-before every later read that sees it, so a
caller that sees the reference sees the finished object. Read the field once into a local, so the
value you checked is the value you return:

```java
static class LazyVolatile {                      // the same with a volatile field
    private volatile Conn conn;

    Conn get() {
        Conn c = conn;
        if (c == null) {
            synchronized (this) {
                c = conn;
                if (c == null) conn = c = new Conn("db-1.example.com", 5432);
            }
        }
        return c;
    }
}
```

```text
volatile field: 50,000,000 calls to get(), 0 returned an unfinished connection, 0 returned null
```

The second check is still needed: two callers can both find `null` before either takes the lock,
and without it the second one builds another connection after the first has released.

For a value per class rather than per object, the other fix is to let class initialization do it
(this `Conn` is a record whose constructor prints `building the connection`):

```java
static final class ConnectionsHolder {
    private static final class Holder {
        static final Conn CONN = new Conn("db-1.example.com", 5432);
    }

    static Conn get() {
        return Holder.CONN;
    }

    static String name() {
        return "connections";
    }
}
```

```text
name(): connections
  building the connection
first get(): Conn[host=db-1.example.com, port=5432]
second get(): Conn[host=db-1.example.com, port=5432]
```

`Holder` is initialized the first time `get()` reads `Holder.CONN`, not when the outer class is used
(JLS §12.4.1). The JVM runs a class's initialization under a lock and makes every thread that uses
the class see its result (JLS §12.4.2), so no `volatile` and no check is needed.

A `Conn` whose fields are all `final` is also safe to see through a racy reference, provided its
constructor doesn't leak `this` (JLS §17.5; the plain-publication run found 0 unfinished objects
with `final` fields; unit 5.2.3). That doesn't repair the `Lazy` above: its two reads of `conn` may
see different values, so the second may return `null` (allowed by the JLS; none of the 150 million
plain-field calls did).

<!-- practice 2 -->

## Summary

- **Releasing a lock publishes every write made before it to the next thread that takes the same
  lock.** A getter that skips the lock may never see the setter's write; the worker that read its
  stop flag without the lock never stopped.
- **A lock on a different object gives no edge.** Two methods that both say `synchronized` but lock
  different objects promise nothing; HotSpot removed a lock no other thread could take, and then
  read the flag once.
- **A reader that skips the lock can see two fields that were never written together.** Every
  writer locking keeps an invariant only for readers that lock too; the unlocked reader saw a
  negative spread about once in 10,000 reads in two runs of three.
- **Double-checked locking reads without the lock, so its field must be `volatile`.** Without it, a
  caller may get a reference to an unfinished object; with `volatile` and one read into a local, or
  with a holder class, it can't.

## Reference

**What gives an edge from a writer to a reader (Java 21):**

| Writer | Reader | Edge |
|---|---|---|
| unlock of monitor `m` (end of `synchronized (m)`) | a later lock of `m` | yes (JLS §17.4.5) |
| unlock of `m` | a lock of another object | no |
| unlock of `m` | no lock | no |
| `Lock.unlock()` | a later successful `lock()`, `tryLock()` of the same `Lock` | yes (`Lock` javadoc, "Memory Synchronization") |
| `Lock.unlock()` | a failed `tryLock()` | not required ("Unsuccessful locking ... do not require any memory synchronization effects") |
| write of a `volatile` field | a later read of that field | yes (unit 6.2.2) |
| a class's static initializer | any use of the class after it is initialized | yes (JLS §12.4.2) |

**Lazy initialization of a shared object:**

| Shape | Safe | Locks after the first call |
|---|---|---|
| `synchronized` getter | yes | every call |
| double-checked, plain field | no: a caller may see an unfinished object | no |
| double-checked, `volatile` field, read once into a local | yes | no |
| holder class (`static final` field of a nested class) | yes, for a `static` value | no |

## Challenges

1. A cache keeps a `HashMap` that every writer updates under `synchronized (this)`. A metrics
   thread calls `map.size()` without the lock once a second. What can go wrong beyond a stale
   number, and would you lock, copy, or switch to `ConcurrentHashMap`?
2. The price book reads the spread under the lock millions of times a second while quotes arrive a
   few thousand times a second. Measure `spread()` against a version that swaps one immutable
   `Quote` record through a `volatile` field. Where does each stop scaling?
3. A team wants double-checked locking for a value that must be reloaded every hour. Which of the
   fixes on this page still work, and what does the reload need that the first creation didn't?

## Design Note: Why the Edge Belongs to the Lock Object

The memory model could have made every lock a full barrier, so that any `synchronized` block
published everything to everyone. Then the `readLock` version of `isStopped()` would be correct,
and so would `new Object()`.

JSR 133, the memory model since Java 5, ties the monitor edge to one monitor instead: an unlock
orders only the later locks of the same object. The JIT can then remove a lock that no
other thread can see, merge adjacent locks on one object, and move reads and writes into a
critical section, all without breaking a program that synchronizes correctly. The price falls on
programs that relied on a lock as a general barrier: they still work on some JVMs, and stop
working on a JVM that removes or merges that lock.

## Code Examples

- Source: [MemoryConsistency.java](src/main/java/com/github/msorkhpar/claudejavatutor/javamemorymodel/MemoryConsistency.java)
- Test: [MemoryConsistencyTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/javamemorymodel/MemoryConsistencyTest.java)
