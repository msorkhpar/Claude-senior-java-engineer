# 6.4.1. ExecutorService Interface and Implementations

The notification service sends its mail through `Executors.newFixedThreadPool(8)`. During an
incident the SMTP relay stops answering, and an hour later the service dies with
`OutOfMemoryError`. The heap dump is dominated by one `LinkedBlockingQueue`. The first fix raised the
pool to 8 core threads and 64 at most, and the next incident went the same way.

An `ExecutorService` takes tasks and runs them on threads it manages. Behind most factories in
`Executors` is one class, `ThreadPoolExecutor`, and its rules for when to queue a task, when to
start a thread and when to refuse decide what happens under load.

## A Pool Queues Before It Grows: Threads Past the Core Start Only When the Queue Is Full

A `ThreadPoolExecutor` has a core size, a maximum size and a work queue. Here the core is 2, the
maximum 4, and the queue holds 2. Each task holds its thread until `release` opens:

```java
CountDownLatch release = new CountDownLatch(1);
Runnable busy = () -> await(release);              // a task that holds its thread until released
ThreadPoolExecutor pool = new ThreadPoolExecutor(2, 4, 60, SECONDS, new ArrayBlockingQueue<>(2));
for (int task = 1; task <= 7; task++) {
    try {
        pool.execute(busy);
        System.out.printf("task %d: threads=%d queued=%d%n", task, pool.getPoolSize(), pool.getQueue().size());
    } catch (RejectedExecutionException e) {
        System.out.printf("task %d: %s%n", task, e.getClass().getSimpleName());
    }
}
```

Pause before the output: when does the third thread start?

```text
task 1: threads=1 queued=0
task 2: threads=2 queued=0
task 3: threads=2 queued=1
task 4: threads=2 queued=2
task 5: threads=3 queued=2
task 6: threads=4 queued=2
task 7: RejectedExecutionException
```

Not at task 3. Below the core size, every task gets a new thread. At the core size, a task goes
into the queue. Only when the queue refuses it does the pool start a thread past the core, up to
the maximum, and when that is reached too, the task is rejected.
That order is why the first fix did nothing. Raising the maximum helps only if the queue can fill,
and a `LinkedBlockingQueue` built without a capacity holds `Integer.MAX_VALUE` tasks:

```text
8..64 threads, unbounded queue: threads=8 queued=9992
newFixedThreadPool(8): core=8 max=8 queue=LinkedBlockingQueue capacity=2147483647
```

With 10,000 tasks the pool stayed at 8 threads. `newFixedThreadPool` is built the same way, so
when the relay stops answering, every new mail waits in memory. Here `mailer` is
`newFixedThreadPool(8)`, `relayDown` a latch that never opens, and the heap 256 MB (`-Xmx256m`):

```java
while (true) {
    byte[] mail = new byte[2_048];             // one rendered notification
    mailer.execute(() -> { await(relayDown); send(mail); });
    queued++;
}
```

```text
java.lang.OutOfMemoryError: Java heap space after 117483 mails queued
```

The queue adds to every task's latency, too: a mail that waits behind 100,000 others is late even
when it doesn't fail. The factories in `Executors` choose the queue's capacity for you (see the
Reference).

## A Full Pool Has to Refuse Work: Bound the Queue and Choose Who Pays

With a bounded queue, some task finds no room, and the pool's `RejectedExecutionHandler` decides
what happens to it. Here a pool of one thread and a queue of 2 gets five tasks under each built-in
`policy`; `ran` and `refused` record the outcome:

```java
ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 1, 0, SECONDS, new ArrayBlockingQueue<>(2), policy);
for (int task = 1; task <= 5; task++) {          // 1 runs, 2 and 3 wait, 4 and 5 find it full
    int id = task;
    try {
        pool.execute(() -> {
            if (id == 1) await(release);
            ran.add(id + (Thread.currentThread().getName().equals("main") ? " (on main)" : ""));
        });
    } catch (RejectedExecutionException e) {
        refused.append(" ").append(id);
    }
}
```

```text
AbortPolicy          execute() threw for: 4 5 ran: [1, 2, 3]
CallerRunsPolicy     execute() threw for: -   ran: [4 (on main), 5 (on main), 1, 2, 3]
DiscardPolicy        execute() threw for: -   ran: [1, 2, 3]
DiscardOldestPolicy  execute() threw for: -   ran: [1, 4, 5]
```

