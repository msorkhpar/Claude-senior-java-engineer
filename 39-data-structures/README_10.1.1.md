# 10.1.1. Common Data Structures

## Concept Explanation

Data structures are fundamental building blocks in software engineering that determine how data is organized, stored, and accessed in memory. Choosing the right data structure directly impacts the performance, scalability, and maintainability of an application.

**Real-world analogy**: Think of data structures like different ways to organize a library. An ArrayList is like a numbered bookshelf where you can instantly find book #47 but inserting a new book in the middle requires shifting everything. A LinkedList is like a chain of book carts where adding or removing a cart is easy, but finding a specific book requires walking through the chain. A HashMap is like an index card catalog that tells you exactly where a book is located. A Tree is like a hierarchical filing system with categories and subcategories. A Graph is like a road map showing connections between cities.

Java provides a rich Collections Framework (in `java.util`) that implements these data structures with well-defined interfaces and multiple implementations, each optimized for different use cases.

### Lists

Lists are ordered collections that allow duplicate elements and provide positional access.

- **ArrayList**: Backed by a dynamic array. Provides O(1) random access and amortized O(1) append, but O(n) insertion/removal in the middle due to element shifting. Best for read-heavy workloads.
- **LinkedList**: Backed by a doubly-linked list. Provides O(1) insertion/removal at head and tail, but O(n) random access. Also implements `Deque`, making it suitable as a queue or stack. Best for frequent insertions/removals at the ends.

### Sets

Sets are collections that contain no duplicate elements.

- **HashSet**: Backed by a HashMap. Provides O(1) average-case add, remove, and contains. Does not maintain any order. The most commonly used Set implementation.
- **TreeSet**: Backed by a Red-Black tree (a self-balancing BST). Provides O(log n) operations and maintains elements in sorted (natural or custom) order. Supports range queries via `NavigableSet` methods.
- **LinkedHashSet**: Backed by a hash table with a linked list running through entries. Provides O(1) operations like HashSet but also maintains insertion order.

### Maps

Maps store key-value pairs, where each key maps to at most one value.

- **HashMap**: The workhorse map. O(1) average-case get/put. Since Java 8, buckets with many collisions use a balanced tree (O(log n)) instead of a linked list. Does not maintain order.
- **TreeMap**: Backed by a Red-Black tree. O(log n) operations with keys maintained in sorted order. Supports range queries, floor/ceiling operations.
- **LinkedHashMap**: Like HashMap but maintains insertion order (or access order if configured). Useful for building LRU caches.

### Trees

Trees are hierarchical data structures consisting of nodes connected by edges.

- **Binary Search Tree (BST)**: Each node has at most two children. Left child < parent < right child. In-order traversal yields sorted elements. Average case O(log n) for search/insert/delete, but can degrade to O(n) if unbalanced.
- **AVL Tree / Red-Black Tree**: Self-balancing BSTs that guarantee O(log n) operations. Java's `TreeMap` and `TreeSet` use Red-Black trees internally.
- **Traversal Orders**: In-order (left, root, right), Pre-order (root, left, right), Post-order (left, right, root), Level-order (BFS).

### Graphs

Graphs consist of vertices (nodes) and edges (connections). They can be directed or undirected, weighted or unweighted.

- **Adjacency List**: Each vertex maintains a list of its neighbors. Space-efficient for sparse graphs: O(V + E).
- **Adjacency Matrix**: A 2D array where `matrix[i][j]` indicates an edge. O(1) edge lookup but O(V^2) space.
- **Common Algorithms**: BFS (shortest path in unweighted graphs), DFS (cycle detection, topological sort), Dijkstra (shortest path in weighted graphs).

## Key Points to Remember

