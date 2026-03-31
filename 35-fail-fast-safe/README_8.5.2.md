# 8.5.2. Applying Fail-Fast and Fail-Safe Principles in Concurrent Collections

## Concept Explanation

Understanding how fail-fast and fail-safe principles apply to Java's concurrent collections is critical for writing correct, thread-safe code. This section covers two sub-topics:

1. **8.5.2.1. Fail-Fast iterators and ConcurrentModificationException** -- how standard collections detect and report structural modifications during iteration.
2. **8.5.2.2. Fail-Safe iterators and snapshot semantics** -- how concurrent collections avoid ConcurrentModificationException using snapshots or weakly consistent traversal.

**Real-world analogy**: Consider a shared whiteboard in an office. With fail-fast behavior, if someone starts reading the whiteboard and another person begins erasing and rewriting it, the reader immediately stops and says "the board changed -- I cannot trust what I am reading." With snapshot (fail-safe) behavior, the reader takes a photo of the whiteboard first and reads from the photo. Modifications to the whiteboard do not affect the photo. With weakly consistent (fail-safe) behavior, the reader is reading the whiteboard directly but tolerates partial changes -- they might see some new content and some old content, but they will not crash or get stuck.

### 8.5.2.1. Fail-Fast Iterators and ConcurrentModificationException

Standard Java collections (those from `java.util`) use fail-fast iterators. The key points are:

- **Single-threaded detection**: The `modCount` mechanism reliably detects modifications within the same thread.
- **Multi-threaded detection**: The mechanism is NOT reliable across threads because `modCount` is not volatile. The specification says "fail-fast iterators throw ConcurrentModificationException on a best-effort basis."
- **Structural vs. non-structural modifications**: Only structural changes (add, remove, clear, resize) increment `modCount`. Changing a value in a `Map` via `put()` for an existing key or calling `set()` on a `ListIterator` are NOT structural modifications and do NOT trigger the exception.
- **for-each loop**: The enhanced for loop (`for (T item : collection)`) uses an iterator behind the scenes, so modifying the collection in a for-each loop triggers the exception.

### 8.5.2.2. Fail-Safe Iterators and Snapshot Semantics

Concurrent collections from `java.util.concurrent` use one of two strategies:

**Snapshot-based (CopyOnWriteArrayList, CopyOnWriteArraySet)**:
- On every write (add, set, remove), the entire internal array is copied.
- Iterators always operate on the array reference captured at iterator creation time.
- Iterators never see modifications made after their creation.
- Iterators do NOT support `remove()`, `set()`, or `add()` -- they throw `UnsupportedOperationException`.
- Ideal for read-heavy, write-rare workloads (e.g., listener registries, configuration lists).

**Weakly consistent (ConcurrentHashMap, ConcurrentSkipListMap, ConcurrentLinkedQueue)**:
- Iterators traverse the actual data structure, not a copy.
- They are guaranteed not to throw `ConcurrentModificationException`.
- They may reflect some, all, or none of the modifications made after their creation.
- They guarantee that each element is returned at most once.
- `ConcurrentHashMap` iterators support `remove()`.

## Key Points to Remember

- `Collections.synchronizedList/Map/Set` wrappers do NOT change the fail-fast iterator contract -- you must still synchronize externally during iteration.
- `CopyOnWriteArrayList` is O(n) on every write because it copies the entire array.
- `ConcurrentHashMap` does not allow null keys or null values (unlike `HashMap`).
- `ConcurrentHashMap.size()` may return an approximation under concurrent modification.
- The `Spliterator` of `ConcurrentHashMap` provides weakly consistent traversal for parallel streams.
- `CopyOnWriteArrayList` iterators do NOT support `remove()` -- this surprises many developers.
- `ConcurrentHashMap.keySet().iterator().remove()` IS supported and works correctly.

## Relevant Java 21 Features

- **Virtual Threads**: With thousands of virtual threads potentially accessing shared collections, choosing the right concurrent collection is more important than ever.
- **Structured Concurrency (JEP 462 Preview)**: Encourages scoped, short-lived concurrent tasks where immutable snapshots or concurrent collections are preferred over shared mutable state.
- **Scoped Values (JEP 464 Preview)**: Provide an alternative to ThreadLocal that pairs well with immutable/snapshot collections.
- **`ConcurrentHashMap` enhancements since Java 8**: Methods like `compute()`, `merge()`, `computeIfAbsent()`, `forEach()`, `reduce()`, `search()` are all designed for concurrent use.

