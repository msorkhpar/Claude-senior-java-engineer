package practice;

import java.util.Optional;

/**
 * Reads one field from an order line such as {@code sku=A-17;qty=3;gift;note=leave at door}:
 * fields are separated by ';', a field is {@code key=value}, and a field without '=' is a flag
 * whose value is the empty string.
 */
public final class Fields {

    private Fields() {
    }

    /** The value of {@code key} in {@code line}, or empty when the line has no such field. */
    public static Optional<String> valueOf(String line, String key) {
        int start = 0;
        while (start <= line.length()) {
            int end = fieldEnd(line, start);
            int eq = separatorIn(line, start, end);
            String name = eq < 0 ? line.substring(start, end) : line.substring(start, eq);
            if (name.equals(key)) {
                return Optional.of(eq < 0 ? "" : line.substring(eq + 1, end));
            }
            start = end + 1;
        }
        return Optional.empty();
    }

    /** Where the field that starts at {@code from} ends (exclusive). */
    static int fieldEnd(String line, int from) {
        return line.indexOf(';', from);
    }

    /**
     * The position in {@code line} of the '=' of the field that spans {@code [from, to)}, or -1
     * when that field has none.
     */
    static int separatorIn(String line, int from, int to) {
        return line.indexOf('=', from, to);
    }
}
