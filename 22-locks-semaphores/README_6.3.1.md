# 6.3.1. Reentrant Locks

## Concept Explanation

Java's `java.util.concurrent.locks` package provides a more sophisticated and flexible locking mechanism than the
built-in `synchronized` keyword. At the heart of this package sits the `Lock` interface and its most commonly used
implementation: `ReentrantLock`.

**Real-world analogy**: Think of a `ReentrantLock` like a hotel master key card system. A synchronized block is like a
simple door lock — anyone with the key can enter, but there's no way to query whether the door is locked, no ability
to give up if you've been waiting too long, and no separate "quiet hours" waiting rooms. A `ReentrantLock` is like an
advanced access-control system: you can check whether a door is currently locked, attempt entry with a timeout, ring a
buzzer to be woken up from a designated waiting area when a room becomes free, and the same person can re-enter rooms
they already hold a key for (reentrancy).

### The Lock Interface

The `java.util.concurrent.locks.Lock` interface declares the core contract:

```java
public interface Lock {
    void lock();                          // Acquire the lock, blocking if necessary
    void lockInterruptibly() throws InterruptedException;  // Acquire or respond to interruption
    boolean tryLock();                    // Non-blocking attempt
    boolean tryLock(long time, TimeUnit unit) throws InterruptedException;  // Timed attempt
    void unlock();                        // Release the lock
    Condition newCondition();             // Create a Condition object for this Lock
}
```

### ReentrantLock Class

`ReentrantLock` is the standard implementation of `Lock`. The name "reentrant" means a thread that already holds the
lock can acquire it again without blocking (avoiding self-deadlock). The lock maintains a **hold count**: each
successful `lock()` call increments it; each `unlock()` decrements it. The lock is fully released only when the hold
count drops to zero.

```java
ReentrantLock lock = new ReentrantLock();        // Non-fair by default
ReentrantLock fairLock = new ReentrantLock(true); // Fair ordering (FIFO)
```

**Fair vs. Non-Fair:**
- **Non-fair** (default): Threads that just arrived can "barge in" before waiting threads. Higher throughput but
  potential starvation.
- **Fair**: Longest-waiting thread gets the lock next. No starvation but lower throughput due to context switching.

### Explicit Locking and Unlocking

The golden rule: **always release in a `finally` block**.

```java
ReentrantLock lock = new ReentrantLock();

lock.lock();
try {
    // critical section
} finally {
    lock.unlock();  // MUST be in finally — or deadlock if an exception occurs
}
```

### Condition Objects

`Condition` objects (produced by `lock.newCondition()`) replace the `Object.wait() / notify() / notifyAll()` mechanism
when using explicit locks. They allow threads to wait for specific state changes, with the crucial advantage that a
single `Lock` can have **multiple distinct Conditions** — unlike `synchronized`, which has only one wait-set per
monitor.

```java
ReentrantLock lock = new ReentrantLock();
Condition notEmpty = lock.newCondition();  // "queue has items" condition
Condition notFull  = lock.newCondition();  // "queue has space" condition
```

| Operation                  | `synchronized` equivalent |
|----------------------------|--------------------------|
| `condition.await()`        | `object.wait()`          |
| `condition.signal()`       | `object.notify()`        |
| `condition.signalAll()`    | `object.notifyAll()`     |

A thread calling `condition.await()` atomically releases the lock and suspends. When another thread calls
`condition.signal()`, the waiting thread is re-awakened and must re-acquire the lock before returning from `await()`.

## Key Points to Remember

1. **Always unlock in `finally`**: Failure to unlock causes deadlock for every thread that tries to acquire afterwards.
2. **Reentrancy**: The owning thread can `lock()` multiple times; it must `unlock()` the same number of times.
3. **`tryLock()`** is non-blocking — use it to implement lock-ordering protocols or avoid deadlock.
4. **`lockInterruptibly()`** lets threads respond to `Thread.interrupt()` while waiting, unlike `synchronized`.
5. **Multiple Conditions per Lock**: Enables more fine-grained thread coordination than `synchronized`.
6. **Fairness is a trade-off**: Fair locks prevent starvation but reduce throughput; non-fair locks are faster but
   may starve low-priority threads.
7. **`getHoldCount()`** returns how many times the current thread has locked; useful for debugging.
8. **`isHeldByCurrentThread()`** and **`isLocked()`** provide diagnostic capabilities not possible with `synchronized`.

