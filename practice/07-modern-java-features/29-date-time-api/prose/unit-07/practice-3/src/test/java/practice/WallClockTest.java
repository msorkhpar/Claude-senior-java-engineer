package practice;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WallClockTest {

    private static final ZoneId NY = ZoneId.of("America/New_York");
    private static final ZoneId LONDON = ZoneId.of("Europe/London");

    @Test
    void anOrdinaryTimeHasOneOffset() {
        ZonedDateTime march = WallClock.at(LocalDateTime.of(2024, 3, 15, 2, 30), NY);
        assertThat(march.toLocalDateTime()).isEqualTo(LocalDateTime.of(2024, 3, 15, 2, 30));
        assertThat(march.getOffset()).isEqualTo(ZoneOffset.ofHours(-4));
        assertThat(march.getZone()).isEqualTo(NY);
        ZonedDateTime january = WallClock.at(LocalDateTime.of(2024, 1, 15, 9, 0), LONDON);
        assertThat(january.toLocalDateTime()).isEqualTo(LocalDateTime.of(2024, 1, 15, 9, 0));
        assertThat(january.getOffset()).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    void aTimeTheClocksSkipIsRefused() {
        assertThatThrownBy(() -> WallClock.at(LocalDateTime.of(2024, 3, 10, 2, 30), NY))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> WallClock.at(LocalDateTime.of(2024, 3, 31, 1, 30), LONDON))
                .isInstanceOf(IllegalArgumentException.class);
        // Lord Howe Island moves its clocks by only 30 minutes: 02:00 becomes 02:30.
        assertThatThrownBy(() -> WallClock.at(LocalDateTime.of(2024, 10, 6, 2, 15), ZoneId.of("Australia/Lord_Howe")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aRepeatedTimeIsTheSecondOne() {
        ZonedDateTime ny = WallClock.at(LocalDateTime.of(2024, 11, 3, 1, 30), NY);
        assertThat(ny.toLocalDateTime()).isEqualTo(LocalDateTime.of(2024, 11, 3, 1, 30));
        assertThat(ny.getOffset()).isEqualTo(ZoneOffset.ofHours(-5));
        ZonedDateTime london = WallClock.at(LocalDateTime.of(2024, 10, 27, 1, 30), LONDON);
        assertThat(london.toLocalDateTime()).isEqualTo(LocalDateTime.of(2024, 10, 27, 1, 30));
        assertThat(london.getOffset()).isEqualTo(ZoneOffset.UTC);
    }
}
