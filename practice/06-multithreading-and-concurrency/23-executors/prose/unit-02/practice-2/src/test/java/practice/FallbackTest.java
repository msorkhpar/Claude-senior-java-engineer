package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class FallbackTest {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final CountDownLatch never = new CountDownLatch(1);

    @AfterEach
    void stop() throws InterruptedException {
        never.countDown();
        executor.shutdownNow();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    /** A task that waits (up to 8 s) for a latch nobody opens, unless it is interrupted. */
    private Callable<String> stuck() {
        return () -> {
            never.await(8, TimeUnit.SECONDS);
            return "late";
        };
    }

    /** Whether the executor's only thread takes a new task within 5 s, that is, whether it is free. */
    private boolean threadIsFree() throws Exception {
        try {
            return executor.submit(() -> "free").get(5, TimeUnit.SECONDS).equals("free");
        } catch (java.util.concurrent.TimeoutException e) {
            return false;
        }
    }

    @Test
    void returnsTheResultInTime() {
        assertThat(Fallback.within(executor, () -> "fresh", 5, TimeUnit.SECONDS, "stale")).isEqualTo("fresh");
    }

    @Test
    void aTimeoutCancelsTheTask() throws Exception {
        assertThat(Fallback.within(executor, stuck(), 200, TimeUnit.MILLISECONDS, "stale")).isEqualTo("stale");
        assertThat(threadIsFree()).as("the timed-out task was cancelled and freed the thread").isTrue();
    }

    @Test
    void aFailureKeepsItsCause() {
        Callable<String> failing = () -> {
            throw new IOException("down");
        };
        assertThatThrownBy(() -> Fallback.within(executor, failing, 5, TimeUnit.SECONDS, "stale"))
                .isInstanceOf(RuntimeException.class)
                .cause().isInstanceOf(IOException.class).hasMessage("down");
    }

    @Test
    void anInterruptedCallerKeepsItsFlag() throws Exception {
        Thread.currentThread().interrupt();
        String result = Fallback.within(executor, stuck(), 5, TimeUnit.SECONDS, "stale");
        boolean flagKept = Thread.interrupted();
        assertThat(result).isEqualTo("stale");
        assertThat(flagKept).as("the caller's interrupt flag is still set").isTrue();
        assertThat(threadIsFree()).as("the abandoned task was cancelled and freed the thread").isTrue();
    }
}
