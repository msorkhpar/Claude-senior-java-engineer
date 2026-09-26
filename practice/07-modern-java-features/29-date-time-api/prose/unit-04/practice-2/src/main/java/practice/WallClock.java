package practice;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class WallClock {

    private WallClock() {
    }

    /** What a wall clock in the zone shows at the instant. */
    public static LocalDateTime localAt(Instant instant, ZoneId zone) {
        throw new UnsupportedOperationException("write localAt");
    }

    /** The instant at which a wall clock in the zone shows the local date-time. */
    public static Instant instantOf(LocalDateTime local, ZoneId zone) {
        throw new UnsupportedOperationException("write instantOf");
    }
}
