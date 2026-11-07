# 5.3.3. Visibility Issues and Proper Synchronization Techniques

At 14:02 the on-call engineer flipped the payment service's kill switch for EU cards. Within a
second, nineteen of its twenty workers stopped charging EU cards. The twentieth went on charging
them until someone restarted it at 14:40. In staging, where debug logging is on, the switch works
every time. When an engineer attached a debugger to a canary and set a breakpoint in the loop, the
stuck worker stopped at once.
The ticket was closed as "can't reproduce".

The switch started as a plain field and went through two fixes that look right before a third held.
Every output comes from Java 21.0.12 (HotSpot, x86_64, 20 CPUs), one JVM per run.

## A Visibility Bug Disappears Under -Xint, C1, a Breakpoint or a Log Line: That Is the Diagnosis, Not a Fix

The charge loop read the switch from a plain static field:

```java
static final boolean DEBUG = Boolean.getBoolean("debug");
static final PrintStream log = new PrintStream(OutputStream.nullOutputStream());
static boolean paused;                         // plain field, the kill switch
static long charges;

static void chargeLoop() {
    while (!paused) {
        charges++;                             // stands in for charging one card
        if (DEBUG) log.println("charged " + charges);
    }
}
```

`main` runs the loop on a worker thread, sets `paused = true` 500 ms later and gives the worker a
second to end. We ran the same class six ways, three times each:

```text
no options                     3 of 3: still charging 1 s after paused = true (RUNNABLE in chargeLoop)
-Ddebug=true (log line on)     3 of 3: stopped
-Xint                          3 of 3: stopped
-XX:TieredStopAtLevel=1        3 of 3: stopped
-XX:CompileCommand=exclude,... 3 of 3: stopped
debug agent, no breakpoint     3 of 3: still charging 1 s after paused = true (RUNNABLE in chargeLoop)
```

Then we attached `jdb` to the JVM three seconds after it started, with the worker stuck, and set a
breakpoint on the `charges++` line:

```text
still charging 1 s after paused = true (RUNNABLE in chargeLoop)
stopped 2 s after paused = true
```

The breakpoint was never hit. With the breakpoint on a line of `main` instead, the worker was still
charging when the program gave up waiting, 31 s after the switch, in three runs of three.

None of the switches that stopped the worker changes what the memory model allows, and no cache
hides the write: the hardware keeps caches coherent (unit 5.1.2). Each switch changes which code
runs. `-Xint` keeps every method in the interpreter, which reads the field on every turn.
`-XX:TieredStopAtLevel=1` keeps C2, the optimizing compiler, out, and C1's code for this loop kept
reading. `CompileCommand=exclude` keeps this one method interpreted. Setting a breakpoint in
`chargeLoop` most likely made HotSpot discard the method's compiled code, and the interpreter then
read `paused` once and left the loop before reaching the breakpoint. The log line puts a call and a lock acquire
into every turn of the loop, which kept the JIT from hoisting the read; `main` never takes that lock,
so it is no edge either (unit 5.2.2). The debug agent alone didn't change this loop. That leaves
C2's code, which most likely read `paused` once, before the loop: the memory model allows it because
no happens-before edge leads from the write to the read (unit 6.2.2).

Two signs together point to a read the JIT was allowed to hoist: a thread dump shows the thread
`RUNNABLE` in a loop, holding no lock and waiting for nothing, after the condition it waits on has
changed; and the hang goes away under `-Xint` or with that one method excluded from compilation.
Narrow it down with `CompileCommand=exclude` on one method at a time, then fix it with an edge:
`volatile`, a lock both sides take, or an atomic class. A passing run under any of those switches
proves nothing about the production JVM.

In production `DEBUG` is a `static final` `false`, so the JIT removes the `if`, and staging tested
a different loop from the one in production.

## volatile Covers Reads of the Field, Not the Object It Points To

The first fix put the switch in a settings object and made the reference `volatile`:

```java
final class Settings {                         // mutable: a plain field with a setter
    private boolean paused;
    boolean isPaused() { return paused; }
    void setPaused(boolean p) { paused = p; }
}

// in the workers' class: static volatile Settings settings = new Settings();
```

The admin endpoint calls `settings.setPaused(true)`. Two workers wait for it: one keeps the object
it read (`Settings s = settings; while (!s.isPaused()) charges++;`), the other reads the field on
every turn (`while (!settings.isPaused()) charges++;`). A third run uses the rereading worker and
replaces the object instead of changing it. Three runs of each gave the same:

```text
held   set     still charging 1 s later
reread set     stopped
reread replace stopped
```

`settings.setPaused(true)` is a volatile *read* of `settings` followed by a plain write to `paused`.
What publishes is a volatile *write*, and this code has none. The worker that holds the object reads
only a plain field, so it is the loop from the last section and hangs the same way.

The worker that rereads stopped, but nothing guarantees it. Its volatile read on every turn kept
HotSpot from moving the plain read of `paused` out of the loop. That is how this compiler treats a
volatile read, not an edge: no write happens-before the read of `paused`, so the JLS allows it to
return `false` forever. That is allowed, and was not observed here.

