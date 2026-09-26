package practice;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LogStampTest {

    @Test
    void formatsAMorningEntry() {
        assertThat(LogStamp.format(LocalDateTime.of(2024, 3, 15, 9, 3, 7))).isEqualTo("2024-03-15 09:03:07");
        assertThat(LogStamp.format(LocalDateTime.of(2024, 1, 5, 10, 1, 0))).isEqualTo("2024-01-05 10:01:00");
        assertThat(LogStamp.format(LocalDateTime.of(2024, 3, 15, 9, 3, 7, 500_000_000))).isEqualTo("2024-03-15 09:03:07");
    }

    @Test
    void theMonthIsNotTheMinute() {
        assertThat(LogStamp.format(LocalDateTime.of(2024, 7, 15, 9, 41, 0))).isEqualTo("2024-07-15 09:41:00");
    }

    @Test
    void afternoonsUseTheTwentyFourHourClock() {
        assertThat(LogStamp.format(LocalDateTime.of(2024, 3, 15, 14, 30, 45))).isEqualTo("2024-03-15 14:30:45");
        assertThat(LogStamp.format(LocalDateTime.of(2024, 3, 15, 0, 15, 0))).isEqualTo("2024-03-15 00:15:00");
    }

    @Test
    void lastDaysOfDecemberKeepTheirYear() {
        assertThat(LogStamp.format(LocalDateTime.of(2024, 12, 30, 23, 59, 59))).isEqualTo("2024-12-30 23:59:59");
        assertThat(LogStamp.format(LocalDateTime.of(2024, 12, 31, 8, 0, 0))).isEqualTo("2024-12-31 08:00:00");
    }
}
