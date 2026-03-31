# 7.4.3. Formatting and Indentation

## Concept Explanation

One of the most elegant aspects of Java text blocks is the automatic handling of indentation. When you embed a text
block in your source code, the indentation caused by your code structure (class nesting, method bodies, etc.) is
automatically removed. This is called **incidental whitespace removal**. Any whitespace beyond the common baseline is
treated as **essential whitespace** and preserved in the output.

**Real-world analogy**: Imagine you receive a letter inside a cardboard box. The box has its own margin padding
(incidental whitespace - caused by the container), but the letter inside has its own formatting and indentation
(essential whitespace - part of the content). The Java compiler automatically "removes the box" and gives you just the
letter with its original formatting intact.

The algorithm works as follows:

1. Determine the minimum number of leading white space characters across all non-blank content lines AND the closing
   delimiter line.
2. Remove that many leading spaces from every line.
3. Strip trailing whitespace from every line (unless preserved with `\s`).

## Key Points to Remember

1. **Incidental whitespace** is the common leading whitespace caused by source code indentation; it is removed.
2. **Essential whitespace** is indentation beyond the common prefix; it is preserved.
3. The closing `"""` delimiter participates in the minimum indent calculation.
4. Blank lines (lines with only whitespace) do NOT participate in the minimum indent calculation.
5. Trailing whitespace on each line is stripped by default.
6. The `\s` escape preserves trailing whitespace.
7. The `indent()` method adds or removes indentation programmatically.
8. The `stripIndent()` method applies the same algorithm as text block compilation to any string.

## Relevant Java 21 Features

- `String.indent(int n)`: Added in Java 12, adjusts indentation by `n` spaces. Positive adds, negative removes
  (minimum 0). Always normalizes line endings and ensures trailing newline.
- `String.stripIndent()`: Added in Java 15, applies the text block indentation algorithm to any string.
- `String.translateEscapes()`: Added in Java 15, processes escape sequences in a string the way the compiler does.
- These methods are available in Java 21 and are the programmatic equivalents of compile-time text block processing.

## Common Pitfalls and How to Avoid Them

1. **Unexpected indentation from closing delimiter position**:
   ```java
   // The closing delimiter is to the left of content, retaining 4 spaces
   String indented = """
           Hello
       """;
   // Result: "    Hello\n" (4 extra spaces!)
   
   // Fix: Align closing delimiter with content
   String clean = """
           Hello
           """;
   // Result: "Hello\n"
   ```

2. **Assuming blank lines affect indentation calculation**:
   ```java
   // The blank line has zero leading spaces, but it doesn't matter
   String block = """
           Line 1
   
           Line 3""";
   // Result: "Line 1\n\nLine 3" (blank lines are excluded from min-indent calculation)
   ```

3. **Losing trailing spaces**:
   ```java
   // Trailing spaces are stripped by default
   String block = """
           Hello   
           World""";
   // Result: "Hello\nWorld" (trailing spaces after "Hello" are gone!)
   
   // Fix: Use \s to preserve trailing whitespace
   String fixed = """
           Hello  \s
           World""";
   // Result: "Hello    \nWorld" (three spaces + \s space preserved)
   ```

4. **Mixing tabs and spaces for indentation**:
   ```java
   // Tabs and spaces don't mix well for indentation
   // The compiler treats them as different characters
   // Stick to spaces only (or tabs only) for consistent results
   ```

5. **Forgetting that `indent()` always adds a trailing newline**:
   ```java
   String s = "hello";
   String indented = s.indent(4);
   // Result: "    hello\n" (note the trailing newline!)
   ```

## Best Practices and Optimization Techniques

1. **Keep the closing delimiter aligned with content** for the cleanest output:
   ```java
   String json = """
           {
               "key": "value"
           }""";
   ```

2. **Use the closing delimiter on its own line** when you need a trailing newline:
   ```java
   String fileContent = """
           First line
           Last line
           """; // Trailing newline for file content
   ```

3. **Use `\s` to preserve significant trailing whitespace**:
   ```java
   String table = """
           Column1   \s
           Column2   \s""";
   ```

