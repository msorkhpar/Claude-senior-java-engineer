# 4.4.2. Creating and Using Streams

## Concept Explanation

Streams can be created from many different sources in Java. Understanding the various creation methods is essential
because each source has different characteristics regarding ordering, nullability, and finiteness.

**Real-world analogy**: Creating a stream is like opening a water tap to a pipe. The source (collection, array, file,
generator) is the reservoir. The tap controls how water (data) flows into the pipe (stream pipeline). Different taps
(creation methods) connect to different reservoirs and may have different flow characteristics.

### Stream Creation Sources

1. **Collections**: The most common source — any `Collection` has a `stream()` and `parallelStream()` method.
2. **Arrays**: `Arrays.stream(array)` or `Stream.of(elements...)` — both support primitive arrays with specialized streams.
3. **Stream factory methods**: `Stream.of()`, `Stream.empty()`, `Stream.ofNullable()`, `Stream.generate()`, `Stream.iterate()`, `Stream.builder()`.
4. **Primitive streams**: `IntStream.range()`, `IntStream.rangeClosed()`, `LongStream.range()` — efficient iteration without boxing.
5. **I/O operations**: `Files.lines()`, `BufferedReader.lines()` — line-by-line file processing.
6. **String operations**: `Pattern.splitAsStream()`, `String.chars()`, `String.codePoints()`.
7. **Other streams**: `Stream.concat()` to merge two streams.

### Key Creation Methods

```java
// From Collection
List<String> list = List.of("a", "b", "c");
Stream<String> fromList = list.stream();

// From array
String[] arr = {"a", "b", "c"};
Stream<String> fromArray = Arrays.stream(arr);

// Stream.of (varargs)
Stream<String> ofStream = Stream.of("a", "b", "c");

// Empty stream (useful as a neutral element in stream composition)
Stream<String> empty = Stream.empty();

// Nullable (Java 9) — returns empty stream if null, otherwise singleton stream
Stream<String> nullable = Stream.ofNullable(possiblyNull);

// Infinite streams
Stream<Double> randoms = Stream.generate(Math::random);      // infinite
Stream<Integer> sequence = Stream.iterate(0, n -> n + 1);   // infinite: 0, 1, 2, ...
Stream<Integer> bounded = Stream.iterate(0, n -> n < 10, n -> n + 1); // Java 9: 0..9

// Primitive ranges (no boxing)
IntStream range = IntStream.range(0, 10);        // 0 to 9 (exclusive end)
IntStream rangeClosed = IntStream.rangeClosed(1, 10); // 1 to 10 (inclusive end)

// Files
Stream<String> lines = Files.lines(Path.of("file.txt")); // AutoCloseable!
```

## Key Points to Remember

1. **`Collection.stream()`** is the most common creation method; returns a sequential stream.
2. **`Arrays.stream()`** is the preferred way to stream arrays — it supports primitive arrays directly without boxing.
3. **`Stream.of()`** is for ad-hoc streams from varargs; note that `Stream.of((T) null)` creates a stream with one null element, not an empty stream.
4. **`Stream.ofNullable()`** (Java 9) creates an empty stream if the argument is null, or a singleton stream otherwise.
5. **`Stream.generate()`** creates an infinite stream from a `Supplier<T>` — always use with `limit()` or short-circuit terminal.
6. **`Stream.iterate()`** with three arguments (Java 9) creates a bounded sequence similar to a for-loop.
7. **Primitive streams** (`IntStream`, `LongStream`, `DoubleStream`) have `range()` and `rangeClosed()` factory methods and additional operations like `sum()`, `average()`, `summaryStatistics()`.
8. **`Files.lines()`** returns an `AutoCloseable` stream — always use in try-with-resources.
9. **`Stream.builder()`** allows dynamically adding elements before building the stream.
10. **`Stream.concat()`** concatenates two streams lazily; both streams are consumed by the resulting stream.

## Relevant Java 21 Features

