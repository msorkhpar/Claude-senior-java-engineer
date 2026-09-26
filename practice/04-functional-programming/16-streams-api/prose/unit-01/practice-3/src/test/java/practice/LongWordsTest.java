package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class LongWordsTest {

    @Test
    void reportsCountAndWords() {
        assertThat(LongWords.report(List.of("hi", "coffee", "juice", "milk"), 3))
                .isEqualTo(new LongWords.Report(3, List.of("coffee", "juice", "milk")));
        assertThat(LongWords.report(List.of("hi", "ok"), 3))
                .isEqualTo(new LongWords.Report(0, List.of()));
    }

    @Test
    void eachGetGivesAFreshStream() {
        Supplier<Stream<String>> longWords = LongWords.longerThan(List.of("hi", "coffee", "juice", "milk"), 3);

        assertThat(longWords.get().toList()).containsExactly("coffee", "juice", "milk");
        assertThat(longWords.get().count()).isEqualTo(3);
        assertThat(longWords.get().findFirst()).contains("coffee");
    }

    @Test
    void aWordOfExactlyMinIsNotLonger() {
        assertThat(LongWords.report(List.of("tea", "coffee", "gin"), 3))
                .isEqualTo(new LongWords.Report(1, List.of("coffee")));
    }
}
