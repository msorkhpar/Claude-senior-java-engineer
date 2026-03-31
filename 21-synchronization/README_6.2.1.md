# 6.2.1. Monitors and Synchronized Blocks

## Concept Explanation

Java's synchronization model is built around the concept of **monitors** — a synchronization construct that allows
threads to have mutual exclusion and the ability to wait for a condition to become true. Every Java object is implicitly
associated with a monitor, which can be thought of as a lock room with a single door: only one thread can be inside at a
time.

**Real-world analogy**: Imagine a single-occupancy bathroom in an office. When someone enters, they lock the door
(acquires the monitor). Others who want to use it wait in line (blocked threads). When the person leaves, they unlock
the door (releases the monitor), and the next person in line can enter. This ensures only one person at a time can use
the bathroom — exactly how intrinsic locks enforce mutual exclusion.

### The `synchronized` Keyword

Java provides the `synchronized` keyword to acquire an object's intrinsic lock (monitor) before executing a block of
code. There are two forms:

**Synchronized instance method** — acquires the lock on `this`:
```java
public synchronized void increment() {
    count++;
}
```

**Synchronized static method** — acquires the lock on the `Class` object:
```java
public static synchronized void staticMethod() {
    // acquires lock on MyClass.class
}
```

**Synchronized block** — acquires the lock on a specified object (more granular control):
```java
public void increment() {
    synchronized (this) {
        count++;
    }
}

// Or with a dedicated lock object (preferred):
private final Object lock = new Object();
public void increment() {
    synchronized (lock) {
        count++;
    }
}
```

### Intrinsic Locks (Monitors)

Intrinsic locks (also called monitor locks) are fundamental to Java's synchronization:

1. **Every object has one intrinsic lock.** When a thread executes code protected by `synchronized`, it acquires the
   intrinsic lock of the object specified.
2. **Mutual exclusion**: Only one thread at a time can hold the intrinsic lock.
3. **Reentrancy**: Java's intrinsic locks are reentrant — a thread that already holds a lock can re-acquire it without
   blocking. This prevents threads from deadlocking on themselves.
4. **Automatic release**: The lock is automatically released when the synchronized block/method exits, whether normally
   or by exception.

### Deadlocks and Starvation

**Deadlock** occurs when two or more threads are permanently blocked, each waiting for a lock held by another:

```
Thread A holds lock1, waiting for lock2
Thread B holds lock2, waiting for lock1
→ Both are blocked forever
```

**Starvation** occurs when a thread is perpetually denied access to a resource because other threads are always
preferred. For example, a high-priority thread that holds a lock for a long time prevents lower-priority threads from
making progress.

**Livelock** is a related concept where threads are not blocked but keep responding to each other without making
progress (like two people in a hallway continually stepping to the same side to let each other pass).

### Avoiding Deadlocks

The most robust strategies to avoid deadlocks are:

1. **Lock ordering**: Always acquire multiple locks in the same order.
2. **Lock timeout**: Use `tryLock()` with a timeout (via `ReentrantLock`) instead of blocking indefinitely.
3. **Lock-free algorithms**: Use atomic variables or concurrent data structures.
4. **Single lock**: Reduce the need for multiple locks through better design.
5. **Resource hierarchy**: Assign a global order to lock acquisition.

## Key Points to Remember

1. Every Java object has an intrinsic lock (monitor).
2. `synchronized` methods lock `this` (instance) or `ClassName.class` (static).
3. `synchronized` blocks can lock any object, providing more fine-grained control.
4. Java's intrinsic locks are **reentrant** — the same thread can re-acquire the same lock.
5. Locks are automatically released when the synchronized block exits (even on exception).
6. Deadlock requires: mutual exclusion, hold and wait, no preemption, circular wait.
7. Lock ordering is the most reliable deadlock prevention strategy.
8. Starvation can occur even without deadlock if locks are held for long periods.
9. Prefer synchronized blocks over synchronized methods for better performance (narrower critical sections).
10. Using `this` as the lock is exposed — external code can also synchronize on your object. Prefer private lock objects.

## Relevant Java 21 Features

