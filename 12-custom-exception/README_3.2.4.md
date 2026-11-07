# 3.2.4 Checked vs. Unchecked Exceptions

Release 2.0 of the rates library added `throws RatesUnavailableException` to `Rates.fetch`, and
the exception is checked. Every team that rebuilt against it got a compile error at each call. The
reporting service wasn't rebuilt: the new jar went into its classpath, and that night the report
died with a `RatesUnavailableException` that got past a `catch (RuntimeException e)`
written to log any failure and carry on. In the service's compiled code, `fetch` declares nothing.

Both outcomes have one cause: only the compiler enforces checked exceptions.

## A Throwable Is Checked Unless It Is a RuntimeException or an Error

Each class is thrown with no `throws` clause:

```java
public class Kinds {
    static class Abort extends Throwable {}                      // straight from Throwable
    static class QuotaExceeded extends Exception {}
    static class StaleCache extends RuntimeException {}
    static class ShardLost extends Error {}

    static void abort()  { throw new Abort(); }
    static void quota()  { throw new QuotaExceeded(); }
    static void stale()  { throw new StaleCache(); }
    static void shard()  { throw new ShardLost(); }
}
```

```text
Kinds.java:7: error: unreported exception Abort; must be caught or declared to be thrown
    static void abort()  { throw new Abort(); }
                           ^
Kinds.java:8: error: unreported exception QuotaExceeded; must be caught or declared to be thrown
    static void quota()  { throw new QuotaExceeded(); }
                           ^
2 errors
```

JLS 11.1.1 defines checked by exclusion: every `Throwable` except `RuntimeException`, `Error`
and their subclasses. "Checked means it extends `Exception`" misses `Abort`, and so does a
`catch (Exception e)` meant to catch everything. In `CatchAll`, whose `Abort` takes a message:

```java
static void step() throws Abort { throw new Abort("workflow aborted by operator"); }
// ...
try {
    try {
        step();
    } catch (Exception e) {                  // "catch everything"
        System.out.println("handled: " + e);
    }
} catch (Throwable t) {
    System.out.println("escaped catch (Exception): " + t);
}
```

```text
escaped catch (Exception): CatchAll$Abort: workflow aborted by operator
```

`Error` and `RuntimeException` are unchecked for different reasons. The `Error` javadoc says it
"indicates serious problems that a reasonable application should not try to catch", such as
running out of memory, so no method should have to declare one. A `RuntimeException` usually means
a bug, which the caller fixes rather than handles. Unit 3.2.1 shows how a custom exception's
superclass decides which kind it is.

## Adding a Checked Exception Breaks Every Caller's Source; Adding an Unchecked One Breaks None

The reporting service calls the library like this, compiled against 1.x:

```java
import rates.Rates;

public class NightlyReport {
    public static void main(String[] args) {
        for (String pair : new String[] {"EUR/USD", "GBP/USD"}) {
            try {
                System.out.println(pair + " " + Rates.fetch(pair));
            } catch (RuntimeException e) {               // log any failure, go on with the next pair
                System.out.println(pair + " skipped: " + e.getMessage());
            }
        }
        System.out.println("report written");
    }
}
```

Recompiled against 2.0, where `RatesUnavailableException extends Exception`:

```text
app/NightlyReport.java:7: error: unreported exception RatesUnavailableException; must be caught or declared to be thrown
                System.out.println(pair + " " + Rates.fetch(pair));
                                                           ^
1 error
```

Against a variant where the same class extends `RuntimeException`, it compiles and, with the feed
down, prints:

```text
EUR/USD skipped: feed down, no rate for EUR/USD
GBP/USD skipped: feed down, no rate for GBP/USD
report written
```

Adding a checked exception forces each call site to catch it or declare it, and declaring it hands
the same choice to their callers: a breaking change for every user of the library. Taking it out again breaks them too: a `catch (RatesUnavailableException e)` around a `fetch` that
no longer declares it fails with "is never thrown in body of corresponding try statement" (unit
3.1.2).

An unchecked addition compiles without a word to callers. This service handled it only because
it already caught `RuntimeException`.

On an interface, adding `throws IOException` to a method breaks code that calls through the
interface, but not the implementations, since an implementation may throw less than the interface
declares. The reverse is the error: an implementation can't add a checked exception that the
interface method lacks.

```java
import java.io.IOException;

public class Sources {
    interface Source { String read(); }

    static class FileSource implements Source {
        public String read() throws IOException { return "x"; }
    }
}
```

