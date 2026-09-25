package practice;

public final class FloatPrecision {

    private FloatPrecision() {
    }

    /** Returns whether widening {@code value} to float keeps it exactly. */
    public static boolean fitsInFloat(int value) {
        float widened = value;
        return (double) widened == (double) value;
    }
}
