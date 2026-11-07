package practice;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * One parcel's tracking timeline, merged from three feeds: the line-haul carrier, the courier,
 * and the scanners of the sorting depot.
 */
public class ParcelTimeline {

    /** One tracking event and the moment it happened. */
    public record Event(String what, Instant at) {}

    private final ZoneId depotZone;
    private final List<Event> events = new ArrayList<>();

    /** depotZone is where the sorting depot is; its scanners' clocks show local time. */
    public ParcelTimeline(ZoneId depotZone) {
        this.depotZone = depotZone;
    }

    /** Line-haul carrier, JSON {"event": "...", "sentAt": 1789000000}: whole seconds since the epoch. */
    public void addLineHaul(String what, long sentAt) {
        events.add(new Event(what, fromSentAt(sentAt)));
    }

    /** Courier, ISO-8601 text with Z or an offset, such as "2026-11-20T09:15:00+05:30". */
    public void addCourier(String what, String time) {
        events.add(new Event(what, Instant.parse(time)));
    }

    /** Depot scanner: the scanner's clock reading, such as "2026-11-20T16:45:00". */
    public void addDepotScan(String what, String clockReading) {
        events.add(new Event(what, fromScanner(clockReading)));
    }

    /** The events in the order they happened. */
    public List<Event> events() {
        return events.stream().sorted(Comparator.comparing(Event::at)).toList();
    }

    /** The moment the line-haul carrier's sentAt value names. */
    Instant fromSentAt(long sentAt) {
        return Instant.ofEpochMilli(sentAt);
    }

    /** The moment at which a depot scanner showed this clock reading. */
    Instant fromScanner(String clockReading) {
        return LocalDateTime.parse(clockReading).atZone(depotZone).toInstant();
    }
}
