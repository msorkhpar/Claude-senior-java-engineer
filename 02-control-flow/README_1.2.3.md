# 1.2.3 While and Do-While Loops in Java

The nightly settlement import had run for a year. One night it never finished: one core at 100 %,
no exception, and no log line after "import started". The thread dump showed the worker inside a
`while` loop over the file's lines. That night's file was the first one with a comment line.

A `while` loop tests its condition before each pass and a `do`-`while` after it. The bugs come
from what the body does to the condition, and interviewers test them by handing you a loop and
asking whether it ends.

## continue in a while Loop Jumps to the Condition, Past the Update at the Bottom

The import counts records and skips comment lines:

```java
static int countRecords(List<String> lines) {
    int i = 0;
    int records = 0;
    while (i < lines.size()) {
        String line = lines.get(i);
        if (line.startsWith("#")) continue;    // skip comments
        records++;
        i++;
    }
    return records;
}
```

What does it return for a file whose first line is `# settlement 2026-11-05`? We ran it on three
data lines without that comment, then on the same file with it, then the fix below, each on a
thread we waited on for one second:

```text
no comment:   3 records
with comment: still running after 1 s
fixed:        2 records
```

`continue` ends the current pass and goes back to the condition. In a `while` loop the update is an
ordinary statement of the body, so `continue` skips it: `i` stays at 0, the same comment line is
read again, and the loop spins on it forever.

The bug usually arrives in a refactoring. Written as a `for`, the loop counts 2 records, because
`continue` there runs the update (unit 1.2.2). Move the update to the end of a `while` body and it
hangs. The fix advances before anything can skip:

```java
String line = lines.get(i++);          // advance first
if (line.startsWith("#")) continue;
```

When a `while` body has a `continue`, or an `if` whose path misses the update, put the update
first or check that every path reaches it. The same holds in a `do`-`while`: there `continue`
jumps to the condition at the bottom, so `do { ...; continue; } while (false);` makes one pass.

<!-- practice 1 -->

## A do-while Fits a Loop Whose First Pass Produces What the Condition Tests

A partner's API returns transactions in pages. Each page carries a cursor for the next one, `null`
on the last page, and `fetch(null)` asks for the first page. Two imports of the same three pages:

```java
static List<String> importWhile() {
    List<String> imported = new ArrayList<>();
    String cursor = null;
    while (cursor != null) {
        Page page = fetch(cursor);
        imported.addAll(page.records());
        cursor = page.next();
    }
    return imported;
}

static List<String> importDoWhile() {
    List<String> imported = new ArrayList<>();
    String cursor = null;
    do {
        Page page = fetch(cursor);
        imported.addAll(page.records());
        cursor = page.next();
    } while (cursor != null);
    return imported;
}
```

```text
while:    []
do-while: [tx-1, tx-2, tx-3, tx-4, tx-5, tx-6, tx-7]
```

The `while` version reports success with nothing imported. Its condition asks a question that only
the first fetch can answer, and `null` means both "start here" and "stop". A `do`-`while` makes the
first call without asking. Use it when the test reads something only the body produces, such as
the reply to a first attempt, or when counting the digits of a number that may be 0.

The condition of a `do`-`while` can't see a variable declared in its body, because the body's
block ends at the closing brace, before `while`:

```java
do {
    Page page = fetch(cursor);
    imported.addAll(page.records());
} while (page.next() != null);
```

```text
compile/scope/Scope.java:15: error: cannot find symbol
        } while (page.next() != null);
                 ^
  symbol:   variable page
  location: class Scope
1 error
```

The reverse mistake: a `do`-`while` over input that may be empty runs once on nothing.
`do { handled.add(queue.poll()); } while (!queue.isEmpty());` on an empty queue handles `[null]`.
When there may be no input at all, test first.

<!-- practice 2 -->

## A Loop Ends Only If Its Condition Can Become False for Every Input

