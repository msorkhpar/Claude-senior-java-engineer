# 6.5.3. Order of Operations in Concurrent Collections

## Concept Explanation

When working with collections in Java, understanding how iteration interacts with concurrent modification is critical.
Non-concurrent collections use **fail-fast** iterators that detect structural modifications and throw
`ConcurrentModificationException`. Concurrent collections use **weakly consistent** iterators that tolerate
modifications during iteration but may or may not reflect them.

**Real-world analogy**: Imagine a museum tour guide leading a group through exhibits. A **fail-fast** guide immediately
stops the tour if someone rearranges exhibits during the walk ("Something changed! Tour cancelled!"). A **weakly
consistent** guide continues the tour undisturbed -- visitors might see some new exhibits that were added during the
tour, or might not see exhibits that were removed, but the tour never crashes.

### 6.5.3.1. Iteration and Modification in Non-Concurrent Collections

Non-concurrent collections (ArrayList, HashMap, HashSet) maintain an internal **modification count** (`modCount`).
When an iterator is created, it records the current `modCount`. On each call to `next()`, the iterator checks if
`modCount` has changed. If it has, the iterator throws `ConcurrentModificationException`.

This is a **best-effort** detection mechanism -- it is not guaranteed to catch every concurrent modification, especially
in multi-threaded scenarios.

### 6.5.3.2. Fail-Fast Behavior and ConcurrentModificationException

Fail-fast iterators:
- Detect structural modifications (add, remove, clear) but NOT value modifications (set on an existing key)
- Throw `ConcurrentModificationException` as soon as they detect a modification
- Work in both single-threaded (modifying collection during for-each) and multi-threaded contexts
- Are NOT guaranteed to fire in all cases -- they are best-effort

### 6.5.3.3. Concurrent Collections and Their Iteration Guarantees

Concurrent collections provide **weakly consistent** iterators:
- **CopyOnWriteArrayList**: Iterator operates on a frozen snapshot taken at iterator creation time. Changes after
  that point are invisible to the iterator.
- **ConcurrentHashMap**: Iterator may reflect some modifications that occur after its creation. It guarantees that
  each element is returned at most once and that no `ConcurrentModificationException` is thrown.
- **ConcurrentSkipListSet**: Similar to ConcurrentHashMap -- weakly consistent, sorted order maintained.

## Key Points to Remember

1. **Fail-fast is best-effort**: Do not write code that depends on catching `ConcurrentModificationException` for
   correctness; use it only for debugging.
2. **`Iterator.remove()` is the ONLY safe way** to remove elements during iteration of non-concurrent collections.
3. **`removeIf()` (Java 8+)** is the preferred modern approach for conditional removal during iteration.
4. **CopyOnWriteArrayList iterators do NOT support `remove()`**: They throw `UnsupportedOperationException`.
5. **Weakly consistent != strongly consistent**: Concurrent collection iterators may miss newly added elements or
   include recently removed ones.
6. **For-each loops** use iterators internally, so they are subject to the same fail-fast rules.
7. **Stream operations** on non-concurrent collections are also subject to `ConcurrentModificationException` if the
   source is modified during stream processing.

## Relevant Java 21 Features

- **`List.of()` / `Set.of()` / `Map.of()`** return unmodifiable collections whose iterators never throw
  `ConcurrentModificationException` because they cannot be structurally modified.
- **Stream `toList()`** (Java 16+) returns an unmodifiable list, avoiding modification-during-iteration issues.
- **Pattern matching in switch** (Java 21) can be used with sealed collection wrapper types for type-safe iteration
  patterns.

## Common Pitfalls and How to Avoid Them

1. **Modifying a collection inside a for-each loop**
   ```java
   // BAD: Throws ConcurrentModificationException
   List<String> list = new ArrayList<>(List.of("a", "b", "c"));
   for (String s : list) {
       if (s.equals("b")) {
           list.remove(s); // Structural modification during iteration!
       }
   }

   // FIX 1: Use Iterator.remove()
   Iterator<String> it = list.iterator();
   while (it.hasNext()) {
       if (it.next().equals("b")) {
           it.remove();
       }
   }

   // FIX 2: Use removeIf() (preferred)
   list.removeIf(s -> s.equals("b"));
   ```

2. **Assuming CopyOnWriteArrayList iterator reflects live changes**
   ```java
   // The iterator works on a SNAPSHOT
   CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>(List.of("a", "b"));
   Iterator<String> it = cowList.iterator();
   cowList.add("c"); // Added AFTER iterator creation
   while (it.hasNext()) {
       System.out.println(it.next()); // Prints "a", "b" - NOT "c"
   }
   ```

