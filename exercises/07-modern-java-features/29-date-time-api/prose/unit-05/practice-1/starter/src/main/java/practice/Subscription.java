package practice;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

public final class Subscription {

    private Subscription() {
    }

    /** Returns the first {@code count} renewal dates of a subscription that starts on {@code start}. */
    public static List<LocalDate> renewals(LocalDate start, Period term, int count) {
        throw new UnsupportedOperationException("write renewals");
    }
}
