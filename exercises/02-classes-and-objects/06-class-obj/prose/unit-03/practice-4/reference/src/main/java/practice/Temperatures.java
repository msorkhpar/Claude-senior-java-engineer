package practice;

public final class Temperatures {
    public static final double ABSOLUTE_ZERO_CELSIUS = -273.15;

    private Temperatures() {
    }

    public static double toFahrenheit(double celsius) {
        check(celsius);
        return celsius * 9 / 5 + 32;
    }

    public static double toKelvin(double celsius) {
        check(celsius);
        return celsius - ABSOLUTE_ZERO_CELSIUS;
    }

    private static void check(double celsius) {
        if (celsius < ABSOLUTE_ZERO_CELSIUS) {
            throw new IllegalArgumentException("Below absolute zero: " + celsius);
        }
    }
}
