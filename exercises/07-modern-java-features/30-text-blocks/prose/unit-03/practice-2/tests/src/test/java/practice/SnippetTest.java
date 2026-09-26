package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SnippetTest {

    /** Lines joined at run time, each ending with a newline. */
    private static String lines(String... lines) {
        return String.join("\n", lines) + "\n";
    }

    @Test
    void rebasesTheSnippet() {
        assertThat(Snippet.nest(String.join("\n", "  a", "    b"), 1)).isEqualTo(lines("    a", "      b"));
        assertThat(Snippet.nest(String.join("\n", "        if (x) {", "            y();", "        }"), 2))
                .isEqualTo(lines("        if (x) {", "            y();", "        }"));
        assertThat(Snippet.nest(String.join("\n", "System.out.println(\"Hello\");"), 1))
                .isEqualTo(lines("    System.out.println(\"Hello\");"));
    }

    @Test
    void blankLinesStayEmpty() {
        assertThat(Snippet.nest(String.join("\n", "  a", "", "  b"), 1)).isEqualTo(lines("    a", "", "    b"));
        assertThat(Snippet.nest(String.join("\n", "  a", "      ", "  b"), 2)).isEqualTo(lines("        a", "", "        b"));
        assertThat(Snippet.nest(String.join("\n", "    a", "    b", "  "), 1)).isEqualTo(lines("    a", "    b", ""));
    }

    @Test
    void blankLinesTakeNoPart() {
        assertThat(Snippet.nest(String.join("\n", "    a", "  ", "    b"), 0)).isEqualTo(lines("a", "", "b"));
        assertThat(Snippet.nest(String.join("\n", "      x", "   ", "      y"), 1)).isEqualTo(lines("    x", "", "    y"));
    }

    @Test
    void levelZeroStillEndsEveryLine() {
        assertThat(Snippet.nest(String.join("\n", "  a", "  b"), 0)).isEqualTo(lines("a", "b"));
        assertThat(Snippet.nest(String.join("\n", "    x", "      y"), 0)).isEqualTo(lines("x", "  y"));
    }

    @Test
    void windowsLineEndingsAreNormalized() {
        assertThat(Snippet.nest(String.join("\r\n", "  a", "  b"), 1)).isEqualTo(lines("    a", "    b"));
        assertThat(Snippet.nest(String.join("\r\n", "  a", "    b", "  c"), 1)).isEqualTo(lines("    a", "      b", "    c"));
    }

    @Test
    void trailingSpacesAreDropped() {
        assertThat(Snippet.nest(String.join("\n", "  a   ", "  b"), 1)).isEqualTo(lines("    a", "    b"));
        assertThat(Snippet.nest(String.join("\n", "  a", "  b  "), 0)).isEqualTo(lines("a", "b"));
    }
}
