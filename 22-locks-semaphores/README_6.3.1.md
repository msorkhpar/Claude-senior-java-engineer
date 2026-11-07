# 6.3.1. Reentrant Locks

At 02:10 the stock service stops answering. The thread dump shows forty request threads `WAITING`
for the same `ReentrantLock`, and the lock's `toString()` names its owner:
`[Locked by thread pool-1-thread-1]`. That thread sits idle in its pool, in
`LinkedBlockingQueue.take()`, and the log says nothing about it.

With `synchronized` (unit 6.2.1) this can't happen. `ReentrantLock`, from
`java.util.concurrent.locks`, guards a critical section with the same memory visibility and adds a
wait you can give up on, several conditions (wait sets) per lock, an optional fair queue, and
methods that report who holds it. You write the locking and unlocking yourself, as method calls.
Every output on this page comes from Java 21.

## Nothing Releases a ReentrantLock but unlock(), So Unlock in finally

The stock service reserves like this:

```java
static final ReentrantLock lock = new ReentrantLock();

static void reserve(String sku, int qty) {
    lock.lock();
    int left = stock.getOrDefault(sku, 0);
    if (left < qty) throw new IllegalStateException("only " + left + " of " + sku + " left");
    stock.put(sku, left - qty);
    lock.unlock();
}
```

A task on a one-thread pool asks for 5 mugs when 3 are left; the exception goes into a `Future`
that nobody reads. Then we print the lock and the pool's thread, and
another thread calls `reserve` for one mug. We repeat this with the body in a `synchronized` block,
and last, a thread named `nightly-import` takes a lock and ends:

```text
java.util.concurrent.locks.ReentrantLock@5acf9800[Locked by thread pool-1-thread-1]
pool-1-thread-1 is WAITING in LinkedBlockingQueue.take()
another thread gets the lock within 1 s: false
synchronized, after the same exception: another thread gets in within 1 s: true
nightly-import alive: false, java.util.concurrent.locks.ReentrantLock@65b54208[Locked by thread nightly-import]
```

The lock stayed with the pool thread after its task ended, and every later `reserve` waits for an
`unlock()` that will never run. Even the terminated `nightly-import` still owns its lock.

`synchronized` releases the monitor on every way out of the block, because the compiler writes the
release. A `ReentrantLock` is an ordinary object that records an owner thread and a
count, and nothing calls `unlock()` for you: not when an exception passes, not when the task ends,
not when the thread dies. So the release goes in a `finally`:

```java
lock.lock();
try {
    int left = stock.getOrDefault(sku, 0);
    if (left < qty) throw new IllegalStateException("only " + left + " of " + sku + " left");
    stock.put(sku, left - qty);
} finally {
    lock.unlock();
}
```

```text
the same failure, with the finally: java.util.concurrent.locks.ReentrantLock@6b884d57[Unlocked]
```

`lock()` goes before the `try`, so that the `finally` runs only once the lock is held.

## The Lock Counts Holds: One unlock() per lock(), by the Thread That Holds It

A price import locks once for the whole batch and calls `apply` for each line, which locks the
same lock again:

```java
static void apply(String sku, int cents) {
    lock.lock();
    try {
        System.out.println("  apply " + sku + ": hold count " + lock.getHoldCount());
        prices.put(sku, cents);
    } finally {
        lock.unlock();
    }
}

static void applyAll(Map<String, Integer> batch) {
    lock.lock();
    try {
        System.out.println("applyAll: hold count " + lock.getHoldCount());
        batch.forEach(Holds::apply);
        System.out.println("applyAll again: hold count " + lock.getHoldCount());
    } finally {
        lock.unlock();
    }
    System.out.println("after applyAll: hold count " + lock.getHoldCount() + ", locked " + lock.isLocked());
}
```

```text
applyAll: hold count 1
  apply mug-blue: hold count 2
  apply mug-red: hold count 2
applyAll again: hold count 1
after applyAll: hold count 0, locked false
```

The thread that holds the lock gets it again at once, and the lock counts how many times: that is
what *reentrant* means. Without it, `applyAll` would wait forever for a lock it holds itself. The
lock is free when the count is back to zero. `synchronized` is reentrant in the same way.

### Spot the Problem: One Path Locks Twice

```java
static void applyWithDiscount(String sku, int cents, boolean discounted) {
    lock.lock();
    try {
        if (discounted) {
            lock.lock();                 // "make sure we hold it" before the discount
            cents = cents * 90 / 100;
        }
        prices.put(sku, cents);
    } finally {
        lock.unlock();
    }
}
```

After each call, the caller's hold count and whether another thread can take the lock within a
second:

```text
plain change:      hold count 0, other thread gets it: true
discounted change: hold count 1, other thread gets it: false
same thread again: hold count 1 (it can always relock)
```