## Common Pitfalls and How to Avoid Them

1. **Using `Collections.synchronizedList` and assuming iteration is safe**
   ```java
   // WRONG -- synchronizedList does NOT protect iteration
   List<String> syncList = Collections.synchronizedList(new ArrayList<>());
   syncList.add("a");
   syncList.add("b");
   
   // Thread 1 iterates
   for (String s : syncList) { // NOT synchronized!
       System.out.println(s);
   }
   // Thread 2 modifies concurrently
   syncList.add("c"); // Can cause ConcurrentModificationException in Thread 1
   
   // FIX -- synchronize on the wrapper during iteration
   synchronized (syncList) {
       for (String s : syncList) {
           System.out.println(s);
       }
   }
   
   // BETTER FIX -- use CopyOnWriteArrayList if reads vastly outnumber writes
   List<String> cowList = new CopyOnWriteArrayList<>();
   ```

2. **Calling remove() on a CopyOnWriteArrayList iterator**
   ```java
   CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>(List.of("a", "b", "c"));
   Iterator<String> it = list.iterator();
   it.next();
   it.remove(); // Throws UnsupportedOperationException!
   
   // FIX -- use the collection's own methods
   list.remove("a"); // Works
   list.removeIf(s -> s.equals("b")); // Works
   ```

3. **Putting null into ConcurrentHashMap**
   ```java
   ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
   map.put("key", null);  // NullPointerException!
   map.put(null, "value"); // NullPointerException!
   
   // FIX -- use sentinel values or Optional
   map.put("key", ""); // Use empty string instead of null
   ```

4. **Assuming ConcurrentHashMap.size() is exact during concurrent modification**
   ```java
   ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
   // ... many threads adding/removing entries
   int size = map.size(); // May be an ESTIMATE, not exact
   
   // FIX -- use mappingCount() for a long return type, but it is still approximate
   long count = map.mappingCount(); // Better for large maps
   ```

5. **Using CopyOnWriteArrayList for write-heavy workloads**
   ```java
   // WRONG -- extremely poor performance with frequent writes
   CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<>();
   for (int i = 0; i < 100000; i++) {
       list.add(i); // Each add copies the entire array: O(n) per add -> O(n^2) total
   }
   
   // FIX -- use a different concurrent collection for write-heavy workloads
   ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();
   for (int i = 0; i < 100000; i++) {
       queue.add(i); // O(1) per add
   }
   ```

## Best Practices and Optimization Techniques

1. **Match the collection to your access pattern**:
   - Read-heavy, write-rare: `CopyOnWriteArrayList`, `CopyOnWriteArraySet`
   - Balanced read/write: `ConcurrentHashMap`, `ConcurrentSkipListMap`
   - Producer-consumer: `ConcurrentLinkedQueue`, `LinkedBlockingQueue`

2. **Use atomic compound operations** on `ConcurrentHashMap`:
   ```java
   map.computeIfAbsent(key, k -> createExpensiveValue(k));
   map.merge(key, 1, Integer::sum);
   ```

3. **Prefer `removeIf()` on concurrent collections** over manual iteration + removal.

4. **Use `List.copyOf()` or `Map.copyOf()`** to create immutable snapshots when sharing data across thread boundaries.

5. **Batch writes to CopyOnWriteArrayList** using `addAll()` instead of multiple `add()` calls to minimize array copies.

## Edge Cases and Their Handling

1. **Iterating a ConcurrentHashMap during bulk putAll()**: The iterator may see some but not all entries from the putAll -- this is expected weakly consistent behavior.
2. **Empty CopyOnWriteArrayList iteration**: Works fine, the snapshot is an empty array.
3. **ConcurrentHashMap with computeIfAbsent that calls itself**: Can cause a deadlock or infinite loop if the mapping function modifies the same map segment.
4. **Concurrent clear() during iteration**: For ConcurrentHashMap, the iterator may still return some elements that existed before clear(). For CopyOnWriteArrayList, the iterator sees the pre-clear snapshot.
5. **Single CopyOnWriteArrayList shared across many readers**: Highly efficient -- all readers share the same array reference until a write occurs.

## Interview-specific Insights

