# 4.4.3. Intermediate Operations (filter, map, flatMap)

The plant dashboard's "first three alarms" panel, most severe on top, ran for a year against the
nightly export of sensor readings. Pointed at the live feed, the request never returns, and the
service's heap climbs until the container is killed:

```java
List<Reading> panel = feed.readings()       // a Stream that ends only when the feed closes
        .filter(Reading::isAlarm)
        .sorted(BY_SEVERITY)
        .limit(3)
        .toList();
```

Each call between the source and `toList()` is an intermediate operation. Most pass an element
on at once; a few can't, and that difference decides what this code does. Every output below is
from Java 21.

## map Gives One Result per Element; flatMap Splices a Stream per Element into One

An order holds a list of SKUs, and the picking screen needs the SKUs. How many elements does each
stream hold?

```java
record Order(String id, String customer, List<String> skus) {}

List<Order> orders = List.of(
        new Order("A-17", "c-1", List.of("pen", "ink")),
        new Order("A-18", "c-2", List.of()),
        new Order("A-19", "c-3", List.of("pad", "pen", "clip")));

Stream<List<String>> skuLists = orders.stream().map(Order::skus);
Stream<String> skus = orders.stream().flatMap(o -> o.skus().stream());
```

```text
map:     [[pen, ink], [], [pad, pen, clip]]  count=3
flatMap: [pen, ink, pad, pen, clip]  count=5
```

`map` calls its function once per element and passes on what it returns, so three orders give
three lists, and the screen reports three items to pick where there are five. `flatMap` expects a
stream from its function and passes on that stream's elements, one after another: two, none,
three. The order without SKUs contributes nothing instead of an empty list.

Because a stream can hold zero elements, `flatMap` is also how a pipeline drops missing values.
`emailOf(customer)` returns an `Optional<String>`, and `c-2` has no address:

```java
orders.stream().map(o -> emailOf(o.customer()))
orders.stream().map(o -> emailOf(o.customer())).flatMap(Optional::stream)
```

```text
map:     [Optional[ana@example.com], Optional.empty, Optional[li@example.com]]
flatMap: [ana@example.com, li@example.com]
```

`Optional.stream()` has one element or none. Unwrapping with `orElse(null)` instead would put a
`null` in the list, and the mailer would fail on it later, far from the lookup.

A function given to `flatMap` may return `null`: the javadoc says an empty stream is used
instead. The same `null` returned to `map` stays in the stream as an element:

```java
orders.stream().flatMap(o -> o.skus().isEmpty() ? null : o.skus().stream())
orders.stream().map(o -> o.skus().isEmpty() ? null : o.skus().get(0))
```

```text
flatMap: [pen, ink, pad, pen, clip]
map:     [pen, null, pad]
```

Return `Stream.empty()` anyway: a reader of the code shouldn't need that javadoc sentence.

### map Stays on the Kind of Stream It Was Called On

`IntStream.map` takes an `IntUnaryOperator`, so its function must return an `int`:

```text
bad/IntMap.java:5: error: incompatible types: bad return type in lambda expression
        IntStream.rangeClosed(1, 3).map(i -> "bay-" + i).forEach(System.out::println);
                                                    ^
    String cannot be converted to int
```

`mapToObj` leaves a primitive stream and `mapToInt` enters one, mid-pipeline too. Here
order-line quantities are filtered as `int`s and turned back into labels:

```java
lines.stream().mapToInt(Line::qty).filter(q -> q > 0).mapToObj(q -> "qty " + q).toList()
```

```text
[qty 2, qty 1]
```

Unit 4.4.2 covers what the primitive streams add (`sum`, `average`) and how `IntStream.sum()`
overflows.

<!-- practice 1 -->

## mapMulti Emits Zero or More Results Without a Stream per Element, but javac Can't Infer What It Emits

Each order line becomes a pick task, plus a wrap task if it is gift-wrapped, and nothing if its
quantity is 0. With `flatMap` that is a new `Stream.of(...)` for every line, usually of one
element. `mapMulti` (Java 16) hands the function a `Consumer` instead, and each `accept` emits one
result:

