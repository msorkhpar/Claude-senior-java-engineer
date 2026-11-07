# 8.1.4. Interface Segregation Principle (ISP)

The nightly expiry sweep for the EU region failed at 02:00 with
`UnsupportedOperationException: replica is read-only`. A config change had pointed it at the read
replica instead of the primary. The change passed review and compiled: the replica implements the
same `ListingStore` interface as the primary, so the compiler had no reason to object.

The Interface Segregation Principle says a client should not depend on methods it doesn't use.
Imports are left out of the examples, and every output is from Java 21.

## A Fat Interface Compiles Calls Its Implementations Can Only Refuse

One interface serves `SearchPage`, which only reads, and `ExpirySweeper`, which reads and deletes.
`PrimaryStore` implements all four methods on a `HashMap`. `ReplicaStore` can't write, so it
refuses:

```java
record Listing(String id, boolean expired) {}

interface ListingStore {
    Optional<Listing> find(String id);
    List<String> ids();
    void save(Listing listing);
    void delete(String id);
}

final class ReplicaStore implements ListingStore {
    private final Map<String, Listing> copy;
    ReplicaStore(Map<String, Listing> snapshot) { copy = Map.copyOf(snapshot); }
    public Optional<Listing> find(String id) { return Optional.ofNullable(copy.get(id)); }
    public List<String> ids() { return List.copyOf(copy.keySet()); }
    public void save(Listing listing) { throw new UnsupportedOperationException("replica is read-only"); }
    public void delete(String id) { throw new UnsupportedOperationException("replica is read-only"); }
}

final class ExpirySweeper {
    private final ListingStore store;
    ExpirySweeper(ListingStore store) { this.store = store; }

    int sweep() {
        int removed = 0;
        for (String id : store.ids()) {
            if (store.find(id).map(Listing::expired).orElse(false)) {
                store.delete(id);
                removed++;
            }
        }
        return removed;
    }
}
```

The job picks its store by region, from configuration; `primary` and `replica` are the two stores:

```java
static ListingStore storeFor(String region) {
    return region.endsWith("-replica") ? replica : primary;
}
// ...
System.out.println("eu-primary: removed " + new ExpirySweeper(storeFor("eu-primary")).sweep());
System.out.println("eu-replica: removed " + new ExpirySweeper(storeFor("eu-replica")).sweep());
```

```text
eu-primary: removed 1
Exception in thread "main" java.lang.UnsupportedOperationException: replica is read-only
	at ReplicaStore.delete(Fat.java:30)
	at ExpirySweeper.sweep(Fat.java:41)
	at Fat.main(Fat.java:63)
```

The compiler checks that `ReplicaStore` has a `delete`, not what it does, so the mistake surfaced
at the first expired listing, at 02:00. Whether a refusing implementation is also a broken subtype is the question of
unit 8.1.3; here the interface made the stub necessary.

## Role Interfaces Turn the Refusal into a Compile Error

Split the interface by what its clients do with it. Reading is one role, writing another, and a
client that needs both names a type that extends both:

```java
interface ListingReader {
    Optional<Listing> find(String id);
    List<String> ids();
}

interface ListingWriter {
    void save(Listing listing);
    void delete(String id);
}

interface ListingStore extends ListingReader, ListingWriter {}

final class ReplicaStore implements ListingReader {
    private final Map<String, Listing> copy;
    ReplicaStore(Map<String, Listing> snapshot) { copy = Map.copyOf(snapshot); }
    public Optional<Listing> find(String id) { return Optional.ofNullable(copy.get(id)); }
    public List<String> ids() { return List.copyOf(copy.keySet()); }
}
```

`PrimaryStore implements ListingStore`, `SearchPage` takes a `ListingReader`, and the sweeper still
takes a `ListingStore`. The same wiring mistake:

```java
System.out.println("sweep on replica: removed " + new ExpirySweeper(replica).sweep());
```

```text
Roles.java:66: error: incompatible types: ReplicaStore cannot be converted to ListingStore
        System.out.println("sweep on replica: removed " + new ExpirySweeper(replica).sweep());
                                                                            ^
Note: Some messages have been simplified; recompile with -Xdiags:verbose to get full output
1 error
```

`ReplicaStore` has no `delete`, so it can't be passed to a client that deletes. Without that line,
the search page runs on the replica and the sweeper on the primary:

```text
search on replica: bike-17
sweep on primary: removed 1
```

Configuration code gets the same check: a `readerFor(region)` method declared to return
`ListingReader` can serve the replica regions, and javac rejects its result as the sweeper's
argument.

