# 10.1.2. Big O Notation

## Concept Explanation

Big O notation is a mathematical notation used to describe the upper bound of an algorithm's growth rate in terms of time or space as the input size increases. It abstracts away constants and lower-order terms to focus on the dominant factor that determines scalability.

**Real-world analogy**: Imagine you are sorting a deck of cards. If you have 10 cards, even a bad sorting method works quickly. But if you have 1,000,000 cards, the method matters enormously. Big O notation tells you HOW your algorithm's performance scales. An O(n) algorithm (like dealing cards one by one) scales linearly: 10x more cards = 10x more time. An O(n^2) algorithm (like repeatedly finding the smallest card) scales quadratically: 10x more cards = 100x more time. An O(log n) algorithm (like binary search on sorted cards) scales logarithmically: 10x more cards = only 3.3x more time.

### Time Complexity

Time complexity measures how the number of operations grows with input size:

| Notation | Name | Example Operations |
|----------|------|-------------------|
| O(1) | Constant | Array access, HashMap get/put, stack push/pop |
| O(log n) | Logarithmic | Binary search, balanced BST operations |
| O(n) | Linear | Linear search, traversing a list, single loop |
| O(n log n) | Linearithmic | Merge sort, TimSort, heap sort |
| O(n^2) | Quadratic | Bubble sort, nested loops, naive duplicate check |
| O(2^n) | Exponential | Recursive fibonacci (without memoization), power set |
| O(n!) | Factorial | Generating all permutations |

### Space Complexity

Space complexity measures how much additional memory an algorithm needs beyond the input:

| Notation | Example |
|----------|---------|
| O(1) | In-place swap, iterative fibonacci |
| O(log n) | Recursive binary search (call stack) |
| O(n) | HashMap for frequency count, merge sort auxiliary array |
| O(n^2) | 2D matrix creation |

### Best, Average, and Worst Case

Most algorithms have different performance depending on the input:

- **Best case**: The input that causes the algorithm to run fastest (e.g., already sorted array for insertion sort: O(n)).
- **Average case**: Expected performance over all possible inputs (e.g., quicksort: O(n log n)).
- **Worst case**: The input that causes the algorithm to run slowest (e.g., already sorted array for naive quicksort: O(n^2)).

Big O typically describes the **worst case** unless stated otherwise. However, **amortized analysis** considers the average over a sequence of operations (e.g., ArrayList append is amortized O(1) despite occasional O(n) resizing).

## Key Points to Remember

1. Big O describes an **upper bound** on growth rate, not exact execution time.
2. **Constants are dropped**: O(2n) = O(n), O(n/2) = O(n), O(1000) = O(1).
3. **Lower-order terms are dropped**: O(n^2 + n) = O(n^2), O(n + log n) = O(n).
4. **Best case is rarely useful** for analysis; worst case and average case matter more.
5. **Space complexity includes** the call stack for recursive algorithms.
6. **Amortized complexity** considers the average cost over a sequence of operations (e.g., dynamic array resizing).
7. O(log n) base is irrelevant (log base changes are constant factors): O(log_2 n) = O(log_10 n) = O(ln n).
8. In Java, `Arrays.sort()` for primitives uses dual-pivot quicksort (O(n log n) average, O(n^2) worst). For objects, it uses TimSort (O(n log n) guaranteed).
9. HashMap operations are O(1) average, O(log n) worst case (Java 8+ with treeification).
10. When analyzing nested loops, multiply the complexities: an O(n) loop inside an O(m) loop is O(n * m).

## Relevant Java 21 Features

- **TimSort**: Java's default sort for objects (`Collections.sort()`, `Arrays.sort()` for objects) uses TimSort, which is O(n log n) worst case and O(n) best case for nearly sorted data. This is a hybrid merge sort/insertion sort.
- **Parallel streams**: `parallelStream()` can improve throughput for CPU-bound O(n) operations on large datasets by distributing work across cores, but introduces overhead that can make small datasets slower.
- **ConcurrentHashMap**: Thread-safe O(1) operations without locking the entire map (uses segment/bucket-level locking).
- **Virtual Threads**: For I/O-bound operations, virtual threads can be used to parallelize processing without the overhead of platform threads, effectively making I/O-bound O(n) work complete in O(n/k) wall-clock time where k is concurrency level.
- **`Stream.toList()`** (Java 16+): More efficient than `.collect(Collectors.toList())` as it creates an unmodifiable list with potentially less copying.

