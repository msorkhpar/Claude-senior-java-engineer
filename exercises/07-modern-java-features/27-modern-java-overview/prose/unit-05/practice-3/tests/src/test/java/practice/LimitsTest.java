package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class LimitsTest {

    private static final Duration LIMIT = Duration.ofSeconds(10);

    @Test
    void ordinaryRequestsStayWithinTheLimits() {
        assertTimeoutPreemptively(LIMIT, () -> {
            assertThat(Limits.pageSize(75)).isEqualTo(75);
            assertThat(Limits.pageSize(0)).isEqualTo(1);
            assertThat(Limits.pageSize(-12)).isEqualTo(1);
            assertThat(Limits.pageSize(5_000)).isEqualTo(200);
            assertThat(Limits.gauge(30, 120)).isEqualTo(0.25);
            assertThat(Limits.gauge(450, 300)).isEqualTo(1.0);
            assertThat(Limits.gauge(-8, 40)).isEqualTo(0.0);
            assertThat(Limits.gauge(7, 0)).isEqualTo(1.0);
        });
    }

    @Test
    void aSizeBeyondTheIntRangeGetsTheLargestPage() {
        assertTimeoutPreemptively(LIMIT, () -> {
            assertThat(Limits.pageSize(4_294_967_346L)).isEqualTo(200);   // 2^32 + 50
            assertThat(Limits.pageSize(8_589_934_593L)).isEqualTo(200);   // 2^33 + 1
            assertThat(Limits.pageSize(Long.MAX_VALUE)).isEqualTo(200);
        });
    }

    @Test
    void anAccountWithNoQuotaAndNoUsageShowsAnEmptyGauge() {
        assertTimeoutPreemptively(LIMIT, () -> {
            assertThat(Limits.gauge(0, 0)).isEqualTo(0.0);
        });
    }
}