The module's `Loop.sumUsingWhile` sums 1 to `n`, and its tests pass:

```java
public static int sumUsingWhile(int n) {
    int sum = 0;
    int i = 1;
    while (i <= n) {
        sum += i;
        i++;
    }
    return sum;
}
```

```text
sumUsingWhile(65_535)            = 2147450880
sumUsingWhile(65_536)            = -2147450880
sumUsingWhile(Integer.MAX_VALUE) = still running after 1 s
```

The sum wraps past `Integer.MAX_VALUE` from 65,536 on (unit 1.1.1). The last call never returns:
every `int` is `<= Integer.MAX_VALUE`, so when `i` reaches the maximum, `i++` wraps it to
`Integer.MIN_VALUE` and the condition still holds. If `n` comes from a request parameter, one
request holds a thread forever.

To answer "does this loop end?", name a quantity that every pass moves toward the exit, and check
that the exit exists for every input. Here the quantity is `i`, and the exit, an `int` above `n`,
doesn't exist when `n` is the largest `int`. Declare `i` and `sum` as `long` and return `long`.
The same check fails for `while (x != 1.0) x += 0.1;` from 0: ten steps give
`0.9999999999999999`, and the next one steps past `1.0` without ever equalling it (unit 1.1.1).
`while (x < 1.0)` ends, but after 11 passes, not 10. Count passes with an integer and compute
`x = i * 0.1`.

A condition that another thread makes false is a different problem: unit 6.2.2 shows a loop on a
plain field that never sees the write, and unit 6.2.1 why `wait()` sits inside a `while`.

<!-- practice 3 -->

## The Invariant Decides the Condition, the Updates and the Midpoint

Builds 1 to 2,000,000,000 have been released, and from some build on, all are broken, the latest
included. Each probe deploys a build, so we binary-search for the first broken one. The invariant:
*the first bad build is in `[low, high]`*.

```java
static int firstBad(int latest, IntPredicate isBad) {
    int low = 1, high = latest;              // the first bad build is in [low, high]
    while (low < high) {                     // stop when one candidate is left
        int mid = (low + high) >>> 1;        // low <= mid < high
        if (isBad.test(mid)) high = mid;     // mid may be the first bad one: keep it
        else low = mid + 1;                  // mid is good: the first bad one is after it
    }
    return low;
}
```

A bad `mid` may be the answer, so `high = mid` keeps it. That update needs `mid < high`, or it
changes nothing, so the midpoint rounds down. The loop stops when one candidate is left, so the
condition is `low < high`, and the answer is `low`.

### Spot the Problem: Two Conventions in One Loop

Each version changes one line of `firstBad`. What does each do when the first bad build is
1,999,999,000?

```java
while (low <= high) {                        // (1)
    int mid = (low + high) >>> 1;
    if (isBad.test(mid)) high = mid;
    else low = mid + 1;
}
```

```java
while (low < high) {
    int mid = (low + high) / 2;              // (2)
    if (isBad.test(mid)) high = mid;
    else low = mid + 1;
}
```

```text
  1,000,000 probes, low=1999999000 high=1999999000 mid=1999999000
overflow: java.lang.IllegalArgumentException: no build -647483647
```

(1) takes `<=` from the other convention, where both bounds move past `mid` (`high = mid - 1`),
but keeps `high = mid`. Once `low == high`, `mid` is that build, it is bad, `high = mid` changes
nothing, and the loop probes it forever. We stopped it at a million.

(2) adds two `int`s near 2 billion: the sum wraps (unit 1.1.1), and the midpoint is a build that
doesn't exist, which our `isBad` rejects. Arrays of a billion elements are rare; ranges of ids, offsets and timestamps are
not. `low + (high - low) / 2` and `(low + high) >>> 1` stay in range, and the JDK's
`Arrays.binarySearch` uses the second.

