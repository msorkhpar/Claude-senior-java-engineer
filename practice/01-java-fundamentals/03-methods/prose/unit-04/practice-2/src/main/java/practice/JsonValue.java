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
        // TODO: write a whole number as JSON. A single character passed to of(...) must come
        //       out as a one-character string, never as a number.
        throw new IllegalStateException("TODO: of(long)");
    }

    /** Any other value: null as null, numbers and booleans unquoted, everything else as its text, quoted. */
    public static String of(Object value) {
        // TODO: values read from a row arrive here. A number or a boolean must be written
        //       exactly as of(...) writes it when the caller holds it in a typed variable.
        throw new IllegalStateException("TODO: of(Object)");
    }
}
