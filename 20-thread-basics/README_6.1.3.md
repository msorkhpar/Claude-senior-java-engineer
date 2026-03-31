# 6.1.3. Stopping Threads (interrupt() Method and Cooperative Cancellation)

## Concept Explanation

Java does not provide a safe way to forcibly stop a thread. The `Thread.stop()` method (deprecated since Java 1.2) was removed in Java 21 because it could leave shared data in inconsistent states by releasing all monitors held by the thread. Instead, Java uses a **cooperative cancellation model**.

The cornerstone of this model is the **interrupt mechanism**:
- `thread.interrupt()` sets the thread's **interrupt flag** to `true`.
- The interrupted thread is responsible for **checking** this flag and **responding** gracefully.

**Real-world analogy**: Imagine asking a colleague (thread) to stop working on a task. You can't physically pull the pen from their hand (`Thread.stop()`). Instead, you place a note on their desk that says "please stop when convenient" (interrupt flag). The colleague checks the note periodically and, when they see it, wraps up what they're doing, cleans up, and stops gracefully. If they're waiting for a phone call (blocking on `sleep()`/`wait()`), the note immediately wakes them up.

### The Two Paths of Interruption

**Path 1: Thread is blocked on a blocking operation**
If a thread is sleeping (`Thread.sleep()`), waiting (`Object.wait()`), or blocked on I/O (`InputStream.read()`), calling `interrupt()` on it immediately throws `InterruptedException`, waking the thread.

**Path 2: Thread is running (not blocked)**
The interrupt flag is set to `true`. The thread must explicitly check `Thread.interrupted()` or `Thread.currentThread().isInterrupted()` to detect the interrupt.

### Key Interrupt Methods

| Method | Description |
|---|---|
| `thread.interrupt()` | Sets the interrupt flag on the target thread |
| `Thread.interrupted()` | Static method: checks AND CLEARS the interrupt flag of the current thread |
| `thread.isInterrupted()` | Instance method: checks the interrupt flag WITHOUT clearing it |

## Key Points to Remember

1. **Cooperative, not forcible**: Java's interruption is a request, not a command. The interrupted thread must cooperate.
2. **`InterruptedException` clears the interrupt flag**: When a blocking method throws `InterruptedException`, the thread's interrupt flag is cleared. You must re-set it or handle it immediately.
3. **Never swallow `InterruptedException`**: Always either re-throw it, or restore the flag via `Thread.currentThread().interrupt()`.
4. **`Thread.interrupted()` vs `isInterrupted()`**: `interrupted()` is static and clears the flag; `isInterrupted()` is instance-based and does not clear it.
5. **`Thread.stop()` is removed in Java 21**: It was deprecated since Java 1.2 and finally removed.
6. **Check the flag in loops**: Long-running loops should periodically check `Thread.currentThread().isInterrupted()`.

## Relevant Java 21 Features

- **`Thread.stop()` removed**: Java 21 permanently removes this dangerous method (JEP 421 - Deprecate Finalization for removal).
- **Virtual threads and interruption**: Virtual threads fully support the interrupt mechanism. Interrupting a virtual thread that is pinned to a carrier thread still works correctly.
- **`Thread.sleep(Duration)`**: Java 21 adds `Thread.sleep(Duration duration)` — more readable than milliseconds:
  ```java
  Thread.sleep(Duration.ofSeconds(2));
  ```
- **Structured Concurrency (`StructuredTaskScope`)**: Provides a higher-level cancellation model where cancellation of a scope propagates to all forked threads:
  ```java
  try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      scope.fork(() -> longRunningTask());
      scope.join().throwIfFailed();
  } // cancels remaining tasks on scope exit
  ```

## Common Pitfalls and How to Avoid Them

1. **Swallowing `InterruptedException`** (the most dangerous mistake):
   ```java
   // WRONG: losing the interrupt signal
   try {
       Thread.sleep(1000);
   } catch (InterruptedException e) {
       // Do nothing! The interrupt is now gone.
   }

   // CORRECT: restore the interrupt flag
   try {
       Thread.sleep(1000);
   } catch (InterruptedException e) {
       Thread.currentThread().interrupt(); // restore
       return; // or break, or throw
   }
   ```

