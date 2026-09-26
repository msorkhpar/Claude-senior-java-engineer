package practice;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class ScoresTest {

    @Test
    void countsTheDroppedEntries() {
        Map<String, Integer> scores = new HashMap<>(Map.of("a", 1, "b", 2, "c", 5));
        assertThat(Scores.dropBelow(scores, 3)).isEqualTo(2);
        assertThat(Scores.dropBelow(new HashMap<>(), 3)).isZero();
    }

    @Test
    void theCallersMapLosesThem() {
        Map<String, Integer> scores = new HashMap<>(Map.of("a", 1, "b", 2, "c", 5));
        Scores.dropBelow(scores, 3);
        assertThat(scores).containsExactly(Map.entry("c", 5));
    }

    @Test
    void aScoreEqualToTheMinimumStays() {
        Map<String, Integer> scores = new HashMap<>(Map.of("a", 1000, "b", 999));
        assertThat(Scores.dropBelow(scores, 1000)).isEqualTo(1);
        assertThat(scores).containsExactly(Map.entry("a", 1000));
    }

    @Test
    void anyIntScoreComparesCorrectly() {
        Map<String, Integer> negative = new HashMap<>(Map.of("a", -5, "b", -1));
        assertThat(Scores.dropBelow(negative, -3)).isEqualTo(1);
        assertThat(negative).containsExactly(Map.entry("b", -1));
        Map<String, Integer> extreme = new HashMap<>(Map.of("a", 2_000_000_000, "b", -2_000_000_000));
        assertThat(Scores.dropBelow(extreme, -2_000_000_000)).isZero();
        assertThat(extreme).hasSize(2);
    }
}