## Common Pitfalls and How to Avoid Them

1. **Confusing time complexity with actual speed**
   ```java
   // O(n) is not always faster than O(n^2) for small n
   // An O(n^2) algorithm with small constant may beat O(n log n) for n < 50
   // Example: Insertion sort (O(n^2)) is faster than merge sort (O(n log n)) for small arrays
   // This is why Java's TimSort switches to insertion sort for small subarrays
   ```
   **Solution**: Big O matters for large inputs. For small inputs, constants and cache behavior matter more.

2. **Ignoring space complexity**
   ```java
   // Memoization trades space for time
   // O(2^n) time -> O(n) time with O(n) space
   // But if n is very large, you might run out of memory
   Map<Integer, Long> memo = new HashMap<>(); // O(n) space
   ```
   **Solution**: Always analyze both time AND space complexity. Consider memory limits.

3. **Forgetting recursive call stack space**
   ```java
   // This recursive method uses O(n) space on the call stack
   int factorial(int n) {
       if (n <= 1) return 1;
       return n * factorial(n - 1); // Each call adds to the stack
   }
   // For n = 100000, this causes StackOverflowError
   ```
   **Solution**: Convert deep recursion to iteration when possible, or increase stack size.

4. **Using the wrong data structure, leading to hidden O(n) operations**
   ```java
   // WRONG: Checking if a List contains an element is O(n)
   List<Integer> list = new ArrayList<>();
   // ... add many elements
   if (list.contains(target)) { ... } // O(n) each time!

   // RIGHT: Use a HashSet for O(1) contains
   Set<Integer> set = new HashSet<>(list);
   if (set.contains(target)) { ... } // O(1) average
   ```
   **Solution**: Know the complexity of each data structure's operations.

5. **Assuming HashMap is always O(1)**
   ```java
   // HashMap worst case is O(log n) per operation (Java 8+)
   // If all keys hash to the same bucket, performance degrades
   // This can be exploited in adversarial scenarios (HashDoS attacks)
   ```
   **Solution**: Use good hash functions and be aware of worst-case scenarios.

## Best Practices and Optimization Techniques

1. **Trade space for time**: Precompute results, use lookup tables, memoization.
2. **Choose the right algorithm**: Use O(n log n) sorting over O(n^2) for large datasets.
3. **Avoid premature optimization**: Write correct code first, then profile to find bottlenecks.
4. **Use built-in library methods**: Java's `Arrays.sort()`, `Collections.sort()` are highly optimized.
5. **Reduce nested loops**: Look for ways to use HashMaps, sorting, or two-pointer techniques to reduce O(n^2) to O(n) or O(n log n).
6. **Consider amortized cost**: ArrayList's O(1) amortized append is fine even though individual appends may be O(n).
7. **Profile before choosing**: Use JMH (Java Microbenchmark Harness) for accurate benchmarks. Cache effects and JIT compilation can make real-world performance differ from theoretical analysis.
8. **Leverage Java's optimizations**: TimSort for nearly sorted data, dual-pivot quicksort for primitives, Red-Black trees in HashMap buckets.
9. **Use primitive streams** (`IntStream`, `LongStream`) to avoid autoboxing overhead in tight loops.
10. **Consider parallel processing** for CPU-bound O(n) or O(n log n) operations on large datasets.

## Edge Cases and Their Handling

1. **Empty input**: An algorithm on an empty array/list should return immediately (O(1)).
2. **Single element**: Binary search on a 1-element array should still work. Sort of a 1-element array is a no-op.
3. **Already sorted input**: Insertion sort is O(n) best case. Naive quicksort (first element pivot) is O(n^2) worst case on sorted input.
4. **All identical elements**: Many sorting algorithms handle this specially. Quicksort with poor pivot selection can degrade to O(n^2).
5. **Integer overflow**: When computing `mid = (left + right) / 2`, use `mid = left + (right - left) / 2` to avoid overflow.
6. **Very large inputs**: O(n^2) algorithms become impractical for n > 10^5. O(n log n) handles up to ~10^7. O(n) handles up to ~10^8-10^9.
7. **Negative numbers**: Ensure algorithms handle negative values correctly (e.g., `Math.abs(Integer.MIN_VALUE)` overflows).
8. **Null inputs**: Always validate inputs at the beginning of methods. Return empty collections or throw explicit exceptions.

