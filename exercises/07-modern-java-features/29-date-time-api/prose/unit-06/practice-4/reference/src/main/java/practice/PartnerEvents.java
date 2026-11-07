package practice;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Takes in delivery events from partner depots and shows them to the support agent who opens the
 * parcel's page. Events are stored as instants.
 */
public class PartnerEvents {

    private static final DateTimeFormatter AGENT_VIEW = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm", Locale.ROOT);

    /** One line of the parcel's history as the agent sees it. */
    public static String historyLine(String eventTime, String what, ZoneId agentZone) {
        return forAgent(receivedAt(eventTime), agentZone) + "  " + what;
    }

    /** The moment a depot's event time names. */
    static Instant receivedAt(String eventTime) {
        return OffsetDateTime.parse(eventTime).toInstant();
    }

    /** The moment as the agent's own clock showed it, to the minute. */
    static String forAgent(Instant at, ZoneId agentZone) {
        return AGENT_VIEW.withZone(agentZone).format(at);
    }
}
