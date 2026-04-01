# 6.1.4. Thread Lifecycle and States

## Concept Explanation

Every Java thread has a well-defined lifecycle governed by the `Thread.State` enum. Understanding this lifecycle is fundamental to writing correct concurrent programs, diagnosing deadlocks, interpreting thread dumps, and answering interview questions about multithreading.

A Java thread transitions through exactly **six states** during its lifetime:

| State | Description |
|---|---|
| `NEW` | Thread object created but `start()` not yet called |
| `RUNNABLE` | Thread is executing or ready to execute (waiting for CPU scheduling) |
| `BLOCKED` | Thread is waiting to acquire a monitor lock (enter a `synchronized` block/method) |
| `WAITING` | Thread is waiting indefinitely for another thread to perform a specific action |
| `TIMED_WAITING` | Thread is waiting for another thread or a timeout, whichever comes first |
| `TERMINATED` | Thread has completed execution (either normally or via an uncaught exception) |

**Real-world analogy**: Think of a thread's lifecycle like an employee's workday. The employee is **hired but hasn't started** (NEW). Once they clock in, they are **actively working or ready to work** (RUNNABLE). If they need a conference room that someone else is using, they **wait outside the door** (BLOCKED). If they are waiting for a colleague to finish a report before they can continue, they **wait at their desk indefinitely** (WAITING). If they set a timer saying "I'll wait 30 minutes, then move on," they are in a **time-bounded wait** (TIMED_WAITING). When they clock out and go home, their workday is **done** (TERMINATED).

### The State Transition Diagram

```
                   start()
        NEW ──────────────────→ RUNNABLE ←─────────────────────┐
                                  │   ↑                         │
                                  │   │ lock acquired /         │
                                  │   │ notified /              │
                                  │   │ timeout expired         │
                                  │   │                         │
                  ┌───────────────┼───┤                         │
                  │               │   │                         │
                  ▼               │   │                         │
              BLOCKED             │   │                    TIMED_WAITING
          (waiting for            │   │               (sleep, join(ms),
           monitor lock)          │   │                wait(ms), parkNanos)
                                  │   │
                                  ▼   │
                              WAITING
                          (wait(), join(),
                           LockSupport.park())
                                  │
                                  │ (run() completes
                                  │  or uncaught exception)
                                  ▼
                             TERMINATED
```

### What Triggers Each State Transition

| From | To | Trigger |
|---|---|---|
| `NEW` | `RUNNABLE` | `thread.start()` |
| `RUNNABLE` | `BLOCKED` | Thread attempts to enter a `synchronized` block held by another thread |
| `BLOCKED` | `RUNNABLE` | The monitor lock becomes available and is acquired |
| `RUNNABLE` | `WAITING` | `Object.wait()`, `Thread.join()`, `LockSupport.park()` |
| `WAITING` | `RUNNABLE` | `Object.notify()` / `notifyAll()`, joined thread terminates, `LockSupport.unpark()` |
| `RUNNABLE` | `TIMED_WAITING` | `Thread.sleep(ms)`, `Object.wait(ms)`, `Thread.join(ms)`, `LockSupport.parkNanos()` |
| `TIMED_WAITING` | `RUNNABLE` | Timeout expires, or `notify()` / `notifyAll()` / `unpark()` / joined thread terminates |
| `RUNNABLE` | `TERMINATED` | `run()` completes normally or throws an uncaught exception |

### Observing Thread State Programmatically

```java
Thread thread = new Thread(() -> {
    try {
        Thread.sleep(5000);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
});

System.out.println(thread.getState()); // NEW
thread.start();
Thread.sleep(100);
System.out.println(thread.getState()); // TIMED_WAITING
thread.interrupt();
thread.join();
System.out.println(thread.getState()); // TERMINATED
```

## Key Points to Remember

