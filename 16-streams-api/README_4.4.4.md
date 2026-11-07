# 4.4.4. Terminal Operations (forEach, reduce, collect)

A billing job adds up a customer's per-request charges and a base fee of 250 cents:
`usage.stream().reduce(baseFee, Integer::sum)`. Its tests pass. To speed up the nightly run, a
colleague changes `stream()` to `parallelStream()`, and the next morning every large bill is
31,750 cents too high. Nothing threw, and the small bills in the tests are still right.

Six rules, one per heading. Laziness and single use are in unit 4.4.1, `forEach` and
`forEachOrdered` in unit 4.2.1, `toMap` in unit 4.2.3.

## reduce Needs an Identity That Changes Nothing and an Operation That Can Be Regrouped

`reduce(identity, op)` is guaranteed to give the same answer on a sequential and a parallel stream
if `identity` leaves every element unchanged (`op(identity, t)` equals `t`) and `op` is associative.
The billing job, on a 20-core machine:

```java
List<Integer> usage = IntStream.rangeClosed(1, 1000).boxed().toList();   // 500,500 cents
int baseFee = 250;
System.out.println("sequential: " + usage.stream().reduce(baseFee, Integer::sum));
System.out.println("parallel:   " + usage.parallelStream().reduce(baseFee, Integer::sum));
System.out.println("fixed:      " + (usage.parallelStream().reduce(0, Integer::sum) + baseFee));
```

```text
sequential: 500750
parallel:   532500
fixed:      500750
```

A parallel stream splits the list into chunks, reduces each from the identity into a partial
result of its own, and combines the partial results with the same operation. Here there were 128 chunks, so the fee was added 128
times. The identity is not "the starting value":
it is a value every chunk may start from, any number of times. For `+` that is 0, for `*` it is 1,
for `Integer::max` it is `Integer.MIN_VALUE`. The fee is added once, outside.

The second condition rules out subtraction. A running balance is the obvious way to write one:

```java
List<Integer> debits = IntStream.rangeClosed(1, 1000).boxed().toList();  // 500,500 cents
int opening = 1_000_000;
System.out.println("sequential: " + debits.stream().reduce(opening, (balance, d) -> balance - d));
System.out.println("parallel:   " + debits.parallelStream().reduce(opening, (balance, d) -> balance - d));
System.out.println("fixed:      " + (opening - debits.parallelStream().reduce(0, Integer::sum)));
```

```text
sequential: 499500
parallel:   0
fixed:      499500
```

`(a - b) - c` is not `a - (b - c)`. In parallel each chunk started from the opening balance, and
the chunks' balances were then subtracted from each other. Sum the debits, then subtract once.

Neither bug shows on a sequential stream. Both appear when someone adds `.parallel()`, or a
library hands you a parallel stream, months after the code was reviewed.

### The Three-Argument Form's Combiner Runs Only on a Parallel Stream

`reduce(identity, accumulator, combiner)` lets the result type differ from the element type: here
the elements are words and the result is the longest length. The accumulator folds one word into a
partial result; the combiner merges two partial results. This combiner was copied from a sum:

```java
List<String> words = new ArrayList<>();
for (int i = 0; i < 1000; i++) words.add("w".repeat(1 + i % 12));   // lengths 1 to 12
AtomicInteger calls = new AtomicInteger();
for (Stream<String> s : List.of(words.stream(), words.parallelStream())) {
    calls.set(0);
    int longest = s.reduce(0,
            (max, w) -> Math.max(max, w.length()),
            (a, b) -> { calls.incrementAndGet(); return a + b; });   // copied from a sum
    System.out.println((s.isParallel() ? "parallel:   " : "sequential: ")
            + longest + ", combiner calls: " + calls.get());
}
```

```text
sequential: 12, combiner calls: 0
parallel:   1413, combiner calls: 127
```

The JDK's sequential path never called the combiner, so no sequential test can find a wrong one.
The javadoc requires it to be compatible with the accumulator:
`combiner.apply(u, accumulator.apply(identity, t))` must equal `accumulator.apply(u, t)`. Merging
a partial result must do what folding in its elements would have done; here that is `Math::max`.
The identity must also be an identity for the combiner.

<!-- practice 1 -->

## Without an Identity, reduce Returns an Optional, and So Do min, max and findFirst

