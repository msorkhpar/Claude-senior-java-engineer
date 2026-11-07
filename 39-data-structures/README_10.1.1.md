# 10.1.1. Common Data Structures

An order book keeps 100,000 resting orders in a `LinkedList`, because the usual Big-O table gives
LinkedList O(1) inserts and new orders go in the middle through `add(index, order)`. Latency climbs with the size of the book. Sampling the inserting thread's
stack 1,000 times at 100,000 orders found it in `LinkedList.node(int)` 997 times: the walk to the
index, not the insert.

The table counts steps and leaves out what each step costs on real hardware, and what the JDK's
classes do at their edges. Growth rates themselves are unit 10.1.2, and the concurrent
collections are unit 6.5.

## A Timing Loop That Warms Up First

Every timing comes from one helper. It runs the code untimed for half a second so the JIT compiles
it, then times five runs of at least 100 ms each and keeps the median:

```java
import java.util.Arrays;
import java.util.function.LongSupplier;

/** A warmed loop, not JMH: warms the body up, then times it in five runs and keeps the median. */
public final class Bench {
    static long sink;                                // results go here, so the JIT can't drop the work

    /** body() does opsPerCall operations; returns the median of five runs, in ns per operation. */
    public static double nsPerOp(long opsPerCall, LongSupplier body) {
        repeatFor(500, body);                        // warm-up: half a second of untimed calls
        double[] ns = new double[5];
        for (int run = 0; run < ns.length; run++) {
            long start = System.nanoTime();
            long calls = repeatFor(100, body);       // each timed run lasts at least 100 ms
            ns[run] = (double) (System.nanoTime() - start) / (calls * opsPerCall);
        }
        Arrays.sort(ns);
        return ns[2];
    }

    private static long repeatFor(long ms, LongSupplier body) {
        long end = System.nanoTime() + ms * 1_000_000, calls = 0;
        do { sink += body.getAsLong(); calls++; } while (System.nanoTime() < end);
        return calls;
    }
}
```

Every body returns a value that goes into `sink`; a loop whose result is never used can be
removed by the JIT and then measures nothing. This is not JMH, which would control forking, GC and
dead code more carefully. Every timing table below is one of three program runs on one laptop
(Temurin 21.0.12, x86-64). The other two runs moved single numbers by as much as 90%, and never
reversed a winner, so only the ratios mean anything.

## LinkedList Loses to ArrayList Unless You Already Hold the Position

Four operations, each on an `ArrayList` and a `LinkedList` of `Integer` at three sizes: `get(i)`
at random indexes, `add(size/2, x)` followed by `remove(size/2)`, a for-each over the whole list,
and a `ListIterator` walk that inserts after every 10th element, followed by a second walk that
removes them again (that column is per inserted element, both walks included):

```java
for (int i : idx) s += list.get(i);                    // get(i)

list.add(list.size() / 2, i);                          // add/remove mid
list.remove(list.size() / 2);

ListIterator<Integer> it = list.listIterator();        // iterator add+remove
for (int seen = 1; it.hasNext(); seen++) {
    it.next();
    if (seen % 10 == 0) it.add(-1);
}
for (it = list.listIterator(); it.hasNext(); ) {
    if (it.next() == -1) it.remove();
}
```

```text
n        list          get(i)  add/remove mid   for-each  iterator add+remove
1000     ArrayList        0.7            45.7       0.44                 83.1
1000     LinkedList     357.7          1096.8       3.36                 83.1
10000    ArrayList        2.4           371.8       1.31                383.3
10000    LinkedList    2701.1         13080.4       2.85                 66.1
100000   ArrayList        2.1          3895.0       1.42               5860.1
100000   LinkedList   35345.5        118217.7       2.86                 68.4
ns per operation, median of 5 runs; sink true
```

At 100,000 elements ArrayList inserted and removed in the middle 30 times faster.

