# 7.1.2. Java 11 (LTS)

## Concept Explanation

Java 11, released in September 2018, was the first Long-Term Support (LTS) release under Oracle's new six-month release
cadence. It consolidated features previewed in Java 9 and 10, removed deprecated modules, and introduced several
productivity improvements that made Java more concise and developer-friendly.

**Real-world analogy**: If Java 8 was adding power tools to a workshop, Java 11 was the renovation that streamlined the
workspace -- removing old unused equipment (Java EE modules, CORBA), modernizing the workbench (String API, HTTP Client),
and adding convenient shortcuts (var in lambdas, single-file execution) so craftsmen could start projects faster.

Key areas of improvement:
1. **Local-Variable Syntax for Lambda Parameters** -- using `var` in lambda formal parameters for annotation support
2. **HTTP Client API** -- a modern, non-blocking HTTP client replacing `HttpURLConnection`
3. **String API Enhancements** -- utility methods like `isBlank()`, `strip()`, `lines()`, `repeat()`
4. **Nested Based Access Control** -- JVM-level support for private member access between nest members
5. **Running Java Files Directly** -- `java MyProgram.java` without explicit `javac` compilation

## Key Points to Remember

- `var` in lambda parameters enables annotations: `(@NonNull var x) -> x.length()`.
- You cannot mix `var` with explicit types in lambda parameters: all or none must be `var`.
- The HTTP Client API supports HTTP/1.1 and HTTP/2, synchronous and asynchronous requests.
- `String.strip()` is Unicode-aware; `String.trim()` only handles ASCII whitespace (chars <= U+0020).
- `String.lines()` returns a `Stream<String>` and handles `\n`, `\r\n`, and `\r`.
- `String.isBlank()` returns true for empty strings and strings containing only whitespace.
- Nest-based access control eliminates compiler-generated bridge methods for inner class access.
- `Files.writeString()` and `Files.readString()` simplify file I/O dramatically.
- `Optional.isEmpty()` is the complement of `isPresent()` -- reads more naturally in conditions.
- Java EE and CORBA modules were removed: `java.xml.ws`, `java.xml.bind`, `javax.activation`, etc.

## Relevant Java 21 Features

Java 21 extends Java 11's foundations:

- **`var`**: Originally introduced in Java 10 for local variables, extended in 11 for lambdas, now used widely with pattern matching in Java 21.
- **HTTP Client**: Remains the standard; virtual threads (Java 21) make asynchronous HTTP particularly powerful.
- **String enhancements**: Java 21 continues the trend with additional methods and text block improvements.
- **Nest access**: Hidden classes (Java 15+) build on the nest concept for dynamic class generation.
- **Single-file execution**: The `java` launcher continues to be enhanced, supporting shebang scripts on Unix.

## Common Pitfalls and How to Avoid Them

1. **Mixing `var` and explicit types in lambda parameters**:
   ```java
   // WRONG: cannot mix var with explicit types
   BiFunction<String, Integer, String> f = (var s, Integer i) -> s.repeat(i);

   // FIX: all var or all explicit
   BiFunction<String, Integer, String> f = (var s, var i) -> s.repeat(i);
   ```

2. **Using `trim()` instead of `strip()` for Unicode whitespace**:
   ```java
   String s = "\u2000Hello\u2000"; // EN QUAD whitespace
   s.trim();   // Returns "\u2000Hello\u2000" -- trim ignores Unicode whitespace
   s.strip();  // Returns "Hello" -- strip handles Unicode whitespace
   ```

3. **Not handling HTTP Client timeouts**:
   ```java
   // WRONG: no timeout -- can hang indefinitely
   HttpClient client = HttpClient.newHttpClient();

   // FIX: always set connection timeout
   HttpClient client = HttpClient.newBuilder()
       .connectTimeout(Duration.ofSeconds(10))
       .build();
   ```

