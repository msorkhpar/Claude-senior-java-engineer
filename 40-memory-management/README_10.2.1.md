# 10.2.1. Heap and Stack Memory Management

## Concept Explanation

Understanding how Java manages memory is fundamental to writing efficient, bug-free applications and a frequent topic in
senior engineering interviews. Java's runtime memory is divided into several areas, but the two most important for
developers to understand are the **stack** and the **heap**.

**Real-world analogy**: Think of your desk (the stack) and a warehouse (the heap). When you start working on a task, you
place your current papers on your desk in a neat pile -- each new sub-task goes on top, and when it's done you remove it
(Last-In, First-Out). But large items like filing cabinets and equipment are stored in the warehouse. You keep a label
(reference) on your desk that tells you where in the warehouse to find the item. Multiple desks (threads) can each have
their own pile of papers, but they all share the same warehouse.

### Stack Memory

The **stack** is a region of memory used for method execution. Every time a method is called, the JVM creates a new
**stack frame** that contains:

- Local primitive variables (`int`, `double`, `boolean`, etc.)
- References (pointers) to objects on the heap
- Method parameters
- The return address (where to continue after the method returns)

Stack memory is thread-private -- each thread has its own stack. Stack allocation and deallocation are extremely fast
because they follow LIFO (Last-In, First-Out) order. When a method returns, its entire frame is popped off the stack
instantly.

### Heap Memory

The **heap** is the shared memory area where all Java objects and arrays are allocated. When you write `new Object()`,
the JVM allocates space on the heap. Key characteristics:

- Shared across all threads
- Managed by the garbage collector
- Divided into generations (Young, Old) for GC efficiency
- Slower allocation than stack (requires finding free space, GC overhead)

### String Pool

The String pool (or intern pool) is a special area within the heap where the JVM stores unique string literals. When you
write `"hello"` in code, the JVM checks the pool first and reuses existing instances, saving memory.

### Memory Allocation and Deallocation

Java does not have manual memory management (no `malloc`/`free`). Object deallocation is handled entirely by the garbage
collector. However, developers control when objects become **eligible** for collection by managing references.

**Reference types** play a critical role:

- **Strong reference** (default): Object is never collected while reachable
- **Soft reference** (`SoftReference<T>`): Collected only under memory pressure
- **Weak reference** (`WeakReference<T>`): Collected at the next GC cycle
- **Phantom reference** (`PhantomReference<T>`): Used for post-mortem cleanup

## Key Points to Remember

1. **Stack is per-thread, heap is shared** -- each thread has its own stack but all threads share the heap.
2. **Primitives on the stack, objects on the heap** -- primitive local variables live on the stack; objects are always
   allocated on the heap (with JIT escape analysis as an exception).
3. **References are on the stack, referents on the heap** -- a local variable like `String s = "hello"` puts the
   reference `s` on the stack and the String object on the heap.
4. **Stack memory is automatically reclaimed** when a method returns -- no GC needed.
5. **StackOverflowError** occurs when the call stack exceeds its size limit (deep recursion).
6. **OutOfMemoryError** occurs when the heap is exhausted and GC cannot free enough memory.
7. **Escape analysis** (JIT optimization) can allocate objects on the stack if they don't escape the method.
8. **String pool** is part of the heap and stores deduplicated string literals.
9. **Pass-by-value** in Java means the reference value (pointer) is copied, not the object itself.
10. **Nullifying references** makes objects eligible for GC but doesn't guarantee immediate collection.

## Relevant Java 21 Features

- **Virtual Threads (Project Loom)**: Virtual threads have much smaller stack sizes (~few KB vs ~1MB for platform
  threads), enabling millions of concurrent threads. The JVM can dynamically grow/shrink virtual thread stacks.
- **Escape Analysis improvements**: Modern JVMs (including Java 21) have improved escape analysis, enabling more
  aggressive scalar replacement (allocating object fields directly on the stack).