2. **Not checking the interrupt flag in CPU-bound loops**:
   ```java
   // WRONG: infinite loop ignores interrupts
   void processItems() {
       while (true) {
           process(getNextItem());
           // interrupt() has no effect here!
       }
   }

   // CORRECT: check the flag periodically
   void processItems() {
       while (!Thread.currentThread().isInterrupted()) {
           process(getNextItem());
       }
   }
   ```

3. **Using `Thread.interrupted()` (static) when you meant `isInterrupted()`**:
   ```java
   // WRONG: clears the flag as a side effect
   if (Thread.interrupted()) {
       // flag is now cleared — subsequent checks won't see it!
   }

   // CORRECT: use instance method to check without clearing
   if (Thread.currentThread().isInterrupted()) {
       // flag remains set
   }
   ```

4. **Calling `stop()` (now removed in Java 21)**:
   ```java
   // ILLEGAL in Java 21
   thread.stop(); // NoSuchMethodError or removed
   ```

5. **Busy-waiting without sleep causes CPU spinning**:
   ```java
   // WRONG: wastes CPU, interrupt checking in tight loop still OK but burns resources
   while (!done) { /* tight spin */ }

   // BETTER: use blocking primitives like wait/notify, locks, or sleep
   while (!done) {
       Thread.sleep(10); // yield CPU
   }
   ```

## Best Practices and Optimization Techniques

1. **Design tasks to check `isInterrupted()` at natural checkpoints** — at loop boundaries, before/after expensive operations.

2. **Use a volatile boolean flag as an additional cancellation mechanism**:
   ```java
   class CancellableTask implements Runnable {
       private volatile boolean cancelled = false;

       public void cancel() { cancelled = true; }

       @Override
       public void run() {
           while (!cancelled && !Thread.currentThread().isInterrupted()) {
               doWork();
           }
           cleanup(); // always run cleanup
       }
   }
   ```

3. **Prefer `Future.cancel(true)`** which calls `interrupt()` on the running thread when using `ExecutorService`.

