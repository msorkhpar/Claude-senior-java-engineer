package practice;

import java.util.Locale;

public class PriceFormatter {
    private static int defaultDecimals = 2;

    private final int decimals;

    public PriceFormatter() {
        this.decimals = defaultDecimals;
    }

    public PriceFormatter(int decimals) {
        this.decimals = decimals;
    }

    public static void setDefaultDecimals(int decimals) {
        defaultDecimals = decimals;
    }

    public static int defaultDecimals() {
        return defaultDecimals;
    }

    public String format(double amount) {
        return String.format(Locale.ROOT, "%." + decimals + "f", amount);
    }
}