- **Compact Object Headers (Project Lilliput preview)**: Ongoing work to reduce object header size from 12-16 bytes to
  8 bytes, reducing heap overhead.
- **Generational ZGC (JEP 439)**: Java 21 introduces generational mode for ZGC, improving young-generation collection
  efficiency.
- **Record classes**: Records produce compact heap objects with minimal overhead compared to traditional classes.
- **Sealed classes**: Enable JIT optimizations through known type hierarchies, potentially improving memory layout
  decisions.

## Common Pitfalls and How to Avoid Them

1. **StackOverflowError from unbounded recursion**

   ```java
   // Problem: No base case or base case never reached
   public int broken(int n) {
       return broken(n - 1); // never terminates for positive n without base case
   }
   
   // Fix: Always have a proper base case
   public long factorial(int n) {
       if (n < 0) throw new IllegalArgumentException("Negative input");
       if (n <= 1) return 1;
       return n * factorial(n - 1);
   }
   ```

2. **Memory leaks through unintentional object retention**

   ```java
   // Problem: Static collection holds references forever
   private static final List<Object> cache = new ArrayList<>();
   public void process(Object data) {
       cache.add(data); // objects never become eligible for GC
   }
   
   // Fix: Use bounded caches, weak references, or explicit cleanup
   private static final Map<Object, String> cache = new WeakHashMap<>();
   ```

3. **Forgetting to close resources**

   ```java
   // Problem: Resource not closed on exception
   InputStream is = new FileInputStream("file.txt");
   is.read(); // if this throws, is is never closed
   
   // Fix: Use try-with-resources
   try (InputStream is = new FileInputStream("file.txt")) {
       is.read();
   } // automatically closed
   ```

4. **Confusion between pass-by-value and pass-by-reference**

   ```java
   // Problem: Expecting reassignment to affect the caller
   public void reassign(List<String> list) {
       list = new ArrayList<>(); // only changes local reference, not caller's
   }
   
   // The caller's reference still points to the original list
   // Fix: Modify the object through the reference instead
   public void modify(List<String> list) {
       list.add("item"); // modifies the actual heap object
   }
   ```

5. **Leaky stack implementations that retain obsolete references**

   ```java
   // Problem: Popped elements still referenced in the array
   public Object pop() {
       return elements[--size]; // elements[size] still holds a reference!
   }
   
   // Fix: Null out the reference
   public Object pop() {
       Object result = elements[--size];
       elements[size] = null; // help GC
       return result;
   }
   ```

## Best Practices and Optimization Techniques

1. **Prefer local variables over instance variables** when possible -- they are stack-allocated and cheaper.
2. **Use try-with-resources** for all `AutoCloseable` resources to prevent leaks.
3. **Avoid excessive object creation in hot paths** -- consider object pooling for expensive-to-create objects.
4. **Use primitive types instead of wrappers** to avoid heap allocation and autoboxing overhead.
5. **Set references to null** in long-lived data structures when elements are logically removed (like the stack example).
6. **Use `WeakHashMap`** or `SoftReference` for caches that should not prevent GC.
7. **Size collections appropriately** (`new ArrayList<>(expectedSize)`) to avoid repeated resizing and copying.
8. **Convert deep recursion to iteration** when stack depth is a concern.
9. **Monitor memory usage** with JMX (`MemoryMXBean`) or tools like VisualVM, JFR, and async-profiler.
10. **Tune JVM stack size** with `-Xss` only when necessary; the default (usually 512KB-1MB) is sufficient for most
    applications.

## Edge Cases and Their Handling

1. **Zero-length arrays**: `new int[0]` is a valid heap allocation that returns an empty array. This is useful as a
   return value instead of null.
2. **String pool edge cases**: `new String("hello")` creates a new heap object even though `"hello"` exists in the pool.
   Use `.intern()` to ensure pool usage.
