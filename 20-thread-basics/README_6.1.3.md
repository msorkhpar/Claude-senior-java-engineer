# 6.1.3. Stopping Threads (interrupt() Method and Cooperative Cancellation)

Every rolling deploy of the metrics service takes 30 seconds longer than it should, and once a week
the orchestrator loses patience and kills the process halfway through writing a file. The shutdown
hook is textbook: `executor.shutdownNow()`, then `awaitTermination(30, SECONDS)`. A thread dump
taken during those 30 seconds shows one pool thread in `Thread.sleep`, inside the loop that sends
metrics, and the log has one new line: `interrupted while waiting, will retry`.

Nobody wrote code to ignore the shutdown, and the thread ignores it anyway. In Java a running
thread can't be stopped from outside: it is asked to stop, and its own code decides. This unit
follows that request to the line that acts on it:

1. `interrupt()` sets a flag, and running code stops only where it looks at it.
2. A blocking call answers an interrupt by throwing, and the throw clears the flag.
3. Whoever catches `InterruptedException` owns the interrupt: rethrow it, or restore it and stop.
4. `Future.cancel(true)` asks; the task decides whether anything stops.
5. A thread blocked in classic I/O doesn't hear an interrupt: close what it waits on.

## A Tool That Asks a Thread to Stop

Every example in this unit runs a job through the same helper. It starts the job on a pool of
its own, waits until the job has done a little work, calls `shutdownNow()`, and reports two
things: whether the pool's thread ended within a second, and how much work the job did after it
was asked to stop.

```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** Runs a job on its own pool, asks the pool to stop, and reports what the job did after that. */
public final class Stops {

    /** A job reports each finished unit of work by calling unit.run(). */
    public interface Job {
        void run(Runnable unit) throws Exception;
    }

    public static void stops(String name, Job job) throws InterruptedException {
        AtomicInteger units = new AtomicInteger();
        ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);                       // a job that never stops can't keep the JVM alive
            return t;
        });
        pool.execute(() -> {
            try {
                job.run(units::incrementAndGet);
            } catch (Exception e) {
                System.out.println("  job ended with " + e);
            }
        });
        while (units.get() < 3) Thread.onSpinWait(); // let it get going
        long asked = System.nanoTime();
        int atRequest = units.get();
        pool.shutdownNow();                          // interrupts the pool's thread
        boolean stopped = pool.awaitTermination(1, TimeUnit.SECONDS);
        long ms = (System.nanoTime() - asked) / 1_000_000;
        System.out.printf("%-16s %s, %d units after the request%n", name,
                stopped ? "stopped after " + ms + " ms" : "still running after 1 s",
                units.get() - atRequest);
    }
}
```

`shutdownNow()` calls `interrupt()` on the thread running the job. The examples call
`currentThread()` through `import static java.lang.Thread.currentThread;`, and every output
comes from Java 21.

## Interrupting a Thread Sets a Flag, and Running Code Stops Only Where It Looks

A reindex job works through 20,000 documents. `reindex(doc)` is a few dozen microseconds of
arithmetic, pure CPU work with no waiting. We ask the job to stop a few documents in. How many
more does it process before the pool ends?

```java
Stops.stops("no check", unit -> {
    for (int doc = 0; doc < 20_000; doc++) { reindex(doc); unit.run(); }
});
Stops.stops("isInterrupted()", unit -> {
    for (int doc = 0; doc < 20_000 && !currentThread().isInterrupted(); doc++) { reindex(doc); unit.run(); }
});
```

```text
no check         stopped after 497 ms, 19997 units after the request
isInterrupted()  stopped after 0 ms, 2 units after the request
```

The first job processed every remaining document. The pool did end, but only because the job ran
out of work. `interrupt()` doesn't stop anything: it sets a boolean on the target thread, the
**interrupt flag**, and returns. A thread that is computing never notices a boolean that nobody
reads. The second job reads it before each document and stops within a document or two.

Only the thread being stopped knows where it can stop without leaving half a result: between two
documents, but not between writing a document and updating its index entry. So the check goes
where stopping is safe, and Java leaves placing it to the code that runs.

Two other ways of checking look equivalent and aren't:

