# 6.3.2. Semaphores

## Concept Explanation

A **semaphore** is a concurrency primitive that controls access to a shared resource by maintaining a set of
**permits**. Unlike a lock (which is binary — held or not held), a semaphore has a non-negative integer count.
Threads acquire permits to gain access and release permits when done. When no permits are available, acquiring threads
block until a permit becomes available.

**Real-world analogy**: Think of a semaphore like a car park with a limited number of spaces and a permit counter
display at the entrance:
- The display shows available spaces (permit count).
- When you enter (acquire), the count decreases by one.
- When you leave (release), the count increases by one.
- If the display shows 0, you wait at the barrier until someone leaves.
- **Counting semaphore** = multi-space car park (N permits).
- **Binary semaphore** = single-space garage (1 permit) — functionally similar to a mutex, but with one important
  difference: a binary semaphore can be released by a different thread than the one that acquired it.

### Java's Semaphore Class

`java.util.concurrent.Semaphore` is Java's standard semaphore implementation.

```java
// Counting semaphore — 3 concurrent accesses allowed
Semaphore semaphore = new Semaphore(3);

// Binary semaphore — at most 1 thread at a time (like a mutex)
Semaphore binarySemaphore = new Semaphore(1);

// Fair semaphore — FIFO ordering for waiting threads
Semaphore fairSemaphore = new Semaphore(3, true);
```

### Core Methods

| Method | Description |
|--------|-------------|
| `acquire()` | Acquire 1 permit, blocking indefinitely if unavailable |
| `acquire(int n)` | Acquire n permits at once |
| `acquireUninterruptibly()` | Like `acquire()` but ignores interrupts |
| `tryAcquire()` | Non-blocking: returns true if permit available, false otherwise |
| `tryAcquire(long timeout, TimeUnit unit)` | Timed attempt |
| `tryAcquire(int n)` | Non-blocking: acquire n permits atomically |
| `release()` | Release 1 permit |
| `release(int n)` | Release n permits |
| `availablePermits()` | Returns current permit count |
| `drainPermits()` | Acquires and returns all available permits |
| `reducePermits(int reduction)` | (protected) Reduce available permits |

### Key Difference: Semaphore vs. Lock

| Property | Lock / ReentrantLock | Semaphore |
|----------|---------------------|-----------|
| Ownership | Only acquiring thread can release | Any thread can release |
| Binary mode | Yes (always) | Yes (with 1 permit) |
| Counting | No | Yes (N permits) |
| Reentrancy | Yes (ReentrantLock) | No |
| Use case | Mutual exclusion | Resource pooling, rate limiting |

The **ownership distinction** is critical: a `ReentrantLock` can only be unlocked by the thread that locked it.
A semaphore permit can be released by any thread — enabling producer/consumer signaling patterns.

### Bounded vs. Unbounded Semaphores

- **Bounded (fixed permits)**: Created with `new Semaphore(n)`. The permit count never exceeds the initial value
  in normal use — releases bring it back up, acquires bring it down. Practical for resource pools.
- **Unbounded**: A semaphore where `release()` is called more times than `acquire()`. The permit count can grow
  beyond the initial value. This is legal in Java (no upper-bound enforcement by default) but usually a bug.
  For true bounded behavior, enforce the invariant in application code.

## Key Points to Remember

1. **Any thread can release**: Unlike locks, the thread that releases need not be the one that acquired.
2. **Not reentrant**: Calling `acquire()` twice in the same thread without releasing consumes two permits.
3. **Fairness available**: `new Semaphore(n, true)` grants permits in FIFO order.
4. **`release()` before `acquire()` is valid**: You can increase permits above the initial count.
5. **Use for resource pools, rate limiting, and cross-thread signaling**.
6. **`drainPermits()`** atomically takes all available permits — useful for "flush" patterns.
7. **`availablePermits()` is not synchronized with `acquire()`**: Its value may change immediately after reading.
8. **No `await()/signal()` analogue**: Use with `Condition` objects or `CountDownLatch` for more complex signaling.

## Relevant Java 21 Features

