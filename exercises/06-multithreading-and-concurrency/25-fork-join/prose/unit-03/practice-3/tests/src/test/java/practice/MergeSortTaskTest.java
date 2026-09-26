package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class MergeSortTaskTest {

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
    void sortsTheValues() {
        int[] values = {12, -3, 45, 0, 7, -18, 33, 5, 21, -9};
        int[] sorted = pool.invoke(new MergeSortTask(values, 3));
        assertThat(sorted).containsExactly(-18, -9, -3, 0, 5, 7, 12, 21, 33, 45);
        assertThat(values).containsExactly(12, -3, 45, 0, 7, -18, 33, 5, 21, -9);
        assertThat(pool.invoke(new MergeSortTask(new int[0], 2))).isEmpty();
    }

    @Test
    void aShortArrayIsNotSortedInPlace() {
        int[] values = {3, 1, 2};
        int[] sorted = pool.invoke(new MergeSortTask(values, 4));
        assertThat(sorted).containsExactly(1, 2, 3);
        assertThat(sorted).as("the result is a new array").isNotSameAs(values);
        assertThat(values).as("the input is unchanged").containsExactly(3, 1, 2);
        int[] one = {7};
        int[] sortedOne = pool.invoke(new MergeSortTask(one, 4));
        assertThat(sortedOne).containsExactly(7);
        assertThat(sortedOne).as("even a single value comes back in a new array").isNotSameAs(one);
    }

    @Test
    void keepsEveryDuplicate() {
        int[] values = {4, 1, 4, 1, 2, 4, 2, 1};
        assertThat(pool.invoke(new MergeSortTask(values, 2))).containsExactly(1, 1, 1, 2, 2, 4, 4, 4);
    }

    @Test
    void anEmptyInputIsNotHandedBack() {
        int[] empty = new int[0];
        int[] result = pool.invoke(new MergeSortTask(empty, 2));
        assertThat(result).isEmpty();
        assertThat(result).as("even an empty input is not handed back as the result").isNotSameAs(empty);
    }

    @Test
    void aThresholdOfOneStillEnds() {
        assertThat(pool.invoke(new MergeSortTask(new int[]{2, 1, 3}, 1))).containsExactly(1, 2, 3);
        assertThat(pool.invoke(new MergeSortTask(new int[]{5, 4}, 1))).containsExactly(4, 5);
    }
}
