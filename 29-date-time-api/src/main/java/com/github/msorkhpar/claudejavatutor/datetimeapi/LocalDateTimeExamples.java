package com.github.msorkhpar.claudejavatutor.datetimeapi;

import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Demonstrates LocalDate, LocalTime, and LocalDateTime from java.time package.
 * These classes represent date/time without timezone information.
 */
public class LocalDateTimeExamples {

    // --- LocalDate ---

    /**
     * Creates a LocalDate from year, month, and day components.
     */
    public static LocalDate createDate(int year, int month, int day) {
        return LocalDate.of(year, month, day);
    }

    /**
     * Calculates age in years from a birth date to a reference date.
     */
    public static int calculateAge(LocalDate birthDate, LocalDate referenceDate) {
        if (birthDate == null || referenceDate == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
        if (birthDate.isAfter(referenceDate)) {
            throw new IllegalArgumentException("Birth date must be before reference date");
        }
        return Period.between(birthDate, referenceDate).getYears();
    }

    /**
     * Gets all dates between two dates (inclusive of start, exclusive of end).
     */
    public static List<LocalDate> getDateRange(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start must be before or equal to end");
        }
        return start.datesUntil(end).collect(Collectors.toList());
    }

    /**
     * Checks if a year is a leap year.
     */
    public static boolean isLeapYear(int year) {
        return Year.of(year).isLeap();
    }

    /**
     * Gets the last day of the month for a given date.
     */
    public static LocalDate getLastDayOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * Gets the next occurrence of a specific day of the week.
     */
    public static LocalDate getNextDayOfWeek(LocalDate from, DayOfWeek dayOfWeek) {
        return from.with(TemporalAdjusters.next(dayOfWeek));
    }

    /**
     * Calculates business days between two dates (excluding weekends).
     */
    public static long countBusinessDays(LocalDate start, LocalDate end) {
        return start.datesUntil(end)
                .filter(d -> d.getDayOfWeek() != DayOfWeek.SATURDAY
                        && d.getDayOfWeek() != DayOfWeek.SUNDAY)
                .count();
    }

    // --- LocalTime ---

    /**
     * Creates a LocalTime from hour, minute, and second components.
     */
    public static LocalTime createTime(int hour, int minute, int second) {
        return LocalTime.of(hour, minute, second);
    }

    /**
     * Calculates duration between two times in minutes.
     */
    public static long minutesBetween(LocalTime start, LocalTime end) {
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * Checks if a time falls within a range (inclusive).
     */
    public static boolean isWithinRange(LocalTime time, LocalTime start, LocalTime end) {
        if (start.isBefore(end) || start.equals(end)) {
            return !time.isBefore(start) && !time.isAfter(end);
        }
        // Handle overnight range (e.g., 22:00 to 06:00)
        return !time.isBefore(start) || !time.isAfter(end);
    }

    /**
     * Truncates a time to the nearest hour.
     */
    public static LocalTime truncateToHour(LocalTime time) {
        return time.truncatedTo(ChronoUnit.HOURS);
    }

    // --- LocalDateTime ---

    /**
     * Combines a LocalDate and LocalTime into a LocalDateTime.
     */
    public static LocalDateTime combine(LocalDate date, LocalTime time) {
        return LocalDateTime.of(date, time);
    }

    /**
     * Adds a specified number of business days to a LocalDateTime.
     */
    public static LocalDateTime addBusinessDays(LocalDateTime dateTime, int days) {
        LocalDate date = dateTime.toLocalDate();
        int added = 0;
        while (added < days) {
            date = date.plusDays(1);
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY
                    && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                added++;
            }
        }
        return LocalDateTime.of(date, dateTime.toLocalTime());
    }

    /**
     * Checks if a LocalDateTime falls on a weekend.
     */
    public static boolean isWeekend(LocalDateTime dateTime) {
        DayOfWeek day = dateTime.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    /**
     * Gets the quarter of the year for a given date.
     */
    public static int getQuarter(LocalDate date) {
        return (date.getMonthValue() - 1) / 3 + 1;
    }

    /**
     * Returns the day of year for a given date.
     */
    public static int getDayOfYear(LocalDate date) {
        return date.getDayOfYear();
    }
}
