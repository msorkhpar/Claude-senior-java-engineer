# 5.3.2. Atomicity Violations and Ensuring Atomic Operations

## Concept Explanation

An **atomicity violation** occurs when an operation that should be indivisible is interrupted midway by another thread,
leaving the system in an inconsistent state. In concurrent programming, "atomic" means that an operation either
completes in full or not at all — from the perspective of other threads, there is no intermediate state.

**Real-world analogy**: Consider a bank transfer: moving $100 from Account A to Account B requires two steps — debit A
and credit B. If the system crashes (or another thread reads the balances) between those two steps, Account A has lost
$100 but Account B has not received it. This is an atomicity violation. The two-step transfer must be an atomic
transaction — both steps happen together or neither does.

In Java, the following operations are NOT automatically atomic:
- `i++` or `i--` (read-modify-write on any type)
- `i += n` for any n
- Any compound operation on multiple variables (e.g., updating both `x` and `y` to maintain an invariant)
- `check-then-act` sequences (even with individually synchronized operations)
- Any multi-step update to a data structure

### Categories of Atomicity Violations

1. **Single-variable compound operations**: `i++`, `i += n`, conditional assignment
2. **Multi-variable invariants**: Two fields that must always satisfy a relationship
3. **Check-then-act**: Reading state, making a decision, acting on it — all three must be atomic
4. **Read-modify-write on collections**: Iterating and modifying a collection

## Key Points to Remember

1. The JVM guarantees atomic reads and writes for `int`, `char`, `byte`, `short`, `boolean`, `float`, and object
   references — but NOT for `long` and `double` (which may be torn on 32-bit JVMs).
2. `volatile` makes reads and writes of all types (including `long`/`double`) atomic for single operations.
3. `AtomicInteger`, `AtomicLong`, `AtomicReference`, and other `java.util.concurrent.atomic` classes provide
   compound-atomic operations via Compare-And-Swap (CAS).
4. `synchronized` blocks/methods make an arbitrary sequence of operations appear atomic to other threads.
5. `java.util.concurrent.locks.Lock` and `StampedLock` provide finer-grained atomicity control.
6. CAS (Compare-And-Swap) is a hardware primitive that enables lock-free atomic operations.
7. ABA problem: A value can be changed from A to B back to A between CAS attempts — `AtomicStampedReference` solves this.

## Relevant Java 21 Features

- **`VarHandle`** (Java 9+): Provides access to variable-level memory ordering and atomic operations with more
  flexibility than `AtomicInteger`. Includes `compareAndSet`, `getAndAdd`, and memory-fence operations.
- **`AtomicReferenceFieldUpdater`** and `VarHandle`: Can apply atomic operations to regular fields, avoiding the overhead
  of wrapper classes.
- **Virtual threads (Project Loom)**: Virtual threads make blocking synchronization (synchronized, ReentrantLock) much
  less costly in terms of system resources, reducing the appeal of complex lock-free algorithms for most use cases.
- **Structured concurrency**: Helps reason about which tasks share state and when state must be synchronized.
- **`LongAccumulator` and `LongAdder`**: Introduced in Java 8, these specialized classes outperform `AtomicLong` in
  high-contention write scenarios by using internal striping.

## Common Pitfalls and How to Avoid Them

### 1. Non-atomic compound operations on shared state

```java
// PROBLEM: counter++ is not atomic
class NonAtomicIncrement {
    private int counter = 0;

    public void increment() {
        counter++; // Compiles to: read counter, add 1, write counter — 3 steps!
    }
    // Two threads calling increment() 1000x each may yield counter < 2000
}

// SOLUTION: AtomicInteger
class AtomicIncrement {
    private final AtomicInteger counter = new AtomicInteger(0);

    public void increment() {
        counter.incrementAndGet(); // Atomic CAS-based operation
    }

    public int get() {
        return counter.get();
    }
}
```

### 2. Check-then-act races

