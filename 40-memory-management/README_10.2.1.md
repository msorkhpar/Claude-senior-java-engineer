# 10.2.1. Heap and Stack Memory Management

The nightly import service failed three times in one month, each time with a different error. The
first was a `StackOverflowError` in a parser, on one customer's file. The second was
`OutOfMemoryError: Java heap space` in the PDF renderer. The third was `OutOfMemoryError: Metaspace`,
with most of the heap free. Each time the first fix was a bigger `-Xmx`, which helped only with the
second, and only for a few weeks.

Each error names a different memory area with its own limit: the thread's stack (`-Xss`), the heap
(`-Xmx`) and Metaspace (`-XX:MaxMetaspaceSize`). Every output below is from Java 21 (Temurin
21.0.12) on Linux x64.

## A Frame Holds a Call's Locals; Every Object the Call Creates Lives on the Heap

Each call pushes a **frame** onto its thread's stack, holding the call's parameters and local
variables, and pops it on return. A local holds a primitive value or a reference. The object a
reference points to lives on the **heap**, which every thread shares (unit 5.1.2 shows what that
sharing means for threads).

This program counts the frames that fit before `StackOverflowError`, for three recursive methods:

```java
static void oneInt(int n) {
    depth++;
    oneInt(n + 1);
}

static long tenLongs(long a) {
    depth++;
    long b = a + 1, c = b + 1, d = c + 1, e = d + 1, f = e + 1,
         g = f + 1, h = g + 1, i = h + 1, j = i + 1;
    return tenLongs(j) + a + b + c + d + e + f + g + h + i + j;   // all ten live across the call
}

static int bigArray(int n) {
    depth++;
    byte[] buffer = new byte[10_000];                // 10 KB per frame, on the heap
    return bigArray(n + 1) + buffer.length;
}
```

Each runs on a new thread with the default 1 MB stack, under `-Xint` so that every frame is a
fixed-size interpreter frame:

```text
run 1  one int:   9,833   ten longs:   3,806   10 KB array:   9,077
```

An interpreter frame has a slot for every local the method declares, so ten `long` locals make
each frame bigger and fewer fit. The method with the array went 9,077 frames deep: about 90 MB of
arrays on a 1 MB stack, because each frame held only a reference and the arrays were on the heap.
A large buffer doesn't need a larger `-Xss`, and returning from the method doesn't free it. The
array becomes garbage for the collector (unit 10.2.2) once nothing refers to it.

## -Xss Sets How Many Frames Fit, So Recursion as Deep as the Data Fails on Large Input

```text
## -Xint -Xss256k
run 1  one int:   1,641   ten longs:     635   10 KB array:   1,515
## -Xint -Xss4m
run 1  one int:  42,601   ten longs:  16,491   10 KB array:  39,324
```

Going from 256 KB to 1 MB added 8,192 frames of `oneInt`, about 96 bytes each. The 256 KB stack
held only 1,641 of them because about 96 KB of every stack is kept free for the JVM: four guard
pages (`StackRedPages`, `StackYellowPages`, `StackReservedPages`) and a 20-page shadow zone
(`StackShadowPages`), 4 KB each on this build.

With the JIT on, the depth changes between runs, here three runs one after another in the same JVM:

```text
## default (JIT on)
run 1  one int:  17,449   ten longs:   5,362   10 KB array:  19,260
run 2  one int:  59,002   ten longs:   6,556   10 KB array:  19,668
```

A compiled frame is smaller than an interpreted one, and the JIT compiles the method while the
recursion is running, so the stack fills with a mix of both. A recursion that is safe at a depth
of 5,000 in a test may not be in production.

That matters when the depth comes from the data. A linked chain summed by recursion takes one frame
per node:

```java
record Node(int value, Node next) {}

static long sumRecursive(Node n) {
    return n == null ? 0 : n.value() + sumRecursive(n.next());
}

static long sumLoop(Node n) {
    long sum = 0;
    for (; n != null; n = n.next()) sum += n.value();
    return sum;
}
```

```text
## default -Xss
    1,000 nodes   recursive: 1000               loop: 1000
  100,000 nodes   recursive: StackOverflowError loop: 100000
1,000,000 nodes   recursive: StackOverflowError loop: 1000000
## -Xss16m
    1,000 nodes   recursive: 1000               loop: 1000
  100,000 nodes   recursive: 100000             loop: 100000
1,000,000 nodes   recursive: StackOverflowError loop: 1000000
```

