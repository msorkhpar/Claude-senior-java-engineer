package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class DedupeTest {

    /** A fresh String object, never the interned literal. */
    private static String word(String text) {
        return new String(text.toCharArray());
    }

    @Test
    void dropsRepeats() {
        String c = word("c");
        List<String> words = new ArrayList<>(List.of(word("a"), word("b"), c, c));
        List<String> before = new ArrayList<>(words);
        List<String> answer = Dedupe.distinct(words);
        assertThat(answer).containsExactly("a", "b", "c");
        assertThat(answer).isNotSameAs(words);
        assertThat(words).as("the input is not changed").isEqualTo(before);
    }

    @Test
    void keepsInsertionOrder() {
        // single-letter strings hash to their char code, so a HashSet iterates them a, b, c, d
        List<String> words = List.of(word("d"), word("c"), word("b"), word("a"));
        assertThat(Dedupe.distinct(words)).containsExactly("d", "c", "b", "a");
    }

    @Test
    void firstOccurrenceWins() {
        String b = word("b");
        List<String> words = List.of(b, word("a"), b);
        assertThat(Dedupe.distinct(words)).containsExactly("b", "a");
    }

    @Test
    void equalWordsAreOneWord() {
        String first = word("a");
        String second = word("a");
        assertThat(first).isNotSameAs(second);
        List<String> answer = Dedupe.distinct(List.of(first, word("b"), second));
        assertThat(answer).containsExactly("a", "b");
    }

    @Test
    void wordsAreComparedExactly() {
        assertThat(Dedupe.distinct(List.of(new String("Aa"), new String("BB")))).as("equal hash codes are not equal words").containsExactly("Aa", "BB");
        assertThat(Dedupe.distinct(List.of(new String("Hello"), new String("hello")))).as("case counts").containsExactly("Hello", "hello");
    }

    @Test
    void eachCallReturnsItsOwnNewList() {
        List<String> input = new ArrayList<>(List.of(new String("x"), new String("y")));
        List<String> first = Dedupe.distinct(input);
        assertThat(first).isNotSameAs(input);
        List<String> second = Dedupe.distinct(List.of(new String("p"), new String("p")));
        assertThat(first).as("an earlier answer is not changed by a later call").containsExactly("x", "y");
        assertThat(second).containsExactly("p");
        assertThat(input).containsExactly("x", "y");
    }
}
