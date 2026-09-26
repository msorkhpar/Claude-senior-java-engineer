package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextBlockValueTest {

    private static final String CLOSE = "\"\"\"";

    /** A body joined at run time from its source lines. */
    private static String body(String... lines) {
        return String.join("\n", lines);
    }

    @Test
    void closingOnTheLastLineStripsTheCommonIndent() {
        assertThat(TextBlockValue.evaluate(body("        Hello", "        World" + CLOSE)))
                .isEqualTo(String.join("\n", "Hello", "World"));
        assertThat(TextBlockValue.evaluate(body("        root", "            child", "                grandchild" + CLOSE)))
                .isEqualTo(String.join("\n", "root", "    child", "        grandchild"));
        assertThat(TextBlockValue.evaluate(body("            child", "        root" + CLOSE)))
                .isEqualTo(String.join("\n", "    child", "root"));
        assertThat(TextBlockValue.evaluate(body("Line 1", "Line 2" + CLOSE)))
                .isEqualTo(String.join("\n", "Line 1", "Line 2"));
    }

    @Test
    void closingOnItsOwnLineAddsANewline() {
        assertThat(TextBlockValue.evaluate(body("        Hello", "        " + CLOSE)))
                .isEqualTo(new StringBuilder("Hello").append('\n').toString());
        assertThat(TextBlockValue.evaluate(body("        Hello", "        World", "        " + CLOSE)))
                .isEqualTo(String.join("\n", "Hello", "World", ""));
    }

    @Test
    void closingLeftOfTheContentKeepsIndentation() {
        assertThat(TextBlockValue.evaluate(body("            Hello", "            World", "        " + CLOSE)))
                .isEqualTo(String.join("\n", "    Hello", "    World", ""));
        assertThat(TextBlockValue.evaluate(body("            Hello", CLOSE)))
                .isEqualTo(String.join("\n", "            Hello", ""));
    }

    @Test
    void closingRightOfTheContentChangesNothing() {
        assertThat(TextBlockValue.evaluate(body("    Hello", "        " + CLOSE)))
                .isEqualTo(String.join("\n", "Hello", ""));
        assertThat(TextBlockValue.evaluate(body("        a", "          b", "            " + CLOSE)))
                .isEqualTo(String.join("\n", "a", "  b", ""));
    }

    @Test
    void anEmptyBlockIsEmpty() {
        assertThat(TextBlockValue.evaluate(body("        " + CLOSE))).isEmpty();
        assertThat(TextBlockValue.evaluate(body(CLOSE))).isEmpty();
    }

    @Test
    void windowsLineEndingsGiveTheSameValue() {
        assertThat(TextBlockValue.evaluate(String.join("\r\n", "        Hello", "        World" + CLOSE)))
                .isEqualTo(String.join("\n", "Hello", "World"));
        assertThat(TextBlockValue.evaluate(String.join("\r\n", "        Hello", "        " + CLOSE)))
                .isEqualTo(String.join("\n", "Hello", ""));
        assertThat(TextBlockValue.evaluate(String.join("\r", "        a", "          b" + CLOSE)))
                .isEqualTo(String.join("\n", "a", "  b"));
    }
}