- **`AbortPolicy`**, the default, throws `RejectedExecutionException` to the code that submitted.
  That code has to handle it: answer 503, or leave the message in its broker for a later retry.
  Uncaught, it fails whichever request arrived at the wrong moment.
- **`CallerRunsPolicy`** runs the task on the submitting thread. While `main` ran tasks 4 and 5, it
  submitted nothing, so the producer slows to the pool's pace and, while the pool runs, nothing is
lost. The cost falls
  on whatever else that thread does: if it accepts connections or reads from a broker, it stops
  doing that while it runs the task.
- **`DiscardPolicy`** drops the new task, and **`DiscardOldestPolicy`** drops the oldest waiting
  one (here tasks 2 and 3) to make room. Neither tells anybody. Use them only for work nobody
  needs to know was lost, such as a periodic sample the next one replaces.

Under load, a pool with an unbounded queue never reaches its policy. That is why a production pool is usually
built with `new ThreadPoolExecutor(...)`: you choose a capacity and a policy, and a `ThreadFactory`
that names the threads (unit 6.1.1).

<!-- practice 1 -->

## execute() Hands a Task's Exception to the Thread; submit() Keeps It in the Future

`execute(Runnable)` comes from `Executor`, the one-method interface that `ExecutorService`
extends. `ThreadPoolExecutor`'s `submit` wraps the task in a `FutureTask` and returns it as a
`Future` (unit 6.4.2). Both tasks here throw, on a pool with one thread:

```java
ExecutorService pool = Executors.newFixedThreadPool(1);
pool.execute(() -> { throw new IllegalStateException("card declined (execute)"); });
Future<?> f = pool.submit(() -> { throw new IllegalStateException("card declined (submit)"); });
pool.submit(() -> System.out.println("next task runs on " + Thread.currentThread().getName())).get();
System.out.println("the submitted task's Future: isDone=" + f.isDone());
try {
    f.get();
} catch (ExecutionException e) {
    System.out.println("get() throws: " + e.getCause());
}
```

```text
Exception in thread "pool-1-thread-1" java.lang.IllegalStateException: card declined (execute)
	at Examples.lambda$failures$4(Examples.java:119)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
	at java.base/java.lang.Thread.run(Thread.java:1583)
next task runs on pool-1-thread-2
the submitted task's Future: isDone=true
get() throws: java.lang.IllegalStateException: card declined (submit)
```

The task run with `execute()` threw out of the worker's `run()`, so the exception ended that
thread like any uncaught exception (unit 6.1.2): the default handler printed it, and the pool
started `pool-1-thread-2` to replace the worker. The submitted task threw inside
`FutureTask.run()`, which caught the exception and stored it in the `Future`. Nothing was printed.
Only `get()` shows that failure, wrapped in `ExecutionException`.

So `submit()` isn't the safer of the two. Code that submits and drops the `Future`, a common way
to "run it in the background", loses every failure without a log line: the nightly settlement
reports success, and three accounts were never settled.

`afterExecute(task, thrown)`, which `ThreadPoolExecutor` calls on the worker after every task, misses
it too (the stack trace on stderr is left out):

```java
ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 1, 0, SECONDS, new LinkedBlockingQueue<>()) {
    @Override
    protected void afterExecute(Runnable task, Throwable thrown) {
        System.out.println("afterExecute: thrown=" + thrown + ", task is a Future: " + (task instanceof Future));
    }
};
pool.execute(() -> { throw new IllegalStateException("card declined (execute)"); });
pool.submit(() -> { throw new IllegalStateException("card declined (submit)"); });
```

```text
afterExecute: thrown=java.lang.IllegalStateException: card declined (execute), task is a Future: false
afterExecute: thrown=null, task is a Future: true
```

For the submitted task `thrown` is `null`, and `task` is the `FutureTask`, already done. A hook that
reports every failure has to ask that `Future` for its outcome, and `get()` doesn't wait there; the
`afterExecute` javadoc shows how, cancelled tasks included. Without such a hook, either the
submitter keeps and reads the `Future`, or the task catches and reports its own failure.

<!-- practice 2 -->

## shutdown() Stops Intake, Not Work: Wait for It With awaitTermination()

A fixed pool's threads, made by the default factory, are not daemons (unit 6.1.1), so a pool
nobody shuts down keeps the JVM running. `shutdown()` is the first step. Here one task holds the
pool's only thread and three wait:

