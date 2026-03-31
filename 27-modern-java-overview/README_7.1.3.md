# 7.1.3. Java 15

## Concept Explanation

Java 15, released in September 2020, was a non-LTS release that standardized text blocks and continued refining pattern matching and sealed classes. While not a long-term support release, Java 15 delivered features that fundamentally improved code readability and set the stage for the pattern-matching revolution in later versions.

**Real-world analogy**: Think of Java 15 as the "fit and finish" release — like a car manufacturer standardizing the premium features (text blocks) that were tested in earlier models (Java 13-14 previews) and starting road-testing the next generation of features (pattern matching, sealed classes). The features aren't all production-ready yet, but the direction is clear and exciting.

The three pillars of Java 15 are:
1. **Text Blocks (Standard)** — multi-line string literals with proper formatting control
2. **Pattern Matching for instanceof (Second Preview)** — eliminating redundant casting after type checks
3. **Hidden Classes** — framework-oriented feature for dynamically generated classes

## Key Points to Remember

- Text blocks were finalized as a standard feature in Java 15 (JEP 378), after previews in Java 13 and 14.
- Text blocks use `"""` triple-quote delimiters; the opening `"""` must be followed by a line terminator.
- Common leading whitespace is automatically stripped based on the position of the closing `"""`.
- The `\s` escape preserves trailing whitespace; `\` at end of line suppresses the line terminator.
- `String.formatted()` was added alongside text blocks for convenient interpolation.
- Pattern matching for `instanceof` (JEP 375) eliminates the need for explicit casts after type checks.
- The pattern variable's scope extends only where the pattern is guaranteed to have matched.
- Hidden classes (JEP 371) cannot be discovered by name and are designed for frameworks generating classes at runtime.
- Lambda expressions in Java 15+ internally use hidden classes instead of anonymous inner classes.
- Hidden classes can be unloaded independently of their defining class loader.

## Relevant Java 21 Features

Java 21 builds extensively on Java 15's foundations:

- **Text blocks** remain the standard for multi-line strings. Java 21 does not add string templates as a standard feature (still preview), but text blocks combined with `formatted()` remain the recommended approach.
- **Pattern matching for instanceof** was standardized in Java 16 and is now used everywhere in Java 21 code, including with sealed classes and record patterns.
- **Hidden classes** underpin virtual threads (Project Loom) and the lambda metafactory in Java 21; they are used transparently by the JVM.
- **Sealed classes**, previewed alongside these features, became standard in Java 17 and are essential for exhaustive pattern matching in Java 21 switch expressions.

## Common Pitfalls and How to Avoid Them

1. **Incorrect text block indentation**: The closing `"""` position controls indentation stripping.
   ```java
   // WRONG: Unexpected indentation because closing """ is at column 0
   String wrong = """
           Hello
           World
   """;
   // "Hello" and "World" retain their leading spaces relative to """

   // RIGHT: Closing """ at same indent level strips common whitespace
   String right = """
           Hello
           World
           """;
   // "Hello\nWorld\n" — clean, no leading spaces
   ```

2. **Forgetting the line terminator after opening `"""`**:
   ```java
   // COMPILE ERROR: opening """ must be followed by a line terminator
   // String bad = """Hello""";

   // CORRECT:
   String good = """
           Hello""";
   ```

3. **Pattern variable scope confusion**:
   ```java
   // WRONG: pattern variable 's' is not in scope in the else branch
   if (!(obj instanceof String s)) {
       // s is NOT available here
       // System.out.println(s.length()); // Compile error
   } else {
       // s IS available here (obj is guaranteed to be String)
       System.out.println(s.length());
   }
   ```

4. **Missing trailing newline in text blocks**:
   ```java
   // This includes a trailing newline:
   String withNewline = """
           content
           """;
   // "content\n"

   // This does NOT include a trailing newline:
   String noNewline = """
           content\
           """;
   // "content"
   ```

5. **Assuming hidden classes behave like normal classes**:
   ```java
   // Hidden classes cannot be discovered by Class.forName()
   // WRONG: trying to load a hidden class by name
   // Class.forName("com.example.Hidden$$Lambda/0x123"); // ClassNotFoundException

   // RIGHT: hidden classes are obtained through MethodHandles.Lookup.defineHiddenClass()
   // or are created automatically by the JVM for lambdas
   ```

## Best Practices and Optimization Techniques

- **Use text blocks for any multi-line string**: JSON, SQL, HTML, XML, error messages, and test data all benefit.
- **Align the closing `"""`** with the content to control indentation precisely.
- **Use `\s` and `\` escape sequences** for precise whitespace control in text blocks.
- **Prefer `formatted()`** over `String.format()` when using text blocks — it reads more naturally as a method chain.
- **Pattern matching for instanceof should replace all cast-after-check patterns** in existing code.
- **Combine pattern matching with `&&` guards** for concise conditional logic.
- **Don't interact with hidden classes directly** unless you're building a framework; they're a JVM implementation detail.
- **Use `Class.isHidden()`** if you need to detect hidden classes (e.g., in debugging or logging frameworks).

## Edge Cases and Their Handling

- **Empty text block**: `"""\n"""` produces an empty string `""`.
- **Text block with only whitespace**: Whitespace-only lines are subject to the same stripping rules.
- **Null checks with pattern matching**: `null instanceof String s` is always `false` — no NPE.
- **Pattern variable in short-circuit expressions**: `obj instanceof String s && s.length() > 5` is safe because `s` is only bound when the instanceof succeeds.
- **Hidden class naming**: Hidden classes have names containing `/` (e.g., `com.example.Foo/0x1234`), which is not a valid class name for `Class.forName()`.

## Interview-specific Insights

Interviewers often focus on:
- **Why text blocks matter**: Not just convenience — they eliminate escape-sequence hell and make code review easier for SQL/JSON-heavy applications.
- **Pattern matching scope rules**: A favorite tricky question is when the pattern variable is available in negated conditions.
- **Hidden classes vs. anonymous classes**: Understanding that lambdas use hidden classes since Java 15 shows deep JVM knowledge.
- **Migration questions**: "How would you refactor legacy String concatenation to text blocks?" — demonstrate knowledge of indentation rules and `formatted()`.

## Interview Q&A Section

### Q1: What are text blocks and how do they differ from regular string literals?

```text
Text blocks are multi-line string literals introduced in Java 13 (preview) and standardized
in Java 15 (JEP 378). They use triple-quote delimiters (""") and automatically handle:
1. Line terminators — no need for \n
2. Common leading whitespace stripping — based on the closing """ position
3. Escape sequence reduction — quotes don't need escaping (unless triple quotes)

Key differences from regular strings:
- Opening """ must be followed by a line terminator (content starts on next line)
- Trailing whitespace is stripped by default (use \s to preserve it)
- Common leading whitespace is stripped (incidental whitespace removal)
- The result is still a regular java.lang.String at runtime — no new type
```

```java
// Regular string literal (messy)
String json = "{\n" +
    "    \"name\": \"John\",\n" +
    "    \"age\": 30\n" +
    "}";