Interviewers typically probe for:
- Understanding the difference between `Collections.synchronizedMap` and `ConcurrentHashMap`
- Knowledge of why `CopyOnWriteArrayList` iterator does not support `remove()`
- Awareness that `ConcurrentHashMap` does not allow nulls (unlike `HashMap`)
- Ability to choose the right concurrent collection for a given use case
- Understanding of "weakly consistent" guarantees

Common follow-up questions:
- "What happens if two threads iterate and modify a HashMap simultaneously?"
- "How does ConcurrentHashMap achieve lock-free reads?"
- "When would you choose CopyOnWriteArrayList over a synchronized list?"

## Interview Q&A Section

**Q1: What happens if you modify a HashMap from two threads simultaneously without synchronization?**

```text
A1: Modifying a HashMap from two threads without synchronization leads to undefined behavior.
The possible outcomes include:

1. ConcurrentModificationException -- if one thread iterates while another modifies. However,
   this is NOT guaranteed because modCount is not volatile.
2. Lost updates -- both threads may write to the same bucket, and one write overwrites the other.
3. Infinite loop -- prior to Java 8, concurrent resize operations could cause linked list cycles,
   leading to infinite loops in get() or put(). Java 8 mitigated this with tree bins, but the
   behavior is still undefined.
4. Corrupted internal state -- the HashMap's size, threshold, or bucket structure may become
   inconsistent.
5. Partially visible entries -- one thread may see a key with a stale or null value.

The correct solution is to use ConcurrentHashMap for concurrent access, or to synchronize all
access to the HashMap externally.
```

```java
// DANGEROUS -- undefined behavior
HashMap<String, Integer> unsafeMap = new HashMap<>();
// Thread 1
new Thread(() -> {
    for (int i = 0; i < 10000; i++) {
        unsafeMap.put("key" + i, i); // Concurrent modification
    }
}).start();
// Thread 2
new Thread(() -> {
    for (int i = 0; i < 10000; i++) {
        unsafeMap.put("key" + i, i * 2); // Concurrent modification
    }
}).start();

// SAFE -- use ConcurrentHashMap
ConcurrentHashMap<String, Integer> safeMap = new ConcurrentHashMap<>();
```

**Q2: How does CopyOnWriteArrayList achieve thread safety?**

```text
A2: CopyOnWriteArrayList achieves thread safety through a "copy-on-write" strategy:

1. The internal array is stored as a volatile reference. All reads go directly to this array
   without synchronization, making reads extremely fast.

2. Every write operation (add, set, remove, clear) acquires a ReentrantLock, creates a new
   copy of the entire internal array with the modification applied, and then atomically
   replaces the array reference.

3. Because the array reference is volatile, all threads immediately see the new array after
   a write. The old array is not modified, so any existing iterators continue to work on
   the old (pre-modification) array.

4. Iterators snapshot the array reference at creation time and traverse that array. Since the
   array is never mutated in place, iterators are inherently thread-safe without any locking.

Performance implications:
- Reads: O(1) with no locking overhead
- Writes: O(n) because the entire array is copied
- Iteration: Lock-free, based on a snapshot
- Memory: Can temporarily have multiple copies of the array in memory

This makes CopyOnWriteArrayList ideal for scenarios where:
- Reads vastly outnumber writes (e.g., listener lists, observer pattern, configuration)
- Iteration must be fast and never fail
- The list is relatively small
```

```java
// How CopyOnWriteArrayList works (simplified):
public class SimplifiedCOWList<E> {
    private volatile Object[] array; // Volatile for visibility
    private final ReentrantLock lock = new ReentrantLock();

    public E get(int index) {
        return (E) array[index]; // No locking needed!
    }

    public void add(E element) {
        lock.lock();
        try {
            Object[] oldArray = array;
            Object[] newArray = Arrays.copyOf(oldArray, oldArray.length + 1);
            newArray[oldArray.length] = element;
            array = newArray; // Atomic volatile write
        } finally {
            lock.unlock();
        }
    }

    public Iterator<E> iterator() {
        return new COWIterator<>(array); // Snapshot of current array
    }
}
```

**Q3: Why does ConcurrentHashMap not allow null keys or values?**

