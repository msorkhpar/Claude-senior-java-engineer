package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static practice.LitmusModel.fence;
import static practice.LitmusModel.read;
import static practice.LitmusModel.write;

class LitmusModelTest {

    @Test
    void eachThreadWritingThenReadingTheOtherCanEndAnyOfFourWays() {
        Set<Map<String, Integer>> out = LitmusModel.outcomesWithStoreBuffers(List.of(
                List.of(write("north", 4), read("south", "n")),
                List.of(write("south", 6), read("north", "s"))));
        assertThat(out).containsExactlyInAnyOrder(
                Map.of("n", 0, "s", 0), Map.of("n", 6, "s", 0),
                Map.of("n", 0, "s", 4), Map.of("n", 6, "s", 4));
    }

    @Test
    void anotherThreadSeesAWriteEitherBeforeOrAfterItReachesMemory() {
        Set<Map<String, Integer>> out = LitmusModel.outcomesWithStoreBuffers(List.of(
                List.of(write("level", 8)),
                List.of(read("level", "seen"))));
        assertThat(out).containsExactlyInAnyOrder(Map.of("seen", 0), Map.of("seen", 8));
    }

    @Test
    void aThreadReadsWhatItJustWrote() {
        Set<Map<String, Integer>> out = LitmusModel.outcomesWithStoreBuffers(List.of(
                List.of(write("count", 3), read("count", "mine"), write("count", 11), read("count", "again"))));
        assertThat(out).containsExactly(Map.of("mine", 3, "again", 11));
    }

    @Test
    void fencesBetweenTheWriteAndTheReadRuleOutBothReadsMissing() {
        Set<Map<String, Integer>> out = LitmusModel.outcomesWithStoreBuffers(List.of(
                List.of(write("north", 4), fence(), read("south", "n")),
                List.of(write("south", 6), fence(), read("north", "s"))));
        assertThat(out).doesNotContain(Map.of("n", 0, "s", 0));
        assertThat(out).contains(Map.of("n", 6, "s", 4));
    }

    @Test
    void anotherThreadSeesOneThreadsWritesInTheOrderTheyWereMade() {
        Set<Map<String, Integer>> out = LitmusModel.outcomesWithStoreBuffers(List.of(
                List.of(write("body", 12), write("ready", 1)),
                List.of(read("ready", "r"), read("body", "b"))));
        assertThat(out).doesNotContain(Map.of("r", 1, "b", 0));
        assertThat(out).contains(Map.of("r", 1, "b", 12), Map.of("r", 0, "b", 12));
    }
}
