# 6.6.1.4. Splitting and Joining Tasks for Optimal Performance

The nightly revaluation prices 4 million positions on a `ForkJoinPool(8)`. Its task is the
`SumTask` of unit 6.6.1 with a pricing call in the loop, and its threshold gives each worker one
leaf. Then the desk added options: 5% of the positions, sorted to the end of the file, each 200
times the work of a bond. The job now takes as long as it does on one thread, the totals are
right, and the CPU graph shows one busy core for most of the run.

Unit 6.6.1 measured the threshold on work where every row costs the same. This unit is about work
where they don't.

## Uneven Work Needs Many More Leaves Than Workers

The cost model, and the threshold for a given number of leaves per worker (`ex/Uneven.java`):

```java
static final int N = 4_000_000;
static final int OPTIONS_FROM = N - N / 20;           // the last 5%

/** Steps to price one position: 20 for a bond or share, 4,000 for an option. */
static int steps(int i) { return i >= OPTIONS_FROM ? 4_000 : 20; }

int threshold = N / (8 * perWorker);                  // 8 workers
```

Before reading on: with one leaf per worker, which worker gets the options, and what do the other
seven do once their leaf is done?

```text
plain loop, one thread: 937 ms (sum 509830424), total steps 876,000,000
by count,   1 per worker     15 tasks,  912 ms  (sum 509830424)
by count,   4 per worker     63 tasks,  567 ms  (sum 509830424)
by count,  16 per worker    255 tasks,  224 ms  (sum 509830424)
by count,  64 per worker  1,535 tasks,  163 ms  (sum 509830424)
by count, 256 per worker  4,607 tasks,  160 ms  (sum 509830424)
```

The last leaf holds all 200,000 options: 800 million of the job's 876 million steps. Since each
task forks its left half and computes its right half itself, that leaf runs on the worker that took
the root. The other seven finish their bonds and go idle, and the job lasts as long as that leaf.

Stealing can't fix this. A thief takes only a queued task, and a leaf that has started is a
plain loop on one thread that nothing can take half of. An uneven job needs leaves small enough to
spread the expensive rows over many of them. Four leaves per worker, the target the JDK's parallel
streams use, put the options in two leaves: 567 ms. Sixteen spread them over seven: 224 ms. The
time stops falling at 64 per worker, 163 ms; 256 per worker gave 160.

Size from the pool you run in, `pool.getParallelism()`. A threshold computed from
`Runtime.availableProcessors()` on a 4-core build server, then run in production's pool of 16,
gives a quarter of the leaves it was meant to. How many leaves are enough depends on how narrow
the expensive region is, so measure with production-shaped data; a uniform test file shows no
difference at all.

<!-- practice 1 -->

## When You Know What Each Item Costs, Split by Cost

Sixty-four leaves per worker fixed the time, but every leaf is sized for the options: the 3.8
million bonds are cut into the same small pieces. When the cost of each item is known, or can be
estimated, the threshold can count steps instead of rows, and the split point can halve the steps:

```java
@Override
protected Long compute() {
    long work = prefix[to] - prefix[from];        // prefix[i] = steps of positions 0..i-1
    if (work <= budget || to - from == 1) {
        long s = 0;
        for (int i = from; i < to; i++) s += price(i);
        return s;
    }
    int mid = splitPoint(from, to);
    ByWork left = new ByWork(from, mid, budget);
    left.fork();
    long r = new ByWork(mid, to, budget).compute();
    return left.join() + r;
}

/** The first index after which half of the range's steps are done, kept inside (from, to). */
static int splitPoint(int from, int to) {
    long half = prefix[from] + (prefix[to] - prefix[from]) / 2;
    int lo = from + 1, hi = to - 1;
    while (lo < hi) {
        int m = (lo + hi) >>> 1;
        if (prefix[m] < half) lo = m + 1; else hi = m;
    }
    return lo;
}
```

With `budget` set to the total steps divided by the leaves wanted:

```text
by work,    1 per worker     15 tasks,  175 ms  (sum 509830424)
by work,    4 per worker    107 tasks,  142 ms  (sum 509830424)
```

Eight leaves of equal work (175 ms) match 1,535 tasks split by count (163 ms). The bonds end up in
a few large leaves and the options in many small ones, which a threshold in rows can't do with one
number.

Two details guard the general case, though no position here comes near the budget. A single item
can cost more than the whole budget, and a range of one item can't split, so `to - from == 1` is a
leaf too. And when the last item holds more than half the range's steps, the first index that
reaches half is `to` itself; `hi = to - 1` keeps the right half non-empty. Without it, the left
half is the whole range again, and the task forks itself until the stack overflows.

The estimate only decides where to cut, so a poor one costs balance, not correctness, and a few
leaves per worker on top of it absorb the error: four per worker by work ran in 142 ms.

<!-- practice 2 -->

## The Split Point Sets the Depth: Halve, or Fork the Chunks From a Loop

An audit job walks a list in chunks of 1,000 items: 500,000 items in the test file, 8 million at
year end. Its task forks the first chunk and handles the rest of the range the same way
(`ex/Peel.java`):

