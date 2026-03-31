# 6.6.1.3. Implementing RecursiveAction for Resultless Tasks

## Concept Explanation

`RecursiveAction` is an abstract class in `java.util.concurrent` that extends `ForkJoinTask<Void>`. It is designed for
fork/join computations that **do not return a result** -- they perform side effects such as modifying an array in-place,
updating shared data structures (with proper synchronization), or writing output.

The `compute()` method returns `void`. Instead of combining results from subtasks, the effects of all subtasks
collectively produce the desired outcome.

**Real-world analogy**: Imagine painting a long fence. You divide the fence into sections and assign each section to a
painter. No painter returns a "result" -- the fence itself is the output. Each painter modifies their section in place.
If a section is small enough, one painter handles it directly. `RecursiveAction` works the same way.

### RecursiveAction vs. RecursiveTask

| Aspect            | RecursiveTask<V>        | RecursiveAction          |
|-------------------|-------------------------|--------------------------|
| Return type       | V (any type)            | Void (no return)         |
| compute() returns | V                       | void                     |
| Use case          | Reduce / aggregate      | In-place transformation  |
| Result combining  | At join points          | Not needed               |
| Preferred pattern | fork-compute-join       | invokeAll()              |

### When to Use RecursiveAction

- **In-place array modifications**: incrementing, squaring, filling, transforming elements
- **Parallel initialization**: setting up large arrays or data structures
- **Side-effect computations**: logging, counting with atomic variables, sending notifications
- **Parallel iteration**: processing each element without producing a combined result

## Key Points to Remember

1. `RecursiveAction` extends `ForkJoinTask<Void>` -- its `compute()` returns `void`.
2. Use `invokeAll(left, right)` to fork and join two subtasks in one call.
3. `invokeAll()` is preferred over manual fork/join for `RecursiveAction` because there is no result to combine inline.
4. Side effects must be **thread-safe** -- use atomic variables, thread-local accumulators, or operate on disjoint
   array ranges.
5. The threshold pattern is the same as `RecursiveTask` -- check size, compute sequentially or split.
6. Multiple subtasks can be passed to `invokeAll(task1, task2, task3, ...)`.
7. `invokeAll()` blocks until all provided tasks complete.
8. For counting or accumulating in `RecursiveAction`, use `AtomicInteger`/`AtomicLong`.

## Relevant Java 21 Features

- `RecursiveAction` is unchanged since Java 7, but modern Java offers alternatives:
  - **`Arrays.parallelSetAll()`**: Built-in parallel array initialization (uses Fork/Join internally).
  - **`Arrays.parallelSort()`**: Built-in parallel sorting.
  - **`Arrays.parallelPrefix()`**: Parallel prefix computation.
  - **Virtual threads**: For I/O-side-effect tasks, prefer virtual threads over Fork/Join.

## Common Pitfalls and How to Avoid Them

1. **Unsynchronized writes to shared state**

   ```java
   // Problem: race condition on count
   private int count = 0;
   protected void compute() {
       if (length <= THRESHOLD) {
           for (int i = start; i < end; i++) {
               if (array[i] == target) count++; // data race!
           }
           return;
       }
       invokeAll(left, right);
   }
   ```

   **Fix**: Use `AtomicInteger` or accumulate locally and update atomically:

   ```java
   private final AtomicInteger count;
   protected void compute() {
       if (length <= THRESHOLD) {
           int localCount = 0;
           for (int i = start; i < end; i++) {
               if (array[i] == target) localCount++;
           }
           count.addAndGet(localCount); // single atomic update
           return;
       }
       invokeAll(left, right);
   }
   ```

2. **Using fork-compute-join pattern unnecessarily**: Unlike `RecursiveTask`, there is no result to compute inline.
   `invokeAll()` is cleaner.

   ```java
   // Unnecessary complexity for RecursiveAction
   left.fork();
   right.compute();
   left.join();

   // Cleaner
   invokeAll(left, right);
   ```

3. **Overlapping array ranges**: If two subtasks write to overlapping ranges, you get data corruption.
   **Fix**: Ensure `[start, mid)` and `[mid, end)` ranges are strictly disjoint.

4. **Modifying the array and reading it concurrently**: If another thread reads the array while `RecursiveAction`
   modifies it, use proper synchronization or ensure the action completes before reading.

## Best Practices and Optimization Techniques

