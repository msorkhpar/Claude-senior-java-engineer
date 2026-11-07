# 5.1.4. Reordering and Optimization by the Compiler and Runtime

A connection pool shuts down while requests are still arriving. A request marks the pool as in use
and then checks that no shutdown has begun; the shutdown marks the pool as closing and then closes
it if no request holds it:

```java
// request thread                         // shutdown thread
pool.inUse = true;                        pool.closing = true;
if (!pool.closing) query(pool);           if (!pool.inUse) close(pool);
```

In any interleaving that keeps each thread's own order, the later check comes after both writes,
so at least one thread sees the other's flag. During deploys, the logs still show queries on a
closed connection. A test that makes the two threads collide a million times (`Handshake.java`,
both fields plain, built like the tool below) counts how often both checks read `false`:

```text
1,000,000 races, 20 CPUs, amd64: query ran on a closed connection 95,973 times
```

In those runs, at least one thread's write became visible to the other only after its own read.
Every run on this page is Java 21 on one x86_64 machine with 20 available processors.

## A Tool That Makes Two Threads Collide on Every Trial

The outcomes below need two threads to touch the same fields within nanoseconds of each other.
`Litmus` makes that happen on every trial: both threads wait for each other at a spin barrier, then
each runs its actor against a fresh `Cell` and records what it read in `r1` or `r2`.

```java
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Map;
import java.util.TreeMap;

/** Runs two actors against a fresh Cell, many times, and counts what they read. */
public final class Litmus {

    /** One trial's shared fields, and the two values the actors read. */
    public static final class Cell {
        int x, y;                    // plain
        volatile int vx, vy;         // volatile
        int r1, r2;                  // what actor 1 and actor 2 read
        Box box;                     // plain reference, for the publication example
    }

    /** An object whose constructor sets two plain fields. */
    public static final class Box {
        int a, b;
        Box(int a, int b) { this.a = a; this.b = b; }
    }

    /** Access modes for x and y, for the VarHandle examples. */
    public static final VarHandle X, Y;
    static {
        try {
            X = MethodHandles.lookup().findVarHandle(Cell.class, "x", int.class);
            Y = MethodHandles.lookup().findVarHandle(Cell.class, "y", int.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public interface Actor { void act(Cell c); }

    private static volatile long arrived1, arrived2;   // a two-thread spin barrier per trial

    public static Map<String, Long> run(int trials, Actor one, Actor two) throws InterruptedException {
        Cell[] cells = new Cell[trials];
        for (int i = 0; i < trials; i++) cells[i] = new Cell();
        arrived1 = arrived2 = 0;
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < trials; i++) {
                arrived1 = i + 1;
                while (arrived2 < i + 1) Thread.onSpinWait();  // both start trial i together
                one.act(cells[i]);
            }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < trials; i++) {
                arrived2 = i + 1;
                while (arrived1 < i + 1) Thread.onSpinWait();
                two.act(cells[i]);
            }
        });
        t1.start(); t2.start(); t1.join(); t2.join();
        Map<String, Long> outcomes = new TreeMap<>();
        for (String k : new String[] {"r1=0 r2=0", "r1=0 r2=1", "r1=1 r2=0", "r1=1 r2=1"}) outcomes.put(k, 0L);
        for (Cell c : cells) outcomes.merge("r1=" + c.r1 + " r2=" + c.r2, 1L, Long::sum);
        return outcomes;
    }

    public static void print(Map<String, Long> outcomes) {
        outcomes.forEach((k, v) -> System.out.printf("  %s  %,10d%n", k, v));
    }
}
```

Each example below ran in its own JVM with 10,000,000 trials. How often production hits an
outcome depends on how often two threads collide.

## A Thread Keeps Its Own Order; Another Thread May See Its Accesses Reordered

Within one thread, the compiler and the processor keep only the order that thread can observe.
Stripped to its accesses, the pool is this test, called *store buffering*: each thread writes its
own field, then reads the other's.

