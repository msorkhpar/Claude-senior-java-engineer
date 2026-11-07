# 7.1.1. Java 8 (LTS)

A service moves from Java 7 to Java 8. One class that compiled for years stops compiling, with
`inherits unrelated defaults for spliterator() from types Set and List`. After the fix it ships, and
a week later the cache-size metric reads 1 while the cache holds thousands of entries. The metric
counts calls to the cache's `put`, and the new code writes with `merge`.

Neither failure is a bug in the old code. Java 8 added methods to interfaces and classes that old
code already implemented and extended. Lambdas, streams and `java.time` have their own units,
listed in the last section; this page covers default and static interface methods meeting existing
classes, and `Optional`. Every output is from Java 21; code marked `--release 8` was also compiled against the
Java 8 API.

## The JDK's New Defaults Reach Classes Written Before Them, and Two of Them Can Collide

Java 8 gave `Collection` a `spliterator()` default so that every collection could be streamed, and
`List` and `Set` each override it with their own default. A class that is both, an insertion-ordered
set written on top of `AbstractList`, now inherits two:

```java
import java.util.*;

/** A list of tags that refuses duplicates: an ordered set. */
public class OrderedTags extends AbstractList<String> implements Set<String> {
    private final List<String> items = new ArrayList<>();

    @Override public String get(int i) { return items.get(i); }
    @Override public int size() { return items.size(); }
    @Override public void add(int i, String tag) { if (!items.contains(tag)) items.add(i, tag); }
    @Override public String remove(int i) { return items.remove(i); }
}
```

```text
OrderedTags.java:4: error: types Set<E#1> and List<E#2> are incompatible;
public class OrderedTags extends AbstractList<String> implements Set<String> {
       ^
  class OrderedTags inherits unrelated defaults for spliterator() from types Set and List
  where E#1,E#2 are type-variables:
    E#1 extends Object declared in interface Set
    E#2 extends Object declared in interface List
1 error
```

`--release 8` gives the same error. Neither default is more specific than the
other, so Java won't pick one: the class must override the method. Unit 2.3.1 has the full rules,
including the `IncompatibleClassChangeError` an old class file that isn't rebuilt gets instead,
when `spliterator()` is first called.

The quick fix is `Set.super.spliterator()`, but it reports only `Set`'s characteristics. What a
plain `List` (`ArrayList`) reports, what `Set.super` reports, and an override that states both:

```java
@Override public Spliterator<String> spliterator() {
    return Spliterators.spliterator(this, Spliterator.ORDERED | Spliterator.DISTINCT);
}
```

```text
ArrayList's spliterator(): [ORDERED, SIZED, SUBSIZED]
Set.super.spliterator():  [DISTINCT, SIZED, SUBSIZED]
own override:             [ORDERED, DISTINCT, SIZED, SUBSIZED]
```

Without `ORDERED`, a stream over the tags has no encounter order, and the `Stream` javadoc then
lets `findFirst()` return any of them. `List.super.spliterator()` doesn't compile here: `not an
enclosing class: List`, and once the class names `List` too, `redundant interface List is extended
by AbstractList`. `X.super` reaches only an interface the class names directly and that no other
supertype already extends.

## A Method Java 8 Added to a Collection Need Not Go Through Your Overrides

The cache from the opening extends `HashMap` and counts writes in `put`. The same calls ran on it
and on a map built from `AbstractMap` with the same counter. An `ArrayList` subclass counting in
`remove` held `[x, y, z, y]` and ran `remove("x")`, then `removeIf(s -> s.equals("y"))`:

```java
static class CountingMap<K, V> extends HashMap<K, V> {
    int writes;
    @Override public V put(K key, V value) { writes++; return super.put(key, value); }
}

