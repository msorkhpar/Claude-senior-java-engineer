# 6.6.1. ForkJoinPool and RecursiveTask/RecursiveAction

## Concept Explanation

The Fork/Join Framework, introduced in Java 7 (`java.util.concurrent`), is a specialized framework for parallelizing
divide-and-conquer algorithms. It is built around two core ideas:

1. **Fork**: Split a large task into smaller, independent subtasks that can be processed in parallel.
2. **Join**: Wait for the subtasks to complete and combine their results.

The framework centers on three key abstractions:

- **`ForkJoinPool`** -- a specialized `ExecutorService` that manages a pool of worker threads, each with its own
  work-stealing deque. Idle threads steal tasks from busy threads' deques, providing excellent load balancing.
- **`RecursiveTask<V>`** -- a `ForkJoinTask` subclass for tasks that return a result.
- **`RecursiveAction`** -- a `ForkJoinTask` subclass for tasks that perform side effects without returning a result.

**Real-world analogy**: Imagine a manager who breaks a massive spreadsheet into sections and distributes them to team
members. Each member may further split their section if it is still too large. When everyone finishes, results are
collected back up the chain. If one person finishes early, they "steal" work from a colleague's pile -- that is
work-stealing.

The Fork/Join Framework underpins Java's parallel streams (`parallelStream()`) and is the default executor for
`CompletableFuture.supplyAsync()` when no explicit executor is provided.

## Key Points to Remember

1. `ForkJoinPool` uses **work-stealing** -- idle threads take tasks from busy threads' queues.
2. The **common pool** (`ForkJoinPool.commonPool()`) is shared JVM-wide and used by parallel streams.
3. `RecursiveTask<V>` returns a value; `RecursiveAction` does not.
4. Both extend `ForkJoinTask<V>`, which implements `Future<V>`.
5. The canonical pattern is: **fork one subtask, compute the other, then join**.
6. A **threshold** determines when to stop splitting and compute sequentially.
7. Choosing the right threshold is critical -- too small creates excessive overhead, too large limits parallelism.
8. `invokeAll()` forks and joins multiple tasks at once and is preferred for `RecursiveAction`.
9. Never block inside fork/join tasks on external I/O or locks -- this defeats work-stealing.
10. The framework is optimized for **CPU-bound** divide-and-conquer problems.

## Relevant Java 21 Features

- **Virtual threads (JEP 444)**: Virtual threads are better suited for I/O-bound tasks, while Fork/Join remains the
  tool of choice for CPU-bound parallel decomposition. They are complementary.
- **Structured Concurrency (JEP 462, preview)**: Provides a higher-level API for forking concurrent tasks and joining
  them, but targets a different use case (task lifecycle management vs. data parallelism).
- **`ForkJoinPool` improvements**: Java 19+ added `ForkJoinPool.ManagedBlocker` enhancements and better diagnostics.
  The common pool's parallelism defaults to `Runtime.getRuntime().availableProcessors() - 1`.
- **Parallel streams**: Internally use the common `ForkJoinPool`, making the framework relevant even if you never use it
  directly.

## Common Pitfalls and How to Avoid Them

1. **Forking both subtasks instead of computing one inline**

   ```java
   // Anti-pattern: wastes the current thread
   left.fork();
   right.fork();
   return left.join() + right.join();
   ```

   **Fix**: Fork one, compute the other in the current thread:

   ```java
   left.fork();
   long rightResult = right.compute();
   long leftResult = left.join();
   return leftResult + rightResult;
   ```

2. **Threshold too small**: Creating millions of tiny tasks causes scheduling overhead to dominate.
   **Fix**: Use a threshold around `N / (parallelism * 4)` or at least a few hundred elements.

3. **Blocking I/O inside tasks**: ForkJoinPool threads that block on I/O reduce parallelism.
   **Fix**: Use `ManagedBlocker` or move I/O-bound work to a separate thread pool.

4. **Shared mutable state**: Concurrent modification of shared structures causes data races.
   **Fix**: Use thread-local accumulators, atomic variables, or merge results at join time.