3. **Trying to use Iterator.remove() on CopyOnWriteArrayList**
   ```java
   // BAD: Throws UnsupportedOperationException
   CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>(List.of("a", "b"));
   Iterator<String> it = cowList.iterator();
   it.next();
   it.remove(); // UnsupportedOperationException!

   // FIX: Use the list's own remove method or removeIf
   cowList.remove("a");
   ```

4. **Relying on ConcurrentModificationException for control flow**
   ```java
   // BAD: Using exception for flow control
   try {
       for (String s : list) {
           if (shouldRemove(s)) list.remove(s);
       }
   } catch (ConcurrentModificationException e) {
       // "Handle" the situation - this is terrible practice
   }

   // FIX: Use proper removal techniques
   list.removeIf(this::shouldRemove);
   ```

## Best Practices and Optimization Techniques

1. **Use `removeIf()` for conditional removal**: It is both safe and efficient (avoids intermediate copies).
2. **Use `List.copyOf()` or `new ArrayList<>(original)` before iterating** if you need to modify the original
   concurrently.
3. **For ConcurrentHashMap, use `forEach()`, `compute()`, `merge()`** instead of iterating and modifying.
4. **Use immutable collections** (`List.of()`, `Collections.unmodifiableList()`) when iteration safety is paramount.
5. **Use streams** for filtering and transformation instead of in-place modification.

## Edge Cases and Their Handling

1. **Modifying the last element**: Even removing the last element during iteration can throw
   `ConcurrentModificationException` (though it sometimes appears to work -- this is unreliable).
2. **Concurrent modification from another thread**: Fail-fast detection may not fire immediately in multi-threaded
   contexts; the collection might become corrupted before the exception is thrown.
3. **Nested iteration**: Modifying a collection while iterating it in a nested loop is doubly dangerous -- use copies
   or concurrent collections.
4. **Map.Entry modifications**: Calling `entry.setValue()` during HashMap iteration is safe and does NOT trigger
   `ConcurrentModificationException` because it is not a structural modification.

## Interview-specific Insights

Interviewers often focus on:
- The difference between fail-fast and fail-safe (weakly consistent) iterators
- How to safely remove elements during iteration
- Why `ConcurrentModificationException` is not guaranteed to be thrown
- The snapshot semantics of CopyOnWriteArrayList
- Whether `ConcurrentHashMap` iterator is "fail-safe" (it is weakly consistent, not truly fail-safe)
- The difference between structural modification and value modification

## Interview Q&A Section

**Q1: What is the difference between fail-fast and weakly consistent iterators?**

```text
A1: These are the two main iterator behaviors in Java collections:

Fail-fast iterators (ArrayList, HashMap, HashSet):
- Detect structural modifications after iterator creation
- Throw ConcurrentModificationException on detection
- Use an internal modification counter (modCount) for detection
- Best-effort: NOT guaranteed to detect all concurrent modifications
- Single-threaded scenario: Modifying collection during for-each throws CME
- Multi-threaded: May detect modification, may not, may corrupt data first

Weakly consistent iterators (ConcurrentHashMap, CopyOnWriteArrayList, ConcurrentSkipListSet):
- NEVER throw ConcurrentModificationException
- May or may not reflect modifications made after iterator creation
- Guarantee each element is returned at most once
- CopyOnWriteArrayList: iterates over a snapshot (frozen at creation time)
- ConcurrentHashMap: may reflect some concurrent changes (not snapshot-based)

Important: The term "fail-safe" is commonly used but technically incorrect for Java concurrent 
collections. The official term is "weakly consistent."
```

```java
// Fail-fast: ArrayList
List<String> list = new ArrayList<>(List.of("a", "b", "c"));
try {
    for (String s : list) {
        list.add("d"); // ConcurrentModificationException!
    }
} catch (ConcurrentModificationException e) {
    System.out.println("Fail-fast detected modification");
}

// Weakly consistent: ConcurrentHashMap
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
map.put("a", 1); map.put("b", 2); map.put("c", 3);
for (Map.Entry<String, Integer> entry : map.entrySet()) {
    map.put("d", 4); // No exception - weakly consistent
    System.out.println(entry); // May or may not see "d"
}

// Snapshot: CopyOnWriteArrayList
CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>(List.of("x", "y"));
for (String s : cowList) {
    cowList.add("z"); // No exception - iterating over snapshot
    // Will NOT print "z"
    System.out.println(s);
}
```

