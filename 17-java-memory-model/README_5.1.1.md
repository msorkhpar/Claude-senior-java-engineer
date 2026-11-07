# 5.1.1. Overview of the Java Memory Model

An `Invoice` is immutable, and three values derived from it are expensive, so a teammate caches
them lazily in plain fields: an `int` checksum of the line items, a `long` total in cents, and a
`HashMap` of totals per tax code. Each getter checks for the field's default value, computes, and
stores. The review comment says these fields are written by one thread and read by others with no
synchronization. The reply: "`String.hashCode` does exactly this. The race is benign."

One of the three caches is safe, and only if its getter is written with care. Deciding which takes
the rule the Java Memory Model (JMM, chapter 17 of the Java Language Specification) states, not
the usual picture of threads with private caches.

Every output below comes from Java 21 (Temurin 21.0.12.1, HotSpot, x86-64, 20 cores).

## The JMM Says Which Writes a Read May Return, and a Test Shows Only Some of Them

The JLS defines the model by checking reads: it "works by examining each read in an execution
trace and checking that the write observed by that read is valid according to certain rules". It
names no caches, registers or main memory. Happens-before is the order the JMM guarantees between
two actions, from program order and from synchronization (unit 5.2.1 lists the edges). For a plain
field the main rule is: a read may return any write to that field, unless the read happens-before
that write, or another write to the field comes between the two in happens-before order. (JLS
17.4.8 also rules out values no write produced.)

Here is the checksum written the way most of us would write it first:

```java
final class Doc {
    private final String text;
    private int hash;                              // plain field, 0 until computed

    Doc(String text) { this.text = text; }

    int hash() {
        if (hash == 0) hash = text.hashCode();     // read 1, and maybe a write
        return hash;                               // read 2
    }
}
```

Thread A stores the hash. Thread B's read 1 sees A's value, so B skips the computation. Can B's
read 2 return 0?

Two threads called `hash()` on the same fresh `Doc`s, a batch of 1,000 at a time. A counting copy
of the class, run the same way, showed both threads computing the hash of more than a quarter of
them, so the threads really did overlap:

```text
read twice: 1,000,000,000 calls, 0 returned 0
read twice: 1,000,000,000 calls, 0 returned 0
read twice: 1,000,000,000 calls, 0 returned 0
```

The JLS allows 0 all the same. It treats the default 0 as a write that happens-before every
action of every thread. A's store is ordered with neither of B's reads, so it doesn't count as a
write between the 0 and read 2, and read 2 may return either (for a reference field, null).

Nothing makes two reads of one plain field see writes in the order they were made: `VarHandle`'s
javadoc says plain accesses "impose no observable ordering constraints with respect to threads
other than the executing thread", and promises "coherently ordered" accesses to one variable (every
thread sees its writes in one order) only from opaque mode up. Within one thread nobody can tell,
so a compiler may load the value it returns before the one it tests. No call returned 0 in three
billion: allowed by the JLS, not observed here.

A run shows what one JIT did on one CPU; the JMM defines the outcomes your code must be correct
for. If `Doc.hashCode()` returned `hash()`, the day another compiler returns 0 a `HashMap` keyed by
`Doc` misses an entry that is there (unit 5.3.1 describes jcstress, the tool built to hunt such
outcomes).

The fix is to read the field once, into a local, and return the local, as `String` does.

## A Program Without Data Races Runs as Some Interleaving of Its Threads

This is the JMM's central promise (JLS 17.4.5): "If a program is correctly synchronized, then all
executions of the program will appear to be sequentially consistent." Correctly synchronized
means free of data races: no two accesses to one variable, at least one of them a write, that
happens-before leaves unordered (unit 5.3.1 has the full definition). Sequentially consistent means
as if the threads' actions ran one at a time, in one order that keeps each thread's own order, with
every read seeing the latest write before it.

Compilers and CPUs keep the reorderings a single thread can't notice, except across
synchronization actions such as volatile accesses and locks, and a program without races can't
notice them either. You reason about
interleavings and ignore the JIT, store buffers and caches. You don't need reorderings even to
decide whether you have a race: the JLS counts a program correctly synchronized if its
sequentially consistent executions have none.

Does race-free mean safe? A rate limiter creates its map of limits lazily. Every access to the
field is volatile and the map is a `ConcurrentHashMap`, so there is no data race:

```java
static volatile Map<String, Integer> limits;

static Map<String, Integer> limits(CyclicBarrier bothChecked) throws Exception {
    Map<String, Integer> m = limits;
    if (m == null) {
        m = new ConcurrentHashMap<>();
        bothChecked.await(5, TimeUnit.SECONDS);   // both threads have seen null
        limits = m;
    }
    return m;
}
```

