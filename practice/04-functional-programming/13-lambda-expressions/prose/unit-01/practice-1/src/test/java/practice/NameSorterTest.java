package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NameSorterTest {

    @Test
    void sortsAlphabetically() {
        assertThat(NameSorter.alphabetical(Arrays.asList("Charlie", "Alice", "Bob")))
                .containsExactly("Alice", "Bob", "Charlie");
        assertThat(NameSorter.alphabetical(new ArrayList<>())).isEmpty();
        assertThat(NameSorter.alphabetical(Arrays.asList("bob", "Bob", "alice", "Bob")))
                .containsExactly("Bob", "Bob", "alice", "bob");
    }

    @Test
    void sortsByLength() {
        assertThat(NameSorter.byLength(Arrays.asList("aaa", "b", "cc"))).containsExactly("b", "cc", "aaa");
        assertThat(NameSorter.byLength(Arrays.asList("hello", "hi"))).containsExactly("hi", "hello");
        assertThat(NameSorter.byLength(Arrays.asList("ccc", "a", "ccc"))).containsExactly("a", "ccc", "ccc");
    }

    @Test
    void leavesTheInputUntouched() {
        List<String> names = new ArrayList<>(List.of("Charlie", "Alice", "Bob"));
        List<String> sorted = NameSorter.alphabetical(names);
        assertThat(names).containsExactly("Charlie", "Alice", "Bob");
        assertThat(sorted).containsExactly("Alice", "Bob", "Charlie");

        List<String> words = new ArrayList<>(List.of("aaa", "b", "cc"));
        NameSorter.byLength(words);
        assertThat(words).containsExactly("aaa", "b", "cc");
    }

    @Test
    void equalLengthsFallBackToAlphabetical() {
        assertThat(NameSorter.byLength(Arrays.asList("pear", "fig", "kiwi", "date", "ant")))
                .containsExactly("ant", "fig", "date", "kiwi", "pear");
        assertThat(NameSorter.byLength(Arrays.asList("bob", "Bob"))).containsExactly("Bob", "bob");
    }
}
