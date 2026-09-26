package practice;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SortedNamesTest {

    @Test
    void sortsUpperCasedNames() {
        assertThat(SortedNames.sortedUpper(List.of("banana", "cherry", "apple")))
                .containsExactly("APPLE", "BANANA", "CHERRY");
        assertThat(SortedNames.sortedUpper(List.of("kiwi"))).containsExactly("KIWI");
    }

    @Test
    void nullsAreSkipped() {
        assertThat(SortedNames.sortedUpper(Arrays.asList("banana", null, "apple", null)))
                .containsExactly("APPLE", "BANANA");
        assertThat(SortedNames.sortedUpper(Arrays.asList(null, null))).isEmpty();
    }

    @Test
    void ordersByTheUpperCasedText() {
        assertThat(SortedNames.sortedUpper(List.of("apple", "Banana", "cherry")))
                .containsExactly("APPLE", "BANANA", "CHERRY");
    }
}
