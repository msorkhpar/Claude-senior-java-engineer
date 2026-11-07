package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Each expected value below is what javac 21 made of the same source lines written as a real text
 * block. A tab is written as \t; the closing delimiter as \"\"\".
 */
class TextBlockPreviewTest {

    private static String value(String... source) {
        return TextBlockPreview.value(List.of(source));
    }

    @Test
    void keepsTheNestingOfAnAlignedBlock() {
        String value = value(
                "            <order>",
                "              <id>7</id>",
                "              <total>12.50</total>",
                "            </order>",
                "            \"\"\"");

        assertThat(value).isEqualTo("<order>\n  <id>7</id>\n  <total>12.50</total>\n</order>\n");
    }

    @Test
    void handlesAClosingDelimiterAfterTheLastLine() {
        String value = value(
                "          SELECT name",
                "            FROM pets",
                "          ORDER BY name\"\"\"");

        assertThat(value).isEqualTo("SELECT name\n  FROM pets\nORDER BY name");
    }

    @Test
    void blankLinesNeitherMoveTheMarginNorKeepSpaces() {
        String value = value(
                "            <order>",
                "",
                "              <id>7</id>",
                "   ",
                "                      ",
                "            </order>",
                "            \"\"\"");

        assertThat(value).isEqualTo("<order>\n\n  <id>7</id>\n\n\n</order>\n");
    }

    @Test
    void aClosingDelimiterLeftOfTheContentIndentsEveryLine() {
        String value = value(
                "        GET /health",
                "        Host: example.com",
                "    \"\"\"");

        assertThat(value).isEqualTo("    GET /health\n    Host: example.com\n");
    }

    @Test
    void aClosingDelimiterRightOfTheContentChangesNothing() {
        String value = value(
                "      a: 1",
                "      b:",
                "        c: 2",
                "              \"\"\"");

        assertThat(value).isEqualTo("a: 1\nb:\n  c: 2\n");
    }

    @Test
    void spacesAtTheEndOfALineAreNotInTheValue() {
        String value = value(
                "        id   name  ",
                "        7    Ada   ",
                "        42   Grace",
                "        \"\"\"");

        assertThat(value).isEqualTo("id   name\n7    Ada\n42   Grace\n");
    }

    @Test
    void aTabCountsAsOneCharacterOfIndentation() {
        String value = value(
                "        x = 1",
                "\t\ty = 2",
                "        \"\"\"");

        assertThat(value).isEqualTo("      x = 1\ny = 2\n");
    }
}
