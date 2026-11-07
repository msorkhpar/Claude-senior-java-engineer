# 6.4.2. Callable and Future for Returning Results

A price-comparison page asks four suppliers for a quote. Each request is a `Callable` submitted to
a pool of eight threads, and the page waits for each `Future` with `get(2, SECONDS)`, showing the
cached price when a supplier is late. On Tuesday one supplier's API stopped answering without
closing its connections. Within minutes every page view showed nothing but cached prices, from
healthy suppliers too, and all eight threads were waiting on the one supplier that had stopped.

A `Future` carries a task's result, its failure and its cancellation back to the caller. Five rules
decide what comes back, one per section.

The examples submit to pools from unit 6.4.1 on Java 21, and most use this helper, a supplier that
answers after a delay:

```java
/** A supplier that answers with a price after the given delay. */
static Callable<Integer> quote(String supplier, long delayMs, int cents) {
    return () -> {
        Thread.sleep(delayMs);
        return cents;
    };
}
```

## A Callable's Result and Its Exception Both Come Back Through get()

`Callable<V>.call()` returns a `V` and may throw any `Exception`, checked ones included;
`Runnable.run()` can do neither. Here `fetchQuote` throws `IOException` because the supplier's API
is down:

```java
Future<Integer> acme = pool.submit(() -> fetchQuote("acme"));
try {
    acme.get();
} catch (ExecutionException e) {
    System.out.println("get() threw: " + e);
    System.out.println("getCause():  " + e.getCause());
}

Future<Integer> nobodyAsks = pool.submit(() -> fetchQuote("globex"));
while (!nobodyAsks.isDone()) Thread.onSpinWait();
System.out.println("never read: isDone()=" + nobodyAsks.isDone() + ", state()=" + nobodyAsks.state());
```

```text
get() threw: java.util.concurrent.ExecutionException: java.io.IOException: acme: HTTP 503
getCause():  java.io.IOException: acme: HTTP 503
never read: isDone()=true, state()=FAILED
```

The pool catches whatever `call()` throws and stores it in the `Future`. `get()` then throws
`ExecutionException` with the original as its cause, whatever its type, an `Error` included. The
wrapper exists because `get()` must declare what it throws and can't know your task's checked
exceptions. A `catch (IOException e)` around `get()` doesn't even compile; the handling has to look
at `getCause()`.

The second quote failed the same way and printed nothing: `submit` reports a failure only to
whoever calls `get()`. (With `execute`, the failure reaches the thread's uncaught-exception handler
instead, as unit 6.4.1 shows.) `isDone()` is `true` whether the task succeeded, failed or was
cancelled; `state()` (Java 19) tells them apart. A job that submits work and never reads the futures
loses every failure this way.

`get()` throws one more checked exception, `InterruptedException`. It means the *waiting* thread
was interrupted, not that the task failed: the task keeps running, and the caller handles the
interrupt as in unit 6.1.3.

<!-- practice 1 -->

## A Timed get() Ends the Wait, Not the Task

An untimed `get()` waits as long as the task takes, so a supplier that never answers makes it wait
forever. `get(timeout, unit)` throws `TimeoutException` instead. Here a pool of one thread asks a
supplier that hangs for two seconds, waits 100 ms for it, and then asks the next supplier. After the
timeout the caller does nothing, calls `cancel(false)`, or calls `cancel(true)`:

```java
Future<Integer> slow = pool.submit(quote("initech", 2_000, 1875)); // the supplier that hangs
try {
    slow.get(100, TimeUnit.MILLISECONDS);
} catch (TimeoutException e) {
    if (handling.equals("cancel(false)")) slow.cancel(false);
    if (handling.equals("cancel(true)")) slow.cancel(true);
}
Future<Integer> next = pool.submit(quote("acme", 10, 1999)); // the next page view's quote
next.get();
System.out.printf("%-13s isCancelled()=%-5s next quote answered after %d ms%n", handling,
        slow.isCancelled(), (System.nanoTime() - start) / 1_000_000);
```

```text
just give up  isCancelled()=false next quote answered after 2011 ms
cancel(false) isCancelled()=true  next quote answered after 2010 ms
cancel(true)  isCancelled()=true  next quote answered after 110 ms
```

With no cancel, the task kept the pool's only thread, and the next quote queued behind it for two
seconds.

`cancel(false)` returns `true`, and from then on the `Future` is cancelled:
`isCancelled()` is `true` and `get()` throws `CancellationException`. But on a task that has
already started, `cancel(false)` only gives up the result. It doesn't touch the thread, which runs
the task to the end. `cancel(true)` also interrupts the thread, and a task that responds to interrupts
stops (unit 6.1.3). `Thread.sleep` does, so the next quote was answered 10 ms after the timeout.

In the opening outage, each page view that timed out on the stuck supplier left a thread
waiting on it. After eight such views the pool had no thread left, and quotes from healthy
suppliers timed out in the queue. A fallback after a timeout needs the `cancel(true)` too.

