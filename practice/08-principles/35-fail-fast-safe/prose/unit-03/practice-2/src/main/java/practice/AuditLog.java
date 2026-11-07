package practice;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * The day's audit trail. Every request thread appends to it; the compliance export walks it and
 * writes each event to the archive, while requests keep coming.
 */
public class AuditLog {

    /** One audited action. Sequence numbers start at 1 and follow the order of appends. */
    public record Event(long seq, String user, String action) {}

    private final AtomicLong lastSeq = new AtomicLong();
    private final Collection<Event> events = newStore();

    /** Records an action and returns its event. Called by every request thread. */
    public Event append(String user, String action) {
        Event e = new Event(lastSeq.incrementAndGet(), user, action);
        store(e);
        return e;
    }

    /** The collection the events are kept in, in the order they are stored. */
    Collection<Event> newStore() {
        // TODO: choose the collection. Every request thread appends; the export walks it meanwhile.
        throw new IllegalStateException("TODO: newStore");
    }

    /** Keeps one event. */
    void store(Event e) {
        // TODO: keep the event.
        throw new IllegalStateException("TODO: store");
    }

    /** Hands the events to the archive in order and returns how many it handed over. */
    public int export(Consumer<Event> archive) {
        // TODO: hand the events to the archive in order, while requests go on appending.
        throw new IllegalStateException("TODO: export");
    }
}
