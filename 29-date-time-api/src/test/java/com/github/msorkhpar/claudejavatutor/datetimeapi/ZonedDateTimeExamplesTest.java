package com.github.msorkhpar.claudejavatutor.datetimeapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ZonedDateTime and OffsetDateTime Tests")
class ZonedDateTimeExamplesTest {

    @Nested
    @DisplayName("ZonedDateTime Creation")
    class ZonedCreationTests {

        @Test
        @DisplayName("Should create ZonedDateTime from components")
        void testCreateZonedDateTime() {
            ZonedDateTime zdt = ZonedDateTimeExamples.createZonedDateTime(
                    2024, 3, 15, 14, 30, 0, "America/New_York");
            assertThat(zdt.getYear()).isEqualTo(2024);
            assertThat(zdt.getMonthValue()).isEqualTo(3);
            assertThat(zdt.getDayOfMonth()).isEqualTo(15);
            assertThat(zdt.getHour()).isEqualTo(14);
        }

        @Test
        @DisplayName("Should throw for invalid timezone")
        void testInvalidTimezone() {
            assertThatThrownBy(() ->
                    ZonedDateTimeExamples.createZonedDateTime(2024, 3, 15, 14, 30, 0, "Invalid/Zone"))
                    .isInstanceOf(DateTimeException.class);
        }
    }

    @Nested
    @DisplayName("Timezone Conversion")
    class TimezoneConversionTests {

        @Test
        @DisplayName("Should convert timezone preserving instant")
        void testConvertTimezone() {
            ZonedDateTime nyTime = ZonedDateTimeExamples.createZonedDateTime(
                    2024, 3, 15, 12, 0, 0, "America/New_York");
            ZonedDateTime londonTime = ZonedDateTimeExamples.convertTimezone(nyTime, "Europe/London");

            // Same instant, different local time
            assertThat(nyTime.toInstant()).isEqualTo(londonTime.toInstant());
            assertThat(londonTime.getHour()).isNotEqualTo(nyTime.getHour());
        }

        @Test
        @DisplayName("Should check same instant across timezones")
        void testIsSameInstant() {
            ZonedDateTime nyTime = ZonedDateTimeExamples.createZonedDateTime(
                    2024, 3, 15, 12, 0, 0, "America/New_York");
            ZonedDateTime converted = ZonedDateTimeExamples.convertTimezone(nyTime, "Asia/Tokyo");
            assertThat(ZonedDateTimeExamples.isSameInstant(nyTime, converted)).isTrue();
        }

        @Test
        @DisplayName("Should NOT be same instant for same local time in different zones")
        void testNotSameInstant() {
            ZonedDateTime nyTime = ZonedDateTimeExamples.createZonedDateTime(
                    2024, 3, 15, 12, 0, 0, "America/New_York");
            ZonedDateTime tokyoTime = ZonedDateTimeExamples.createZonedDateTime(
                    2024, 3, 15, 12, 0, 0, "Asia/Tokyo");
            assertThat(ZonedDateTimeExamples.isSameInstant(nyTime, tokyoTime)).isFalse();
        }
    }

    @Nested
    @DisplayName("Offset Operations")
    class OffsetTests {

        @Test
        @DisplayName("Should get timezone offset")
        void testGetOffset() {
            ZonedDateTime utcTime = ZonedDateTimeExamples.createZonedDateTime(
                    2024, 1, 15, 12, 0, 0, "UTC");
            ZoneOffset offset = ZonedDateTimeExamples.getOffset(utcTime);
            assertThat(offset).isEqualTo(ZoneOffset.UTC);
        }

        @Test
        @DisplayName("Should create OffsetDateTime")
        void testCreateOffsetDateTime() {
            OffsetDateTime odt = ZonedDateTimeExamples.createOffsetDateTime(
                    2024, 3, 15, 14, 30, 0, 5);
            assertThat(odt.getOffset()).isEqualTo(ZoneOffset.ofHours(5));
        }

        @Test
        @DisplayName("Should convert ZonedDateTime to OffsetDateTime")
        void testToOffsetDateTime() {
            ZonedDateTime zdt = ZonedDateTimeExamples.createZonedDateTime(
                    2024, 3, 15, 14, 30, 0, "America/New_York");
            OffsetDateTime odt = ZonedDateTimeExamples.toOffsetDateTime(zdt);
            assertThat(odt.toInstant()).isEqualTo(zdt.toInstant());
        }
    }