3. **Integer cache**: `Integer.valueOf(127) == Integer.valueOf(127)` is `true` (cached), but
   `Integer.valueOf(128) == Integer.valueOf(128)` is `false` (new heap objects).
4. **Empty collections**: `Collections.emptyList()` returns a shared singleton -- no heap allocation per call.
5. **Stack overflow with virtual threads**: Virtual threads have dynamically growing stacks, so StackOverflowError
   thresholds differ from platform threads.
6. **Null references**: A null reference occupies stack space but points to no heap object. Dereferencing it throws
   `NullPointerException`.

## Interview-specific Insights

Interviewers frequently test:

- **Ability to diagram stack and heap** for a given code snippet (whiteboard question)
- **Understanding pass-by-value semantics** in Java (one of the most commonly confused topics)
- **Knowledge of when objects become eligible for GC** -- tracing reference chains
- **Memory leak identification** in code samples
- **Difference between StackOverflowError and OutOfMemoryError**
- **Escape analysis** awareness (shows deep JVM knowledge)
- **Reference types** (strong, soft, weak, phantom) and when to use each

Common tricky questions:
- "Does Java pass objects by reference?" (No -- it passes references by value)
- "Where are String literals stored?" (In the string pool, which is part of the heap since Java 7)
- "Can an object be allocated on the stack?" (Yes, through escape analysis / scalar replacement by JIT)
- "What happens to method local variables when the method returns?" (Stack frame is popped, locals are gone)

## Interview Q&A Section

**Q1: Explain the difference between stack and heap memory in Java. When is each used?**

```text
A1: Stack and heap serve different purposes in Java's memory model:

Stack Memory:
- Per-thread: each thread has its own stack
- Stores: method frames (local primitives, references, parameters, return addresses)
- Allocation: LIFO order, extremely fast (just move a pointer)
- Deallocation: automatic when method returns (frame is popped)
- Size: typically 512KB-1MB per thread (configurable via -Xss)
- Error: StackOverflowError when exhausted

Heap Memory:
- Shared: all threads access the same heap
- Stores: all objects and arrays (anything created with 'new')
- Allocation: requires finding free space, managed by GC
- Deallocation: handled by the garbage collector asynchronously
- Size: configurable via -Xms (initial) and -Xmx (maximum)
- Error: OutOfMemoryError when exhausted

Key insight: A local variable `Object obj = new Object()` puts the reference 'obj'
on the stack and the actual Object on the heap. When the method returns, the stack
reference is gone, making the heap object eligible for GC if no other references exist.
```

```java
// Demonstration of stack vs heap allocation
public class StackVsHeap {
    public void demonstrate() {
        int x = 42;                    // x is on the stack (primitive)
        String s = "hello";            // s (reference) on stack, String on heap (pool)
        List<String> list = new ArrayList<>();  // list ref on stack, ArrayList on heap

        // When this method returns:
        // - x, s, list references are all popped from the stack
        // - The ArrayList becomes eligible for GC if not referenced elsewhere
        // - The String "hello" stays in the string pool
    }
}
```

**Q2: What is escape analysis and how does it affect memory allocation?**

```text
A2: Escape analysis is a JIT compiler optimization that determines whether an object
allocated inside a method escapes that method's scope. If the object does NOT escape,
the JVM can apply several optimizations:

1. Scalar Replacement: Instead of allocating the object on the heap, the JVM breaks
   it into its individual fields and stores them as local variables on the stack.
   This eliminates heap allocation and GC overhead entirely.

2. Lock Elision: If a synchronized object doesn't escape, the JVM can remove the
   synchronization since no other thread can access it.

3. Stack Allocation: In some cases, the object may be allocated on the stack frame
   directly (though HotSpot primarily uses scalar replacement).

An object "escapes" if:
- It is returned from the method
- It is assigned to a field of another object
- It is passed to another method that might store it
- It is stored in a static field

Escape analysis is performed by the C2 JIT compiler after methods are identified as
"hot" (frequently called). It is NOT performed during interpretation.
```

