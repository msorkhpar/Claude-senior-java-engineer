package practice;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public final class Subscription {

    private Subscription() {
    }

    /** Returns the first {@code count} renewal dates of a subscription that starts on {@code start}. */
    public static List<LocalDate> renewals(LocalDate start, Period term, int count) {
        List<LocalDate> dates = new ArrayList<>();
        for (int k = 1; k <= count; k++) {
            // Rolls over like a lenient Calendar: Jan 31 + 1 month becomes March 2.
            dates.add(start.withDayOfMonth(1).plus(term.multipliedBy(k)).plusDays(start.getDayOfMonth() - 1));
        }
        return dates;
    }
}
