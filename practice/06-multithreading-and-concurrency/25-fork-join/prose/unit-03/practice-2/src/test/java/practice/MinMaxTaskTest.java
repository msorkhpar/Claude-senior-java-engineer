package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class MinMaxTaskTest {

    private ForkJoinPool pool;

    @BeforeEach
    void startPool() {
        pool = new ForkJoinPool(2);
    }

    @AfterEach
    void stopPool() {
        pool.shutdownNow();
    }

    @Test
    void findsTheSmallestAndLargest() {
        int[] values = {3, -1, 7, 2, 9, 0};
        assertThat(pool.invoke(new MinMaxTask(values, 0, values.length, 2))).isEqualTo(new MinMaxTask.Range(-1, 9));
        assertThat(pool.invoke(new MinMaxTask(values, 1, 3, 2))).isEqualTo(new MinMaxTask.Range(-1, 7));
    }

    @Test
    void allNegativeValues() {
        int[] values = {-5, -3, -9, -4};
        assertThat(pool.invoke(new MinMaxTask(values, 0, values.length, 1))).isEqualTo(new MinMaxTask.Range(-9, -3));
        int[] lowest = {Integer.MIN_VALUE, Integer.MIN_VALUE};
        assertThat(pool.invoke(new MinMaxTask(lowest, 0, 2, 1))).isEqualTo(new MinMaxTask.Range(Integer.MIN_VALUE, Integer.MIN_VALUE));
    }

    @Test
    void allPositiveValues() {
        int[] values = {5, 3, 9, 4};
        assertThat(pool.invoke(new MinMaxTask(values, 0, values.length, 1))).isEqualTo(new MinMaxTask.Range(3, 9));
    }

    @Test
    void anEmptyRangeIsRefused() {
        int[] values = {5, 3, 9, 4};
        assertThatThrownBy(() -> new MinMaxTask(values, 2, 2, 1)).isInstanceOf(IllegalArgumentException.class);
    }
}
