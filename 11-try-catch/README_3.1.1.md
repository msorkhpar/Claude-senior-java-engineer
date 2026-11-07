# 3.1.1. Handling Exceptions with try/catch

After a front-end release, every web order shipped one item, whatever the customer had typed, and
nothing was logged. The release had renamed a form field from `qty` to `quantity`. The code that
read it:

```java
static int quantityBroad(Map<String, String> form) {
    try {
        return Integer.parseInt(form.get("qty").strip());
    } catch (Exception e) {
        return 1;                                   // "bad input"
    }
}
```

Outputs are from Java 21.

## A catch Clause Catches Its Type and Every Subtype

`quantityNarrow` is the same method with `catch (NumberFormatException e)`:

```text
broad,  qty=3:      3
broad,  qty=three:  1
broad,  quantity=3: 1
narrow, qty=three:  1
narrow, quantity=3: java.lang.NullPointerException: Cannot invoke "String.strip()" because the return value of "java.util.Map.get(Object)" is null
```

`catch (X e)` takes an `X` or any subclass of `X`. The author meant "whatever `parseInt` throws",
but every checked exception and every `RuntimeException` is an `Exception`, so the `NullPointerException` from
the missing field became a valid-looking quantity. The narrow catch lets the bug through, and the
first order fails loudly.

Around a `sleep` or a `Future.get`, `catch (Exception)` catches `InterruptedException`, whose
throw has already cleared the interrupt flag, so code further up never learns the thread was asked
to stop unless the catch restores it (unit 6.1.3). `StackOverflowError` and `OutOfMemoryError`
extend `Error`, not `Exception`, so `catch (Exception)` lets them pass. `catch (Throwable)` takes
them, and the thread runs on with whatever half-finished update the error interrupted.

Catch the type you can handle, and let the rest reach code that reports it.

<!-- practice 1 -->

## A New Exception Without the Cause Loses the Failure It Replaces

Two price-file loaders wrap the `IOException`; the second passes it as the cause:

```java
} catch (IOException e) {
    throw new IllegalStateException("could not load prices");
}
// versus
} catch (IOException e) {
    throw new IllegalStateException("could not load prices from " + file, e);
}
```

```text
java.lang.IllegalStateException: could not load prices
	at Prices.loadWithoutCause(Prices.java:13)
	at Prices.main(Prices.java:28)
---
java.lang.IllegalStateException: could not load prices from prices-2026-11.csv
	at Prices.loadWithCause(Prices.java:21)
	at Prices.main(Prices.java:34)
Caused by: java.nio.file.NoSuchFileException: prices-2026-11.csv
	at java.base/sun.nio.fs.UnixException.translateToIOException(UnixException.java:92)
	at java.base/sun.nio.fs.UnixException.rethrowAsIOException(UnixException.java:106)
	at java.base/sun.nio.fs.UnixException.rethrowAsIOException(UnixException.java:111)
	at java.base/sun.nio.fs.UnixFileSystemProvider.newByteChannel(UnixFileSystemProvider.java:261)
	at java.base/java.nio.file.Files.newByteChannel(Files.java:380)
	at java.base/java.nio.file.Files.newByteChannel(Files.java:432)
	at java.base/java.nio.file.spi.FileSystemProvider.newInputStream(FileSystemProvider.java:420)
	at java.base/java.nio.file.Files.newInputStream(Files.java:160)
	at java.base/java.nio.file.Files.newBufferedReader(Files.java:2910)
	at java.base/java.nio.file.Files.readAllLines(Files.java:3405)
	at java.base/java.nio.file.Files.readAllLines(Files.java:3446)
	at Prices.loadWithCause(Prices.java:19)
	... 1 more
```

Without the cause, the trace ends in our code: which file, and whether it was missing or
unreadable, is gone. `... 1 more` means the
cause's remaining frames are the last frames of the enclosing exception's trace.

Copying `e.getMessage()` into the new message keeps neither type nor trace; an empty catch keeps
nothing. Rethrowing the same object, `throw e;`, keeps everything. A task's exception
reaches `Future.get` as the cause of an `ExecutionException` (unit 6.4.2).

## An Exception Thrown in a catch Block Replaces the One Being Handled

A rates client reads from a primary source and falls back to a mirror:

```java
static String rates(Source primary, Source mirror) throws IOException {
    try {
        return primary.read("eur-usd");
    } catch (IOException e) {
        return mirror.read("eur-usd");              // may throw too
    }
}
```

The primary answers `401 Unauthorized` and the mirror times out. The caller prints what reaches
it:

```text
java.io.IOException: mirror: connect timed out, suppressed: 0, cause: null
```

Java doesn't link a new exception to the one being handled, which only the variable `e` refers
to; when the catch block throws, `e` is dropped. On-call chases a network timeout while
the real fault is an expired key. Attaching the first failure keeps both:

```java
} catch (IOException e) {
    try {
        return mirror.read("eur-usd");
    } catch (IOException fromMirror) {
        fromMirror.addSuppressed(e);
        throw fromMirror;
    }
}
```

Suppressed, not cause: the timeout didn't happen because of the 401.

