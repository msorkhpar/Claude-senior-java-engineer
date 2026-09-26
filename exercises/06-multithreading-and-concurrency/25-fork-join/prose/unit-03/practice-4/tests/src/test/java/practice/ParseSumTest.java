package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ParseSumTest {

    private ForkJoinPool pool;

    @BeforeEach
    void startPool() {
        pool = new ForkJoinPool(1);
    }

    @AfterEach
    void stopPool() {
        pool.shutdownNow();
    }

    private static final String[] WITH_BAD_ITEM = {"1", "2", "3", "4", "5", "6", "x", "8"};

    @Test
    void sumsTheNumbers() {
        String[] items = IntStream.rangeClosed(1, 10).mapToObj(Integer::toString).toArray(String[]::new);
        assertThat(ParseSum.total(pool, items, 2)).isEqualTo(55L);
        assertThat(ParseSum.total(pool, new String[0], 2)).isZero();
    }

    @Test
    void aBadItemIsNotSwallowed() {
        Throwable thrown = catchThrowable(() -> ParseSum.total(pool, WITH_BAD_ITEM, 2));
        assertThat(thrown).as("a bad item fails the total").isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aBadItemIsReportedWithTheOriginalMessage() {
        Throwable thrown = catchThrowable(() -> ParseSum.total(pool, WITH_BAD_ITEM, 2));
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class).hasMessage("item 6 is not a number: x");
    }

    @Test
    void numbersBeyondIntAreNumbers() {
        assertThat(ParseSum.total(pool, new String[]{"3000000000", "4000000000", "-1"}, 1)).isEqualTo(6_999_999_999L);
    }
}