```java
Litmus.run(n,
        c -> { c.x = 1; c.r1 = c.y; },
        c -> { c.y = 1; c.r2 = c.x; });
```

Before you look: which of the four outcomes can't happen if each thread runs its two lines in
order?

```text
sb-plain: 10,000,000 trials, 20 CPUs, amd64, Java 21.0.12.1
  r1=0 r2=0   2,267,408
  r1=0 r2=1   3,927,960
  r1=1 r2=0   3,804,626
  r1=1 r2=1           6
```

`r1=0 r2=0` needs both reads to run before both writes, and no interleaving of two in-order threads
does that. It came up in 23% of the trials.

The JLS allows it. Within a thread, the rule is *intra-thread semantics* (JLS 17.4): the thread
must behave as its program order says, as far as the thread itself can tell. `c.x = 1` and
`r1 = c.y` touch different fields, so doing the read first changes nothing thread 1 can observe,
and the compiler, the JIT and the processor may all do it. JLS Example 17.4-1 makes the same point
with a read followed by a write. Another thread can tell, and the JMM makes promises to other
threads through happens-before and the order of synchronization actions (unit 5.2.1): a program
free of data races behaves as if sequentially consistent (unit 5.1.1), and this one has two races.

In the pool, both threads read the old value, both proceed, and the request queries a closed
connection. A single-threaded test can't show it.

<!-- practice 1 -->

## The Processor's Store Buffer Lets a Read Overtake an Earlier Write, with No JIT at All

The reordering doesn't need a compiler. Under `-Xint` nothing is compiled, and the interpreter
runs the bytecode in order:

```text
--- -Xint (interpreter only)
sb-plain: 10,000,000 trials, 20 CPUs, amd64, Java 21.0.12.1
  r1=0 r2=0   5,100,374
  r1=0 r2=1   2,533,963
  r1=1 r2=0   2,365,262
  r1=1 r2=1         401
```

No compiler moved anything, and both reads still missed both writes. A core doesn't wait for a
store to reach the cache: it puts the store in its own *store buffer* and goes on to the next
instruction, and the buffer drains to the cache a little later. A load that comes next can run
while the store is still in the buffer, so to every other core the read happened first. The core's
own loads check its buffer before the cache, which is why the thread itself always reads what it
just wrote.

The caches stay coherent throughout. What hides the write is a queue in front of the cache, not a
stale copy in a thread's "working memory" (unit 5.1.2).

On x86 the buffer drains in program order and loads aren't reordered with other loads, so other
cores see one core's stores in the order it made them; the next section's interpreted run agrees.
That is why switching off the JIT doesn't fix the pool: the swap it suffers is made by the hardware.

> **Other processors differ.** ARM and POWER allow more reorderings in hardware than x86 does, so a
> race that x86 never shows can appear there.

<!-- practice 2 -->

## The JIT Reorders Too, So x86 Shows a Reordering Its Hardware Doesn't Make

The compiler may reorder plain accesses as freely as the processor. HotSpot compiles hot code
first with C1, then with the optimizing C2. The test is *message passing*: thread 1 writes the data
(`x`), then a ready flag (`y`); thread 2 reads the flag, then the data. A reader that sees the flag
should see the data.

```java
Litmus.run(n,
        c -> { c.x = 1; c.y = 1; },
        c -> { c.r1 = c.y; c.r2 = c.x; });
```

```text
mp-plain: 10,000,000 trials, 20 CPUs, amd64, Java 21.0.12.1
  r1=0 r2=0   5,738,143
  r1=0 r2=1      28,361
  r1=1 r2=0       6,727
  r1=1 r2=1   4,226,769
```

`r1=1 r2=0`: the flag was set and the data was not. Interpreted, and with C1 only, it never
happened:

