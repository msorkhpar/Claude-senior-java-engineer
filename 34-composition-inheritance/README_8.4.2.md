# 8.4.2. Applying Composition over Inheritance in Concurrent Programming

A webhook service keeps each customer's endpoint URLs in a list that every event walks. The list
class extends `Vector<String>` and adds one method, a `synchronized subscribe(url)` that adds the
URL only if it isn't there yet. Since events far outnumber new subscriptions, someone changes
`extends Vector<String>` to `extends CopyOnWriteArrayList<String>`, whose reads take no lock, and a
new nightly import job adds URLs with the list's own `addIfAbsent(url)`. It compiles, every test
passes, and a week later a customer reports receiving each order event twice: their URL is in the
list twice.

Every output below is from Java 21 on a 20-core machine.

## A Subclass's synchronized Method Excludes Only Code That Locks this

```java
static class Subscribers extends CopyOnWriteArrayList<String> {
    synchronized boolean subscribe(String url) {
        if (contains(url)) return false;
        validate(url);
        add(url);
        return true;
    }
}
```

`contains` and `add` are each atomic, and `subscribe` holds the subclass's monitor across both, so
two `subscribe` calls on the same object can't overlap. Which lock does the import's
`addIfAbsent` take?

In the run, `validate` waits on a latch when called from a thread named `subscribe`, so that
thread stops after its `contains` found nothing. The main thread then calls `addIfAbsent` for the
same URL:

```java
Thread subscriber = new Thread(() -> subs.subscribe(URL), "subscribe");
subscriber.start();
checked.await();                              // subscribe() holds the monitor, saw no URL
boolean imported = subs.addIfAbsent(URL);     // the import job, on the same list
resume.countDown();
subscriber.join();
```

```text
import's addIfAbsent: true, copies of the URL: 2
```

`addIfAbsent` didn't wait. `CopyOnWriteArrayList` locks an internal field,
`final transient Object lock`, and never `this`, so the subclass's `synchronized` excluded only
other `subscribe` calls. Every reading and writing operation of `Vector` locks `this`, so over
`Vector` the same subclass excluded every other caller. `Vector`'s javadoc says only that it "is
synchronized"; which object it locks is in its source.

Without the latch, eight threads add the same 2,000 URLs, half through `subscribe` and half
through `addIfAbsent`; a second test sends all of them through `subscribe`:

```text
subclass: subscribe() vs addIfAbsent()   duplicates in 5 runs: [2, 84, 1, 0, 1]
subclass: subscribe() only               duplicates in 5 runs: [0, 0, 0, 0, 0]
```

The window is the stretch between `contains` and `add`, and a run with no duplicates proves
nothing; the forced interleaving shows the race every time. A subclass also inherits every public method of the base,
and each one reaches the same data under the base's own lock: `add`, `set`, `addAllAbsent`,
`removeIf`. The subclass can't take them away. Unit 8.4.1 shows the same coupling without threads:
a `HashSet` subclass whose count depends on how `addAll` is written inside.

## A Helper Outside the Class Must Take the Lock the Class Takes

Each tenant's request handler gets its own service over one shared `Collections.synchronizedList`:

```java
static class SubscriptionService {
    private final List<String> urls;              // one Collections.synchronizedList, shared

    SubscriptionService(List<String> urls) {
        this.urls = urls;
    }

    synchronized boolean subscribe(String url) {
        if (urls.contains(url)) return false;
        validate(url);
        urls.add(url);
        return true;
    }
}
```

A second version's `subscribe` is not `synchronized` and wraps its body in
`synchronized (urls) { ... }` instead. Eight threads, one service each:

```text
helper locking itself, one per thread    duplicates in 5 runs: [183, 250, 209, 97, 624]
helper locking the list, one per thread  duplicates in 5 runs: [0, 0, 0, 0, 0]
```

The `synchronized` method locks the service, and there are eight services: the per-object lock
of unit 6.2.1's order counter. `Collections.synchronizedList`'s methods lock the returned
wrapper for one call each, which is why its javadoc tells callers to synchronize on it for iteration (unit 6.5.2).
Locking the collection a helper works on is called client-side locking, and it holds only while
the class keeps locking that object. Over a `CopyOnWriteArrayList` or a `ConcurrentHashMap`,
`synchronized (urls)` excludes none of the class's own methods.

