package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UniqueWordsTest {

    @Test
    void sortsTheWords() {
        assertThat(UniqueWords.of("cherry apple banana")).containsExactly("apple", "banana", "cherry");
        assertThat(UniqueWords.of("hello")).containsExactly("hello");
    }

    @Test
    void eachWordAppearsOnce() {
        assertThat(UniqueWords.of("banana apple banana apple")).containsExactly("apple", "banana");
    }

    @Test
    void anyRunOfWhitespaceSeparatesWords() {
        assertThat(UniqueWords.of("apple   banana\tcherry")).containsExactly("apple", "banana", "cherry");
        assertThat(UniqueWords.of("pear\nfig\r\nplum")).containsExactly("fig", "pear", "plum");
    }

    @Test
    void surroundingWhitespaceIsIgnored() {
        assertThat(UniqueWords.of("  banana apple  ")).containsExactly("apple", "banana");
    }

    @Test
    void wordsKeepTheirCaseAndPunctuation() {
        assertThat(UniqueWords.of("Banana apple banana don't")).containsExactly("Banana", "apple", "banana", "don't");
        assertThat(UniqueWords.of("end. end")).containsExactly("end", "end.");
    }

    @Test
    void nullOrBlankGivesAnEmptyList() {
        assertThat(UniqueWords.of(null)).isNotNull().isEmpty();
        assertThat(UniqueWords.of("   ")).isNotNull().isEmpty();
    }
}
