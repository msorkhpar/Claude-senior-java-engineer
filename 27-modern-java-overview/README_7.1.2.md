# 7.1.2. Java 11 (LTS)

An order service built for Java 8 moves to Java 21. The build stops at
`package javax.xml.bind does not exist`. A reporting jar nobody rebuilt starts, passes its health
check, and throws `NoClassDefFoundError` on the first export. A sign-up check rewritten with
`Set.of` on the way throws `NullPointerException` for a missing username. All of this arrived in Java 9,
10 and 11, and 11 was the first long-term-support release to carry it.

Features of 9 to 11 taught in other units are listed in the Reference.

## Java 11 Removed the Java EE Modules, and Code Built for 8 Fails at Its First Use

JEP 320 took JAXB (`java.xml.bind`), JAX-WS, JavaBeans Activation, CORBA and the
`java.transaction` module out of the JDK. This helper compiles for Java 8:

```java
import javax.xml.bind.DatatypeConverter;

public class Token {
    public static void main(String[] args) {
        System.out.println("service started on Java " + System.getProperty("java.specification.version"));
        byte[] digest = {(byte) 0xCA, (byte) 0xFE, 0x01, 0x7F};
        System.out.println(hex(digest));                 // the first call that needs JAXB
    }

    static String hex(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes);
    }
}
```

Built with `javac --release 8`, then run on Java 21:

```text
service started on Java 21
Exception in thread "main" java.lang.NoClassDefFoundError: javax/xml/bind/DatatypeConverter
	at Token.hex(Token.java:11)
	at Token.main(Token.java:7)
```

The JVM loads a class when code first needs it, so the error waits for the first call to `hex`:
a smoke test that never exports passes. With `--release 11` the import fails to compile. The fix
is a dependency, and its version matters:

```text
$ java -cp out8:jakarta.xml.bind-api-2.3.3.jar Token
service started on Java 21
CAFE017F
$ java -cp out8:jakarta.xml.bind-api-4.0.2.jar Token
service started on Java 21
Exception in thread "main" java.lang.NoClassDefFoundError: javax/xml/bind/DatatypeConverter
```

The 4.0.2 jar holds `jakarta.xml.bind.DatatypeConverter`: Jakarta EE 9 renamed the packages, so
the newest jar needs new imports too. For hex or Base64 alone, the JDK has `HexFormat` (17) and
`java.util.Base64` (8).

> **Java 9 and 10 differ.** The modules were still in the JDK but not resolved by default:
> `javac --release 9` reports `package javax.xml.bind is not visible`, and
> `--add-modules java.xml.bind` fixes it. With `--release 11` that flag fails:
> `module not found: java.xml.bind`.

## var Takes the Initializer's Type, Which Is Not Always the Type You Meant

Java 10's `var` gives a local variable the type of its initializer, fixed at the declaration:

```java
var names = new ArrayList<>();           // ArrayList<Object>
names.add("Ada");
names.add(42);                           // compiles: the element type is Object

List<Double> prices = List.of(19.99, 5.25, 0.80);
var total = 0;                           // int
for (double p : prices) total += p;      // compound assignment narrows to int on each add
System.out.println("total = " + total);
```

```text
total = 24
```

The prices add up to 26.04. `0` makes `total` an `int`, and `total += p` means
`total = (int) (total + p)`, so each sum loses its fraction. An invoice that drops its cents
passes every test with whole-number prices. `javac -Xlint:lossy-conversions` (Java 20 and later)
flags the line; `var total = 0.0` or a declared type fixes it.

With nothing to infer from, the diamond makes `names` an `ArrayList<Object>` that takes the 42,
and the error moves to the read: `String first = names.get(0)` doesn't compile.

`var` needs an initializer with a type, and only declares local variables (the reference lists
what javac refuses). Java 11 allowed it on lambda parameters, so a parameter can carry an
annotation and keep an inferred type: `(@NonNull var s, var n) -> s.length() + n` fails with
`--release 10`. `var` is not a reserved keyword: a variable named `var` compiles.

