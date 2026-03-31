package com.github.msorkhpar.claudejavatutor.textblocks;

/**
 * Demonstrates escape sequences and special characters in Java text blocks.
 * Text blocks support all traditional escape sequences plus two new ones:
 * \s (space) and \ (line continuation).
 */
public class TextBlockEscapeSequences {

    /**
     * Demonstrates that double quotes do not need escaping in text blocks.
     */
    public static String doubleQuotesUnescaped() {
        return """
                He said "Hello" and she said "Goodbye".""";
    }

    /**
     * Demonstrates that three consecutive double quotes inside a text block
     * require at least one to be escaped.
     */
    public static String tripleQuotesEscaped() {
        return """
                The text block delimiter is \""" (triple quotes).""";
    }

    /**
     * Demonstrates the newline escape (\n) inside a text block.
     */
    public static String explicitNewline() {
        return """
                Line 1\nLine 2""";
    }

    /**
     * Demonstrates the tab escape (\t) inside a text block.
     */
    public static String tabCharacter() {
        return """
                Column1\tColumn2\tColumn3""";
    }

    /**
     * Demonstrates the \s escape sequence (space) introduced in Java 15.
     * \s translates to a single space and prevents trailing whitespace stripping.
     */
    public static String spaceEscapeSequence() {
        return """
                trailing space\s
                also trailing\s""";
    }

    /**
     * Demonstrates the line continuation escape (\ at end of line) introduced in Java 15.
     * This suppresses the newline character at the end of the line.
     */
    public static String lineContinuation() {
        return """
                This is a single \
                long line that spans \
                multiple source lines.""";
    }

    /**
     * Demonstrates backslash escaping in text blocks.
     */
    public static String backslashEscaping() {
        return """
                Windows path: C:\\Users\\admin\\docs
                Unix path: /home/user/docs""";
    }

    /**
     * Demonstrates single quotes (do not need escaping in text blocks or regular strings).
     */
    public static String singleQuotes() {
        return """
                It's a beautiful day.
                She said, 'Hello!'""";
    }

    /**
     * Demonstrates unicode escapes in text blocks.
     */
    public static String unicodeEscapes() {
        return """
                Smiley: \u263A
                Heart: \u2665
                Star: \u2605""";
    }

    /**
     * Demonstrates combining line continuation with formatted() for long lines.
     */
    public static String lineContinuationWithFormatted(String name) {
        return """
                Hello %s, welcome to \
                the text blocks tutorial.\
                """.formatted(name);
    }

    /**
     * Demonstrates octal escape sequences in text blocks.
     */
    public static String octalEscape() {
        // \101 is octal for 'A' (65 decimal)
        return """
                Octal A: \101""";
    }

    /**
     * Demonstrates the carriage return escape in text blocks.
     * Note: text blocks normalize line endings to \n (LF).
     */
    public static String carriageReturnHandling() {
        return """
                Line 1\r
                Line 2""";
    }

    /**
     * Demonstrates null character in text blocks.
     */
    public static String nullCharacter() {
        return """
                Before\0After""";
    }

    /**
     * Demonstrates using text blocks for strings that contain both
     * single and double quotes.
     */
    public static String mixedQuotes() {
        return """
                She said, "It's wonderful!"
                He replied, "That's great!\"""";
    }

    /**
     * Demonstrates that the \s escape is not the same as a regular space
     * when it comes to trailing whitespace stripping.
     */
    public static String spaceEscapeVsRegularSpace() {
        // Without \s, trailing spaces would be stripped
        // With \s, the trailing space is preserved
        return """
                With escape:\s
                Without:""";
    }
}
