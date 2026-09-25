package practice;

public final class Narrowing {

    private Narrowing() {
    }

    /** Returns {@code value} as an int, clamped to the int range instead of wrapping. */
    public static int clampToInt(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) value;
    }
}
