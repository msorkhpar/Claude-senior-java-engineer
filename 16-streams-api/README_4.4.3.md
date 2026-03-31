# 4.4.3. Intermediate Operations (filter, map, flatMap)

## Concept Explanation

Intermediate operations transform a stream into another stream. They are **lazy** — they do not execute until a
terminal operation is invoked. This enables the JVM to optimize the pipeline (e.g., fusion of operations, short-circuit
evaluation).

**Real-world analogy**: Intermediate operations are like workstations on an assembly line. Each workstation takes the
current item, applies its transformation or check, and passes the result to the next workstation. No final product is
produced until the last workstation (the terminal operation) completes its work.

### Categories of Intermediate Operations

**Filtering operations** — reduce the number of elements:
- `filter(Predicate<T>)` — keep only elements matching the predicate
- `distinct()` — remove duplicates (uses `equals()`/`hashCode()`)
- `limit(long n)` — keep at most n elements (short-circuit, useful with infinite streams)
- `skip(long n)` — skip the first n elements
- `takeWhile(Predicate<T>)` (Java 9) — take elements while predicate is true, then stop
- `dropWhile(Predicate<T>)` (Java 9) — drop elements while predicate is true, then pass the rest

**Mapping operations** — transform elements one-to-one or one-to-many:
- `map(Function<T, R>)` — transform each element T to R (1:1)
- `flatMap(Function<T, Stream<R>>)` — transform each element to a stream, then flatten (1:many)
- `mapToInt/mapToLong/mapToDouble(ToIntFunction<T>)` — convert to primitive stream
- `flatMapToInt/flatMapToLong/flatMapToDouble` — flatten to primitive stream

**Sorting/peeking operations** — reorder or observe elements:
- `sorted()` — natural order (elements must be `Comparable`)
- `sorted(Comparator<T>)` — custom order
- `peek(Consumer<T>)` — observe each element without modifying (mainly for debugging)

### filter vs map vs flatMap at a Glance

```
filter:  [1, 2, 3, 4, 5] --filter(x > 2)--> [3, 4, 5]
map:     ["a", "bb", "ccc"] --map(length)--> [1, 2, 3]
flatMap: [["a","b"], ["c"]] --flatMap--> ["a", "b", "c"]
```

## Key Points to Remember

1. **`filter(Predicate<T>)`** keeps elements for which the predicate returns `true`; it is a 1:0-or-1 mapping.
2. **`map(Function<T, R>)`** transforms every element; the number of elements stays the same (1:1 mapping).
3. **`flatMap(Function<T, Stream<R>>)`** transforms each element into a stream, then flattens all those streams into one (1:many).
4. **`distinct()`** uses `equals()` and `hashCode()`; for custom objects, ensure these are properly overridden.
5. **`sorted()`** is a stateful operation — it must see all elements before producing output, so it breaks short-circuit optimizations downstream.
6. **`peek(Consumer<T>)`** is intended for debugging (e.g., printing elements mid-pipeline). Avoid relying on side effects.
7. **`limit(n)`** is a short-circuit intermediate operation — the pipeline upstream does not need to produce more elements than `n`.
8. **`takeWhile()` / `dropWhile()`** (Java 9) work best on ordered streams; behavior on unordered streams is nondeterministic.
9. **`mapToInt/mapToLong/mapToDouble`** convert to primitive streams — use to avoid boxing when working with numeric values.
10. Order of `filter`, `map`, and `sorted` matters for both correctness and performance — apply cheap filters before expensive maps.

## Relevant Java 21 Features

- **`takeWhile()` / `dropWhile()`** (Java 9): Allow prefix-based stream slicing.
- **`mapMulti()`** (Java 16): A flexible alternative to `flatMap` that uses a `BiConsumer` for element expansion.
- **Pattern matching in lambdas** (Java 16+, 21): Use in `filter()` and `map()` lambdas with modern `instanceof` patterns.
- **`Stream<T>.mapToInt()`** has always been present; Java 21 continues to encourage using primitive streams to avoid auto-boxing overhead.

## Common Pitfalls and How to Avoid Them

1. **Using `filter` after an expensive `map` when filter could come first**:
   ```java
   // SLOW — maps every element, then filters
   list.stream()
       .map(this::expensiveTransform)
       .filter(s -> s.startsWith("A"))
       .collect(Collectors.toList());
   ```
   **Fix**: Filter first to reduce the work for the expensive operation.
   ```java
   // FAST — filters first, then maps only matching elements
   list.stream()
       .filter(s -> s.startsWith("A"))
       .map(this::expensiveTransform)
       .collect(Collectors.toList());
   ```