1. **ArrayList** is the default choice for most list operations due to O(1) random access and cache-friendly memory layout.
2. **LinkedList** should only be preferred when frequent insertions/removals at both ends are needed and random access is not required.
3. **HashSet/HashMap** require proper `hashCode()` and `equals()` implementations for custom objects. Java records provide these automatically.
4. **TreeSet/TreeMap** require elements to implement `Comparable` or a `Comparator` to be provided.
5. Never modify a collection while iterating over it (unless using `Iterator.remove()` or concurrent collections).
6. The `Collections.unmodifiableXXX()` methods create read-only views, not copies.
7. Java's `HashMap` uses treeification (converting linked-list buckets to balanced trees) when a bucket reaches 8 entries, improving worst-case from O(n) to O(log n).
8. `ConcurrentHashMap` is the thread-safe alternative to `HashMap`; `Collections.synchronizedMap()` is a simpler but coarser alternative.
9. Graph representations should be chosen based on density: adjacency list for sparse graphs, adjacency matrix for dense graphs.
10. BST traversals are a common interview topic: in-order gives sorted output, pre-order is useful for serialization, level-order for breadth-first processing.

## Relevant Java 21 Features

- **Sequenced Collections (JEP 431)**: Java 21 introduced `SequencedCollection`, `SequencedSet`, and `SequencedMap` interfaces that provide uniform access to the first and last elements and a reversed view. `ArrayList`, `LinkedList`, `LinkedHashSet`, `LinkedHashMap`, and `TreeSet`/`TreeMap` all implement these new interfaces.
- **Records**: Java records automatically generate `hashCode()`, `equals()`, and `toString()`, making them ideal for use as keys in hash-based collections or elements in sets.
- **Pattern Matching for switch**: Useful when processing different types of collection elements or graph node types.
- **Virtual Threads (Project Loom)**: When traversing large graphs or processing large collections concurrently, virtual threads provide lightweight concurrency.
- **`List.of()`, `Set.of()`, `Map.of()`**: Factory methods introduced in Java 9 for creating unmodifiable collections. These are compact, null-intolerant, and optimized for small sizes.

## Common Pitfalls and How to Avoid Them

1. **Using `==` instead of `equals()` for collection lookups with wrapper types**
   ```java
   // WRONG: Integer caching only works for -128 to 127
   Integer a = 200;
   Integer b = 200;
   Set<Integer> set = new HashSet<>();
   set.add(a);
   System.out.println(set.contains(b)); // true (equals works), but a == b is false
   ```
   **Solution**: Always use `equals()` for object comparison. Collections use `equals()` internally.

2. **Forgetting to override `hashCode()` when overriding `equals()`**
   ```java
   // WRONG: Objects equal by equals() but with different hashCodes
   // will be stored in different buckets in a HashSet/HashMap
   class Point {
       int x, y;
       @Override public boolean equals(Object o) { /* correct impl */ }
       // Missing hashCode()!
   }
   ```
   **Solution**: Always override both together, or use Java records which do this automatically.

3. **Modifying a collection during iteration**
   ```java
   // WRONG: ConcurrentModificationException
   List<String> list = new ArrayList<>(List.of("a", "b", "c"));
   for (String s : list) {
       if (s.equals("b")) list.remove(s);
   }
   ```
   **Solution**: Use `Iterator.remove()`, `List.removeIf()`, or create a new collection.

4. **Using a mutable object as a HashMap key and then modifying it**
   ```java
   // WRONG: After mutation, the hash changes, and the entry becomes unreachable
   List<String> key = new ArrayList<>(List.of("a"));
   Map<List<String>, String> map = new HashMap<>();
   map.put(key, "value");
   key.add("b"); // Modifies the key!
   map.get(key); // Returns null!
   ```
   **Solution**: Use immutable objects (records, `List.of()`, etc.) as map keys.

5. **Assuming LinkedList is faster for insertions than ArrayList without benchmarking**
   ```java
   // LinkedList has poor cache locality and high per-node memory overhead
   // For most workloads, ArrayList is faster even for insertions
   ```
   **Solution**: Profile before choosing. ArrayList is usually the better default.

## Best Practices and Optimization Techniques

