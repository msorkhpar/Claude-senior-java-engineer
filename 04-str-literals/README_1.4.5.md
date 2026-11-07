# 1.4.5 StringBuilder and StringBuffer in Java

The payments service keeps its audit trail in one `StringBuffer` field, chosen because
`StringBuffer` is thread-safe. Request threads append `user=… action=…` lines to it, and a flusher
writes it to disk every second. After a load test the trail holds lines like
`user=u4 action=loginuser=user=`. In review, one engineer wants a `StringBuilder`, because
`StringBuffer` is slow. Another wants one `StringBuilder` per thread in a `ThreadLocal`, cleared
with `setLength(0)`, to save allocations. The first change loses lines and throws; the second,
in a service whose threads also build multi-megabyte reports, keeps hundreds of megabytes that
nobody uses.

## StringBuffer Locks Each Call, So a Line Built from Several Calls Still Interleaves

Four threads record 50,000 lines each, three ways, in three rounds in one JVM (`\0` prints as
`.`):

```java
StringBuffer shared = new StringBuffer();
report("StringBuffer, three calls", shared, (user, action) -> {
    shared.append("user=").append(user);
    shared.append(" action=").append(action).append('\n');
});
StringBuilder unsafe = new StringBuilder();
report("StringBuilder, three calls", unsafe, (user, action) -> {
    unsafe.append("user=").append(user);
    unsafe.append(" action=").append(action).append('\n');
});
StringBuffer out = new StringBuffer();
report("local line, one append", out, (user, action) -> {
    StringBuilder line = new StringBuilder(48);
    line.append("user=").append(user).append(" action=").append(action).append('\n');
    out.append(line);
});
```

```text
round 1
  StringBuffer, three calls   89,513 good, 110,487 broken
      e.g. user=u4 action=loginuser=user=
  StringBuilder, three calls  49,845 good,     269 broken, 3 of 4 threads died with ArrayIndexOutOfBoundsException
      e.g. user=..........................................................
  local line, one append     200,000 good,       0 broken
round 2
  StringBuffer, three calls  200,000 good,       0 broken
  StringBuilder, three calls  51,156 good,   9,056 broken, 2 of 4 threads died with ArrayIndexOutOfBoundsException
  local line, one append     200,000 good,       0 broken
```

Each `StringBuffer` call such as `append` or `length()` locks the buffer and sees what every
earlier call wrote, but between two calls the thread holds no lock, so another thread's text lands
mid-line. What must be atomic is the whole line, and no buffer method writes one: a compound
action, as in unit 6.2.1.

A shared `StringBuilder` also corrupts the text. Its appends read and update the length with no
lock, so two threads write to the same positions, or one writes into the old array just after
another copied it to a larger one; the dots are where a write was lost. And when one thread checks
the capacity, then another advances the length, the first writes past the end of an array that is
too small: `ArrayIndexOutOfBoundsException`, and here three of four threads died.

The local builder never leaves its thread, so it needs no lock; the only shared operation is one
`append` of a finished line, which `StringBuffer` does make atomic. Where several calls on a shared
object must stay together, hold its lock across all of them, and build the text before you take it.

> **Warm-up hides it.** By round 2 the JIT had merged the adjacent locks on the same buffer into
> one (lock coarsening). With `-XX:-EliminateLocks`, which turns that off, every round broke 63 to
> 69 % of the lines. A concurrency test that passes after warm-up shows what this JIT did with this
> code, not what the language promises.

<!-- practice 1 -->

## A Builder Is an Array: Growing It and Inserting at the Front Copy What Is Already There

A builder keeps its text in an array with room to spare. The capacities a default builder passes
through while we append one character at a time:

```text
capacities: 16, 34, 70, 142, 286, 574, 1150, 2302, 4606, 9214
```

When an append doesn't fit, JDK 21's `AbstractStringBuilder.newCapacity` picks the larger of what
is needed and twice the old capacity plus 2, allocates that array and copies everything over. The
copies add up to about the final size again, so appending stays linear, but the old arrays are
garbage. We built a 1,400,000-character export from 100,000 rows, with the default builder and
with one sized to the result:

```java
StringBuilder sb = initialCapacity < 0 ? new StringBuilder() : new StringBuilder(initialCapacity);
for (int i = 0; i < rows; i++) sb.append(ROW);
return sb.toString();
```

```text
new StringBuilder()          1,400,000 chars  5,975 KB allocated    450 us
new StringBuilder(1400000)   1,400,000 chars  2,734 KB allocated    331 us
```

Sizing halved the allocation. Half of what remains is `toString()`, which always copies the text
into a new `String`. Times varied from 0.3 to 1.4 ms across runs for both, so expect less garbage, not
a reliable speed-up. Size a builder when you know the result's length, and
don't guess high: an oversized builder holds its whole array until it is dropped.

Appending at the end copies only on growth. `insert(0, row)` shifts every character already in the builder, on every call. A newest-first log view built that way:

```text
 20,000 rows: insert(0, row)     23 ms   append(row)    446 us
 40,000 rows: insert(0, row)     96 ms   append(row)    885 us
 80,000 rows: insert(0, row)    409 ms   append(row)  1,030 us
```

Doubling the rows quadrupled the time, as with `+=` in unit 1.4.2. `deleteCharAt(0)` and
`delete(0, n)` shift the rest the same way. Collect the rows, then append them in the order you
want.

## setLength(0) Empties a Builder and Keeps Its Array

```text
after setLength(0): length 0, capacity 3000000
after trimToSize(): length 0, capacity 0
```