2. **Using `map` when `flatMap` is needed (nested collections)**:
   ```java
   // BROKEN — produces Stream<List<String>>, not Stream<String>
   List<List<String>> nested = List.of(List.of("a","b"), List.of("c","d"));
   Stream<List<String>> wrong = nested.stream().map(l -> l);
   ```
   **Fix**: Use `flatMap` to flatten.
   ```java
   Stream<String> flat = nested.stream().flatMap(Collection::stream);
   ```

3. **`distinct()` on objects without proper `equals()`/`hashCode()`**:
   ```java
   // BROKEN — all elements kept because Object.equals() uses reference equality
   class Point { int x, y; } // no equals/hashCode
   List.of(new Point(1,2), new Point(1,2)).stream().distinct().count(); // 2, not 1
   ```
   **Fix**: Override `equals()` and `hashCode()`, or use records.

4. **Expecting `sorted()` to not affect performance in large parallel streams**:
   Sorted is stateful and requires collecting all elements before sorting — this is expensive in parallel.
   **Fix**: Sort after collecting, or sort in sequential mode before parallelizing.

5. **Misusing `peek()` for important side effects**:
   ```java
   // RISKY — peek may not execute with short-circuit terminal operations
   long count = list.stream()
       .peek(s -> database.save(s)) // Will NOT save if short-circuit fires early
       .filter(s -> s.length() > 3)
       .findFirst()
       .stream().count();
   ```
   **Fix**: Use `forEach()` or collect first, then process.

## Best Practices and Optimization Techniques

1. **Order operations for maximum efficiency**: `filter` → `map` → `sorted` → `limit`.
2. **Use `mapToInt/mapToLong/mapToDouble`** when converting to numeric values to avoid boxing overhead.
3. **Prefer `flatMap(Collection::stream)`** for flattening nested collections — clear and idiomatic.
4. **Use `mapMulti()`** (Java 16) for complex one-to-many transformations where `flatMap` would require creating many small intermediate streams.
5. **Keep lambdas in intermediate operations stateless** — do not read from or write to shared mutable state.
6. **Consider `distinct()` cost**: it maintains a `HashSet` internally, so it uses O(n) memory and O(1) amortized time per element.
7. **`sorted()` requires O(n log n)** time and O(n) space — avoid unnecessary sorting.

## Edge Cases and Their Handling

1. **`filter` on empty stream**: Returns empty stream; no exception.
2. **`map` returning null**: Allowed, but downstream operations on null elements may throw `NullPointerException`. Use `filter(Objects::nonNull)` or handle nulls explicitly.
3. **`flatMap` with a function returning null**: Throws `NullPointerException`. The function must return a non-null stream (use `Stream.empty()` for empty results).
4. **`distinct()` on empty stream**: Returns empty stream.
5. **`sorted()` with `null` elements and natural ordering**: Throws `NullPointerException`. Sort with a null-safe comparator: `Comparator.nullsFirst(Comparator.naturalOrder())`.
6. **`limit(0)` / `skip(MAX_VALUE)`**: Returns empty stream; works correctly.
7. **`takeWhile` / `dropWhile` on unordered streams**: Behavior is nondeterministic — may take/drop different elements on each run.

## Interview-specific Insights

Interviewers commonly focus on:
- The difference between `map` and `flatMap` (the most common interview question)
- Performance ordering of operations
- The fact that `sorted` is stateful and may break parallelism benefits
- `distinct()` requiring proper `equals()`/`hashCode()`
- When to use `mapToInt` vs `map`
- `takeWhile`/`dropWhile` behavior on unordered streams

Tricky questions:
- "If you call `limit(5)` and `sorted()`, does `sorted()` process all elements or just 5?" (All elements before `limit`, or just 5 depending on order)
- "What is the difference between `filter(x -> !list.contains(x))` and `distinct()`?"
- "Can `flatMap` produce an empty stream for some elements?" (Yes — return `Stream.empty()`)

## Interview Q&A Section

**Q1: What is the difference between `map` and `flatMap`?**

