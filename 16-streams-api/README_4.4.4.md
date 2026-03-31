# 4.4.4. Terminal Operations (forEach, reduce, collect)

## Concept Explanation

Terminal operations are the final step in a stream pipeline. They **trigger the execution** of all lazy intermediate
operations and produce a result (or side effect). After a terminal operation executes, the stream is exhausted and
cannot be reused.

**Real-world analogy**: Terminal operations are like the checkout counter at a supermarket. You browse the aisles
(source), place items in your cart based on criteria (intermediate operations), but nothing is tallied until you reach
the checkout (terminal operation). The checkout produces the final result — a receipt with the total.

### Categories of Terminal Operations

**Reduction operations** — aggregate all elements into a single result:
- `reduce(identity, BinaryOperator<T>)` — combines all elements using an accumulator; returns T
- `reduce(BinaryOperator<T>)` — no identity; returns `Optional<T>` (handles empty streams)
- `count()` — number of elements
- `sum()`, `average()`, `min()`, `max()`, `summaryStatistics()` — only on primitive streams

**Collecting operations** — gather elements into a container:
- `collect(Collector<T, A, R>)` — flexible collection with Collectors utility
  - `toList()`, `toSet()`, `toMap()`, `toUnmodifiableList()`
  - `groupingBy()` — group into `Map<K, List<T>>`
  - `partitioningBy()` — split into two groups (true/false map)
  - `joining()` — concatenate strings
  - `counting()`, `summingInt()`, `averagingInt()`
  - `mapping()`, `collectingAndThen()`
- `toArray()` — collect into an array

**Search operations** — find specific elements:
- `findFirst()` — first element (Optional); respects encounter order
- `findAny()` — any element (Optional); may return different results in parallel

**Matching operations** — short-circuit boolean checks:
- `anyMatch(Predicate<T>)` — true if any element matches
- `allMatch(Predicate<T>)` — true if all elements match (or stream is empty)
- `noneMatch(Predicate<T>)` — true if no element matches (or stream is empty)

**Side-effect operations**:
- `forEach(Consumer<T>)` — process each element with a side effect; order not guaranteed in parallel
- `forEachOrdered(Consumer<T>)` — process in encounter order, even in parallel

## Key Points to Remember

