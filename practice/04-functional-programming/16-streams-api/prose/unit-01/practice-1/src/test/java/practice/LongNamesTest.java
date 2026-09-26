package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class LongNamesTest {

    @Test
    void keepsLongNamesUpperCased() {
        assertThat(LongNames.upperLongNames(new ArrayList<>(List.of("Hi", "Alice", "Jo", "Charlie", "Ed", "Alice", " Jo "))))
                .containsExactly("ALICE", "CHARLIE", "ALICE", " JO ");
        assertThat(LongNames.upperLongNames(new ArrayList<>(List.of("Hi", "Jo")))).isEmpty();
        assertThat(LongNames.upperLongNames(new ArrayList<>())).isEmpty();
    }

    @Test
    void threeLettersIsNotLonger() {
        assertThat(LongNames.upperLongNames(new ArrayList<>(List.of("Bob", "Anna", "Eve", "Dave"))))
                .containsExactly("ANNA", "DAVE");
    }

    @Test
    void theSourceIsLeftUnchanged() {
        List<String> source = new ArrayList<>(List.of("hi", "hello", "hey", "goodbye"));
        List<String> before = new ArrayList<>(source);

        List<String> result = LongNames.upperLongNames(source);

        assertThat(result).containsExactly("HELLO", "GOODBYE");
        assertThat(source).isEqualTo(before);
    }

    @Test
    void theResultIsNotTheSource() {
        List<String> source = new ArrayList<>(List.of("ALICE", "CHARLIE"));

        List<String> result = LongNames.upperLongNames(source);
        source.add("DAVID");

        assertThat(result).containsExactly("ALICE", "CHARLIE");
    }

    @Test
    void upperCasesTheSameInEveryLocale() {
        Locale saved = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("tr"));
        try {
            assertThat(LongNames.upperLongNames(new ArrayList<>(List.of("Linda", "timid"))))
                    .containsExactly("LINDA", "TIMID");
        } finally {
            Locale.setDefault(saved);
        }
    }
}
