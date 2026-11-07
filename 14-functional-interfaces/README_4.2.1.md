# 4.2.1. Consumer — Accepting and Consuming Values

The nightly customer import counts its rows and writes one audit line per row. On a test file of
four rows the count is right and the audit is empty. The line that does both jobs:

```java
List<String> rows = List.of("r1", "r2", "r3", "r4");
LongAdder audited = new LongAdder();
long n = rows.stream().peek(r -> audited.increment()).count();
```

```text
count=4 audited=0
```

`peek` takes a `Consumer`, and the stream never called it. `Consumer<T>` is the
`java.util.function` interface for "do something with this value": one abstract method,
`void accept(T t)`, and a default `andThen`. You write the side effect; the code you pass it to
decides when, how often, in what order and on which thread it runs. The page has four rules, one
per heading.

## A Consumer Is Called for Its Side Effect, and Whatever the Lambda Returns Is Dropped

`accept` returns `void`, so a Consumer exists only for what it changes, such as a collection or
a log. It declares no checked exceptions either (unit 3.2.2 covers that case). An event handler remembers the ids it has seen. Pause before the output: after the third
call, has anything told us that `evt-17` was a repeat?

```java
Set<String> seen = new HashSet<>();
Consumer<String> record = seen::add;               // add returns boolean; the Consumer drops it
for (String id : List.of("evt-17", "evt-42", "evt-17")) record.accept(id);
System.out.println("seen: " + seen);
```

```text
seen: [evt-42, evt-17]
```

Nothing did: `Set.add` returned `false` and the Consumer dropped it. `seen::add`, like
`s -> seen.add(s)`, compiles as a Consumer because a `void` target may discard the result of a
method call (unit 4.1.2). If the caller needs the result, the step is not a Consumer; make the
call where its result is read:

```java
Set<String> seen = new HashSet<>();
int fresh = 0;
for (String id : List.of("evt-17", "evt-42", "evt-17")) if (seen.add(id)) fresh++;
System.out.println("new: " + fresh);
```

```text
new: 2
```

Chain a notifier after `seen::add` and subscribers hear about `evt-17` twice, because the only
thing that knew it was a repeat was the discarded `false`.

The JDK takes a Consumer where it iterates for you or hands you a value that may be absent.
`Iterable.forEach` calls it once per element. `Map.forEach` takes a `BiConsumer<K, V>` and calls
it with each key and value, in the map's iteration order (sorted here, because it is a `TreeMap`).
`Optional.ifPresentOrElse` takes a Consumer for the value and a `Runnable` for its absence:

```java
Map<String, Integer> stockBySku = new TreeMap<>(Map.of("SKU-1", 4, "SKU-2", 0));
stockBySku.forEach((sku, qty) -> System.out.println(sku + " -> " + qty));
Optional<String> owner = Optional.empty();
owner.ifPresentOrElse(o -> System.out.println("notify " + o),
        () -> System.out.println("no owner, nobody notified"));
```

```text
SKU-1 -> 4
SKU-2 -> 0
no owner, nobody notified
```

`IntStream.forEach` takes an `IntConsumer`, whose `accept(int)` needs no boxing, and
`ObjIntConsumer<T>`, with `accept(T, int)`, is the accumulator of `IntStream.collect`:

```java
ObjIntConsumer<StringBuilder> append = (sb, i) -> sb.append(i).append(';');
String s = IntStream.of(7, 3, 9).collect(StringBuilder::new, append, StringBuilder::append).toString();
```

```text
7;3;9;
```

## andThen Runs the Steps in Order, and Stops at the First One That Throws

`first.andThen(second)` returns a Consumer that calls `first.accept(t)`, then `second.accept(t)`
with the same `t`. If either throws, the exception reaches the caller, and if `first` throws,
`second` never runs. The parameter is a `Consumer<? super T>`, so a `Consumer<Object>` logger can
follow a `Consumer<Order>`.

An order service reserves stock and emails a confirmation. Order A-100 asks for 9 items and 5 are
in stock. Before reading the output, decide which of the two chains sends an email:

```java
public class Examples {
    record Order(String id, int qty) {}
    static int stock = 5;
    // ... the chains below run in a static method of this class
}
```

```java
Consumer<Order> reserve = o -> {
    if (o.qty() > stock) throw new IllegalStateException("out of stock: " + o.id());
    stock -= o.qty();
    System.out.println("reserved " + o.id());
};
Consumer<Order> confirm = o -> System.out.println("emailed confirmation for " + o.id());

Consumer<Order> reserveThenConfirm = reserve.andThen(confirm);
Consumer<Order> confirmThenReserve = confirm.andThen(reserve);
for (var step : List.of(Map.entry("reserve, then confirm", reserveThenConfirm),
                        Map.entry("confirm, then reserve", confirmThenReserve))) {
    System.out.println(step.getKey() + ":");
    try {
        step.getValue().accept(new Order("A-100", 9));
    } catch (IllegalStateException e) {
        System.out.println("  caller got " + e.getMessage());
    }
}
```