A dashboard shows the day's largest order. `Integer.MIN_VALUE` is a true identity for
`Integer::max`, and in parallel it gives the right answer. The trouble is the day with no orders:

```java
List<Integer> none = List.of();
System.out.println(none.stream().reduce(Integer.MIN_VALUE, Integer::max));
System.out.println(none.stream().reduce(Integer::max));
```

```text
-2147483648
Optional.empty
```

An empty stream returns the identity, so the dashboard reports an order of -2,147,483,648 cents,
and nothing downstream can tell it from a real value. `reduce(op)` has no identity to fall back on:
it returns an `Optional` that is empty for an empty stream, and the caller must decide what "no
orders" means. `min`, `max`,
`findFirst` and `findAny` return `Optional` for the same reason.

### findAny Is Free to Return Any Match

On an ordered stream, `findFirst` returns the first match in encounter order, even in parallel. `findAny`'s
javadoc calls it "explicitly nondeterministic". Five runs each, looking for multiples of 1,000 in
1 to 10,000 on a parallel stream:

```text
findAny:   3000 5000 9000 1000 2000 
findFirst: 1000 1000 1000 1000 1000 
```

Sequentially, `findAny` returned 1000, so code that relies on it passes its tests. Use `findAny`
when any match will do, such as "any idle worker"; use `findFirst` when you need the earliest, at
the cost of waiting for the chunks before the one that found it.

<!-- practice 5 -->

## reduce Combines Values; a Mutable Container Goes Through collect

`reduce` assumes the accumulator returns a new value and leaves its inputs alone. An `ArrayList`
as the identity breaks that assumption:

```java
List<String> ids = List.of("o1", "o2", "o3", "o4", "o5", "o6", "o7", "o8");
System.out.println("sequential: " + ids.stream().reduce(new ArrayList<String>(),
        (list, id) -> { list.add(id); return list; },
        (a, b) -> { a.addAll(b); return a; }));
```

The three `parallel` lines run the same `reduce` on `ids.parallelStream()`, and the last line is
the `collect` shown further down:

```text
sequential: [o1, o2, o3, o4, o5, o6, o7, o8]
parallel:   size 198
parallel:   size 80
parallel:   size 352
collect:    [o1, o2, o3, o4, o5, o6, o7, o8]
```

The identity is one list, shared by every chunk. All the worker threads added to it
unsynchronized, and the combiner then added it to itself. Eight ids came out as 80 to 352 entries;
1,000 ids threw `ArrayIndexOutOfBoundsException` on every run. A `StringBuilder` as identity fails
the same way.

`collect` is the mutable form of a reduction. It takes a supplier instead of an identity and calls
it once per chunk, so each thread fills a container of its own, and the combiner merges two
different containers:

```java
ArrayList<String> collected = ids.parallelStream().collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
```

`Collectors.toList()` is the same supplier, accumulator and combiner packaged as a `Collector`.
Use `reduce` for immutable values such as numbers and records, `joining` for strings, and
`collect` for anything you add to.

<!-- practice 2 -->

## groupingBy Gives a Map of Lists in No Promised Order; a Downstream and a Map Factory Change Both

`groupingBy(classifier)` puts each element in a list under its key. A downstream collector
changes what each group becomes; a map factory changes the map. Six orders in four regions:

```java
record Order(String region, String customer, int cents) {}

List<Order> orders = orders();   // west: 3 orders from c-17 (twice) and c-33; one each elsewhere
Map<String, List<Order>> byRegion = orders.stream().collect(Collectors.groupingBy(Order::region));
Map<String, Long> count = orders.stream()
        .collect(Collectors.groupingBy(Order::region, Collectors.counting()));
Map<String, Integer> cents = orders.stream()
        .collect(Collectors.groupingBy(Order::region, Collectors.summingInt(Order::cents)));
Map<String, Set<String>> customers = orders.stream()
        .collect(Collectors.groupingBy(Order::region, Collectors.mapping(Order::customer, Collectors.toSet())));
TreeMap<String, Long> sorted = orders.stream()
        .collect(Collectors.groupingBy(Order::region, TreeMap::new, Collectors.counting()));
System.out.println(byRegion.getClass().getSimpleName() + " " + byRegion.keySet());
```

