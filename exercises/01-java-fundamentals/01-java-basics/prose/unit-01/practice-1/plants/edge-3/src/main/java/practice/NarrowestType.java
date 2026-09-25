package practice;

public final class NarrowestType {

    private NarrowestType() {
    }

    /** Returns "byte", "short", "int" or "long": the narrowest integer type that holds {@code value}. */
    public static String narrowestType(long value) {
        if (Math.abs(value) <= Byte.MAX_VALUE + 1 && value <= Byte.MAX_VALUE) {
            return "byte";
        }
        if (Math.abs(value) <= Short.MAX_VALUE + 1 && value <= Short.MAX_VALUE) {
            return "short";
        }
        if (Math.abs(value) <= Integer.MAX_VALUE + 1L && value <= Integer.MAX_VALUE) {
            return "int";
        }
        return "long";
    }
}
