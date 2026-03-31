# 6.5.4. Collection Capacity and Resizing

## Concept Explanation

Understanding how collections manage their internal storage is critical for writing performant Java code. Most
array-backed collections (ArrayList, HashMap, HashSet) allocate an internal array with some initial capacity and grow
it when the number of elements exceeds a threshold. This growth involves creating a new, larger array and copying all
existing elements -- an expensive O(n) operation.

**Real-world analogy**: Think of collection capacity like renting office space. Starting with a small office (initial
capacity) is cheap, but when your team grows beyond the office's capacity, you need to move to a bigger space (resize).
Moving means copying all your furniture and files (rehashing). If you know your team will be 100 people, you should
rent a 100-person office from the start (pre-sizing) rather than moving 3-4 times as you grow from 10 to 20 to 40 to
100.

### 6.5.4.1. Initial Capacity and Load Factor in HashMap

- **Initial capacity**: The number of buckets in the hash table when it is created. Default: **16**.
- **Load factor**: The ratio of entries to capacity at which the table resizes. Default: **0.75**.
- **Threshold**: `capacity * loadFactor`. When `size > threshold`, the table doubles.
- HashMap always rounds the capacity up to the nearest **power of 2** (e.g., requesting 10 gives 16).

### 6.5.4.2. Resizing and Rehashing in HashMap

When a HashMap exceeds its threshold:
1. A new array of **double** the current capacity is allocated.
2. Every entry is **rehashed** and placed in its new position (`hash & (newCapacity - 1)`).
3. This is an O(n) operation that temporarily doubles memory usage.
4. In Java 8+, tree bins (red-black trees used for collision buckets) may be split during rehashing.

### 6.5.4.3. Capacity Considerations in Other Collections

- **ArrayList**: Default initial capacity is **10**. Grows by approximately **50%** (`oldCapacity + (oldCapacity >> 1)`).
  Use `trimToSize()` to shrink to actual size.
- **ConcurrentHashMap**: Accepts initial capacity, load factor, and **concurrency level** (hint for internal sizing).
- **HashSet**: Internally backed by HashMap, so the same capacity and load factor rules apply.
- **LinkedList**: No capacity concept -- nodes are individually allocated.
- **CopyOnWriteArrayList**: The internal array always exactly matches the number of elements (no spare capacity).

## Key Points to Remember

1. **Pre-sizing saves time**: If you know the expected number of elements, set the initial capacity to avoid repeated
   resizing. For HashMap: `new HashMap<>((int)(expectedSize / 0.75f) + 1)`.
2. **HashMap capacity is always a power of 2**: This enables fast modulo via bitwise AND (`hash & (capacity - 1)`).
3. **Load factor 0.75 is a good trade-off**: Lower values waste space; higher values increase collision probability.
4. **Resizing is O(n)**: Every element must be rehashed and repositioned.
5. **ArrayList grows by ~50%**: Each resize copies the entire array. Pre-sizing with the constructor avoids this.
6. **`trimToSize()`** reduces ArrayList's internal array to match its current size, freeing unused memory.
7. **ConcurrentHashMap** adjusts internal sizing based on the concurrency level parameter.

## Relevant Java 21 Features

- **`HashMap.newHashMap(int numMappings)`** (Java 19+): Factory method that creates a HashMap pre-sized to hold the
  specified number of mappings without resizing. Eliminates the need to manually calculate initial capacity.
- **`HashSet.newHashSet(int numElements)`** (Java 19+): Similar factory method for HashSet.
- **`LinkedHashMap.newLinkedHashMap(int numMappings)`** (Java 19+): Same for LinkedHashMap.

```java
// Java 19+ - the right way to pre-size
Map<String, Integer> map = HashMap.newHashMap(100); // Sized to hold 100 entries
Set<String> set = HashSet.newHashSet(50);           // Sized to hold 50 elements
```

## Common Pitfalls and How to Avoid Them