```java
record Line(String sku, int qty, boolean giftWrap) {}

List<Line> lines = List.of(new Line("pen", 2, true), new Line("ink", 0, false), new Line("pad", 1, false));
List<String> tasks = lines.stream()
        .<String>mapMulti((line, sink) -> {
            if (line.qty() == 0) return;              // nothing to pick
            sink.accept("pick " + line.qty() + " x " + line.sku());
            if (line.giftWrap()) sink.accept("wrap " + line.sku());
        })
        .toList();
```

```text
[pick 2 x pen, wrap pen, pick 1 x pad]
```

The javadoc recommends `mapMulti` over `flatMap` when each element yields a few results, possibly
none, since it creates no `Stream` per group, or when an `if` or a loop is easier than building a
stream. Without the `<String>`, the same code doesn't compile:

```text
bad/NoWitness.java:13: error: incompatible types: List<Object> cannot be converted to List<String>
                .toList();
                       ^
```

Among the arguments, the result type `R` appears only in the type of `sink`, a parameter of the
lambda. javac doesn't
infer it from the values the body passes to `accept`, and the `List<String>` on the left doesn't
reach back through `.toList()`, so `R` becomes `Object`. Give the type argument,
`.<String>mapMulti(...)`, or declare the lambda's parameters: `(Line line, Consumer<String> sink)`.
Only when `mapMulti`'s result goes straight to a `Stream<String>` (an assignment, a return, an
argument) does the target type decide `R`; add `.filter(s -> s.isEmpty())` after it and `s` is an
`Object` again.

## filter and map Pass Each Element On; sorted Holds Every Element Before It Emits One

Unit 4.4.1 traced elements one at a time through a pipeline. With `sorted` in the middle, they
wait:

```java
Optional<Integer> first = Stream.of(30, 10, 20)
        .peek(n -> System.out.println("  filter sees " + n))
        .filter(n -> n > 5)
        .sorted()
        .peek(n -> System.out.println("  after sorted " + n))
        .findFirst();
```

```text
  filter sees 30
  filter sees 10
  filter sees 20
  after sorted 10
findFirst -> 10
```

`filter` and `map` are stateless: each looks at one element and passes it on at once. `sorted` is
stateful: it can't know which element comes first until it has seen the last, so it buffers
everything and emits only when the source is exhausted. A `findFirst()` or `limit(3)` after it
stops nothing upstream. On the
dashboard's feed the source is never exhausted:

```java
Stream.iterate(1, n -> n + 1).limit(3).sorted().toList()
Stream.iterate(1, n -> n + 1).peek(n -> pulled.incrementAndGet()).sorted().limit(3).toList()  // on a pool thread, get(2, SECONDS)
```

```text
limit(3).sorted(): [1, 2, 3]
sorted().limit(3): nothing after 2 s, 66,503,114 elements pulled
```

That buffer is where the dashboard's heap went. The two orders also mean different things on a
finite list: `sorted().limit(3)` is the first three in sort order of everything, `limit(3).sorted()`
is the first three to arrive, sorted. The panel wants the first three alarms, most severe on top,
so `filter`, then `limit`, then `sorted`.

`sorted()` without a comparator needs `Comparable` elements, and nothing checks that until the
terminal operation compares two of them:

```java
record Ticket(String id, int priority) {}

Stream<Ticket> pipeline = Stream.of(new Ticket("T-9", 2), new Ticket("T-4", 1)).sorted();
System.out.println("pipeline built");
pipeline.toList();
Stream.of(new Ticket("T-9", 2)).sorted().toList();                // one ticket
```

```text
pipeline built
toList(): java.lang.ClassCastException: class Sorted$Ticket cannot be cast to class java.lang.Comparable (Sorted$Ticket is in unnamed module of loader 'app'; java.lang.Comparable is in module java.base of loader 'bootstrap')
one ticket: [Ticket[id=T-9, priority=2]]
```

One ticket sorts without complaint, because there is nothing to compare it with, so a one-row
test passes. Pass a `Comparator`, such as
`Comparator.comparingInt(Ticket::priority)`, for anything without a natural order.

## A Cheap filter Goes Before an Expensive map, Because map Runs for Every Element That Reaches It

A catalogue has 2,000 products, 40 of them in stock. `quote(product)` calls the pricing service,
and a counter records the calls. Both pipelines return the same 40 quotes:

```java
catalog.stream().map(Cost::quote).filter(q -> q.product().inStock()).toList();
catalog.stream().filter(Product::inStock).map(Cost::quote).toList();
```