- **Virtual Threads**: `Semaphore.acquire()` causes a virtual thread to park (unmount from carrier), enabling high
  concurrency. Semaphores are virtual-thread-friendly.
- **Structured Concurrency**: Semaphores can be used inside `StructuredTaskScope` to limit parallelism within a
  scope, though `StructuredTaskScope` itself has parallelism controls.
- **`Thread.ofVirtual()`**: Easy creation of many virtual threads each acquiring semaphore permits models real-world
  connection pools and rate limiters at scale.

## Common Pitfalls and How to Avoid Them

1. **Not releasing in `finally`**:
   ```java
   // WRONG — exception leaks the permit
   semaphore.acquire();
   doRiskyWork(); // throws RuntimeException
   semaphore.release(); // never reached

   // CORRECT
   semaphore.acquire();
   try {
       doRiskyWork();
   } finally {
       semaphore.release();
   }
   ```

2. **Releasing more than acquired (permit inflation)**:
   ```java
   // Accidentally inflates permit count
   Semaphore sem = new Semaphore(2);
   sem.release(); // now has 3 permits!
   // sem.acquire() now allows 3 concurrent threads instead of 2

   // CORRECT — only release what you acquired
   sem.acquire();
   try { /* work */ } finally { sem.release(); }
   ```

3. **Thinking semaphore provides mutual exclusion identity**:
   ```java
   // WRONG assumption: only "the" thread that acquired can access
   // Binary semaphore does NOT track which thread holds it
   Semaphore binary = new Semaphore(1);
   binary.acquire();
   // ... another thread can also call binary.release() legally
   // This is actually a feature, but can be a bug if not anticipated
   ```

4. **Calling `acquire()` twice without `release()`**:
   ```java
   // Semaphore is NOT reentrant
   Semaphore sem = new Semaphore(1);
   sem.acquire(); // acquires 1 permit
   sem.acquire(); // DEADLOCKS — only 1 permit total, already taken
   ```

5. **Race condition with `availablePermits()`**:
   ```java
   // WRONG — check-then-act is not atomic
   if (sem.availablePermits() > 0) {
       sem.acquire(); // another thread may have taken the permit between check and acquire
   }

   // CORRECT — use tryAcquire() for atomic check-and-acquire
   if (sem.tryAcquire()) {
       try { /* work */ } finally { sem.release(); }
   }
   ```

6. **Not handling `InterruptedException`**:
   ```java
   // WRONG — swallows interruption
   try {
       semaphore.acquire();
   } catch (InterruptedException e) {
       // ignored — thread won't respond to cancellation
   }

   // CORRECT — restore interrupt status
   try {
       semaphore.acquire();
   } catch (InterruptedException e) {
       Thread.currentThread().interrupt();
       return; // or rethrow as RuntimeException
   }
   ```

## Best Practices and Optimization Techniques

1. **Always release in `finally`**: Prevents permit leaks that degrade system capacity over time.
2. **Use `tryAcquire()` for non-blocking checks**: Avoids deadlock in lock-ordering protocols.
3. **Use `tryAcquire(timeout)` for bounded waits**: Fail fast with meaningful error messages.
4. **Prefer `acquire(n)` over n individual `acquire()` calls**: Avoids partial acquisition issues where a thread
   holds some permits but can't complete.
5. **Name your semaphores clearly**: `connectionPoolSemaphore`, `rateLimitSemaphore` communicate intent.
6. **Document initial permit count and its meaning** in code comments.
7. **Consider `ReentrantLock` instead of binary semaphore for mutual exclusion**: If you need ownership tracking
   (who holds the lock), use a lock. Use binary semaphores for signaling.
8. **For rate limiting**: Use `tryAcquire()` without blocking; reject or queue excess requests.

## Edge Cases and Their Handling

1. **Zero initial permits**: `new Semaphore(0)` — useful for "start gate" patterns. All acquirers block until
   a coordinator calls `release()`.
2. **Negative permits**: Not directly constructable (constructor clamps to 0 via `sync`), but `reducePermits()`
   (a protected method) can be used in subclasses to reduce below initial.
3. **Very large permit counts**: Permit count is an `int`; overflow is possible if `release()` is called too
   many times.
