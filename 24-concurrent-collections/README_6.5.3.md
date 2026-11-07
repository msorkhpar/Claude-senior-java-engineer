# 6.5.3. Order of Operations in Concurrent Collections

A nightly job walks the list of open sessions, an `ArrayList`, and calls `sessions.remove(s)` for
every expired session. Some mornings the log shows a `ConcurrentModificationException` and the job
stopped part-way. Other mornings the log is clean, yet an expired session is still in the list.
The job runs on one thread. Someone proposes switching to a concurrent collection.

Whether that helps depends on what each kind of iterator checks while it walks. Every output
below is from Java 21.

## A Fail-Fast Iterator Checks a Counter on Every Step, and One Thread Is Enough to Trip It

Three changes to a `HashMap` of balances, each made inside a walk over the same map:

```java
for (Map.Entry<String, Integer> e : balances.entrySet()) {
    e.setValue(e.getValue() + 1);                  // changes a value
}
for (String name : balances.keySet()) {
    balances.put(name, 0);                         // replaces a value: key already there
}
System.out.println("setValue and put(existing key): " + balances);
try {
    for (String name : balances.keySet()) {
        balances.put(name + "-old", 0);            // adds a key
    }
} catch (ConcurrentModificationException e) {
    System.out.println("put(new key): " + e);
}
```

```text
setValue and put(existing key): {ann=0, bob=0, cy=0}
put(new key): java.util.ConcurrentModificationException
```

No second thread is involved: "concurrent" means concurrent with the walk. `ArrayList`,
`HashMap`, `HashSet` and `TreeMap` keep a counter, `modCount`, that every **structural** change
increments: adding or removing an element, or `clear()`. An iterator copies the counter when it
is created, and `next()` throws once the two differ. Replacing the value of an existing key isn't
structural, so it isn't counted.

The iterator throws because it can no longer trust its position. An `ArrayList` iterator holds an
index, and a removal shifts every later element down one place. A `HashMap` iterator holds the
next node to return, and an insert can resize the table and move nodes between buckets. Carrying
on would skip elements or return one twice.

The session job's `remove(s)` is structural. The step after the first removal throws, and the
sessions the job had not reached are never examined.

<!-- practice 1 -->

## The Check Is Best-Effort: Some Changes Slip Past It

A list of four jobs, and the loop removes `j3` when it reaches it:

```java
List<String> jobs = new ArrayList<>(List.of("j1", "j2", "j3", "j4"));
List<String> seen = new ArrayList<>();
for (String job : jobs) {
    seen.add(job);
    if (job.equals("j3")) jobs.remove(job);
}
System.out.println("seen " + seen + ", left " + jobs);
```

```text
seen [j1, j2, j3], left [j1, j2, j4]
```

Nothing was thrown, and `j4` was never visited. The check sits in `next()`, but `hasNext()` only
compares the iterator's index, 3, with the list's size, now also 3, so the loop ends before
`next()` can look at the counter. Remove the last element instead and `hasNext()` answers `true`,
so that one throws. What matters is where the walk is, not which element goes: removing `j1` while
the walk is on `j3` skips `j4` in the same way. That is the session job's clean morning: the
expired session that came last was never examined.

Across threads nothing is promised. The counter is a plain field, not `volatile`, and the javadoc
of `ArrayList` says fail-fast iterators throw "on a best-effort basis" and should be used "only to
detect bugs". A walk that ends without an exception proves nothing, and a
`catch (ConcurrentModificationException e)` that retries recovers only from the cases the check
happened to catch.

## Change a Collection During Its Own Walk Only Through the Iterator, or with removeIf

The usual way around the iterator is an index loop. Here three versions remove `bad` from three
copies of a list in which two matches sit next to each other:

```java
List<String> byIndex = new ArrayList<>(List.of("ok", "bad", "bad", "ok", "bad"));
List<String> byIterator = new ArrayList<>(byIndex);
List<String> byRemoveIf = new ArrayList<>(byIndex);
for (int i = 0; i < byIndex.size(); i++) {
    if (byIndex.get(i).equals("bad")) byIndex.remove(i);
}
for (Iterator<String> it = byIterator.iterator(); it.hasNext(); ) {
    if (it.next().equals("bad")) it.remove();
}
byRemoveIf.removeIf(s -> s.equals("bad"));
```

