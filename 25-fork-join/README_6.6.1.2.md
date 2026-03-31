# 6.6.1.2. Implementing RecursiveTask for Results-Bearing Tasks

## Concept Explanation

`RecursiveTask<V>` is an abstract class in `java.util.concurrent` that extends `ForkJoinTask<V>`. It is designed for
fork/join computations that **produce a result**. You subclass `RecursiveTask<V>`, override the `compute()` method, and
implement the recursive decomposition logic.

The `compute()` method follows a standard template:

1. **Base case**: If the problem is small enough (below a threshold), solve it sequentially and return the result.
2. **Recursive case**: Split the problem, fork one subtask, compute the other inline, join the forked subtask, and
   combine the results.

**Real-world analogy**: Imagine you need to count all the books in a large library. You can divide the library into
sections, assign each section to a helper, and then sum up everyone's counts. If a section is small enough, you count it
yourself. `RecursiveTask` works the same way -- it recursively divides the problem and combines partial results.

### Class Hierarchy

```
ForkJoinTask<V>       (abstract, implements Future<V>)
  |
  +-- RecursiveTask<V>    (abstract, for result-bearing tasks)
  +-- RecursiveAction     (abstract, for void tasks)
  +-- CountedCompleter<V> (for tasks with completion triggers)
```

## Key Points to Remember

1. Override `compute()` to define the fork/join logic.
2. `compute()` must return a value of type `V`.
3. Use `fork()` to asynchronously execute a subtask in another thread.
4. Use `join()` to wait for and retrieve the result of a forked subtask.
5. Use `compute()` (not `fork()`) for one subtask to keep the current thread busy.
6. The threshold determines when to switch from parallel to sequential computation.
7. `RecursiveTask` is **not serializable** -- it is designed for in-process parallelism.
8. Results from subtasks are combined at each level of the recursion tree.
9. Exceptions thrown in `compute()` are propagated to the caller via `join()` or `invoke()`.

## Relevant Java 21 Features

- `RecursiveTask` itself has not changed significantly since Java 7, but its ecosystem has evolved:
  - Java 21's **pattern matching** can simplify result handling from fork/join tasks.
  - **Virtual threads** complement `RecursiveTask` -- use Fork/Join for CPU-bound decomposition, virtual threads for
    I/O-bound concurrency.
  - **Structured Concurrency** (`StructuredTaskScope`) offers an alternative for simple fork/join patterns where you
    don't need recursive decomposition.

## Common Pitfalls and How to Avoid Them

1. **Returning mutable shared state from compute()**

   ```java
   // Problem: shared mutable list
   private List<Integer> sharedResults = new ArrayList<>();
   protected Long compute() {
       sharedResults.add(partialResult); // race condition!
       return sum;
   }
   ```

   **Fix**: Each task should compute and return its own result; combine at join:

   ```java
   protected Long compute() {
       if (small) return sequentialSum();
       left.fork();
       long rightResult = right.compute();
       return left.join() + rightResult; // combine immutably
   }
   ```

2. **Not handling exceptions from join()**

   ```java
   // Problem: exception swallowed
   left.fork();
   long right = right.compute();
   long leftResult = left.join(); // may throw wrapped exception
   ```

   **Fix**: Let exceptions propagate or catch `CompletionException`:

   ```java
   try {
       long leftResult = left.join();
   } catch (CompletionException e) {
       // handle the wrapped exception
   }
   ```

3. **Using RecursiveTask for I/O-bound operations**: Fork/Join is for CPU work. I/O in `compute()` blocks worker
   threads.

4. **Forgetting the base case**: Without a proper threshold, recursion never stops.

   ```java
   // Problem: infinite recursion
   protected Long compute() {
       // missing threshold check!
       left.fork();
       return right.compute() + left.join();
   }
   ```

5. **Threshold too small for Fibonacci-style tasks**: Naive recursive Fibonacci creates an exponential number of tasks.
   Use a sequential cutoff of at least 20.

## Best Practices and Optimization Techniques

1. **Choose a sensible threshold**: A common heuristic is `N / (parallelism * 4)` with a minimum of 100-1000 elements.
2. **Avoid object allocation in the hot path**: Reuse arrays with index ranges instead of copying subarrays.
3. **Use the fork-compute-join pattern**: Fork left, compute right, join left.
4. **Keep compute() pure**: Return results rather than mutating shared state.
5. **Profile sequential vs. parallel**: Not all problems benefit from parallelism. Small arrays or simple operations
   may be faster sequentially.
