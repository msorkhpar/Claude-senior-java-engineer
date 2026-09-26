package practice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.Locale;

public final class EventTimes {

    private static final DateTimeFormatter FEED = new DateTimeFormatterBuilder()
            .appendPattern("uuuu-MM-dd['T'HH:mm:ss]")
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .toFormatter(Locale.US)
            .withResolverStyle(ResolverStyle.STRICT);

    private EventTimes() {
    }

    /** The local date and time {@code text} names; a date alone is the start of that day. */
    public static LocalDateTime parse(String text) {
        return LocalDateTime.parse(text, FEED);
    }
}