```java
Stops.stops("interrupted()", unit -> {
    for (int batch = 0; batch < 40 && !currentThread().isInterrupted(); batch++) {
        for (int doc = batch * 500; doc < (batch + 1) * 500; doc++) {
            if (Thread.interrupted()) break;          // ends this batch...
            reindex(doc); unit.run();
        }
    }
});
AtomicBoolean cancelled = new AtomicBoolean();      // stands in for a volatile boolean field
Stops.stops("volatile flag", unit -> {
    for (int doc = 0; doc < 20_000 && !cancelled.get(); doc++) { reindex(doc); unit.run(); }
});
```

```text
interrupted()    stopped after 496 ms, 19501 units after the request
volatile flag    stopped after 514 ms, 19997 units after the request
```

**`Thread.interrupted()` reads the flag and clears it.** The inner loop saw the request and ended
its batch, and in doing so erased the request. The outer loop's check then found nothing, and 39
more batches ran. `Thread.currentThread().isInterrupted()` only reads. Use `Thread.interrupted()`
only at the point that acts on the request and ends the work, never in a check that someone above
you also relies on.

**A cancellation field of your own is invisible to the JDK.** `shutdownNow()`, and `cancel(true)`
on a `Future` from `ExecutorService.submit`, set the interrupt flag, not your field. A field can sit
beside the flag when you need a reason the flag can't carry ("cancelled by the user" rather than
"shutting down"), but the loop must check the flag too.

<!-- practice 1 -->

## A Blocking Call Answers by Throwing, and the Throw Clears the Flag

Most threads spend their time waiting, not computing: in `sleep`, `wait`, `join`, a queue's
`take`. A thread that is parked there can't check anything, so these methods check for it. When
the thread is interrupted while blocked, or is already interrupted when it calls one, the method
returns at once by throwing `InterruptedException`.

```java
Thread t = new Thread(() -> {
    try {
        Thread.sleep(10_000);
    } catch (InterruptedException e) {
        System.out.println("in the catch, isInterrupted() = " + currentThread().isInterrupted());
    }
});
t.start();
t.interrupt();
t.join();
```

```text
in the catch, isInterrupted() = false
```

The request has been turned into the exception. The flag is cleared, and the exception is now the
only record that anyone asked. The metrics sender from the start of the unit has this shape:

```java
Stops.stops("log and go on", unit -> {
    while (!currentThread().isInterrupted()) {
        unit.run();                                  // send one batch of metrics
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            System.out.println("  interrupted while waiting, will retry");
        }
    }
});
```

```text
  interrupted while waiting, will retry
log and go on    still running after 1 s, 20 units after the request
```

The condition would work if the request arrived while the thread was sending. It almost never
does. The thread sleeps 50 ms per cycle, so the interrupt lands in `sleep`, the exception clears
the flag, the catch logs it, and the `while` reads `false` forever. In production,
`awaitTermination` times out after 30 seconds and the orchestrator kills whatever is half-done.

The JDK clears the flag because the exception now carries the request. Left set, the flag would
make every later blocking call in the thread throw again. Only two things clear it: a call to
`Thread.interrupted()`, and a waiting method that throws. Code that never looks at the flag leaves
it set, and the thread's next wait throws at once.

<!-- practice 5 -->

## Whoever Catches InterruptedException Rethrows It, or Restores It and Stops

The compiler forces every caller of `sleep` to deal with `InterruptedException`, and the
quickest way to make it compile is an empty catch. Here are three catches in a webhook sender
that retries up to 100 times. `sleepyWork()` is the pause between attempts, a 20 ms `sleep`:

```java
Stops.stops("empty catch", unit -> {
    for (int attempt = 0; attempt < 100; attempt++) {
        unit.run();
        try { sleepyWork(); } catch (InterruptedException e) { }
    }
});
Stops.stops("restore, go on", unit -> {
    for (int attempt = 0; attempt < 100; attempt++) {
        unit.run();
        try { sleepyWork(); } catch (InterruptedException e) { currentThread().interrupt(); }
    }
});
Stops.stops("restore, return", unit -> {
    for (int attempt = 0; attempt < 100; attempt++) {
        unit.run();
        try { sleepyWork(); } catch (InterruptedException e) { currentThread().interrupt(); return; }
    }
});
```

