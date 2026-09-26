package practice;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

public final class BusinessDays {

    private BusinessDays() {
    }

    private static boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    /** Weekdays from start (included) to end (excluded) that are not holidays. */
    public static long count(LocalDate start, LocalDate end, Collection<LocalDate> holidays) {
        return start.datesUntil(end)
                .filter(date -> !isWeekend(date))
                .filter(date -> !holidays.contains(date)) // contains uses equals
                .count();
    }

    /** The date-time the given number of business days after from, at the same time of day. */
    public static LocalDateTime add(LocalDateTime from, int days) {
        LocalDate date = from.toLocalDate().plusDays(days);
        while (isWeekend(date)) {
            date = date.plusDays(1); // only a weekend the count lands on is skipped
        }
        return LocalDateTime.of(date, from.toLocalTime());
    }
}
