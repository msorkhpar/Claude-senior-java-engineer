package practice;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores wiki pages. Its table keeps each page's updated_at as epoch milliseconds, and an edit is
 * accepted only if it was made from the page's current version, named by its updated_at.
 */
public class PageStore {

    /** What the client gets back: the page, and the stamp to send with its next edit. */
    public record Version(String id, String text, Instant updatedAt) {}

    /** The edit was made from a version that is no longer current. */
    public static final class EditConflict extends RuntimeException {
        public EditConflict(String id) {
            super("page " + id + " was changed by someone else");
        }
    }

    /** One row of the table: the column updated_at holds epoch milliseconds. */
    private record Row(String text, long updatedAtMillis) {}

    private final Clock clock;
    private final Map<String, Row> table = new HashMap<>();

    public PageStore(Clock clock) {
        this.clock = clock;
    }

    public synchronized Version create(String id, String text) {
        Instant now = stamp();
        table.put(id, new Row(text, now.toEpochMilli()));
        return new Version(id, text, now);
    }

    public synchronized Version read(String id) {
        Row row = table.get(id);
        return new Version(id, row.text(), Instant.ofEpochMilli(row.updatedAtMillis()));
    }

    /** Saves an edit made from the version stamped {@code editedFrom}. */
    public synchronized Version save(String id, String text, Instant editedFrom) {
        Row row = table.get(id);
        if (!isCurrent(editedFrom, row.updatedAtMillis())) {
            throw new EditConflict(id);
        }
        Instant now = stamp();
        table.put(id, new Row(text, now.toEpochMilli()));
        return new Version(id, text, now);
    }

    /** The moment to record as a page's updated_at, and to hand to the client. */
    Instant stamp() {
        return clock.instant().truncatedTo(ChronoUnit.MILLIS);
    }

    /** Whether a client's stamp names the version whose updated_at column holds this value. */
    boolean isCurrent(Instant editedFrom, long updatedAtMillis) {
        return editedFrom.equals(Instant.ofEpochMilli(updatedAtMillis));
    }
}
