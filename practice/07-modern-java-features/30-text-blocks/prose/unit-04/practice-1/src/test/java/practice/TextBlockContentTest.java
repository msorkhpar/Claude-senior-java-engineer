package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextBlockContentTest {

    /** Source lines joined at run time with \n. */
    private static String raw(String... lines) {
        return String.join("\n", lines);
    }

    @Test
    void stripsThenInterpretsEscapes() {
        assertThat(TextBlockContent.value(raw("        Tab:\\there", "        Quote: \\\"")))
                .isEqualTo(String.join("\n", "Tab:\there", "Quote: \""));
        assertThat(TextBlockContent.value(raw("        Hello", "        World", "        ")))
                .isEqualTo(String.join("\n", "Hello", "World", ""));
        assertThat(TextBlockContent.value(raw("            He said \"Hello\"", "        ")))
                .isEqualTo(String.join("", "    He said \"Hello\"", "\n"));
    }

    @Test
    void theSpaceEscapeKeepsTheSpacesBeforeIt() {
        assertThat(TextBlockContent.value(raw("        Text   \\s"))).isEqualTo(String.join("", "Text", "    "));
        assertThat(TextBlockContent.value(raw("        Name  \\s", "        Bob")))
                .isEqualTo(String.join("\n", "Name   ", "Bob"));
    }

    @Test
    void aLineContinuationJoinsWithoutTheIndent() {
        assertThat(TextBlockContent.value(raw("        Hello \\", "        World")))
                .isEqualTo(String.join(" ", "Hello", "World"));
        assertThat(TextBlockContent.value(raw("        https://api.example.org\\", "        /v2/users\\", "        ?active=true")))
                .isEqualTo(String.join("", "https://api.example.org", "/v2/users", "?active=true"));
    }

    @Test
    void anEscapedBackslashAtLineEndIsKept() {
        assertThat(TextBlockContent.value(raw("        C:\\\\", "        D:")))
                .isEqualTo(String.join("\n", "C:\\", "D:"));
        assertThat(TextBlockContent.value(raw("        Hello \\\\", "        World")))
                .isEqualTo(String.join("\n", "Hello \\", "World"));
    }

    @Test
    void anEscapedCarriageReturnSurvives() {
        assertThat(TextBlockContent.value(String.join("\r\n", "        Line 1\\r", "        Line 2")))
                .isEqualTo(String.join("\r\n", "Line 1", "Line 2"));
        assertThat(TextBlockContent.value(String.join("\r\n", "        a", "        b")))
                .isEqualTo(String.join("\n", "a", "b"));
    }
}
