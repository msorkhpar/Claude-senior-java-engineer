package practice;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class ExpandTest {

    @Test
    void emitsThroughTheDownstream() {
        assertThat(Expand.valueAndSquare(List.of(2, 3, 4))).containsExactly(2, 4, 3, 9, 4, 16);
        assertThat(Expand.valueAndSquare(List.of())).isEmpty();
        assertThat(Expand.upperStrings(List.of("hello", "world"))).containsExactly("HELLO", "WORLD");
        assertThat(Expand.upperStrings(List.of("", "x"))).containsExactly("", "X");
    }

    @Test
    void nonStringsAreSkipped() {
        assertThat(Expand.upperStrings(List.of(1, "hello", 2.0, "world", 3))).containsExactly("HELLO", "WORLD");
        assertThat(Expand.upperStrings(List.of(1, 2, 3.0, 4L))).isEmpty();
        assertThat(Expand.upperStrings(List.of(new StringBuilder("sb"), "s"))).containsExactly("S");
    }

    @Test
    void nullsAreSkipped() {
        assertThat(Expand.upperStrings(Arrays.asList("a", null, "b"))).containsExactly("A", "B");
    }

    @Test
    void upperCasesTheSameInEveryLocale() {
        Locale saved = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("tr"));
        try {
            assertThat(Expand.upperStrings(List.of("this", 1))).containsExactly("THIS");
        } finally {
            Locale.setDefault(saved);
        }
    }
}
