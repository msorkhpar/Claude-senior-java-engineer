package practice;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExpandTest {

    @Test
    void emitsThroughTheDownstream() {
        assertThat(Expand.valueAndSquare(List.of(2, 3, 4))).containsExactly(2, 4, 3, 9, 4, 16);
        assertThat(Expand.valueAndSquare(List.of())).isEmpty();
        assertThat(Expand.upperStrings(List.of("hello", "world"))).containsExactly("HELLO", "WORLD");
    }

    @Test
    void nonStringsAreSkipped() {
        assertThat(Expand.upperStrings(List.of(1, "hello", 2.0, "world", 3))).containsExactly("HELLO", "WORLD");
        assertThat(Expand.upperStrings(List.of(1, 2, 3.0, 4L))).isEmpty();
    }

    @Test
    void nullsAreSkipped() {
        assertThat(Expand.upperStrings(Arrays.asList("a", null, "b"))).containsExactly("A", "B");
    }
}
