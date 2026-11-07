# 6.5.1. Common Collections and Their Implementations

The tenant-settings cache is a `HashMap<String, String>`, and a setting a tenant cleared is
stored as `null`, so the cache can tell it from one never set. A reviewer asks for thread safety, and the change is one line:
`settings = new ConcurrentHashMap<>(settings)`. The next deploy fails at startup with a
`NullPointerException` from that line.

Both classes implement `Map`, and the interface leaves `null`, order and cost to each
implementation. Swapping one for another changes those promises along with the speed.

## A Collection Costs What Its Structure Makes It Cost: ArrayList by Index, ArrayDeque at the Ends

The same call costs a different amount in each implementation, and in a loop the difference
multiplies. An export sums 100,000 order amounts by index. Pause before the output: how much slower is the
same loop over a `LinkedList`? The second measurement uses a list as a queue, taking from the
front:

```java
static long sumByIndex(List<Integer> orders) {
    long sum = 0;
    for (int i = 0; i < orders.size(); i++) sum += orders.get(i);
    return sum;
}

while (!queue.isEmpty()) queue.remove(0);      // an ArrayList of 100,000
while (deque.poll() != null) { }               // an ArrayDeque of 100,000
```

```text
get(i) over 100,000:    ArrayList 0 ms, LinkedList 3098 ms
take 100,000 from the front: ArrayList.remove(0) 145 ms, ArrayDeque.poll() 0 ms
```

An `ArrayList` is an array: `get(i)` is one address computation. A `LinkedList` is a chain of
nodes, and `get(i)` walks from the nearer end, so the loop makes about n²/4 hops: 2.5 billion
here. `LinkedList`'s constant-time insert needs a position you already
hold, through a `ListIterator`; getting there is the same walk.

Taking from the front is the opposite case: `remove(0)` shifts every remaining element down by
one. `ArrayDeque` is a circular array that only moves its head index, so `poll()` costs the same
at any size.

Both loops are quadratic: ten times the rows makes them about a hundred times slower. The code
that hits this rarely builds the list. It takes a `List` parameter, and one caller passes a
`LinkedList`. Loop with for-each, which walks either list once, or check for the
`RandomAccess` marker interface, which `ArrayList` implements and `LinkedList` doesn't. For a
queue or a stack, use `ArrayDeque`.

## A Hash Set Iterates in Hash Order, and the Order Changes as the Set Grows

A `HashSet` promises no order at all. An importer drops repeated airport codes with a `HashSet` and then writes the codes out:

```java
List<String> first = List.of("LIS", "OSL", "ROM", "BER", "PAR");
Set<String> hash = new HashSet<>(), linked = new LinkedHashSet<>();
hash.addAll(first); linked.addAll(first);
hash.addAll(List.of("MAD", "VIE", "DUB", "AMS", "BRU", "CPH", "HEL", "ATH", "WAW", "PRG"));
```

```text
added:         [LIS, OSL, ROM, BER, PAR]
HashSet:       [PAR, ROM, LIS, OSL, BER]
LinkedHashSet: [LIS, OSL, ROM, BER, PAR]
HashSet after 10 more, the first five: [PAR, OSL, BER, ROM, LIS]
```

A `HashSet` keeps its elements in a table of buckets chosen by `hashCode()`, and iterates by
walking the table. For the same elements and table size the order is the same on every run, so it
looks stable while the set is small. The default table has 16 buckets and doubles when it passes
three-quarters full, 12 elements; then the elements are spread over twice as many buckets, and the same five codes come out in
another order (unit 6.5.4 explains the resize). A test written with five codes passes, and
production, with fifteen, writes them in a different order.

When order matters, say which one: `LinkedHashSet` keeps the order of first insertion, and
`add` of an element already there doesn't move it (Java 21's `addFirst` and `addLast` do). `TreeSet` and
`ConcurrentSkipListSet` keep sorted order.

<!-- practice 1 -->

## ConcurrentHashMap Refuses null, So That null from get Means Absent

`ConcurrentHashMap` throws on a `null` key or value, where `HashMap` stores it. The settings map
from the start of the unit, and a few other collections (`attempt` runs a call and prints how it
ended):

