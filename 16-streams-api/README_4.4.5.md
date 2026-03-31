# 4.4.5. Parallel Streams and Performance Considerations

## Concept Explanation

Parallel streams allow stream pipelines to execute operations concurrently using multiple threads, potentially speeding
up processing for large datasets. Under the hood, parallel streams use the **Fork/Join framework** with the common
`ForkJoinPool` to split work into subtasks and merge results.

**Real-world analogy**: Think of parallel streams like assembling a large puzzle with a team. A single person assembling
the puzzle (sequential stream) works on one piece at a time. With a team (parallel stream), the puzzle is divided into
sections, each person assembles their section simultaneously, and then the sections are combined. For small puzzles,
the coordination overhead of splitting and combining might take longer than just doing it alone. For massive puzzles,
the team wins every time.

### How Parallel Streams Work

1. **Splitting**: The source is split into chunks using a `Spliterator` (splittable iterator).
2. **Processing**: Each chunk is processed independently in a `ForkJoinPool` thread.
3. **Combining**: Results are merged in reverse split order using the operation's combiner.

```
Source [1..1000]
    └── Fork → [1..500] and [501..1000]
         ├── Fork → [1..250] ... → [1..125] → process
         └── Fork → [501..750] ... → [751..1000] → process
              └── Join → combine partial results
                     └── Final result
```

### Creating Parallel Streams

```java
// From collection
List<Integer> list = List.of(1, 2, 3, 4, 5);
list.parallelStream();           // Direct parallel stream

// From sequential stream
list.stream().parallel();        // Convert to parallel

// From parallel to sequential
list.parallelStream().sequential(); // Convert back to sequential

// Check if parallel
boolean isParallel = list.parallelStream().isParallel(); // true
```

### The Common ForkJoinPool

By default, parallel streams use `ForkJoinPool.commonPool()`, which has a parallelism level of
`Runtime.getRuntime().availableProcessors() - 1` (number of CPU cores minus one, leaving one for the main thread).

To control the pool or isolate parallel operations from other parallel work, submit to a custom `ForkJoinPool`:

```java
ForkJoinPool customPool = new ForkJoinPool(4); // 4 worker threads
List<Integer> result = customPool.submit(() ->
    list.parallelStream().filter(n -> n % 2 == 0).toList()
).get();
customPool.shutdown();
```

## Key Points to Remember

1. **Parallel streams use `ForkJoinPool.commonPool()`** by default — shared across the JVM.
2. **Performance benefit is NOT guaranteed** — parallel streams have overhead (splitting, coordination, combining) that exceeds the benefit for small datasets or simple operations.
3. **Thread safety**: operations in parallel stream lambdas must be thread-safe. Shared mutable state leads to race conditions.
4. **Ordering**: parallel streams may process and produce results in a different order than sequential streams; use `forEachOrdered()` or `sorted()` explicitly when order matters.
5. **`collect()` with thread-safe collectors** (like `Collectors.toList()`, `groupingBy()`) is safe in parallel; building an `ArrayList` manually in `forEach` is NOT.
6. **Stateful intermediate operations** (like `distinct()`, `sorted()`, `limit()`) are expensive in parallel because they require coordination.
7. **The performance break-even** is typically around 10,000+ elements for CPU-bound operations; I/O-bound operations may benefit less due to bottlenecking.
8. **Reduction operations** (`reduce()`, `collect()`) require an associative and non-interfering combiner for correct parallel results.
9. **`findAny()`** is faster than `findFirst()` in parallel because it doesn't enforce encounter order.
10. **`Arrays.parallelSort()`** is an alternative for sorting large arrays using the Fork/Join framework.

## Relevant Java 21 Features

- **Virtual threads (Java 21, JEP 444)**: Not directly related to parallel streams, but relevant for I/O-bound concurrency. For CPU-bound work, parallel streams with platform threads remain appropriate.
- **Structured concurrency (Java 21, JEP 453 preview)**: A higher-level alternative to parallel streams for managing concurrent subtasks.
- **`Spliterator` improvements**: Ongoing JVM improvements to `Spliterator` splitting strategies improve parallel stream performance.
- **Common ForkJoinPool sizing**: As of Java 21, the common pool size can be tuned with `java.util.concurrent.ForkJoinPool.common.parallelism` system property.

## Common Pitfalls and How to Avoid Them

1. **Using parallel streams for small collections**:
   ```java
   // SLOW — parallel overhead dominates for small datasets
   List<Integer> small = List.of(1, 2, 3, 4, 5);
   int sum = small.parallelStream().mapToInt(Integer::intValue).sum();
   ```
   **Fix**: Use sequential stream for small datasets. Benchmark before switching to parallel.