4. **Assuming `Files.readString()` handles large files efficiently**:
   ```java
   // WRONG for large files: reads entire file into memory
   String content = Files.readString(Path.of("huge-file.log"));

   // FIX: use BufferedReader or streaming for large files
   try (var reader = Files.newBufferedReader(Path.of("huge-file.log"))) {
       reader.lines().forEach(this::processLine);
   }
   ```

5. **Forgetting removed Java EE modules**:
   ```java
   // WRONG: these are removed in Java 11
   import javax.xml.bind.JAXB;

   // FIX: add explicit Maven/Gradle dependencies
   // <dependency>
   //   <groupId>jakarta.xml.bind</groupId>
   //   <artifactId>jakarta.xml.bind-api</artifactId>
   // </dependency>
   ```

## Best Practices and Optimization Techniques

1. **Use `strip()` over `trim()`** -- it is the modern, Unicode-aware replacement.
2. **Use `isBlank()` instead of `trim().isEmpty()`** -- more readable and efficient.
3. **Configure HTTP Client as a singleton** -- it is thread-safe and expensive to create.
4. **Use `HttpResponse.BodyHandlers.ofLines()`** for streaming large HTTP responses.
5. **Use `Files.readString()` / `writeString()`** for small files to reduce boilerplate.
6. **Leverage `String.repeat()`** instead of loops or `StringBuilder` for repeated patterns.
7. **Use `String.lines()`** instead of `split("\\n")` for correct cross-platform line splitting.
8. **Annotate lambda parameters with `var`** when you need `@Nullable` or `@NonNull` annotations.

## Edge Cases and Their Handling

1. **`String.repeat(0)`** returns an empty string, not null.
2. **`String.repeat()` with negative count** throws `IllegalArgumentException`.
3. **`String.lines()`** on an empty string returns an empty stream (zero elements).
4. **`String.lines()`** does not include a trailing empty string if the input ends with a line terminator.
5. **`String.strip()` on empty string** returns empty string.
6. **HTTP Client with HTTP/2** falls back to HTTP/1.1 if the server does not support HTTP/2.
7. **`Optional.isEmpty()`** on `Optional.of("")` returns false (empty string is present, not absent).
8. **Nest-based access**: `Class.isNestmateOf()` returns true for the class itself.

## Interview-specific Insights

Interviewers test:
- The difference between `strip()` and `trim()` (Unicode awareness)
- Why `var` was added to lambda parameters specifically (annotation support)
- How the HTTP Client API compares to `HttpURLConnection` and third-party libraries
- What modules were removed in Java 11 and how to handle migration
- Understanding of LTS release model and Java's new release cadence

Tricky questions:
- "Can you use `var` as a variable name?" (Yes! `var` is a reserved type name, not a keyword)
- "Does `String.lines()` include trailing empty strings?" (No)
- "Is the HTTP Client thread-safe?" (Yes, and it should be reused)

## Interview Q&A Section

**Q1: What is the purpose of allowing `var` in lambda parameters in Java 11?**

```text
A1: The primary purpose is to allow annotations on lambda parameters. Before Java 11,
you had two choices for lambda parameter types:
1. Explicit types: (String s, Integer i) -> ... (allows annotations)
2. Inferred types: (s, i) -> ... (no annotations possible)

Java 10 introduced var for local variable type inference, but it was not allowed in
lambda parameters. Java 11 extended var to lambda parameters, giving a third option:
3. var types: (var s, var i) -> ... (allows annotations with inference)

This is especially useful with annotation-based null checking:
(@NonNull var s, @Nullable var i) -> s.length() + (i != null ? i : 0)

Without var, you would need explicit types to annotate parameters, losing the
convenience of type inference.

Important restrictions:
- Cannot mix var with explicit types: (var s, Integer i) is illegal
- Cannot mix var with inferred (no-type): (var s, i) is illegal
- All parameters must use var, or none must
```

```java
import org.jetbrains.annotations.NotNull;
import java.util.function.BiFunction;

// Before Java 11: must use explicit types for annotations
BiFunction<String, Integer, String> f1 =
    (@NotNull String s, @NotNull Integer i) -> s.repeat(i);

// Java 11: var enables annotations with type inference
BiFunction<String, Integer, String> f2 =
    (@NotNull var s, @NotNull var i) -> s.repeat(i);

// Still valid: no types, no annotations
BiFunction<String, Integer, String> f3 = (s, i) -> s.repeat(i);
```

