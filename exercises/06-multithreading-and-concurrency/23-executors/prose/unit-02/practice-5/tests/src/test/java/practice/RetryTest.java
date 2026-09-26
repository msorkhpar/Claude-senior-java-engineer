package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class RetryTest {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicInteger attempts = new AtomicInteger();

    @AfterEach
    void stop() throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void succeedsAfterTransientFailures() throws Exception {
        Callable<String> flaky = () -> {
            if (attempts.incrementAndGet() <= 2) {
                throw new IOException("transient");
            }
            return "ok";
        };
        assertThat(Retry.call(executor, flaky, 3)).isEqualTo("ok");
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void makesMaxRetriesPlusOneAttempts() {
        Callable<String> broken = () -> {
            attempts.incrementAndGet();
            throw new IOException("down");
        };
        assertThatThrownBy(() -> Retry.call(executor, broken, 2)).isInstanceOf(IOException.class);
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void throwsTheLastAttemptsOwnException() {
        Callable<String> broken = () -> {
            throw new IOException("attempt " + attempts.incrementAndGet());
        };
        assertThatThrownBy(() -> Retry.call(executor, broken, 2))
                .isInstanceOf(IOException.class).hasMessage("attempt 3");
    }

    @Test
    void anErrorStaysWrapped() {
        Callable<String> broken = () -> {
            attempts.incrementAndGet();
            throw new AssertionError("broken");
        };
        assertThatThrownBy(() -> Retry.call(executor, broken, 0))
                .isInstanceOf(ExecutionException.class)
                .cause().isInstanceOf(AssertionError.class).hasMessage("broken");
        assertThat(attempts.get()).isEqualTo(1);
    }

    @Test
    void aHungAttemptIsCancelledAndRetried() throws Exception {
        CountDownLatch never = new CountDownLatch(1);
        Callable<String> hangsOnce = () -> {
            if (attempts.incrementAndGet() == 1) {
                never.await(30, TimeUnit.SECONDS);
                return "late";
            }
            return "ok";
        };
        assertThat(Retry.call(executor, hangsOnce, 1)).isEqualTo("ok");
        assertThat(attempts.get()).isEqualTo(2);
    }

    @Test
    void aTimedOutLastAttemptIsTheFailure() throws Exception {
        Callable<String> task = () -> {
            if (attempts.incrementAndGet() == 1) {
                throw new IOException("attempt 1");
            }
            new CountDownLatch(1).await(8, TimeUnit.SECONDS);
            return "never";
        };
        assertThatThrownBy(() -> Retry.call(executor, task, 1))
                .as("the last attempt timed out, so its TimeoutException is what is thrown")
                .isInstanceOf(TimeoutException.class);
        assertThat(attempts.get()).isEqualTo(2);
    }

    @Test
    void eachAttemptGetsTheFullFiveSeconds() throws Exception {
        Callable<String> task = () -> {
            attempts.incrementAndGet();
            Thread.sleep(1_000);
            return "ok";
        };
        assertThat(Retry.call(executor, task, 0)).as("an attempt that takes 1 s is within its 5 s").isEqualTo("ok");
        assertThat(attempts.get()).isEqualTo(1);
    }
}
