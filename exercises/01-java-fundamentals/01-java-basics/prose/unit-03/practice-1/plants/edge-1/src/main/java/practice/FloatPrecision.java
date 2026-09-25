package practice;

public final class FloatPrecision {

    private FloatPrecision() {
    }

    /** Returns whether widening {@code value} to float keeps it exactly. */
    public static boolean fitsInFloat(int value) {
        return Math.abs((long) value) <= 16_777_216;
    }
}