```text
map, then filter: 40 quotes, quote() ran 2000 times
filter, then map: 40 quotes, quote() ran 40 times
```

The stream fuses the stages into one pass, but it runs them in the order you wrote them, on the
elements that reach each one. If a quote takes 15 ms, the first pipeline spends 30 seconds where
0.6 would do, and a pricing service that limits calls per second throttles it long before the
end. When the condition needs the mapped value, split it: filter on what the source already
knows, map, then filter again on the result.

Moving `limit` changes the result, not only the cost. Before an expensive `map`, it caps how many
elements reach the map; before a `filter`, it counts elements that the filter then throws away.

<!-- practice 2 -->

## distinct Uses equals and hashCode, So a Class Without Both Keeps Every Copy

A scanner reads the same SKU twice. Each scan builds its own object, as a parser would, and the
pick list runs `distinct()` over three versions of the SKU class:

```java
String[][] scans = {{"acme", "P-100"}, {"acme", "P-200"}, {"acme", "P-100"}};
Stream.of(scans).map(s -> new SkuRecord(s[0], s[1])).distinct().toList()      // record
Stream.of(scans).map(s -> new SkuPlain(s[0], s[1])).distinct().toList()       // class, no equals
Stream.of(scans).map(s -> new SkuEqualsOnly(s[0], s[1])).distinct().toList()  // equals, no hashCode
```

```text
record:         [SkuRecord[vendor=acme, code=P-100], SkuRecord[vendor=acme, code=P-200]]
plain class:    [acme/P-100, acme/P-200, acme/P-100]
equals only:    [acme/P-100, acme/P-200, acme/P-100]
```

`distinct` remembers what it has passed in a hash set. A class without `equals` inherits identity
from `Object`, so two scans of one SKU count as different. With `equals` but no `hashCode`, the two copies are
equal but have different identity hash codes, and the set never gets as far as asking `equals`. A
record generates both, from all of its components.

On an ordered stream, `distinct` keeps the first of each group of equal elements, so the fields
in `equals` decide what counts as a copy, and with it what survives. This `Scan` compares the
SKU only, and keeps the 09:00 scan:

```java
record Scan(String sku, String at) {
    @Override public boolean equals(Object o) { return o instanceof Scan s && sku.equals(s.sku); }
    @Override public int hashCode() { return sku.hashCode(); }
}

List<Scan> kept = Stream.of(new Scan("P-100", "09:00"), new Scan("P-200", "09:01"),
        new Scan("P-100", "09:05")).distinct().toList();
```

```text
[Scan[sku=P-100, at=09:00], Scan[sku=P-200, at=09:01]]
```

Drop the two overrides and the record compares all its components: the scan time joins the
comparison, and two scans of one SKU differ again. For "distinct by one field" without changing `equals`, unit 4.2.4 builds a predicate that
remembers keys.

<!-- practice 3 -->

## limit and skip Count in Encounter Order, and HashSet and Set.of Have None

`skip(page * size).limit(size)` cuts a page out of a stream. A `HashSet` has no encounter order,
and `Set.of` changes its order from one JVM run to the next (unit 4.4.2). Pages of six regions,
from one program run three times:

```java
Set<String> regions = Set.of("north", "south", "east", "west", "central", "coast");
regions.stream().limit(2).toList()                     // first page
regions.stream().skip(2).limit(2).toList()             // second page
regions.stream().sorted().skip(2).limit(2).toList()    // second page, sorted first
```

```text
Set.of,  first page: [central, coast]  second page: [east, north]
sorted,  first page: [central, coast]  second page: [east, north]
Set.of,  first page: [south, west]  second page: [north, east]
sorted,  first page: [central, coast]  second page: [east, north]
Set.of,  first page: [north, east]  second page: [coast, central]
sorted,  first page: [central, coast]  second page: [east, north]
```

`limit` and `skip` take the first elements in whatever order the stream has. Behind a load
balancer, page one can come from one instance and page two from another: a user who got
`[south, west]` and then `[coast, central]` never sees `north` or `east`. Sort first, then skip
and limit. Sorting after paging only orders a page whose members were picked arbitrarily.

