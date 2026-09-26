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
        ZonedDateTime next = first;
        for (int k = 0; k < count; k++) {
            out.add(next.toOffsetDateTime());
            next = next.plusWeeks(1);
        }
        return out;
    }
}
