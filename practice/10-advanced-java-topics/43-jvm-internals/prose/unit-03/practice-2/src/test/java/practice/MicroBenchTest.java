package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

class MicroBenchTest {

    /** A clock that moves only when the work runs: 250 ns per call. */
    private final AtomicLong now = new AtomicLong(1_000_000);
    private final List<Long> seen = new ArrayList<>();

    private final LongUnaryOperator work = x -> {
        seen.add(x);
        now.addAndGet(250);
        return x * 3 + 1;
    };

    private final MicroBench bench = new MicroBench(now::get);

    @Test
    void reportsTheTimePerCallOfTheTimedRound() {
        MicroBench.Report report = bench.measure(work, new long[] {17, 4, 90, 61, 8}, 0);

        assertThat(report.nanosPerCall()).isEqualTo(250.0);
    }

    @Test
    void runsTheWarmUpRoundsBeforeTheTimedOne() {
        bench.measure(work, new long[] {12, 7, 33}, 4);

        assertThat(seen).hasSize(15);
    }

    @Test
    void everyValueTheWorkReturnsReachesTheChecksum() {
        MicroBench.Report report = bench.measure(work, new long[] {5, 11, 2, 40}, 2);

        long perRound = (5 * 3 + 1) + (11 * 3 + 1) + (2 * 3 + 1) + (40 * 3 + 1);
        assertThat(report.checksum()).isEqualTo(3 * perRound);
    }

    @Test
    void everyRoundGivesTheWorkEachInputInTurn() {
        bench.measure(work, new long[] {9, 21, 3}, 1);

        assertThat(seen).containsExactly(9L, 21L, 3L, 9L, 21L, 3L);
    }

    @Test
    void theWarmUpIsNotPartOfTheTime() {
        MicroBench.Report report = bench.measure(work, new long[] {6, 14, 25, 3}, 3);

        assertThat(report.nanosPerCall()).isEqualTo(250.0);
    }
}
