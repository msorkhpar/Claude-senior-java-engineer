package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class CountMatchingTest {

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
    void countsTheMatches() {
        int[] values = {1, 2, 3, 2, 1, 2};
        AtomicInteger counter = new AtomicInteger();
        pool.invoke(new CountMatching(values, 0, values.length, 6, 2, counter));
        assertThat(counter.get()).isEqualTo(3);
    }

    @Test
    void addsToWhatTheCounterHolds() {
        int[] values = {7, 1, 7, 7, 2, 7, 3, 7};
        AtomicInteger counter = new AtomicInteger(10);
        pool.invoke(new CountMatching(values, 0, values.length, 2, 7, counter));
        assertThat(counter.get()).isEqualTo(15);
    }

    @Test
    void aNullCounterIsRefusedWhenBuilt() {
        assertThatThrownBy(() -> new CountMatching(new int[]{1, 2, 3}, 0, 3, 2, 2, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