```text
--- -Xint (interpreter only)
mp-plain: 10,000,000 trials, 20 CPUs, amd64, Java 21.0.12.1
  r1=0 r2=0   8,249,427
  r1=0 r2=1     286,509
  r1=1 r2=0           0
  r1=1 r2=1   1,464,064
--- C1 only (-XX:TieredStopAtLevel=1)
mp-plain: 10,000,000 trials, 20 CPUs, amd64, Java 21.0.12.1
  r1=0 r2=0   5,858,305
  r1=0 r2=1      71,613
  r1=1 r2=0           0
  r1=1 r2=1   4,070,082
```

A separate run moved the writer into a method of its own, `writeDataThenFlag`
(`ex/run-1.txt`). The default JIT gave 6,552 such trials, C2 alone (`-XX:-TieredCompilation`)
14,220, C1 alone 0, and 0 once `-XX:CompileCommand=exclude` kept that one method interpreted. The
reordering came from the C2-compiled writer, which intra-thread semantics allows: the two stores go
to different fields.

Here, a reader that trusts the flag uses data that isn't there in about one collision in 1,500, but
only once C2 has compiled the writer. A test run cold, or under `-Xint`, passes. Another JIT or
another platform may reorder elsewhere, so a pass on x86 proves little. Reading a field once before
a loop instead of on every iteration is the same freedom (unit 6.2.2). A bug that disappears under
`-Xint` suggests the compiler, though the interpreter's different timing can hide a race too (unit
5.3.3).

## volatile on Every Access That Needs Ordering Rules Out Both Reorderings

Make the fields volatile and neither forbidden outcome appears. In `sb-volatile` both fields are
volatile (`vx`, `vy`); in `mp-volatile` only the flag is (`c.x = 1; c.vy = 1;` against
`c.r1 = c.vy; c.r2 = c.x;`):

```text
sb-volatile: 10,000,000 trials, 20 CPUs, amd64, Java 21.0.12.1
  r1=0 r2=0           0
  r1=0 r2=1   3,627,564
  r1=1 r2=0   4,582,009
  r1=1 r2=1   1,790,427
mp-volatile: 10,000,000 trials, 20 CPUs, amd64, Java 21.0.12.1
  r1=0 r2=0   4,674,647
  r1=0 r2=1     221,859
  r1=1 r2=0           0
  r1=1 r2=1   5,103,494
```

Volatile reads and writes are synchronization actions, and JLS 17.4.4 puts all of them in one total
order that agrees with each thread's program order. A volatile read sees the last write to its
field before it in that order (JLS 17.4.7). In store buffering, whichever volatile write comes
second in that order is followed, in its own thread, by a read that comes later still, and that
read sees the other write. In message passing, the data write comes before the volatile flag
write in program order, and the flag write happens-before the read that sees it, so the data read
sees 1.

### Spot the Problem: One Volatile Flag Out of Two

A review of the pool makes `inUse` volatile and leaves `closing` plain, since message passing needed
only one volatile field. In the test, thread 1 is the request (`vx` for `inUse`) and thread 2 the
shutdown (`y` for `closing`). Does this fix the pool?

```java
Litmus.run(n,
        c -> { c.vx = 1; c.r1 = c.y; },
        c -> { c.y = 1; c.r2 = c.vx; });
```

```text
sb-half: 10,000,000 trials, 20 CPUs, amd64, Java 21.0.12.1
  r1=0 r2=0     455,114
  r1=0 r2=1   4,907,170
  r1=1 r2=0   3,911,939
  r1=1 r2=1     725,777
```

Thread 2's plain write and its read are still free to swap, and the JMM doesn't stop thread 1's
plain read from moving ahead of its volatile write either. Message passing needs one ordered edge,
from a write to the read that sees it. Store buffering needs each thread's write ordered before its
own read, so both fields must be volatile, or both checks made under one lock (unit 5.1.3).

## A Racy Reference May Expose an Object Before Its Constructor's Writes Land