```text
empty catch      still running after 1 s, 50 units after the request
restore, go on   stopped after 1 ms, 97 units after the request
restore, return  stopped after 0 ms, 0 units after the request
```

The empty catch threw the request away, and the sender keeps sending as if nobody had asked. The
last one does what the request meant: it puts the flag back, so code above it can still see it,
and it stops.

### Spot the Problem: Restoring the Flag and Retrying Turns Every Wait into a Spin

The middle catch passes code review: it restores the flag, and the pool ended within a
millisecond. Look at the count again before reading on: what did it do in that millisecond?

It made the 97 remaining attempts back to back. With the flag set again, every following `sleep`
threw at once, so the pause between attempts disappeared and the sender hammered the webhook as
fast as the loop could turn. A loop bounded by time instead of attempts runs until its time is
up. This sender backs off 100 ms between tries for 250 ms, and is interrupted once:

```java
AtomicLong throwsSeen = new AtomicLong();
Thread sender = new Thread(() -> {
    long end = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(250);
    while (System.nanoTime() < end) {
        try {
            Thread.sleep(100);                       // back off before the next attempt
        } catch (InterruptedException e) {
            throwsSeen.incrementAndGet();
            currentThread().interrupt();
        }
    }
});
sender.start();
sender.interrupt();
sender.join();
System.out.println("sleeps that threw in 250 ms: " + throwsSeen.get());
```

```text
sleeps that threw in 250 ms: 193734
```

Almost 200,000 failed sleeps in a quarter of a second, with one core at 100 % until the loop ends
by itself. The code that restores the flag must also stop. Code that is meant to stop has two
correct shapes:

- **Rethrow it.** A method that can declare `throws InterruptedException` does, and doesn't catch
  it at all. A helper that sleeps, takes from a queue or waits on a latch has no idea whether an
  interrupt means "shut down" or "the user pressed Cancel", so it lets the exception reach the
  code that does.
- **Restore it and stop.** Code that can't throw it, typically the `run()` at the top of a task,
  calls `Thread.currentThread().interrupt()` and returns. The flag tells the pool, or whoever
  called `run()`, that the task ended because it was asked to.

(A task that must not be cancelled at all can keep waiting, remember that it was interrupted,
and restore the flag in a `finally` before it returns. That is a deliberate choice for a few
tasks, not a default.)

Wrapping it in an unchecked exception is neither shape. This helper hands its caller something
that looks like any other failure:

```java
static String fetchWrapped() {
    try {
        Thread.sleep(20);
        return "ok";
    } catch (InterruptedException e) {
        throw new RuntimeException("fetch interrupted", e);
    }
}
```

and the sender around it retries failures, as senders do:

```java
Stops.stops("wrapped", unit -> {
    for (int attempt = 0; attempt < 100; attempt++) {
        try {
            fetchWrapped();
        } catch (RuntimeException e) {
            if (failures.incrementAndGet() <= 2) System.out.println("  fetch failed, retrying: " + e.getMessage()
                    + ", interrupted=" + currentThread().isInterrupted());
        }
        unit.run();
    }
});
```

```text
  fetch failed, retrying: fetch interrupted, interrupted=false
wrapped          still running after 1 s, 50 units after the request
```

The caller's retry logic treats "we were asked to stop" as "the fetch failed", and with the flag
cleared, the next fetch succeeds and the loop goes on. If an interface really forbids checked
exceptions, restore the flag before wrapping, so at least the next check sees it.

<!-- practice 2 -->

## Cancelling a Future Asks; the Task Decides

A user starts an export and presses Cancel. The service calls `future.cancel(true)`, which
returns `true`, and the UI says "cancelled". The export writes six parts, pausing between them,
and then uploads the file:

```java
static void noCheck() {
    for (int part = 0; part < 6; part++) {
        try { Thread.sleep(50); } catch (InterruptedException e) { }
        writePart();
    }
    upload();
}
```

A second version, `checkSwallow`, is the same with one line added before the upload:
`if (currentThread().isInterrupted()) return;`. A third also changes the catch:

```java
static void checkReturn() {
    for (int part = 0; part < 6; part++) {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            currentThread().interrupt();
            return;
        }
        writePart();
    }
    if (currentThread().isInterrupted()) return;
    upload();
}
```

Each is submitted to a single-thread pool, and once two parts are written, the service calls
`export.cancel(true)` on the `Future` that `submit` returned:

```text
no check:       cancel=true isCancelled=true isDone=true
                parts written: 6, published: true
check, swallow: cancel=true isCancelled=true isDone=true
                parts written: 6, published: true
check, return:  cancel=true isCancelled=true isDone=true
                parts written: 2, published: false
```

The three `Future`s report the same thing. `isCancelled()` and `isDone()` describe the `Future`,
not the thread: from the moment `cancel` succeeds, `get()` throws `CancellationException`, and
nobody will ever see the task's result. What the thread does next is up to the task, and the
first two kept going and uploaded a file the user cancelled. The second one even checked: its
catch had swallowed the interrupt, so the check found nothing. The user, told it was cancelled,
starts it again, and now there are two.

`cancel(true)` interrupts the running task's thread. `cancel(false)` only keeps a queued task from
starting. On a running export both return `true` and mark the `Future` cancelled, so after
`cancel(false)` the caller loses the result and the thread carries on, uninterrupted.

> **Not every Future interrupts.** A `CompletableFuture` ignores the argument: its `cancel(true)`
> completes the future with a `CancellationException` and never interrupts whatever computes it.

When the task has a side effect that must not happen after a cancel, check right before it, and
make sure nothing between the request and the check can swallow it.

<!-- practice 3 -->

## Classic I/O Doesn't Hear an Interrupt: Close What It Waits On

Not every blocking call checks the flag. Here a platform thread waits in `accept()`, and we
interrupt it, then close the socket:

```java
Runnable accept = () -> {
    try {
        server.accept();
    } catch (IOException e) {
        System.out.println("  accept() ended: " + e);
    }
};
Thread t = virtual ? Thread.ofVirtual().start(accept) : Thread.ofPlatform().start(accept);
// ... once t is waiting in accept():
t.interrupt();
t.join(500);
System.out.println(kind + ": after interrupt(), alive 500 ms later: " + t.isAlive());
if (t.isAlive()) {
    server.close();
    t.join(500);
    System.out.println(kind + ": after close(), alive: " + t.isAlive());
}
```

```text
platform: after interrupt(), alive 500 ms later: true
  accept() ended: java.net.SocketException: Socket closed
platform: after close(), alive: false
```

`interrupt()` only set the flag. The waiting methods of the earlier sections are written in Java
and check the flag when they are woken; a platform thread in a socket call is blocked in the
operating system, and `java.io` gives the interrupt nothing to wake. Closing the socket from
another thread does: the blocked call fails with an exception. So a component that owns a
blocking stream stops by closing it, and its read loop treats that exception as the end of the
work, not as a failure to report.

The same code with `virtual` set to `true`:

```text
  accept() ended: java.net.SocketException: Closed by interrupt
virtual: after interrupt(), alive 500 ms later: false
```

> **Virtual threads differ.** On a virtual thread, Java 21 makes blocking socket operations
> interruptible: the interrupt closes the socket and the call throws. NIO channels
> (`InterruptibleChannel`) behave that way on any thread, with `ClosedByInterruptException`.
> In both cases the socket or channel is closed afterwards, and the interrupt flag stays set.

<!-- practice 4 -->

## Summary

- **`interrupt()` sets a flag; running code stops only where it checks.** A loop with no check
  finishes its whole batch after shutdown, and `Thread.interrupted()` in an inner loop erases the
  request before the outer loop sees it.
- **A blocking call answers by throwing, and the throw clears the flag.** A catch that logs and
  carries on leaves a loop that can't be stopped, and a shutdown that waits out its timeout.
- **Whoever catches `InterruptedException` rethrows it, or restores it and stops.** An empty catch
  ignores the request; restoring and carrying on turns every later wait into a busy loop.
- **`Future.cancel(true)` only asks.** `isCancelled()` is true at once; whether the task stops,
  and before which side effect, is decided by the task's own checks.
- **Classic I/O doesn't hear an interrupt.** Stop a reader by closing what it waits on, and treat
  the exception that follows as a stop, not as a failure.

