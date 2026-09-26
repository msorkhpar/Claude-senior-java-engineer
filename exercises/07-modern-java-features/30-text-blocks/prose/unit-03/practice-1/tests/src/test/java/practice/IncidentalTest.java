package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IncidentalTest {

    @Test
    void theSmallestContentIndentIsIncidental() {
        assertThat(Incidental.width(List.of("        root", "            child"), null)).isEqualTo(8);
        assertThat(Incidental.width(List.of("            child", "        root"), null)).isEqualTo(8);
        assertThat(Incidental.width(List.of("    Hello", "    World"), "    ")).isEqualTo(4);
        assertThat(Incidental.width(List.of("Line 1", "    Line 2"), null)).isEqualTo(0);
    }

    @Test
    void emptyLinesDoNotCount() {
        assertThat(Incidental.width(List.of("        a", "", "        b"), null)).isEqualTo(8);
        assertThat(Incidental.width(List.of("    Start", "", "", "    End"), "    ")).isEqualTo(4);
    }

    @Test
    void whitespaceOnlyLinesDoNotCount() {
        assertThat(Incidental.width(List.of("        a", "   ", "        b"), null)).isEqualTo(8);
        assertThat(Incidental.width(List.of("    Content", "       ", "    More"), null)).isEqualTo(4);
    }

    @Test
    void aClosingLineToTheLeftCounts() {
        assertThat(Incidental.width(List.of("            Hello"), "        ")).isEqualTo(8);
        assertThat(Incidental.width(List.of("            Hello", "            World"), "")).isEqualTo(0);
    }

    @Test
    void aClosingLineToTheRightChangesNothing() {
        assertThat(Incidental.width(List.of("    Hello"), "            ")).isEqualTo(4);
        assertThat(Incidental.width(List.of("        a", "          b"), "                ")).isEqualTo(8);
    }
}