```java
// Example: non-escaping object (candidate for scalar replacement)
public int computeDistance(int x1, int y1, int x2, int y2) {
    // This Point object does not escape the method
    // The JIT may replace it with two stack variables (dx, dy)
    record Point(int x, int y) {}
    Point p1 = new Point(x1, y1);
    Point p2 = new Point(x2, y2);
    int dx = p2.x() - p1.x();
    int dy = p2.y() - p1.y();
    return dx * dx + dy * dy;
}

// Example: escaping object (must be heap-allocated)
public List<String> createList() {
    List<String> list = new ArrayList<>();  // escapes via return
    list.add("item");
    return list;  // the list escapes this method
}
```

**Q3: What are the four types of references in Java and when would you use each?**

```text
A3: Java provides four reference types with different GC behavior:

1. Strong Reference (default):
   - Created with: Object obj = new Object();
   - GC behavior: Never collected while reachable
   - Use case: Normal object references in application code

2. Soft Reference (java.lang.ref.SoftReference):
   - Created with: SoftReference<Object> ref = new SoftReference<>(obj);
   - GC behavior: Collected only when JVM is running low on memory
   - Use case: Memory-sensitive caches (image caches, computed results)
   - The JVM guarantees soft refs are cleared before throwing OutOfMemoryError

3. Weak Reference (java.lang.ref.WeakReference):
   - Created with: WeakReference<Object> ref = new WeakReference<>(obj);
   - GC behavior: Collected at the next GC cycle if no strong refs exist
   - Use case: Canonicalizing mappings (WeakHashMap), listener registries
   - Does not prevent GC even if memory is plentiful

4. Phantom Reference (java.lang.ref.PhantomReference):
   - Created with: PhantomReference<Object> ref = new PhantomReference<>(obj, queue);
   - GC behavior: Enqueued after the referent is finalized; get() always returns null
   - Use case: Post-mortem cleanup, tracking object finalization, native resource cleanup
   - More reliable than finalize() for cleanup actions

The hierarchy of "strength": Strong > Soft > Weak > Phantom
```

```java
// Practical example: soft reference cache
public class ImageCache {
    private final Map<String, SoftReference<byte[]>> cache = new ConcurrentHashMap<>();

    public void cacheImage(String path, byte[] imageData) {
        cache.put(path, new SoftReference<>(imageData));
    }

    public byte[] getImage(String path) {
        SoftReference<byte[]> ref = cache.get(path);
        if (ref == null) return null;
        byte[] data = ref.get();
        if (data == null) {
            cache.remove(path); // soft ref was cleared by GC
        }
        return data; // may be null if GC collected it
    }
}

// Practical example: weak reference for listener management
public class EventBus {
    private final List<WeakReference<Runnable>> listeners = new CopyOnWriteArrayList<>();

    public void register(Runnable listener) {
        listeners.add(new WeakReference<>(listener));
    }

    public void fire() {
        listeners.removeIf(ref -> ref.get() == null); // cleanup dead refs
        listeners.forEach(ref -> {
            Runnable listener = ref.get();
            if (listener != null) listener.run();
        });
    }
}
```

**Q4: How does Java's pass-by-value work with objects? Show with code.**

```text
A4: Java is ALWAYS pass-by-value. There is no pass-by-reference in Java.

However, for objects, the "value" that is passed is the reference (pointer) to the object,
not the object itself. This means:

1. You CAN modify the object's state through the reference (because both the caller
   and the method have references pointing to the same heap object).

2. You CANNOT change what the caller's reference points to (because reassigning the
   parameter only changes the local copy of the reference).

This is often confused with pass-by-reference, but the distinction is critical:
- Pass-by-reference would allow the called method to make the caller's variable
  point to a different object.
- Pass-by-value of a reference only gives the method a copy of the pointer.

Common interview trap: "Java passes objects by reference" -- this is FALSE.
Java passes the value of the reference. The caller's reference variable cannot be
reassigned by the callee.
```

