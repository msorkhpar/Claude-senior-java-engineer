# 5.1.2. Shared Memory and Thread-Local Memory

The audit log of the payments service says that `ana` ran last night's cleanup job, which no user
runs. On another day a report run is logged as `cy`, who was exporting a file at the time. The
current user sits in a `ThreadLocal`, the service runs on a thread pool, and a colleague's theory is
that each thread works on its own cached copy of memory and the fix is `volatile` everywhere.

The log and the theory both turn on which memory one thread can reach and another can't; each
section below states one rule about it.

## Fields, Statics and Array Elements Are Shared; Locals and Parameters Are Not

The JLS (§17.4.1) draws the line: "All instance fields, static fields, and array elements are
stored in heap memory", and "Local variables, formal method parameters, and exception handler
parameters are never shared between threads and are unaffected by the memory model."

A lambda can't change a local it captures:

```java
public class Capture {
    public static void main(String[] args) {
        int count = 0;
        Runnable task = () -> count++;
        task.run();
    }
}
```

```text
Capture.java:4: error: local variables referenced from a lambda expression must be final or effectively final
```

The usual workaround is a one-element array. Below, two threads run the same lambda and each
counts a million documents into `hits[0]`; then two threads each count into their own local `n`:

```java
int[] hits = {0};                           // a local, captured by both tasks
Runnable count = () -> {
    for (int doc = 0; doc < 1_000_000; doc++) { reindex(doc); hits[0]++; }
};
// ... run it on two threads and join them; then:
Callable<Integer> countLocally = () -> {
    int n = 0;                              // a local of each running thread
    for (int doc = 0; doc < 1_000_000; doc++) { reindex(doc); n++; }
    return n;
};
```

```text
availableProcessors: 20
run 1: shared int[] counted 1,921,125; two locals counted 2,000,000
run 2: shared int[] counted 1,998,290; two locals counted 2,000,000
run 3: shared int[] counted 1,998,589; two locals counted 2,000,000
run 4: shared int[] counted 1,997,855; two locals counted 2,000,000
run 5: shared int[] counted 2,000,000; two locals counted 2,000,000
```

A lambda copies each local it captures when the lambda is created. The compiler requires that the
local never changes, so the copy can't go stale. For `hits`, the value copied is a reference: the
local and the lambda's copy point to the same array on the heap, and both threads run that one
lambda. `hits` is a local; `hits[0]` is an array element, shared by both threads, and four runs of
five lost updates (why `++` loses them is unit 6.2.1). In `countLocally` each thread has its own
frame and its own `n`, so nothing is lost and nothing needs a lock.

A local that refers to an object is private, but the object is shared once a second thread can
reach it: through a field, a collection, an argument passed to another thread, or a task given to an
executor. A singleton
service that keeps per-request scratch data in a field, such as a `StringBuilder` reused to save
allocations, mixes requests the first time two of them overlap. Per-call state belongs in locals.

<!-- practice 1 -->

## Threads Don't Keep Private Copies of Memory; Compiled Code Keeps Values in Registers

The old picture gives each thread a "working memory" that copies values from "main memory" and
flushes them back at unknown times. It comes from the JLS before Java 5; the current memory model
(§17.4) is defined without hardware caches. On the x86 and ARM machines Java runs on, caches are
coherent: a coherence protocol makes every core that reads an address see the latest store that has
reached any cache.

Unit 6.2.2 shows a loop over a plain `stop` field that never ends once the JIT has compiled it.
Here is that loop again, beside one that reads the same plain field through a `VarHandle` in
*opaque* mode, which the javadoc defines as a read "accessed in program order, but with no assurance
of memory ordering effects with respect to other threads". The writer does the same plain write in
both cases:

```java
static class Worker {
    boolean stop;                                   // plain field, no volatile
    long spins;
    static final VarHandle STOP;
    static {
        try {
            STOP = MethodHandles.lookup().findVarHandle(Worker.class, "stop", boolean.class);
        } catch (ReflectiveOperationException e) { throw new ExceptionInInitializerError(e); }
    }
    void plainLoop()  { while (!stop) spins++; }
    void opaqueLoop() { while (!(boolean) STOP.getOpaque(this)) spins++; }
}
// start the loop on its own thread, wait 500 ms for the JIT, then:
w.stop = true;
t.join(1000);
```

