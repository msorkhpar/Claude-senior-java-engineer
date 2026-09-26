package practice;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Events {

    private Events() {
    }

    /** The first event at each moment on the timeline, in input order. */
    public static List<ZonedDateTime> distinctMoments(List<ZonedDateTime> events) {
        Set<Instant> seen = new HashSet<>();
        List<ZonedDateTime> kept = new ArrayList<>();
        for (ZonedDateTime event : events) {
            if (seen.add(event.toInstant())) {
                kept.add(event);
            }
        }
        return kept;
    }
}