<!-- practice 2 -->

## Futures Read in Submission Order Wait for the Slowest; a CompletionService Hands Them Back as They Finish

Three suppliers answer in 300, 100 and 200 ms, on a pool of three threads. We read their futures
in a loop, then submit them again through an `ExecutorCompletionService` around the same pool:

```java
List<Future<Integer>> futures = new ArrayList<>();
for (Callable<Integer> q : quotes) futures.add(pool.submit(q));
for (Future<Integer> f : futures) {
    int cents = f.get();
    System.out.printf("in order:   %d at %d ms%n", cents, (System.nanoTime() - start) / 1_000_000);
}

start = System.nanoTime();
CompletionService<Integer> done = new ExecutorCompletionService<>(pool);
for (Callable<Integer> q : quotes) done.submit(q);
for (int i = 0; i < quotes.size(); i++) {
    int cents = done.take().get();                 // the next one to finish
    System.out.printf("as done:    %d at %d ms%n", cents, (System.nanoTime() - start) / 1_000_000);
}
```

```text
in order:   1875 at 300 ms
in order:   1999 at 300 ms
in order:   2450 at 300 ms
as done:    1999 at 100 ms
as done:    2450 at 200 ms
as done:    1875 at 300 ms
```

The tasks ran in parallel both times. The loop's first `get()` waited for the
slowest supplier, and the quotes that were ready at 100 and 200 ms sat in their futures until then.
A `CompletionService` puts each future on a queue when its task completes, so `take()` returns the
next one to finish. The `get()` after it returns at once, or throws `ExecutionException`
with the task's exception as its cause.

The batch still takes 300 ms, but a page that shows each quote as it arrives shows the first at
100 ms instead of 300, and a caller that needs any two quotes can stop after two.

<!-- practice 3 -->

## invokeAll's Deadline Cancels the Stragglers, and Their get() Throws an Unchecked CancellationException

`invokeAll(tasks)` submits every task, waits until all are done and returns their futures in the
order of the tasks. `invokeAll(tasks, timeout, unit)` waits at most that long for the whole batch,
then cancels every task not yet done; the JDK's pools do it with an interrupt. Here three suppliers share 300 ms: one
answers in 50 ms, one fails, one takes five seconds. The loop handles a failed quote:

```java
long start = System.nanoTime();
List<Future<Integer>> futures = pool.invokeAll(quotes, 300, TimeUnit.MILLISECONDS);
System.out.printf("invokeAll returned after %d ms%n", (System.nanoTime() - start) / 1_000_000);
for (Future<Integer> f : futures) System.out.println("  " + f.state());

try {
    for (Future<Integer> f : futures) {
        try {
            System.out.println("quote: " + f.get());
        } catch (ExecutionException e) {
            System.out.println("no quote: " + e.getCause().getMessage());
        }
    }
} catch (RuntimeException e) {
    System.out.println("the loop ended with " + e);
}
```

```text
invokeAll returned after 300 ms
  SUCCESS
  FAILED
  CANCELLED
quote: 1999
no quote: globex: HTTP 503
the loop ended with java.util.concurrent.CancellationException
```

The late supplier is still in the list, in its place, with a cancelled future. Its `get()` throws
`CancellationException`, which is unchecked. The compiler made the loop handle
`ExecutionException` and said nothing about this one, so the first late supplier ended the loop. In
production that exception leaves the request handler, and the page loses the quotes it already
had. Code that reads a future that can be cancelled catches `CancellationException` too.

Calling `get(remaining, unit)` on each future in turn waits just as long, but leaves the late task
running unless you cancel it yourself; the timed `invokeAll` cancels it for you.

<!-- practice 4 -->

## invokeAny Returns the First Success, Not the First to Finish

A hedged read sends the same query to three replicas and keeps the first answer. Replica 1 fails
after 20 ms, replica 2 answers after 100 ms, replica 3 would take two seconds. `invokeAny` gets all
three; a `CompletionService` written by hand keeps the first future to finish (replica 3 can't change
its answer, so it is left out). A last call gives `invokeAny` two replicas that both fail:

```java
List<Callable<String>> replicas = List.of(
        () -> { Thread.sleep(20); throw new IOException("replica-1: connection reset"); },
        () -> { Thread.sleep(100); return "replica-2: 1999"; },
        () -> {
            try {
                Thread.sleep(2_000);
                return "replica-3: 1999";
            } catch (InterruptedException e) {
                System.out.println("  replica-3 interrupted at " + (System.nanoTime() - anyStart) / 1_000_000 + " ms");
                throw e;
            }
        });
anyStart = System.nanoTime();
String answer = pool.invokeAny(replicas);
System.out.printf("invokeAny: %s after %d ms%n", answer, (System.nanoTime() - anyStart) / 1_000_000);
```

