package com.github.msorkhpar.claudejavatutor.datetimeapi;

import java.time.*;
import java.time.temporal.ChronoUnit;

/**
 * Demonstrates Instant and timestamp-related operations from java.time.
 * Instant represents a single instantaneous point on the timeline (UTC).
 */
public class InstantTimestampExamples {

    /**
     * Creates an Instant from epoch seconds.
     */
    public static Instant fromEpochSeconds(long epochSeconds) {
        return Instant.ofEpochSecond(epochSeconds);
    }

    /**
     * Creates an Instant from epoch milliseconds.
     */
    public static Instant fromEpochMillis(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis);
    }

    /**
     * Gets the epoch second from an Instant.
     */
    public static long toEpochSeconds(Instant instant) {
        return instant.getEpochSecond();
    }

    /**
     * Gets the epoch millisecond from an Instant.
     */
    public static long toEpochMillis(Instant instant) {
        return instant.toEpochMilli();
    }

    /**
     * Calculates the duration between two Instants in milliseconds.
     */
    public static long millisBetween(Instant start, Instant end) {
        return Duration.between(start, end).toMillis();
    }

    /**
     * Checks if an Instant is before another.
     */
    public static boolean isBefore(Instant first, Instant second) {
        return first.isBefore(second);
    }

    /**
     * Adds a specified duration to an Instant.
     */
    public static Instant addDuration(Instant instant, Duration duration) {
        return instant.plus(duration);
    }

    /**
     * Converts an Instant to a LocalDateTime in the specified timezone.
     */
    public static LocalDateTime toLocalDateTime(Instant instant, ZoneId zoneId) {
        return LocalDateTime.ofInstant(instant, zoneId);
    }

    /**
     * Converts a LocalDateTime in a timezone to an Instant.
     */
    public static Instant fromLocalDateTime(LocalDateTime localDateTime, ZoneId zoneId) {
        return localDateTime.atZone(zoneId).toInstant();
    }

    /**
     * Truncates an Instant to seconds (removing nanoseconds).
     */
    public static Instant truncateToSeconds(Instant instant) {
        return instant.truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * Checks if an Instant falls within a given time window.
     */
    public static boolean isWithinWindow(Instant instant, Instant windowStart, Instant windowEnd) {
        return !instant.isBefore(windowStart) && !instant.isAfter(windowEnd);
    }

    /**
     * Calculates the number of seconds between two Instants.
     */
    public static long secondsBetween(Instant start, Instant end) {
        return ChronoUnit.SECONDS.between(start, end);
    }

    /**
     * Creates an Instant that is a specific number of hours from now.
     */
    public static Instant hoursFromNow(int hours) {
        return Instant.now().plus(hours, ChronoUnit.HOURS);
    }

    /**
     * Demonstrates nanosecond precision of Instant.
     */
    public static Instant fromEpochSecondAndNanos(long epochSecond, long nanoAdjustment) {
        return Instant.ofEpochSecond(epochSecond, nanoAdjustment);
    }

    /**
     * Gets the nano-of-second component of an Instant.
     */
    public static int getNano(Instant instant) {
        return instant.getNano();
    }
}
