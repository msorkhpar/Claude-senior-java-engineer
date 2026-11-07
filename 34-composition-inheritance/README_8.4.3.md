# 8.4.3. Benefits of Composition over Inheritance for Concurrency

The login service keeps its sessions in `MapSessions`, a small class with a private `HashMap`.
Since the request handlers moved onto more threads, the "active sessions" gauge reads tens of
thousands low, and now and then a user who has just logged in is told the session doesn't exist.
The fix is one line inside `MapSessions`. What else has to change, and what has to be checked
again? Every output below is from Java 21 on a 20-core machine.

## Callers That Know Only the Wrapper Run Unchanged When Its HashMap Becomes a ConcurrentHashMap

The handlers see an interface, and the map is a private field of the class behind it:

```java
/** What the login service's handlers know about the session store. */
public interface Sessions {
    void open(String id, String user);
    String user(String id);           // null when there is no such session
    boolean close(String id);
    int count();
}
```

```java
import java.util.HashMap;
import java.util.Map;

public final class MapSessions implements Sessions {
    private final Map<String, String> byId = new HashMap<>();

    public void open(String id, String user) { byId.put(id, user); }
    public String user(String id)            { return byId.get(id); }
    public boolean close(String id)          { return byId.remove(id) != null; }
    public int count()                       { return byId.size(); }
}
```

A load program, `LoginLoad`, plays eight request threads. Each logs in 25,000 users, checks right
away that its own session is there, and logs every second one out again, so 100,000 sessions
should stay open:

```java
for (int i = 0; i < 25_000; i++) {
    String id = th + "-" + i;
    sessions.open(id, "user-" + i);
    if (sessions.user(id) == null) notFound.incrementAndGet();   // just logged in
    if (i % 2 == 1) sessions.close(id);                         // logs out again
}
```

We compile everything once and run the load five times. Then we change one line of
`MapSessions` to `new ConcurrentHashMap<>()` (and its import), recompile that file alone, and run
the same `LoginLoad.class` five more times. Each block's first line gives the first 16 hex digits
of the SHA-256 of each caller's class file (`GuestLogin` comes in the next section):

```text
LoginLoad.class 24c79f54405ae471, GuestLogin.class 7f2b8782a0ed5e4f, MapSessions over HashMap:
open sessions: 52999 of 100000, own session not found: 19, threads failed: [], stuck after 3 s: 0
open sessions: 38066 of 100000, own session not found: 24, threads failed: [], stuck after 3 s: 5
open sessions: 51588 of 100000, own session not found: 25, threads failed: [ClassCastException], stuck after 3 s: 1
open sessions: 54761 of 100000, own session not found: 40, threads failed: [], stuck after 3 s: 0
open sessions: 36576 of 100000, own session not found: 32, threads failed: [], stuck after 3 s: 6

LoginLoad.class 24c79f54405ae471, GuestLogin.class 7f2b8782a0ed5e4f, MapSessions over ConcurrentHashMap:
open sessions: 100000 of 100000, own session not found: 0, threads failed: [], stuck after 3 s: 0
open sessions: 100000 of 100000, own session not found: 0, threads failed: [], stuck after 3 s: 0
open sessions: 100000 of 100000, own session not found: 0, threads failed: [], stuck after 3 s: 0
open sessions: 100000 of 100000, own session not found: 0, threads failed: [], stuck after 3 s: 0
open sessions: 100000 of 100000, own session not found: 0, threads failed: [], stuck after 3 s: 0
```

Over `HashMap` the store loses sessions, misses ones just written, and leaves threads looping
inside it (unit 6.5.2 shows why). Over `ConcurrentHashMap` every run is exact, and the caller's
bytes are the same. A compiled call names the method it calls, `Sessions.open(String, String)`,
and never the type of a private field, so nothing a caller was compiled against moved.

The same store written as a subclass has no private part to change. Here a nightly report copies
it with `clone()`, which `HashMap` makes public:

```java
import java.util.HashMap;

/** The same store, written as a subclass. */
public class SessionTable extends HashMap<String, String> {
}
```

```java
SessionTable table = new SessionTable();
table.put("s-1", "user-1");
@SuppressWarnings("unchecked")
Map<String, String> copy = (Map<String, String>) table.clone();
```

After `extends HashMap` becomes `extends ConcurrentHashMap` and only `SessionTable` is
recompiled, the old report class fails when it runs, and recompiling it fails too:

```text
Exception in thread "main" java.lang.IllegalAccessError: class NightlyReport tried to access protected method 'java.lang.Object java.util.AbstractMap.clone()' (NightlyReport is in unnamed module of loader 'app'; java.util.AbstractMap is in module java.base of loader 'bootstrap')
	at NightlyReport.main(NightlyReport.java:9)
```

