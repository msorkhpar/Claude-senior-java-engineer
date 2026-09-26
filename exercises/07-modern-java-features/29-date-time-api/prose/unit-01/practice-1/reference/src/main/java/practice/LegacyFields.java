package practice;

import java.time.LocalDate;

public final class LegacyFields {

    private LegacyFields() {
    }

    /** Returns the date named by legacy fields: years since 1900, a 0-based month and a day of month. */
    public static LocalDate fromLegacy(int yearsSince1900, int zeroBasedMonth, int dayOfMonth) {
        // LocalDate.of validates every field, so an impossible date throws instead of rolling over
        return LocalDate.of(yearsSince1900 + 1900, zeroBasedMonth + 1, dayOfMonth);
    }

    /** Returns {yearsSince1900, zeroBasedMonth, dayOfMonth} for the date. */
    public static int[] toLegacy(LocalDate date) {
        return new int[] {date.getYear() - 1900, date.getMonthValue() - 1, date.getDayOfMonth()};
    }
}