A 16 MB stack moved the limit and didn't remove it. It also applies to every thread in the
process, because `-Xss` is the default for all of them: each reserves 16 MB of address space, and
a runaway recursion in any of them gets 16 times deeper before it fails. The loop uses one frame whatever the length. For a
tree, the loop keeps the nodes still to visit in an `ArrayDeque` on the heap, which holds as many
as memory allows. Java has no tail-call elimination: a recursive call in the last position still
pushes a frame.

`Thread.Builder.OfPlatform.stackSize` gives one thread a bigger stack. A virtual thread is still
capped:

```java
int byDefault = on(Thread.ofPlatform());                       // the -Xss default
int sixteenMb = on(Thread.ofPlatform().stackSize(16 << 20));   // 16 MB for this thread only
int virtual = on(Thread.ofVirtual());
```

```text
## -Xint
platform, -Xss default:   9,835
platform, stackSize 16 MB: 173,675
virtual:                  9,818
## -Xint -Xss256k
platform, -Xss default:   1,643
platform, stackSize 16 MB: 173,675
virtual:                  1,626
```

A virtual thread's frames are copied to the heap when it unmounts, so they cost only what is in
use, but `-Xss` still caps how deep it can go. The `Thread` javadoc warns that on some platforms
the stack size argument has no effect.

<!-- practice 1 -->

## OutOfMemoryError Is Thrown by Whichever Allocation Fails, Not by the Code That Filled the Heap

The PDF renderer from the opening has two allocations per request: a 256 KB report cached in a
list that nothing evicts, and a 4 MB render buffer that is garbage once `render()` returns. It runs
with `-Xmx64m`:

```java
static final List<byte[]> reportCache = new ArrayList<>();   // filled, never evicted

static void cacheReport() {
    reportCache.add(new byte[256 * 1024]);
}

static int render() {
    byte[] buffer = new byte[4 * 1024 * 1024];                // freed when render() returns
    return buffer.length;
}
```

```text
request 176, 176 reports cached (44 MB)
java.lang.OutOfMemoryError: Java heap space
	at HeapFull.render(HeapFull.java:13)
	at HeapFull.main(HeapFull.java:23)
```

The stack trace names `render()`, which holds nothing after it returns. The cache, reachable from a
static field, holds 44 MB that no collection can free. The other 20 MB weren't enough for one
4 MB array: G1, the default collector here, needs free regions in a row for an array that large,
and keeps part of the heap for new objects. When the buffer didn't fit even after a full
collection, the JVM threw the error at the `new` that asked. A smaller buffer in `render()` would
move the failure to the next allocation, and a bigger heap moves it to a later request:

```text
request 370, 370 reports cached (92 MB)
request 754, 754 reports cached (188 MB)
```

Those are `-Xmx128m` and `-Xmx256m`: twice the heap, twice the requests, because the cache grows
with every request. A heap dump (`-XX:+HeapDumpOnOutOfMemoryError`) shows what holds the memory.

The collector frees only objects that nothing reachable refers to. A collection that is empty to
its callers can still refer to everything it held. Here an array-backed queue takes forty 1 MB
uploads and hands all forty out. Its `take()` returns the oldest item and moves past its array
slot; the second version also sets that slot to `null`:

```text
take() leaves the slot: queue empty, heap in use after GC: 41 MB
take() clears the slot: queue empty, heap in use after GC: 1 MB
```

The first still refers to every upload, from slots it will never read again.

<!-- practice 2 -->

## An Object Costs a Header Plus Its Fields, Rounded Up to 8 Bytes; a Reference Costs 4 Bytes Below 32 GB

Object sizes aren't in the language specification, but they can be measured. This program stores
100,000 objects of each kind in an array, so that none can be optimised away, and reads the thread's
allocation counter (`com.sun.management.ThreadMXBean.getCurrentThreadAllocatedBytes`, which its
javadoc calls an approximation) before and after:

```java
static long perObject(IntFunction<Object> make) {
    Object[] keep = new Object[N];                           // allocated before we start counting
    long before = THREADS.getCurrentThreadAllocatedBytes();
    for (int i = 0; i < N; i++) keep[i] = make.apply(i);     // stored, so nothing is optimised away
    long after = THREADS.getCurrentThreadAllocatedBytes();
    return Math.round((after - before) / (double) N);
}
```

```text
compressed oops: true
new Object()                        16 bytes
class with one int                  16 bytes
class with one long                 24 bytes
class with one reference            16 bytes
Long.valueOf(1_000_000 + i)         24 bytes
record Reading(long, double)        32 bytes
new byte[0]                         16 bytes
new byte[1]                         24 bytes
new long[1]                         24 bytes
new Object[1]                       24 bytes
String of 3 Latin-1 chars + byte[]  48 bytes
a million longs: long[] 8.0 bytes each, ArrayList<Long> 28.0 bytes each
```