```text
java.io.IOException: mirror: connect timed out
	at Fallback.lambda$main$1(Fallback.java:30)
	at Fallback.ratesKeepingBoth(Fallback.java:20)
	at Fallback.main(Fallback.java:37)
	Suppressed: java.io.IOException: primary: 401 Unauthorized
		at Fallback.lambda$main$0(Fallback.java:29)
		at Fallback.ratesKeepingBoth(Fallback.java:17)
		... 1 more
```

A throwing `finally` block replaces an exception the same way (unit 3.1.3). Try-with-resources
attaches the other way round: an exception from `close()` is suppressed into the body's
(unit 3.1.4).

<!-- practice 2 -->

## The Stack Trace Is Recorded Where the Exception Is Created, Not Where It Is Thrown

To save allocations, a catalog throws one shared instance for every unknown SKU:

```java
static final IllegalArgumentException UNKNOWN_SKU = new IllegalArgumentException("unknown SKU");

static long price(String sku) {
    if (!sku.startsWith("SKU-")) throw UNKNOWN_SKU;
    return 100;
}
```

A caller, `checkout("BOOK-17")`, calls `price` and prints what it catches:

```text
java.lang.IllegalArgumentException: unknown SKU
	at Shared.<clinit>(Shared.java:4)
```

`Throwable`'s constructor calls `fillInStackTrace()`, which records the creating thread's stack;
`throw` records nothing. `<clinit>` is the static initializer; neither `checkout` nor `price`
appears. Calling `fillInStackTrace()` on the shared object before each throw walks the stack
anyway, and overwrites the trace of an earlier failure still queued for the log. Create a new
exception for each failure.

> **The JIT can drop traces too.** HotSpot's `OmitStackTraceInFastThrow`, on by default, lets
> compiled code throw a preallocated exception for some exceptions the JVM raises itself. In our
> loop, after about 2,800 `NullPointerException`s from one line, the next had `message=null` and
> no frames; with `-XX:-OmitStackTraceInFastThrow` all 100,000 kept their traces.

## Throwing Costs Microseconds, Mostly the Stack Trace; a try That Doesn't Throw Costs Nothing

We timed four parsers on 200,000 inputs, median of 7 rounds after warm-up, at the caller's depth
and 100 frames deeper (the deep rows include the 100 calls to get there):

```text
extra stack depth   0
  valid input, parseInt                6 ns
  bad input, catch NFE               314 ns
  bad input, check first               5 ns
  bad input, no-trace exception       10 ns
extra stack depth 100
  valid input, parseInt               80 ns
  bad input, catch NFE              2676 ns
  bad input, check first              83 ns
  bad input, no-trace exception       92 ns
```

A `try` adds no instruction: `javac` writes an exception table that the JVM reads only when
something is thrown, so valid input costs what checking first does. A throw cost about 0.3 µs, and
over 2 µs with 100 more frames to record, because filling in the trace walks the stack. An
exception built with `writableStackTrace` set to `false` (a protected four-argument constructor)
cost close to checking first.

> **One machine, not JMH.** A hand-written loop: across three runs the deep
> throw ranged from 2.1 to 2.7 µs. The ratios held in every run.

At 100 frames deep, a thousand bad rows a second cost under 3 ms of CPU, and a million would keep
more than two cores busy. Use exceptions for what the caller can't expect, and a return value for
an outcome it can.

<!-- practice 3 -->

<!-- practice 4 -->

## Summary

- **A catch clause catches its type and every subtype.** `catch (Exception)` turns bugs and
  interrupts into defaults.
- **A new exception without the cause loses the failure it replaces.**
- **An exception thrown in a catch block replaces the one being handled.** Attach the first with
  `addSuppressed`.
- **The stack trace is recorded at construction.** A shared exception reports where it was made.
- **A throw costs microseconds, growing with stack depth.** A try that doesn't throw costs nothing.

## Reference

**What a catch clause takes (Java 21):**

| Clause | Takes | Lets through |
|---|---|---|
| `catch (NumberFormatException e)` | it and its subclasses | `NullPointerException`, every checked exception |
| `catch (Exception e)` | every `RuntimeException`, plus checked ones such as `IOException` and `InterruptedException` | `Error`s |
| `catch (Throwable e)` | everything, `StackOverflowError` and `OutOfMemoryError` included | nothing |

## Challenges

1. A batch job must skip bad records but stop on bugs. Where does each `catch` go, and how does
   it report what it skipped?
2. A client library throws only `RuntimeException`, with the error in the message. How do you
   recover from one case?
3. Profile a service that validates with exceptions. Is `fillInStackTrace` in the top ten?

## Design Note: Why the Trace Is Taken at Construction

Recorded when the object is built, the trace travels with the exception: through a `Future`,
inside a cause, into a later log. The price is a stack walk for every exception, read or not. A type
that opts out, through the four-argument constructor or an overridden `fillInStackTrace()`, is cheap
and leaves nothing to debug.

## Code Examples

- Test: [ExceptionHandlingExampleTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/trycatch/ExceptionHandlingExampleTest.java)
- Source: [ExceptionHandlingExample.java](src/main/java/com/github/msorkhpar/claudejavatutor/trycatch/ExceptionHandlingExample.java)