```java
public class PassByValueDemo {
    
    public static void modifyList(List<String> list) {
        list.add("modified");  // modifies the heap object -- visible to caller
    }

    public static void reassignList(List<String> list) {
        list = new ArrayList<>();  // only changes the LOCAL copy of the reference
        list.add("new list");      // this goes into the new list, not the caller's
    }

    public static void main(String[] args) {
        List<String> myList = new ArrayList<>(List.of("original"));

        modifyList(myList);
        System.out.println(myList); // [original, modified] -- state was changed

        reassignList(myList);
        System.out.println(myList); // [original, modified] -- reassignment had no effect

        // Primitive example:
        int x = 10;
        increment(x);
        System.out.println(x); // 10 -- unchanged, because primitives are copied
    }

    public static void increment(int val) {
        val++; // only changes the local copy
    }
}
```

**Q5: What is a memory leak in Java? Give three common patterns and their fixes.**

```text
A5: A memory leak in Java occurs when objects are no longer needed by the application
but are still reachable from GC roots, preventing garbage collection. Unlike C/C++
where you forget to call free(), Java memory leaks happen because references are
unintentionally kept alive.

Common patterns:

1. Unintentional object retention in collections:
   - Problem: Adding objects to a static or long-lived collection without removing them
   - Example: A static List that grows indefinitely
   - Fix: Use bounded collections, eviction policies, or weak references

2. Unclosed resources:
   - Problem: Streams, connections, or handles that are not closed
   - Example: Opening a FileInputStream without closing it
   - Fix: Always use try-with-resources for AutoCloseable objects

3. Listener/callback registration without deregistration:
   - Problem: Registering event listeners that are never removed
   - Example: GUI components registering observers but not unregistering on disposal
   - Fix: Always pair addListener with removeListener, or use WeakReference

Additional patterns:
- Inner class instances holding references to outer class (especially in Android)
- ThreadLocal variables not cleaned up after use
- Custom data structures (like Stack) not nulling out removed elements
- String.substring() in older Java versions (pre-Java 7u6) retaining the original char[]
```

```java
// Pattern 1: Collection-based leak and fix
public class CollectionLeak {
    // LEAK: objects added but never removed
    private static final List<byte[]> leakyCache = new ArrayList<>();
    
    public void leakyMethod() {
        leakyCache.add(new byte[1024 * 1024]); // 1MB added, never freed
    }

    // FIX: Use a bounded cache with eviction
    private static final Map<String, byte[]> boundedCache = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
            return size() > 100; // evict oldest when exceeding 100 entries
        }
    };
}

// Pattern 2: Resource leak and fix
public class ResourceLeak {
    // LEAK: exception causes resource to never close
    public void leakyRead(String path) throws IOException {
        InputStream is = new FileInputStream(path);
        is.read(); // if this throws, 'is' is never closed
    }

    // FIX: try-with-resources
    public void fixedRead(String path) throws IOException {
        try (InputStream is = new FileInputStream(path)) {
            is.read(); // 'is' is closed even on exception
        }
    }
}

// Pattern 3: Obsolete reference in custom data structure
public class FixedStack<E> {
    private Object[] elements;
    private int size = 0;

    public E pop() {
        if (size == 0) throw new EmptyStackException();
        E result = (E) elements[--size];
        elements[size] = null;  // CRITICAL: eliminate obsolete reference
        return result;
    }
}
```

**Q6: Explain the difference between StackOverflowError and OutOfMemoryError.**

