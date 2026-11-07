# 6.1.1. Creating Threads (Runnable Interface and Thread Class)

A command-line tool exports the day's orders to a file. To keep its progress bar moving, someone
moved the export onto a thread of its own: `new Thread(exporter).start()`. Since then the tool
sometimes never exits on the batch server, and a thread dump of a stuck run shows `"Thread-3"` in
`TIMED_WAITING`, which could be any of its four background threads. Someone made the thread a
daemon so the tool would exit, and now, on some nights, the export file is empty. Every output
below is from Java 21.

## A Thread Runs a Task: Write the Task as a Runnable, Not as a Thread

A `Thread` object is the handle for one thread of execution. The work it does is a `Runnable`, an
interface with one method, `void run()`, so a lambda can go straight to the constructor:

```java
Thread t = new Thread(() -> System.out.println("running on " + Thread.currentThread().getName()));
```

The other way is to extend `Thread` and override `run()`. The two differ once the task runs on
another thread.

### Spot the Problem: A Thread Subclass Handed to a Pool

Another team's nightly export was written years ago as a thread of its own
(`Thread.onSpinWait()` stands in for one batch of work):

```java
import java.util.concurrent.atomic.AtomicInteger;

class ExportJob extends Thread {
    final AtomicInteger batches = new AtomicInteger();

    ExportJob() {
        super("order-export");
    }

    @Override
    public void run() {
        System.out.println("  export running, getName() = " + getName()
                + ", currentThread().getName() = " + Thread.currentThread().getName());
        while (!isInterrupted()) {                     // "stop when asked"
            batches.incrementAndGet();                 // export one batch
            Thread.onSpinWait();
        }
        System.out.println("  export stopped");
    }
}
```

Someone moves it onto the service's pool. `Thread` implements `Runnable`, so this compiles.
`shutdownNow()` stops the pool by interrupting its threads (unit 6.1.3):

```java
ExportJob job = new ExportJob();
ExecutorService pool = Executors.newFixedThreadPool(2);
pool.execute(job);                                 // a Thread is a Runnable, so this compiles
while (job.batches.get() == 0) Thread.onSpinWait();
pool.shutdownNow();
boolean ended = pool.awaitTermination(1, TimeUnit.SECONDS);
System.out.println("pool ended within 1 s: " + ended + ", job.getState() = " + job.getState());
```

Which thread does the log line name, and does `shutdownNow()` stop the export?

```text
  export running, getName() = order-export, currentThread().getName() = pool-1-thread-1
pool ended within 1 s: false, job.getState() = NEW
```

The pool's thread calls `job.run()` as an ordinary method, so the code runs on
`pool-1-thread-1`. Every method `ExportJob` inherited from `Thread`, though, describes the
`ExportJob` object, which was never started and is still `NEW`. `getName()` names a thread that
doesn't exist, and `isInterrupted()` reads a flag nobody sets: `shutdownNow()` interrupted
`pool-1-thread-1`. The loop never ends, and since a pool's threads are not daemons, neither does
the process: run without its final `System.exit`, it was still busy when killed after 5 s.

The same loop written as a task asks `Thread.currentThread()`, the thread running the code:

```java
import java.util.concurrent.atomic.AtomicInteger;

class ExportTask implements Runnable {
    final AtomicInteger batches = new AtomicInteger();

    @Override
    public void run() {
        Thread me = Thread.currentThread();
        System.out.println("  export running on " + me.getName());
        while (!me.isInterrupted()) {
            batches.incrementAndGet();
            Thread.onSpinWait();
        }
        System.out.println("  export stopped");
    }
}
```

The same pool code with `ExportTask`, then the task on a named thread and on a virtual thread (the
last section), each stopped with an interrupt:

```java
Thread own = new Thread(new ExportTask(), "order-export");
own.start();
Thread virtual = Thread.ofVirtual().name("order-export-v").start(new ExportTask());
Thread.sleep(100);
own.interrupt();
virtual.interrupt();
```

```text
  export running on pool-1-thread-1
  export stopped
pool ended within 1 s: true
  export running on order-export
  export running on order-export-v
  export stopped
  export stopped
```

