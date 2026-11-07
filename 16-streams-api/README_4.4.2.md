# 4.4.2. Creating and Using Streams

A log indexer counts the `ERROR` lines in every file of a directory, one
`Files.lines(log).filter(...).count()` per file. It passes every test. In production, after a few
hours, it dies with `FileSystemException: ... Too many open files`.

The method that creates a stream decides more than where its elements come from: whether it holds a
file, in what order the elements arrive, what type they have, where the sequence ends and which
arithmetic you get. Seven rules, one per heading. The pipeline itself, lazy and single-use, is
unit 4.4.1.

## A Stream Over a File Holds It Open Until close(), Not Until the Terminal Operation

We count the errors in 1,000 small log files, with and without try-with-resources, and count the
process's open descriptors in `/proc/self/fd` (Linux):

```java
static long errorsLeaky(Path log) throws IOException {
    return Files.lines(log).filter(l -> l.contains("ERROR")).count();
}

static long errorsClosed(Path log) throws IOException {
    try (Stream<String> lines = Files.lines(log)) {
        return lines.filter(l -> l.contains("ERROR")).count();
    }
}
```

```text
open before:          8
with try, 1000 errors: open after 8
without, 1000 errors:  open after 981
```

`count()` read every line, and the file stayed open. A terminal operation finishes the pipeline;
only `close()` releases what the source holds, and javac compiled `errorsLeaky` without a warning.
`Files.lines`, `Files.list` and `Files.walk` all keep descriptors open until closed. The leaked
ones do close eventually: 981 rather than 1,008 because a collection ran during the loop, and in a
second run a `System.gc()` after the loop took the count from 1,008 back to 8. So the garbage
collector decides when your files close. With `ulimit -n 512`, the same loop failed partway:

```text
Exception in thread "main" java.nio.file.FileSystemException: ./logs9662185227350139375/app-881.log: Too many open files
```

`onClose` handlers also run only on `close()`:

```java
Stream<String> s = Stream.of("a", "b").onClose(() -> System.out.println("  onClose ran"));
System.out.println("count = " + s.count());
System.out.println("terminal operation done");
s.close();
```

```text
count = 2
terminal operation done
  onClose ran
```

`flatMap` closes each stream it maps to once that stream's elements have passed, so a `flatMap`
that opens one file per element doesn't leak the inner streams; the outer stream is still yours to
close. `BufferedReader.lines()` is the opposite case: closing the stream leaves the reader open, so
you close the reader. A read error inside such a stream arrives as `UncheckedIOException` from the
terminal operation (unit 3.2.4).

<!-- practice 1 -->

## A Collection's Stream Has the Collection's Order, and a HashSet Has None

Five codes, added in the same order to four collections:

```java
List<String> codes = List.of("delta", "alpha", "echo", "bravo", "charlie");
Set<String> hash = new HashSet<>(codes);
System.out.println("List          " + codes.stream().toList());
System.out.println("HashSet       " + hash.stream().toList() + "  findFirst=" + hash.stream().findFirst().orElseThrow());
System.out.println("LinkedHashSet " + new LinkedHashSet<>(codes).stream().toList());
System.out.println("TreeSet       " + new TreeSet<>(codes).stream().toList());
for (int i = 0; i < 20; i++) hash.add("x" + i);
hash.removeIf(s -> s.startsWith("x"));
System.out.println("HashSet, same 5 after growing and shrinking " + hash.stream().toList()
        + "  findFirst=" + hash.stream().findFirst().orElseThrow());
```

```text
List          [delta, alpha, echo, bravo, charlie]
HashSet       [bravo, alpha, delta, echo, charlie]  findFirst=bravo
LinkedHashSet [delta, alpha, echo, bravo, charlie]
TreeSet       [alpha, bravo, charlie, delta, echo]
HashSet, same 5 after growing and shrinking [delta, echo, bravo, alpha, charlie]  findFirst=delta
```

A `HashSet` has no encounter order: its spliterator doesn't report `ORDERED`, and what you see is
its bucket layout. The set held the same five codes both times, but resizing for 20 temporary
entries moved them, and `findFirst()` changed from `bravo` to `delta`. A `Set.of` set changes
order between JVM runs: two runs of one program printed `[echo, charlie, delta, bravo, alpha]` and
`[alpha, bravo, delta, charlie, echo]`. A report that takes "the first" element of a set, or a test
that compares one to a list, works until the set resizes, the JDK changes or, for `Set.of`, the JVM
restarts.

