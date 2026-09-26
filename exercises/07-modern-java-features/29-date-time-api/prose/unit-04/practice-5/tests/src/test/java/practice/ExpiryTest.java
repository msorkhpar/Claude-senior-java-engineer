package practice;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpiryTest {

    private static final Instant ISSUED = Instant.parse("2024-03-15T12:00:00Z");
    private static final Duration HALF_HOUR = Duration.ofMinutes(30);

    private static Clock at(String instant) {
        return Clock.fixed(Instant.parse(instant), ZoneOffset.UTC);
    }

    @Test
    void expiresAfterItsTimeToLive() {
        assertThat(Expiry.expiresAt(ISSUED, HALF_HOUR)).isEqualTo(Instant.parse("2024-03-15T12:30:00Z"));
        assertThat(Expiry.expiresAt(ISSUED, Duration.ZERO)).as("a zero time to live is allowed").isEqualTo(ISSUED);
        assertThat(Expiry.expiresAt(ISSUED, Duration.ofNanos(1500))).isEqualTo(ISSUED.plusNanos(1500));
        assertThat(Expiry.isExpired(ISSUED, HALF_HOUR, at("2024-03-15T12:29:59Z"))).isFalse();
        assertThat(Expiry.isExpired(ISSUED, HALF_HOUR, at("2024-03-15T13:00:00Z"))).isTrue();
    }

    @Test
    void aHugeTimeToLiveNeverExpires() {
        assertThat(Expiry.expiresAt(ISSUED, Duration.ofSeconds(Long.MAX_VALUE))).isEqualTo(Instant.MAX);
        assertThat(Expiry.expiresAt(ISSUED, Duration.ofDays(365L * 2_000_000_000L))).isEqualTo(Instant.MAX);
        assertThat(Expiry.isExpired(ISSUED, Duration.ofSeconds(Long.MAX_VALUE), at("2999-01-01T00:00:00Z"))).isFalse();
    }

    @Test
    void expiredAtTheExactInstant() {
        assertThat(Expiry.isExpired(ISSUED, HALF_HOUR, at("2024-03-15T12:30:00Z"))).isTrue();
    }

    @Test
    void aNegativeTimeToLiveIsRefused() {
        assertThatThrownBy(() -> Expiry.expiresAt(ISSUED, Duration.ofMinutes(-1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
