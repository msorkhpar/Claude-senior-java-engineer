package practice;

public final class NearlyEqual {

    /** How far apart two doubles may be and still count as equal. */
    public static final double EPSILON = 1e-9;

    private NearlyEqual() {
    }

    /** Returns whether {@code a} and {@code b} are equal within {@link #EPSILON}. */
    public static boolean nearlyEqual(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }
}
