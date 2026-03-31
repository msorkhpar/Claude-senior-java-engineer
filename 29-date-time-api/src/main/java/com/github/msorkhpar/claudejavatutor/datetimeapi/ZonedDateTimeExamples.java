package com.github.msorkhpar.claudejavatutor.datetimeapi;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Set;

/**
 * Demonstrates ZonedDateTime and OffsetDateTime from java.time package.
 * Covers timezone handling, daylight saving time, and zone conversions.
 */
public class ZonedDateTimeExamples {

    /**
     * Creates a ZonedDateTime from date, time, and zone.
     */
    public static ZonedDateTime createZonedDateTime(int year, int month, int day,
                                                     int hour, int minute, int second,
                                                     String zoneId) {
        return ZonedDateTime.of(year, month, day, hour, minute, second, 0, ZoneId.of(zoneId));
    }

    /**
     * Converts a ZonedDateTime from one timezone to another.
     */
    public static ZonedDateTime convertTimezone(ZonedDateTime dateTime, String targetZoneId) {
        return dateTime.withZoneSameInstant(ZoneId.of(targetZoneId));
    }

    /**
     * Gets the offset from UTC for a ZonedDateTime.
     */
    public static ZoneOffset getOffset(ZonedDateTime dateTime) {
        return dateTime.getOffset();
    }

    /**
     * Creates an OffsetDateTime from date, time, and offset.
     */
    public static OffsetDateTime createOffsetDateTime(int year, int month, int day,
                                                       int hour, int minute, int second,
                                                       int offsetHours) {
        ZoneOffset offset = ZoneOffset.ofHours(offsetHours);
        return OffsetDateTime.of(year, month, day, hour, minute, second, 0, offset);
    }

    /**
     * Converts a ZonedDateTime to an OffsetDateTime.
     */
    public static OffsetDateTime toOffsetDateTime(ZonedDateTime zonedDateTime) {
        return zonedDateTime.toOffsetDateTime();
    }

    /**
     * Checks if two ZonedDateTimes represent the same instant in time.
     */
    public static boolean isSameInstant(ZonedDateTime first, ZonedDateTime second) {
        return first.toInstant().equals(second.toInstant());
    }

    /**
     * Gets all available timezone IDs.
     */
    public static Set<String> getAvailableZoneIds() {
        return ZoneId.getAvailableZoneIds();
    }

    /**
     * Calculates the offset difference in total seconds between two timezones at a given instant.
     */
    public static int offsetDifferenceInSeconds(Instant instant, String zone1, String zone2) {
        ZoneOffset offset1 = instant.atZone(ZoneId.of(zone1)).getOffset();
        ZoneOffset offset2 = instant.atZone(ZoneId.of(zone2)).getOffset();
        return offset1.getTotalSeconds() - offset2.getTotalSeconds();
    }

    /**
     * Demonstrates DST handling: creates a ZonedDateTime during a DST gap.
     * In spring, clocks move forward (e.g., 2:00 AM becomes 3:00 AM).
     * The API adjusts automatically.
     */
    public static ZonedDateTime createDuringDstGap(int year, int month, int day,
                                                    int hour, int minute,
                                                    String zoneId) {
        return ZonedDateTime.of(
                LocalDateTime.of(year, month, day, hour, minute),
                ZoneId.of(zoneId)
        );
    }

    /**
     * Converts a LocalDateTime to a ZonedDateTime in a specific timezone.
     */
    public static ZonedDateTime localToZoned(LocalDateTime localDateTime, String zoneId) {
        return localDateTime.atZone(ZoneId.of(zoneId));
    }

    /**
     * Extracts the LocalDateTime from a ZonedDateTime (drops timezone info).
     */
    public static LocalDateTime zonedToLocal(ZonedDateTime zonedDateTime) {
        return zonedDateTime.toLocalDateTime();
    }

    /**
     * Converts a ZonedDateTime to an Instant (UTC timestamp).
     */
    public static Instant zonedToInstant(ZonedDateTime zonedDateTime) {
        return zonedDateTime.toInstant();
    }

    /**
     * Creates a ZonedDateTime at a specific zone, keeping the same local time
     * (not the same instant).
     */
    public static ZonedDateTime withSameLocal(ZonedDateTime dateTime, String targetZoneId) {
        return dateTime.withZoneSameLocal(ZoneId.of(targetZoneId));
    }
}
