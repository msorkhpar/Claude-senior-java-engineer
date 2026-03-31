# 6.6.1.4. Splitting and Joining Tasks for Optimal Performance

## Concept Explanation

The performance of a fork/join computation depends heavily on how tasks are **split** (decomposed into subtasks) and
**joined** (results combined). Poor splitting leads to load imbalance or excessive overhead; poor joining patterns waste
threads or cause unnecessary blocking.

This section covers the key strategies and trade-offs:

1. **Threshold selection**: When to stop splitting and compute sequentially.
2. **Balanced vs. unbalanced splits**: How the split point affects parallelism.
3. **Multi-way splits**: Splitting into more than two subtasks.
4. **Fork-compute-join ordering**: The correct pattern for maximum thread utilization.
5. **Work-stealing**: How ForkJoinPool automatically rebalances work.

**Real-world analogy**: Consider dividing a pizza. You could cut it exactly in half (balanced split), cut a tiny sliver
and a huge piece (unbalanced), or cut it into 8 equal slices (multi-way). The balanced approach gives each person roughly
equal work, while unbalanced splits leave one person with most of the pizza. The threshold is like deciding when a piece
is small enough to eat in one bite rather than cutting further.

## Key Points to Remember

1. The **threshold** determines when to stop recursing and compute sequentially.
2. A threshold that is too small creates too many tasks (scheduling overhead dominates).
3. A threshold that is too large limits parallelism (some cores sit idle).
4. **Balanced splits** (50/50) generally provide the best load distribution.
5. **Unbalanced splits** (e.g., 10/90) create deeper recursion trees and worse load balance.
6. Work-stealing mitigates some imbalance, but extreme imbalance still hurts.
7. **Multi-way splits** (e.g., 4-way, 8-way) can improve parallelism for very large inputs.
8. Always **fork before join** -- joining before forking forces sequential execution.
9. The **fork-compute-join** pattern: fork left, compute right inline, join left.
10. **invokeAll()** handles symmetric fork/join for you (best for RecursiveAction).

## Relevant Java 21 Features

- **Structured Concurrency (preview)**: `StructuredTaskScope.ShutdownOnFailure` provides automatic cancellation of
  sibling tasks when one fails -- something Fork/Join does not do natively.
- **Virtual threads**: For mixed CPU/IO workloads, consider decomposing the CPU part with Fork/Join and the IO part with
  virtual threads.
- **Stream Spliterators**: `Spliterator.trySplit()` in parallel streams uses similar threshold and splitting logic.
  Understanding Fork/Join splitting helps you write better custom Spliterators.

## Common Pitfalls and How to Avoid Them

1. **Threshold = 1 (splitting down to individual elements)**

   ```java
   // Problem: each element becomes a separate task
   if (end - start <= 1) {
       return array[start];
   }
   ```

   With N = 1,000,000 this creates ~2 million tasks. The overhead of creating, scheduling, and joining each task
   vastly exceeds the cost of a single addition.

   **Fix**: Use a threshold of at least several hundred:

   ```java
   private static final int THRESHOLD = 1000;
   if (end - start <= THRESHOLD) {
       long sum = 0;
       for (int i = start; i < end; i++) sum += array[i];
       return sum;
   }
   ```

2. **Joining before forking**

   ```java
   // Problem: sequential execution disguised as fork/join
   left.fork();
   long leftResult = left.join(); // blocks immediately!
   long rightResult = right.compute();
   ```

   **Fix**: Fork first, compute the other subtask, then join:

   ```java
   left.fork();
   long rightResult = right.compute(); // do useful work while left runs
   long leftResult = left.join();
   ```

3. **Unbalanced splits without justification**

   ```java
   // Problem: 10/90 split creates deep, unbalanced recursion
   int splitPoint = start + length / 10;
   ```

   **Fix**: Split at the midpoint unless you have data-specific reasons not to:

   ```java
   int mid = start + length / 2;
   ```

4. **Creating too many subtasks in multi-way split**

   ```java
   // Problem: creating 1000 subtasks for a 1000-element array
   for (int i = 0; i < array.length; i++) {
       tasks.add(new Task(array, i, i + 1));
   }
   ```

   **Fix**: Limit the number of ways to the parallelism level or a small multiple of it.