1. **Pre-size collections when the final size is known**: `new ArrayList<>(expectedSize)` avoids costly array resizing.
2. **Program to interfaces**: Declare variables as `List`, `Set`, `Map` rather than `ArrayList`, `HashSet`, `HashMap`. This allows swapping implementations without changing client code.
3. **Use `Map.computeIfAbsent()`** for lazy initialization of map values (e.g., building a `Map<K, List<V>>`).
4. **Use `Map.merge()`** for aggregation (e.g., counting frequencies).
5. **Prefer `EnumSet` and `EnumMap`** when keys are enum values; they are extremely fast and memory-efficient.
6. **Use `Collections.unmodifiableXXX()` or `List.copyOf()`** to create defensive copies when returning collections from methods.
7. **Consider `ArrayDeque` instead of `LinkedList`** for stack/queue operations; it is faster and more memory-efficient.
8. **For large BSTs, prefer `TreeMap`/`TreeSet`** (Red-Black tree) over custom BST implementations; they handle balancing automatically.
9. **Choose graph representation based on edge density**: adjacency list for sparse, adjacency matrix for dense graphs.
10. **Use streams judiciously**: For simple operations, a for-loop may be faster; for complex pipelines, streams improve readability.

## Edge Cases and Their Handling

1. **Null elements**: `HashSet`, `HashMap`, `ArrayList`, and `LinkedList` allow null elements/keys. `TreeSet` and `TreeMap` do NOT allow null (throws `NullPointerException` during comparison). `List.of()`, `Set.of()`, `Map.of()` do NOT allow null.
2. **Empty collections**: Always check `isEmpty()` before calling `first()`/`last()` on `TreeSet` or `firstKey()`/`lastKey()` on `TreeMap`. Use `Optional` or null checks.
3. **Single-element collections**: BST with one node has height 0. A graph with one vertex and no edges is trivially acyclic.
4. **Integer overflow in HashMap capacity**: The internal capacity is always a power of 2. The maximum is `1 << 30`. Beyond that, further resizing is not possible.
5. **Concurrent modification**: Always use concurrent collections (`ConcurrentHashMap`, `CopyOnWriteArrayList`) or explicit synchronization for multi-threaded access.
6. **Graph cycles**: BFS/DFS on cyclic graphs require a "visited" set to avoid infinite loops.
7. **Disconnected graphs**: BFS/DFS from a single source will not visit disconnected components. Iterate over all vertices to handle disconnected graphs.

## Interview-specific Insights

Interviewers commonly focus on:

- **Choosing the right data structure** for a given problem (e.g., "How would you implement an LRU cache?" Answer: `LinkedHashMap`).
- **Time and space complexity** of operations on each data structure.
- **Trade-offs** between different implementations (ArrayList vs. LinkedList, HashMap vs. TreeMap).
- **The `hashCode`/`equals` contract** and what happens when it is violated.
- **BST traversals** (in-order, pre-order, post-order, level-order) and their applications.
- **Graph algorithms**: BFS for shortest path (unweighted), DFS for cycle detection, topological sort.
- **Internal implementation details**: How HashMap handles collisions (chaining, treeification), how ArrayList resizes (1.5x growth factor).

Common tricky questions:

- "What is the time complexity of `HashMap.get()` in the worst case?" (O(log n) after Java 8 treeification; O(n) before Java 8)
- "When would you use a LinkedList over an ArrayList?" (Almost never in practice; only for very specific Deque/queue patterns)
- "How does TreeMap maintain order?" (Red-Black tree, a self-balancing BST)
- "What happens if two objects have the same hashCode but are not equal?" (They end up in the same bucket; equals() resolves the collision)

## Interview Q&A Section

**Q1: What are the key differences between ArrayList and LinkedList, and when would you use each?**

```text
A1: ArrayList and LinkedList both implement the List interface but have fundamentally different
internal structures and performance characteristics:

ArrayList:
- Backed by a contiguous dynamic array
- O(1) random access via index
- O(1) amortized append (occasional O(n) when resizing)
- O(n) insert/remove in the middle (requires shifting elements)
- Excellent cache locality due to contiguous memory layout
- Lower memory overhead (no per-element node objects)

LinkedList:
- Backed by a doubly-linked list with node objects
- O(n) random access (must traverse from head/tail)
- O(1) insert/remove at head/tail (if you already have a reference)
- O(n) insert/remove by index (must first traverse to the index)
- Implements Deque, so it can be used as a queue or stack
- Higher memory overhead (each element wrapped in a Node with prev/next pointers)

When to use ArrayList (almost always):
- Random access is needed
- Iteration is the primary operation
- Memory efficiency matters

When to use LinkedList (rare):
- Frequent additions/removals at both ends (use as Deque)
- Iterator-based removal during traversal

In practice, ArrayDeque is preferred over LinkedList for Deque/Queue operations because
it has better cache performance and lower memory overhead.
```