**Q2: How do you safely remove elements from a List during iteration?**

```text
A2: There are several safe approaches, from most to least preferred:

1. removeIf() (Java 8+) - BEST approach:
   - Clean, functional, single method call
   - Internally uses Iterator.remove() efficiently
   - Works on all Collection types

2. Iterator.remove():
   - The only safe way to remove during explicit iteration
   - Must call next() before each remove()
   - Only removes the last element returned by next()

3. Stream filtering:
   - Creates a new collection, does not modify the original
   - Ideal when you want to produce a filtered copy

4. Copy-then-modify:
   - Iterate a copy, modify the original
   - Higher memory usage but simple to understand

5. Indexed reverse iteration:
   - Iterate backwards by index, remove by index
   - Works only with indexed lists (ArrayList)
   - Avoids shifting issues since removed elements are behind the cursor
```

```java
List<String> list = new ArrayList<>(List.of("a", "b", "c", "b", "d"));

// 1. removeIf() - PREFERRED
list.removeIf(s -> s.equals("b"));
// list = [a, c, d]

// 2. Iterator.remove()
list = new ArrayList<>(List.of("a", "b", "c", "b", "d"));
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    if (it.next().equals("b")) {
        it.remove();
    }
}

// 3. Stream filtering (new list)
List<String> filtered = list.stream()
    .filter(s -> !s.equals("b"))
    .toList();

// 4. Copy-then-modify
List<String> toRemove = new ArrayList<>();
for (String s : list) {
    if (s.equals("b")) toRemove.add(s);
}
list.removeAll(toRemove);

// 5. Reverse index iteration
list = new ArrayList<>(List.of("a", "b", "c", "b", "d"));
for (int i = list.size() - 1; i >= 0; i--) {
    if (list.get(i).equals("b")) {
        list.remove(i);
    }
}
```

**Q3: Can you explain the snapshot semantics of CopyOnWriteArrayList's iterator?**

```text
A3: When you create an iterator (or use a for-each loop) on a CopyOnWriteArrayList, the iterator 
receives a reference to the CURRENT underlying array at that moment in time. This is the "snapshot."

Key behaviors:
1. The iterator will traverse exactly the elements that existed when it was created.
2. Any add/remove/set operations after iterator creation create a NEW array (copy-on-write), but 
   the iterator still references the OLD array.
3. The iterator NEVER throws ConcurrentModificationException.
4. The iterator does NOT support remove(), set(), or add() - these throw UnsupportedOperationException.
5. Multiple iterators created at different times may see different snapshots.

This design is excellent for scenarios where:
- Consistency during iteration is important
- Reads vastly outnumber writes
- You want zero-cost reads (no synchronization)

The trade-off is memory: during a write, both the old array (held by existing iterators) and the 
new array (the current list) exist in memory simultaneously.
```

```java
CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>(List.of("a", "b", "c"));

// Create iterator - captures snapshot [a, b, c]
Iterator<String> iter1 = list.iterator();

// Modify the list - creates new array
list.add("d");
list.remove("a");

// Create another iterator - captures snapshot [b, c, d]
Iterator<String> iter2 = list.iterator();

// iter1 still sees the original snapshot
List<String> fromIter1 = new ArrayList<>();
iter1.forEachRemaining(fromIter1::add);
System.out.println(fromIter1); // [a, b, c]

// iter2 sees the modified snapshot
List<String> fromIter2 = new ArrayList<>();
iter2.forEachRemaining(fromIter2::add);
System.out.println(fromIter2); // [b, c, d]

// Iterator.remove() is not supported
Iterator<String> iter3 = list.iterator();
iter3.next();
// iter3.remove(); // Throws UnsupportedOperationException!
```

**Q4: What happens if you modify a HashMap while iterating with streams?**

```text
A4: Modifying a HashMap (or any non-concurrent collection) while processing it with a stream 
will result in undefined behavior, and the stream may throw ConcurrentModificationException.

Streams use Spliterators internally, which have the same fail-fast detection as iterators. The 
stream pipeline checks the modification count at various stages, and if it detects a change, it 
throws ConcurrentModificationException.

This applies to:
- stream().filter().collect() - if the source is modified during execution
- parallelStream() - even more dangerous because modifications from any thread can corrupt state
- forEach() on streams - if the lambda modifies the source collection

The safe approach is to collect results into a new collection rather than modifying the source.
Note: ConcurrentHashMap streams are weakly consistent and will NOT throw CME.
```