```java
// PROBLEM: Non-atomic check-then-act on a map
class NonAtomicCheckThenAct {
    private final Map<String, Integer> map = new HashMap<>();

    public void initializeIfAbsent(String key, int value) {
        if (!map.containsKey(key)) {    // check
            map.put(key, value);         // act — race: another thread may have inserted by now!
        }
    }
}

// SOLUTION: ConcurrentHashMap.putIfAbsent or computeIfAbsent
class AtomicCheckThenAct {
    private final ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

    public void initializeIfAbsent(String key, int value) {
        map.putIfAbsent(key, value); // Atomic check-and-insert
    }

    // Even better for expensive computations — compute is called at most once per key
    public Integer getOrCompute(String key) {
        return map.computeIfAbsent(key, k -> expensiveComputation(k));
    }
}
```

### 3. Non-atomic multi-variable updates

```java
// PROBLEM: Two fields must always be consistent, but they're updated separately
class NonAtomicCoordinate {
    private int x = 0;
    private int y = 0;

    public void moveTo(int newX, int newY) {
        x = newX; // A reader may see x updated but not y
        y = newY;
    }

    public String getPosition() {
        return "(" + x + ", " + y + ")"; // May read an inconsistent state!
    }
}

// SOLUTION 1: synchronized
class SynchronizedCoordinate {
    private int x = 0;
    private int y = 0;

    public synchronized void moveTo(int newX, int newY) {
        x = newX;
        y = newY;
    }

    public synchronized String getPosition() {
        return "(" + x + ", " + y + ")";
    }
}

// SOLUTION 2: Immutable value + volatile reference (lock-free)
class ImmutableCoordinate {
    private record Point(int x, int y) {}
    private volatile Point position = new Point(0, 0);

    public void moveTo(int newX, int newY) {
        position = new Point(newX, newY); // Atomic reference replacement
    }

    public String getPosition() {
        Point p = position; // Atomic volatile read — consistent snapshot
        return "(" + p.x() + ", " + p.y() + ")";
    }
}
```

### 4. Iterating and modifying a list

```java
// PROBLEM: Concurrent modification during iteration
class ConcurrentModificationProblem {
    private final List<String> list = new ArrayList<>();

    public void addAndClean(String item) {
        list.add(item);
        list.removeIf(s -> s.length() > 10); // ConcurrentModificationException if another thread adds!
    }
}

// SOLUTION: CopyOnWriteArrayList (read-heavy workloads)
class CopyOnWriteSolution {
    private final List<String> list = new CopyOnWriteArrayList<>();
    // All mutating operations create a new copy — iterators are always safe
}
```

## Best Practices and Optimization Techniques

1. **Prefer high-level abstractions**: `ConcurrentHashMap`, `CopyOnWriteArrayList`, `BlockingQueue` are designed for
   concurrent use and eliminate most atomicity concerns.
2. **Use atomic classes**: `AtomicInteger`, `AtomicLong`, `AtomicReference`, `LongAdder` for single-variable
   compound operations without locking.
3. **Use synchronized for multi-variable invariants**: When multiple fields must be consistent together, synchronize
   all accesses.
4. **Minimize lock scope**: Hold locks only as long as necessary to reduce contention.
5. **Use `LongAdder` over `AtomicLong` under high contention**: `LongAdder` uses internal striping to reduce CAS
   failures.
6. **Prefer immutable value objects**: Replacing mutable state with immutable snapshots + volatile reference
   eliminates multi-variable atomicity issues.
7. **Avoid holding locks during I/O or long computations**: This starves other threads. Compute outside the lock, then
   apply results atomically.

## Edge Cases and Their Handling

### 1. ABA Problem in CAS-based algorithms

```java
// ABA: A -> B -> A between CAS attempts — CAS succeeds incorrectly
class ABAProblemDemo {
    private final AtomicReference<String> ref = new AtomicReference<>("A");

    public void demonstrateABA() {
        String expected = ref.get(); // Reads "A"
        // Another thread changes A -> B -> A
        // Our CAS: is ref still "A"? Yes — but the intermediate change is invisible!
        ref.compareAndSet(expected, "C"); // Succeeds, but shouldn't in some algorithms
    }

    // Fix: AtomicStampedReference tracks version/stamp alongside the value
    private final AtomicStampedReference<String> stampedRef =
        new AtomicStampedReference<>("A", 0);

    public boolean safeCompareAndSet(String expected, String update) {
        int[] stamp = new int[1];
        String current = stampedRef.get(stamp);
        return stampedRef.compareAndSet(current, update, stamp[0], stamp[0] + 1);
    }
}
```

