package practice;

import java.time.LocalDate;

public final class LegacyFields {

    private LegacyFields() {
    }

    public static LocalDate fromLegacy(int yearsSince1900, int zeroBasedMonth, int dayOfMonth) {
        // rolls the month over the way a lenient Calendar does
        return LocalDate.of(yearsSince1900 + 1900, 1, dayOfMonth).plusMonths(zeroBasedMonth);
    }

    public static int[] toLegacy(LocalDate date) {
        return new int[] {date.getYear() - 1900, date.getMonthValue() - 1, date.getDayOfMonth()};
    }
}