```java
// Demonstrating the performance difference
List<String> arrayList = new ArrayList<>();
arrayList.add("fast random access");
String element = arrayList.get(0); // O(1)

LinkedList<String> linkedList = new LinkedList<>();
linkedList.addFirst("fast at head"); // O(1)
linkedList.addLast("fast at tail");  // O(1)
String first = linkedList.peekFirst(); // O(1), returns null if empty
```

**Q2: Explain how HashMap works internally in Java 8+. What is treeification?**

```text
A2: HashMap uses an array of buckets (called the "table"). Each bucket initially uses a
linked list to handle hash collisions.

How put(key, value) works:
1. Compute hash: hashCode() of the key is further spread using a supplemental hash function
   to reduce collisions.
2. Determine bucket index: index = hash & (table.length - 1). This works because table
   length is always a power of 2.
3. If the bucket is empty, create a new node.
4. If the bucket has entries, traverse the chain:
   - If a matching key is found (by equals()), replace the value.
   - Otherwise, append a new node to the chain.
5. If the total number of entries exceeds capacity * loadFactor (default 0.75), resize
   (double the table and rehash all entries).

Treeification (Java 8+):
- When a single bucket's chain length reaches 8 (TREEIFY_THRESHOLD), the linked list is
  converted to a balanced Red-Black tree.
- This improves worst-case lookup from O(n) to O(log n) for that bucket.
- When the tree shrinks below 6 (UNTREEIFY_THRESHOLD) due to removals, it converts back
  to a linked list.
- Treeification requires keys to implement Comparable (or uses identity hash as tiebreaker).

Key constants:
- DEFAULT_INITIAL_CAPACITY = 16
- DEFAULT_LOAD_FACTOR = 0.75
- TREEIFY_THRESHOLD = 8
- UNTREEIFY_THRESHOLD = 6
- MIN_TREEIFY_CAPACITY = 64 (table must be at least this size for treeification)
```

```java
// HashMap usage with proper hashCode/equals via records
record Employee(String id, String name) {} // Records auto-generate hashCode/equals

Map<Employee, String> departments = new HashMap<>();
departments.put(new Employee("E001", "Alice"), "Engineering");
departments.put(new Employee("E002", "Bob"), "Marketing");

// Retrieval uses hashCode to find bucket, then equals to find exact match
String dept = departments.get(new Employee("E001", "Alice")); // "Engineering"

// computeIfAbsent for lazy value initialization
Map<String, List<String>> grouped = new HashMap<>();
grouped.computeIfAbsent("Engineering", k -> new ArrayList<>()).add("Alice");
grouped.computeIfAbsent("Engineering", k -> new ArrayList<>()).add("Charlie");
// Result: {"Engineering": ["Alice", "Charlie"]}
```

**Q3: What are the four types of tree traversals and when would you use each?**

```text
A3: There are four standard tree traversal orders:

1. In-order (Left, Root, Right):
   - For BST, visits nodes in sorted (ascending) order.
   - Used when you need sorted output from a BST.
   - Example: printing elements in order, validating BST property.

2. Pre-order (Root, Left, Right):
   - Visits the root before its children.
   - Used for tree serialization/copying (can reconstruct the tree from pre-order output).
   - Also used for creating prefix expression from expression trees.

3. Post-order (Left, Right, Root):
   - Visits the root after its children.
   - Used for tree deletion (delete children before parent).
   - Also used for evaluating postfix expressions and calculating directory sizes.

4. Level-order (Breadth-First):
   - Visits nodes level by level, left to right.
   - Implemented using a queue (BFS).
   - Used for finding shortest path in unweighted trees, printing tree by levels,
     and serialization/deserialization.

Time complexity: All traversals are O(n) where n is the number of nodes.
Space complexity:
- In-order, Pre-order, Post-order: O(h) where h is the height (recursion stack).
- Level-order: O(w) where w is the maximum width of the tree.
```

