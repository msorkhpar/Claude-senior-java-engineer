package practice;

import java.time.DateTimeException;
import java.time.ZoneId;

public final class ZoneLookup {

    private ZoneLookup() {
    }

    /** Returns the zone with this region ID; unknown IDs and abbreviations are an IllegalArgumentException. */
    public static ZoneId zone(String id) {
        try {
            // ZoneId.of fails fast on an unknown ID, and knows no three-letter abbreviations
            return ZoneId.of(id);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("unknown time zone: " + id, e);
        }
    }
}