## Relevant Java 21 Features

- **Virtual Threads (Project Loom, JEP 444)**: Virtual threads park efficiently when waiting on `ReentrantLock`, just
  like platform threads. The JVM ensures that a virtual thread blocked on a lock does not block its carrier
  (platform) thread. This makes `ReentrantLock` a better choice than `synchronized` for high-concurrency virtual
  thread scenarios in Java 21 (though from Java 24 `synchronized` is also non-pinning).
- **Structured Concurrency (JEP 453)**: While structured concurrency uses `StructuredTaskScope`, locks can still be
  used inside scoped tasks to protect shared resources.
- **`LockSupport.park`/`unpark`**: The underlying primitive that `ReentrantLock` and `Condition` use internally;
  understanding it helps with debugging thread dumps.

## Common Pitfalls and How to Avoid Them

1. **Forgetting to unlock (Deadlock)**:
   ```java
   // WRONG — exception causes permanent deadlock
   lock.lock();
   doSomethingThatMightThrow();
   lock.unlock(); // never reached if exception thrown

   // CORRECT
   lock.lock();
   try {
       doSomethingThatMightThrow();
   } finally {
       lock.unlock();
   }
   ```

2. **Unlocking without locking (IllegalMonitorStateException)**:
   ```java
   // WRONG — unlock called before lock
   ReentrantLock lock = new ReentrantLock();
   lock.unlock(); // throws IllegalMonitorStateException

   // CORRECT — always pair lock() with unlock() in the same method/thread
   lock.lock();
   try { /* ... */ } finally { lock.unlock(); }
   ```

3. **Using `tryLock()` but forgetting to check return value**:
   ```java
   // WRONG — enters critical section even if lock not acquired
   lock.tryLock();
   try { /* critical section */ } finally { lock.unlock(); }

   // CORRECT
   if (lock.tryLock()) {
       try { /* critical section */ } finally { lock.unlock(); }
   } else {
       // handle failed acquisition
   }
   ```

4. **Calling `await()` without holding the lock**:
   ```java
   // WRONG — will throw IllegalMonitorStateException
   Condition cond = lock.newCondition();
   cond.await(); // IllegalMonitorStateException

   // CORRECT — must hold the lock before calling await()
   lock.lock();
   try {
       while (!conditionMet()) {
           cond.await();
       }
   } finally {
       lock.unlock();
   }
   ```

5. **Spurious wakeups — not re-checking condition after `await()`**:
   ```java
   // WRONG — using if instead of while
   lock.lock();
   try {
       if (queue.isEmpty()) condition.await(); // spurious wakeup: queue still empty
       process(queue.take()); // NPE or wrong state
   } finally { lock.unlock(); }

   // CORRECT — always loop around await()
   lock.lock();
   try {
       while (queue.isEmpty()) condition.await();
       process(queue.take());
   } finally { lock.unlock(); }
   ```

6. **Using wrong condition's signal**:
   ```java
   // WRONG — signaling "notFull" when you should signal "notEmpty"
   producerCondition.signal(); // when you want to wake consumers

   // CORRECT — signal the condition whose waiting threads can now proceed
   consumerCondition.signal(); // wake exactly one consumer
   ```

## Best Practices and Optimization Techniques

1. **Prefer `synchronized` for simple, uncontended, short critical sections**: Simpler to read and the JVM can
   optimize it (lock elision, biased locking).
2. **Choose `ReentrantLock` when you need**: timed or interruptible locking, `tryLock()`, multiple conditions, or
   fair ordering.
3. **Keep critical sections short**: Holding a lock for long increases contention.
4. **Use `tryLock(timeout)` to detect potential deadlock early** in production code.
5. **Prefer `signalAll()` over `signal()` when in doubt**: `signal()` is an optimization that requires careful
   reasoning; `signalAll()` is always safe.
6. **Use `ReadWriteLock` (specifically `ReentrantReadWriteLock`) when reads dominate writes**: Multiple readers can
   hold concurrently; a writer gets exclusive access.
7. **Document lock usage** in Javadoc: which methods acquire/release which locks.

## Edge Cases and Their Handling

1. **Reentrant lock count overflow**: The hold count is stored in an `int`; if a thread enters more than
   `Integer.MAX_VALUE` times (pathologically recursive code), it overflows. This is an error in design.
