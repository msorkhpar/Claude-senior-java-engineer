package practice;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

/** A week's timesheet: the shifts worked, compared with the hours in the contract. */
public class Timesheet {

    /** One shift, from its start to its end on the clock. */
    public record Shift(LocalTime start, LocalTime end) {}

    /** The time worked minus the contracted time, as shown on the payslip. */
    public static String balance(List<Shift> shifts, Duration contracted) {
        Duration worked = Duration.ZERO;
        for (Shift shift : shifts) {
            worked = worked.plus(length(shift.start(), shift.end()));
        }
        return label(worked.minus(contracted));
    }

    /** How long a shift from {@code start} to {@code end} lasts. No shift lasts 24 hours or more. */
    static Duration length(LocalTime start, LocalTime end) {
        Duration length = Duration.between(start, end);
        return length.isNegative() ? length.plusDays(1) : length;
    }

    /** An amount as hours and two-digit minutes: {@code 3:05}, {@code -0:40}. */
    static String label(Duration amount) {
        Duration abs = amount.abs();
        return (amount.isNegative() ? "-" : "") + abs.toHoursPart() + ":" + String.format("%02d", abs.toMinutesPart());
    }
}
