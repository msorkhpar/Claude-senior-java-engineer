package com.github.msorkhpar.claudejavatutor.datetimeapi;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * Demonstrates DateTimeFormatter from java.time.format package.
 * Covers formatting, parsing, and custom patterns.
 */
public class DateTimeFormatterExamples {

    /**
     * Formats a LocalDate using ISO format (yyyy-MM-dd).
     */
    public static String formatIsoDate(LocalDate date) {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Formats a LocalDateTime using ISO format.
     */
    public static String formatIsoDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Formats a date with a custom pattern.
     */
    public static String formatWithPattern(LocalDate date, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.format(formatter);
    }

    /**
     * Formats a datetime with a custom pattern.
     */
    public static String formatDateTimeWithPattern(LocalDateTime dateTime, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }

    /**
     * Formats a date using a localized style.
     */
    public static String formatLocalized(LocalDate date, FormatStyle style, Locale locale) {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(style).withLocale(locale);
        return date.format(formatter);
    }

    /**
     * Parses a date string using ISO format.
     */
    public static LocalDate parseIsoDate(String dateStr) {
        return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Parses a date string using a custom pattern.
     */
    public static LocalDate parseDateWithPattern(String dateStr, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDate.parse(dateStr, formatter);
    }

    /**
     * Parses a datetime string using a custom pattern.
     */
    public static LocalDateTime parseDateTimeWithPattern(String dateTimeStr, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(dateTimeStr, formatter);
    }

    /**
     * Safely parses a date string, returning null if invalid.
     */
    public static LocalDate safeParse(String dateStr, String pattern) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return LocalDate.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Formats a ZonedDateTime showing the timezone.
     */
    public static String formatZonedDateTime(ZonedDateTime zonedDateTime, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return zonedDateTime.format(formatter);
    }

    /**
     * Demonstrates thread-safety of DateTimeFormatter by reusing a static instance.
     * Unlike SimpleDateFormat, DateTimeFormatter is immutable and thread-safe.
     */
    private static final DateTimeFormatter THREAD_SAFE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String formatWithThreadSafeFormatter(LocalDateTime dateTime) {
        return dateTime.format(THREAD_SAFE_FORMATTER);
    }

    /**
     * Creates a formatter with a specific locale.
     */
    public static String formatWithLocale(LocalDate date, String pattern, Locale locale) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, locale);
        return date.format(formatter);
    }

    /**
     * Formats an Instant as a readable string in a given timezone.
     */
    public static String formatInstant(Instant instant, ZoneId zoneId, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withZone(zoneId);
        return formatter.format(instant);
    }
}
