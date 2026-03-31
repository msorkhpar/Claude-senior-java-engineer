# 4.4.1. Introduction to the Streams API

## Concept Explanation

The Java Streams API, introduced in Java 8, represents a fundamental shift in how Java developers process collections
of data. A stream is a sequence of elements that supports sequential and parallel aggregate operations. Unlike
collections, streams do not store data; they carry values from a source through a pipeline of computational steps.

**Real-world analogy**: Think of a stream like an assembly line in a factory. Raw materials (data) enter one end,
travel through a series of workstations (operations), and finished products (results) emerge at the other end. Each
workstation performs a specific transformation or check. Crucially, the assembly line only runs when triggered — no
product is made until someone presses the "start" button (the terminal operation).

### The Stream Pipeline

A stream pipeline consists of three parts:
1. **Source** — where the data comes from (collection, array, file, generator)
2. **Intermediate operations** — lazy transformations that return a new stream (filter, map, sorted, etc.)
3. **Terminal operation** — the trigger that consumes the stream and produces a result (collect, forEach, reduce, etc.)

```
Source → [intermediate op 1] → [intermediate op 2] → ... → Terminal Op → Result
```

### Lazy Evaluation

One of the most important characteristics of streams is **lazy evaluation**. Intermediate operations are not executed
until a terminal operation is invoked. This enables two key optimizations:

- **Short-circuiting**: Operations like `findFirst()` or `limit()` can terminate early without processing all elements.
- **Pipeline fusion**: The JVM can merge multiple operations into a single pass over the data.

```java
// Nothing executes until the terminal operation collect() is called
Stream<String> stream = list.stream()
    .filter(s -> s.length() > 3)   // NOT executed yet
    .map(String::toUpperCase);      // NOT executed yet

List<String> result = stream.collect(Collectors.toList()); // NOW everything runs
```

### Streams vs Collections

| Feature           | Collection             | Stream                         |
|-------------------|------------------------|--------------------------------|
| Data storage      | Stores elements        | Does not store elements        |
| Iteration         | External (for loop)    | Internal (declarative)         |
| Reuse             | Can be iterated many times | Single-use (consumed once) |
| Evaluation        | Eager                  | Lazy                           |
| Modification      | Elements can be added/removed | Cannot modify source    |
| Parallel support  | Manual synchronization needed | Built-in parallel support |

## Key Points to Remember

1. Streams are **single-use**: once consumed by a terminal operation, they cannot be reused.
2. Streams do **not modify the source**: all operations produce new streams or results.
3. Intermediate operations are **lazy**: they don't execute until a terminal operation is called.
4. **Short-circuit operations** can stop processing early (e.g., `findFirst`, `anyMatch`, `limit`).
5. Streams support both **sequential** and **parallel** execution.
6. There are specialized streams for primitives: `IntStream`, `LongStream`, `DoubleStream` — these avoid boxing overhead.
7. A stream pipeline has **zero or more** intermediate operations and **exactly one** terminal operation.
8. Stream operations should be **stateless and non-interfering** (should not modify the source or rely on mutable shared state).

## Relevant Java 21 Features

- **Sequenced collections** (Java 21, JEP 431): `SequencedCollection`, `SequencedSet`, and `SequencedMap` provide
  ordered access, and their streams preserve encounter order more predictably.
- **Pattern matching in switch** (Java 21): Can be combined with stream operations for powerful data processing.
- **Virtual threads** (Java 21, JEP 444): Parallel streams can leverage virtual threads for I/O-bound work.
- **Gatherers** (Java 22 preview, JEP 461): A new mechanism for custom intermediate stream operations, extending what
  the built-in operations can do.
- Stream API has been continuously enhanced since Java 8:
  - Java 9: `takeWhile`, `dropWhile`, `Stream.iterate` with predicate, `Stream.ofNullable`
  - Java 10: `Collectors.toUnmodifiableList/Set/Map`
  - Java 16: `Stream.toList()` (unmodifiable) — shortcut for `collect(Collectors.toList())`

## Common Pitfalls and How to Avoid Them

1. **Reusing a consumed stream**: Streams can only be consumed once.
   ```java
   // BROKEN
   Stream<String> stream = list.stream().filter(s -> s.length() > 3);
   long count = stream.count();          // Stream consumed here
   List<String> result = stream.collect(Collectors.toList()); // IllegalStateException!
   ```
   **Fix**: Create a new stream for each terminal operation.
   ```java
   long count = list.stream().filter(s -> s.length() > 3).count();
   List<String> result = list.stream().filter(s -> s.length() > 3).collect(Collectors.toList());
   ```