5. **Not considering the cost of the combine step**: For merge sort, the merge step is O(n) per level. If
   splits create too many levels, the total merge overhead can dominate.

## Best Practices and Optimization Techniques

1. **Threshold heuristic**: `N / (parallelism * 4)` with a minimum of 100-1000. This creates enough tasks for good load
   balancing without excessive overhead.

2. **Profile to find the optimal threshold**: The ideal threshold depends on:
   - The cost of processing each element
   - The cost of task creation and scheduling
   - The number of available cores

3. **Use balanced (50/50) splits** for homogeneous workloads where each element takes roughly the same time.

4. **Consider unbalanced splits** only for heterogeneous workloads where certain ranges are known to be cheaper.

5. **Multi-way splits** can be useful when:
   - The input is very large and binary splits create too many recursion levels
   - You want to match the number of subtasks to the available parallelism

6. **Avoid excessive forking**: Fork only when there is enough work to justify the overhead. A simple `if` check
   on the problem size suffices.

7. **Measure work-stealing**: `pool.getStealCount()` indicates how much rebalancing occurred. High steal counts
   suggest good utilization; zero steals suggest the work was already balanced or too small.

## Edge Cases and Their Handling

1. **Array size < threshold**: The entire computation runs sequentially (no forking). This is correct and efficient.
2. **Array size = 0**: Return immediately without creating any tasks.
3. **Threshold > array size**: Same as above -- no splitting occurs.
4. **Threshold = array size**: One task does all the work sequentially.
5. **Odd-sized arrays**: The midpoint calculation `start + length / 2` handles this naturally (one half gets the extra
   element).
6. **Multi-way split with more ways than elements**: Reduce the number of ways to the number of elements.
7. **Single-core machine**: Fork/Join still works but offers no parallelism benefit. The framework gracefully
   degrades to sequential execution.

## Interview-specific Insights

- Interviewers love asking about threshold selection -- know the heuristics and trade-offs.
- Be able to draw the recursion tree for a given array size and threshold on a whiteboard.
- Explain work-stealing with per-thread deques and LIFO/FIFO semantics.
- Know why fork-compute-join is better than fork-fork-join-join.
- Be ready to discuss how unbalanced splits affect performance and when they might be acceptable.
- Understand the relationship between Fork/Join splits and Spliterator.trySplit() in streams.

## Interview Q&A Section

**Q1: How do you choose the right threshold for a fork/join task?**

```text
A1: The threshold determines when to stop splitting and compute sequentially.
Choosing it involves balancing two costs:

1. Too small a threshold: excessive task creation overhead. Each fork()
   creates a ForkJoinTask object, pushes it to a deque, and may trigger
   work-stealing. For millions of tiny tasks, this overhead dominates.

2. Too large a threshold: insufficient parallelism. If the threshold is
   close to N, few tasks are created and most cores sit idle.

Heuristics:
- Start with N / (parallelism * 4), where parallelism is the pool size
- Minimum 100-1000 elements (depends on the cost per element)
- For cheap operations (addition): higher threshold (~10,000)
- For expensive operations (complex math, string processing): lower (~100)

Best practice: Profile with different thresholds using JMH or similar
benchmarking tools. The optimal value depends on the specific hardware,
JVM, and operation cost.

Rule of thumb for interviews: "between 1,000 and 10,000 for typical
array operations, adjusted based on profiling."
```

```java
// Threshold that adapts to available parallelism
int parallelism = ForkJoinPool.commonPool().getParallelism();
int threshold = Math.max(1000, array.length / (parallelism * 4));

// Using the threshold in a task
if (end - start <= threshold) {
    // sequential computation
    long sum = 0;
    for (int i = start; i < end; i++) sum += array[i];
    return sum;
}
```

**Q2: What is work-stealing and how does it help with load balancing?**