## Interview-specific Insights

Interviewers commonly focus on:

- **Analyzing complexity of given code**: "What is the time complexity of this snippet?" -- Look for nested loops, recursive calls, and hidden costs in library methods.
- **Optimizing brute-force solutions**: Start with O(n^2) and optimize to O(n log n) or O(n) using appropriate data structures.
- **Space-time trade-offs**: "Can you solve this in O(n) time?" often means using a HashMap for O(n) space.
- **Comparing algorithms**: "Why use merge sort over quicksort?" (Merge sort is stable and has guaranteed O(n log n); quicksort is in-place but O(n^2) worst case.)
- **Amortized analysis**: "What is the time complexity of n ArrayList appends?" (O(n) total, O(1) amortized per append.)

Common tricky questions:

- "Is O(n) always faster than O(n^2)?" (No, for small n with different constants.)
- "What is the space complexity of a recursive DFS on a graph?" (O(V) for the visited set + O(V) for the call stack = O(V).)
- "What is the time complexity of `Arrays.sort()` in Java?" (O(n log n) for objects/TimSort; O(n log n) average, O(n^2) worst for primitives/dual-pivot quicksort.)
- "How do you reduce O(n^2) to O(n) for the two-sum problem?" (Use a HashSet/HashMap.)

## Interview Q&A Section

**Q1: What does Big O notation actually represent? Why do we drop constants and lower-order terms?**

```text
A1: Big O notation describes the asymptotic upper bound of an algorithm's growth rate.
It answers: "How does the running time/space grow as the input size approaches infinity?"

We drop constants because:
1. Constants depend on hardware, language, and implementation details that vary.
   An O(n) algorithm on a supercomputer may be faster than an O(1) operation on a
   slow embedded device.
2. As n grows very large, constants become irrelevant compared to the growth rate.
   O(1000n) is still fundamentally linear; O(0.001n^2) is still fundamentally quadratic.
   At large enough n, the quadratic always overtakes the linear.

We drop lower-order terms because:
1. For large n, the dominant term overwhelms all others.
   In O(n^2 + 100n + 500), when n = 1,000,000, the n^2 term (10^12) dwarfs
   100n (10^8) and 500.
2. It simplifies analysis while preserving the essential scaling behavior.

Big O gives us a language to quickly compare algorithms at scale:
- O(n) vs O(n^2): For 10x more data, O(n) takes 10x longer; O(n^2) takes 100x longer.
- This difference becomes critical in production systems handling millions of records.
```

```java
// Example: Two algorithms that find the max element
// Both are O(n), but with different constants

// Version 1: Single pass (1 comparison per element)
int max1 = array[0];
for (int i = 1; i < array.length; i++) {
    if (array[i] > max1) max1 = array[i]; // ~n comparisons
}

// Version 2: Unnecessary extra work (2 comparisons per element)
int max2 = Integer.MIN_VALUE;
for (int i = 0; i < array.length; i++) {
    if (array[i] > max2) max2 = array[i];
    if (array[i] == max2) continue; // Unnecessary comparison
    // ~2n comparisons
}

// Both are O(n), but Version 1 is ~2x faster in practice.
// Big O tells us they SCALE the same; profiling tells us which is faster.
```

**Q2: Explain the difference between O(1), O(log n), O(n), O(n log n), and O(n^2) with concrete Java examples.**

