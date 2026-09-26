package practice;

import java.time.Instant;
import java.time.ZoneId;

public final class ViewerStamp {

    private ViewerStamp() {
    }

    /** {@code instant} as a viewer in {@code viewer} sees it on their own clock. */
    public static String format(Instant instant, ZoneId viewer) {
        throw new UnsupportedOperationException("write format");
    }
}
