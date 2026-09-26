package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class MultiWaySumTest {

    private ForkJoinPool pool;

    @BeforeEach
    void startPool() {
        pool = new ForkJoinPool(3);
    }

    @AfterEach
    void stopPool() {
        pool.shutdownNow();
    }

    /** Records each leaf as "start-end". */
    private static final class Leaves implements MultiWaySum.Probe {
        private final List<int[]> seen = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void leaf(int start, int end) {
            seen.add(new int[]{start, end});
        }

        List<String> sorted() {
            List<String> out = new ArrayList<>();
            synchronized (seen) {
                seen.stream().sorted((x, y) -> x[0] != y[0] ? Integer.compare(x[0], y[0]) : Integer.compare(x[1], y[1]))
                        .forEach(r -> out.add(r[0] + "-" + r[1]));
            }
            return out;
        }
    }

    @Test
    void sumsInChunks() {
        Leaves leaves = new Leaves();
        assertThat(pool.invoke(new MultiWaySum(LongStream.rangeClosed(1, 12).toArray(), 0, 12, 3, 2, leaves))).isEqualTo(78L);
        assertThat(leaves.sorted()).containsExactly("0-4", "4-8", "8-12");
    }

    @Test
    void theLastChunkTakesTheRemainder() {
        Leaves leaves = new Leaves();
        assertThat(pool.invoke(new MultiWaySum(LongStream.rangeClosed(1, 10).toArray(), 0, 10, 3, 2, leaves))).isEqualTo(55L);
        assertThat(leaves.sorted()).containsExactly("0-3", "3-6", "6-10");
    }

    @Test
    void neverMoreWaysThanElements() {
        Leaves leaves = new Leaves();
        assertThat(pool.invoke(new MultiWaySum(new long[]{5, 6, 7}, 0, 3, 8, 1, leaves))).isEqualTo(18L);
        assertThat(leaves.sorted()).containsExactly("0-1", "1-2", "2-3");
    }

    @Test
    void chunksRunAtTheSameTime() {
        CyclicBarrier allChunks = new CyclicBarrier(3);
        MultiWaySum.Probe meet = (start, end) -> {
            try {
                allChunks.await(3, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new IllegalStateException("chunk " + start + "-" + end + " did not run beside the others", e);
            }
        };
        assertThat(pool.invoke(new MultiWaySum(new long[]{1, 1, 1, 1, 1, 1}, 0, 6, 3, 1, meet))).isEqualTo(6L);
    }
}
