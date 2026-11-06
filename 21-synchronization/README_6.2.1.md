# 6.2.1. Monitors and Synchronized Blocks

A flash sale offers 500 consoles, and the stock service sells 507. Every method of the class that
holds the stock is `synchronized`, and the checkout does what looks safe:

```java
if (stock.available() >= 1) stock.take(1);
```

The hotfix moves the whole checkout into one `synchronized` method, payment call included. The
overselling stops, and the service now completes one checkout per payment round trip: at 50 ms a
call, 20 orders a second, however many request threads it has. Both versions use the same keyword;
they differ in what they lock and how much code runs under one hold.

## A Tool That Starts Threads Together

The racing examples use one helper. It releases the threads together, gives them a second, and
prints any still running with what the JVM's deadlock detector says about them.

```java
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/** Starts threads at the same moment, waits up to a second for them, and reports what happened. */
public final class Together {

    /** The body of thread number i. */
    public interface Body {
        void run(int i) throws Exception;
    }

    public static void run(String name, int threads, Body body) throws InterruptedException {
        CountDownLatch go = new CountDownLatch(1);
        List<Thread> started = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            int n = i;
            Thread t = new Thread(() -> {
                try {
                    go.await();
                    body.run(n);
                } catch (Exception e) {
                    System.out.println("  " + Thread.currentThread().getName() + " ended with " + e);
                }
            }, name + "-" + i);
            t.setDaemon(true);                       // a thread that never ends can't keep the JVM alive
            t.start();
            started.add(t);
        }
        long begin = System.nanoTime();
        go.countDown();
        long deadline = begin + 1_000_000_000L;
        for (Thread t : started) t.join(Math.max(1, (deadline - System.nanoTime()) / 1_000_000));
        long ms = (System.nanoTime() - begin) / 1_000_000;
        List<String> stuck = started.stream().filter(Thread::isAlive).map(Thread::getName).toList();
        if (stuck.isEmpty()) {
            System.out.printf("%-14s %d threads done in %d ms%n", name, threads, ms);
            return;
        }
        System.out.printf("%-14s still running after 1 s: %s%n", name, stuck);
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        long[] ids = mx.findDeadlockedThreads();
        if (ids == null) {
            System.out.println("  findDeadlockedThreads(): null");
            return;
        }
        for (ThreadInfo info : mx.getThreadInfo(ids)) {
            System.out.printf("  deadlocked: %s waits for %s held by %s%n", info.getThreadName(),
                    info.getLockName().replaceAll("@.*", ""), info.getLockOwnerName());
        }
    }
}
```

Every output on this page comes from Java 21 on a 20-core machine, one JVM per example.

## synchronized Locks an Object, and Excludes Only Code That Locks the Same One

Every Java object has a **monitor**: a lock that one thread at a time can own. `synchronized (x)
{ ... }` takes `x`'s monitor before the block runs, waiting while another thread owns it, and
gives it back when the block ends. A `synchronized` instance method does the same with `this`, and
a `static synchronized` method with the class's `Class` object. The keyword sits on the code, but
the lock belongs to an object.

Each of 8 threads increments an order counter, `static int ordersToday`, 100,000 times. Which of
these four versions reach 800,000?

```java
Together.run("no lock", 8, i -> {
    for (int k = 0; k < 100_000; k++) ordersToday++;
});
Together.run("new Object()", 8, i -> {
    for (int k = 0; k < 100_000; k++) {
        synchronized (new Object()) { ordersToday++; }
    }
});
Together.run("handler", 8, i -> {             // one handler per request, as a web framework does
    for (int k = 0; k < 100_000; k++) new CheckoutHandler().countOrder();
});
Together.run("ORDERS_LOCK", 8, i -> {         // private static final Object ORDERS_LOCK
    for (int k = 0; k < 100_000; k++) {
        synchronized (ORDERS_LOCK) { ordersToday++; }
    }
});
```

where the handler is

```java
static class CheckoutHandler {
    synchronized void countOrder() {
        ordersToday++;
    }
}
```

```text
no lock        8 threads done in 8 ms
  ordersToday = 480535
new Object()   8 threads done in 40 ms
  ordersToday = 702443
handler        8 threads done in 52 ms
  ordersToday = 620003
ORDERS_LOCK    8 threads done in 47 ms
  ordersToday = 800000
```

`ordersToday++` is three steps: read the field, add one, write it back. Two threads that read the
same value both write the same result, and one order is gone.

