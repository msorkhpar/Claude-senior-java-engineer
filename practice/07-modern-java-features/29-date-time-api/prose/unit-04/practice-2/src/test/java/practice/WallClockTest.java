package practice;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class WallClockTest {

    private static final ZoneId NEW_YORK = ZoneId.of("America/New_York");
    private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");
    private static final Instant EVENING_UTC = Instant.parse("2024-03-15T18:30:00Z");

    @Test
    void convertsThroughAZone() {
        assertThat(WallClock.localAt(EVENING_UTC, NEW_YORK)).isEqualTo(LocalDateTime.of(2024, 3, 15, 14, 30));
        assertThat(WallClock.localAt(EVENING_UTC, ZoneOffset.UTC)).isEqualTo(LocalDateTime.of(2024, 3, 15, 18, 30));
        assertThat(WallClock.localAt(Instant.parse("2024-01-15T19:30:00Z"), NEW_YORK))
                .as("winter: New York is UTC-5").isEqualTo(LocalDateTime.of(2024, 1, 15, 14, 30));
        assertThat(WallClock.instantOf(LocalDateTime.of(2024, 3, 15, 14, 30), ZoneOffset.UTC))
                .isEqualTo(Instant.parse("2024-03-15T14:30:00Z"));
    }

    @Test
    void theZoneDecidesTheInstant() {
        LocalDateTime local = LocalDateTime.of(2024, 3, 15, 14, 30);
        assertThat(WallClock.instantOf(local, NEW_YORK)).isEqualTo(Instant.parse("2024-03-15T18:30:00Z"));
        assertThat(WallClock.instantOf(local, TOKYO)).isEqualTo(Instant.parse("2024-03-15T05:30:00Z"));
        assertThat(WallClock.instantOf(LocalDateTime.of(2024, 1, 15, 14, 30), NEW_YORK))
                .as("winter: New York is UTC-5").isEqualTo(Instant.parse("2024-01-15T19:30:00Z"));
    }

    @Test
    void theDateCanChange() {
        assertThat(WallClock.localAt(EVENING_UTC, TOKYO)).isEqualTo(LocalDateTime.of(2024, 3, 16, 3, 30));
    }
}
