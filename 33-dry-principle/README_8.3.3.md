# 8.3.3. Benefits of DRY for Concurrency

## Concept Explanation

Applying DRY to concurrent code yields two critical benefits: **improved code maintainability** and **consistency in concurrent behavior**. Concurrency bugs are notoriously difficult to reproduce and debug because they depend on thread scheduling, timing, and memory visibility. When synchronization logic is duplicated, each copy becomes an independent source of potential race conditions, deadlocks, or visibility issues. DRY ensures there is one correct implementation that all code paths share.

**Real-world analogy**: Consider a fleet of delivery trucks. If each truck has its own navigation system with its own map data, updating a road closure requires updating every truck individually. Miss one, and that truck takes the wrong route. A centralized navigation service (DRY) pushes updates once, and every truck immediately benefits. The same principle applies to concurrent code: one locking strategy, one cache implementation, one retry policy -- all consumers benefit from a single fix.

This section covers:
- **8.3.3.1. Improved code maintainability** -- changing synchronization strategy in one place propagates everywhere.
- **8.3.3.2. Consistency in concurrent behavior** -- all code paths use the same thread-safe mechanism, preventing mixed-mode bugs.

## Key Points to Remember

- A bug fix in a shared synchronization utility automatically fixes every caller.
- DRY reduces the testing surface for concurrency -- test the utility once, trust it everywhere.
- Mixed synchronization strategies (some code uses `synchronized`, others use `ReentrantLock`, others use `AtomicInteger`) cause reasoning complexity and potential bugs.
- Consistent concurrent behavior means deterministic outcomes regardless of which code path executes.
- DRY-compliant concurrent code is easier to audit for correctness by security and performance reviewers.
- Read-write lock wrappers (`ReadWriteLockedResource<T>`) let multiple repositories share the same locking strategy.

## Relevant Java 21 Features

- **Virtual threads**: When all code uses the same executor abstraction, migrating from platform threads to virtual threads is a one-line change.
- **Scoped values (JEP 446, preview)**: Replace `ThreadLocal` with scoped values -- DRY enables this migration in a single shared utility.
- **`ExecutorService` as `AutoCloseable`**: Consistent executor lifecycle management through try-with-resources.
- **Records**: Immutable DTOs returned from concurrent utilities avoid shared mutable state.

## Common Pitfalls and How to Avoid Them

1. **Mixed locking strategies across repositories**
   ```java
   // UserRepo uses synchronized, ProductRepo uses ReentrantLock
   // Reasoning about their interaction becomes very difficult
   ```
   **Solution**: Use a shared `ReadWriteLockedResource<T>` wrapper for all repositories.

2. **Inconsistent counter implementations**
   ```java
   // CounterA uses synchronized, CounterB uses AtomicInteger
   // Hard to reason about composability and visibility guarantees
   ```
   **Solution**: Standardize on a single `ConsistentCounter` abstraction.

3. **Updating locking logic in one class but forgetting another**
   ```java
   // Fixed a deadlock in UserService by changing lock ordering
   // OrderService still has the old ordering -- deadlock persists
   ```
   **Solution**: Centralize lock management in a shared utility.

4. **Event handling with inconsistent thread safety**
   ```java
   // Some event handlers use CopyOnWriteArrayList, others use synchronized ArrayList
   ```
   **Solution**: Use a single `SimpleEventBus` with consistent internal synchronization.

## Best Practices and Optimization Techniques

1. **Use `ReadWriteLockedResource<T>`** for read-heavy workloads: readers don't block each other, only writers block.
2. **Wrap all repositories with the same locking abstraction**: changing from `ReentrantReadWriteLock` to `StampedLock` requires modifying only one class.
3. **Standardize on `AtomicInteger` for counters**: simpler, faster, and lock-free compared to `synchronized`.
4. **Use a thread-safe event bus for publish-subscribe**: `ConcurrentHashMap` for topics + `CopyOnWriteArrayList` for listeners is a well-tested pattern.
5. **Test the shared utility exhaustively with concurrent stress tests**: once proven correct, all callers inherit the guarantee.