CountingMap<String, Integer> hits = new CountingMap<>();
hits.put("a", 1);
hits.merge("a", 1, Integer::sum);
hits.computeIfAbsent("b", k -> 1);
hits.putIfAbsent("c", 1);
hits.compute("a", (k, v) -> v + 1);
```

```text
HashMap subclass:     size 3, writes counted 1 {a=3, b=1, c=1}
AbstractMap subclass: size 3, writes counted 5 {a=3, b=1, c=1}
ArrayList subclass:   [z], removals counted 1 of 3
```

`Map`'s defaults for `merge`, `compute`,
`computeIfAbsent` and `putIfAbsent` are written with `get` and `put`, and their javadoc spells that
out, so the `AbstractMap` version counted all five. `HashMap` overrides them with its own code,
which writes to its table directly. `ArrayList.removeIf` removes without calling
`remove`, so the counter missed both `y`s.

A subclass that guards a concrete collection's mutators covers only the methods that existed when
it was written. Java 8 added `putIfAbsent`, `merge`, `compute*`, `replace`, `replaceAll`, `removeIf` and `sort`,
and Java 21 added more (unit 7.1.5). Override each new mutator so that it goes through your hook,
or extend `AbstractMap` or `AbstractList` around the real collection, where the defaults call your
methods.

<!-- practice 1 -->

## Static Interface Methods Belong to the Interface: Implementers Don't Inherit Them

Java 8 also allowed static interface methods, such as `Comparator.naturalOrder()`. Unlike a
class's statics, which a subclass can be used to call, they can't be called through an implementer:

```java
static class ByLength implements Comparator<String> {
    public int compare(String a, String b) { return Integer.compare(a.length(), b.length()); }
}

Comparator<String> natural = Comparator.naturalOrder();          // on the interface: compiles
Comparator<String> viaClass = ByLength.naturalOrder();           // cannot find symbol
Comparator<String> viaInstance = new ByLength().naturalOrder();  // cannot find symbol
Comparator<String> d = natural.naturalOrder();                   // illegal static interface method call
```

These errors are from `--release 8`. A class can implement any number of interfaces, and if
it inherited their statics, two interfaces with the same static would make a call through the class
ambiguous. Because nothing is inherited, a library can add a static method to an interface and break
no implementer.

A default method can't override `equals`, `hashCode` or `toString` either: `default method toString
in interface Named overrides a member of java.lang.Object`. Every class inherits those from
`Object`, and a class's method beats a default, so such a default could never run.

<!-- practice 4 -->

## Optional Is a Return Type, and get() Without a Check Only Renames the NullPointerException

`Optional<String> email(String customer)` tells the caller that a customer may have no address
(unit 1.3.2). A caller that is sure the address is there calls `get()`, and then it isn't:

```java
email("c-9").get();
email("c-9").orElseThrow(() -> new UnknownCustomerException("c-9"));
```

```text
get():            java.util.NoSuchElementException: No value present
orElseThrow(...): Lookups$UnknownCustomerException: no email for customer c-9
```

`get()` throws just as dereferencing `null` did, and with less information: since Java 15 an
NPE names the expression that was null (unit 3.2.2), while `No value present` names nothing. The
on-call engineer can't tell which customer or which lookup failed. Use `map`, `orElse`,
`orElseGet`, or `orElseThrow` with an exception that carries the key.

> **Java 8 has fewer methods.** `orElseThrow()` without arguments (Java 10) and `isEmpty()` (Java 11)
> don't compile with `--release 8`: `cannot find symbol` and `method orElseThrow in class
> Optional<T> cannot be applied to given types`. Code that must build on 8 passes a `Supplier`.

<!-- practice 2 -->

## An Optional Field Breaks Serialization, and an Optional Parameter Can Still Be null

The javadoc calls `Optional` "primarily intended for use as a method return type". `Profile` keeps
one in a field and is stored in the HTTP session; `receipt` takes one as a parameter:

```java
static class Profile implements Serializable {
    final String id;
    final Optional<String> email;
    Profile(String id, Optional<String> email) { this.id = id; this.email = email; }
}