The two middle versions lock and still lose increments. `new Object()` gives every increment a
lock no other thread will ask for. The handler's `synchronized` method locks the handler, and
every increment has a new one, while the `static` field is shared by every handler. Only the last
version has all eight threads asking for the same object.

Choose the lock by the state it protects: one lock object per piece of shared state, and every
access to that state goes through it, reads included. Releasing a monitor also makes everything
the thread wrote before the release visible to the next thread that takes the same monitor; a read
that skips the lock has no such guarantee (unit 6.2.2).

### Spot the Problem: Someone Else Holds Your Monitor

A price cache protects its map with `synchronized` methods:

```java
static class PriceCache {
    private final Map<String, Long> prices = new HashMap<>();

    synchronized Long price(String sku) {
        return prices.get(sku);
    }

    synchronized void update(String sku, long cents) {
        prices.put(sku, cents);
    }
}
```

A metrics exporter, someone else's code, receives the cache and exports it every minute:

```java
static void exportSnapshot(Object cache) throws InterruptedException {
    synchronized (cache) {                        // "so the snapshot is consistent"
        Thread.sleep(300);                        // serializing and sending it
    }
}
```

A request asks for a price while an export runs, once on this cache and once on
`PrivateLockPriceCache`, a copy whose methods lock a `private final Object lock` instead:

```text
price() on PriceCache:            280 ms
price() on PrivateLockPriceCache: 0 ms
```

A `synchronized` method locks `this`, and any code that holds a reference to the object can lock
it too. The cache's latency now depends on code its author never sees, and if that code takes a
second lock, on a lock order nobody planned. Only the class itself can take a lock that is a
private field.

Lock a `private final` object, unless you mean outside code to share your lock, and then document
it. Never lock an object other code can reach by accident: a string literal is interned, one
object JVM-wide, so two unrelated classes that lock `"lock"` stall each other.

<!-- practice 1 -->

## Each Synchronized Method Is Atomic; a Check Followed by an Act Is Not

The flash sale's stock, every method `synchronized` on the same object:

```java
static class Stock {
    private int units;

    Stock(int units) {
        this.units = units;
    }

    synchronized int available() {
        return units;
    }

    synchronized void take(int n) {
        units -= n;
    }
}
```

64 buyers run the checkout until the stock is gone, pricing each order between the check and the
take:

```java
while (true) {
    if (stock.available() >= 1) {
        priceTheOrder();              // a few microseconds of work
        stock.take(1);
        sold.incrementAndGet();
    } else {
        break;
    }
}
```

Five runs, 500 units each:

```text
check, then act 64 threads done in 28 ms
  sold 509, units left -9
check, then act 64 threads done in 8 ms
  sold 505, units left -5
check, then act 64 threads done in 6 ms
  sold 500, units left 0
check, then act 64 threads done in 9 ms
  sold 501, units left -1
check, then act 64 threads done in 11 ms
  sold 503, units left -3
```

Each call was atomic, but between `available()` returning and `take(1)` starting the buyer holds
no lock. Several buyers saw the last unit as available, and each took it. Check and act in one
hold of the lock, inside the class:

```java
synchronized boolean tryTake(int n) {
    if (available() < n) return false;        // the thread already holds this monitor
    take(n);                                  // and takes it again here
    return true;
}
```

```text
tryTake        64 threads done in 25 ms
  sold 500, units left 0
```

`tryTake` calls `available()` and `take()`, which lock the monitor the thread already owns. That
works because a monitor is **reentrant**: it records its owner and a count, entering again adds
one, leaving takes one off, and other threads can enter only when the count is back at zero.
Without reentrancy, `tryTake` would wait for itself forever.

A service with 200 request threads can sell up to 199 units it doesn't have. A class shared
between threads should offer the combined operation (`tryTake`, or `putIfAbsent` and `compute` on
a `ConcurrentHashMap`) rather than leave callers to compose one from atomic parts.

## The Monitor Is Released However the Block Exits, Half-Done Changes Included

A ledger moves money from checking to savings, and refuses amounts over a daily limit:

```java
static class Ledger {
    private long checking = 1_000;
    private long savings = 0;

    synchronized void moveToSavings(long amount, long dailyLimit) {
        checking -= amount;
        if (amount > dailyLimit) throw new IllegalArgumentException("over the daily limit");
        savings += amount;
    }

    synchronized long total() {
        return checking + savings;
    }
}
```

One thread calls `moveToSavings(300, 200)`. When it has ended, another thread asks for the total:

```text
mover ended with java.lang.IllegalArgumentException: over the daily limit
total seen by another thread: 700
```

