package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

class LegacyDaysTest {

    private static final ZoneId UTC = ZoneId.of("UTC");
    private TimeZone saved;

    @BeforeEach
    void aServerOnUtc() {
        saved = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @AfterEach
    void restoreTheZone() {
        TimeZone.setDefault(saved);
    }

    private static Date date(String instant) {
        return new Date(Instant.parse(instant).toEpochMilli());
    }

    @Test
    void convertsInUtc() {
        assertThat(LegacyDays.dayOf(date("2024-03-15T23:30:00Z"), UTC)).isEqualTo(LocalDate.of(2024, 3, 15));
        assertThat(LegacyDays.dayOf(date("2024-03-16T00:30:00Z"), UTC)).isEqualTo(LocalDate.of(2024, 3, 16));
        assertThat(LegacyDays.toDate(LocalDateTime.of(2024, 3, 15, 10, 0), UTC)).isEqualTo(date("2024-03-15T10:00:00Z"));
    }

    @Test
    void theZoneDecidesTheDay() {
        Date lateEvening = date("2024-03-15T23:30:00Z");
        assertThat(LegacyDays.dayOf(lateEvening, ZoneId.of("Asia/Tokyo"))).isEqualTo(LocalDate.of(2024, 3, 16));
        assertThat(LegacyDays.dayOf(lateEvening, ZoneId.of("America/New_York"))).isEqualTo(LocalDate.of(2024, 3, 15));
        // New York is UTC-5 in winter and UTC-4 in summer: 04:30 UTC is the day before in January, the same day in July.
        assertThat(LegacyDays.dayOf(date("2024-01-15T04:30:00Z"), ZoneId.of("America/New_York"))).isEqualTo(LocalDate.of(2024, 1, 14));
        assertThat(LegacyDays.dayOf(date("2024-07-15T04:30:00Z"), ZoneId.of("America/New_York"))).isEqualTo(LocalDate.of(2024, 7, 15));
    }

    @Test
    void aLocalTimeIsReadInItsZone() {
        assertThat(LegacyDays.toDate(LocalDateTime.of(2024, 3, 15, 10, 0), ZoneId.of("America/New_York")))
                .isEqualTo(date("2024-03-15T14:00:00Z"));
        assertThat(LegacyDays.toDate(LocalDateTime.of(2024, 1, 15, 10, 0), ZoneId.of("America/New_York")))
                .isEqualTo(date("2024-01-15T15:00:00Z"));
        assertThat(LegacyDays.toDate(LocalDateTime.of(2024, 7, 15, 10, 0), ZoneId.of("America/New_York")))
                .isEqualTo(date("2024-07-15T14:00:00Z"));
        assertThat(LegacyDays.toDate(LocalDateTime.of(2024, 3, 15, 10, 0), ZoneId.of("Asia/Tokyo")))
                .isEqualTo(date("2024-03-15T01:00:00Z"));
    }

    @Test
    void theServerZoneIsIgnored() {
        TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Kiritimati"));
        assertThat(LegacyDays.dayOf(date("2024-03-15T23:30:00Z"), UTC)).isEqualTo(LocalDate.of(2024, 3, 15));
        assertThat(LegacyDays.toDate(LocalDateTime.of(2024, 3, 15, 10, 0), UTC)).isEqualTo(date("2024-03-15T10:00:00Z"));
        assertThat(TimeZone.getDefault().getID()).isEqualTo("Pacific/Kiritimati");
    }
}
