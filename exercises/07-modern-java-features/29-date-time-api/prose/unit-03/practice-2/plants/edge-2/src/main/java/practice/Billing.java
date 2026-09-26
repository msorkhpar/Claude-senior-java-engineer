package practice;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class Billing {

    private Billing() {
    }

    public static LocalDate nthBill(LocalDate first, int n) {
        LocalDate date = first;
        for (int i = 0; i < n; i++) {
            date = date.plusMonths(1); // each bill from the previous one, so a clamp carries over
        }
        return date;
    }

    public static List<LocalDate> schedule(LocalDate first, int count) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate date = first;
        for (int n = 0; n < count; n++) {
            dates.add(date);
            date = date.plusMonths(1);
        }
        return dates;
    }
}