2. **Shared mutable state in parallel stream operations** (race condition):
   ```java
   // BROKEN — race condition: multiple threads write to unsynchronized list
   List<String> results = new ArrayList<>();
   list.parallelStream().forEach(s -> results.add(s.toUpperCase())); // Data corruption!
   ```
   **Fix**: Use `collect()` with a thread-safe collector.
   ```java
   List<String> results = list.parallelStream()
       .map(String::toUpperCase)
       .collect(Collectors.toList()); // Thread-safe
   ```

3. **Assuming order is preserved in parallel**:
   ```java
   // WRONG ASSUMPTION — order not guaranteed
   list.parallelStream().forEach(System.out::println); // May print out of order
   ```
   **Fix**: Use `forEachOrdered()` when order matters.
   ```java
   list.parallelStream().forEachOrdered(System.out::println); // Ordered but slower
   ```

4. **Using stateful lambdas in parallel streams**:
   ```java
   // BROKEN — list.contains() depends on shared mutable 'seen' list
   Set<String> seen = new HashSet<>();
   list.parallelStream()
       .filter(s -> seen.add(s))  // HashSet is not thread-safe!
       .collect(Collectors.toList());
   ```
   **Fix**: Use `distinct()` or a `ConcurrentHashMap.newKeySet()`.
   ```java
   Set<String> seen = ConcurrentHashMap.newKeySet();
   list.parallelStream().filter(seen::add).collect(Collectors.toList());
   // Or simply:
   list.parallelStream().distinct().collect(Collectors.toList());
   ```

5. **Blocking operations in parallel streams starving the common pool**:
   ```java
   // RISKY — blocking I/O in parallel stream starves the common ForkJoinPool
   list.parallelStream()
       .map(id -> fetchFromDatabase(id)) // Blocking I/O!
       .collect(Collectors.toList());
   ```
   **Fix**: Use a custom `ForkJoinPool`, virtual threads, or `CompletableFuture` for I/O-bound work.

## Best Practices and Optimization Techniques

1. **Benchmark before parallelizing** — measure with realistic data sizes. Use `PerformanceTestUtil` or JMH.
2. **Choose the right workload**: parallel streams shine for CPU-bound, compute-intensive operations on large datasets (>10k elements).
3. **Avoid I/O in parallel streams** — blocking in ForkJoinPool threads starves other tasks. Use virtual threads or async I/O for I/O-bound work.
4. **Use thread-safe collectors** — `Collectors.toList()`, `toSet()`, `groupingBy()` are all safe in parallel.
5. **Minimize synchronization** — design operations to be stateless and side-effect-free.
6. **Prefer `reduce()` over mutable `collect()` in parallel** when the operation supports associativity.
7. **Avoid `sorted()`, `distinct()`, `limit()` early in parallel pipelines** — these are stateful and force synchronization.
8. **Use `unordered()`** to hint that encounter order doesn't matter, enabling optimizations for `distinct()` and `limit()` in parallel.
9. **Custom ForkJoinPool** for isolating parallel work from the application's common pool (avoids starvation).

## Edge Cases and Their Handling

1. **Single-element stream in parallel**: Works correctly; parallelism has no effect.
2. **Empty stream in parallel**: All operations return empty results; no error.
3. **Parallel stream with `limit()`**: Correct but can be slow — `limit()` forces encounter order tracking.
4. **`reduce()` in parallel with non-associative operation**: Produces incorrect results.
   ```java
   // WRONG — subtraction is not associative: (1-2)-3 != 1-(2-3)
   int wrong = Stream.of(1,2,3,4,5).parallel().reduce(0, (a,b) -> a - b); // nondeterministic!
   // Use only associative operations: +, *, max, min, string concat
   ```
5. **`forEach` order in parallel**: Nondeterministic — some elements may be processed by any thread in any order.
6. **`forEachOrdered` in parallel**: Correct order but slower than sequential `forEach` — coordination overhead.

## Interview-specific Insights

Interviewers frequently ask:
- "When would you use parallel streams and when would you avoid them?"
- "What threading model do parallel streams use?"
- "Why is using `ArrayList` in a parallel stream `forEach` problematic?"
- "How do you control the thread pool size for a parallel stream?"
- "What is `forEachOrdered()` and when is it needed?"
- "Why does `reduce()` with subtraction produce wrong results in parallel?"

Tricky questions:
- "If a parallel stream with `limit(1)` is applied to a large list, how many elements are processed?"
- "What happens when you run two parallel stream operations simultaneously from different threads?" (They share the common pool)
- "Is `collect(Collectors.toList())` safe in a parallel stream?" (Yes — the collector is thread-safe)

## Interview Q&A Section

**Q1: When should you use parallel streams, and when should you avoid them?**