`add(index, x)` on a LinkedList is two steps. The link itself is O(1), but first `node(index)`
walks from whichever end is closer, following one pointer per element, and each node is a
separate object the CPU has to fetch. ArrayList also does O(n) work, shifting the tail of its
array with one `System.arraycopy` over contiguous memory; at 100,000 elements an add and a remove
in the middle took about 4 µs together. The for-each
column shows the same effect with no index at all: LinkedList took two to eight times as long per
element, even though its nodes were allocated one after another.

LinkedList wins one column: inserting through an iterator at a position the code already holds.
From 10,000 elements on its cost per insert stayed flat while ArrayList's grew with n. Use
LinkedList for a long list edited during a walk, and ArrayList for one read by index or iterated.
The order book finds each position by index, so it is the second kind: in this run an insert and
a remove in the middle took ~118 µs on the LinkedList and ~4 µs on an ArrayList.

> **Memory.** Each LinkedList element is a node object with two links besides the element; an
> ArrayList holds one reference per slot plus spare capacity. Unit 10.2.1 measures object sizes.

<!-- practice 5 -->

## ArrayDeque Is the Stack and the Queue; Stack Is a Synchronized Vector

`java.util.Stack` predates the collections framework. It extends `Vector`: `pop` and `peek` are
`synchronized`, `push` goes through `Vector`'s synchronized `addElement`, and it inherits the whole
`List` API:

```java
Deque<String> deque = new ArrayDeque<>();
Stack<String> stack = new Stack<>();
for (String s : new String[] {"a", "b", "c"}) { deque.push(s); stack.push(s); }
```

```text
ArrayDeque after push a, b, c: [c, b, a]  peek() -> c
Stack      after push a, b, c: [a, b, c]  peek() -> c
Stack superclass: java.util.Vector, pop is public synchronized
stack.add(0, "x"), stack.get(2): [x, a, b, c] b
```

Both pop `c` first, but a `Stack` iterates bottom-first, in `Vector` order, while an `ArrayDeque`
used with `push` iterates from the top. Code that lists "the last three actions" by iterating a
`Stack` shows the oldest three. Anyone holding the `Stack` can also insert under the bottom or
read by index.

The lock costs time even with one thread. Pushing 1,000 elements and popping them again:

```text
push 1,000 then pop 1,000, ns per push+pop (median of 5 runs):
  ArrayDeque    3.6
  Stack        37.9
  LinkedList    6.7
```

`Stack` was ten times slower than `ArrayDeque`. Most of the difference points to the monitor:
`ArrayList` and `Vector`, whose code is close to the same, took 2.6 and 38.4 ns for the same add
and remove at the end. An uncontended `synchronized` call still acquires and releases the monitor;
biased locking, which once made that nearly free, was disabled in JDK 15 (JEP 374) and removed in
JDK 18. The lock doesn't make the stack thread-safe either: `if (!stack.isEmpty()) stack.pop()` is two
calls, and another thread can pop between them (unit 6.5 covers the concurrent deques).

### Which End a Deque Method Uses

`ArrayDeque` is both a stack and a queue, and the method names decide which. `push`, `pop`,
`peek` and `poll` all work on the head; `add` and `offer` work on the tail. Mixing the families
turns the stack into a queue:

```java
Deque<String> mixed = new ArrayDeque<>();
for (String s : new String[] {"a", "b", "c"}) mixed.add(s);   // add() is addLast()
System.out.println("add a, b, c then pop, pop, pop: "
        + mixed.pop() + " " + mixed.pop() + " " + mixed.pop());
```

```text
add a, b, c then pop, pop, pop: a b c
empty ArrayDeque: poll() -> null, peek() -> null
empty ArrayDeque: pop() -> java.util.NoSuchElementException
empty Stack: pop() -> java.util.EmptyStackException
ArrayDeque.push(null) -> java.lang.NullPointerException
Stack.push(null) -> [x, a, b, c, null]
```

On an empty deque `pop()` throws, and `poll()` and `peek()`
return `null`. `ArrayDeque` refuses `null` elements, because `null` is how `poll` says "empty";
`Stack` accepts them.

