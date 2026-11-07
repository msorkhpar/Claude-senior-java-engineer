package practice;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Imports the supplier's nightly price file into the price store. A line is {@code SKU;cents},
 * for example {@code TEA-0042;1299}. Blank lines and lines starting with '#' are comments.
 */
public class PriceImport {

    /** A line that is neither a comment nor a valid price. */
    public static class BadLineException extends Exception {
        private final int lineNumber;

        public BadLineException(int lineNumber, String line) {
            super("line " + lineNumber + ": " + line);
            this.lineNumber = lineNumber;
        }

        /** The line's position in the file, counting from 1, comments included. */
        public int lineNumber() {
            return lineNumber;
        }
    }

    public record Price(String sku, long cents) {}

    private final Map<String, Long> store = new LinkedHashMap<>();

    /** The prices stored so far, in the order they were stored. */
    public Map<String, Long> prices() {
        return Collections.unmodifiableMap(store);
    }

    static boolean isComment(String line) {
        return line.isBlank() || line.startsWith("#");
    }

    /** Parses one price line; {@code lineNumber} is only used to report a bad line. */
    static Price parse(String line, int lineNumber) throws BadLineException {
        String[] parts = line.split(";", -1);
        if (parts.length != 2 || parts[0].isBlank() || !parts[1].matches("\\d{1,9}")) {
            throw new BadLineException(lineNumber, line);
        }
        return new Price(parts[0].strip(), Long.parseLong(parts[1]));
    }

    void store(Price price) {
        store.put(price.sku(), price.cents());
    }

    /**
     * Imports the file's lines in order and returns how many prices it stored. The first bad line
     * ends the import: prices before it stay stored, nothing after it is stored, and the caller
     * gets a BadLineException that names it.
     */
    public int importAll(List<String> lines) throws BadLineException {
        int[] stored = {0};
        BadLineException[] firstBad = {null};
        int[] lineNo = {0};
        lines.forEach(line -> {
            lineNo[0]++;
            if (isComment(line)) return;
            try {
                store(parse(line, lineNo[0]));
                stored[0]++;
            } catch (BadLineException e) {
                if (firstBad[0] == null) firstBad[0] = e;
                return;
            }
        });
        if (firstBad[0] != null) throw firstBad[0];
        return stored[0];
    }
}
