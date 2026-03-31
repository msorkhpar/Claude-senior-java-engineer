package com.github.msorkhpar.claudejavatutor.textblocks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Traditional String Limitations Tests")
class TraditionalStringLimitationsTest {

    @Nested
    @DisplayName("JSON Building")
    class JsonBuildingTest {

        @Test
        @DisplayName("Should build JSON with traditional concatenation")
        void testBuildJsonTraditional() {
            String result = TraditionalStringLimitations.buildJsonTraditional("Alice", 30);

            assertThat(result)
                    .contains("\"name\": \"Alice\"")
                    .contains("\"age\": 30")
                    .contains("{")
                    .contains("}");
        }

        @Test
        @DisplayName("Should build JSON with text block")
        void testBuildJsonTextBlock() {
            String result = TraditionalStringLimitations.buildJsonTextBlock("Alice", 30);

            assertThat(result)
                    .contains("\"name\": \"Alice\"")
                    .contains("\"age\": 30");
        }

        @Test
        @DisplayName("Traditional and text block JSON should have equivalent content")
        void testJsonEquivalence() {
            String traditional = TraditionalStringLimitations.buildJsonTraditional("Bob", 25);
            String textBlock = TraditionalStringLimitations.buildJsonTextBlock("Bob", 25);

            assertThat(traditional).isEqualTo(textBlock);
        }

        @Test
        @DisplayName("Should handle special characters in JSON name")
        void testJsonWithSpecialCharacters() {
            String result = TraditionalStringLimitations.buildJsonTraditional("O'Brien", 40);

            assertThat(result).contains("O'Brien");
        }

        @Test
        @DisplayName("Should handle empty name in JSON")
        void testJsonWithEmptyName() {
            String result = TraditionalStringLimitations.buildJsonTraditional("", 0);

            assertThat(result)
                    .contains("\"name\": \"\"")
                    .contains("\"age\": 0");
        }
    }

    @Nested
    @DisplayName("HTML Building")
    class HtmlBuildingTest {

        @Test
        @DisplayName("Should build HTML with traditional concatenation")
        void testBuildHtmlTraditional() {
            String result = TraditionalStringLimitations.buildHtmlTraditional("My Page", "Hello!");

            assertThat(result)
                    .startsWith("<html>")
                    .contains("<title>My Page</title>")
                    .contains("<p>Hello!</p>")
                    .endsWith("</html>");
        }

        @Test
        @DisplayName("Should build HTML with text block")
        void testBuildHtmlTextBlock() {
            String result = TraditionalStringLimitations.buildHtmlTextBlock("My Page", "Hello!");

            assertThat(result)
                    .contains("<title>My Page</title>")
                    .contains("<p>Hello!</p>");
        }

        @Test
        @DisplayName("Traditional and text block HTML should have equivalent content")
        void testHtmlEquivalence() {
            String traditional = TraditionalStringLimitations.buildHtmlTraditional("Title", "Body");
            String textBlock = TraditionalStringLimitations.buildHtmlTextBlock("Title", "Body");

            assertThat(traditional).isEqualTo(textBlock);
        }
    }

    @Nested
    @DisplayName("SQL Building")
    class SqlBuildingTest {

        @Test
        @DisplayName("Should build SQL with traditional concatenation")
        void testBuildSqlTraditional() {
            String result = TraditionalStringLimitations.buildSqlTraditional("users", "name", "Alice");

            assertThat(result)
                    .contains("SELECT *")
                    .contains("FROM users")
                    .contains("WHERE name = 'Alice'")
                    .contains("ORDER BY id;");
        }

        @Test
        @DisplayName("Traditional and text block SQL should have equivalent content")
        void testSqlEquivalence() {
            String traditional = TraditionalStringLimitations.buildSqlTraditional("orders", "status", "active");
            String textBlock = TraditionalStringLimitations.buildSqlTextBlock("orders", "status", "active");

            assertThat(traditional).isEqualTo(textBlock);
        }
    }

    @Nested
    @DisplayName("Escape-heavy Strings")
    class EscapeHeavyTest {

        @Test
        @DisplayName("Should correctly build string with many escapes")
        void testEscapeHeavyTraditional() {
            String result = TraditionalStringLimitations.buildEscapeHeavyTraditional();

            assertThat(result)
                    .contains("He said, \"Hello, World!\"")
                    .contains("System.out.println(\"test\")")
                    .contains("C:\\Users\\admin\\docs");
        }

        @Test
        @DisplayName("Should contain tab characters")
        void testEscapeHeavyContainsTabs() {
            String result = TraditionalStringLimitations.buildEscapeHeavyTraditional();

            assertThat(result).contains("\t");
        }

        @Test
        @DisplayName("Should contain newline characters")
        void testEscapeHeavyContainsNewlines() {
            String result = TraditionalStringLimitations.buildEscapeHeavyTraditional();

            assertThat(result.split("\n")).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Regex Patterns")
    class RegexPatternTest {

        @Test
        @DisplayName("Traditional and text block regex should be identical")
        void testRegexEquivalence() {
            String traditional = TraditionalStringLimitations.getEmailRegexTraditional();
            String textBlock = TraditionalStringLimitations.getEmailRegexTextBlock();

            assertThat(traditional).isEqualTo(textBlock);
        }

        @Test
        @DisplayName("Regex pattern should match valid email")
        void testRegexMatchesValidEmail() {
            String regex = TraditionalStringLimitations.getEmailRegexTraditional();

            assertThat("user@example.com").matches(regex);
            assertThat("admin@sub.domain.org").matches(regex);
        }

        @Test
        @DisplayName("Regex pattern should not match invalid email")
        void testRegexRejectsInvalidEmail() {
            String regex = TraditionalStringLimitations.getEmailRegexTraditional();

            assertThat("not-an-email").doesNotMatch(regex);
            assertThat("@missing.com").doesNotMatch(regex);
        }
    }

    @Nested
    @DisplayName("Multiline with Tabs")
    class MultilineWithTabsTest {

        @Test
        @DisplayName("Should build multiline string with tabs")
        void testMultilineWithTabs() {
            String result = TraditionalStringLimitations.buildMultilineWithTabsTraditional();

            assertThat(result)
                    .contains("Line 1")
                    .contains("\tIndented Line 2")
                    .contains("\t\tDouble Indented Line 3");
        }

        @Test
        @DisplayName("Should have three lines")
        void testMultilineLineCount() {
            String result = TraditionalStringLimitations.buildMultilineWithTabsTraditional();

            assertThat(result.split("\n")).hasSize(3);
        }
    }
}
