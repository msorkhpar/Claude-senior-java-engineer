package com.github.msorkhpar.claudejavatutor.datetimeapi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Demonstrates limitations of the legacy java.util.Date and java.util.Calendar classes.
 * These issues motivated the introduction of java.time in Java 8.
 */
public class LegacyDateLimitations {

    /**
     * Demonstrates that java.util.Date is mutable - dates can be changed after creation.
     */
    public static Date createAndMutateDate() {
        Date date = new Date(2024 - 1900, Calendar.JANUARY, 1); // Year offset from 1900
        date.setYear(2025 - 1900); // Mutated!
        return date;
    }

    /**
     * Demonstrates the confusing year offset (years since 1900) in java.util.Date.
     */
    @SuppressWarnings("deprecation")
    public static int getYearFromLegacyDate(Date date) {
        return date.getYear() + 1900; // Must add 1900 to get the actual year
    }

    /**
     * Demonstrates the confusing zero-based month indexing in Calendar.
     * January = 0, February = 1, ..., December = 11
     */
    public static int getMonthFromCalendar(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day); // Must subtract 1 for correct month
        return calendar.get(Calendar.MONTH); // Returns 0-based month
    }

    /**
     * Demonstrates that SimpleDateFormat is NOT thread-safe.
     * Multiple threads sharing a SimpleDateFormat instance can produce incorrect results.
     */
    public static String formatDateUnsafe(Date date, SimpleDateFormat sharedFormatter) {
        return sharedFormatter.format(date);
    }

    /**
     * Demonstrates Calendar's lenient mode allowing invalid dates silently.
     */
    public static Date createLenientDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setLenient(true); // default behavior
        calendar.set(year, month - 1, day);
        return calendar.getTime();
    }

    /**
     * Demonstrates strict mode that can reject invalid dates.
     */
    public static Date createStrictDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setLenient(false);
        calendar.set(year, month - 1, day);
        return calendar.getTime(); // May throw if invalid
    }

    /**
     * Demonstrates that java.util.Date actually represents a timestamp (date+time),
     * not just a date. There is no clean way to represent just a date.
     */
    public static boolean dateIncludesTime() {
        Date date = new Date();
        return date.getHours() >= 0; // Date carries time information
    }

    /**
     * Demonstrates that java.sql.Date extends java.util.Date but has different semantics,
     * leading to confusion in the API.
     */
    public static java.sql.Date convertToSqlDate(Date utilDate) {
        return new java.sql.Date(utilDate.getTime());
    }

    /**
     * Demonstrates that Date.toString() uses the system default timezone,
     * making the output unpredictable across environments.
     */
    public static String formatWithDefaultTimezone(Date date) {
        return date.toString(); // Output depends on JVM timezone
    }

    /**
     * Demonstrates parsing fragility with SimpleDateFormat.
     */
    public static Date parseDateString(String dateStr, String pattern) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setLenient(false);
        return sdf.parse(dateStr);
    }

    /**
     * Demonstrates that Calendar silently overflows months/days in lenient mode.
     */
    public static Date overflowingCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.setLenient(true);
        cal.set(2024, 12, 1); // Month 12 = January of next year (0-indexed)
        return cal.getTime();
    }
}