1. **Not pre-sizing HashMap when the number of entries is known**
   ```java
   // BAD: Default capacity 16, will resize multiple times for 1000 entries
   Map<String, Integer> map = new HashMap<>();
   for (int i = 0; i < 1000; i++) {
       map.put("key" + i, i); // Resizes at 12, 24, 48, 96, 192, 384, 768
   }

   // GOOD: Pre-sized to avoid resizing
   Map<String, Integer> preSized = new HashMap<>((int)(1000 / 0.75f) + 1);
   // Or in Java 19+:
   // Map<String, Integer> preSized = HashMap.newHashMap(1000);
   ```

2. **Confusing initial capacity with number of elements for HashMap**
   ```java
   // BAD: capacity 100 with load factor 0.75 resizes at 75 elements!
   Map<String, Integer> map = new HashMap<>(100);
   // To hold 100 elements without resizing, you need capacity >= 134

   // GOOD: Account for load factor
   Map<String, Integer> correct = new HashMap<>((int)(100 / 0.75f) + 1);
   ```

3. **Using overly large initial capacities**
   ```java
   // BAD: Wastes 4MB of memory for a map that holds 10 entries
   Map<String, Integer> wasteful = new HashMap<>(1_000_000);

   // GOOD: Size appropriately
   Map<String, Integer> appropriate = new HashMap<>(16); // Holds up to 12 elements
   ```

4. **Forgetting to trimToSize() large ArrayLists after initial population**
   ```java
   // The internal array may be much larger than needed after removals
   ArrayList<String> list = new ArrayList<>(10_000);
   // ... populate and then remove most elements
   list.subList(100, list.size()).clear();
   // Internal array still has capacity 10_000
   list.trimToSize(); // Now internal array is exactly 100
   ```

## Best Practices and Optimization Techniques

1. **Pre-size collections when possible**: `new HashMap<>(expectedSize * 4 / 3 + 1)` or `HashMap.newHashMap(n)`.
2. **Use the `ensureCapacity()` method on ArrayList** before bulk insertions.
3. **Profile before optimizing**: Collection resizing is rarely the bottleneck in production applications.
4. **Consider `Map.of()` / `List.of()`** for small, fixed-size collections -- they use optimized internal
   representations.
5. **For very large maps**, consider setting concurrency level in ConcurrentHashMap to match expected thread count.
6. **Use `Collections.emptyList()` / `Collections.emptyMap()`** instead of creating new empty collections.

## Edge Cases and Their Handling

1. **Capacity of 0**: `new HashMap<>(0)` creates a valid map with minimum capacity (1 bucket, or resized to the
   minimum on first put).
2. **Negative capacity**: Throws `IllegalArgumentException` for ArrayList and HashMap.
3. **Load factor of 0 or negative**: Throws `IllegalArgumentException`.
4. **Load factor > 1**: Valid but unusual. The map will hold more entries than buckets before resizing, increasing
   collision chains but using less memory.
5. **Integer overflow in capacity**: Extremely large capacities may cause `OutOfMemoryError` during array allocation.
6. **Empty ArrayList after trimToSize()**: The internal array has length 0; the next `add()` will allocate a new array.

## Interview-specific Insights

Interviewers commonly ask about:
- Default initial capacity and load factor of HashMap (16 and 0.75)
- Why HashMap capacity is a power of 2 (enables bitwise AND for bucket index)
- How to properly pre-size a HashMap for N elements
- The resizing behavior (doubles capacity, O(n) rehash)
- ArrayList growth strategy (~50% increase each time)
- The difference between `size()` and `capacity` (size is public, capacity is internal)
- How to calculate the number of resizes for a given number of insertions
- Why CopyOnWriteArrayList has no capacity concept (exact-size array)

## Interview Q&A Section

**Q1: What is the default initial capacity and load factor of HashMap, and why?**

