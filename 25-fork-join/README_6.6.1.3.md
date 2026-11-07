# 6.6.1.3. Implementing RecursiveAction for Resultless Tasks

The nightly repricing job raises 2 million catalogue prices by 3 % in place, and the web-shop
export starts as soon as `pool.invoke` returns. Every morning the shop shows old prices for a few
hundred thousand products, different ones each night. On a laptop with 500 test products it never
happens. One night a negative price made the job throw, the operator re-ran it, and most of the
catalogue went up twice.

The job is a `RecursiveAction`: a fork/join task whose `compute()` returns `void`, so its result is
whatever it wrote. Each section below is one rule about writing such a task.

## A RecursiveAction Is Finished Only When compute() Has Waited for Its Subtasks

The task, with the 10,000-row threshold of unit 6.6.1:

```java
import java.util.concurrent.RecursiveAction;

class Markup extends RecursiveAction {
    static final int THRESHOLD = 10_000;
    final long[] cents;
    final int from, to;

    Markup(long[] cents, int from, int to) { this.cents = cents; this.from = from; this.to = to; }

    @Override
    protected void compute() {
        if (to - from <= THRESHOLD) {
            for (int i = from; i < to; i++) cents[i] = raise(cents[i]);
            return;
        }
        int mid = (from + to) >>> 1;
        invokeAll(new Markup(cents, from, mid), new Markup(cents, mid, to));
    }

    static long raise(long c) {
        if (c < 0) throw new IllegalArgumentException("negative price: " + c);
        return c * 103 / 100;
    }
}
```

The job's version had no result to join, so its author forked both halves and returned:

```java
int mid = (from + to) >>> 1;
new MarkupNoJoin(cents, from, mid).fork();
new MarkupNoJoin(cents, mid, to).fork();
```

Both run on `ForkJoinPool(8)` over 2 million prices (`ex/Reprice.java`, Java 21), and the caller
counts the prices still unchanged as soon as `pool.invoke` returns:

```text
invokeAll:     0 of 2,000,000 prices still old when invoke returned
fork, no join: 1,739,131 of 2,000,000 prices still old when invoke returned
```

`invoke` returns when the root task's `compute()` returns. The forking version returns after two
`fork()` calls, so the caller reads the array while the leaves are still writing it, and nothing
in this code waits for them. The count changes from run to run. With 500 products there is one leaf,
no fork, and no bug.

`invokeAll(left, right)` forks `right`, runs `left` on the current thread, then waits for `right`:
the fork, compute, join of unit 6.6.1 in one call. Because it waits, a task can also work after its
subtasks. The `RecursiveAction` javadoc's sort example sorts both halves with `invokeAll`, then
merges them.

## Subtasks Write Disjoint Ranges and Read Nothing Another Subtask Writes

The leaves share one array and no lock. That is safe only because no element belongs to two
leaves. Make the left half `[from, mid]` instead of `[from, mid)`, by passing `mid + 1`:

```java
invokeAll(new MarkupOverlap(cents, from, mid + 1), new MarkupOverlap(cents, mid, to));
```

```text
overlap:       255 prices raised twice
```

Why 255? Two million prices in leaves of at most 10,000 make 256 leaves
and 255 splits, and each split hands its `mid` to both sides. The count is the same every run, so a
test with an exact expected array catches it; a test on 500 prices does not split.

Reading is the other half of the rule. A three-point moving average written in place reads
`a[i - 1]` and `a[i + 1]`, and at a leaf's edge those belong to the neighbouring leaf, which may or
may not have overwritten them yet (`ex/Smooth.java`, four runs over a million values, against the
same average written to a second array):

```text
in place: hash -813368940   to a copy: hash 1843535354
in place: hash 1353513801   to a copy: hash 1843535354
in place: hash -1680316791   to a copy: hash 1843535354
in place: hash 659750338   to a copy: hash 1843535354
```

<!-- practice 1 -->

## Leaves Count Locally and Add to the Shared Total Once

A counting action has nowhere to return its count, so the leaves add it to an object they all
share. Twenty million request latencies, counting those over a 250 ms target, three ways
(`ex/Tally.java`, threshold 10,000, best of five runs):

```java
// PLAIN_FIELD: a long field of an object every leaf shares
for (int i = from; i < to; i++) if (ms[i] > SLO_MS) counts.slow++;
// ATOMIC_PER_HIT: an AtomicLong, once per slow request
for (int i = from; i < to; i++) if (ms[i] > SLO_MS) slow.incrementAndGet();
// LOCAL_THEN_ADD: a local count, added once per leaf
long local = 0;
for (int i = from; i < to; i++) if (ms[i] > SLO_MS) local++;
slow.addAndGet(local);
```

```text
expected: 9,960,000 slow requests
PLAIN_FIELD       3,543,771    20 ms
ATOMIC_PER_HIT    9,960,000    91 ms
LOCAL_THEN_ADD    9,960,000     3 ms
```

`counts.slow++` reads, adds and writes, and eight workers doing it at once overwrite each other's
updates (unit 6.2.1). It lost almost two thirds of the count, a different amount on every run, and
nothing failed. Adding a local count to the plain field once per leaf makes the loss rarer, not
impossible: `+=` is the same three steps. The atomic count is right, but each slow request writes
one memory location that all eight cores contend for. A local count added once per leaf makes
2,048 atomic updates instead of ten million.

