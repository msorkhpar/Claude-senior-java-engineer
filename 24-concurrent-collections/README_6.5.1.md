# 6.5.1. Common Collections and Their Implementations

## Concept Explanation

Java's Collections Framework provides a rich set of data structures, each designed for specific use cases. Understanding
the characteristics, performance trade-offs, and thread-safety guarantees of each collection is essential for writing
efficient, correct Java code.

**Real-world analogy**: Think of collections like different types of containers in a warehouse. An **ArrayList** is like
a numbered shelf where you can quickly find items by position but inserting in the middle requires shifting everything.
A **LinkedList** is like a chain of boxes where adding/removing from either end is fast but finding a specific box
requires walking the chain. A **CopyOnWriteArrayList** is like a whiteboard that gets photocopied before any changes --
readers always see a consistent snapshot while the writer works on the original.

### 6.5.1.1. List Implementations

| Implementation         | Random Access | Insertion (middle) | Thread-Safe | Null Elements | Iterator Behavior     |
|------------------------|---------------|--------------------|-------------|---------------|-----------------------|
| ArrayList              | O(1)          | O(n)               | No          | Yes           | Fail-fast             |
| LinkedList             | O(n)          | O(1) at position   | No          | Yes           | Fail-fast             |
| CopyOnWriteArrayList   | O(1)          | O(n) (full copy)   | Yes         | Yes           | Snapshot (weakly consistent) |

### 6.5.1.2. Set Implementations

| Implementation           | Ordering     | Lookup    | Thread-Safe | Null Elements | Iterator Behavior     |
|--------------------------|-------------|-----------|-------------|---------------|-----------------------|
| HashSet                  | None        | O(1) avg  | No          | Yes (one)     | Fail-fast             |
| ConcurrentSkipListSet    | Sorted      | O(log n)  | Yes         | No            | Weakly consistent     |

### 6.5.1.3. Map Implementations

| Implementation       | Ordering     | Lookup    | Thread-Safe | Null Key/Value | Iterator Behavior     |
|----------------------|-------------|-----------|-------------|----------------|-----------------------|
| HashMap              | None        | O(1) avg  | No          | Yes / Yes      | Fail-fast             |
| ConcurrentHashMap    | None        | O(1) avg  | Yes         | No / No        | Weakly consistent     |

## Key Points to Remember

1. **ArrayList** is the go-to general-purpose list: backed by an array, O(1) random access, O(n) insertion/removal in
   the middle.
2. **LinkedList** implements both `List` and `Deque`: ideal for queue/deque usage, O(1) add/remove at head/tail, but
   O(n) random access.
3. **CopyOnWriteArrayList** creates a fresh copy of the underlying array on every mutation: ideal for read-heavy
   workloads with rare writes.
4. **HashSet** uses a **HashMap** internally; it does not preserve insertion order (use `LinkedHashSet` if you need
   that).
5. **ConcurrentSkipListSet** is a thread-safe, sorted set backed by a skip list; it does **not** allow null elements.
6. **HashMap** offers O(1) average lookup but degrades to O(log n) when many keys collide (Java 8+ tree bins).
7. **ConcurrentHashMap** uses fine-grained locking (lock striping) for high-throughput concurrent access; it does
   **not** allow null keys or values.
8. All non-concurrent collections are **not** thread-safe by default.

## Relevant Java 21 Features

- **Sequenced Collections (JEP 431)**: Java 21 introduced `SequencedCollection`, `SequencedSet`, and `SequencedMap`
  interfaces that provide uniform access to the first and last elements and a reversed view. `ArrayList`, `LinkedList`,
  and `LinkedHashSet` implement these.
- **`List.of()` / `Set.of()` / `Map.of()` factory methods** (Java 9+): create unmodifiable collections.
- **`toList()` terminal operation** on streams (Java 16+): returns an unmodifiable list.
- **Virtual threads (Java 21)**: ConcurrentHashMap and CopyOnWriteArrayList work well with virtual threads since they
  avoid long-held locks.

## Common Pitfalls and How to Avoid Them