```text
Sources.java:7: error: read() in FileSource cannot implement read() in Source
        public String read() throws IOException { return "x"; }
                      ^
  overridden method does not throw IOException
1 error
```

<!-- practice 1 -->

## Catch-or-Declare Is a Compiler Rule; at Run Time Any Method Can Throw a Checked Exception

The `NightlyReport.class` compiled against 1.x, run with the 2.0 jar:

```text
Exception in thread "main" rates.RatesUnavailableException: feed down, no rate for EUR/USD
	at rates.Rates.fetch(Rates.java:5)
	at NightlyReport.main(NightlyReport.java:7)
```

The call linked, and a checked exception left `main`, which declares none. The JVM links a call by
name and descriptor, and the descriptor holds no exceptions. From `javap -v` on 2.0's `Rates`:

```text
  public static double fetch(java.lang.String) throws rates.RatesUnavailableException;
    descriptor: (Ljava/lang/String;)D
    Exceptions:
      throws rates.RatesUnavailableException
```

The `throws` clause sits in an `Exceptions` attribute that javac reads and the JVM doesn't check.
JLS 13.4.21: changing a `throws` clause doesn't break compatibility with existing binaries. At run time, checked and unchecked exceptions are thrown, caught and cost the same.

The service caught `RuntimeException`, which this exception isn't. Recompiling against 2.0 would
have forced it to catch or declare. Without recompiling, a catch for a type the 1.x `fetch`
doesn't declare doesn't compile (unit 3.1.2), so the clause has to be `catch (Exception e)` or
`catch (Throwable t)`.

`Class.newInstance()` (deprecated since Java 9) also passes on whatever the constructor throws, checked or not; its replacement,
`getDeclaredConstructor().newInstance()`, wraps it:

```java
import java.io.IOException;

public class Plugin {
    public Plugin() throws IOException {
        throw new IOException("plugin.properties not found");
    }
}
```

```java
Plugin.class.newInstance();                            // declares InstantiationException, IllegalAccessException
Plugin.class.getDeclaredConstructor().newInstance();   // its checked exceptions are ReflectiveOperationExceptions
```

```text
Class.newInstance let through: java.io.IOException: plugin.properties not found
Constructor.newInstance threw: java.lang.reflect.InvocationTargetException, cause java.io.IOException: plugin.properties not found
```

A sneaky throw through a generic method does the same (unit 3.2.2). Code at the top of a thread
that must survive any exception its callees throw, such as a scheduler loop or a plugin host,
catches `Exception`, not `RuntimeException`.

## Streams Can't Throw IOException, So Files.lines Throws UncheckedIOException from the Terminal Operation

A log scanner counts `ERROR` lines per file and skips a file it can't read:

```java
static long countErrors(Path log) {
    try (Stream<String> lines = Files.lines(log)) {
        return lines.filter(l -> l.contains("ERROR")).count();
    } catch (IOException e) {
        System.out.println("  skipped " + log.getFileName() + ": " + e);
        return 0;
    }
}
```

Of three logs, `app-1` (good), `app-3` (missing) and `app-2` (written in Latin-1, so its `é` isn't
valid UTF-8), which does the catch skip?

```text
app-1.log: 1 ERROR lines
  skipped app-3.log: java.nio.file.NoSuchFileException: logs/app-3.log
app-3.log: 0 ERROR lines
app-2.log: batch stopped by java.io.UncheckedIOException: java.nio.charset.MalformedInputException: Input length = 1
    thrown inside Lines.countErrors(Lines.java:11)
```

`Files.lines` opens the file at once, so a missing file fails there, with a checked `IOException`.
The lines are read later, while `count()` pulls them through the stream, and a stream's
`Iterator` and `Spliterator` methods can't throw checked exceptions. So a read error in the middle
of the file arrives as an `UncheckedIOException` from `count()`, the terminal operation (line 11),
and goes past the `catch (IOException e)`. The javadoc of `Files.lines` and of
`BufferedReader.lines()` documents this wrapping.

`UncheckedIOException` is the JDK's standard wrapper: its constructors require an `IOException`
cause, and its `getCause()` returns one. When the stream code sits in a method that declares
`IOException`, unwrap it where the stream ends, so callers handle one exception type:

```java
} catch (UncheckedIOException e) {
    throw e.getCause();
}
```

Wrapping your own checked exception inside a lambda is unit 3.2.2.

<!-- practice 2 -->

