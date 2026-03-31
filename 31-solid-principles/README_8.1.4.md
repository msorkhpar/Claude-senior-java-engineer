# 8.1.4. Interface Segregation Principle (ISP)

## Concept Explanation

The Interface Segregation Principle states that **clients should not be forced to depend on methods they do not use**. Instead of one large "fat" interface, create multiple smaller, focused interfaces so that implementing classes only need to know about the methods they actually need.

**Real-world analogy**: Think of a Swiss Army knife vs. specialized tools. A surgeon does not need a corkscrew, scissors, and a saw when all they need is a scalpel. Giving them a Swiss Army knife forces them to carry (depend on) tools they will never use. ISP says: give each professional exactly the tools (interface methods) they need -- a scalpel interface for the surgeon, a saw interface for the carpenter.

### 8.1.4.1. Definition and Purpose of ISP

Robert C. Martin introduced ISP to combat "fat interfaces" -- interfaces that accumulate methods for multiple different clients. When a fat interface changes, all implementing classes must be updated, even if the change is irrelevant to most of them.

The purpose:
- Reduce the impact of changes by limiting what each client depends on
- Improve cohesion of interfaces
- Make implementations simpler and more focused
- Enable better mocking and testing

### 8.1.4.2. Applying ISP in Concurrent Programming

In concurrent systems, ISP is particularly valuable:
- A `TaskSubmitter` client only needs `submit()` -- it should not depend on `shutdown()` or `getActiveCount()`
- A `LifecycleManager` only needs `shutdown()` and `awaitTermination()`
- A `Monitor` only needs `getActiveCount()` and `getCompletedCount()`

Each client depends only on its relevant interface, reducing coupling and making testing easier.

### 8.1.4.3. Designing Fine-grained Interfaces for Concurrency

Fine-grained interfaces for concurrent systems:
- `Readable<K, V>` -- read operations only
- `Writable<K, V>` -- write operations only
- `Listable<K>` -- enumeration operations only

A read-only client depends only on `Readable` and never sees write methods.

### 8.1.4.4. ISP and Minimizing Dependencies in Concurrent Systems

Minimizing dependencies reduces:
- The surface area for thread-safety bugs
- The number of methods that need synchronization
- The coupling between components that may run on different threads

## Key Points to Remember

- ISP is the interface-level equivalent of SRP -- one responsibility per interface
- "Fat interfaces" force implementations to provide stub methods or throw `UnsupportedOperationException` (an LSP smell)
- Java's default methods can partially mitigate fat interfaces but don't solve the fundamental coupling problem
- In concurrent code, ISP reduces the synchronization surface area
- Compose multiple small interfaces using `implements A, B, C` syntax
- ISP enables the Adapter pattern: wrap a fat interface with a thin one

## Relevant Java 21 Features

- **Functional interfaces**: The ultimate ISP -- one method per interface (`Predicate`, `Function`, `Consumer`)
- **Sealed interfaces**: Can be combined with ISP to define focused sealed hierarchies
- **Default methods**: Allow adding methods to interfaces without breaking implementations, but should not be used to create fat interfaces
- **Records**: Can implement multiple focused interfaces, providing clean ISP-compliant data carriers

## Common Pitfalls and How to Avoid Them

1. **The "God interface"**: One interface with 20+ methods that covers reading, writing, deleting, searching, backing up, and restoring.
   ```java
   // BAD: clients forced to depend on methods they don't use
   interface DataStore {
       String read(String key);
       void write(String key, String value);
       void delete(String key);
       void backup(String destination);
       void restore(String source);
       List<String> search(String query);
       void compact();
       Map<String, String> exportAll();
   }
   ```
   **Fix**: Split into `Readable`, `Writable`, `Searchable`, `Exportable`, etc.

2. **UnsupportedOperationException in implementations**:
   ```java
   class ReadOnlyStore implements DataStore {
       public void write(String k, String v) {
           throw new UnsupportedOperationException(); // ISP violation signal!
       }
   }
   ```
   **Fix**: The read-only store should only implement `Readable`, not the full `DataStore`.

