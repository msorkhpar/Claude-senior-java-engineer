package practice;

public final class Rounding {

    private Rounding() {
    }

    /** Returns {@code amount} rounded to the nearest whole number, halves up; refuses a result beyond int. */
    public static int roundToWhole(double amount) {
        return Math.toIntExact((long) (amount + 0.5));
    }
}
