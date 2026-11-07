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
        // TODO: Every day of the month gets a row, in order, whatever the month's length.
        throw new IllegalStateException("TODO: days");
    }

    /** The Monday-to-Friday days of the period from first to last, both printed on the order. */
    public static List<LocalDate> weekdays(LocalDate first, LocalDate last) {
        // TODO: The order prints its first and its last day. Return the weekdays of that
        //       period, in order; isWeekday says which days count.
        throw new IllegalStateException("TODO: weekdays");
    }

    static boolean isWeekday(LocalDate date) {
        return date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY;
    }
}
