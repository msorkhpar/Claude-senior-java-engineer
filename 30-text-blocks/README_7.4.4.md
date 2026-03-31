# 7.4.4. Escape Sequences and Special Characters

## Concept Explanation

Java text blocks support all the escape sequences available in traditional string literals, plus two new escape sequences
introduced specifically for text blocks in Java 14/15. Understanding how escape sequences work within text blocks is
crucial because text blocks process escapes **after** incidental whitespace removal, which affects the final result.

**Real-world analogy**: Think of escape sequences as special instructions embedded in a letter. The postman
(incidental whitespace removal) handles the envelope and packaging first, then the recipient reads the letter and
follows the embedded instructions (escape sequence processing). The order matters - the instructions are interpreted
only after the packaging has been removed.

The two new escape sequences specific to text blocks are:

1. **`\s`** (space escape): Translates to a single space character (`\u0020`). Its primary purpose is to prevent
   trailing whitespace stripping on a line.
2. **`\` (line continuation)**: When a backslash appears at the end of a line, it suppresses the line terminator,
   effectively joining the current line with the next one.

## Key Points to Remember

1. Text blocks support all traditional escape sequences: `\n`, `\t`, `\\`, `\"`, `\'`, `\b`, `\f`, `\r`, octal
   escapes.
2. Double quotes (`"`) generally do NOT need escaping in text blocks (unlike traditional strings).
3. Three consecutive double quotes (`"""`) inside a text block require at least one to be escaped (`\"""`).
4. The `\s` escape preserves trailing whitespace and translates to a space character.
5. The `\` (line continuation) at end of line suppresses the newline, joining adjacent lines.
6. Escape sequences are processed in Step 3, AFTER line normalization and whitespace removal.
7. Unicode escapes (`\uXXXX`) are processed by the lexer BEFORE text block processing begins.
8. The `\0` (null character) escape works in text blocks just as in traditional strings.

## Relevant Java 21 Features

- The `\s` and `\` (line continuation) escapes were introduced in JEP 368 (Java 14, Second Preview) and standardized in
  JEP 378 (Java 15).
- In Java 21, these escapes are fully standard and widely used.
- `String.translateEscapes()` (Java 15) can process escape sequences at runtime.
- The escape sequences work identically in both text blocks and traditional string literals (since Java 15).

## Common Pitfalls and How to Avoid Them

1. **Assuming double quotes always need escaping**:
   ```java
   // In traditional strings, quotes must be escaped
   String traditional = "He said \"Hello\""; // Required

   // In text blocks, quotes don't need escaping
   String textBlock = """
           He said "Hello" """; // Works fine!
   
   // Exception: three consecutive quotes need escaping
   String tripleQuote = """
           The delimiter is \""" """;
   ```

2. **Forgetting that `\s` is a full space character, not just a marker**:
   ```java
   // \s adds an actual space to the output
   String block = """
           Hello\s""";
   // Result: "Hello " (5 chars + 1 space = 6 chars)
   // Not just "Hello" with trailing whitespace preserved
   ```

3. **Using `\n` when a natural line break would suffice**:
   ```java
   // Unnecessary: using \n inside a text block
   String bad = """
           Line 1\nLine 2""";
   // Result: "Line 1\nLine 2" - works but defeats the purpose of text blocks
   
   // Better: use natural line breaks
   String good = """
           Line 1
           Line 2""";
   ```

4. **Line continuation producing unexpected results with indentation**:
   ```java
   // The continuation joins lines BEFORE whitespace removal
   // Actually, escapes are processed AFTER whitespace removal
   String block = """
           Hello \
           World""";
   // Result: "Hello World" (newline suppressed, next line's indent removed by Step 2)
   ```

5. **Confusing `\` (line continuation) with `\\` (literal backslash)**:
   ```java
   // Line continuation: suppresses newline
   String continuation = """
           Hello \
           World""";
   // Result: "Hello World"
   
   // Literal backslash at end of line
   String literal = """
           Hello \\
           World""";
   // Result: "Hello \\\nWorld" -- actually "Hello \\" followed by newline then "World"
   ```

## Best Practices and Optimization Techniques

1. **Use `\s` only when trailing whitespace is meaningful**:
   ```java
   // Good: preserving alignment in table formatting
   String table = """
           Name  \s
           Alice \s
           Bob   \s""";
   
   // Bad: using \s unnecessarily
   String unnecessary = """
           Hello\s
           World\s""";
   // If trailing space doesn't matter, leave it off
   ```

2. **Use line continuation for very long single-line strings**:
   ```java
   String longLine = """
           This is a very long string that would exceed \
           the line length limit in your IDE, so we break \
           it across multiple source lines for readability.""";
   // Result is a single line
   ```

3. **Let text blocks handle quote escaping naturally**:
   ```java
   // Text blocks shine with JSON, SQL, etc. that use quotes
   String json = """
           {"key": "value", "nested": {"inner": "data"}}""";
   // No escaping needed for the double quotes!
   ```

4. **Be explicit about backslashes**:
   ```java
   // Always double backslashes for literal backslashes
   String path = """
           C:\\Users\\admin\\Documents""";
   // Even in text blocks, backslash is still an escape character
   ```

## Edge Cases and Their Handling

1. **Two consecutive double quotes at end of text block**: Requires escaping to avoid ambiguity with closing delimiter.
2. **Empty line with `\s`**: The `\s` creates a line with a single space.
3. **Line continuation on the last content line**: Joins with the closing delimiter, which is unusual.
4. **Multiple `\s` on the same line**: Each `\s` adds one space; they can be chained.
5. **`\s` followed by other whitespace**: The `\s` prevents stripping of the whitespace before it.
6. **Unicode escapes in text blocks**: Processed by the lexer before text block processing, so `\u000A` (newline) would
   break the text block syntax.

## Interview-specific Insights

Interviewers often test:

- Knowledge of the two new escape sequences (`\s` and `\`)
- Understanding of when escaping is needed vs. not needed in text blocks
- The order of processing (line normalization -> whitespace removal -> escape processing)
- How `\s` prevents trailing whitespace stripping
- The behavior of unicode escapes vs. regular escapes in text blocks
- Edge cases with consecutive quotes near the closing delimiter

Common tricky questions:

- "Do you need to escape double quotes in text blocks?"
- "What does `\s` do and why was it introduced?"
- "What happens if you put `\u000A` in a text block?"
- "How does line continuation interact with indentation?"

## Interview Q&A Section

**Q1: What new escape sequences were introduced with text blocks, and why were they needed?**

```text
A1: Two new escape sequences were introduced in Java 14 (JEP 368) alongside text blocks:

1. \s (space escape):
   - Translates to a single space character (U+0020)
   - Primary purpose: prevent trailing whitespace stripping
   - Without \s, the compiler strips all trailing whitespace from each line
   - Placing \s at the end of a line acts as a "fence" - it and everything before
     it on that line are preserved
   - Use case: aligning columns, preserving significant trailing spaces

2. \ (line continuation / line terminator escape):
   - When placed at the end of a line, it suppresses the newline character
   - The current line is joined with the next line
   - Use case: writing very long strings that should be a single line in the output
     while keeping the source code readable
   - Similar to line continuation in shell scripts and some other languages

Why they were needed:
- \s solves the problem of meaningful trailing whitespace being silently removed
- \ solves the problem of very long single-line strings being unreadable in source code
- Both issues are specific to text blocks because traditional strings don't have
  automatic trailing whitespace stripping or multi-line content
```

```java
public class NewEscapeSequences {
    // \s example: preserving trailing whitespace
    String withTrailing = """
            First \s
            Second\s""";
    // Result: "First  \nSecond " (trailing spaces preserved)

    // Without \s, trailing spaces would be stripped
    String withoutTrailing = """
            First   
            Second""";
    // Result: "First\nSecond" (trailing spaces gone!)

    // \ example: line continuation for long strings
    String longUrl = """
            https://www.example.com\
            /api/v2\
            /users\
            ?active=true""";
    // Result: "https://www.example.com/api/v2/users?active=true"

    // Combining both: long line with trailing space
    String combined = """
            This is a long line that continues \
            and ends with a significant space\s""";
    // Result: "This is a long line that continues and ends with a significant space "
}
```

**Q2: Do double quotes need to be escaped in text blocks?**

```text
A2: In most cases, NO - double quotes do NOT need escaping in text blocks.
This is one of the key advantages of text blocks over traditional strings.