```text
A6: Both are subclasses of VirtualMachineError (which extends Error), but they indicate
very different memory exhaustion scenarios:

StackOverflowError:
- Cause: The call stack for a thread exceeds its maximum size
- Typical trigger: Unbounded or very deep recursion
- Scope: Per-thread (each thread has its own stack)
- Stack size: Configurable with -Xss (default ~512KB-1MB)
- Prevention: Use iterative algorithms, limit recursion depth, increase -Xss
- Recovery: Generally recoverable by catching the error (the stack unwinds)

OutOfMemoryError:
- Cause: The JVM cannot allocate more memory for objects
- Subtypes:
  * "Java heap space" -- heap is full
  * "Metaspace" -- class metadata area full
  * "GC overhead limit exceeded" -- GC spending >98% time recovering <2% memory
  * "unable to create new native thread" -- OS thread limit reached
- Scope: JVM-wide (heap is shared)
- Heap size: Configurable with -Xms/-Xmx
- Prevention: Fix memory leaks, increase heap, optimize object usage
- Recovery: Very difficult to recover from; usually requires process restart

Key difference: StackOverflowError is about call depth (vertical growth).
OutOfMemoryError is about total object count/size (horizontal growth).
```

```java
// StackOverflowError example
public class StackOverflowDemo {
    // This will throw StackOverflowError
    public static int infiniteRecursion(int n) {
        return infiniteRecursion(n + 1); // no base case!
    }

    // Fix: add proper base case or convert to iteration
    public static long factorialIterative(int n) {
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }
}

// OutOfMemoryError example
public class OutOfMemoryDemo {
    // This will throw OutOfMemoryError: Java heap space
    public static void exhaustHeap() {
        List<byte[]> list = new ArrayList<>();
        while (true) {
            list.add(new byte[1024 * 1024]); // 1MB per iteration, never freed
        }
    }
}
```

**Q7: How do virtual threads (Project Loom) change stack memory management?**

```text
A7: Virtual threads, introduced as a preview in Java 19 and finalized in Java 21,
fundamentally change how stack memory is managed:

Traditional Platform Threads:
- Each thread gets a fixed-size stack (default ~1MB)
- Stack size is allocated upfront from OS memory
- 10,000 threads = ~10GB of stack memory alone
- This limits scalability for I/O-heavy applications

Virtual Threads:
- Stacks start very small (a few KB) and grow/shrink dynamically
- Stacks are stored on the heap (as continuation objects)
- When a virtual thread blocks on I/O, its stack is "parked" (unmounted)
  and the carrier thread can run another virtual thread
- Memory is only used for the actual stack depth, not a pre-allocated block
- Millions of virtual threads can coexist with modest memory

Implications:
1. Stack memory is no longer a limiting factor for thread count
2. -Xss has no effect on virtual threads
3. StackOverflowError can still occur but at different thresholds
4. Stack frames are stored on the heap, making them subject to GC
5. Pinning: synchronized blocks can prevent virtual thread unmounting,
   causing a carrier thread to be blocked

For interview: This shows how Java's memory model is evolving to support
modern concurrency patterns while maintaining backward compatibility.
```

```java
// Demonstrating virtual threads and their memory efficiency
public class VirtualThreadMemory {
    public static void main(String[] args) throws Exception {
        // Creating 100,000 virtual threads uses minimal stack memory
        // compared to 100,000 platform threads
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 100_000; i++) {
            Thread vt = Thread.ofVirtual().start(() -> {
                try {
                    Thread.sleep(1000); // blocked, stack is parked
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threads.add(vt);
        }

        // Wait for all to complete
        for (Thread t : threads) {
            t.join();
        }

        // Compare: this would likely cause OutOfMemoryError with platform threads
        // Thread.ofPlatform().start(() -> { ... }) x 100,000
    }
}
```

## Code Examples

- Test: [HeapStackMemoryTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/memorymanagement/HeapStackMemoryTest.java)
- Source: [HeapStackMemory.java](src/main/java/com/github/msorkhpar/claudejavatutor/memorymanagement/HeapStackMemory.java)
