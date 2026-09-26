package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CleanerTest {

    @Test
    void keepsOnlyEntriesWithText() {
        assertThat(Cleaner.meaningful(new ArrayList<>(List.of("hello", new String(""), "world")))).containsExactly("hello", "world");

        List<String> names = new ArrayList<>(List.of("Alice", new String(""), "Bob"));
        assertThat(Cleaner.prune(names)).isEqualTo(1);
        assertThat(names).containsExactly("Alice", "Bob");

        List<String> clean = new ArrayList<>(List.of("x"));
        assertThat(Cleaner.prune(clean)).isZero();
        assertThat(clean).containsExactly("x");
    }

    @Test
    void nullsAreDropped() {
        assertThat(Cleaner.meaningful(new ArrayList<>(Arrays.asList("hello", null, "java")))).containsExactly("hello", "java");

        List<String> names = new ArrayList<>(Arrays.asList(null, "Bob", null));
        assertThat(Cleaner.prune(names)).isEqualTo(2);
        assertThat(names).containsExactly("Bob");
    }

    @Test
    void whitespaceOnlyIsDropped() {
        assertThat(Cleaner.meaningful(new ArrayList<>(List.of("  ", "java", "\t")))).containsExactly("java");

        List<String> names = new ArrayList<>(List.of("Alice", "  ", "Charlie"));
        assertThat(Cleaner.prune(names)).isEqualTo(1);
        assertThat(names).containsExactly("Alice", "Charlie");
    }

    @Test
    void meaningfulLeavesItsInputAlone() {
        List<String> items = new ArrayList<>(List.of("a", "", "b"));

        Cleaner.meaningful(items);

        assertThat(items).containsExactly("a", "", "b");
    }
}