2. **Thread interruption during `lockInterruptibly()`**: The interrupted thread receives `InterruptedException` and
   does not acquire the lock — the lock state is clean.
3. **Condition `await()` with timeout**: Returns `false` if the timeout elapsed without signal; always check the
   return value.
4. **Condition `awaitUntil(Date)` and `awaitNanos(long)`**: More precise timing control; the current state of the
   condition still needs re-checking after return.
5. **Owner thread termination**: If a thread holding a `ReentrantLock` terminates without unlocking, the lock is
   **never released** (unlike `synchronized` which releases locks when a thread dies). This is a critical
   difference — `synchronized` cleanup is JVM-managed.

## Interview-specific Insights

Interviewers focus on:
- Ability to compare `ReentrantLock` vs. `synchronized` — when to use each.
- Understanding of what "reentrant" means and why it matters.
- Correct lock-unlock pattern (always `finally`).
- Understanding `Condition` objects and the spurious-wakeup problem.
- Knowledge of fair vs. non-fair locks and the performance trade-off.
- Impact of virtual threads (Java 21) on locking strategies.

Common tricky questions:
- "What happens if a thread holding a `ReentrantLock` throws an exception?"
- "Why does `await()` require holding the lock?"
- "When would you use `signal()` vs. `signalAll()`?"
- "What is a spurious wakeup and how do you handle it?"

## Interview Q&A Section

**Q1: What is the difference between `synchronized` and `ReentrantLock`?**

```text
A1: Both provide mutual exclusion, but ReentrantLock offers significantly more flexibility:

Feature Comparison:
1. Interruptibility: lockInterruptibly() can be interrupted while waiting; synchronized cannot.
2. Timeout: tryLock(time, unit) gives up after a timeout; synchronized blocks indefinitely.
3. Non-blocking try: tryLock() returns immediately with false if locked; no equivalent in synchronized.
4. Multiple Conditions: One ReentrantLock can have many named Condition objects; synchronized has one wait-set.
5. Fairness: ReentrantLock(true) ensures FIFO ordering; synchronized has no fairness guarantee.
6. Diagnostics: isLocked(), isHeldByCurrentThread(), getQueueLength() aid debugging; synchronized has no such API.
7. Scope: synchronized auto-releases on scope exit; ReentrantLock requires explicit unlock (risk and flexibility).

When to choose synchronized:
- Simple short critical sections
- Readability is paramount
- Java 21+ virtual threads (synchronized no longer pins carrier threads in Java 24)
- No need for Condition variables or timeouts

When to choose ReentrantLock:
- Need timed lock acquisition to avoid deadlocks
- Need interruptible lock waiting
- Need multiple Condition variables (e.g., producer/consumer queue)
- Need tryLock() for lock-ordering protocols
- Need fair scheduling to prevent starvation
```

```java
// synchronized version
public synchronized void incrementSync() {
    count++;
}

// ReentrantLock version — equivalent but more powerful options available
private final ReentrantLock lock = new ReentrantLock();

public void incrementLock() {
    lock.lock();
    try {
        count++;
    } finally {
        lock.unlock();
    }
}

// tryLock() — impossible with synchronized
public boolean tryIncrement() {
    if (lock.tryLock()) {
        try {
            count++;
            return true;
        } finally {
            lock.unlock();
        }
    }
    return false; // lock was not available
}
```

**Q2: What does "reentrant" mean and why is it important?**

```text
A2: "Reentrant" (also called "recursive") means that a thread which already holds the lock can acquire it again
without blocking itself. The lock maintains a hold count:
- Each lock() call by the owning thread increments the count.
- Each unlock() decrements the count.
- The lock is fully released only when the count reaches zero.

Why it matters:
1. Avoids self-deadlock in recursive algorithms or methods calling other synchronized methods.
2. Allows refactoring: you can safely call synchronized methods from within other synchronized methods on the same lock.
3. Simplifies implementation of complex data structures where helper methods also need the lock.

Note: synchronized in Java is also reentrant — the same thread can enter nested synchronized(sameObject) blocks.
```

```java
public class ReentrantExample {
    private final ReentrantLock lock = new ReentrantLock();

    public void outer() {
        lock.lock();
        try {
            System.out.println("Hold count in outer: " + lock.getHoldCount()); // 1
            inner(); // safe — same thread re-acquires
            System.out.println("Hold count back in outer: " + lock.getHoldCount()); // 1
        } finally {
            lock.unlock();
        }
    }

    public void inner() {
        lock.lock(); // NOT a deadlock — same thread re-enters
        try {
            System.out.println("Hold count in inner: " + lock.getHoldCount()); // 2
        } finally {
            lock.unlock(); // hold count goes back to 1
        }
    }
}
```

