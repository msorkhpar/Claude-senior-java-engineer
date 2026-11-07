package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 20, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class HealthCheckTest {

    @Test
    void reportsProbesThatFinishWithoutAProblemAsPassed() throws InterruptedException {
        Map<String, Runnable> probes = new LinkedHashMap<>();
        probes.put("postgres", () -> { });
        probes.put("redis", () -> Math.sqrt(2));

        Map<String, HealthCheck.Status> report = new HealthCheck().run(probes, Duration.ofSeconds(5));

        assertThat(report).containsExactly(
                Map.entry("postgres", HealthCheck.Status.PASSED),
                Map.entry("redis", HealthCheck.Status.PASSED));
    }

    @Test
    void reportsAProbeThatThrowsAsFailed() throws InterruptedException {
        Map<String, Runnable> probes = new LinkedHashMap<>();
        probes.put("postgres", () -> { });
        probes.put("redis", () -> {
            throw new IllegalStateException("connection refused: 127.0.0.1:6379");
        });

        Map<String, HealthCheck.Status> report = new HealthCheck().run(probes, Duration.ofSeconds(5));

        assertThat(report).containsExactly(
                Map.entry("postgres", HealthCheck.Status.PASSED),
                Map.entry("redis", HealthCheck.Status.FAILED));
    }

    @Test
    void reportsAProbeThatFailsAnAssertionAsFailed() throws InterruptedException {
        Map<String, Runnable> probes = new LinkedHashMap<>();
        probes.put("replica", () -> {
            throw new AssertionError("replication lag 41 s, limit 30 s");
        });
        probes.put("s3", () -> { });

        Map<String, HealthCheck.Status> report = new HealthCheck().run(probes, Duration.ofSeconds(5));

        assertThat(report).containsExactly(
                Map.entry("replica", HealthCheck.Status.FAILED),
                Map.entry("s3", HealthCheck.Status.PASSED));
    }
}