```java
ExecutorService pool = Executors.newSingleThreadExecutor();
pool.execute(() -> { await(release); ran.incrementAndGet(); });  // running
for (int i = 0; i < 3; i++) pool.execute(ran::incrementAndGet);  // queued
pool.shutdown();
System.out.println("after shutdown(): isShutdown=" + pool.isShutdown() + " isTerminated=" + pool.isTerminated()
        + " ran=" + ran.get());
try {
    pool.execute(ran::incrementAndGet);
} catch (RejectedExecutionException e) {
    System.out.println("a new task: " + e.getClass().getSimpleName());
}
release.countDown();
boolean done = pool.awaitTermination(5, SECONDS);
System.out.println("awaitTermination: " + done + " ran=" + ran.get());
```

```text
after shutdown(): isShutdown=true isTerminated=false ran=0
a new task: RejectedExecutionException
awaitTermination: true ran=4
```

`shutdown()` returned at once. It refused the new task and kept the four it had: the queued ones
still ran. `awaitTermination` is what waits, and it returns `false` if its time runs out first.

A shutdown hook that calls `pool.shutdown()` and then closes the database connections breaks the
queued tasks: they run after `shutdown()` returns, against closed connections. Close what the
tasks use only after `awaitTermination` returns `true`. When it returns `false`, `shutdownNow()`
interrupts the running tasks and hands back the queued ones (unit 6.1.3), and a second
`awaitTermination` gives the interrupted tasks time to end. The `ExecutorService` javadoc gives
this two-step stop as its example.

Since Java 19 an `ExecutorService` is `AutoCloseable`, and `close()` is `shutdown()` plus waiting,
with no time limit. A task that never ends holds it there:

```java
ExecutorService pool = Executors.newFixedThreadPool(2);
Thread deploy = new Thread(() -> {
    try (pool) {
        pool.submit(() -> {
            try {
                new CountDownLatch(1).await();           // a task that never finishes
            } catch (InterruptedException e) {
                System.out.println("  task interrupted");
            }
        });
    }                                                   // close() here
    System.out.println("  close() returned, interrupted=" + Thread.currentThread().isInterrupted());
}, "deploy");
deploy.start();
deploy.join(1_000);
System.out.println("after 1 s: close() still waiting=" + deploy.isAlive());
deploy.interrupt();
deploy.join();
```

```text
after 1 s: close() still waiting=true
  task interrupted
  close() returned, interrupted=true
```

Only an interrupt of the closing thread made `close()` call `shutdownNow()`. Try-with-resources
fits a scope whose tasks are known to end, such as one batch of requests. A long-lived pool that
must stop within a deploy's grace period gets the two-step stop with a timeout.

<!-- practice 3 -->

## A Pool's Size Is Also a Limit on Concurrency, and Some Executors Have None

A thousand tasks each make one 20 ms database query, on three executors. `inside` counts the
queries in flight and `most` keeps its peak; "threads made" is `getLargestPoolSize()`, the most
threads alive at once:

```java
try (ExecutorService pool = kind.pool()) {
    for (int call = 0; call < 1_000; call++) {
        pool.execute(() -> {
            most.accumulateAndGet(inside.incrementAndGet(), Math::max);
            try {
                Thread.sleep(20);                    // one database query
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            inside.decrementAndGet();
        });
    }
}
```

```text
newFixedThreadPool(10)             at most   10 queries at once,  2021 ms, threads made 10
newCachedThreadPool()              at most  546 queries at once,    67 ms, threads made 549
newVirtualThreadPerTaskExecutor()  at most 1000 queries at once,    33 ms
```

The fixed pool kept the database at 10 queries, though nobody wrote 10 down as a limit for the
database. `newCachedThreadPool` has no limit: it starts a platform thread whenever no idle one is
waiting, and 549 were alive at once here (the count varies from run to run). `newVirtualThreadPerTaskExecutor()` starts a virtual
thread per task, and all 1,000 queries ran together.

That is what virtual threads are for (unit 6.1.1): a virtual thread that waits gives its carrier
thread back, so waiting costs almost nothing, and the number of
tasks needs no pool to cap it. Replacing a fixed pool of 10 with virtual threads also removes the
limit of 10 that the database or a vendor's API relied on. The database gets 1,000 queries at
once, or the vendor answers 429. Limit what the downstream can take where the call
is made, with a `Semaphore` of 10 permits around the query (unit 6.3.2), and leave the executor
unbounded.

> **Java 21 pins.** In Java 21 a virtual thread that blocks inside `synchronized` keeps its carrier
> thread, so a JDBC driver that waits under a monitor costs a platform thread after all. JDK 24
> removed that limitation (JEP 491).

<!-- practice 4 -->

## Summary