**Q3: How do Condition objects work and how do they compare to wait/notify?**

```text
A3: Condition objects are produced by Lock.newCondition(). They provide per-lock wait-sets analogous to
Object.wait()/notify(), but with two major advantages:

1. Multiple conditions per lock: A single lock can have N Condition objects, each with its own wait queue.
   This allows precise thread notification (e.g., wake only consumers, not producers).
2. More API options: awaitUntil(), awaitNanos(), await(time, unit), awaitUninterruptibly().

Correspondences:
  Object.wait()       → condition.await()
  Object.notify()     → condition.signal()
  Object.notifyAll()  → condition.signalAll()

Protocol (identical to wait/notify):
1. Acquire the lock before calling await()/signal().
2. Check the predicate in a loop (spurious wakeups can occur).
3. Call signal() or signalAll() when the predicate may have changed.
4. Release the lock (await() does this atomically; finally block for the rest).

Spurious wakeups: A thread may return from await() without signal() being called.
Always guard with while(!condition) { await(); }, never if(!condition) { await(); }.
```

```java
public class BoundedQueue<T> {
    private final Queue<T> queue = new LinkedList<>();
    private final int capacity;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull  = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    public BoundedQueue(int capacity) { this.capacity = capacity; }

    public void put(T item) throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == capacity) {
                notFull.await(); // wait until space available (loop handles spurious wakeups)
            }
            queue.offer(item);
            notEmpty.signal(); // wake one consumer
        } finally {
            lock.unlock();
        }
    }

    public T take() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                notEmpty.await(); // wait until item available
            }
            T item = queue.poll();
            notFull.signal(); // wake one producer
            return item;
        } finally {
            lock.unlock();
        }
    }
}
```

**Q4: When would you choose a fair ReentrantLock over a non-fair one?**

```text
A4: Fairness controls the lock acquisition order when multiple threads are waiting.

Non-fair (default):
- A thread that just called lock() may acquire it before threads that have been waiting longer ("barging").
- Higher overall throughput because the JVM avoids expensive context switches to specific waiting threads.
- Risk of starvation: a low-priority thread could wait indefinitely if high-throughput threads keep barging.

Fair:
- Threads acquire the lock in FIFO order of their wait start times.
- No starvation — every thread is guaranteed to eventually acquire the lock.
- Lower throughput due to mandatory context-switching overhead.

When to use fair locking:
- Long-running tasks where starvation is unacceptable (e.g., request processing with SLA requirements).
- Systems where equitable distribution of lock access matters more than raw throughput.
- Rate-limiting or time-sensitive scenarios.

Benchmark note: Non-fair can outperform fair by 5-10x in high-contention benchmarks.
The difference diminishes in low-contention scenarios.
```

```java
// Non-fair (default) — optimal for high-throughput scenarios
ReentrantLock highThroughputLock = new ReentrantLock();

// Fair — guarantees FIFO acquisition, prevents starvation
ReentrantLock fairLock = new ReentrantLock(true);

// Checking fairness
System.out.println("Is fair: " + fairLock.isFair()); // true
System.out.println("Queue length: " + fairLock.getQueueLength()); // threads waiting
```

**Q5: What is the difference between `lock()`, `tryLock()`, and `lockInterruptibly()`?**

```text
A5: The three acquisition methods differ in how they handle unavailability of the lock:

lock():
- Blocks indefinitely until the lock is acquired.
- Ignores thread interruption (thread.interrupt() has no effect while waiting).
- Use when: lock must always be acquired, and waiting indefinitely is acceptable.

tryLock():
- Returns immediately — true if acquired, false if not.
- Non-blocking variant: tryLock(time, unit) waits up to the specified time, then returns false.
- Can respond to interruption if using timed form.
- Use when: you want to avoid blocking, or need to implement lock-ordering to prevent deadlocks.

lockInterruptibly():
- Blocks like lock(), but can be interrupted via Thread.interrupt().
- Throws InterruptedException if the thread is interrupted while waiting.
- Use when: threads should be able to cancel lock-waiting operations (e.g., in response to shutdown).

Order of preference based on scenario:
- Simple mutual exclusion: lock() in try-finally
- Deadlock avoidance: tryLock() with backoff
- Cancellable operations: lockInterruptibly()
```

