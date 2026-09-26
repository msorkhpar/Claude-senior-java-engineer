package practice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public final class LegacyDays {

    private LegacyDays() {
    }

    /** The calendar day {@code date} falls on in {@code zone}. */
    public static LocalDate dayOf(Date date, ZoneId zone) {
        throw new UnsupportedOperationException("write dayOf");
    }

    /** The Date for the moment {@code local} names on {@code zone}'s clock. */
    public static Date toDate(LocalDateTime local, ZoneId zone) {
        throw new UnsupportedOperationException("write toDate");
    }
}