The discounted path locks twice and unlocks once, so its thread keeps one hold for good and every
other thread waits. A test that makes all its calls from one thread passes, because the owner can
always relock. Pair each `lock()` with its own `unlock()` in a `finally` of the same method.

Only the thread that holds the lock can release it:

```text
unlock() from another thread: java.lang.IllegalMonitorStateException
unlock() once too often: java.lang.IllegalMonitorStateException
```

So a lock can't be taken on a request thread and released by a callback on another thread. A
permit that one thread takes and another gives back is a semaphore (unit 6.3.2).

## lock() Waits Until It Gets the Lock; tryLock and lockInterruptibly Can Give Up

A thread called `price-refresh` holds the lock while it waits for a slow upstream service. Three
threads ask for the lock in three ways, and each is interrupted while it waits:

```java
waiter("lock()", ReentrantLock::lock);
waiter("lockInterruptibly()", ReentrantLock::lockInterruptibly);
waiter("tryLock(10, SECONDS)", l -> l.tryLock(10, TimeUnit.SECONDS));
```

```text
lock()                     interrupted, 500 ms later: still waiting (WAITING)
  lockInterruptibly(): InterruptedException after 0 ms
lockInterruptibly()        interrupted, 500 ms later: gone
  tryLock(10, SECONDS): InterruptedException after 0 ms
tryLock(10, SECONDS)       interrupted, 500 ms later: gone
```

Then the main thread tries without waiting, and with a budget of 200 ms:

```text
tryLock():          false after 0 ms
tryLock(200 ms):    false after 200 ms
```

("gone" means the thread has ended.) `lock()` waits as long as it takes, like entering
`synchronized`; an interrupt only sets the flag (unit 6.1.3), so a deploy that calls
`shutdownNow()` can't move a thread out of it. `lockInterruptibly()` answers an interrupt with
`InterruptedException`, without taking the lock. `tryLock(time, unit)` does the same, and also
returns `false` when its time is up. `tryLock()` doesn't wait at all.

A request with a two-second budget uses a timed `tryLock` and answers "busy" when it doesn't get
the lock. A worker that a shutdown must be able to stop uses `lockInterruptibly()`. `lock()` fits
waits that are always short.

Here the code ignores the result of `tryLock`:

```java
lock.tryLock(200, TimeUnit.MILLISECONDS);   // result ignored
try {
    stock[0]--;
} finally {
    lock.unlock();
}
```

```text
result ignored: stock changed to 4, then unlock() threw java.lang.IllegalMonitorStateException
```

The critical section ran without the lock, beside the thread that held it, and then `unlock()`
threw in place of whatever the method was going to return. Test the result before the `try`:

```java
if (!lock.tryLock(200, TimeUnit.MILLISECONDS)) return Answer.BUSY;
try {
    // ...
    return Answer.DONE;
} finally {
    lock.unlock();
}
```

```text
checked, while price-refresh holds the lock: BUSY
```

`lockInterruptibly()` also goes before the `try`. Inside it, an interrupt while waiting runs the
`finally`, and `unlock()` throws `IllegalMonitorStateException` in place of the
`InterruptedException`:

```text
lockInterruptibly() inside try, interrupted: the caller sees java.lang.IllegalMonitorStateException
```

If the thread already holds the lock from an outer call, the caller gets its
`InterruptedException`, but the stray `unlock()` silently takes away the outer hold:

```text
same, with an outer hold: InterruptedException, and the outer hold is gone: hold count 0
```

<!-- practice 1 -->

<!-- practice 2 -->

## A Condition Gives Up the Lock While It Waits, So Check Again After await()

A `Condition` is a wait set that belongs to a lock, like `wait()` and `notify()` on a monitor
(unit 6.2.1), and one lock can have several. A print queue keeps one for printers waiting
for a job and one for clerks waiting for room:

```java
final ReentrantLock lock = new ReentrantLock();
final Condition notEmpty = lock.newCondition();
final Condition notFull = lock.newCondition();
final Deque<String> jobs = new ArrayDeque<>();
final int capacity = 100;

void put(String job) throws InterruptedException {
    lock.lock();
    try {
        while (jobs.size() == capacity) notFull.await();
        jobs.addLast(job);
        notEmpty.signal();
    } finally {
        lock.unlock();
    }
}
```

`await()` must be called with the lock held. It releases all of the thread's holds, not just one,
parks the thread, and takes them all back before it returns. Here thread A awaits from a nested call while another
thread tries to take the lock:

```text
A awaits with hold count 2; another thread's tryLock(1 s): true
A's hold count when await() returned: 2
```

`signal()` wakes one thread waiting on that condition, `signalAll()` all of them. All three throw
`IllegalMonitorStateException` in a thread that doesn't hold the lock.