1. **Exactly six states**: Java defines exactly six thread states in the `Thread.State` enum: `NEW`, `RUNNABLE`, `BLOCKED`, `WAITING`, `TIMED_WAITING`, and `TERMINATED`.
2. **`RUNNABLE` includes both "running" and "ready to run"**: Unlike some OS-level thread models that distinguish between "running" and "ready," Java collapses these into a single `RUNNABLE` state. A thread in `RUNNABLE` may or may not currently have CPU time.
3. **`BLOCKED` is strictly about monitor locks**: A thread enters `BLOCKED` only when waiting to acquire an intrinsic lock (`synchronized`). Waiting on a `ReentrantLock` puts the thread in `WAITING` or `TIMED_WAITING` (because `ReentrantLock` uses `LockSupport.park()` internally).
4. **`WAITING` vs `TIMED_WAITING`**: The only difference is whether a timeout is specified. `wait()` -> `WAITING`, `wait(1000)` -> `TIMED_WAITING`.
5. **State transitions are one-directional for lifecycle boundaries**: A thread can only move from `NEW` to `RUNNABLE` (via `start()`) once, and once it reaches `TERMINATED`, it can never return to any other state.
6. **`getState()` returns a snapshot**: The state may change immediately after you read it. Do not use `getState()` for synchronization logic.
7. **Thread state is observable from any thread**: You call `thread.getState()` from any thread — the target thread does not need to cooperate.

## Relevant Java 21 Features

- **Virtual threads share the same `Thread.State` enum**: Virtual threads (Project Loom, JEP 444) go through the same six states. However, their internal implementation differs significantly — when a virtual thread enters `WAITING` or `TIMED_WAITING`, the underlying carrier (platform) thread is released to execute other virtual threads. This is transparent to the application but is a critical performance optimization.

- **`Thread.State` for virtual threads on pinned monitors**: When a virtual thread is blocked on a `synchronized` block (pinning the carrier thread), its state appears as `BLOCKED` just like a platform thread. However, in Java 21, this pinning prevents the carrier thread from being reused, which can reduce scalability. Best practice: prefer `ReentrantLock` over `synchronized` in virtual-thread-heavy code.

- **Thread dump improvements**: Java 21's `jcmd <pid> Thread.dump_to_file -format=json` produces structured JSON thread dumps that include virtual thread states, making lifecycle debugging easier in high-concurrency applications.

- **Evolution across Java versions**:
  - Java 1.0: Threads with `suspend()`, `resume()`, `stop()` — all later deprecated due to safety issues.
  - Java 5: `Thread.State` enum introduced, providing a standard way to query thread state. `java.util.concurrent` package added `ReentrantLock`, `Condition`, and other advanced primitives.
  - Java 9: `onSpinWait()` hint for spin-loops, allowing the JVM/CPU to optimize busy-waiting.
  - Java 19-21: Virtual threads introduced. Same state model, radically different resource usage.

## Common Pitfalls and How to Avoid Them

1. **Confusing `BLOCKED` with `WAITING`**:
   ```java
   // BLOCKED: waiting for a synchronized lock
   synchronized (lock) { // <-- if lock is held by another thread, this thread is BLOCKED
       doWork();
   }

   // WAITING: explicitly waiting for a condition
   synchronized (lock) {
       lock.wait(); // <-- this thread is WAITING (and it has released the lock)
   }
   ```
   **Key distinction**: `BLOCKED` means the thread wants to acquire a lock. `WAITING` means the thread has voluntarily released its hold and is waiting for a signal.

2. **Assuming `RUNNABLE` means "actively running on a CPU"**:
   ```java
   Thread t = new Thread(() -> {
       while (true) { Thread.yield(); } // State is RUNNABLE, but may not be on CPU
   });
   t.start();
   System.out.println(t.getState()); // RUNNABLE — but thread may be descheduled
   ```
   **Fix**: Understand that `RUNNABLE` means "eligible to run." The OS scheduler decides when the thread actually gets CPU time. Java does not distinguish between "running" and "ready."

3. **Using `getState()` for synchronization decisions**:
   ```java
   // WRONG: race condition
   if (thread.getState() == Thread.State.TERMINATED) {
       readResults(); // thread may have just started terminating!
   }

   // CORRECT: use join() for synchronization
   thread.join();
   readResults();
   ```
   **Fix**: Use proper synchronization mechanisms (`join()`, `CountDownLatch`, `Future.get()`) instead of polling thread state.

4. **Forgetting that `ReentrantLock` blocking shows as `WAITING`, not `BLOCKED`**:
   ```java
   ReentrantLock lock = new ReentrantLock();
   lock.lock(); // held by thread A

   // Thread B tries to acquire:
   lock.lock(); // Thread B state is WAITING (not BLOCKED!)
   ```
   **Why**: `ReentrantLock` internally uses `LockSupport.park()`, which puts the thread in `WAITING`. Only intrinsic `synchronized` monitors cause `BLOCKED` state. This is a frequent source of confusion when reading thread dumps.