1. **Using ArrayList for frequent insertions/removals in the middle**
   ```java
   // BAD: O(n) for each removal
   List<String> list = new ArrayList<>(List.of("a", "b", "c", "d"));
   list.remove(0); // Shifts all elements left

   // BETTER: Use LinkedList or filter with streams
   List<String> filtered = list.stream().skip(1).toList();
   ```

2. **Assuming HashSet preserves insertion order**
   ```java
   // BAD: Order may differ from insertion order
   Set<String> set = new HashSet<>();
   set.add("banana"); set.add("apple"); set.add("cherry");
   // Iteration order is NOT guaranteed to be banana, apple, cherry

   // FIX: Use LinkedHashSet for insertion-order preservation
   Set<String> ordered = new LinkedHashSet<>();
   ```

3. **Putting null into ConcurrentHashMap or ConcurrentSkipListSet**
   ```java
   // THROWS NullPointerException
   ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
   map.put("key", null); // NPE!

   // FIX: Use Optional or a sentinel value
   map.put("key", "");
   ```

4. **Using CopyOnWriteArrayList for write-heavy workloads**
   ```java
   // BAD: Every add copies the entire array
   CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<>();
   for (int i = 0; i < 100_000; i++) {
       list.add(i); // Each add allocates a new array
   }

   // BETTER: Use Collections.synchronizedList or a concurrent queue
   ```

## Best Practices and Optimization Techniques

1. **Choose the right collection for your access pattern**: Random access -> ArrayList; Queue/Deque -> LinkedList or
   ArrayDeque; Thread-safe reads -> CopyOnWriteArrayList.
2. **Pre-size collections** when the number of elements is known to avoid resizing overhead.
3. **Use `ConcurrentHashMap.computeIfAbsent()`** for lazy initialization in concurrent contexts.
4. **Prefer `ArrayDeque` over `LinkedList`** for stack/queue usage -- it has less memory overhead and better cache
   locality.
5. **Use `Map.of()` / `List.of()`** for small, immutable collections.

## Edge Cases and Their Handling

1. **Null elements**: ArrayList, LinkedList, HashSet, HashMap all accept null. ConcurrentHashMap,
   ConcurrentSkipListSet, and CopyOnWriteArrayList (for `addIfAbsent`) reject null.
2. **Empty collections**: All collections handle empty state gracefully; check with `isEmpty()` before calling
   `get(0)` on lists.
3. **Single-element collections**: Use `Collections.singletonList()` or `List.of(element)` for immutable
   single-element collections.
4. **Concurrent iteration**: Non-concurrent collections throw `ConcurrentModificationException`; concurrent collections
   provide weakly consistent iterators.

## Interview-specific Insights

Interviewers frequently ask about:
- When to use ArrayList vs. LinkedList (answer: almost always ArrayList due to cache locality)
- The difference between HashMap and ConcurrentHashMap (null handling, thread safety, performance)
- Why ConcurrentHashMap does not allow null keys/values (ambiguity: does `get()` returning null mean absent or null value?)
- Internal structure of HashMap (array of buckets, linked list / tree for collisions)
- Time complexity of common operations across different collection types

## Interview Q&A Section

**Q1: When should you use ArrayList vs. LinkedList?**

```text
A1: In almost all cases, ArrayList is preferred over LinkedList because:

1. Cache locality: ArrayList's contiguous memory layout is CPU-cache-friendly, making sequential access 
   significantly faster in practice.
2. Random access: ArrayList provides O(1) access by index; LinkedList is O(n).
3. Memory overhead: Each LinkedList node carries two extra pointers (prev/next), roughly 3x the memory 
   overhead per element compared to ArrayList.

LinkedList is better ONLY when:
- You need constant-time insertions/removals at both ends (use as Deque)
- You never need random access by index
- You need to remove elements during iteration via ListIterator

In modern Java, ArrayDeque is generally preferred over LinkedList even for queue/deque usage.
```

```java
// ArrayList - preferred for most use cases
List<String> arrayList = new ArrayList<>();
arrayList.add("fast");           // O(1) amortized
String item = arrayList.get(0);  // O(1) random access

// LinkedList as Deque - valid use case
Deque<String> deque = new LinkedList<>();
deque.addFirst("front");  // O(1)
deque.addLast("back");    // O(1)
String first = deque.pollFirst(); // O(1)

// ArrayDeque - usually better than LinkedList for queue/stack
Deque<String> better = new ArrayDeque<>();
better.push("stack");
better.offer("queue");
```

