# 3.1.4 Try-with-resources Statement

An upload service accepts gzipped CSV files. Partners started sending plain CSV by mistake, the
service rejected each one, and a week later it failed with `Too many open files`. Every stream it
opens sits in a try-with-resources statement:

```java
try (InputStream in = new GZIPInputStream(new FileInputStream(path))) {
```

The statement (Java 7) closes each `AutoCloseable` declared in its header when the block exits.
Outputs are from Java 21.

## Resources Close in Reverse Order, Before the Statement's catch and finally Run

```java
public class Order {
    record Res(String name) implements AutoCloseable {
        Res { System.out.println("open  " + name); }
        @Override public void close() { System.out.println("close " + name); }
    }

    public static void main(String[] args) {
        try (Res conn = new Res("connection");
             Res tx = new Res("transaction");
             Res cursor = new Res("cursor")) {
            System.out.println("body: query fails");
            throw new IllegalStateException("deadlock detected");
        } catch (IllegalStateException e) {
            System.out.println("catch: " + e.getMessage());
        } finally {
            System.out.println("finally");
        }
    }
}
```

```text
open  connection
open  transaction
open  cursor
body: query fails
close cursor
close transaction
close connection
catch: deadlock detected
finally
```

A later resource usually depends on an earlier one, so it is closed first. The `catch` and
`finally` belong to the statement, not to the block, and run after every resource is closed. A
catch that retries on the connection, or assumes the transaction is still open, works on
something already released.

## A close() Failure Is Attached as Suppressed, Not Thrown in Place of the Body's Exception

A bad row in the upload, and an `Upload` whose `close()` throws `IOException`, handled with
try-with-resources and with `finally` (unit 3.1.3):

```java
static void withResources() throws IOException {
    try (Upload up = new Upload()) {
        throw new IllegalArgumentException("row 42: bad amount");
    }
}

static void withFinally() throws IOException {
    Upload up = new Upload();
    try {
        throw new IllegalArgumentException("row 42: bad amount");
    } finally {
        up.close();
    }
}
```

```text
try-with-resources: java.lang.IllegalArgumentException: row 42: bad amount
  suppressed: java.io.IOException: connection reset while closing upload
finally:            java.io.IOException: connection reset while closing upload
  suppressed: none
```

With `finally`, the log shows a network error and never learns about row 42. Try-with-resources
throws the body's exception and adds the close failure to it with `addSuppressed`: `getSuppressed()`
returns it and a printed stack trace lists it, but a log line built from `getMessage()` drops it.

When the body completes and `close()` throws, the close exception is the one thrown, and the
statement's `catch` receives it.

<!-- practice 1 -->

## Only a Resource That Finished Constructing Is Closed, So Declare Each Stream Separately

The opening's upload, where `TracedFile` is a `FileInputStream` that prints when it is closed:

```java
static void nested(String path) throws IOException {
    try (InputStream in = new GZIPInputStream(new TracedFile(path))) {
        in.read();
    }
}

static void separate(String path) throws IOException {
    try (InputStream file = new TracedFile(path);
         InputStream in = new GZIPInputStream(file)) {
        in.read();
    }
}
```

```text
nested:
  java.util.zip.ZipException: Not in GZIP format
separate:
  file closed
  java.util.zip.ZipException: Not in GZIP format
```

`GZIPInputStream`'s constructor reads the header and throws on a plain CSV. In the nested form the
only resource is the `GZIPInputStream`, which never finished constructing, so nothing closes the
file. Declared separately, the file is its own resource and is closed. The nested form, run 1,000
times:

```text
open files after 1000 rejected uploads: +1000
after a full GC: +0
```

The descriptors come back only when a garbage collection lets the JDK's cleaner close them, and a
large heap can go hours without one.

Once both are constructed, the separate form closes the file twice: `GZIPInputStream.close()`
closes it, then the statement does. `Closeable.close()` must have no effect the second time;
`AutoCloseable.close()` needn't. Declared as `try (Conn c = pool.pop(); Stmt s = new Stmt(c))`,
this pooled connection goes back to the pool twice:

```java
import java.util.Deque;

record Conn(String id, Deque<Conn> pool) implements AutoCloseable {
    @Override public void close() { pool.push(this); }
}
record Stmt(Conn conn) implements AutoCloseable {
    @Override public void close() { conn.close(); }
}
```

```text
the next two borrowers get: [conn-2, conn-2]
```

<!-- practice 2 -->

## A Variable Named in the Header Is Closed Too, Whoever Opened It

Since Java 9 the header can name an existing final or effectively final variable. This helper
reads the first line from a reader its caller passed in:

```java
static String header(BufferedReader reader) throws IOException {
    try (reader) {
        return reader.readLine();
    }
}
```

```text
header: id,amount
first row: java.io.IOException: Stream closed
```

Write `try (x)` only where the method owns `x`. A reassigned variable is refused:
`variable reader used as a try-with-resources resource neither final nor effectively final`.

<!-- practice 3 -->

## Summary

- **Resources close in reverse order, before the statement's `catch` and `finally`.**
- **A `close()` failure is suppressed under the body's exception**; `finally` would replace it.
- **Only fully constructed resources are closed**, so a wrapped stream leaks when its wrapper's
  constructor throws, unless declared on its own; then its `close()` runs twice and must be
  idempotent.
- **A variable named in the header is closed**, whoever opened it.

## Reference

| Situation (Java 21) | What happens |
|---|---|
| a resource initialised to `null` | the body runs, nothing is closed, no exception |
| body throws, two resources' `close()` throw | body's exception thrown; both close failures suppressed, last-declared first |
| body completes, two resources' `close()` throw | the last-declared resource's exception thrown, the other suppressed under it |

## Challenges

1. A `close()` that flushes buffered rows fails after the body succeeded. Is the upload failed?
2. Your logger prints only `getMessage()`. Where do you add suppressed exceptions?
3. A method returns an open `Stream<String>` from `Files.lines`. Who closes it, and how would a
   reviewer know?

## Design Note: Why Suppression and Not Replacement

Before Java 7, careful code wrapped each `close()` in `finally` in its own try/catch and
discarded the failure, or let it replace the body's exception. Java 7 added
`Throwable.addSuppressed` with the statement, so the body's failure is thrown and the close
failures are kept. They are invisible to code that never calls `getSuppressed()`.

## Code Examples

- Test: [TryWithResourcesDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/trycatch/TryWithResourcesDemoTest.java)
- Source: [TryWithResourcesDemo.java](src/main/java/com/github/msorkhpar/claudejavatutor/trycatch/TryWithResourcesDemo.java)