# 5.3.1. Data Races and How to Prevent Them

A customer forwards two documents for one order: the confirmation email says reference 48213, the
invoice says 48214. Both were produced by `order.reference()`, which takes a number from a shared
sequence the first time it is called and returns the stored one after that. The email and the
invoice are rendered on two threads. In code review, a month earlier, the question "is there a
data race?" got the answer "no, `ref` is an `AtomicInteger`". That answer was right, and the order
still got two numbers.

## A Tool That Counts Outcomes

A race condition shows up only when two threads meet inside a few instructions. To see how often
that happens, the examples use this helper: two threads walk the same batch of fresh objects in the same order,
2,000 batches of 1,000, and afterwards the main thread sorts each object into an outcome.

```java
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CyclicBarrier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Runs two actors against many fresh objects at once and counts what each object ended up as. */
public final class Outcomes {

    public static <S> void count(String name, Supplier<S> fresh, Consumer<S> actor1,
                                 Consumer<S> actor2, Function<S, String> outcome) throws Exception {
        int batches = 2_000, size = 1_000;
        List<S> batch = new ArrayList<>();
        CyclicBarrier go = new CyclicBarrier(3), done = new CyclicBarrier(3);
        Thread[] actors = new Thread[2];
        List<Consumer<S>> bodies = List.of(actor1, actor2);
        for (int a = 0; a < 2; a++) {
            Consumer<S> body = bodies.get(a);
            actors[a] = new Thread(() -> {
                try {
                    for (int b = 0; b < batches; b++) {
                        go.await();                          // both actors start a batch together
                        for (S s : batch) body.accept(s);    // and walk it in the same order
                        done.await();
                    }
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });
            actors[a].setDaemon(true);
            actors[a].start();
        }
        Map<String, Long> seen = new TreeMap<>();
        for (int b = 0; b < batches; b++) {
            batch.clear();
            for (int i = 0; i < size; i++) batch.add(fresh.get());
            go.await();
            done.await();                                    // the barrier orders the actors' writes before our reads
            for (S s : batch) seen.merge(outcome.apply(s), 1L, Long::sum);
        }
        System.out.printf("%-22s %s%n", name, seen);
    }
}
```

Every output on this page is from Java 21 on a 20-thread x86-64 machine, one JVM per run.

## A Data Race Is Two Accesses to One Variable, One a Write, With No Happens-Before Between Them

The first version of the order keeps the reference in a plain `int`:

```java
static final AtomicInteger SEQUENCE = new AtomicInteger();

int ref;                                   // 0 until assigned
int seenByEmail, seenByInvoice;

int reference() {
    if (ref == 0) ref = SEQUENCE.incrementAndGet();
    return ref;
}
```

One actor plays the email renderer, the other the invoice:

```java
Outcomes.count("plain int", PlainOrder::new,
        o -> o.seenByEmail = o.reference(), o -> o.seenByInvoice = o.reference(),
        o -> o.seenByEmail == o.seenByInvoice ? "same" : "different");
```

```text
plain int              {different=118980, same=1881020}
plain int              {different=145020, same=1854980}
plain int              {different=72164, same=1927836}
```

Before reading on, list the pairs of accesses in this example that form a data race.

The Java Memory Model (JLS 17.4.5) defines one: two accesses to the same variable, from different
threads, at least one of them a write, and no happens-before order between them. In `reference()`,
each thread's read of `ref` and the other thread's write of `ref` qualify, and so do the two
writes. No happens-before edge orders them: no lock, no volatile, no `start` or `join` between the
two threads (the edges are units 5.2.1 and 5.2.2).

Three other shared variables in the program are not raced on:

- `SEQUENCE` is an `AtomicInteger`. `incrementAndGet()` is one atomic read-modify-write on a
  volatile field, so two calls never return the same number.
- `seenByEmail` is written by an actor and read by the main thread. The `CyclicBarrier` orders
  them: its javadoc says actions before `await()` happen-before actions after the matching
  `await()` returns in other threads.
