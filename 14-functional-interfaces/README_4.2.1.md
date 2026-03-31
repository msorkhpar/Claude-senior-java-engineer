# 4.2.1. Consumer — Accepting and Consuming Values

## Concept Explanation

`Consumer<T>` is one of the four core functional interfaces in `java.util.function`. It represents an operation that
accepts a single input argument of type `T` and returns **no result** (`void`). The defining characteristic of a
Consumer is that it is used exclusively for **side effects**: printing, logging, storing data, modifying external state,
or sending notifications.

**Real-world analogy**: Think of a Consumer like a recycling bin. You put something in (the input), and the bin
processes it — but nothing comes back out. The action is the point, not a return value. A printer is also a perfect
analogy: you feed it a document and it produces output as a side effect (the printed page), but the method call itself
returns nothing.

`Consumer<T>` is annotated with `@FunctionalInterface`, meaning it has exactly one abstract method:

```java
@FunctionalInterface
public interface Consumer<T> {
    void accept(T t);

    default Consumer<T> andThen(Consumer<? super T> after) { ... }
}
```

The `andThen()` default method enables **chaining**: when you call `c1.andThen(c2)`, calling `accept()` on the result
will execute `c1` first, then `c2`, on the same input. This is useful for composing pipelines of side-effecting
operations (e.g., log then store).

**BiConsumer<T, U>** is the two-argument variant: it accepts two arguments of types `T` and `U` and returns void. It
is the natural fit for iterating `Map` entries (key + value) or any operation that requires two correlated inputs.

**Primitive specializations** exist to avoid autoboxing overhead when working with primitive types:
- `IntConsumer` — `void accept(int value)`
- `LongConsumer` — `void accept(long value)`
- `DoubleConsumer` — `void accept(double value)`
- `ObjIntConsumer<T>` — `void accept(T t, int value)`
- `ObjLongConsumer<T>` — `void accept(T t, long value)`
- `ObjDoubleConsumer<T>` — `void accept(T t, double value)`

## Key Points to Remember

1. `Consumer<T>` has one abstract method: `void accept(T t)` — it consumes a value and returns nothing.
2. `andThen(Consumer after)` chains two consumers sequentially; if `after` is null, a `NullPointerException` is thrown.
3. `Consumer` is the right choice when you need iteration side effects (e.g., `List.forEach(Consumer)`).
4. `BiConsumer<T, U>` is used by `Map.forEach(BiConsumer)` to process key-value pairs.
5. For performance-critical code with primitives, use `IntConsumer`, `LongConsumer`, or `DoubleConsumer` to avoid boxing.
6. Variables captured inside a Consumer lambda must be **effectively final**. Use an `int[]` array trick for mutable counters.
7. `Consumer.andThen()` guarantees **left-to-right** order of execution; the second consumer always runs even if the first throws.
8. Consumers cannot be used where a return value is needed — use `Function` instead.

## Relevant Java 21 Features

- **Java 8**: `Consumer<T>`, `BiConsumer<T,U>`, and all primitive specializations introduced in `java.util.function`.
- **Java 8**: `Iterable.forEach(Consumer)` and `Map.forEach(BiConsumer)` added as default methods — the primary use sites for Consumer.
- **Java 16+**: Records work seamlessly with Consumers. A `Consumer<MyRecord>` can destructure and process record components.
- **Java 21**: Virtual threads mean Consumer-based side effects in `forEach` loops can safely be submitted to virtual thread executors without thread pool exhaustion concerns.
- **Java 21**: Pattern matching in switch expressions can be combined with Consumer dispatch to route inputs to the right consumer based on type.

## Common Pitfalls and How to Avoid Them

1. **Passing null to `andThen()`** — `andThen` throws `NullPointerException` if the `after` Consumer is null, at call time (not at `accept` time).

   ```java
   // Broken: NPE thrown when andThen is called
   Consumer<String> printer = System.out::println;
   Consumer<String> chained = printer.andThen(null); // throws NPE here!
   ```

   ```java
   // Fix: guard against null before chaining
   Consumer<String> chain(Consumer<String> first, Consumer<String> second) {
       return second != null ? first.andThen(second) : first;
   }
   ```

