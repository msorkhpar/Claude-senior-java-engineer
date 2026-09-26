package practice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class LogStamp {

    private static final DateTimeFormatter LOG = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss", Locale.US);

    private LogStamp() {
    }

    /** Prints {@code t} as a log timestamp such as 2024-03-15 14:30:45. */
    public static String format(LocalDateTime t) {
        return t.format(LOG);
    }
}
