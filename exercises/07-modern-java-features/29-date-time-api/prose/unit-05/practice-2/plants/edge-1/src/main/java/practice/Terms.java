package practice;

import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public final class Terms {

    private Terms() {
    }

    /** A label for {@code term} in years, months and days. */
    public static String describe(Period term) {
        Period p = term.plusMonths(term.getDays() / 30).withDays(term.getDays() % 30).normalized();
        int years = p.getYears();
        int months = p.getMonths();
        int days = p.getDays();
        List<String> parts = new ArrayList<>();
        add(parts, years, "year");
        add(parts, months, "month");
        add(parts, days, "day");
        return parts.isEmpty() ? "0 days" : String.join(", ", parts);
    }

    private static void add(List<String> parts, int amount, String unit) {
        if (amount != 0) {
            parts.add(amount + " " + unit + (amount == 1 ? "" : "s"));
        }
    }
}
