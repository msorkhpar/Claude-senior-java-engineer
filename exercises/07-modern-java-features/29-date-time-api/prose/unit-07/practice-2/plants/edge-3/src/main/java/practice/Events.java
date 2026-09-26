package practice;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class Events {

    private Events() {
    }

    /** The first event at each moment on the timeline, in input order. */
    public static List<ZonedDateTime> distinctMoments(List<ZonedDateTime> events) {
        Map<Instant, ZonedDateTime> byMoment = new TreeMap<>();
        for (ZonedDateTime event : events) {
            byMoment.putIfAbsent(event.toInstant(), event);
        }
        return new ArrayList<>(byMoment.values());
    }
}