Two threads each put one tenant's limit through the same barrier `b`,
`limits(b).put("tenant-a", 100)` and `limits(b).put("tenant-b", 250)`. The barrier holds both between the check and the write, where the
scheduler could pause them in production:

```text
limits after both puts: {tenant-b=250}
```

Three runs kept `tenant-b` once and `tenant-a` twice, never both. Nothing surprising happened to
memory: this is an ordinary interleaving. A checks, B checks, each creates a map and stores it, the
later store wins, and the other tenant's limit sits in a map nobody can reach. Sequential
consistency settles which write each read sees; it doesn't make a check and the write that depends
on it one step. That is atomicity, a separate kind of failure (the last section maps them).

The promise covers whole programs: the JLS gives it to programs with no data race, and says nothing
of the race-free part of a program that races elsewhere.

## A Plain long or double May Be Read Half Old, Half New

JLS 17.7: "a single write to a non-volatile long or double value is treated as two separate
writes: one to each 32-bit half". A reader can combine the high half of one write with the low half
of another, and get a value no thread wrote. Reads and writes of `volatile` `long` and `double` are
always atomic, and so are reads and writes of references.

A writer stores values whose two halves are equal, `((long) i << 32) | (i & 0xFFFF_FFFFL)` for
`i = 1, 2, 3, …`,
into a static `long`, and a reader counts values whose halves differ:

```java
for (long n = 0; n < reads; n++) {
    long v = useVolatile ? vol : plain;
    if (stop) break;                                     // a volatile read: keeps the plain read inside the loop
    if ((int) (v >>> 32) != (int) v) torn++;
    if (v != last) { changes++; last = v; }
}
```

```text
plain    long: 1,000,000,000 reads, 21,964,812 new values seen, 0 torn
plain    long: 1,000,000,000 reads, 10,521,451 new values seen, 0 torn
plain    long: 1,000,000,000 reads, 24,063,759 new values seen, 0 torn
volatile long: 1,000,000,000 reads, 24,492,069 new values seen, 0 torn
```

A further 100 million interpreted reads (`-Xint`) found none either: no read was torn, so tearing
is allowed by the JLS, not observed here. The JLS lets any JVM split the write, whatever the
hardware it runs on; the rule exists so that hardware
without an atomic 64-bit access needs no lock for every `long`.

What a torn read costs depends on where the halves meet. A byte counter going from 4,294,967,295
to 4,294,967,296 changes both halves:

```text
before 4,294,967,295 (0x00000000FFFFFFFF)
after  4,294,967,296 (0x0000000100000000)
new high half, old low half: 8,589,934,591
old high half, new low half: 0
```

A usage report that reads the counter at that moment bills 8 GiB, or nothing. Declare a shared
`long` or `double` `volatile`, use `AtomicLong`, or read and write it under one lock.

The JLS forbids the other kind of tearing (17.6): two threads writing neighbouring fields, or
neighbouring elements of a `byte[]`, never overwrite each other's values.

## A Race Is Benign Only When Every Thread Writes the Same Value, in One Indivisible Write, Read Once

`String.hashCode()` in JDK 21 races on purpose, and its comment says on what condition:

```java
public int hashCode() {
    // The hash or hashIsZero fields are subject to a benign data race,
    // making it crucial to ensure that any observable result of the
    // calculation in this method stays correct under any possible read of
    // these fields. Necessary restrictions to allow this to be correct
    // without explicit memory fences or similar concurrency primitives is
    // that we can ever only write to one of these two fields for a given
    // String instance, and that the computation is idempotent and derived
    // from immutable state
    int h = hash;
    if (h == 0 && !hashIsZero) {
        h = isLatin1() ? StringLatin1.hashCode(value)
                       : StringUTF16.hashCode(value);
        if (h == 0) {
            hashIsZero = true;
        } else {
            hash = h;
        }
    }
    return h;
}
```

The same barrier as before holds two threads after the check on a static `int` cache that reads
its field once like `String`'s (without `hashIsZero`):

```text
hash computed 2 times; the threads got -474862093 and -474862093; the field holds -474862093
```

The race cost a second computation and nothing else. It is safe because:

- **Every thread writes the same value.** The input is immutable and the computation idempotent,
  so it doesn't matter which write a reader sees, or whether it sees one at all.
- **One write, of a type that can't tear.** `hash` is an `int`. Only one of the two fields is ever
  written for a given string, so no reader can see half of a two-field update.
- **The field is read once.** `h` is tested and returned, so the first section's second read
  doesn't exist.