2. **Forgetting the terminal operation**: Without a terminal operation, nothing executes.
   ```java
   // BROKEN — side effects never happen!
   list.stream().filter(s -> s.length() > 3).map(String::toUpperCase);
   ```
   **Fix**: Always end with a terminal operation.
   ```java
   List<String> result = list.stream()
       .filter(s -> s.length() > 3)
       .map(String::toUpperCase)
       .collect(Collectors.toList());
   ```

3. **Modifying the source during streaming**: This leads to undefined behavior or `ConcurrentModificationException`.
   ```java
   // BROKEN
   list.stream().filter(s -> {
       if (s.isEmpty()) list.remove(s); // Modifying source!
       return true;
   }).count();
   ```
   **Fix**: Collect to a new list; never modify the source inside a stream operation.

4. **Using stateful lambdas with parallel streams**: Stateful intermediate operations in parallel streams can produce
   wrong results.
   ```java
   // BROKEN with parallel streams
   List<Integer> seen = new ArrayList<>(); // shared mutable state
   list.parallelStream().filter(seen::add).collect(Collectors.toList()); // Race condition!
   ```
   **Fix**: Use stateless operations, or proper concurrent collections.

## Best Practices and Optimization Techniques

1. **Prefer `Stream.toList()` (Java 16+)** over `collect(Collectors.toList())` for immutable results.
2. **Use primitive streams** (`IntStream`, `LongStream`, `DoubleStream`) when working with primitives to avoid boxing.
3. **Avoid storing streams in fields** — streams are ephemeral pipeline descriptors, not data containers.
4. **Keep lambdas in stream operations short and stateless** — extract complex logic to named methods.
5. **Use `peek()` only for debugging**, not for side effects in production code.
6. **Prefer method references** over equivalent lambdas for readability (e.g., `String::toUpperCase` vs `s -> s.toUpperCase()`).
7. **Short-circuit when possible**: use `findFirst()`, `anyMatch()`, `limit()` to avoid processing the entire stream.

## Edge Cases and Their Handling

1. **Empty streams**: Most terminal operations handle empty streams gracefully.
   ```java
   Optional<String> first = Stream.<String>empty().findFirst(); // Optional.empty()
   long count = Stream.empty().count(); // 0
   ```

2. **Null elements**: Streams allow null elements, but some operations (like `sorted()` with natural ordering) will
   throw `NullPointerException`. Filter nulls explicitly.
   ```java
   list.stream()
       .filter(Objects::nonNull)
       .sorted()
       .collect(Collectors.toList());
   ```

3. **Infinite streams**: `Stream.generate()` and `Stream.iterate()` create infinite streams. Always use `limit()` or
   short-circuiting terminal operations.
   ```java
   Stream.iterate(0, n -> n + 1).limit(10).forEach(System.out::println);
   ```

4. **Stream of a single element**: Behaves correctly; just processes that one element.

## Interview-specific Insights

Interviewers frequently ask about:
- The difference between streams and collections
- Lazy evaluation and when it matters
- How streams handle null values
- The distinction between intermediate and terminal operations
- Why streams are single-use
- Performance implications (when to use parallel streams)
- `Stream.toList()` vs `Collectors.toList()` (Java 16 difference: the former is unmodifiable)

Tricky questions to expect:
- "What happens if you don't call a terminal operation on a stream?"
- "Can you have a stream pipeline with no intermediate operations?"
- "What is the difference between `map` and `flatMap`?"
- "Why should stream lambdas be stateless?"

## Interview Q&A Section

**Q1: What is the Java Streams API and how does it differ from Collections?**

```text
A1: The Java Streams API is a framework for processing sequences of elements with aggregate operations in a
declarative, functional style. Key differences from Collections:

1. Storage: Collections store data; Streams are computational pipelines over a data source.
2. Iteration: Collections use external iteration (for loops); Streams use internal iteration (the framework
   controls the traversal).
3. Single-use: A Stream is consumed by a terminal operation and cannot be reused. A Collection can be iterated
   any number of times.
4. Laziness: Stream intermediate operations are lazy — they don't execute until a terminal operation is reached.
   Collection operations are eager.
5. Parallel execution: Streams have built-in support for parallel processing with parallelStream() or parallel().
   Collections require manual synchronization.
6. Modification: Streams do not allow modifying the underlying data source. Collections are mutable.
```

```java
// Collection: external iteration, eager
List<String> result = new ArrayList<>();
for (String s : list) {
    if (s.length() > 3) result.add(s.toUpperCase());
}

// Stream: internal iteration, lazy, declarative
List<String> result2 = list.stream()
    .filter(s -> s.length() > 3)
    .map(String::toUpperCase)
    .collect(Collectors.toList());
```

**Q2: What does "lazy evaluation" mean in the context of streams, and why does it matter?**