<!-- practice 1 -->

## Colliding HashMap Keys Stay Fast Only When They Are Comparable

An API gateway parses each request's JSON fields into a `HashMap`. `"Aa"` and `"BB"` have the
same `String.hashCode()`, so every string made of 13 such blocks has the same hash too: 8,192
distinct names, one hash code. An attacker can send all of them in one request. We look each
name up, once with `String` keys and once with the name wrapped in a record:

```java
record FieldName(String value) {}
```

```text
AaAaAaAaAaAaAaAaAaAaAaAaAa AaAaAaAaAaAaAaAaAaAaAaAaBB AaAaAaAaAaAaAaAaAaAaAaBBAa ...: hashCode 1256557376 1256557376 1256557376
distinct hash codes: 1
String       8192 keys: built in    6 ms,    138 ns per get
FieldName    8192 keys: built in  278 ms,  22482 ns per get
```

With the same names and hash codes, the record keys were 160 times slower per lookup. The first
build of the map, which the gateway does for every request, took 278 ms of CPU for about 210 KB of
field names.

Since Java 8, a bin of colliding keys turns into a red-black tree. The tree is ordered by hash
first; among keys with equal hashes, `HashMap` uses `compareTo` only when the key's class
implements `Comparable` of itself, as `String` does. A record wrapping a `String` isn't
`Comparable`, so the tree has nothing to descend by and a lookup may search both subtrees: on
average half the bin for a key that is there, all of it for one that isn't, as in the linked list
it replaced. Counting `equals` calls per `get`, with two key classes
whose `hashCode()` returns 42 and whose only difference is that the second implements
`Comparable<ComparableKey>`:

```text
key               keys     equals per get     ns per get
PlainKey           100               51.5             72
ComparableKey      100                7.2             31
PlainKey          1000              501.5            996
ComparableKey     1000               10.4             53
PlainKey         10000             5001.5          13456
ComparableKey    10000               13.9             75
record           10000        not counted              4
```

PlainKey compared half the bin per lookup; ComparableKey's count grew by about three per tenfold
n: linear against logarithmic. The last row is a record with a working `hashCode`, for scale. A
`compareTo` that returns 0 for two unequal keys gives the tree nothing to go on. At 1,000 keys,
a key class whose `compareTo` compares hash codes, and one that implements raw `Comparable`, both
needed 501.5 `equals` calls per get, the same as PlainKey. Ordering by the fields `equals`
compares is what helps.

Reading `HashMap`'s private table by reflection (with
`--add-opens java.base/java.util=ALL-UNNAMED`; unit 10.3.2 covers why that flag is needed) while
adding colliding keys to a new map:

```text
 7 keys: table  16 buckets, bin holds a Node
 8 keys: table  16 buckets, bin holds a Node
 9 keys: table  32 buckets, bin holds a Node
10 keys: table  64 buckets, bin holds a Node
11 keys: table  64 buckets, bin holds a TreeNode
12 keys: table  64 buckets, bin holds a TreeNode
```

A bin already holding 8 entries is converted on the next insert, but only in a table of at least
64 buckets; a smaller table is doubled instead. So the 9th and 10th keys resized the table and the
11th converted the bin.
The fix for the gateway is to keep `String` keys or make the wrapper
`implements Comparable<FieldName>`, and to cap the number of fields a request may carry.

<!-- practice 2 -->

## A PriorityQueue Is Ordered Only at Its Head

A scheduler keeps jobs in a `PriorityQueue`, and its status page prints the queue. Eight
priorities, added one at a time:

```java
PriorityQueue<Integer> pq = new PriorityQueue<>();
for (int v : new int[] {42, 7, 19, 3, 25, 11, 30, 5}) pq.add(v);
```

```text
toString():      [3, 5, 11, 7, 25, 19, 30, 42]
for-each:        [3, 5, 11, 7, 25, 19, 30, 42]
stream().limit(3): [3, 5, 11]
poll() until empty: [3, 5, 7, 11, 19, 25, 30, 42]
equal priorities, added t1..t6, polled: [t2:1, t4:1, t6:1, t1:2, t5:2, t3:2]
```

