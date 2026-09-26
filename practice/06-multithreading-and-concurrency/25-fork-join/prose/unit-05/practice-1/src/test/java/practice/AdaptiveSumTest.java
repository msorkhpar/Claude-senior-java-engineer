package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class AdaptiveSumTest {

    private ForkJoinPool pool;

    @BeforeEach
    void startPool() {
        pool = new ForkJoinPool(2);
    }

    @AfterEach
    void stopPool() {
        pool.shutdownNow();
    }

    /** Leaf lengths, as recorded by the probe. */
    private static final class Lengths implements AdaptiveSum.Probe {
        final List<Integer> seen = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void leaf(int start, int end) {
            seen.add(end - start);
        }
    }

    private static long[] ones(int n) {
        long[] values = new long[n];
        Arrays.fill(values, 1);
        return values;
    }

    @Test
    void sumsTheArray() {
        assertThat(AdaptiveSum.sum(pool, LongStream.rangeClosed(1, 100).toArray(), 10, (s, e) -> {
        })).isEqualTo(5050L);
    }

    @Test
    void theThresholdFollowsThePoolsParallelism() {
        Lengths lengths = new Lengths();
        assertThat(AdaptiveSum.sum(pool, ones(160), 5, lengths)).isEqualTo(160L);
        assertThat(lengths.seen).as("parallelism 2: threshold 20").hasSize(8).containsOnly(20);
        ForkJoinPool five = new ForkJoinPool(5);
        try {
            Lengths more = new Lengths();
            assertThat(AdaptiveSum.sum(five, ones(160), 5, more)).isEqualTo(160L);
            assertThat(more.seen).as("parallelism 5: threshold 8").hasSize(32).containsOnly(5);
        } finally {
            five.shutdownNow();
        }
    }

    @Test
    void theFloorWinsForASmallArray() {
        Lengths lengths = new Lengths();
        assertThat(AdaptiveSum.sum(pool, ones(40), 16, lengths)).isEqualTo(40L);
        assertThat(lengths.seen).hasSize(4).containsOnly(10);
    }

    @Test
    void runsInTheGivenPool() {
        List<ForkJoinPool> pools = Collections.synchronizedList(new ArrayList<>());
        AdaptiveSum.sum(pool, ones(160), 5, (s, e) -> pools.add(ForkJoinTask.getPool()));
        assertThat(pools).isNotEmpty().allMatch(p -> p == pool, "is the pool passed in");
    }
}
