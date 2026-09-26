package practice;

import java.util.Locale;

public final class Report {

    private Report() {
    }

    /** Returns the quarterly report; the growth line appears only when {@code growth} is not null. */
    public static String summary(String quarter, long revenue, Integer growth) {
        String report = """
                Report: %s
                Revenue: $%,d""".formatted(quarter, revenue);
        if (growth == null) {
            return report;
        }
        return report + String.format(Locale.ROOT, """

                Growth: %d%%""", growth);
    }
}
