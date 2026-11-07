package practice;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Calendar operations for meetings. A meeting is stored as a ZonedDateTime in the zone of the
 * office that hosts it.
 */
public class Invites {

    /** One line per attendee for the invite email: the attendee's zone and the meeting's start. */
    public static List<String> inviteLines(ZonedDateTime meeting, List<ZoneId> attendees) {
        List<String> lines = new ArrayList<>();
        for (ZoneId attendee : attendees) {
            lines.add(attendee + ": " + asSeenBy(meeting, attendee).toLocalDateTime());
        }
        return lines;
    }

    /** The meeting as it appears in the calendar of an attendee who lives in attendeeZone. */
    static ZonedDateTime asSeenBy(ZonedDateTime meeting, ZoneId attendeeZone) {
        return meeting.withZoneSameLocal(attendeeZone);
    }

    /** The meeting after the organiser hands it to the office in officeZone. */
    static ZonedDateTime moveToOffice(ZonedDateTime meeting, ZoneId officeZone) {
        return meeting.withZoneSameLocal(officeZone);
    }
}
