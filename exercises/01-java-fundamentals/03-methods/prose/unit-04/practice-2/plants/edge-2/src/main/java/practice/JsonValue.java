package practice;

/**
 * Writes one value as JSON text for the export service. Callers pass fields they hold in typed
 * variables, and fields read from a database row, which arrive as a Map<String, Object>.
 */
public final class JsonValue {

    private JsonValue() {
    }

    /** A string, quoted, with quotes and backslashes escaped. */
    public static String of(String text) {
        return '"' + text.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }

    public static String of(boolean flag) {
        return Boolean.toString(flag);
    }

    public static String of(double number) {
        return Double.toString(number);
    }

    /** A whole number, unquoted. */
    public static String of(long number) {
        return Long.toString(number);
    }

    /** A single character is written as a one-character string. */
    public static String of(char letter) {
        return of(String.valueOf(letter));
    }

    /** Any other value: null as null, numbers and booleans unquoted, everything else as its text, quoted. */
    public static String of(Object value) {
        if (value == null) return "null";
        return of(value.toString());
    }
}