```text
A1: The default initial capacity is 16 and the default load factor is 0.75.

Why 16?
- It is a power of 2, which enables fast bucket index computation using bitwise AND: 
  index = hash & (capacity - 1). This is equivalent to hash % capacity but significantly faster.
- 16 is a reasonable default that balances memory usage (16 object references) with the need to 
  avoid immediate resizing for small maps.

Why 0.75?
- It represents a sweet spot between space and time efficiency:
  * Lower load factors (e.g., 0.5) -> fewer collisions, faster lookups, but more wasted space
  * Higher load factors (e.g., 0.9) -> more collisions, slower lookups, but less wasted space
- At 0.75, the expected number of entries per bucket is about 0.5 (using Poisson distribution), 
  meaning most buckets have 0 or 1 entries, keeping lookup time close to O(1).
- The Javadoc states that 0.75 offers "a good tradeoff between time and space costs."

The threshold (when resizing occurs) = capacity * loadFactor = 16 * 0.75 = 12.
So a default HashMap resizes after the 12th entry.
```

```java
// Default HashMap: capacity=16, loadFactor=0.75, threshold=12
Map<String, Integer> defaultMap = new HashMap<>();

// After 12 entries, the map resizes to capacity=32, threshold=24
for (int i = 0; i < 13; i++) {
    defaultMap.put("key" + i, i);
}
// Internal capacity is now 32

// Custom capacity and load factor
Map<String, Integer> custom = new HashMap<>(32, 0.6f);
// threshold = 32 * 0.6 = 19, resizes after 19 entries

// Pre-sizing for known count (Java 19+)
Map<String, Integer> preSized = HashMap.newHashMap(100);
// Properly sized to hold 100 entries without resizing
```

**Q2: How many times does a HashMap resize when you insert 1000 elements with default settings?**

```text
A2: Starting from default capacity (16) with load factor 0.75:

Resize #1: At 13th element (threshold 12 exceeded) -> capacity 32, threshold 24
Resize #2: At 25th element (threshold 24 exceeded) -> capacity 64, threshold 48
Resize #3: At 49th element (threshold 48 exceeded) -> capacity 128, threshold 96
Resize #4: At 97th element (threshold 96 exceeded) -> capacity 256, threshold 192
Resize #5: At 193rd element (threshold 192 exceeded) -> capacity 512, threshold 384
Resize #6: At 385th element (threshold 384 exceeded) -> capacity 1024, threshold 768
Resize #7: At 769th element (threshold 768 exceeded) -> capacity 2048, threshold 1536

Total: 7 resizes! Each resize rehashes ALL existing elements.

To avoid this, pre-size: new HashMap<>((int)(1000 / 0.75f) + 1) = new HashMap<>(1334)
This rounds up to capacity 2048 (next power of 2) with threshold 1536 -- no resizing needed.

Or use Java 19+: HashMap.newHashMap(1000)
```

```java
// 7 resizes for 1000 elements with default capacity
Map<String, Integer> inefficient = new HashMap<>();
for (int i = 0; i < 1000; i++) {
    inefficient.put("key" + i, i);
}

// 0 resizes with pre-sizing
Map<String, Integer> efficient = new HashMap<>((int)(1000 / 0.75f) + 1);
for (int i = 0; i < 1000; i++) {
    efficient.put("key" + i, i);
}

// The efficient version avoids 7 O(n) resize operations
```

**Q3: Why must HashMap capacity always be a power of 2?**

```text
A3: HashMap capacity must be a power of 2 for performance and correctness reasons:

1. Fast modulo operation:
   - The bucket index is computed as: index = hash & (capacity - 1)
   - When capacity is a power of 2, (capacity - 1) is a bitmask of all 1s
   - Example: capacity=16 -> 16-1=15 -> binary: 1111
   - hash & 1111 extracts the lower 4 bits, equivalent to hash % 16
   - Bitwise AND is much faster than modulo division

2. Even distribution:
   - The bitmask ensures hash values are evenly distributed across buckets
   - With non-power-of-2 capacity, some buckets would never be used

3. Efficient resizing:
   - When capacity doubles (power of 2 to power of 2), each entry either stays in the same 
     bucket or moves to (oldIndex + oldCapacity)
   - This is determined by a single bit: the NEW high bit in the new capacity
   - This optimization makes resizing faster and avoids full rehashing

The tableSizeFor() method ensures any requested capacity is rounded up to the nearest power of 2:
   tableSizeFor(10) -> 16
   tableSizeFor(17) -> 32
   tableSizeFor(64) -> 64 (already a power of 2)
```

