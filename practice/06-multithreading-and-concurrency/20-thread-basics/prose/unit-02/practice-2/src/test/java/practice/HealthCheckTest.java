package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 20, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class HealthCheckTest {

    /** A probe that hangs until released (at most 10 s), the way a call to a dead host does. */
    private static Runnable hangsUntil(CountDownLatch release) {
        return () -> {
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }

    /** A started daemon thread running the probe. */
    private static Thread started(Runnable probe) {
        Thread t = new Thread(probe, "probe-under-test");
        t.setDaemon(true);
        t.start();
        return t;
    }

    /** Calls await on a daemon thread; the latch opens when it returns. */
    private static CountDownLatch awaitOnAnotherThread(HealthCheck check, Thread probe, long deadline,
                                                       AtomicReference<Boolean> answer) {
        CountDownLatch returned = new CountDownLatch(1);
        Thread caller = new Thread(() -> {
            try {
                answer.set(check.await(probe, deadline));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                returned.countDown();
            }
        }, "health-request");
        caller.setDaemon(true);
        caller.start();
        return returned;
    }

    @Test
    void reportsProbesThatFinishInTimeAsPassed() throws InterruptedException {
        Map<String, Runnable> probes = new LinkedHashMap<>();
        probes.put("postgres", () -> { });
        probes.put("redis", () -> Math.sqrt(2));

        Map<String, HealthCheck.Status> report = new HealthCheck().run(probes, Duration.ofSeconds(5));

        assertThat(report).containsExactly(
                Map.entry("postgres", HealthCheck.Status.PASSED),
                Map.entry("redis", HealthCheck.Status.PASSED));
    }

    @Test
    void reportsAProbeStillRunningAtTheDeadlineAsTimedOut() throws InterruptedException {
        CountDownLatch release = new CountDownLatch(1);
        try {
            Map<String, Runnable> probes = new LinkedHashMap<>();
            probes.put("kafka", hangsUntil(release));
            probes.put("s3", () -> { });

            Map<String, HealthCheck.Status> report = new HealthCheck().run(probes, Duration.ofMillis(250));

            assertThat(report).containsEntry("kafka", HealthCheck.Status.TIMED_OUT)
                    .containsEntry("s3", HealthCheck.Status.PASSED);
        } finally {
            release.countDown();
        }
    }

    @Test
    void doesNotWaitOnceTheDeadlineIsReached() throws InterruptedException {
        CountDownLatch release = new CountDownLatch(1);
        try {
            Thread hung = started(hangsUntil(release));
            HealthCheck check = new HealthCheck();

            AtomicReference<Boolean> spent = new AtomicReference<>();
            CountDownLatch first = awaitOnAnotherThread(check, hung, System.nanoTime() - 1_000_000, spent);
            assertThat(first.await(3, TimeUnit.SECONDS)).as("await returned with the budget spent").isTrue();
            assertThat(spent.get()).isFalse();

            AtomicReference<Boolean> almost = new AtomicReference<>();
            CountDownLatch second = awaitOnAnotherThread(check, hung, System.nanoTime() + 300_000, almost);
            assertThat(second.await(3, TimeUnit.SECONDS)).as("await returned with 0.3 ms left").isTrue();
            assertThat(almost.get()).isFalse();
        } finally {
            release.countDown();
        }
    }
}