The same program then has two threads pass a counter back and forth through two fields, each
written with `setOpaque` and read with `getOpaque`, a million times:

```text
availableProcessors: 20
plain read     still running 1 s after stop = true: true
opaque read    still running 1 s after stop = true: false (ended after 111 µs)
opaque ping-pong: 1,000,000 round trips, 108 ns each on average
```

All three runs gave the same verdicts, with 111 to 134 µs (from the write to `join` returning,
thread wake-up included) and 91 to 164 ns. Opaque mode orders nothing with respect to other
variables, but the compiler may no longer drop the read, and the loop saw the write within a
fraction of a millisecond. The ping-pong put a round trip through plain memory at about 100 ns. The
plain loop's hang came from the compiled code, not from a cache: unit 6.2.2 shows it disappear when
the JIT is turned off.

The effects the old picture blames on caches have three real sources: the JIT keeping a value in a
register or moving a read out of a loop (unit 6.2.2), the compiler and the CPU reordering
independent accesses, and each core's store buffer, which holds a write briefly before it reaches
the cache on its own, no fence needed, so the core can read another variable's old value after its
own write (unit 5.1.4). In correctly synchronized code, happens-before (unit 5.2.1) is what rules
these effects out.

`Thread.sleep`, `System.gc()` or a `println` to "flush the
cache" can make a test pass by changing what the JIT compiles, and they guarantee nothing.
`volatile` doesn't bypass the cache either; it forbids the reorderings and the register copy.
Opaque access, which ended the loop above, is no fix in general: it orders nothing, so it can't
make an object's other fields visible along with a flag.

## A ThreadLocal Value Belongs to the Thread, So a Pool Hands It to the Next Task

`ThreadLocal` is thread-local memory you ask for: each thread that calls `get()` sees the value it
set, and no other thread's. The javadoc states how long it lives: "Each thread holds an implicit
reference to its copy of a thread-local variable as long as the thread is alive and the
`ThreadLocal` instance is accessible". A `static final` one is always accessible, and on a pool a
thread outlives the task. Here are six tasks on one single-thread pool; the audit line reads the
user from a `ThreadLocal`:

```java
static final ThreadLocal<String> USER = new ThreadLocal<>();

pool.submit(() -> { USER.set("ana"); audit("refund #1041"); });          // sets, never removes
pool.submit(() -> audit("nightly cleanup"));                             // a system task, no user
pool.submit(() -> {
    USER.set("ben");
    try { audit("refund #1042"); } finally { USER.remove(); }
});
pool.submit(() -> audit("cache warm-up"));
pool.submit(() -> {
    USER.set("cy");
    export(77);                                                          // throws: disk full
    USER.remove();
});
pool.submit(() -> audit("report run"));
```

```text
  refund #1041 by ana
  nightly cleanup by ana
  refund #1042 by ben
  cache warm-up by null
  export #77 by cy
  report run by cy
```

That is the opening's audit log. The first task never removed `ana`, so the next task on that
thread ran as her. `export` audits, then throws, and the `remove()` after it never ran; `submit`
keeps the exception in the `Future`, so nothing was printed. With more threads in the pool the same
mistake would show up less often, depending on which thread picks up the next task. In an
authorization check, a leftover user is a security bug.

A leftover value also keeps memory. Eight tasks on a pool of eight each set a 16 MB scratch buffer
in a `ThreadLocal<byte[]>`, with and without `remove()` in `finally`:

```text
no remove()  heap held after the tasks ended, pool idle: 136 MB
remove()     heap held after the tasks ended, pool idle: 0 MB
```

The 128 MB of buffers (136 MB of used heap after `System.gc()`, an approximate measure) stay until
the pool's threads end, which for a server's pool means never. So: `set`, then `try`, then `remove()`
in `finally`. When calls can nest, the inner call saves the value it found and restores it in
`finally` instead of removing it, or it leaves the outer caller with no value.

> **Virtual threads.** With a thread per task a `ThreadLocal` can't leak into the next task, but it
> can't work as a per-thread cache either; unit 6.7.1 measures that and introduces `ScopedValue`.

<!-- practice 2 -->

## InheritableThreadLocal Copies the Value Once, When a Thread Is Created