That JDK method keeps both bounds inclusive, `while (low <= high)` with `low = mid + 1` and
`high = mid - 1`, and returns as soon as it finds the key. Either convention works on its own; a
search that moves `low = mid` instead needs a midpoint that rounds up, `low + (high - low + 1) / 2`,
or it spins on the last two candidates.

<!-- practice 4 -->

## Summary

- **`continue` in a `while` skips everything after it, the update included.** One skipped element
  then spins the loop forever without an error; advance before anything that can skip.
- **A `do`-`while` makes its first pass before the test.** Use it when the test reads what a pass
  produces, like a page cursor; a `while` there imports nothing, and a `do`-`while` on input that
  may be empty handles `null`.
- **A loop ends only if its exit is reachable for every input.** `i <= n` never fails when `n` is
  `Integer.MAX_VALUE`, and `x != 1.0` never fails for a sum of tenths.
- **A loop's invariant fixes its condition and its updates.** Mixing two binary-search conventions
  spins on the last candidate, and `(low + high) / 2` overflows on large ranges.

## Reference

**Loops that compile and misbehave (Java 21, as run):**

| Loop | Result |
|---|---|
| `while (i < n) { if (skip) continue; ...; i++; }` | spins on the first skipped element |
| `while (r.readLine() != null) use(r.readLine());` | on lines `a` to `e`: uses `b`, `d`, then `null` |
| `cursor = null; while (cursor != null) { ... }` | no pass |
| `do { use(queue.poll()); } while (!queue.isEmpty());` on an empty queue | uses `null` once |
| `do { Page page = ...; } while (page.next() != null);` | `error: cannot find symbol` |
| `do { ...; continue; } while (false);` | one pass: `continue` jumps to the condition |
| `short s = 0; while (s < 40_000) s++;` | never ends: `s++` wraps from 32,767 to -32,768 |
| `int i = 1; while (i <= Integer.MAX_VALUE) i++;` | never ends: `i` wraps to `Integer.MIN_VALUE` |
| `while (x != 1.0) x += 0.1;` from `0` | never ends: ten steps give `0.9999999999999999` |
| `while (x < 1.0) x += 0.1;` from `0` | 11 passes, ends at `1.0999999999999999` |
| `while (false) { ... }` | `error: unreachable statement` (unit 1.2.1) |

**Binary search, the two conventions:**

| Invariant | Condition | Updates | Midpoint |
|---|---|---|---|
| the answer is in `[low, high]` | `low < high` | `high = mid`, `low = mid + 1` | `(low + high) >>> 1` |
| the key, if present, is in `[low, high]` (`Arrays.binarySearch`) | `low <= high` | `high = mid - 1`, `low = mid + 1`, return on a match | `(low + high) >>> 1` |

## Challenges

1. Find a `while` loop in your code base whose body has a `continue`. Name the quantity each pass
   moves toward the exit, and check that every path changes it.
2. A sender retries with `do { reply = send(request); } while (reply.retryable() && ++attempts < 5);`.
   After the loop, how does the caller tell a success from a give-up, and where does the backoff
   between attempts go?
3. Make `Loop.sumUsingWhile` right, or clearly failing, for every `int`. Compare a `long` counter,
   a condition that can't overflow and the formula `n * (n + 1L) / 2`: which still needs a loop,
   and what must the return type become?

## Design Note: Why Java Kept do-while

Java took `do`-`while` from C for one shape: a first pass that must run because it produces what
the test reads. Without it, that
shape needs a copy of the body before the loop, or a `while (true)` with a `break` in the middle,
which hides the exit inside the body.

The price is the scope rule shown above. The condition is written after the block but sits outside
it, so whatever it tests is declared before `do` and stays in scope after the loop, even when
nothing needs it there.

## Code Examples

- Test: [LoopTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/controlflow/LoopTest.java)
- Source: [Loop.java](src/main/java/com/github/msorkhpar/claudejavatutor/controlflow/Loop.java)