<!-- practice 3 -->

## A Class That Owns the State and the Lock Doesn't Depend on What It Wraps

`SubscriberSet` holds the list as a private field and does its own locking:

```java
static final class SubscriberSet {
    private final Object lock = new Object();
    private final List<String> urls = new ArrayList<>();

    boolean subscribe(String url) {
        synchronized (lock) {
            if (urls.contains(url)) return false;
            validate(url);
            urls.add(url);
            return true;
        }
    }

    boolean unsubscribe(String url) {
        synchronized (lock) {
            return urls.remove(url);
        }
    }

    List<String> snapshot() {
        synchronized (lock) {
            return List.copyOf(urls);
        }
    }
}
```

The same latch, with the second caller another `subscribe` on a thread named `import`; the run
reads that thread's state and lock owner from `ThreadMXBean.getThreadInfo`:

```text
import: BLOCKED, lock owner: subscribe
copies of the URL: 1
```

```text
composed class with its own lock         duplicates in 5 runs: [0, 0, 0, 0, 0]
```

The inner list can be a plain `ArrayList`: no reference to it leaves the class, so every access
goes through `lock`, whatever the list's own locking is. Swapping the inner list for another type
touches no caller (unit 8.4.3).

Every operation callers need is a method you write: there is no
inherited `removeIf` or `iterator`. One lock now serializes readers too, so every event's walk
starts with a `snapshot()` copy taken under the lock, the cost the switch to
`CopyOnWriteArrayList` was meant to remove. And the guarantee holds only while the list stays
private: a constructor that stored a list the caller passed in, or a getter that returned it,
would give callers a path around the lock.

<!-- practice 1 -->

## When the Class Already Has the Compound Action, Call It

`CopyOnWriteArrayList` already had the action the subclass wrote:

```text
CopyOnWriteArrayList.addIfAbsent         duplicates in 5 runs: [0, 0, 0, 0, 0]
```

`addIfAbsent` is atomic against every other method of the list: it adds under the list's lock,
and checks again there if the array changed since its first look. A composed class that keeps a `CopyOnWriteArrayList` private and calls
`addIfAbsent` from `subscribe` needs no lock of its own, and its readers keep walking without one.

An API gateway keeps one rate limiter per client in a `ConcurrentHashMap`:

```java
static RateLimiter getThenPut(String client) {
    RateLimiter l = limiters.get(client);
    if (l == null) {
        l = new RateLimiter(client);
        limiters.put(client, l);
    }
    return l;
}

static RateLimiter computeIfAbsent(String client) {
    return limiters.computeIfAbsent(client, RateLimiter::new);
}
```

Eight threads ask for the same 10,000 clients; the run counts the limiters constructed and the
clients that were handed two different ones:

```text
get, then put    limiters created: 11145, clients given two limiters: 871
computeIfAbsent  limiters created: 10000, clients given two limiters: 0
```

Two more runs gave 1,286 and 10 split clients for get-then-put, and 0 for `computeIfAbsent`. A
split client is counted against two or more limiters (up to one per thread), so it gets a
multiple of its quota. The javadoc of `computeIfAbsent` states the guarantee: the whole call is
atomic, and the function runs exactly once per call if the key is absent, else not at all. Some
updates to the map by other threads may wait while it runs, so the function should be short, and
it must not modify the map.

Inheriting from `ConcurrentHashMap` to add a `synchronized` compound method fails as the list
subclass did: the map's own methods never lock the map object (unit 6.5.2). Removing a limiter has
the same shape. `remove(client, limiter)` removes the entry only if it still maps to that limiter,
so a caller holding a limiter that an admin reset already replaced leaves the new one in place:

```text
remove(key, stale limiter): false, current one still mapped: true
```

A plain `remove(client)` would have removed the new limiter. When no single call expresses the
action, such as a limiter and its quota in two maps changing together, a composed class's own lock
has to guard both maps, and the maps' own concurrency then buys nothing.

