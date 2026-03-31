# 7.4.2. Syntax and Usage of Text Blocks

## Concept Explanation

A text block is a multi-line string literal that was introduced as a standard feature in Java 15 (JEP 378). It avoids
the need for most escape sequences, automatically formats the string in a predictable way, and gives the developer
control over the format of the string.

**Real-world analogy**: Think of traditional strings as writing on a single-line label maker versus text blocks as
writing on a full sheet of paper. With the label maker, you need special codes for every line break and special
character. With the paper, you simply write naturally, and the formatting is preserved as-is.

The syntax of a text block is:

```java
String block = """
        content goes here
        across multiple lines
        """;
```

Key syntax rules:
1. A text block begins with three double-quote characters (`"""`), followed by optional whitespace, and a **mandatory
   line terminator** (newline). No content is allowed on the opening delimiter line.
2. The content starts on the next line after the opening delimiter.
3. A text block ends with three double-quote characters (`"""`). The closing delimiter may appear on the same line as
   content or on its own line.
4. Text blocks produce a `String` object and can be used anywhere a `String` is expected.

## Key Points to Remember

1. The opening `"""` must be followed by a newline; content cannot start on the same line.
2. The closing `"""` position determines how much incidental whitespace is removed.
3. Text blocks are compiled to the same bytecode as equivalent traditional strings.
4. Text blocks are interned in the string pool just like regular string literals.
5. Text blocks support the `formatted()` instance method (Java 15+) for parameterization.
6. All standard `String` methods work on text blocks (`length()`, `substring()`, `toUpperCase()`, etc.).
7. Text blocks can be concatenated with `+` just like traditional strings.
8. The type of a text block expression is `java.lang.String`.

## Relevant Java 21 Features

- Text blocks became standard in Java 15 and remain unchanged in Java 21.
- The `formatted()` instance method, introduced alongside text blocks, is the idiomatic way to parameterize text blocks.
- `String.stripIndent()` and `String.translateEscapes()` methods, added in Java 15, provide programmatic access to the
  same algorithms the compiler uses for text blocks.
- In Java 21, text blocks are widely used in the standard library documentation and examples.

## Common Pitfalls and How to Avoid Them

1. **Putting content on the opening delimiter line**:
   ```java
   // Compilation error! Content cannot be on the same line as opening """
   String bad = """content""";
   
   // Correct: content must start on the next line
   String good = """
           content""";
   ```

2. **Forgetting that the closing delimiter position matters**:
   ```java
   // This adds 4 spaces of indentation to every line
   String indented = """
           Hello
           World
       """;
   // Result: "    Hello\n    World\n"
   
   // This removes all common indentation
   String noIndent = """
           Hello
           World""";
   // Result: "Hello\nWorld"
   ```

3. **Unexpected trailing newline**:
   ```java
   // Closing delimiter on its own line adds trailing newline
   String withNewline = """
           Hello
           """;
   // Result: "Hello\n"
   
   // Closing delimiter on the last content line - no trailing newline
   String noTrailingNewline = """
           Hello""";
   // Result: "Hello"
   ```

4. **Assuming text blocks don't process escape sequences**:
   ```java
   // Escape sequences ARE processed in text blocks
   String block = """
           Tab here:\tEnd
           Newline here:\nExtra line""";
   // \t and \n are interpreted as tab and newline characters
   ```

5. **Using `String.format()` instead of `formatted()`**:
   ```java
   // Works but less idiomatic
   String old = String.format("""
           Name: %s
           Age: %d""", name, age);
   
   // Preferred: use the instance method
   String preferred = """
           Name: %s
           Age: %d""".formatted(name, age);
   ```

## Best Practices and Optimization Techniques

1. **Align closing delimiter with the content** to remove all incidental indentation:
   ```java
   String clean = """
           SELECT *
           FROM users
           WHERE active = true""";
   ```

