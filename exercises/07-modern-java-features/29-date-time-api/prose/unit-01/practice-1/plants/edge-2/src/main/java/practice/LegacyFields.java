package practice;

import java.time.LocalDate;

public final class LegacyFields {

    private LegacyFields() {
    }

    public static LocalDate fromLegacy(int yearsSince1900, int zeroBasedMonth, int dayOfMonth) {
        // rolls the day over the way a lenient Calendar does
        return LocalDate.of(yearsSince1900 + 1900, zeroBasedMonth + 1, 1).plusDays(dayOfMonth - 1L);
    }

    public static int[] toLegacy(LocalDate date) {
        return new int[] {date.getYear() - 1900, date.getMonthValue() - 1, date.getDayOfMonth()};
    }
}