4. **Multiple permits in single acquire**: `acquire(n)` atomically waits for all n permits to be available, not
   one-at-a-time. A thread needing 5 permits won't partially hold 3 and deadlock.
5. **`drainPermits()` for throttle reset**: Useful to atomically consume all outstanding permits when resetting
   a rate limiter state.

## Interview-specific Insights

Interviewers focus on:
- Understanding the conceptual difference between semaphore and lock (ownership, counting).
- Ability to implement a connection pool or rate limiter with semaphores.
- Understanding bounded vs. unbounded semaphores.
- Knowing when to use semaphore vs. lock vs. countdown latch.
- Understanding the acquire/release symmetry requirement.
- Fair vs. non-fair semaphore impact.

Common tricky questions:
- "Can a different thread release a semaphore that another thread acquired? Is that legal?"
- "How is a binary semaphore different from a mutex/ReentrantLock?"
- "How would you implement a connection pool of 10 connections using a semaphore?"
- "What's the risk of calling release() before acquire()?"

## Interview Q&A Section

**Q1: What is a semaphore and when would you use it over a ReentrantLock?**

```text
A1: A semaphore is a concurrency primitive with a permit counter that controls concurrent access to a resource.

Use Semaphore when:
1. Resource pooling: Limit concurrent access to N resources (database connections, file handles, GPU units).
   Semaphore(N) ensures at most N threads are accessing the resource simultaneously.

2. Rate limiting: Allow at most N operations per time window.

3. Cross-thread signaling: One thread acquires (waits), another releases (signals).
   Binary semaphore with initial count 0 = "start gate" pattern.

4. One-way signaling (producer -> consumer): A producer releases to signal availability; consumer acquires.

Use ReentrantLock when:
1. Mutual exclusion with ownership: Only the acquiring thread should release.
2. Complex coordination: Need Condition objects for multi-predicate waiting.
3. Recursive locking: Same thread re-enters protected code.
4. Diagnostics: Need isHeldByCurrentThread(), getQueueLength(), etc.

Key difference: Semaphores have no notion of "owner" — any thread can release any permit.
This makes them flexible for cross-thread signaling but unsuitable for classic mutual exclusion
where ownership identity matters.
```

```java
// Connection pool using Semaphore
public class ConnectionPool {
    private final Semaphore permits;
    private final Queue<Connection> connections = new ConcurrentLinkedQueue<>();

    public ConnectionPool(int size) {
        this.permits = new Semaphore(size, true); // fair
        for (int i = 0; i < size; i++) {
            connections.offer(createConnection());
        }
    }

    public Connection acquire() throws InterruptedException {
        permits.acquire(); // wait for available connection
        return connections.poll();
    }

    public void release(Connection conn) {
        connections.offer(conn);
        permits.release(); // signal a waiting thread
    }
}
```

**Q2: What is the difference between a binary semaphore and a mutex (ReentrantLock)?**

```text
A2: While both a binary semaphore (Semaphore(1)) and a ReentrantLock provide mutual exclusion, they differ in:

1. Ownership:
   - Mutex (ReentrantLock): Only the acquiring thread can release. Attempting unlock from another thread
     throws IllegalMonitorStateException.
   - Binary semaphore: Any thread can release, regardless of which thread acquired.

2. Reentrancy:
   - ReentrantLock: Reentrant — the same thread can acquire multiple times (hold count tracks it).
   - Binary semaphore: NOT reentrant — the same thread acquiring twice will deadlock (only 1 permit).

3. Semantics:
   - Mutex: "I own this section; only I can release it."
   - Binary semaphore: "One thing is happening; when done, someone (possibly different) signals completion."

4. Use cases:
   - Mutex: Protecting mutable state from concurrent modification.
   - Binary semaphore(0): Start-gate / one-shot signaling (e.g., "work item is ready").

Common misconception: "semaphore is just a non-reentrant mutex." While functionally true in the binary case,
the ownership distinction makes binary semaphores suited for inter-thread signaling patterns that
mutexes cannot express cleanly.
```