The queue is a binary heap in an array: each element is no larger than its two children, at
indexes `2i+1` and `2i+2`. `toString`, the iterator and `stream()`
walk that array as it is (the heap layout is from the JDK source; the javadoc promises only the
head), so a "next three jobs" panel built from `stream().limit(3)` shows 11
where 7 is due. Only `peek` and `poll` see the order, because `poll` moves the next-smallest
element to the root each time; that costs O(log n) per element. For the k smallest without
emptying the queue, poll from a copy or sort a snapshot; the `PriorityQueue` javadoc suggests
`Arrays.sort(pq.toArray())`.

The last line is jobs `t1` to `t6` with priorities 2, 1, 2, 1, 2, 1. Priority 2 came out
`t1, t5, t3`, not in the order added. The javadoc says ties are broken
arbitrarily. A queue that must be first-come-first-served within a priority needs a sequence
number in the comparator.

<!-- practice 3 -->

## TreeMap Answers "Which Key Is Nearest?", Compares with compareTo, and Returns Live Views

A price list keyed by the date each price takes effect: what was the price on 10 April? A
`HashMap` can only answer for the exact dates it holds. A `TreeMap` keeps its keys sorted and
answers questions about neighbours:

```java
NavigableMap<LocalDate, String> price = new TreeMap<>();   // price in effect from each date
price.put(LocalDate.of(2026, 1, 1), "10.00");
price.put(LocalDate.of(2026, 3, 1), "12.00");
price.put(LocalDate.of(2026, 6, 15), "12.50");
```

```text
floorEntry(2026-04-10)   2026-03-01=12.00
floorEntry(2026-03-01)   2026-03-01=12.00
lowerEntry(2026-03-01)   2026-01-01=10.00
ceilingEntry(2026-04-10) 2026-06-15=12.50
floorEntry(2025-12-31)   null
subMap(01-01, 06-15)     {2026-01-01=10.00, 2026-03-01=12.00}
subMap(.., true, .., true) {2026-01-01=10.00, 2026-03-01=12.00, 2026-06-15=12.50}
```

`floorEntry(d)` is "the price in effect on d": the greatest key at or below d.
`floor` and `ceiling` include an equal key and `lower` and `higher` don't, so on the change date
itself `lowerEntry` returns the old price. Before the first key, `floorEntry` returns `null`,
and code that calls `getValue()` on it fails with a `NullPointerException`. The two-argument
`subMap(from, to)` includes `from` and excludes `to`, like `List.subList`; the four-argument form
says which ends it wants.

`subMap`, `headMap` and `tailMap` return views, not copies:

```text
tailMap(03-01) after a put into price: {2026-03-01=12.00, 2026-06-15=12.50, 2026-09-01=13.00}
tailMap(03-01).put(2026-02-01): java.lang.IllegalArgumentException: key out of range
1.0 and 1.00: equals false, compareTo 0; HashSet size 2, TreeSet size 1
```

A key put into the map after the view was taken shows up in the view, and a key outside the
view's range can't be put through it. Copy the view (`new TreeMap<>(view)`) to keep a snapshot.

A `TreeMap` or `TreeSet` compares keys with `compareTo` (or
its comparator), not with `equals`. `BigDecimal` 1.0 and 1.00 are not equal, because their
scales differ, yet `compareTo` returns 0, so the `TreeSet` kept one of them and the `HashSet`
both. The `TreeMap` javadoc calls this an ordering inconsistent with equals: the map still works,
by its comparator's rules, and stops obeying the `Map` contract.

<!-- practice 4 -->

## Summary

- **LinkedList loses to ArrayList unless you already hold the position.** `add(index, x)` walks
  to the index first; only `ListIterator.add` during a walk is cheaper than ArrayList's array copy.
- **ArrayDeque is the stack and the queue; Stack is a synchronized Vector.** `Stack` iterates
  bottom-first and was ten times slower uncontended; on a deque, `add` plus `pop` is a queue.