5. **Not accounting for transient states in thread dump analysis**:
   ```java
   // A thread can change state between the time you take a dump and the time you read it
   // Multiple dumps spaced a few seconds apart give a more reliable picture
   ```
   **Fix**: When diagnosing issues, take multiple thread dumps (3-5, spaced 5-10 seconds apart) and compare.

## Best Practices and Optimization Techniques

1. **Use thread dumps to diagnose state-related issues**: In production, `jstack <pid>` or `jcmd <pid> Thread.print` reveals the state of every thread. Look for:
   - Many threads in `BLOCKED` state -> possible lock contention or deadlock
   - Threads in `WAITING` that never wake up -> possible missed signal (notify never called)
   - Threads stuck in `RUNNABLE` on the same stack frame -> possible infinite loop

2. **Prefer higher-level constructs over raw thread state management**:
   ```java
   // Instead of manually managing thread states with wait/notify:
   CompletableFuture.supplyAsync(() -> computeResult())
       .thenApply(result -> transform(result))
       .thenAccept(final_ -> display(final_));
   ```

3. **Use `LockSupport.park()` / `unpark()` for precise thread control**: Unlike `wait()` / `notify()`, `LockSupport` does not require holding a monitor and allows permit-based unparking (no lost-signal problem).

4. **Monitor thread states in production with JMX**:
   ```java
   ThreadMXBean bean = ManagementFactory.getThreadMXBean();
   long[] threadIds = bean.getAllThreadIds();
   ThreadInfo[] infos = bean.getThreadInfo(threadIds, true, true);
   for (ThreadInfo info : infos) {
       System.out.println(info.getThreadName() + ": " + info.getThreadState());
   }
   ```

5. **Use `Thread.onSpinWait()` for busy-wait loops** (Java 9+): Signals the JVM and CPU that the thread is in a spin-loop, enabling power-saving optimizations:
   ```java
   while (!condition) {
       Thread.onSpinWait();
   }
   ```

6. **Design tasks to spend minimal time in `BLOCKED`**: Lock contention is a common scalability bottleneck. Use finer-grained locks, lock-free data structures, or `ReadWriteLock` to reduce contention.

## Edge Cases and Their Handling

1. **Thread state after an uncaught exception**: The thread transitions directly to `TERMINATED`. The exception is passed to the `UncaughtExceptionHandler`. The state is `TERMINATED` regardless of whether the exception was caught.
   ```java
   Thread t = new Thread(() -> { throw new RuntimeException("oops"); });
   t.setUncaughtExceptionHandler((th, ex) -> {});
   t.start();
   t.join();
   assert t.getState() == Thread.State.TERMINATED; // always true
   ```

2. **Observing `RUNNABLE` vs `WAITING` is timing-dependent**: When a thread calls `canFinish.await()` (a `CountDownLatch`), it enters `WAITING`. But if you query `getState()` before the thread reaches the `await()` call, you see `RUNNABLE`. This is a race condition inherent to state observation, not a bug.

3. **`NEW` state is only observable before `start()`**: Once `start()` is called, the thread can never return to `NEW`. Calling `start()` twice throws `IllegalThreadStateException`.

4. **`TERMINATED` is permanent**: There is no mechanism to restart a terminated thread. Create a new `Thread` instance.

5. **State during `Thread.yield()`**: A thread calling `Thread.yield()` remains in `RUNNABLE` state. `yield()` is merely a hint to the scheduler to give other threads a chance; it does not change the thread's state.

6. **State during `Thread.onSpinWait()`**: Like `yield()`, the thread remains `RUNNABLE`. This method is a CPU-level hint, not a state change.

7. **Spurious wakeups and WAITING**: A thread in `WAITING` (via `Object.wait()`) can wake up spuriously without being notified. This is why `wait()` should always be called in a loop that re-checks the condition:
   ```java
   synchronized (lock) {
       while (!conditionMet) {
           lock.wait(); // may wake spuriously
       }
   }
   ```

## Interview-specific Insights

Interviewers focus on:
- Ability to name and describe all six thread states from memory.
- Understanding the exact conditions that trigger each state transition.
- The distinction between `BLOCKED` (monitor lock contention) and `WAITING` (voluntary wait).
- The distinction between `WAITING` and `TIMED_WAITING`.
- How thread states appear in thread dumps and how to use them for debugging.
- Understanding that `RUNNABLE` includes both "running" and "ready to run."
- Knowledge of how virtual threads use the same state model.

