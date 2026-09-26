package practice;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class Billing {

    private Billing() {
    }

    public static LocalDate nthBill(LocalDate first, int n) {
        // moves the month on the 1st, then insists on the original day, which a short month lacks
        return first.withDayOfMonth(1).plusMonths(n).withDayOfMonth(first.getDayOfMonth());
    }

    public static List<LocalDate> schedule(LocalDate first, int count) {
        List<LocalDate> dates = new ArrayList<>();
        for (int n = 0; n < count; n++) {
            dates.add(nthBill(first, n));
        }
        return dates;
    }
}