```text
reserve, then confirm:
  caller got out of stock: A-100
confirm, then reserve:
emailed confirmation for A-100
  caller got out of stock: A-100
```

The caller gets the same exception from both chains, but the second has already told a customer
that an order nobody can ship is on its way. `andThen` is a sequence, not a transaction: a step that has run
stays done when a later step throws. Put the step that can refuse first, and the step that can't
be taken back (an email, a payment, a message to another system) after everything that can fail.

### A Null Step Fails When the Chain Is Built, Not When It Runs

An extra hook is configured per deployment, and staging has none:

```java
Consumer<Order> confirm = o -> System.out.println("emailed " + o.id());
Consumer<Order> extraHook = null;                       // not configured
try {
    Consumer<Order> pipeline = confirm.andThen(extraHook);
    System.out.println("pipeline built");
    pipeline.accept(new Order("A-101", 1));
} catch (NullPointerException e) {
    System.out.println(e);
    for (StackTraceElement f : Arrays.copyOf(e.getStackTrace(), 3)) System.out.println("\tat " + f);
}
```

```text
java.lang.NullPointerException
	at java.base/java.util.Objects.requireNonNull(Objects.java:233)
	at java.base/java.util.function.Consumer.andThen(Consumer.java:64)
	at Examples.nullStep(Examples.java:75)
```

The third frame is the `andThen` line. `pipeline built` never prints: `andThen` only composes, calls neither step, and checks its
argument with `Objects.requireNonNull` before it returns. For a
step that must exist, that is what you want: a service with a missing hook fails at startup, not
on the first order an hour later. For an optional step, decide explicitly, with
`extraHook == null ? confirm : confirm.andThen(extraHook)`, or a do-nothing `t -> {}` (the JDK
has no ready-made no-op Consumer).

<!-- practice 1 -->

## A Stream Calls Your Consumer Only for the Elements It Needs, and peek May Not Run at All

Back to the import. `peek(action)` is an intermediate operation: it calls the Consumer as each
element passes through, if it passes through. Four sequential pipelines over the same four rows,
each with its own `LongAdder`:

```java
long n = rows.stream().peek(r -> audited.increment()).count();
long m = rows.stream().filter(r -> !r.isEmpty()).peek(r -> audited2.increment()).count();
Optional<String> first = rows.stream().peek(r -> audited3.increment()).findFirst();
rows.stream().peek(r -> audited4.increment());       // no terminal operation
```

```text
count=4 audited=0
with filter: count=4 audited=4
findFirst: r1 audited=1
no terminal: audited=0
```

`count()` on a list's stream knows the answer from the list's size. Its javadoc lets the
implementation skip the pipeline in that case: "no source elements will be traversed and no
intermediate operations will be evaluated". A `filter` hides the size, so the second pipeline ran
every row. `map` and `sorted` keep the size, and `map(...).peek(...).count()` skips `peek` the
same way. `findFirst` pulls one element and stops, so `peek` ran once. A stream with no terminal
operation runs nothing.

A refactor that removes a `filter` can therefore turn the audit off without a test noticing. The
javadoc says `peek` "exists mainly to support debugging", but nothing switches it on or off: these
runs used no flags, and the skip comes from `count()` alone. A side effect that must happen once per
element goes in the stream's terminal `forEach`, or, as here, in `Iterable.forEach` beside the
stream:

```java
rows.forEach(r -> audited5.increment());
long imported = rows.size();
```

```text
forEach: imported=4 audited=4
```

> **Java 8 differs.** Java 8's `count()` traversed the stream, so a `peek` before it ran. The
> skip, and the javadoc note that allows it, came with Java 9, which is how code like the
> import's stops auditing after an upgrade.

<!-- practice 2 -->

## On a Parallel Stream, forEach Calls the Consumer from Several Threads, in No Order

`Stream.forEach` promises no order, and on a parallel stream it calls the Consumer from whichever
worker thread holds the element, several at once. `forEachOrdered` calls it one element after
another, in the source's order:

```java
List<String> ids = List.of("o1", "o2", "o3", "o4", "o5", "o6", "o7", "o8");
StringBuilder a = new StringBuilder(), b = new StringBuilder();
ids.parallelStream().forEach(id -> { synchronized (a) { a.append(id).append(' '); } });
ids.parallelStream().forEachOrdered(id -> b.append(id).append(' '));
```

```text
forEach:        o6 o5 o8 o7 o1 o3 o4 o2 
forEachOrdered: o1 o2 o3 o4 o5 o6 o7 o8 
```

`forEachOrdered` may still call the Consumer on different threads, but each call
happens-before the next, so `b` needs no lock; `a` does. What happens without one? Here 10,000
numbers go into an `ArrayList` from a parallel `forEach`, five times:

```java
for (int run = 1; run <= 5; run++) {
    List<Integer> out = new ArrayList<>();
    IntStream.range(0, 10_000).parallel().forEach(out::add);
    System.out.println("run " + run + ": size " + out.size());
}
```

```text
run 1: size 8414
run 2: size 9986
run 3: size 6702
run 4: size 8692
run 5: size 8586
```

Up to a third of the elements are lost, with no exception. `ArrayList.add` reads the size, writes
the slot and stores the new size. Two threads that read the same size write the same slot, and
one element is lost. When the array grows during a race, the same code can throw
`ArrayIndexOutOfBoundsException` instead. With ten elements it failed in about one run in six, so
the unit test is flaky rather than red.

The fix is to let the stream build the result, so that no thread touches a shared list:

```java
List<Integer> collected = IntStream.range(0, 10_000).parallel().boxed().toList();
```

```text
toList(): size 10000
```

`collect(Collectors.toList())` does the same. When the Consumer writes to something that is not
thread-safe, such as a file writer, compute in parallel and collect, then hand the result to the
Consumer from one thread, or use `forEachOrdered`. A thread-safe target, such as a `ConcurrentLinkedQueue`, or a
lock around the Consumer, as for `a` above, keeps every element and still gets them in no order:
5,000 ids added to a `ConcurrentLinkedQueue` this way came out 5,000 strong, in a different order
on each of five runs.

<!-- practice 3 -->

<!-- practice 4 -->

## Summary

- **A Consumer is for its side effect.** A lambda or method reference such as `seen::add` that
  returns a value still compiles as a Consumer and the value is dropped, so a step whose result
  matters is not a Consumer.
- **`andThen` runs the steps in order and stops at the first throw.** Steps that already ran stay
  done, so the irreversible step goes last; a `null` step fails when the chain is built.
- **A stream calls a Consumer only for the elements it needs.** `peek` may run for every element,
  one, or none (`count()` on a sized stream), so a required side effect goes in the terminal
  operation.
- **A parallel `forEach` calls the Consumer from several threads in no order.** A target that is
  not thread-safe loses elements silently; collect instead, or use `forEachOrdered`.

## Reference

**The Consumer family (`java.util.function`):**

| Interface | Method | `andThen` | Where the JDK takes one |
|---|---|---|---|
| `Consumer<T>` | `void accept(T t)` | yes | `Iterable.forEach`, `Stream.forEach`, `forEachOrdered`, `peek`, `Optional.ifPresent`, `ifPresentOrElse` |
| `BiConsumer<T, U>` | `void accept(T t, U u)` | yes | `Map.forEach`; accumulator and combiner of `Stream.collect(supplier, accumulator, combiner)` |
| `IntConsumer`, `LongConsumer`, `DoubleConsumer` | `void accept(int value)`, and so on | yes | `IntStream.forEach` and `peek`, likewise for long and double |
| `ObjIntConsumer<T>`, `ObjLongConsumer<T>`, `ObjDoubleConsumer<T>` | `void accept(T t, int value)`, and so on | no | accumulator of `IntStream.collect`, likewise for long and double |

**When a stream calls the Consumer you give it:**

| Operation | Calls it for | Order | On a parallel stream |
|---|---|---|---|
| `forEach` | every element | none promised | from several threads at once |
| `forEachOrdered` | every element | encounter order, if the stream has one | one element after another |
| `peek` | the elements the terminal operation pulls (on a sequential stream, one for `findFirst`); none when `count()` can be computed from the source | as elements pass | from several threads at once |

## Challenges

1. A team wants "log every element" in a pipeline that ends in `count()`. Compare `peek`, a
   `filter` that always returns `true`, and logging in a separate loop: which still logs after a
   refactor, and which would you accept in review?
2. You compose six handlers with `andThen`, and one of them, a metrics push, fails once a day.
   Design the chain so that failure never blocks the others, without hiding the failures of the
   steps that matter.
3. Measure how many elements a parallel `forEach` into an `ArrayList` loses for 1,000, 10,000 and
   1,000,000 elements on your machine. Which size do unit tests usually use, and what do they see?

## Design Note: Why the Stream Library May Skip Your Consumer

A stream describes a result, not a sequence of calls. That lets the library fuse operations, stop
early for `findFirst`, split the work across threads, and skip the traversal when the source
already knows the answer, as `count()` does. Each of those freedoms assumes the operations have
no effects the caller depends on. `peek` and `forEach` are where a Consumer enters that model,
and the library treats them on its own terms: `peek` as a debugging hook it may never call,
`forEach` as an action whose order and thread it chooses.

The cost falls on code that treats a stream as a loop. A loop promises one call per element, in
order, on your thread; a stream promises the result of its terminal operation.

## Code Examples

- Source: [ConsumerDemo.java](src/main/java/com/github/msorkhpar/claudejavatutor/functionalinterfaces/ConsumerDemo.java)
- Test: [ConsumerDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/functionalinterfaces/ConsumerDemoTest.java)
