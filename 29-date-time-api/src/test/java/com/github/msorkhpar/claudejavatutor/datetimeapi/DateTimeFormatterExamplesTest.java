package com.github.msorkhpar.claudejavatutor.datetimeapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DateTimeFormatter Tests")
class DateTimeFormatterExamplesTest {

    @Nested
    @DisplayName("ISO Formatting")
    class IsoFormattingTests {

        @Test
        @DisplayName("Should format date as ISO-8601")
        void testFormatIsoDate() {
            LocalDate date = LocalDate.of(2024, 3, 15);
            assertThat(DateTimeFormatterExamples.formatIsoDate(date)).isEqualTo("2024-03-15");
        }

        @Test
        @DisplayName("Should format datetime as ISO-8601")
        void testFormatIsoDateTime() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 0);
            assertThat(DateTimeFormatterExamples.formatIsoDateTime(dateTime)).isEqualTo("2024-03-15T14:30:00");
        }

        @Test
        @DisplayName("Should format datetime with seconds and nanos")
        void testFormatIsoDateTimeWithNanos() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 45, 123000000);
            String result = DateTimeFormatterExamples.formatIsoDateTime(dateTime);
            assertThat(result).startsWith("2024-03-15T14:30:45");
        }
    }

    @Nested
    @DisplayName("Custom Pattern Formatting")
    class CustomPatternTests {

        @Test
        @DisplayName("Should format date with custom pattern")
        void testFormatWithPattern() {
            LocalDate date = LocalDate.of(2024, 3, 15);
            assertThat(DateTimeFormatterExamples.formatWithPattern(date, "dd/MM/yyyy"))
                    .isEqualTo("15/03/2024");
        }

        @Test
        @DisplayName("Should format datetime with custom pattern")
        void testFormatDateTimeWithPattern() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 0);
            assertThat(DateTimeFormatterExamples.formatDateTimeWithPattern(dateTime, "yyyy-MM-dd HH:mm:ss"))
                    .isEqualTo("2024-03-15 14:30:00");
        }

        @Test
        @DisplayName("Should format with day of week pattern")
        void testFormatWithDayOfWeek() {
            LocalDate date = LocalDate.of(2024, 3, 15); // Friday
            String result = DateTimeFormatterExamples.formatWithPattern(date, "EEEE, MMMM dd, yyyy");
            assertThat(result).contains("Friday");
            assertThat(result).contains("March");
        }
    }

    @Nested
    @DisplayName("Localized Formatting")
    class LocalizedFormattingTests {

        @Test
        @DisplayName("Should format date with US locale")
        void testFormatLocalizedUS() {
            LocalDate date = LocalDate.of(2024, 3, 15);
            String result = DateTimeFormatterExamples.formatLocalized(date, FormatStyle.MEDIUM, Locale.US);
            assertThat(result).isEqualTo("Mar 15, 2024");
        }

        @Test
        @DisplayName("Should format with locale-specific pattern")
        void testFormatWithLocale() {
            LocalDate date = LocalDate.of(2024, 3, 15);
            String resultUS = DateTimeFormatterExamples.formatWithLocale(date, "MMMM", Locale.US);
            String resultFR = DateTimeFormatterExamples.formatWithLocale(date, "MMMM", Locale.FRANCE);
            assertThat(resultUS).isEqualTo("March");
            assertThat(resultFR).isEqualToIgnoringCase("mars");
        }
    }

    @Nested
    @DisplayName("Parsing")
    class ParsingTests {

        @Test
        @DisplayName("Should parse ISO date")
        void testParseIsoDate() {
            LocalDate date = DateTimeFormatterExamples.parseIsoDate("2024-03-15");
            assertThat(date).isEqualTo(LocalDate.of(2024, 3, 15));
        }

        @Test
        @DisplayName("Should parse date with custom pattern")
        void testParseDateWithPattern() {
            LocalDate date = DateTimeFormatterExamples.parseDateWithPattern("15/03/2024", "dd/MM/yyyy");
            assertThat(date).isEqualTo(LocalDate.of(2024, 3, 15));
        }

        @Test
        @DisplayName("Should parse datetime with custom pattern")
        void testParseDateTimeWithPattern() {
            LocalDateTime dateTime = DateTimeFormatterExamples.parseDateTimeWithPattern(
                    "2024-03-15 14:30:00", "yyyy-MM-dd HH:mm:ss");
            assertThat(dateTime).isEqualTo(LocalDateTime.of(2024, 3, 15, 14, 30, 0));
        }

        @Test
        @DisplayName("Should throw for invalid date string")
        void testParseInvalid() {
            assertThatThrownBy(() -> DateTimeFormatterExamples.parseIsoDate("invalid"))
                    .isInstanceOf(DateTimeParseException.class);
        }

        @Test
        @DisplayName("Should throw for mismatched pattern")
        void testParseMismatchedPattern() {
            assertThatThrownBy(() ->
                    DateTimeFormatterExamples.parseDateWithPattern("2024-03-15", "dd/MM/yyyy"))
                    .isInstanceOf(DateTimeParseException.class);
        }
    }

    @Nested
    @DisplayName("Safe Parsing")
    class SafeParsingTests {

        @Test
        @DisplayName("Should return date for valid input")
        void testSafeParseValid() {
            LocalDate date = DateTimeFormatterExamples.safeParse("15/03/2024", "dd/MM/yyyy");
            assertThat(date).isEqualTo(LocalDate.of(2024, 3, 15));
        }

        @Test
        @DisplayName("Should return null for invalid input")
        void testSafeParseInvalid() {
            LocalDate date = DateTimeFormatterExamples.safeParse("invalid", "dd/MM/yyyy");
            assertThat(date).isNull();
        }

        @Test
        @DisplayName("Should return null for empty string")
        void testSafeParseEmpty() {
            LocalDate date = DateTimeFormatterExamples.safeParse("", "dd/MM/yyyy");
            assertThat(date).isNull();
        }
    }

    @Nested
    @DisplayName("Thread Safety")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Should format with thread-safe formatter")
        void testThreadSafeFormatter() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 45);
            String result = DateTimeFormatterExamples.formatWithThreadSafeFormatter(dateTime);
            assertThat(result).isEqualTo("2024-03-15 14:30:45");
        }
    }

    @Nested
    @DisplayName("ZonedDateTime and Instant Formatting")
    class ZonedFormattingTests {

        @Test
        @DisplayName("Should format ZonedDateTime")
        void testFormatZonedDateTime() {
            ZonedDateTime zdt = ZonedDateTime.of(2024, 3, 15, 14, 30, 0, 0, ZoneId.of("America/New_York"));
            String result = DateTimeFormatterExamples.formatZonedDateTime(zdt, "yyyy-MM-dd HH:mm:ss z");
            assertThat(result).contains("2024-03-15 14:30:00");
        }

        @Test
        @DisplayName("Should format Instant with timezone")
        void testFormatInstant() {
            Instant instant = Instant.parse("2024-03-15T12:00:00Z");
            String result = DateTimeFormatterExamples.formatInstant(
                    instant, ZoneId.of("UTC"), "yyyy-MM-dd HH:mm:ss");
            assertThat(result).isEqualTo("2024-03-15 12:00:00");
        }
    }
}
