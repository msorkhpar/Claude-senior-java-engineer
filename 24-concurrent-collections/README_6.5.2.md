# 6.5.2. Thread Safety of Common Data Structures

## Concept Explanation

Thread safety in the context of collections means that a data structure can be safely accessed and modified by multiple
threads concurrently without causing data corruption, inconsistent reads, or unexpected exceptions.

Java provides two primary approaches to making collections thread-safe:

1. **Synchronization wrappers** (`Collections.synchronizedXXX()`): Wrap an existing non-thread-safe collection with a
   synchronized decorator that locks on the wrapper object for every method call.
2. **Concurrent collections** (`java.util.concurrent` package): Purpose-built data structures designed from the ground
   up for concurrent access, using fine-grained locking, CAS operations, or copy-on-write semantics.

**Real-world analogy**: Synchronization wrappers are like putting a single-entry turnstile at a library's front door --
only one person can enter or exit at a time, but the library itself is unchanged. Concurrent collections are like
redesigning the library with multiple help desks, self-checkout stations, and independent reading rooms -- many people
can work simultaneously without interfering with each other.

## Key Points to Remember

1. **No standard collection is thread-safe by default**: `ArrayList`, `HashMap`, `HashSet`, `LinkedList` are all
   non-thread-safe.
2. **`Collections.synchronizedXXX()` wraps collections with coarse-grained locking**: Every method call acquires the
   same lock, creating a bottleneck under contention.
3. **Synchronized wrappers do NOT make iteration thread-safe**: You must manually synchronize on the wrapper during
   iteration.
4. **`ConcurrentHashMap`** uses lock striping and CAS for fine-grained concurrency; it outperforms synchronized HashMap
   by orders of magnitude under contention.
5. **`CopyOnWriteArrayList`** trades write performance for read performance: zero-cost reads, expensive writes.
6. **Legacy synchronized collections** (`Vector`, `Hashtable`) exist but are generally discouraged in favor of modern
   alternatives.
7. **Thread safety != atomic compound operations**: Even with thread-safe collections, "check-then-act" sequences
   (like `if (!map.containsKey(k)) map.put(k, v)`) require atomic methods like `putIfAbsent()`.

## Relevant Java 21 Features

- **Virtual threads (JEP 444)**: Concurrent collections work well with virtual threads. However, be cautious with
  `synchronized` blocks inside virtual thread tasks as they pin the carrier thread; prefer `ReentrantLock` instead.
- **Structured concurrency (Preview)**: When using `StructuredTaskScope`, concurrent collections are often used to
  aggregate results from subtasks.
- **Scoped values (Preview, JEP 446)**: Can replace some uses of ConcurrentHashMap for thread-local data sharing in
  structured concurrency patterns.

## Common Pitfalls and How to Avoid Them

1. **Iterating a synchronized collection without holding the lock**
   ```java
   // BAD: Iterator is not protected by the synchronized wrapper
   List<String> syncList = Collections.synchronizedList(new ArrayList<>());
   syncList.add("a");
   syncList.add("b");
   for (String s : syncList) { // ConcurrentModificationException possible!
       System.out.println(s);
   }

   // FIX: Manually synchronize on the wrapper
   synchronized (syncList) {
       for (String s : syncList) {
           System.out.println(s);
       }
   }
   ```

2. **Assuming synchronized wrappers provide atomic compound operations**
   ```java
   // BAD: Race condition between containsKey and put
   Map<String, Integer> syncMap = Collections.synchronizedMap(new HashMap<>());
   if (!syncMap.containsKey("key")) { // Thread A checks
       syncMap.put("key", 1);         // Thread B might have inserted between check and put
   }

   // FIX: Use ConcurrentHashMap with atomic operations
   ConcurrentHashMap<String, Integer> concMap = new ConcurrentHashMap<>();
   concMap.putIfAbsent("key", 1); // Atomic operation
   ```

3. **Using CopyOnWriteArrayList for write-heavy workloads**
   ```java
   // BAD: Each add copies the entire array - O(n) per write
   CopyOnWriteArrayList<Integer> cowList = new CopyOnWriteArrayList<>();
   for (int i = 0; i < 100_000; i++) {
       cowList.add(i); // 100,000 array copies!
   }

   // FIX: Use synchronizedList or a concurrent queue for write-heavy scenarios
   List<Integer> syncList = Collections.synchronizedList(new ArrayList<>());
   ```