The second thread got the lock, so the monitor was released when the exception left the method.
That is guaranteed: a `synchronized` block is compiled with a handler that releases the monitor
and rethrows, and the JVM releases a `synchronized` method's monitor however the method
completes.

The release undoes nothing. The 300 left checking and never reached savings, and the next thread
saw a ledger 300 short, with no error anywhere but the mover's exception.

Inside a `synchronized` block, make every check that can throw before the first write, here the
limit check before `checking -= amount`. Where a write must come first, undo it in a `catch`
before rethrowing.

<!-- practice 5 -->

## Hold the Lock for the State, Not for the Slow Call

The flash sale's hotfix, scaled down: a checkout that holds the lock while the payment service
answers (50 ms here).

```java
static class WideShop {
    private int units = 100;

    synchronized boolean checkout(int qty, Payment payment) throws InterruptedException {
        if (units < qty) return false;
        if (!payment.charge(qty * 49_900L)) return false;
        units -= qty;
        return true;
    }
}
```

The second version holds the lock twice, briefly, and calls the payment service in between:

```java
static class Shop {
    private final Object lock = new Object();
    private int units = 100;

    boolean checkout(int qty, Payment payment) throws InterruptedException {
        synchronized (lock) {                     // check and reserve in one hold
            if (units < qty) return false;
            units -= qty;
        }
        boolean paid = false;
        try {
            paid = payment.charge(qty * 49_900L); // the slow call, with no lock held
        } finally {
            if (!paid) {
                synchronized (lock) { units += qty; }   // declined or threw: give them back
            }
        }
        return paid;
    }
}
```

Eight checkouts at once on each, then eight on the second shop with a payment service that
declines every card:

```text
pay inside     8 threads done in 410 ms
  units left 92
pay outside    8 threads done in 50 ms
  units left 92
declined       8 threads done in 50 ms
  units left 100
```

Both sell the right number of units. The first takes eight payment round trips in a row: each
checkout keeps the stock locked while it waits for the network, and every other checkout waits in
`BLOCKED` (unit 6.1.4). Its throughput is one checkout per round trip whatever the pool size.

The second shop treats the units being paid for as a state of their own. It reserves them in one
hold, pays with no lock held, and if the charge fails or throws, gives them back under the lock
from a `finally`, so that an exception can't keep them reserved.

### Spot the Problem: a Listener Called Under the Lock

A price feed calls its listeners with each new price while it holds its lock:

```java
void publishInside(long newCents) {
    synchronized (lock) {
        cents = newCents;
        for (LongConsumer l : listeners) l.accept(newCents);   // other code, lock held
    }
}

long current() {
    synchronized (lock) { return cents; }
}
```

Months later, someone adds a listener that records each price on an audit thread and waits until
it is recorded. The audit task reads the price through `current()`:

```java
feed.subscribe(cents -> {
    try {
        auditPool.submit(() -> feed.current()).get();
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
});
```

On the next price, `Together` reports, then each thread's state and the owner of its lock:

```text
publishInside  still running after 1 s: [publishInside-0]
  findDeadlockedThreads(): null
  publishInside-0  WAITING
  audit-1          BLOCKED  lock held by publishInside-0
```

The publisher holds the feed's lock and waits for the audit task, which waits for the feed's lock.
It is a deadlock, and the JVM's detector doesn't report it: one of the two waits is on a `Future`,
which has no owner for the detector to follow.

Neither author could see the cycle. Run code you don't control (listeners, callbacks, methods a
subclass can override, a call to another service) after the lock is released, with a copy of what
it needs: here a `CopyOnWriteArrayList`, which can be iterated without a lock; a plain list is
copied inside the block. With the loop moved below the `synchronized` block:

```text
publishOutside 1 threads done in 2 ms
```

<!-- practice 2 -->

## Two Locks Taken in Opposite Orders Deadlock; One Order for Every Pair Prevents It

A transfer locks both accounts, so that no other thread sees the money in neither of them:

```java
static void transfer(Account from, Account to, long amount) {
    synchronized (from) {
        synchronized (to) {
            from.balance -= amount;
            to.balance += amount;
        }
    }
}
```

One thread moves 1 from `a` to `b` 100,000 times, another moves 1 from `b` to `a` 100,000 times.
How many transfers complete?

```text
transfer       still running after 1 s: [transfer-0, transfer-1]
  deadlocked: transfer-0 waits for Examples$Account held by transfer-1
  deadlocked: transfer-1 waits for Examples$Account held by transfer-0
```