> **Parallel pipelines differ.** On an ordered parallel stream, `limit` and `skip` must still
> find the first n elements, which the javadoc calls "quite expensive"; `unordered()` lifts that
> constraint when any n will do. Unit 4.4.5 covers parallel streams.

<!-- practice 4 -->

## Summary

- **`map` gives one result per element; `flatMap` splices a stream per element.** `map` to a list
  gives a stream of lists; `flatMap(Optional::stream)` drops empties; a `null` stream counts as
  empty. `mapToInt` and `mapToObj` switch between object and primitive streams.
- **`mapMulti` emits zero or more results through a `Consumer`.** Followed by another call, it
  needs `.<R>mapMulti` or typed lambda parameters, or `R` becomes `Object`.
- **`sorted` holds every element before it emits one.** `limit` or `findFirst` after it doesn't
  stop the source, so on an endless stream it never returns. Non-`Comparable` elements fail at the
  terminal operation, and only once two are compared.
- **A cheap `filter` goes before an expensive `map`.** The stream doesn't reorder stages, so `map`
  runs for every element that reaches it.
- **`distinct` uses `equals` and `hashCode`.** Without both, every copy stays; the fields in
  `equals` decide what counts as a copy, and the first in encounter order survives.
- **`limit` and `skip` count in encounter order.** Sort an unordered source before paging it.

## Reference

**Which operations pass elements on** (sequential; `Stream.iterate(0, i -> i + 1)`, the
operation, then `limit(3)`, with a 1 s timeout):

| Operation | Kind (javadoc, package summary) | Endless source, `limit(3)` after it |
|---|---|---|
| `filter`, `map`, `flatMap`, `mapMulti`, `peek` | stateless | returns |
| `distinct` | stateful | returns once 3 different values have arrived |
| `skip(n)` | stateful | returns |
| `limit(n)` | short-circuiting stateful | returns |
| `sorted()`, `sorted(cmp)` | stateful | never returns; the same with `findFirst()` |

**Mapping operations:**

| Operation | Its function returns | Results per element | Note |
|---|---|---|---|
| `map` | `R` | one | a `null` result is an element |
| `flatMap` | `Stream<R>` | zero or more | a `null` stream counts as empty; each stream is closed after use |
| `mapMulti` | nothing; calls `accept` | zero or more | `R` must be spelled out unless the target type decides it |
| `mapToInt`, `mapToLong`, `mapToDouble` | a primitive | one | enters a primitive stream |
| `mapToObj` (on a primitive stream) | `R` | one | leaves it |

**Order and nulls:** on an ordered stream `sorted` is stable and `distinct` keeps the first of
equal elements; on an unordered one neither is promised (javadoc). `sorted()` throws
`NullPointerException` once a `null` is compared (a lone `null` comes back as `[null]`); `sorted(Comparator.nullsFirst(Comparator.naturalOrder()))`
puts nulls first (ran).

## Challenges

1. A report needs a customer's ten most recent orders from a stream of all orders. Where do
   `filter`, `sorted` and `limit` go, and how many orders does the stream hold at its peak? What
   would a database query do differently?
2. Measure `flatMap` against `mapMulti` on ten million elements that each expand to zero or one
   result, then to fifty. At what size does the javadoc's advice stop mattering?
3. A `Customer` class is a `HashMap` key in one place and goes through `distinct()` in another.
   Its `equals` compares the email ignoring case. What must `hashCode` do, and what breaks if it
   hashes the email as stored?

## Design Note: Why sorted Is the Barrier in a Lazy Pipeline

A stream pushes each element through every stage before it pulls the next, which is what lets
`limit` and `findFirst` stop an endless source. A stateless operation needs no buffer.
`sorted` can't do without one: the first element it emits may be the last one the source produces. The library
gives it a buffer and a whole-input pass, and the package documentation says so: stateful
operations "may need to process the entire input before producing a result".

The cost is a pipeline whose behaviour depends on the order of its lines. Moving `limit` one line
down, past `sorted`, turns a bounded job into one that holds every element. The compiler accepts
both, and a test on a small input sees at most a different result, never the memory.

## Code Examples

- Source: [IntermediateOperations.java](src/main/java/com/github/msorkhpar/claudejavatutor/streamsapi/IntermediateOperations.java)
- Test: [IntermediateOperationsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/streamsapi/IntermediateOperationsTest.java)