The task runs unchanged wherever it is put, and code on its own thread today can be handed to a
pool or a virtual thread later. Extend `Thread` only to change how the thread itself behaves, which
application code almost never needs. Inside a task, ask `Thread.currentThread()`, never `this`.

<!-- practice 1 -->

## A Runnable Has No Way Back: Return a Result or a Failure Through a Callable

`run()` returns `void` and declares no checked exceptions. Here a loader thread reads the orders
into a list, and `readOrders()` fails:

```java
List<String> orders = new ArrayList<>();
Thread loader = new Thread(() -> {
    try {
        orders.addAll(readOrders());
    } catch (IOException e) {
        throw new UncheckedIOException(e);       // run() can't throw IOException
    }
});
loader.start();
loader.join();
System.out.println("main: loaded " + orders.size() + " orders, carrying on");
```

```text
Exception in thread "Thread-0" java.io.UncheckedIOException: java.io.IOException: orders.csv: permission denied
	at Examples.lambda$lost$1(Examples.java:91)
	at java.base/java.lang.Thread.run(Thread.java:1583)
Caused by: java.io.IOException: orders.csv: permission denied
	at Examples.readOrders(Examples.java:82)
	at Examples.lambda$lost$1(Examples.java:89)
	... 1 more
main: loaded 0 orders, carrying on
```

The exception ended the loader thread. Nothing connects a thread's failure to the thread that
started it: the JVM hands it to the failing thread's uncaught-exception handler, whose default
prints it to standard error (unit 6.1.2 sets your own). `join()` returned normally, because it
waits for the thread to end, not to succeed, and `main` went on with an empty list: in the
opening's tool, an empty export reported as a success.

`Callable<V>` (`V call() throws Exception`) returns a value and
may throw any exception. A `Thread` runs only a `Runnable`, so wrap the `Callable` in a
`FutureTask`, a `Runnable` for the thread and a `Future` the caller reads the outcome from:

```java
Callable<List<String>> load = () -> readOrders();  // call() may throw IOException
FutureTask<List<String>> result = new FutureTask<>(load);
new Thread(result, "order-loader").start();       // FutureTask is a Runnable
try {
    List<String> orders = result.get();
    System.out.println("main: loaded " + orders.size() + " orders");
} catch (ExecutionException e) {
    System.out.println("main: load failed: " + e.getCause());
}
```

```text
main: load failed: java.io.IOException: orders.csv: permission denied
```

`get()` waits for `call()` to finish, then returns its value or throws `ExecutionException` with
the task's exception as its cause. Handle `e.getCause()`, the task's own failure, not the wrapper.
In services the thread usually comes from an `ExecutorService`, whose `submit` wraps the task the
same way (units 6.4.1 and 6.4.2).

<!-- practice 2 -->

## Name Every Thread Where You Create It

Four threads printed `Thread.currentThread()`: from `new Thread(task)`, an
`Executors.newFixedThreadPool(2)` pool, `new Thread(task, "invoice-mailer")` and
`Thread.ofVirtual()`:

```text
Thread[#28,Thread-0,5,main]
Thread[#29,pool-1-thread-1,5,main]
Thread[#32,invoice-mailer,5,main]
VirtualThread[#30]/runnable@ForkJoinPool-1-worker-1
```

Only `invoice-mailer` says what it does. `Thread-0` and `pool-1-thread-1` are generated, and a
virtual thread has no name: `getName()` returns the empty string. In a thread dump the name is the
only thing that ties a stack to a component.

So the code that creates a thread, and knows what it is for, names it. The constructor takes a
name, and `Thread.Builder` counts for you. A builder also gives a `ThreadFactory`, the interface an
`ExecutorService` calls to create each of its threads, so a named factory names a pool's threads:

```java
Thread.Builder.OfPlatform mailers = Thread.ofPlatform().name("invoice-mailer-", 1);
Thread a = mailers.unstarted(() -> {});
Thread b = mailers.unstarted(() -> {});
ThreadFactory f1 = mailers.factory();
ThreadFactory f2 = mailers.factory();
System.out.println(a.getName() + ", " + b.getName()
        + "; factory 1: " + f1.newThread(() -> {}).getName() + ", " + f1.newThread(() -> {}).getName()
        + "; factory 2: " + f2.newThread(() -> {}).getName());
Thread v = Thread.ofVirtual().name("pdf-render-", 1).unstarted(() -> {});
System.out.println("virtual: " + v.getName() + ", unnamed virtual: \"" + Thread.ofVirtual().unstarted(() -> {}).getName() + "\"");
```