```text
work/NightlyReport.java:9: error: clone() has protected access in AbstractMap
        Map<String, String> copy = (Map<String, String>) table.clone();
                                                              ^
1 error
```

A subclass publishes every public method of its superclass, so the superclass is part of what
each caller compiled against, and changing it means finding every caller. Units 8.4.1 and 8.4.2
show the coupling in the other direction: a subclass that depends on how its base calls its own
methods, and one that locks a different object than its base.

## The Swap Is Free Only for What the Wrapper Promises: HashMap Accepted null, ConcurrentHashMap Throws

Both builds also ran `GuestLogin`, compiled once with the load:

```java
Sessions sessions = new MapSessions();
sessions.open("s-guest", null);
System.out.println("count: " + sessions.count() + ", logged in: " + (sessions.user("s-guest") != null));
```

Over `HashMap`, then over `ConcurrentHashMap`:

```text
count: 1, logged in: false
```

```text
Exception in thread "main" java.lang.NullPointerException
	at java.base/java.util.concurrent.ConcurrentHashMap.putVal(ConcurrentHashMap.java:1011)
	at java.base/java.util.concurrent.ConcurrentHashMap.put(ConcurrentHashMap.java:1006)
	at MapSessions.open(MapSessions.java:7)
```

Over `HashMap` the guest's session was stored and then looked missing, since `user` returns
`null` for both. Over `ConcurrentHashMap` the same call throws from inside the map, in a class
nobody edited. The `Map` interface allows both: its javadoc says some implementations prohibit
null keys and values and throw when given one, and `ConcurrentHashMap`'s says it allows neither.

A wrapper hides an implementation only where its own contract is narrower than every
implementation it may hold. `MapSessions` decides about `null` itself, in every method; `open`
and `user`:

```java
public void open(String id, String user) {
    byId.put(Objects.requireNonNull(id, "id"), Objects.requireNonNull(user, "user"));
}
public String user(String id)   { return byId.get(Objects.requireNonNull(id, "id")); }
```

The guest login over both backings now prints the same thing, pointing at the caller's line
(unit 8.5.1):

```text
Exception in thread "main" java.lang.NullPointerException: user
	at java.base/java.util.Objects.requireNonNull(Objects.java:259)
	at MapSessions.open(MapSessions.java:9)
	at GuestLogin.main(GuestLogin.java:5)
```

Iteration is the other difference. A method that returned the live map, or a view of it, would
give callers a fail-fast iterator over `HashMap` and a weakly consistent one over
`ConcurrentHashMap` (unit 6.5.3). A wrapper that lists its contents returns a copy, such as
`Map.copyOf(byId)`.

<!-- practice 1 -->

## One Metrics Decorator Serves Every Implementation, and Its Own Counters Must Be Thread-Safe

Operations wants call counts and latency for the store. A decorator implements `Sessions`, holds
another `Sessions`, and measures each call it passes on:

```java
import java.util.concurrent.atomic.LongAdder;

/** Adds call counts and timings to any Sessions. */
public final class MeteredSessions implements Sessions {
    private final Sessions inner;
    private final LongAdder calls = new LongAdder();
    private final LongAdder nanos = new LongAdder();

    public MeteredSessions(Sessions inner) { this.inner = inner; }

    public void open(String id, String user) {
        long start = System.nanoTime();
        try { inner.open(id, user); } finally { record(start); }
    }
    public String user(String id) {
        long start = System.nanoTime();
        try { return inner.user(id); } finally { record(start); }
    }
    public boolean close(String id) {
        long start = System.nanoTime();
        try { return inner.close(id); } finally { record(start); }
    }
    public int count() {
        long start = System.nanoTime();
        try { return inner.count(); } finally { record(start); }
    }

    private void record(long start) {
        calls.increment();
        nanos.add(System.nanoTime() - start);
    }

    public long calls()     { return calls.sum(); }
    public long meanNanos() { long c = calls.sum(); return c == 0 ? 0 : nanos.sum() / c; }
}
```

`new MeteredSessions(new MapSessions())` measures the store over either map, and wraps a test
fake as well. Measuring by subclassing would take one subclass per class measured.

Its first version kept `private long calls;` and counted with `calls++`. Eight threads made
800,000 calls through each version, over the `ConcurrentHashMap` store, five runs each:

