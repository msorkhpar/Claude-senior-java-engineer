package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WordOrderTest {

    @Test
    void sortsLowerCaseWordsAndDropsEmptyLists() {
        List<String> words = List.of("banana", "apple", "cherry");
        assertThat(WordOrder.natural(words)).containsExactly("apple", "banana", "cherry");
        assertThat(WordOrder.ignoringCase(words)).containsExactly("apple", "banana", "cherry");
        assertThat(WordOrder.shortestFirst(List.of("ccc", "a", "bb"))).containsExactly("a", "bb", "ccc");
        assertThat(WordOrder.nonEmpty(List.of(List.of("a"), List.of(), List.of("b"))))
                .containsExactly(List.of("a"), List.of("b"));
    }

    @Test
    void capitalsSortBeforeLowerCaseInNaturalOrder() {
        assertThat(WordOrder.natural(List.of("banana", "Apple", "apple", "Banana")))
                .containsExactly("Apple", "Banana", "apple", "banana");
    }

    @Test
    void ignoringCaseMixesTheCases() {
        assertThat(WordOrder.ignoringCase(List.of("Banana", "apple", "CHERRY")))
                .containsExactly("apple", "Banana", "CHERRY");
    }

    @Test
    void equalLengthsFallBackToAlphabeticalOrder() {
        assertThat(WordOrder.shortestFirst(List.of("bb", "ab", "c"))).containsExactly("c", "ab", "bb");
    }
}
