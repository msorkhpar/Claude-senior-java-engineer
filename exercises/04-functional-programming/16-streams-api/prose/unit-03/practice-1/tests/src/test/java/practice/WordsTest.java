package practice;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class WordsTest {

    @Test
    void flattensWordsAndValues() {
        assertThat(Words.words(List.of("Hello World", "Java Streams", "FlatMap Example")))
                .containsExactly("Hello", "World", "Java", "Streams", "FlatMap", "Example");
        assertThat(Words.words(List.of())).isEmpty();
        assertThat(Words.present(List.of(Optional.of("a"), Optional.of("b")))).containsExactly("a", "b");
    }

    @Test
    void extraSpacesMakeNoEmptyWords() {
        assertThat(Words.words(List.of("  Hello   World  "))).containsExactly("Hello", "World");
        assertThat(Words.words(List.of("one", "   ", new String(""), "two  three"))).containsExactly("one", "two", "three");
    }

    @Test
    void aNullSentenceAddsNoWords() {
        assertThat(Words.words(Arrays.asList("Hello World", null, "Bye"))).containsExactly("Hello", "World", "Bye");
    }

    @Test
    void emptyOptionalsAddNothing() {
        assertThat(Words.present(List.of(Optional.of("a"), Optional.empty(), Optional.of("c")))).containsExactly("a", "c");
        assertThat(Words.present(List.of(Optional.empty(), Optional.empty()))).isEmpty();
    }
}
