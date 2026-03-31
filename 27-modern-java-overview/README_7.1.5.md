# 7.1.5. Java 21 (LTS)

## Concept Explanation

Java 21, released in September 2023, is the latest Long-Term Support release and represents the culmination of years of evolution in the Java platform. It finalizes pattern matching for switch, record patterns, and virtual threads — three features that together transform how Java applications are written, from data processing to high-concurrency server-side code.

**Real-world analogy**: Java 21 is like a city completing its multi-year transit overhaul. The express rail line (virtual threads) is now open to all passengers — handling millions of riders without needing a dedicated platform thread per passenger. The smart traffic routing system (pattern matching for switch) can now inspect and route any kind of vehicle (object type) with full GPS data (record patterns), and the new organized neighborhoods (sequenced collections) finally give every district a clear first and last address.

The five pillars of Java 21 are:
1. **Pattern Matching for switch (JEP 441)** — type-safe, exhaustive switch over any object type
2. **Record Patterns (JEP 440)** — deconstruct records in pattern-matching contexts
3. **Virtual Threads (JEP 444)** — lightweight threads for massive concurrency
4. **Structured Concurrency (JEP 453, Preview)** — treating groups of concurrent tasks as a unit
5. **Foreign Function & Memory API (JEP 442, Third Preview)** — safe interop with native code and memory

## Key Points to Remember

- Pattern matching for switch supports type patterns, guarded patterns (`when`), null handling, and exhaustiveness checking.
- Record patterns allow nested deconstruction: `case Line(Point(int x1, int y1), Point(int x2, int y2))`.
- Virtual threads are managed by the JVM, not the OS — you can create millions of them.
- Virtual threads are designed for I/O-bound workloads; they unmount from carrier threads when blocking.
- `Executors.newVirtualThreadPerTaskExecutor()` is the primary way to use virtual threads.
- `Thread.ofVirtual()` and `Thread.ofPlatform()` provide the builder API for thread creation.
- Sequenced collections (`SequencedCollection`, `SequencedSet`, `SequencedMap`) add `getFirst()`, `getLast()`, and `reversed()`.
- Structured concurrency ensures child tasks complete before the parent scope exits.
- The Foreign Function & Memory API replaces JNI with a pure-Java, type-safe alternative.
- Java 21 switch expressions with sealed types don't need a `default` branch — the compiler verifies exhaustiveness.

## Relevant Java 21 Features

This *is* the Java 21 module. Key interactions between features:

- **Pattern matching + sealed classes + records** = algebraic data types with exhaustive handling, enabling functional programming patterns in Java.
- **Virtual threads + structured concurrency** = scalable server applications without callback hell or reactive frameworks.
- **Sequenced collections** = consistent first/last element access across `List`, `LinkedHashSet`, `LinkedHashMap`, `SortedSet`, and `SortedMap`.
- **Foreign Function & Memory API** = safe native interop that works with virtual threads (no thread-pinning issues like JNI).

## Common Pitfalls and How to Avoid Them

1. **Using virtual threads for CPU-bound work**:
   ```java
   // WRONG: virtual threads don't help with CPU-bound tasks
   try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
       executor.submit(() -> fibonacci(45)); // CPU-bound — no benefit
   }

   // RIGHT: use platform thread pools for CPU-bound work
   try (var executor = Executors.newFixedThreadPool(
           Runtime.getRuntime().availableProcessors())) {
       executor.submit(() -> fibonacci(45));
   }
   ```

2. **Forgetting null handling in switch patterns**:
   ```java
   // WRONG: NPE if obj is null and no null case
   // String result = switch (obj) {
   //     case String s -> s;
   //     default -> "other";  // null falls through to default but throws NPE
   // };

   // RIGHT: handle null explicitly
   String result = switch (obj) {
       case null -> "null value";
       case String s -> s;
       default -> "other";
   };
   ```

3. **Missing the `when` clause in guarded patterns**:
   ```java
   // WRONG: using if inside the case body for pattern filtering
   String result = switch (obj) {
       case Integer i -> {
           if (i < 0) yield "negative";
           yield "positive";
       }
       default -> "other";
   };

   // RIGHT: use guarded patterns with 'when'
   String result = switch (obj) {
       case Integer i when i < 0 -> "negative";
       case Integer i -> "non-negative";
       default -> "other";
   };
   ```

4. **Pinning virtual threads with synchronized blocks**:
   ```java
   // WRONG: synchronized pins the virtual thread to its carrier
   synchronized (lock) {
       socket.read(); // Virtual thread cannot unmount — blocks carrier thread
   }

   // RIGHT: use ReentrantLock instead
   ReentrantLock lock = new ReentrantLock();
   lock.lock();
   try {
       socket.read(); // Virtual thread can unmount while waiting
   } finally {
       lock.unlock();
   }
   ```

