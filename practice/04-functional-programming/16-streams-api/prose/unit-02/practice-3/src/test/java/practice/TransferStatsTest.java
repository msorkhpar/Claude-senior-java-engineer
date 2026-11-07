package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class TransferStatsTest {

    private final TransferStats stats = new TransferStats();

    @Test
    void addsUpTheSizesOfTheTransfers() {
        assertThat(stats.totalBytes(new int[] {4_096, 65_536, 512})).isEqualTo(70_144L);
        assertThat(stats.totalBytes(new int[] {})).isZero();
    }

    @Test
    void averagesTheRecentDurations() {
        // the last two are 300 and 300, and the last three 260 + 300 + 300
        assertThat(stats.meanOfLast(new int[] {90, 260, 300, 300}, 2).getAsDouble()).isCloseTo(300.0, within(1e-9));
        assertThat(stats.meanOfLast(new int[] {75, 75}, 5).getAsDouble()).isCloseTo(75.0, within(1e-9));
    }

    @Test
    void aTotalLargerThanAnIntIsKept() {
        int[] isoImages = {1_900_000_000, 1_800_000_000, 700_000_000};

        assertThat(stats.totalBytes(isoImages)).isEqualTo(4_400_000_000L);
    }

    @Test
    void noTransfersMeansNoMean() {
        assertThat(stats.meanOfLast(new int[] {}, 10)).isEmpty();
        assertThat(stats.summary(new int[] {}, new int[] {}, 10)).isEqualTo("total 0 B, recent mean n/a");
    }

    @Test
    void theNewestDurationIsPartOfTheWindow() {
        assertThat(stats.meanOfLast(new int[] {40, 50, 60, 1_000}, 2).getAsDouble()).isCloseTo(530.0, within(1e-9));
    }
}