```text
index loop:      [ok, bad, ok]
Iterator.remove: [ok, ok]
removeIf:        [ok, ok]
```

The index loop throws nothing and is still wrong. Removing at `i` moves the next element to `i`,
and `i++` steps over it, so the second of two adjacent matches survives.

`Iterator.remove()` removes the element `next()` just returned, on an `ArrayList` moves the
iterator's index back, and copies the new counter into the iterator, so the next step's check passes. `removeIf` does
the walk for you; on an `ArrayList` it removes all matches in one pass instead of shifting the
tail once per match. Maps have the same tools on their views, such as
`map.values().removeIf(...)`. For the session job it is one line,
`sessions.removeIf(s -> s.expiresAt() < now)`, and its single thread never needed a concurrent
collection.

To add while walking, collect the additions in a second collection and add them after the loop.

<!-- practice 2 -->

## A CopyOnWriteArrayList Iterator Walks the Array It Started With

An event bus keeps its listeners in a `CopyOnWriteArrayList`, so a listener may subscribe or
unsubscribe during a dispatch. On `logout`, the session listener removes the audit listener and
adds another, `late`:

```java
Listener session = ev -> {
    log.add("session got " + ev);
    if (ev.equals("logout")) {
        listeners.remove(audit);                   // the session closes its audit trail
        listeners.add(late);
    }
};
listeners.add(session);
listeners.add(audit);
for (Listener l : listeners) l.on("logout");
for (Listener l : listeners) l.on("next");
```

```text
session got logout
audit got logout
session got next
late got next
```

The audit listener had already been removed, and it still got the event. `late` was added
before that dispatch ended, and didn't get it.

As unit 6.5.1 showed, every write to a `CopyOnWriteArrayList` copies the array. An iterator keeps
the array that was current when it was created and walks it to the end. It never throws
`ConcurrentModificationException`, takes no lock, and never sees a later change. A listener that
closes its resource when it is removed must therefore expect one more call.

That array is read-only, so the iterator can't change it either:

```java
Iterator<Listener> it = listeners.iterator();
it.next();
it.remove();
```

```text
Iterator.remove: java.lang.UnsupportedOperationException
```

Remove through the list (`remove`, `removeIf`), which makes a new copy.

<!-- practice 3 -->

## A ConcurrentHashMap Walk Never Throws, and It Is Not a Snapshot

`ConcurrentHashMap` walks the live table instead. Its iterators are **weakly consistent**: in the
words of the `java.util.concurrent` package javadoc, they never throw
`ConcurrentModificationException`, "are guaranteed to traverse elements as they existed upon
construction exactly once, and may (but are not guaranteed to) reflect any modifications
subsequent to construction". A single-threaded walk over the keys `k0` to `k99` adds one new key
for each of them:

```java
for (String k : map.keySet()) {
    walked++;
    if (k.startsWith("k")) map.put("new-" + k, 0);
}
System.out.println("walk saw " + walked + ", map has " + map.size());
```

```text
walk saw 140, map has 200
```

The walk saw 40 of the 100 new keys, and no rule says which.

One thread moves money between 100 accounts of
1,000 each, crediting first and debiting second, so every state the map ever holds totals at
least 100,000. Another thread totals the balances by walking:

```java
Thread transfers = new Thread(() -> {
    Random r = new Random(42);
    while (!stop.get()) {
        String from = "acct-" + r.nextInt(100), to = "acct-" + r.nextInt(100);
        accounts.merge(to, 10, Integer::sum);         // credit first...
        accounts.merge(from, -10, Integer::sum);      // ...then debit
    }
});
transfers.start();
int below = 0, low = Integer.MAX_VALUE;
for (int walk = 0; walk < 1_000; walk++) {
    int total = 0;
    for (int balance : accounts.values()) total += balance;
    if (total < 100_000) below++;
    low = Math.min(low, total);
}
```

```text
1000 totals: 276 below 100000, lowest 99950
```

A walk read one account before a credit reached it and another after a debit left it, and added
up a total the map never held. The counts vary from run to run; the low totals don't go away. If
a number must hold across several keys, stop the writers while you
read, or keep the number in one place that each update changes atomically.
`ConcurrentSkipListMap`, `ConcurrentSkipListSet` and `ConcurrentLinkedQueue` iterate the same
way, the skip-list ones in sorted order.