2. **Use `formatted()` for parameterized text blocks**:
   ```java
   String query = """
           SELECT * FROM %s WHERE %s = ?""".formatted(table, column);
   ```

3. **Place closing delimiter on its own line** when a trailing newline is desired (e.g., for file content):
   ```java
   String fileContent = """
           line 1
           line 2
           """; // Trailing newline included
   ```

4. **Use text blocks for test assertions** to make expected values readable:
   ```java
   assertThat(result).isEqualTo("""
           Expected output line 1
           Expected output line 2""");
   ```

5. **Do not use text blocks for single-line strings**; traditional literals are cleaner.

## Edge Cases and Their Handling

1. **Empty text block**: `"""\n"""` produces an empty string `""`.
2. **Text block with only whitespace lines**: The result depends on the closing delimiter position.
3. **Text block with the closing delimiter at column 0**: No indentation is removed.
4. **Text block used in a constant expression**: Text blocks can be used as compile-time constants.
5. **Text block in annotation values**: Text blocks work in annotation string elements.
6. **Concatenation of text blocks**: `"""a""" + """b"""` works but is unusual; use a single text block.

## Interview-specific Insights

Interviewers test understanding of:

- The three-step compile-time processing (line ending normalization, whitespace removal, escape processing)
- How the closing delimiter controls indentation
- The difference between a trailing newline and no trailing newline
- String interning behavior with text blocks
- When to use `formatted()` vs `String.format()`

Common tricky questions:

- "What happens if you put content on the same line as the opening `"""`?"
- "How do you control whether a text block ends with a newline?"
- "Are text blocks the same type as regular strings?"
- "Can you use a text block as a switch case label?"

## Interview Q&A Section

**Q1: What is the basic syntax of a text block and what are the rules for the opening and closing delimiters?**

```text
A1: A text block is delimited by triple double-quote characters (""").

Opening delimiter rules:
- Must be three double-quote characters: """
- May be followed by optional whitespace (spaces/tabs), but NO content
- Must be followed by a line terminator (newline)
- Content begins on the next line

Closing delimiter rules:
- Must be three double-quote characters: """
- Can appear on the same line as the last line of content
- Can appear on its own line (which adds a trailing newline to the result)
- Its column position determines how much leading whitespace is "incidental"
  and gets removed

The content between the delimiters is processed at compile time through three steps:
1. Line terminators are normalized to \n (LF)
2. Incidental whitespace is removed based on the common indent
3. Escape sequences are interpreted

The result is a standard java.lang.String object.
```

```java
// Syntax demonstrations
public class TextBlockSyntaxDemo {
    // Basic text block
    String basic = """
            Hello, World!""";
    // Result: "Hello, World!"

    // Multi-line text block
    String multiLine = """
            Line 1
            Line 2
            Line 3""";
    // Result: "Line 1\nLine 2\nLine 3"

    // With trailing newline (closing delimiter on its own line)
    String trailing = """
            Hello
            """;
    // Result: "Hello\n"

    // Compilation errors:
    // String bad1 = """content""";    // Content on opening line
    // String bad2 = """ content""";   // Content on opening line (after spaces)
}
```

**Q2: How does the position of the closing delimiter affect the text block output?**

```text
A2: The closing delimiter's position is critical because it determines:

1. How much leading whitespace is considered "incidental" and removed
2. Whether the result includes a trailing newline

Indentation rule:
- The compiler calculates the minimum indentation across all non-blank content lines
  AND the closing delimiter line
- This minimum indentation is the "incidental" whitespace that gets stripped
- Any indentation beyond the minimum is preserved as "essential" whitespace

Trailing newline rule:
- If the closing delimiter is on its own line: trailing newline is added
- If the closing delimiter is on the same line as the last content: no trailing newline

This gives developers precise control over the output without any runtime processing.
```

```java
public class ClosingDelimiterPosition {
    // Case 1: Closing delimiter aligned with content - no extra indent
    String noIndent = """
            Hello
            World""";
    // Result: "Hello\nWorld"

    // Case 2: Closing delimiter left of content - content retains relative indent
    String withIndent = """
            Hello
            World
        """;
    // Result: "    Hello\n    World\n"
    // (4 spaces preserved because closing is 4 columns left of content)

    // Case 3: Closing delimiter on its own line, aligned - just adds newline
    String trailingNewline = """
            Hello
            World
            """;
    // Result: "Hello\nWorld\n"

    // Case 4: Closing delimiter at column 0
    String maxIndent = """
            Hello
            World
""";
    // Result: "            Hello\n            World\n"
    // (all source indentation preserved)
}
```

**Q3: Are text blocks the same as regular `String` objects? Can they be used interchangeably?**

```text
A3: Yes, text blocks produce exactly the same java.lang.String objects as traditional
string literals. There is no new type or class.

Key facts:
- The type of a text block expression is String
- Text blocks are interned in the string pool (just like traditional string literals)
- A text block and a traditional string with identical content are == equal
  (when both are compile-time constants)
- All String methods work: length(), charAt(), substring(), equals(), hashCode(), etc.
- Text blocks can be used anywhere a String is expected: variable assignment,
  method arguments, return values, switch cases, annotation values, etc.
- Text blocks can be concatenated with + and other strings
- Text blocks are compile-time constant expressions when they don't use formatted()

The only difference is in source code representation, not in the compiled result.
```

```java
public class TextBlockEquality {
    public static void main(String[] args) {
        String traditional = "Hello\nWorld";
        String textBlock = """
                Hello
                World""";

        // Content equality
        System.out.println(traditional.equals(textBlock)); // true

        // Reference equality (both are interned constants)
        System.out.println(traditional == textBlock); // true

        // All String methods work
        System.out.println(textBlock.length());        // 11
        System.out.println(textBlock.contains("Hello")); // true
        System.out.println(textBlock.toUpperCase());   // "HELLO\nWORLD"
        System.out.println(textBlock.split("\n").length); // 2

        // Text block in switch expression
        String greeting = """
                Hi""";
        switch (greeting) {
            case "Hi" -> System.out.println("Matched!"); // Works
            default -> System.out.println("No match");
        }
    }
}
```

**Q4: How does the `formatted()` method work with text blocks?**

```text
A4: The formatted() instance method was added to String in Java 15, specifically to
complement text blocks. It is equivalent to String.format() but called on the string
instance itself.

Syntax: textBlock.formatted(args...)

How it works:
- It uses the same format specifiers as String.format() (%s, %d, %f, %n, etc.)
- It returns a new String with the format specifiers replaced by the arguments
- It does NOT modify the original string (strings are immutable)

Advantages over String.format():
- More natural reading order: the template comes first, then the arguments
- Chains nicely with text blocks without wrapping in String.format()
- Reads left-to-right: template.formatted(values)

The formatted() method is the idiomatic way to parameterize text blocks in modern Java.
```

```java
public class FormattedMethod {
    // Using formatted() with text blocks
    String createUser(String name, int age, String email) {
        return """
                {
                    "name": "%s",
                    "age": %d,
                    "email": "%s"
                }""".formatted(name, age, email);
    }

    // Compared to String.format() - less readable
    String createUserOldStyle(String name, int age, String email) {
        return String.format("""
                {
                    "name": "%s",
                    "age": %d,
                    "email": "%s"
                }""", name, age, email);
    }

    // Chaining with other String methods
    String createAndUpperCase(String name) {
        return """
                Hello, %s!""".formatted(name).toUpperCase();
        // Result: "HELLO, ALICE!" (if name is "Alice")
    }

    // Multiple parameters
    String sqlInsert(String table, String col1, String col2, Object val1, Object val2) {
        return """
                INSERT INTO %s (%s, %s)
                VALUES ('%s', '%s')""".formatted(table, col1, col2, val1, val2);
    }
}
```

**Q5: Can text blocks be used as compile-time constant expressions?**

```text
A5: Yes, text blocks can be compile-time constant expressions, with the same rules
as traditional string literals:

A text block IS a compile-time constant when:
- It consists entirely of literal content (no formatted() call)
- It doesn't include any non-constant concatenation

A text block is NOT a compile-time constant when:
- formatted() is called on it (the result is computed at runtime)
- It is concatenated with a non-constant expression

Where compile-time constants matter:
1. switch case labels: Only compile-time constants are allowed
2. String interning: Compile-time constant strings are automatically interned
3. Annotation values: Only compile-time constants are allowed
4. Final field optimization: The compiler can inline constant values

This means text blocks can be used in switch cases and annotations without issue,
as long as they don't use formatted() or runtime concatenation.
```

```java
public class TextBlockConstants {
    // Compile-time constant
    static final String GREETING = """
            Hello, World!""";

    // Also a compile-time constant (concatenation of constants)
    static final String FULL = """
            Hello""" + ", " + """
            World!""";

    // NOT a compile-time constant (uses formatted())
    static final String DYNAMIC = """
            Hello, %s!""".formatted("World");

    // Text block in switch case (must be compile-time constant)
    void process(String input) {
        switch (input) {
            case """
                 Hello""" -> System.out.println("Greeting");
            case """
                 Bye""" -> System.out.println("Farewell");
            default -> System.out.println("Unknown");
        }
    }

    // Text block in annotation
    @SuppressWarnings("""
            unchecked""")
    void annotatedMethod() {
    }
}
```

**Q6: What are the three steps the compiler applies to process a text block?**

```text
A6: The Java compiler processes text blocks through three sequential steps at compile time:

Step 1 - Line terminator normalization:
- All line terminators (CR, LF, CRLF) are normalized to LF (\n)
- This ensures consistent behavior across Windows, macOS, and Linux
- The content is the same regardless of the OS where the source code was written

Step 2 - Incidental whitespace removal:
- The compiler determines the "common white space prefix" across all non-blank
  content lines and the closing delimiter line
- This common prefix (incidental whitespace) is removed from the beginning
  of every line
- Trailing whitespace on each line is also removed (unless preserved with \s)
- This step is what makes text blocks insensitive to source code indentation

Step 3 - Escape sequence interpretation:
- All Java escape sequences are processed: \n, \t, \\, \", \', \s, \<newline>, etc.
- This happens AFTER whitespace processing, so \s and \ (line continuation)
  affect the final result after indentation has been resolved
- Unicode escapes (\uXXXX) are processed even earlier (by the lexer), before
  all three steps

The result of these three steps is a plain String constant in the class file.
```

```java
public class ThreeStepProcessing {
    // Step 1: Line terminator normalization
    // Source file may have \r\n (Windows) or \n (Unix) or \r (old Mac)
    // All become \n in the result
    String normalized = """
            Line 1
            Line 2""";
    // Always "Line 1\nLine 2" regardless of source file line endings

    // Step 2: Incidental whitespace removal
    // In source, each line has 12 spaces of indentation
    // The closing delimiter is at column 12 (same alignment)
    // So 12 spaces are "incidental" and removed
    String stripped = """
            Hello
            World""";
    // Result: "Hello\nWorld" (no leading spaces)

    // Step 3: Escape sequence interpretation
    String escaped = """
            Tab:\there
            Quote: \"""";
    // Result: "Tab:\there\nQuote: \""
    // (After step 2 removes indentation, step 3 processes \t and \")
}
```

## Code Examples

- Test: [TextBlockSyntaxTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/textblocks/TextBlockSyntaxTest.java)
- Source: [TextBlockSyntax.java](src/main/java/com/github/msorkhpar/claudejavatutor/textblocks/TextBlockSyntax.java)
