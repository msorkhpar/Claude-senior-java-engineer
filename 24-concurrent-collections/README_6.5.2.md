# 6.5.2. Thread Safety of Common Data Structures

The ops dashboard counts requests per endpoint and minute in a map that every request thread
writes. It was a `HashMap`. After a week of low counts, someone wrapped it in
`Collections.synchronizedMap(...)`. The counts are still low, and the hourly export that walks the
map now fails with `ConcurrentModificationException`.

Java has two ways to share a collection: a wrapper that locks each call, and the
`java.util.concurrent` collections. Neither makes a sequence of your calls atomic. Every output
below is from Java 21 on a 20-core machine.

## A Plain HashMap Shared by Writers Loses Entries and Can Hang

Four threads put 25,000 keys each into one `HashMap`; `raceFor` returns how many are still
running after 2,000 ms:

```java
Map<Integer, Integer> sessions = new HashMap<>();
int stuck = raceFor(4, t -> {
    for (int i = 0; i < 25_000; i++) sessions.put(t * 25_000 + i, i);   // keys never repeat
}, 2_000);
```

Five runs:

```text
run 1: 100000 distinct keys put, size() = 40934, 3 threads still in put() after 2 s
  a thread ended with java.lang.ClassCastException
  a thread ended with java.lang.ClassCastException
  a thread ended with java.lang.ClassCastException
  a thread ended with java.lang.ClassCastException
run 2: 100000 distinct keys put, size() = 2690
run 3: 100000 distinct keys put, size() = 45610, 1 threads still in put() after 2 s
  a thread ended with java.lang.ClassCastException
  a thread ended with java.lang.ClassCastException
  a thread ended with java.lang.ClassCastException
  a thread ended with java.lang.ClassCastException
run 4: 100000 distinct keys put, size() = 624
run 5: 100000 distinct keys put, size() = 75477
```

One thread alone keeps every distinct key, colliding or not, but different threads still share the
map's table. When it fills, `put` moves every entry into a larger one, and threads that link
entries or resize at the same time overwrite each other's work. Entries vanish without an error. The table is left in a shape that later calls fail on
(`ClassCastException` from inside `HashMap`) or loop in: those threads were still inside `put()`
two seconds later, each keeping a core busy, and a request thread caught there never answers.
`ArrayList`, `HashSet` and the other mutable `java.util` collections break the same way, with lost
elements and exceptions from inside the collection.

## A Synchronized Wrapper Makes Each Call Atomic, Not a Sequence of Calls

The dashboard's counter, eight threads, 10,000 hits each over four endpoints:

```java
Integer old = hits.get(e);
hits.put(e, old == null ? 1 : old + 1);
```

and the same count in one call:

```java
hits.merge(endpoints[i % 4], 1, Integer::sum);
```

```text
synchronizedMap   get, then put: 45285 of 80000
ConcurrentHashMap get, then put: 37417 of 80000
synchronizedMap   merge:        80000 of 80000
ConcurrentHashMap merge:        80000 of 80000
```

The wrapper locks each `get` and each `put`, and releases the lock between them. Two threads read
41, both write 42, and one hit is gone. This is the race of unit 6.2.1's flash sale, on a map. Moving
to `ConcurrentHashMap` changes nothing while the code still makes two calls.

`merge`, `compute`, `computeIfAbsent` and `putIfAbsent` read and write in one call, and both maps
run each of them atomically. Make every read-modify-write on a shared map one of those calls.

<!-- practice 1 -->

## Walking a Wrapper Is Many Calls: Hold the Wrapper's Lock for the Whole Walk

A report walks a `synchronizedList` of 1,000 events 10,000 times while a writer adds the newest
event and removes the oldest: once without a lock, once holding the list's lock, once over a
`CopyOnWriteArrayList`:

```java
synchronized (events) {
    for (String e : events) n++;
}
```

```text
no lock              {ConcurrentModificationException=9997, finished=3}
synchronized (list)  {finished=10000}
CopyOnWriteArrayList {finished=10000}
```

A for-each loop calls `iterator()`, then `hasNext()` and `next()` per element, and the wrapper
locks none of them: its `iterator()` hands back the `ArrayList`'s own iterator, which notices the
change and throws. Only the wrapper's one-call walks, `forEach` and `removeIf`, hold its lock.
The wrapper's lock is the wrapper object itself, which is why `synchronized (events)` excludes
every writer that goes through it. Locking anything else, such as `this` of the reporting class,
excludes nobody.