## Reference

**Reading and setting the flag:**

| Call | Effect on the flag |
|---|---|
| `t.interrupt()` | sets `t`'s flag; wakes `t` if it is blocked in an interruptible call |
| `t.isInterrupted()` | reads `t`'s flag, leaves it as it is |
| `Thread.interrupted()` | reads the **current** thread's flag and clears it |

**What a blocked thread does when interrupted (Java 21, platform threads unless noted):**

| Blocked in | On `interrupt()` | Flag afterwards |
|---|---|---|
| `Thread.sleep`, `Object.wait`, `Thread.join` | throws `InterruptedException` | cleared |
| `BlockingQueue.take`/`put`/`poll(timeout)`, `CountDownLatch.await`, `Future.get` | throws `InterruptedException` | cleared |
| `Lock.lockInterruptibly`, `Condition.await` | throws `InterruptedException` | cleared |
| `Lock.lock`, entering `synchronized` | keeps waiting for the lock | set |
| NIO channel read or write (`InterruptibleChannel`) | throws `ClosedByInterruptException`, closes the channel | set |
| socket `accept`/`read`, `InputStream.read` | keeps waiting; closing the socket ends it with `SocketException` | set |
| the same socket call on a virtual thread | throws `SocketException`, closes the socket | set |

A thread whose flag is already set when it enters one of the first three rows throws at once if
the call would have to wait. A `join` on a thread that has ended, or a `get` on a finished
`Future`, returns without looking at the flag.

**Edge cases (Java 21):**

| Situation | What happens |
|---|---|
| `interrupt()` before `start()` | the thread starts with its flag set; the javadoc says this "need not have any effect", so don't rely on it |
| `interrupt()` on a terminated thread | no exception; the javadoc says it "need not have any effect" (in JDK 21, `isInterrupted()` then returns `true`), and nothing is left to act on it |
| `Thread.stop()` | deprecated for removal; since Java 20 it throws `UnsupportedOperationException` |
| `ExecutorService.shutdown()` | interrupts no running task; running tasks finish, queued ones still run |
| `ExecutorService.shutdownNow()` | interrupts every running task; returns the queued ones without running them |
| `Future.cancel(false)` | never interrupts; a running task carries on |

## Challenges

These have no single answer. Try them with the `Stops` helper.

1. A task copies rows from one database to another in transactions of 1,000 rows. Where do you
   check the flag, and what does a reader of the target database see if the check is in the
   wrong place?
2. Your task calls a third-party client that swallows `InterruptedException` inside its retry
   loop. You can't change the client. How do you make your task stoppable, and what does it cost?
3. Measure how long `shutdownNow()` plus `awaitTermination` takes for a pool of 50 tasks, each one
   of the shapes in this unit. Which one decides your deploy time?
4. Structured concurrency (`StructuredTaskScope`, a preview API in Java 21) has a policy,
   `ShutdownOnFailure`, that cancels the remaining subtasks when one fails, by interrupting their
   threads. What must each subtask
   do for that to work, and what happens to one that calls the client from challenge 2?

## Design Note: Why Java Has No Safe stop()

Java 1.0 had `Thread.stop()`, which stopped a thread from outside by throwing an error into it
wherever it was. The error released every monitor the thread held on the way out. A thread
stopped between debiting one account and crediting another released the lock with the money taken
from the first and never added to the second, and the next thread to take the lock saw that state as if it were consistent. No code
can defend against an exception that can appear between any two of its instructions, so the
method was deprecated in Java 1.2 and, since Java 20, it throws instead of stopping anything.

The replacement leaves the decision to the code that runs, which knows where it is safe to stop.
The request is a flag, and the JDK's waiting methods turn it into an exception. The price is that
cancellation works only if every layer between the request and the work cooperates. One empty
catch in a library three calls down, or a reader blocked in a socket that nobody closes, and the
whole pool can't stop.

## Code Examples

- Source: [ThreadStopping.java](src/main/java/com/github/msorkhpar/claudejavatutor/threadbasics/ThreadStopping.java)
- Test: [ThreadStoppingTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/threadbasics/ThreadStoppingTest.java)