Common tricky questions:
- "What state is a thread in when it's waiting to acquire a `ReentrantLock`?" (Answer: `WAITING`, not `BLOCKED`)
- "Is there a 'RUNNING' state in Java?" (Answer: No, Java only has `RUNNABLE` which covers both running and ready-to-run)
- "Can a thread go from `BLOCKED` to `WAITING` directly?" (Answer: No, it must transition through `RUNNABLE` first)
- "What happens to the thread state if `run()` throws a `RuntimeException`?" (Answer: Transitions to `TERMINATED`)

## Interview Q&A Section

**Q1: List all six Java thread states and explain what causes a thread to enter each one.**

```text
A1: The six thread states defined in Thread.State are:

1. NEW: A thread that has been created (new Thread()) but start() has not been called yet.
   Cause: Thread constructor.

2. RUNNABLE: A thread that is executing in the JVM or is eligible to execute (waiting for CPU scheduling).
   Cause: thread.start() transitions from NEW to RUNNABLE. A thread also returns to RUNNABLE after acquiring a contested lock, being notified, or timing out.

3. BLOCKED: A thread that is waiting to acquire an intrinsic monitor lock to enter or re-enter a synchronized block/method.
   Cause: Attempting to enter a synchronized block whose monitor is held by another thread.

4. WAITING: A thread that is waiting indefinitely for another thread to perform a particular action.
   Cause: Calling Object.wait() (without timeout), Thread.join() (without timeout), or LockSupport.park().

5. TIMED_WAITING: A thread that is waiting for another thread or for a specified time period, whichever comes first.
   Cause: Calling Thread.sleep(millis), Object.wait(millis), Thread.join(millis), or LockSupport.parkNanos(nanos).

6. TERMINATED: A thread that has exited. Either run() completed normally, or an uncaught exception propagated out of run().
   Cause: run() returning or throwing an uncaught exception.
```

```java
// Demonstrating all six states
public class AllStatesDemo {
    public static void main(String[] args) throws InterruptedException {
        // NEW
        Thread t = new Thread(() -> {});
        System.out.println("NEW: " + t.getState()); // NEW

        // RUNNABLE
        CountDownLatch running = new CountDownLatch(1);
        CountDownLatch finish = new CountDownLatch(1);
        Thread runnable = new Thread(() -> {
            running.countDown();
            while (finish.getCount() > 0) Thread.yield();
        });
        runnable.start();
        running.await();
        System.out.println("RUNNABLE: " + runnable.getState()); // RUNNABLE
        finish.countDown();
        runnable.join();

        // TIMED_WAITING
        CountDownLatch sleepStarted = new CountDownLatch(1);
        Thread sleeping = new Thread(() -> {
            sleepStarted.countDown();
            try { Thread.sleep(10_000); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        sleeping.start();
        sleepStarted.await();
        Thread.sleep(50);
        System.out.println("TIMED_WAITING: " + sleeping.getState()); // TIMED_WAITING
        sleeping.interrupt();
        sleeping.join();

        // TERMINATED
        Thread done = new Thread(() -> {});
        done.start();
        done.join();
        System.out.println("TERMINATED: " + done.getState()); // TERMINATED
    }
}
```

**Q2: What is the difference between `BLOCKED` and `WAITING` states?**

```text
A2: This is a critical distinction:

BLOCKED:
- The thread WANTS to acquire an intrinsic monitor lock (synchronized block/method).
- Another thread currently holds that lock.
- The thread is passively waiting for the lock to be released.
- The thread did NOT voluntarily give up anything — it never had the lock in the first place.
- Only caused by contention on synchronized monitors.

WAITING:
- The thread has VOLUNTARILY suspended its execution.
- It is waiting for a specific signal (notify/notifyAll) or for another thread to complete (join).
- If the thread was holding a monitor (via wait()), it RELEASES the monitor upon entering WAITING.
- Caused by Object.wait(), Thread.join(), or LockSupport.park().

Key insight: A thread in BLOCKED never held the lock and is trying to acquire it.
A thread in WAITING (via wait()) previously held the lock, released it, and is waiting to be signaled so it can re-acquire it.

In thread dumps:
- BLOCKED shows as "waiting to lock <0xABC...>"
- WAITING shows as "waiting on <0xABC...>" (for wait()) or "parking" (for LockSupport)

This matters for diagnosis: many BLOCKED threads indicate lock contention.
Many WAITING threads might indicate a missing notify() or a deadlocked producer-consumer.
```

