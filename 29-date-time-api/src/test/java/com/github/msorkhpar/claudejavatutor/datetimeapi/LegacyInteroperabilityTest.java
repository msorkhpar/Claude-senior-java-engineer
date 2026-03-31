package com.github.msorkhpar.claudejavatutor.datetimeapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Legacy Interoperability Tests")
class LegacyInteroperabilityTest {

    @Nested
    @DisplayName("Date to Instant Conversions")
    class DateInstantTests {

        @Test
        @DisplayName("Should convert Date to Instant")
        void testDateToInstant() {
            Date date = new Date(0L); // Epoch
            Instant instant = LegacyInteroperability.dateToInstant(date);
            assertThat(instant).isEqualTo(Instant.EPOCH);
        }

        @Test
        @DisplayName("Should convert Instant to Date")
        void testInstantToDate() {
            Instant instant = Instant.ofEpochMilli(1000);
            Date date = LegacyInteroperability.instantToDate(instant);
            assertThat(date.getTime()).isEqualTo(1000);
        }

        @Test
        @DisplayName("Should round-trip Date -> Instant -> Date")
        void testDateInstantRoundTrip() {
            Date original = new Date();
            Instant instant = LegacyInteroperability.dateToInstant(original);
            Date roundTripped = LegacyInteroperability.instantToDate(instant);
            assertThat(roundTripped).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("Date to LocalDate Conversions")
    class DateLocalDateTests {

        @Test
        @DisplayName("Should convert Date to LocalDate in UTC")
        void testDateToLocalDate() {
            // Create a date for 2024-03-15 at UTC midnight
            Instant instant = LocalDate.of(2024, 3, 15).atStartOfDay(ZoneId.of("UTC")).toInstant();
            Date date = Date.from(instant);
            LocalDate localDate = LegacyInteroperability.dateToLocalDate(date, ZoneId.of("UTC"));
            assertThat(localDate).isEqualTo(LocalDate.of(2024, 3, 15));
        }

        @Test
        @DisplayName("Should convert LocalDate to Date")
        void testLocalDateToDate() {
            LocalDate localDate = LocalDate.of(2024, 3, 15);
            Date date = LegacyInteroperability.localDateToDate(localDate, ZoneId.of("UTC"));
            Instant instant = date.toInstant();
            assertThat(instant.atZone(ZoneId.of("UTC")).toLocalDate())
                    .isEqualTo(LocalDate.of(2024, 3, 15));
        }
    }

    @Nested
    @DisplayName("Date to LocalDateTime Conversions")
    class DateLocalDateTimeTests {

        @Test
        @DisplayName("Should convert Date to LocalDateTime")
        void testDateToLocalDateTime() {
            Instant instant = Instant.parse("2024-03-15T14:30:00Z");
            Date date = Date.from(instant);
            LocalDateTime ldt = LegacyInteroperability.dateToLocalDateTime(date, ZoneId.of("UTC"));
            assertThat(ldt).isEqualTo(LocalDateTime.of(2024, 3, 15, 14, 30, 0));
        }

        @Test
        @DisplayName("Should convert LocalDateTime to Date")
        void testLocalDateTimeToDate() {
            LocalDateTime ldt = LocalDateTime.of(2024, 3, 15, 14, 30);
            Date date = LegacyInteroperability.localDateTimeToDate(ldt, ZoneId.of("UTC"));
            Instant instant = date.toInstant();
            assertThat(instant).isEqualTo(Instant.parse("2024-03-15T14:30:00Z"));
        }
    }

    @Nested
    @DisplayName("Calendar to ZonedDateTime Conversions")
    class CalendarZonedTests {

        @Test
        @DisplayName("Should convert GregorianCalendar to ZonedDateTime")
        void testCalendarToZonedDateTime() {
            GregorianCalendar gc = new GregorianCalendar(2024, Calendar.MARCH, 15, 14, 30, 0);
            gc.setTimeZone(TimeZone.getTimeZone("UTC"));
            ZonedDateTime zdt = LegacyInteroperability.calendarToZonedDateTime(gc);
            assertThat(zdt.getYear()).isEqualTo(2024);
            assertThat(zdt.getMonthValue()).isEqualTo(3);
            assertThat(zdt.getHour()).isEqualTo(14);
        }

        @Test
        @DisplayName("Should convert ZonedDateTime to GregorianCalendar")
        void testZonedDateTimeToCalendar() {
            ZonedDateTime zdt = ZonedDateTime.of(2024, 3, 15, 14, 30, 0, 0, ZoneId.of("UTC"));
            GregorianCalendar gc = LegacyInteroperability.zonedDateTimeToCalendar(zdt);
            assertThat(gc.get(Calendar.YEAR)).isEqualTo(2024);
            assertThat(gc.get(Calendar.MONTH)).isEqualTo(Calendar.MARCH);
        }

        @Test
        @DisplayName("Should round-trip Calendar -> ZonedDateTime -> Calendar")
        void testCalendarRoundTrip() {
            GregorianCalendar original = new GregorianCalendar(2024, Calendar.JUNE, 15);
            original.setTimeZone(TimeZone.getTimeZone("America/New_York"));
            ZonedDateTime zdt = LegacyInteroperability.calendarToZonedDateTime(original);
            GregorianCalendar roundTripped = LegacyInteroperability.zonedDateTimeToCalendar(zdt);
            assertThat(roundTripped.getTimeInMillis()).isEqualTo(original.getTimeInMillis());
        }
    }

    @Nested
    @DisplayName("TimeZone to ZoneId Conversions")
    class TimeZoneZoneIdTests {

        @Test
        @DisplayName("Should convert TimeZone to ZoneId")
        void testTimeZoneToZoneId() {
            TimeZone tz = TimeZone.getTimeZone("America/New_York");
            ZoneId zoneId = LegacyInteroperability.timeZoneToZoneId(tz);
            assertThat(zoneId.getId()).isEqualTo("America/New_York");
        }

        @Test
        @DisplayName("Should convert ZoneId to TimeZone")
        void testZoneIdToTimeZone() {
            ZoneId zoneId = ZoneId.of("Europe/London");
            TimeZone tz = LegacyInteroperability.zoneIdToTimeZone(zoneId);
            assertThat(tz.getID()).isEqualTo("Europe/London");
        }
    }

    @Nested
    @DisplayName("SQL Type Conversions")
    class SqlTypeTests {

        @Test
        @DisplayName("Should convert sql.Date to LocalDate")
        void testSqlDateToLocalDate() {
            java.sql.Date sqlDate = java.sql.Date.valueOf("2024-03-15");
            LocalDate localDate = LegacyInteroperability.sqlDateToLocalDate(sqlDate);
            assertThat(localDate).isEqualTo(LocalDate.of(2024, 3, 15));
        }

        @Test
        @DisplayName("Should convert LocalDate to sql.Date")
        void testLocalDateToSqlDate() {
            LocalDate localDate = LocalDate.of(2024, 3, 15);
            java.sql.Date sqlDate = LegacyInteroperability.localDateToSqlDate(localDate);
            assertThat(sqlDate.toString()).isEqualTo("2024-03-15");
        }

        @Test
        @DisplayName("Should convert Timestamp to LocalDateTime")
        void testTimestampToLocalDateTime() {
            Timestamp ts = Timestamp.valueOf("2024-03-15 14:30:00");
            LocalDateTime ldt = LegacyInteroperability.timestampToLocalDateTime(ts);
            assertThat(ldt).isEqualTo(LocalDateTime.of(2024, 3, 15, 14, 30, 0));
        }

        @Test
        @DisplayName("Should convert LocalDateTime to Timestamp")
        void testLocalDateTimeToTimestamp() {
            LocalDateTime ldt = LocalDateTime.of(2024, 3, 15, 14, 30, 0);
            Timestamp ts = LegacyInteroperability.localDateTimeToTimestamp(ldt);
            assertThat(ts.toLocalDateTime()).isEqualTo(ldt);
        }

        @Test
        @DisplayName("Should convert Timestamp to Instant")
        void testTimestampToInstant() {
            Timestamp ts = Timestamp.valueOf("2024-03-15 14:30:00");
            Instant instant = LegacyInteroperability.timestampToInstant(ts);
            assertThat(instant).isNotNull();
        }

        @Test
        @DisplayName("Should convert Instant to Timestamp")
        void testInstantToTimestamp() {
            Instant instant = Instant.parse("2024-03-15T14:30:00Z");
            Timestamp ts = LegacyInteroperability.instantToTimestamp(instant);
            assertThat(ts.toInstant()).isEqualTo(instant);
        }

        @Test
        @DisplayName("Should convert sql.Time to LocalTime")
        void testSqlTimeToLocalTime() {
            java.sql.Time sqlTime = java.sql.Time.valueOf("14:30:00");
            LocalTime localTime = LegacyInteroperability.sqlTimeToLocalTime(sqlTime);
            assertThat(localTime).isEqualTo(LocalTime.of(14, 30, 0));
        }

        @Test
        @DisplayName("Should convert LocalTime to sql.Time")
        void testLocalTimeToSqlTime() {
            LocalTime localTime = LocalTime.of(14, 30, 0);
            java.sql.Time sqlTime = LegacyInteroperability.localTimeToSqlTime(localTime);
            assertThat(sqlTime.toString()).isEqualTo("14:30:00");
        }
    }
}
