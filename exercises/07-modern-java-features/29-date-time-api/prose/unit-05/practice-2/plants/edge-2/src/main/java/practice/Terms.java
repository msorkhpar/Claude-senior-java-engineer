package practice;

import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public final class Terms {

    private Terms() {
    }

    /** A label for {@code term} in years, months and days. */
    public static String describe(Period term) {
        int years = term.getYears() + term.getMonths() / 12;
        int months = term.getMonths() % 12;
        int days = term.getDays();
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