```text
A2: Here are concrete examples for each complexity class:

O(1) - Constant: The operation takes the same time regardless of input size.
- HashMap.get(), HashMap.put() (average case)
- ArrayList.get(index)
- Checking if a number is even
- Stack push/pop (ArrayDeque)

O(log n) - Logarithmic: Each step halves the problem size.
- Binary search on a sorted array
- TreeMap.get(), TreeSet.contains()
- Finding an element in a balanced BST
- Computing power using fast exponentiation

O(n) - Linear: Must examine every element once.
- Linear search through an unsorted array
- Finding the maximum element
- Summing all elements
- HashMap.containsValue() (not containsKey!)

O(n log n) - Linearithmic: Typically "sort then process" or divide-and-conquer.
- Arrays.sort() for objects (TimSort)
- Merge sort, heap sort
- "Sort and then use binary search" pattern

O(n^2) - Quadratic: Nested iteration over all pairs.
- Bubble sort, insertion sort (worst case), selection sort
- Checking all pairs for duplicates (naive)
- Nested loops: for each element, scan all other elements
```

```java
// O(1) - Constant time
int[] arr = {1, 2, 3, 4, 5};
int val = arr[2]; // Direct index access, always same speed

// O(log n) - Logarithmic time
int idx = Arrays.binarySearch(new int[]{1, 3, 5, 7, 9}, 7); // Halves search space each step

// O(n) - Linear time
int max = Arrays.stream(arr).max().orElseThrow(); // Must check every element

// O(n log n) - Linearithmic time
int[] sorted = arr.clone();
Arrays.sort(sorted); // TimSort: O(n log n)

// O(n^2) - Quadratic time
// Find all pairs that sum to a target (naive approach)
for (int i = 0; i < arr.length; i++) {
    for (int j = i + 1; j < arr.length; j++) {
        if (arr[i] + arr[j] == 6) {
            // Found pair
        }
    }
}
```

**Q3: What is amortized analysis? Explain with the ArrayList example.**

```text
A3: Amortized analysis considers the average cost of an operation over a worst-case
sequence of operations, rather than the worst case for a single operation.

ArrayList.add() example:
- ArrayList is backed by an array with a fixed capacity.
- When the array is full and you add an element, it creates a new array with 1.5x
  the capacity and copies all existing elements. This single operation is O(n).
- However, this expensive resize happens rarely. Most add() calls are O(1).

Analysis for n add operations:
- Start with capacity 10, grow by 1.5x each time.
- Resizes happen at: 10, 15, 22, 33, 49, 73, 109, ...
- Total copies across all resizes: ~3n (geometric series)
- Total cost for n additions: n (individual adds) + 3n (total copies) = O(4n) = O(n)
- Amortized cost per add: O(n) / n = O(1)

So while a single add CAN be O(n), the amortized cost per operation is O(1).
This is why ArrayList is efficient despite occasional resizing.

Other amortized O(1) examples:
- StringBuilder.append(): Same dynamic array strategy
- HashMap.put(): Rehashing is O(n), but happens rarely enough to be amortized O(1)
- ArrayDeque operations: Circular buffer with occasional resizing
```

```java
// Demonstrating ArrayList resizing behavior
List<Integer> list = new ArrayList<>(4); // Start with capacity 4

// First 4 adds: O(1) each, no resizing
list.add(1); // size=1, capacity=4
list.add(2); // size=2, capacity=4
list.add(3); // size=3, capacity=4
list.add(4); // size=4, capacity=4

// 5th add triggers resize: O(n) - copies 4 elements, capacity becomes 6
list.add(5); // size=5, capacity=6

// Next add is O(1) again
list.add(6); // size=6, capacity=6

// 7th add triggers resize: O(n) - copies 6 elements, capacity becomes 9
list.add(7); // size=7, capacity=9

// Despite occasional O(n) resizes, adding n elements total is O(n)
// Amortized cost per add = O(1)
```

**Q4: How do you optimize a brute-force O(n^2) solution to O(n) using a HashMap?**

```text
A4: The classic example is the "Two Sum" problem: given an array of integers and a
target sum, find two numbers that add up to the target.

Brute-force O(n^2): Check every pair of elements.
Optimized O(n): Use a HashMap to store seen elements and check for complements.

The key insight: Instead of looking forward for each element (O(n) per element = O(n^2)),
look backward in a HashMap (O(1) per element = O(n) total).

This space-time trade-off is one of the most common interview optimization patterns:
- Build a lookup structure (HashMap/HashSet) in O(n) space
- Use it for O(1) lookups instead of O(n) scans

Similar optimizations:
- Finding duplicates: O(n^2) nested loops -> O(n) with HashSet
- Finding frequency of elements: O(n^2) counting -> O(n) with HashMap
- Checking anagrams: O(n^2) comparison -> O(n) with character frequency map
- Finding pairs with a given difference: O(n^2) -> O(n) with HashSet
```

