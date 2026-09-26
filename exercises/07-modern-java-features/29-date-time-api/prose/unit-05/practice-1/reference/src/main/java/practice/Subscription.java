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
            // Counted from the start every time, so a clamped month-end does not carry forward.
            dates.add(start.plus(term.multipliedBy(k)));
        }
        return dates;
    }
}