```java
Map<String, String> settings = new HashMap<>();
settings.put("timezone", "UTC");
settings.put("proxy", null);                 // the tenant cleared it
System.out.println("get(\"proxy\") = " + settings.get("proxy") + ", containsKey = " + settings.containsKey("proxy"));
System.out.println("get(\"locale\") = " + settings.get("locale") + ", containsKey = " + settings.containsKey("locale"));

attempt("new ConcurrentHashMap<>(settings)", () -> new ConcurrentHashMap<>(settings));
attempt("ConcurrentSkipListSet.add(null)", () -> new ConcurrentSkipListSet<String>().add(null));
attempt("ArrayDeque.add(null)", () -> new ArrayDeque<String>().add(null));
attempt("CopyOnWriteArrayList.add(null)", () -> new CopyOnWriteArrayList<String>().add(null));
attempt("ConcurrentHashMap.get(null)", () -> new ConcurrentHashMap<String, String>().get(null));
```

```text
get("proxy") = null, containsKey = true
get("locale") = null, containsKey = false
new ConcurrentHashMap<>(settings): NullPointerException
ConcurrentSkipListSet.add(null): NullPointerException
ArrayDeque.add(null): NullPointerException
CopyOnWriteArrayList.add(null): ok
ConcurrentHashMap.get(null): NullPointerException
```

A `HashMap` returns `null` both for a key mapped to `null` and for a missing key, and
`containsKey` tells them apart. In a map that other threads write, that takes two calls, and
another thread can put or remove the key between them, so the two answers describe different
moments. `ConcurrentHashMap` refuses `null` keys and values, which makes `get` returning `null`
mean "absent" on its own. The skip-list collections and the concurrent queues refuse `null` for
the same reason, and so does `ArrayDeque`, which isn't concurrent, through `add` and `offer` alike: `poll()` returning `null` must
mean "empty". The copy-on-write classes and the synchronized wrappers accept it.

Storing `null` let the cache tell "the tenant cleared this" from "never set, use the default".
The quick fix is to store `""` for "cleared". Then a tenant whose order-number suffix really is
empty reads back as cleared. A stand-in for `null` has to be a value no caller can store: a private
object in a `Map<String, Object>`, or an `Optional<String>` value type.

<!-- practice 2 -->

## CopyOnWriteArrayList Copies Its Array on Every Write, So Readers Iterate a Snapshot

A deploy event goes to a list of listeners. The first listener runs once: it removes itself, and
the audit listener with it, while the event is being delivered:

```java
Consumer<String> once = new Consumer<>() {
    public void accept(String e) {
        System.out.println("  once got " + e + ", removes itself and audit");
        listeners.remove(this);
        listeners.remove(audit);
    }
};
listeners.add(once);                         // listeners is an ArrayList, then a CopyOnWriteArrayList
listeners.add(audit);
for (Consumer<String> l : listeners) l.accept("deploy-1");
```

```text
ArrayList:
  once got deploy-1, removes itself and audit
  ConcurrentModificationException
CopyOnWriteArrayList:
  once got deploy-1, removes itself and audit
  audit got deploy-1
  listeners left: 0
```

Every write to a `CopyOnWriteArrayList` copies the array, changes the copy and swaps it in. An
iterator keeps the array it started with, so it takes no lock, a writer never waits for it, it
never throws `ConcurrentModificationException`, and it doesn't see a change made after it began. That is why `audit` heard the event after being removed: removal
takes effect from the next event.

That fits a listener list, written a few times at startup and read on every event; unit 6.5.2
measures what each write costs, and unit 6.5.3 shows how the iterator behaves.

<!-- practice 3 -->

## A Sorted Set Answers Ranges Through Views Backed by the Set

A stock report lists the SKUs in a band. `ConcurrentSkipListSet` is the concurrent counterpart of
`TreeSet`: sorted, a `NavigableSet`, with O(log n) expected cost. Is `D-400` in the band below?

```java
ConcurrentSkipListSet<String> skus = new ConcurrentSkipListSet<>(List.of("B-200", "A-100", "D-400", "C-300", "b-250"));
NavigableSet<String> band = skus.subSet("B-200", "D-400");
skus.add("C-350");
try { band.add("E-500"); } catch (IllegalArgumentException e) { /* printed below */ }
List<String> copy = List.copyOf(band);
skus.remove("C-300");
```