## Edge Cases and Their Handling

1. **Null resources**: `ReadWriteLockedResource(null)` should throw immediately -- fail fast before any locking occurs.
2. **Null read/write actions**: Reject null lambdas before acquiring locks.
3. **Counter overflow**: `AtomicInteger` wraps at `Integer.MAX_VALUE` -- for unbounded counts, use `AtomicLong` or `LongAdder`.
4. **Event bus with no listeners**: `publish()` on a topic with no subscribers should be a no-op, not throw.
5. **Empty repository queries**: `getAllUsers()` on an empty repository should return an empty list, not null.

## Interview-specific Insights

Interviewers focus on:
- How you ensure consistent synchronization across a codebase
- Your understanding of why mixed locking strategies cause subtle bugs
- Ability to articulate the maintenance cost of duplicated concurrency code
- Knowledge of DRY patterns like `ReadWriteLockedResource<T>` and event buses

Tricky questions:
- "What happens when you need to change from `ReentrantReadWriteLock` to `StampedLock`?" (With DRY: one class change. Without: every repository.)
- "How do you test that all code paths use the same synchronization?" (Code review + shared abstraction that makes it structurally impossible to diverge.)

## Interview Q&A Section

**Q1: How does DRY improve maintainability of concurrent code?**

```text
A1: When synchronization logic lives in a shared abstraction (e.g., ReadWriteLockedResource),
maintenance improves in several ways:

1. Single point of change: Switching from ReentrantReadWriteLock to StampedLock requires
   editing only the wrapper class.
2. Single point of testing: Concurrency tests focus on the shared utility.
3. Reduced code review burden: Reviewers verify the locking pattern once, not per repository.
4. Easier migration: Moving to virtual threads or structured concurrency affects one utility.

Without DRY, each repository independently implements locking, and a strategy change
requires shotgun surgery across the entire codebase.
```

```java
// DRY: Shared read-write locking wrapper
public class ReadWriteLockedResource<T> {
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final T resource;

    public <R> R read(Function<T, R> action) {
        rwLock.readLock().lock();
        try { return action.apply(resource); }
        finally { rwLock.readLock().unlock(); }
    }

    public void writeVoid(Consumer<T> action) {
        rwLock.writeLock().lock();
        try { action.accept(resource); }
        finally { rwLock.writeLock().unlock(); }
    }
}

// Both repos use the same locking -- change once, applied everywhere
class UserRepo {
    private final ReadWriteLockedResource<Map<String, String>> data = ...;
    public String getUser(String id) { return data.read(m -> m.get(id)); }
}
class ProductRepo {
    private final ReadWriteLockedResource<Map<String, Double>> data = ...;
    public Double getPrice(String name) { return data.read(m -> m.get(name)); }
}
```

**Q2: Why are inconsistent synchronization strategies dangerous?**

```text
A2: When different parts of a codebase use different synchronization mechanisms
(synchronized, ReentrantLock, AtomicInteger, volatile), several problems arise:

1. Composability issues: synchronized blocks and ReentrantLock blocks don't
   interoperate -- holding one doesn't prevent the other from proceeding.
2. Visibility confusion: volatile provides visibility but not atomicity;
   synchronized provides both -- mixing them creates false safety assumptions.
3. Reasoning complexity: Developers must track which mechanism each class uses,
   increasing cognitive load during code review.
4. Deadlock risk: Different lock ordering in different mechanisms makes deadlock
   analysis much harder.

DRY eliminates this by standardizing on a single mechanism across the codebase.
```

```java
// VIOLATION: Mixed mechanisms -- hard to reason about interactions
class ServiceA {
    private synchronized void update() { /* uses intrinsic lock */ }
}
class ServiceB {
    private final Lock lock = new ReentrantLock();
    void update() { lock.lock(); try { /*...*/ } finally { lock.unlock(); } }
}

// DRY: Consistent mechanism everywhere
class ServiceA {
    private final LockExecutor locker = new LockExecutor(new ReentrantLock());
    void update() { locker.withLockRun(() -> { /*...*/ }); }
}
class ServiceB {
    private final LockExecutor locker = new LockExecutor(new ReentrantLock());
    void update() { locker.withLockRun(() -> { /*...*/ }); }
}
```

