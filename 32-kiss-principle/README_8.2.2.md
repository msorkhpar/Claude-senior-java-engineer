# 8.2.2. Applying KISS in Concurrent Programming

A warehouse service moves stock between 100 bins on four threads. A dashboard audit adds up the
bins and raises an alert when the total isn't 100,000. The alert fires on almost every audit, yet
the closing count is always exactly 100,000. Every bin has its own lock, and every move takes both
bins' locks in a fixed order.

Every output is from Java 21 on one 20-core
machine; timings come from plain loops, not a benchmark harness.

## A Lock per Bin Keeps Every Move Right and Every Total Wrong

```java
import java.util.Arrays;

/** Stock in numbered bins, with a lock per bin. */
public class LockedBins {
    private final long[] qty;
    private final Object[] locks;

    public LockedBins(int bins, long each) {
        qty = new long[bins];
        Arrays.fill(qty, each);
        locks = new Object[bins];
        Arrays.setAll(locks, i -> new Object());
    }

    public boolean move(int from, int to, long n) {
        Object first = locks[Math.min(from, to)], second = locks[Math.max(from, to)];
        synchronized (first) {                      // always the lower bin first (unit 6.2.1)
            synchronized (second) {
                if (qty[from] < n) return false;
                qty[from] -= n;
                qty[to] += n;
                return true;
            }
        }
    }

    public long total() {
        long sum = 0;
        for (int b = 0; b < qty.length; b++) {
            synchronized (locks[b]) { sum += qty[b]; }
        }
        return sum;
    }
}
```

No field is read or written without its lock, and the lock order rules out deadlock. Four threads
make random moves for one second while a fifth audits in a loop:

```text
lock per bin      11,855,782 moves,    258,239 audits,  247,317 wrong, final total 100,000
```

Three runs gave 233,571 to 247,366 wrong totals out of 243,962 to 262,097 audits.

Say the audit has added up bins 0 to 40 when a move takes 3 units from bin 70 to bin 12.
Bin 12 was counted before the move and bin 70 will be counted after it, so the audit comes out 3
short. A move the other way makes it 3 too high. Each lock protects one bin, and the total is a
fact about all 100 at once, which no single lock protects. Inside this design the direct cure is
for `total()` to hold all 100 locks, taken in order, for the whole sum.

## One Lock Makes the Total Right; One Owner Thread Also Keeps Readers From Waiting

The first fix deletes code: drop the lock array and make `move` and `total` `synchronized`. The
second gives the bins an owner: a single-thread executor is the only thread that touches `qty`
after construction, so the array needs no lock (unit 5.3.4), and after each move it publishes a
copy that nothing changes:

```java
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Stock in numbered bins, changed only by one owner thread. */
public class OwnedBins implements AutoCloseable {

    /** What readers get: a copy that nothing changes after it is built. */
    public static final class Stock {
        private final long[] qty;
        Stock(long[] live) { qty = live.clone(); }
        public long of(int bin) { return qty[bin]; }
        public long total() { return Arrays.stream(qty).sum(); }
    }

    private final ExecutorService owner = Executors.newSingleThreadExecutor();
    private final long[] qty;                       // read and written only by the owner thread
    private volatile Stock stock;                   // replaced whole after every move

    public OwnedBins(int bins, long each) {
        qty = new long[bins];
        Arrays.fill(qty, each);
        stock = new Stock(qty);
    }

    public Future<Boolean> move(int from, int to, long n) {
        return owner.submit(() -> {
            if (qty[from] < n) return false;
            qty[from] -= n;
            qty[to] += n;
            stock = new Stock(qty);
            return true;
        });
    }

    public Stock stock() { return stock; }

    @Override
    public void close() { owner.shutdownNow(); }
}
```

The same race, with the movers waiting on each `Future` and the audit reading `stock().total()`:

```text
owner, snapshot      368,597 moves, 12,285,992 audits,        0 wrong, final total 100,000
one lock           6,952,234 moves,  2,327,678 audits,        0 wrong, final total 100,000
```

Both are right in all three runs. The volatile write publishes the finished `Stock` (unit 5.2.3),
and a reader holds one whole state, from before or after any move, for as long as it likes.
`Stock` copies the array. A read-only view of live state would not do: the javadoc of
`Collections.unmodifiableMap(m)` says its reads go through to `m`, so it changes under the reader
with every move.

The two fixes differ in who waits. With one lock, an audit waits for a move and a move waits for an
audit. With an owner, readers never wait: 12 million audits against 2.3 million. Moves cost more:
each is a hand-off to another thread and back, and the owner did 340,000 to 490,000 moves a second
against the one lock's 6.4 to 9 million. When nobody needs reads that never block, one lock is the
simpler choice.

A decision belongs on the owner too. A caller that checks `stock().of(from) >= n` and then submits
a move without the check is the check-then-act of unit 5.3.1. Two callers asked to take 3 units
from a bin holding 3, while the owner was still busy with an earlier task:

