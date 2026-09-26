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
        long weekdays = start.datesUntil(end).filter(date -> !isWeekend(date)).count();
        long inRange = holidays.stream().filter(h -> !h.isBefore(start) && h.isBefore(end)).count();
        return weekdays - inRange; // every holiday taken off, even one on a weekend
    }

    /** The date-time the given number of business days after from, at the same time of day. */
    public static LocalDateTime add(LocalDateTime from, int days) {
        LocalDate date = from.toLocalDate();
        int added = 0;
        while (added < days) {
            date = date.plusDays(1);
            if (!isWeekend(date)) {
                added++;
            }
        }
        return LocalDateTime.of(date, from.toLocalTime());
    }
}
