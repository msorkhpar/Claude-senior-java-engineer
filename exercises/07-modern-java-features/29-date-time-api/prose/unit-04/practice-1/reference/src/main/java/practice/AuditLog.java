package practice;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class AuditLog {

    /** One recorded action and the instant it was recorded at. */
    public record Entry(Instant at, String action) {
    }

    private final Clock clock;
    private final List<Entry> entries = new ArrayList<>();

    public AuditLog(Clock clock) {
        this.clock = clock;
    }

    /** Records the action at the clock's current instant. */
    public Entry record(String action) {
        Entry entry = new Entry(Instant.now(clock), action);
        entries.add(entry);
        return entry;
    }

    /** The actions recorded no earlier than window before the clock's current instant, oldest first. */
    public List<String> actionsWithin(Duration window) {
        Instant cutoff = Instant.now(clock).minus(window);
        return entries.stream()
                .filter(entry -> !entry.at().isBefore(cutoff))
                .map(Entry::action)
                .toList();
    }
}
