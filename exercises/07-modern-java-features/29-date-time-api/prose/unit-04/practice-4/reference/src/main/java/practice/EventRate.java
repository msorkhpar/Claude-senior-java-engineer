package practice;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public final class EventRate {

    private EventRate() {
    }

    /** How many events happened in each whole second, in time order. */
    public static SortedMap<Instant, Integer> perSecond(List<Instant> events) {
        SortedMap<Instant, Integer> counts = new TreeMap<>();
        for (Instant event : events) {
            counts.merge(event.truncatedTo(ChronoUnit.SECONDS), 1, Integer::sum);
        }
        return counts;
    }
}
