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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ScaleActionTest {

    private ForkJoinPool pool;

    @BeforeEach
    void startPool() {
        pool = new ForkJoinPool(2);
    }

    @AfterEach
    void stopPool() {
        pool.shutdownNow();
    }

    private static final ScaleAction.Probe NONE = (start, end) -> {
    };

    private static double[] oneToTen() {
        return IntStream.rangeClosed(1, 10).asDoubleStream().toArray();
    }

    @Test
    void scalesAShortArray() {
        double[] values = {1, 2, 3, 4};
        pool.invoke(new ScaleAction(values, 0, 4, 4, 2, 1, NONE));
        assertThat(values).containsExactly(3, 5, 7, 9);
    }

    @Test
    void scalesASplitArray() {
        double[] values = oneToTen();
        pool.invoke(new ScaleAction(values, 1, 9, 3, 10, 0, NONE));
        assertThat(values).containsExactly(1, 20, 30, 40, 50, 60, 70, 80, 90, 10);
    }

    @Test
    void leafRangesCoverEachIndexOnce() {
        List<int[]> leaves = Collections.synchronizedList(new ArrayList<>());
        pool.invoke(new ScaleAction(oneToTen(), 0, 10, 3, 1, 0, (start, end) -> leaves.add(new int[]{start, end})));
        List<String> sorted = new ArrayList<>();
        synchronized (leaves) {
            leaves.stream().sorted((a, b) -> Integer.compare(a[0], b[0])).forEach(r -> sorted.add(r[0] + "-" + r[1]));
        }
        assertThat(sorted).containsExactly("0-2", "2-5", "5-7", "7-10");
    }

    @Test
    void bothHalvesRunAtTheSameTime() {
        CyclicBarrier bothLeaves = new CyclicBarrier(2);
        ScaleAction.Probe meet = (start, end) -> {
            try {
                bothLeaves.await(3, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new IllegalStateException("leaf " + start + "-" + end + " ran alone: the halves did not overlap", e);
            }
        };
        double[] values = {1, 2, 3, 4};
        pool.invoke(new ScaleAction(values, 0, 4, 2, 2, 0, meet));
        assertThat(values).containsExactly(2, 4, 6, 8);
    }

    @Test
    void aNullArrayIsRefusedWhenBuilt() {
        assertThatThrownBy(() -> new ScaleAction(null, 0, 0, 1, 1, 0, NONE)).isInstanceOf(IllegalArgumentException.class);
    }
}