4. **Not using the right level of atomicity**
   ```java
   // BAD: Two separate atomic operations are NOT collectively atomic
   ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
   Integer old = map.get("counter"); // Atomic read
   map.put("counter", old + 1);      // Atomic write, but NOT atomic read-modify-write

   // FIX: Use merge() or compute() for atomic read-modify-write
   map.merge("counter", 1, Integer::sum); // Atomic increment
   ```

## Best Practices and Optimization Techniques

1. **Prefer concurrent collections over synchronized wrappers**: `ConcurrentHashMap` typically offers 10-100x better
   throughput than `Collections.synchronizedMap()` under contention.
2. **Use `ConcurrentHashMap.computeIfAbsent()` for lazy initialization**: It is atomic and avoids double-computation.
3. **Use `CopyOnWriteArrayList` for event listener registries**: The read-heavy, write-rare pattern is a perfect match.
4. **Measure before choosing**: Profile your application's read/write ratio before selecting a concurrent collection.
5. **Use `AtomicInteger`/`AtomicLong` with ConcurrentHashMap** for thread-safe counters.
6. **Avoid holding locks while performing I/O**: This applies to both synchronized wrappers and explicit locks.

## Edge Cases and Their Handling

1. **Empty concurrent collections**: All concurrent collections handle empty state correctly. `ConcurrentHashMap.get()`
   returns null for missing keys; `CopyOnWriteArrayList.iterator()` on an empty list returns an empty iterator.
2. **Single-writer / multiple-reader**: If only one thread writes, `CopyOnWriteArrayList` is optimal. If writes come
   from multiple threads, use `Collections.synchronizedList()` or a `ConcurrentLinkedQueue`.
3. **Size queries**: `ConcurrentHashMap.size()` may return stale values during concurrent modification. Use
   `mappingCount()` for a long-valued count that avoids overflow.
4. **Serialization**: Both synchronized wrappers and concurrent collections are serializable, but be aware that
   deserialization does not restore the synchronized/concurrent wrapper automatically for wrapper-based collections.

## Interview-specific Insights

Interviewers commonly test:
- The difference between synchronized wrappers and concurrent collections (locking granularity, performance)
- Why `ConcurrentHashMap` outperforms `Collections.synchronizedMap()`
- When to use `CopyOnWriteArrayList` vs. `synchronizedList`
- How to avoid the "check-then-act" race condition
- Understanding of `volatile`, `synchronized`, and CAS in the context of collection internals
- Why `Hashtable` and `Vector` are discouraged

## Interview Q&A Section

**Q1: What is the difference between `Collections.synchronizedMap()` and `ConcurrentHashMap`?**

```text
A1: The key differences are:

1. Locking granularity:
   - synchronizedMap: Locks the ENTIRE map for every operation (single monitor lock)
   - ConcurrentHashMap: Locks only individual buckets (lock striping) or uses CAS operations

2. Performance under contention:
   - synchronizedMap: All threads compete for one lock -> severe contention bottleneck
   - ConcurrentHashMap: Multiple threads can read/write different segments simultaneously

3. Iteration safety:
   - synchronizedMap: Iterator is fail-fast; requires external synchronization during iteration
   - ConcurrentHashMap: Weakly consistent iterators; never throws ConcurrentModificationException

4. Null handling:
   - synchronizedMap: Allows null keys and values (delegates to underlying HashMap)
   - ConcurrentHashMap: Does NOT allow null keys or values

5. Atomic compound operations:
   - synchronizedMap: No built-in atomic compound operations (putIfAbsent, compute, merge)
   - ConcurrentHashMap: Provides atomic compute(), merge(), putIfAbsent(), computeIfAbsent()

6. Bulk operations:
   - synchronizedMap: No parallel bulk operations
   - ConcurrentHashMap: forEach(), search(), reduce() with parallelism threshold
```