```java
Object lock = new Object();

// Thread A: holds the lock, then waits (WAITING)
Thread threadA = new Thread(() -> {
    synchronized (lock) {
        try {
            lock.wait(); // WAITING — releases the lock voluntarily
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
});

// Thread B: tries to enter synchronized — BLOCKED
Thread threadB = new Thread(() -> {
    synchronized (lock) { // BLOCKED if lock is held by someone else
        lock.notify();
    }
});

// Scenario:
// 1. threadA starts, acquires lock, calls wait() -> WAITING, releases lock
// 2. threadB starts, acquires the now-free lock -> RUNNABLE, calls notify()
// 3. threadA wakes up, needs to re-acquire lock -> BLOCKED (until threadB exits synchronized)
// 4. threadB exits synchronized -> threadA re-acquires lock -> RUNNABLE
```

**Q3: Is there a `RUNNING` state in Java? How does `RUNNABLE` relate to actual CPU execution?**

```text
A3: No, Java does NOT have a RUNNING state. The RUNNABLE state encompasses both:
- Threads that are actively executing on a CPU core ("running")
- Threads that are ready to execute and waiting for CPU scheduling ("ready")

This design decision was made because Java runs on top of the OS, and the JVM cannot always know whether a thread currently has CPU time. The OS scheduler makes those decisions transparently.

From the JVM's perspective, once a thread has been started and is not blocked/waiting/terminated, it is RUNNABLE — regardless of whether the OS has currently allocated a CPU time slice to it.

Practical implications:
1. You cannot determine if a thread is actively consuming CPU just by checking getState().
2. A thread doing a CPU-intensive computation and a thread that just yielded are both RUNNABLE.
3. To measure actual CPU usage per thread, use ThreadMXBean:
   - bean.getThreadCpuTime(threadId) returns the actual CPU time consumed.

In other thread models (e.g., POSIX pthreads), RUNNING and READY are distinct states. Java intentionally abstracts this away for portability across different OS schedulers.
```

```java
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

// Both threads are RUNNABLE, but only one may be on CPU at a given instant
Thread cpuBound = new Thread(() -> {
    long sum = 0;
    for (long i = 0; i < 1_000_000_000L; i++) sum += i;
}, "cpu-bound");

Thread yielder = new Thread(() -> {
    for (int i = 0; i < 1000; i++) Thread.yield();
}, "yielder");

cpuBound.start();
yielder.start();

// Both are RUNNABLE
System.out.println(cpuBound.getState()); // RUNNABLE
System.out.println(yielder.getState());  // RUNNABLE

// To see actual CPU usage, use ThreadMXBean
ThreadMXBean bean = ManagementFactory.getThreadMXBean();
long cpuTime = bean.getThreadCpuTime(cpuBound.threadId()); // nanoseconds of actual CPU time
```

**Q4: What state is a thread in when waiting to acquire a `ReentrantLock`?**

```text
A4: A thread waiting to acquire a ReentrantLock is in the WAITING state, NOT BLOCKED.

This is because ReentrantLock (and all java.util.concurrent.locks.Lock implementations) use LockSupport.park() internally to suspend threads, which puts them in WAITING (or TIMED_WAITING for tryLock(timeout)).

Only the intrinsic synchronized monitor causes the BLOCKED state.

This distinction matters when reading thread dumps:
- A thread stuck on "synchronized" appears as BLOCKED with "waiting to lock <monitor>"
- A thread stuck on ReentrantLock.lock() appears as WAITING with "parking to wait for <ReentrantLock>"

In practice:
- If you see many BLOCKED threads in a dump, look for synchronized contention.
- If you see many WAITING threads with a ReentrantLock stack trace, look for Lock contention or a thread holding the lock for too long.

This is a very common interview question because many candidates assume any lock acquisition leads to BLOCKED state.
```