3. **"Just in case" interface methods**: Adding methods to an interface because "someone might need them later."
   **Fix**: Follow YAGNI (You Ain't Gonna Need It). Add methods when there is a concrete client that needs them.

## Best Practices and Optimization Techniques

1. Start with the smallest possible interface -- one or two methods
2. Combine interfaces using `implements A, B` when a class legitimately needs multiple capabilities
3. Use the `@FunctionalInterface` annotation for single-method interfaces
4. In concurrent systems, separate read/write/lifecycle/monitoring interfaces
5. When refactoring a fat interface, identify client clusters (groups of clients that use the same subset of methods) and create one interface per cluster
6. Test implementations against the specific interface they implement, not the composed type

## Edge Cases and Their Handling

1. **Null keys and values**: Each interface should define its own null-handling contract
2. **Empty stores**: `Listable.listKeys()` should return an empty list, not null
3. **Null data maps in constructors**: Handle gracefully with `Map.of()` default
4. **Concurrent modification**: `ReadWriteStore` uses `ConcurrentHashMap` for thread safety; `ReadOnlyStore` wraps an immutable copy
5. **Shutdown race conditions**: The `ManagedExecutor` uses `AtomicBoolean` to prevent task submission after shutdown

## Interview-specific Insights

Interviewers often focus on:
- Identifying fat interfaces and explaining why they are problematic
- Refactoring a monolithic interface into segregated interfaces
- Understanding the relationship between ISP and the Adapter pattern
- Discussing ISP in the context of the Java Collections framework

Common tricky questions:
- "Is `java.util.List` a violation of ISP?" (Arguable -- it has both read and write methods, but the "optional operations" pattern partially addresses this)
- "How do you balance ISP with having too many tiny interfaces?"
- "When does Java's `default` method mechanism help or hurt ISP?"

## Interview Q&A Section

**Q1: What is the Interface Segregation Principle and why is it important?**

```text
A1: The Interface Segregation Principle states that clients should not be forced 
to depend on methods they do not use. Instead of one large interface, create 
multiple smaller, focused interfaces.

Why it is important:
1. Reduces coupling: Clients only depend on methods they actually call
2. Simplifies implementations: Classes only implement relevant methods
3. Improves testability: Smaller interfaces are easier to mock
4. Reduces change impact: Adding a method to one interface doesn't affect 
   clients of other interfaces
5. Prevents UnsupportedOperationException: Classes don't need stub methods

ISP is the interface-level equivalent of SRP. Just as SRP says a class should 
have one reason to change, ISP says an interface should serve one client role.
```

```java
// FAT interface -- violates ISP
interface Worker {
    void code();
    void test();
    void deploy();
    void manageSprint();
    void reviewCode();
    void conductInterview();
}

// ISP-compliant: segregated interfaces
interface Developer {
    void code();
    void test();
}

interface DevOps {
    void deploy();
}

interface ScrumMaster {
    void manageSprint();
}

// A senior developer implements what they actually do
class SeniorDeveloper implements Developer, DevOps {
    public void code() { /* ... */ }
    public void test() { /* ... */ }
    public void deploy() { /* ... */ }
}

// A junior developer only codes and tests
class JuniorDeveloper implements Developer {
    public void code() { /* ... */ }
    public void test() { /* ... */ }
}
```

**Q2: How does ISP apply to concurrent systems?**

```text
A2: In concurrent systems, ISP is particularly valuable because:

1. Thread-safety surface reduction: A client that only submits tasks doesn't 
   need to see shutdown() or monitoring methods. This means fewer methods 
   need thread-safety analysis in that client's context.

2. Lock contention isolation: Read-only clients can use lock-free paths 
   when they depend only on a Readable interface, while write clients 
   synchronize separately.

3. Lifecycle management separation: The component that submits tasks should 
   not be responsible for shutting down the executor. ISP naturally separates 
   these concerns.

4. Testing isolation: You can test task submission without setting up monitoring. 
   You can test monitoring without submitting tasks. Each test focuses on one 
   interface.

5. Dependency injection clarity: It's clear from a constructor what a class 
   actually needs: does it need TaskSubmitter or LifecycleManageable or both?
```

```java
// Segregated interfaces for a concurrent executor
interface TaskSubmitter<T> {
    Future<T> submit(Callable<T> task);
}

interface LifecycleManageable {
    void shutdown();
    boolean isShutdown();
    boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;
}

interface Monitorable {
    int getActiveCount();
    long getCompletedCount();
}

// Full implementation composes all three
class ManagedExecutor implements TaskSubmitter<Object>, LifecycleManageable, Monitorable {
    // ... implements all methods
}

// Client A: only needs to submit tasks
class TaskClient {
    private final TaskSubmitter<Object> submitter; // minimal dependency
    TaskClient(TaskSubmitter<Object> submitter) { this.submitter = submitter; }
    void run() { submitter.submit(() -> "work"); }
}

// Client B: only needs lifecycle management
class LifecycleManager {
    private final LifecycleManageable manageable; // minimal dependency
    void gracefulShutdown() { manageable.shutdown(); }
}
```

**Q3: How do you refactor a fat interface into segregated interfaces?**

```text
A3: Step-by-step refactoring process:

1. Identify client clusters: Group clients by which methods they call.
   - Client A calls read(), exists(), listKeys()
   - Client B calls write(), delete()
   - Client C calls backup(), restore()

2. Create one interface per cluster:
   - Readable: read(), exists()
   - Writable: write(), delete()
   - Listable: listKeys(), size()
   - Backupable: backup(), restore()

3. Update implementations to implement the relevant interfaces:
   - ReadWriteStore implements Readable, Writable, Listable
   - ReadOnlyStore implements Readable, Listable

4. Update client code to depend on the specific interface:
   - Read-only service takes Readable<String, String>
   - Admin service takes Readable + Writable + Listable

5. Run tests to verify nothing broke.

The key insight: start from the CLIENTS, not the implementation. The interfaces 
should be shaped by what clients need, not by what the implementation offers.
```

```java
// Before: fat interface
interface DataStore {
    String read(String key);
    void write(String key, String value);
    void delete(String key);
    List<String> listKeys();
    int size();
}

// After: segregated
interface Readable<K, V> {
    V read(K key);
    boolean exists(K key);
}

interface Writable<K, V> {
    void write(K key, V value);
    void delete(K key);
}

interface Listable<K> {
    List<K> listKeys();
    int size();
    boolean isEmpty();
}

// Implementation composes what it needs
class ReadWriteStore implements Readable<String, String>,
                                Writable<String, String>,
                                Listable<String> {
    private final ConcurrentHashMap<String, String> data = new ConcurrentHashMap<>();
    // ... implement all methods
}

// Read-only implementation -- no write stubs needed!
class ReadOnlyStore implements Readable<String, String>, Listable<String> {
    private final Map<String, String> data;
    // ... implement only read and list methods
}
```

**Q4: What is the relationship between ISP and the Java Collections framework?**

```text
A4: The Java Collections framework has a complicated relationship with ISP:

Partial ISP compliance:
- Collection, List, Set, Map are separate interfaces (good ISP)
- Iterable is segregated from Collection (good ISP)
- Iterator is a focused interface with just hasNext(), next(), remove()

ISP violations:
- List includes both read methods (get, indexOf) and write methods (add, set, 
  remove) in one interface
- This forces Collections.unmodifiableList() to throw UnsupportedOperationException 
  on write methods
- Map similarly combines read and write operations

Modern improvements:
- List.of(), Set.of(), Map.of() create unmodifiable collections, but they still 
  implement the full List/Set/Map interface
- SequencedCollection (Java 21) adds new methods but to existing interfaces

If Java were redesigned today, you might see:
- ReadableList, WritableList, MutableList
- ReadableMap, WritableMap, MutableMap
But backward compatibility prevents this change.
```

```java
// Java Collections ISP tension
List<String> unmodifiable = List.of("a", "b", "c");
// unmodifiable.add("d");  // Throws UnsupportedOperationException

// If Java had ISP-compliant collections:
interface ReadableList<E> {
    E get(int index);
    int size();
    boolean contains(Object o);
}

interface WritableList<E> extends ReadableList<E> {
    void add(E e);
    void remove(int index);
    E set(int index, E e);
}

// Read-only clients would only see ReadableList
void printAll(ReadableList<String> list) {
    for (int i = 0; i < list.size(); i++) {
        System.out.println(list.get(i));
    }
}
```

**Q5: How do you balance ISP with avoiding too many small interfaces?**

```text
A5: The balance comes from focusing on CLIENT NEEDS, not theoretical purity:

Signs of too many interfaces:
1. Most classes implement 5+ interfaces with 1 method each
2. Interfaces are so granular that no client uses just one
3. Interface names become meaningless (IDoOneThing, IDoAnotherThing)
4. Navigation in the IDE becomes difficult
5. New developers are overwhelmed by the number of types

Signs of too few interfaces:
1. Implementations throw UnsupportedOperationException
2. Clients depend on methods they never call
3. Mock setup in tests is complex because of unused methods
4. Interface changes affect unrelated code

Practical guidelines:
1. Start with 2-3 method interfaces -- the "role" level
2. Group methods by client usage, not by implementation similarity
3. If two methods are ALWAYS used together, they belong in one interface
4. If a method is used by a DIFFERENT set of clients, it belongs in a separate interface
5. Refactor when you see pain, not preemptively
6. Java functional interfaces (@FunctionalInterface) are the natural minimum granularity
```

```java
// Too granular -- over-segregation
interface Readable { String read(String key); }
interface Existable { boolean exists(String key); }
interface Sizable { int size(); }
interface Emptyable { boolean isEmpty(); }

// Better: group by role
interface Readable<K, V> {
    V read(K key);
    boolean exists(K key);  // Almost always needed alongside read
}

interface Listable<K> {
    List<K> listKeys();
    int size();        // Natural companion to listing
    boolean isEmpty();  // Derived from size, always useful with listing
}

// Each interface represents a meaningful client role, not just one method
```

## Code Examples

- Source: [InterfaceSegregation.java](src/main/java/com/github/msorkhpar/claudejavatutor/solidprinciples/InterfaceSegregation.java)
- Test: [InterfaceSegregationTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/solidprinciples/InterfaceSegregationTest.java)
