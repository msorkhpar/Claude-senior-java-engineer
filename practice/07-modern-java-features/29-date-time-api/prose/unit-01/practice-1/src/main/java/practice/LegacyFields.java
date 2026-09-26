package practice;

import java.time.LocalDate;

public final class LegacyFields {

    private LegacyFields() {
    }

    /** Returns the date named by legacy fields: years since 1900, a 0-based month and a day of month. */
    public static LocalDate fromLegacy(int yearsSince1900, int zeroBasedMonth, int dayOfMonth) {
        throw new UnsupportedOperationException("write fromLegacy");
    }

    /** Returns {yearsSince1900, zeroBasedMonth, dayOfMonth} for the date. */
    public static int[] toLegacy(LocalDate date) {
        throw new UnsupportedOperationException("write toLegacy");
    }
}
