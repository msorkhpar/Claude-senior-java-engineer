# 7.4.5. Use Cases and Benefits

## Concept Explanation

Text blocks are not just a cosmetic improvement to Java; they fundamentally change how developers work with multi-line
string content. By removing the noise of escape sequences and concatenation operators, text blocks let you see the actual
content, making code easier to write, review, test, and maintain.

**Real-world analogy**: Consider the difference between reading a book's manuscript in its final typeset form versus
reading it as a pile of encoded telegrams where every space, comma, and line break has been replaced with a code. Text
blocks let you embed content in Java the way it will actually appear, rather than encoding it into a dense, hard-to-parse
format.

The primary use cases for text blocks include:

1. **JSON and API payloads**: Request/response bodies, test fixtures, configuration.
2. **SQL queries**: Complex queries with joins, subqueries, and formatting.
3. **HTML/XML templates**: Email templates, generated markup, configuration files.
4. **Code generation**: Generating Java, SQL, or other source code programmatically.
5. **Test assertions**: Expected output strings for testing.
6. **Configuration snippets**: YAML, properties, and other config formats.
7. **Documentation strings**: Multi-line error messages, help text, usage instructions.

## Key Points to Remember

1. Text blocks improve readability by preserving the natural structure of embedded content.
2. They reduce bugs caused by missing escape sequences or concatenation operators.
3. They make code reviews easier because reviewers can see the actual content.
4. They are ideal for any content that is typically multi-line or contains quotes.
5. They are compile-time features with zero runtime overhead.
6. The `formatted()` method enables safe parameterization of text block templates.
7. Text blocks integrate well with testing frameworks for assertion values.
8. They do NOT replace external template files for very large templates (e.g., full HTML pages).

## Relevant Java 21 Features

- Text blocks are a mature, standard feature in Java 21.
- They are used extensively in the JDK's own source code and documentation.
- Combined with records (Java 16), sealed classes (Java 17), and pattern matching (Java 21), text blocks
  contribute to a more expressive Java language.
- `formatted()` (Java 15) remains the preferred way to parameterize text blocks.
- Virtual threads (Java 21) often use text blocks for structured logging and error messages.

## Common Pitfalls and How to Avoid Them

1. **Using text blocks for single-line strings**:
   ```java
   // Overkill for a single line
   String bad = """
           Hello, World!""";
   // Better
   String good = "Hello, World!";
   ```

2. **Neglecting SQL injection when using `formatted()` with user input**:
   ```java
   // DANGEROUS: SQL injection risk
   String query = """
           SELECT * FROM users WHERE name = '%s'""".formatted(userInput);
   
   // SAFE: Use parameterized queries
   String query = "SELECT * FROM users WHERE name = ?";
   preparedStatement.setString(1, userInput);
   ```

3. **Embedding very large templates inline instead of using resource files**:
   ```java
   // If the template is hundreds of lines, use an external file
   String template = new String(getClass().getResourceAsStream("/template.html").readAllBytes());
   ```

4. **Forgetting that `formatted()` returns a new string (not a constant)**:
   ```java
   // Not a compile-time constant - can't use in switch case
   String value = """
           %s""".formatted(input);
   // switch (value) { case """...""": ... } // This is fine
   // But the formatted value can't be a case label
   ```

5. **Over-formatting with `formatted()` when concatenation is simpler**:
   ```java
   // Overkill
   String msg = """
           Error: %s""".formatted(error);
   // Simpler
   String msg = "Error: " + error;
   ```

## Best Practices and Optimization Techniques

1. **Use text blocks for test assertions** to make expected values self-documenting:
   ```java
   assertThat(service.generateReport()).isEqualTo("""
           Report: Q1 2024
           Revenue: $1,000,000
           Growth: 15%""");
   ```

2. **Combine with `formatted()` for template patterns**:
   ```java
   String emailBody = """
           Dear %s,
           
           Your order #%d has been shipped.
           
           Best regards,
           The Team""".formatted(customerName, orderId);
   ```

3. **Use for configuration snippets in tests**:
   ```java
   String config = """
           server:
             port: 8080
             host: localhost""";
   ```

