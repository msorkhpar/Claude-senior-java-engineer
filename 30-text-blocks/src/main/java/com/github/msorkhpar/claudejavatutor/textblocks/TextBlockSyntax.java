package com.github.msorkhpar.claudejavatutor.textblocks;

/**
 * Demonstrates the syntax and usage of text blocks in Java (JEP 378).
 * Text blocks were introduced as a preview in Java 13, second preview in Java 14,
 * and finalized in Java 15.
 */
public class TextBlockSyntax {

    /**
     * Basic text block with opening delimiter on its own line.
     * The content begins on the line following the opening triple quotes.
     */
    public static String basicTextBlock() {
        return """
                Hello, World!""";
    }

    /**
     * A text block with multiple lines.
     */
    public static String multiLineTextBlock() {
        return """
                Line 1
                Line 2
                Line 3""";
    }

    /**
     * Demonstrates content written at column 0, with the closing delimiter on the
     * last content line. There is no leading whitespace to remove, so the result is
     * the same as indented content with the closing delimiter aligned to it.
     * (A closing delimiter at column 0 on its OWN line would instead keep all of the
     * content's source indentation.)
     */
    public static String closingDelimiterAtStart() {
        return """
Line 1
Line 2
Line 3""";
    }

    /**
     * Demonstrates a text block with the closing delimiter on its own line.
     * This adds a trailing newline to the content.
     */
    public static String textBlockWithTrailingNewline() {
        return """
                Hello, World!
                """;
    }

    /**
     * An empty text block. The closing delimiter on the next line
     * at the same indentation produces an empty string.
     */
    public static String emptyTextBlock() {
        return """
                """;
    }

    /**
     * A text block containing a single blank line (produces "\n").
     */
    public static String singleBlankLine() {
        return """

                """;
    }

    /**
     * Demonstrates that text blocks are regular String objects.
     */
    public static boolean textBlockIsString() {
        String textBlock = """
                Hello""";
        String regular = "Hello";
        return textBlock.equals(regular) && textBlock == regular.intern();
    }

    /**
     * Text blocks can be used anywhere a String is expected.
     */
    public static int textBlockLength() {
        String block = """
                ABCDE""";
        return block.length();
    }

    /**
     * Text blocks can be concatenated with other strings.
     */
    public static String concatenateWithTextBlock(String suffix) {
        return """
                Hello""" + " " + suffix;
    }

    /**
     * Demonstrates using text blocks with String methods.
     */
    public static String textBlockToUpperCase() {
        return """
                hello world""".toUpperCase();
    }

    /**
     * Demonstrates that text blocks support the formatted() instance method (Java 15+).
     */
    public static String textBlockFormatted(String name, int value) {
        return """
                Name: %s
                Value: %d""".formatted(name, value);
    }

    /**
     * Demonstrates using text blocks for JSON content.
     */
    public static String jsonTextBlock() {
        return """
                {
                    "name": "Alice",
                    "age": 30,
                    "active": true
                }""";
    }

    /**
     * Demonstrates using text blocks for SQL content.
     */
    public static String sqlTextBlock() {
        return """
                SELECT e.name, d.department_name
                FROM employees e
                JOIN departments d ON e.dept_id = d.id
                WHERE e.active = true
                ORDER BY e.name ASC""";
    }

    /**
     * Demonstrates using text blocks for XML content.
     */
    public static String xmlTextBlock() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>demo</artifactId>
                </project>""";
    }
}