```java
import java.util.concurrent.locks.ReentrantLock;

ReentrantLock lock = new ReentrantLock();
CountDownLatch holderReady = new CountDownLatch(1);
CountDownLatch waiterStarted = new CountDownLatch(1);

// Thread holding the lock
Thread holder = new Thread(() -> {
    lock.lock();
    try {
        holderReady.countDown();
        Thread.sleep(10_000); // hold lock for a long time
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    } finally {
        lock.unlock();
    }
});

// Thread waiting for the lock
Thread waiter = new Thread(() -> {
    waiterStarted.countDown();
    lock.lock(); // will park here — state becomes WAITING
    try {
        // eventually gets the lock
    } finally {
        lock.unlock();
    }
});

holder.start();
holderReady.await();
waiter.start();
waiterStarted.await();
Thread.sleep(100);

System.out.println(waiter.getState()); // WAITING (not BLOCKED!)

holder.interrupt();
holder.join();
waiter.join();
```

**Q5: How do you use thread dumps to diagnose deadlocks based on thread states?**

```text
A5: Thread dumps are the primary tool for diagnosing deadlocks and other concurrency issues. Here's how thread states help:

1. DEADLOCK DETECTION:
   - Look for threads in BLOCKED state that form a circular dependency.
   - Thread A is BLOCKED waiting for lock X (held by Thread B).
   - Thread B is BLOCKED waiting for lock Y (held by Thread A).
   - The JVM's built-in deadlock detector (via jstack or ThreadMXBean.findDeadlockedThreads()) can identify these automatically.

2. LOCK CONTENTION:
   - Many threads BLOCKED on the same monitor -> a hot lock.
   - Solution: reduce lock granularity, use concurrent collections, or use read-write locks.

3. THREAD STARVATION:
   - Threads stuck in WAITING that never get notified -> missed signal or bug in producer-consumer logic.
   - Check if notify()/notifyAll() is being called, or if CountDownLatch/Condition is being signaled.

4. THREAD LEAK:
   - Growing number of threads in WAITING or TIMED_WAITING -> threads being created but never cleaned up.
   - Often caused by ExecutorService not being shut down, or threads waiting on resources that never arrive.

5. CPU SPINNING:
   - Threads in RUNNABLE with high CPU usage on the same stack frame -> infinite loop or busy-wait.
   - Compare multiple dumps to confirm the thread is stuck in the same method.

HOW TO TAKE A THREAD DUMP:
- jstack <pid>
- jcmd <pid> Thread.print
- jcmd <pid> Thread.dump_to_file -format=json (Java 21)
- kill -3 <pid> (SIGQUIT on Unix/macOS)
- ThreadMXBean.dumpAllThreads() programmatically
```

```java
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

// Programmatic deadlock detection
ThreadMXBean bean = ManagementFactory.getThreadMXBean();

// Find deadlocked threads (returns null if no deadlock)
long[] deadlockedIds = bean.findDeadlockedThreads();
if (deadlockedIds != null) {
    ThreadInfo[] deadlocked = bean.getThreadInfo(deadlockedIds, true, true);
    for (ThreadInfo info : deadlocked) {
        System.out.println("Deadlocked thread: " + info.getThreadName());
        System.out.println("  State: " + info.getThreadState());
        System.out.println("  Waiting for lock: " + info.getLockName());
        System.out.println("  Held by: " + info.getLockOwnerName());
    }
} else {
    System.out.println("No deadlock detected.");
}

// Dump all thread states
ThreadInfo[] allThreads = bean.dumpAllThreads(true, true);
for (ThreadInfo info : allThreads) {
    System.out.printf("%-30s %s%n", info.getThreadName(), info.getThreadState());
}
```

**Q6: Can a thread transition directly from `BLOCKED` to `WAITING` or from `WAITING` to `BLOCKED`?**

```text
A6: No direct transitions exist between BLOCKED and WAITING. Every transition must pass through RUNNABLE.

The full set of valid transitions:
- NEW -> RUNNABLE (start())
- RUNNABLE -> BLOCKED (attempt to enter synchronized held by another thread)
- BLOCKED -> RUNNABLE (lock acquired)
- RUNNABLE -> WAITING (wait(), join(), park())
- WAITING -> RUNNABLE (notified, joined thread terminates, unparked)
- RUNNABLE -> TIMED_WAITING (sleep(ms), wait(ms), join(ms), parkNanos())
- TIMED_WAITING -> RUNNABLE (timeout, notified, unparked)
- RUNNABLE -> TERMINATED (run() completes or uncaught exception)

However, there is a subtle case that appears like a direct WAITING -> BLOCKED transition:
When a thread is in WAITING (via Object.wait()) and is notified, it must re-acquire the monitor before proceeding. If another thread holds the monitor at that point, the notified thread transitions to BLOCKED (waiting for the monitor). But logically, it first transitions to RUNNABLE (eligible to run) and then immediately to BLOCKED. In practice, this happens so quickly that thread dumps may show the thread as BLOCKED right after being notified.

This subtlety is an advanced interview topic that tests deep understanding of the Java Memory Model and monitor mechanics.
```