4. **Prefer text blocks for any content with embedded quotes**:
   ```java
   String json = """
           {"users": [{"name": "Alice"}, {"name": "Bob"}]}""";
   ```

5. **Keep text block content focused** - if it grows beyond ~50 lines, consider external files.

6. **Use `indent()` for dynamic indentation in code generation**:
   ```java
   String method = """
           public void %s() {
           %s}""".formatted(methodName, body.indent(4));
   ```

## Edge Cases and Their Handling

1. **Empty JSON/XML**: Use text blocks for readability even with minimal content.
2. **Binary data**: Text blocks are for text; use byte arrays for binary data.
3. **Content with `%` characters**: When using `formatted()`, escape `%` as `%%`.
4. **Very long lines without natural breaks**: Use line continuation `\` for readability.
5. **Content that must end without a newline**: Place closing delimiter on the last content line.
6. **Content that must end with a newline**: Place closing delimiter on its own line.
7. **Mixing text blocks with String.format**: Either works, but `formatted()` is preferred.

## Interview-specific Insights

Interviewers evaluate:

- Practical knowledge of when to use text blocks vs. traditional strings
- Understanding of security implications (SQL injection with `formatted()`)
- Knowledge of `formatted()` vs. `String.format()`
- Ability to identify code that would benefit from text blocks
- Understanding of text blocks in testing contexts
- Knowledge of limitations (not a template engine, not for binary data)

Common tricky questions:

- "Give me three real-world use cases for text blocks."
- "What are the risks of using `formatted()` with user input?"
- "When would you NOT use a text block?"
- "How do text blocks compare to template engines like Thymeleaf or Freemarker?"

## Interview Q&A Section

**Q1: What are the primary real-world use cases for text blocks in Java?**

```text
A1: Text blocks have numerous practical use cases in production Java code:

1. JSON handling: API request/response bodies, test fixtures, configuration.
   Text blocks eliminate the need to escape every double quote in JSON.

2. SQL queries: Complex queries with joins, CTEs, and formatting. The natural
   indentation of SQL is preserved without concatenation.

3. HTML/XML templates: Email templates, generated markup, SVG content.
   Angle brackets and attributes with quotes are written naturally.

4. Test assertions: Expected output strings become self-documenting.
   The test reader can see exactly what the output should look like.

5. Code generation: Generating source code (Java, SQL, scripts) where
   indentation and formatting matter.

6. Configuration snippets: YAML, TOML, properties files embedded in code
   for testing or default values.

7. Log messages and error reports: Structured multi-line messages that
   are easy to read in logs.

8. Documentation/help text: CLI usage messages, API documentation,
   inline help content.

9. Regular expression documentation: Long regex patterns with comments
   explaining each part.

10. Bash/shell script generation: Scripts that need to be written from Java.

The common thread: any content that is multi-line or contains characters
that would need escaping in traditional strings.
```

```java
public class RealWorldUseCases {
    // 1. JSON API response
    String jsonResponse(int status, String message) {
        return """
                {
                    "status": %d,
                    "message": "%s",
                    "timestamp": "%s"
                }""".formatted(status, message, java.time.Instant.now());
    }

    // 2. Complex SQL query
    String complexQuery = """
            WITH active_users AS (
                SELECT id, name, department_id
                FROM users
                WHERE active = true AND created_at > '2024-01-01'
            )
            SELECT au.name, d.name AS department
            FROM active_users au
            JOIN departments d ON au.department_id = d.id
            ORDER BY au.name""";

    // 3. HTML email template
    String emailTemplate(String name, String content) {
        return """
                <!DOCTYPE html>
                <html>
                <body>
                    <h1>Hello, %s</h1>
                    <p>%s</p>
                </body>
                </html>""".formatted(name, content);
    }

    // 4. Test assertion (in a test class)
    // assertThat(report.generate()).isEqualTo("""
    //         Q1 Report
    //         ---------
    //         Revenue: $1M
    //         Growth: 15%%""");