The lock has a price: every writer waits for the whole walk. A thread that called `add` during a
locked walk of 2,000,000 elements returned after 37 ms. For a long walk, copy the list
(`new ArrayList<>(events)` copies through the wrapper's locked `toArray()`) and walk the copy. A
map's copy (`new HashMap<>(map)`) walks the map, so it goes inside the block. Unit 6.5.3 shows what the
iterators of the concurrent collections see instead of throwing.

<!-- practice 2 -->

## A Thread-Safe Map Guards Its Entries, Not the Objects Stored in Them

A chat service keeps each room's messages in a `ConcurrentHashMap<String, List<String>>`. Eight
threads post ten messages each into two rooms, in 1,000 rounds, in three ways:

```java
List<String> list = a.get(room);                      // get, put if null
if (list == null) {
    list = Collections.synchronizedList(new ArrayList<>());
    a.put(room, list);
}
list.add(msg);

b.computeIfAbsent(room, r -> new ArrayList<>()).add(msg);

c.computeIfAbsent(room, r -> Collections.synchronizedList(new ArrayList<>())).add(msg);
```

```text
get, put if null                   80000 posted,  263 lost,  0 threads ended with an exception
computeIfAbsent, ArrayList         80000 posted,  602 lost, 10 threads ended with an exception
computeIfAbsent, synchronizedList  80000 posted,    0 lost,  0 threads ended with an exception
```

How does the first lose messages when every list is synchronized? Two threads find no list and
each creates one; the second `put` replaces the first list with the messages already in it.
`computeIfAbsent` runs the factory at most once per missing key and gives every caller the same
list.

The second creates the list once, then eight threads call `add` on one `ArrayList`; over the
1,000 rounds it threw `ArrayIndexOutOfBoundsException` from inside `add` ten times, ending that
posting thread. The
map's atomicity covers finding or creating the entry, and ends before `add` runs. A value that
threads change after they get it is shared state of its own, and needs its own thread safety: a
synchronized or concurrent collection, or an `AtomicInteger`. Removing an entry once its value is
empty has the same gap: a thread that fetched the value before the removal adds to an object the
map no longer holds. Do the removal and the add inside `compute`, so the entry and its value
change together.

<!-- practice 3 -->

## CopyOnWriteArrayList Copies the Whole Array on Every Write

Each write to a `CopyOnWriteArrayList` copies the array, changes the copy, and publishes it. Reads
and walks use whichever array was current, with no lock. One thread, 100,000 adds, then 1,000 walks
of the full list (the synchronized list walked under its lock):

```text
CopyOnWriteArrayList  100000 adds:  1110 ms   1000 full walks:   55 ms
SynchronizedRandomAccessList 100000 adds:     2 ms   1000 full walks:  157 ms
```

The cost of an add grows with the list: the last one copied 99,999 elements. In return, a walk
never waits and never throws (unit 6.5.3 shows what it sees).

That fits a list of listeners or routes, read on every event and changed at deploy time. An event
buffer that grows on every request belongs in a synchronized list or a queue.

<!-- practice 4 -->

## Summary

- **A plain `HashMap` or `ArrayList` shared by writers loses entries** (even with distinct keys),
  and can throw or hang inside the collection.
- **A synchronized wrapper makes each call atomic, not a sequence.** A read-modify-write is one
  call (`merge`, `compute`), on either map.
- **Walking a wrapper takes many calls.** Hold the wrapper's own lock for the walk, and every
  writer waits for it.
- **A thread-safe map guards its entries, not their values.** Create values with
  `computeIfAbsent`, and make a shared value thread-safe itself.
- **`CopyOnWriteArrayList` copies on every write.** It suits lists read far more often than
  written.

## Reference

| | `Collections.synchronizedMap/List` | `ConcurrentHashMap` | `CopyOnWriteArrayList` |
|---|---|---|---|
| Locking | the wrapper object, for every call | per bin, for updates; retrievals generally don't lock | one lock, for writes only |
| `merge`, `compute`, `putIfAbsent` | atomic, under the wrapper's lock | atomic | `addIfAbsent` atomic |
| Walking while others write | hold `synchronized (wrapper)`, else the backing iterator's behaviour (fail-fast for `ArrayList`, `HashMap`) | weakly consistent, no lock (unit 6.5.3) | snapshot, no lock; `iterator().remove()` throws `UnsupportedOperationException` |
| `null` keys or values | as the backing collection allows | `NullPointerException` | allowed |
| `size()` while others write | exact, under the lock | a transient value; `mappingCount()` gives it as a `long` | the current array's length |

**Edge cases (Java 21):**

| Situation | What happens |
|---|---|
| `computeIfAbsent` whose function returns `null` | no entry is created, the call returns `null` |
| `ConcurrentHashMap.computeIfAbsent` whose function updates the same key | `IllegalStateException: Recursive update`; the javadoc forbids updating any other mapping too |
| `Hashtable`, `Vector` | every method `synchronized`, like a wrapper; `Hashtable` rejects `null` |
| `synchronized (concurrentHashMap)` held by another thread | `put` and `merge` go ahead |

## Challenges

1. The export must see one consistent minute of counts while 200 threads keep counting. Compare
   locking a wrapper for the export, copying under the lock, and swapping in a fresh map each
   minute. What does each cost the request threads?
2. Replace the `merge` counter with `computeIfAbsent(e, k -> new LongAdder()).increment()` and
   measure both with 64 threads on one key. When is the difference worth a less obvious type?
3. A listener registry gets one listener added per user session. At what size does
   `CopyOnWriteArrayList` stop being the right choice, and what do you use then?

## Design Note: Why a ConcurrentHashMap Can't Be Locked From Outside

A synchronized wrapper locks on itself, so a caller can extend its atomicity: `synchronized (map)`
around a check and a put makes them one step for every thread that also goes through the wrapper.
The price is one lock for the whole map, so readers wait for writers and for each other.

`ConcurrentHashMap` gave up that lock to let threads work on different bins at once. Its methods
never lock the map object, so `synchronized (map)` around a sequence excludes only code that does
the same. What a caller can't build from outside, the map provides as single calls: `merge`,
`compute`, `computeIfAbsent`, `putIfAbsent`, `replace(key, old, new)`. A sequence that none of them
expresses needs your own lock around every access to the map, and then the map's concurrency buys
nothing.

## Code Examples

- Test: [ThreadSafetyExamplesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/concurrentcollections/ThreadSafetyExamplesTest.java)
- Source: [ThreadSafetyExamples.java](src/main/java/com/github/msorkhpar/claudejavatutor/concurrentcollections/ThreadSafetyExamples.java)