### Split by Client, Not by Method

A role is the set of methods one kind of client uses together. Split further, and the clients pay
instead. With one interface per method, the sweeper needs an intersection type and three fields
that all hold the same object:

```java
interface ListingFinder { Optional<Listing> find(String id); }
interface ListingIds { List<String> ids(); }
interface ListingRemover { void delete(String id); }

final class ExpirySweeper {
    private final ListingFinder finder;
    private final ListingIds index;
    private final ListingRemover remover;

    <S extends ListingFinder & ListingIds & ListingRemover> ExpirySweeper(S store) {
        this.finder = store;
        this.index = store;
        this.remover = store;
    }
    // sweep() as before, through the three fields
}
```

No implementation can refuse anything here either, so this split buys nothing over two roles.

<!-- practice 1 -->

## A Method Added to a Fat Interface Breaks Classes That Never Call It

Back in the fat version from the first section, the nightly export needs every listing, so someone
adds one method to `ListingStore` and implements it in `PrimaryStore`. The search page's unit test has a fake, which already stubs
`save` and `delete`:

```java
interface ListingStore {
    Optional<Listing> find(String id);
    List<String> ids();
    void save(Listing listing);
    void delete(String id);
    Stream<Listing> exportAll();                     // new: the nightly export needs it
}

/** The test fake for SearchPage, in src/test. */
final class FakeStore implements ListingStore {
    public Optional<Listing> find(String id) { return Optional.of(new Listing(id, false)); }
    public List<String> ids() { return List.of(); }
    public void save(Listing listing) { }
    public void delete(String id) { }
}
```

```text
FatGrows.java:27: error: ReplicaStore is not abstract and does not override abstract method exportAll() in ListingStore
final class ReplicaStore implements ListingStore {
      ^
FatGrows.java:59: error: FakeStore is not abstract and does not override abstract method exportAll() in ListingStore
final class FakeStore implements ListingStore {
      ^
2 errors
```

Neither the replica nor the search page exports anything, yet the replica gets a third stub and
the search page's fake a fourth empty method.

With roles, the export is a new role, `interface ListingExporter { Stream<Listing> exportAll(); }`,
implemented only by `PrimaryStore`. That version compiles with the replica, `SearchPage` and its
two-method `ListingReader` fake unchanged.

## A Default Method Keeps Implementations Compiling, and Runs Code That Never Saw Their Locks

Java 8 added `removeIf` to `Collection` as a default method that iterates with `iterator()` and
removes through it, so ordinary implementations kept compiling. That is the partial fix for growing
an interface: old implementations compile, and they inherit code written without them in mind. This
bag was written years earlier, with a lock on every method:

```java
/** Written in 2006: every method locks the bag; callers lock it around iteration. */
final class LockedBag<E> extends AbstractCollection<E> {
    private final List<E> items = new ArrayList<>();
    @Override public synchronized boolean add(E e) { return items.add(e); }
    @Override public synchronized boolean remove(Object o) { return items.remove(o); }
    @Override public synchronized int size() { return items.size(); }
    @Override public Iterator<E> iterator() { return items.iterator(); }   // caller holds the lock
}
```

A caller in 2015 runs `bag.removeIf(...)` on `[a, b, c]`, removing `b`. While the predicate looks
at `a`, a second thread calls `bag.add("d")` and is given 200 ms to finish. `LockedBag2` is the
same class with one more method:

```java
    @Override public synchronized boolean removeIf(Predicate<? super E> filter) { return items.removeIf(filter); }
```

The third row is the JDK's `Collections.synchronizedCollection`:

```text
LockedBag               other thread's add ran during removeIf: true; removeIf threw java.util.ConcurrentModificationException
LockedBag2              other thread's add ran during removeIf: false; bag = [a, c, d]
synchronizedCollection  other thread's add ran during removeIf: false; bag = [a, c, d]
```

The inherited `removeIf` iterated without the bag's lock, so the add landed in the middle. The
`ConcurrentModificationException` is the lucky outcome: the fail-fast check is best effort (unit
6.5.3), and a missed one can leave the list silently wrong. The JDK's synchronized wrapper overrides
`removeIf` and `forEach` to run them under its lock; its source marks them "Override default
methods in Collection". A default that throws `UnsupportedOperationException` instead,
as `SequencedCollection.addFirst` does, keeps everything compiling and moves the failure back to
run time, as in the first section.

<!-- practice 2 -->

## Collection Is Fat on Purpose, and Java 21's SequencedCollection Repeats It

