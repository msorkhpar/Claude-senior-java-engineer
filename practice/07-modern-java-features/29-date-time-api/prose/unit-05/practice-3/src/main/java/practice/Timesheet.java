package practice;

import java.time.LocalTime;
import java.util.List;

public final class Timesheet {

    private Timesheet() {
    }

    /** One shift, from {@code start} to {@code end}. */
    public record Shift(LocalTime start, LocalTime end) {
    }

    /** The total time worked, as hours, a colon and two-digit minutes. */
    public static String total(List<Shift> shifts) {
        throw new UnsupportedOperationException("write total");
    }
}
