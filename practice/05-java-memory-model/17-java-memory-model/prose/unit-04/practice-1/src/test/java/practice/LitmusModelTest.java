package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static practice.LitmusModel.read;
import static practice.LitmusModel.write;

class LitmusModelTest {

    /** Two threads, each publishes its own slot, then looks at the other's. */
    private static final List<List<LitmusModel.Op>> CLAIM_SLOTS = List.of(
            List.of(write("left", 7), read("right", "sawRight")),
            List.of(write("right", 9), read("left", "sawLeft")));

    @Test
    void aReaderAloneSeesTheStartingValues() {
        Set<Map<String, Integer>> out = LitmusModel.outcomes(List.of(
                List.of(read("price", "p"), read("tax", "t"))));
        assertThat(out).containsExactlyInAnyOrder(Map.of("p", 0, "t", 0));
    }

    @Test
    void aReadRacingAWriteCanSeeEitherValue() {
        Set<Map<String, Integer>> out = LitmusModel.outcomes(List.of(
                List.of(write("stock", 5)),
                List.of(read("stock", "seen"))));
        assertThat(out).containsExactlyInAnyOrder(Map.of("seen", 0), Map.of("seen", 5));
    }

    @Test
    void aThirdThreadCanSeeTwoWritersInEitherOrder() {
        Set<Map<String, Integer>> out = LitmusModel.outcomes(List.of(
                List.of(write("p", 2)),
                List.of(write("q", 3)),
                List.of(read("p", "rp"), read("q", "rq"))));
        assertThat(out).containsExactlyInAnyOrder(
                Map.of("rp", 0, "rq", 0), Map.of("rp", 2, "rq", 0),
                Map.of("rp", 0, "rq", 3), Map.of("rp", 2, "rq", 3));
    }

    @Test
    void bothThreadsCanSeeTheOtherOnesWrite() {
        assertThat(LitmusModel.outcomes(CLAIM_SLOTS)).contains(Map.of("sawRight", 9, "sawLeft", 7));
    }

    @Test
    void noExecutionMissesBothWrites() {
        Set<Map<String, Integer>> out = LitmusModel.outcomes(CLAIM_SLOTS);
        assertThat(out).doesNotContain(Map.of("sawRight", 0, "sawLeft", 0));
        assertThat(out).contains(Map.of("sawRight", 0, "sawLeft", 7), Map.of("sawRight", 9, "sawLeft", 0));
    }
}
