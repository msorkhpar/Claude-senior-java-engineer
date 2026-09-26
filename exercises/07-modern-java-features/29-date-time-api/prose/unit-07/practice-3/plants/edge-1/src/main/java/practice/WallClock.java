package practice;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class WallClock {

    private WallClock() {
    }

    /** The job start at {@code local} in {@code zone}: a skipped time is refused, a repeated one taken the second time. */
    public static ZonedDateTime at(LocalDateTime local, ZoneId zone) {
        return ZonedDateTime.of(local, zone).withLaterOffsetAtOverlap();
    }
}