```text
A2: Lazy evaluation means that intermediate stream operations are not executed until a terminal operation is
invoked. The stream pipeline is constructed as a description of what to do, but the actual computation is
deferred until needed.

Why it matters:
1. Short-circuiting: Operations like findFirst(), anyMatch(), and limit() can terminate early without
   processing all elements, potentially improving performance significantly for large datasets.
2. Pipeline fusion: The JVM can merge multiple intermediate operations into a single pass over the data,
   reducing overhead compared to chaining eager operations.
3. Infinite streams: Lazy evaluation makes it possible to work with infinite sequences (Stream.generate(),
   Stream.iterate()) as long as a short-circuiting terminal operation is used.

Without laziness, every intermediate operation would produce an intermediate collection, wasting memory and time.
```

```java
// Demonstrating short-circuiting with lazy evaluation
List<String> names = List.of("Alice", "Bob", "Charlie", "David", "Eve");

// Only processes elements until it finds one starting with 'C'
Optional<String> first = names.stream()
    .filter(s -> {
        System.out.println("Filtering: " + s); // Observe how many times this runs
        return s.startsWith("C");
    })
    .findFirst();
// Output: "Filtering: Alice", "Filtering: Bob", "Filtering: Charlie"
// Stops after finding "Charlie" — "David" and "Eve" are never processed
```

**Q3: What is the difference between intermediate and terminal stream operations?**

```text
A3:
Intermediate operations:
- Return a new Stream (enabling chaining)
- Are lazy — they do not execute until a terminal operation is invoked
- Examples: filter(), map(), flatMap(), sorted(), distinct(), limit(), skip(), peek(), mapToInt()

Terminal operations:
- Consume the stream and produce a result (or side effect)
- Trigger the execution of the entire pipeline
- After a terminal operation, the stream is exhausted and cannot be reused
- Examples: collect(), forEach(), reduce(), count(), findFirst(), anyMatch(), allMatch(), noneMatch(), min(), max(), toArray()

Short-circuiting operations (subset of terminal or intermediate):
- findFirst(), findAny(), anyMatch(), allMatch(), noneMatch() (terminal, short-circuit)
- limit(), takeWhile() (intermediate, short-circuit)
```

```java
Stream<String> stream = list.stream()        // Source
    .filter(s -> s.length() > 3)             // Intermediate (lazy)
    .map(String::toUpperCase)                 // Intermediate (lazy)
    .sorted();                                // Intermediate (lazy — stateful!)

// Nothing has executed yet. Now trigger:
List<String> result = stream.collect(Collectors.toList()); // Terminal — executes all
```

**Q4: Why can't a stream be reused after a terminal operation?**

```text
A4: Stream design deliberately makes them single-use for several reasons:

1. Implementation efficiency: Streams are pipelines, not data stores. Once data has flowed through the
   pipeline to the terminal operation, there is nothing left to iterate.
2. Parallel safety: Streams can be parallelized. Allowing reuse would require complex state tracking to
   ensure correctness across threads.
3. Design clarity: Enforcing single-use makes the data flow explicit and prevents subtle bugs where
   developers assume a stream can be traversed multiple times.

The underlying source (the Collection, array, etc.) is unchanged — you simply create a new stream from the
source for each operation.
```

```java
Stream<String> stream = List.of("a", "b", "c").stream();
stream.forEach(System.out::println); // Works fine

// stream.forEach(System.out::println); // Throws IllegalStateException: stream has already been operated upon

// Correct: create a new stream
List<String> list = List.of("a", "b", "c");
list.stream().forEach(System.out::println);
list.stream().forEach(System.out::println); // Works — new stream each time
```

**Q5: What is the difference between `Stream.toList()` (Java 16+) and `collect(Collectors.toList())`?**

```text
A5: Both collect stream elements into a List, but with an important difference:

Stream.toList() (Java 16+):
- Returns an unmodifiable List (attempting add/remove throws UnsupportedOperationException)
- More concise syntax
- Preserves encounter order

Collectors.toList():
- Returns a mutable List (usually ArrayList)
- Guaranteed to accept null elements
- Elements can be added or removed after collection
- The exact type is not specified by the API contract (implementation may vary)

In most interview scenarios, prefer Stream.toList() for read-only results and Collectors.toList() when
you need to modify the resulting list.
```

```java
// Java 16+: immutable list
List<String> immutable = Stream.of("a", "b", "c").toList();
// immutable.add("d"); // UnsupportedOperationException

// Mutable list
List<String> mutable = Stream.of("a", "b", "c").collect(Collectors.toList());
mutable.add("d"); // OK — mutable.size() is now 4

// Explicitly unmodifiable with Collectors (Java 10+)
List<String> unmodifiableViaCollectors = Stream.of("a", "b", "c")
    .collect(Collectors.toUnmodifiableList());
```

## Code Examples

- Source: [StreamIntro.java](src/main/java/com/github/msorkhpar/claudejavatutor/streamsapi/StreamIntro.java)
- Test: [StreamIntroTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/streamsapi/StreamIntroTest.java)
