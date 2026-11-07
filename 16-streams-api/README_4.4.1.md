# 4.4.1. Introduction to the Streams API

The warehouse dashboard shows how many items are low on stock. After each deploy the first request
gets an answer, and every request after it fails with an `IllegalStateException`. The class behind
it passed review:

```java
import java.util.List;
import java.util.stream.Stream;

record Item(String sku, int onHand) {}

class LowStockReport {
    private final Stream<Item> lowStock;

    LowStockReport(List<Item> items) {
        lowStock = items.stream().filter(i -> i.onHand() < 5);
    }

    long count() {
        return lowStock.count();
    }
}
```

Two requests against `A-1` (2 on hand), `B-7` (40) and `C-3` (0):

```text
request 1: 2 items low
request 2: java.lang.IllegalStateException: stream has already been operated upon or closed
```

The field reads like a filtered view of the items. A `Stream` is something else: a one-shot
pipeline that reads its source only when a terminal operation asks.

## Building a Pipeline Runs Nothing; the Terminal Operation Runs It

A pipeline is a source, any number of intermediate operations (`filter`, `map`, `sorted`, each
returning a `Stream`), and one terminal operation (`toList`, `count`, `forEach`, `findFirst`),
which returns something else or nothing. Three SKUs, and a `filter` and a `map` that print when
they are called. Pause before the output: what has been printed by the time the last line runs?

```java
List<String> skus = List.of("A-1", "B-7", "C-3");
Stream<String> pipeline = skus.stream()
        .filter(s -> { System.out.println("filter " + s); return !s.startsWith("B"); })
        .map(s -> { System.out.println("map    " + s); return s.toLowerCase(); });
System.out.println("pipeline built");
```

```text
pipeline built
```

Neither lambda ran. `filter` and `map` only added a stage to a chain. The `Stream` they return
holds that chain and the source, not elements. Waiting is what
lets the library run all the stages in one pass, stop early, and read from a source that never
ends (unit 4.4.2).

A pipeline written for its side effects with no terminal operation therefore compiles and does
nothing (unit 4.2.1 shows it with `peek`). And each lambda runs when the terminal operation
runs, which can be long after the line that wrote it.

## Each Element Goes Through the Whole Pipeline Before the Next, and Short-Circuiting Stops the Pulling

Now the same pipeline gets its terminal operation. Does `filter` see all three SKUs before `map`
sees any?

```java
System.out.println(pipeline.toList());
```

```text
filter A-1
map    A-1
filter B-7
filter C-3
map    C-3
[a-1, c-3]
```

The terminal operation pulls one element from the source and pushes it through every stage
before it pulls the next. `B-7` stops at the filter and never reaches `map`. No list is built
between the stages.

Because elements arrive one at a time, an operation that has its answer can stop pulling. A
fraud check runs over 1,000 order ids and flags every 300th; a counter records how often the check
is called. Each row below is a fresh `orderIds.stream()`, with the counter reset:

```java
List<Integer> orderIds = IntStream.rangeClosed(1, 1_000).boxed().toList();
AtomicInteger checks = new AtomicInteger();
Predicate<Integer> flagged = id -> { checks.incrementAndGet(); return id % 300 == 0; };
```

```text
filter(flagged).findFirst()          Optional[300] checks=300
anyMatch(flagged)                    true         checks=300
filter(flagged).limit(2).toList()    [300, 600]   checks=600
filter(flagged).toList().get(0)      300          checks=1000
filter(flagged).count() > 0          true         checks=1000
```

The first three stop as soon as they have their answer. The last two ask the same questions as
the first two and check every order, because `toList()` and `count()` need every element. If the
check is a 20 ms call to a fraud service, that is 6 seconds against 20 for the same answer, and
over an infinite source the last two never produce a result.

A stateful operation such as `sorted` breaks the one-at-a-time flow: it has to pull every element
before it can pass the first one on. Unit 4.4.3 shows how that changes the trace.

<!-- practice 1 -->

## A Stream Can Be Consumed Once, So Keep a Supplier, Not the Stream

Back to the dashboard. The first `count()` pulled every item through the filter, and the stream was
spent. The second `count()` found a stream that had already run. The check is stricter than "one
terminal operation": a second operation that builds on the same `Stream` object throws, intermediate
ones included. Branching one stream into two filters fails before anything has run:

```java
Stream<String> base = skus.stream();
Stream<String> aisleA = base.filter(s -> s.startsWith("A"));
Stream<String> aisleC = base.filter(s -> s.startsWith("C"));   // throws
```

```text
second filter: stream has already been operated upon or closed
```

A `Stream` reads its source through a `Spliterator`, a cursor that moves forward and can't be
rewound, and each stage marks the one before it as used when it links to it. That mark is what
the second `filter` tripped over.

The dashboard's test called `count()` once, so it passed; production made a second request. A
method that returns a stream it keeps in a field fails the same way for its second caller. The fix
is to keep the recipe and build a new stream each time. A method that returns
`items.stream().filter(...)` on every call does that, and so does a `Supplier`:

```java
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

record Item(String sku, int onHand) {}

class LowStockReport {
    private final Supplier<Stream<Item>> lowStock;

    LowStockReport(List<Item> items) {
        lowStock = () -> items.stream().filter(i -> i.onHand() < 5);
    }

    long count() {
        return lowStock.get().count();
    }

    List<String> skus() {
        return lowStock.get().map(Item::sku).toList();
    }
}
```

Over a mutable list this time, two requests, then a third after an item is added:

```java
List<Item> items = new ArrayList<>(List.of(new Item("A-1", 2), new Item("B-7", 40), new Item("C-3", 0)));
LowStockReport report = new LowStockReport(items);
System.out.println("request 1: " + report.count() + " items low");
System.out.println("request 2: " + report.count() + " items low " + report.skus());
items.add(new Item("D-9", 1));
System.out.println("after D-9 is added: " + report.count() + " items low " + report.skus());
```

```text
request 1: 2 items low
request 2: 2 items low [A-1, C-3]
after D-9 is added: 3 items low [A-1, C-3, D-9]
```

Each `get()` streams the list as it is at that moment. Collecting the low items into a list in
the constructor would also survive the second request, but would keep answering with the items as
they were when the report was built.

<!-- practice 2 -->

## The Source Is Read When the Terminal Operation Runs, and Must Not Change Under It

A stream is built over a queue of orders, an order is added, and only then does the terminal
operation run. The same steps follow over a `CopyOnWriteArrayList`:

```java
List<String> queue = new ArrayList<>(List.of("o-1", "o-2"));
Stream<String> pending = queue.stream().map(String::toUpperCase);
queue.add("o-3");
System.out.println("ArrayList:            " + pending.toList());

List<String> snapshot = new CopyOnWriteArrayList<>(List.of("o-1", "o-2"));
Stream<String> pendingCow = snapshot.stream().map(String::toUpperCase);
snapshot.add("o-3");
System.out.println("CopyOnWriteArrayList: " + pendingCow.toList());
```

```text
ArrayList:            [O-1, O-2, O-3]
CopyOnWriteArrayList: [O-1, O-2]
```

`ArrayList`'s javadoc calls its spliterator *late-binding*: it binds to the list's elements at
the first traversal, not when it is created, so `o-3` is in. `CopyOnWriteArrayList`'s javadoc
promises a snapshot of the list as it was when the spliterator was created, and `stream()` creates
it at once. The `java.util.stream` package javadoc uses the `ArrayList` case as its example and
says streams from JDK collections behave that way; `CopyOnWriteArrayList` doesn't, despite that
wording.

Changing the source *while* the terminal operation runs is different. The package javadoc calls it
interference and says it must never happen with a non-concurrent source. Here a shipping job
re-queues order `o-2` from inside the pipeline:

```java
List<String> orders = new ArrayList<>(List.of("o-1", "o-2", "o-3", "o-4"));
orders.stream()
      .map(o -> { if (o.equals("o-2")) orders.add("o-2-retry"); return o; })
      .forEach(o -> System.out.println("shipped " + o));
```

```text
shipped o-1
shipped o-2
shipped o-3
shipped o-4
java.util.ConcurrentModificationException, orders now [o-1, o-2, o-3, o-4, o-2-retry]
```

The `add` succeeded, the retry was never shipped, and the exception came after all four orders had
shipped. For `forEach` and `toList`, `ArrayList`'s spliterator compares the list's modification
count once, after the last element. A short-circuiting operation compares after each element, so
`filter(o -> o.equals("o-3")).findFirst()` in place of `forEach` throws as soon as `o-2` is done.
Either way the exception doesn't say how much work was already done, and a caller that retries the
batch on failure ships all four orders again.

Removing is worse. A cart cleanup removes an empty cart from inside `filter`:

```java
List<String> carts = new ArrayList<>(List.of("c-1", "", "c-3", "c-4"));
carts.stream()
     .filter(c -> { System.out.println("checked " + c); if (c.isEmpty()) carts.remove(c); return true; })
     .forEach(c -> { });
```

```text
checked c-1
checked 
checked c-4
checked null
java.lang.NullPointerException: Cannot invoke "String.isEmpty()" because "c" is null
```

Cart `c-3` moved into the removed cart's slot and was never checked, and the spliterator, still
counting four elements, handed the lambda the `null` left at the end. The `NullPointerException`
came first; with a null-safe check the same run ends in `ConcurrentModificationException` after
the `null`. Collect what has to change into a new list and apply it
after the terminal operation, or use `removeIf` (unit 4.2.4).

<!-- practice 4 -->

## A Loop Is Clearer When the Work Stops Early, Throws a Checked Exception, or Updates Several Variables

Stock adjustments arrive as lines like `A-1 +5`. The import must stop at the first bad line, and
someone wrote it with `forEach`:

```java
static void applyAll(List<String> adjustments) {
    adjustments.stream().forEach(line -> {
        if (!valid(line)) return;                     // meant: stop at the first bad line
        applied.add(line);
    });
}
```

`valid` checks a line against `SKU ±n`, and `applied` is a list. With
`List.of("A-1 +5", "B-7 -x", "C-3 +2")`, then the same import written as the loop below:

