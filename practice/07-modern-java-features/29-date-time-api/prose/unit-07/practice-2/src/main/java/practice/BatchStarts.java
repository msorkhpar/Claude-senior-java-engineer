package practice;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Turns the start time an operator types for a batch run, a local date-time in the data
 * centre's zone, into the moment the run is queued for.
 */
public class BatchStarts {

    /** The moment the job queue receives for a run the operator set for local in zone. */
    public static Instant queueAt(LocalDateTime local, ZoneId zone) {
        return startOf(local, zone).toInstant();
    }

    /** The start of the run as a ZonedDateTime in the data centre's zone. */
    static ZonedDateTime startOf(LocalDateTime local, ZoneId zone) {
        // TODO: Most local times name exactly one moment. On the nights the clocks change, a
        //       local time may never happen, or happen twice. Apply the operations team's
        //       rules for those nights (see the task) and return the start.
        throw new IllegalStateException("TODO: startOf");
    }
}