```text
HashMap [east, south, north, west]
counting():   {east=1, south=1, north=1, west=3}
summingInt(): {east=3000, south=700, north=450, west=2150}
mapping(..., toSet()): {east=[c-40], south=[c-09], north=[c-02], west=[c-33, c-17]}
TreeMap::new: {east=1, north=1, south=1, west=3}
```

West has three orders from two customers: `counting()` counts orders, while
`mapping(Order::customer, toSet())` keeps the distinct customers. The default map was a `HashMap`,
with the regions in hash order, and the javadoc gives "no guarantees on the type, mutability,
serializability, or thread-safety" of that map. The three-argument form takes a supplier of the
map: `TreeMap::new` for sorted keys, `LinkedHashMap::new` for the
order in which keys were first seen.

### teeing Feeds Two Collectors in One Pass, and joining Writes Its Prefix Even for Nothing

`Collectors.teeing` (Java 12) sends every element to two collectors and merges their results.
`joining(delimiter, prefix, suffix)` concatenates strings:

```java
String line = orders.stream().collect(Collectors.teeing(
        Collectors.summingInt(Order::cents),
        Collectors.counting(),
        (total, n) -> total + " cents in " + n + " orders"));
```

The output's last two lines are `joining(", ", "[", "]")` over the distinct customers and over an
empty stream:

```text
6300 cents in 6 orders
[c-17, c-02, c-40, c-09, c-33]
[]
```

The empty stream still yields `[]`, so an `isEmpty()` check on the joined string never fires.

## partitioningBy Always Has Both Keys; groupingBy Has Only the Keys It Saw

A nightly report splits the orders of 1,000 cents or more from the rest. On a day when every order
is small, the two collectors disagree:

```java
List<Order> small = orders().stream().filter(o -> o.cents() < 1000).toList();
Map<Boolean, List<Order>> p = small.stream().collect(Collectors.partitioningBy(o -> o.cents() >= 1000));
Map<Boolean, List<Order>> g = small.stream().collect(Collectors.groupingBy(o -> o.cents() >= 1000));
```

```text
partitioningBy: [false, true], get(true) = []
groupingBy:     [false], get(true) = null
```

`groupingBy` creates a key the first time an element maps to it, so a group with no members does
not exist, and `g.get(true).size()` throws `NullPointerException` on a day with no large orders.
`partitioningBy`'s javadoc promises that the map "always contains mappings for both false and true
keys", with empty lists where nothing matched. For a yes/no split, use `partitioningBy`; read a
`groupingBy` map with `getOrDefault`.

<!-- practice 3 -->

## toList(), Collectors.toList() and toUnmodifiableList() Return Three Different Lists

Each line collects `"a", null, "c"`, then calls `add("d")` on the result:

```java
show("toList()", () -> Stream.of("a", null, "c").toList());
show("Collectors.toList()", () -> Stream.of("a", null, "c").collect(Collectors.toList()));
show("toUnmodifiableList()", () -> Stream.of("a", null, "c").collect(Collectors.toUnmodifiableList()));
```

```text
toList()              java.util.ImmutableCollections$ListN, add(): UnsupportedOperationException
Collectors.toList()   java.util.ArrayList, add() worked: [a, null, c, d]
toUnmodifiableList()  NullPointerException
```

`Collectors.toList()` returned an
`ArrayList`, but its javadoc gives "no guarantees on the type, mutability, serializability, or
thread-safety", so a caller that adds to it relies on an implementation detail.
`Collectors.toUnmodifiableList()` (Java 10) rejects nulls with `NullPointerException`.

The costly swap is the refactor from `collect(Collectors.toList())` to `toList()`: it compiles, and
the first caller that sorts or appends to the result throws `UnsupportedOperationException` at run
time. Choose by what the caller does: `toList()` (Java 16) for a result nobody changes,
`Collectors.toCollection(ArrayList::new)` for one the caller will change, and
`toUnmodifiableList()` when a null would be a bug you want reported at once.

`toArray()` returns an `Object[]`, and casting it to
`String[]` throws `ClassCastException`; `toArray(String[]::new)` passes the array's constructor
and returns a real `String[]`.

<!-- practice 4 -->

## Summary

