package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShiftTest {

    /** Lines joined at run time, each ending with \n. */
    private static String lines(String... lines) {
        return String.join("\n", lines) + "\n";
    }

    @Test
    void removesUpToNLeadingSpaces() {
        assertThat(Shift.left(lines("    Line 1", "    Line 2"), 2)).isEqualTo(lines("  Line 1", "  Line 2"));
        assertThat(Shift.left(lines("        root", "            child"), 8)).isEqualTo(lines("root", "    child"));
        assertThat(Shift.left(lines("  x"), 0)).isEqualTo(lines("  x"));
    }

    @Test
    void aShallowLineLosesOnlyWhatItHas() {
        assertThat(Shift.left(String.join("\n", "  a", "      b"), 4)).isEqualTo(lines("a", "  b"));
        assertThat(Shift.left(lines("ab", "    cd"), 2)).isEqualTo(lines("ab", "  cd"));
    }

    @Test
    void aTabIsOneWhitespaceCharacter() {
        assertThat(Shift.left(String.join("", "\t\t", "b"), 1)).isEqualTo(lines("\tb"));
        assertThat(Shift.left(lines("\t  c"), 2)).isEqualTo(lines(" c"));
    }

    @Test
    void windowsLineEndingsBecomeNewlines() {
        assertThat(Shift.left(String.join("\r\n", "  a", "  b"), 2)).isEqualTo(lines("a", "b"));
        assertThat(Shift.left(String.join("\r", "   x", " y", ""), 1)).isEqualTo(lines("  x", "y"));
    }

    @Test
    void theLastLineEndsWithANewlineToo() {
        assertThat(Shift.left(new String("hello"), 0)).isEqualTo(lines("hello"));
        assertThat(Shift.left(String.join("\n", "  a", "  b"), 2)).isEqualTo(lines("a", "b"));
    }
}
