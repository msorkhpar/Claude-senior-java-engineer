package practice;

import java.time.ZoneId;

public final class ZoneLookup {

    private ZoneLookup() {
    }

    /** Returns the zone with this region ID; unknown IDs and abbreviations are an IllegalArgumentException. */
    public static ZoneId zone(String id) {
        throw new UnsupportedOperationException("write zone");
    }
}