```text
A1: Both map and flatMap transform stream elements, but with a key difference in structure:

map(Function<T, R>): applies a function to each element and produces exactly one output element per
input element. The output is a Stream<R>. The result stream has the same number of elements as the input.

flatMap(Function<T, Stream<R>>): applies a function to each element that returns a Stream<R>, then
"flattens" all those streams into a single Stream<R>. The result can have more or fewer elements than
the input (including zero elements for any input element).

Mental model:
- map: one element in → one element out
- flatMap: one element in → zero or more elements out (via a sub-stream)

Common use case: flattening a list of lists, splitting strings into words, expanding an optional value.
```

```java
List<String> sentences = List.of("Hello World", "Java Streams", "FlatMap Example");

// map: each sentence becomes a String[] (produces Stream<String[]>)
Stream<String[]> withMap = sentences.stream()
    .map(s -> s.split(" "));

// flatMap: each sentence is split, arrays are flattened into individual words
List<String> words = sentences.stream()
    .flatMap(s -> Arrays.stream(s.split(" ")))
    .toList();
// ["Hello", "World", "Java", "Streams", "FlatMap", "Example"]

// Another example: flattening nested lists
List<List<Integer>> nested = List.of(
    List.of(1, 2, 3),
    List.of(4, 5),
    List.of(6, 7, 8, 9)
);
List<Integer> flat = nested.stream()
    .flatMap(Collection::stream)
    .toList();
// [1, 2, 3, 4, 5, 6, 7, 8, 9]

// flatMap with Optional (Java 9+: Optional.stream())
List<Optional<String>> optionals = List.of(
    Optional.of("a"), Optional.empty(), Optional.of("c")
);
List<String> present = optionals.stream()
    .flatMap(Optional::stream) // only non-empty optionals
    .toList();
// ["a", "c"]
```

**Q2: How does `filter` interact with laziness and short-circuit operations?**

```text
A2: filter() is a lazy intermediate operation. It does not examine any elements until a terminal operation
triggers the pipeline. This enables important optimizations:

1. Short-circuiting: If the terminal operation is findFirst() or anyMatch(), the pipeline stops as soon as
   the first element passes the filter, without examining remaining elements.

2. Lazy composition: When filter() and map() are combined, the JVM fuses them into a single pass —
   the element is filtered, then mapped (or discarded), without creating an intermediate stream.

3. Order matters: Placing filter() before expensive operations (like map() with expensive transformations)
   reduces the total work because elements are eliminated early.
```

```java
List<String> names = List.of("Alice", "Bob", "Anna", "Charlie", "Amanda");

// Short-circuit: stops after finding first name starting with 'C'
Optional<String> first = names.stream()
    .peek(s -> System.out.println("Checking: " + s)) // observe traversal
    .filter(s -> s.startsWith("C"))
    .findFirst();
// Prints: Checking: Alice, Checking: Bob, Checking: Anna, Checking: Charlie
// Stops after "Charlie" — "Amanda" is never checked

// Performance: filter before expensive operation
List<User> activeAdults = users.stream()
    .filter(User::isActive)           // cheap: boolean field check
    .filter(u -> u.age() >= 18)       // cheap: int comparison
    .map(u -> enrichFromDatabase(u))   // expensive: DB call — only for filtered elements
    .toList();
```

**Q3: What is `distinct()` and what are its performance characteristics?**

```text
A3: distinct() is a stateful intermediate operation that removes duplicate elements from a stream,
where duplicates are defined by Object.equals() and Object.hashCode().

Performance characteristics:
- Time complexity: O(1) amortized per element (backed by a HashSet internally)
- Space complexity: O(k) where k is the number of distinct elements seen so far
- For ordered streams, distinct() preserves encounter order (first occurrence is kept)
- For unordered streams (e.g., after calling unordered() or from a HashSet), it may be more efficient
  since it doesn't need to track ordering

Requirements on objects:
- equals() and hashCode() must be consistently implemented (contract from Object)
- For custom classes, override both equals() and hashCode(), or use records (which do this automatically)

Parallel consideration: distinct() in parallel streams requires coordination across threads to track
seen elements, which can negate parallelism benefits for small datasets.
```

