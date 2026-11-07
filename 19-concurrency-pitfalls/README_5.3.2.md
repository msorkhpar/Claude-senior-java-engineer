# 5.3.2. Atomicity Violations and Ensuring Atomic Operations

A launch campaign gives a voucher to the first 1,000 people who sign up. The count is an
`AtomicInteger`, and the voucher email goes out from inside the update, so that a customer is
emailed only when the count really moves:

```java
claimed.updateAndGet(n -> {
    if (n >= CAP) return n;
    emails.incrementAndGet();          // sendVoucherEmail(customer, n + 1)
    return n + 1;
});
```

Sixteen threads take the launch burst. Afterwards the counter reads exactly 1,000, and the mail
log shows 3,520 voucher emails. Every operation in the code is atomic.

An operation is atomic when no other thread can see it half done or act in its middle. A
**compound action** (read, decide, write; or change two values that must agree) is atomic only if
one operation does all of it. Every output below is from Java 21 on one machine
(`availableProcessors()` printed 20); timings come from plain loops, not a JMH benchmark.

## An Atomic Update Retries Until Its compareAndSet Wins, So Its Function Can Run Many Times

`updateAndGet` takes no lock; its JDK 21 source is a loop:

```java
public final int updateAndGet(IntUnaryOperator updateFunction) {
    int prev = get(), next = 0;
    for (boolean haveNext = false;;) {
        if (!haveNext)
            next = updateFunction.applyAsInt(prev);
        if (weakCompareAndSetVolatile(prev, next))
            return next;
        haveNext = (prev == (prev = get()));
    }
}
```

It writes only if the value is still the one it read; otherwise it reads again and re-runs the
function. So how many times does the function run when 8 threads each call
`updateAndGet(n -> n + 1)` a million times?

```text
updateAndGet: counter = 8,000,000, function ran 22,139,089 times (14,139,089 re-runs)
```

The function ran almost three times per update. For `n + 1` that only costs CPU; the voucher
function sends an email from every losing attempt that was still below the cap. Three runs of the campaign, each followed by a version that decides inside the
function and acts after it, on the value it returned:

```java
int before = claimed.getAndUpdate(n -> n < CAP ? n + 1 : n);
if (before < CAP) emails.incrementAndGet(); // sendVoucherEmail(customer, before + 1)
```

```text
email inside the function: vouchers = 1000, emails = 3520
email after getAndUpdate:  vouchers = 1000, emails = 1000
email inside the function: vouchers = 1000, emails = 1050
email after getAndUpdate:  vouchers = 1000, emails = 1000
email inside the function: vouchers = 1000, emails = 1000
email after getAndUpdate:  vouchers = 1000, emails = 1000
```

The extra emails depend on how often threads collided, so a test with one thread, or a quiet day,
sends exactly 1,000. The javadoc of `getAndUpdate`, `accumulateAndGet` and `LongAccumulator` says
the function "should be side-effect-free, since it may be re-applied when attempted updates fail
due to contention among threads". Keep the function a pure computation of the next value, and do
the side effect once, after the update, from the value it returned. 

Write the loop yourself when the caller needs more than the new value back (did this caller win,
which number did it get) or when the decision calls out, to a quota service, say: read, decide,
`compareAndSet`, and on failure go back to the read. Every retry decides again on the new value;
a decision kept from the first attempt is a check-then-act again (unit 5.3.1).

<!-- practice 1 -->

## LongAdder Counts Faster Under Contention Because No Single Variable Holds the Total

Every `incrementAndGet` on an `AtomicLong` is an atomic add to one memory location, so threads
that update it at the same time queue up for that location's cache line. `LongAdder` spreads
contending threads over several cells and adds them up when asked. Ten million increments, split
over the threads:

```text
threads  AtomicLong  LongAdder   (ms for 10,000,000 increments, best of 5)
      1          48         43
      2          77         22
      4          83         16
      8          91          8
     16         105          7
```