- **`Stream.ofNullable()`** (Java 9): Simplifies null-safe stream creation patterns.
- **`Stream.iterate(seed, hasNext, next)`** (Java 9): Three-argument form that replaces common `iterate().takeWhile()` patterns.
- **`Stream.toList()`** (Java 16): Convenient terminal operation producing unmodifiable list.
- **`takeWhile()` / `dropWhile()`** (Java 9): New intermediate operations for conditional slicing.
- **Sequenced collections** (Java 21): `SequencedCollection.stream()` guarantees encounter order matching insertion order.

## Common Pitfalls and How to Avoid Them

1. **Streaming a null array/collection without null check**:
   ```java
   // BROKEN — NullPointerException
   String[] arr = null;
   Arrays.stream(arr).forEach(System.out::println);
   ```
   **Fix**: Guard against null before streaming.
   ```java
   Optional.ofNullable(arr)
       .map(Arrays::stream)
       .ifPresent(s -> s.forEach(System.out::println));
   ```

2. **Forgetting to close resource-backed streams**:
   ```java
   // BROKEN — resource leak!
   Stream<String> lines = Files.lines(path);
   lines.forEach(System.out::println);
   // File handle never closed!
   ```
   **Fix**: Use try-with-resources.
   ```java
   try (Stream<String> lines = Files.lines(path)) {
       lines.forEach(System.out::println);
   }
   ```

3. **Confusing `Stream.of(null)` with `Stream.ofNullable(null)`**:
   ```java
   // BROKEN — creates a stream with ONE null element, not empty
   Stream.of((String) null).count(); // Returns 1, not 0
   ```
   **Fix**: Use `Stream.ofNullable()` for null-safe singleton streams.
   ```java
   Stream.ofNullable(null).count(); // Returns 0
   ```

4. **Infinite stream without limit**:
   ```java
   // BROKEN — runs forever
   Stream.generate(Math::random).forEach(System.out::println);
   ```
   **Fix**: Always bound infinite streams.
   ```java
   Stream.generate(Math::random).limit(10).forEach(System.out::println);
   ```

5. **Reusing a Stream.Builder after build()**:
   ```java
   // BROKEN — builder is in terminal state after build()
   Stream.Builder<String> builder = Stream.builder();
   builder.add("a");
   Stream<String> s = builder.build();
   builder.add("b"); // IllegalStateException!
   ```
   **Fix**: Add all elements before calling `build()`.

## Best Practices and Optimization Techniques

1. **Prefer `Collection.stream()`** over `Stream.of()` with collection elements for better clarity.
2. **Use primitive streams** for numeric ranges — `IntStream.range(0, n)` is far more efficient than `Stream.iterate(0, n -> n < limit, n -> n + 1)` boxing integers.
3. **Always close I/O streams** with try-with-resources.
4. **Use `Stream.concat()` sparingly** — for many streams, collect into a list and stream that, or use `flatMap`.
5. **Prefer `Stream.ofNullable()`** (Java 9) for null-safe streaming instead of ternary with `Stream.empty()`.
6. **Builder pattern** (`Stream.builder()`) is useful when elements are generated conditionally in a loop — add all elements first, then call `build()`.

## Edge Cases and Their Handling

1. **Empty source**: `Collections.emptyList().stream()` and `Stream.empty()` both produce empty streams; all operations handle this gracefully.
2. **Single-element streams**: Work correctly with all operations; `Optional` terminal operations return the single element.
3. **`IntStream.range(n, n)`**: Returns an empty stream (not an error).
4. **`Arrays.stream(arr, from, to)`**: Supports sub-array streaming; `from == to` returns empty stream.
5. **Concurrent modification of the source**: Modifying a collection while iterating its stream causes undefined behavior or `ConcurrentModificationException`.

## Interview-specific Insights

Interviewers often ask:
- "What are the different ways to create a stream?"
- "When would you use `Stream.generate()` vs `Stream.iterate()`?"
- "What's special about primitive streams like `IntStream`?"
- "How do you safely stream from a file?"
- "What does `Stream.ofNullable()` do and when is it useful?"