## List.of and Map.of Build Unmodifiable Collections That Reject null, Even in a Lookup

Java 9's factories (`List.of`, `Set.of`, `Map.of`, and `copyOf` from Java 10) return collections
that throw `UnsupportedOperationException` on `add`, `set` or `remove`, and refuse `null`:

```java
List<String> blocked = new ArrayList<>(List.of("spam.example", "junk.example"));
List<String> view = Collections.unmodifiableList(blocked);
List<String> copy = List.copyOf(blocked);
blocked.add("fake.example");
System.out.println("view: " + view + "  copy: " + copy);

Set<String> reserved = Set.of("admin", "root", "support");
String username = null;                              // the form had no username field
System.out.println(new HashSet<>(reserved).contains(username));
System.out.println(reserved.contains(username));
```

```text
view: [spam.example, junk.example, fake.example]  copy: [spam.example, junk.example]
false
Exception in thread "main" java.lang.NullPointerException
	at java.base/java.util.Objects.requireNonNull(Objects.java:233)
	at java.base/java.util.ImmutableCollections$SetN.contains(ImmutableCollections.java:944)
```

A `HashSet` answers `false` for `null`; `Collection.contains` lets a null-refusing collection
throw, and `Set.of` does. Replacing a `new HashSet<>(Arrays.asList(...))` constant with
`Set.of(...)` turns every sign-up without a username into a 500.
Check for `null` before the lookup.

`Collections.unmodifiableList` is a read-only view: a caller holding it sees every later change to
the list behind it. `List.copyOf` takes a snapshot.

`Map.of` and `Set.of` don't keep insertion order, and their order can change between JVM starts. One
program, run three times:

```java
Map<String, Integer> stock = Map.of("apple", 3, "pear", 5, "plum", 7, "fig", 2);
System.out.println(stock.keySet());
```

```text
[fig, pear, apple, plum]
[pear, fig, plum, apple]
[pear, fig, plum, apple]
```

A test that compares a printed map passes on one run and fails on the next. Where order matters,
sort, or use a `TreeMap` or `LinkedHashMap`.

<!-- practice 1 -->

## lines() Splits on Every Line Terminator; split("\n") Leaves the \r Behind

Java 11 added `isBlank`, `strip`, `stripLeading`, `stripTrailing`, `lines` and `repeat` to
`String`. `strip` and `isBlank` use `Character.isWhitespace`, where `trim` removes everything up
to U+0020 (unit 1.4.3). `lines()` fixes a common bug:

```java
String upload = "OK\r\nFAIL\r\n\r\nOK\r\n";            // a file saved on Windows
String[] split = upload.split("\n");
List<String> lines = upload.lines().toList();
System.out.println("split: " + split.length + " " + Arrays.toString(split).replace("\r", "\\r"));
System.out.println("lines: " + lines.size() + " " + lines);
System.out.println("\"OK\".equals(split[0]): " + "OK".equals(split[0]));
```

```text
split: 4 [OK\r, FAIL\r, \r, OK\r]
lines: 4 [OK, FAIL, , OK]
"OK".equals(split[0]): false
```

`split("\n")` cuts at line feeds and leaves each carriage return at the end of its line, so no
line equals `"OK"` and every upload from Windows fails validation. `lines()` ends a line at `\n`,
`\r\n` or `\r`, keeps an empty line in the middle, and adds none after a final terminator. Skip
the empty line with `isBlank()`, which also skips a line of spaces or tabs.

<!-- practice 2 -->

## HttpClient's Connect Timeout Bounds Only the Connection, and an Error Status Is a Response

`java.net.http.HttpClient` (standard since 11) is the modern alternative to `HttpURLConnection`.
The examples call a `com.sun.net.httpserver.HttpServer` on 127.0.0.1 at `base`: `/quote` doesn't
answer until the program lets it, and `/stock` answers 503.