Rules:
1. Single double quotes (") - never need escaping
2. Two consecutive double quotes ("") - never need escaping
3. Three consecutive double quotes (""") - at least ONE must be escaped
   because """ is the text block closing delimiter

The three-quote rule:
- The compiler scans for the first unescaped """ to find the closing delimiter
- If your content needs """, escape at least one: \"""  or "\"" or ""\""
- Four or more quotes: escape as needed to avoid any unescaped """

This makes text blocks ideal for JSON, HTML, and SQL where double quotes
appear frequently but three consecutive quotes are rare.
```

```java
public class QuoteEscaping {
    // Single quotes - no escaping needed
    String single = """
            He said "Hello" to her.""";
    // Result: He said "Hello" to her.

    // Double consecutive quotes - no escaping needed
    String doubleQuote = """
            The value is ""empty"".""";
    // Result: The value is ""empty"".

    // Triple quotes - must escape at least one
    String tripleQuote = """
            Text block delimiter: \""" """;
    // Result: Text block delimiter: """

    // Alternative escaping for triple quotes
    String alt1 = """
            Delimiter: "\\"\\"\\" """;  // Each quote escaped individually

    // JSON example - quotes never need escaping
    String json = """
            {
                "name": "Alice",
                "greeting": "She said, "Hello!""
            }""";
    // Note: "Hello!" with surrounding quotes works fine

    // Contrast with traditional string
    String traditional = "{\"name\": \"Alice\", \"greeting\": \"She said, \\\"Hello!\\\"\"}";
    // Much harder to read!
}
```

**Q3: How does the order of text block processing affect escape sequences?**

```text
A3: The order of the three processing steps is critical because each step
operates on the result of the previous step:

Step 1: Line terminator normalization (CR, CRLF -> LF)
- Happens first
- All line endings become \n
- This does NOT affect escape sequences in the content

Step 2: Incidental whitespace removal
- Happens second
- Removes common leading whitespace
- Strips trailing whitespace from each line
- The \s escape is still a literal backslash-s at this point
- Trailing whitespace before \s IS stripped (but \s prevents further stripping)

Step 3: Escape sequence interpretation
- Happens last
- \s becomes a space character (preventing the already-stripped trailing whitespace issue
  is because the compiler actually handles \s specially during step 2)
- \<newline> suppresses the newline (line continuation)
- \n, \t, \\, \" etc. are all processed
- Invalid escape sequences cause a compilation error

Important detail about \s and step 2:
- The compiler actually recognizes \s during trailing whitespace stripping
- It treats \s as a "fence" that stops the stripping
- Characters before \s on the same line are preserved
- Then in step 3, \s is translated to a space

This ordering means escape sequences in text blocks are predictable
and consistent with their behavior in traditional strings.
```

```java
public class ProcessingOrder {
    // Step 2 removes indentation, Step 3 processes escapes
    String example = """
            Hello\tWorld""";
    // After Step 1: (normalized line endings)
    // After Step 2: "Hello\tWorld" (indentation removed; \t is still literal)
    // After Step 3: "Hello\tWorld" (but now \t is an actual tab character)

    // \s interacts with Step 2
    String spaceExample = """
            Text   \s""";
    // After Step 2: "Text   \s" (trailing whitespace preserved because of \s fence)
    // After Step 3: "Text    " (the \s becomes a space, so 4 trailing spaces total)

    // Line continuation interacts with Step 2
    String continuation = """
            Hello \
            World""";
    // After Step 2: "Hello \\\nWorld" (indentation removed, \ is still literal)
    // After Step 3: "Hello World" (\ suppresses newline, lines joined)
    // Note: "World" has no leading space because Step 2 removed the indentation
}
```

**Q4: How do you handle backslashes in text blocks, especially for regex patterns and file paths?**

```text
A4: Backslashes in text blocks work exactly the same as in traditional strings -
they are escape characters and must be doubled for a literal backslash.

Common scenarios:

1. File paths (Windows):
   - Traditional: "C:\\Users\\admin"
   - Text block: same - """C:\\Users\\admin"""
   - Text blocks do NOT help with backslash escaping

2. Regular expressions:
   - Regex \d+ in traditional: "\\d+"
   - Regex \d+ in text block: """\\d+"""
   - Same double-escaping is required

3. JSON with backslashes:
   - Same escaping rules apply

Why text blocks don't help with backslashes:
- Text blocks process escape sequences (Step 3)
- A single backslash starts an escape sequence
- To get a literal backslash in the output, you MUST use \\
- This was a deliberate design choice to maintain consistency
  with traditional strings

Alternative for regex:
- Consider Pattern.compile() with named constants
- Use character classes where possible to avoid escaping
- Java has no "raw string" feature (unlike Kotlin or Python)
```

```java
public class BackslashHandling {
    // File paths - same escaping as traditional strings
    String windowsPath = """
            C:\\Users\\admin\\Documents\\file.txt""";
    // Result: C:\Users\admin\Documents\file.txt

    // Unix paths - no escaping needed (no backslashes)
    String unixPath = """
            /home/user/documents/file.txt""";

    // Regex patterns - still need double escaping
    String emailRegex = """
            ^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$""";
    // Result: ^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$

    // Complex regex - text blocks help with readability but not escaping
    String complexRegex = """
            (?x)           # Enable comments mode
            ^              # Start of string
            [a-zA-Z0-9]+   # Alphanumeric characters
            @              # Literal @
            [a-zA-Z0-9.-]+ # Domain
            \\.            # Literal dot
            [a-zA-Z]{2,}   # TLD
            $              # End of string""";

    // JSON with escaped characters
    String jsonWithBackslash = """
            {"path": "C:\\\\Users\\\\admin"}""";
    // Result: {"path": "C:\\Users\\admin"}
    // Four backslashes in source -> two in output (JSON requires its own escaping)
}
```

**Q5: What happens with unicode escapes in text blocks?**

```text
A5: Unicode escapes (\uXXXX) are handled differently from all other escape sequences
in text blocks. They are processed by the Java LEXER, not by the text block compiler.

Processing timeline:
1. Lexer processes \uXXXX escapes (very early, before parsing)
2. Parser identifies the text block
3. Step 1: Line terminator normalization
4. Step 2: Incidental whitespace removal
5. Step 3: Regular escape sequence processing

Implications:
- \u000A (newline) would create an actual newline in the SOURCE CODE
  before the text block is even identified, potentially breaking the syntax
- \u0022 (double quote) would create an actual quote character in the source,
  potentially terminating the text block prematurely
- Other unicode escapes (like \u0041 for 'A') work fine because they don't
  affect the text block structure

Best practice:
- Avoid \u000A and \u000D in text blocks
- Use \n and \r instead (processed in Step 3, safely)
- Non-structural unicode escapes work fine (\u263A for smiley, etc.)
- For problematic unicode values, use the actual Unicode character in source
  (modern editors support this)
```

```java
public class UnicodeEscapes {
    // Safe unicode escapes - non-structural characters
    String safe = """
            Smiley: \u263A
            Heart: \u2665
            Copyright: \u00A9""";
    // Result: "Smiley: ☺\nHeart: ♥\nCopyright: ©"

    // DANGEROUS: \u000A is a newline - processed by lexer!
    // String broken = """
    //         Hello\u000AWorld""";
    // This would break because the lexer creates a newline BEFORE
    // the text block is parsed, splitting the line

    // DANGEROUS: \u0022 is a double quote - processed by lexer!
    // String alsobroken = """
    //         Quote: \u0022""";
    // This would break because the lexer creates a quote

    // Safe alternative: use regular escape sequences
    String safeNewline = """
            Hello\nWorld""";
    // Result: "Hello\nWorld" (actual newline)

    String safeQuote = """
            Quote: \"""";
    // Result: "Quote: \""
}
```

**Q6: How does the line continuation escape (`\`) work and when should you use it?**

```text
A6: The line continuation escape (\) at the end of a line suppresses the line
terminator, joining the current line with the next.

How it works:
1. Write content on a line
2. End the line with \ (backslash)
3. The newline after \ is suppressed
4. The next line's content is appended directly
5. Leading whitespace on the next line (incidental) is still removed

Key behaviors:
- The \ must be the last character on the line (before the line terminator)
- Whitespace after \ but before the line terminator is NOT allowed
  (the compiler would see it as an invalid escape sequence)
- The joined result has no space between the lines unless you explicitly add one
- Incidental whitespace from the next line is removed (Step 2 happens first)

When to use:
1. Very long single-line strings (URLs, queries, messages)
2. When you want readable source code but a single-line output
3. Long format strings where line breaks in source improve readability

When NOT to use:
1. When the content naturally has line breaks (use actual line breaks)
2. When it makes the source harder to read
3. For short strings that fit on one line
```

```java
public class LineContinuation {
    // Basic line continuation
    String singleLine = """
            This is one \
            continuous \
            line.""";
    // Result: "This is one continuous line."

    // Long URL
    String url = """
            https://api.example.com\
            /v2/users\
            ?active=true\
            &limit=100""";
    // Result: "https://api.example.com/v2/users?active=true&limit=100"

    // Long SQL (but usually you want line breaks in SQL)
    String sql = """
            SELECT u.name, u.email, d.name \
            FROM users u \
            JOIN departments d ON u.dept_id = d.id""";
    // Result: one line - usually NOT what you want for SQL

    // Better: keep SQL with natural line breaks
    String betterSql = """
            SELECT u.name, u.email, d.name
            FROM users u
            JOIN departments d ON u.dept_id = d.id""";

    // Adding explicit space before continuation
    String withSpace = """
            Hello\s\
            World""";
    // Result: "Hello World" (\s adds space, \ suppresses newline)

    // Without space, lines are directly joined
    String noSpace = """
            Hello\
            World""";
    // Result: "HelloWorld"
}
```

## Code Examples

- Test: [TextBlockEscapeSequencesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/textblocks/TextBlockEscapeSequencesTest.java)
- Source: [TextBlockEscapeSequences.java](src/main/java/com/github/msorkhpar/claudejavatutor/textblocks/TextBlockEscapeSequences.java)