```java
public class LockAcquisitionStrategies {
    private final ReentrantLock lock = new ReentrantLock();

    // Strategy 1: Always acquire (blocking)
    public void alwaysAcquire() {
        lock.lock();
        try { /* critical section */ }
        finally { lock.unlock(); }
    }

    // Strategy 2: Non-blocking try
    public boolean tryAcquire() {
        if (lock.tryLock()) {
            try { /* critical section */ return true; }
            finally { lock.unlock(); }
        }
        return false; // couldn't acquire
    }

    // Strategy 3: Timed try — deadlock prevention
    public boolean timedAcquire(long ms) throws InterruptedException {
        if (lock.tryLock(ms, TimeUnit.MILLISECONDS)) {
            try { /* critical section */ return true; }
            finally { lock.unlock(); }
        }
        return false; // timed out
    }

    // Strategy 4: Interruptible — supports cancellation
    public void interruptibleAcquire() throws InterruptedException {
        lock.lockInterruptibly();
        try { /* critical section */ }
        finally { lock.unlock(); }
    }
}
```

**Q6: How does ReentrantLock interact with virtual threads in Java 21?**

```text
A6: Virtual threads (Project Loom, Java 21) are lightweight threads managed by the JVM. They are mounted on
carrier (platform OS) threads when running and unmounted (parked) when blocking.

Key point for Java 21: When a virtual thread blocks on ReentrantLock.lock(), it unmounts from its carrier
thread. The carrier thread is free to run other virtual threads. This is exactly what makes virtual threads
scale to millions — blocking I/O and locking don't consume carrier threads.

Java 21 vs. synchronized:
- In Java 21, synchronized blocks can cause virtual thread "pinning": the virtual thread stays mounted on
  its carrier thread while blocked, tying up that carrier. This is a temporary JVM limitation being addressed.
- In Java 24 (JEP 491), synchronized no longer pins — but for Java 21 production code, ReentrantLock is
  recommended when lock contention is expected in virtual thread contexts.

Implication: Library code and frameworks using synchronized may prevent the scalability benefits of virtual
threads. Lock-based code (ReentrantLock) works correctly with virtual threads immediately.
```

```java
// Java 21 — virtual thread + ReentrantLock (correct, no pinning)
ReentrantLock lock = new ReentrantLock();

Thread.ofVirtual().start(() -> {
    lock.lock(); // virtual thread unmounts, carrier is freed for others
    try {
        Thread.sleep(Duration.ofMillis(100)); // IO-like wait
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    } finally {
        lock.unlock();
    }
});

// Java 21 — synchronized (may pin virtual thread to carrier)
synchronized (this) { // potential pinning in Java 21
    Thread.sleep(100); // blocks carrier thread
}
```

**Q7: How do you implement a thread-safe counter with optional timeout using ReentrantLock?**

```java
public class TimedCounter {
    private long count = 0;
    private final ReentrantLock lock = new ReentrantLock();

    public boolean increment(long timeoutMs) throws InterruptedException {
        if (lock.tryLock(timeoutMs, TimeUnit.MILLISECONDS)) {
            try {
                count++;
                return true;
            } finally {
                lock.unlock();
            }
        }
        return false; // could not acquire within timeout
    }

    public long getCount() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }

    // Diagnostic methods
    public boolean isContended() {
        return lock.hasQueuedThreads();
    }

    public int getWaiterCount() {
        return lock.getQueueLength();
    }
}
```

```text
A7: The pattern above demonstrates:
1. tryLock(timeout) for bounded waiting — prevents indefinite blocking.
2. Always unlock in finally — ensures lock is released even on exception.
3. Diagnostic methods (hasQueuedThreads, getQueueLength) — available only with ReentrantLock.
4. Timed operations integrate well with SLA-driven systems where hard timeouts are required.

When increment() returns false, the caller knows the counter is under high contention and can decide
whether to retry, log, or fail gracefully — a design choice impossible with synchronized.
```

## Code Examples

- Source: [ReentrantLocks.java](src/main/java/com/github/msorkhpar/claudejavatutor/lockssemaphores/ReentrantLocks.java)
- Test: [ReentrantLocksTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/lockssemaphores/ReentrantLocksTest.java)