Tricky questions:
- "What is `Stream.of(null)` — is it an empty stream?" (No — it's a stream with one null element)
- "Can you create a stream from a Map?" (Yes — `map.entrySet().stream()`, `map.keySet().stream()`, `map.values().stream()`)
- "How would you create a stream of all lines in a file without loading the whole file into memory?" (`Files.lines()`)

## Interview Q&A Section

**Q1: What are the main ways to create a Stream in Java?**

```text
A1: Java provides many stream creation methods:

1. From Collections: list.stream(), set.stream(), map.entrySet().stream()
2. From arrays: Arrays.stream(arr), Arrays.stream(arr, start, end)
3. Factory methods: Stream.of(elements...), Stream.empty(), Stream.ofNullable(value)
4. Infinite streams: Stream.generate(supplier), Stream.iterate(seed, function)
5. Bounded iterate (Java 9): Stream.iterate(seed, hasNextPredicate, nextFunction)
6. Primitive ranges: IntStream.range(start, end), IntStream.rangeClosed(start, end)
7. I/O: Files.lines(path), bufferedReader.lines()
8. String operations: "hello".chars() (IntStream), Pattern.compile(",").splitAsStream(str)
9. Stream.builder(): for dynamic element addition
10. Stream.concat(stream1, stream2): merge two streams
```

```java
import java.util.*;
import java.util.stream.*;
import java.nio.file.*;

// 1. From collection
List.of("a","b","c").stream();

// 2. From array
Arrays.stream(new int[]{1, 2, 3});         // IntStream — no boxing
Arrays.stream(new String[]{"a", "b"});     // Stream<String>

// 3. Factory methods
Stream.of("x", "y", "z");
Stream.empty();
Stream.ofNullable(null);    // Empty stream
Stream.ofNullable("value"); // Stream with one element

// 4. Infinite
Stream.generate(Math::random).limit(5);
Stream.iterate(1, n -> n * 2).limit(10); // 1, 2, 4, 8, ...

// 5. Bounded iterate (Java 9)
Stream.iterate(0, n -> n < 5, n -> n + 1); // 0, 1, 2, 3, 4

// 6. Primitive ranges
IntStream.range(0, 5);        // 0, 1, 2, 3, 4
IntStream.rangeClosed(1, 5);  // 1, 2, 3, 4, 5

// 7. Files (must close!)
try (Stream<String> lines = Files.lines(Path.of("data.txt"))) {
    lines.forEach(System.out::println);
}
```

**Q2: When should you use `IntStream`, `LongStream`, or `DoubleStream` instead of `Stream<Integer>`?**

```text
A2: Use primitive streams whenever you are working with primitive numeric values. The benefits are:

1. No boxing/unboxing overhead: Stream<Integer> boxes each int into an Integer object. IntStream operates
   on raw int values, which is faster and uses less memory.
2. Specialized operations: sum(), average(), min(), max(), summaryStatistics() are only available on
   primitive streams. On Stream<Integer>, you would need to use reduce() or Collectors.summarizingInt().
3. Efficient range generation: IntStream.range() and IntStream.rangeClosed() provide efficient numeric
   ranges without creating an intermediate collection.

Conversion between stream types:
- Stream<T> to IntStream: stream.mapToInt(T::someIntMethod)
- IntStream to Stream<Integer>: intStream.boxed()
- IntStream to Stream<String>: intStream.mapToObj(Integer::toString)
```

```java
// Slow: boxing overhead
int sum1 = Stream.of(1, 2, 3, 4, 5)
    .mapToInt(Integer::intValue)  // Unboxes
    .sum();

// Fast: no boxing
int sum2 = IntStream.of(1, 2, 3, 4, 5).sum();
int sum3 = IntStream.rangeClosed(1, 5).sum(); // 15

// Specialized statistics
IntSummaryStatistics stats = IntStream.of(3, 1, 4, 1, 5, 9, 2, 6)
    .summaryStatistics();
System.out.println("Sum: " + stats.getSum());         // 31
System.out.println("Average: " + stats.getAverage()); // 3.875
System.out.println("Min: " + stats.getMin());          // 1
System.out.println("Max: " + stats.getMax());          // 9

// Conversions
IntStream intStream = Stream.of("hello", "world").mapToInt(String::length);
Stream<Integer> boxed = IntStream.range(0, 5).boxed();
Stream<String> asStrings = IntStream.range(0, 5).mapToObj(Integer::toString);
```

**Q3: How do you safely stream the contents of a file?**

```text
A3: Use Files.lines() with try-with-resources. Files.lines() returns a Stream<String> that is backed by
an I/O resource (a BufferedReader). If this stream is not closed, the file handle is leaked. Try-with-resources
ensures the stream (and thus the underlying reader) is closed when the block exits, even if an exception occurs.

Key considerations:
- The stream is lazy — file lines are read on demand, not all at once.
- The charset defaults to UTF-8; specify another if needed: Files.lines(path, StandardCharsets.ISO_8859_1).
- For very large files, this is memory-efficient (no need to load all lines into a List first).
- Files.readAllLines() is an alternative that eagerly loads all lines into a List — suitable for small files.
```

```java
import java.io.IOException;
import java.nio.file.*;
import java.util.stream.*;

// Safe file streaming with try-with-resources
Path path = Path.of("data.txt");
try (Stream<String> lines = Files.lines(path)) {
    long count = lines
        .filter(line -> !line.isBlank())
        .count();
    System.out.println("Non-blank lines: " + count);
} catch (IOException e) {
    throw new RuntimeException("Failed to read file: " + path, e);
}

// Collecting all non-empty lines
List<String> nonEmpty;
try (Stream<String> lines = Files.lines(path)) {
    nonEmpty = lines.filter(l -> !l.isBlank()).toList();
}
```

**Q4: What is `Stream.Builder` and when would you use it?**

```text
A4: Stream.Builder is a mutable builder for Stream objects. It allows you to add elements one at a time
before creating the stream. Once build() is called, the builder enters a terminal state and cannot accept
more elements.

Use cases:
1. When you don't know all elements upfront and build them conditionally in a loop.
2. When combining elements from different sources into a single stream without creating an intermediate collection.
3. When you want to create a stream of a fixed, small set of elements where the elements are computed
   programmatically (as opposed to Stream.of() which requires all elements at once).

Prefer Stream.of() or collection.stream() when possible — Stream.Builder adds complexity without benefit
for straightforward cases.
```

```java
Stream.Builder<String> builder = Stream.builder();

// Conditionally add elements
if (includeHeader) builder.add("HEADER");
for (String item : items) {
    if (item != null && !item.isBlank()) {
        builder.add(item.trim());
    }
}
if (includeFooter) builder.add("FOOTER");

Stream<String> stream = builder.build();
// builder.add("extra"); // IllegalStateException — already built!

stream.forEach(System.out::println);
```

**Q5: What is `Stream.concat()` and what are its limitations?**

```text
A5: Stream.concat(Stream<T> a, Stream<T> b) creates a lazily concatenated stream whose elements are all
elements of the first stream followed by all elements of the second stream.

Limitations:
1. Only two streams at a time: to concat many streams, you must nest concat() calls or use flatMap().
2. Ordered: the result is ordered if both input streams are ordered.
3. Parallel: the result is parallel if either input stream is parallel.
4. Both streams must share a compatible type T.
5. Deeply nested concat() calls (for many streams) can cause StackOverflowError due to deep call chains.

For concatenating many streams, prefer:
  Stream.of(stream1, stream2, stream3).flatMap(Function.identity())
or collect all elements and stream the collection.
```

```java
Stream<String> first = Stream.of("a", "b", "c");
Stream<String> second = Stream.of("d", "e", "f");
Stream<String> combined = Stream.concat(first, second);
combined.forEach(System.out::println); // a, b, c, d, e, f

// Concatenating many streams with flatMap (preferred for >2 streams)
Stream<String> merged = Stream.of(
    Stream.of("a", "b"),
    Stream.of("c", "d"),
    Stream.of("e", "f")
).flatMap(s -> s);

merged.toList(); // [a, b, c, d, e, f]
```

## Code Examples

- Source: [StreamCreation.java](src/main/java/com/github/msorkhpar/claudejavatutor/streamsapi/StreamCreation.java)
- Test: [StreamCreationTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/streamsapi/StreamCreationTest.java)