### 2. Spurious failures in compareAndSet

CAS can fail spuriously (return false even when the expected value matches) on some architectures. Always use CAS in a
retry loop:

```java
class CASRetryLoop {
    private final AtomicInteger value = new AtomicInteger(0);

    public void incrementIfPositive() {
        int current;
        do {
            current = value.get();
            if (current <= 0) return; // Condition not met — give up
        } while (!value.compareAndSet(current, current + 1)); // Retry on failure
    }
}
```

### 3. Overflow-safe atomic accumulation

```java
class OverflowSafeAccumulator {
    private final LongAccumulator accumulator =
        new LongAccumulator(Long::max, Long.MIN_VALUE); // Tracks the maximum

    public void observe(long value) {
        accumulator.accumulate(value);
    }

    public long getMax() {
        return accumulator.get();
    }
}
```

## Interview-specific Insights

Interviewers focus on:
- The difference between atomicity and visibility (two orthogonal concerns)
- Why `synchronized` provides both atomicity AND visibility (volatile only provides visibility)
- The performance trade-offs between `synchronized`, `volatile`, and `Atomic*` classes
- Understanding of CAS and its limitations (ABA problem, spin-loop overhead)
- When to use `LongAdder` vs. `AtomicLong`

Common tricky questions:
- "Is reading a `volatile long` atomic?" — Yes, volatile makes long/double reads atomic.
- "Can two `synchronized` methods on different objects deadlock?" — Yes, if they both try to lock each other's monitors.
- "What is the difference between `AtomicInteger.getAndIncrement()` and `incrementAndGet()`?" — Return value: old vs.
  new. Semantically equivalent for thread safety.

## Interview Q&A Section

**Q1: What is the difference between atomicity and visibility in concurrent programming?**

```text
A1: Atomicity and visibility are two distinct and orthogonal properties in concurrent programming,
both of which must be ensured for correct multi-threaded code.

Atomicity means that a set of operations appears as a single, indivisible action from the
perspective of other threads. No other thread can observe an intermediate state. For example,
a bank transfer (debit + credit) must be atomic — no thread should ever see the debit without the
corresponding credit.

Visibility means that when one thread writes a value, that write becomes visible to other threads.
Without proper visibility guarantees, a thread may read a stale value from its CPU cache even
though another thread has written a new value to main memory.

Relationship:
- synchronized provides BOTH atomicity (mutual exclusion) AND visibility (memory flush/reload).
- volatile provides ONLY visibility (no atomicity for compound operations).
- AtomicInteger provides atomicity for specific operations (via CAS) AND visibility.

A common mistake is using volatile thinking it provides atomicity — it does not!
volatile int counter = 0; counter++; // Still not atomic!
```

```java
class AtomicityVsVisibility {
    // volatile: visible but NOT atomic for compound operations
    private volatile int visibilityOnly = 0;

    // AtomicInteger: both visible and atomic
    private final AtomicInteger atomicAndVisible = new AtomicInteger(0);

    // synchronized: provides both atomicity and visibility
    private int bothAtomicAndVisible = 0;

    // NOT thread-safe: visible write but compound operation is still racy
    public void unsafeIncrement() {
        visibilityOnly++; // Read, add, write — 3 steps — racy!
    }

    // Thread-safe: CAS provides atomicity, and AtomicInteger ensures visibility
    public void safeAtomicIncrement() {
        atomicAndVisible.incrementAndGet();
    }

    // Thread-safe: synchronized provides both mutual exclusion and memory flushing
    public synchronized void safeSynchronizedIncrement() {
        bothAtomicAndVisible++;
    }
}
```

**Q2: Explain Compare-And-Swap (CAS) and how it enables lock-free programming.**

```text
A2: Compare-And-Swap (CAS) is a hardware-level atomic instruction supported by modern CPUs. It
takes three operands:
- V: the memory location to update
- E: the expected value
- N: the new value

Semantics: "If the current value of V equals E, atomically replace it with N. Return whether the
swap occurred (true/false)."

The key insight is that this is done atomically at the hardware level — no interrupt or context
switch can occur between the compare and the swap.

Lock-free algorithms use CAS in a retry loop:
1. Read current state
2. Compute desired new state
3. Try CAS — if it succeeds, we're done; if it fails, another thread changed the value, so retry

Advantages over locks:
- No deadlocks (threads never block each other)
- Better throughput under low contention
- More resilient to thread preemption

Disadvantages:
- ABA problem (resolved by AtomicStampedReference)
- Live-lock: threads can spin indefinitely under very high contention
- Complex to implement correctly for non-trivial data structures
```