- The two actors write `seenByEmail` and `seenByInvoice` of the same object at the same moment.
  They are different variables, and JLS 17.6 forbids a write to one field from disturbing another.

The definition is about order, not about timing: two accesses a second apart with no edge between
them still race.

Both threads read `0` before either wrote, so `incrementAndGet()` ran twice for one order. "Writes
to an `int` are atomic" is true and doesn't prevent that interleaving. What the data race adds is
that the JMM no longer promises sequential consistency (unit 5.1.1), so a read may also see an
older write than the latest one.

## Code With No Data Race Can Still Race: a Check and an Act Are Two Steps

The version from the opening, in which every access to `ref` goes through the `AtomicInteger`:

```java
final AtomicInteger ref = new AtomicInteger();

int reference() {
    if (ref.get() == 0) ref.set(SEQUENCE.incrementAndGet());
    return ref.get();
}
```

```text
AtomicInteger          {different=355798, same=1644202}
AtomicInteger          {different=344723, same=1655277}
AtomicInteger          {different=490673, same=1509327}
```

There is no data race left. Volatile reads and writes are synchronization actions, which are not
counted as data races: the JMM puts them in one total order, and each read returns the latest write
in it (JLS 17.4.4, 17.4.7). No read of `ref` is stale. The result is still wrong in at least a sixth
to a quarter of the orders (two threads that both set `ref` can also both return the second number,
which counts as `same`), because the
check (`get() == 0`) and the act (`set`) are two atomic steps, and the other thread can run both of
its own between them. That is a **race condition**: the outcome depends on how the threads
interleave. It needs no data race: unit 6.2.1 shows one with every method `synchronized`, and unit
6.5.2 one on a `ConcurrentHashMap`. Making `ref` volatile,
or moving it into a thread-safe class, removes the data race and leaves the race condition.

The fix lets one atomic step decide who sets the value:

```java
int reference() {
    int current = ref.get();
    if (current != 0) return current;
    ref.compareAndSet(0, SEQUENCE.incrementAndGet());
    return ref.get();
}
```

```text
compareAndSet          {same=2000000}
compareAndSet          {same=2000000}
```

`compareAndSet(0, n)` sets the value only if it is still `0`, and only one thread can win that.
The loser returns the winner's number and its own is never used, so the sequence gets gaps.
Unit 5.3.2 covers compare-and-set loops in general.

On a `ConcurrentMap` the same shape is `if (!map.containsKey(k)) map.put(k, v)`, and `putIfAbsent`
or `computeIfAbsent` does it in one call; `remove(key, value)` and `replace(key, old, new)` do the
same for a check followed by a remove or a replace. (On a plain `Map` those default methods promise
no atomicity.) When you review shared state, after "is there a data race?" ask: does any decision
rest on a value another thread can change before the decision takes effect?

<!-- practice 1 -->

<!-- practice 2 -->

## A Data Race Is Harmless Only When Every Value the Read Can See Gives the Right Answer

An order's checksum is cached with the same shape as the first reference, in a plain `int` with no
lock:

```java
int checksum() {
    int c = checksum;                     // one read into a local
    if (c == 0) {
        c = checksumOf(lines);
        computed.incrementAndGet();
        checksum = c;
    }
    return c;
}
```

```text
checksum cache         {same=1830516, same, computed twice=169484}
checksum cache         {same=1771544, same, computed twice=228456}
checksum cache         {same=1925317, same, computed twice=74683}
```

This is a data race by the definition above, and both threads got the same checksum every time. Up to 11 % of
the objects computed the checksum twice, and both computations wrote the same number. Each read
could see `0` or the checksum, and either leads to the right result. Such a race is called
**benign**, and it stays benign only while three things hold:

- every computation produces the same value from inputs that don't change;
- the value is read and written in one access: any primitive but a non-volatile `long` or
  `double`, which may tear (unit 5.1.1), or a reference to an immutable object with `final` fields
  (unit 5.2.3);
- the field is read once, into a local.

The order reference fails the first condition: each computation takes a new number.

The third condition is easy to break while tidying the code:

```java
int checksumReadTwice() {
    if (checksum == 0) checksum = checksumOf(lines);
    return checksum;                      // a second, separate read
}
```

The two reads of `checksum` are separate racy reads. Each may see the initial `0`, which is the
last write ordered before it (JLS 17.4.4, 17.4.5), or the other thread's write, which isn't ordered
against it. So the first read may see the checksum and the second `0`, and the method returns `0`.
No write is undone; the second read simply sees an older one. This is allowed by the JLS and was not observed here: twelve
runs of 2,000,000 objects returned the right value every time. `String.hashCode()` reads its
cached field once into a local for this reason (unit 5.1.1).

A benign race saves synchronization on a hot path. It is worth that only after you have
checked all three conditions and written them in a comment beside the field, because the next
person to edit the method won't see them otherwise.

## A Passing Stress Test Shows Only the Interleavings That Run Produced

The usual test starts two threads, one call each, and compares. Here it is, repeated 1,000 times
against the `AtomicInteger` version:

```java
for (int r = 0; r < repeats; r++) {
    Refs.AtomicOrder order = new Refs.AtomicOrder();
    CountDownLatch go = new CountDownLatch(1);
    Thread email = new Thread(() -> { await(go); order.seenByEmail = order.reference(); });
    Thread invoice = new Thread(() -> { await(go); order.seenByInvoice = order.reference(); });
    email.start();
    invoice.start();
    go.countDown();                       // release both at once
    email.join();
    invoice.join();
    if (order.seenByEmail != order.seenByInvoice) failed++;
}
```

```text
two threads, one call each: failed 14 of 1000
two threads, one call each: failed 7 of 1000
two threads, one call each: failed 3 of 1000
two threads, one call each: failed 13 of 1000
two threads, one call each: failed 10 of 1000
```

A test that makes one attempt fails about one build in a hundred: the flaky test that gets a
retry annotation instead of a fix. The two threads leave the latch at slightly different moments,
and one call to `reference()` is short, so the calls seldom overlap. Even the harness, which
overlaps them on purpose, never produced the read-twice checksum's `0`, an outcome the JLS allows.

A thread dump doesn't help either. It shows each thread's stack and the locks it holds or waits
for, and a data race involves no lock: both threads are `RUNNABLE` somewhere in `reference()`,
or already finished.

> **jcstress (described here, not run).** OpenJDK's Java Concurrency Stress tool does what the
> helper above does, at scale. A test class holds the shared state (`@State`),
> two or more `@Actor` methods that the tool runs on different threads against fresh states many
> millions of times, and `@Outcome` annotations that mark each possible result as acceptable,
> interesting or forbidden. It runs the test in several JVM forks with different compiler
> settings and prints how often each outcome appeared. It is the tool for memory-model questions
> (can this read see `0`?), and its verdict is still per machine: run it on the CPUs you deploy to.

## A Check-Then-Act Is Tested by Pausing One Thread Between the Check and the Act

A race condition doesn't need luck to test. The test makes the interleaving happen: the sequence
handed to the order pauses its first caller, which has just found `ref` at `0`, until the second
caller has finished. Here `Order` takes its sequence in the constructor, and `call` picks the
version under test:

```java
Order order = new Order(() -> {
    int n = numbers.incrementAndGet();
    if (n == 1) {                                  // the first caller has checked and found 0
        firstChecked.countDown();
        try { goOn.await(5, TimeUnit.SECONDS); } catch (InterruptedException e) { throw new IllegalStateException(e); }
    }
    return n;
});
int[] seen = new int[2];
Thread email = new Thread(() -> seen[0] = call.on(order));
email.start();
firstChecked.await(5, TimeUnit.SECONDS);           // email is now between its check and its act
Thread invoice = new Thread(() -> seen[1] = call.on(order));
invoice.start();
invoice.join(5_000);                               // invoice runs its whole call meanwhile
goOn.countDown();
email.join(5_000);
```

```text
check, then set  email 1, invoice 2, stored 1
check, then set  email 1, invoice 2, stored 1
check, then set  email 1, invoice 2, stored 1
compareAndSet    email 2, invoice 2, stored 2
compareAndSet    email 2, invoice 2, stored 2
compareAndSet    email 2, invoice 2, stored 2
```