```java
// Demonstrating the power-of-2 bucket index calculation
int capacity = 16; // 10000 in binary
int mask = capacity - 1; // 01111 in binary

// For hash value 42:
int hash = 42; // 101010 in binary
int index = hash & mask; // 101010 & 01111 = 01010 = 10

// For hash value 58:
hash = 58; // 111010 in binary
index = hash & mask; // 111010 & 01111 = 01010 = 10 (same bucket! collision)

// After resize to 32:
int newCapacity = 32; // 100000 in binary
int newMask = newCapacity - 1; // 011111 in binary
// hash 42: 101010 & 011111 = 001010 = 10 (stays in bucket 10)
// hash 58: 111010 & 011111 = 011010 = 26 (moves to bucket 26 = 10 + 16)
```

**Q4: How does ArrayList grow when it runs out of capacity?**

```text
A4: ArrayList grows by approximately 50% each time it runs out of capacity:

Growth formula (OpenJDK implementation):
   newCapacity = oldCapacity + (oldCapacity >> 1)
   This is equivalent to: newCapacity = oldCapacity * 1.5 (integer arithmetic)

Growth sequence from default capacity 10:
   10 -> 15 -> 22 -> 33 -> 49 -> 73 -> 109 -> 163 -> ...

The growth process:
1. When add() detects that size == capacity, it calls grow()
2. grow() calculates newCapacity = oldCapacity + oldCapacity/2
3. A new array of newCapacity is allocated
4. All existing elements are copied using Arrays.copyOf() (which uses System.arraycopy)
5. The old array becomes eligible for garbage collection

Key differences from HashMap:
- ArrayList grows by ~50% (not doubling)
- No load factor concept - grows only when array is completely full
- No rehashing - just array copy
- ensureCapacity(minCapacity) lets you pre-allocate
- trimToSize() lets you shrink the array to match size

Performance tip: If you know the final size, use the constructor:
   new ArrayList<>(expectedSize)
```

```java
// Default ArrayList: initial capacity 10
ArrayList<Integer> list = new ArrayList<>();
// Growth: 10 -> 15 -> 22 -> 33 -> 49 -> 73 -> ...

// Pre-sized ArrayList: no resizing needed
ArrayList<Integer> preSized = new ArrayList<>(1000);
for (int i = 0; i < 1000; i++) {
    preSized.add(i); // No array copying!
}

// ensureCapacity before bulk insert
ArrayList<String> list2 = new ArrayList<>();
list2.ensureCapacity(5000); // Pre-allocate before large insert
for (int i = 0; i < 5000; i++) {
    list2.add("item" + i);
}

// trimToSize to reclaim memory
ArrayList<String> list3 = new ArrayList<>(10_000);
list3.addAll(List.of("a", "b", "c"));
// Internal array has 10,000 slots but only 3 elements
list3.trimToSize();
// Internal array now has exactly 3 slots
```

**Q5: What are the capacity considerations for ConcurrentHashMap?**