5. **Pattern order matters — unreachable patterns cause compile errors**:
   ```java
   // WRONG: more specific pattern after general pattern
   // String result = switch (obj) {
   //     case Integer i -> "integer";
   //     case Integer i when i > 0 -> "positive"; // COMPILE ERROR: unreachable
   //     default -> "other";
   // };

   // RIGHT: specific patterns first
   String result = switch (obj) {
       case Integer i when i > 0 -> "positive";
       case Integer i -> "integer";
       default -> "other";
   };
   ```

## Best Practices and Optimization Techniques

- **Use sealed types with pattern matching** for domain models — the compiler catches missing cases.
- **Prefer record patterns over manual accessor calls** for cleaner, more readable code.
- **Use virtual threads for I/O-bound servers** (HTTP handlers, database queries, file I/O) — replace thread pools with `newVirtualThreadPerTaskExecutor()`.
- **Avoid thread-local variables with virtual threads** — they consume memory proportional to thread count, which can be millions.
- **Use `ScopedValue` (preview)** instead of `ThreadLocal` when using virtual threads.
- **Order switch patterns from most specific to most general** — the first matching pattern wins.
- **Use `SequencedCollection` methods** (`getFirst()`, `getLast()`, `reversed()`) instead of index-based workarounds.
- **Replace `if-else instanceof` chains** with pattern-matching switch expressions for cleaner code.

## Edge Cases and Their Handling

- **Null in pattern-matching switch**: Must be handled explicitly with `case null ->` or it throws `NullPointerException`.
- **Empty `SequencedCollection`**: `getFirst()` and `getLast()` throw `NoSuchElementException` on empty collections.
- **Virtual thread interruption**: Virtual threads respond to `interrupt()` the same way as platform threads.
- **Record pattern with null component**: `case Point(var x, var y)` matches even if `x` or `y` is null (for reference types).
- **Guarded pattern with side effects**: The `when` clause should be side-effect-free — it's evaluated as part of pattern matching.
- **Nested record patterns**: Deeply nested patterns like `case Line(Point(int x1, int y1), Point(int x2, int y2))` are legal and useful.
- **Virtual thread stack traces**: Stack traces work normally but may show carrier thread information in certain debugging contexts.

## Interview-specific Insights

Java 21 is a critical topic for senior interviews:
- **"Explain virtual threads and when to use them."** — The most common Java 21 question. Focus on I/O-bound vs CPU-bound, carrier threads, and the pinning problem.
- **"How does pattern matching for switch differ from traditional switch?"** — Exhaustiveness, type patterns, guarded patterns, null handling.
- **"What are record patterns?"** — Nested deconstruction, combining with sealed types for algebraic data types.
- **"How would you migrate a reactive application to virtual threads?"** — Shows understanding of both paradigms and practical migration knowledge.
- **"What replaces JNI?"** — Foreign Function & Memory API — safe, performant, pure-Java native interop.

## Interview Q&A Section

### Q1: What are virtual threads and how do they differ from platform threads?

```text
Virtual threads (JEP 444) are lightweight threads managed by the JVM rather than the OS.

Key differences:
1. Resource cost: Platform threads map 1:1 to OS threads (~1MB stack each). Virtual
   threads are JVM-managed with small initial stacks (~few KB), growing as needed.
2. Scalability: You can create millions of virtual threads vs. thousands of platform threads.
3. Scheduling: Platform threads are scheduled by the OS. Virtual threads are scheduled
   by the JVM onto a pool of carrier (platform) threads.
4. Blocking behavior: When a virtual thread blocks on I/O, it unmounts from its carrier
   thread, freeing it for other virtual threads. Platform threads hold their OS thread.
5. Use case: Virtual threads excel at I/O-bound tasks (HTTP, database, file I/O).
   Platform threads are better for CPU-bound work.
6. Thread-locals: Both support ThreadLocal, but virtual threads make them expensive
   (millions of copies). Use ScopedValue (preview) instead.

Important: Virtual threads are NOT faster for individual tasks. They enable higher
throughput by allowing more concurrent tasks without the overhead of OS threads.
```