Every object starts with a 12-byte header (an 8-byte mark word and a 4-byte class pointer,
compressed by default whether or not references are), and
its size is rounded up to a multiple of 8. An `int` fits in the 4 bytes left after the header; a
`long` must start at an 8-byte boundary, so a `Long` takes 24 bytes for 8 bytes of value. An array
adds a 4-byte length to the header. A list of a million `Long`s costs 4 bytes per reference in the
list's array plus 24 per `Long`: 28 bytes per number, 3.5 times a `long[]`.

A reference is 4 bytes because of **compressed oops**: the JVM stores a heap address shifted right
by 3 bits, which reaches 32 GB. The same program with `-XX:-UseCompressedOops`, trimmed to the rows
that changed:

```text
compressed oops: false
class with one reference            24 bytes
String of 3 Latin-1 chars + byte[]  56 bytes
a million longs: long[] 8.0 bytes each, ArrayList<Long> 32.0 bytes each
```

A heap that the shifted address can't reach turns them off:

```text
-Xmx31g:      bool UseCompressedOops                        = true                           {product lp64_product} {ergonomic}
-Xmx32g:      bool UseCompressedOops                        = false                          {product lp64_product} {default}
```

At `-Xmx32g` every reference field and object-array slot doubles, so a heap full of collections
can hold less data than at `-Xmx31g`.

<!-- practice 3 -->

## Escape Analysis Can Remove an Allocation, but Only in Compiled Code and Only While the Object Stays Local

Here two methods create the same `Point`
ten million times per round; one keeps it local, the other also stores it in a static field:

```java
record Point(int x, int y) {}

static Point last;                                     // a field outlives the call

static int staysLocal(int x, int y) {
    Point p = new Point(x, y);
    return p.x() * p.x() + p.y() * p.y();
}

static int escapes(int x, int y) {
    Point p = new Point(x, y);
    last = p;                                          // now reachable after the call returns
    return p.x() * p.x() + p.y() * p.y();
}
```

The allocation counter per round, by default and with `-XX:-DoEscapeAnalysis`:

```text
## default
round 1   stays local:       496 KB   escapes:   234,375 KB
round 2   stays local:         0 KB   escapes:   234,375 KB
## -XX:-DoEscapeAnalysis
round 2   stays local:   234,375 KB   escapes:   234,375 KB
```

234,375 KB is ten million 24-byte `Point`s. Once C2, the optimising JIT compiler, compiled the
loop with `staysLocal` inlined, it saw that no reference to the `Point` outlived the call and
removed the allocation. The 496 KB of round 1 were allocated while the loop still ran in the
interpreter and in C1, which doesn't do this analysis. The stored `Point` escapes and is allocated every
time.

> **No stack allocation.** HotSpot doesn't put the `Point` on the stack instead. Oracle's
> documentation of the server compiler's escape analysis says it eliminates scalar-replaceable
> allocations and does not replace a heap allocation with a stack allocation.

The analysis sees only what C2 has inlined. With the record's constructor excluded from inlining
(`-XX:CompileCommand=dontinline,Escape$Point::<init>`), the `Point` is passed to a call the
compiler can't see into, and counts as escaping:

```text
## -XX:CompileCommand=dontinline,Escape$Point::<init>
round 2   stays local:   234,375 KB   escapes:   234,375 KB
```

`-XX:+PrintCompilation` shows `staysLocal` compiled at tier 4, by C2, in that run as well: the
method is still compiled, and the allocation stays in the compiled code. Unit 4.1.3 shows the
same effect on boxing, through a lambda call site that stops being inlined.
A refactoring that stores the object, or adds a call the JIT doesn't inline, brings the allocation
back without a change in behaviour that a test would see. The numbers are from one machine.

## Classes Live in Metaspace, Outside -Xmx

The JVM keeps a loaded class's metadata (its methods, bytecode and constant pool) in **Metaspace**,
native memory outside the heap. `-Xmx` doesn't bound it; `-XX:MaxMetaspaceSize` does, and by
default it is unlimited. Code that generates classes at run time fills it. This program defines
hidden classes from the same bytes in a loop (`MethodHandles.Lookup.defineHiddenClass`), with
Metaspace capped at 32 MB and a 1 GB heap. With `keep` it holds every generated class in a list;
with `drop` it doesn't:

```java
Class<?> c = lookup.defineHiddenClass(bytes, true).lookupClass();
defined++;
if (keep) generated.add(c);
```