### putIfAbsent Builds the Value Before It Knows It Is Needed

`putIfAbsent` is atomic too, and a third version uses it correctly, returning the winner:

```java
static RateLimiter putIfAbsent(String client) {
    RateLimiter fresh = new RateLimiter(client);
    RateLimiter old = limiters.putIfAbsent(client, fresh);
    return old != null ? old : fresh;
}
```

```text
putIfAbsent      limiters created: 80000, clients given two limiters: 0
```

No client was split, but every call built a limiter, and 70,000 were thrown away: the value is an
argument, so it exists before the map sees the key. Where the constructor registers a metric,
reads a quota from a database or opens a connection, that work runs on every lookup, not once per
client. `putIfAbsent` fits a value that already exists or costs nothing to make.

<!-- practice 2 -->

## Summary

- **A subclass's `synchronized` excludes only code that locks `this`.** Over a base that locks a
  private object, like `CopyOnWriteArrayList`, the compound action races with every inherited
  write, and a stress test may miss it.
- **A helper must take the lock the class itself takes.** Locking the helper excludes nothing;
  locking the collection works only where its contract names that lock, as
  `Collections.synchronizedList` does.
- **A class that owns its state and its lock is correct whatever it wraps**, while no reference to
  the state escapes. It pays with methods you write and one lock for readers too.
- **When the class already has the compound action, call it.** `addIfAbsent`,
  `computeIfAbsent` and `remove(key, value)` are atomic against the class's own methods;
  `putIfAbsent` builds its value on every call.

## Reference

**Which lock a thread-safe JDK class takes (Java 21):**

| Class | Its methods lock | `synchronized (obj)` from outside |
|---|---|---|
| `Vector`, `Hashtable` | `this` (in the source; the javadoc says "synchronized") | excludes their methods |
| `Collections.synchronizedList/Map/Set` | the returned wrapper (the javadoc tells callers to lock it for iteration) | excludes their methods |
| `CopyOnWriteArrayList` | an internal `lock` field, for writes and sub-list views | excludes none of their methods |
| `ConcurrentHashMap` | per bin, for writes; never the map object | excludes none of their methods |

**Atomic compound actions the JDK provides:**

| Class | Methods |
|---|---|
| `CopyOnWriteArrayList` | `addIfAbsent`, `addAllAbsent` |
| `ConcurrentHashMap` | `putIfAbsent`, `computeIfAbsent`, `computeIfPresent`, `compute`, `merge`, `remove(key, value)`, `replace(key, old, new)` |
| `Collections.synchronizedMap` | the same `Map` methods, each under the wrapper's lock |

## Challenges

1. The subscriber list must also cap each customer at five URLs. Can `CopyOnWriteArrayList`'s own
   methods express "add if absent and fewer than five", and if not, what does the composed class
   cost its readers?
2. Run the `subscribe()` vs `addIfAbsent()` stress test with 64 threads and with `validate`
   removed. How many runs does it take to see one duplicate, and what does that say about load
   tests as proof of thread safety?
3. A team wants `TracingConcurrentHashMap extends ConcurrentHashMap` that logs every `put`. Which
   inherited methods add entries without calling `put`, and how would a composed version avoid
   the question?

## Design Note: Why the Newer Collections Keep Their Locks Out of Reach

`Vector` and `Hashtable` lock `this`, which makes client-side locking and subclassing work, and
also lets any holder of a reference stall every caller (unit 6.2.1). The `java.util.concurrent`
classes lock something no caller can reach: `CopyOnWriteArrayList` an internal object,
`ConcurrentHashMap` its bins. Their locking stays an implementation detail they can change. The
price is that every compound action a caller needs has to be a method on the class, since there
is no lock to extend from outside: `CopyOnWriteArrayList` has `addIfAbsent`, but no "add if absent
and below a limit".

## Code Examples

- Source: [CompositionInConcurrency.java](src/main/java/com/github/msorkhpar/claudejavatutor/compositioninheritance/CompositionInConcurrency.java)
- Test: [CompositionInConcurrencyTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/compositioninheritance/CompositionInConcurrencyTest.java)