```text
A3: ConcurrentHashMap prohibits null keys and null values for a fundamental design reason
related to ambiguity in concurrent contexts:

The problem: In a concurrent map, if get(key) returns null, you cannot distinguish between:
a) The key is not in the map (mapping does not exist)
b) The key is mapped to null (mapping exists with null value)

In a single-threaded HashMap, you can resolve this ambiguity by calling containsKey():
  if (map.containsKey(key)) { /* mapped to null */ }
  else { /* not present */ }

But in a concurrent context, another thread could modify the map between your containsKey()
and get() calls, making the two-step check unreliable:
  if (map.containsKey(key)) {  // true at this moment
      // Another thread removes the key HERE
      map.get(key); // Returns null -- but now the key doesn't exist!
  }

By prohibiting null, ConcurrentHashMap ensures that:
- get(key) returning null unambiguously means "key not present"
- putIfAbsent(key, value) works correctly
- compute() and merge() methods have clear semantics
- The map's atomic compound operations remain unambiguous

This is a deliberate design choice by Doug Lea (the author of java.util.concurrent).
```

```java
// HashMap allows null -- ambiguous in concurrent scenarios
HashMap<String, String> hashMap = new HashMap<>();
hashMap.put("key", null);     // OK
hashMap.put(null, "value");   // OK

// ConcurrentHashMap does not allow null -- unambiguous
ConcurrentHashMap<String, String> concMap = new ConcurrentHashMap<>();
// concMap.put("key", null);  // NullPointerException
// concMap.put(null, "value"); // NullPointerException

// Use Optional or sentinel values as alternatives
ConcurrentHashMap<String, Optional<String>> mapWithOptional = new ConcurrentHashMap<>();
mapWithOptional.put("key", Optional.empty()); // Represents "mapped but no value"
mapWithOptional.put("key2", Optional.of("value")); // Represents "mapped with value"
```

**Q4: What is the difference between Collections.synchronizedMap and ConcurrentHashMap?**

```text
A4: While both provide thread-safe map access, they differ fundamentally:

Collections.synchronizedMap:
- Wraps an existing map with synchronized blocks on EVERY method call
- Uses a single lock (the wrapper object itself) -- only one thread can access the map at a time
- Still uses fail-fast iterators -- you must synchronize externally during iteration
- Does not provide atomic compound operations (check-then-act is NOT atomic)
- Simple wrapper with O(1) overhead per operation but poor concurrency

ConcurrentHashMap:
- Purpose-built concurrent data structure
- Uses fine-grained locking (lock striping) or CAS (compare-and-swap) operations
- Multiple threads can read AND write concurrently to different segments
- Provides atomic compound operations: putIfAbsent, compute, merge, replace
- Uses weakly consistent iterators that never throw ConcurrentModificationException
- Does not allow null keys or values
- Provides bulk operations: forEach, reduce, search (with parallelism threshold)

Performance comparison:
- Single thread: synchronizedMap is slightly faster (no CAS overhead)
- Multiple readers: ConcurrentHashMap is dramatically faster (no locking for reads)
- Multiple writers: ConcurrentHashMap is significantly faster (concurrent writes allowed)
- Mixed workload: ConcurrentHashMap scales far better as thread count increases

Recommendation: Almost always prefer ConcurrentHashMap for concurrent access. Use
synchronizedMap only if you need null key/value support or are wrapping a specialized map.
```

```java
// Collections.synchronizedMap -- single lock, fail-fast iteration
Map<String, Integer> syncMap = Collections.synchronizedMap(new HashMap<>());
syncMap.put("a", 1);

// Must synchronize externally for iteration!
synchronized (syncMap) {
    for (var entry : syncMap.entrySet()) {
        // safe inside synchronized block
    }
}

// check-then-act is NOT atomic
if (!syncMap.containsKey("b")) { // Thread 2 could add "b" HERE
    syncMap.put("b", 2);         // RACE CONDITION
}

// ConcurrentHashMap -- fine-grained locking, weakly consistent iteration
ConcurrentHashMap<String, Integer> concMap = new ConcurrentHashMap<>();
concMap.put("a", 1);

// No external synchronization needed for iteration
for (var entry : concMap.entrySet()) {
    // weakly consistent -- safe without synchronization
}

// Atomic compound operation -- no race condition
concMap.putIfAbsent("b", 2); // Atomic check-and-put
concMap.compute("a", (k, v) -> v + 1); // Atomic read-modify-write
```

**Q5: When should you use CopyOnWriteArrayList vs. a synchronized list vs. ConcurrentLinkedQueue?**