With one thread they cost the same, as the javadoc says ("Under low update contention, the two
classes have similar characteristics"). Neither lost an update: after the threads ended, every
run's total was exactly 10,000,000.

The price is that no variable holds the total. `sum()` takes no lock: it reads the cells one
after another while they change, and its javadoc says the result is "NOT an atomic snapshot". A queue-depth gauge
shows what that means: 4 producers add 1 and then enqueue a job, 4 consumers take a job and then
subtract 1. Every subtraction follows its addition, so the depth is never below zero. A monitor
read it 20 million times:

```text
LongAdder.sum()   reads below 0: 123  lowest: -149
AtomicLong.get()  reads below 0: 0  lowest: 0
```

Two more runs gave 15 and 110 negative reads. `sum()` read a consumer's cell after its
subtraction and a producer's cell before the matching addition, and returned a value the gauge
never held. An alert on "depth below 0" fires; one on "depth above N" can fire late or early.

`LongAdder` also has no `compareAndSet`, `incrementAndGet` or `getAndIncrement`. It can't hand out the next order number or stop at a cap. Use it for statistics
that are read now and then; use `AtomicLong` when the value takes part in a decision.

## compute and merge Are Atomic for One Key, Not for an Action Across Keys

`ConcurrentHashMap.compute` and `merge` lock the key's bin while the function runs (unit 6.5.2),
and the javadoc of `compute` promises "The supplied function is invoked exactly once per
invocation of this method":

```text
ConcurrentHashMap.compute:      value 8,000,000, function ran 8,000,000 times
AtomicReference.updateAndGet:   value 8,000,000, function ran 34,569,375 times
```

In exchange, other updates to that bin wait while the function runs, so the javadoc asks for a
computation that is "short and simple" and must not modify the map.

The atomicity covers one call on one key. A show has 200 seats, kept as two keys, and a booking
moves one seat from free to sold with two `compute` calls:

```java
void book(Runnable between) {
    seats.compute("show-9:free", (k, n) -> n - 1);
    between.run();                                   // the booking thread is descheduled here
    seats.compute("show-9:sold", (k, n) -> n + 1);
}
```

A latch holds the booking at `between` while another thread adds up the two keys. The fix keeps
both numbers in one immutable value under one key, so the only pause point is before its one
`compute`:

```java
record Show(int free, int sold) {
    Show book() { return new Show(free - 1, sold + 1); }
}

void book(Runnable between) {
    between.run();                                   // a single compute has no middle to pause in
    shows.compute("show-9", (k, s) -> s.book());
}
```

```text
two keys: seats during a booking: 199
two keys: seats after the booking:  200
one key : seats during a booking: 200
one key : seats after the booking:  200
```

Each `compute` was atomic, and a reader still saw 199 seats. A capacity check that runs in that
window admits or refuses on a total that never existed; a scan of the whole map does the same
(unit 6.5.3). What must stay consistent goes under one key, so that one `compute` changes all of
it. If the values must live under different keys, every writer and every reader takes the same
lock, and the map's own concurrency no longer helps.

<!-- practice 2 -->

## Values That Must Agree Need One Lock or One Immutable Holder Replaced by compareAndSet

An account may go below zero only down to its overdraft limit: `balance >= -limit`. Both fields
are `AtomicLong`s, and both operations check the rule against the other field. `pause.here()` is
the example's hook for holding a thread after its check, on its first attempt only:

```java
boolean withdraw(long amount, Pause pause) {
    long after = balance.get() - amount;
    if (after < -limit.get()) return false;          // check against the other field
    pause.here();
    balance.set(after);
    return true;
}

boolean lowerLimit(long newLimit, Pause pause) {
    if (balance.get() < -newLimit) return false;
    pause.here();
    limit.set(newLimit);
    return true;
}
```

From balance 0 and limit 100, a withdrawal of 80 is held after its check while another thread
lowers the limit to 50. Each check passes on its own; what does the account end up with?

```text
two atomics: withdraw 80 -> true, lower limit to 50 -> true, now balance -80, limit 50
```

Replacing `set` with `compareAndSet` on each field doesn't help: the withdrawal's CAS on `balance`
succeeds, because the limit changed and the balance didn't. A CAS protects one variable; the rule
reads two.

So make them one variable: an immutable `Account` record holds both and refuses to exist in a
state that breaks the rule, and an `AtomicReference` holds the current record. An update builds
the next record from the current one and swaps it in with `compareAndSet`, retrying from the read:

```java
record Account(long balance, long limit) {
    Account {
        if (balance < -limit) throw new IllegalStateException("balance " + balance + " below -" + limit);
    }
}

private boolean update(java.util.function.UnaryOperator<Account> change, Pause pause) {
    while (true) {
        Account current = account.get();
        Account next;
        try {
            next = change.apply(current);            // the record's constructor checks the rule
        } catch (IllegalStateException e) {
            return false;
        }
        pause.here();
        if (account.compareAndSet(current, next)) return true;
    }
}

boolean withdraw(long amount, Pause pause) {
    return update(a -> new Account(a.balance() - amount, a.limit()), pause);
}
```

```text
one holder : withdraw 80 -> false, lower limit to 50 -> true, now balance 0, limit 50
```

The withdrawal's swap failed because the record had been replaced; its retry built a record with
balance -80 against limit 50, and the constructor refused it. A `synchronized` method for every
read and write of both fields (unit 6.2.1) gives the same guarantee and is simpler when the
critical section is short. The holder lets readers take one consistent pair without a lock, and
it costs an allocation per update and retries under contention. Unit 5.3.3 covers the
single-writer case of this swap, where no compare-and-set is needed.

<!-- practice 3 -->

## Summary

- **An atomic class's update is a compare-and-set retried until it wins.** The function you pass
  can run several times per call; side effects belong after the update, decided from its result.
- **`LongAdder` scales a counter by splitting it.** It is faster under contention, `sum()` is not a
  snapshot, and it has no compare-and-set to make decisions with.
- **`compute` and `merge` are atomic for one key.** Two calls, on two keys or on one, are two
  steps that a reader can fall between; keep what must agree under one key.
- **Values that must agree need one lock or one immutable holder.** Atomic fields keep each value
  whole and let the rule between them break.

## Reference

**What makes which compound action atomic (Java 21):**

| Tool | Atomic unit | Function re-run under contention | Notes |
|---|---|---|---|
| `AtomicInteger`/`AtomicLong`/`AtomicReference` `updateAndGet`, `getAndUpdate`, `accumulateAndGet` | one variable | yes ("may be re-applied") | lock-free; retries cost CPU |
| hand-written `get` + `compareAndSet` loop | one variable | the loop body re-runs on every failure | decision retaken on the current value |
| `LongAdder` `add`/`increment` | one increment | no function | `sum()` "is NOT an atomic snapshot"; `reset()` and `sumThenReset()` are exact only with no concurrent updates |
| `LongAccumulator.accumulate` | one update | yes ("may be re-applied"); order of accumulation not guaranteed | for predictable results, the function should be associative and commutative |
| `ConcurrentHashMap.compute` | one key | no: "invoked exactly once per invocation" | other updates to the bin wait while it runs |
| `ConcurrentHashMap.merge` | one key | the javadoc says the whole call "is performed atomically" and doesn't promise one run | function must not update other mappings |
| `synchronized` / a `Lock` on every access | whatever the block covers | no | readers must lock too |
| immutable holder in an `AtomicReference` | every field of the holder | yes, the build of the next holder | one allocation per update |

## Challenges

1. The voucher fix sends the email after `getAndUpdate`. If the mail server is down, the voucher
   is counted and never sent. What would you record, and where, so that a retry sends each voucher
   exactly once?
2. Measure the `AtomicLong`/`LongAdder` table on your own machine with JMH. At how many threads do
   they cross, and does that change with work between the increments?
3. A rate limiter allows 100 requests per second per tenant and is checked on every request.
   Would you keep the counts in a `ConcurrentHashMap<String, LongAdder>`, a
   `ConcurrentHashMap<String, AtomicLong>` or one `compute` per request? Which wrong answers would
   each one let through?

## Design Note: Why the Atomic Classes Retry Instead of Lock

A compare-and-set never blocks: a thread that loses tries again, and some thread always wins, so a
descheduled thread can't hold the others up the way a lock holder can. A lock gives the opposite
trade: the work runs once, and everyone else waits for the holder, however long it is
descheduled. Which to use depends first on whether the work may run twice, and then on how long
it runs.

## Code Examples

- Source: [AtomicityViolation.java](src/main/java/com/github/msorkhpar/claudejavatutor/concurrencypitfalls/AtomicityViolation.java)
- Test: [AtomicityViolationTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/concurrencypitfalls/AtomicityViolationTest.java)