4. **Use `indent()` for dynamic indentation** in code generation:
   ```java
   String body = """
           System.out.println("Hello");""";
   String method = """
           public void greet() {
           %s
           }""".formatted(body.indent(4).stripTrailing());
   ```

5. **Understand `stripIndent()` for normalizing external input**:
   ```java
   // Apply text block indentation rules to any string
   String normalized = rawInput.stripIndent();
   ```

## Edge Cases and Their Handling

1. **All lines have different indentation**: The minimum is used as the common prefix.
2. **Content on the closing delimiter line**: Treated as content; the closing delimiter line's indent counts.
3. **Only blank lines in the text block**: The result depends on closing delimiter position.
4. **Single-line text block**: Works but is unusual; traditional string is better.
5. **Negative indent values in `indent()`**: Removes leading spaces (but never fewer than 0 per line).
6. **`indent(0)`**: Does not change indentation but normalizes line endings and ensures trailing newline.

## Interview-specific Insights

Interviewers often focus on:

- How the minimum indent algorithm works step by step
- The role of the closing delimiter in indentation
- The difference between incidental and essential whitespace
- How blank lines are treated in the algorithm
- How to control trailing whitespace with `\s`
- How `indent()` and `stripIndent()` relate to text block compilation

Common tricky questions:

- "What happens if the closing delimiter is indented less than the content?"
- "Do blank lines affect the minimum indentation calculation?"
- "How do you preserve trailing whitespace in a text block?"
- "What does `indent()` do differently from just prepending spaces?"

## Interview Q&A Section

**Q1: Explain the difference between incidental whitespace and essential whitespace in text blocks.**

```text
A1: In Java text blocks, whitespace is categorized into two types:

Incidental whitespace:
- The common leading whitespace shared by ALL non-blank content lines and the
  closing delimiter line
- Caused by the source code's indentation (nesting inside classes, methods, etc.)
- Automatically removed by the compiler
- NOT part of the string's intended content

Essential whitespace:
- Any whitespace beyond the common prefix
- Represents intentional indentation in the string content
- Preserved by the compiler
- IS part of the string's intended content

The algorithm:
1. Find the minimum number of leading spaces across all non-blank content lines
   and the closing delimiter line
2. That minimum is the incidental whitespace
3. Remove it from every line
4. Whatever remains is essential whitespace

This design means you can freely refactor your code's indentation without
changing the text block's content.
```

```java
public class WhitespaceTypes {
    // In this example, all lines have 12 spaces of leading whitespace in source
    // The closing delimiter also has 12 spaces
    // So 12 spaces are "incidental" and removed
    String noEssential = """
            Line 1
            Line 2
            Line 3""";
    // Result: "Line 1\nLine 2\nLine 3"

    // Here, "root" has 12 spaces, "child" has 16, "grandchild" has 20
    // Minimum is 12 (from "root" and closing delimiter)
    // So 12 are incidental, remaining are essential
    String withEssential = """
            root
                child
                    grandchild""";
    // Result: "root\n    child\n        grandchild"
    // "child" has 4 essential spaces, "grandchild" has 8

    // Moving closing delimiter left increases essential whitespace
    String shiftedClosing = """
                Line 1
                Line 2
            """;
    // Minimum indent is 12 (from closing delimiter line)
    // Content has 16 spaces each, so 4 essential spaces per line
    // Result: "    Line 1\n    Line 2\n"
}
```

**Q2: How does the closing delimiter's position control the output indentation?**

```text
A2: The closing delimiter is a critical part of the minimum indent calculation.
It effectively acts as a "margin marker" for the text block.

Rules:
1. If the closing delimiter is aligned with the content:
   - Incidental whitespace equals the content's leading spaces
   - Result has no leading spaces

2. If the closing delimiter is to the LEFT of the content:
   - Incidental whitespace equals the closing delimiter's indent
   - Content retains the difference as essential whitespace

3. If the closing delimiter is to the RIGHT of the content:
   - The content's minimum indent is used (closing doesn't further reduce it)
   - This is less common but valid

4. If the closing delimiter is on the same line as content:
   - No trailing newline is added
   - The line counts as both content and delimiter

This gives you precise control: slide the closing delimiter left to add
indentation, or align it with content to remove all indentation.
```

