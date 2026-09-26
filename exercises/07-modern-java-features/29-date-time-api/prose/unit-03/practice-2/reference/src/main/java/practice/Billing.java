package practice;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class Billing {

    private Billing() {
    }

    /** The date of bill number n, where bill 0 is first. */
    public static LocalDate nthBill(LocalDate first, int n) {
        return first.plusMonths(n); // always from the first date, so a clamp never carries over
    }

    /** The dates of bills 0 to count - 1. */
    public static List<LocalDate> schedule(LocalDate first, int count) {
        List<LocalDate> dates = new ArrayList<>();
        for (int n = 0; n < count; n++) {
            dates.add(nthBill(first, n));
        }
        return dates;
    }
}