```text
checked on the caller:     first move true, second move true, bin 0 now holds -3
checked inside the task:   first move true, second move false, bin 0 now holds 0
```

Checked on the caller, both moves read the same snapshot (bin 0 holds 3) and passed. Inside the
task, the check runs on the only thread that changes the bins, so nothing comes between it and the
move.

### A Snapshot of 100,000 Bins Copies 800 KB on Every Move

The same race with 100,000 bins, plus an owner with no snapshot that runs each audit as a task
submitted to the owner:

```text
lock per bin      21,433,573 moves,        593 audits,      593 wrong, final total 100,000,000
owner, snapshot       10,542 moves,     28,722 audits,        0 wrong, final total 100,000,000
owner, audit task    137,774 moves,     20,674 audits,        0 wrong, final total 100,000,000
one lock           2,522,335 moves,     28,821 audits,        0 wrong, final total 100,000,000
```

Every move now copies 800 KB, and the owner managed 10,500 to 12,900 moves a second over three
runs. A snapshot suits small state. For large state that changes often, send the read to the
owner as a task, which then waits its turn behind the moves, or use one lock (790,000 to 2.5
million moves a second here).

<!-- practice 1 -->

<!-- practice 2 -->

## A compareAndSet Loop That Reads the Value Once Spins Forever; incrementAndGet Leaves No Loop to Get Wrong

A counter whose author read that atomics beat locks, with its retry loop written by hand:

```java
private final AtomicLong count = new AtomicLong();

public void increment() {                          // hand-written, "lock-free"
    long seen = count.get();
    while (!count.compareAndSet(seen, seen + 1)) {
        Thread.onSpinWait();                       // someone else won; try again
    }
}
```

Spot the problem before the run. Four threads, a million increments each, two seconds to finish:

```text
hand-written CAS 3 of 4 threads still running after 2 s, count = 1,000,000
```

Ten runs out of ten ended this way, with the count between 1,000,000 and 1,000,255. `seen` is
read once, before the loop. The first time another thread wins, `count` moves past `seen` and never
comes back, so every later `compareAndSet(seen, …)` fails: the thread spins at 100 % until the JVM
exits. A retry loop has to read the value again on every failure, as `updateAndGet()` does inside
the JDK (unit 5.3.2 shows it). `incrementAndGet()` and a `synchronized` method around `count++`
leave no loop to write, three runs each:

```text
incrementAndGet  all threads done in 49 ms, count = 4,000,000
synchronized     all threads done in 246 ms, count = 4,000,000
```

`incrementAndGet()` took 41 to 49 ms and `synchronized` 246 to 294 ms, for four threads doing
nothing but counting. For a counter that is the hot spot, unit 5.3.2 measures `LongAdder`.

## A Double-Checked Flag Can Say Loaded Before the Value Is; a synchronized Getter Can't

Exchange rates are loaded on first use. The author wanted no lock once they are loaded, and set
the flag first so that no second caller starts another load:

```java
import java.util.Map;
import java.util.function.Supplier;

/** Exchange rates loaded on first use, with no lock once they are loaded. */
public class FlagRates {
    private final Supplier<Map<String, Long>> loader;
    private volatile boolean loaded;
    private Map<String, Long> rates;

    public FlagRates(Supplier<Map<String, Long>> loader) { this.loader = loader; }

    public Map<String, Long> rates() {
        if (!loaded) {
            synchronized (this) {
                if (!loaded) {
                    loaded = true;                  // first, so no second caller starts another load
                    rates = loader.get();
                }
            }
        }
        return rates;
    }
}
```

The plain version locks every call and uses the value itself as the flag:

```java
import java.util.Map;
import java.util.function.Supplier;

/** Exchange rates loaded on first use, under one lock. */
public class SyncRates {
    private final Supplier<Map<String, Long>> loader;
    private Map<String, Long> rates;

    public SyncRates(Supplier<Map<String, Long>> loader) { this.loader = loader; }

    public synchronized Map<String, Long> rates() {
        if (rates == null) rates = loader.get();
        return rates;
    }
}
```

Thread A calls `rates()`, and its load is held open while thread B calls `rates()` and for 500 ms
after. A second run makes the first load throw. `volatile ref` is the double-checked locking of
unit 5.1.3: one `volatile` field holding the map, read once into a local.

```text
flag          B 500 ms into the load: returned null; B finally got null
synchronized  B 500 ms into the load: waiting (BLOCKED); B finally got {EUR/USD=10842, GBP/USD=12671}
volatile ref  B 500 ms into the load: waiting (BLOCKED); B finally got {EUR/USD=10842, GBP/USD=12671}
flag          1st call: rate service timed out; 2nd: null; 3rd: null
synchronized  1st call: rate service timed out; 2nd: {EUR/USD=10842, GBP/USD=12671}; 3rd: {EUR/USD=10842, GBP/USD=12671}
volatile ref  1st call: rate service timed out; 2nd: {EUR/USD=10842, GBP/USD=12671}; 3rd: {EUR/USD=10842, GBP/USD=12671}
```

