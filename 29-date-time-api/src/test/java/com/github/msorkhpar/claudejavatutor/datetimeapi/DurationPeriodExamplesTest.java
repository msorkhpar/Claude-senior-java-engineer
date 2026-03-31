package com.github.msorkhpar.claudejavatutor.datetimeapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Duration and Period Tests")
class DurationPeriodExamplesTest {

    @Nested
    @DisplayName("Duration Creation")
    class DurationCreationTests {

        @Test
        @DisplayName("Should create duration from hours, minutes, seconds")
        void testCreateDuration() {
            Duration duration = DurationPeriodExamples.createDuration(1, 30, 15);
            assertThat(duration.toSeconds()).isEqualTo(3600 + 1800 + 15);
        }

        @Test
        @DisplayName("Should create zero duration")
        void testCreateZeroDuration() {
            Duration duration = DurationPeriodExamples.createDuration(0, 0, 0);
            assertThat(duration.isZero()).isTrue();
        }

        @Test
        @DisplayName("Should calculate duration between times")
        void testDurationBetweenTimes() {
            LocalTime start = LocalTime.of(9, 0);
            LocalTime end = LocalTime.of(17, 30);
            Duration duration = DurationPeriodExamples.durationBetweenTimes(start, end);
            assertThat(duration.toHours()).isEqualTo(8);
            assertThat(duration.toMinutesPart()).isEqualTo(30);
        }

        @Test
        @DisplayName("Should handle negative duration between times")
        void testDurationBetweenTimesNegative() {
            LocalTime start = LocalTime.of(17, 0);
            LocalTime end = LocalTime.of(9, 0);
            Duration duration = DurationPeriodExamples.durationBetweenTimes(start, end);
            assertThat(duration.isNegative()).isTrue();
        }

