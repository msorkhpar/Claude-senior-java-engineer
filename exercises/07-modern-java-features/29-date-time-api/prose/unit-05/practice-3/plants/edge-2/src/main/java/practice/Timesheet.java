package practice;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

public final class Timesheet {

    private Timesheet() {
    }

    /** One shift, from {@code start} to {@code end}. */
    public record Shift(LocalTime start, LocalTime end) {
    }

    /** The total time worked, as hours, a colon and two-digit minutes. */
    public static String total(List<Shift> shifts) {
        Duration total = Duration.ZERO;
        for (Shift shift : shifts) {
            Duration worked = Duration.between(shift.start(), shift.end());
            if (worked.isNegative()) {
                worked = worked.plusDays(1); // the shift ran past midnight
            }
            total = total.plus(worked);
        }
        return String.format(Locale.ROOT, "%d:%02d", total.toHoursPart(), total.toMinutesPart());
    }
}