Within the first second, `transfer-0` held `a` and asked for `b` while `transfer-1` held `b` and
asked for `a`. A deadlock needs four things at once: locks that one thread at a time can hold, a
thread that holds one while it waits for another, no way to take a lock from its holder, and a
cycle of threads each waiting for the next. A monitor supplies the first and the third by design:
entering `synchronized` has no timeout, and an interrupt doesn't end the wait. Hold-and-wait
comes from nesting two blocks and goes away only with one coarser lock. Where two locks are
needed, remove the cycle by taking them in the same order in every thread:

```java
static void orderedTransfer(Account from, Account to, long amount) {
    Account first = from.id < to.id ? from : to;  // the lower id is locked first, always
    Account second = first == from ? to : from;
    synchronized (first) {
        synchronized (second) {
            from.balance -= amount;
            to.balance += amount;
        }
    }
}
```

```text
ordered        2 threads done in 16 ms
  a + b = 2000000
```

Both threads now ask for `a` first, and the one that gets it can always get `b`, because nobody
holds `b` while waiting for `a`. The key that sets the order has to be fixed for the life of the
object and different for every object: an id, not a balance that changes between two transfers,
and not `hashCode()`, which two different objects may share.

The order must hold for every piece of code that takes two of these locks, not only `transfer`:
an audit that locks the two accounts it compares in its own order deadlocks with the transfers
just the same. A deadlock also spreads: every thread that next needs `a` or `b` queues behind it
in `BLOCKED` until only a restart helps. Take a thread dump (`jcmd <pid> Thread.print`) first.

### Threads Can Also Stop Making Progress Without Blocking

In a **livelock**, the threads keep running and reacting to each other, and none gets anywhere.
In the module's `DeadlockPrevention.LivelockDemo`, two polite walkers meet in a hallway and each
round both step forward, see the other, and step back. With random back-off, a walker steps
forward after a conflict only on a coin toss:

```text
polite:         Outcome[walker1Done=false, walker2Done=false, walker1Rounds=25, walker2Rounds=25]
random backoff: Outcome[walker1Done=true, walker2Done=true, walker1Rounds=8, walker2Rounds=5]
```

In **starvation**, one thread keeps losing the lock to the others. Nothing promises which waiting
thread gets a monitor next, so a monitor can't prevent it. `new ReentrantLock(true)` (unit 6.3.1)
hands the lock to the longest waiter on `lock()`.

<!-- practice 3 -->

## wait() Belongs in a Loop Over Its Condition, and a Change Is Announced With notifyAll()

Unit 6.1.4 showed that `wait()` releases the monitor while the thread waits, and that a notified
thread is `BLOCKED` until it gets the monitor back. What does it find when it does? Two packers
wait for orders, and one order arrives:

```java
static class Orders {
    private final Deque<String> queue = new ArrayDeque<>();

    synchronized void add(String order) {
        queue.add(order);
        notifyAll();
    }

    synchronized String nextIf() throws InterruptedException {
        if (queue.isEmpty()) wait();
        return queue.removeFirst();
    }
}
```

```java
p1 = packer("packer-1", orders::nextIf);
p2 = packer("packer-2", orders::nextIf);
// once both packers are waiting:
orders.add("order-7001");
```

```text
packer-2 ended with java.util.NoSuchElementException
packer-1 packs order-7001
```

`notifyAll()` woke both packers, and each had to take the monitor back before returning from
`wait()`. `packer-1` got it first and took the order. `packer-2` got it next and returned into
code that assumed the queue wasn't empty (each prints after leaving the monitor, so the lines can
come in either order). A return from `wait()` doesn't mean the condition is true, only that
somebody notified, or the timeout ran out, or nothing happened at all: the javadoc of
`Object.wait` warns of spurious wakeups and says to wait in a loop. With
`while (queue.isEmpty()) wait();` the second packer checks again and goes back to waiting:

```text
packer-1 packs order-7001
packer-2 WAITING
```

The opposite mistake is to wake too few threads. Packers now wait for orders of their own region,
`next("EU")` and `next("US")`, each in a loop over "is there an order for my region?". The EU
packer starts waiting first, then the US packer, and a US order arrives, announced with `notify()`
in one run and `notifyAll()` in the other:

```java
synchronized void add(String order) {
    queue.add(order);
    if (all) notifyAll(); else notify();
}
```

```text
notify():    us-packer WAITING, queued [US-1001]
notifyAll(): us-packer TERMINATED, queued []
```

`notify()` wakes one waiting thread, and its javadoc says the choice is arbitrary. Here it woke
the EU packer, which found nothing for EU and waited again. The notification was spent, and the US
order sits in the queue until some other order happens to wake the right packer. With
`notifyAll()` both woke, both checked, and the US packer took its order.

