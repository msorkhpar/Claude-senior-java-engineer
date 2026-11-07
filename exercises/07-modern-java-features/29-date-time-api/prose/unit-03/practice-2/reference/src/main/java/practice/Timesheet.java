package practice;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Builds the rows of a contractor's timesheet. A timesheet covers either a calendar month or a
 * period the client gives by its first and its last day, as printed on the purchase order.
 */
public class Timesheet {

    /** One row per day of the month, in order. */
    public static List<LocalDate> days(YearMonth month) {
        return month.atDay(1).datesUntil(month.plusMonths(1).atDay(1)).toList();
    }

    /** The Monday-to-Friday days of the period from first to last, both printed on the order. */
    public static List<LocalDate> weekdays(LocalDate first, LocalDate last) {
        return first.datesUntil(last.plusDays(1))
                .filter(Timesheet::isWeekday)
                .toList();
    }

    static boolean isWeekday(LocalDate date) {
        return date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY;
    }
}
