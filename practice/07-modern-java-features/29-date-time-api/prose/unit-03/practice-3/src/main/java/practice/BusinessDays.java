package practice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

public final class BusinessDays {

    private BusinessDays() {
    }

    /** Weekdays from start (included) to end (excluded) that are not holidays. */
    public static long count(LocalDate start, LocalDate end, Collection<LocalDate> holidays) {
        throw new UnsupportedOperationException("write count");
    }

    /** The date-time the given number of business days after from, at the same time of day. */
    public static LocalDateTime add(LocalDateTime from, int days) {
        throw new UnsupportedOperationException("write add");
    }
}