        @Test
        @DisplayName("Should calculate duration between instants")
        void testDurationBetweenInstants() {
            Instant start = Instant.ofEpochSecond(0);
            Instant end = Instant.ofEpochSecond(7200);
            Duration duration = DurationPeriodExamples.durationBetweenInstants(start, end);
            assertThat(duration.toHours()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Duration Conversion")
    class DurationConversionTests {

        @Test
        @DisplayName("Should convert to total minutes")
        void testToTotalMinutes() {
            Duration duration = Duration.ofHours(2).plusMinutes(30);
            assertThat(DurationPeriodExamples.toTotalMinutes(duration)).isEqualTo(150);
        }

        @Test
        @DisplayName("Should convert to total seconds")
        void testToTotalSeconds() {
            Duration duration = Duration.ofMinutes(5);
            assertThat(DurationPeriodExamples.toTotalSeconds(duration)).isEqualTo(300);
        }
    }

    @Nested
    @DisplayName("Duration Operations")
    class DurationOperationTests {

        @Test
        @DisplayName("Should check negative duration")
        void testIsNegative() {
            assertThat(DurationPeriodExamples.isNegative(Duration.ofSeconds(-1))).isTrue();
            assertThat(DurationPeriodExamples.isNegative(Duration.ofSeconds(1))).isFalse();
            assertThat(DurationPeriodExamples.isNegative(Duration.ZERO)).isFalse();
        }

        @Test
        @DisplayName("Should get absolute value of duration")
        void testAbsoluteDuration() {
            Duration negative = Duration.ofHours(-2);
            Duration result = DurationPeriodExamples.absoluteDuration(negative);
            assertThat(result).isEqualTo(Duration.ofHours(2));
        }

        @Test
        @DisplayName("Should multiply duration")
        void testMultiply() {
            Duration duration = Duration.ofMinutes(30);
            assertThat(DurationPeriodExamples.multiply(duration, 3)).isEqualTo(Duration.ofMinutes(90));
        }

        @Test
        @DisplayName("Should parse ISO-8601 duration string")
        void testParseDuration() {
            Duration duration = DurationPeriodExamples.parseDuration("PT2H30M");
            assertThat(duration.toHours()).isEqualTo(2);
            assertThat(duration.toMinutesPart()).isEqualTo(30);
        }

        @Test
        @DisplayName("Should throw for invalid duration string")
        void testParseDurationInvalid() {
            assertThatThrownBy(() -> DurationPeriodExamples.parseDuration("invalid"))
                    .isInstanceOf(DateTimeParseException.class);
        }
    }

    @Nested
    @DisplayName("Period Creation")
    class PeriodCreationTests {

        @Test
        @DisplayName("Should create period from components")
        void testCreatePeriod() {
            Period period = DurationPeriodExamples.createPeriod(1, 6, 15);
            assertThat(period.getYears()).isEqualTo(1);
            assertThat(period.getMonths()).isEqualTo(6);
            assertThat(period.getDays()).isEqualTo(15);
        }

        @Test
        @DisplayName("Should calculate period between dates")
        void testPeriodBetween() {
            LocalDate start = LocalDate.of(2020, 1, 1);
            LocalDate end = LocalDate.of(2024, 7, 15);
            Period period = DurationPeriodExamples.periodBetween(start, end);
            assertThat(period.getYears()).isEqualTo(4);
            assertThat(period.getMonths()).isEqualTo(6);
            assertThat(period.getDays()).isEqualTo(14);
        }

        @Test
        @DisplayName("Should handle negative period")
        void testNegativePeriod() {
            LocalDate start = LocalDate.of(2024, 6, 1);
            LocalDate end = LocalDate.of(2024, 1, 1);
            Period period = DurationPeriodExamples.periodBetween(start, end);
            assertThat(DurationPeriodExamples.isNegativePeriod(period)).isTrue();
        }
    }

    @Nested
    @DisplayName("Period Operations")
    class PeriodOperationTests {

        @Test
        @DisplayName("Should get individual period components")
        void testGetComponents() {
            Period period = Period.of(2, 5, 10);
            assertThat(DurationPeriodExamples.getYears(period)).isEqualTo(2);
            assertThat(DurationPeriodExamples.getMonths(period)).isEqualTo(5);
            assertThat(DurationPeriodExamples.getDays(period)).isEqualTo(10);
        }

        @Test
        @DisplayName("Should calculate total months")
        void testToTotalMonths() {
            Period period = Period.of(2, 3, 15);
            assertThat(DurationPeriodExamples.toTotalMonths(period)).isEqualTo(27);
        }

        @Test
        @DisplayName("Should check if period is zero")
        void testIsZero() {
            assertThat(DurationPeriodExamples.isZero(Period.ZERO)).isTrue();
            assertThat(DurationPeriodExamples.isZero(Period.ofDays(1))).isFalse();
        }

        @Test
        @DisplayName("Should add period to date")
        void testAddPeriod() {
            LocalDate date = LocalDate.of(2024, 1, 31);
            Period period = Period.ofMonths(1);
            // Adding 1 month to Jan 31 gives Feb 29 (leap year 2024)
            assertThat(DurationPeriodExamples.addPeriod(date, period))
                    .isEqualTo(LocalDate.of(2024, 2, 29));
        }

        @Test
        @DisplayName("Should normalize period")
        void testNormalizePeriod() {
            Period period = Period.of(1, 15, 10);
            Period normalized = DurationPeriodExamples.normalizePeriod(period);
            assertThat(normalized.getYears()).isEqualTo(2);
            assertThat(normalized.getMonths()).isEqualTo(3);
            assertThat(normalized.getDays()).isEqualTo(10); // Days not normalized
        }

        @Test
        @DisplayName("Should parse ISO-8601 period string")
        void testParsePeriod() {
            Period period = DurationPeriodExamples.parsePeriod("P1Y2M3D");
            assertThat(period.getYears()).isEqualTo(1);
            assertThat(period.getMonths()).isEqualTo(2);
            assertThat(period.getDays()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should throw for invalid period string")
        void testParsePeriodInvalid() {
            assertThatThrownBy(() -> DurationPeriodExamples.parsePeriod("invalid"))
                    .isInstanceOf(DateTimeParseException.class);
        }
    }

    @Nested
    @DisplayName("Duration vs Period")
    class DurationVsPeriodTests {

        @Test
        @DisplayName("Should count exact days between dates")
        void testDaysBetweenDates() {
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 12, 31);
            // 2024 is a leap year = 366 days, range is 365 days (Jan 1 to Dec 31 exclusive of Dec 31)
            assertThat(DurationPeriodExamples.daysBetweenDates(start, end)).isEqualTo(365);
        }

        @Test
        @DisplayName("Should return zero for same dates")
        void testDaysBetweenSameDates() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            assertThat(DurationPeriodExamples.daysBetweenDates(date, date)).isEqualTo(0);
        }
    }
}
