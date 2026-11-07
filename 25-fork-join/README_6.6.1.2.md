# 6.6.1.2. Implementing RecursiveTask for Results-Bearing Tasks

The finance team's month-end job runs on a `ForkJoinPool`, and it raised three tickets in one
month. Revenue per region came out different every time the job ran. The count of distinct
customers jumped from 150,000 to 2,000,000 after someone lowered the threshold. And one corrupt
row made the import log `import failed: null`, then keep the CPUs busy for a while.

## A Task Returns Its Part of the Answer; Tasks That Write into Shared State Lose It

The revenue report sums 4 million orders per region. Its first version has every leaf add into
one map that all tasks share. Since it returns nothing, it is a `RecursiveAction`:

```java
if (to - from <= THRESHOLD) {
    for (int i = from; i < to; i++) totals.merge(REGIONS[region[i]], cents[i], Long::sum);
    return;
}
```

The second has each task return a map of its own, and the parent merges the two it gets back:

```java
@Override
protected Map<String, Long> compute() {
    if (to - from <= THRESHOLD) {
        Map<String, Long> totals = new HashMap<>();
        for (int i = from; i < to; i++) totals.merge(REGIONS[region[i]], cents[i], Long::sum);
        return totals;
    }
    int mid = (from + to) >>> 1;
    ByRegion left = new ByRegion(from, mid);
    left.fork();
    Map<String, Long> totals = new ByRegion(mid, to).compute();
    left.join().forEach((r, c) -> totals.merge(r, c, Long::sum));
    return totals;
}
```

Before reading on: the leaves never touch the same order. What does the shared map add up to?

Three runs of each on `ForkJoinPool(8)` (`ex/Shared.java`, Java 21):

```text
expected total: 18,388,000,000
shared map, run 1:   8,091,764,762
shared map, run 2:   java.util.ConcurrentModificationException: java.util.ConcurrentModificationException: java.util.ConcurrentModificationException
shared map, run 3:   7,354,387,872
returned maps, run 1: 18,388,000,000
returned maps, run 2: 18,388,000,000
returned maps, run 3: 18,388,000,000
```

The leaves read different orders but write the same four entries. When two workers `merge` into
one region at once, both read the old total and one sum overwrites the other: more than half the
revenue is gone, a different amount each run. In run 2 a `HashMap` noticed another thread
changing it mid-`merge` and threw. The second version shares nothing that changes. A
leaf's map belongs to one thread until it returns, and `join()` hands it to the parent, so the
merge needs no lock. `join()` returns only once that task's `compute()` has returned, which here is
after it has joined its own halves.

Making the shared state thread-safe fixes the arithmetic but not the ownership. A
`ConcurrentHashMap` or an `AtomicLong` held by the report object adds correctly, and then every
report run on that object adds into the same field: the second run starts from the first one's
total, and two runs at once mix their sums. A value returned from `compute()` belongs to one
invocation.

<!-- practice 1 -->

## The Parent Can Only Combine What Its Halves Return

The customer report counts the distinct customers among 2 million orders; there are 150,000 of
them. Each leaf collects its customers
in a `HashSet`. The first version returns the set's size, and the parent adds the two counts. The
second returns the set itself, and the parent merges the two sets (`ex/Distinct.java`):

```java
Set<Long> seen = new SetTask(mid, to, threshold).compute();
seen.addAll(left.join());
return seen;
```

```text
threshold 2,000,000: counts added   150,000, sets merged 150,000
threshold   500,000: counts added   600,000, sets merged 150,000
threshold    10,000: counts added 2,000,000, sets merged 150,000
```

Every leaf's count is right, and their sum is wrong. A customer who ordered in both halves is
counted once by each half, and the parent, holding two numbers, can't tell. With one task the
answer is right, so a test on a small file passes.

The value a task returns must hold enough to combine with its sibling's, so that any split gives
the answer one task would. Adding works for sums and row counts, and `max` works for maxima. A
distinct count needs the sets. An average needs the sum and the count, not two averages. A
median needs the values themselves: no weighting of two medians gives the median of both halves. The most
frequent value needs every value's count, since the overall winner can come second in both
halves. Choose that type first, then write `compute()`.

<!-- practice 2 -->

## A Subtask's Exception Reaches join() as a Copy, and Its Siblings Keep Running

The ledger import parses a million amounts, and row 600,123 says `12O0`, with a letter O.
`Long.parseLong` throws in some worker, and the caller catches the exception around
`pool.invoke(...)`. The program then follows the cause chain to its end, and counts the rows
parsed after the failure happened (`ex/Failure.java`, second of three runs):

```text
no stop:
  caught:     java.lang.NumberFormatException
  getCause(): java.lang.NumberFormatException
  3 causes down: java.lang.NumberFormatException: For input string: "12O0"
  rows parsed after the bad one: 690,986
```

The class is right: as unit 6.6.1.1 showed, `join()` and `invoke()` rethrow the task's own type.
The object is not the one thrown. When the joining thread isn't the thread that failed,
`ForkJoinTask` builds a new exception of the same class, so that the stack trace shows both
threads, and keeps the original as its cause. It uses a public constructor taking a `Throwable` if
the class has one, and otherwise the no-argument constructor. `NumberFormatException` has no
`Throwable` constructor, so the copy has no message, and the log said `null`. A class that has
one, such as `IllegalStateException` or the `ConcurrentModificationException` in the first
section, gets the original's
`toString()` as its message. Each thread the failure crossed added a copy: three here, four in
another run. To report the failure, follow `getCause()` while it is the same class.

