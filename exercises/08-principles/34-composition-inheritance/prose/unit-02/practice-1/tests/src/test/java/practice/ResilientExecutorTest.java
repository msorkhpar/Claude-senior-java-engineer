package practice;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

class ResilientExecutorTest {

    /** A strategy the test controls: retry while attempt <= max, a fixed delay, and a record of each question. */
    static final class Scripted implements RetryStrategy {
        final int max;
        final long delay;
        final List<Integer> asked = new ArrayList<>();
        final List<Exception> seen = new ArrayList<>();

        Scripted(int max, long delay) {
            this.max = max;
            this.delay = delay;
        }

        @Override
        public boolean shouldRetry(int attempt, Exception lastException) {
            asked.add(attempt);
            seen.add(lastException);
            return attempt <= max;
        }

        @Override
        public long delayMillis(int attempt) {
            return delay;
        }
    }

    /** Retries while attempt <= max, waiting attempt * 10 ms after attempt `attempt`. */
    static final class Stepped implements RetryStrategy {
        final int max;

        Stepped(int max) {
            this.max = max;
        }

        @Override
        public boolean shouldRetry(int attempt, Exception lastException) {
            return attempt <= max;
        }

        @Override
        public long delayMillis(int attempt) {
            return attempt * 10L;
        }
    }

    /** A task that throws a new exception on each of its first `failures` calls, then returns "ok". */
    static Supplier<String> failing(int failures, List<RuntimeException> thrown) {
        int[] calls = {0};
        return () -> {
            calls[0]++;
            if (calls[0] <= failures) {
                RuntimeException e = new IllegalStateException("attempt " + calls[0] + " failed");
                thrown.add(e);
                throw e;
            }
            return new String("ok");
        };
    }

    @Test
    void retriesUntilTheTaskSucceeds() throws Exception {
        Scripted strategy = new Scripted(3, 7);
        List<Long> sleeps = new ArrayList<>();
        ResilientExecutor executor = new ResilientExecutor(strategy, sleeps::add);
        assertThat(executor.execute(failing(2, new ArrayList<>()))).isEqualTo("ok");
        assertThat(executor.attempts()).isEqualTo(3);
        assertThat(sleeps).containsExactly(7L, 7L);
    }

    @Test
    void givesUpWithTheLastException() throws Exception {
        List<RuntimeException> thrown = new ArrayList<>();
        List<Long> sleeps = new ArrayList<>();
        ResilientExecutor executor = new ResilientExecutor(new Scripted(2, 0), sleeps::add);
        Throwable caught = catchThrowable(() -> executor.execute(failing(10, thrown)));
        assertThat(thrown).hasSize(3);
        assertThat(caught).isSameAs(thrown.get(2));
        assertThat(executor.attempts()).isEqualTo(3);
        assertThat(sleeps).as("a delay of 0 is not slept").isEmpty();
    }

    @Test
    void theInjectedStrategyDecidesHowOftenToRetry() throws Exception {
        List<RuntimeException> thrown = new ArrayList<>();
        ResilientExecutor never = new ResilientExecutor(new Scripted(0, 0), d -> { });
        Throwable caught = catchThrowable(() -> never.execute(failing(1, thrown)));
        assertThat(caught).isSameAs(thrown.get(0));
        assertThat(never.attempts()).isEqualTo(1);
        Scripted five = new Scripted(5, 0);
        ResilientExecutor patient = new ResilientExecutor(five, d -> { });
        assertThat(patient.execute(failing(5, new ArrayList<>()))).isEqualTo("ok");
        assertThat(patient.attempts()).isEqualTo(6);
        assertThat(five.asked).containsExactly(1, 2, 3, 4, 5);
        List<RuntimeException> seenThrown = new ArrayList<>();
        Scripted asking = new Scripted(3, 0);
        new ResilientExecutor(asking, d -> { }).execute(failing(2, seenThrown));
        assertThat(asking.seen).as("the strategy is handed each exception the task threw")
                .hasSize(2).satisfiesExactly(e -> assertThat(e).isSameAs(seenThrown.get(0)), e -> assertThat(e).isSameAs(seenThrown.get(1)));
        List<Long> steps = new ArrayList<>();
        ResilientExecutor stepped = new ResilientExecutor(new Stepped(3), steps::add);
        assertThat(stepped.execute(failing(2, new ArrayList<>()))).isEqualTo("ok");
        assertThat(steps).as("the delay asked for is the one for the attempt that failed").containsExactly(10L, 20L);
        assertThat(stepped.execute(failing(1, new ArrayList<>()))).isEqualTo("ok");
        assertThat(stepped.attempts()).as("attempts add up over execute calls").isEqualTo(5);
    }

    @Test
    void backoffDoublesAndIsCapped() throws Exception {
        ExponentialBackoff backoff = new ExponentialBackoff(5, 100, 1000);
        assertThat(backoff.delayMillis(1)).isEqualTo(100);
        assertThat(backoff.delayMillis(2)).isEqualTo(200);
        assertThat(backoff.delayMillis(3)).isEqualTo(400);
        assertThat(backoff.delayMillis(4)).isEqualTo(800);
        assertThat(backoff.delayMillis(5)).isEqualTo(1000);
        assertThat(backoff.delayMillis(6)).isEqualTo(1000);
        assertThat(backoff.shouldRetry(5, new RuntimeException())).isTrue();
        assertThat(backoff.shouldRetry(6, new RuntimeException())).isFalse();
    }

    @Test
    void aHugeAttemptStillWaitsTheCap() throws Exception {
        ExponentialBackoff backoff = new ExponentialBackoff(5, 100, 1000);
        assertThat(backoff.delayMillis(40)).isEqualTo(1000);
        assertThat(backoff.delayMillis(64)).isEqualTo(1000);
        assertThat(backoff.delayMillis(Integer.MAX_VALUE)).isEqualTo(1000);
        for (int attempt = 7; attempt <= 70; attempt++) {
            assertThat(backoff.delayMillis(attempt)).as("delayMillis(%d)", attempt).isEqualTo(1000);
        }
    }
}
