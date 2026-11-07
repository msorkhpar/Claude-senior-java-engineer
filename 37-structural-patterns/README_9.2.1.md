# 9.2.1. Adapter Pattern

The invoice service gets each order from a legacy pricing library as two arrays matched by index,
`String[] skus` and `Integer[] cents`. A new footer shows the most expensive line:

```java
List<Integer> prices = Arrays.asList(cents);
prices.sort(Comparator.reverseOrder());
System.out.println("highest line: " + prices.get(0));
```

Since the release, customers see SKU A-100 billed at 72.00. It costs 19.99, nobody changed a
price, and the footer's number is right. Before reading on, work out what moved the prices.

An adapter makes an object you already have usable through an interface its class doesn't
implement.

## Arrays.asList Adapts an Array Without Copying It, So Sorting the List Reorders the Array

The invoice lines after the footer, then the same code copying first with
`new ArrayList<>(Arrays.asList(cents))`:

```text
highest line: 7200
A-100  7200
B-200  1999
C-300  1250
D-400  450
---
highest line: 7200
A-100  1999
B-200  450
C-300  7200
D-400  1250
```

`Arrays.asList` returns a `List` that reads and writes the array you passed. The sort rearranged
`cents`, so every price now sits beside another SKU. The javadoc
calls it "a fixed-size list backed by the specified array", and the backing works both ways:

```java
String[] codes = {"EUR", "USD", "GBP"};
List<String> list = Arrays.asList(codes);
list.set(0, "CHF");                       // through the list
codes[2] = "JPY";                         // through the array
System.out.println("array " + Arrays.toString(codes) + "  list " + list);

int[] ids = {7, 3, 9};
var wrong = Arrays.asList(ids);           // List<int[]>, one element
System.out.println("asList(int[]): size " + wrong.size() + ", contains(7) " + wrong.contains(7));
```

```text
array [CHF, USD, JPY]  list [CHF, USD, JPY]
asList(int[]): size 1, contains(7) false
```

That is the adapter: the same data behind a second interface, with no copy, which is why a caller
who wanted a list of their own got someone else's array. The view keeps the array's size, so `add` and `remove` throw (unit 8.1.3).

The last line is the other trap. `asList(T...)` adapts arrays of objects only. Given an `int[]`, `T`
becomes `int[]` and you get a one-element list holding the array. It compiles, and `contains(7)`
answers `false`. The JDK has no `List<Integer>` view of an `int[]`.

Copy when the receiver must not see your later changes, or you must not see its changes:
`new ArrayList<>(...)` if it may change the list, `List.copyOf(...)` if it may not.

<!-- practice 1 -->

## InputStreamReader Adapts Bytes to Chars Through a Charset, and the One-Argument Constructor Takes the JVM's Default

A parser that takes a `Reader` can't read a socket's or a file's `InputStream`: one deals in bytes,
the other in characters. `InputStreamReader` sits between them, and the charset is the table it
translates with. One string's UTF-8 bytes, read four ways. `drain` reads a `Reader` to the end,
`perRead` does `new String(buf, 0, n, UTF_8)` for each `read()`, and `trickle` wraps the bytes in a
stream that returns at most 3 bytes per read, as a socket may:

```java
byte[] utf8 = "Zürich → Genève".getBytes(StandardCharsets.UTF_8);
System.out.println("default charset: " + java.nio.charset.Charset.defaultCharset());
System.out.println("no charset:    " + drain(new InputStreamReader(new ByteArrayInputStream(utf8))));
System.out.println("UTF_8:         " + drain(new InputStreamReader(new ByteArrayInputStream(utf8), StandardCharsets.UTF_8)));
System.out.println("per read:      " + perRead(trickle(utf8)));
System.out.println("reader, trickle: " + drain(new InputStreamReader(trickle(utf8), StandardCharsets.UTF_8)));
```

```text
$ java Decode
default charset: UTF-8
no charset:    Zürich → Genève
UTF_8:         Zürich → Genève
per read:      Zürich ��� Genève
reader, trickle: Zürich → Genève
$ java -Dfile.encoding=windows-1252 Decode
default charset: windows-1252
no charset:    ZÃ¼rich â†’ GenÃ¨ve
UTF_8:         Zürich → Genève
per read:      Zürich ��� Genève
reader, trickle: Zürich → Genève
```

The constructor without a charset uses the JVM's default: UTF-8 on Java 18 and later unless the
JVM is started with another `file.encoding` (unit 1.4.1 has the
history). The same jar reads the same bytes correctly on a laptop and turns `ü` into `Ã¼` on a
server whose start script sets `windows-1252`. Single-byte charsets like that one give almost every
byte a character, and Latin-1 gives all 256 one, so each byte of `ü` became a wrong character
rather than `�`. Nothing throws, so the broken names are stored. Pass
the charset the data is in.