1. **Terminal operations trigger execution** of all preceding lazy intermediate operations.
2. **`collect()`** is the most versatile terminal operation — use `Collectors` for many common patterns.
3. **`reduce(identity, op)`** returns `T` (works on empty streams using identity); **`reduce(op)`** returns `Optional<T>`.
4. **`allMatch()`** on empty stream returns `true` (vacuous truth); **`anyMatch()`` returns `false`; **`noneMatch()`** returns `true`.
5. **`findFirst()`** vs **`findAny()`**: `findFirst()` is deterministic (first in encounter order); `findAny()` may return any element (useful in parallel for performance).
6. **`forEach()` does not guarantee order in parallel streams**; use `forEachOrdered()` when order matters.
7. **`Collectors.groupingBy()`** groups elements into a `Map<K, List<V>>` by default; downstream collectors can further transform the values.
8. **`Collectors.partitioningBy()`** is a special case of `groupingBy()` producing `Map<Boolean, List<T>>`.
9. **`Collectors.joining()`** is the preferred way to concatenate stream elements into a String.
10. **`Stream.toList()` (Java 16+)** is more concise than `collect(Collectors.toList())` and returns an unmodifiable list.

## Relevant Java 21 Features

- **`Stream.toList()` (Java 16+)**: Shorthand for `collect(Collectors.toUnmodifiableList())`.
- **`Collectors.toUnmodifiableList/Set/Map()` (Java 10+)**: Explicitly unmodifiable collectors.
- **`Collectors.teeing()` (Java 12+)**: Applies two collectors in parallel and merges results with a merger function.
- **`mapMulti()` (Java 16)**: Flexible element expansion before collection.
- **Sequenced collection methods (Java 21)**: `getFirst()`, `getLast()` on ordered collections complement stream `findFirst()`.

## Common Pitfalls and How to Avoid Them

1. **Using `forEach` when `collect` is more appropriate**:
   ```java
   // BROKEN — using forEach to build a list (mutating shared state)
   List<String> result = new ArrayList<>();
   stream.forEach(result::add); // Works but violates functional style; unsafe in parallel
   ```
   **Fix**: Use `collect(Collectors.toList())` or `toList()`.
   ```java
   List<String> result = stream.collect(Collectors.toList());
   ```

2. **`reduce` with wrong identity value**:
   ```java
   // BROKEN — wrong identity for multiplication: 0 would always give 0
   int product = Stream.of(1, 2, 3, 4).reduce(0, (a, b) -> a * b); // Gives 0!
   ```
   **Fix**: Use the correct identity (1 for multiplication, 0 for addition, empty string for concatenation).
   ```java
   int product = Stream.of(1, 2, 3, 4).reduce(1, (a, b) -> a * b); // Gives 24
   ```

3. **Ignoring the `Optional` return of `reduce`/`findFirst`/`min`/`max`**:
   ```java
   // BROKEN — throws NoSuchElementException if stream is empty
   String first = stream.findFirst().get(); // Dangerous!
   ```
   **Fix**: Handle the Optional properly.
   ```java
   String first = stream.findFirst().orElse("default");
   // or
   Optional<String> opt = stream.findFirst();
   opt.ifPresent(System.out::println);
   ```

4. **`Collectors.toMap()` with duplicate keys**:
   ```java
   // BROKEN — throws IllegalStateException if two elements map to the same key
   Map<Integer, String> map = list.stream()
       .collect(Collectors.toMap(String::length, s -> s));
   ```
   **Fix**: Provide a merge function.
   ```java
   Map<Integer, String> map = list.stream()
       .collect(Collectors.toMap(String::length, s -> s, (s1, s2) -> s1 + "," + s2));
   ```

5. **Using `allMatch()` on empty streams without understanding vacuous truth**:
   ```java
   // This returns true — empty stream vacuously satisfies any predicate
   boolean result = Stream.empty().allMatch(s -> s.length() > 100); // true!
   ```

## Best Practices and Optimization Techniques

1. **Prefer `collect(Collectors.toList())` or `Stream.toList()`** over manual `forEach + add` patterns.
2. **Use `Collectors.groupingBy()` with downstream collectors** for powerful aggregation (e.g., counting per group, averaging per group).
3. **Use `Collectors.joining(delimiter, prefix, suffix)`** for human-readable string output.
4. **Use `Collectors.teeing()` (Java 12+)** when you need two different views of the same data in one pass.
5. **Use `IntStream.sum()`, `average()`, `summaryStatistics()`** directly on primitive streams — more efficient than `collect()`.
6. **Prefer `findFirst()` over `min(Comparator)`** when you only need the first element in encounter order.
7. **Use `anyMatch()` instead of `filter().findFirst().isPresent()`** — it short-circuits sooner and is more readable.

## Edge Cases and Their Handling

1. **`reduce` on empty stream with identity**: Returns the identity value.
2. **`reduce` on empty stream without identity**: Returns `Optional.empty()`.
3. **`collect` on empty stream**: Returns an empty collection/map/string.
4. **`allMatch` on empty stream**: Returns `true` (vacuous truth).
5. **`anyMatch` on empty stream**: Returns `false`.
6. **`noneMatch` on empty stream**: Returns `true`.
7. **`min`/`max` on empty stream**: Returns `Optional.empty()`.
8. **`count` on empty stream**: Returns 0.
9. **`groupingBy` with all elements in same group**: Produces a map with one entry.
10. **`joining` on empty stream**: Returns empty string (or prefix+suffix if specified).

## Interview-specific Insights

Interviewers frequently test:
- The `allMatch`/`anyMatch`/`noneMatch` behavior on empty streams (trick question)
- The correct identity values for common `reduce` operations
- `Collectors.groupingBy()` with downstream collectors
- `Collectors.toMap()` duplicate key handling
- `findFirst()` vs `findAny()` and when to use each
- `forEach` vs `forEachOrdered` in parallel streams
- `collect(Collectors.joining())` for string concatenation

Tricky whiteboard questions:
- "Write a stream that counts words grouped by their first character"
- "Use `reduce()` to compute the product of all numbers in a list"
- "What does `allMatch()` return for an empty stream?"

## Interview Q&A Section

**Q1: What is the difference between `reduce()` with an identity and without one?**

```text
A1: reduce() comes in two main forms:

With identity (T identity, BinaryOperator<T> accumulator):
- The identity is the starting value and the result for an empty stream.
- Always returns T (never Optional) because the identity is returned for empty streams.
- The identity must be a "neutral element": identity OP element == element for all elements.
  (e.g., 0 for addition, 1 for multiplication, "" for string concatenation)
- Example: Stream.empty().reduce(0, Integer::sum) returns 0.

Without identity (BinaryOperator<T> accumulator):
- Returns Optional<T> — empty Optional if the stream is empty.
- Safer for cases where you don't know what the identity should be, or where no identity makes sense.
- Example: Stream.empty().reduce(Integer::sum) returns Optional.empty().

The three-argument form reduce(identity, BiFunction<U,T,U>, BinaryOperator<U>) is used for type-transforming
reductions and is required for parallel correctness when the accumulator type differs from the stream element type.
```

```java
List<Integer> numbers = List.of(1, 2, 3, 4, 5);

// With identity — safe for empty streams
int sum = numbers.stream().reduce(0, Integer::sum); // 15
int emptySum = Stream.<Integer>empty().reduce(0, Integer::sum); // 0 (identity returned)

// Without identity — returns Optional
Optional<Integer> product = numbers.stream().reduce((a, b) -> a * b); // Optional[120]
Optional<Integer> emptyProduct = Stream.<Integer>empty().reduce((a, b) -> a * b); // Optional.empty()

// Max using reduce (prefer Stream.max() in practice)
Optional<Integer> max = numbers.stream().reduce(Integer::max); // Optional[5]

// Wrong identity!
int wrongProduct = numbers.stream().reduce(0, (a, b) -> a * b); // 0 — always 0!
int rightProduct = numbers.stream().reduce(1, (a, b) -> a * b); // 120
```

**Q2: How does `Collectors.groupingBy()` work, and what are downstream collectors?**

```text
A2: Collectors.groupingBy(classifier) groups stream elements into a Map<K, List<V>> where K is the key
computed by the classifier function and V is the element type.

The basic form: groupingBy(Function<T,K>) produces Map<K, List<T>>

The two-argument form: groupingBy(Function<T,K>, Collector<T,A,D>) applies a downstream collector to
the values in each group, producing Map<K, D> instead of Map<K, List<T>>.

Downstream collectors enable powerful aggregation:
- Collectors.counting() — count elements per group
- Collectors.toSet() — collect to Set per group
- Collectors.averagingInt() — average per group
- Collectors.summingInt() — sum per group
- Collectors.mapping() — transform then collect per group
- Collectors.joining() — join strings per group
- Another groupingBy — nested grouping (multi-level)
```

```java
record Employee(String name, String dept, int salary) {}

List<Employee> employees = List.of(
    new Employee("Alice", "Engineering", 90000),
    new Employee("Bob",   "Engineering", 85000),
    new Employee("Carol", "Marketing",   70000),
    new Employee("Dave",  "Marketing",   72000),
    new Employee("Eve",   "Engineering", 95000)
);

// Basic grouping: Map<String, List<Employee>>
Map<String, List<Employee>> byDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::dept));

// Count per department: Map<String, Long>
Map<String, Long> countByDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::dept, Collectors.counting()));
// {Engineering=3, Marketing=2}

// Average salary per department: Map<String, Double>
Map<String, Double> avgSalary = employees.stream()
    .collect(Collectors.groupingBy(Employee::dept, Collectors.averagingInt(Employee::salary)));