**Q2: How does the Java 11 HTTP Client API compare to HttpURLConnection?**

```text
A2: The Java 11 HTTP Client (java.net.http) is a complete replacement for
HttpURLConnection with major improvements:

1. Modern Design:
   - Fluent builder API for requests and clients
   - Immutable request objects
   - Clean separation of request/response handling

2. Protocol Support:
   - HTTP/2 support with fallback to HTTP/1.1
   - WebSocket support built-in
   - Automatic protocol negotiation

3. Async Support:
   - CompletableFuture-based asynchronous API
   - Non-blocking I/O
   - Can be combined with virtual threads (Java 21)

4. Body Handlers:
   - Type-safe body handlers: ofString(), ofByteArray(), ofFile(), ofLines()
   - Custom body handlers possible
   - Streaming support for large responses

5. Configuration:
   - Connection timeouts
   - Redirect policies
   - Cookie handling
   - Proxy support
   - SSL/TLS configuration

HttpURLConnection issues that are resolved:
- Clunky, confusing API (e.g., must call getInputStream() before getResponseCode())
- No HTTP/2 support
- Blocking-only design
- Inconsistent error handling
```

```java
// Old way: HttpURLConnection
URL url = new URL("https://api.example.com/data");
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("GET");
conn.setConnectTimeout(5000);
int status = conn.getResponseCode();
try (var reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
    String body = reader.lines().collect(Collectors.joining());
}
conn.disconnect();

// New way: Java 11 HTTP Client
HttpClient client = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(5))
    .build();

HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://api.example.com/data"))
    .GET()
    .build();

// Synchronous
HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
int status = response.statusCode();
String body = response.body();

// Asynchronous
client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
    .thenApply(HttpResponse::body)
    .thenAccept(System.out::println);
```

**Q3: What String API methods were added in Java 11 and when should each be used?**

```text
A3: Java 11 added six String methods:

1. isBlank(): Returns true if the string is empty or contains only whitespace.
   Use instead of: str.trim().isEmpty()

2. strip(): Removes leading and trailing whitespace (Unicode-aware).
   Use instead of: str.trim() (which only handles ASCII whitespace <= U+0020)

3. stripLeading(): Removes only leading whitespace (Unicode-aware).
   Use when: You only want to clean the start of a string.

4. stripTrailing(): Removes only trailing whitespace (Unicode-aware).
   Use when: You only want to clean the end of a string.

5. lines(): Returns a Stream<String> of lines, splitting on \n, \r, or \r\n.
   Use instead of: str.split("\\R") or str.split("\\n")

6. repeat(int count): Returns the string repeated count times.
   Use instead of: manual loop or String.join("", Collections.nCopies(n, str))

Key distinction: strip() vs trim()
- trim() uses char <= ' ' (U+0020) as whitespace definition
- strip() uses Character.isWhitespace() which includes Unicode whitespace
  like EN QUAD (U+2000), EM SPACE (U+2003), etc.

In modern code, always prefer strip() over trim().
```

```java
// isBlank() - better than trim().isEmpty()
"".isBlank();        // true
"   ".isBlank();     // true
" a ".isBlank();     // false

// strip() vs trim() - Unicode awareness
String s = "\u2000Hello\u2000";
s.trim();    // "\u2000Hello\u2000" - trim misses Unicode whitespace
s.strip();   // "Hello" - strip handles Unicode whitespace

// stripLeading() / stripTrailing()
"  hello  ".stripLeading();   // "hello  "
"  hello  ".stripTrailing();  // "  hello"

// lines() - cross-platform line splitting
"line1\nline2\r\nline3".lines()
    .forEach(System.out::println);
// Outputs: line1, line2, line3

// repeat() - string repetition
"ab".repeat(3);   // "ababab"
"-".repeat(40);   // "----------------------------------------"
"x".repeat(0);    // ""
```

