package com.github.msorkhpar.claudejavatutor.datetimeapi;

import java.time.*;
import java.time.temporal.ChronoUnit;

/**
 * Demonstrates Duration and Period from java.time package.
 * Duration measures time-based amounts (hours, minutes, seconds).
 * Period measures date-based amounts (years, months, days).
 */
public class DurationPeriodExamples {

    // --- Duration ---

    /**
     * Creates a Duration from hours, minutes, and seconds.
     */
    public static Duration createDuration(long hours, long minutes, long seconds) {
        return Duration.ofHours(hours)
                .plusMinutes(minutes)
                .plusSeconds(seconds);
    }

    /**
     * Calculates the Duration between two LocalTimes.
     */
    public static Duration durationBetweenTimes(LocalTime start, LocalTime end) {
        return Duration.between(start, end);
    }

    /**
     * Calculates the Duration between two Instants.
     */
    public static Duration durationBetweenInstants(Instant start, Instant end) {
        return Duration.between(start, end);
    }

    /**
     * Converts a Duration to total minutes.
     */
    public static long toTotalMinutes(Duration duration) {
        return duration.toMinutes();
    }

    /**
     * Converts a Duration to total seconds.
     */
    public static long toTotalSeconds(Duration duration) {
        return duration.toSeconds();
    }

    /**
     * Checks if a Duration is negative.
     */
    public static boolean isNegative(Duration duration) {
        return duration.isNegative();
    }

    /**
     * Returns the absolute value of a Duration.
     */
    public static Duration absoluteDuration(Duration duration) {
        return duration.abs();
    }

    /**
     * Multiplies a Duration by a scalar.
     */
    public static Duration multiply(Duration duration, long scalar) {
        return duration.multipliedBy(scalar);
    }

    /**
     * Parses a Duration from an ISO-8601 string (e.g., "PT2H30M").
     */
    public static Duration parseDuration(String isoString) {
        return Duration.parse(isoString);
    }

    // --- Period ---

    /**
     * Creates a Period from years, months, and days.
     */
    public static Period createPeriod(int years, int months, int days) {
        return Period.of(years, months, days);
    }

    /**
     * Calculates the Period between two LocalDates.
     */
    public static Period periodBetween(LocalDate start, LocalDate end) {
        return Period.between(start, end);
    }

    /**
     * Gets the years component of a Period.
     */
    public static int getYears(Period period) {
        return period.getYears();
    }

    /**
     * Gets the months component of a Period.
     */
    public static int getMonths(Period period) {
        return period.getMonths();
    }

    /**
     * Gets the days component of a Period.
     */
    public static int getDays(Period period) {
        return period.getDays();
    }

    /**
     * Calculates total months from a Period (years * 12 + months).
     */
    public static long toTotalMonths(Period period) {
        return period.toTotalMonths();
    }

    /**
     * Checks if a Period is zero.
     */
    public static boolean isZero(Period period) {
        return period.isZero();
    }

    /**
     * Checks if a Period is negative (any component is negative).
     */
    public static boolean isNegativePeriod(Period period) {
        return period.isNegative();
    }

    /**
     * Adds a Period to a LocalDate.
     */
    public static LocalDate addPeriod(LocalDate date, Period period) {
        return date.plus(period);
    }

    /**
     * Normalizes a Period so that months are within 0-11 range.
     */
    public static Period normalizePeriod(Period period) {
        return period.normalized();
    }

    /**
     * Parses a Period from an ISO-8601 string (e.g., "P1Y2M3D").
     */
    public static Period parsePeriod(String isoString) {
        return Period.parse(isoString);
    }

    /**
     * Demonstrates the key difference between Duration and Period:
     * Duration is exact (seconds-based), Period is calendar-based.
     */
    public static long daysBetweenDates(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }
}