```java
// Synchronized map - coarse-grained locking
Map<String, Integer> syncMap = Collections.synchronizedMap(new HashMap<>());
syncMap.put("key", 1);
// Must synchronize for iteration
synchronized (syncMap) {
    syncMap.forEach((k, v) -> System.out.println(k + "=" + v));
}

// ConcurrentHashMap - fine-grained locking
ConcurrentHashMap<String, Integer> concMap = new ConcurrentHashMap<>();
concMap.put("key", 1);
// Safe iteration without external synchronization
concMap.forEach((k, v) -> System.out.println(k + "=" + v));
// Atomic compound operations
concMap.merge("key", 1, Integer::sum);
```

**Q2: How do you safely increment a counter in a ConcurrentHashMap?**

```text
A2: There are several approaches, ranked from best to worst:

1. merge() - Most idiomatic for simple increment:
   map.merge("counter", 1, Integer::sum);
   This atomically adds 1 if absent or increments the existing value.

2. compute() - For more complex transformations:
   map.compute("counter", (k, v) -> v == null ? 1 : v + 1);

3. AtomicInteger values - For high-contention counters:
   ConcurrentHashMap<String, AtomicInteger> map = new ConcurrentHashMap<>();
   map.computeIfAbsent("counter", k -> new AtomicInteger(0)).incrementAndGet();

4. LongAdder values - For extremely high contention:
   ConcurrentHashMap<String, LongAdder> map = new ConcurrentHashMap<>();
   map.computeIfAbsent("counter", k -> new LongAdder()).increment();

The LongAdder approach has the highest throughput under extreme contention because it distributes 
the counter across multiple cells to reduce CAS contention.
```

```java
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

// Approach 1: merge (best for simple cases)
map.merge("visits", 1, Integer::sum);
map.merge("visits", 1, Integer::sum);
// map.get("visits") == 2

// Approach 2: compute
map.compute("score", (k, v) -> v == null ? 10 : v + 10);

// Approach 3: AtomicInteger for high-contention
ConcurrentHashMap<String, AtomicInteger> atomicMap = new ConcurrentHashMap<>();
atomicMap.computeIfAbsent("counter", k -> new AtomicInteger(0)).incrementAndGet();

// Approach 4: LongAdder for extreme contention
ConcurrentHashMap<String, java.util.concurrent.atomic.LongAdder> adderMap = new ConcurrentHashMap<>();
adderMap.computeIfAbsent("counter", k -> new java.util.concurrent.atomic.LongAdder()).increment();
long count = adderMap.get("counter").sum();
```

**Q3: When should you use CopyOnWriteArrayList vs. Collections.synchronizedList()?**

```text
A3: The choice depends on the read/write ratio:

CopyOnWriteArrayList:
- Best when: reads vastly outnumber writes (e.g., 1000:1 ratio)
- Reads: Lock-free, O(1) array access, no synchronization overhead
- Writes: O(n) because the entire array is copied
- Iteration: Returns a snapshot; never throws ConcurrentModificationException
- Use cases: Listener lists, configuration caches, security policy lists

Collections.synchronizedList():
- Best when: reads and writes are balanced, or writes are frequent
- Reads: Requires acquiring a lock (monitor)
- Writes: O(1) amortized for add (same as ArrayList)
- Iteration: Must be manually synchronized; throws ConcurrentModificationException on concurrent modification
- Use cases: General-purpose thread-safe lists, write-heavy workloads

Key decision criteria:
- Small list + rare writes -> CopyOnWriteArrayList
- Large list OR frequent writes -> synchronizedList
- Need safe iteration without locking -> CopyOnWriteArrayList
- Need high write throughput -> synchronizedList or ConcurrentLinkedQueue
```

```java
// CopyOnWriteArrayList - for listener registries (read-heavy)
CopyOnWriteArrayList<Runnable> listeners = new CopyOnWriteArrayList<>();
listeners.add(() -> System.out.println("Listener 1"));

// Safe iteration without locks
for (Runnable r : listeners) {
    r.run(); // Another thread can safely call listeners.add() here
}

// synchronizedList - for balanced read/write workloads
List<String> syncList = Collections.synchronizedList(new ArrayList<>());
syncList.add("item1"); // Fast write

// MUST synchronize for iteration
synchronized (syncList) {
    for (String s : syncList) {
        System.out.println(s);
    }
}
```

