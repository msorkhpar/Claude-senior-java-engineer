package practice;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class SortedNamesTest {

    @Test
    void sortsUpperCasedNames() {
        assertThat(SortedNames.sortedUpper(List.of("banana", "cherry", "apple")))
                .containsExactly("APPLE", "BANANA", "CHERRY");
        assertThat(SortedNames.sortedUpper(List.of("kiwi"))).containsExactly("KIWI");
        assertThat(SortedNames.sortedUpper(List.of("fig", "apple", "fig"))).containsExactly("APPLE", "FIG", "FIG");
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
        assertThat(SortedNames.sortedUpper(List.of("a_", "ab"))).containsExactly("AB", "A_");
    }

    @Test
    void upperCasesTheSameInEveryLocale() {
        Locale saved = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("tr"));
        try {
            assertThat(SortedNames.sortedUpper(List.of("kiwi", "fig"))).containsExactly("FIG", "KIWI");
        } finally {
            Locale.setDefault(saved);
        }
    }
}