Pausing a thread after a read is a schedule the real system can produce, so a correct
implementation must survive it. The latches order the threads, and the timeouts keep a wrong
solution from hanging the build. A fix that holds a lock across the call makes the second caller
wait for the paused one, so a real test lets it wait, then releases the first and checks both
results.

The hook has to sit between the check and the act, so the code must call out at that point: an
injected sequence, clock, map or repository. Code that does everything inline can't be paused
there, which is one more reason to inject collaborators. A memory-model question (can a racy read
see a stale value?) has no such hook; that is jcstress's job.

<!-- practice 3 -->

## Summary

- **A data race is two accesses to one variable, at least one a write, with no happens-before
  between them.** Timing doesn't enter the definition, and two fields of one object never race
  with each other.
- **Code with no data race can still race.** Atomic or volatile accesses remove stale reads, but
  a check and an act are two steps unless one atomic call (`compareAndSet`, or `putIfAbsent` on a
  `ConcurrentMap`) or one lock does both.
- **A data race is harmless only when every value a read can see gives the right answer:** an
  idempotent computation, a value read in one access, and one read into a local.
- **A passing stress test shows only the interleavings it produced.** A naive two-thread test
  failed about one run in a hundred; an outcome the JLS allows may never show on one machine.
- **A check-then-act is tested deterministically** by pausing one thread between the check and
  the act; memory-model effects need jcstress.

## Reference

**Is it a data race? (JLS 17.4.5; edges in units 5.2.1 and 5.2.2)**

| Accesses to one variable from two threads | Data race? |
|---|---|
| plain field, one thread writes, the other reads, no edge between them | yes |
| written before `t.start()`, read by `t` | no |
| written by `t`, read after `t.join()` returns | no |
| written and read while holding the same lock | no |
| written and read under two different locks | yes |
| a `volatile` field, or an `Atomic*` class through its volatile-mode methods (`get`, `set`, `compareAndSet`, ...) | no |
| written before `CyclicBarrier.await()`, read after the matching `await()` returns elsewhere | no |
| two threads writing two different fields or array elements: not one variable (JLS 17.6) | no |

**Finding races:**

| Method | Shows | Misses |
|---|---|---|
| thread dump | where each thread is, the locks it holds and waits for | every data race: no lock is involved |
| repeated multithreaded test | a race that happened in that run | everything else; fails intermittently |
| outcome counting (jcstress) | how often each outcome appears, per JVM and machine | outcomes this hardware and JIT never produce |
| pausing a thread between check and act | a race condition, on every run | memory-model effects |

## Challenges

1. The harness found the plain-`int` reference wrong in 4–7 % of orders and the `AtomicInteger`
   one in 17–25 %. Propose why the version with no data race failed more often, then test your
   explanation by changing the harness.
2. A metrics field `long lastRequestMillis` is written by every request thread and read by a
   health check that alerts after 60 s of silence. Is this race benign? What changes on a JVM that
   tears `long` writes, and what would you change?
3. Find a check-then-act in code you maintain that calls no collaborator between the check and
   the act. How would you restructure it so a test can pause it there?

## Design Note: Why Java Gives a Racy Program Any Meaning at All

Java could have declared a program with a data race undefined and left compilers free to do
anything with it. It can't: a racy read of a reference must still return a reference to a real
object of the right type, or the JVM's type safety is gone. So JLS 17.4 constrains what a racy
read may return: a value that some write to that variable stored, never one "out of thin air"
(JLS 17.4.8).

The cost is that a racy Java program is guaranteed to keep running on plausible values: a stale
read or a duplicated computation doesn't crash, it issues a second order number. The guarantee
protects the JVM's type safety; your invariants still depend on removing the race.

## Code Examples

- Source: [DataRace.java](src/main/java/com/github/msorkhpar/claudejavatutor/concurrencypitfalls/DataRace.java)
- Test: [DataRaceTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/concurrencypitfalls/DataRaceTest.java)