2. **Trying to modify a captured local variable** — Lambda closures require variables to be effectively final.

   ```java
   // Broken: count is modified, so it is not effectively final
   int count = 0;
   list.forEach(item -> count++); // compilation error
   ```

   ```java
   // Fix 1: use an array wrapper (effectively final reference, mutable content)
   int[] count = {0};
   list.forEach(item -> count[0]++);

   // Fix 2: use AtomicInteger (thread-safe)
   AtomicInteger count = new AtomicInteger(0);
   list.forEach(item -> count.incrementAndGet());
   ```

3. **Using Consumer where a Function is needed** — If you need a return value from the operation, use `Function<T,R>`, not `Consumer<T>`.

   ```java
   // Broken: cannot use Consumer when caller needs a result
   Consumer<String> upper = s -> s.toUpperCase(); // result is discarded!
   ```

   ```java
   // Fix: use Function when the result matters
   Function<String, String> upper = String::toUpperCase;
   String result = upper.apply("hello"); // "HELLO"
   ```

4. **Relying on Consumer order within parallel streams** — `forEach` with a Consumer does NOT guarantee order in parallel streams. Use `forEachOrdered` if order matters.

   ```java
   // Broken: order unpredictable with parallel()
   list.parallelStream().forEach(System.out::println);
   ```

   ```java
   // Fix: use forEachOrdered to preserve encounter order
   list.parallelStream().forEachOrdered(System.out::println);
   ```

5. **Exception handling in Consumer** — `Consumer.accept()` does not declare checked exceptions. Checked exceptions must be caught inside the lambda body.

   ```java
   // Broken: IOException cannot propagate through Consumer.accept()
   list.forEach(path -> Files.delete(path)); // compile error
   ```

   ```java
   // Fix: wrap in try-catch inside the lambda
   list.forEach(path -> {
       try { Files.delete(path); }
       catch (IOException e) { throw new UncheckedIOException(e); }
   });
   ```

## Best Practices and Optimization Techniques

1. **Keep Consumer lambdas focused on a single side effect.** Chain multiple concerns with `andThen()` rather than stuffing everything into one lambda — this makes each step independently testable.

2. **Prefer method references for simple consumers.** `list.forEach(System.out::println)` is cleaner than `list.forEach(s -> System.out.println(s))`.

3. **Use primitive specializations for numeric iteration.** If you are iterating over an `int[]` or a collection of numbers, `IntConsumer` avoids boxing each element.

   ```java
   // Suboptimal: each int is boxed to Integer
   Consumer<Integer> printer = n -> System.out.println(n);

   // Optimal: no boxing
   IntConsumer printer = n -> System.out.println(n);
   IntStream.range(0, 100).forEach(printer);
   ```

4. **Use `BiConsumer` for `Map.forEach` iteration** — it is idiomatic and avoids creating intermediate `Map.Entry` objects.

   ```java
   Map<String, Integer> scores = Map.of("Alice", 95, "Bob", 87);
   scores.forEach((name, score) -> System.out.println(name + ": " + score));
   ```

5. **Pass Consumers as parameters to make methods flexible.** Instead of hardcoding what happens to processed items, accept a `Consumer<T>` parameter — this is a clean implementation of the Strategy pattern.

6. **Use `Consumer` to implement the Visitor pattern** when processing tree structures or heterogeneous collections.

## Edge Cases and Their Handling

1. **Empty list input** — `List.forEach(Consumer)` is a no-op on an empty list; no errors or special handling needed.

   ```java
   List<String> empty = List.of();
   empty.forEach(System.out::println); // nothing happens, no exception
   ```