5. **Joining before forking**: Calling `join()` before `fork()` forces sequential execution.
   **Fix**: Always fork before joining.

## Best Practices and Optimization Techniques

1. Follow the **fork-compute-join** pattern: fork the left subtask, compute the right in the current thread, join left.
2. Choose a threshold that balances task-creation overhead against parallelism -- profile to find the sweet spot.
3. Use the **common pool** for most cases; create a custom pool only when you need isolation or different parallelism.
4. Prefer `invokeAll()` for `RecursiveAction` tasks with symmetric subtask structure.
5. Make tasks **stateless** or use **thread-safe accumulators** (e.g., `AtomicLong`).
6. Use `ForkJoinPool` for CPU-bound decomposition; use virtual threads for I/O-bound concurrency.
7. Profile with JMH or `PerformanceTestUtil` to verify that parallelism actually improves throughput.

## Edge Cases and Their Handling

1. **Null arrays**: Return a sensible default (0 for sum, empty list for collections) instead of throwing.
2. **Empty arrays**: Handle as a base case before submitting to the pool.
3. **Single-element arrays**: The threshold check naturally handles this -- compute sequentially.
4. **Array size smaller than threshold**: Entire computation runs sequentially, which is correct.
5. **Very large arrays**: Work-stealing ensures balanced processing even with imbalanced splits.
6. **Negative parallelism**: Validate and throw `IllegalArgumentException`.

## Interview-specific Insights

Interviewers frequently ask about:

- The difference between `RecursiveTask` and `RecursiveAction`
- How work-stealing improves load balancing
- Why you should compute one subtask inline instead of forking both
- The relationship between Fork/Join and parallel streams
- Choosing an appropriate threshold
- When to use Fork/Join vs. `ExecutorService` vs. virtual threads
- Potential pitfalls (blocking, shared state, threshold selection)

## Interview Q&A Section

**Q1: What is the Fork/Join Framework and when should you use it?**

```text
A1: The Fork/Join Framework is a parallel computing framework in java.util.concurrent
designed for divide-and-conquer algorithms. It consists of ForkJoinPool (the executor),
RecursiveTask (for tasks that return a result), and RecursiveAction (for void tasks).

Use it when:
- The problem can be recursively decomposed into independent subproblems
- The work is CPU-bound (not I/O-bound)
- The dataset is large enough to benefit from parallel processing

Do NOT use it for:
- I/O-bound tasks (use virtual threads or a cached thread pool)
- Tasks that require heavy synchronization between subtasks
- Small datasets where overhead exceeds parallelism benefits
```

```java
// Basic Fork/Join usage
ForkJoinPool pool = ForkJoinPool.commonPool();
long result = pool.invoke(new SumTask(array, 0, array.length));
```

**Q2: How does work-stealing work in ForkJoinPool?**

```text
A2: Each worker thread in a ForkJoinPool has its own double-ended queue (deque).
When a task forks subtasks, they are pushed onto the current thread's deque.

Work-stealing happens when a worker thread's deque is empty:
1. The idle thread looks at other workers' deques
2. It steals tasks from the TAIL of another thread's deque (LIFO for owner, FIFO for stealer)
3. Stolen tasks tend to be larger (closer to the root of the recursion tree),
   giving the stealer more work to do

This LIFO/FIFO design is optimal because:
- The owning thread processes smaller, recently forked tasks (good for cache locality)
- The stealing thread gets larger tasks (more work, fewer steals needed)

This automatically load-balances uneven workloads without explicit coordination.
```

```java
// The common pool uses work-stealing internally
ForkJoinPool pool = ForkJoinPool.commonPool();
System.out.println("Parallelism: " + pool.getParallelism());
System.out.println("Steal count: " + pool.getStealCount());
```

**Q3: What is the difference between RecursiveTask and RecursiveAction?**

```text
A3: Both extend ForkJoinTask and are used with ForkJoinPool:

RecursiveTask<V>:
- Returns a result of type V
- Override compute() which returns V
- Use for operations like sum, max, merge sort, search
- Results are combined at join points

RecursiveAction:
- Returns no result (void)
- Override compute() which returns void
- Use for in-place mutations (array fill, increment, transform)
- Side effects happen directly (e.g., modifying array elements)

Key guideline: If you need the computation's result, use RecursiveTask.
If you are performing an in-place transformation, use RecursiveAction.
```