Holding the object is the common case. Any method that takes `Settings` as a parameter holds it, and
so does a local copy taken to read the field once per decision, as unit 6.2.2 advises. So a
published object is never changed again; a change is a new object. Assigning a new `Settings` is a
volatile write, and a reader that sees the new reference sees every write made to the object before
the assignment. A worker that holds the old object still has to read the field again to get the new
one; the last section is about when.

<!-- practice 1 -->

## A volatile Array Gives Its Elements No Edge: Use AtomicIntegerArray or a VarHandle

The second fix gave each of the eight card regions its own switch, in an array. Five versions, each
worker waiting for element 3:

```java
static volatile boolean[] volatileArray = new boolean[8];
static final boolean[] finalArray = new boolean[8];
static final AtomicIntegerArray atomicArray = new AtomicIntegerArray(8);       // 0 = running, 1 = paused
static final boolean[] handleArray = new boolean[8];
static final VarHandle ELEMENT = MethodHandles.arrayElementVarHandle(boolean[].class);

static void chargeShard(boolean[] paused, int shard) {
    while (!paused[shard]) charges++;
}

// the workers' loops
while (!finalArray[SHARD]) charges++;
while (!volatileArray[SHARD]) charges++;
chargeShard(volatileArray, SHARD);
while (atomicArray.get(SHARD) == 0) charges++;
while (!(boolean) ELEMENT.getAcquire(handleArray, SHARD)) charges++;
```

The admin thread writes the element with the matching call: `finalArray[SHARD] = true`,
`volatileArray[SHARD] = true`, `atomicArray.set(SHARD, 1)`, `ELEMENT.setRelease(handleArray, SHARD,
true)`. Three runs of each gave the same:

```text
final boolean[]           still charging 1 s later
volatile boolean[]        stopped
volatile, as a parameter  still charging 1 s later
AtomicIntegerArray        stopped
VarHandle getAcquire      stopped
```

`volatile` belongs to the field that holds the array reference. `volatileArray[3] = true` reads that
field and then writes element 3 with a plain write, exactly like `setPaused` above. The worker that
indexes the field directly stopped for the same reason as the rereading worker, and with the same
lack of a guarantee. Passed to a method as a parameter, the array is a plain reference, and the
worker hangs as with the `final` array.

There is no `AtomicBooleanArray` in the JDK, so switches go into an `AtomicIntegerArray` as 0 and 1. The package javadoc calls the atomic array
classes "notable in providing volatile access semantics for their array elements". A `VarHandle`
from `MethodHandles.arrayElementVarHandle` keeps the plain `boolean[]` and gives each access a mode:
`setVolatile`/`getVolatile`, or `setRelease`/`getAcquire`, which is enough for a switch that one
thread sets and others read (unit 5.1.4 compares the modes). Every access must go through the
handle; a plain `paused[3] = true` elsewhere is a plain write again. A third option for arrays is to
copy the array, change the copy and assign it to the volatile field, the shape of the next section.

## Settings Are One Immutable Snapshot, Replaced Whole and Read Once per Unit of Work

The version that held keeps all settings in one immutable record and replaces it on every change:

```java
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

record Settings(Set<String> pausedRegions, Map<String, Long> limitCents) {
    Settings {
        pausedRegions = Set.copyOf(pausedRegions);           // nobody can change it after this
        limitCents = Map.copyOf(limitCents);
    }

    Settings pause(String region) {                          // a new snapshot, this one untouched
        Set<String> paused = new HashSet<>(pausedRegions);
        paused.add(region);
        return new Settings(paused, limitCents);
    }

    boolean allows(Charge c) {
        return !pausedRegions.contains(c.region()) && c.cents() <= limitCents.getOrDefault(c.region(), 0L);
    }
}

final class SettingsStore {
    private volatile Settings current = new Settings(Set.of(), Map.of("eu", 50_000L, "us", 80_000L));

    Settings current() {
        return current;
    }

    synchronized void pause(String region) {                 // writers one at a time
        current = current.pause(region);                     // one volatile write publishes it
    }
}

record Charge(String region, long cents) {}
```

