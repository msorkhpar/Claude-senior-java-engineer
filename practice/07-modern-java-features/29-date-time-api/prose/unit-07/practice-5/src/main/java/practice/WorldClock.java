package practice;

import java.time.Clock;
import java.time.ZoneId;
import java.util.List;

public final class WorldClock {

    private final Clock clock;

    public WorldClock(Clock clock) {
        this.clock = clock;
    }

    /** One line per zone, all for the same moment. */
    public List<String> show(List<ZoneId> zones) {
        throw new UnsupportedOperationException("write show");
    }
}