```java
Object lock = new Object();

// Thread 1: enters wait() — WAITING
Thread waiter = new Thread(() -> {
    synchronized (lock) {
        try {
            lock.wait(); // WAITING — releases the monitor
            // After being notified, must re-acquire monitor
            // If monitor is held by notifier, briefly BLOCKED
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}, "waiter");

// Thread 2: holds lock, notifies, then does more work while holding lock
Thread notifier = new Thread(() -> {
    synchronized (lock) {
        lock.notify(); // wakes waiter, but notifier still holds the lock
        // waiter is now: notified -> needs monitor -> BLOCKED (until this block exits)
        try { Thread.sleep(2000); } catch (InterruptedException e) {}
    } // lock released — waiter transitions BLOCKED -> RUNNABLE
}, "notifier");

waiter.start();
Thread.sleep(100);
System.out.println("Waiter: " + waiter.getState()); // WAITING

notifier.start();
Thread.sleep(100);
System.out.println("Waiter after notify: " + waiter.getState()); // BLOCKED (waiting for monitor)

notifier.join();
waiter.join();
System.out.println("Waiter final: " + waiter.getState()); // TERMINATED
```

**Q7: How does the thread lifecycle differ for virtual threads versus platform threads?**

```text
A7: Virtual threads (Java 21, JEP 444) share the same Thread.State enum and the same six states as platform threads. From the application's perspective, the lifecycle is identical. However, the internal implementation differs significantly:

PLATFORM THREADS:
- Each thread maps 1:1 to an OS thread.
- BLOCKED/WAITING/TIMED_WAITING causes the OS thread to be parked (consuming OS resources).
- Context switching is expensive (kernel-mode transition).

VIRTUAL THREADS:
- Many virtual threads share a small pool of carrier (platform) threads.
- When a virtual thread enters WAITING or TIMED_WAITING (via sleep, I/O, etc.), the JVM unmounts it from the carrier thread.
- The carrier thread is then free to run another virtual thread.
- This means millions of virtual threads can be in WAITING without consuming OS resources.
- BLOCKED on synchronized is special: it "pins" the virtual thread to the carrier thread, which defeats the purpose. Use ReentrantLock instead.

State observation:
- thread.getState() returns the same Thread.State values for virtual threads.
- Thread dumps via jcmd show virtual thread states in the same format.
- ThreadMXBean may not list virtual threads by default; use jcmd for comprehensive dumps.

Best practices:
- Use virtual threads for I/O-bound workloads where threads spend most time in WAITING/TIMED_WAITING.
- Avoid synchronized blocks in virtual-thread code (prefer ReentrantLock) to prevent pinning.
- Do not pool virtual threads — they are cheap to create and discard.
```

```java
// Virtual thread lifecycle — same states, different resource usage
Thread vt = Thread.ofVirtual().name("my-virtual-thread").unstarted(() -> {
    try {
        System.out.println("Virtual thread running");
        Thread.sleep(1000); // TIMED_WAITING — carrier thread is released!
        System.out.println("Virtual thread resumed");
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
});

System.out.println(vt.getState()); // NEW
vt.start();
Thread.sleep(50);
System.out.println(vt.getState()); // TIMED_WAITING (same as platform thread)
vt.join();
System.out.println(vt.getState()); // TERMINATED

// Demonstrating pinning (what to avoid)
Object monitor = new Object();
Thread pinned = Thread.ofVirtual().start(() -> {
    synchronized (monitor) { // PINS the virtual thread to carrier!
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
    }
});

// Better: use ReentrantLock with virtual threads
ReentrantLock lock = new ReentrantLock();
Thread unpinned = Thread.ofVirtual().start(() -> {
    lock.lock(); // Does NOT pin — carrier is released if lock is contended
    try { Thread.sleep(1000); } catch (InterruptedException e) {}
    finally { lock.unlock(); }
});
```

## Code Examples

- Source: [ThreadLifecycle.java](src/main/java/com/github/msorkhpar/claudejavatutor/threadbasics/ThreadLifecycle.java)
- Test: [ThreadLifecycleTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/threadbasics/ThreadLifecycleTest.java)