```java
// ReentrantLock — owner-tracked mutual exclusion
ReentrantLock mutex = new ReentrantLock();
mutex.lock();       // Thread A acquires
mutex.unlock();     // Thread A must release (IllegalMonitorStateException if Thread B tries)
mutex.lock();       // Re-entrant: Thread A can acquire again without blocking

// Binary semaphore — cross-thread signaling
Semaphore gate = new Semaphore(0); // starts at 0 (no permits)

// Consumer thread
Thread consumer = Thread.ofVirtual().start(() -> {
    try {
        gate.acquire(); // blocks until producer releases
        System.out.println("Received signal!");
    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
});

// Producer thread signals the consumer
Thread.ofVirtual().start(() -> {
    prepareData();
    gate.release(); // any thread can release — cross-thread signaling!
});
```

**Q3: How would you implement a rate limiter using a Semaphore?**

```text
A3: A semaphore-based rate limiter restricts the number of concurrent operations. For a per-second rate
limit, combine a semaphore with periodic permit replenishment.

Approach 1 — Concurrent capacity limit (not time-based):
Use Semaphore(N) to allow at most N operations to be in-flight at once. This is a concurrency limiter,
not strictly a rate limiter, but it's simple and effective for resource protection.

Approach 2 — Time-window rate limit:
Use tryAcquire() to non-blockingly check and acquire. Periodically release permits (replenish the bucket)
using a scheduled executor. This implements a "token bucket" algorithm.

Key design decisions:
1. Fair vs. non-fair: Fair ensures no request starves; non-fair gives higher throughput.
2. Blocking vs. non-blocking: tryAcquire() for "fail fast"; acquire() for "queue and wait".
3. Replenishment frequency: More frequent replenishment → smoother rate; less frequent → burstier.
```

```java
public class RateLimiter {
    private final Semaphore semaphore;
    private final int maxPermitsPerSecond;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public RateLimiter(int permitsPerSecond) {
        this.maxPermitsPerSecond = permitsPerSecond;
        this.semaphore = new Semaphore(permitsPerSecond, true);

        // Replenish permits every second
        scheduler.scheduleAtFixedRate(this::replenish, 1, 1, TimeUnit.SECONDS);
    }

    private void replenish() {
        int currentPermits = semaphore.availablePermits();
        int toAdd = maxPermitsPerSecond - currentPermits;
        if (toAdd > 0) {
            semaphore.release(toAdd);
        }
    }

    /** Blocking — waits until a permit is available */
    public void acquire() throws InterruptedException {
        semaphore.acquire();
    }

    /** Non-blocking — returns false if rate limit exceeded */
    public boolean tryAcquire() {
        return semaphore.tryAcquire();
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
```

**Q4: What is a "start gate" pattern using a Semaphore(0)?**

```text
A4: A start gate (or "bang" pattern) uses a Semaphore initialized with 0 permits. No thread can acquire
until the "starter" releases one or more permits. This is useful for:

1. Synchronizing the start of multiple threads (all start at the same time).
2. One-shot "event has occurred" signaling.
3. Pipeline stages where downstream stages wait for upstream completion.

Compared to CountDownLatch:
- CountDownLatch(1) is simpler for one-shot "signal to all" (signalAll semantics).
- Semaphore(0) is more flexible: you can signal N waiters by releasing N times, or signal repeatedly.
- Neither is reusable (Semaphore can be, CountDownLatch cannot).

Compared to CyclicBarrier:
- CyclicBarrier waits for ALL parties to arrive; Semaphore(0) is signaled by a controlling thread.
```

```java
public class StartGateExample {

    public void demonstrateStartGate() throws InterruptedException {
        int threadCount = 5;
        Semaphore startGate = new Semaphore(0); // no permits yet
        Semaphore endGate  = new Semaphore(0);  // count completions

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            final int id = i;
            threads.add(Thread.ofVirtual().start(() -> {
                try {
                    startGate.acquire(); // all threads block here
                    System.out.println("Thread " + id + " started!");
                    Thread.sleep(Duration.ofMillis(10));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endGate.release(); // signal completion
                }
            }));
        }

        System.out.println("All threads ready. Starting...");
        startGate.release(threadCount); // release ALL at once — start gate!

        endGate.acquire(threadCount); // wait for all to finish
        System.out.println("All threads completed!");
    }
}
```