**Q2: Why does ConcurrentHashMap not allow null keys or values?**

```text
A2: ConcurrentHashMap prohibits null keys and values to avoid ambiguity in concurrent contexts.

With a regular HashMap, when map.get(key) returns null, you can call map.containsKey(key) to distinguish 
between "key is absent" and "key maps to null." But in a concurrent environment, between the get() and 
containsKey() calls, another thread could modify the map, making the check unreliable.

This is called the "check-then-act" race condition. By disallowing null values, ConcurrentHashMap 
ensures that get() returning null unambiguously means "key not present," and all single-method 
operations (like putIfAbsent, computeIfAbsent) can be safely atomic.

Doug Lea (the author) explicitly chose this design to prevent subtle bugs in concurrent code.
```

```java
// HashMap allows null - fine in single-threaded contexts
HashMap<String, String> hashMap = new HashMap<>();
hashMap.put("key", null);
if (hashMap.containsKey("key")) {
    // We know the key exists but maps to null
}

// ConcurrentHashMap rejects null
ConcurrentHashMap<String, String> concMap = new ConcurrentHashMap<>();
// concMap.put("key", null);  // Throws NullPointerException!
// concMap.put(null, "value"); // Throws NullPointerException!

// Safe alternative: use a sentinel value or Optional
concMap.put("key", ""); // Empty string as sentinel
```

**Q3: What is CopyOnWriteArrayList and when would you use it?**

```text
A3: CopyOnWriteArrayList is a thread-safe variant of ArrayList where every mutative operation 
(add, set, remove) creates a fresh copy of the underlying array.

When to use it:
1. Read-heavy, write-rare scenarios (e.g., listener/observer lists, configuration caches)
2. When you need safe iteration without external synchronization
3. When the dataset is small enough that copying the array is acceptable

Key characteristics:
- Reads (get, iteration) are lock-free and very fast
- Writes are expensive: O(n) time and memory for each mutation
- Iterators operate on a snapshot and never throw ConcurrentModificationException
- Iterator.remove() throws UnsupportedOperationException (snapshot is immutable)

Real-world examples:
- Event listener registries in GUI frameworks
- Security policy lists that rarely change
- Configuration lists read by many threads
```

```java
// Typical use case: event listener registry
CopyOnWriteArrayList<Runnable> listeners = new CopyOnWriteArrayList<>();

// Registration (rare) - copies the array
listeners.add(() -> System.out.println("Event fired!"));

// Notification (frequent) - iterates over snapshot, no locking
for (Runnable listener : listeners) {
    listener.run(); // Safe even if another thread modifies the list
}

// addIfAbsent prevents duplicates
listeners.addIfAbsent(() -> System.out.println("Unique listener"));
```

**Q4: How does ConcurrentSkipListSet differ from a synchronized TreeSet?**

```text
A4: ConcurrentSkipListSet is a lock-free, thread-safe, sorted set implementation based on a skip list 
data structure. It differs from a synchronized TreeSet in several important ways:

1. Concurrency: ConcurrentSkipListSet uses lock-free algorithms (CAS operations), allowing multiple 
   threads to read and write simultaneously without blocking. A synchronized TreeSet locks the entire 
   structure for every operation.

2. Scalability: ConcurrentSkipListSet scales much better under contention because operations on 
   different parts of the skip list don't interfere with each other.

3. Iteration: ConcurrentSkipListSet provides weakly consistent iterators that never throw 
   ConcurrentModificationException. A synchronized TreeSet's iterator requires external synchronization.

4. Performance: Individual operations on ConcurrentSkipListSet are O(log n) like TreeSet, but under 
   high concurrency, the throughput is significantly higher.

5. Null handling: ConcurrentSkipListSet does NOT allow null elements (throws NullPointerException). 
   TreeSet allows null only if the comparator handles it.

6. Navigation: Both support NavigableSet operations (headSet, tailSet, subSet, floor, ceiling).
```