```java
public class ClosingDelimiterControl {
    // Aligned: no extra indent
    String aligned = """
            Hello""";
    // Result: "Hello"

    // Closing left by 4: adds 4 spaces
    String leftBy4 = """
                Hello
            """;
    // Result: "    Hello\n"

    // Closing at column 0: preserves all source indentation
    String atColumnZero = """
            Hello
""";
    // Result: "            Hello\n"

    // Closing right of content: content's indent used
    String rightOfContent = """
            Hello
                    """;
    // Result: "Hello\n" (closing to the right doesn't help)
}
```

**Q3: How are blank lines treated in the text block indentation algorithm?**

```text
A3: Blank lines (lines containing only whitespace or nothing) receive special treatment
in the text block indentation algorithm:

Key rules:
1. Blank lines are EXCLUDED from the minimum indent calculation.
   They do not count toward determining the common whitespace prefix.

2. After incidental whitespace removal, blank lines become empty lines (\n).

3. Trailing whitespace on blank lines is stripped just like on content lines.

Why this design?
- If blank lines counted toward minimum indent, a blank line with zero leading
  spaces would force the minimum to 0, effectively preserving all indentation.
- By excluding them, blank lines don't accidentally change the output format.

This is important because IDEs may auto-strip whitespace from blank lines,
and different editors handle blank line whitespace differently. By ignoring
them, text blocks produce consistent results regardless of editor settings.
```

```java
public class BlankLineHandling {
    // Blank line between content lines
    String withBlank = """
            Line 1

            Line 3""";
    // The blank line has 0 leading spaces, but it's excluded from min-indent
    // Min-indent is 12 (from "Line 1", "Line 3", and closing delimiter)
    // Result: "Line 1\n\nLine 3"

    // Multiple blank lines
    String multipleBlank = """
            Start


            End""";
    // Result: "Start\n\n\nEnd"

    // Blank line with spaces (editor artifact)
    String blankWithSpaces = """
            Content
               
            More content""";
    // The middle line (with spaces) is blank after trailing whitespace removal
    // It's excluded from min-indent calculation
    // Result: "Content\n\nMore content"
}
```

**Q4: How do the `indent()` and `stripIndent()` methods work?**

```text
A4: These methods provide programmatic access to indentation control:

String.indent(int n):
- If n > 0: prepends n spaces to each line
- If n < 0: removes up to |n| leading whitespace characters per line
- If n == 0: no indentation change
- ALWAYS normalizes line endings to \n
- ALWAYS ensures the result ends with \n
- Returns a new String (strings are immutable)

String.stripIndent():
- Applies the same algorithm as text block compilation
- Finds the minimum leading whitespace across non-blank lines
- Removes that many leading spaces from all lines
- Strips trailing whitespace from each line
- Useful for normalizing strings read from external sources

Key differences from text blocks:
- indent() is a runtime operation on any string
- stripIndent() can be applied to any string, not just text blocks
- Text block processing happens at compile time with zero runtime cost
- indent() guarantees trailing newline; text blocks depend on delimiter position
```

```java
public class IndentMethods {
    public static void main(String[] args) {
        // indent() adding spaces
        String original = "Line 1\nLine 2\nLine 3";
        String indented = original.indent(4);
        // Result: "    Line 1\n    Line 2\n    Line 3\n"
        // Note the trailing newline!

        // indent() removing spaces
        String spacey = "    Line 1\n    Line 2";
        String trimmed = spacey.indent(-2);
        // Result: "  Line 1\n  Line 2\n"

        // indent(0) just normalizes
        String messy = "hello";
        String normalized = messy.indent(0);
        // Result: "hello\n" (trailing newline added!)

        // stripIndent() - text block algorithm on regular string
        String raw = "    Line 1\n    Line 2\n    Line 3";
        String stripped = raw.stripIndent();
        // Result: "Line 1\nLine 2\nLine 3"

        // Combining with text blocks for code generation
        String body = """
                System.out.println("Hello");
                System.out.println("World");""";
        String indentedBody = body.indent(8); // Add 8 spaces for method body
        System.out.println(indentedBody);
    }
}
```