```java
class CASDemo {
    private final AtomicInteger counter = new AtomicInteger(0);

    // Manual CAS retry loop — demonstrates how AtomicInteger works internally
    public void incrementByN(int n) {
        int current;
        int next;
        do {
            current = counter.get();          // Read
            next = current + n;               // Compute
        } while (!counter.compareAndSet(current, next)); // Swap or retry
    }

    // Using VarHandle for direct field CAS (Java 9+)
    private int rawField = 0;
    private static final java.lang.invoke.VarHandle RAW_FIELD;

    static {
        try {
            RAW_FIELD = java.lang.invoke.MethodHandles.lookup()
                .findVarHandle(CASDemo.class, "rawField", int.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public boolean casOnRawField(int expected, int newValue) {
        return RAW_FIELD.compareAndSet(this, expected, newValue);
    }
}
```

**Q3: When should you use `LongAdder` instead of `AtomicLong`?**

```text
A3: LongAdder (and its sibling DoubleAdder) are designed for scenarios with high write contention
and infrequent reads. They achieve this by using an array of "cells" (a technique called striping),
where threads update different cells concurrently. The sum() method adds all cells together.

Use LongAdder when:
1. Multiple threads frequently increment/add (high write contention)
2. You only occasionally need the total (sum() is infrequent)
3. You don't need atomic "get-and-set" semantics

Use AtomicLong when:
1. You need get-and-set, compareAndSet, or similar compound read-modify-write operations
2. Write contention is low to moderate
3. You need the current value immediately after an operation (not just eventual sum)
4. You need atomic conditional updates (compareAndSet)

Performance rule of thumb: Under high thread contention on writes, LongAdder can outperform
AtomicLong by 10x or more. Under low contention, they are comparable.
```

```java
class LongAdderVsAtomicLong {
    private final AtomicLong atomicCounter = new AtomicLong(0);
    private final LongAdder adderCounter = new LongAdder();

    // AtomicLong: use when you need CAS or get-and-set
    public long incrementAndGetAtomic() {
        return atomicCounter.incrementAndGet(); // Returns new value
    }

    public boolean conditionalUpdate(long expected, long newValue) {
        return atomicCounter.compareAndSet(expected, newValue); // CAS
    }

    // LongAdder: use for high-contention increment-only scenarios
    public void incrementAdder() {
        adderCounter.increment(); // Very low contention — each thread uses its own cell
    }

    public long getTotalAdder() {
        return adderCounter.sum(); // Sums all internal cells
    }

    public void resetAdder() {
        adderCounter.reset(); // Resets to 0
    }

    // LongAccumulator for custom binary operations
    private final LongAccumulator maxAccumulator =
        new LongAccumulator(Long::max, Long.MIN_VALUE);

    public void observe(long value) {
        maxAccumulator.accumulate(value);
    }

    public long getObservedMax() {
        return maxAccumulator.get();
    }
}
```

**Q4: What is the ABA problem and how do you solve it?**

```text
A4: The ABA problem is a subtle issue in CAS-based algorithms. It occurs when:

1. Thread A reads value V, getting result "A"
2. Thread B changes V from "A" to "B"
3. Thread B (or another thread) changes V from "B" back to "A"
4. Thread A performs CAS: is V still "A"? Yes! But the intermediate state change was invisible.

This can cause incorrect behavior in algorithms that need to detect whether a value has changed
at all (not just what the current value is). Classic example: ABA in linked-list stack pop.

Solutions:
1. AtomicStampedReference<V>: pairs the value with an integer "stamp" (version counter). CAS
   checks both value AND stamp — even if V returns to "A", the stamp will be different.
2. AtomicMarkableReference<V>: uses a boolean mark alongside the reference.
3. Epoch-based reclamation: tracks epochs when objects can be safely freed.
4. Hazard pointers: track which objects are currently referenced by threads.

In practice, many Java concurrent algorithms are immune to ABA because Java's garbage collector
ensures that if you hold a reference to an object, that object can't be freed and reused at the
same address (as can happen in C/C++). But logical ABA (same reference to a different "state") is
still possible with mutable objects.
```

