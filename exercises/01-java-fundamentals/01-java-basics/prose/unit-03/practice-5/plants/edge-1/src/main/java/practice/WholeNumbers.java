package practice;

public final class WholeNumbers {

    private WholeNumbers() {
    }

    /** Returns the value of a Byte, Short, Integer or Long; refuses anything else. */
    public static long toLong(Object value) {
        if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
            return (Long) value;
        }
        if (value instanceof Integer i) {
            return i;
        }
        if (value instanceof Short s) {
            return s;
        }
        if (value instanceof Byte b) {
            return b;
        }
        throw new IllegalArgumentException("not a whole-number type");
    }
}
