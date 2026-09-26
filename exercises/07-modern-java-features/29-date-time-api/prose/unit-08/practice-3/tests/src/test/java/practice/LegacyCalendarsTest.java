package practice;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

class LegacyCalendarsTest {

    private static GregorianCalendar gregorian(String zone, String instant) {
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone(zone), Locale.US);
        calendar.setTimeInMillis(Instant.parse(instant).toEpochMilli());
        return calendar;
    }

    @Test
    void convertsAUtcCalendar() {
        ZonedDateTime zoned = LegacyCalendars.toZoned(gregorian("UTC", "2024-03-15T10:00:00Z"));
        assertThat(zoned.toInstant()).isEqualTo(Instant.parse("2024-03-15T10:00:00Z"));
        assertThat(zoned.toLocalDateTime()).isEqualTo(LocalDateTime.of(2024, 3, 15, 10, 0));
        Calendar later = LegacyCalendars.plusOneDay(gregorian("UTC", "2024-03-15T10:00:00Z"));
        assertThat(later.toInstant()).isEqualTo(Instant.parse("2024-03-16T10:00:00Z"));
    }

    @Test
    void keepsTheCalendarsZone() {
        ZonedDateTime zoned = LegacyCalendars.toZoned(gregorian("America/New_York", "2024-03-15T14:00:00Z"));
        // AssertJ compares ZonedDateTimes by instant, so the zone and the wall clock are checked on their own.
        assertThat(zoned.getZone()).isEqualTo(ZoneId.of("America/New_York"));
        assertThat(zoned.toLocalDateTime()).isEqualTo(LocalDateTime.of(2024, 3, 15, 10, 0));
    }

    @Test
    void aNonGregorianCalendarConverts() {
        Calendar japanese = new Calendar.Builder()
                .setCalendarType("japanese")
                .setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"))
                .setInstant(Instant.parse("2024-03-15T10:00:00Z").toEpochMilli())
                .build();
        assertThat(japanese).isNotInstanceOf(GregorianCalendar.class);
        ZonedDateTime zoned = LegacyCalendars.toZoned(japanese);
        assertThat(zoned.getZone()).isEqualTo(ZoneId.of("Asia/Tokyo"));
        assertThat(zoned.toLocalDateTime()).isEqualTo(LocalDateTime.of(2024, 3, 15, 19, 0));
    }

    @Test
    void theCallersCalendarIsUntouched() {
        GregorianCalendar mine = gregorian("UTC", "2024-03-15T10:00:00Z");
        Calendar later = LegacyCalendars.plusOneDay(mine);
        assertThat(mine.toInstant()).isEqualTo(Instant.parse("2024-03-15T10:00:00Z"));
        assertThat(later).isNotSameAs(mine);
    }

    @Test
    void aDayLaterKeepsTheWallClock() {
        Calendar later = LegacyCalendars.plusOneDay(gregorian("America/New_York", "2024-03-09T17:00:00Z"));
        assertThat(later.toInstant()).isEqualTo(Instant.parse("2024-03-10T16:00:00Z"));
        assertThat(later.getTimeZone().toZoneId()).isEqualTo(ZoneId.of("America/New_York"));
    }
}