When the action's whole output is one number, a `RecursiveTask` returns it through `join()` with
no shared state at all (unit 6.6.1.2). The shared total fits when the action's main work is
elsewhere, such as rewriting an array, and the count comes along.

<!-- practice 2 -->

## A Failure Leaves the Array Partly Changed, So Write to a Copy

The same `Markup`, with one price set to `-1` a quarter of the way in:

```text
bad row:       invoke threw IllegalArgumentException, 1,750,000 of 2,000,000 prices raised
```

Other runs raised 1,992,188. The exception reached the caller as unit 6.6.1.1 describes, but every
leaf that ran kept its writes, including leaves that finished after the throw. Re-running the job raises those prices a second time.

Rolling back in the `catch` with a snapshot taken before is not enough either. When the task run
on the current thread fails, `invokeAll` rethrows at once and cancels the other one, and
cancelling stops a task only if it hasn't started (`ForkJoinTask.cancel` javadoc). A subtask that is
already running keeps going, and so do the subtasks it forks. In `ex/Straggler.java` the left
leaf of an eight-price array throws while a latch holds the right leaf:

```text
invoke threw:    [1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000]
200 ms later:    [1000, 1000, 1000, 1000, 1030, 1030, 1030, 1030]
```

The caller had caught the exception, and the right leaf wrote its half afterwards. The left leaf
stopped at its throw and wrote nothing more.

Run the action on a copy, and publish the copy only when `invoke` returns normally:

```java
long[] next = prices.clone();
pool.invoke(new Markup(next, 0, next.length));
prices = next;                               // reached only if every leaf succeeded
```

A failure leaves `prices` untouched, and a leaf still running writes into a copy nobody reads. If
other threads read `prices`, make the field `volatile` so they see the new array whole. The copy
costs one array's memory and one pass to clone. Validating the input first does not replace it: a
leaf can still fail on something no check foresaw.

<!-- practice 3 -->

## Summary

- **A `RecursiveAction` is finished only when `compute()` has waited for its subtasks.** Fork
  without join, and `invoke` returns while the leaves are still writing.
- **Subtasks write disjoint ranges and read nothing another subtask writes.** An inclusive `mid`
  changes the same elements twice every run; reading a neighbour's range gives a different answer
  every run.
- **Leaves count locally and add to the shared total once.** A plain field loses updates silently;
  an atomic per element works but makes every core wait for one memory location.
- **A failure leaves the array partly changed.** Write to a copy and publish it only on success;
  a rollback in the `catch` can be overwritten by a leaf still running.

## Reference

| Call | What it does |
|---|---|
| `invokeAll(t1, t2)` | forks `t2`, runs `t1` on this thread, waits for `t2`; if `t1` fails, cancels `t2` and rethrows at once |
| `invokeAll(t1, t2, t3, ...)`, `invokeAll(collection)` | for any number of tasks: runs the first, waits for the others in order; on the first failure it sees, cancels the rest and rethrows |
| `RecursiveAction.compute()` | returns `void`; `join()` and `invoke()` on the task return `null` |
| `task.cancel(...)` | stops a task that hasn't started; one already running keeps running, though it reports cancelled and done |
| `RecursiveTask<V>.compute()` | returns `V`, which `join()` hands back (unit 6.6.1.2) |
| `Arrays.parallelSetAll(a, i -> ...)` | sets every element from its index, through a parallel stream (the common pool, unless called from a task of another pool) |
| `Arrays.parallelPrefix(a, op)` | cumulative results in place, e.g. running totals |
| `Arrays.parallelSort(a)` | sorts in parallel; an object array of 8,192 elements or fewer is sorted sequentially (javadoc: "minimum granularity") |

For plain array work these built-ins already run on fork/join; write your own action when the
per-element step or the split is your own.

## Challenges

1. The repricing job also has to report how many prices crossed a round number. Is that a shared
   total inside `Markup`, a second pass, or a `RecursiveTask`? Measure the first two.
2. Clone 2 million prices, then 200 million. At what size does the copy in the last section cost
   more than the job, and what would you do instead?
3. A tree of product categories gets a discount applied to every node, and each node's total must
   include its children. Where in `compute()` does the node's own update go, and why can it not go
   before `invokeAll`?

## Design Note: A Task Without a Result Leaves the Caller Trusting Shared State

A `RecursiveTask` hands each partial result to its parent through `join()`: the data travels with
the waiting, so a parent cannot use a result it hasn't waited for. A `RecursiveAction` separates
the two. The result is in shared memory the whole time, readable half-written, and nothing in the
type forces anyone to wait before reading it. That is what makes in-place work cheap, with no
partial arrays to allocate and merge, and it is why every rule on this page is about who may touch
the shared array, and when.

## Code Examples

- Source: [RecursiveActionExamples.java](src/main/java/com/github/msorkhpar/claudejavatutor/forkjoin/RecursiveActionExamples.java)
- Test: [RecursiveActionExamplesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/forkjoin/RecursiveActionExamplesTest.java)