```java
// ConcurrentSkipListSet - thread-safe, sorted, no locking needed
ConcurrentSkipListSet<String> skipSet = new ConcurrentSkipListSet<>();
skipSet.add("banana");
skipSet.add("apple");
skipSet.add("cherry");
// Always iterates in sorted order: apple, banana, cherry

// Range operations
NavigableSet<String> subset = skipSet.subSet("apple", true, "cherry", false);
// Returns: [apple, banana]

// vs. synchronized TreeSet - requires external locking for iteration
Set<String> syncTree = Collections.synchronizedSet(new TreeSet<>());
synchronized (syncTree) {
    for (String s : syncTree) {
        // Must hold lock during entire iteration
    }
}
```

**Q5: What are the internal mechanics of HashMap?**

```text
A5: HashMap is an array-based hash table that stores key-value pairs in "buckets."

Internal structure:
1. An array of Node objects (called "table"), where each Node contains: hash, key, value, next pointer.
2. Default initial capacity: 16 buckets. Default load factor: 0.75.
3. When the number of entries exceeds capacity * loadFactor (the "threshold"), the table doubles in size 
   (rehashing all entries).

Collision handling (Java 8+):
- When multiple keys hash to the same bucket, they form a linked list.
- If a bucket's linked list exceeds 8 nodes AND the table has >= 64 buckets, the list is converted to 
  a balanced red-black tree (O(log n) lookup instead of O(n)).
- If the tree shrinks below 6 nodes, it converts back to a linked list.

Hash computation:
- HashMap applies a supplementary hash function that XORs the upper and lower 16 bits of hashCode():
  hash = (h = key.hashCode()) ^ (h >>> 16)
- This reduces collisions when many keys have similar lower bits.

The bucket index is computed as: index = hash & (capacity - 1), which works because capacity is always 
a power of 2.
```

```java
// Creating HashMap with custom capacity and load factor
Map<String, Integer> map = new HashMap<>(32, 0.75f);

// merge() for atomic-like operations
map.merge("key", 1, Integer::sum); // Adds 1 if absent, increments if present

// computeIfAbsent() for lazy initialization
Map<String, List<Integer>> groups = new HashMap<>();
groups.computeIfAbsent("group1", k -> new ArrayList<>()).add(42);

// HashMap supports null key (stored at index 0)
map.put(null, 0);
Integer nullValue = map.get(null); // Returns 0
```

**Q6: How does ConcurrentHashMap achieve thread safety without locking the entire map?**

```text
A6: ConcurrentHashMap uses several techniques for fine-grained concurrency:

Java 8+ implementation (current):
1. Lock striping: Instead of locking the entire table, it locks individual buckets (segments) using 
   synchronized blocks on the first node of each bucket.
2. CAS operations: Many operations (like adding to an empty bucket) use Compare-And-Swap (CAS) 
   atomic operations, avoiding locks entirely.
3. Volatile reads: The table array and node values use volatile semantics, ensuring visibility across 
   threads without locking.

Bulk operations (Java 8+):
- forEach, search, reduce with a parallelism threshold parameter
- When threshold is 1, operations run in the ForkJoinPool.commonPool()
- These operations are designed for concurrent traversal

Key guarantees:
- Individual operations (get, put, remove) are thread-safe and atomic
- Compound operations (putIfAbsent, computeIfAbsent, merge) are also atomic
- Iteration is weakly consistent: reflects some concurrent modifications
- size() and isEmpty() are approximations under concurrent modification
```

```java
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

// Atomic compound operations
map.putIfAbsent("counter", 0);
map.merge("counter", 1, Integer::sum); // Atomic increment

// Bulk operations with parallelism threshold
map.put("a", 10); map.put("b", 20); map.put("c", 30);

// Parallel forEach (threshold=1 means always parallelize)
map.forEach(1, (key, value) -> System.out.println(key + "=" + value));

// Parallel reduce
long sum = map.reduceValuesToLong(1, Integer::longValue, 0L, Long::sum);

// Parallel search
String found = map.search(1, (key, value) -> value > 15 ? key : null);
```

## Code Examples

- Test: [CommonCollectionsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/concurrentcollections/CommonCollectionsTest.java)
- Source: [CommonCollections.java](src/main/java/com/github/msorkhpar/claudejavatutor/concurrentcollections/CommonCollections.java)