```text
A5: ConcurrentHashMap has a more complex capacity model than HashMap:

Constructor parameters:
1. initialCapacity - hint for the initial table size (default 16)
2. loadFactor - threshold ratio for resizing (default 0.75)
3. concurrencyLevel - estimated number of concurrently updating threads (default 16)

Key differences from HashMap:
- The concurrencyLevel parameter was significant in Java 7 (determined segment count) but in 
  Java 8+ it primarily serves as a sizing hint for initial capacity
- In Java 8+, ConcurrentHashMap uses a single flat table with per-bucket locking
- If initialCapacity < concurrencyLevel, it is rounded up to concurrencyLevel
- The table may be lazily initialized (allocated on first put, not at construction time)

Resizing behavior:
- Resizing is done cooperatively: when a thread detects the table needs resizing, it helps 
  migrate entries from the old table to the new table
- Multiple threads can participate in the migration simultaneously
- New insertions during migration may go into either the old or new table
- The forwarding node mechanism ensures correct routing during migration

Best practice:
- Pre-size for the expected number of entries: new ConcurrentHashMap<>(expectedSize)
- Set concurrencyLevel to the expected number of writer threads
```

```java
// Default ConcurrentHashMap
ConcurrentHashMap<String, Integer> map1 = new ConcurrentHashMap<>();
// initialCapacity=16, loadFactor=0.75, concurrencyLevel=16

// Pre-sized for 1000 entries with 8 writer threads
ConcurrentHashMap<String, Integer> map2 = new ConcurrentHashMap<>(
    1000,   // initialCapacity
    0.75f,  // loadFactor
    8       // concurrencyLevel
);

// Common pattern: sized for known data
List<String> keys = List.of("a", "b", "c", "d", "e");
ConcurrentHashMap<String, Integer> map3 = new ConcurrentHashMap<>(keys.size());
for (int i = 0; i < keys.size(); i++) {
    map3.put(keys.get(i), i);
}

// mappingCount() instead of size() for better accuracy in concurrent contexts
long count = map3.mappingCount();
```

**Q6: How does CopyOnWriteArrayList handle capacity differently from ArrayList?**

```text
A6: CopyOnWriteArrayList has no concept of "spare capacity." Its internal array is ALWAYS exactly 
the size of the number of elements it contains.

Key differences from ArrayList:
1. No initial capacity parameter: The constructor creates an array of exactly the size needed.
2. No growth factor: Every add() creates a new array of size = old.length + 1.
3. No ensureCapacity(): There is no way to pre-allocate space.
4. No trimToSize(): Not needed because the array is always exact-sized.
5. Bulk operations: addAll() creates a new array of size = old.length + elements.length.

Memory implications:
- Every single add() allocates a new array and copies all existing elements
- After add(), the old array becomes garbage (unless held by an existing iterator)
- This is why CopyOnWriteArrayList is expensive for writes: O(n) per mutation

When this design makes sense:
- The list is small (< 100 elements typically)
- Writes are very rare
- Reads (including iteration) dominate
- Thread safety without external locking is required

For large lists or frequent writes, use Collections.synchronizedList(new ArrayList<>()) 
or a ConcurrentLinkedQueue instead.
```

```java
// CopyOnWriteArrayList - array always matches size
CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>();
// Internal array: [] (length 0)

cowList.add("a");
// Internal array: ["a"] (length 1) - allocated new array

cowList.add("b");
// Internal array: ["a", "b"] (length 2) - allocated new array, copied "a"

cowList.add("c");
// Internal array: ["a", "b", "c"] (length 3) - allocated new array, copied "a", "b"

// vs. ArrayList - has spare capacity
ArrayList<String> arrayList = new ArrayList<>();
// Internal array: [] (length 0, lazy init)

arrayList.add("a");
// Internal array: [a, null, null, ..., null] (length 10) - default capacity

arrayList.add("b");
// Internal array: [a, b, null, ..., null] (length 10) - no allocation needed!

// Bulk initialization is more efficient
CopyOnWriteArrayList<String> bulk = new CopyOnWriteArrayList<>(
    List.of("a", "b", "c", "d", "e") // Single array allocation
);
```

## Code Examples

- Test: [CollectionCapacityTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/concurrentcollections/CollectionCapacityTest.java)
- Source: [CollectionCapacity.java](src/main/java/com/github/msorkhpar/claudejavatutor/concurrentcollections/CollectionCapacity.java)
