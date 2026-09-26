package practice;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SortingTest {

    @Test
    void sortsByLengthAndNaturally() {
        assertThat(Sorting.byLengthThenAlpha(List.of("banana", "fig", "apple", "kiwi")))
                .containsExactly("fig", "kiwi", "apple", "banana");
        assertThat(Sorting.nullsFirst(List.of("banana", "apple", "cherry"))).containsExactly("apple", "banana", "cherry");
    }

    @Test
    void tiesAreBrokenAlphabetically() {
        assertThat(Sorting.byLengthThenAlpha(List.of("banana", "fig", "apple", "kiwi", "cat")))
                .containsExactly("cat", "fig", "kiwi", "apple", "banana");
        assertThat(Sorting.byLengthThenAlpha(List.of("b", "a", "A"))).containsExactly("A", "a", "b");
    }

    @Test
    void nullsComeFirst() {
        assertThat(Sorting.nullsFirst(Arrays.asList("banana", null, "apple", null, "cherry")))
                .containsExactly(null, null, "apple", "banana", "cherry");
        assertThat(Sorting.nullsFirst(Arrays.asList("a", null, "B"))).containsExactly(null, "B", "a");
    }
}