```text
A5: The choice depends on your access pattern and requirements:

CopyOnWriteArrayList -- use when:
- Reads vastly outnumber writes (95%+ reads)
- The list is relatively small (hundreds of elements, not millions)
- Iteration must be fast and never fail
- Random access by index is needed
- Use cases: listener lists, observer pattern, configuration data, ACL lists

Collections.synchronizedList(ArrayList) -- use when:
- Read/write ratio is balanced
- You need null element support
- The list is large and write-heavy
- You don't mind manual synchronization during iteration
- Use cases: legacy code migration, simple thread-safe wrappers

ConcurrentLinkedQueue -- use when:
- You need a thread-safe queue (FIFO order)
- Producer-consumer patterns
- Both reads and writes are frequent
- You don't need random access by index
- Lock-free algorithm for high throughput
- Use cases: task queues, event queues, work stealing

Other options to consider:
- ConcurrentLinkedDeque: double-ended queue for work-stealing algorithms
- LinkedBlockingQueue: blocking producer-consumer with capacity bounds
- ConcurrentSkipListSet: sorted concurrent set
```

```java
// Listener registry -- read-heavy, perfect for CopyOnWriteArrayList
CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();
listeners.add(new MyListener()); // Rare
// Fire event to all listeners -- very frequent, lock-free iteration
for (EventListener listener : listeners) {
    listener.onEvent(event); // No ConcurrentModificationException possible
}

// Shared work queue -- producer-consumer pattern
ConcurrentLinkedQueue<Task> taskQueue = new ConcurrentLinkedQueue<>();
// Producers
taskQueue.offer(new Task("work1")); // Lock-free enqueue
// Consumers
Task task = taskQueue.poll(); // Lock-free dequeue

// General-purpose synchronized list
List<String> syncList = Collections.synchronizedList(new ArrayList<>());
syncList.add("item"); // Synchronized add
synchronized (syncList) { // Must synchronize for iteration
    for (String s : syncList) {
        process(s);
    }
}
```

**Q6: How do parallel streams interact with fail-fast and fail-safe collections?**

```text
A6: Parallel streams split the source collection into segments and process them in parallel
using the ForkJoinPool. The interaction with fail-fast/fail-safe depends on the source:

Fail-fast collections (ArrayList, HashMap):
- The stream's Spliterator captures the modCount at creation time.
- If the source collection is modified during stream processing, the Spliterator may throw
  ConcurrentModificationException -- just like regular iterators.
- This means you must NOT modify the source collection during a parallel stream operation.

Fail-safe/concurrent collections (ConcurrentHashMap):
- Their Spliterators are weakly consistent.
- Modifications during stream processing do not throw exceptions.
- The stream may process some, all, or none of the concurrently added elements.
- ConcurrentHashMap has a special parallel forEach/reduce/search API with a
  parallelismThreshold parameter for fine-grained control.

Best practices for parallel streams:
1. Never modify the source collection during stream processing.
2. Use concurrent collections if modifications are unavoidable.
3. Prefer collecting results into a new collection rather than modifying the source.
4. Use ConcurrentHashMap's built-in parallel methods when possible -- they are optimized
   for the map's internal structure.
```

```java
// WRONG -- modifying source during parallel stream
List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4, 5));
list.parallelStream()
    .filter(n -> n > 2)
    .forEach(n -> list.remove(n)); // ConcurrentModificationException!

// CORRECT -- collect into new collection
List<Integer> filtered = list.parallelStream()
    .filter(n -> n <= 2)
    .collect(Collectors.toList());

// ConcurrentHashMap parallel operations
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
map.put("a", 1); map.put("b", 2); map.put("c", 3);

// Built-in parallel forEach with threshold
map.forEach(2, (key, value) -> {
    System.out.println(key + "=" + value);
});

// Built-in parallel reduce
int sum = map.reduceValues(2, Integer::sum);
```

## Code Examples

- Test: [FailFastIteratorDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/failfastsafe/FailFastIteratorDemoTest.java)
- Source: [FailFastIteratorDemo.java](src/main/java/com/github/msorkhpar/claudejavatutor/failfastsafe/FailFastIteratorDemo.java)
- Test: [FailSafeIteratorDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/failfastsafe/FailSafeIteratorDemoTest.java)
- Source: [FailSafeIteratorDemo.java](src/main/java/com/github/msorkhpar/claudejavatutor/failfastsafe/FailSafeIteratorDemo.java)