In all three runs the flag said "loaded" while the map was still `null`: the flag and the map are
two variables. B read the flag without the lock, so it never waited for the load. When
the load threw, the flag stayed `true` and the map `null`, and every later call returns `null`
until the process restarts. In `SyncRates` there is only the map: `null` means "not loaded", a
failed load leaves it `null`, and the next call tries again. The volatile version also has a
single variable.

The lock costs time. Eight threads that do nothing but call `rates()` 10 million times each:

```text
synchronized  8 threads x 10,000,000 calls: 3,327 ms
volatile ref  8 threads x 10,000,000 calls: 47 ms
```

Keep the `synchronized` getter until a profile shows it, then switch to the volatile reference.

<!-- practice 3 -->

## Summary

- **A lock per object keeps each object right and leaves every fact about several objects
  unprotected.** Making the total right inside that design means holding every lock at once.
- **One lock fixes it by deleting code; one owner thread with an immutable snapshot fixes it and
  keeps readers from waiting.** The owner costs a hand-off per write and a copy per snapshot, and
  decisions about the state are made on the owner, not on a snapshot.
- **A hand-written compareAndSet loop that doesn't re-read the value spins forever** after its
  first lost race. `incrementAndGet()` or `synchronized` is correct with no loop of your own.
- **A double-checked flag beside the value can say "loaded" while the value is `null`**, and a
  failed load leaves it so for good. A `synchronized` getter whose value is its own flag is right;
  the volatile-reference version of unit 5.1.3 is the fix when the lock shows in a profile.

## Reference

**Measured on this page (Java 21, one 20-core machine, three runs each, plain loops):**

| Design | Result |
|---|---|
| a lock per bin, audit takes each lock in turn, 100 bins | 94–96 % of audits wrong; final total right |
| one lock, 100 bins | 0 wrong; 6.4–9 million moves/s, 1.9–2.6 million audits/s |
| owner thread and snapshot, 100 bins | 0 wrong; 340,000–490,000 moves/s, 10.7–14.8 million audits/s |
| owner thread and snapshot, 100,000 bins | 0 wrong; 10,500–12,900 moves/s |
| owner thread, audit as a task, 100,000 bins | 0 wrong; 137,000–181,000 moves/s |
| `compareAndSet` loop reading the value once | 3 of 4 threads spinning after 2 s, 10 of 10 runs |
| `incrementAndGet()` / `synchronized` counter, 4 × 1 million | 41–49 ms / 246–294 ms |
| `synchronized` / volatile-reference lazy getter, 8 × 10 million calls | 3.3–3.6 s / 43–47 ms |

**What a reader of each construct can rely on (javadoc):**

| Construct | Guarantee |
|---|---|
| `ExecutorService` | actions before `submit` happen-before the task; the task happens-before `Future.get()` returns |
| `Executors.newSingleThreadExecutor()` | tasks run sequentially, never two at once; a thread that dies of a failure is replaced |
| `Collections.unmodifiableMap(m)` | a read-only view; reads go through to `m` |
| `AtomicLong.compareAndSet(expect, update)` | sets the value only if it currently equals `expect`; returns whether it did |

## Challenges

1. The warehouse also needs "move 10 units from bin 4 to whichever of bins 7, 8 and 9 has the
   least stock". Write it for one lock and for the owner thread. Which version needs a new idea?
2. At what number of bins does the snapshot's copy cost more than the hand-off on your machine?
   Race four movers against an auditor as this page does, then try a snapshot built from a
   persistent map instead of a copy.
3. Your counter must also record the time of the last increment, and readers want both values
   together. Which of `AtomicLong`, `synchronized` and an immutable pair behind an
   `AtomicReference` (unit 5.3.2) do you choose, and why?

## Design Note: Simple Means Fewer Shared Variables, Not Fewer Lines

Unit 8.2.1 judged simplicity by the code you read. With threads, the measure is how many shared
mutable variables a reader has to keep in mind at once, and in how many places they change.
`OwnedBins` is longer than `LockedBins`, yet once it is built the only variable two threads share
is `stock`, and only the owner writes it. `FlagRates` is longer than `SyncRates` because it keeps
a second variable in step with the first, and that second variable is the bug.

The price of that design is throughput: one lock admits one thread at a time and one owner thread
does one thing at a time, so neither scales with cores the way the broken per-bin locks did. Unit
8.2.3 counts what the simpler design buys in return: fewer interleavings to reason about and test.

## Code Examples

- Test: [KissConcurrencyTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/kissprinciple/KissConcurrencyTest.java)
- Source: [KissConcurrency.java](src/main/java/com/github/msorkhpar/claudejavatutor/kissprinciple/KissConcurrency.java)