```text
A2: Work-stealing is the algorithm ForkJoinPool uses to balance work
across threads without explicit coordination.

How it works:
1. Each worker thread has its own deque (double-ended queue)
2. When a thread forks a subtask, it pushes it onto ITS OWN deque (LIFO)
3. When a thread needs work, it pops from its own deque (LIFO - top)
4. When a thread's deque is empty, it steals from ANOTHER thread's deque (FIFO - bottom)

Why LIFO for own work and FIFO for stealing:
- The owning thread pops recently forked tasks (smaller, at the top) -- good for
  cache locality and working on the most recently created subtask
- The stealing thread takes tasks from the bottom (older, larger) -- these are
  closer to the root of the recursion tree and represent more work, reducing
  the number of steals needed

Load balancing benefits:
- No explicit load distribution needed
- Naturally handles uneven workloads
- Idle threads automatically find work
- Minimal contention (each thread usually works on its own deque)

Limitations:
- Work-stealing has overhead (lock-free deque operations)
- Very small tasks may be stolen before the owning thread processes them
- Blocking in tasks reduces the effectiveness of stealing
```

```java
// Observing work-stealing in action
ForkJoinPool pool = new ForkJoinPool(4);
try {
    pool.invoke(new LargeTask(array));
    System.out.println("Steal count: " + pool.getStealCount());
    System.out.println("Pool size: " + pool.getPoolSize());
} finally {
    pool.shutdown();
}
```

**Q3: Why is balanced splitting generally better than unbalanced splitting?**

```text
A3: Balanced splitting (50/50) distributes work evenly across the recursion
tree, while unbalanced splitting (e.g., 10/90) creates asymmetry.

Balanced (50/50) advantages:
- Each subtask at the same level has approximately equal work
- The recursion tree has depth log2(N/T), creating O(N/T) leaf tasks
- All threads finish at roughly the same time
- Work-stealing has less to compensate for

Unbalanced (10/90) problems:
- The recursion tree is deeper on one side (log10(N/T) vs log1.11(N/T))
- More total tasks are created (deeper tree = more internal nodes)
- One thread may process a chain of large tasks while others are idle
- Work-stealing must work harder to rebalance

When unbalanced splits ARE appropriate:
- When certain data ranges are known to be cheaper to process
- When the split point corresponds to a natural boundary (e.g., sorted data)
- When the cost per element varies and you can estimate it

In general, default to balanced splitting unless you have specific knowledge
about the workload distribution.
```

```java
// Balanced split
int mid = start + (end - start) / 2;

// Unbalanced split (only when justified by data characteristics)
int splitPoint = start + (end - start) / 10;

// Both produce the same result, but balanced is generally more efficient
// for homogeneous workloads
```

**Q4: What is the correct fork/join pattern and why?**

```text
A4: The correct pattern for RecursiveTask is: fork-compute-join.

  left.fork();                        // 1. Fork left to another thread
  long rightResult = right.compute(); // 2. Compute right in THIS thread
  long leftResult = left.join();      // 3. Join left (wait for it)
  return leftResult + rightResult;    // 4. Combine results

Why this is optimal:
- Step 2 keeps the current thread busy doing useful work
- While the current thread computes right, a stolen thread processes left
- join() in step 3 may return immediately if left is already done

Anti-pattern 1: fork-fork-join-join
  left.fork();
  right.fork();
  return left.join() + right.join();  // current thread is idle!

  This wastes the current thread. It forks two tasks and then blocks
  waiting for both. A third thread is needed for one of them.

Anti-pattern 2: join-before-fork-completes
  left.fork();
  long leftResult = left.join();  // blocks immediately!
  long rightResult = right.compute();

  This forces sequential execution. The current thread waits for left
  to complete before starting right.

For RecursiveAction, invokeAll() is preferred because there is no result
to compute inline.
```

```java
// CORRECT pattern for RecursiveTask
@Override
protected Long compute() {
    if (length <= THRESHOLD) return sequentialSum();

    SumTask left = new SumTask(array, start, mid);
    SumTask right = new SumTask(array, mid, end);

    left.fork();                          // fork left
    long rightResult = right.compute();   // compute right HERE
    long leftResult = left.join();        // join left
    return leftResult + rightResult;      // combine
}

// CORRECT pattern for RecursiveAction
@Override
protected void compute() {
    if (length <= THRESHOLD) { doWork(); return; }
    invokeAll(                            // fork and join both
        new MyAction(array, start, mid),
        new MyAction(array, mid, end)
    );
}
```

