package practice;

import java.time.DateTimeException;
import java.time.ZoneId;

public final class ZoneLookup {

    private ZoneLookup() {
    }

    public static ZoneId zone(String id) {
        try {
            // the short-ID map turns abbreviations such as CST into a guessed region
            return ZoneId.of(id, ZoneId.SHORT_IDS);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("unknown time zone: " + id, e);
        }
    }
}