```java
PeelChunk first = new PeelChunk(from, from + chunk, chunk, depth + 1);
first.fork();
long rest = new PeelChunk(from + chunk, to, chunk, depth + 1).compute();
return first.join() + rest;
```

The same items, halved instead, on a `ForkJoinPool(8)`:

```text
halve       500,000 items, chunk  1,000: depth      9, steals     15,    6 ms
peel        500,000 items, chunk  1,000: depth    499, steals    405,    5 ms
halve     8,000,000 items, chunk  1,000: depth     13, steals     46,   73 ms
peel      8,000,000 items, chunk  1,000: java.lang.StackOverflowError
```

On the test file both are fast, because the other workers steal the chunks as they are forked. But
`compute()` on the rest is a call on the same thread, so peeling adds a stack frame per chunk:
499 on the test file, and 7,999 needed on the year-end file, more than a worker's stack holds.
Halving adds a level each time the range halves, 13 levels for 8 million items. Each steal of a
half also carries half of what is left, so the thieves split it further on their own threads,
and 46 steals were enough instead of one per chunk.

An uneven ratio deepens the tree far less than peeling. The module's `SplittingAndJoining` also
has a 10/90 split, cut at `start + length / 10`. Over 8 million items at a threshold of 1,000 it
went 86 levels deep and made 48,531 tasks, against 13 and 16,383 for 50/50, and ran in 47 ms
against 53 (`ex/Ratio.java`). On even work it costs tasks, and the time doesn't show it.

When the chunks are known in advance, as in a list of files, fork them from a loop instead of
recursing. `ForkJoinTask.invokeAll(chunks)` forks every chunk but the first, runs that one on the
calling thread, then joins the rest, newest first. The stack stays one level deep whatever the
count. Every chunk then sits on one worker's queue and costs a steal of its own, which is cheap
while the chunks are few:

```java
long s = 0;
for (Chunk c : ForkJoinTask.invokeAll(chunks)) s += c.join();
return s;
```

Over 8 million items, `invokeAll` with 8 to 80,000 chunks ran in 55 to 77 ms (`ex/JoinOrder.java`).

<!-- practice 3 -->

## Summary

- **Uneven work needs many more leaves than workers.** A started leaf can't be stolen, so the leaf
  that holds the expensive rows sets the job's time; four per worker wasn't enough when 5% of the rows
  held 91% of the work.
- **When you know what each item costs, split by cost.** A threshold in steps and a split point
  at half the steps balance with few tasks; a single item is always a leaf, and the split point
  stays inside the range.
- **The split point sets the depth.** Peeling a chunk at a time costs a stack frame per chunk and
  overflows on large inputs; halve, or fork known chunks from a loop.

## Reference

| Fact | Source |
|---|---|
| A thief takes only a queued task; a running leaf finishes on its thread | `ex/Uneven.java`: one leaf per worker took as long as one thread |
| Parallel streams aim at 4 leaves per worker of the common pool | JDK 21 source, `java.util.stream.AbstractTask.LEAF_TARGET` |
| A task should do "more than 100 and less than 10000 basic computational steps" (basic operations, smaller than this page's pricing steps) | `ForkJoinTask` javadoc |
| Joins "should be performed innermost-first": `a.fork(); b.fork(); b.join(); a.join();` | `ForkJoinTask` javadoc; `ex/JoinOrder.java` measured no consistent difference |
| `invokeAll(list)` forks all but the first, runs the first, then joins the rest | JDK 21 source of `ForkJoinTask` |
| `getSurplusQueuedTaskCount()` estimates how many more tasks this worker has queued than other workers might steal; the javadoc suggests keeping a surplus of about 3 and computing locally above it | `ForkJoinTask` javadoc |
| `pool.getStealCount()` is an estimate that undercounts while the pool is busy, and it varies from run to run | `ForkJoinPool` javadoc; three runs of `ex/Peel.java` gave 46, 53 and 53 steals for the same halving |

## Challenges

1. Your positions are not sorted by type, and the options are spread through the file. Run
   `Uneven` with the options scattered: how many leaves per worker do you need now, and why?
2. The pricing cost of an option depends on its maturity, and you only have an estimate. How wrong
   can the estimate be before split-by-cost is worse than sixteen leaves per worker by count?
3. Replace the fixed threshold with `getSurplusQueuedTaskCount() > 3` as the leaf test, and
   compare its task counts and times with yours on the uneven data.

## Design Note: Why the Pool Can't Split a Leaf for You

A `ForkJoinPool` balances by moving tasks, and a task is opaque: once `compute()` is running, the
pool sees a thread and a stack, not a loop with a range it could cut. Splitting a running leaf
would need every task to report its progress and accept a new end while it runs. Fork/join keeps
the contract small, a `compute()` that returns, and leaves the decision of how small a piece must
be to the code that knows what its items cost. That code has to decide before the work starts,
from an estimate, and no test fails when it decides wrong.

## Code Examples

- Source: [SplittingAndJoining.java](src/main/java/com/github/msorkhpar/claudejavatutor/forkjoin/SplittingAndJoining.java)
- Test: [SplittingAndJoiningTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/forkjoin/SplittingAndJoiningTest.java)