```java
// Creating virtual threads
Thread vt = Thread.ofVirtual().name("my-vt").start(() -> {
    System.out.println("Running on: " + Thread.currentThread());
    System.out.println("Is virtual: " + Thread.currentThread().isVirtual());
});
vt.join();

// Virtual thread executor (recommended for servers)
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    // Submit 100,000 I/O-bound tasks — no problem!
    List<Future<String>> futures = IntStream.range(0, 100_000)
        .mapToObj(i -> executor.submit(() -> fetchUrl("http://example.com/" + i)))
        .toList();

    for (Future<String> f : futures) {
        String result = f.get(); // Each virtual thread blocks independently
    }
}

// Comparing thread creation
Thread platform = Thread.ofPlatform().name("platform-1").start(() -> { });
Thread virtual = Thread.ofVirtual().name("virtual-1").start(() -> { });
System.out.println(platform.isVirtual()); // false
System.out.println(virtual.isVirtual());  // true
```

### Q2: Explain pattern matching for switch with examples of guarded patterns and null handling.

```text
Pattern matching for switch (JEP 441) extends switch to work with type patterns,
guarded patterns, null patterns, and record patterns.

Key features:
1. Type patterns: match and bind in one step — case Integer i -> ...
2. Guarded patterns: add conditions with 'when' — case Integer i when i > 0 -> ...
3. Null handling: explicit null case — case null -> ... (previously NPE)
4. Exhaustiveness: with sealed types, no default needed if all subtypes are covered
5. Dominance checking: more specific patterns must come before general ones
6. Pattern order: first matching pattern wins (not fall-through like traditional switch)

The 'when' keyword replaces the need for if-statements inside case blocks, making
the intent clearer and enabling the compiler to verify pattern dominance.
```

```java
// Comprehensive pattern matching switch
public String describe(Object obj) {
    return switch (obj) {
        case null -> "null";
        case Integer i when i < 0 -> "negative: " + i;
        case Integer i when i == 0 -> "zero";
        case Integer i -> "positive: " + i;
        case String s when s.isBlank() -> "blank string";
        case String s -> "string: " + s;
        case List<?> l when l.isEmpty() -> "empty list";
        case List<?> l -> "list of " + l.size();
        case int[] arr -> "int array of length " + arr.length;
        default -> "other: " + obj.getClass().getSimpleName();
    };
}

// Exhaustive switch with sealed types (no default needed)
sealed interface Result permits Success, Failure {}
record Success(String data) implements Result {}
record Failure(String error) implements Result {}

public String handleResult(Result result) {
    return switch (result) {
        case Success(String data) -> "OK: " + data;   // Record pattern!
        case Failure(String error) -> "ERR: " + error;
        // No default — compiler knows these are all cases
    };
}
```

### Q3: What are record patterns and how do they enable nested deconstruction?

```text
Record patterns (JEP 440) allow you to deconstruct a record's components directly
in pattern-matching contexts (instanceof and switch).

Key concepts:
1. Basic deconstruction: case Point(int x, int y) — binds x and y
2. Nested deconstruction: case Line(Point(int x1, int y1), Point(int x2, int y2))
3. Type inference: use 'var' for inferred types — case Point(var x, var y)
4. Combined with guards: case Point(int x, int y) when x > 0 && y > 0
5. Works in instanceof: if (obj instanceof Point(int x, int y))

Benefits:
- Eliminates accessor method calls in destructuring scenarios
- Enables deep pattern matching in one expression
- Combined with sealed interfaces, creates exhaustive algebraic data type handling
- Makes Java competitive with Scala/Kotlin for data-oriented programming
```

```java
// Record definitions
record Point(int x, int y) {}
record Line(Point start, Point end) {}
record Circle(Point center, double radius) {}

// Basic record pattern
String describe(Object obj) {
    return switch (obj) {
        case Point(int x, int y) when x == 0 && y == 0 -> "origin";
        case Point(int x, int y) -> "(%d, %d)".formatted(x, y);
        default -> "unknown";
    };
}

// Nested record pattern — deconstruct through multiple levels
String describeLine(Line line) {
    return switch (line) {
        case Line(Point(int x1, int y1), Point(int x2, int y2))
            when x1 == x2 && y1 == y2 -> "degenerate (single point)";
        case Line(Point(int x1, var _), Point(int x2, var _))
            when x1 == x2 -> "vertical at x=" + x1;
        case Line(Point(var _, int y1), Point(var _, int y2))
            when y1 == y2 -> "horizontal at y=" + y1;
        case Line(Point(int x1, int y1), Point(int x2, int y2)) ->
            "from (%d,%d) to (%d,%d)".formatted(x1, y1, x2, y2);
    };
}

// instanceof with record pattern
if (obj instanceof Circle(Point(int cx, int cy), double r) && r > 0) {
    System.out.println("Circle at (" + cx + "," + cy + ") with radius " + r);
}
```

### Q4: What is structured concurrency and why is it important?