```text
set:                     [A-100, B-200, C-300, D-400, b-250]
subSet(B-200, D-400):    [B-200, C-300]
subSet(.., true, .., true): [B-200, C-300, D-400]
after skus.add(C-350):   [B-200, C-300, C-350]
band.add(E-500):         IllegalArgumentException
after remove(C-300): view [B-200, C-350], copy [B-200, C-300, C-350]
```

The two-argument `subSet` includes its lower bound and excludes its upper one, so adjacent bands
don't overlap. The four-argument form sets each bound. `b-250` sorts last because natural `String`
order compares character codes, and lower case comes after upper case.

`subSet`, `headSet` and `tailSet` return live views, not copies. A view shows later changes to the
set, writes through to it, and refuses an element outside its range. A report that returns the
view and prints a total first and the lines later can print lines that don't add up to the total.
A result that must stay as it was returns `List.copyOf(view)`.

<!-- practice 4 -->

<!-- practice 5 -->

## Summary

- **A collection costs what its structure makes it cost.** `get(i)` on a `LinkedList` walks, and
  `remove(0)` on an `ArrayList` shifts; both turn a loop quadratic. Use `ArrayDeque` for queues.
- **A hash set iterates in hash order**, which changes when the table resizes. Ask for the order
  you need with `LinkedHashSet` or a sorted set.
- **`ConcurrentHashMap` refuses `null`**, as do the skip-list collections, the concurrent queues
  and `ArrayDeque`, so `null` from `get` or `poll` means absent or empty. A stand-in for `null` must be a value no caller can store.
- **`CopyOnWriteArrayList` copies on every write.** Iteration takes no lock and walks a snapshot that
  ignores later changes; writes cost the whole array.
- **A sorted set's ranges are views**, half-open by default, that follow the set.

## Reference

Java 21. Costs are the javadoc's; `null` handling is from a run.

| Class | Iteration order | `null` | Cost | Thread-safe |
|---|---|---|---|---|
| `ArrayList` | index | yes | `get(i)` constant; add or remove at the front or middle linear | no |
| `LinkedList` | index | yes | ends constant; `get(i)` linear | no |
| `ArrayDeque` | head to tail | no | most operations amortized constant | no |
| `CopyOnWriteArrayList` | index | yes | every write copies the array | yes |
| `HashSet`, `HashMap` | none promised | yes, keys and values | basic operations constant, given a good hash | no |
| `LinkedHashSet`, `LinkedHashMap` | first insertion, unless moved by `addFirst`/`addLast` or `putFirst`/`putLast` | yes | basic operations constant | no |
| `TreeSet`, `TreeMap` | sorted | no elements or keys with natural ordering; values yes | log(n) | no |
| `ConcurrentSkipListSet`, `ConcurrentSkipListMap` | sorted | no elements, keys or values | expected log(n) | yes |
| `ConcurrentHashMap` | none promised | no keys, no values | a hash table, like `HashMap` | yes |

## Challenges

1. A method takes a `List<Row>` and loops over it by index. You can't control what callers pass.
   Which change do you make, and how do you test that it worked?
2. Your registry has 10,000 subscribers and gains one per second. Measure whether
   `CopyOnWriteArrayList` is still the right choice, and what you would replace it with.
3. A report must list the distinct customer ids from today's events in a stable order. Compare
   `LinkedHashSet`, `TreeSet` and sorting a `HashSet`'s contents: which order does each give,
   and which does the reader of the report expect?

## Design Note: One Interface, Different Promises

The collection interfaces let a method accept any `List` or `Map`, and let a caller swap
implementations without touching it. The cost is that a parameter typed `Map` tells you nothing
about the promises this unit compared: whether `null` is allowed, what order iteration gives,
what `get` costs, or whether another thread may write. Code that relies on one of those relies on
something the type doesn't say, and the one-line swap from the start of the unit breaks it. Name
the promise you depend on, in the type (`SequencedSet`, `NavigableSet`, `ConcurrentMap`) where
one exists, and in the documentation where none does.

## Code Examples

- Test: [CommonCollectionsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/concurrentcollections/CommonCollectionsTest.java)
- Source: [CommonCollections.java](src/main/java/com/github/msorkhpar/claudejavatutor/concurrentcollections/CommonCollections.java)