The `per read` line is why decoding needs an adapter that keeps state, not a `new String` per
buffer. The arrow is three bytes in UTF-8, the reads cut it apart, and each piece decoded alone is
malformed and becomes `�`. `InputStreamReader` holds the bytes of an unfinished character until the
next read completes it, so it decoded the same trickle correctly.

When the bytes aren't in the charset you named, the adapter doesn't fail by default. Latin-1 bytes
read as UTF-8, once through a `Charset` and once through `UTF_8.newDecoder()`:

```text
Charset:        Gen�ve
CharsetDecoder: java.nio.charset.MalformedInputException: Input length = 1
```

The `Charset` constructor replaced the malformed byte with `U+FFFD`. A decoder from `newDecoder()`
reports it, so where a feed in the wrong charset must be rejected, pass a decoder.

<!-- practice 2 -->

## A Callback Adapter Must Complete the Future on Every Path the Legacy Call Can Take

A legacy quote client answers through a listener, on its own I/O thread, about 50 ms after
`request` is called. It checks its argument on the caller's thread first:

```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

interface QuoteListener {
    void onQuote(String symbol, long cents);
    void onError(String symbol, int code);
}

final class LegacyQuoteClient {
    private final ExecutorService io = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "quote-io");
        t.setDaemon(true);
        return t;
    });

    void request(String symbol, QuoteListener listener) {
        if (symbol.isBlank()) throw new IllegalArgumentException("blank symbol");
        io.execute(() -> {
            try { Thread.sleep(50); } catch (InterruptedException e) { return; }
            if (symbol.equals("ZZZZ")) listener.onError(symbol, 404);
            else listener.onQuote(symbol, 12_345);
        });
    }
}
```

The new code wants a `CompletableFuture<Long>`. The first adapter completes the future when a
quote arrives:

```java
static CompletableFuture<Long> quoteV1(LegacyQuoteClient client, String symbol) {
    CompletableFuture<Long> f = new CompletableFuture<>();
    client.request(symbol, new QuoteListener() {
        @Override public void onQuote(String s, long cents) { f.complete(cents); }
        @Override public void onError(String s, int code) { System.out.println("  quote failed: " + code); }
    });
    return f;
}
```

The caller formats the price and shows `n/a` on any failure:
`.thenApply(c -> c / 100 + "." + c % 100).exceptionally(e -> "n/a")`. This run adds
`.orTimeout(1, SECONDS)` after that, so a hang shows up as a timeout, and prints whatever the call
throws. Three symbols through `quoteV1`, then the same three through the fixed adapter below:

```text
v1     'ACME' 123.45
  quote failed: 404
v1     'ZZZZ' java.util.concurrent.CompletionException: java.util.concurrent.TimeoutException
v1     ' '    java.lang.IllegalArgumentException: blank symbol
fixed  'ACME' 123.45
fixed  'ZZZZ' n/a
fixed  ' '    n/a
```

A `CompletableFuture` made with `new` completes only when some code calls `complete` or
`completeExceptionally`. The unknown symbol reached `onError`, which logged and returned, so
nothing completed the future. Without the timeout, `join()` would block that request's thread for
good. The blank symbol failed another
way: `request` threw before any callback existed, and the exception left `quoteV1` itself, past
the `exceptionally` the caller wrote for failures.

The fixed adapter maps each way the legacy call can end to the future:

```java
static CompletableFuture<Long> quote(LegacyQuoteClient client, String symbol) {
    CompletableFuture<Long> f = new CompletableFuture<>();
    try {
        client.request(symbol, new QuoteListener() {
            @Override public void onQuote(String s, long cents) { f.complete(cents); }
            @Override public void onError(String s, int code) {
                f.completeExceptionally(new IllegalStateException(s + ": error " + code));
            }
        });
    } catch (RuntimeException e) {
        f.completeExceptionally(e);
    }
    return f;
}
```

### Continuations Run on Whichever Thread Completes the Future

`thenApply` itself returns a new stage at once and never blocks the thread that calls it. The
question is where its function runs. One stage attached while the quote is in flight, one after
`join()` has returned:

```java
CompletableFuture<Long> pending = quote(client, "ACME");
CompletableFuture<String> early = pending.thenApply(c -> Thread.currentThread().getName());
pending.join();
CompletableFuture<String> late = pending.thenApply(c -> Thread.currentThread().getName());
```

```text
attached before the answer runs on: quote-io
attached after the answer runs on:  main
```

`complete` runs the stages already waiting on its own thread, here the client's only I/O thread,
so a slow stage holds up every other quote. The adapter can't know which stages will be slow, so a
caller with real work uses `thenApplyAsync(fn, executor)`.

<!-- practice 3 -->

## Java Has Only Object Adapters When the Target Is a Class

The textbook class adapter extends the adaptee and implements the target, so it inherits the code
it translates to. Trying that for bytes and characters:

```java
import java.io.ByteArrayInputStream;
import java.io.Reader;

/** A class adapter: is-a ByteArrayInputStream, and should be usable as a Reader. */
class BytesAsReader extends ByteArrayInputStream implements Reader {
    BytesAsReader(byte[] bytes) { super(bytes); }
}
```

```text
BytesAsReader.java:5: error: interface expected here
class BytesAsReader extends ByteArrayInputStream implements Reader {
                                                            ^
1 error
```

`Reader` is an abstract class, and a Java class extends only one. When the target is a class, the
adapter extends the target and holds the adaptee in a field, an object adapter. That is how
`InputStreamReader` is built, and its `trickle` run above is that adapter working. When the target is an interface, a class adapter compiles but adapts
only the instances it constructs itself, never `System.in` or the stream a socket hands you. It
also exposes the adaptee's public methods, so callers can change its state behind the
translation. An object adapter forwards to the object it holds, as the forwarding wrapper of unit
8.4.1 does, and for a single-method target a method reference is the whole adapter (unit 4.3.1).

The other wrappers in this module keep the interface they wrap: a decorator adds behaviour behind
it (unit 9.2.2), and a proxy controls access to its calls (unit 9.2.3).

<!-- practice 4 -->

## Summary

- **`Arrays.asList` is a view.** Sorting or setting through it changes the array, and `asList` of
  an `int[]` is a one-element `List<int[]>`. Copy when the receiver needs a list of its own.
- **`InputStreamReader` decodes with a charset.** Without one it takes the JVM's default. It
  keeps a split character between reads, and only a `CharsetDecoder` makes it reject bad bytes.
- **A callback adapter completes its future on every path.** A forgotten error callback leaves
  `join()` waiting forever, and a synchronous throw must become a failed future. Stages attached
  early run on the legacy client's thread, so slow ones go to an executor.
- **When the target is a class, Java allows only an object adapter**, and for an interface target
  it is still the only kind that adapts an object you were given.

## Reference

**Adapters in the JDK (Java 21):**

| Call | Adapts | Behaviour |
|---|---|---|
| `Arrays.asList(T...)` | object array to `List` | view: `set` and `sort` reach the array; `add`, `remove` throw |
| `new InputStreamReader(in)` | `InputStream` to `Reader` | default charset |
| `new InputStreamReader(in, Charset)` | same | malformed input replaced with `U+FFFD` |
| `new InputStreamReader(in, CharsetDecoder)` | same | the decoder's actions; `newDecoder()` reports |
| `new OutputStreamWriter(out, Charset)` | `OutputStream` to `Writer` | unmappable characters replaced |
| `Enumeration.asIterator()` (Java 9) | `Enumeration` to `Iterator` | view |
| `Collections.list(Enumeration)` | `Enumeration` to `ArrayList` | a copy, not a view |
| `Executors.callable(Runnable, T)` | `Runnable` to `Callable<T>` | returns the given result |

**Where a `CompletableFuture` stage runs** (the javadoc lets a non-async stage run on the
completing thread or on any caller of a completion method; the first two rows are this page's run):

| Stage | Ran on |
|---|---|
| `thenApply` attached before completion | the thread that called `complete` |
| `thenApply` attached after completion | the thread that attached it |
| `thenApplyAsync(fn)` | `ForkJoinPool.commonPool()`; a new thread per task if its parallelism is below 2 |
| `thenApplyAsync(fn, executor)` | the given executor |

## Challenges

1. A method returns `Arrays.asList(internalArray)` so that callers can't add to it. What can a
   caller still change? Choose between `List.copyOf` and `Collections.unmodifiableList` for the
   fix: which one copies on every call, and which one still shows later changes to the array?
2. The legacy client calls `onError` and then, after an internal retry, `onQuote` for the same
   request. What does the fixed adapter's caller see, and is that the answer you want?
3. A caller cancels the future the adapter returned, and the legacy request keeps running. What
   would the adapter need from the legacy API to stop it, and what can it do if there is nothing?

## Design Note: Why Arrays.asList Is a View

The javadoc calls `asList` a "bridge between array-based and collection-based APIs", the
counterpart of `Collection.toArray()`. A view makes the bridge cost one small object whatever the
array's size, and lets list algorithms such as `Collections.sort` or `Collections.reverse` work on
an array in place. A copy would have been safe to hand out and useless for that second job.

The price is aliasing that no type shows: a `List<Integer>` parameter doesn't say whether someone's
array sits behind it. `List.of`, added in Java 9, took the other side: it copies the array and
refuses nulls and every change.

## Code Examples

- Implementation: [AdapterPattern.java](src/main/java/com/github/msorkhpar/claudejavatutor/structuralpatterns/AdapterPattern.java)
- Tests: [AdapterPatternTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/structuralpatterns/AdapterPatternTest.java)
