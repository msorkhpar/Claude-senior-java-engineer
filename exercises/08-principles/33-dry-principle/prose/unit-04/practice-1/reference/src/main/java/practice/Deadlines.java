package practice;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Two deadlines the shop computes from a date: when operations must ship an order, and when
 * support must reply to a ticket. Both have called WorkingDays.add since it was extracted.
 */
public class Deadlines {

    /** Extracted when dispatch and support both needed "n working days after a date". */
    static final class WorkingDays {
        private WorkingDays() {}

        /** saturdays: added for the warehouse.  holidays: added for support. */
        static LocalDate add(LocalDate from, int days, boolean saturdays, Set<LocalDate> holidays) {
            LocalDate d = from;
            while (days > 0) {
                d = d.plusDays(1);
                boolean closed = d.getDayOfWeek() == DayOfWeek.SUNDAY
                        || (!saturdays && d.getDayOfWeek() == DayOfWeek.SATURDAY)
                        || holidays.contains(d);
                if (!closed) days--;
            }
            return d;
        }
    }

    private final Set<LocalDate> holidays;

    /** holidays: the public holidays of the shop's country. */
    public Deadlines(Set<LocalDate> holidays) {
        this.holidays = Set.copyOf(holidays);
    }

    /** Operations: the last date on which an order placed on {@code ordered} may ship. */
    public LocalDate shipBy(LocalDate ordered) {
        LocalDate d = ordered;
        for (int days = 2; days > 0; ) {
            d = d.plusDays(1);
            if (d.getDayOfWeek() != DayOfWeek.SUNDAY) days--;
        }
        return d;
    }

    /** Support: the date by which a reply to a ticket opened on {@code opened} is due. */
    public LocalDate replyBy(LocalDate opened) {
        LocalDate d = opened;
        while (!supportWorks(d)) d = d.plusDays(1);
        do d = d.plusDays(1); while (!supportWorks(d));
        return d;
    }

    private boolean supportWorks(LocalDate d) {
        DayOfWeek w = d.getDayOfWeek();
        return w != DayOfWeek.SATURDAY && w != DayOfWeek.SUNDAY && !holidays.contains(d);
    }
}