    // 5. Code generation
    String generateRecord(String name, String field1, String type1) {
        return """
                public record %s(
                    %s %s
                ) {}""".formatted(name, type1, field1);
    }
}
```

**Q2: How do text blocks compare to template engines like Thymeleaf or Freemarker?**

```text
A2: Text blocks and template engines serve different purposes and operate at different
levels of abstraction:

Text blocks:
- Compile-time string literals with no runtime engine
- Simple parameterization via formatted() (%s, %d)
- No logic (no if/else, loops, or conditionals)
- No template inheritance or includes
- Zero runtime overhead (compiled to constant strings)
- Best for: small to medium templates, test fixtures, SQL, configuration

Template engines (Thymeleaf, Freemarker, Mustache):
- Runtime template processing with a full engine
- Rich expression language with conditionals, loops, functions
- Template inheritance, layouts, fragments, includes
- Separate template files with hot-reloading
- Runtime overhead (parsing, evaluation, rendering)
- Best for: web pages, emails, complex documents, user-facing content

When to use text blocks:
- Content is simple and mostly static
- Parameterization is limited to variable substitution
- Template is small (< 50 lines)
- You want compile-time safety and zero overhead
- Content is developer-facing (tests, logs, config)

When to use a template engine:
- Content has conditional logic or loops
- Template is large or complex
- Non-developers need to edit templates
- Template inheritance or composition is needed
- Hot-reloading of templates is desired

They can work together: use text blocks for default template strings
and template engines for complex user-facing content.
```

```java
public class TextBlockVsTemplateEngine {
    // Text block: simple substitution
    String simpleEmail(String name) {
        return """
                Dear %s,
                
                Thank you for your purchase.
                
                Best regards,
                The Team""".formatted(name);
    }

    // For complex logic, you'd need a template engine:
    // Thymeleaf template (in a .html file):
    // <div th:each="item : ${items}">
    //     <p th:if="${item.inStock}" th:text="${item.name}">Item</p>
    // </div>

    // But for test fixtures, text blocks are perfect
    String testFixture = """
            id,name,email,active
            1,Alice,alice@test.com,true
            2,Bob,bob@test.com,false
            3,Charlie,charlie@test.com,true""";

    // For configuration defaults
    String defaultConfig = """
            app.name=MyService
            app.port=8080
            app.log.level=INFO""";
}
```

**Q3: What are the security considerations when using `formatted()` with text blocks?**

```text
A3: The formatted() method performs simple string substitution, which carries
security risks when the parameters come from untrusted sources:

1. SQL Injection:
   - Never use formatted() to build SQL with user input
   - Use PreparedStatement with parameter binding instead
   - formatted() has no SQL escaping or sanitization