```text
keep: java.lang.OutOfMemoryError: Metaspace after 16,056 classes; heap in use 3 MB of 1024 MB max
drop: defined 300,000 classes, 299,159 unloaded
```

The JVM ran out of memory with the heap almost empty, so a bigger `-Xmx` would have changed
nothing. A class is unloaded only when nothing can reach it any more: a hidden class through its
`Class` object, an ordinary class through its class loader. A cache of generated classes keyed by something that changes
on every request (a template's text with an id in it, or a new class loader per script) is the
`keep` case. Without a `MaxMetaspaceSize` the same leak grows the process's native memory, which
no heap setting limits, until the compressed class space (`-XX:CompressedClassSpaceSize`, 1 GB by
default) runs out with `OutOfMemoryError: Compressed class space`. With that space capped at 8 MB
and no Metaspace limit, the `keep` run failed that way after 16,123 classes.

<!-- practice 4 -->

## Summary

- **A frame holds a call's locals; objects live on the heap.** A big local array costs the stack one
  reference and stays on the heap until nothing refers to it.
- **`-Xss` sets how many frames fit.** The depth changes with frame size and with JIT compilation,
  so recursion as deep as the input fails on some input; a loop or an explicit `ArrayDeque` doesn't.
  A virtual thread is capped by `-Xss` too.
- **`OutOfMemoryError` is thrown by the allocation that failed.** The trace names that allocation, a
  bigger heap moves the failure, and a collection that keeps references in slots it no longer uses
  holds everything.
- **An object is a 12-byte header plus fields, padded to 8.** A `Long` is 24 bytes, a list of
  `Long`s 28 bytes per number; from `-Xmx32g` up every reference doubles.
- **Escape analysis removes an allocation only in compiled code for an object that stays local.**
  A field store or a call the JIT doesn't inline brings it back.
- **Classes live in Metaspace, outside `-Xmx`.** Generated classes kept reachable exhaust it with
  the heap nearly empty.

## Reference

**Where things live, and what fails (HotSpot, Java 21):**

| Area | Holds | Size set by | When it runs out |
|---|---|---|---|
| Thread stack | one frame per active call: parameters, locals, partial results | `-Xss` (default 1 MB on Linux x64), `Thread.Builder.OfPlatform.stackSize` | `StackOverflowError` in that thread |
| Heap | every object and array | `-Xmx` (default a quarter of physical memory) | `OutOfMemoryError: Java heap space` |
| Metaspace | class metadata | `-XX:MaxMetaspaceSize` (default unlimited) | `OutOfMemoryError: Metaspace` |
| Compressed class space (part of Metaspace) | class structures | `-XX:CompressedClassSpaceSize` (default 1 GB) | `OutOfMemoryError: Compressed class space` |

**Flags used on this page:**

| Flag | Effect |
|---|---|
| `-Xint` | interpret only, no JIT |
| `-XX:-DoEscapeAnalysis` | turns off C2's escape analysis |
| `-XX:-UseCompressedOops` | 8-byte references; also the default from `-Xmx32g` up |
| `-XX:+HeapDumpOnOutOfMemoryError` | writes a heap dump when the first `OutOfMemoryError` is thrown |

## Challenges

1. A JSON library parses nested arrays recursively. What nesting depth does it survive on your
   service's request threads, and what should the service do with a request nested deeper?
2. Your service holds 50 million `HashMap<Long, Long>` entries. Estimate the bytes per entry from
   the sizes on this page, then measure it the way `Sizes` does. What would a primitive-keyed
   structure save?
3. A template engine compiles each template to a class. How would you find out, on a running
   service, whether it unloads them?

## Design Note: Why HotSpot Removes Allocations Instead of Putting Objects on the Stack

An object's lifetime in Java isn't tied to the method that created it: any reference can be
stored in a field, returned, or handed to another thread, and the language has no way to say "this
object dies with the frame". A JVM that put objects on the stack would have to prove for each one
that no reference outlives the frame. HotSpot asks for more: that no reference leaves the code C2
has compiled together. When it can prove that, it drops the object and works with the values of
its fields; when it can't, as with the constructor kept out of inlining, the object goes on the
heap even though it never outlives the frame.

The cost is that allocation stops being visible in the source. The same method can cost 234 MB per
ten million calls or nothing, depending on an inlining decision the JIT makes at run time and may
make differently after a refactoring, so allocation has to be measured, with a counter like the
one on this page or with a profiler.

## Code Examples

- Test: [HeapStackMemoryTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/memorymanagement/HeapStackMemoryTest.java)
- Source: [HeapStackMemory.java](src/main/java/com/github/msorkhpar/claudejavatutor/memorymanagement/HeapStackMemory.java)
