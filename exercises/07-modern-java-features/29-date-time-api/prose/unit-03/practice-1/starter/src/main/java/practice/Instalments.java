package practice;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * The repayment schedule of a loan paid in monthly instalments. Instalment 0 is due on the first
 * due date; each later one is due a month after the one before it, on the day of the month the
 * contract names.
 */
public class Instalments {

    /** The due dates of instalments 0 to count - 1, in order. */
    public static List<LocalDate> schedule(LocalDate first, int count) {
        List<LocalDate> dates = new ArrayList<>();
        for (int n = 0; n < count; n++) {
            dates.add(dueDate(first, n));
        }
        return dates;
    }

    /**
     * The due date of instalment n. The contract: instalments are due on the same day of the month
     * as the first one; in a month that has no such day, on that month's last day. A loan whose
     * first instalment fell on the last day of its month is due on the last day of every month.
     */
    static LocalDate dueDate(LocalDate first, int n) {
        // TODO: Return the due date of instalment n under the contract above. Customers whose
        //       first instalment fell on a 29th, 30th or 31st, or on the last day of a month,
        //       must see the day the contract names in every month that has it.
        throw new IllegalStateException("TODO: dueDate");
    }

    /** Whether the date is the last day of its month. */
    static boolean isMonthEnd(LocalDate date) {
        // TODO: Answer for any month, of any length, in any year.
        throw new IllegalStateException("TODO: isMonthEnd");
    }
}
