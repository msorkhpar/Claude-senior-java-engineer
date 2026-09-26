package practice;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZonedDateTime;

public final class IsoText {

    private IsoText() {
    }

    /** Returns the java.time value that the ISO-8601 text spells. */
    public static Object read(String text) {
        if (text.startsWith("P") || text.startsWith("-P")) {
            return text.contains("T") ? Duration.parse(text) : Period.parse(text);
        }
        if (text.endsWith("]")) {
            return ZonedDateTime.parse(text);
        }
        int t = text.indexOf('T');
        if (t >= 0) {
            if (text.endsWith("Z")) {
                return Instant.parse(text);
            }
            String time = text.substring(t + 1);
            if (time.contains("+") || time.contains("-")) {
                return OffsetDateTime.parse(text);
            }
            return LocalDateTime.parse(text);
        }
        return text.contains(":") ? LocalTime.parse(text) : LocalDate.parse(text);
    }
}
