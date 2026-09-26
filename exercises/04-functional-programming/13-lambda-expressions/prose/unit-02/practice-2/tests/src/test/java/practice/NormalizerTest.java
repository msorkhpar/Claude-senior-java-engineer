package practice;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NormalizerTest {

    /** Text built at run time, a distinct object from any literal with the same content. */
    private static String text(String s) {
        return new String(s.toCharArray());
    }

    @Test
    void trimsAndUpperCases() {
        assertThat(Normalizer.normalizer().apply(text(" hello "))).isEqualTo("HELLO");
        assertThat(Normalizer.normalizeAll(List.of(text("World"), text(" java 21 ")))).containsExactly("WORLD", "JAVA 21");
    }

    @Test
    void nullBecomesNullMarker() {
        assertThat(Normalizer.normalizer().apply(null)).isEqualTo("NULL");
        assertThat(Normalizer.normalizeAll(Arrays.asList(text("a"), null, text("b")))).containsExactly("A", "NULL", "B");
    }

    @Test
    void blankBecomesEmptyMarker() {
        assertThat(Normalizer.normalizer().apply(text("   "))).isEqualTo("EMPTY");
        assertThat(Normalizer.normalizer().apply(text(" \t\n "))).isEqualTo("EMPTY");
        assertThat(Normalizer.normalizeAll(List.of(text("x"), text(" \t ")))).containsExactly("X", "EMPTY");
    }

    @Test
    void emptyIsFoundByContent() {
        assertThat(Normalizer.normalizer().apply(new String())).isEqualTo("EMPTY");
        assertThat(Normalizer.normalizeAll(List.of(new String(new char[0]), text("ok")))).containsExactly("EMPTY", "OK");
    }
}
