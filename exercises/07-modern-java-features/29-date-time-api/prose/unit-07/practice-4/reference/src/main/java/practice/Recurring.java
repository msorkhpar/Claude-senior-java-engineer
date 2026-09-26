package practice;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public final class Recurring {

    private Recurring() {
    }

    /** The first {@code count} weekly occurrences from {@code first}, as offset date-times for the API. */
    public static List<OffsetDateTime> occurrences(ZonedDateTime first, int count) {
        List<OffsetDateTime> out = new ArrayList<>();
        for (int k = 0; k < count; k++) {
            // Week arithmetic on the ZonedDateTime, so each week gets the zone's offset for that date.
            out.add(first.plusWeeks(k).toOffsetDateTime());
        }
        return out;
    }
}