```text
invoice-mailer-1, invoice-mailer-2; factory 1: invoice-mailer-3, invoice-mailer-4; factory 2: invoice-mailer-3
virtual: pdf-render-1, unnamed virtual: ""
```

`name(prefix, start)` appends a counter that the builder increments for each thread. A factory
taken from the builder starts from a copy of the counter's current value, which is why both
factories began at 3 and two threads are now called `invoice-mailer-3`. Take one factory per pool,
each with its own prefix.

## A Daemon Thread Doesn't Hold the JVM Open, and Exit Drops It Mid-Step

An exporter thread writes five orders, 100 ms apart, through a buffered writer, and `main` returns
after 250 ms. `log` prints milliseconds since the start.

```java
Thread exporter = new Thread(() -> {
    try (Writer out = Files.newBufferedWriter(file)) {
        for (int order = 1; order <= 5; order++) {
            out.write("order " + order + "\n");
            Thread.sleep(100);                   // fetch the next order
        }
        out.write("END\n");
        log("exporter: wrote all 5 orders");
    } catch (IOException | InterruptedException e) {
        log("exporter: " + e);
    } finally {
        log("exporter: finally");
    }
}, "order-export");
exporter.setDaemon(daemon);
exporter.start();
Thread.sleep(250);
log("main: returning");
```

Run once with `daemon` false:

```text
  253 ms  main: returning
  511 ms  exporter: wrote all 5 orders
  513 ms  exporter: finally
```

The process ended after 539 ms, and the file held all five orders and `END`: 44 bytes. Then with
`daemon` true:

```text
  253 ms  main: returning
```

The process ended after 283 ms, and the file was empty: 0 bytes.

The JVM begins to shut down when the last started non-daemon thread ends. In the first run the
exporter was one, so the JVM waited for it; the opening's tool hangs because one of its non-daemon
threads never ends.

A daemon thread doesn't count. In the second run the JVM shut down as `main` returned, with the
exporter in the middle of a `sleep`. The exporter wasn't interrupted and got no exception. Its
`finally` never ran, so the writer was never closed, and the three orders in its buffer never
reached the file. No error appeared anywhere.

`setDaemon(true)` lets the JVM abandon the thread's work between any two instructions, which suits
a cache refresher. Work that must finish, such as a file or a commit, runs on a non-daemon thread
whose owner waits for it (units 6.1.2 and 6.1.3).

Daemon status can't change while the thread is alive: `setDaemon` on a running thread throws.

```text
setDaemon after start(): java.lang.IllegalThreadStateException
```

A thread you don't mark is not necessarily non-daemon: a platform thread inherits the daemon status
of the thread that creates it. These four were created on a daemon thread called `housekeeping`:

```java
Thread viaConstructor = new Thread(() -> {});
Thread viaBuilder = Thread.ofPlatform().unstarted(() -> {});
Thread explicit = Thread.ofPlatform().daemon(false).unstarted(() -> {});
ExecutorService pool = Executors.newSingleThreadExecutor();
Future<Boolean> poolDaemon = pool.submit(() -> Thread.currentThread().isDaemon());
```

```text
created on a daemon thread: new Thread -> daemon=true, ofPlatform() -> daemon=true, daemon(false) -> daemon=false, Executors pool thread -> daemon=false
```

The two that didn't say became daemons, like their creator. The pool's thread didn't, because
`Executors.defaultThreadFactory()` sets non-daemon explicitly. Code that creates threads for work
that must finish says `daemon(false)` or `setDaemon(false)`, so the answer doesn't depend on which
thread calls it.

<!-- practice 3 -->

## A Virtual Thread Is Cheap to Create and to Block, Not Faster at Computing

A platform thread, the kind every earlier example created, wraps an operating-system thread with
its own native stack. A virtual thread is scheduled by the JVM onto a small pool of platform
threads, its carriers, and in most cases releases its carrier while it is blocked.