- **Virtual Threads (Project Loom, JEP 444)**: Virtual threads can block on `synchronized` blocks without pinning the
  carrier thread in all cases. However, be careful — in Java 21, a virtual thread that blocks inside a `synchronized`
  block does pin its carrier thread. This is a known limitation and may change in future releases. `ReentrantLock` is
  preferred for virtual thread-friendly code.
- **Structured Concurrency (JEP 453)**: Encourages organizing multi-threaded code so that the lifetime of threads is
  structured and bounded, reducing synchronization complexity.
- **Pattern matching in switch (JEP 441)**: Reduces type-checking boilerplate but is orthogonal to synchronization.
- **Records**: Since records are immutable, they often don't need synchronization, but the record reference itself may
  need to be guarded.

Java's synchronization has evolved but the intrinsic lock model remains core. Modern Java prefers
`java.util.concurrent.locks` for complex locking scenarios (try-lock, timed lock, condition variables).

## Common Pitfalls and How to Avoid Them

1. **Synchronizing on a non-shared object** — locks only work if multiple threads use the *same* lock object.

   ```java
   // WRONG: Each thread creates its own String instance
   public void badExample(int value) {
       synchronized (new Object()) { // pointless — different instance each time
           count += value;
       }
   }

   // CORRECT: Use a shared, final lock object
   private final Object lock = new Object();
   public void goodExample(int value) {
       synchronized (lock) {
           count += value;
       }
   }
   ```

2. **Lock scope too wide** — holding a lock longer than necessary reduces concurrency.

   ```java
   // BAD: Lock held during expensive I/O
   public synchronized void processAndSave(Data data) {
       process(data);       // CPU-bound
       saveToDatabase(data); // Slow I/O — lock held the whole time!
   }

   // BETTER: Narrow critical section
   public void processAndSave(Data data) {
       Result result;
       synchronized (lock) {
           result = process(data); // fast, CPU-bound
       }
       saveToDatabase(result); // slow I/O outside lock
   }
   ```

3. **Deadlock from inconsistent lock ordering**:

   ```java
   // Thread A: synchronized(account1) { synchronized(account2) { ... } }
   // Thread B: synchronized(account2) { synchronized(account1) { ... } }
   // → DEADLOCK

   // FIX: Always acquire locks in consistent order
   public void transfer(Account from, Account to, int amount) {
       Account first = from.id() < to.id() ? from : to;
       Account second = from.id() < to.id() ? to : from;
       synchronized (first) {
           synchronized (second) {
               from.debit(amount);
               to.credit(amount);
           }
       }
   }
   ```

4. **Exposing `this` as the lock** — external code can disrupt your synchronization:

   ```java
   // Dangerous: external code can do synchronized(myObject) { ... }
   public synchronized void method() { }

   // Safer: private lock object
   private final Object lock = new Object();
   public void method() {
       synchronized (lock) { }
   }
   ```

5. **Not handling `InterruptedException`** in wait loops — always restore the interrupt flag.

   ```java
   // BAD: Swallowing interrupt
   try { Thread.sleep(100); } catch (InterruptedException e) { /* ignored */ }

   // GOOD: Restore interrupt flag
   try { Thread.sleep(100); } catch (InterruptedException e) {
       Thread.currentThread().interrupt();
   }
   ```

6. **Calling alien methods while holding a lock** — calling unknown code while holding a lock can cause deadlock:

   ```java
   // RISKY: calling listener.onEvent() while holding lock
   synchronized (lock) {
       data.update();
       listener.onEvent(data); // unknown code — could deadlock!
   }

   // SAFER: Release lock before calling alien methods
   synchronized (lock) { data.update(); }
   listener.onEvent(data); // outside lock
   ```

## Best Practices and Optimization Techniques

