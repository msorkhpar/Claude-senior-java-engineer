package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreReportTest {

    @Test
    void rendersEachEntry() {
        assertThat(ScoreReport.lines(Map.of("Alice", 95))).containsExactly("Alice=95");
        assertThat(ScoreReport.lines(Map.of())).isEmpty();
    }

    @Test
    void bothRunsFirstThenSecond() {
        List<String> log = new ArrayList<>();
        BiConsumer<String, Integer> a = (k, v) -> log.add("a:" + k + "=" + v);
        BiConsumer<String, Integer> b = (k, v) -> log.add("b:" + k + "=" + v);

        ScoreReport.both(a, b).accept("Bob", 87);
        ScoreReport.both(b, a).accept("Eve", 1);

        assertThat(log).containsExactly("a:Bob=87", "b:Bob=87", "b:Eve=1", "a:Eve=1");
    }

    @Test
    void keepsTheMapsOwnOrder() {
        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("Charlie", 91);
        scores.put("Alice", 95);
        scores.put("Bob", 87);

        assertThat(ScoreReport.lines(scores)).containsExactly("Charlie=91", "Alice=95", "Bob=87");
    }

    @Test
    void aNullScoreShowsADash() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("Dana", null);

        assertThat(ScoreReport.lines(scores)).containsExactly("Dana=-");
    }

    @Test
    void aThrowingFirstSkipsTheSecond() {
        List<String> log = new ArrayList<>();
        IllegalStateException failure = new IllegalStateException("first failed");
        BiConsumer<String, Integer> first = (k, v) -> {
            throw failure;
        };
        BiConsumer<String, Integer> second = (k, v) -> log.add("second:" + k);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> ScoreReport.both(first, second).accept("Bob", 87))
                .isSameAs(failure);
        assertThat(log).isEmpty();
    }

    @Test
    void aLargeMapIsReportedInItsOwnOrder() throws Exception {
        Map<String, Integer> scores = new LinkedHashMap<>();
        List<String> expected = new ArrayList<>();
        for (int i = 50_000; i > 0; i--) {
            scores.put("p" + i, i);
            expected.add("p" + i + "=" + i);
        }
        java.util.concurrent.ForkJoinPool pool = new java.util.concurrent.ForkJoinPool(4);
        try {
            List<String> lines = pool.submit(() -> ScoreReport.lines(scores)).get();
            assertThat(lines).isEqualTo(expected);
        } finally {
            pool.shutdown();
        }
    }
}