6. **Consider using `invokeAll()`** for symmetric splits where you don't need to process one inline.

## Edge Cases and Their Handling

1. **Null input**: Validate in the constructor; throw `IllegalArgumentException` early.
2. **Empty input**: Return a sensible default (0 for sum, `Integer.MIN_VALUE` for max, empty array for sort).
3. **Single element**: The base case handles this naturally.
4. **All identical elements**: The algorithm should work correctly (no assumptions about element uniqueness).
5. **Integer overflow**: Use `long` for sums of large `int` arrays.
6. **Negative numbers**: Ensure operations like max/min handle negatives correctly (initialize with
   `Integer.MIN_VALUE`/`Integer.MAX_VALUE`).

## Interview-specific Insights

- Interviewers may ask you to implement a parallel sum, max, search, or sort using `RecursiveTask`.
- Be ready to write the fork-compute-join pattern from memory.
- Know why sequential Fibonacci is better implemented iteratively and when fork/join overhead is justified.
- Understand the recursion tree and how tasks flow through the pool.
- Be able to estimate the number of tasks created for a given input size and threshold.

## Interview Q&A Section

**Q1: Walk through implementing a RecursiveTask that sums an array.**

```text
A1: A SumTask extends RecursiveTask<Long> and takes an array with start/end indices:

1. Base case: If (end - start) <= THRESHOLD, sum elements sequentially.
2. Recursive case:
   a. Calculate midpoint: mid = start + (end - start) / 2
   b. Create left subtask: SumTask(array, start, mid)
   c. Create right subtask: SumTask(array, mid, end)
   d. Fork left subtask
   e. Compute right subtask in current thread
   f. Join left subtask
   g. Return leftResult + rightResult

The threshold prevents excessive task creation. For an array of 10,000 elements
with a threshold of 1,000, we create roughly 20 tasks (binary tree of depth ~4).
```

```java
public class SumTask extends RecursiveTask<Long> {
    private static final int THRESHOLD = 1000;
    private final int[] array;
    private final int start, end;

    public SumTask(int[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Long compute() {
        int length = end - start;
        if (length <= THRESHOLD) {
            long sum = 0;
            for (int i = start; i < end; i++) sum += array[i];
            return sum;
        }
        int mid = start + length / 2;
        SumTask left = new SumTask(array, start, mid);
        SumTask right = new SumTask(array, mid, end);
        left.fork();
        long rightResult = right.compute();
        long leftResult = left.join();
        return leftResult + rightResult;
    }
}
```

**Q2: How would you implement parallel merge sort with RecursiveTask?**

```text
A2: MergeSortTask extends RecursiveTask<int[]> and returns the sorted subarray:

1. Base case: If array.length <= THRESHOLD, use Arrays.sort() on a copy.
2. Recursive case:
   a. Split array into left and right halves
   b. Create MergeSortTask for each half
   c. Fork left, compute right, join left
   d. Merge the two sorted halves into one array

Key design decisions:
- Copy subarrays for each task (simpler but uses more memory)
- OR use index-based ranges with an auxiliary array (more efficient)
- The merge step is O(n) and must be done sequentially for each level

The merge sort is stable (preserves order of equal elements) and the fork/join
version maintains this property because each subtask operates on disjoint ranges.
```

```java
public class MergeSortTask extends RecursiveTask<int[]> {
    private static final int THRESHOLD = 64;
    private final int[] array;

    public MergeSortTask(int[] array) {
        this.array = array;
    }

    @Override
    protected int[] compute() {
        if (array.length <= THRESHOLD) {
            int[] copy = Arrays.copyOf(array, array.length);
            Arrays.sort(copy);
            return copy;
        }
        int mid = array.length / 2;
        MergeSortTask left = new MergeSortTask(Arrays.copyOfRange(array, 0, mid));
        MergeSortTask right = new MergeSortTask(Arrays.copyOfRange(array, mid, array.length));
        left.fork();
        int[] rightSorted = right.compute();
        int[] leftSorted = left.join();
        return merge(leftSorted, rightSorted);
    }

    private int[] merge(int[] a, int[] b) {
        int[] result = new int[a.length + b.length];
        int i = 0, j = 0, k = 0;
        while (i < a.length && j < b.length)
            result[k++] = (a[i] <= b[j]) ? a[i++] : b[j++];
        while (i < a.length) result[k++] = a[i++];
        while (j < b.length) result[k++] = b[j++];
        return result;
    }
}
```