A `Map` isn't a `Collection`: stream one of its views, `entrySet()`, `keySet()` or `values()`.
They have the map's order: none for a `HashMap`, key order for a `TreeMap`, insertion order for a
`LinkedHashMap`.

## Stream.of Streams Its Arguments: an int[] Is One Element, and So Is null

`Stream` has `of(T t)` beside `of(T... values)`. An `int[]` isn't a `T[]`, so `Stream.of(ports)`
picks `of(T t)` with `T = int[]`: a stream of one array.

```java
int[] ports = {8080, 8443, 9090, 9443};
System.out.println("Stream.of(ports).count()          = " + Stream.of(ports).count());
System.out.println("Arrays.stream(ports).count()      = " + Arrays.stream(ports).count());
System.out.println("Arrays.stream(ports, 1, 3)        = " + Arrays.toString(Arrays.stream(ports, 1, 3).toArray()));
System.out.println("Stream.of(ports).contains 8080?   = " + Stream.of(ports).anyMatch(p -> p.equals(8080)));

String nickname = null;
System.out.println("Stream.of(nickname).count()       = " + Stream.of(nickname).count());
System.out.println("Stream.ofNullable(nickname).count() = " + Stream.ofNullable(nickname).count());
```

```text
Stream.of(ports).count()          = 1
Arrays.stream(ports).count()      = 4
Arrays.stream(ports, 1, 3)        = [8443, 9090]
Stream.of(ports).contains 8080?   = false
Stream.of(nickname).count()       = 1
Stream.ofNullable(nickname).count() = 0
```

The lookup compiles because `p` is the whole `int[]`, and an array never equals an `Integer`.
`Arrays.stream(int[])` and `IntStream.of(int...)` give an `IntStream` of the values, and
`Arrays.stream(array, from, to)` a slice with `to` excluded. A `String[] hosts` has no such trap:
`Stream.of(hosts)` streams its elements.

A `null` variable is one argument too, so `Stream.of(nickname)` holds one `null`, which fails later
in whatever calls a method on it. `Stream.ofNullable` (Java 9) is empty for `null`. With a bare
`Stream.of(null)`, javac warns, the `null` binds to the array parameter, and `Stream.of` itself
throws `NullPointerException`.

<!-- practice 2 -->

## A Generated Sequence Ends Only Where Its Factory Says

`Stream.iterate(seed, next)` never ends; the Java 9 form `iterate(seed, hasNext, next)` stops like
a `for` loop. A filter doesn't end a stream, it only drops elements:

```java
System.out.println("3-arg: " + Stream.iterate(1, n -> n < 1000, n -> n * 2).toList());
// the 2-arg form with a filter: the first 14 of the elements that pass it
System.out.println("2-arg + filter: " + Stream.iterate(1, n -> n * 2).filter(n -> n < 1000).limit(14).toList());
```

```text
3-arg: [1, 2, 4, 8, 16, 32, 64, 128, 256, 512]
2-arg + filter: [1, 2, 4, 8, 16, 32, 64, 128, 256, 512, -2147483648, 0, 0, 0]
```

Without `limit(14)` the second line never prints. The doubling overflows to `-2147483648`, then to
`0`, and `0 < 1000` passes forever. Ending at the first element that fails a test is `takeWhile`
(unit 4.2.4); `Stream.generate` is endless in the same way (unit 4.2.2).

`IntStream.range(1, 5)` is `1, 2, 3, 4`, and `rangeClosed(1, 5)` adds the `5`. `range(5, 5)` and
`range(5, 1)` are empty, not errors; `rangeClosed` is empty only when the end is below the start.

## Only Primitive Streams Have sum() and average(), and IntStream.sum() Overflows

`Stream<Integer>` has no `sum()`:

```text
NoSum.java:6: error: cannot find symbol
        int total = latencies.stream().sum();
                                      ^
  symbol:   method sum()
  location: interface Stream<Integer>
```

`sum()`, `average()` and `summaryStatistics()` exist on `IntStream`, `LongStream` and
`DoubleStream`, where the elements are known to be numbers. `mapToInt` gets there; `boxed()` and
`mapToObj` come back:

```java
List<Integer> latencies = List.of(120, 85, 240, 95);
int total = latencies.stream().mapToInt(Integer::intValue).sum();
OptionalDouble mean = latencies.stream().mapToInt(Integer::intValue).average();
System.out.println("sum " + total + ", average " + mean);
OptionalDouble none = IntStream.empty().average();
System.out.println("empty: sum " + IntStream.empty().sum() + ", average " + none);
int[] uploads = {1_500_000_000, 1_500_000_000};   // bytes
System.out.println("IntStream.sum()        = " + IntStream.of(uploads).sum());
System.out.println("asLongStream().sum()   = " + IntStream.of(uploads).asLongStream().sum());
System.out.println("mapToObj " + IntStream.rangeClosed(1, 3).mapToObj(i -> "node-" + i).toList());
```

```text
sum 540, average OptionalDouble[135.0]
empty: sum 0, average OptionalDouble.empty
IntStream.sum()        = -1294967296
asLongStream().sum()   = 3000000000
mapToObj [node-1, node-2, node-3]
```

The sum of nothing is 0, but the mean of nothing doesn't exist, so `average()` returns an
`OptionalDouble`. `getAsDouble()` on the empty one throws `NoSuchElementException`, and
`orElse(0)` shows an idle service as answering in 0 ms. `IntStream.sum()` returns an `int` and
wraps silently: two 1.5 GB uploads total -1.3 GB. `summaryStatistics().getSum()` and
`asLongStream().sum()` are `long`.

<!-- practice 3 -->

## String.chars() Streams UTF-16 Units, Not Characters

A post may be at most 280 characters long. How long is `"Ship it 🚀"`?

```java
String status = "Ship it 🚀";   // "Ship it " and a rocket emoji
System.out.println("chars().count()        " + status.chars().count());
System.out.println("codePoints().count()   " + status.codePoints().count());
String cut = status.chars().limit(9)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
System.out.println("first 9 chars:      " + cut + "  last unit is surrogate? " + Character.isSurrogate(cut.charAt(cut.length() - 1)));
```

```text
chars().count()        10
codePoints().count()   9
first 9 chars:      Ship it ?  last unit is surrogate? true
```

`chars()` streams a `String`'s 16-bit UTF-16 units. The rocket, U+1F680, doesn't
fit in one, so it is stored as the pair `U+D83D U+DE80`: `chars()` counts it twice, and a cut after
nine units keeps half of it, which prints as `?`. `codePoints()` streams one element per Unicode
code point. A flag or an emoji with a skin tone is several code points; counting the symbols a
reader sees takes `java.text.BreakIterator`.

`Pattern.splitAsStream` streams the pieces between matches. Its argument is a regular expression,
so `Pattern.compile(".")` matches every character and leaves no pieces, while
`Pattern.compile(Pattern.quote("."))` matches a dot. A delimiter at the start gives an empty first
piece, and trailing empty pieces are dropped:

```java
Pattern sep = Pattern.compile("\\s*,\\s*");
System.out.println("splitAsStream: " + sep.splitAsStream(", red ,green,, blue,").map(s -> "[" + s + "]").toList());
```

```text
splitAsStream: [[], [red], [green], [], [blue]]
```

<!-- practice 4 -->

## Builder and concat Assemble a Stream; Repeated concat Overflows the Stack

`Stream.builder()` collects elements until `build()`; after that, `add` throws
`IllegalStateException`. `Stream.concat(a, b)` joins two streams lazily, and each `concat` in a
loop wraps the previous result:

```java
for (int pages : new int[] {1_000, 10_000, 20_000, 50_000, 100_000}) {
    String step = "concat";
    try {
        Stream<String> all = Stream.empty();
        for (int p = 0; p < pages; p++) all = Stream.concat(all, Stream.of("page-" + p));
        step = "toList";
        System.out.println(pages + " concats: toList size " + all.toList().size());
    } catch (StackOverflowError e) {
        System.out.println(pages + " concats: StackOverflowError in " + step);
    }
}
List<List<String>> pagesOfRows = IntStream.range(0, 100_000).mapToObj(p -> List.of("page-" + p)).toList();
System.out.println("flatMap over 100000 pages: toList size " + pagesOfRows.stream().flatMap(List::stream).toList().size());
```

```text
1000 concats: toList size 1000
10000 concats: toList size 10000
20000 concats: toList size 20000
50000 concats: StackOverflowError in concat
100000 concats: StackOverflowError in concat
flatMap over 100000 pages: toList size 100000
```