```text
long counters:     calls made 800000, counted 588550, sessions 400000
long counters:     calls made 800000, counted 587597, sessions 400000
long counters:     calls made 800000, counted 584955, sessions 400000
long counters:     calls made 800000, counted 589093, sessions 400000
long counters:     calls made 800000, counted 584595, sessions 400000
LongAdder counters: calls made 800000, counted 800000, sessions 400000
LongAdder counters: calls made 800000, counted 800000, sessions 400000
LongAdder counters: calls made 800000, counted 800000, sessions 400000
LongAdder counters: calls made 800000, counted 800000, sessions 400000
LongAdder counters: calls made 800000, counted 800000, sessions 400000
```

The store kept all 400,000 sessions while the metrics lost a quarter of the calls. A decorator
has its own fields, every thread that uses the store uses them, and the thread-safety of what it
wraps doesn't extend to them. `calls++` reads, adds and writes, and two
threads that read the same value write the same result (unit 6.2.1). `LongAdder` or `AtomicLong`
makes each update atomic. `LongAdder` keeps several cells under contention and adds them up when
read, so eight counting threads don't all fight over one variable.

<!-- practice 2 -->

## A Synchronizing Decorator Protects Only the Calls That Go Through It

A decorator can add a lock instead of a different map:

```java
/** Runs every call on the wrapped Sessions under one lock. */
public final class SynchronizedSessions implements Sessions {
    private final Sessions inner;
    private final Object lock = new Object();

    public SynchronizedSessions(Sessions inner) { this.inner = inner; }

    public void open(String id, String user) { synchronized (lock) { inner.open(id, user); } }
    public String user(String id)            { synchronized (lock) { return inner.user(id); } }
    public boolean close(String id)          { synchronized (lock) { return inner.close(id); } }
    public int count()                       { synchronized (lock) { return inner.count(); } }
}
```

`HashMapSessions` is `MapSessions` with its original `HashMap` field and the null checks.
Wrapped, it kept every session in all five runs of the first section's load:

```text
SynchronizedSessions over HashMapSessions: open sessions 100000 of 100000
```

Reads take the lock too: in the first section, a `user` call that ran beside another thread's
`put` missed sessions its own thread had just written.

`Collections.synchronizedMap(m)` is the JDK's version of this class. It returns a `Map` that
holds `m` and runs each `Map` method under one lock, the wrapper object itself, over any `Map`. A
`TreeMap` inside still walks in key order, but the wrapper is only a `Map`: it is not a
`SortedMap`, which is why `synchronizedSortedMap` exists. A walk is many calls, so it must hold
the wrapper's lock (unit 6.5.2). The javadoc adds a condition: all access to the backing map goes through the
returned map.

Here the expiry job was built before the wrap and kept the `HashMap` itself. Seven request
threads put 25,000 sessions each through the wrapper while the job removes 50,000 expired ones
through its own reference:

```java
Map<String, String> backing = new HashMap<>();
for (int i = 0; i < 50_000; i++) backing.put("old-" + i, "user-" + i);
Map<String, String> sessions = Collections.synchronizedMap(backing);
Map<String, String> expiryView = escape ? backing : sessions;   // what the expiry job was given
```

```text
expiry job given the HashMap: found 175000 of 175000 new, 0 expired left, size() 175335
expiry job given the HashMap: found 175000 of 175000 new, 0 expired left, size() 175362
expiry job given the HashMap: found 175000 of 175000 new, 0 expired left, size() 175420
expiry job given the HashMap: found 175000 of 175000 new, 0 expired left, size() 175577
expiry job given the HashMap: found 174999 of 175000 new, 0 expired left, size() 175234
expiry job given the wrapper: found 175000 of 175000 new, 0 expired left, size() 175000
expiry job given the wrapper: found 175000 of 175000 new, 0 expired left, size() 175000
expiry job given the wrapper: found 175000 of 175000 new, 0 expired left, size() 175000
expiry job given the wrapper: found 175000 of 175000 new, 0 expired left, size() 175000
expiry job given the wrapper: found 175000 of 175000 new, 0 expired left, size() 175000
```

The wrapper's lock kept its own callers apart and nobody else: the job's removes ran beside the
puts. `size()`, the number on the gauge, was wrong in every run, by up to 577, and one session
vanished in one run. Any reference to the inner object that outlives the wrap is a path around
the lock. `new SynchronizedSessions(new HashMapSessions())` leaves no such reference.

### Which Layer Holds the Lock Decides What the Metrics Measure

Stacking both decorators gives two orders, and both compile and pass a single-threaded test. In
the second, threads call `insideShared` and the metrics are read from `inside`:

```java
MeteredSessions outside = new MeteredSessions(new SynchronizedSessions(new HashMapSessions()));
MeteredSessions inside = new MeteredSessions(new HashMapSessions());
Sessions insideShared = new SynchronizedSessions(inside);
```