```java
// Basic usage
List<Integer> numbers = List.of(1, 2, 3, 2, 1, 4, 5, 4);
List<Integer> distinct = numbers.stream()
    .distinct()
    .toList(); // [1, 2, 3, 4, 5] — order preserved

// With custom objects — requires equals/hashCode
record Point(int x, int y) {} // Record auto-generates equals/hashCode
List<Point> points = List.of(new Point(1,2), new Point(3,4), new Point(1,2));
List<Point> distinctPoints = points.stream().distinct().toList();
// [Point(1,2), Point(3,4)] — duplicates removed correctly

// Without proper equals/hashCode — distinct() won't work as expected
class BadPoint { int x, y; BadPoint(int x, int y){ this.x=x; this.y=y; } }
// Uses reference equality by default — distinct() keeps all instances
```

**Q4: Explain `takeWhile()` and `dropWhile()` added in Java 9.**

```text
A4: takeWhile(Predicate<T>) and dropWhile(Predicate<T>) were added in Java 9 for prefix-based stream slicing.

takeWhile(predicate): Processes elements in order. As soon as one element does NOT match the predicate,
processing stops and that element (and all subsequent elements) are excluded — even if later elements
would match the predicate.

dropWhile(predicate): Skips elements as long as the predicate matches. Once the predicate fails, it
passes that element and all remaining elements through — even if later elements would match the predicate.

Both are particularly useful with sorted/ordered streams. On unordered streams, behavior is nondeterministic.

Difference from filter():
- filter() checks every element independently
- takeWhile()/dropWhile() operate on a prefix/suffix pattern
```

```java
// takeWhile: stop at first element that doesn't match
List<Integer> numbers = List.of(1, 2, 3, 4, 5, 1, 2, 3);
List<Integer> taken = numbers.stream()
    .takeWhile(n -> n < 4)
    .toList(); // [1, 2, 3] — stops at 4; the second 1,2,3 are never reached

// dropWhile: skip until first element that doesn't match
List<Integer> dropped = numbers.stream()
    .dropWhile(n -> n < 4)
    .toList(); // [4, 5, 1, 2, 3] — drops first three, passes rest

// Comparison with filter (filter is independent per element):
List<Integer> filtered = numbers.stream()
    .filter(n -> n < 4)
    .toList(); // [1, 2, 3, 1, 2, 3] — includes ALL elements < 4

// Useful for sorted data: take the first N elements meeting a condition
List<Integer> sorted = List.of(1, 2, 3, 5, 8, 13, 21);
List<Integer> smallFibs = sorted.stream()
    .takeWhile(n -> n < 10)
    .toList(); // [1, 2, 3, 5, 8]
```

**Q5: What is `mapMulti()` and when would you use it over `flatMap()`?**

```text
A5: mapMulti() (introduced in Java 16) is an alternative to flatMap() for one-to-many element expansion.
Instead of returning a Stream<R>, the function accepts a BiConsumer<T, Consumer<R>> — the second argument
is a "downstream" consumer that you call once for each output element you want to emit.

When to prefer mapMulti() over flatMap():
1. When expanding a small or fixed number of elements: avoids creating a Stream object for each input.
2. When the expansion logic is imperative (if/else, loops) — simpler to express with a consumer than
   with stream creation.
3. When performance is critical with many short streams: flatMap has overhead per sub-stream created.

When to prefer flatMap():
1. When you already have a stream or collection to flatten: flatMap(Collection::stream) is idiomatic.
2. For readability: flatMap is well-known; mapMulti is less familiar to most developers.
```

```java
// flatMap version: create a stream for each element
List<Integer> doubled = List.of(1, 2, 3).stream()
    .<Integer>flatMap(n -> Stream.of(n, n * 2))
    .toList(); // [1, 2, 2, 4, 3, 6]

// mapMulti version: emit elements via consumer — no intermediate Stream creation
List<Integer> doubledMulti = List.of(1, 2, 3).stream()
    .<Integer>mapMulti((n, downstream) -> {
        downstream.accept(n);
        downstream.accept(n * 2);
    })
    .toList(); // [1, 2, 2, 4, 3, 6] — same result, potentially faster

// Useful for conditional expansion
List<Object> mixed = List.of(1, "hello", 2, "world", 3);
List<String> strings = mixed.stream()
    .<String>mapMulti((obj, downstream) -> {
        if (obj instanceof String s) downstream.accept(s);
    })
    .toList(); // ["hello", "world"]
```

## Code Examples

- Source: [IntermediateOperations.java](src/main/java/com/github/msorkhpar/claudejavatutor/streamsapi/IntermediateOperations.java)
- Test: [IntermediateOperationsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/streamsapi/IntermediateOperationsTest.java)
