package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextBlockSourceTest {

    private static final String Q = "\"\"\"";
    private static final String IN = "        ";

    /** Source lines joined at run time with \n. */
    private static String source(String... lines) {
        return String.join("\n", lines);
    }

    @Test
    void writesPlainLinesAndQuotesAsTheyAre() {
        assertThat(TextBlockSource.of(String.join("\n", "Hello", "World")))
                .isEqualTo(source(Q, IN + "Hello", IN + "World" + Q));
        assertThat(TextBlockSource.of(String.join(" ", "He", "said", "\"Hello\"", "to", "her.")))
                .isEqualTo(source(Q, IN + "He said \"Hello\" to her." + Q));
        assertThat(TextBlockSource.of(String.join("\n", "Hello", "")))
                .isEqualTo(source(Q, IN + "Hello", IN + Q));
        assertThat(TextBlockSource.of(String.join("\n", "a", "", "b")))
                .isEqualTo(source(Q, IN + "a", "", IN + "b" + Q));
        assertThat(TextBlockSource.of(new String(""))).isEqualTo(source(Q, IN + Q));
        assertThat(TextBlockSource.of(String.join("", "{", "\"key\": \"value\"", "}")))
                .isEqualTo(source(Q, IN + "{\"key\": \"value\"}" + Q));
    }

    @Test
    void backslashesAreDoubled() {
        assertThat(TextBlockSource.of(String.join("\\", "C:", "Users", "admin")))
                .isEqualTo(source(Q, IN + "C:\\\\Users\\\\admin" + Q));
        assertThat(TextBlockSource.of(String.join("", "\\", "d+")))
                .isEqualTo(source(Q, IN + "\\\\d+" + Q));
    }

    @Test
    void aRunOfThreeQuotesIsBroken() {
        assertThat(TextBlockSource.of(String.join("", "The delimiter is ", Q, " here")))
                .isEqualTo(source(Q, IN + "The delimiter is \"\"\\\" here" + Q));
        assertThat(TextBlockSource.of(String.join("", "x", Q, "\"", "\"", "y")))
                .isEqualTo(source(Q, IN + "x\"\"\\\"\"\"y" + Q));
        assertThat(TextBlockSource.of(String.join("", "x", Q, Q, "y")))
                .isEqualTo(source(Q, IN + "x\"\"\\\"\"\"\\\"y" + Q));
    }

    @Test
    void trailingSpacesAreFenced() {
        assertThat(TextBlockSource.of(String.join("\n", "Name  ", "Bob")))
                .isEqualTo(source(Q, IN + "Name \\s", IN + "Bob" + Q));
        assertThat(TextBlockSource.of(String.join("", "Hello", " ")))
                .isEqualTo(source(Q, IN + "Hello\\s" + Q));
    }

    @Test
    void aQuoteRightBeforeTheClosingDelimiterIsEscaped() {
        assertThat(TextBlockSource.of(String.join(" ", "say", "\"hi\"")))
                .isEqualTo(source(Q, IN + "say \"hi\\\"" + Q));
        assertThat(TextBlockSource.of(String.join("", "a", "\"\"")))
                .isEqualTo(source(Q, IN + "a\"\\\"" + Q));
        assertThat(TextBlockSource.of(String.join("", "a", Q)))
                .isEqualTo(source(Q, IN + "a\"\"\\\"" + Q));
        assertThat(TextBlockSource.of(String.join("\n", "say \"hi\"", "ok")))
                .isEqualTo(source(Q, IN + "say \"hi\"", IN + "ok" + Q));
    }
}
