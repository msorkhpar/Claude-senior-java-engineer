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
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class SumTaskTest {

    private ForkJoinPool pool;

    @BeforeEach
    void startPool() {
        pool = new ForkJoinPool(2);
    }

    @AfterEach
    void stopPool() {
        pool.shutdownNow();
    }

    /** Records each leaf as "start-end", in sorted order on read. */
    private static final class Leaves implements SumTask.Probe {
        private final List<String> seen = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void leaf(int start, int end) {
            seen.add(start + "-" + end);
        }

        List<String> sorted() {
            List<String> copy = new ArrayList<>(seen);
            copy.sort((a, b) -> Integer.compare(Integer.parseInt(a.split("-")[0]), Integer.parseInt(b.split("-")[0])));
            return copy;
        }
    }

    private static final SumTask.Probe NONE = (start, end) -> {
    };

    private static int[] oneToTen() {
        return IntStream.rangeClosed(1, 10).toArray();
    }

    @Test
    void sumsARange() {
        assertThat(pool.invoke(new SumTask(oneToTen(), 0, 10, 3, NONE))).isEqualTo(55L);
        assertThat(pool.invoke(new SumTask(oneToTen(), 2, 5, 3, NONE))).isEqualTo(12L);
    }

    @Test
    void sumsBeyondTheIntRange() {
        int[] big = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
        assertThat(pool.invoke(new SumTask(big, 0, 4, 2, NONE))).isEqualTo(4L * Integer.MAX_VALUE);
    }

    @Test
    void aRangeOfThresholdLengthIsOneLeaf() {
        Leaves leaves = new Leaves();
        assertThat(pool.invoke(new SumTask(new int[]{1, 2, 3, 4}, 0, 4, 4, leaves))).isEqualTo(10L);
        assertThat(leaves.sorted()).containsExactly("0-4");
    }

    @Test
    void splitsAtTheMidpoint() {
        Leaves leaves = new Leaves();
        assertThat(pool.invoke(new SumTask(oneToTen(), 0, 10, 3, leaves))).isEqualTo(55L);
        assertThat(leaves.sorted()).containsExactly("0-2", "2-5", "5-7", "7-10");
    }

    @Test
    void bothHalvesRunAtTheSameTime() {
        CyclicBarrier bothLeaves = new CyclicBarrier(2);
        SumTask.Probe meet = (start, end) -> {
            try {
                bothLeaves.await(3, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new IllegalStateException("leaf " + start + "-" + end + " ran alone: the halves did not overlap", e);
            }
        };
        assertThat(pool.invoke(new SumTask(new int[]{1, 2, 3, 4}, 0, 4, 2, meet))).isEqualTo(10L);
    }
}