```java
CompletionService<String> first = new ExecutorCompletionService<>(pool);
for (Callable<String> r : replicas.subList(0, 2)) first.submit(r);
try {
    System.out.println("first to finish: " + first.take().get());
} catch (ExecutionException e) {
    System.out.println("first to finish: " + e.getCause());
}

List<Callable<String>> allFail = List.of(
        () -> { throw new IOException("replica-1: connection reset"); },
        () -> { throw new IOException("replica-2: connection reset"); });
try {
    pool.invokeAny(allFail);
} catch (ExecutionException e) {
    System.out.println("all failed: " + e);
}
```

```text
invokeAny: replica-2: 1999 after 100 ms
  replica-3 interrupted at 100 ms
first to finish: java.io.IOException: replica-1: connection reset
all failed: java.util.concurrent.ExecutionException: java.io.IOException: replica-1: connection reset
```

`invokeAny` returns a value, not a future. It skipped the failure that finished first, returned the
first successful result, and cancelled replica 3 as soon as it had one, here with an interrupt. The hand-written version
returned replica 1's failure while a correct answer was 80 ms away. `invokeAny` throws
`ExecutionException` only when every task fails, with one of their exceptions as the cause; which
one is not specified, and in other runs it was replica 2's.

<!-- practice 5 -->

## Summary

- **A task's result and its exception both come back through `get()`.** The exception arrives
  wrapped in `ExecutionException`; a future nobody reads fails silently.
- **A timed `get()` ends the wait, not the task.** After a timeout, `cancel(true)` frees the
  thread if the task responds to interrupts; `cancel(false)` only gives up the result.
- **Reading futures in submission order waits for the slowest.** A `CompletionService` returns
  each one as it finishes.
- **`invokeAll` with a timeout cancels the late tasks and keeps their places.** Their `get()`
  throws the unchecked `CancellationException`, which a loop must catch.
- **`invokeAny` returns the first success and cancels the rest.** It fails only when every task
  fails.

## Reference

**What `Future.get()` throws (Java 21):**

| Situation | `get()` / `get(timeout, unit)` |
|---|---|
| the task returned `v` | returns `v`; a `Runnable` given to `submit` returns `null` |
| the task threw anything, an `Error` included | `ExecutionException`, the original as `getCause()` |
| the future was cancelled, with `true` or `false` | `CancellationException` (unchecked) |
| the timed `get` ran out | `TimeoutException`; the task keeps running |
| the waiting thread is interrupted | `InterruptedException`, flag cleared; the task keeps running |

**Cancelling and inspecting:**

| Call | Result |
|---|---|
| `cancel(false)` on a running task | `true`; the future is cancelled, the thread runs the task to the end |
| `cancel(true)` on a running task | `true`; also interrupts the thread |
| `cancel(...)` on a finished or cancelled future | `false`; nothing changes |
| `isDone()` | `true` after success, failure or cancellation |
| `state()` (Java 19+) | `RUNNING`, `SUCCESS`, `FAILED` or `CANCELLED` |
| `resultNow()` / `exceptionNow()` (Java 19+) | the result or the exception, without waiting; `IllegalStateException` if the future is not in that state |

**Batches:**

| Call | Result |
|---|---|
| `invokeAll(tasks)` | waits for all; futures in task order, all done |
| `invokeAll(tasks, timeout, unit)` | the same, but cancels the tasks not done at the deadline (the JDK's pools interrupt them) |
| `invokeAny(tasks)` | the first successful result; cancels the rest; `ExecutionException` if all fail |
| `invokeAny(tasks, timeout, unit)` | `TimeoutException` if no task succeeds in time |
| `invokeAll` / `invokeAny` of an empty list | an empty list / `IllegalArgumentException` |

## Challenges

1. The comparison page shows the cheapest of five quotes and must answer within 400 ms. Would you
   use a timed `invokeAll` or a `CompletionService` with a deadline? What does each show when two
   suppliers are late?
2. A supplier's client blocks in a socket read that ignores interrupts (unit 6.1.3), so
   `cancel(true)` after a timeout doesn't free the thread. How do you keep that supplier from
   filling the pool?
3. A hedged read with `invokeAny` doubles the load on the replicas. When is that worth it, and how
   would you send the second request only when the first is slow?

## Design Note: A Future Is Something You Wait On

`Future` (Java 5) is a pull model: the caller asks for the result and blocks until it is there.
Everything above follows from that. The caller decides how long to wait, in what order to read and
whether to read at all, and a result nobody pulls is lost, failure included.

`CompletableFuture` (Java 8) adds the push side: `thenApply` or `exceptionally` attach what happens
next, and it runs when the result arrives, with no thread blocked in `get()`. Failures then travel
through a chain of callbacks instead of up a stack, and a chain whose end nobody handles loses its
failure the same way. With virtual threads (Java 21), a blocked `get()` parks a virtual thread
rather than holding an operating-system thread, which makes the plain pull model affordable
for many request handlers.

## Code Examples

- Source: [CallableFutureDemo.java](src/main/java/com/github/msorkhpar/claudejavatutor/executors/CallableFutureDemo.java)
- Test: [CallableFutureDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/executors/CallableFutureDemoTest.java)
