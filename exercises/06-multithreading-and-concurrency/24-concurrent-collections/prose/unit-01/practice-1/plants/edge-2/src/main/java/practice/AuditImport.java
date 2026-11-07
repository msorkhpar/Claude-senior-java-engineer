package practice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Imports audit events collected from several collectors. A collector that times out sends its
 * batch again, so the same event can arrive more than once, each copy stamped with the time this
 * copy was received.
 */
public class AuditImport {

    /** One audit event. Two copies of the same event share its id. */
    public record Event(String id, String action, long receivedAt) {}

    /** Writes the events to the audit log, each event once. */
    public interface AuditLog {
        void write(Event event);
    }

    private final AuditLog log;

    public AuditImport(AuditLog log) {
        this.log = log;
    }

    /** Writes every event of the batch once and returns how many it wrote. */
    public int importBatch(List<Event> received) {
        List<Event> events = firstOfEach(received);
        events.forEach(log::write);
        return events.size();
    }

    /**
     * The events of the batch with each event once, as the first copy that arrived, in the order
     * the events first arrived. The input is not changed.
     */
    List<Event> firstOfEach(List<Event> received) {
        Map<String, Event> first = new LinkedHashMap<>();
        for (Event e : received) first.put(e.id(), e);
        return new ArrayList<>(first.values());
    }
}