```text
A1: Parallel streams are beneficial when:
1. The dataset is LARGE (typically 10,000+ elements for simple operations; more for complex ones).
2. Operations are CPU-BOUND and computationally expensive (not I/O-bound).
3. Operations are STATELESS and INDEPENDENT (no shared mutable state between elements).
4. The operation is easily decomposable and results are easily combinable (associative).
5. The source supports efficient splitting (ArrayList, arrays split well; LinkedList, Iterator do not).

Avoid parallel streams when:
1. The dataset is SMALL — splitting and coordination overhead exceeds the benefit.
2. Operations are I/O-BOUND (database calls, network requests, file I/O) — threads block, starving the pool.
3. Operations have SIDE EFFECTS on shared mutable state — race conditions.
4. You need GUARANTEED ORDER — parallel doesn't preserve encounter order without forEachOrdered.
5. The source does not split efficiently (LinkedList, streams without size information).
6. You need PREDICTABLE, REPRODUCIBLE results for testing (parallel order is nondeterministic).

Rule of thumb: measure first, parallelize second.
```

```java
import com.github.msorkhpar.claudejavatutor.base.PerformanceTestUtil;

// Benchmark to determine if parallel is faster
List<Integer> largeList = IntStream.rangeClosed(1, 1_000_000)
    .boxed().collect(Collectors.toList());

var sequential = PerformanceTestUtil.measureExecution(() ->
    largeList.stream().mapToLong(n -> n * n).sum()
);

var parallel = PerformanceTestUtil.measureExecution(() ->
    largeList.parallelStream().mapToLong(n -> n * n).sum()
);

System.out.printf("Sequential: %dms%n", sequential.executionTime() / 1_000_000);
System.out.printf("Parallel:   %dms%n", parallel.executionTime() / 1_000_000);
// Parallel is typically faster for 1M elements on multi-core machines
```

**Q2: What is the Fork/Join framework and how do parallel streams use it?**

```text
A2: The Fork/Join framework (java.util.concurrent.ForkJoinPool) is a work-stealing thread pool designed for
recursive, divide-and-conquer tasks. It was introduced in Java 7.

How it works:
1. A ForkJoinPool maintains multiple worker threads.
2. Tasks are recursively split (forked) into smaller subtasks until they are small enough to execute directly.
3. Each thread has its own deque (double-ended queue) of tasks. When a thread finishes its tasks, it
   "steals" tasks from the tail of another thread's deque — this is "work stealing".
4. Subtask results are joined back together bottom-up.

Parallel streams and Fork/Join:
- Parallel streams use ForkJoinPool.commonPool() by default.
- The source's Spliterator is used to recursively split the data into chunks.
- Each chunk is processed as a ForkJoinTask.
- Results are combined in reverse split order (for ordered streams) or in any order (for unordered).
- Common pool parallelism = Runtime.getRuntime().availableProcessors() - 1.
```

```java
// Default: uses common ForkJoinPool
int sum = IntStream.rangeClosed(1, 1_000_000)
    .parallel()
    .sum(); // Uses ForkJoinPool.commonPool()

// Custom pool for isolation
ForkJoinPool customPool = new ForkJoinPool(4);
try {
    long result = customPool.submit(() ->
        IntStream.rangeClosed(1, 1_000_000)
            .parallel()
            .filter(n -> n % 2 == 0)
            .asLongStream()
            .sum()
    ).get();
    System.out.println("Sum of evens: " + result);
} catch (Exception e) {
    Thread.currentThread().interrupt();
} finally {
    customPool.shutdown();
}

// Check parallelism level
System.out.println("Common pool parallelism: " +
    ForkJoinPool.commonPool().getParallelism()); // CPUs - 1
```

**Q3: Why must `reduce()` operations use associative functions in parallel streams?**

```text
A3: In parallel streams, reduce() splits the stream into chunks, reduces each chunk independently, then
combines the partial results. For the final result to be correct regardless of how the data is split, the
combining function must be ASSOCIATIVE:

    f(f(a, b), c) == f(a, f(b, c))  for all a, b, c

Associative operations: +, *, max, min, string concatenation, set union, logical AND/OR
Non-associative operations: subtraction, division, average (needs both sum and count)

The identity value must also satisfy: f(identity, x) == x for all x

In parallel, with subtraction (1-2-3-4-5 = -13):
  Thread 1 might compute: 1-2 = -1
  Thread 2 might compute: 3-4-5 = -6
  Combining: -1 - (-6) = 5  [WRONG! Should be -13]

The result varies based on how the data was split — fundamentally incorrect.
```

