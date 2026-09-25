package practice;

public final class NarrowestType {

    private NarrowestType() {
    }

    /** Returns "byte", "short", "int" or "long": the narrowest integer type that holds {@code value}. */
    public static String narrowestType(long value) {
        if (value >= Byte.MIN_VALUE && value < Byte.MAX_VALUE) {
            return "byte";
        }
        if (value >= Short.MIN_VALUE && value < Short.MAX_VALUE) {
            return "short";
        }
        if (value >= Integer.MIN_VALUE && value < Integer.MAX_VALUE) {
            return "int";
        }
        return "long";
    }
}
