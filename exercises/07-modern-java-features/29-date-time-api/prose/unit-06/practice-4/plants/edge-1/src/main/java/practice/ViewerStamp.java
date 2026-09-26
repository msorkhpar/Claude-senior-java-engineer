package practice;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class ViewerStamp {

    private static final DateTimeFormatter SHOWN = DateTimeFormatter.ofPattern("EEE, MMM d, uuuu h:mm a z", Locale.US);

    private ViewerStamp() {
    }

    /** {@code instant} as a viewer in {@code viewer} sees it on their own clock. */
    public static String format(Instant instant, ZoneId viewer) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC).atZone(viewer).format(SHOWN);
    }
}