4. **Propagate `InterruptedException`** in library code (don't catch it unless you handle it):
   ```java
   // In library code: let the caller decide
   public void doWork() throws InterruptedException {
       Thread.sleep(1000); // just propagate
   }
   ```

5. **Use `try-finally` for cleanup** to ensure resources are released even when interrupted:
   ```java
   public void run() {
       try {
           while (!Thread.currentThread().isInterrupted()) {
               process();
           }
       } finally {
           releaseResources(); // always runs
       }
   }
   ```

## Edge Cases and Their Handling

1. **Interrupting a thread that hasn't started**: Sets the interrupt flag. If the thread eventually starts and calls a blocking method, `InterruptedException` is thrown immediately.

2. **Interrupting a terminated thread**: Silently ignored. No exception is thrown.

3. **Interrupting a thread that ignores interrupts**: The interrupt flag stays set indefinitely. The thread never stops unless it checks the flag or calls a blocking operation.

4. **`InterruptedException` in a `Callable`**: Can be propagated — `Callable.call()` declares `throws Exception`.

5. **Nested `InterruptedException` handling**: If an inner catch re-interrupts and the outer code calls another blocking operation, `InterruptedException` is thrown again immediately.

## Interview-specific Insights

Interviewers focus heavily on:
- Why `Thread.stop()` is dangerous and what replaced it.
- The difference between `Thread.interrupted()` and `isInterrupted()`.
- The correct way to handle `InterruptedException` (NEVER swallow it).
- The difference between interrupt-based and flag-based cancellation.
- How `Future.cancel(true)` uses interruption internally.

Frequently asked tricky questions:
- "What does `InterruptedException` indicate, and how should you handle it?"
- "When would you use a `volatile boolean` flag instead of (or in addition to) `interrupt()`?"
- "Can you interrupt a thread that is executing a native method or blocked on I/O?"

## Interview Q&A Section

**Q1: Why was `Thread.stop()` deprecated and eventually removed in Java 21?**

```text
A1: Thread.stop() was dangerous because of how it worked: it forcibly terminated the thread by throwing a ThreadDeath Error, which caused the thread to unwind its stack and release all held monitors (synchronized locks).

The problem: at the moment stop() is called, the thread might be in the middle of modifying shared data — it could have updated some fields but not others, leaving the data in an inconsistent, partially-updated state. Another thread acquiring the now-released lock would see this corrupt state.

Example: imagine a thread is executing:
  synchronized (account) {
    account.debit(100);   // Step 1: done
    account.credit(100);  // Step 2: thread.stop() called here!
  }
If stopped between steps 1 and 2, the money is simply gone — deducted from one account but never credited to another.

The replacement: cooperative cancellation via interrupt(). The interrupted thread has the opportunity to complete its current unit of work, release resources properly, and then stop gracefully.

Thread.stop() was deprecated in Java 1.2 (1998) and finally removed in Java 21 (2023).
```

```java
// Java 21: this no longer compiles/runs
// thread.stop(); // NoSuchMethodError

// Correct approach: cooperative cancellation
class BankTransfer implements Runnable {
    private volatile boolean cancelled = false;

    public void cancel() { cancelled = true; }

    @Override
    public void run() {
        while (!cancelled && !Thread.currentThread().isInterrupted()) {
            // Each iteration is a complete unit of work
            performTransfer(); // Atomic from application's perspective
        }
    }
}
```

**Q2: What is the correct way to handle `InterruptedException`?**

```text
A2: There are three correct ways, depending on context:

1. PROPAGATE IT: If your method can declare throws InterruptedException, propagate it to the caller. This is the cleanest approach for library code.

2. RESTORE THE FLAG AND RETURN: If you can't propagate (e.g., because you're implementing Runnable.run() which can't throw checked exceptions), catch InterruptedException, restore the flag with Thread.currentThread().interrupt(), and return or break out of the loop.

3. HANDLE IT MEANINGFULLY: In some cases (e.g., task cancellation logic), you catch InterruptedException, perform necessary cleanup, restore the flag, and exit.

NEVER DO: Catch InterruptedException and do nothing (empty catch block). This loses the interrupt signal permanently. The thread continues running as if nothing happened, with no way for callers to know it was interrupted.
```

```java
// Option 1: Propagate (best for library code)
public void waitForData() throws InterruptedException {
    Thread.sleep(1000); // just let it propagate
}

// Option 2: Restore and return (for Runnable implementations)
@Override
public void run() {
    try {
        while (true) {
            processItem();
            Thread.sleep(100);
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // restore the flag
        // run() ends, thread terminates gracefully
    }
}

// Option 3: Meaningful handling
public boolean waitWithTimeout(long timeoutMs) {
    long deadline = System.currentTimeMillis() + timeoutMs;
    while (System.currentTimeMillis() < deadline) {
        try {
            Thread.sleep(50);
            if (isConditionMet()) return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false; // treat interrupt as timeout/failure
        }
    }
    return false;
}
```

**Q3: What is the difference between `Thread.interrupted()` (static) and `thread.isInterrupted()` (instance)?**

```text
A3:
Thread.interrupted() — static method:
- Tests whether the CURRENT thread has been interrupted
- CLEARS the interrupt flag as a side effect (resets it to false)
- Use this when you intend to "consume" the interrupt and act on it

thread.isInterrupted() — instance method:
- Tests whether the specific thread has been interrupted
- Does NOT modify the interrupt flag
- Use this to CHECK the status without affecting it

The side-effect of Thread.interrupted() is a common source of bugs. If you call it in a condition check but then don't act on the interrupt, the flag is silently cleared and subsequent code won't see the interrupt.
```

```java
// Demonstrating the difference
Thread t = new Thread(() -> {
    // Simulate interrupt
    Thread.currentThread().interrupt();

    // Check with isInterrupted() — does NOT clear
    System.out.println("isInterrupted: " + Thread.currentThread().isInterrupted()); // true
    System.out.println("isInterrupted again: " + Thread.currentThread().isInterrupted()); // still true

    // Check with interrupted() — CLEARS the flag
    System.out.println("interrupted(): " + Thread.interrupted()); // true
    System.out.println("interrupted() again: " + Thread.interrupted()); // false! flag was cleared
});
t.start();
t.join();
```

**Q4: When should you use a `volatile boolean` cancellation flag versus `interrupt()`?**

```text
A4: Both are valid, but they have different strengths:

interrupt() is better when:
- The task involves blocking operations (sleep, wait, I/O) — interrupt() wakes them up immediately
- You want to use Future.cancel(true) which uses interrupt() internally
- You're dealing with Java's standard concurrency primitives that respect interrupts

volatile boolean flag is better when:
- The task is CPU-bound with no blocking operations
- You want a more explicit, readable cancellation mechanism
- The task spans multiple methods and passing interrupts through is inconvenient
- You need to distinguish between "interrupted" and "cancelled" semantically

BEST PRACTICE: Use BOTH — volatile boolean for CPU-bound sections, interrupt() for blocking sections.

A volatile flag alone cannot wake a sleeping thread. interrupt() alone requires careful handling of InterruptedException.
```

```java
class RobustTask implements Runnable {
    private volatile boolean cancelled = false;

    public void cancel() {
        cancelled = true;
        // Also interrupt in case thread is blocked
        // Note: in practice, keep a reference to the thread to call interrupt()
    }

    @Override
    public void run() {
        while (!cancelled && !Thread.currentThread().isInterrupted()) {
            // CPU-bound work: volatile flag catches cancellation
            processChunk();

            // Blocking section: interrupt() catches it here
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break; // exit the loop
            }
        }
        cleanup();
    }
}
```

**Q5: How does `Future.cancel(true)` use thread interruption?**

```text
A5: When you call Future.cancel(true) on a running task submitted to an ExecutorService:
1. If the task hasn't started yet, it is removed from the queue and will never run.
2. If the task is currently running, the thread executing it is interrupted (interrupt() is called on it).
3. The boolean parameter 'true' means "interrupt if running". 'false' means "cancel if not started, but don't interrupt if running".

After cancellation:
- future.isCancelled() returns true
- future.isDone() returns true
- future.get() throws CancellationException

The task's run/call method is responsible for detecting the interrupt (via isInterrupted() or catching InterruptedException) and stopping. Future.cancel() does NOT guarantee the task stops immediately.
```

```java
ExecutorService executor = Executors.newSingleThreadExecutor();

Future<?> future = executor.submit(() -> {
    try {
        while (!Thread.currentThread().isInterrupted()) {
            doWork();
            Thread.sleep(100);
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        // Task ends gracefully
    }
});

Thread.sleep(500);
future.cancel(true); // interrupts the running thread

System.out.println("Cancelled: " + future.isCancelled()); // true
executor.shutdown();
```

**Q6: What happens if you interrupt a thread that is blocked on native I/O?**

```text
A6: This is implementation-dependent and often problematic:

For java.io (legacy I/O):
- Most blocking operations (InputStream.read(), ServerSocket.accept()) do NOT respond to thread interruption.
- The thread stays blocked. The interrupt flag is set, but the operation doesn't wake up.
- Solution: close the underlying stream/socket from another thread, which causes an IOException.

For java.nio (non-blocking I/O):
- NIO channels implement InterruptibleChannel.
- If a thread is blocked on an NIO channel operation, interruption causes an ClosedByInterruptException (an IOException subclass) and the channel is closed.
- This is one reason NIO is preferred for cancellable I/O operations.

This is why for high-performance servers, NIO or virtual threads (which handle I/O blocking differently) are preferred over blocking java.io with platform threads.
```

```java
// java.io: interrupt doesn't help
InputStream is = socket.getInputStream();
Thread reader = new Thread(() -> {
    try {
        int data = is.read(); // BLOCKED - interrupt() won't wake this up
    } catch (IOException e) {
        // Woken only if socket is closed from outside
    }
});
reader.start();
reader.interrupt(); // Interrupt flag is set, but read() remains blocked!

// java.nio: interrupt works
SocketChannel channel = SocketChannel.open(address);
Thread reader2 = new Thread(() -> {
    try {
        ByteBuffer buf = ByteBuffer.allocate(1024);
        channel.read(buf); // BLOCKED - but responds to interrupt()
    } catch (ClosedByInterruptException e) {
        // Interrupted! Channel is now closed.
    } catch (IOException e) { /* ... */ }
});
reader2.start();
reader2.interrupt(); // Wakes up the read and closes the channel
```

## Code Examples

- Source: [ThreadStopping.java](src/main/java/com/github/msorkhpar/claudejavatutor/threadbasics/ThreadStopping.java)
- Test: [ThreadStoppingTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/threadbasics/ThreadStoppingTest.java)