1. Use `invokeAll()` for `RecursiveAction` -- it is cleaner and handles fork/join internally.
2. Operate on **disjoint array ranges** to avoid synchronization entirely.
3. For accumulation patterns, use `AtomicInteger`/`AtomicLong` with **local accumulation** -- count locally in the
   base case and update the atomic variable once.
4. Consider using `Arrays.parallelSetAll()` instead of a custom `RecursiveAction` for simple array initialization.
5. Validate inputs in the constructor, not in `compute()` -- fail fast.
6. Keep the base case simple and fast -- this is where most time is spent.

## Edge Cases and Their Handling

1. **Null array**: Throw `IllegalArgumentException` in the constructor.
2. **Empty array**: Return immediately from `compute()` without forking.
3. **Single element**: Handled by the threshold -- compute sequentially.
4. **Start >= end**: No work to do -- return immediately.
5. **AtomicInteger overflow**: For very large arrays with many matches, use `AtomicLong`.
6. **Concurrent reads during modification**: The array is being modified in place. Ensure no other thread reads it
   until the action completes (call `invoke()` which blocks until done).

## Interview-specific Insights

- Interviewers may ask you to implement an in-place parallel array transformation.
- Know the difference between `invokeAll()` and manual fork/join.
- Be able to explain why `RecursiveAction` uses `invokeAll()` while `RecursiveTask` uses fork-compute-join.
- Understand thread safety concerns with in-place modifications.
- Be ready to discuss when `RecursiveAction` is better than `RecursiveTask` (and vice versa).

## Interview Q&A Section

**Q1: When should you use RecursiveAction instead of RecursiveTask?**

```text
A1: Use RecursiveAction when your computation produces no return value -- it
performs side effects instead. Common scenarios:

1. In-place array modifications (increment, square, fill, transform)
2. Parallel initialization of data structures
3. Side-effect operations (logging, counting with atomics)
4. Writing output to files or streams

Use RecursiveTask when you need to compute and return a result that is
combined from subtask results (sum, max, sorted array, search result).

Rule of thumb: If your sequential version returns void, use RecursiveAction.
If it returns a value, use RecursiveTask.
```

```java
// RecursiveAction -- no return value, modifies array in place
class SquareAction extends RecursiveAction {
    protected void compute() {
        if (length <= THRESHOLD) {
            for (int i = start; i < end; i++)
                array[i] = array[i] * array[i];
            return;
        }
        invokeAll(
            new SquareAction(array, start, mid),
            new SquareAction(array, mid, end)
        );
    }
}
```

**Q2: What is invokeAll() and why is it preferred for RecursiveAction?**

```text
A2: invokeAll() is a static method on ForkJoinTask that forks all provided tasks
and waits for all of them to complete. It is a convenience method that combines
fork() and join() for multiple tasks.

Why it is preferred for RecursiveAction:
- RecursiveAction has no result to combine, so there is no advantage to
  computing one subtask inline (the fork-compute-join pattern)
- invokeAll() is more readable and less error-prone
- It handles the fork/join lifecycle automatically

For RecursiveTask, fork-compute-join is preferred because the current thread
can productively compute one subtask's result while waiting for the other.
With RecursiveAction, both subtasks perform side effects independently, so
invokeAll() is equally efficient and cleaner.

invokeAll() can accept 2 or more tasks:
  invokeAll(task1, task2);
  invokeAll(task1, task2, task3, task4);
  invokeAll(collectionOfTasks);
```

```java
// Using invokeAll with RecursiveAction
protected void compute() {
    if (end - start <= THRESHOLD) {
        // base case: sequential work
        for (int i = start; i < end; i++) {
            array[i] += incrementBy;
        }
        return;
    }
    int mid = start + (end - start) / 2;
    invokeAll(
        new IncrementAction(array, start, mid, incrementBy),
        new IncrementAction(array, mid, end, incrementBy)
    );
}
```

**Q3: How do you accumulate results in a RecursiveAction?**

```text
A3: Since RecursiveAction returns void, accumulation requires a shared
thread-safe container. The recommended approach is:

1. Pass an AtomicInteger/AtomicLong to the task
2. In the base case, count locally (no atomics needed for local variable)
3. Update the atomic variable ONCE with the local count
4. This minimizes contention on the atomic variable

Why local accumulation matters:
- Updating an atomic variable on every iteration causes contention
- Local counting is purely sequential (fast)
- A single addAndGet() at the end of each base case is minimal contention
- With K leaf tasks, there are only K atomic updates instead of N

Alternative: Use RecursiveTask<Integer> instead, which avoids shared state
entirely by returning counts that are combined at join points.
```