// Text block (clean)
String jsonBlock = """
        {
            "name": "John",
            "age": 30
        }
        """;
```

### Q2: Explain incidental whitespace stripping in text blocks.

```text
The Java compiler determines the common leading whitespace across all content lines and
the closing delimiter line, then strips that prefix from every line. This is called
"incidental whitespace" removal.

The algorithm:
1. Find the minimum leading whitespace count across all non-blank content lines and
   the closing """ line
2. Remove that many leading spaces from every line
3. Trailing whitespace on each line is also stripped (unless \s is used)

This means the position of the closing """ controls indentation:
- If the closing """ is at the same indent as the content, all indentation is stripped
- If the closing """ is further left, some indentation is preserved
- If the closing """ is further right, it doesn't add indentation (minimum rule)
```

```java
// Closing """ at same level — all indentation stripped
String a = """
        line1
        line2
        """;
// Result: "line1\nline2\n"

// Closing """ further left — preserves 4 spaces of indentation
String b = """
        line1
        line2
    """;
// Result: "    line1\n    line2\n"

// Content at different levels — relative indentation preserved
String c = """
        parent
            child
                grandchild
        """;
// Result: "parent\n    child\n        grandchild\n"
```

### Q3: How does pattern matching for instanceof work, and what are its scope rules?

```text
Pattern matching for instanceof (JEP 394, standard in Java 16, previewed in Java 14-15)
allows you to combine a type test and a variable binding in a single expression:
    if (obj instanceof Type varName) { ... }

