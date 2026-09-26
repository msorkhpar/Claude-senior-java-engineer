package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class TotalsTest {

    private static Callable<Integer> range(int from, int to) {
        return () -> {
            int sum = 0;
            for (int i = from; i <= to; i++) {
                sum += i;
            }
            return sum;
        };
    }

    @Test
    void sumsThePartsResults() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            assertThat(Totals.total(pool, List.of(range(1, 50), range(51, 100)))).isEqualTo(5050L);
            assertThat(Totals.total(pool, List.of(() -> 7))).isEqualTo(7L);
            assertThat(Totals.total(pool, List.of())).isEqualTo(0L);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void submitsEveryPartBeforeWaiting() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch arrived = new CountDownLatch(2);
        Callable<Integer> meetTheOther = () -> {
            arrived.countDown();
            if (!arrived.await(1, TimeUnit.SECONDS)) {
                throw new TimeoutException("the other part was never submitted while this one ran");
            }
            return 1;
        };
        try {
            assertThat(Totals.total(pool, List.of(meetTheOther, meetTheOther))).isEqualTo(2L);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void aFailureCarriesThePartsOwnException() {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        Callable<Integer> broken = () -> {
            throw new IOException("disk gone");
        };
        try {
            assertThatThrownBy(() -> Totals.total(pool, List.of(range(1, 3), broken)))
                    .isInstanceOf(IllegalStateException.class)
                    .cause()
                    .isInstanceOf(IOException.class)
                    .hasMessage("disk gone");
        } finally {
            pool.shutdownNow();
        }
    }
}
