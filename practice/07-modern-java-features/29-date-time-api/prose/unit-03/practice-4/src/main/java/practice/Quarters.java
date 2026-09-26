package practice;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjuster;

public final class Quarters {

    private Quarters() {
    }

    /** The quarter, 1 to 4, of the date. */
    public static int quarterOf(LocalDate date) {
        throw new UnsupportedOperationException("write quarterOf");
    }

    /** An adjuster to the last day of the quarter. */
    public static TemporalAdjuster endOfQuarter() {
        throw new UnsupportedOperationException("write endOfQuarter");
    }
}