static String receipt(String order, Optional<String> to) {
    return to.isPresent() ? "receipt " + order + " to " + to.get() : "receipt " + order + " printed";
}
```

Writing a `Profile` that holds an address to an `ObjectOutputStream`, then calling
`receipt("o-18", null)`:

```text
session write:     java.io.NotSerializableException: java.util.Optional
receipt(null):    java.lang.NullPointerException: Cannot invoke "java.util.Optional.isPresent()" because "to" is null
```

`Optional` isn't `Serializable`, though a generic class can be (`ArrayList<String>` is), so
session replication, a distributed cache or RMI fails on `Profile` at run time; the compiler never
complains. Keep the field a nullable `String` and return
`Optional.ofNullable(email)` from the getter.

A parameter of type `Optional` has three states: present, empty and `null`, and nothing stops a caller from passing the third. Overload the
method instead, or take a nullable argument and document it.

<!-- practice 3 -->

## The Rest of Java 8 Is Taught in Its Own Units

| Java 8 change | What bites on upgrade or in an interview | Unit |
|---|---|---|
| lambdas | capture only effectively final locals; `this` is the enclosing object | 4.1.x |
| `java.util.function` | a checked exception doesn't fit `Function`; `Supplier` runs on every `get()` | 4.2.x |
| method references | `Class::method` is static or unbound, and the declaration decides | 4.3.x |
| streams | lazy until a terminal operation; parallel streams share the common pool | 4.4.x |
| `java.time` | immutable replacements for `Date` and `Calendar` | 7.3.x |
| `CompletableFuture` | a non-async callback runs on the completing thread, or on the caller if already complete | 6.4.2, 5.3.4 |
| `merge`, `compute` on `ConcurrentHashMap` | one atomic call, where `get` then `put` loses updates | 6.5.2 |
| Metaspace replaces PermGen | Java 21 refuses `-XX:MaxPermSize`: `Unrecognized VM option` | 10.5.1 |
| default methods | conflict rules and evolving a published interface | 2.3.1, 2.3.3 |

## Summary

- **New JDK defaults reach old classes, and two can collide.** A class that is both a `List` and a
  `Set` must override `spliterator()`, and `X.super` keeps only one interface's promises.
- **Methods Java 8 added need not go through your overrides.** `HashMap.merge` and
  `ArrayList.removeIf` bypass a subclass's `put` and `remove`; override them or wrap the collection.
- **Static interface methods aren't inherited.** Call them on the interface; that is why adding one
  breaks no implementer.
- **`Optional` is a return type.** `get()` without a check fails like `null` did, with less
  information; give `orElseThrow` an exception that names the key.
- **Not a field or a parameter.** An `Optional` field doesn't serialize, and an `Optional` parameter
  can be `null`.

## Reference

**Interface methods (checked with `--release 8`):**

| Declaration or call | Result |
|---|---|
| a class inherits two unrelated defaults with the same signature | compile error until the class overrides |
| `X.super.m()`, `X` a direct superinterface that no other supertype extends | calls `X`'s default |
| `X.super.m()`, `X` named directly but also extended by the superclass | compile error: redundant interface |
| `X.super.m()`, `X` not named in the class's `implements` clause | compile error: not an enclosing class |
| `Iface.staticM()` | compiles |
| `Impl.staticM()` or `instance.staticM()` | compile error |
| `default String toString()` in an interface | compile error: overrides a member of `Object` |

**`Optional` methods by release (javadoc `@since`):**

| Method | Since |
|---|---|
| `of`, `ofNullable`, `empty`, `get`, `isPresent`, `ifPresent`, `map`, `flatMap`, `filter`, `orElse`, `orElseGet`, `orElseThrow(Supplier)` | 8 |
| `or`, `ifPresentOrElse`, `stream` | 9 |
| `orElseThrow()` | 10 |
| `isEmpty` | 11 |

## Challenges

1. Your library ships a `Map` subclass that enforces a size cap in `put`. List every method a
   Java 8 caller can use to add a key, and decide for each one: override it, or rebuild the class
   on `AbstractMap`?
2. An entity class has an `Optional<Address>` getter over a nullable field. Which frameworks in your
   stack read the field, which call the getter, and what breaks for the ones that call it?
3. Find a class in your codebase that implements two JDK interfaces. Which defaults does it inherit
   from each, and what would a new default in a future release do to it?

## Design Note: Why Optional Is a Class and Not a Rule About null

Java 8 could not make the type system track `null` without changing every existing signature, so
it added a library class that new methods could return: `Stream.findFirst`, `max` and
`Optional`-returning lookups. Old methods kept their contracts, and `Map.get` still returns `null`
for a missing key. The cost is two conventions in one codebase, and an `Optional` reference that
can itself be `null`; the javadoc only asks that it never be. The class is also value-based, and
the rules for value-based classes warn against `==` and synchronizing on its instances, which leaves later releases free
to change how they are represented.

## Code Examples

- Test: [Java8FeaturesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/modernjava/Java8FeaturesTest.java)
- Source: [Java8Features.java](src/main/java/com/github/msorkhpar/claudejavatutor/modernjava/Java8Features.java)