### Spot the Problem: if Around await()

A printer takes the next job like this:

```java
String takeIf() throws InterruptedException {
    lock.lock();
    try {
        if (jobs.isEmpty()) notEmpty.await();
        String job = jobs.pollFirst();
        notFull.signal();
        return job;
    } finally {
        lock.unlock();
    }
}
```

`printer-2` waits on an empty queue. A clerk puts `invoice-118.pdf` and signals. Before `printer-2`
has the lock back, `printer-1` arrives and takes the job. The program forces that order, with `if`
and then with `while`:

```text
if   : printer-1 took invoice-118.pdf
if   : printer-2 got null
while: printer-1 took invoice-118.pdf
while: printer-2 got (still waiting)
```

A signal says the state changed, not that it is what you wanted. Before the woken thread holds the
lock again, any thread can take the lock and change the state, whatever the lock's fairness
policy. The javadoc of `Condition` also allows a thread to wake with no signal at all, a *spurious
wakeup*. With `while`, the woken thread checks again and goes back to waiting. With `if`, it
carries on with `null`, and a printer sends that to the device.

### Signal the Condition Its Waiters Wait On

`put` signals `notEmpty` and `takeIf` signals `notFull`, so each change wakes the side that can use
it. Signal the other one and the waiter isn't woken. Here
a job is added and `notFull` is signalled while a printer waits on `notEmpty`:

```text
signalled notFull: printer alive true, jobs waiting [label-7.pdf]
```

With a single condition for both sides, `signal()` can wake a clerk when a printer was needed;
the clerk finds the queue still full and waits again, and the signal is lost. Two clerks and two
printers sharing one condition, a queue of capacity one, 2,000 items per thread, twenty runs each:

```text
one condition, signal()   : 20 of 20 runs stalled
one condition, signalAll(): 0 of 20 runs stalled
```

A single condition works only with `signalAll()`, which wakes every waiter so that one of them can
go on.

<!-- practice 3 -->

## A Non-Fair Lock Lets a Newcomer Go First; a Fair Lock Queues, and Costs Throughput

A sync job holds the lock while it syncs a row, unlocks, and locks again for the next row, while a
report thread waits in `lock()`. Three runs with the default lock, then three with
`new ReentrantLock(true)`:

```text
non-fair: the sync job relocked 166 times before the waiting report thread got in
non-fair: the sync job relocked 2,485 times before the waiting report thread got in
non-fair: the sync job relocked 1,593 times before the waiting report thread got in
fair:     the sync job relocked 1 times before the waiting report thread got in
fair:     the sync job relocked 1 times before the waiting report thread got in
fair:     the sync job relocked 1 times before the waiting report thread got in
```

Waking the parked report thread takes time, and the default, non-fair lock doesn't hold the lock
for it: a thread that asks in the meantime, here the sync job, takes it at once. This *barging* is
why the default is fast: the lock goes to a running thread instead of staying idle. A fair lock
hands it to the thread that has waited longest, so every handover costs a wake-up; the fair count
is 1 because the sync job's first relock waited for the report thread. Four threads taking the same lock in a tight loop for one second:

```text
non-fair:  76,157,742 acquisitions in 1 s
fair:       7,601,130 acquisitions in 1 s
```

Other runs (not shown) ranged from about seven to more than thirty times fewer. With non-fair, a
waiter has no bound on how many newcomers go first; behind threads that hold the lock for seconds
and keep coming back, it can wait for minutes.

Keep the default, and keep the time the lock is held short. Use a fair lock when the wait of the
unluckiest thread is what you are measured on. Even then, the untimed `tryLock()` takes a free lock
while others wait, where `tryLock(0, TimeUnit.SECONDS)` respects the queue. One thread queued on a fair lock that was just released, a hundred times:

```text
fair lock, lock just released, a thread still queued: tryLock() went ahead of it 83/100, tryLock(0, SECONDS) went ahead of it 0/100
```

In the other 17 runs, the queued thread got the lock before `tryLock()` was called.

<!-- practice 5 -->

## A Read-Write Lock Lets Readers Share, but a Reader Can't Upgrade

A product catalog is read thousands of times for every change. A `ReentrantReadWriteLock` holds two locks:
any number of threads can hold its read lock together, and its write lock excludes all other
readers and writers. Two threads read while a writer tries:

```text
readers holding the read lock together: 2
a writer meanwhile: tryLock(200 ms) = false after 200 ms
```

### Spot the Problem: Loading on a Miss

The catalog loads a missing entry when a read misses it:

```java
rw.readLock().lock();
try {
    if (catalog.get("sku-5120") == null) {
        rw.writeLock().lock();                    // load the missing entry
        try {
            catalog.put("sku-5120", "Saucer, white");
        } finally {
            rw.writeLock().unlock();
        }
    }
} finally {
    rw.readLock().unlock();
}
```