**Q4: What is nested-based access control and why does it matter?**

```text
A4: Nested-based access control (JEP 181) is a JVM-level improvement that allows
classes in the same nest (e.g., an outer class and its inner classes) to access
each other's private members directly, without compiler-generated bridge methods.

Before Java 11:
- Inner classes accessing outer private members (and vice versa) required the
  compiler to generate synthetic "bridge" or "accessor" methods.
- These bridge methods were:
  - Invisible in source code but present in bytecode
  - Package-private access, creating potential security issues
  - Extra method calls that the JIT compiler had to optimize away
  - Confusing in stack traces and reflection

After Java 11:
- The JVM natively understands the concept of "nests" (groups of related classes).
- Private members can be accessed directly between nestmates.
- No bridge methods needed.
- Reflection works correctly: Class.getNestHost(), Class.getNestMembers(),
  Class.isNestmateOf().

New APIs:
- Class.getNestHost(): Returns the nest host (outermost enclosing class)
- Class.getNestMembers(): Returns all classes in the nest
- Class.isNestmateOf(Class): Checks if two classes are nestmates

This is mostly transparent to developers but matters for:
- Framework developers using reflection
- Security-sensitive code
- Performance in deep nesting hierarchies
```

```java
public class Outer {
    private String secret = "outer";

    public class Inner {
        private String innerSecret = "inner";

        // Before Java 11: compiler generated a bridge method for this access
        // After Java 11: direct access at JVM level
        public String getOuterSecret() {
            return secret;
        }
    }

    public String getInnerSecret() {
        return new Inner().innerSecret; // Direct access, no bridge method
    }

    // Reflection API for nest inspection
    public static void inspectNest() {
        System.out.println("Nest host: " + Outer.class.getNestHost());
        System.out.println("Nest members: " +
            java.util.Arrays.toString(Outer.class.getNestMembers()));
        System.out.println("Is nestmate: " +
            Outer.class.isNestmateOf(Inner.class));
    }
}
```

**Q5: How does single-file source-code execution work in Java 11?**

```text
A5: Java 11 allows running a Java source file directly without explicit compilation:

  java HelloWorld.java

How it works:
1. The java launcher detects that the argument is a .java file (not a .class).
2. It compiles the source in memory using the built-in compiler.
3. It executes the resulting bytecode immediately.
4. No .class file is written to disk.

Rules and limitations:
- The file must contain a class with a main method.
- All classes must be in the same file (no multi-file programs).
- The first class in the file is used as the entry point.
- You can pass command-line arguments after the file name.
- You can use --source flag to specify source version.
- Shebang support on Unix: #!/usr/bin/java --source 11

Use cases:
- Quick scripts and prototyping
- Teaching and demonstrations
- Simple automation scripts
- Replacing bash scripts with Java

Java 11 also added Files.readString() and Files.writeString() which pair well
with scripting-style programs, making simple file operations much more concise
than the traditional BufferedReader/BufferedWriter approach.
```

```java
// Save as Greeter.java and run: java Greeter.java World
public class Greeter {
    public static void main(String[] args) {
        String name = args.length > 0 ? args[0] : "World";
        System.out.println("Hello, " + name + "!");

        // Files API enhancements (also Java 11)
        var path = java.nio.file.Path.of("greeting.txt");
        try {
            java.nio.file.Files.writeString(path, "Hello, " + name + "!");
            String content = java.nio.file.Files.readString(path);
            System.out.println("Written and read back: " + content);
            java.nio.file.Files.delete(path);
        } catch (java.io.IOException e) {
            System.err.println("File error: " + e.getMessage());
        }
    }
}

// Unix shebang example (save as greeter, chmod +x):
// #!/usr/bin/java --source 11
// public class Greeter { ... }
```

## Code Examples

- Test: [Java11FeaturesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/modernjava/Java11FeaturesTest.java)
- Source: [Java11Features.java](src/main/java/com/github/msorkhpar/claudejavatutor/modernjava/Java11Features.java)