`InheritableThreadLocal` looks like the way to carry the user into work done on other threads. The
javadoc says when it copies: "when a child thread is created, the child receives initial values for
all inheritable thread-local variables for which the parent has values." Three request threads, one
per tenant and one after another, each send a mail on a one-thread pool that nobody has used yet:

```java
static final InheritableThreadLocal<String> TENANT = new InheritableThreadLocal<>();
static final ExecutorService MAILER = Executors.newFixedThreadPool(1);   // threads start on first use

static void handle(String tenant) throws Exception {
    TENANT.set(tenant);
    MAILER.submit(() -> System.out.println("  mail for " + tenant + " sent as tenant " + TENANT.get())).get();
    TENANT.remove();
}
```

```text
  mail for acme sent as tenant acme
  mail for globex sent as tenant acme
  mail for initech sent as tenant acme
  parent set globex after start; child read acme
```

A `ThreadPoolExecutor` creates its threads as tasks arrive, inside `submit`, on the submitting
thread; until it reaches its core size it starts a new thread for each submit, even while others sit
idle (its javadoc). `acme`'s request created the mailer's only thread, which copied `acme` and keeps
it for every later task. The last line comes from a second part of the program, a plain parent and
child: the parent changed its value after starting the child, and the child still read the old one.
The copy is also shallow: by default `childValue` returns the parent's object itself, so a mutable
value is shared between the two threads.

To hand a value to a task, read it on the submitting thread when the task is submitted, and set it
in the task, with the same `finally` as before:

```java
String tenant = TENANT.get();                // on the submitting thread
MAILER.submit(() -> {
    TENANT.set(tenant);
    try { sendMail(); } finally { TENANT.remove(); }
});
```

For a long-lived thread that shouldn't keep whatever its creator had, a `Thread.Builder` with
`inheritInheritableThreadLocals(false)` builds threads that get no initial inheritable values.

<!-- practice 3 -->

## Threads That Share No Variable Can Still Share a Cache Line

Coherence works on cache lines, not on variables; Linux reports 64-byte lines for this machine's
CPU (`coherency_line_size`). A core must own a line
to write it, so two cores writing different variables in one line pass the line back and forth.
Two threads here each increment their own `volatile long`, 100 million times, in three layouts:

```java
import jdk.internal.vm.annotation.Contended;

public class Counters {
    static final class Adjacent  { volatile long a; volatile long b; }
    static final class Padded    { volatile long a; long p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15; volatile long b; }
    static final class Separated { @Contended volatile long a; @Contended volatile long b; }
    // ... two threads, one incrementing c.a and the other c.b, timed with System.nanoTime
}
```

Where HotSpot put the fields, read with `Unsafe.objectFieldOffset`, first with the default flags,
then with `-XX:-RestrictContended`:

```text
Adjacent             a at byte  16, b at byte  24
Padded               a at byte  16, b at byte 144
Separated            a at byte  16, b at byte  24
```

and with the flag:

```text
Adjacent             a at byte  16, b at byte  24
Padded               a at byte  16, b at byte 144
Separated            a at byte 144, b at byte 280
```

The timings, with `-XX:-RestrictContended`, median of five runs:

```text
availableProcessors: 20
one thread, one counter    median   495 ms  [472, 489, 495, 645, 647]
two threads, adjacent      median 2,224 ms  [1690, 2203, 2224, 2250, 2287]
two threads, 120 B padding median   531 ms  [486, 497, 531, 638, 713]
two threads, @Contended    median   665 ms  [450, 472, 665, 668, 684]
```

This is one machine, with other jobs running, timed with `System.nanoTime` and not JMH; a second run
gave medians of 471, 2,187, 740 and 758 ms. In both, two threads on adjacent fields took more than
four times as long as one thread doing half the total work, and separating the fields cut that to
between 1.1 and 1.6 times.

Hand padding depends on HotSpot keeping the declared field order; it did here, but nothing
guarantees it. It also left `a` at byte 16, in the line that holds the object's header. An earlier
version of this program called the counters through an interface, a call that can read the
object's class pointer from its header each time, and there the same 120-byte padding took medians of 1,671 and 1,757 ms
against 762 and 808 ms for `@Contended`, most likely because of that shared line.