**Q5: How does acquire(n) differ from calling acquire() n times?**

```text
A5: acquire(n) is semantically different from n calls to acquire() in two important ways:

1. Atomicity of the multi-permit acquisition:
   acquire(n) waits until ALL n permits are simultaneously available before returning.
   Calling acquire() n times acquires permits one at a time — a thread could hold k < n permits
   and then block waiting for the rest, while other threads may need those k permits to proceed.
   This can cause deadlock in certain patterns.

2. Partial acquisition problem:
   Example: Semaphore(5), Thread A needs 3, Thread B needs 3.
   With individual acquire() calls:
     - Thread A takes 1, Thread B takes 1, Thread A takes 1, Thread B takes 1... both stall.
   With acquire(3):
     - Thread A blocks until 3 are available (all at once); no partial state.

3. Performance:
   acquire(n) may be slightly more efficient as it performs one atomic CAS instead of n.

4. Exception safety:
   acquire(n) is either fully acquired or fully not acquired (on InterruptedException).
   With n individual calls, an interrupt after k < n acquires leaves those k permits consumed.

Use acquire(n) when you genuinely need multiple permits atomically (e.g., allocating N I/O buffers).
```

```java
public class MultiPermitExample {
    private final Semaphore semaphore = new Semaphore(10);

    // SAFE: atomic acquisition of 3 permits
    public void processInBatch() throws InterruptedException {
        semaphore.acquire(3); // waits for ALL 3 permits simultaneously
        try {
            // use 3 resources concurrently
            processResource1();
            processResource2();
            processResource3();
        } finally {
            semaphore.release(3); // release all 3 at once
        }
    }

    // RISKY: individual acquisitions — partial acquisition possible
    public void processInBatchRisky() throws InterruptedException {
        semaphore.acquire(); // got 1
        semaphore.acquire(); // got 2
        semaphore.acquire(); // blocking here with 2 permits already held — potential deadlock
        try { /* ... */ } finally {
            semaphore.release();
            semaphore.release();
            semaphore.release();
        }
    }

    private void processResource1() {}
    private void processResource2() {}
    private void processResource3() {}
}
```

**Q6: What happens when you create a Semaphore with 0 permits and immediately call release()?**

```text
A6: It is perfectly legal to call release() on a Semaphore before any acquire(). The permit count
will increase from 0 to 1 (or by n if release(n) is called). This is one of the key differences
from locks — there is no "ownership" requirement.

This behavior is intentionally useful:
1. The "start gate" pattern (Semaphore(0) + release to go) relies on it.
2. A producer can release() to signal work availability before a consumer calls acquire().
3. Cross-thread signaling where the signaler doesn't "hold" anything.

Contrast with ReentrantLock:
  lock.unlock() without lock.lock() throws IllegalMonitorStateException.
  semaphore.release() is always valid regardless of acquisition history.

Potential bug: Calling release() too many times inflates the permit count beyond the intended maximum.
If a Semaphore(2) has release() called 3 times more than acquire(), it becomes a Semaphore(5), allowing
5 concurrent accesses instead of 2. Guard against this with ownership tracking in application code if needed.
```

```java
// Legal and useful: release before acquire
Semaphore sem = new Semaphore(0);
System.out.println("Permits before: " + sem.availablePermits()); // 0

sem.release();
System.out.println("Permits after release: " + sem.availablePermits()); // 1

sem.acquire(); // succeeds immediately
System.out.println("Permits after acquire: " + sem.availablePermits()); // 0

// Permit inflation — usually a bug
Semaphore bounded = new Semaphore(3);
bounded.release(5); // INFLATES to 8 permits — any 8 threads can now acquire!
System.out.println("Inflated permits: " + bounded.availablePermits()); // 8
```

## Code Examples

- Source: [SemaphoreUsage.java](src/main/java/com/github/msorkhpar/claudejavatutor/lockssemaphores/SemaphoreUsage.java)
- Test: [SemaphoreUsageTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/lockssemaphores/SemaphoreUsageTest.java)