```java
HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(1))
        .build();

// 1. connectTimeout only: the connection is made at once, then the response never comes
HttpRequest slow = HttpRequest.newBuilder(URI.create(base + "/quote")).build();
CompletableFuture<HttpResponse<String>> pending =
        client.sendAsync(slow, HttpResponse.BodyHandlers.ofString());
try {
    pending.get(3, TimeUnit.SECONDS);
} catch (TimeoutException e) {
    System.out.println("connectTimeout 1 s: no response after 3 s, still waiting");
}

// 2. the same request with its own timeout
HttpRequest bounded = HttpRequest.newBuilder(URI.create(base + "/quote"))
        .timeout(Duration.ofMillis(500))
        .build();
long start = System.nanoTime();
try {
    client.send(bounded, HttpResponse.BodyHandlers.ofString());
} catch (IOException e) {
    System.out.printf("request timeout 500 ms: %s after %d ms%n", e,
            (System.nanoTime() - start) / 1_000_000);
}
```

```text
connectTimeout 1 s: no response after 3 s, still waiting
request timeout 500 ms: java.net.http.HttpTimeoutException: request timed out after 502 ms
```

A request without its own timeout waits as long as the server takes ("block forever", says the javadoc).
When a partner API stalls, every thread that calls it parks in `send()` until the pool is used
up and the service stops answering its own callers.

The `/stock` call:

```java
HttpResponse<String> stock = client.send(
        HttpRequest.newBuilder(URI.create(base + "/stock")).timeout(Duration.ofSeconds(2)).build(),
        HttpResponse.BodyHandlers.ofString());
System.out.println("status " + stock.statusCode() + ", body " + stock.body());
```

```text
status 503, body {"error":"maintenance"}
```

`send` throws `IOException` on I/O failures, never because of the status. A 4xx or 5xx returns
like a 200, so code
that reads the body without checking `statusCode()` treats an error page as data. Build one
client and share it: it is immutable, and a new client per call can't reuse connections.

> **Java 21 differs.** `HttpClient` is `AutoCloseable` from Java 21 (`--release 17` rejects
> try-with-resources on it). `close()` waits for requests in flight, so a request without a
> timeout hangs the close too: against `/quote` it had not returned after 2 s.

<!-- practice 3 -->

## java Report.java Compiles One File in Memory and Runs Its First Class

Since Java 11 the launcher runs a source file (JEP 330): it compiles the file in memory, writes no
`.class` file, and runs the first top-level class:

```java
import java.nio.file.Files;
import java.nio.file.Path;

public class Report {
    public static void main(String[] args) throws Exception {
        long errors = Files.readString(Path.of(args[0])).lines().filter(l -> l.contains("ERROR")).count();
        System.out.println(Helper.label(errors));
    }
}

class Helper {
    static String label(long n) { return n + " error lines"; }
}
```

```text
$ java Report.java app.log
2 error lines
```

Two edits `javac` accepts break it on Java 21: declaring `Helper` above `Report`, or moving
`Helper` to its own file beside it:

```text
$ java first/Report.java app.log
error: can't find main(String[]) method in class: Helper
$ java split/Report.java app.log
split/Report.java:7: error: cannot find symbol
        System.out.println(Helper.label(errors));
                           ^
```

Other classes come from the same file or, compiled, from the class path. On Unix, a file not
named `*.java` can start with `#!/usr/bin/java --source 21` and run as a script; in a `.java` file
that line doesn't compile.

> **Later releases differ.** On Java 25 both commands above print `2 error lines`. JEP 458
> (Java 22) lets the launcher compile other source files from the program's directory.

<!-- practice 4 -->

## Summary

- **The Java EE modules are gone.** Code built for 8 fails at its first call into
  `javax.xml.bind`; the 2.3.x API jar restores the package, the 4.x jar renames it.
- **`var` takes the initializer's type.** `var total = 0` is an `int` that `+=` truncates.
- **The factories are unmodifiable and reject null.** `contains(null)` throws, `copyOf`
  snapshots, and `Map.of` order can change between runs.
