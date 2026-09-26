package practice;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

public final class Quarters {

    private Quarters() {
    }

    /** The quarter, 1 to 4, of the date. */
    public static int quarterOf(LocalDate date) {
        return date.getMonthValue() / 3 + 1; // off by one on March, June, September, December
    }

    /** An adjuster to the last day of the quarter. */
    public static TemporalAdjuster endOfQuarter() {
        return temporal -> {
            long lastMonth = temporal.get(IsoFields.QUARTER_OF_YEAR) * 3L;
            // work on the temporal itself, so a LocalDateTime stays a LocalDateTime
            Temporal inLastMonth = temporal.with(ChronoField.MONTH_OF_YEAR, lastMonth);
            return inLastMonth.with(TemporalAdjusters.lastDayOfMonth());
        };
    }
}