With the default stack, 50,000 overflowed inside `concat` itself, before any element was read: the stack trace is one `ConcatSpliterator.characteristics` calling the next. The
`concat` javadoc warns that deep concatenation can end in `StackOverflowError`. For more than a few
streams, put them in a list and `flatMap` it, as the last line did.

## Summary

- **A stream over a file holds it open until `close()`.** Wrap `Files.lines`, `Files.list` and
  `Files.walk` in try-with-resources; the terminal operation doesn't close, and `onClose` handlers
  run only on `close()`.
- **A collection's stream has the collection's order.** A `HashSet` or `Set.of` has none, and the
  order you see changes when the set resizes, or for `Set.of` when the JVM restarts.
- **`Stream.of` streams its arguments.** An `int[]` is one element and a `null` variable is one
  `null`; use `Arrays.stream` and `Stream.ofNullable`.
- **A generated sequence ends only where its factory says.** The two-argument `iterate` is endless,
  a `filter` doesn't stop it, and `range` excludes its end.
- **Only primitive streams have `sum()` and `average()`.** `average()` is empty for no elements, and
  `IntStream.sum()` overflows silently.
- **`String.chars()` streams UTF-16 units.** Count and cut with `codePoints()`.
- **Repeated `concat` overflows the stack.** Use `flatMap` over a list of streams.

## Reference

| Source | Gives | Order | Ends | Close it? |
|---|---|---|---|---|
| `collection.stream()` | `Stream<T>` | the collection's; none for `HashSet`, `Set.of` | yes | no |
| `map.entrySet().stream()` | `Stream<Map.Entry<K,V>>` | the map's | yes | no |
| `Arrays.stream(array[, from, to])` | `Stream<T>`, or `IntStream`/`LongStream`/`DoubleStream` | index order | yes | no |
| `Stream.of(a, b, ...)`, `Stream.ofNullable(x)` | `Stream<T>`; `ofNullable(null)` is empty | argument order | yes | no |
| `Stream.iterate(seed, next)` | `Stream<T>` | iteration order | never | no |
| `Stream.generate(supplier)` | `Stream<T>` | unordered | never | no |
| `Stream.iterate(seed, hasNext, next)` | `Stream<T>` | iteration order | when `hasNext` is false | no |
| `IntStream.range(a, b)`, `rangeClosed(a, b)` | `IntStream` | ascending | `b` excluded / included | no |
| `str.chars()`, `str.codePoints()` | `IntStream` of UTF-16 units / code points | string order | yes | no |
| `pattern.splitAsStream(text)` | `Stream<String>` | text order | yes | no |
| `reader.lines()` | `Stream<String>` | line order | yes | close the reader |
| `Files.lines(path)` | `Stream<String>` | line order | yes | yes |
| `Files.list(dir)`, `Files.walk(dir)` | `Stream<Path>` | not specified | yes | yes |
| `Stream.builder()` ... `build()` | `Stream<T>` | `add` order | yes | no |
| `Stream.concat(a, b)` | `Stream<T>` | ordered if both are | yes | closing it closes both |

## Challenges

1. A service streams a directory tree with `Files.walk` and returns the stream to its caller. Who
   closes it, and how do you make that hard to forget?
2. A test asserts that `set.stream().findFirst()` equals a value, and it passes on your machine.
   What change would make it fail, and what would you assert instead?
3. Measure a sum over a `List<Integer>` of ten million elements with `mapToInt(...).sum()` and with
   `reduce(0, Integer::sum)`. Where does the difference come from?

## Design Note: Every Stream Is AutoCloseable, Few Need Closing

`BaseStream` extends `AutoCloseable`, so every stream has `close()`. The `Stream` javadoc says most streams
don't need it, since collections, arrays and generating functions hold nothing to release. Putting `close()` on every stream lets a stream over a
file travel as an ordinary `Stream<T>`, and lets code that consumes a stream ignore where it came
from. The cost is that the type no longer tells you whether closing matters, so a convention has
to: whoever
opens a stream over a resource closes it with try-with-resources, and a method that returns one
says so in its javadoc.

## Code Examples

- Source: [StreamCreation.java](src/main/java/com/github/msorkhpar/claudejavatutor/streamsapi/StreamCreation.java)
- Test: [StreamCreationTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/streamsapi/StreamCreationTest.java)