```java
// BST traversal example
var bst = new CommonDataStructures.BinarySearchTree<Integer>();
bst.insert(5);
bst.insert(3);
bst.insert(7);
bst.insert(1);
bst.insert(4);

bst.inOrderTraversal();    // [1, 3, 4, 5, 7] - sorted
bst.preOrderTraversal();   // [5, 3, 1, 4, 7] - root first
bst.postOrderTraversal();  // [1, 4, 3, 7, 5] - root last
bst.levelOrderTraversal(); // [5, 3, 7, 1, 4] - level by level
```

**Q4: When would you use a TreeSet/TreeMap over a HashSet/HashMap?**

```text
A4: Use TreeSet/TreeMap when you need:

1. Sorted order: Elements/keys are maintained in natural order or custom Comparator order.
2. Range queries: subSet(), headSet(), tailSet() for TreeSet; subMap(), headMap(),
   tailMap() for TreeMap. These are O(log n) to create.
3. Navigation methods: first(), last(), ceiling(), floor(), higher(), lower().
4. Ordered iteration: Iterating always gives elements in sorted order.

Use HashSet/HashMap when:
1. Order doesn't matter
2. You need O(1) average-case performance
3. Elements don't need to be Comparable

Performance comparison:
- HashSet/HashMap: O(1) average, O(log n) worst case (Java 8+)
- TreeSet/TreeMap: O(log n) guaranteed for all operations

Memory:
- HashSet/HashMap use less memory per element
- TreeSet/TreeMap have additional left/right/parent pointers and color bits per node

Common interview scenario: "Implement a data structure that supports range queries"
-> Answer: TreeMap/TreeSet

Common interview scenario: "Find the k closest values to a target"
-> Answer: TreeSet with ceiling()/floor() navigation
```

```java
// TreeSet for range queries
TreeSet<Integer> scores = new TreeSet<>(List.of(50, 70, 85, 90, 95, 100));

// Find all scores between 70 and 95 (inclusive)
SortedSet<Integer> range = scores.subSet(70, true, 95, true);
// [70, 85, 90, 95]

// Find the next score >= 80
Integer ceiling = scores.ceiling(80); // 85

// Find the previous score <= 80
Integer floor = scores.floor(80); // 70

// TreeMap for sorted key-value storage
TreeMap<String, Integer> wordCount = new TreeMap<>();
wordCount.put("banana", 3);
wordCount.put("apple", 5);
wordCount.put("cherry", 2);
String firstWord = wordCount.firstKey(); // "apple" (alphabetically first)
```

**Q5: How would you implement a graph and what are the trade-offs between adjacency list and adjacency matrix representations?**

```text
A5: A graph consists of vertices (V) and edges (E). The two main representations are:

Adjacency List:
- Each vertex stores a collection of its neighbors.
- Space: O(V + E)
- Edge lookup: O(degree of vertex) = O(V) worst case
- Add vertex: O(1)
- Add edge: O(1)
- Best for sparse graphs (E << V^2)
- Most common in practice

Adjacency Matrix:
- A 2D boolean/int array of size V x V.
- matrix[i][j] = true/weight if edge exists from i to j.
- Space: O(V^2)
- Edge lookup: O(1)
- Add vertex: O(V^2) - need to resize matrix
- Add edge: O(1)
- Best for dense graphs (E close to V^2)
- Easier for weighted graphs and checking if specific edge exists

Choosing between them:
- Sparse graph (social network, web links): Adjacency List
- Dense graph (complete graph, correlation matrix): Adjacency Matrix
- Need fast "does edge X-Y exist?" queries: Adjacency Matrix
- Need to iterate over neighbors: Adjacency List

Common graph algorithms and their complexities:
- BFS: O(V + E) - shortest path in unweighted graphs
- DFS: O(V + E) - cycle detection, topological sort, connected components
- Dijkstra: O((V + E) log V) with priority queue - shortest path in weighted graphs
- Bellman-Ford: O(V * E) - handles negative weights
```

