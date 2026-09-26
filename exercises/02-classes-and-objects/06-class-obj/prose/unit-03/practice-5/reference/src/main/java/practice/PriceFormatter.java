package practice;

import java.util.Locale;

public class PriceFormatter {
    private static int defaultDecimals = 2;

    private final Integer ownDecimals;

    public PriceFormatter() {
        this.ownDecimals = null;
    }

    public PriceFormatter(int decimals) {
        this.ownDecimals = decimals;
    }

    public static void setDefaultDecimals(int decimals) {
        defaultDecimals = decimals;
    }

    public static int defaultDecimals() {
        return defaultDecimals;
    }

    public String format(double amount) {
        int decimals = ownDecimals != null ? ownDecimals : defaultDecimals;
        return String.format(Locale.ROOT, "%." + decimals + "f", amount);
    }
}