This program starts one thread per task and waits for all of them: first tasks that each wait 1 s,
as a call to a slow remote service would, then tasks that each compute about 50 ms of arithmetic.

```java
static long run(Thread.Builder builder, int tasks, Runnable task) throws InterruptedException {
    long start = System.nanoTime();
    List<Thread> threads = new ArrayList<>(tasks);
    for (int i = 0; i < tasks; i++) threads.add(builder.start(task));
    for (Thread t : threads) t.join();
    return (System.nanoTime() - start) / 1_000_000;
}
```

```text
100,000 waiting tasks, platform threads: 15,464 ms
100,000 waiting tasks, virtual threads: 1,349 ms
80 computing tasks on 20 cores, platform threads: 219 ms
80 computing tasks on 20 cores, virtual threads:  220 ms
```

The waiting tasks wait the same second either way, so on platform threads the extra 14 s went into handling 100,000 operating-system threads. The computing tasks took the same time
either way: 20 logical CPUs do 20 tasks' arithmetic at a time, whatever kind of thread asks.

One virtual thread per blocking task (HTTP calls, database queries) replaces a pool sized to
ration platform threads; work that keeps a core busy gains nothing. Unit 6.7.2 covers what a
virtual thread still costs.

A virtual thread is also always a daemon:

```java
Thread report = Thread.ofVirtual().unstarted(() -> {});
try {
    report.setDaemon(false);
} catch (IllegalArgumentException e) {
    System.out.println("setDaemon(false): " + e.getMessage());
}
System.out.println("isDaemon(): " + report.isDaemon());
```

```text
setDaemon(false): 'false' not legal for virtual threads
isDaemon(): true
```

So virtual threads started from `main` hold nothing open: at exit they are dropped mid-step, like
the daemon exporter. Code that starts virtual threads for work that must finish waits for them
before it returns.

<!-- practice 4 -->

## Summary

- **A thread runs a task; write the task as a `Runnable`.** A `Thread` subclass run by a pool
  describes an unstarted object: wrong name, and shutdown can't stop it.
- **A `Runnable` has no way back.** Its failure goes to standard error and `join()` returns
  normally; a `FutureTask`'s `get()` returns a `Callable`'s result or throws its failure as the
  cause of an `ExecutionException`.
- **Name every thread where you create it**; generated names make a thread dump unreadable.
- **A daemon thread doesn't hold the JVM open, and exit drops it mid-step**, with no `finally`.
  Daemon status is fixed at `start()` and, unless set, copied from the creating thread.
- **A virtual thread is cheap to create and to block, not faster at computing**, and is always a
  daemon.

## Reference

**Edge cases (Java 21):**

| Situation | What happens |
|---|---|
| `setDaemon(...)` on a started, live thread | `IllegalThreadStateException` |
| `setDaemon(false)` on a virtual thread | `IllegalArgumentException` |
| an unstarted non-daemon thread when `main` returns | doesn't keep the JVM alive; only started ones do |

## Challenges

1. A library creates its background threads with `new Thread(task)`, and your service creates
   the library's clients from a daemon housekeeping thread. Which of the library's threads are
   daemons, and how would you check in a running service without its source?
2. The opening's export must finish its file even when the operator presses Ctrl-C. Which thread
   owns the file, who waits for it, and what does the operator see if the export takes three
   minutes?
3. Your service makes 2,000 concurrent calls to a slow partner API, each waiting about 300 ms.
   Compare a fixed pool of 200 platform threads with one virtual thread per call: measure the time
   for a burst, and decide what keeps the partner from receiving all 2,000 calls at once.

## Design Note: Why Thread Implements Runnable

In Java 1.0, `Thread` is both the handle for a thread and, since it implements `Runnable`, a
task, which kept the first programs short and left this unit's first trap. Later libraries
separate the two: executors (Java 5) take tasks and make threads in a `ThreadFactory`, and
`Thread.Builder` (Java 21) describes a thread apart from its task. The original design can't
change without breaking every `Thread` subclass, so the code that creates threads keeps them
apart.

## Code Examples

- Source: [ThreadCreation.java](src/main/java/com/github/msorkhpar/claudejavatutor/threadbasics/ThreadCreation.java)
- Test: [ThreadCreationTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/threadbasics/ThreadCreationTest.java)