2. **Null elements in the list** — A Consumer that does not null-check will throw `NullPointerException` if the list contains nulls. Wrap with a null guard:

   ```java
   Consumer<String> safeUpper = s -> {
       if (s != null) System.out.println(s.toUpperCase());
   };
   ```

3. **Consumer that throws a RuntimeException** — The exception propagates out of `forEach`; subsequent elements are NOT processed. If partial failure is acceptable, catch inside the lambda.

4. **BiConsumer with null key or value** — `Map.forEach` will pass null keys/values if the map implementation permits them (e.g., `HashMap` does, `Map.of()` does not). Guard accordingly.

5. **Self-referencing `andThen` chain** — Chaining a consumer with itself is valid and simply runs the same logic twice:

   ```java
   Consumer<String> printer = System.out::println;
   Consumer<String> twice = printer.andThen(printer);
   twice.accept("hello"); // prints "hello" twice
   ```

6. **Consumer with mutable external state** — When a Consumer captures and mutates a shared mutable object (e.g., a list it is populating), thread-safety is the developer's responsibility. Use thread-safe collections or synchronization when the Consumer may be invoked from multiple threads.

## Interview-specific Insights

Interviewers focus on:

- Whether you know the difference between `Consumer` and `Function` (both accept input; only Function returns a value).
- Whether you understand `andThen()` execution order and what happens if one consumer throws.
- Knowledge of `BiConsumer` and its use with `Map.forEach()`.
- Ability to explain the "effectively final" requirement and workarounds (`int[]` array trick, `AtomicInteger`).
- Understanding of primitive specializations and *why* they exist (boxing overhead).
- Real-world use cases: event handlers, validators, formatters, loggers.

