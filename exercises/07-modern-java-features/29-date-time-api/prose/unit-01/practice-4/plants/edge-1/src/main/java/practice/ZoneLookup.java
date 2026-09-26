package practice;

import java.time.ZoneId;
import java.util.TimeZone;

public final class ZoneLookup {

    private ZoneLookup() {
    }

    public static ZoneId zone(String id) {
        // the legacy lookup: an unknown ID silently becomes GMT
        return TimeZone.getTimeZone(id).toZoneId();
    }
}