<!-- practice 4 -->

## Summary

- **A fail-fast iterator checks a counter on every step.** A structural change made through the
  collection, even by the same thread, fails the next step; replacing a value doesn't.
- **The check is best-effort.** A removal made while an `ArrayList` walk is on its second-to-last
  element skips the last one silently, and across threads nothing is promised: use the exception to find
  bugs, never to drive logic.
- **Change a collection during its walk only through the iterator, or with `removeIf`.** A
  forward index loop skips the element after each removal.
- **A `CopyOnWriteArrayList` iterator walks the array it started with.** It never throws, never
  sees later changes, and its `remove()` throws `UnsupportedOperationException`.
- **A `ConcurrentHashMap` walk never throws and is not a snapshot.** A total computed by walking
  can be one the map never held.

## Reference

**What trips a fail-fast iterator (Java 21, a change made inside a for-each over the same
collection):**

| Change | Result |
|---|---|
| `ArrayList`: `add`; `remove` while the walk is anywhere but the second-to-last element | `ConcurrentModificationException` |
| `ArrayList`: `remove` while the walk is on the second-to-last element | no exception; the last element is skipped |
| `ArrayList`: `set` | no exception |
| `ArrayList`: `sort`, `replaceAll` | `ConcurrentModificationException` |
| `ArrayList.forEach(action)` or `stream().forEach(action)` whose action adds or removes | `ConcurrentModificationException` |
| `HashMap`: `put` of an existing key, `entry.setValue`, `replaceAll` | no exception |
| `HashMap`, `TreeMap`: `put` of a new key | `ConcurrentModificationException` |
| `LinkedHashMap` in access order: `get` | `ConcurrentModificationException` |
| `Collections.synchronizedList`: `add` | `ConcurrentModificationException` (unit 6.5.2) |

**Iterators by collection:**

| Collection | Iterator | Sees changes made after it started | `Iterator.remove()` |
|---|---|---|---|
| `ArrayList`, `HashMap`, `HashSet`, `TreeMap` | fail-fast | structural ones: throws at the next step instead | yes |
| `List.of`, `Set.of`, `Map.of` | unmodifiable | no change is possible | `UnsupportedOperationException` |
| `CopyOnWriteArrayList`, `CopyOnWriteArraySet` | snapshot | never | `UnsupportedOperationException` |
| `ConcurrentHashMap` | weakly consistent | maybe | yes |
| `ConcurrentSkipListMap`, `ConcurrentSkipListSet` | weakly consistent, sorted | maybe (ran: walking `[b, d, f]`, adding `a` and `e` at `b`: saw `b, d, e, f`) | yes |
| `ConcurrentLinkedQueue` | weakly consistent | maybe (ran: elements added at the tail were seen) | yes |

## Challenges

1. A cache's eviction thread walks a `ConcurrentHashMap` and removes entries older than an hour
   while request threads add new ones. Can an entry that expires during the walk survive it? What
   would you measure to find out how often, and does it matter?
2. Your listener list changes once a minute and is read 50,000 times a second, and a listener
   must never get an event after it was removed. Compare a `CopyOnWriteArrayList` with a check
   before each call, and an `ArrayList` behind a `ReadWriteLock`.
3. A report must total the balances in a `ConcurrentHashMap` that keeps changing. Find three ways
   to get a total the map really held, and what each costs the writers.

## Design Note: Why Fail Fast Instead of Coping

`ArrayList` and `HashMap` could have tolerated changes during a walk, by re-finding the position
after each change or by copying on write. Both would cost every caller all the time, for a case
that is almost always a bug. The counter costs one increment per change and one comparison per
step, and turns the bug into an exception at the next step, close to its cause.

The price is that people learn to rely on the exception, because it catches the common
single-threaded mistake most of the time, and it promises nothing. The concurrent collections made the
opposite trade: they never throw, and each chose what a walk sees, the past for copy-on-write and
a mix of past and present for the rest. Neither kind promises that what you read was true at a
single moment.

## Code Examples

- Test: [IterationBehaviorTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/concurrentcollections/IterationBehaviorTest.java)
- Source: [IterationBehavior.java](src/main/java/com/github/msorkhpar/claudejavatutor/concurrentcollections/IterationBehavior.java)