Eight threads made 200,000 calls each through each stack, three rounds:

```text
metrics outside the lock: mean 1163 ns/call   metrics inside the lock: mean  79 ns/call
metrics outside the lock: mean  951 ns/call   metrics inside the lock: mean  70 ns/call
metrics outside the lock: mean  826 ns/call   metrics inside the lock: mean  55 ns/call
```

The store did the same work. Outside the lock the timer also runs while a caller waits for the
lock, which is the latency a request sees. Inside, it is how long the store holds the lock, the
number to check before blaming the map. These are one machine's numbers; the ratio depends on
cores and contention.

<!-- practice 3 -->

## Summary

- **Callers that see only the wrapper's methods survive a change of what it wraps.** Replacing a
  private `HashMap` with a `ConcurrentHashMap` changed no caller's class file and ended the lost
  sessions; the same change to a subclass of `HashMap` broke a caller at run time and at compile
  time.
- **The swap is free only within the wrapper's own contract.** Maps differ on `null` and on
  iteration; a wrapper that rejects `null` itself and returns copies behaves the same over all of
  them.
- **A decorator's own state needs its own thread-safety.** `long` counters in a metrics decorator
  lost a quarter of the calls over a correct store; `LongAdder` counted every one.
- **A synchronizing decorator protects only the calls that go through it.** `Collections.synchronizedMap`
  miscounted once another reference to its map was used. Stacked with metrics, its position
  decides whether the timings include waiting for the lock.

## Reference

**What a map accepts and how it iterates (Java 21, run single-threaded):**

| Map | `put(k, null)` | `put(null, v)` | `get(null)` | `put` during a walk |
|---|---|---|---|---|
| `HashMap`, `LinkedHashMap` | accepted | accepted | returns the value or `null` | `ConcurrentModificationException` |
| `TreeMap` (natural order) | accepted | `NullPointerException` | `NullPointerException` | `ConcurrentModificationException` |
| `ConcurrentHashMap`, `ConcurrentSkipListMap` | `NullPointerException` | `NullPointerException` | `NullPointerException` | walk continues |
| `Collections.synchronizedMap(m)` | as `m` | as `m` | as `m` | as `m` |

**Decorators in the JDK** (each wraps any implementation of its interface):

| Factory | Adds | Condition in its javadoc |
|---|---|---|
| `Collections.synchronizedMap/List/Set` | one lock around each call | all access through the returned object; hold its lock while walking it |
| `Collections.unmodifiableMap/List/Set` | rejects changes with `UnsupportedOperationException` | a view: reads go through to the backing collection, so its changes show |
| `Collections.checkedMap/List/Set` | a type check on insert | guaranteed only while all access goes through the returned object |

## Challenges

1. `MapSessions` must also list the sessions of one user. Can you add that without making a swap
   back to `HashMap` (for a single-threaded batch tool) unsafe, and what does the copy cost on
   100,000 sessions?
2. Write a test that would have caught the `long` counters in `MeteredSessions`. How many threads
   and calls does it need to fail every time on your machine, and on a two-core CI runner?
3. A cache needs eviction (`LinkedHashMap` in access order) and sharing between threads. Compare
   `Collections.synchronizedMap` over it with a `ConcurrentHashMap` plus your own eviction: what
   does each cost readers, given that `get` on an access-ordered `LinkedHashMap` moves the entry?

## Design Note: Why the JDK Wraps Instead of Subclassing

Java 1.2 could have shipped `SynchronizedHashMap`, `SynchronizedTreeMap` and so on, one subclass
per map. It shipped `Collections.synchronizedMap`, one private class that holds any `Map`, and
the same for unmodifiable and checked collections. One class covers every implementation,
including ones written later, and none of them has to be designed for extension. The design
pattern behind it is the decorator of unit 9.2.2. It costs in two ways. A decorator exposes only
the interface it implements, so a wrapped `TreeMap` stops being a `SortedMap` and the JDK needed
`synchronizedSortedMap` and `synchronizedNavigableMap` as well. And a decorator can't control who
else holds the object inside, so its guarantee depends on how callers use it, as the javadoc
conditions say; a compound action still needs its lock or a single call such as `merge`. `ConcurrentHashMap`, added in Java 5, is the other answer: the thread-safety is built
into the implementation, with no lock to hold from outside.

## Code Examples

- Source: [CompositionBenefits.java](src/main/java/com/github/msorkhpar/claudejavatutor/compositioninheritance/CompositionBenefits.java)
- Test: [CompositionBenefitsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/compositioninheritance/CompositionBenefitsTest.java)