**Q4: What is wrong with Vector and Hashtable? Why are they discouraged?**

```text
A4: Vector and Hashtable are legacy synchronized collections from Java 1.0 that are discouraged for 
several reasons:

1. Coarse-grained synchronization: Every method is synchronized, even when no contention exists. This 
   causes unnecessary overhead in single-threaded or low-contention scenarios.

2. No atomic compound operations: Like synchronized wrappers, they don't provide atomic putIfAbsent(), 
   compute(), or merge() operations.

3. API design: They expose implementation details (e.g., Vector.capacity(), Hashtable.elements()) 
   and have legacy methods that don't align with the Collections Framework.

4. Iteration: Their Enumeration-based iterators are not fail-fast. Their Collection-based iterators 
   ARE fail-fast but still require external synchronization.

5. Performance: Under contention, they perform similarly to synchronized wrappers (same bottleneck). 
   Modern alternatives like ConcurrentHashMap vastly outperform Hashtable.

Modern replacements:
- Vector -> ArrayList (non-concurrent) or CopyOnWriteArrayList (concurrent)
- Hashtable -> HashMap (non-concurrent) or ConcurrentHashMap (concurrent)
- Stack (extends Vector) -> ArrayDeque
```

```java
// LEGACY - Avoid these
Vector<String> vector = new Vector<>();
Hashtable<String, Integer> hashtable = new Hashtable<>();

// MODERN replacements
// For non-concurrent use:
List<String> list = new ArrayList<>();
Map<String, Integer> map = new HashMap<>();

// For concurrent use:
CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>();
ConcurrentHashMap<String, Integer> concMap = new ConcurrentHashMap<>();

// For stack:
Deque<String> stack = new ArrayDeque<>();
stack.push("item");
String top = stack.pop();
```

**Q5: How can you demonstrate a race condition with a non-thread-safe HashMap?**

```text
A5: A classic race condition with HashMap occurs when multiple threads perform read-modify-write 
operations concurrently:

The problem:
1. Thread A reads counter = 5
2. Thread B reads counter = 5
3. Thread A writes counter = 6
4. Thread B writes counter = 6  (should be 7, but Thread B used stale value)

This results in a "lost update." In extreme cases with HashMap, concurrent structural modifications 
(like rehashing) can cause infinite loops, corrupted data structures, or even thrown exceptions.

Additional dangers of concurrent HashMap access:
- During resize, entries can be lost or duplicated
- The internal linked list/tree structure can become corrupted
- get() can return wrong values or enter infinite loops (in older Java versions)

This is why you must ALWAYS use ConcurrentHashMap or external synchronization when multiple threads 
access a map.
```

```java
// DANGEROUS: Race condition with HashMap
Map<String, Integer> unsafeMap = new HashMap<>();
unsafeMap.put("counter", 0);

ExecutorService executor = Executors.newFixedThreadPool(4);
for (int i = 0; i < 4; i++) {
    executor.submit(() -> {
        for (int j = 0; j < 1000; j++) {
            int current = unsafeMap.get("counter"); // Read
            unsafeMap.put("counter", current + 1);   // Write (race condition!)
        }
    });
}
executor.shutdown();
// Expected: 4000, Actual: likely much less (e.g., 1200, 2500, ...)

// SAFE: Using ConcurrentHashMap.merge()
ConcurrentHashMap<String, Integer> safeMap = new ConcurrentHashMap<>();
safeMap.put("counter", 0);

ExecutorService safeExecutor = Executors.newFixedThreadPool(4);
for (int i = 0; i < 4; i++) {
    safeExecutor.submit(() -> {
        for (int j = 0; j < 1000; j++) {
            safeMap.merge("counter", 1, Integer::sum); // Atomic!
        }
    });
}
safeExecutor.shutdown();
// Always: 4000
```

## Code Examples

- Test: [ThreadSafetyExamplesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/concurrentcollections/ThreadSafetyExamplesTest.java)
- Source: [ThreadSafetyExamples.java](src/main/java/com/github/msorkhpar/claudejavatutor/concurrentcollections/ThreadSafetyExamples.java)