`setLength(0)` only sets the length; the array the builder grew stays. We gave each of 32 pool
threads one 3,000,000-character report to build with its `ThreadLocal` builder, then only short
lines:

```text
heap held by 32 idle pool threads' builders: 160 MB
after the pool's threads end: 0 MB
```

Every thread that ever built something large keeps that much for as long as it lives, and pool
threads live as long as the service. `trimToSize()` gives it back, but leaves capacity 0, so the
next line regrows from nothing. For short lines, reuse saved nothing:

```text
setLength(0) on one builder  79 bytes,  21 ns per line (0)
new StringBuilder(256)      79 bytes,  15 ns per line (0)
```

79 bytes is the result `String`. The JIT removed the fresh builder entirely (with
`-XX:-OptimizeStringConcat`, 375 bytes per line), but not for every chain: unit 1.4.2's chain with
a `long` kept its builder. Create a builder where you build the text. If you do keep one, replace it when it
has grown past what normal work needs.

<!-- practice 2 -->

## Two Builders Are Equal Only When They Are the Same Object

```java
StringBuilder a = new StringBuilder("ORD-").append(7);
StringBuilder b = new StringBuilder("ORD-").append(7);
System.out.println("a.equals(b):          " + a.equals(b));
System.out.println("hash codes equal:     " + (a.hashCode() == b.hashCode()));
System.out.println("a.compareTo(b):       " + a.compareTo(b));
System.out.println("a.toString().contentEquals(b): " + a.toString().contentEquals(b));
Set<StringBuilder> seen = new HashSet<>();
seen.add(a);
System.out.println("seen.contains(b):     " + seen.contains(b));
```

```text
a.equals(b):          false
hash codes equal:     false
a.compareTo(b):       0
a.toString().contentEquals(b): true
seen.contains(b):     false
```

`StringBuilder` and `StringBuffer` inherit `equals` and `hashCode` from `Object`. A content hash on
a mutable object would change with every append, and a set would lose the entry it filed under
the old hash. So a `HashSet<StringBuilder>` used to drop duplicate messages keeps every one. Compare contents with
`compareTo` (since Java 11), `CharSequence.compare`, or `String.contentEquals` (unit 1.4.4), and
key collections by `toString()`.

<!-- practice 3 -->

## Summary

- **`StringBuffer` locks each call, not the line.** Text written with several calls interleaves;
  build it in a local `StringBuilder` and publish it in one call. A shared `StringBuilder` loses
  text and throws.
- **A builder is an array.** It grows to twice its capacity plus 2 by copying; sizing it halves the allocation, and `insert(0, …)` in a loop is quadratic.
- **`setLength(0)` keeps the array.** A reused builder holds its largest size for the life of its
  thread, and saved nothing per line in our run.
- **Builders compare by identity.** Use `compareTo`, `contentEquals` or `toString()` for content.

## Reference

**Capacity (Java 21):**

| Call | Result |
|---|---|
| `new StringBuilder()` | capacity 16 |
| `new StringBuilder("abc")` | capacity 19: the text's length plus 16 |
| `new StringBuilder(-1)` | `NegativeArraySizeException` |
| `new StringBuilder((String) null)` | `NullPointerException` |
| an append that doesn't fit | new capacity max(needed, 2 × capacity + 2) |
| `ensureCapacity(20)` on capacity 16 | 34; `ensureCapacity(50)` gives 50 |
| `setLength(0)` | length 0, capacity unchanged |
| `trimToSize()` | capacity = length |

**Edits:**

| Call | Result |
|---|---|
| `append((String) null)`, `append((Object) null)` | appends `null` |
| `append((char[]) null)` | `NullPointerException` |
| `append(null)` | does not compile: `append(CharSequence)` and `append(char[])` both match |
| `"abcdef"`: `delete(2, 100)` | `ab`: an end past the length is clamped |
| `"abc"`: `insert(4, "x")`, `charAt(3)` | `StringIndexOutOfBoundsException` |
| `"ab"`: `setLength(4)` | length 4, padded with `\0` |
| `"abcdef"`: `replace(1, 3, "XYZW")` | `aXYZWdef` |
| `append(…)` | returns the same builder, so calls chain |
| `toString()` | a new `String` on every call, on both classes |
| `"ok 👍"`: `reverse()` | `👍 ko`: a surrogate pair stays in order |
| `"cafe\u0301"` (`e` + combining acute): `reverse()` | the accent moves before `e`: a combining mark is not kept with its letter |
| `"ok 👍"`: `deleteCharAt(length() - 1)` or `setLength(4)` | leaves a lone high surrogate (unit 1.4.3) |

## Challenges

1. Find a `StringBuffer` in a codebase you know. Is it shared between threads, and if so, is every
   line written to it in one call?
2. A report's output varies from 2 KB to 40 MB. How would you size its builder, and what does a
   wrong estimate cost in each direction?
3. Measure what your service's per-thread builders hold: take a heap histogram after a day of
   traffic and look for large `byte[]` arrays reachable from `ThreadLocal`s.

## Design Note: Why StringBuilder Came Eight Years After StringBuffer

Java 1.0 shipped `StringBuffer` with its methods synchronized, and `javac` compiled `+` to it.
Java 5 added `StringBuilder`, the same class without the locks, and moved the compiler to it:
nearly every buffer is local to one method, and each concatenation paid for locks no other thread
could contend. `StringBuffer` stays for compatibility, and today it is worth choosing
only where an old API asks for one.

## Code Examples

- Test: [StringBuilderBufferTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/literals/StringBuilderBufferTest.java)
- Source: [StringBuilderBuffer.java](src/main/java/com/github/msorkhpar/claudejavatutor/literals/StringBuilderBuffer.java)
