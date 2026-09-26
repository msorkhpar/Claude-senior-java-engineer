package practice;

import java.time.ZonedDateTime;

public final class Reminders {

    private Reminders() {
    }

    /** The same wall-clock time on the next calendar day. */
    public static ZonedDateTime sameTimeTomorrow(ZonedDateTime t) {
        throw new UnsupportedOperationException("write sameTimeTomorrow");
    }

    /** The moment exactly 24 hours after {@code t}. */
    public static ZonedDateTime exactlyADayLater(ZonedDateTime t) {
        throw new UnsupportedOperationException("write exactlyADayLater");
    }

    /** The whole hours that elapse from {@code a} to {@code b}. */
    public static long hoursBetween(ZonedDateTime a, ZonedDateTime b) {
        throw new UnsupportedOperationException("write hoursBetween");
    }
}