- **`lines()` splits on every terminator.** `split("\n")` leaves the `\r`.
- **Each `HttpClient` request needs its own timeout**, and a 5xx is a response, not an exception.
- **`java File.java` compiles that one file and runs its first class** on Java 21.

## Reference

**Where javac 21 refuses `var`:**

| Declaration | Error |
|---|---|
| field, return type | `'var' is not allowed here` |
| `var x;` | `cannot use 'var' on variable without initializer` |
| `var x = null;` | `variable initializer is 'null'` |
| `var a = {1, 2};` | `array initializer needs an explicit target-type` |
| `var f = () -> "x";` | `lambda expression needs an explicit target-type` |
| `(var s, Integer n) -> ...` | `cannot mix 'var' and explicitly-typed parameters` |

**Collection factories (Java 21):**

| Call | Result |
|---|---|
| `List.of("a", null)`, `List.copyOf` of a list holding `null` | `NullPointerException` |
| `Map.of(...).get(null)`, `containsKey(null)` | `NullPointerException` |
| `Map.of("a", 1, "a", 2)`, `Set.of("a", "a")` | `IllegalArgumentException: duplicate key: a` / `duplicate element: a` |
| `List.copyOf(list)` where `list` came from `List.of` | returns `list` itself |
| `Arrays.asList(...).set(0, x)` / `.add(x)` | allowed / `UnsupportedOperationException` |

**Strings:** `"".lines()` has no lines, `"\n".lines()` one empty line; `"ab".repeat(0)` is `""`, and
`repeat(-1)` throws `IllegalArgumentException`.

**HttpClient (Java 21):**

| Fact | Value |
|---|---|
| `client.version()` by default | `HTTP_2`; on `http://` it sends `Upgrade: h2c`, and the example's server answered `HTTP_1_1` |
| its worker and selector threads | daemon |

**Removed in 11, and what replaces them:**

| Module | Replacement |
|---|---|
| `java.xml.bind` | `jakarta.xml.bind-api` 2.3.x for `javax.xml.bind`; `JAXBContext` without an implementation throws `JAXBException: Implementation of JAXB-API has not been found` |
| `java.transaction` | `java.transaction.xa` stays in the JDK |
| `java.xml.ws`, `java.activation`, `java.corba`, `java.xml.ws.annotation` | separate libraries |

**Taught elsewhere:** `Optional.isEmpty` (11) in 7.1.1; `takeWhile`/`dropWhile` (9) in 4.4.2;
nest-based access (11) in 10.5.2; garbage collectors in 10.2.2.

## Challenges

1. How do you find every use of a removed module, third-party jars included?
2. Your team wants every `new HashSet<>(Arrays.asList(...))` constant replaced with `Set.of(...)`.
   Which call sites change behaviour, and how would you find them?
3. An upstream answers in 800 ms at p99, and your caller gives you 2 s. Pick a request timeout and
   a retry policy, and say what happens to your thread pool when the upstream stalls.

## Design Note: Why the Factories Refuse null and Vary Map.of's Order

A `null` in a constant collection is almost always a bug, and failing at once finds it sooner.
For the sets and maps there is a second reason: they are arrays searched by probing, and the JDK
source marks an empty slot with `null`, so a `null` element couldn't be stored.

The order varies on purpose: `ImmutableCollections` draws a salt at JVM start so that, in its
own comment, "iteration order will vary between JVM runs". Code that relies on hash order then
breaks on the next restart, in a test, rather than on a JDK upgrade. The cost is that
moving to the factories isn't mechanical: every lookup that might see `null` and every output
that depends on order has to be read.

## Code Examples

- Test: [Java11FeaturesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/modernjava/Java11FeaturesTest.java)
- Source: [Java11Features.java](src/main/java/com/github/msorkhpar/claudejavatutor/modernjava/Java11Features.java)
