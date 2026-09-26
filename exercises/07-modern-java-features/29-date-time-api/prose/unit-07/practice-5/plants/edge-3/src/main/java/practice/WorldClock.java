package practice;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class WorldClock {

    

    private final Clock clock;

    public WorldClock(Clock clock) {
        this.clock = clock;
    }

    /** One line per zone, all for the same moment. */
    public List<String> show(List<ZoneId> zones) {
        Instant now = clock.instant();
        List<String> lines = new ArrayList<>();
        for (ZoneId zone : zones) {
            lines.add(zone.getId() + " " + now.atZone(zone).format(DateTimeFormatter.ofPattern("EEE HH:mm z")));
        }
        return lines;
    }
}