Scope rules:
- The pattern variable is in scope only where the compiler can prove the match succeeded.
- In an if-then block: available inside the if body.
- With && (short-circuit AND): available after the && because both sides must be true.
- With || (short-circuit OR): NOT available because the match might not have occurred.
- In negated conditions: available in the else branch (if !(obj instanceof Type t)).
- The variable must be effectively final (can't reassign it).
```

```java
public static String process(Object obj) {
    // Basic pattern matching
    if (obj instanceof String s) {
        return s.toUpperCase(); // s is in scope
    }

    // Combined with guard condition
    if (obj instanceof Integer i && i > 0) {
        return "Positive: " + i; // i in scope because && guarantees match
    }

    // Negated pattern — variable in else branch
    if (!(obj instanceof Double d)) {
        return "Not a double";
    } else {
        return "Double: " + d; // d is in scope here
    }

    // Flow scoping — after the if-return, pattern variable is in scope
    // if (!(obj instanceof Long l)) return "not long";
    // return "Long: " + l; // l is in scope because the only way to reach here is if match succeeded
}
```

### Q4: What are hidden classes and why were they introduced?

```text
Hidden classes (JEP 371) are classes that:
1. Cannot be discovered by other classes — they are not registered in any class loader's
   namespace, so Class.forName() cannot find them.
2. Cannot be used as a superclass, field type, return type, or parameter type by other classes.
3. Can be unloaded when they are no longer reachable, independently of their class loader.
4. Have names that include "/" characters (not valid in source code).

Why introduced:
- Frameworks (Spring, Hibernate) dynamically generate classes at runtime for proxies,
  lambda implementations, and bytecode manipulation.
- Before hidden classes, these generated classes polluted the class loader namespace,
  could not be efficiently garbage collected, and had naming conflicts.
- Since Java 15, lambda expressions use hidden classes internally (previously they used
  anonymous classes via LambdaMetafactory).

Created via: MethodHandles.Lookup.defineHiddenClass(byte[], boolean, options...)
```

```java
// Checking if a class is hidden
Runnable lambda = () -> System.out.println("Hello");
Class<?> lambdaClass = lambda.getClass();
System.out.println(lambdaClass.isHidden());    // true (since Java 15)
System.out.println(lambdaClass.getName());     // Something like: com.example.Main$$Lambda/0x00001234

// Hidden classes for frameworks (conceptual)
// In a real framework, you'd do:
// MethodHandles.Lookup lookup = MethodHandles.lookup();
// Class<?> hidden = lookup.defineHiddenClass(classBytes, true,
//     MethodHandles.Lookup.ClassOption.NESTMATE).lookupClass();
```

### Q5: How do text blocks handle special characters and escape sequences?

```text
Text blocks support all traditional escape sequences (\n, \t, \\, \", etc.) plus
two new ones:
1. \s — a space character that prevents trailing whitespace stripping on that line.
   Useful for preserving significant trailing spaces.
2. \ (at end of line) — suppresses the line terminator, effectively joining the
   current line with the next line.

Important behaviors:
- Single and double quotes don't need escaping inside text blocks.
- Triple quotes (""") need escaping if they appear in the content: \""" or ""\".
- Unicode escapes (\uXXXX) work the same as in regular strings.
- Octal escapes (\0 through \377) work the same as in regular strings.
```

```java
// \s preserves trailing whitespace
String aligned = """
        Name:  John\s\s
        Age:   30\s\s\s
        Email: john@example.com
        """;
// Each line has exactly the trailing spaces marked by \s

// \ suppresses line terminator (line continuation)
String singleLine = """
        This is a very long line that we want to \
        break in source code but not in the output\
        """;
// Result: "This is a very long line that we want to break in source code but not in the output"

// Quotes inside text blocks
String withQuotes = """
        He said "Hello, World!"
        And then said 'Goodbye'
        Triple quotes need escaping: \"""
        """;

// Using formatted() with text blocks
String template = """
        Dear %s,
        Your order #%d has been %s.
        Total: $%.2f
        """.formatted("Alice", 12345, "shipped", 99.99);
```

## Code Examples

- Implementation: [Java15Features.java](src/main/java/com/github/msorkhpar/claudejavatutor/modernjava/Java15Features.java)
- Tests: [Java15FeaturesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/modernjava/Java15FeaturesTest.java)