// Names per department: Map<String, List<String>>
Map<String, List<String>> namesByDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::dept,
        Collectors.mapping(Employee::name, Collectors.toList())
    ));
// {Engineering=[Alice, Bob, Eve], Marketing=[Carol, Dave]}

// Names joined per department: Map<String, String>
Map<String, String> joinedNames = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::dept,
        Collectors.mapping(Employee::name, Collectors.joining(", "))
    ));
// {Engineering="Alice, Bob, Eve", Marketing="Carol, Dave"}
```

**Q3: How does `Collectors.toMap()` work and what happens with duplicate keys?**

```text
A3: Collectors.toMap(keyMapper, valueMapper) collects stream elements into a Map where the key and value
for each entry are computed by the respective mapper functions.

Duplicate key handling:
- The basic two-argument form THROWS IllegalStateException if two elements produce the same key.
- The three-argument form: toMap(keyMapper, valueMapper, mergeFunction) uses the mergeFunction to
  resolve conflicts: (existingValue, newValue) -> resolvedValue.
- The four-argument form: toMap(keyMapper, valueMapper, mergeFunction, mapFactory) allows specifying
  the Map implementation (e.g., LinkedHashMap to preserve insertion order, TreeMap for sorted keys).

Important: toMap() does not allow null values (unlike groupingBy). Use special handling for nulls.
```

```java
List<String> words = List.of("apple", "banana", "cherry", "avocado", "blueberry");

// Basic: word -> length (no duplicates in this example)
Map<String, Integer> wordLengths = words.stream()
    .collect(Collectors.toMap(w -> w, String::length));
// {apple=5, banana=6, cherry=6, avocado=7, blueberry=9}

// ERROR: duplicate keys (cherry and banana both have length 6)
// Map<Integer, String> byLength = words.stream()
//     .collect(Collectors.toMap(String::length, w -> w)); // IllegalStateException!

// Fix: merge function — keep the first value
Map<Integer, String> byLengthFirst = words.stream()
    .collect(Collectors.toMap(
        String::length,
        w -> w,
        (existing, newVal) -> existing // keep first
    ));

// Fix: merge function — concatenate duplicates
Map<Integer, String> byLengthConcat = words.stream()
    .collect(Collectors.toMap(
        String::length,
        w -> w,
        (a, b) -> a + ", " + b
    ));
// {5=apple, 6=banana, cherry, 7=avocado, 9=blueberry}

// LinkedHashMap to preserve insertion order
Map<String, Integer> ordered = words.stream()
    .collect(Collectors.toMap(w -> w, String::length, (a, b) -> a, LinkedHashMap::new));
```

**Q4: When should you use `findFirst()` vs `findAny()`?**

```text
A4: Both findFirst() and findAny() return Optional<T> from the stream, but differ in their guarantees:

findFirst():
- Returns the first element in encounter order.
- Deterministic: always returns the same element for the same input.
- In parallel streams, it still respects encounter order, but may be slower because threads must
  coordinate to determine which element is "first".
- Use when the result must be the first element (e.g., results are ordered by priority).

findAny():
- Returns any element — no guarantee which one.
- In sequential streams, typically returns the first element (as an optimization), but this is not guaranteed.
- In parallel streams, may return any element that a thread finishes processing first — faster than
  findFirst() because no inter-thread coordination is needed.
- Use when you just need any matching element and order doesn't matter (e.g., existence check).

In practice: prefer findFirst() for deterministic results; use findAny() in parallel streams for performance.
```

```java
List<Integer> numbers = List.of(5, 3, 8, 1, 9, 2, 7, 4, 6);

// Sequential: findFirst and findAny typically return the same result
Optional<Integer> first = numbers.stream().filter(n -> n > 5).findFirst(); // Optional[8]
Optional<Integer> any = numbers.stream().filter(n -> n > 5).findAny();    // Optional[8] (sequential)