**Common tricky questions:**
- "What is the difference between `Consumer.andThen()` and `Function.andThen()`?" (Consumer chains side effects; Function chains transformations.)
- "If the first Consumer in an `andThen` chain throws a RuntimeException, does the second Consumer execute?" (No — the exception propagates before `andThen`'s second consumer is called.)
- "Can a Consumer return a value?" (No — that would make it a Function.)
- "How do you iterate a Map with a BiConsumer?" (Use `map.forEach((k, v) -> ...)` — `Map.forEach` takes a `BiConsumer`.)

## Interview Q&A Section

**Q1: What is `Consumer<T>` and when should you use it instead of `Function<T,R>`?**

```text
A1: Consumer<T> represents an operation that accepts one input and returns nothing (void). It is designed
for side-effecting operations: printing, logging, storing, updating state, or sending notifications.

Use Consumer when the caller does NOT need a return value from the operation.
Use Function<T,R> when the caller needs the result of a transformation.

Key distinction:
- Consumer.accept(T) returns void — suitable for forEach, event handlers, logging
- Function.apply(T) returns R   — suitable for stream map(), data transformation

Example:
    Consumer<String> logger = msg -> log.info(msg);      // side effect only
    Function<String, Integer> length = String::length;   // returns Integer

In the Streams API:
    stream.forEach(consumer)        // terminal, no result
    stream.map(function)            // intermediate, produces new stream
```

```java
// Consumer: side effect only
Consumer<String> printUpper = s -> System.out.println(s.toUpperCase());
printUpper.accept("hello"); // prints HELLO, returns nothing

// Function: transformation with result
Function<String, String> toUpper = String::toUpperCase;
String result = toUpper.apply("hello"); // returns "HELLO"

// Using Consumer with forEach
List<String> items = List.of("a", "b", "c");
items.forEach(printUpper); // prints each in uppercase

// Would NOT compile: Consumer doesn't return a value
// String s = printUpper.accept("hello"); // compile error
```

---

**Q2: How does `Consumer.andThen()` work and what is its execution order?**

```text
A2: Consumer.andThen(after) creates a new Consumer that, when accept() is called:
  1. Calls this.accept(t) first
  2. Calls after.accept(t) second (with the same original input)

The order is strictly left-to-right. Both consumers receive the exact same input — the result
(void) of the first is not passed to the second. If the first consumer throws a RuntimeException,
the second consumer is NOT executed.

andThen() throws NullPointerException immediately if 'after' is null.

This is used to compose pipelines of side effects, for example: validate then log then store.
```

```java
List<String> auditLog = new ArrayList<>();
List<String> storage = new ArrayList<>();

Consumer<String> logger = item -> auditLog.add("Processing: " + item);
Consumer<String> storer = item -> storage.add(item);
Consumer<String> printer = System.out::println;

// Chain: logger -> storer -> printer (left to right)
Consumer<String> pipeline = logger.andThen(storer).andThen(printer);
pipeline.accept("Alice");

// After accept("Alice"):
// auditLog = ["Processing: Alice"]
// storage  = ["Alice"]
// console  = "Alice"

// Same input ("Alice") is passed to each step
```

---

**Q3: What is `BiConsumer<T,U>` and how is it used with `Map.forEach()`?**

```text
A3: BiConsumer<T,U> accepts two arguments (types T and U) and returns void. Like Consumer,
it is designed for side-effecting operations, but on two correlated inputs.

Map.forEach(BiConsumer<K,V>) is the canonical use case. It iterates all entries, passing each
key and value as separate arguments to the BiConsumer — avoiding the need to create Map.Entry
objects or call entry.getKey()/entry.getValue().

BiConsumer also has andThen(BiConsumer after) for chaining, just like Consumer.
```

```java
Map<String, Integer> scores = new LinkedHashMap<>();
scores.put("Alice", 95);
scores.put("Bob", 87);
scores.put("Charlie", 91);

// Basic BiConsumer with Map.forEach
BiConsumer<String, Integer> printer =
    (name, score) -> System.out.println(name + " scored " + score);
scores.forEach(printer);

// Chaining two BiConsumers
List<String> log = new ArrayList<>();
BiConsumer<String, Integer> logger = (name, score) -> log.add(name + "=" + score);
BiConsumer<String, Integer> combined = logger.andThen(printer);
scores.forEach(combined);
// log will contain ["Alice=95", "Bob=87", "Charlie=91"]

// BiConsumer to build a formatted report
BiConsumer<String, Integer> formatter =
    (name, score) -> System.out.printf("%-10s | %3d%n", name, score);
scores.forEach(formatter);
```

---

**Q4: Explain the "effectively final" requirement for lambda closures, and how do you work around it for counters?**

```text
A4: A variable captured by a lambda must be either declared final or "effectively final" —
meaning its value is never changed after its first assignment. This rule exists because lambdas
may outlive the stack frame that created them; if the variable were mutable, different invocations
could see inconsistent values, creating race conditions.

Local primitive variables (int, long, etc.) cannot be mutated inside a lambda body.
The two standard workarounds are:

1. int[] array trick: an int[] is a reference type; the array reference is effectively final,
   but the array contents can be modified.

2. AtomicInteger / AtomicLong: thread-safe reference types that wrap a mutable value.
   Preferred when the Consumer may run concurrently.
```

```java
// Does NOT compile: count is modified, so not effectively final
// int count = 0;
// list.forEach(item -> count++); // compile error

// Workaround 1: int[] array (single-threaded use only)
List<String> items = List.of("a", "b", "c");
int[] count = {0};
items.forEach(item -> count[0]++);
System.out.println(count[0]); // 3

// Workaround 2: AtomicInteger (thread-safe)
AtomicInteger atomicCount = new AtomicInteger(0);
items.forEach(item -> atomicCount.incrementAndGet());
System.out.println(atomicCount.get()); // 3

// Workaround 3: Don't use a Consumer at all — use stream reduction
long streamCount = items.stream().count();
```

---

**Q5: What are the primitive specializations of Consumer and why do they matter?**

```text
A5: The generic Consumer<T> requires its type parameter T to be a reference type. When T is
Integer, Long, or Double, each value must be autoboxed from the primitive — allocating a heap
object per element. Over millions of elements, this creates significant GC pressure and
reduces throughput.

Java provides primitive-specific variants that work directly with primitive types:
- IntConsumer:      void accept(int value)
- LongConsumer:     void accept(long value)
- DoubleConsumer:   void accept(double value)
- ObjIntConsumer<T>:    void accept(T t, int value)
- ObjLongConsumer<T>:   void accept(T t, long value)
- ObjDoubleConsumer<T>: void accept(T t, double value)

These integrate with the primitive stream API (IntStream, LongStream, DoubleStream).
Use them when processing numeric data in hot code paths.
```

```java
// Suboptimal: each element is boxed to Integer
Consumer<Integer> boxedPrinter = n -> System.out.println(n);
List.of(1, 2, 3).forEach(boxedPrinter); // boxing: int -> Integer

// Optimal: no boxing — works directly with int primitives
IntConsumer primitivePrinter = n -> System.out.println(n);
IntStream.rangeClosed(1, 3).forEach(primitivePrinter); // no boxing

// ObjIntConsumer: T + int -> void (useful for index-aware iteration)
ObjIntConsumer<String> indexedPrinter = (s, i) -> System.out.println(i + ": " + s);
List<String> names = List.of("Alice", "Bob", "Charlie");
for (int i = 0; i < names.size(); i++) {
    indexedPrinter.accept(names.get(i), i);
}
// 0: Alice
// 1: Bob
// 2: Charlie
```

---

**Q6: How would you implement a reusable null-safe Consumer wrapper?**

```text
A6: A utility method that wraps any Consumer to silently skip null inputs is a common
real-world pattern. The outer Consumer does the null check; the inner Consumer handles
non-null values. This keeps null-handling concerns separated from business logic.
```

```java
// Utility: wrap any Consumer to skip nulls
public static <T> Consumer<T> nullSafe(Consumer<T> downstream) {
    return item -> {
        if (item != null) {
            downstream.accept(item);
        }
    };
}

// Usage
Consumer<String> upperPrinter = s -> System.out.println(s.toUpperCase());
Consumer<String> safeUpperPrinter = nullSafe(upperPrinter);

List<String> withNulls = Arrays.asList("Alice", null, "Bob", null, "Charlie");
withNulls.forEach(safeUpperPrinter);
// Prints: ALICE, BOB, CHARLIE (nulls silently skipped)

// Can also compose null-safe consumers
Consumer<String> logger = nullSafe(s -> System.out.println("Log: " + s));
Consumer<String> pipeline = safeUpperPrinter.andThen(logger);
withNulls.forEach(pipeline);
```

---

**Q7: Demonstrate how Consumer implements the validation-dispatch pattern.**

```text
A7: A common pattern is to accept a list plus two Consumers — one for valid items and one for
invalid items. The dispatcher Consumer checks each item and routes it to the appropriate
consumer. This decouples validation logic from what happens to valid/invalid items, following
the Open/Closed Principle.
```

```java
public void validateAndRoute(
        List<String> items,
        Consumer<String> validHandler,
        Consumer<String> invalidHandler) {

    Consumer<String> dispatcher = item -> {
        if (item != null && !item.isBlank()) {
            validHandler.accept(item);
        } else {
            invalidHandler.accept(item);
        }
    };
    items.forEach(dispatcher);
}

// Usage
List<String> valid = new ArrayList<>();
List<String> invalid = new ArrayList<>();

validateAndRoute(
    Arrays.asList("Alice", "", null, "Bob", "  "),
    valid::add,
    item -> invalid.add(item == null ? "<null>" : "<blank>")
);

System.out.println("Valid:   " + valid);   // [Alice, Bob]
System.out.println("Invalid: " + invalid); // [<blank>, <null>, <blank>]
```

## Code Examples

- Source: [ConsumerDemo.java](src/main/java/com/github/msorkhpar/claudejavatutor/functionalinterfaces/ConsumerDemo.java)
- Test: [ConsumerDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/functionalinterfaces/ConsumerDemoTest.java)
