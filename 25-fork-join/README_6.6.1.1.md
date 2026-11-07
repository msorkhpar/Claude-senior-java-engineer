# 6.6.1.1. Parallelizing Tasks with ForkJoinPool

The analytics service's thread count climbs all day, from 90 threads at 08:00 to several thousand
by evening, and some evenings the JVM can no longer start a thread. Each request aggregates its rows
in `new ForkJoinPool(4)`. In the same service, the morning scoring job runs on a pool of four and
takes as long as it did on one thread.

Unit 6.6.1 showed why blocking work belongs on a pool of its own, away from the common pool. This
unit is about owning one.

## A Pool You Create Keeps Its Threads Until You Close It

A handler that makes a pool per request and never closes it:

```java
static long handleLeaky(long n) {
    ForkJoinPool pool = new ForkJoinPool(4);
    return pool.submit(() -> LongStream.range(0, n).parallel().map(x -> x * x).sum()).join();
}
```

```text
200 requests, each pool closed:   0 pool threads alive
200 requests, pools never closed: 800 pool threads alive
```

Every abandoned pool kept its four workers. They are daemon threads, so a short program still
exits and the leak shows only in a server. Idle workers do end after a keep-alive time (60 s by
default), but on Java 21 each pool retired one per minute: 50 abandoned pools, 200 idle threads,
still had 48 three minutes later. Under steady traffic they pile up until the JVM can't
create a native thread.
`close()` (Java 19) shuts the pool down and waits for its tasks, so `try`-with-resources does it;
before 19, call `shutdown()` in a `finally`. Closing the common pool does nothing.

<!-- practice 1 -->

## invoke() Waits for One Task; submit() Lets Them All Run

Four regions to score, about 200 ms of CPU each, on a pool of four:

```java
for (String r : REGIONS) pool.invoke(new Score(r));

List<ForkJoinTask<Long>> tasks = new ArrayList<>();
for (String r : REGIONS) tasks.add(pool.submit(new Score(r)));
for (ForkJoinTask<Long> task : tasks) task.join();
```

```text
invoke, one after another: 801 ms
submit all, then join:     201 ms
```

`invoke` hands over one task and waits for its result, so the loop never had more than one task
in the pool: that is the scoring job from the start of the unit. `submit` returns the task (a
`ForkJoinTask`, which is a `Future`) at once; all four ran together and the joins collected them.
If one task waits for another, the `invoke` loop hangs.

Called from a thread that is not a pool worker, `task.fork()` schedules the task in the common
pool, whatever pool you meant.

<!-- practice 4 -->

## A Failure Reaches the Caller Through invoke() or join(), and execute() Keeps It in the Task

Region `north-7` has a bad row. The same task, handed over each way:

```text
invoke:          throws IllegalStateException
submit, join():  throws IllegalStateException
submit, get():   throws ExecutionException, cause IllegalStateException
execute:         returned
                 the task holds IllegalStateException
```

`invoke` and `join()` rethrow the task's unchecked exception as its own type; `get()` wraps it, as
any `Future` does. `execute` returns nothing, so the exception stays inside the task object, and
unless you keep the task and join it, nobody learns that a region was never scored. (An
`execute(Runnable)` that throws goes to the worker's uncaught-exception handler, which prints a
stack trace and nothing more.)

<!-- practice 2 -->

## A Worker That Blocks Is Lost to the Pool, Unless It Blocks Through managedBlock()

A pool of four runs four tasks that each wait for a configuration, and then a fifth task that
loads it:

```java
for (int i = 0; i < 4; i++) {
    pool.execute(() -> {
        waiting.countDown();
        try {
            if (managed) awaitManaged(configLoaded);
            else configLoaded.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    });
}
waiting.await();
pool.execute(configLoaded::countDown);              // the loader
```

```text
plain await():   config loaded: false pool threads: 4
managedBlock:    config loaded: true  pool threads: 5
```

All four workers are parked in `await()`, the loader sits in the queue, and nothing will ever
release them. The pool can't see that a worker is waiting unless the task blocks through
`ForkJoinPool.managedBlock`:

```java
static void awaitManaged(CountDownLatch latch) throws InterruptedException {
    ForkJoinPool.managedBlock(new ForkJoinPool.ManagedBlocker() {
        @Override
        public boolean block() throws InterruptedException {
            latch.await();
            return true;
        }

        @Override
        public boolean isReleasable() {
            return latch.getCount() == 0;
        }
    });
}
```

`managedBlock` first asks `isReleasable()`, which must answer without blocking: if the wait is
already over, it never blocks. Otherwise it may start a spare worker before it calls `block()`,
which is why the second run had five threads with a parallelism of four.

Some JDK waits already do this for you, such as `Condition.await()`, the blocking queues in the
Reference and `CompletableFuture.join()`. `CountDownLatch`, `Semaphore`, `Thread.sleep`, `Object.wait` and a
socket read don't (see the Reference). A pool that waits on them works in testing and freezes on the day
as many tasks wait as there are workers.

<!-- practice 3 -->

## Summary

- **A pool you create keeps its threads until you close it.** A pool per request without `close()`
  leaves its workers alive long after its last task.
- **`invoke` waits for one task; `submit` lets them all run.** Submit everything before joining
  anything, or the pool runs one task at a time.
- **A failure reaches the caller through `invoke` or `join`.** After `execute`, it stays in the
  task unless you keep the task and join it.
- **A worker that blocks is lost to the pool unless it uses `managedBlock`.** Without it, waiters
  can take every worker and starve the task that would release them.

## Reference

**Handing a task to a pool:**

| Call | Returns | A failure reaches the caller |
|---|---|---|
| `pool.invoke(task)` | the result, after waiting | thrown from `invoke`, its own type |
| `pool.submit(task)` | the task, at once | from `join()` as its own type; from `get()` in an `ExecutionException` |
| `pool.execute(task)` | nothing | only if you keep the task and join it |

`task.fork()` called outside any pool schedules the task in the common pool.

**One worker of a parallelism-1 pool blocks in this call: does another task of the pool still
run?** (ran on Java 21 and 25)

| Wait | Pool adds a worker |
|---|---|
| `ForkJoinPool.managedBlock(...)` | yes |
| `Condition.await()`, `LinkedBlockingQueue`/`ArrayBlockingQueue`/`SynchronousQueue.take()` | yes |
| `CompletableFuture.join()`/`get()`, `Phaser.arriveAndAwaitAdvance()` | yes |
| `CountDownLatch.await()`, `Semaphore.acquire()`, `FutureTask.get()` | no |
| `Object.wait()`, `Thread.sleep()`, socket `InputStream.read()` | no |

**Pool facts (Java 21):** `new ForkJoinPool()` has a parallelism of one per available processor;
the common pool one fewer, settable with `-Djava.util.concurrent.ForkJoinPool.common.parallelism=N`. Workers
are daemon threads. `close()` (Java 19) waits for submitted tasks and shuts the pool down.

## Challenges

1. The analytics service needs a pool for its aggregations. One pool for the service's lifetime,
   or one per request with `close()`? Measure the cost of creating and closing a pool of four, and
   say what each choice does to a slow request's neighbours.
2. A task must wait on a `CountDownLatch` that a library owns, inside a pool you can't resize.
   Wrap it in a `ManagedBlocker`, then find out how many spare workers the pool adds when 50 tasks
   wait at once.
3. Your batch job uses `execute` for fire-and-forget tasks. Design how a failure gets reported
   without making the job wait for every task.

## Design Note: Why the Pool Waits to Be Told

A `ForkJoinPool` is sized for the cores: parallelism is the number of tasks it means to run at
once. If it started a thread whenever a worker looked idle, a task that spins or computes would be
mistaken for one that waits, and the pool would oversubscribe the CPU it was built to fill.
`managedBlock` moves that decision to the code that knows it is about to wait, and lets the pool
add a spare only for that time. The cost is that the knowledge is opt-in: a library that waits on
a latch inside your pool takes a worker with it, and nothing in the pool's API shows you which
waits it can see.

## Code Examples

- Source: [ForkJoinPoolBasics.java](src/main/java/com/github/msorkhpar/claudejavatutor/forkjoin/ForkJoinPoolBasics.java)
- Test: [ForkJoinPoolBasicsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/forkjoin/ForkJoinPoolBasicsTest.java)