1. **Use private final lock objects** instead of `this` to prevent lock exposure.
2. **Minimize critical section size** — hold locks only for the minimum time necessary.
3. **Prefer `java.util.concurrent`** utilities (`ReentrantLock`, `ReadWriteLock`, `StampedLock`) for complex scenarios.
4. **Document your locking policy** with `@GuardedBy` annotation (from `javax.annotation` or Checker Framework).
5. **Avoid nested locks** where possible; when unavoidable, enforce a strict acquisition order.
6. **Use thread-safe collections** (`ConcurrentHashMap`, `CopyOnWriteArrayList`) instead of manually synchronizing.
7. **Prefer atomic variables** (`AtomicInteger`, `AtomicReference`) for single-variable atomic operations.
8. **Test with stress testing tools** like JCStress to find concurrency bugs.
9. **Avoid synchronization in constructors** (the object isn't fully constructed yet, and `this` may escape).
10. **Profile before optimizing** — premature concurrency optimization is a source of bugs.

## Edge Cases and Their Handling

1. **Reentrant lock acquisition**: A thread can re-acquire its own lock without deadlock:

   ```java
   public synchronized void outer() {
       inner(); // Works because intrinsic locks are reentrant
   }
   public synchronized void inner() {
       // Same thread already holds the lock
   }
   ```

2. **Exception in synchronized block**: The lock is *always* released when a synchronized block exits, even on exception:

   ```java
   synchronized (lock) {
       throw new RuntimeException(); // Lock is released automatically
   }
   ```

3. **Synchronized on `null`**: Throws `NullPointerException` at runtime:

   ```java
   Object obj = null;
   synchronized (obj) { } // NullPointerException!
   ```

4. **Static vs. instance locking**: Synchronizing a static method and an instance method on the same class use
   *different* locks — they can run concurrently:

   ```java
   public static synchronized void staticMethod() { } // lock on Class object
   public synchronized void instanceMethod() { }      // lock on 'this' — different lock!
   ```

5. **Long-running synchronized blocks**: Can cause significant thread contention. Consider splitting the work or using
   `ReadWriteLock` if reads are frequent.

## Interview-specific Insights

Interviewers focus heavily on:

- The difference between `synchronized` methods and `synchronized` blocks
- Understanding intrinsic locks, reentrancy, and visibility guarantees
- Deadlock: how it happens, detection, prevention, and avoidance strategies
- The difference between `synchronized` and `java.util.concurrent.locks.ReentrantLock`
- How `wait()`, `notify()`, and `notifyAll()` relate to monitors
- Performance implications of synchronization
- Common bugs: missed synchronization, inconsistent locking, deadlock

Tricky questions to expect:
- "Can a class have both a synchronized static method and a synchronized instance method running at the same time?"
  (Yes — they use different locks)
- "What happens if an exception is thrown inside a synchronized block?" (Lock is released)
- "Can a thread deadlock with itself using `synchronized`?" (No — intrinsic locks are reentrant)
- "What's wrong with `synchronized(new Object())`?" (Each call gets a different lock — no mutual exclusion)

## Interview Q&A Section

**Q1: What is an intrinsic lock (monitor) in Java, and how does the `synchronized` keyword use it?**

```text
A1: Every Java object has an associated intrinsic lock (also called a monitor lock or just monitor).
The synchronized keyword tells the JVM to acquire the intrinsic lock of a specified object before
entering the block, and release it when exiting.

- synchronized instance method: acquires lock on `this`
- synchronized static method: acquires lock on the Class object
- synchronized(obj) block: acquires lock on `obj`

Key guarantee: only one thread can hold an object's intrinsic lock at a time, giving mutual exclusion.
Additionally, the JMM guarantees that releasing a lock (exiting synchronized) happens-before any
subsequent acquisition of the same lock, providing memory visibility guarantees.
```

```java
public class Counter {
    private int count = 0;
    private final Object lock = new Object();

    // Method-level synchronization (locks 'this')
    public synchronized void incrementSynchronizedMethod() {
        count++;
    }

    // Block-level synchronization (locks 'lock' — more granular)
    public void incrementSynchronizedBlock() {
        synchronized (lock) {
            count++;
        }
    }

    public synchronized int getCount() {
        return count;
    }
}
```

**Q2: What is a deadlock? How do you detect and prevent it?**

```text
A2: A deadlock is a situation where two or more threads are permanently blocked, each waiting
for a lock held by another thread. Four necessary conditions must ALL be present:
1. Mutual exclusion: a resource can be held by only one thread at a time
2. Hold and wait: a thread holds at least one resource while waiting for additional resources
3. No preemption: resources cannot be forcibly taken from threads
4. Circular wait: a circular chain of threads each waiting for a resource held by the next

Detection:
- Thread dumps (jstack) show threads in BLOCKED state with a cyclic wait pattern
- JVM's built-in deadlock detection (ThreadMXBean.findDeadlockedThreads())
- Monitoring tools like VisualVM, JMC

Prevention strategies:
1. Lock ordering: always acquire multiple locks in the same global order
2. Lock timeout: use ReentrantLock.tryLock(timeout) to avoid waiting forever
3. Lock-free programming: use atomic variables, concurrent collections
4. Reduce lock scope: minimize the number of locks held simultaneously
```

```java
// DEADLOCK EXAMPLE
class DeadlockExample {
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();

    // Thread A calls this
    public void methodA() {
        synchronized (lock1) {
            synchronized (lock2) { /* work */ }  // Deadlock if Thread B runs simultaneously
        }
    }

    // Thread B calls this
    public void methodB() {
        synchronized (lock2) {
            synchronized (lock1) { /* work */ }  // Deadlock!
        }
    }
}

// PREVENTION: Consistent lock ordering
class BankAccount {
    private final long id;
    private int balance;
    private final Object lock = new Object();

    public BankAccount(long id, int balance) {
        this.id = id;
        this.balance = balance;
    }

    public static void transfer(BankAccount from, BankAccount to, int amount) {
        // Always acquire locks in ID order to prevent deadlock
        BankAccount first = from.id < to.id ? from : to;
        BankAccount second = from.id < to.id ? to : from;

        synchronized (first.lock) {
            synchronized (second.lock) {
                from.balance -= amount;
                to.balance += amount;
            }
        }
    }
}
```

**Q3: What is lock reentrancy, and why does Java support it?**

```text
A3: Reentrancy means a thread can re-acquire a lock that it already holds without blocking.
Java's intrinsic locks are reentrant by design.

Why it matters: Without reentrancy, if a synchronized method called another synchronized method
on the same object, the second call would block forever (the thread waits for its own lock).

Implementation: The JVM tracks a "hold count" per thread per lock. Each re-acquisition increments
the count; each release decrements it. The lock is fully released only when the count reaches zero.

This is especially important for inheritance: if a subclass calls super.method() and both methods
are synchronized on `this`, reentrancy prevents deadlock.
```

```java
public class ReentrancyDemo {
    public synchronized void outer() {
        System.out.println("Entering outer");
        inner();  // Can call synchronized inner() without deadlock
        System.out.println("Exiting outer");
    }

    public synchronized void inner() {
        // This thread already holds the lock on 'this' from outer()
        // Reentrancy allows re-acquisition without blocking
        System.out.println("In inner");
    }

    // Inheritance example
    static class Base {
        public synchronized void doSomething() {
            System.out.println("Base.doSomething");
        }
    }

    static class Derived extends Base {
        @Override
        public synchronized void doSomething() {
            // Calls super.doSomething() which is also synchronized on 'this'
            // Without reentrancy, this would deadlock!
            super.doSomething();
            System.out.println("Derived.doSomething");
        }
    }
}
```

**Q4: What is the difference between `synchronized` methods and `synchronized` blocks? When would you prefer one over the other?**

```text
A4:
synchronized methods:
- Convenience syntax: automatically locks 'this' (instance) or Class object (static)
- The entire method body is the critical section
- The lock object is exposed (external code can synchronize on the same object)
- Slightly easier to read for simple cases

synchronized blocks:
- More flexible: you can specify any lock object
- Narrower critical sections: lock only the parts that need synchronization
- Support for multiple independent locks in the same class
- Enable private lock objects (lock encapsulation)
- Generally preferred for production code

Prefer synchronized blocks when:
- You need to minimize the critical section size (performance)
- You want a private lock object to prevent lock exposure
- You need different locks for different state in the same class
- You want to document the lock object explicitly (@GuardedBy)
```

```java
public class BankAccountExample {
    private int balance;
    private final List<String> transactionHistory = new ArrayList<>();

    // Two separate locks for independent state
    private final Object balanceLock = new Object();
    private final Object historyLock = new Object();

    // synchronized method — simple but locks 'this' (exposed)
    public synchronized int getBalanceSimple() {
        return balance;
    }

    // synchronized block with private lock — preferred
    public int getBalance() {
        synchronized (balanceLock) {
            return balance;
        }
    }

    // Two independent operations don't block each other
    public void addTransaction(String description) {
        synchronized (historyLock) {  // Only locks history, not balance
            transactionHistory.add(description);
        }
    }

    public void deposit(int amount) {
        synchronized (balanceLock) {  // Only locks balance, not history
            balance += amount;
        }
        // History update doesn't need to hold balanceLock
        addTransaction("deposit: " + amount);
    }
}
```

**Q5: What is starvation in concurrent programming, and how can it be prevented?**

```text
A5: Starvation occurs when a thread is perpetually denied access to a resource, preventing it
from making progress. Unlike deadlock (where threads are completely blocked), a starving thread
is technically runnable but never gets scheduled or can never acquire a lock.

Causes of starvation:
1. High-priority threads always preempt low-priority threads
2. A thread holds a lock for an extremely long time, starving waiting threads
3. Non-fair scheduling where some threads are repeatedly preferred
4. Synchronized blocks with unfair lock acquisition (intrinsic locks are not fair)

Prevention:
1. Use fair locks: ReentrantLock(true) guarantees FIFO order for waiting threads
2. Limit lock hold duration — release locks promptly
3. Avoid holding locks during I/O, sleep, or other long operations
4. Use appropriate data structures (e.g., ReadWriteLock for read-heavy workloads)
5. Avoid thread priority as a synchronization mechanism
```

```java
import java.util.concurrent.locks.ReentrantLock;

public class StarvationPreventionDemo {
    // Non-fair lock (default) — no ordering guarantee for waiting threads
    private final ReentrantLock unfairLock = new ReentrantLock(false);

    // Fair lock — waiting threads are served in FIFO order
    private final ReentrantLock fairLock = new ReentrantLock(true);

    private int resourceValue = 0;

    // With fair lock, each waiting thread gets a turn in order
    public int getValueFairly() {
        fairLock.lock();
        try {
            return resourceValue;
        } finally {
            fairLock.unlock();
        }
    }

    // Alternative: use ReadWriteLock for read-heavy scenarios
    // Multiple readers can proceed concurrently — reduces writer starvation risk
    private final java.util.concurrent.locks.ReadWriteLock rwLock =
            new java.util.concurrent.locks.ReentrantReadWriteLock();

    public int readValue() {
        rwLock.readLock().lock();
        try {
            return resourceValue; // concurrent reads allowed
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public void writeValue(int value) {
        rwLock.writeLock().lock();
        try {
            resourceValue = value; // exclusive write
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
```

**Q6: How do `wait()`, `notify()`, and `notifyAll()` relate to monitors?**

```text
A6: wait(), notify(), and notifyAll() are methods defined on java.lang.Object and are tightly
coupled to the monitor/intrinsic lock mechanism. They MUST be called from within a synchronized
block or method (otherwise IllegalMonitorStateException is thrown).

wait(): Causes the current thread to release the lock and wait until notified (or interrupted).
        The thread enters a "wait set" associated with the object's monitor.

notify(): Wakes up exactly one thread from the wait set (JVM chooses which — non-deterministic).
          The awakened thread must re-acquire the lock before proceeding.

notifyAll(): Wakes up ALL threads in the wait set. Each must compete to re-acquire the lock.
             Generally preferred over notify() to avoid missed wakeups.

Pattern: Always use wait() in a loop (not an if statement) to guard against spurious wakeups.
```

```java
public class ProducerConsumerMonitor {
    private final Object monitor = new Object();
    private int data;
    private boolean hasData = false;

    // Producer
    public void produce(int value) throws InterruptedException {
        synchronized (monitor) {
            while (hasData) {
                monitor.wait(); // Release lock and wait for consumer
            }
            data = value;
            hasData = true;
            monitor.notifyAll(); // Wake up waiting consumers
        }
    }

    // Consumer
    public int consume() throws InterruptedException {
        synchronized (monitor) {
            while (!hasData) {
                monitor.wait(); // Release lock and wait for producer
            }
            int value = data;
            hasData = false;
            monitor.notifyAll(); // Wake up waiting producers
            return value;
        }
    }
}
```

## Code Examples

- Implementation: [SynchronizedBlocks.java](src/main/java/com/github/msorkhpar/claudejavatutor/synchronization/SynchronizedBlocks.java)
- Test: [SynchronizedBlocksTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/synchronization/SynchronizedBlocksTest.java)