Nothing cancels a task's siblings, and the pool doesn't run a failed task again: it stays failed,
and its exception goes to whoever joins it. The parent
whose `compute()` threw never joined its forked half, which was still queued, so a worker ran it.
`cancel(true)` wouldn't stop a running task either: in a `RecursiveTask` the argument has no
effect, and no task is interrupted. When one failure makes the rest worthless, make the tasks
check, for example a shared `AtomicBoolean` that a failing leaf sets and every `compute()` reads
first (`failed` is `null` in the first run):

```java
if (failed != null && failed.get()) return 0L;        // another part already failed
// ... and in the leaf:
} catch (RuntimeException e) {
    if (failed != null) failed.set(true);
    throw e;
}
```

```text
shared flag:
  caught:     java.lang.NumberFormatException
  getCause(): java.lang.NumberFormatException
  3 causes down: java.lang.NumberFormatException: For input string: "12O0"
  rows parsed after the bad one: 2,995
```

The rows still parsed belong to leaves that had started before the flag was set.

<!-- practice 3 -->

## Splitting Shares the Work Among Workers; It Can't Remove Work the Recursion Repeats

Fibonacci is the textbook `RecursiveTask`: `fib(n)` forks `fib(n - 1)`, computes `fib(n - 2)`, and
adds them. Below the cutoff a task calls the plain recursive method instead. `fib(40)` on
`ForkJoinPool(8)`, against one thread (`ex/Fib.java`, third of three runs):

```text
tasks, cutoff  1: fib(40) = 102,334,155  331,160,281 tasks  1,519 ms
tasks, cutoff 20: fib(40) = 102,334,155       35,421 tasks     98 ms
tasks, cutoff 30: fib(40) = 102,334,155          287 tasks     94 ms
one thread, recursive: fib(40) = 102,334,155     475 ms
one thread, loop:      fib(40) = 102,334,155       2 us
```

With a task per call there are 331 million tasks, and the job is three times slower than one
thread. With a cutoff it is about five times faster: eight workers sharing the same 331 million
calls. The loop does 40 additions, each needing the two results before it, so it can't be split, and it
doesn't need to be. The recursion computes `fib(20)` 10,946 times, because
the two halves of a split overlap, and the workers can only share that waste. Before
parallelising a recursion, check that its subproblems are independent.

<!-- practice 4 -->

## Summary

- **A task returns its part of the answer.** Leaves that write into a shared map lose updates,
  and a field made thread-safe still mixes the totals of two runs.
- **The parent can only combine what its halves return.** Return what the combine needs (sets for
  a distinct count, sum and count for an average), or the answer changes with the threshold.
- **A subtask's exception reaches `join()` as a copy** of the same class with the original as its
  cause, and the other subtasks keep running unless they check a flag.
- **Splitting shares work; it can't remove work the recursion repeats.** Fork/join speeds up
  recursive Fibonacci by the number of workers at best, and a loop is far faster still.

## Reference

**A `RecursiveTask<V>` and its result (Java 21 and 25):**

| Call | What it does |
|---|---|
| `compute()` | returns the task's `V`, on the thread that calls it |
| `join()` | returns a forked task's `V`; a failure is rethrown as an unchecked exception of the same class |
| `task.invoke()` | runs `compute()` on the calling thread and returns its `V` |
| `get()` | as `join()`, but a failure comes wrapped in `ExecutionException` |
| `cancel(true)` or `cancel(false)` on a `RecursiveTask` | marks a task that isn't done as cancelled, and `join()` then throws `CancellationException`; a running task is not interrupted and runs to its end |
| `isCompletedAbnormally()` | `true` if the task threw or was cancelled |
| `getException()` | the exception, `CancellationException` if cancelled, or `null` |

**What a joining thread receives when another thread threw (ran on 21 and 25):**

| The thrown class has | Rethrown as |
|---|---|
| a public constructor whose one parameter is `Throwable` | `new X(original)`; with the usual constructor, its message is `original.toString()` |
| a public no-argument constructor, but not the one above | `new X()` with the original as its cause; message `null` |
| neither, or building the copy throws | the original object |

## Challenges

1. The month-end job also reports the median basket. What would a task have to return so that
   two halves combine into the median, and is it still worth splitting?
2. The ledger import should report every bad row, not only the first. Design the result type, and
   say what it costs in memory when half the file is bad.
3. A `LongAdder` per region, created for one run, also gives the right revenue. Measure it against
   returned maps on 8 workers, and say when each wins.

## Design Note: Why the Result Travels Through join()

A fork/join task is written so that two halves run with nothing in common: each gets its own
range, builds its own value and hands it to one parent through `join()`, once per task. The price is allocation and a combine at every level.
A tree of 2,000 tasks builds 2,000 partial maps or sets and merges each into another, and every
merge of two sets walks one of them. When the
combine gets that expensive, a concurrent structure that all leaves write to may be cheaper, as
long as it belongs to one invocation and not to the object that runs it.

## Code Examples

- Source: [RecursiveTaskExamples.java](src/main/java/com/github/msorkhpar/claudejavatutor/forkjoin/RecursiveTaskExamples.java)
- Test: [RecursiveTaskExamplesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/forkjoin/RecursiveTaskExamplesTest.java)
