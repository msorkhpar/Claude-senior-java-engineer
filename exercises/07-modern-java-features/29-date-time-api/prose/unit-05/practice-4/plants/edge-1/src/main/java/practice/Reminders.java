package practice;

import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public final class Reminders {

    private Reminders() {
    }

    /** The same wall-clock time on the next calendar day. */
    public static ZonedDateTime sameTimeTomorrow(ZonedDateTime t) {
        return t.plus(Duration.ofDays(1));
    }

    /** The moment exactly 24 hours after {@code t}. */
    public static ZonedDateTime exactlyADayLater(ZonedDateTime t) {
        return t.plus(Duration.ofDays(1));
    }

    /** The whole hours that elapse from {@code a} to {@code b}. */
    public static long hoursBetween(ZonedDateTime a, ZonedDateTime b) {
        return ChronoUnit.HOURS.between(a, b);
    }
}
