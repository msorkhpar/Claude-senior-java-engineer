package practice;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.*;

class RetryExecutorTest {

    /** An action that fails its first `failures` calls, each with a new exception, then returns "ok". */
    static final class Flaky implements Callable<String> {
        final int failures;
        final List<Exception> thrown = new ArrayList<>();
        int calls;

        Flaky(int failures) {
            this.failures = failures;
        }

        @Override
        public String call() throws Exception {
            calls++;
            if (calls <= failures) {
                Exception e = new IllegalStateException("attempt " + calls);
                thrown.add(e);
                throw e;
            }
            return "ok";
        }
    }

    @Test
    void retriesUntilTheActionSucceeds() throws Exception {
        Flaky action = new Flaky(2);
        assertThat(new RetryExecutor(3, 0).executeWithRetry(action)).isEqualTo("ok");
        assertThat(action.calls).isEqualTo(3);
        Flaky steady = new Flaky(0);
        assertThat(new RetryExecutor(3, 0).executeWithRetry(steady)).isEqualTo("ok");
        assertThat(steady.calls).isEqualTo(1);
    }

    @Test
    void zeroRetriesRunsTheActionExactlyOnce() throws Exception {
        Flaky steady = new Flaky(0);
        assertThat(new RetryExecutor(0, 0).executeWithRetry(steady)).isEqualTo("ok");
        assertThat(steady.calls).isEqualTo(1);
        Flaky failing = new Flaky(5);
        assertThatThrownBy(() -> new RetryExecutor(0, 0).executeWithRetry(failing)).isInstanceOf(IllegalStateException.class);
        assertThat(failing.calls).isEqualTo(1);
    }

    @Test
    void theLastFailureIsRethrown() throws Exception {
        Flaky failing = new Flaky(10);
        Throwable thrown = catchThrowable(() -> new RetryExecutor(2, 0).executeWithRetry(failing));
        assertThat(failing.calls).isEqualTo(3);
        assertThat(thrown).isSameAs(failing.thrown.get(2)).hasMessage("attempt 3");
    }

    @Test
    void negativeSettingsAreRefused() throws Exception {
        assertThatThrownBy(() -> new RetryExecutor(-1, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RetryExecutor(1, -5)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theDelayIsMillisecondsBetweenTries() throws Exception {
        java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();
        RetryExecutor retry = new RetryExecutor(1, 300);
        long start = System.nanoTime();
        String result = org.junit.jupiter.api.Assertions.assertTimeoutPreemptively(java.time.Duration.ofSeconds(5), () -> retry.executeWithRetry(() -> {
            if (calls.incrementAndGet() == 1) {
                throw new IllegalStateException("first try fails");
            }
            return "ok";
        }));
        long tookMs = (System.nanoTime() - start) / 1_000_000;
        assertThat(result).isEqualTo("ok");
        assertThat(tookMs).as("the retry waited the 300 ms delay").isGreaterThanOrEqualTo(290);
    }

    @Test
    void noDelayAfterTheLastTry() throws Exception {
        RetryExecutor retry = new RetryExecutor(0, 1000);
        IllegalStateException boom = new IllegalStateException("boom");
        long start = System.nanoTime();
        assertThatThrownBy(() -> retry.executeWithRetry(() -> {
            throw boom;
        })).isSameAs(boom);
        long tookMs = (System.nanoTime() - start) / 1_000_000;
        assertThat(tookMs).as("no sleep after the last try").isLessThan(900);
    }

    @Test
    void checkedFailuresAreRetriedToo() throws Exception {
        java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();
        RetryExecutor retry = new RetryExecutor(3, 0);
        assertThat(retry.executeWithRetry(() -> {
            if (calls.incrementAndGet() < 3) {
                throw new java.io.IOException("disk busy");
            }
            return "ok";
        })).isEqualTo("ok");
        assertThat(calls.get()).isEqualTo(3);
    }
}
