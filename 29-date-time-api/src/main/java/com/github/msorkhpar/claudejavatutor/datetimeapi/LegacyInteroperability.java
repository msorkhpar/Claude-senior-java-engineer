package com.github.msorkhpar.claudejavatutor.datetimeapi;

import java.sql.Timestamp;
import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Demonstrates interoperability between legacy date/time classes and java.time.
 * Covers conversions between java.util.Date, Calendar, java.sql types, and java.time.
 */
public class LegacyInteroperability {

    // --- java.util.Date <-> Instant ---

    /**
     * Converts a java.util.Date to an Instant.
     */
    public static Instant dateToInstant(Date date) {
        return date.toInstant();
    }

    /**
     * Converts an Instant to a java.util.Date.
     */
    public static Date instantToDate(Instant instant) {
        return Date.from(instant);
    }

    // --- java.util.Date <-> LocalDate ---

    /**
     * Converts a java.util.Date to a LocalDate using a specific timezone.
     */
    public static LocalDate dateToLocalDate(Date date, ZoneId zoneId) {
        return date.toInstant().atZone(zoneId).toLocalDate();
    }

    /**
     * Converts a LocalDate to a java.util.Date at the start of day in a given timezone.
     */
    public static Date localDateToDate(LocalDate localDate, ZoneId zoneId) {
        Instant instant = localDate.atStartOfDay(zoneId).toInstant();
        return Date.from(instant);
    }

    // --- java.util.Date <-> LocalDateTime ---

    /**
     * Converts a java.util.Date to a LocalDateTime in a specific timezone.
     */
    public static LocalDateTime dateToLocalDateTime(Date date, ZoneId zoneId) {
        return date.toInstant().atZone(zoneId).toLocalDateTime();
    }

    /**
     * Converts a LocalDateTime to a java.util.Date in a specific timezone.
     */
    public static Date localDateTimeToDate(LocalDateTime localDateTime, ZoneId zoneId) {
        Instant instant = localDateTime.atZone(zoneId).toInstant();
        return Date.from(instant);
    }

    // --- Calendar <-> ZonedDateTime ---

    /**
     * Converts a Calendar to a ZonedDateTime.
     */
    public static ZonedDateTime calendarToZonedDateTime(Calendar calendar) {
        if (calendar instanceof GregorianCalendar gc) {
            return gc.toZonedDateTime();
        }
        Instant instant = calendar.toInstant();
        ZoneId zoneId = calendar.getTimeZone().toZoneId();
        return ZonedDateTime.ofInstant(instant, zoneId);
    }

    /**
     * Converts a ZonedDateTime to a GregorianCalendar.
     */
    public static GregorianCalendar zonedDateTimeToCalendar(ZonedDateTime zonedDateTime) {
        return GregorianCalendar.from(zonedDateTime);
    }

    // --- TimeZone <-> ZoneId ---

    /**
     * Converts a legacy TimeZone to a ZoneId.
     */
    public static ZoneId timeZoneToZoneId(TimeZone timeZone) {
        return timeZone.toZoneId();
    }

    /**
     * Converts a ZoneId to a legacy TimeZone.
     */
    public static TimeZone zoneIdToTimeZone(ZoneId zoneId) {
        return TimeZone.getTimeZone(zoneId);
    }

    // --- java.sql types ---

    /**
     * Converts a java.sql.Date to a LocalDate.
     */
    public static LocalDate sqlDateToLocalDate(java.sql.Date sqlDate) {
        return sqlDate.toLocalDate();
    }

    /**
     * Converts a LocalDate to a java.sql.Date.
     */
    public static java.sql.Date localDateToSqlDate(LocalDate localDate) {
        return java.sql.Date.valueOf(localDate);
    }

    /**
     * Converts a java.sql.Timestamp to a LocalDateTime.
     */
    public static LocalDateTime timestampToLocalDateTime(Timestamp timestamp) {
        return timestamp.toLocalDateTime();
    }

    /**
     * Converts a LocalDateTime to a java.sql.Timestamp.
     */
    public static Timestamp localDateTimeToTimestamp(LocalDateTime localDateTime) {
        return Timestamp.valueOf(localDateTime);
    }

    /**
     * Converts a java.sql.Timestamp to an Instant.
     */
    public static Instant timestampToInstant(Timestamp timestamp) {
        return timestamp.toInstant();
    }

    /**
     * Converts an Instant to a java.sql.Timestamp.
     */
    public static Timestamp instantToTimestamp(Instant instant) {
        return Timestamp.from(instant);
    }

    /**
     * Converts a java.sql.Time to a LocalTime.
     */
    public static LocalTime sqlTimeToLocalTime(java.sql.Time sqlTime) {
        return sqlTime.toLocalTime();
    }

    /**
     * Converts a LocalTime to a java.sql.Time.
     */
    public static java.sql.Time localTimeToSqlTime(LocalTime localTime) {
        return java.sql.Time.valueOf(localTime);
    }
}