```text
read lock held, then writeLock().lock(): 1 s later WAITING, read holds 1, write locked false
any other writer: tryLock(200 ms) = false
a new reader: tryLock(200 ms) = false
```

The write lock waits until every reader has left, including the thread asking for it, so it waits
forever, and nothing detects it. Other writers can't get in, and in this implementation neither
can new readers that call `readLock().lock()` or a timed `tryLock`: a reader that arrives while a
writer waits at the head of the queue waits behind it, so that a stream of readers can't shut a
writer out. Here one miss stops the whole catalog.

There is no upgrade from read to write. Release the read lock, take the write lock, and look again
before loading, because another thread may have loaded the entry in the gap. The other direction
works: a thread that holds the write lock can take the read lock and then release the write lock,
a *downgrade* with no gap for a writer:

```text
downgrade: write locked false, read holds 1
```

A read-write lock pays off when reads are long or many and writes are rare. It does more
bookkeeping than a plain lock, so for short reads, measure first.

<!-- practice 4 -->

## Summary

- **Nothing releases a `ReentrantLock` but `unlock()`.** Lock before the `try`, unlock in the
  `finally`.
- **The lock counts holds per thread.** One `unlock()` per `lock()`; only the owner can unlock.
- **`lock()` waits through interrupts.** A timed `tryLock`, its result checked, gives a budget;
  `lockInterruptibly()` lets a shutdown stop the waiter.
- **A `Condition` gives up the lock while it waits.** Wait in a `while` loop, and signal the
  condition the right side waits on.
- **A non-fair lock lets newcomers go first.** That is its throughput; a fair lock serves waiters
  in order at several times the cost.
- **A read-write lock lets readers share, but doesn't upgrade.** Release the read lock before
  taking the write lock.

## Reference

**Taking a `ReentrantLock` (Java 21):**

| Call | If another thread holds the lock | On interrupt while waiting |
|---|---|---|
| `lock()` | waits until it gets it | keeps waiting; returns with the flag set |
| `lockInterruptibly()` | waits until it gets it | throws `InterruptedException`, doesn't take the lock |
| `tryLock()` | returns `false` at once | doesn't wait |
| `tryLock(time, unit)` | waits up to `time`, then returns `false` | throws `InterruptedException` |
| `unlock()` | from a thread that doesn't hold it: `IllegalMonitorStateException` | — |

**What the lock reports:**

| Method | Answers |
|---|---|
| `getHoldCount()` | holds of the **current** thread (0 if it doesn't hold the lock) |
| `isLocked()` | whether any thread holds it |
| `toString()` | `[Unlocked]` or `[Locked by thread <name>]` |

## Challenges

These have no single answer.

1. A cache refresh holds the lock for 800 ms every minute, and requests hold it for a few
   microseconds. Which acquisition method does each side use, and what does a request answer when
   it can't get the lock in time?
2. `applyAll` holds the lock for a whole batch so that readers never see half a batch. A batch of
   50,000 lines takes two seconds. What do readers see during those two seconds, and how would you
   keep both the consistency and the response times?
3. Measure the print queue with one condition and `signalAll()` against two conditions and
   `signal()`: how many threads wake up per job with 50 printers and 2 clerks?

## Design Note: Why Java Kept synchronized

Java 5 added `ReentrantLock` beside `synchronized`, because a block of the language can't offer a
lock wait with a timeout, a lock wait that answers an interrupt, an unlock in a different method
from the lock, several conditions per lock, or a choice of fairness. Each of those is a method
call, and a forgotten call is a bug the compiler doesn't see; `synchronized` can't leak a monitor,
unlock twice or be released by the wrong thread.

For a short critical section with none of those needs, `synchronized` is the safer choice and
reads better. When you need one of them, use `ReentrantLock`, and write the release yourself in
every method that locks.

## Code Examples

- Source: [ReentrantLockBasics.java](src/main/java/com/github/msorkhpar/claudejavatutor/lockssemaphores/ReentrantLockBasics.java)
- Source: [ConditionUsage.java](src/main/java/com/github/msorkhpar/claudejavatutor/lockssemaphores/ConditionUsage.java)
- Source: [ReadWriteLockUsage.java](src/main/java/com/github/msorkhpar/claudejavatutor/lockssemaphores/ReadWriteLockUsage.java)
- Test: [ReentrantLockBasicsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/lockssemaphores/ReentrantLockBasicsTest.java)
- Test: [ConditionUsageTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/lockssemaphores/ConditionUsageTest.java)
- Test: [ReadWriteLockUsageTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/lockssemaphores/ReadWriteLockUsageTest.java)