2. Cross-Site Scripting (XSS):
   - Never use formatted() to build HTML with user input
   - HTML special characters (<, >, &, ", ') are not escaped
   - Use proper HTML escaping libraries or template engines

3. Command Injection:
   - Never use formatted() to build shell commands with user input
   - Use ProcessBuilder with separate arguments instead

4. Log Injection:
   - User input in log messages can contain newlines or special characters
   - This can corrupt log formatting or inject fake log entries
   - Sanitize user input before including in log messages

5. Format String Attacks:
   - If user input is used as the format string (not just parameters),
     it could contain format specifiers that cause exceptions
   - Always use text blocks as the format template, never user input

Safe uses of formatted():
- Developer-controlled templates with validated/sanitized parameters
- Test fixtures and assertions
- Internal logging with controlled data
- Code generation with known inputs
```

```java
public class SecurityConsiderations {
    // DANGEROUS: SQL injection
    String unsafeQuery(String userInput) {
        return """
                SELECT * FROM users WHERE name = '%s'
                """.formatted(userInput);
        // If userInput = "'; DROP TABLE users; --"
        // Result: SELECT * FROM users WHERE name = ''; DROP TABLE users; --'
    }

    // SAFE: Parameterized query
    // String safeQuery = "SELECT * FROM users WHERE name = ?";
    // preparedStatement.setString(1, userInput);

    // DANGEROUS: XSS in HTML
    String unsafeHtml(String userInput) {
        return """
                <div>%s</div>""".formatted(userInput);
        // If userInput = "<script>alert('XSS')</script>"
        // Result: <div><script>alert('XSS')</script></div>
    }

    // SAFE: Escape HTML entities
    String safeHtml(String userInput) {
        String escaped = userInput
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
        return """
                <div>%s</div>""".formatted(escaped);
    }

    // SAFE: Text blocks for test fixtures (no user input)
    String testJson = """
            {"name": "test", "value": 42}""";
}
```

**Q4: How do text blocks improve code readability and maintainability?**

```text
A4: Text blocks improve code quality in several measurable ways:

Readability improvements:
1. Content visibility: The actual structure of JSON, SQL, HTML is visible.
   No mental translation of \n, \t, \" is needed.
2. Reduced noise: No + concatenation operators or escaped characters
   cluttering the content.
3. Natural formatting: Indentation, line breaks, and quotes appear as they
   will in the output.
4. Self-documenting: The text block IS the documentation - what you see
   is what you get.

Maintainability improvements:
1. Fewer bugs: No missing + operators, unbalanced quotes, or forgotten \n.
2. Easy updates: Changing a JSON field or SQL column is a simple text edit.
3. Better diffs: Git diffs show exactly what content changed, not which
   escape sequence was modified.
4. Copy-paste friendly: You can copy SQL from a database tool and paste it
   directly into a text block.
5. Validation: You can visually verify that the content matches expectations.

Code review improvements:
1. Reviewers can read the embedded content naturally
2. SQL logic errors are visible without mental de-escaping
3. JSON structure is clear at a glance
4. HTML markup is readable

Metrics (typical improvements):
- ~40-60% reduction in character count for JSON/SQL content
- Significantly fewer string-related bugs in code reviews
- Faster content updates (no escape sequence management)
```

```java
public class ReadabilityComparison {
    // BEFORE text blocks: 6 lines, 12 escape sequences, 5 concatenation operators
    String jsonBefore = "{\n" +
            "    \"users\": [\n" +
            "        {\"name\": \"Alice\", \"role\": \"admin\"},\n" +
            "        {\"name\": \"Bob\", \"role\": \"user\"}\n" +
            "    ]\n" +
            "}";

    // AFTER text blocks: 6 lines, 0 escape sequences, 0 concatenation operators
    String jsonAfter = """
            {
                "users": [
                    {"name": "Alice", "role": "admin"},
                    {"name": "Bob", "role": "user"}
                ]
            }""";

    // Both produce identical strings at runtime

    // BEFORE: SQL query (hard to spot logical errors)
    String sqlBefore = "SELECT u.name, u.email, d.dept_name\n" +
            "FROM users u\n" +
            "INNER JOIN departments d ON u.dept_id = d.id\n" +
            "WHERE u.active = true\n" +
            "  AND u.created_at > '2024-01-01'\n" +
            "ORDER BY u.name ASC";

    // AFTER: SQL query (easy to read and verify)
    String sqlAfter = """
            SELECT u.name, u.email, d.dept_name
            FROM users u
            INNER JOIN departments d ON u.dept_id = d.id
            WHERE u.active = true
              AND u.created_at > '2024-01-01'
            ORDER BY u.name ASC""";
}
```

**Q5: When should you NOT use text blocks?**

```text
A5: Despite their benefits, text blocks are not always the best choice:

1. Single-line strings:
   - "Hello, World!" is simpler than a text block
   - Text blocks add ceremony (""" and newline) for no benefit

2. Empty or near-empty strings:
   - "" is cleaner than """\n"""
   - Short strings don't benefit from multi-line formatting

3. Strings without special characters or line breaks:
   - "status=active" doesn't benefit from text block syntax
   - Traditional strings are more concise

4. Dynamic strings built entirely at runtime:
   - StringBuilder or String.join() may be more appropriate
   - Text blocks are compile-time features

5. Binary or encoded data:
   - Base64, hex strings, and binary data should use byte arrays
   - Text blocks are for human-readable text

6. Very large templates (100+ lines):
   - Use external resource files (.sql, .html, .json)
   - Large inline strings clutter the source code
   - External files support hot-reloading and non-developer editing

7. Content requiring complex logic:
   - Template engines (Thymeleaf, Freemarker) are better for conditionals/loops
   - Text blocks only support simple substitution via formatted()

8. Performance-critical loops:
   - Calling formatted() in a tight loop creates many intermediate strings
   - Pre-compute or use StringBuilder for hot paths

Rule of thumb: use text blocks when the content has multiple lines OR
contains characters that need escaping. Otherwise, stick to traditional strings.
```

```java
public class WhenNotToUseTextBlocks {
    // DON'T: Single-line string
    String bad1 = """
            Hello"""; // Use "Hello" instead

    // DON'T: Empty string
    // String bad2 = """
    //         """; // Use "" instead

    // DON'T: Simple key-value
    String bad3 = """
            active"""; // Use "active" instead

    // DO: Multi-line structured content
    String good1 = """
            {
                "name": "Alice",
                "age": 30
            }""";

    // DO: Content with quotes
    String good2 = """
            She said "Hello" and he said "Goodbye".""";

    // DON'T: Very large templates - use resource files
    // String bad4 = """ ... 200 lines of HTML ... """;
    // Instead:
    // String good3 = Files.readString(Path.of("template.html"));

    // DON'T: Dynamic content with logic
    // Use a template engine instead
    // String bad5 = """
    //         ${if user.active}Active${else}Inactive${endif}""";
}
```

**Q6: How do text blocks improve testing in Java?**

```text
A6: Text blocks significantly improve the testing experience in several ways:

1. Readable expected values:
   - Expected output in assertions is visible and natural
   - No escape sequences obscure the expected content
   - Test failures show a clear diff between expected and actual

2. Test fixture creation:
   - JSON, XML, CSV test data can be written inline
   - No need for external fixture files for small data
   - The data is visible right next to the test code

3. Request/response mocking:
   - WireMock stubs with JSON bodies are easy to write
   - HTTP headers and bodies are naturally formatted

4. SQL test queries:
   - Complex test queries are readable
   - Easy to verify query correctness

5. Multi-line assertion messages:
   - Custom failure messages can be multi-line and informative

6. Snapshot testing:
   - Expected snapshots can be stored as text blocks
   - Updates are easy to review in pull requests

7. Code generation testing:
   - Expected generated code is visible and verifiable
   - Indentation and formatting are preserved

Best practices for testing with text blocks:
- Keep test text blocks focused and minimal
- Use formatted() for parameterized test data
- For large fixtures, consider @ParameterizedTest with external sources
```

```java
public class TestingWithTextBlocks {
    // Readable assertion
    void testJsonOutput() {
        String actual = service.generateUser("Alice", 30);
        // assertThat(actual).isEqualTo("""
        //         {
        //             "name": "Alice",
        //             "age": 30,
        //             "active": true
        //         }""");
    }

    // Test fixture
    String csvFixture = """
            id,name,email
            1,Alice,alice@test.com
            2,Bob,bob@test.com""";

    // WireMock stub body
    String mockResponse = """
            {
                "status": 200,
                "data": [
                    {"id": 1, "name": "Product A"},
                    {"id": 2, "name": "Product B"}
                ]
            }""";

    // SQL for test setup
    String setupSql = """
            INSERT INTO users (id, name, email, active) VALUES
            (1, 'Alice', 'alice@test.com', true),
            (2, 'Bob', 'bob@test.com', false)""";

    // Multi-line error message in assertion
    // assertThat(result)
    //     .as("""
    //         Expected the service to return active users only.
    //         Input: all users
    //         Filter: active = true
    //         Expected count: 5""")
    //     .hasSize(5);

    // Parameterized test data
    String createTestCase(String name, int age, boolean expected) {
        return """
                {"name": "%s", "age": %d, "expected": %b}"""
                .formatted(name, age, expected);
    }
}
```

## Code Examples

- Test: [TextBlockUseCasesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/textblocks/TextBlockUseCasesTest.java)
- Source: [TextBlockUseCases.java](src/main/java/com/github/msorkhpar/claudejavatutor/textblocks/TextBlockUseCases.java)
