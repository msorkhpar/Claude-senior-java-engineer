package practice;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;

import static org.assertj.core.api.Assertions.assertThat;

class MatcherTest {

    private static final List<String> WORDS = List.of("hi", "hello", "java", "javafx");

    @Test
    void keepsWordsThatPassTheRule() {
        BiPredicate<String, String> rule = Matcher.startsWithAndLongerThan(3);
        assertThat(Matcher.matching(WORDS, "ja", rule)).containsExactly("java", "javafx");
        assertThat(Matcher.matching(WORDS, "x", rule)).isEmpty();
        assertThat(Matcher.matching(WORDS, "ja", rule.negate())).containsExactly("hi", "hello");
        assertThat(Matcher.matching(WORDS, "java", String::equals)).containsExactly("java");
    }

    @Test
    void theLengthLimitIsStrict() {
        List<String> words = List.of("hi", "hello", "hey", "java", "javafx");
        assertThat(Matcher.matching(words, "he", Matcher.startsWithAndLongerThan(3))).containsExactly("hello");
        assertThat(Matcher.matching(words, "ja", Matcher.startsWithAndLongerThan(4))).containsExactly("javafx");
    }

    @Test
    void nullWordsNeverMatch() {
        List<String> words = Arrays.asList("hello", null, "hi");
        BiPredicate<String, String> rule = Matcher.startsWithAndLongerThan(3);

        assertThat(Matcher.matching(words, "he", rule)).containsExactly("hello");
        assertThat(Matcher.matching(words, "he", rule.negate())).containsExactly("hi");
    }

    @Test
    void aRuleIsNeverHandedNull() {
        List<String> seen = new java.util.ArrayList<>();
        BiPredicate<String, String> recording = (word, prefix) -> {
            seen.add(String.valueOf(word));
            return word == null;
        };

        assertThat(Matcher.matching(Arrays.asList("a", null, "b"), "x", recording)).isEmpty();
        assertThat(seen).containsExactly("a", "b");
    }

    @Test
    void repeatedWordsAreAllKept() {
        assertThat(Matcher.matching(List.of("java", "hi", "java"), "ja", Matcher.startsWithAndLongerThan(3)))
                .containsExactly("java", "java");
    }

    @Test
    void thePrefixIsCaseSensitive() {
        assertThat(Matcher.matching(List.of("Java", "javafx"), "ja", Matcher.startsWithAndLongerThan(3)))
                .containsExactly("javafx");
    }
}
