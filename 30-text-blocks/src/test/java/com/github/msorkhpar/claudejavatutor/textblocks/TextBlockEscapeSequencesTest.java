package com.github.msorkhpar.claudejavatutor.textblocks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Text Block Escape Sequences Tests")
class TextBlockEscapeSequencesTest {

    @Nested
    @DisplayName("Double Quotes")
    class DoubleQuotesTest {

        @Test
        @DisplayName("Should include unescaped double quotes")
        void testDoubleQuotesUnescaped() {
            String result = TextBlockEscapeSequences.doubleQuotesUnescaped();

            assertThat(result)
                    .contains("\"Hello\"")
                    .contains("\"Goodbye\"");
        }

        @Test
        @DisplayName("Mixed quotes should work correctly")
        void testMixedQuotes() {
            String result = TextBlockEscapeSequences.mixedQuotes();

            assertThat(result)
                    .contains("She said, \"It's wonderful!\"")
                    .contains("He replied, \"That's great!\"");
        }
    }

    @Nested
    @DisplayName("Triple Quotes")
    class TripleQuotesTest {

        @Test
        @DisplayName("Should include escaped triple quotes")
        void testTripleQuotesEscaped() {
            String result = TextBlockEscapeSequences.tripleQuotesEscaped();

            assertThat(result).contains("\"\"\"");
        }
    }

    @Nested
    @DisplayName("Newline and Tab Escapes")
    class NewlineAndTabTest {

        @Test
        @DisplayName("Explicit newline should create two lines from one source line")
        void testExplicitNewline() {
            String result = TextBlockEscapeSequences.explicitNewline();

            assertThat(result).isEqualTo("Line 1\nLine 2");
            assertThat(result.lines().count()).isEqualTo(2);
        }

        @Test
        @DisplayName("Tab character should be present")
        void testTabCharacter() {
            String result = TextBlockEscapeSequences.tabCharacter();

            assertThat(result).contains("\t");
            assertThat(result).isEqualTo("Column1\tColumn2\tColumn3");
        }
    }

    @Nested
    @DisplayName("Space Escape (\\s)")
    class SpaceEscapeTest {

        @Test
        @DisplayName("\\s should preserve trailing space")
        void testSpaceEscapeSequence() {
            String result = TextBlockEscapeSequences.spaceEscapeSequence();
            String[] lines = result.split("\n");

            assertThat(lines[0]).endsWith(" ");
            assertThat(lines[1]).endsWith(" ");
        }

        @Test
        @DisplayName("\\s vs regular space difference")
        void testSpaceEscapeVsRegularSpace() {
            String result = TextBlockEscapeSequences.spaceEscapeVsRegularSpace();
            String[] lines = result.split("\n");

            // "With escape:" has \s so trailing space preserved
            assertThat(lines[0]).endsWith(" ");
            // "Without:" has no trailing space
            assertThat(lines[1]).isEqualTo("Without:");
        }
    }

    @Nested
    @DisplayName("Line Continuation")
    class LineContinuationTest {

        @Test
        @DisplayName("Line continuation should produce single line")
        void testLineContinuation() {
            String result = TextBlockEscapeSequences.lineContinuation();

            assertThat(result).isEqualTo("This is a single long line that spans multiple source lines.");
            assertThat(result.lines().count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Line continuation with formatted should work")
        void testLineContinuationWithFormatted() {
            String result = TextBlockEscapeSequences.lineContinuationWithFormatted("Alice");

            assertThat(result).contains("Hello Alice");
            assertThat(result).contains("the text blocks tutorial.");
        }
    }

    @Nested
    @DisplayName("Backslash Escaping")
    class BackslashEscapingTest {

        @Test
        @DisplayName("Should contain Windows path with backslashes")
        void testBackslashEscaping() {
            String result = TextBlockEscapeSequences.backslashEscaping();

            assertThat(result).contains("C:\\Users\\admin\\docs");
            assertThat(result).contains("/home/user/docs");
        }

        @Test
        @DisplayName("Should have two lines")
        void testBackslashEscapingLineCount() {
            String result = TextBlockEscapeSequences.backslashEscaping();

            assertThat(result.lines().count()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Single Quotes")
    class SingleQuotesTest {

        @Test
        @DisplayName("Should include single quotes without escaping")
        void testSingleQuotes() {
            String result = TextBlockEscapeSequences.singleQuotes();

            assertThat(result)
                    .contains("It's")
                    .contains("'Hello!'");
        }
    }

    @Nested
    @DisplayName("Unicode Escapes")
    class UnicodeEscapesTest {

        @Test
        @DisplayName("Should include unicode characters")
        void testUnicodeEscapes() {
            String result = TextBlockEscapeSequences.unicodeEscapes();

            assertThat(result)
                    .contains("\u263A")  // smiley
                    .contains("\u2665")  // heart
                    .contains("\u2605"); // star
        }

        @Test
        @DisplayName("Unicode text block should have three lines")
        void testUnicodeLineCount() {
            String result = TextBlockEscapeSequences.unicodeEscapes();

            assertThat(result.lines().count()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Octal Escapes")
    class OctalEscapesTest {

        @Test
        @DisplayName("Octal escape should produce correct character")
        void testOctalEscape() {
            String result = TextBlockEscapeSequences.octalEscape();

            assertThat(result).contains("A");
            assertThat(result).isEqualTo("Octal A: A");
        }
    }

    @Nested
    @DisplayName("Carriage Return")
    class CarriageReturnTest {

        @Test
        @DisplayName("Carriage return escape should be present in output")
        void testCarriageReturnHandling() {
            String result = TextBlockEscapeSequences.carriageReturnHandling();

            assertThat(result).contains("\r");
        }
    }

    @Nested
    @DisplayName("Null Character")
    class NullCharacterTest {

        @Test
        @DisplayName("Null character should be present")
        void testNullCharacter() {
            String result = TextBlockEscapeSequences.nullCharacter();

            assertThat(result).contains("\0");
            assertThat(result).hasSize("Before\0After".length());
        }
    }
}