Nobody can change a published `Settings`, so there is no in-place write to miss: `copyOf` makes copies that the caller's collections can't reach (unit 5.2.3
shows why an unmodifiable view isn't enough). The one volatile write publishes the pause together
with the limits, so a reader never sees half of a change (unit 6.2.2). Readers take no lock. Writers
synchronize, because without it two admins pausing two regions at the same time could both build on the
same old snapshot, and the second write would drop the first pause. `AtomicReference.updateAndGet`
does the same without a lock, retrying the function if another write got in first (unit 5.3.2).

A snapshot reaches a reader only when the reader reads the field again. Two workers charge a batch
of six EU charges, one reading the settings once per batch and one once per charge:

```java
static void chargeOncePerBatch(SettingsStore store, List<Charge> batch, Consumer<Charge> send) {
    Settings s = store.current();
    for (Charge c : batch) {
        if (s.allows(c)) send.accept(c);
    }
}

static void chargeOncePerCharge(SettingsStore store, List<Charge> batch, Consumer<Charge> send) {
    for (Charge c : batch) {
        Settings s = store.current();
        if (s.allows(c)) send.accept(c);
    }
}
```

`send` holds the worker on a latch during the second charge while the admin pauses EU:

```text
settings read once per batch : 2 charges sent before the pause, 4 after it
settings read once per charge: 2 charges sent before the pause, 0 after it
```

Both workers are free of data races. How often a worker reads the field sets how long a change takes
to reach it: the next charge, the rest of the batch, or never, if the worker reads its settings in
its constructor. Read once per unit of work, a charge or a request, and use that one snapshot for
every check inside it, so the pause check and the limit check agree.

The price is a full copy on every change. For settings changed a few times a day the copy is
negligible. For a large table changed thousands of times a second, a `ConcurrentHashMap` (unit
6.5.2) fits better, and readers give up seeing all keys from one version.

<!-- practice 2 -->

<!-- practice 3 -->

## Summary

- **A visibility bug disappears under `-Xint`, C1 only, a breakpoint in the method or a log line.**
  Each changes which code runs, not what the memory model allows; use the pattern to find the read,
  then fix it with an edge. A run under any of them proves nothing about the production JVM.
- **`volatile` covers reads of the field, not the object behind it.** A setter on a published
  object is a plain write; a reader that holds the object can miss it forever. Replace the object
  instead of changing it.
- **A volatile array gives its elements no edge.** Element writes are plain, and an array passed as
  a parameter is plain all the way; use `AtomicIntegerArray` (there is no `AtomicBooleanArray`), a
  `VarHandle` on every access, or a replaced copy.
- **Settings are one immutable snapshot, replaced whole and read once per unit of work.** Readers
  take no lock and never see half a change; writers serialize; a worker sees a change only at its
  next read, so read per charge or per request, never once per worker.

## Reference

**What a reader is promised after a write (Java 21):**

| The writer does | The reader does | Reader guaranteed to see it |
|---|---|---|
| `holder.obj.x = 1`, `holder` volatile | `holder.obj.x`, or `x` of a reference it holds | no: the write to `x` is plain |
| `holder.obj = newObj`, `holder` volatile | reads `holder.obj` again | yes, with every write made to `newObj` before the assignment |
| `arr[i] = 1`, field `arr` volatile | `arr[i]`, or `a[i]` on a held reference | no: the element write is plain |
| `atomicArray.set(i, 1)` | `atomicArray.get(i)` | yes: volatile semantics per element |
| `H.setRelease(arr, i, true)`, `H` from `MethodHandles.arrayElementVarHandle` | `H.getAcquire(arr, i)` that returns `true` | yes, with everything written before the `setRelease` |
| assigns a new copy of the array to volatile `arr` | reads `arr` again | yes |

**Diagnostic switches (HotSpot; they change which code runs):**

| Switch | Effect |
|---|---|
| `-Xint` | no JIT; every method interpreted |
| `-XX:TieredStopAtLevel=1` | only the C1 compiler; no C2 |
| `-XX:CompileCommand=exclude,pkg.Class::method` | that method is never compiled |
| a debugger breakpoint in the method | here, the hung loop ended without reaching the breakpoint; most likely the method was deoptimized |
| `-agentlib:jdwp=...` with no breakpoint | no change in this unit's run |

## Challenges

1. A routing table of 200,000 entries changes 50 times a second and is read on every request.
   Measure the copy-on-write snapshot against a `ConcurrentHashMap` for both the readers and the
   writer, and decide what consistency the readers lose with the map.
2. Your service reads its settings once per request, but some requests stream for ten minutes.
   Where do you read them again, and what must stay the same for the length of one stream?
3. A library's `getConfig()` returns an object that the library changes in place on every reload.
   You can't change the library. How do you give your readers a consistent snapshot?
4. Write a test that would have caught the opening bug before production. What can it check
   deterministically, and what can no test of it prove?

## Design Note: Why volatile Belongs to a Field, Not to an Object

Java has no volatile object and no volatile element type. `volatile` marks one field, and the
ordering it gives belongs to the accesses of that field: in terms of what a compiler may do, no
earlier read or write can be moved below a volatile write, and no later one above a volatile read. A "deep" volatile would have to give that
treatment to every field of every object reachable from the reference, depending on how the object
was reached, on every access, and the compiler could keep almost nothing in a register.

What the language gives instead is one ordering point per hand-off, and it publishes everything
written before it to a reader that reads the same field and sees the value. That makes immutability
the lock-free way to get a whole object graph across threads:
build it, write the reference once, and never write to the graph again. The cost is a copy for every
change, and a reader sees the change only when it reads the field again.

## Code Examples

- Source: [VisibilityIssue.java](src/main/java/com/github/msorkhpar/claudejavatutor/concurrencypitfalls/VisibilityIssue.java)
- Test: [VisibilityIssueTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/concurrencypitfalls/VisibilityIssueTest.java)