**Q5: When should you use multi-way splitting (more than 2 subtasks)?**

```text
A5: Multi-way splitting creates N subtasks at once instead of the standard 2.

When to use multi-way splitting:
1. Very large inputs where binary splits create too many recursion levels
   (e.g., array of 1 billion elements with threshold 1000 = 20 levels deep)
2. When you want to match the number of leaf tasks to the available cores
3. When the split cost is high and you want to minimize the number of splits

Implementation considerations:
- Fork all tasks except the last one
- Compute the last task in the current thread
- Join all forked tasks in reverse order
- The number of ways should be 2-4x the parallelism level

Example: For a 4-core machine, splitting into 4-8 chunks at the top level
creates enough parallelism immediately without deep recursion.

Multi-way splitting is rarely needed in practice because:
- Binary splitting with work-stealing handles most cases well
- The overhead of creating more tasks at each level can offset benefits
- The recursion depth for binary splitting is only log2(N/T), which is
  manageable even for very large inputs
```

```java
// Multi-way split into N subtasks
@Override
protected Long compute() {
    int length = end - start;
    if (length <= THRESHOLD || ways <= 1) {
        return sequentialSum();
    }

    int chunkSize = length / ways;
    List<SumTask> tasks = new ArrayList<>();
    for (int i = 0; i < ways; i++) {
        int chunkEnd = (i == ways - 1) ? end : start + (i + 1) * chunkSize;
        tasks.add(new SumTask(array, start + i * chunkSize, chunkEnd));
    }

    // Fork all but the last
    for (int i = 0; i < tasks.size() - 1; i++) {
        tasks.get(i).fork();
    }
    // Compute the last in current thread
    long total = tasks.get(tasks.size() - 1).compute();
    // Join all forked tasks
    for (int i = tasks.size() - 2; i >= 0; i--) {
        total += tasks.get(i).join();
    }
    return total;
}
```

**Q6: How do you verify that your fork/join implementation benefits from parallelism?**

```text
A6: Verification requires both correctness testing and performance measurement:

Correctness:
1. Compare parallel results with sequential results for the same input
2. Test with various array sizes (below, at, and above threshold)
3. Test with edge cases (null, empty, single element, all identical)
4. Run multiple times to detect race conditions

Performance:
1. Use JMH (Java Microbenchmark Harness) for reliable measurements
2. Compare sequential vs. parallel execution times
3. Vary the array size to find the crossover point (where parallel wins)
4. Test with different parallelism levels
5. Monitor pool statistics (steal count, active threads)

Key metrics:
- Speedup = sequential time / parallel time
- Ideal speedup = number of cores
- Actual speedup is always less due to overhead (Amdahl's law)
- If speedup < 1, the overhead exceeds the benefit -- increase threshold

Common gotchas:
- JIT warmup affects measurements (use JMH with warmup iterations)
- Small arrays may show parallel being SLOWER than sequential
- GC pauses can skew results
```

```java
// Simple performance comparison (use JMH for production benchmarks)
long[] array = LongStream.rangeClosed(1, 10_000_000).toArray();

// Sequential
long startTime = System.nanoTime();
long seqSum = Arrays.stream(array).sum();
long seqTime = System.nanoTime() - startTime;

// Parallel with Fork/Join
startTime = System.nanoTime();
long parSum = ForkJoinPool.commonPool().invoke(
    new ArraySumTask(array, 0, array.length));
long parTime = System.nanoTime() - startTime;

System.out.printf("Sequential: %d ns, Parallel: %d ns%n", seqTime, parTime);
System.out.printf("Speedup: %.2fx%n", (double) seqTime / parTime);
assert seqSum == parSum; // correctness check
```

## Code Examples

- Source: [SplittingAndJoining.java](src/main/java/com/github/msorkhpar/claudejavatutor/forkjoin/SplittingAndJoining.java)
- Test: [SplittingAndJoiningTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/forkjoin/SplittingAndJoiningTest.java)