- **A pool queues before it grows.** Threads past the core start only when the queue is full, so
  with an unbounded queue the maximum is never used and waiting work grows in memory.
- **A bounded pool has to refuse.** `AbortPolicy` throws to the submitter, `CallerRunsPolicy` makes
  the submitter do the work, and the two discard policies lose tasks silently.
- **`execute()` hands a failure to the thread, `submit()` keeps it in the `Future`.** A dropped
  `Future` loses the failure, and `afterExecute` gets `thrown == null` for it.
- **`shutdown()` stops intake, not work.** Queued tasks still run; wait with `awaitTermination`,
  then fall back to `shutdownNow()`. `close()` waits without a limit.
- **A pool's size limits concurrency.** The cached pool and the virtual-thread executor have no
  limit, so bound the downstream call itself.

## Reference

**What the factories build (Java 21; printed, the rest from the `Executors` javadoc):**

| Factory | Core / max threads | Queue | Notes |
|---|---|---|---|
| `newFixedThreadPool(n)` | n / n | `LinkedBlockingQueue`, capacity `Integer.MAX_VALUE` | |
| `newCachedThreadPool()` | 0 / `Integer.MAX_VALUE` | `SynchronousQueue`, capacity 0 | idle threads end after 60 s |
| `newSingleThreadExecutor()` | one thread | unbounded | a wrapper, not a `ThreadPoolExecutor`; runs tasks one at a time, in order |
| `newScheduledThreadPool(n)` | n / `Integer.MAX_VALUE` | `DelayedWorkQueue`, unbounded | the maximum has no effect; a `ScheduledExecutorService`: `schedule`, `scheduleAtFixedRate` |
| `newVirtualThreadPerTaskExecutor()` | a new virtual thread per task | none | not a `ThreadPoolExecutor` |
| `new ThreadPoolExecutor(...)` | your choice | your choice | default policy `AbortPolicy` |

**Lifecycle (`ExecutorService` javadoc, and as ran):**

| Call | Effect |
|---|---|
| `shutdown()` | refuses new tasks; running and queued tasks still run; returns at once |
| `shutdownNow()` | refuses new tasks, tries to stop running ones (`ThreadPoolExecutor` interrupts them), returns the queued ones unrun |
| `awaitTermination(t, unit)` | waits until every task has ended after a shutdown; `false` on timeout |
| `isShutdown()` / `isTerminated()` | shutdown was called / it was, and every task has ended |
| `close()` (Java 19+) | `shutdown()`, then waits with no limit; if interrupted, `shutdownNow()`, waits on, and sets the flag again |
| a task submitted after shutdown | goes to the policy: `AbortPolicy` throws `RejectedExecutionException`; `CallerRunsPolicy` discards it silently |

The keep-alive time (60 s in the first example) is how long a thread past the core may stay idle
before it ends. `ThreadPoolExecutor` also has `beforeExecute`, `afterExecute` and `terminated` hooks for
subclasses (they do nothing by default), and `getPoolSize()`, `getActiveCount()`, `getQueue()`, `getCompletedTaskCount()` and
`getLargestPoolSize()` for monitoring.

## Challenges

1. An API gateway hands requests to a pool with a bounded queue. Which policy would you choose,
   and what does the client see with each? What changes if the submitting thread is a Netty event
   loop?
2. A pool of 8 threads runs tasks of about 50 ms. How long can a new task wait behind a full queue
   of 1,000? Choose a capacity for a 2-second latency budget.
3. Measure a deploy: how long do `shutdown()` and `awaitTermination` take for a pool of 8 with 200
   queued tasks, and how many tasks does `shutdownNow()` hand back after a 5-second grace?

## Design Note: Why a Pool Queues Before It Grows

`ThreadPoolExecutor` could start threads up to the maximum first and queue only after that. Its
javadoc states the opposite order without giving a reason. The likely one is cost: a platform
thread has a native stack and a place in the OS scheduler, so a pool that queues absorbs a short
burst with the threads it already has, and starts more only when the queue is full.

The price is that the maximum depends on the queue in a way the constructor doesn't show. With an
unbounded queue the maximum does nothing; with a `SynchronousQueue`, as in the cached pool, the
core does little, because every task without an idle thread gets a new one.

## Code Examples

- Source: [ExecutorServiceDemo.java](src/main/java/com/github/msorkhpar/claudejavatutor/executors/ExecutorServiceDemo.java)
- Test: [ExecutorServiceDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/executors/ExecutorServiceDemoTest.java)