**Q3: How does a consistent counter abstraction prevent concurrency bugs?**

```text
A3: Without DRY, different counters use different mechanisms:
- Counter A: synchronized increment/get
- Counter B: AtomicInteger.incrementAndGet()
- Counter C: volatile int with non-atomic increment (BUG!)

A single ConsistentCounter class using AtomicInteger ensures:
1. All increments are atomic -- no lost updates.
2. All reads see the latest value -- guaranteed by AtomicInteger's memory semantics.
3. No lock contention -- AtomicInteger is lock-free.
4. Consistent API -- increment(), decrement(), get(), reset().

If the team later decides to switch to LongAdder for high-contention scenarios,
only the ConsistentCounter class changes.
```

```java
public class ConsistentCounter {
    private final AtomicInteger count = new AtomicInteger(0);

    public int increment() { return count.incrementAndGet(); }
    public int decrement() { return count.decrementAndGet(); }
    public int get() { return count.get(); }
    public void reset() { count.set(0); }
}

// All code uses the same counter -- consistent behavior guaranteed
ConsistentCounter requestCount = new ConsistentCounter();
ConsistentCounter errorCount = new ConsistentCounter();
```

**Q4: How does a thread-safe event bus demonstrate DRY benefits for concurrency?**

```text
A4: A SimpleEventBus centralizes publish-subscribe synchronization:

1. ConcurrentHashMap for topic registration -- thread-safe without external locking.
2. CopyOnWriteArrayList for listener lists -- safe iteration during concurrent modification.
3. All event types use the same mechanism -- no inconsistency risk.

Without DRY, each component implements its own listener management:
- ComponentA uses synchronized ArrayList
- ComponentB uses CopyOnWriteArrayList
- ComponentC uses unsynchronized ArrayList (BUG!)

The event bus ensures all publish-subscribe behavior is consistent and thread-safe.
```

```java
public class SimpleEventBus {
    private final Map<String, List<Consumer<Object>>> listeners = new ConcurrentHashMap<>();

    public void subscribe(String eventType, Consumer<Object> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public void publish(String eventType, Object event) {
        List<Consumer<Object>> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.forEach(l -> l.accept(event));
        }
    }
}
```

**Q5: What are the testing benefits of DRY concurrent code?**

```text
A5: DRY concurrent code dramatically reduces the testing burden:

1. Test the shared utility once with stress tests (multiple threads, race conditions,
   edge cases) and trust it everywhere.
2. Consumer classes only need to test their business logic, not their synchronization.
3. Easier to write deterministic tests -- mock or control the shared utility.
4. Easier to achieve coverage -- one set of concurrency tests covers all consumers.

Without DRY, each class with its own locking needs its own concurrency tests.
A codebase with 10 repositories each implementing ReadWriteLock needs 10 concurrent
test suites. With a shared ReadWriteLockedResource, you need 1.
```

```java
// Test the shared utility exhaustively ONCE
@Test
void readWriteLockedResource_concurrentAccess() throws Exception {
    var resource = new ReadWriteLockedResource<>(new HashMap<String, String>());
    int threads = 100;
    ExecutorService executor = Executors.newFixedThreadPool(threads);

    // Concurrent writes
    for (int i = 0; i < threads; i++) {
        final int idx = i;
        executor.submit(() -> resource.writeVoid(m -> m.put("key" + idx, "val" + idx)));
    }
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);

    // Verify all writes are visible
    int size = resource.read(Map::size);
    assertThat(size).isEqualTo(threads);
}
```

## Code Examples

- Test: [DryBenefitsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/dryprinciple/DryBenefitsTest.java)
- Source: [DryBenefits.java](src/main/java/com/github/msorkhpar/claudejavatutor/dryprinciple/DryBenefits.java)