```java
class CountMatchingAction extends RecursiveAction {
    private final int[] array;
    private final int target;
    private final AtomicInteger counter;

    protected void compute() {
        if (end - start <= THRESHOLD) {
            int localCount = 0; // no atomics needed locally
            for (int i = start; i < end; i++) {
                if (array[i] == target) localCount++;
            }
            counter.addAndGet(localCount); // single atomic update
            return;
        }
        int mid = start + (end - start) / 2;
        invokeAll(
            new CountMatchingAction(array, start, mid, target, counter),
            new CountMatchingAction(array, mid, end, target, counter)
        );
    }
}

// Usage
AtomicInteger count = new AtomicInteger(0);
pool.invoke(new CountMatchingAction(array, 0, array.length, target, count));
System.out.println("Count: " + count.get());
```

**Q4: What built-in Java methods use RecursiveAction internally?**

```text
A4: Several java.util.Arrays methods use the Fork/Join framework internally:

1. Arrays.parallelSort() -- sorts an array in parallel using a merge-sort
   variant implemented as RecursiveAction/RecursiveTask internally.

2. Arrays.parallelSetAll() -- initializes array elements in parallel using
   an IntUnaryOperator (for int[]) or IntFunction<T> (for T[]).

3. Arrays.parallelPrefix() -- computes prefix sums (or other associative
   operations) in parallel.

These methods use the common ForkJoinPool and handle threshold selection
internally. For standard array operations, prefer these built-in methods
over writing custom RecursiveAction tasks.

Custom RecursiveAction is needed when:
- Your transformation logic is complex
- You need a custom pool
- You need custom threshold behavior
- The built-in methods don't cover your use case
```

```java
// Built-in parallel array operations (use Fork/Join internally)

// parallelSort
int[] array = {5, 3, 1, 4, 2};
Arrays.parallelSort(array); // [1, 2, 3, 4, 5]

// parallelSetAll -- initialize with index-based function
int[] squares = new int[100];
Arrays.parallelSetAll(squares, i -> i * i);
// squares = [0, 1, 4, 9, 16, ...]

// parallelPrefix -- cumulative sum
int[] cumSum = {1, 2, 3, 4, 5};
Arrays.parallelPrefix(cumSum, Integer::sum);
// cumSum = [1, 3, 6, 10, 15]
```

**Q5: How do you test RecursiveAction tasks?**

```text
A5: Testing RecursiveAction requires verifying side effects since there is
no return value. Key strategies:

1. Array verification: Create an input array, invoke the action, then
   assert the array contents match expected values.

2. AtomicInteger verification: For counting tasks, verify the counter's
   final value.

3. Null/empty handling: Test that null and empty arrays are handled
   gracefully (no exceptions, no modifications).

4. Boundary testing: Test with arrays smaller than, equal to, and larger
   than the threshold to exercise both the base case and recursive case.

5. Correctness vs. sequential: Compare the result of the parallel action
   with a simple sequential implementation.

6. Concurrent consistency: For large arrays, verify that all elements are
   correctly modified (no missed or double-modified elements).
```

```java
@Test
void testIncrementAction() {
    int[] array = {1, 2, 3, 4, 5};
    ForkJoinPool.commonPool().invoke(new IncrementAction(array, 0, 5, 10));
    assertThat(array).containsExactly(11, 12, 13, 14, 15);
}

@Test
void testCountMatchingAction() {
    int[] array = {1, 2, 3, 2, 1, 2};
    AtomicInteger counter = new AtomicInteger(0);
    ForkJoinPool.commonPool().invoke(
        new CountMatchingAction(array, 0, 6, 2, counter));
    assertThat(counter.get()).isEqualTo(3);
}

@Test
void testLargeArrayConsistency() {
    int[] array = new int[10_000];
    Arrays.fill(array, 5);
    ForkJoinPool.commonPool().invoke(new SquareAction(array, 0, 10_000));
    assertThat(array).containsOnly(25); // every element should be 25
}
```

## Code Examples

- Source: [RecursiveActionExamples.java](src/main/java/com/github/msorkhpar/claudejavatutor/forkjoin/RecursiveActionExamples.java)
- Test: [RecursiveActionExamplesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/forkjoin/RecursiveActionExamplesTest.java)