```java
// RecursiveTask example -- returns a result
class SumTask extends RecursiveTask<Long> {
    protected Long compute() {
        if (size <= THRESHOLD) return sequentialSum();
        SumTask left = new SumTask(array, start, mid);
        SumTask right = new SumTask(array, mid, end);
        left.fork();
        long rightResult = right.compute();
        return left.join() + rightResult;
    }
}

// RecursiveAction example -- modifies in-place
class IncrementAction extends RecursiveAction {
    protected void compute() {
        if (size <= THRESHOLD) { incrementSequentially(); return; }
        IncrementAction left = new IncrementAction(array, start, mid);
        IncrementAction right = new IncrementAction(array, mid, end);
        invokeAll(left, right);
    }
}
```

**Q4: Why should you compute one subtask inline instead of forking both?**

```text
A4: Forking both subtasks wastes the current thread:

Anti-pattern:
  left.fork();   // push left to deque
  right.fork();  // push right to deque
  return left.join() + right.join();  // current thread is idle!

The current thread forks two tasks and then blocks waiting. It does no useful work itself.
Another thread must steal one of the tasks, effectively wasting one thread.

Correct pattern:
  left.fork();                        // push left to deque
  long rightResult = right.compute(); // compute right in THIS thread
  long leftResult = left.join();      // join left
  return leftResult + rightResult;

The current thread computes the right subtask directly, staying productive while a
stolen thread processes the left subtask. This maximizes CPU utilization.

Additionally, always join the LAST-forked task first (or call compute on the right
after forking the left) to minimize task queue depth.
```

```java
// Correct pattern
left.fork();
long rightResult = right.compute();  // current thread stays busy
long leftResult = left.join();
return leftResult + rightResult;
```

**Q5: How does ForkJoinPool relate to parallel streams?**

```text
A5: Java's parallel streams (stream.parallel() or collection.parallelStream())
use the common ForkJoinPool internally. When you write:

  list.parallelStream().mapToInt(x -> x * x).sum();

The stream framework submits Spliterator-based tasks to ForkJoinPool.commonPool().

Key implications:
1. All parallel streams in the JVM share the same common pool by default
2. A long-running parallel stream operation can starve other parallel operations
3. You can run parallel streams in a custom ForkJoinPool by submitting
   the stream operation as a task to that pool

Caution: The common pool's parallelism defaults to availableProcessors() - 1.
Saturating it with blocking operations affects all parallel streams in the JVM.
```

```java
// Running a parallel stream in a custom ForkJoinPool
ForkJoinPool customPool = new ForkJoinPool(4);
long sum = customPool.submit(() ->
    IntStream.range(0, 10_000)
        .parallel()
        .mapToLong(i -> array[i])
        .sum()
).join();
customPool.shutdown();
```

## Code Examples

- Source: [ForkJoinPoolBasics.java](src/main/java/com/github/msorkhpar/claudejavatutor/forkjoin/ForkJoinPoolBasics.java)
- Source: [RecursiveTaskExamples.java](src/main/java/com/github/msorkhpar/claudejavatutor/forkjoin/RecursiveTaskExamples.java)
- Source: [RecursiveActionExamples.java](src/main/java/com/github/msorkhpar/claudejavatutor/forkjoin/RecursiveActionExamples.java)
- Source: [SplittingAndJoining.java](src/main/java/com/github/msorkhpar/claudejavatutor/forkjoin/SplittingAndJoining.java)
- Test: [ForkJoinPoolBasicsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/forkjoin/ForkJoinPoolBasicsTest.java)
- Test: [RecursiveTaskExamplesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/forkjoin/RecursiveTaskExamplesTest.java)
- Test: [RecursiveActionExamplesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/forkjoin/RecursiveActionExamplesTest.java)
- Test: [SplittingAndJoiningTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/forkjoin/SplittingAndJoiningTest.java)