Thread 1 builds an object and stores it in a plain field; thread 2 reads the field and, if the
reference is there, both fields:

```java
Litmus.run(n,
        c -> c.box = new Litmus.Box(1, 1),
        c -> { Litmus.Box b = c.box; c.r1 = b == null ? 0 : 1; c.r2 = b != null && b.a == 1 && b.b == 1 ? 1 : 0; });
```

`r1=1 r2=0` would be a reference to an object whose constructor's writes the reader did not see.
The first of three runs:

```text
publish-plain: 10,000,000 trials, 20 CPUs, amd64, Java 21.0.12.1
  r1=0 r2=0   5,512,547
  r1=0 r2=1           0
  r1=1 r2=0           0
  r1=1 r2=1   4,487,453
```

The other two runs also gave 0: none in 30,000,000 trials. The JLS still allows it: the
constructor's writes and the store of the reference are plain writes to different variables, the
same freedom the JIT used on `x` and `y` above, and only `final` fields come with a guarantee for a
reader that gets the reference through a race (JLS 17.5, unit 5.2.3). A zero on one machine and one
JVM is not a property of the code: a weaker processor or another JIT may produce it. Double-checked locking without `volatile` (unit 5.1.3) bets on it never happening.
Publish through a `volatile` field, a lock or `final` fields.

## Acquire/Release Orders a Handoff, Not a Write Followed by a Read

`VarHandle` gives one access to a plain field a stronger mode. Release/acquire is enough to hand
data over, not to keep a thread's read behind its own earlier write:

```java
Litmus.run(n,                                       // message passing
        c -> { c.x = 1; Litmus.Y.setRelease(c, 1); },
        c -> { c.r1 = (int) Litmus.Y.getAcquire(c); c.r2 = c.x; });
Litmus.run(n,                                       // store buffering
        c -> { Litmus.X.setRelease(c, 1); c.r1 = (int) Litmus.Y.getAcquire(c); },
        c -> { Litmus.Y.setRelease(c, 1); c.r2 = (int) Litmus.X.getAcquire(c); });
Litmus.run(n,                                       // store buffering with full fences
        c -> { c.x = 1; java.lang.invoke.VarHandle.fullFence(); c.r1 = c.y; },
        c -> { c.y = 1; java.lang.invoke.VarHandle.fullFence(); c.r2 = c.x; });
```

```text
mp-release-acquire: 10,000,000 trials, 20 CPUs, amd64, Java 21.0.12.1
  r1=0 r2=0   5,552,773
  r1=0 r2=1      74,445
  r1=1 r2=0           0
  r1=1 r2=1   4,372,782
sb-release-acquire: 10,000,000 trials, 20 CPUs, amd64, Java 21.0.12.1
  r1=0 r2=0     610,780
  r1=0 r2=1   4,626,162
  r1=1 r2=0   4,747,161
  r1=1 r2=1      15,897
sb-full-fence: 10,000,000 trials, 20 CPUs, amd64, Java 21.0.12.1
  r1=0 r2=0           0
  r1=0 r2=1   4,819,505
  r1=1 r2=0   4,734,429
  r1=1 r2=1     446,066
```

The javadoc defines the modes in steps: an *acquire* read and the accesses after it are ordered
after a matching *release* write and the accesses before it; *volatile* accesses are, in addition,
totally ordered with each other. Message passing held. Neither stops an acquire read from overtaking a
release write that comes before it in the same thread, which is the store buffer's reordering, so
store buffering failed 610,780 times. `VarHandle.fullFence()` orders every access before it with
every access after it and, like `volatile`, gave 0.

Use release/acquire for one-way handoffs, such as a producer publishing a filled buffer, when
measurement shows volatile costs too much. Use volatile, or a lock, when each thread writes and then
reads what the other wrote.

<!-- practice 3 -->

## Summary

