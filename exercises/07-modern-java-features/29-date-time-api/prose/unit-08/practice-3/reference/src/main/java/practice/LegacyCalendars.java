package practice;

import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

public final class LegacyCalendars {

    private LegacyCalendars() {
    }

    /** The same moment as a ZonedDateTime in the calendar's own time zone. */
    public static ZonedDateTime toZoned(Calendar calendar) {
        if (calendar instanceof GregorianCalendar gregorian) {
            return gregorian.toZonedDateTime();
        }
        // Any other Calendar: its instant, read in its own time zone.
        return calendar.toInstant().atZone(calendar.getTimeZone().toZoneId());
    }

    /** A new Calendar one calendar day later at the same wall-clock time; the argument is not changed. */
    public static Calendar plusOneDay(Calendar calendar) {
        return GregorianCalendar.from(toZoned(calendar).plusDays(1));
    }
}