```text
Structured concurrency (JEP 453, preview in Java 21) ensures that concurrent tasks
have a clear lifecycle scope — child tasks must complete before the parent scope exits.

The problem it solves:
- Unstructured concurrency: tasks launched via ExecutorService can outlive the code
  that launched them, leading to resource leaks, orphaned tasks, and hard-to-debug errors.
- Thread leaks: if a task is forgotten, it runs indefinitely.
- Cancellation: canceling a parent task doesn't automatically cancel its children.

Structured concurrency guarantees:
1. All subtasks complete (succeed or fail) before the scope closes.
2. If one subtask fails, the others can be automatically cancelled.
3. Thread dumps show the parent-child relationship between tasks.
4. The lifetime of concurrent tasks is bounded by a syntactic block.

StructuredTaskScope is the API (preview). Policies include:
- ShutdownOnFailure: cancel all tasks if any fails
- ShutdownOnSuccess: cancel remaining tasks once one succeeds
```

```java
// Structured concurrency pattern (conceptual — preview API)
// Using StructuredTaskScope (requires --enable-preview)
// Response fetchUserProfile(long userId) throws Exception {
//     try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
//         Subtask<User> userTask = scope.fork(() -> fetchUser(userId));
//         Subtask<List<Order>> ordersTask = scope.fork(() -> fetchOrders(userId));
//         Subtask<Settings> settingsTask = scope.fork(() -> fetchSettings(userId));
//
//         scope.join();           // Wait for all tasks
//         scope.throwIfFailed();  // Propagate first failure
//
//         return new Response(
//             userTask.get(),
//             ordersTask.get(),
//             settingsTask.get()
//         );
//     }
//     // All tasks guaranteed complete here — no leaks possible
// }

// Without preview API — simulating structured concurrency with virtual threads
public static <T> List<T> runAllOrFail(List<Callable<T>> tasks) throws Exception {
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
        List<Future<T>> futures = tasks.stream()
            .map(executor::submit)
            .toList();

        List<T> results = new ArrayList<>();
        for (Future<T> f : futures) {
            results.add(f.get(10, TimeUnit.SECONDS));
        }
        return results;
    } // executor.close() waits for all tasks — structured!
}
```

### Q5: What are sequenced collections and why were they added?

```text
Sequenced collections (JEP 431) add three new interfaces to the Collections framework:
- SequencedCollection<E> extends Collection<E>
- SequencedSet<E> extends SequencedCollection<E>, Set<E>
- SequencedMap<K,V> extends Map<K,V>

Problem they solve:
Before Java 21, accessing the first/last elements of ordered collections was inconsistent:
- List: list.get(0) / list.get(list.size()-1)
- LinkedHashSet: iterator().next() / ???  (no easy way to get last!)
- SortedSet: sortedSet.first() / sortedSet.last()
- Deque: deque.getFirst() / deque.getLast()

Each collection type had different methods for the same concept. Sequenced collections
unify this with:
- getFirst(), getLast() — access first/last elements
- addFirst(E), addLast(E) — add at either end
- removeFirst(), removeLast() — remove from either end
- reversed() — returns a reversed view of the collection

Existing classes were retrofitted:
- ArrayList, LinkedList → implement SequencedCollection
- LinkedHashSet → implements SequencedSet
- LinkedHashMap → implements SequencedMap
- TreeSet, TreeMap → implement SequencedSet/SequencedMap
```

```java
// Unified first/last access
List<String> list = new ArrayList<>(List.of("a", "b", "c"));
System.out.println(list.getFirst()); // "a"
System.out.println(list.getLast());  // "c"

LinkedHashSet<String> set = new LinkedHashSet<>(List.of("x", "y", "z"));
System.out.println(set.getFirst()); // "x"
System.out.println(set.getLast());  // "z"

// Reversed view
SequencedCollection<String> reversed = list.reversed();
System.out.println(reversed.getFirst()); // "c"
System.out.println(reversed.getLast());  // "a"

// Iterate in reverse without creating a new list
for (String s : list.reversed()) {
    System.out.println(s); // c, b, a
}

// SequencedMap
LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
map.put("first", 1);
map.put("second", 2);
map.put("third", 3);

System.out.println(map.firstEntry()); // first=1
System.out.println(map.lastEntry());  // third=3

SequencedMap<String, Integer> reversedMap = map.reversed();
System.out.println(reversedMap.firstEntry()); // third=3
```

## Code Examples

- Implementation: [Java21Features.java](src/main/java/com/github/msorkhpar/claudejavatutor/modernjava/Java21Features.java)
- Tests: [Java21FeaturesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/modernjava/Java21FeaturesTest.java)
