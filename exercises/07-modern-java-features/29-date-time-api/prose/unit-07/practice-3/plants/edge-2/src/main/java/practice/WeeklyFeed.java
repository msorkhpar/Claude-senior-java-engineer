package practice;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The public API's feed for a weekly class. A class is set up with its first session, in the
 * zone of the gym that runs it, and meets every week at the same time on that gym's clocks.
 */
public class WeeklyFeed {

    /** The feed as the API sends it: one ISO-8601 line per session. */
    public static List<String> feed(ZonedDateTime firstSession, int count) {
        List<String> lines = new ArrayList<>();
        for (OffsetDateTime session : sessions(firstSession, count)) {
            lines.add(session.toString());
        }
        return lines;
    }

    /** The first count sessions, starting with firstSession itself. */
    static List<OffsetDateTime> sessions(ZonedDateTime firstSession, int count) {
        List<OffsetDateTime> sessions = new ArrayList<>();
        ZonedDateTime session = firstSession;
        for (int week = 0; week < count; week++) {
            sessions.add(session.toOffsetDateTime());
            session = session.plusWeeks(1);
        }
        return sessions;
    }
}
