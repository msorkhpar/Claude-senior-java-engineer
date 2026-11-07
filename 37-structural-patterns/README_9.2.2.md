# 9.2.2. Decorator Pattern

The nightly orders export reaches a partner as `orders.csv.gz`, and since Tuesday their import
fails with `Unexpected end of ZLIB input stream`. The writer:

```java
try (FileOutputStream file = new FileOutputStream(p.toFile())) {
    OutputStream o = new BufferedOutputStream(new GZIPOutputStream(file));
    write(o);                                      // 20,000 order lines
}
```

The test checks that the file exists and isn't empty, and it passes. Before reading on, work out
which part of the export never reached the disk.

A decorator implements the same type as the object it wraps, passes calls on to it and adds
something on the way. `java.io` is built from them: `BufferedOutputStream` adds a buffer to any
`OutputStream`, `GZIPOutputStream` adds compression, and each holds the next stream in its `out`
field.

## Each Layer Can Hold Bytes, So Only Closing the Outermost Stream Writes Them All

We wrote the same 20,000 lines through the same three layers, ended them three ways, and read
each file back with a `GZIPInputStream`. The first run closes the outermost stream:

```java
OutputStream out = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(p.toFile())));
write(out);
out.close();
```

```text
close the outer stream         54947 bytes, read back 20000 lines
close the file only            44444 bytes, read back failed: java.io.EOFException: Unexpected end of ZLIB input stream
flush the outer, close file    44444 bytes, read back failed: java.io.EOFException: Unexpected end of ZLIB input stream
```

When the file was closed, two layers above it still held data. The `BufferedOutputStream` had up
to 8 KB of lines it hadn't passed on. The `GZIPOutputStream`'s `Deflater` held compressed data
it hadn't written out, and the GZIP trailer, a CRC-32 and the length, is written only by `finish()`.
Closing the file wrote none of that: the file has no reference to the layers above it.