```java
class ABASolution {
    // Problem: ABA with AtomicReference
    private final AtomicReference<String> atomicRef = new AtomicReference<>("A");

    // Stamped reference: tracks version number alongside value
    private final AtomicStampedReference<String> stampedRef =
        new AtomicStampedReference<>("A", 0);

    public boolean updateWithStamp(String expectedValue, String newValue) {
        int[] stampHolder = new int[1];
        String current = stampedRef.get(stampHolder);
        int currentStamp = stampHolder[0];

        // Both value AND stamp must match — prevents ABA
        return stampedRef.compareAndSet(
            expectedValue,     // expected value
            newValue,          // new value
            currentStamp,      // expected stamp
            currentStamp + 1   // new stamp (incremented to detect any changes)
        );
    }

    // Markable reference: tracks a boolean flag alongside value
    private final AtomicMarkableReference<String> markableRef =
        new AtomicMarkableReference<>("A", false);

    public boolean markAndUpdate(String expectedValue, String newValue) {
        boolean[] markHolder = new boolean[1];
        String current = markableRef.get(markHolder);
        // Only update if not marked for deletion
        if (markHolder[0]) return false;
        return markableRef.compareAndSet(expectedValue, newValue, false, false);
    }
}
```

**Q5: How do you ensure atomicity when multiple variables must be updated consistently?**

```text
A5: When multiple variables must satisfy a shared invariant, you have several options:

1. synchronized blocks: Lock a common monitor before reading or writing any of the related fields.
   This is the simplest and safest approach for most applications.

2. Immutable snapshot + volatile reference: Replace the mutable multi-field state with an
   immutable value object. Use a volatile reference to the current snapshot. Writers create a new
   snapshot; readers read the reference (atomic volatile read provides a consistent snapshot).
   This is lock-free and excellent for read-heavy workloads.

3. ReadWriteLock: If reads are far more frequent than writes, ReadWriteLock allows concurrent
   reads but exclusive writes.

4. StampedLock (Java 8+): A more advanced lock supporting optimistic reads — check if a write
   occurred after a read and retry if so. Very efficient for read-heavy workloads.

5. Software transactional memory (STM): Not natively in Java, but frameworks like Akka provide
   transactional memory semantics.

The choice depends on contention, read/write ratio, and whether you can afford object creation
for the immutable snapshot approach.
```

```java
class MultiVariableAtomicity {
    // Approach 1: synchronized
    private int balance;
    private int transactionCount;

    public synchronized void deposit(int amount) {
        balance += amount;
        transactionCount++;
    }

    public synchronized int[] getBalanceAndCount() {
        return new int[]{balance, transactionCount};
    }

    // Approach 2: Immutable snapshot + volatile reference
    private record AccountState(int balance, int transactionCount) {}
    private volatile AccountState state = new AccountState(0, 0);

    public void depositLockFree(int amount) {
        AccountState current;
        AccountState next;
        do {
            current = state;
            next = new AccountState(current.balance() + amount, current.transactionCount() + 1);
        } while (!compareAndSetState(current, next));
    }

    private boolean compareAndSetState(AccountState expected, AccountState next) {
        // Using AtomicReference for the snapshot
        return stateRef.compareAndSet(expected, next);
    }

    private final java.util.concurrent.atomic.AtomicReference<AccountState> stateRef =
        new java.util.concurrent.atomic.AtomicReference<>(new AccountState(0, 0));

    // Approach 3: ReadWriteLock for read-heavy
    private final java.util.concurrent.locks.ReadWriteLock rwLock =
        new java.util.concurrent.locks.ReentrantReadWriteLock();

    public int readBalance() {
        rwLock.readLock().lock();
        try {
            return balance;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public void writeDeposit(int amount) {
        rwLock.writeLock().lock();
        try {
            balance += amount;
            transactionCount++;
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
```

## Code Examples

- Source: [AtomicityViolation.java](src/main/java/com/github/msorkhpar/claudejavatutor/concurrencypitfalls/AtomicityViolation.java)
- Test: [AtomicityViolationTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/concurrencypitfalls/AtomicityViolationTest.java)
