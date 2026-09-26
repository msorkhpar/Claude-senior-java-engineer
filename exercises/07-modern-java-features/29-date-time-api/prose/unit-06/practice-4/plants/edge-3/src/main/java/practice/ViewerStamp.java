package practice;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class ViewerStamp {

    

    private ViewerStamp() {
    }

    /** {@code instant} as a viewer in {@code viewer} sees it on their own clock. */
    public static String format(Instant instant, ZoneId viewer) {
        return DateTimeFormatter.ofPattern("EEE, MMM d, uuuu h:mm a z").withZone(viewer).format(instant); // the server's locale
    }
}
