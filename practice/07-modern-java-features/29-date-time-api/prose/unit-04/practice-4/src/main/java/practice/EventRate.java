package practice;

import java.time.Instant;
import java.util.List;
import java.util.SortedMap;

public final class EventRate {

    private EventRate() {
    }

    /** How many events happened in each whole second, in time order. */
    public static SortedMap<Instant, Integer> perSecond(List<Instant> events) {
        throw new UnsupportedOperationException("write perSecond");
    }
}
