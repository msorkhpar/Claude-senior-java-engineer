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
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /** The Date for the moment {@code local} names on {@code zone}'s clock. */
    public static Date toDate(LocalDateTime local, ZoneId zone) {
        return Date.from(local.atZone(ZoneId.systemDefault()).toInstant());
    }
}