**Q3: How does exception handling work in RecursiveTask?**

```text
A3: Exceptions in RecursiveTask are handled as follows:

1. If compute() throws an unchecked exception, it is captured by the ForkJoinTask.
2. The exception is re-thrown when join() or get() is called on the task:
   - join() wraps it in a CompletionException (unchecked)
   - get() wraps it in an ExecutionException (checked)
3. invoke() on the pool propagates the exception directly.

If a subtask fails, the parent task that calls join() will receive the exception.
The other subtask may still complete normally (it is not automatically cancelled).

To cancel a subtask programmatically, call task.cancel(true).
To check if a task completed with an exception, use task.isCompletedAbnormally().
```

```java
RecursiveTask<Long> faultyTask = new RecursiveTask<>() {
    @Override
    protected Long compute() {
        throw new ArithmeticException("Division by zero");
    }
};

try {
    ForkJoinPool.commonPool().invoke(faultyTask);
} catch (ArithmeticException e) {
    System.out.println("Caught: " + e.getMessage());
}

// Or with submit + join
var future = ForkJoinPool.commonPool().submit(faultyTask);
try {
    future.join();
} catch (CompletionException e) {
    System.out.println("Cause: " + e.getCause().getMessage());
}
```

**Q4: When is RecursiveTask NOT a good fit?**

```text
A4: RecursiveTask is NOT a good fit in these scenarios:

1. I/O-bound operations: Reading files, network calls, database queries.
   Fork/Join threads that block on I/O reduce parallelism. Use virtual threads.

2. Small datasets: The overhead of task creation, forking, and joining exceeds
   the benefit of parallelism. Profile to find the break-even point.

3. Tasks with heavy inter-dependencies: If subtasks need to communicate
   during execution, Fork/Join's divide-and-conquer model breaks down.

4. Non-decomposable problems: If the problem cannot be split into independent
   subproblems (e.g., iterative algorithms with loop-carried dependencies),
   Fork/Join cannot help.

5. Simple embarrassingly parallel problems: For map-style operations without
   reduction, parallel streams or a simple ExecutorService may be simpler.

6. Tasks requiring ordered execution: Fork/Join tasks execute in unpredictable
   order. Use a sequential executor if order matters.
```

```java
// BAD: I/O-bound task in ForkJoinPool
class FetchTask extends RecursiveTask<String> {
    protected String compute() {
        return httpClient.send(request, handler).body(); // blocks!
    }
}

// GOOD: Use virtual threads for I/O
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    List<Future<String>> futures = urls.stream()
        .map(url -> executor.submit(() -> httpClient.send(request, handler).body()))
        .toList();
}
```

**Q5: How many tasks are created for an array of N elements with threshold T?**

```text
A5: For a binary split (the standard approach), the number of tasks is:

- Leaf tasks (base cases): ceil(N / T)
- Internal tasks (recursive splits): ceil(N / T) - 1
- Total tasks: approximately 2 * ceil(N / T) - 1

For example, N = 10,000 and T = 1,000:
- Leaf tasks: 10
- Internal tasks: 9
- Total: 19 tasks

The recursion tree has depth log2(N / T):
- N = 10,000, T = 1,000 => depth ~3-4

This is important for tuning:
- Too many tasks (small T): excessive overhead from task creation and scheduling
- Too few tasks (large T): insufficient parallelism, some cores may be idle
- Guideline: aim for at least parallelism * 4 leaf tasks for good load balancing

The work-stealing algorithm handles slight imbalances well, so exact balance
is not required.
```

```java
// Estimating task count
int n = 10_000;
int threshold = 1_000;
int leafTasks = (int) Math.ceil((double) n / threshold); // 10
int totalTasks = 2 * leafTasks - 1; // 19
int depth = (int) Math.ceil(Math.log(leafTasks) / Math.log(2)); // ~4

System.out.printf("Array: %d, Threshold: %d%n", n, threshold);
System.out.printf("Leaf tasks: %d, Total tasks: %d, Depth: %d%n",
    leafTasks, totalTasks, depth);
```

## Code Examples

- Source: [RecursiveTaskExamples.java](src/main/java/com/github/msorkhpar/claudejavatutor/forkjoin/RecursiveTaskExamples.java)
- Test: [RecursiveTaskExamplesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/forkjoin/RecursiveTaskExamplesTest.java)