`@Contended` asks the JVM itself to isolate a field: the offsets show 128 bytes (the default width)
before `a` and between the fields. It is a JDK-internal annotation: compiling against it takes
`--add-exports java.base/jdk.internal.vm.annotation=ALL-UNNAMED`, and outside the JDK it is ignored
unless the JVM runs with `-XX:-RestrictContended`. The JDK uses it in `LongAdder`'s cells and
`ConcurrentHashMap`'s counter cells.

A line that is only read can sit in every core's cache at once; a write takes it from all of them.
In application code the usual victims are per-thread slots in one `long[]` or `AtomicLongArray` (the
atomic classes are "lock-free", in the words of their package javadoc, so the slots share no lock,
only lines), or small counter objects allocated one after another. Measure before padding: every
padded field costs memory in every instance.

<!-- practice 4 -->

## Summary

- **Fields, statics and array elements are shared; locals and parameters are not.** A captured
  local is a copy, and a copied reference still points to a shared object.
- **Threads don't keep private copies of memory.** Caches are coherent; the stale reads come from
  the JIT, reordering and store buffers, and happens-before is what rules them out.
- **A `ThreadLocal` value lives as long as its thread.** On a pool, `remove()` it in `finally`, or
  the next task inherits the value and the memory stays held.
- **`InheritableThreadLocal` copies once, at thread creation.** A pool's threads keep whatever their
  creator had; capture the value at submit time instead.
- **Separate variables in one cache line still contend.** Padding or `@Contended` separates them, at
  a cost in memory; measure first.

## Reference

**What is shared (JLS §17.4.1):**

| Storage | Shared between threads |
|---|---|
| instance fields, static fields, array elements | yes |
| local variables, method parameters, catch parameters | no |
| a local captured by a lambda or an inner class | no: the lambda gets a copy of the value |
| the object a local refers to | yes, once a second thread has the reference |

**`ThreadLocal` and `InheritableThreadLocal` (javadoc, Java 21):**

| Call | Effect |
|---|---|
| `ThreadLocal.withInitial(s)` | each thread's first `get()` without a value calls `s` |
| `set(v)` / `get()` | the current thread's value only |
| `remove()` | removes the current thread's value; the next `get()` starts over |
| `InheritableThreadLocal.childValue(p)` | the child's initial value, computed in the parent when the child is created; returns `p` itself by default |
| `Thread.Builder.inheritInheritableThreadLocals(false)` | the new thread inherits no values |

**`@Contended` (HotSpot, Java 21):**

| Setting | Effect |
|---|---|
| `javac --add-exports java.base/jdk.internal.vm.annotation=ALL-UNNAMED` | lets application code compile against the annotation |
| default (`-XX:+RestrictContended`) | the annotation is ignored outside the JDK's own classes |
| `-XX:-RestrictContended` | the annotation isolates the field; 128 bytes of padding by default (`-XX:ContendedPaddingWidth`) |

## Challenges

1. Your service propagates a trace id with `InheritableThreadLocal` and uses `CompletableFuture`
   without an executor argument. Which threads do the futures run on, when were they created, and
   what trace id do they log?
2. A metrics library keeps one `long` per thread in a `long[64]` indexed by a thread number. How would
   you find out whether false sharing costs anything in your service, and what would you change?
3. A framework sets the current user in a `ThreadLocal` in a servlet filter and removes it in
   `finally`. Which code can still see the wrong user, and how would a test catch it?

## Design Note: Why a Per-Thread Variable at All

`ThreadLocal` exists for values that would otherwise be passed to every method of a call chain:
the current user, a transaction, a non-thread-safe formatter. It turns a parameter into an implicit
one, so the signatures stay short and the value reaches code that doesn't declare the parameter.

The cost is that the value's lifetime is the thread's, not the request's, and nothing in the types
says when it was set or who must clear it. On a pool, where threads outlive requests, every user of
a `ThreadLocal` must clear it, and `InheritableThreadLocal`'s copy-at-creation rule only matches a
design where a thread is created for each piece of work. `ScopedValue` (unit 6.7.1) ties the value
to the code instead: it is bound for one block and unbound when the block ends.

## Code Examples

- Source: [SharedMemory.java](src/main/java/com/github/msorkhpar/claudejavatutor/javamemorymodel/SharedMemory.java)
- Test: [SharedMemoryTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/javamemorymodel/SharedMemoryTest.java)