- **A thread keeps its own order; another thread may see its accesses reordered.** Two threads that
  each write a flag and then read the other's can both read the old value.
- **The processor's store buffer lets a read overtake an earlier write.** It happens with no JIT at
  all; the thread itself still reads its own write.
- **The JIT reorders too.** On x86 it made a ready flag visible before the data, which the hardware
  alone never did.
- **volatile on every access that needs ordering rules out both reorderings.** One volatile field is enough to hand data
  over, not for a write-then-read handshake.
- **A racy reference may expose an object before its constructor's writes land.** Not observed here;
  allowed by the JLS, so publish safely.
- **Acquire/release orders a handoff, not a write followed by a read.** Store buffering needs
  volatile or a full fence.

## Reference

**`VarHandle` access modes (from the javadoc), weakest first:**

| Mode | Methods | Guarantee |
|---|---|---|
| plain | `get`, `set` | no ordering other threads can observe; bitwise atomic only for references and values of at most 32 bits |
| opaque | `getOpaque`, `setOpaque` | bitwise atomic, coherently ordered with other accesses to the same variable |
| acquire / release | `getAcquire`, `setRelease` | an acquire read and later accesses are ordered after a matching release write and earlier accesses |
| volatile | `getVolatile`, `setVolatile` | in addition, all volatile accesses are totally ordered with each other |

**Fences (static methods of `VarHandle`):**

| Method | Accesses before the fence | are not reordered with |
|---|---|---|
| `fullFence()` | loads and stores | loads and stores after it |
| `acquireFence()` | loads | loads and stores after it |
| `releaseFence()` | loads and stores | stores after it |

**What each test showed here (10,000,000 trials, Java 21, x86_64, 20 processors):**

| Test | Outcome counted | default JIT | `-Xint` |
|---|---|---|---|
| store buffering, plain | `r1=0 r2=0` | 2,267,408 | 5,100,374 |
| store buffering, one field volatile | `r1=0 r2=0` | 455,114 | not run |
| store buffering, both volatile / `fullFence()` | `r1=0 r2=0` | 0 / 0 | not run |
| store buffering, release/acquire | `r1=0 r2=0` | 610,780 | not run |
| message passing, plain | `r1=1 r2=0` | 6,727 | 0 |
| message passing, volatile flag / release-acquire | `r1=1 r2=0` | 0 / 0 | not run |
| plain publication of a new object | reference without its fields | 0 (3 runs) | not run |

## Challenges

1. Run `sb-plain` and `mp-plain` on an ARM machine. Which counts change, and does `-Xint` still
   show `r1=1 r2=0` there?
2. Replace the pool's two flags with one `AtomicReference` holding a state (`OPEN`, `IN_USE`,
   `CLOSING`). What does a compare-and-set give you that two volatile fields did not?
3. Measure `volatile` against release/acquire on a single-producer queue's index. Is the difference
   visible next to the work each item does?

## Design Note: Why the JMM Doesn't Promise Sequential Consistency to Every Program

Java could have promised that every program runs as some interleaving of its threads, races
included. Every plain store would then need at least what the `fullFence()` run added, an ordering
against the loads after it, and the JIT could no longer reorder, merge or keep in a register any field
access another thread might see. Most fields are touched by one thread at a time, so all code would
pay for the few that are shared.

The JMM makes the opposite trade. Plain accesses get no ordering promise to other threads, and the
program marks the few that need one with `volatile`, a lock, `final` or a `VarHandle` mode. The cost
is what this page measured: a missing mark is not a compile error and often not a test failure. On
one x86 machine it showed up in 23% of collisions for one test, one in 1,500 for another, and never
for the third.

## Code Examples

- Source: [InstructionReordering.java](src/main/java/com/github/msorkhpar/claudejavatutor/javamemorymodel/InstructionReordering.java)
- Test: [InstructionReorderingTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/javamemorymodel/InstructionReorderingTest.java)
