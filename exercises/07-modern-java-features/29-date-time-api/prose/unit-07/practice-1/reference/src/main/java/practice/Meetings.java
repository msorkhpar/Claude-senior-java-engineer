package practice;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class Meetings {

    private Meetings() {
    }

    /** The meeting as an attendee in {@code attendee} sees it: the same moment on their clock. */
    public static ZonedDateTime forAttendee(ZonedDateTime meeting, ZoneId attendee) {
        return meeting.withZoneSameInstant(attendee);
    }

    /** The meeting moved to {@code newZone} at the same wall-clock time. */
    public static ZonedDateTime reschedule(ZonedDateTime meeting, ZoneId newZone) {
        return meeting.withZoneSameLocal(newZone);
    }
}