```text
forEach applied: [A-1 +5, C-3 +2]
loop applied:    [A-1 +5], then line 2: B-7 -x
```

`return` in a lambda ends that one call of the lambda, so it skipped one line and the stream
carried on. A loop can `return`,
`break` or throw out of the method in the middle of the work:

```java
static void applyAllLoop(List<String> adjustments) throws BadLineException {
    for (int i = 0; i < adjustments.size(); i++) {
        String line = adjustments.get(i);
        if (!valid(line)) throw new BadLineException(i + 1, line);
        applied.add(line);
    }
}
```

Checked exceptions and running totals are things a lambda can't handle. It can't throw a checked
exception that `Consumer.accept` doesn't declare (unit 4.1.3), so a stream version has to wrap it,
and the caller's `catch (BadLineException e)` no longer sees it. It can't assign the method's local
variables (unit 4.1.1), so counting accepted lines, rejected lines and a total in one pass needs
holders or a custom collector, where a loop needs three `+=`. A stream also has no index: after a
`filter`, an element's position in the source is gone, while a loop has it in `i`. A stream reads
best as one transformation from a source to a result; when the body is about control flow, write the
loop.

<!-- practice 3 -->

## Summary

- **Building a pipeline runs nothing.** Intermediate operations record stages; the terminal
  operation runs them, so a pipeline without one does nothing.
- **Each element passes through every stage before the next one is pulled.** Short-circuiting
  operations (`findFirst`, `anyMatch`, `limit`) stop pulling; collecting first and picking later
  checks every element.
- **A stream can be operated on once.** A second operation on the same `Stream`, intermediate or
  terminal, throws `IllegalStateException`; keep a `Supplier<Stream<T>>` or a method that builds a
  new stream.
- **The source is read when the terminal operation runs, and must not change under it.** An
  `ArrayList` change made before the terminal operation is seen; one made during it ends in a
  `ConcurrentModificationException`, possibly after all the work is done; a removal also skips
elements and passes on a `null`.
- **A loop is clearer for early exits, checked exceptions and several running totals.** `return`
  in a `forEach` lambda skips one element and does not stop the stream.

## Reference

**How much of the source a terminal operation reads (1,000 elements, the 300th matches; Java 21,
sequential):**

| Pipeline | Elements checked by `p` |
|---|---|
| `filter(p).findFirst()`, `filter(p).findAny()`, `anyMatch(p)` | 300 |
| `allMatch(not p)`, `noneMatch(p)` | 300 (stop at the first element that decides) |
| `filter(p).limit(2).toList()` | 600 (until two matches) |
| `filter(p).toList().get(0)`, `filter(p).count() > 0` | 1,000 |
| `sorted().filter(p).findFirst()` | 300, but `sorted` pulls all 1,000 from the source first |
| `filter(p).sorted().findFirst()` | 1,000 |

**A second call on the same `Stream` object (Java 21):**

| Call | Second time |
|---|---|
| an intermediate operation (`filter`, `map`, ...) | `IllegalStateException` |
| a terminal operation, `iterator()`, `spliterator()` | `IllegalStateException` |
| an intermediate or terminal operation after `close()` | `IllegalStateException` |
| `onClose()` | `IllegalStateException` once the stream is used; before that, no exception |
| `parallel()`, `sequential()` | no exception, even after use or `close()`: they return the same stream |

**Changing an `ArrayList` source (Java 21):**

| When | What happens |
|---|---|
| after `stream()`, before the terminal operation | the change is seen (late-binding spliterator) |
| an add during `forEach` or `toList` | every original element processed, then `ConcurrentModificationException` |
| an add during a short-circuiting operation that is still pulling | `ConcurrentModificationException` right after the element that changed it |
| a remove during `forEach` | an element skipped and `null` passed on, then `ConcurrentModificationException`, or a `NullPointerException` first |

## Challenges

1. A service method returns `Stream<Order>` so that callers can filter further. What does it
   promise about when the list or table behind it is read, and how many times a caller may use
   it? Compare it with returning `List<Order>`.
2. A pipeline calls a remote price service in `map` and ends with `findFirst`. Someone adds
   `sorted()` before `findFirst`. Predict the number of remote calls, then measure it.
3. Take a long loop from your codebase that keeps several counters and exits early. Write it as a
   stream, and decide which version you would rather debug during an incident.

## Design Note: Why a Stream Runs Once

A `Stream` is closer to an `Iterator` than to a `Collection`: it wraps a cursor over the source
and a chain of stages that is fused into one pass when the terminal operation starts. Making it
reusable would mean either buffering every element, which an infinite or very large source can't
afford, or reading the source again, which a stream over a file, a socket or a generator can't do.
The designers chose one pass and an exception for a second use. The cost is that one pipeline
can't feed two results: counting and listing take two streams, or one collector that does both
(`Collectors.teeing`; collectors are unit 4.4.4).

## Code Examples

- Source: [StreamIntro.java](src/main/java/com/github/msorkhpar/claudejavatutor/streamsapi/StreamIntro.java)
- Test: [StreamIntroTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/streamsapi/StreamIntroTest.java)