Back to the `Invoice`. The `int` checksum is safe if its getter reads the field once, like
`String`. The `long` total can tear on a JVM that splits it. The `HashMap` of totals is built from
immutable data, so two threads build equal maps, but publishing it through a race fails:
`HashMap`'s fields aren't final, so nothing orders them before the plain reference
write, and a reader may see the reference and an empty table (allowed by the JLS; unit 5.1.4 shows
what reordering does to a partly built object, and unit 5.2.3 the final-field rule that makes an
immutable object safe to share through a race). A map that callers keep changing after it is
published is worse still: the tenant's limit showed what happens to writes into a losing copy. A
`volatile` field fixes it, and so does an unmodifiable map (`Map.copyOf`) built first, stored once
and read once, because the final fields inside it carry its contents along with the reference.

<!-- practice 1 -->

## Every Failure Here Is One of Three Kinds, and Each Has Its Unit

The failures on this page fall into three kinds:

| Kind | What a thread sees | Example | Fixed by | Units |
|---|---|---|---|---|
| Visibility | another thread's write late, or never | a plain stop flag the JIT reads once (unit 6.2.2) | any happens-before edge: `volatile`, a lock, `start`/`join`, a concurrent collection | 5.1.2, 5.1.3, 5.2.1, 5.2.2, 5.3.3 |
| Ordering | writes in an order nobody made them | the read-twice hash returning 0; a reference seen before the object's fields | `volatile` publication, final fields, safe publication | 5.1.4, 5.2.3 |
| Atomicity | an operation half done | a torn `long`; the lost tenant limit; `count++` losing updates | a lock, `AtomicLong` and friends, one immutable holder swapped whole | 5.3.1, 5.3.2, 6.2.1, 6.2.2 |

A data race causes the first two, and removing every race removes them. Atomicity failures can
come from a race (the torn `long`, `count++` on a plain field) but don't need one: the tenant's
limit was lost in a race-free program. Unit 5.3.4 collects the design
habits that prevent all three.

<!-- practice 2 -->

## Summary

- **The JMM specifies which writes each read may return.** A test shows what one JIT did on one
  CPU; a plain field read twice may return 0 after the hash was stored, though no run here did.
- **A program without data races behaves as an interleaving of its threads.** That removes
  reordering surprises, not check-then-act failures: a race-free lazy map still lost a limit.
- **A plain `long` or `double` may be read half old, half new.** 64-bit HotSpot never tore one in
  three billion reads; the JLS still allows it, and `volatile` forbids it.
- **A race is benign only when every thread writes the same value, in one indivisible write, read
  once.** `String.hashCode` qualifies; a lazy `long` or a lazy mutable map doesn't.
- **Visibility, ordering, atomicity.** Races cause the first two; the third can happen without a
  race.

## Reference

**What the JLS promises (chapter 17):**

| Access | Atomic | Ordered with other threads |
|---|---|---|
| plain `int`, `short`, `byte`, `char`, `boolean`, `float`, reference | yes | only through happens-before |
| plain `long`, `double` | not guaranteed: may be split into two 32-bit halves (17.7) | only through happens-before |
| `volatile` field of any type | yes | a write happens-before every later read of that field (later in the synchronization order) |
| neighbouring fields or array elements | each updated independently, never torn into each other (17.6) | — |

**`VarHandle` access modes, weakest to strongest (from its javadoc):**

| Mode | Adds |
|---|---|
| plain | atomic only for references and values of at most 32 bits; no ordering seen by other threads |
| opaque | atomic, and accesses to one variable are coherently ordered |
| acquire / release | an acquire read and what follows it are ordered after the matching release write and what precedes it |
| volatile | all volatile accesses totally ordered with each other |

## Challenges

1. Your service runs on 64-bit HotSpot only. Would you still make a shared `long` counter
   `volatile`? What would you gain beyond atomicity, and what would you measure first?
2. The lost tenant limit came from a race-free program. Fix it three ways (a lock, a
   `ConcurrentHashMap` created eagerly, an `AtomicReference` with `compareAndSet`) and compare what
   each costs on the path where the map already exists.
3. Pick a lazily cached field in a codebase you know. Which of the three conditions does it meet,
   and would you rather enforce them with `volatile` or with a comment like `String`'s?

## Design Note: Why Plain Fields Promise So Little

The JMM leaves plain accesses unordered, and not even coherent, so that the compiler can treat a
plain field almost like a local: keep it in a register, read it once for a whole loop, reorder two
reads that a single thread can't tell apart. Most fields are never shared, and they lose nothing.
Where ordering is needed you ask for it, and `VarHandle` offers each guarantee separately: opaque
for coherence, acquire and release for one-way ordering, volatile for a single order everyone
agrees on.

The cost falls on every field that is shared. Its default is the weakest guarantee, and nothing
warns when a field crosses threads. The benign race lives inside that default, and
`String.hashCode` needs a paragraph of comment to justify it.

## Code Examples

- Source: [JmmOverview.java](src/main/java/com/github/msorkhpar/claudejavatutor/javamemorymodel/JmmOverview.java)
- Test: [JmmOverviewTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/javamemorymodel/JmmOverviewTest.java)