- **Colliding HashMap keys stay fast only when they are Comparable.** Its tree bins order equal
  hashes by `compareTo`; without it a lookup searches through the bin, 160 times slower in our run.
- **A PriorityQueue is ordered only at its head.** Its iterator, `toString` and `stream` follow
  the heap array, and ties come out in no particular order.
- **TreeMap answers nearest-key questions with compareTo, and its range methods return live
  views.** `floor`/`ceiling` include the key, `subMap(a, b)` excludes b, and keys equal by
  `compareTo` are one key.

## Reference

**Which method uses which end of an `ArrayDeque` (from the `Deque` javadoc; null/exception rows ran):**

| Method | End | Edge |
|---|---|---|
| `push(e)` / `addFirst(e)` / `offerFirst(e)` | head | `null` throws `NullPointerException` |
| `add(e)` / `addLast(e)` / `offer(e)` | tail | `null` throws `NullPointerException` |
| `pop()` / `remove()` / `removeFirst()` | head | empty: throws `NoSuchElementException` |
| `poll()` / `pollFirst()` / `peek()` | head | empty: returns `null` |
| `pollLast()` / `peekLast()` | tail | empty: returns `null` |

**`HashMap` bins (JDK 21; constants from `javap -constants java.util.HashMap`, behaviour from the
run above):**

| Constant | Value | Effect |
|---|---|---|
| `TREEIFY_THRESHOLD` | 8 | a bin already holding 8 entries is converted on the next insert |
| `MIN_TREEIFY_CAPACITY` | 64 | below 64 buckets the table doubles instead |

**`NavigableMap` navigation (from the `NavigableMap` javadoc):**

| Method | Returns |
|---|---|
| `floorEntry(k)` / `ceilingEntry(k)` | greatest key ≤ k / least key ≥ k, or `null` |
| `lowerEntry(k)` / `higherEntry(k)` | greatest key < k / least key > k, or `null` |
| `firstEntry()` / `pollFirstEntry()` | lowest entry / removes and returns it |
| `headMap(k)` / `tailMap(k)` | keys < k / keys ≥ k, live views |
| `subMap(a, b)` | keys ≥ a and < b, a live view |
| `descendingMap()` | a live view in reverse order |

## Challenges

1. The ListIterator column made LinkedList win from 10,000 elements. Rewrite that edit for an
   `ArrayList` as one pass that builds a new list, and measure it with `Bench` against both.
2. The gateway can't change its key type this release. Which of these limits the damage most:
   a cap on fields per request, a cap on request size, or a different map? Measure one.
3. A scheduler needs "the next job" and "the next 20 jobs" often, and adds jobs constantly. A
   `PriorityQueue` with a sorted copy per panel refresh, or a `TreeSet` with a tie-breaking
   comparator? Decide what each refresh costs at 50,000 jobs.

## Design Note: Why Stack and LinkedList Are Still There

`Stack` and `Vector` date from Java 1.0, when the JDK's collection classes (`Vector`, `Hashtable`)
were synchronized. Java 1.2 added
the unsynchronized framework and Java 6 added `Deque` and `ArrayDeque`, whose javadoc says it is
"likely to be faster than `Stack` when used as a stack, and faster than `LinkedList` when used as
a queue." Neither old class was deprecated: removing a public class from the JDK breaks every
program that names it, and code written against `Vector`'s API in 1997 still compiles. The cost
is that the JDK's simplest name for a stack is the one to avoid, and `LinkedList`, which also
implements `Deque`, looks like a natural queue. In the push-and-pop run `ArrayDeque` was nearly
twice as fast as `LinkedList`, and it allocates no node per element.

## Code Examples

- Test: [CommonDataStructuresTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/datastructures/CommonDataStructuresTest.java)
- Source: [CommonDataStructures.java](src/main/java/com/github/msorkhpar/claudejavatutor/datastructures/CommonDataStructures.java)