```java
// Brute-force O(n^2): Check all pairs
public int[] twoSumBruteForce(int[] nums, int target) {
    for (int i = 0; i < nums.length; i++) {
        for (int j = i + 1; j < nums.length; j++) {
            if (nums[i] + nums[j] == target) {
                return new int[]{i, j};
            }
        }
    }
    return new int[]{}; // Not found
}

// Optimized O(n): Use HashMap for O(1) lookups
public int[] twoSumOptimized(int[] nums, int target) {
    Map<Integer, Integer> seen = new HashMap<>(); // O(n) space
    for (int i = 0; i < nums.length; i++) {
        int complement = target - nums[i];
        if (seen.containsKey(complement)) {    // O(1) lookup
            return new int[]{seen.get(complement), i};
        }
        seen.put(nums[i], i);                  // O(1) insert
    }
    return new int[]{}; // Not found
}
// Total: O(n) time, O(n) space
```

**Q5: What is the difference between best case, average case, and worst case? Give examples with QuickSort.**

```text
A5: Different inputs can cause the same algorithm to perform very differently:

QuickSort Analysis:
- Best case O(n log n): Occurs when the pivot always divides the array into two equal
  halves. Each level of recursion processes n elements, and there are log n levels.

- Average case O(n log n): For random inputs, the pivot is expected to divide the array
  reasonably well on average. Mathematical analysis shows the expected number of
  comparisons is ~1.39 * n * log2(n).

- Worst case O(n^2): Occurs when the pivot is always the smallest or largest element
  (e.g., already sorted array with first-element pivot). Each partition removes only
  one element, leading to n levels of recursion with O(n) work each.

How to mitigate QuickSort's worst case:
1. Median-of-three pivot: Choose the median of the first, middle, and last elements.
2. Random pivot: Choose a random element as pivot.
3. Introsort: Switch to heapsort when recursion depth exceeds a threshold (used by
   many C++ standard libraries).

Comparison with other sorts:
- Insertion sort: Best O(n), Average O(n^2), Worst O(n^2)
- Merge sort: Best O(n log n), Average O(n log n), Worst O(n log n) -- consistent!
- TimSort: Best O(n), Average O(n log n), Worst O(n log n) -- best of both worlds

This is why Java uses:
- TimSort for objects (stable, guaranteed O(n log n), great for nearly sorted data)
- Dual-pivot QuickSort for primitives (faster in practice, stability not needed)
```

```java
// QuickSort with median-of-three pivot to avoid worst case
var analysis = new BigONotation.CaseAnalysis();

// Best/Average case: random array, O(n log n)
int[] random = {5, 2, 8, 1, 9, 3, 7, 4, 6};
int[] sorted = analysis.quickSort(random); // [1, 2, 3, 4, 5, 6, 7, 8, 9]

// Potential worst case input (already sorted) - mitigated by median-of-three
int[] alreadySorted = {1, 2, 3, 4, 5, 6, 7, 8, 9};
int[] result = analysis.quickSort(alreadySorted); // Still works efficiently

// Insertion sort: best case O(n) on already sorted input
int[] bestCase = {1, 2, 3, 4, 5};
int[] insertSorted = analysis.insertionSort(bestCase); // Nearly O(n)

// Insertion sort: worst case O(n^2) on reverse sorted input
int[] worstCase = {5, 4, 3, 2, 1};
int[] insertWorst = analysis.insertionSort(worstCase); // O(n^2) comparisons
```

**Q6: Explain space complexity with examples of O(1), O(n), and O(n^2) algorithms.**

