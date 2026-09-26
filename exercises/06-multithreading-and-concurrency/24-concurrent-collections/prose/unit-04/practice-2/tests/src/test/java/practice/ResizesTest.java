package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ResizesTest {

    /** HashMap's rounding of a requested capacity, so the tests can build a table from a request. */
    private static int tableFor(int requested) {
        return requested <= 1 ? 1 : Integer.highestOneBit(requested - 1) << 1;
    }

    @Test
    void doublesPastTheThreshold() {
        assertThat(Resizes.capacities(16, 0.75f, 13)).containsExactly(16, 32);
        assertThat(Resizes.capacities(16, 0.75f, 1000)).containsExactly(16, 32, 64, 128, 256, 512, 1024, 2048);
        assertThat(Resizes.capacities(128, 0.75f, 97)).containsExactly(128, 256);
        assertThat(Resizes.capacities(16, 2.0f, 33)).containsExactly(16, 32);
    }

    @Test
    void exactlyAtThresholdNoResize() {
        assertThat(Resizes.capacities(16, 0.75f, 12)).containsExactly(16);
        assertThat(Resizes.capacities(128, 0.75f, 96)).containsExactly(128);
        assertThat(Resizes.capacities(16, 0.75f, 24)).containsExactly(16, 32);
        assertThat(Resizes.capacities(16, 0.75f, 13)).as("the 13th put goes above 12").containsExactly(16, 32);
        assertThat(Resizes.capacities(128, 0.75f, 97)).containsExactly(128, 256);
    }

    @Test
    void presizedMapNeverResizes() {
        assertThat(Resizes.initialCapacityFor(100)).isEqualTo(134);
        assertThat(Resizes.initialCapacityFor(1000)).isEqualTo(1334);
        for (int n : new int[] {12, 100, 1000, 5000}) {
            int table = tableFor(Resizes.initialCapacityFor(n));
            assertThat(Resizes.capacities(table, 0.75f, n))
                    .as("a map pre-sized for %d entries", n).hasSize(1);
        }
    }

    @Test
    void thePagesFormulaExactly() {
        assertThat(Resizes.initialCapacityFor(75)).isEqualTo(101);
        assertThat(Resizes.initialCapacityFor(3)).isEqualTo(5);
    }

    @Test
    void theThresholdIsRecomputedAfterEachResize() {
        assertThat(Resizes.capacities(16, 0.6f, 19)).as("32 * 0.6 is 19.2, so the threshold at 32 is 19").containsExactly(16, 32);
        assertThat(Resizes.capacities(2, 0.75f, 5)).containsExactly(2, 4, 8);
    }
}