```java
// DANGEROUS: Modifying source during stream processing
Map<String, Integer> map = new HashMap<>();
map.put("a", 1); map.put("b", 2); map.put("c", 3);

// BAD: This may throw ConcurrentModificationException
// map.entrySet().stream().forEach(e -> {
//     if (e.getValue() < 2) map.remove(e.getKey());
// });

// SAFE: Collect keys to remove, then remove them
List<String> toRemove = map.entrySet().stream()
    .filter(e -> e.getValue() < 2)
    .map(Map.Entry::getKey)
    .toList();
toRemove.forEach(map::remove);

// SAFE: Create a new filtered map
Map<String, Integer> filtered = map.entrySet().stream()
    .filter(e -> e.getValue() >= 2)
    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

// SAFE: ConcurrentHashMap - weakly consistent streams
ConcurrentHashMap<String, Integer> concMap = new ConcurrentHashMap<>(map);
concMap.entrySet().removeIf(e -> e.getValue() < 2); // Safe and atomic per-entry
```

**Q5: Is it safe to call `Map.Entry.setValue()` during HashMap iteration?**

```text
A5: Yes! Calling entry.setValue() during HashMap iteration is SAFE and does NOT throw 
ConcurrentModificationException. This is because setValue() modifies the VALUE of an existing 
entry, which is NOT a structural modification.

Structural modifications are operations that change the MAP'S STRUCTURE:
- Adding a new key (put with a new key)
- Removing a key (remove)
- Clearing the map (clear)

Non-structural modifications that are SAFE during iteration:
- entry.setValue() - changes value of existing entry
- put() with an EXISTING key - replaces value without structural change
- replace(key, value) - replaces value of existing key

However, be cautious: put() with a NEW key IS a structural modification and WILL trigger CME.
```

```java
Map<String, Integer> map = new HashMap<>();
map.put("a", 1); map.put("b", 2); map.put("c", 3);

// SAFE: setValue() during iteration
for (Map.Entry<String, Integer> entry : map.entrySet()) {
    entry.setValue(entry.getValue() * 10); // Safe! Not structural modification
}
// map = {a=10, b=20, c=30}

// SAFE: replaceAll() - applies a function to all values
map.replaceAll((key, value) -> value + 1);
// map = {a=11, b=21, c=31}

// DANGEROUS: put() with NEW key during iteration
// for (Map.Entry<String, Integer> entry : map.entrySet()) {
//     map.put("new_" + entry.getKey(), entry.getValue()); // CME!
// }
```

**Q6: How does the modCount mechanism work internally for fail-fast detection?**

```text
A6: The modCount mechanism is a simple counter-based detection system:

1. Every collection that supports fail-fast maintains an int field called modCount (modification count).
2. modCount is incremented on every structural modification (add, remove, clear).
3. When an iterator is created, it copies the current modCount into an expectedModCount field.
4. Before each iterator operation (next(), remove()), the iterator checks:
   if (modCount != expectedModCount) throw new ConcurrentModificationException();
5. Iterator.remove() is safe because it increments BOTH modCount AND expectedModCount.

Limitations:
- modCount is NOT volatile or synchronized, so in multi-threaded scenarios, the check may not 
  detect concurrent modifications due to memory visibility issues.
- Integer overflow: After 2^31 modifications, modCount wraps around. If it happens to match 
  expectedModCount after wrapping, the detection fails silently.
- It detects structural modifications only, not value changes.

This is why the Javadoc says: "the fail-fast behavior of iterators should be used only to detect bugs."
```

```java
// Conceptual illustration of modCount behavior
// (simplified from actual ArrayList source)

// ArrayList internal state:
// int modCount = 0;

// list.add("a") -> modCount becomes 1
// list.add("b") -> modCount becomes 2

// Iterator creation:
// expectedModCount = modCount (= 2)

// Iterator.next():
// check: modCount (2) == expectedModCount (2) -> OK

// list.add("c") -> modCount becomes 3

// Iterator.next():
// check: modCount (3) != expectedModCount (2) -> THROW ConcurrentModificationException

// Iterator.remove() is special:
// It calls list.remove() which increments modCount (to 4)
// Then it sets expectedModCount = modCount (to 4)
// So the next check passes: modCount (4) == expectedModCount (4)
```

## Code Examples

- Test: [IterationBehaviorTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/concurrentcollections/IterationBehaviorTest.java)
- Source: [IterationBehavior.java](src/main/java/com/github/msorkhpar/claudejavatutor/concurrentcollections/IterationBehavior.java)
