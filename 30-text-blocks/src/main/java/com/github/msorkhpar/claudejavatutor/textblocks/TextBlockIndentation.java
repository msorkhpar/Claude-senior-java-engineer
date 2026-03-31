package com.github.msorkhpar.claudejavatutor.textblocks;

/**
 * Demonstrates formatting and indentation rules for Java text blocks.
 * Text blocks use "incidental whitespace" removal to determine final content.
 */
public class TextBlockIndentation {

    /**
     * Demonstrates incidental whitespace removal.
     * The compiler removes the common leading whitespace from all content lines
     * and the line containing the closing delimiter.
     */
    public static String incidentalWhitespaceRemoval() {
        // The closing delimiter is at the same indentation as the content,
        // so all leading whitespace (8 spaces of indentation) is removed.
        return """
                Line A
                Line B
                Line C""";
    }

    /**
     * Demonstrates how the closing delimiter position controls indentation.
     * Moving the closing delimiter left retains more whitespace in the content.
     */
    public static String closingDelimiterLeftOfContent() {
        return """
                    Line A
                    Line B
                    Line C
                """;
    }

    /**
     * Demonstrates essential whitespace: whitespace that is part of the content.
     * Indentation beyond the common prefix is preserved.
     */
    public static String essentialWhitespace() {
        return """
                No indent
                    Four spaces indent
                        Eight spaces indent""";
    }

    /**
     * Demonstrates mixed indentation within a text block.
     */
    public static String mixedIndentation() {
        return """
                root
                    child1
                        grandchild1
                    child2""";
    }

    /**
     * Demonstrates controlling trailing whitespace.
     * By default, trailing whitespace on each line is stripped.
     */
    public static String trailingWhitespaceStripped() {
        // Trailing spaces after "Hello" are stripped by the compiler
        return """
                Hello\s
                World\s""";
    }

    /**
     * Demonstrates preserving trailing whitespace using the \s escape.
     * The \s escape translates to a single space and prevents trailing whitespace stripping.
     */
    public static String preserveTrailingWithEscapeS() {
        return """
                Hello\s
                World\s""";
    }

    /**
     * Demonstrates the indent() method for programmatic indentation.
     */
    public static String programmaticIndent(int spaces) {
        return """
                Line 1
                Line 2
                Line 3""".indent(spaces);
    }

    /**
     * Demonstrates the stripIndent() method.
     * This is the same algorithm the compiler uses for text blocks,
     * but can be applied to any string.
     */
    public static String manualStripIndent() {
        String s = "    Line 1\n    Line 2\n    Line 3";
        return s.stripIndent();
    }

    /**
     * Demonstrates that blank lines do not affect incidental whitespace calculation.
     */
    public static String blankLinesIgnoredForWhitespace() {
        return """
                Line 1

                Line 3""";
    }

    /**
     * Demonstrates nested indentation for code generation use cases.
     */
    public static String codeGenerationExample(String className, String methodName) {
        return """
                public class %s {

                    public void %s() {
                        System.out.println("Generated");
                    }
                }""".formatted(className, methodName);
    }

    /**
     * Demonstrates the translateEscapes() method added in Java 15.
     * Converts escape sequences in a regular string the same way
     * the compiler processes them.
     */
    public static String translateEscapesDemo() {
        String raw = "Hello\\nWorld\\tTab";
        return raw.translateEscapes();
    }
}
