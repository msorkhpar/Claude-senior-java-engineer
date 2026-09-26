package practice;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public final class AuditLog {

    /** One recorded action and the instant it was recorded at. */
    public record Entry(Instant at, String action) {
    }

    public AuditLog(Clock clock) {
    }

    /** Records the action at the clock's current instant. */
    public Entry record(String action) {
        throw new UnsupportedOperationException("write record");
    }

    /** The actions recorded no earlier than window before the clock's current instant, oldest first. */
    public List<String> actionsWithin(Duration window) {
        throw new UnsupportedOperationException("write actionsWithin");
    }
}