**Q5: How does trailing whitespace handling work in text blocks?**

```text
A5: Text blocks strip trailing whitespace from every line by default.
This includes spaces and tabs at the end of lines.

Default behavior:
- All trailing whitespace on every line is removed during compilation
- This prevents invisible whitespace from affecting string content
- It also means trailing spaces in your source code don't matter

Preserving trailing whitespace with \s:
- The \s escape sequence translates to a single space character
- It is NOT stripped because it's an escape sequence, not raw whitespace
- Characters before \s are also preserved (the stripping stops at \s)
- Use \s at the end of a line to mark the "fence" for trailing whitespace

Why this matters:
1. Test assertions: trailing space differences can cause test failures
2. Markdown/text formatting: some formats rely on trailing spaces
3. Alignment: column-aligned text needs preserved trailing whitespace
4. Code generation: generated code may need exact spacing

The \s escape is unique to text blocks (though it technically works in
regular strings too since Java 15). It was specifically designed for
this trailing whitespace preservation use case.
```

```java
public class TrailingWhitespace {
    // Trailing spaces are stripped
    String stripped = """
            Hello   
            World""";
    // Result: "Hello\nWorld" (trailing spaces after "Hello" removed)

    // \s preserves trailing space
    String preserved = """
            Hello   \s
            World""";
    // Result: "Hello    \nWorld" (three spaces + \s space = 4 spaces preserved)

    // Column alignment example
    String table = """
            Name     \s
            --------\s
            Alice   \s
            Bob     \s""";
    // Trailing spaces preserved for alignment

    // Without \s - alignment is lost
    String tableBroken = """
            Name
            --------
            Alice
            Bob""";
    // No trailing spaces - alignment depends on content length only
}
```

**Q6: How does `translateEscapes()` work and when would you use it?**

```text
A6: String.translateEscapes() was added in Java 15 and processes escape sequences
in a string the same way the Java compiler processes them in string literals.

What it does:
- Takes a string containing literal backslash-character sequences
- Converts them to the corresponding character values
- "\n" becomes a newline character, "\t" becomes a tab, etc.
- Supports: \b, \f, \n, \r, \s, \t, \\, \', \", \0-\377 (octal)
- Throws IllegalArgumentException for invalid escape sequences

When to use it:
1. Processing strings read from files or external sources that contain
   escape sequences in their text representation
2. Building tools that process Java source code or string templates
3. Normalizing strings from configuration files or user input
4. Any situation where you have a string like "Hello\\nWorld" and need
   it to become "Hello\nWorld" (actual newline)

Relation to text blocks:
- It is the programmatic equivalent of Step 3 in text block compilation
- The compiler does this automatically for text blocks
- translateEscapes() lets you do the same thing at runtime
```

```java
public class TranslateEscapesExample {
    public static void main(String[] args) {
        // String read from a file containing literal escape sequences
        String fromFile = "Hello\\nWorld\\tTab";

        // Before translateEscapes: length = 20 (literal \n and \t)
        System.out.println(fromFile.length()); // 20
        System.out.println(fromFile);          // Hello\nWorld\tTab (literal)

        // After translateEscapes: length = 16 (actual newline and tab)
        String translated = fromFile.translateEscapes();
        System.out.println(translated.length()); // 16
        System.out.println(translated);           // Hello
                                                  // World	Tab

        // Error handling: invalid escape throws exception
        try {
            "Invalid \\x escape".translateEscapes();
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid escape: " + e.getMessage());
        }

        // Common use case: processing config file values
        String configValue = "path=C:\\\\Users\\\\admin";
        String actual = configValue.translateEscapes();
        // Result: "path=C:\\Users\\admin" (doubled backslashes become single)
    }
}
```

## Code Examples

- Test: [TextBlockIndentationTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/textblocks/TextBlockIndentationTest.java)
- Source: [TextBlockIndentation.java](src/main/java/com/github/msorkhpar/claudejavatutor/textblocks/TextBlockIndentation.java)
