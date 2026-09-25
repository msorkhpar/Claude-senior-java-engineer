package com.github.msorkhpar.claudejavatutor.datetimeapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Instant and Timestamp Tests")
class InstantTimestampExamplesTest {

    @Nested
    @DisplayName("Instant Creation")
    class CreationTests {

        @Test
        @DisplayName("Should create Instant from epoch seconds")
        void testFromEpochSeconds() {
            Instant instant = InstantTimestampExamples.fromEpochSeconds(0);
            assertThat(instant).isEqualTo(Instant.EPOCH);
        }

        @Test
        @DisplayName("Should create Instant from epoch millis")
        void testFromEpochMillis() {
            Instant instant = InstantTimestampExamples.fromEpochMillis(1000);
            assertThat(instant).isEqualTo(Instant.ofEpochSecond(1));
        }

        @Test
        @DisplayName("Should create Instant with nanosecond precision")
        void testFromEpochSecondAndNanos() {
            Instant instant = InstantTimestampExamples.fromEpochSecondAndNanos(1, 500_000_000);
            assertThat(instant.getEpochSecond()).isEqualTo(1);
            assertThat(instant.getNano()).isEqualTo(500_000_000);
        }

        @Test
        @DisplayName("Should handle negative epoch seconds (before epoch)")
        void testNegativeEpochSeconds() {
            Instant instant = InstantTimestampExamples.fromEpochSeconds(-1);
            assertThat(instant.getEpochSecond()).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("Instant Conversion")
    class ConversionTests {

        @Test
        @DisplayName("Should convert to epoch seconds")
        void testToEpochSeconds() {
            Instant instant = Instant.ofEpochSecond(12345);
            assertThat(InstantTimestampExamples.toEpochSeconds(instant)).isEqualTo(12345);
        }

        @Test
        @DisplayName("Should convert to epoch millis")
        void testToEpochMillis() {
            Instant instant = Instant.ofEpochMilli(12345678);
            assertThat(InstantTimestampExamples.toEpochMillis(instant)).isEqualTo(12345678);
        }

        @Test
        @DisplayName("Should convert Instant to LocalDateTime in timezone")
        void testToLocalDateTime() {
            Instant instant = Instant.parse("2024-03-15T12:00:00Z");
            LocalDateTime ldt = InstantTimestampExamples.toLocalDateTime(instant, ZoneId.of("America/New_York"));
            // New York is UTC-4 in March (EDT)
            assertThat(ldt.getHour()).isEqualTo(8);
        }

        @Test
        @DisplayName("Should convert LocalDateTime to Instant")
        void testFromLocalDateTime() {
            LocalDateTime ldt = LocalDateTime.of(2024, 3, 15, 12, 0);
            Instant instant = InstantTimestampExamples.fromLocalDateTime(ldt, ZoneId.of("UTC"));
            assertThat(instant).isEqualTo(Instant.parse("2024-03-15T12:00:00Z"));
        }
    }

    @Nested
    @DisplayName("Instant Arithmetic")
    class ArithmeticTests {

        @Test
        @DisplayName("Should calculate millis between instants")
        void testMillisBetween() {
            Instant start = Instant.ofEpochMilli(1000);
            Instant end = Instant.ofEpochMilli(5000);
            assertThat(InstantTimestampExamples.millisBetween(start, end)).isEqualTo(4000);
        }

        @Test
        @DisplayName("Should return negative for reversed order")
        void testMillisBetweenReversed() {
            Instant start = Instant.ofEpochMilli(5000);
            Instant end = Instant.ofEpochMilli(1000);
            assertThat(InstantTimestampExamples.millisBetween(start, end)).isEqualTo(-4000);
        }

        @Test
        @DisplayName("Should add duration to instant")
        void testAddDuration() {
            Instant instant = Instant.ofEpochSecond(0);
            Duration duration = Duration.ofHours(1);
            Instant result = InstantTimestampExamples.addDuration(instant, duration);
            assertThat(result).isEqualTo(Instant.ofEpochSecond(3600));
        }

        @Test
        @DisplayName("Should calculate seconds between instants")
        void testSecondsBetween() {
            Instant start = Instant.ofEpochSecond(0);
            Instant end = Instant.ofEpochSecond(3600);
            assertThat(InstantTimestampExamples.secondsBetween(start, end)).isEqualTo(3600);
        }
    }

    @Nested
    @DisplayName("Instant Comparison")
    class ComparisonTests {

        @Test
        @DisplayName("Should check if instant is before another")
        void testIsBefore() {
            Instant first = Instant.ofEpochSecond(100);
            Instant second = Instant.ofEpochSecond(200);
            assertThat(InstantTimestampExamples.isBefore(first, second)).isTrue();
            assertThat(InstantTimestampExamples.isBefore(second, first)).isFalse();
        }

        @Test
        @DisplayName("Should check if instant is within window")
        void testIsWithinWindow() {
            Instant start = Instant.ofEpochSecond(100);
            Instant end = Instant.ofEpochSecond(200);
            Instant inside = Instant.ofEpochSecond(150);
            Instant outside = Instant.ofEpochSecond(250);

            assertThat(InstantTimestampExamples.isWithinWindow(inside, start, end)).isTrue();
            assertThat(InstantTimestampExamples.isWithinWindow(outside, start, end)).isFalse();
        }

        @Test
        @DisplayName("Should include boundaries in window check")
        void testIsWithinWindowBoundaries() {
            Instant start = Instant.ofEpochSecond(100);
            Instant end = Instant.ofEpochSecond(200);

            assertThat(InstantTimestampExamples.isWithinWindow(start, start, end)).isTrue();
            assertThat(InstantTimestampExamples.isWithinWindow(end, start, end)).isTrue();
        }
    }

    @Nested
    @DisplayName("Instant Truncation")
    class TruncationTests {

        @Test
        @DisplayName("Should truncate to seconds")
        void testTruncateToSeconds() {
            Instant instant = Instant.ofEpochSecond(100, 500_000_000);
            Instant truncated = InstantTimestampExamples.truncateToSeconds(instant);
            assertThat(truncated.getNano()).isEqualTo(0);
            assertThat(truncated.getEpochSecond()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("Nano Precision")
    class NanoPrecisionTests {

        @Test
        @DisplayName("Should get nano component")
        void testGetNano() {
            Instant instant = Instant.ofEpochSecond(0, 123456789);
            assertThat(InstantTimestampExamples.getNano(instant)).isEqualTo(123456789);
        }

        @Test
        @DisplayName("Should have zero nanos for whole seconds")
        void testZeroNanos() {
            Instant instant = Instant.ofEpochSecond(100);
            assertThat(InstantTimestampExamples.getNano(instant)).isEqualTo(0);
        }
    }
}
