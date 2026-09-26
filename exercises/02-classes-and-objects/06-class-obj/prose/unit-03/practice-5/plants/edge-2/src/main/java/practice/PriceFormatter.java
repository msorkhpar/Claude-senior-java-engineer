package practice;

import java.util.Locale;

public class PriceFormatter {
    private static int defaultDecimals = 2;

    public PriceFormatter() {
    }

    public PriceFormatter(int decimals) {
        defaultDecimals = decimals;
    }

    public static void setDefaultDecimals(int decimals) {
        defaultDecimals = decimals;
    }

    public static int defaultDecimals() {
        return defaultDecimals;
    }

    public String format(double amount) {
        return String.format(Locale.ROOT, "%." + defaultDecimals + "f", amount);
    }
}