    @Nested
    @DisplayName("Available Zone IDs")
    class AvailableZoneTests {

        @Test
        @DisplayName("Should return available zone IDs")
        void testGetAvailableZoneIds() {
            Set<String> zoneIds = ZonedDateTimeExamples.getAvailableZoneIds();
            assertThat(zoneIds).isNotEmpty();
            assertThat(zoneIds).contains("UTC", "America/New_York", "Europe/London", "Asia/Tokyo");
        }
    }

    @Nested
    @DisplayName("Hour Difference Between Zones")
    class HourDifferenceTests {

        @Test
        @DisplayName("Should calculate offset difference between UTC and Asia/Kolkata")
        void testOffsetDifference() {
            Instant instant = Instant.parse("2024-06-15T12:00:00Z");
            // Asia/Kolkata is UTC+5:30 = 19800 seconds ahead of UTC
            int diffSeconds = ZonedDateTimeExamples.offsetDifferenceInSeconds(
                    instant, "Asia/Kolkata", "UTC");
            assertThat(diffSeconds).isEqualTo(19800); // 5h30m = 5*3600 + 30*60
        }

        @Test
        @DisplayName("Should calculate offset difference between same zones as zero")
        void testOffsetDifferenceSameZone() {
            Instant instant = Instant.parse("2024-06-15T12:00:00Z");
            int diffSeconds = ZonedDateTimeExamples.offsetDifferenceInSeconds(
                    instant, "UTC", "UTC");
            assertThat(diffSeconds).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("DST Handling")
    class DstTests {

        @Test
        @DisplayName("Should handle DST gap by adjusting time forward")
        void testDstGap() {
            // In US Eastern, 2024 spring forward: March 10, 2:00 AM -> 3:00 AM
            ZonedDateTime zdt = ZonedDateTimeExamples.createDuringDstGap(
                    2024, 3, 10, 2, 30, "America/New_York");
            // The API should adjust the time forward
            assertThat(zdt.getHour()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Local to Zoned Conversion")
    class LocalToZonedTests {

        @Test
        @DisplayName("Should convert LocalDateTime to ZonedDateTime")
        void testLocalToZoned() {
            LocalDateTime ldt = LocalDateTime.of(2024, 3, 15, 14, 30);
            ZonedDateTime zdt = ZonedDateTimeExamples.localToZoned(ldt, "America/New_York");
            assertThat(zdt.toLocalDateTime()).isEqualTo(ldt);
            assertThat(zdt.getZone()).isEqualTo(ZoneId.of("America/New_York"));
        }

        @Test
        @DisplayName("Should extract LocalDateTime from ZonedDateTime")
        void testZonedToLocal() {
            ZonedDateTime zdt = ZonedDateTimeExamples.createZonedDateTime(
                    2024, 3, 15, 14, 30, 0, "America/New_York");
            LocalDateTime ldt = ZonedDateTimeExamples.zonedToLocal(zdt);
            assertThat(ldt).isEqualTo(LocalDateTime.of(2024, 3, 15, 14, 30, 0));
        }

        @Test
        @DisplayName("Should convert ZonedDateTime to Instant")
        void testZonedToInstant() {
            ZonedDateTime zdt = ZonedDateTimeExamples.createZonedDateTime(
                    2024, 3, 15, 12, 0, 0, "UTC");
            Instant instant = ZonedDateTimeExamples.zonedToInstant(zdt);
            assertThat(instant).isEqualTo(Instant.parse("2024-03-15T12:00:00Z"));
        }
    }

    @Nested
    @DisplayName("Same Local vs Same Instant")
    class SameLocalVsSameInstantTests {

        @Test
        @DisplayName("Should keep same local time when changing zone with withSameLocal")
        void testWithSameLocal() {
            ZonedDateTime nyTime = ZonedDateTimeExamples.createZonedDateTime(
                    2024, 3, 15, 12, 0, 0, "America/New_York");
            ZonedDateTime tokyoTime = ZonedDateTimeExamples.withSameLocal(nyTime, "Asia/Tokyo");

            // Same local time
            assertThat(tokyoTime.toLocalDateTime()).isEqualTo(nyTime.toLocalDateTime());
            // Different instant
            assertThat(tokyoTime.toInstant()).isNotEqualTo(nyTime.toInstant());
        }
    }
}
