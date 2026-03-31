package com.github.msorkhpar.claudejavatutor.textblocks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Text Block Syntax Tests")
class TextBlockSyntaxTest {

    @Nested
    @DisplayName("Basic Text Block")
    class BasicTextBlockTest {

        @Test
        @DisplayName("Should create a basic text block")
        void testBasicTextBlock() {
            String result = TextBlockSyntax.basicTextBlock();

            assertThat(result).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should create multi-line text block")
        void testMultiLineTextBlock() {
            String result = TextBlockSyntax.multiLineTextBlock();

            assertThat(result).isEqualTo("Line 1\nLine 2\nLine 3");
        }

        @Test
        @DisplayName("Multi-line text block should have three lines")
        void testMultiLineTextBlockLineCount() {
            String result = TextBlockSyntax.multiLineTextBlock();

            assertThat(result.lines().count()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Closing Delimiter Position")
    class ClosingDelimiterTest {

        @Test
        @DisplayName("Closing delimiter at start should preserve no extra indentation")
        void testClosingDelimiterAtStart() {
            String result = TextBlockSyntax.closingDelimiterAtStart();

            assertThat(result).isEqualTo("Line 1\nLine 2\nLine 3");
        }

        @Test
        @DisplayName("Should add trailing newline when closing delimiter is on its own line")
        void testTrailingNewline() {
            String result = TextBlockSyntax.textBlockWithTrailingNewline();

            assertThat(result).endsWith("\n");
            assertThat(result).isEqualTo("Hello, World!\n");
        }
    }

    @Nested
    @DisplayName("Empty and Minimal Text Blocks")
    class EmptyTextBlockTest {

        @Test
        @DisplayName("Empty text block with closing delimiter on next line should be empty string")
        void testEmptyTextBlock() {
            String result = TextBlockSyntax.emptyTextBlock();

            // When the closing delimiter is on the next line with same indentation,
            // the result is an empty string (no content lines)
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Single blank line text block should contain one newline")
        void testSingleBlankLine() {
            String result = TextBlockSyntax.singleBlankLine();

            // A blank line between opening and closing delimiters produces a single newline
            assertThat(result).isEqualTo("\n");
        }
    }

    @Nested
    @DisplayName("Text Block Is a String")
    class TextBlockIsStringTest {

        @Test
        @DisplayName("Text block should be equal to equivalent regular string")
        void testTextBlockIsString() {
            assertThat(TextBlockSyntax.textBlockIsString()).isTrue();
        }

        @Test
        @DisplayName("Text block should report correct length")
        void testTextBlockLength() {
            assertThat(TextBlockSyntax.textBlockLength()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Text Block Operations")
    class TextBlockOperationsTest {

        @Test
        @DisplayName("Should concatenate text block with regular string")
        void testConcatenateWithTextBlock() {
            String result = TextBlockSyntax.concatenateWithTextBlock("World");

            assertThat(result).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("Should apply toUpperCase to text block")
        void testTextBlockToUpperCase() {
            String result = TextBlockSyntax.textBlockToUpperCase();

            assertThat(result).isEqualTo("HELLO WORLD");
        }

        @Test
        @DisplayName("Should format text block with parameters")
        void testTextBlockFormatted() {
            String result = TextBlockSyntax.textBlockFormatted("Alice", 42);

            assertThat(result).isEqualTo("Name: Alice\nValue: 42");
        }

        @Test
        @DisplayName("Should handle empty string in formatted text block")
        void testTextBlockFormattedWithEmptyString() {
            String result = TextBlockSyntax.textBlockFormatted("", 0);

            assertThat(result).isEqualTo("Name: \nValue: 0");
        }
    }

    @Nested
    @DisplayName("Real-world Content")
    class RealWorldContentTest {

        @Test
        @DisplayName("Should create valid JSON text block")
        void testJsonTextBlock() {
            String result = TextBlockSyntax.jsonTextBlock();

            assertThat(result)
                    .contains("\"name\": \"Alice\"")
                    .contains("\"age\": 30")
                    .contains("\"active\": true")
                    .startsWith("{")
                    .endsWith("}");
        }

        @Test
        @DisplayName("Should create valid SQL text block")
        void testSqlTextBlock() {
            String result = TextBlockSyntax.sqlTextBlock();

            assertThat(result)
                    .contains("SELECT e.name, d.department_name")
                    .contains("FROM employees e")
                    .contains("JOIN departments d")
                    .contains("WHERE e.active = true")
                    .contains("ORDER BY e.name ASC");
        }

        @Test
        @DisplayName("Should create valid XML text block")
        void testXmlTextBlock() {
            String result = TextBlockSyntax.xmlTextBlock();

            assertThat(result)
                    .contains("<?xml version=\"1.0\"")
                    .contains("<project>")
                    .contains("<groupId>com.example</groupId>")
                    .contains("</project>");
        }

        @Test
        @DisplayName("SQL text block should have 5 lines")
        void testSqlTextBlockLineCount() {
            String result = TextBlockSyntax.sqlTextBlock();

            assertThat(result.lines().count()).isEqualTo(5);
        }
    }
}
