package practice;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BatchTest {

    // Every call builds new objects, so no test compares an object with itself.
    private static Batch batch(String device, int... samples) {
        return new Batch(new String(device), samples.clone());
    }

    @Test
    void batchesFromTheSameDeviceWithTheSameSamplesAreEqual() {
        Batch first = batch("greenhouse-7", 2140, 2155, 2161);
        Batch resent = batch("greenhouse-7", 2140, 2155, 2161);

        assertThat(first.equals(resent)).isTrue();
        assertThat(first.equals(batch("greenhouse-7", 2140, 2161, 2155))).isFalse();
        assertThat(first.equals(batch("greenhouse-8", 2140, 2155, 2161))).isFalse();
        assertThat(first.equals(batch("greenhouse-7", 2140, 2155))).isFalse();
    }

    @Test
    void theGatewaysSetDropsAResentBatch() {
        Set<Object> stored = new HashSet<>();
        stored.add(batch("cold-room-2", -180, -182, -179, -181));

        assertThat(stored.add(batch("cold-room-2", -180, -182, -179, -181))).isFalse();
        assertThat(stored.contains(batch("cold-room-2", -180, -182, -179, -181))).isTrue();
        assertThat(stored).hasSize(1);
    }

    @Test
    void equalBatchesHaveEqualHashCodes() {
        for (int i = 0; i < 20; i++) {
            int[] samples = {i, 1000 + i, -i};
            assertThat(batch("dock-" + i, samples).hashCode())
                    .isEqualTo(batch("dock-" + i, samples).hashCode());
        }
    }

    @Test
    void aBatchKeepsItsSamplesWhenTheDriverReusesItsBuffer() {
        int[] buffer = {1990, 2003, 2011};
        Batch stored = new Batch("loft-1", buffer);

        buffer[0] = 2500;
        buffer[1] = 2501;
        buffer[2] = 2502;

        assertThat(stored.samples()).containsExactly(1990, 2003, 2011);
        assertThat(stored.equals(batch("loft-1", 1990, 2003, 2011))).isTrue();
    }

    @Test
    void changingTheSamplesReadFromABatchLeavesTheBatchAsItWas() {
        Batch stored = batch("attic-3", 1712, 1720);
        Set<Object> set = new HashSet<>();
        set.add(stored);

        int[] read = stored.samples();
        read[0] = 0;

        assertThat(stored.samples()).containsExactly(1712, 1720);
        assertThat(set.contains(batch("attic-3", 1712, 1720))).isTrue();
    }
}