The JDK's collection interfaces carry every mutator, and the `Collection` javadoc calls them
"optional operations": an implementation may refuse them. There is no read-only `List` type to ask
for, so a method that only reads a list still receives `add`, `set` and `sort`; unit 8.1.3 shows
such a method sorting its argument and failing on `List.of`.

Java 21 added `SequencedCollection`, a role for collections with an order. It puts `getFirst` and
`getLast` beside `addFirst`, `addLast`, `removeFirst` and `removeLast`, and its defaults for
`addFirst` and `addLast` throw. A read-only playlist that takes on the role, and four calls on it
(`attempt` runs a call and prints the exception it throws):

```java
/** A read-only playlist that opts in to Java 21's ordered-collection interface. */
final class Playlist extends AbstractCollection<String> implements SequencedCollection<String> {
    private final List<String> tracks;
    Playlist(List<String> tracks) { this.tracks = List.copyOf(tracks); }
    @Override public Iterator<String> iterator() { return tracks.iterator(); }
    @Override public int size() { return tracks.size(); }
    @Override public Playlist reversed() { return new Playlist(tracks.reversed()); }
}
```

```java
SequencedCollection<String> playlist = new Playlist(List.of("intro", "verse", "outro"));
System.out.println("getFirst: " + playlist.getFirst() + ", getLast: " + playlist.getLast());
attempt("addFirst(\"jingle\")", () -> playlist.addFirst("jingle"));
attempt("removeLast()", () -> playlist.removeLast());
```

```text
getFirst: intro, getLast: outro
addFirst("jingle"): java.lang.UnsupportedOperationException
removeLast(): java.lang.UnsupportedOperationException
```

javac accepted all four calls. The playlist can serve only the reads, and the role offers no type
that holds just those, so a client that needs the first and last track still depends on four
mutators, and the playlist refuses them at run time, as the replica did in the first section. In
your own interfaces, give the reads a role of their own.

<!-- practice 3 -->

## Summary

- **A fat interface compiles calls its implementations can only refuse.** A stub that throws
  turns a wiring mistake into a production failure half-way through a job.
- **Role interfaces turn the refusal into a compile error.** Split by the methods one client uses
  together; one interface per method makes clients carry intersection types.
- **A method added to a fat interface breaks classes that never call it**, test fakes included.
  A new role breaks nobody.
- **A default method keeps implementations compiling and runs code that never saw their
  invariants**, such as a lock. Override it where the class has rules the default can't know.
- **`Collection` is fat on purpose, and Java 21's `SequencedCollection` repeats it.** A read-only
  implementation compiles calls to `addFirst` and refuses them at run time.

## Reference

**JDK default methods (Java 21):**

| Method or factory | What it does |
|---|---|
| `Collection.removeIf` (default) | iterates with `iterator()`, removes with `Iterator.remove()`; takes no lock |
| `Collections.synchronizedCollection(c).removeIf` | overrides the default and holds the wrapper's lock |
| `SequencedCollection.getFirst`, `getLast` (default) | read through `iterator()` and `reversed().iterator()` |
| `SequencedCollection.addFirst`, `addLast` (default) | always throw `UnsupportedOperationException` |
| `SequencedCollection.removeFirst`, `removeLast` (default) | remove through the iterator, so a read-only iterator makes them throw |

## Challenges

1. A `PaymentGateway` interface has `charge`, `refund`, `cancel` and `settle`, and a mock in 40
   tests. Group its clients by the methods they call. How many roles do you get, and which test
   fakes shrink?
2. You maintain a library interface with 200 implementations outside your company and need one
   more method. Compare a default method, a new sub-interface, and a new role. What does each cost
   the implementers you can't see?
3. Sketch the split you would have given `SequencedCollection`. Which JDK classes would implement
   each part, and which client of yours would take each one?

## Design Note: Three Ways to Grow an Interface

An interface gains a method in one of three ways, and each has a different cost. An abstract
method breaks every implementation at compile time, which is honest and expensive when the
implementations belong to other teams. A default method breaks none of them at compile time, and
each one inherits behaviour its author never reviewed. A new role interface breaks nobody and
changes nothing, but only the clients that ask for it get the method. Java 8 chose defaults for
`Collection`; Java 21 chose a new role, `SequencedCollection`, then filled it with defaults that
throw, so the cost moved back to run time. Why the collection interfaces have optional operations
at all is the design note of unit 8.1.3.

## Code Examples

- Source: [InterfaceSegregation.java](src/main/java/com/github/msorkhpar/claudejavatutor/solidprinciples/InterfaceSegregation.java)
- Test: [InterfaceSegregationTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/solidprinciples/InterfaceSegregationTest.java)