```java
// Adjacency list graph implementation
var graph = new CommonDataStructures.Graph<String>(false); // undirected
graph.addEdge("New York", "Boston");
graph.addEdge("New York", "Philadelphia");
graph.addEdge("Boston", "Portland");
graph.addEdge("Philadelphia", "Washington");

// BFS from New York - finds shortest path in unweighted graph
List<String> bfsOrder = graph.bfs("New York");
// Visits: New York -> Boston, Philadelphia -> Portland, Washington

// Shortest path
List<String> path = graph.shortestPath("Portland", "Washington");
// [Portland, Boston, New York, Philadelphia, Washington]

// Directed graph for cycle detection
var directedGraph = new CommonDataStructures.Graph<String>(true);
directedGraph.addEdge("A", "B");
directedGraph.addEdge("B", "C");
directedGraph.addEdge("C", "A"); // Creates a cycle
boolean hasCycle = directedGraph.hasCycle(); // true
```

**Q6: What is the difference between `Set.of()`, `new HashSet<>()`, and `Collections.unmodifiableSet()`?**

```text
A6: These three produce sets with very different characteristics:

1. Set.of(elements):
   - Creates a compact, unmodifiable set (truly immutable)
   - Null elements are NOT allowed (throws NullPointerException)
   - Duplicate elements are NOT allowed (throws IllegalArgumentException)
   - Uses optimized internal storage (field-based for small sizes)
   - Iteration order is unspecified and may change across JVM runs

2. new HashSet<>(collection):
   - Creates a mutable set backed by a HashMap
   - Allows null elements
   - Duplicate elements are silently ignored
   - Uses hash table internally
   - Iteration order is unspecified but consistent within a single JVM run

3. Collections.unmodifiableSet(set):
   - Creates a read-only VIEW of the underlying set
   - Does NOT create a copy - changes to the original set are visible
   - Throws UnsupportedOperationException on modification attempts
   - Allows null if the underlying set allows it
   - For a true immutable copy, use Set.copyOf(set) instead

When to use each:
- Set.of(): For small, known-at-compile-time immutable sets
- new HashSet<>(): For general-purpose mutable sets
- Collections.unmodifiableSet(): When you want to expose a read-only view
- Set.copyOf(): When you want to create an immutable copy of an existing set
```

```java
// Set.of - compact, immutable, no nulls
Set<String> immutable = Set.of("a", "b", "c");
// immutable.add("d"); // UnsupportedOperationException
// Set.of("a", null);  // NullPointerException
// Set.of("a", "a");   // IllegalArgumentException (duplicates)

// new HashSet - mutable, allows null
Set<String> mutable = new HashSet<>(List.of("a", "b"));
mutable.add("c");      // OK
mutable.add(null);     // OK

// Collections.unmodifiableSet - read-only VIEW
Set<String> original = new HashSet<>(Set.of("a", "b"));
Set<String> view = Collections.unmodifiableSet(original);
original.add("c");     // Modifies original
view.contains("c");    // true! The view reflects changes

// Set.copyOf - immutable COPY
Set<String> copy = Set.copyOf(original);
original.add("d");     // copy is NOT affected
copy.contains("d");    // false
```

**Q7: How would you implement an LRU (Least Recently Used) cache in Java?**

```text
A7: An LRU cache evicts the least recently used entry when the cache reaches its capacity.
Java's LinkedHashMap provides a built-in mechanism for this:

LinkedHashMap maintains insertion order by default, but can be configured for access order
(most recently accessed entry moves to the end). By overriding removeEldestEntry(), you
can automatically evict the oldest entry when the map exceeds a given size.

This gives O(1) for both get and put operations, which is optimal for a cache.

Alternative approach: Combine a HashMap with a doubly-linked list manually. This is a
common interview coding question. The HashMap provides O(1) lookup, and the linked list
maintains the access order.
```

```java
// LRU cache using LinkedHashMap
class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    public LRUCache(int capacity) {
        super(capacity, 0.75f, true); // true = access order
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}

// Usage
LRUCache<String, String> cache = new LRUCache<>(3);
cache.put("a", "1");
cache.put("b", "2");
cache.put("c", "3");
cache.get("a");       // Access "a", moves it to the end
cache.put("d", "4");  // Evicts "b" (least recently used)
// Cache now contains: c, a, d
```

## Code Examples

- Test: [CommonDataStructuresTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/datastructures/CommonDataStructuresTest.java)
- Source: [CommonDataStructures.java](src/main/java/com/github/msorkhpar/claudejavatutor/datastructures/CommonDataStructures.java)
