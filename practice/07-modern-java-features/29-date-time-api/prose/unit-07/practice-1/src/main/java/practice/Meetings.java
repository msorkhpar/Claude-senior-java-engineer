package practice;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class Meetings {

    private Meetings() {
    }

    /** The meeting as an attendee in {@code attendee} sees it: the same moment on their clock. */
    public static ZonedDateTime forAttendee(ZonedDateTime meeting, ZoneId attendee) {
        throw new UnsupportedOperationException("write forAttendee");
    }

    /** The meeting moved to {@code newZone} at the same wall-clock time. */
    public static ZonedDateTime reschedule(ZonedDateTime meeting, ZoneId newZone) {
        throw new UnsupportedOperationException("write reschedule");
    }
}