```text
A6: Space complexity measures the additional memory an algorithm uses beyond the input.
It does NOT include the input itself (that's already allocated).

O(1) Space - Constant:
- The algorithm uses a fixed number of variables regardless of input size.
- Examples: in-place swap, iterative fibonacci, finding max element.
- Note: An in-place sorting algorithm (like heapsort) uses O(1) extra space.

O(n) Space - Linear:
- The algorithm's extra memory grows proportionally with input size.
- Examples: creating a HashMap for frequency counting, merge sort's auxiliary array,
  recursive DFS call stack on a path of length n.
- Memoization: Fibonacci with memo uses O(n) space to avoid O(2^n) time.

O(n^2) Space - Quadratic:
- Typically seen when creating 2D data structures.
- Examples: adjacency matrix for a graph, 2D DP table, n x n grid.

Important considerations:
1. Recursive calls add to the call stack: each frame uses O(1) space, and the depth
   determines total stack space. A recursive DFS on a tree uses O(h) where h is height.
2. Tail call optimization does NOT exist in Java. Recursive algorithms always use
   stack space proportional to recursion depth.
3. In garbage-collected languages like Java, space complexity also considers GC pressure
   from temporary objects.
```

```java
// O(1) space: Iterative fibonacci - only 2 variables
var space = new BigONotation.SpaceComplexity();
long fib = space.fibonacciIterative(50); // Uses only prev1, prev2 variables

// O(n) space: Frequency map
Map<Character, Integer> freq = space.characterFrequency("hello world");
// Creates a map with at most n unique characters

// O(n) space: Recursive fibonacci call stack
// fibonacciRecursive(n) has recursion depth n, each frame on the stack
long fibRec = space.fibonacciRecursive(10); // O(n) stack frames

// O(n) space: Memoization trades space for time
long fibMemo = space.fibonacciMemoized(50); // O(n) HashMap entries
// But O(n) time instead of O(2^n)!

// O(n^2) space: Creating a matrix
int[][] matrix = space.createMatrix(100); // 100x100 = 10,000 entries
```

**Q7: How does Java's `Arrays.sort()` work differently for primitives vs objects?**

```text
A7: Java uses two different sorting algorithms depending on the data type:

For primitive arrays (int[], long[], double[], etc.):
- Algorithm: Dual-Pivot QuickSort (since Java 7)
- Time: O(n log n) average, O(n^2) worst case
- Space: O(log n) for recursion stack
- Stability: NOT stable (equal elements may be reordered)
- Why: Fastest in practice for primitives due to cache efficiency and
  no object overhead. Worst case is rare with the dual-pivot strategy.

For object arrays (Integer[], String[], etc.) and Collections.sort():
- Algorithm: TimSort (since Java 7)
- Time: O(n log n) worst case, O(n) best case (nearly sorted)
- Space: O(n) for temporary merge array
- Stability: STABLE (equal elements maintain their relative order)
- Why: Stability is important for objects (you might sort by one field,
  then by another). TimSort excels on real-world data that is often
  partially sorted.

TimSort details:
- Hybrid of merge sort and insertion sort
- Divides the array into "runs" (pre-sorted subsequences)
- Uses insertion sort for small runs (typically < 32 elements)
- Merges runs using a modified merge sort with galloping mode
- Exploits existing order in the data, making it O(n) for sorted input

For parallel sorting: Arrays.parallelSort()
- Uses a parallel merge sort for large arrays
- Falls back to Arrays.sort() for small arrays (< 8192 elements)
- Time: O(n log n / p) where p is number of processors
- Space: O(n) for merge operations
```

```java
// Primitive array: uses Dual-Pivot QuickSort
int[] primitives = {5, 2, 8, 1, 9, 3};
Arrays.sort(primitives); // O(n log n) average, NOT stable

// Object array: uses TimSort
Integer[] objects = {5, 2, 8, 1, 9, 3};
Arrays.sort(objects); // O(n log n) worst case, STABLE

// Collections.sort: also uses TimSort
List<Integer> list = new ArrayList<>(List.of(5, 2, 8, 1, 9, 3));
Collections.sort(list); // O(n log n), stable

// Parallel sort for large arrays
int[] large = new int[1_000_000];
Arrays.parallelSort(large); // Uses multiple threads

// TimSort's best case: nearly sorted data is O(n)
int[] nearlySorted = {1, 2, 3, 5, 4, 6, 7, 8, 10, 9};
Arrays.sort(nearlySorted); // Very fast due to existing runs
```

## Code Examples

- Test: [BigONotationTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/datastructures/BigONotationTest.java)
- Source: [BigONotation.java](src/main/java/com/github/msorkhpar/claudejavatutor/datastructures/BigONotation.java)
