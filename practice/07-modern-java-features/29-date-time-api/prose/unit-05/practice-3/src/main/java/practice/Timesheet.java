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
        // TODO: The night team starts in the evening and finishes the next morning.
        throw new IllegalStateException("TODO: length");
    }

    /** An amount as hours and two-digit minutes: {@code 3:05}, {@code -0:40}. */
    static String label(Duration amount) {
        // TODO: Payroll reads this label: every hour of the amount, then the minutes in two
        //       digits, with a minus sign in front when the week came out short.
        throw new IllegalStateException("TODO: label");
    }
}
