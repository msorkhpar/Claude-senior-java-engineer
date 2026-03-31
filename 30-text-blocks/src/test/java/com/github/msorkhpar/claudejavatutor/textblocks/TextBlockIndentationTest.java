package com.github.msorkhpar.claudejavatutor.textblocks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Text Block Indentation Tests")
class TextBlockIndentationTest {

    @Nested
    @DisplayName("Incidental Whitespace Removal")
    class IncidentalWhitespaceTest {

        @Test
        @DisplayName("Should remove common leading whitespace")
        void testIncidentalWhitespaceRemoval() {
            String result = TextBlockIndentation.incidentalWhitespaceRemoval();

            assertThat(result).isEqualTo("Line A\nLine B\nLine C");
        }

        @Test
        @DisplayName("Lines should not start with spaces after incidental removal")
        void testNoLeadingSpacesAfterRemoval() {
            String result = TextBlockIndentation.incidentalWhitespaceRemoval();

            result.lines().forEach(line ->
                    assertThat(line).doesNotStartWith(" ")
            );
        }
    }

    @Nested
    @DisplayName("Closing Delimiter Position Effect")
    class ClosingDelimiterEffectTest {

        @Test
        @DisplayName("Content should be indented when closing delimiter is left of content")
        void testClosingDelimiterLeftOfContent() {
            String result = TextBlockIndentation.closingDelimiterLeftOfContent();

            assertThat(result.lines().toList().get(0)).startsWith("    ");
        }

        @Test
        @DisplayName("Should end with trailing newline when delimiter is on its own line")
        void testTrailingNewline() {
            String result = TextBlockIndentation.closingDelimiterLeftOfContent();

            assertThat(result).endsWith("\n");
        }
    }

    @Nested
    @DisplayName("Essential Whitespace")
    class EssentialWhitespaceTest {

        @Test
        @DisplayName("Should preserve relative indentation")
        void testEssentialWhitespace() {
            String result = TextBlockIndentation.essentialWhitespace();
            String[] lines = result.split("\n");

            assertThat(lines[0]).isEqualTo("No indent");
            assertThat(lines[1]).isEqualTo("    Four spaces indent");
            assertThat(lines[2]).isEqualTo("        Eight spaces indent");
        }

        @Test
        @DisplayName("Should have three lines")
        void testEssentialWhitespaceLineCount() {
            String result = TextBlockIndentation.essentialWhitespace();

            assertThat(result.lines().count()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Mixed Indentation")
    class MixedIndentationTest {

        @Test
        @DisplayName("Should preserve hierarchical indentation")
        void testMixedIndentation() {
            String result = TextBlockIndentation.mixedIndentation();
            String[] lines = result.split("\n");

            assertThat(lines[0]).isEqualTo("root");
            assertThat(lines[1]).startsWith("    ");
            assertThat(lines[2]).startsWith("        ");
            assertThat(lines[3]).startsWith("    ");
        }

        @Test
        @DisplayName("Should have four lines")
        void testMixedIndentationLineCount() {
            String result = TextBlockIndentation.mixedIndentation();

            assertThat(result.lines().count()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("Trailing Whitespace")
    class TrailingWhitespaceTest {

        @Test
        @DisplayName("\\s escape should preserve trailing space")
        void testPreserveTrailingWithEscapeS() {
            String result = TextBlockIndentation.preserveTrailingWithEscapeS();
            String[] lines = result.split("\n");

            assertThat(lines[0]).endsWith(" ");
            assertThat(lines[1]).endsWith(" ");
        }
    }

    @Nested
    @DisplayName("Programmatic Indentation")
    class ProgrammaticIndentTest {

        @Test
        @DisplayName("Should add positive indentation")
        void testPositiveIndent() {
            String result = TextBlockIndentation.programmaticIndent(4);

            result.lines().forEach(line -> {
                if (!line.isBlank()) {
                    assertThat(line).startsWith("    ");
                }
            });
        }

        @Test
        @DisplayName("Should remove indentation with negative value")
        void testNegativeIndent() {
            // indent() with 0 should not add whitespace
            String result = TextBlockIndentation.programmaticIndent(0);

            assertThat(result).contains("Line 1");
        }

        @Test
        @DisplayName("indent() should always end with newline")
        void testIndentAlwaysEndsWithNewline() {
            String result = TextBlockIndentation.programmaticIndent(2);

            assertThat(result).endsWith("\n");
        }
    }

    @Nested
    @DisplayName("Manual Strip Indent")
    class ManualStripIndentTest {

        @Test
        @DisplayName("Should strip common leading whitespace from regular string")
        void testManualStripIndent() {
            String result = TextBlockIndentation.manualStripIndent();

            assertThat(result).isEqualTo("Line 1\nLine 2\nLine 3");
        }
    }

    @Nested
    @DisplayName("Blank Lines in Whitespace Calculation")
    class BlankLinesTest {

        @Test
        @DisplayName("Blank lines should not affect indentation removal")
        void testBlankLinesIgnoredForWhitespace() {
            String result = TextBlockIndentation.blankLinesIgnoredForWhitespace();
            String[] lines = result.split("\n", -1);

            assertThat(lines[0]).isEqualTo("Line 1");
            assertThat(lines[1]).isEmpty();
            assertThat(lines[2]).isEqualTo("Line 3");
        }

        @Test
        @DisplayName("Should have three lines including blank")
        void testBlankLineCount() {
            String result = TextBlockIndentation.blankLinesIgnoredForWhitespace();

            assertThat(result.lines().count()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Code Generation")
    class CodeGenerationTest {

        @Test
        @DisplayName("Should generate valid class structure")
        void testCodeGenerationExample() {
            String result = TextBlockIndentation.codeGenerationExample("MyService", "execute");

            assertThat(result)
                    .contains("public class MyService")
                    .contains("public void execute()")
                    .contains("System.out.println(\"Generated\")");
        }

        @Test
        @DisplayName("Should use correct class and method names")
        void testCodeGenerationWithDifferentNames() {
            String result = TextBlockIndentation.codeGenerationExample("UserDao", "findAll");

            assertThat(result)
                    .contains("public class UserDao")
                    .contains("public void findAll()");
        }
    }

    @Nested
    @DisplayName("Translate Escapes")
    class TranslateEscapesTest {

        @Test
        @DisplayName("Should translate escape sequences in regular string")
        void testTranslateEscapes() {
            String result = TextBlockIndentation.translateEscapesDemo();

            assertThat(result).isEqualTo("Hello\nWorld\tTab");
        }

        @Test
        @DisplayName("Translated string should contain actual newline")
        void testTranslateEscapesContainsNewline() {
            String result = TextBlockIndentation.translateEscapesDemo();

            assertThat(result).contains("\n");
            assertThat(result).contains("\t");
        }
    }
}