- **`reduce` needs a true identity and an associative operation.** A fee used as identity, or a
  running subtraction, is wrong only in parallel, where the three-argument form's combiner also
  first runs.
- **Without an identity, `reduce` returns an `Optional`,** as do `min`, `max`, `findFirst` and
  `findAny`, so "no data" can't pass for a value; `findAny` may return any match.
- **`reduce` is for values, `collect` for containers.** A mutable identity is one object shared by
  every chunk; `collect`'s supplier gives each chunk its own.
- **`groupingBy` returns a map of lists with no promised type or order.** A downstream collector
  changes the values, a map factory the map.
- **`partitioningBy` always has both keys;** `groupingBy` only has the keys it saw.
- **The three list collectors differ in mutability and nulls.** `toList()` is unmodifiable and
  keeps nulls, `Collectors.toList()` promises neither, `toUnmodifiableList()` rejects nulls.

## Reference

**The three `reduce` forms, and `collect`, with their contracts (from the `Stream` javadoc):**

| Form | Returns | The functions must satisfy |
|---|---|---|
| `reduce(identity, op)` | `T`; `identity` for an empty stream | `op(identity, t)` equals `t`; `op` associative |
| `reduce(op)` | `Optional<T>`; empty for an empty stream | `op` associative |
| `reduce(identity, accumulator, combiner)` | `U` | `combiner(identity, u)` equals `u`; `combiner(u, accumulator(identity, t))` equals `accumulator(u, t)`; both associative |
| `collect(supplier, accumulator, combiner)` | `R`, a container | combiner compatible with accumulator; both associative; the supplier returns a new container each call |

**Other terminal operations (Java 21):**

| Operation | Result | Note |
|---|---|---|
| `min`, `max`, `findFirst`, `findAny` | `Optional<T>` | empty for an empty stream |
| `count()` | `long` | may skip the pipeline when the size is known (unit 4.2.1) |
| `anyMatch` | `boolean` | `false` on an empty stream |
| `allMatch`, `noneMatch` | `boolean` | `true` on an empty stream |
| `toArray()` / `toArray(String[]::new)` | `Object[]` / `String[]` | |

**Collectors used on this page:**

| Collector | Result |
|---|---|
| `groupingBy(f)` | `Map<K, List<T>>`; a `HashMap` in Java 21, no promised type |
| `groupingBy(f, downstream)` | `Map<K, D>`: `counting()` gives `Long`, `summingInt` gives `Integer`, `mapping(g, toSet())` gives `Set` |
| `groupingBy(f, TreeMap::new, downstream)` | the map the factory makes |
| `partitioningBy(p)` | `Map<Boolean, List<T>>` with both keys |
| `joining(delim, prefix, suffix)` | `String`; `prefix + suffix` for an empty stream |
| `teeing(c1, c2, merger)` | the merger's result (Java 12) |
| `Collectors.toList()` / `Collectors.toUnmodifiableList()` | no promised type / unmodifiable, rejects nulls (Java 10) |

## Challenges

1. A dashboard computes the average order value with one `reduce`. Write it with the
   three-argument form and a small record as the partial result, then check your combiner against
   the compatibility rule. When is `teeing` or `summaryStatistics()` (unit 4.4.2) the better tool?
2. Your team adds `.parallel()` to every stream in a batch job. Which of its `reduce` and `collect`
   calls could change their results, and what test would find each one before production does?
3. A method returns `collect(Collectors.toList())` and fifty callers use the result, some of them
   adding to it. How would you move it to `toList()` safely, and what does each choice promise the
   callers?

## Design Note: Why Java Has Both reduce and collect

`reduce` comes from functional programming, where values never change: combining two partial sums
makes a third, and the identity can be shared because nobody modifies it. That is what lets the
library split the work across threads without locks. Building a list that way would copy the list
at every step, which is quadratic, and `collect` exists for that case: it trades shared values for
a container per chunk and a combiner that merges two of them. The compiler can't tell the two
apart: `reduce(new ArrayList<>(), ...)` compiles, and only a parallel run shows the shared
identity.

## Code Examples

- Source: [TerminalOperations.java](src/main/java/com/github/msorkhpar/claudejavatutor/streamsapi/TerminalOperations.java)
- Test: [TerminalOperationsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/streamsapi/TerminalOperationsTest.java)