`close()` on a decorator empties it into the stream it wraps (`BufferedOutputStream` flushes,
`GZIPOutputStream` calls `finish()`), then closes that stream. So closing the outermost stream
empties every layer from the top down. `flush()` goes down too, but `GZIPOutputStream` doesn't
flush its compressor on `flush()` unless it was built with `syncFlush` set, and even then it
writes no trailer. Make the outermost stream the resource of the `try`, or declare every layer as
a resource: they close in reverse order, closing these streams a second time does nothing, and
the file is still closed if a constructor above it throws (`GZIPOutputStream`'s writes the header).

Closing also closes streams you didn't open: closing a `PrintStream` built over `System.out`
closes `System.out`, and every later `System.out.println` prints nothing.

## finish() Completes the GZIP Data Without Closing, but Only What Has Reached the Compressor

Sometimes the stream below isn't yours to close: a `ZipOutputStream` with more entries to come,
or a connection that carries more messages. `GZIPOutputStream.finish()` compresses what it holds
and writes the trailer without closing the stream it wraps. Three stacks, each ended with
`finish()` before try-with-resources closes the file. The first puts a buffer over the compressor:

```java
try (FileOutputStream file = new FileOutputStream(p.toFile())) {
    GZIPOutputStream gz = new GZIPOutputStream(file);
    OutputStream o = new BufferedOutputStream(gz);
    write(o);
    gz.finish();
}
```

```text
Buffered(GZIP): finish()       54269 bytes, read back 19742 lines
GZIP(Buffered): finish()       48028 bytes, read back failed: java.io.EOFException: Unexpected end of ZLIB input stream
Buffered(GZIP): flush, finish   54947 bytes, read back 20000 lines
```

The first file is valid GZIP data that reads back without an error and lacks 258 orders. `finish()` completed the data it had received, and the last lines were
still in the buffer above it. The second stack, `new GZIPOutputStream(new BufferedOutputStream(file))`,
has the buffer below: `finish()` wrote the end of the data and the trailer into it, and nobody
flushed it. Before `finish()`, flush every layer above the compressor, as the third run does with
the first stack; after it, flush every layer below, which the second stack needed.

<!-- practice 1 -->

## Buffer Above the Compressor: the Order Decides How Many Calls Reach Each Layer

Closed properly, both orders write the same file, at different cost. A counting decorator placed
just above the `GZIPOutputStream` and another just above the file count the `write` calls each
receives, for an export of 200,000 lines written one line per call:

```text
Buffered(GZIP(file))           writes into GZIP     582   writes into file  1,120     28 ms
GZIP(Buffered(file))           writes into GZIP 200,000   writes into file     71     49 ms
GZIP(file)                     writes into GZIP 200,000   writes into file  1,120     53 ms
Buffered(GZIP(Buffered(file))) writes into GZIP     582   writes into file     71     30 ms
```

Each `write` into the `GZIPOutputStream` hands its bytes to the native `Deflater` and runs it,
which passes its output on in pieces of at most 512 bytes. So
a buffer above it turns 200,000 calls of about 24 bytes into 582 calls of up to 8 KB, and halves
the time on this machine. A buffer below it only collects the compressor's output, and turns its
1,120 small file writes into 71. Each file write is a system call, which costs more on a network
file system or a socket than on this local disk. The last stack does both. Place each decorator where
the calls it should change pass through.

## A Decorator That Doesn't Override a Default Method Runs the Interface's Code, Not the Wrapped Object's

A rates cache is a `ConcurrentHashMap`. Callers load a missing rate with `computeIfAbsent`,
which `ConcurrentHashMap` runs atomically, so each pair is fetched once (unit 8.4.2). Then someone
adds a metrics decorator. `AbstractMap` builds the rest of `Map` on `entrySet()` and `put`, so
forwarding four methods looks like enough:

```java
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

/** Counts cache lookups and misses; every Map call it implements goes to the wrapped map. */
class MeteredMap<K, V> extends AbstractMap<K, V> {
    final Map<K, V> inner;
    final LongAdder lookups = new LongAdder();
    final LongAdder misses = new LongAdder();

    MeteredMap(Map<K, V> inner) { this.inner = inner; }

    @Override public V get(Object key) {
        lookups.increment();
        V v = inner.get(key);
        if (v == null) misses.increment();
        return v;
    }
    @Override public V put(K key, V value) { return inner.put(key, value); }
    @Override public V remove(Object key) { return inner.remove(key); }
    @Override public Set<Entry<K, V>> entrySet() { return inner.entrySet(); }
}
```

Two threads ask for `EUR/JPY` at the same moment. Each fetch is numbered, and the fetch function
waits up to 500 ms for the other caller to reach it too, standing in for a slow rates service:

```text
ConcurrentHashMap                  fetches: 1, the two callers got the rate from fetch 1 and fetch 1
MeteredMap over ConcurrentHashMap  fetches: 2, the two callers got the rate from fetch 1 and fetch 2
  metrics: lookups 2, misses 2
```

`computeIfAbsent` is a default method of `Map`, and `MeteredMap` doesn't override it, so the
call runs `Map`'s version on the decorator: `get`, call the function, `put`. Both threads missed,
both fetched, and the second `put` replaced the first. Each `get` and each `put` is atomic on
the `ConcurrentHashMap`; the loss is in the gap between them. `ConcurrentHashMap`'s own `computeIfAbsent`
was never called. The javadoc of the default says it "makes no guarantees about synchronization
or atomicity", and that an implementation providing them must override it. Every default
the decorator doesn't override bypasses the wrapped object's own version the same way: `merge`,
`compute`, `putIfAbsent`. Unit 8.1.4
shows the same with `removeIf` running outside a lock.

Forwarding it straight to `inner` would keep the fetch atomic but skip the metrics, because
`inner.computeIfAbsent` never calls the decorator's `get`. The override has to do both:

```java
@Override
public V computeIfAbsent(K key, Function<? super K, ? extends V> loader) {
    lookups.increment();
    return inner.computeIfAbsent(key, k -> {
        misses.increment();
        return loader.apply(k);
    });
}
```

```text
forwarding computeIfAbsent         fetches: 1, the two callers got the rate from fetch 1 and fetch 1
  metrics: lookups 2, misses 1
```

A decorator over an interface with default methods decides, for each one, whether the default
running on the decorator is right. A default added to the interface in a later release is never
reviewed: the decorator still compiles and runs the new default, whatever the wrapped class
overrides.

<!-- practice 2 -->

## A Decorator Is Another Object: equals Is Forwarded Only Where the Interface Defines It

Two JDK decorators of the same `ArrayList`:

```java
List<String> tags = new ArrayList<>(List.of("java", "jvm"));
List<String> asList = Collections.unmodifiableList(tags);
Collection<String> asCollection = Collections.unmodifiableCollection(tags);
```

```text
unmodifiableList(tags).equals(tags):       true
unmodifiableCollection(tags).equals(tags): false
tags.equals(unmodifiableCollection(tags)): false
two unmodifiableCollection(tags) equal:    false
published.contains(another view of tags):  false
same with unmodifiableList:                true
```

`List` defines equality: same elements in the same order. `unmodifiableList` forwards `equals`
and `hashCode` to the list it wraps, so it equals any list with the same elements.
`Collection` defines no equality of its own, since a `List` and a `Set` with the same elements
must not be equal. A `Collection` decorator can't know which rule its contents follow, so
`unmodifiableCollection` keeps `Object`'s identity `equals`, as its javadoc says, and
`ArrayList.equals` accepts only a `List`. Each call returns a new wrapper, so two
views of one list are two unequal objects, and a `HashSet` of published views never finds the second.
`synchronizedCollection` does the same.

For your own decorator, forward `equals` and `hashCode` when the interface defines them (`List`,
`Set`, `Map`; unit 8.4.1 shows what breaks when a forwarding `Set` doesn't). Otherwise keep
identity. Either way the wrapper and the object inside are two objects: a `synchronizedList`
locks on the wrapper, so code that synchronizes on the inner list doesn't exclude the wrapper's
callers (units 6.5.2 and 8.4.3).

<!-- practice 3 -->

## Summary

- **Only closing the outermost stream writes every layer's bytes.** Closing the file under a
  `BufferedOutputStream` and a `GZIPOutputStream` leaves a truncated, unreadable file.
- **`finish()` ends the GZIP data without closing, for what has reached it.** Flush the layers
  above it first and the layers below it after, or the file is invalid or silently short.
- **The order of decorators decides what each one sees.** A buffer above the compressor cuts its
  calls; a buffer below it cuts writes to the file.
- **A default method the decorator doesn't override runs on the decorator.** The wrapped object's
  override, such as an atomic `computeIfAbsent`, is never called.
- **A decorator is a different object.** It is equal to what it wraps only if it forwards
  `equals`, which the JDK does where the interface defines equality.

## Reference

**What a `java.io` decorator call passes on (JDK 21 javadoc and source):**

| Call | What reaches the wrapped stream |
|---|---|
| `FilterOutputStream.close()`, `BufferedOutputStream.close()` | `flush()`, then `out.close()`; a second call does nothing |
| `GZIPOutputStream.close()` | `finish()`, then `out.close()`; no `flush()`; a second call does nothing |
| `BufferedOutputStream.flush()` | the buffered bytes, then `out.flush()` |
| `GZIPOutputStream.flush()` | `out.flush()` only, unless built with `syncFlush` `true` |
| `GZIPOutputStream.finish()` | the rest of the compressed data and the trailer; no flush, no close |
| `FilterOutputStream.write(byte[], int, int)` | one `write(int)` call on this object per byte |
| `FilterInputStream.read(byte[], int, int)` | `in.read(b, off, len)`, bypassing this object's `read()` |

**JDK collection decorators and `equals` (Java 21):**

| Decorator | `equals` and `hashCode` |
|---|---|
| `unmodifiableList`, `unmodifiableSet`, `unmodifiableMap`, `synchronizedList`, `synchronizedSet`, `synchronizedMap` | forwarded to the wrapped collection |
| `unmodifiableCollection`, `synchronizedCollection` | `Object`'s identity |

## Challenges

1. A `DataOutputStream` writes 4-byte integers into a stack with a `CipherOutputStream` and a
   socket. Where do buffers go, and how many calls reach the cipher and the socket for 100,000
   integers in each order?
2. Write a `CountingInputStream` that extends `FilterInputStream`. Which of its methods must you
   override so that `readAllBytes()` and `transferTo()` are counted, and how do you check you
   found them all?
3. A library interface gains a default method in its next release. Your decorators of it compile
   unchanged. What check in your build would tell you a decorator needs a decision?

## Design Note: Why java.io Stacks Decorators Instead of Offering One Class per Combination

A buffered, compressed, encrypted file stream could be one class, and `java.io` would then need
one for every combination of buffering, compression, encryption and data encoding, over every
source and sink. Decorators make each feature one class that takes any stream, and the
combinations are built at the call site. The price is what this page shows: the caller assembles
the stack and must know which layer holds data, which order keeps the layers cheap, and which
reference to close. An adapter (unit 9.2.1) changes the interface it wraps instead of keeping it;
a proxy (unit 9.2.3) keeps it too, but decides whether and how a call reaches the object behind
it.

## Code Examples

- Implementation: [DecoratorPattern.java](src/main/java/com/github/msorkhpar/claudejavatutor/structuralpatterns/DecoratorPattern.java)
- Tests: [DecoratorPatternTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/structuralpatterns/DecoratorPatternTest.java)