```java
List<Integer> numbers = List.of(1, 2, 3, 4, 5);

// CORRECT: addition is associative
int sum = numbers.parallelStream().reduce(0, Integer::sum); // Always 15

// WRONG: subtraction is NOT associative
// Sequential result: ((((0-1)-2)-3)-4)-5 = -15
int seqResult = numbers.stream().reduce(0, (a, b) -> a - b);
// Parallel result: varies (data split differently)
int parResult = numbers.parallelStream().reduce(0, (a, b) -> a - b);
// parResult may differ from seqResult!

// CORRECT: for non-associative operations, use sequential
int correctDiff = numbers.stream()      // NOT parallelStream()
    .reduce(0, (a, b) -> a - b);       // Sequential: deterministic

// CORRECT: compute average with associative operations
double avg = numbers.stream()
    .mapToInt(Integer::intValue)
    .average()
    .orElse(0.0); // Uses specialized reduction internally — correct in parallel too
```

**Q4: How does encounter order affect parallel stream performance and correctness?**

```text
A4: Encounter order is the order in which elements appear in the stream source (e.g., insertion order for
a List, arbitrary order for a HashSet).

Ordered streams (from List, arrays, sorted sets): parallel operations that must respect order (like
forEachOrdered, limit, findFirst, distinct) require inter-thread coordination, reducing parallelism benefits.

Unordered streams (from HashSet, or after calling unordered()): operations can process elements in any
order, enabling better parallelization since no coordination is needed.

Performance implications:
- distinct() on ordered parallel stream: O(n) synchronized lookups
- distinct() on unordered parallel stream: can be more efficient
- limit(n) on ordered parallel: must track which n elements come first across threads
- limit(n) on unordered: any n elements, no coordination needed

Use stream.unordered() to give the stream a performance hint when order doesn't matter.
```

```java
List<Integer> list = IntStream.rangeClosed(1, 1_000_000)
    .boxed().collect(Collectors.toList());

// Ordered parallel — forEachOrdered waits for correct order
list.parallelStream()
    .filter(n -> n % 2 == 0)
    .limit(5)
    .forEachOrdered(System.out::println); // Always: 2, 4, 6, 8, 10

// Unordered parallel — faster; any 5 even numbers
list.parallelStream()
    .unordered()
    .filter(n -> n % 2 == 0)
    .limit(5)
    .forEach(System.out::println); // May print 5 arbitrary even numbers

// HashSet source: naturally unordered, better parallel performance for distinct()
Set<String> set = new HashSet<>(List.of("a","b","c","a","b","c","d"));
long distinct = set.parallelStream().distinct().count(); // Faster than from ordered source
```

**Q5: How do you measure whether parallel streams actually improve performance?**

```text
A5: Measuring parallel stream performance requires careful benchmarking. Key guidelines:

1. Use a proper benchmarking framework: JMH (Java Microbenchmark Harness) is the industry standard for
   JVM benchmarks. It handles JVM warmup, JIT compilation, and statistical noise.

2. Use PerformanceTestUtil for quick comparisons (as used in this module).

3. Test with realistic data sizes: test with the actual expected dataset size, not artificially small samples.

4. Test on the target hardware: multi-core benefits depend on available CPUs.

5. Warm up the JVM before measuring: the first few runs are slower due to JIT compilation.

6. Consider the full pipeline, not just the parallel operation: if the bottleneck is I/O, parallelism
   on computation won't help.

7. Profile for bottlenecks: thread contention, GC pressure, or memory bandwidth can negate parallel gains.

Common results:
- Lists < 10,000 elements: sequential is usually faster
- Simple operations (sum, count): often fast enough sequentially
- Complex, CPU-intensive transformations on large data: parallel wins
```

```java
import com.github.msorkhpar.claudejavatutor.base.PerformanceTestUtil;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.*;

public class ParallelBenchmark {

    public static void benchmarkStreamProcessing(int dataSize) {
        List<Integer> data = IntStream.rangeClosed(1, dataSize).boxed().toList();

        // Sequential benchmark
        var seqResult = PerformanceTestUtil.measureExecution(() ->
            data.stream()
                .filter(n -> n % 2 == 0)
                .mapToLong(n -> (long) n * n)
                .sum()
        );

        // Parallel benchmark
        var parResult = PerformanceTestUtil.measureExecution(() ->
            data.parallelStream()
                .filter(n -> n % 2 == 0)
                .mapToLong(n -> (long) n * n)
                .sum()
        );

        System.out.printf("Data size: %,d%n", dataSize);
        System.out.printf("Sequential: %,d ns (result: %d)%n",
            seqResult.executionTime(), seqResult.result());
        System.out.printf("Parallel:   %,d ns (result: %d)%n",
            parResult.executionTime(), parResult.result());
        System.out.printf("Speedup: %.2fx%n",
            (double) seqResult.executionTime() / parResult.executionTime());
    }
}
```

## Code Examples

- Source: [ParallelStreams.java](src/main/java/com/github/msorkhpar/claudejavatutor/streamsapi/ParallelStreams.java)
- Test: [ParallelStreamsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/streamsapi/ParallelStreamsTest.java)