## Make It Checked When the Caller Can Act on It, Unchecked When It Means a Bug

The JDK offers both for one failure. `new URI(String)` throws the checked
`URISyntaxException`; `URI.create(String)` throws the unchecked `IllegalArgumentException`, with
the same message:

```text
URI.create: java.lang.IllegalArgumentException: Illegal character in path at index 29: https://rates.example.com/eur usd
new URI:    java.net.URISyntaxException: Illegal character in path at index 29: https://rates.example.com/eur usd
```

A form where a user types a webhook URL can act on the
failure: it tells the user the URL has a space in it, and the checked constructor makes sure that
handler gets written. A URL that is a constant in our code fails only if we mistyped it, and the
caller can do nothing at run time but crash. `URI.create`'s javadoc names that case: "URI
constants declared within a program", where a failure "would be considered a programming error".

So ask whether the caller can act on the failure and couldn't have prevented it by checking first. A rate feed that is down, a file that isn't there, a declined card: the caller retries,
falls back or tells the user, and checked fits. A null argument, an index past the end, a call in
the wrong state: the caller could have prevented these and can't repair them afterwards, and
unchecked fits.

A checked exception for a bug gives every caller a catch block with
nothing to do in it, and those blocks end up empty or wrapping. An unchecked exception for an
expected outcome compiles without a handler, and the user's typo comes back as an HTTP 500. Where the
caller is a lambda, `Function.apply` declares no checked exception, so the lambda must wrap (the
previous section).

<!-- practice 3 -->

## Summary

- **Every `Throwable` is checked except `RuntimeException`, `Error` and their subclasses.** A class
  extending `Throwable` directly is checked, and `catch (Exception e)` doesn't catch it.
- **Adding a checked exception to a method breaks every caller's source; removing one breaks
  callers that catch it.** An unchecked one changes nothing at compile time.
- **The JVM never checks `throws`.** Code compiled against an older library, or
  `Class.newInstance`, receives checked exceptions it never declared, and only a catch for
  `Exception` or `Throwable` stops them.
- **`Files.lines` and `BufferedReader.lines` wrap read errors in `UncheckedIOException`, thrown by
  the terminal operation.** A `catch (IOException e)` around the stream misses them.
- **Checked when the caller can act on the failure and couldn't prevent it; unchecked for a bug.**
  The same failure can be either, depending on where the input comes from.

## Reference

**Which exceptions are checked (JLS 11.1.1):**

| Class | Checked |
|---|---|
| `Throwable`, and its subclasses outside `Exception` and `Error` | yes |
| `Exception` and its subclasses, except `RuntimeException`'s | yes |
| `RuntimeException` and its subclasses | no |
| `Error` and its subclasses | no |

**What a change to `throws` does (javac 21; binaries per JLS 13.4.21):**

| Change | Callers' source | Callers' compiled classes |
|---|---|---|
| add a checked exception | unreported exception at each call | run; the exception reaches them undeclared |
| add an unchecked exception | compiles | run |
| remove a checked exception | a `catch` for it is "never thrown" | run |
| add a checked exception to an interface method | calls through the interface fail; implementations compile | run |
| an implementation adds a checked exception its interface lacks | the implementation fails: "overridden method does not throw" | not applicable |

## Challenges

1. Your library must report a new failure from a method thousands of callers use. Compare a new
   checked exception in a major version, an unchecked one in a minor version, and a new method
   that declares it while the old one keeps its signature.
2. A plugin host loads jars built against older versions of its API. Which exceptions can reach
   its top-level `catch`, and what should it do with an `Error`?
3. Find a catch block in your code with nothing useful in it. Was the exception checked? Should it
   have been?

## Design Note: A Contract Only the Compiler Reads

Java made exceptions part of a method's type so a caller can't forget a failure it should handle.
The check costs nothing at run time because only javac makes it, and that is also its limit:
whatever javac doesn't see gets through, such as a jar swapped after the build, reflection or a
generic trick. Checked exceptions also don't compose: `Function.apply` declares none, so every
lambda passed to a stream that does I/O has to wrap. Kotlin, also on the JVM, has no checked
exceptions at all. Common practice reserves them for failures the caller can act on, and where
they get in the way, as in streams, the JDK wraps them.

## Code Examples

- Test: [ExceptionTypeDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/exceptions/ExceptionTypeDemoTest.java)
- Source: [ExceptionTypeDemo.java](src/main/java/com/github/msorkhpar/claudejavatutor/exceptions/ExceptionTypeDemo.java)