// Parallel: findAny is faster but nondeterministic
Optional<Integer> parallelAny = numbers.parallelStream()
    .filter(n -> n > 5)
    .findAny(); // Could be 8, 9, 7, or 6 — whichever thread finishes first

Optional<Integer> parallelFirst = numbers.parallelStream()
    .filter(n -> n > 5)
    .findFirst(); // Always 8 — first in encounter order, but slower in parallel

// Existence check: prefer anyMatch() over findAny().isPresent()
boolean hasLarge = numbers.stream().anyMatch(n -> n > 5); // true — more readable
```

**Q5: What does `Collectors.joining()` do and what are its variants?**

```text
A5: Collectors.joining() concatenates stream elements (which must be CharSequence, typically String) into
a single String. It has three forms:

1. joining() — simple concatenation, no delimiter: "abc"
2. joining(delimiter) — elements separated by delimiter: "a,b,c"
3. joining(delimiter, prefix, suffix) — with prefix and suffix: "[a,b,c]"

Internally, it uses a StringBuilder for efficient string concatenation — far better than using reduce()
with string concatenation, which creates O(n^2) intermediate strings.

joining() is the idiomatic, efficient way to concatenate stream elements into a string. It replaces
patterns like String.join() when you also need to filter/map elements.
```

```java
List<String> names = List.of("Alice", "Bob", "Charlie", "David");

// Simple concatenation
String simple = names.stream().collect(Collectors.joining()); // "AliceBobCharlieDAVID"

// With delimiter
String csv = names.stream().collect(Collectors.joining(", ")); // "Alice, Bob, Charlie, David"

// With delimiter, prefix, suffix
String formatted = names.stream()
    .collect(Collectors.joining(", ", "[", "]")); // "[Alice, Bob, Charlie, David]"

// Common pattern: filter, transform, then join
String emailList = employees.stream()
    .filter(Employee::isActive)
    .map(Employee::email)
    .collect(Collectors.joining("; ")); // "alice@co.com; bob@co.com"

// Joining with prefix/suffix for SQL IN clause
String inClause = ids.stream()
    .map(String::valueOf)
    .collect(Collectors.joining(", ", "(", ")")); // "(1, 2, 3, 4)"
```

**Q6: What is `Collectors.teeing()` (Java 12+)?**

```text
A6: Collectors.teeing(downstream1, downstream2, merger) applies two collectors simultaneously to the same
stream elements, then combines the two results using a BiFunction merger. It is a "tee" in the functional
sense — like a T-pipe that splits the flow.

Use cases:
1. Computing two different aggregations in a single stream pass (avoids iterating twice).
2. Computing both min and max in one pass.
3. Separating summary statistics from detail data.
4. Any scenario where you need two independent reductions from the same input.

Without teeing, you would need to iterate the collection twice or collect to a list first.
```

```java
import java.util.stream.Collectors;

record Stats(long count, double average) {}

List<Integer> numbers = List.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3);

// Compute count and sum in one pass, then derive average
Stats stats = numbers.stream().collect(
    Collectors.teeing(
        Collectors.counting(),                             // downstream1: count
        Collectors.summingDouble(Integer::doubleValue),   // downstream2: sum
        (count, sum) -> new Stats(count, sum / count)    // merger
    )
);
System.out.println(stats); // Stats[count=10, average=3.9]

// Min and max in one pass
record MinMax(int min, int max) {}
MinMax minMax = numbers.stream().collect(
    Collectors.teeing(
        Collectors.minBy(Comparator.naturalOrder()),
        Collectors.maxBy(Comparator.naturalOrder()),
        (min, max) -> new MinMax(min.orElseThrow(), max.orElseThrow())
    )
);
System.out.println(minMax); // MinMax[min=1, max=9]
```

## Code Examples

- Source: [TerminalOperations.java](src/main/java/com/github/msorkhpar/claudejavatutor/streamsapi/TerminalOperations.java)
- Test: [TerminalOperationsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/streamsapi/TerminalOperationsTest.java)