`notify()` is enough only when every waiter waits for the same condition and any one of them can
handle the change. Otherwise, and whenever you aren't sure, use `notifyAll()`: the cost is a few
threads that wake up, check, and wait again. A waiting thread answers an interrupt with
`InterruptedException` (unit 6.1.3), so the loop needs no exit of its own for shutdown.

<!-- practice 4 -->

## Summary

- **`synchronized` locks an object, and excludes only code that locks the same one.** Use one
  `private final` lock per piece of state; a monitor others can reach lets them stall yours.
- **Each synchronized method is atomic; a check followed by an act is not.** Check and act in one
  hold; reentrancy makes it easy to write.
- **The monitor is released however the block exits, half-done changes included.** Check before
  the first write.
- **Hold the lock for the state, not for the slow call.** Reserve, call, then settle; call
  listeners after the lock is released.
- **Two locks taken in opposite orders deadlock.** One fixed, unique order for every place that
  takes two of the locks removes the cycle.
- **`wait()` belongs in a loop, and changes are announced with `notifyAll()`.** A woken thread
  re-checks its condition; `notify()` can wake a thread that can't use the change.

## Reference

**What each form locks:**

| Form | Monitor taken |
|---|---|
| `synchronized` instance method | `this` |
| `static synchronized` method | the class's `Class` object (`Thread.holdsLock(X.class)` is `true` inside a `static synchronized` method of `X`) |
| `synchronized (expr) { ... }` | the object `expr` evaluates to; `null` throws `NullPointerException` |

**Monitor behaviour (Java 21):**

| Situation | What happens |
|---|---|
| a thread enters a monitor it already holds | it enters (reentrant), as often as it nests |
| the block or method ends with an exception | the monitor is released; writes made before the throw stay |
| a thread waiting to enter is interrupted | it keeps waiting, `BLOCKED`, with its interrupt flag set |
| `wait()`, `notify()`, `notifyAll()` without holding that monitor | `IllegalMonitorStateException` |
| `wait()` while holding two monitors | releases only the one it waits on; the other stays held |
| `Thread.sleep` inside `synchronized` | keeps the monitor (unit 6.1.4) |
| a string literal as the lock | one object JVM-wide, shared with every class that locks the same literal |
| a boxed small number as the lock | `Integer.valueOf(7) == Integer.valueOf(7)`: shared the same way |
| a virtual thread blocks inside `synchronized` | it pins its carrier thread; `-Djdk.tracePinnedThreads=short` prints `reason:MONITOR` |

**Finding a deadlock:**

| Tool | What it reports |
|---|---|
| `ThreadMXBean.findDeadlockedThreads()` | thread ids in a cycle of monitors or ownable synchronizers (such as `ReentrantLock`); `null` if none |
| `jcmd <pid> Thread.print` | `Found one Java-level deadlock:`, each thread, the monitor it waits for and who holds it |
| a cycle through a `Future`, a latch or a queue | not reported by any of the above; read the stacks in the thread dump |

## Challenges

These have no single answer.

1. A cache has a `getOrLoad(key)` that loads from the database on a miss. Where does the load go
   relative to the lock, and what happens when 50 threads miss the same key at once with your
   choice?
2. Find every place in a codebase you know that takes two locks at once. What order do they use,
   and how would you make sure the next change keeps it (a helper that takes both, a review rule,
   one coarser lock)?
3. Run the order counter with `ORDERS_LOCK`, an `AtomicInteger` and a `LongAdder`, with 8 and with
   64 threads. When does the lock stop being the right tool?

## Design Note: Why Every Object Is a Lock

Java 1.0 gave every object a monitor: a lock and a single wait queue built into the object, with
`synchronized` to take the lock and `wait`/`notify` to use the queue. Protecting a class became one
keyword, and the lock can't leak, because the block structure releases it on every path.

The costs come from the same choice: anyone can lock your object, one wait queue makes
`notifyAll()` wake every condition's waiters, and entering has no timeout, so a deadlock on
monitors is permanent. Java 5 added `java.util.concurrent.locks` for those cases (unit 6.3.1): a
lock with a timeout, several conditions per lock, and an optional fair order. The monitor remains
the simplest correct choice for a short critical section that doesn't block.

## Code Examples

- Implementation: [SynchronizedBlocks.java](src/main/java/com/github/msorkhpar/claudejavatutor/synchronization/SynchronizedBlocks.java)
- Test: [SynchronizedBlocksTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/synchronization/SynchronizedBlocksTest.java)
