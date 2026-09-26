package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class DotProductTest {

    private ForkJoinPool pool;

    @BeforeEach
    void startPool() {
        pool = new ForkJoinPool(2);
    }

    @AfterEach
    void stopPool() {
        pool.shutdownNow();
    }

    private static final DotProduct.Probe NONE = (start, end) -> {
    };

    @Test
    void multipliesAndAdds() {
        assertThat(pool.invoke(new DotProduct(new int[]{1, 2, 3}, new int[]{4, 5, 6}, 0, 3, 1, NONE))).isEqualTo(32L);
    }

    @Test
    void halvesRunAtTheSameTime() {
        CyclicBarrier bothLeaves = new CyclicBarrier(2);
        DotProduct.Probe meet = (start, end) -> {
            try {
                bothLeaves.await(3, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new IllegalStateException("leaf " + start + "-" + end + " ran alone: the halves did not overlap", e);
            }
        };
        assertThat(pool.invoke(new DotProduct(new int[]{1, 2, 3, 4}, new int[]{1, 1, 1, 1}, 0, 4, 2, meet))).isEqualTo(10L);
    }

    @Test
    void arraysOfDifferentLengthsAreRefused() {
        assertThatThrownBy(() -> new DotProduct(new int[]{1, 2}, new int[]{1, 2, 3}, 0, 2, 1, NONE))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
