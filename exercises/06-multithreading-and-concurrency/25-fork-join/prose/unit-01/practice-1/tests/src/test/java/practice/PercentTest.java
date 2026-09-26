package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class PercentTest {

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
    void scalesToPercentOfTheMax() {
        int[] values = {5, 10, 20, 15, 0, 2};
        Percent.ofMax(pool, values, 2);
        assertThat(values).containsExactly(25, 50, 100, 75, 0, 10);
        int[] empty = {};
        Percent.ofMax(pool, empty, 2);
        assertThat(empty).isEmpty();
    }

    @Test
    void allZerosStayZero() {
        int[] values = {0, 0, 0, 0};
        Percent.ofMax(pool, values, 2);
        assertThat(values).containsExactly(0, 0, 0, 0);
    }

    @Test
    void largeValuesDoNotOverflow() {
        int[] values = {50_000_000, 100_000_000, 25_000_000};
        Percent.ofMax(pool, values, 1);
        assertThat(values).containsExactly(50, 100, 25);
    }

    @Test
    void percentagesRoundDown() {
        int[] values = {2, 3};
        Percent.ofMax(pool, values, 1);
        assertThat(values).containsExactly(66, 100);
    }

    @Test
    void theGivenPoolDoesTheWork() {
        int[] values = {3, 7, 5};
        Percent.ofMax(pool, values, 1);
        assertThat(values).containsExactly(42, 100, 71);
        assertThat(pool.getPoolSize()).as("the work ran on the given pool's own workers, not the common pool").isGreaterThan(0);
    }

    @Test
    void aSingleValueIsItsOwnMax() {
        int[] one = {7};
        Percent.ofMax(pool, one, 2);
        assertThat(one).containsExactly(100);
        int[] zero = {0};
        Percent.ofMax(pool, zero, 2);
        assertThat(zero).containsExactly(0);
    }
}
