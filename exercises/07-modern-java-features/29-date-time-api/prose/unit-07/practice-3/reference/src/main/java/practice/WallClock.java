package practice;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

public final class WallClock {

    private WallClock() {
    }

    /** The job start at {@code local} in {@code zone}: a skipped time is refused, a repeated one taken the second time. */
    public static ZonedDateTime at(LocalDateTime local, ZoneId zone) {
        List<ZoneOffset> valid = zone.getRules().getValidOffsets(local);
        if (valid.isEmpty()) {
            throw new IllegalArgumentException(local + " does not happen in " + zone);
        }
        // One offset normally; in an overlap the second entry is the offset after the clocks went back.
        return ZonedDateTime.ofLocal(local, zone, valid.get(valid.size() - 1));
    }
}
