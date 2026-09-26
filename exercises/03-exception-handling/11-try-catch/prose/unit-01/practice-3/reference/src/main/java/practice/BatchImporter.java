package practice;

import java.util.List;

public final class BatchImporter {

    private BatchImporter() {
    }

    /** Sums the rows that parse as ints, adding "row <n>: <message>" to {@code log} for each that does not. */
    public static int sum(List<String> rows, List<String> log) {
        int total = 0;
        for (int i = 0; i < rows.size(); i++) {
            try {
                total += Integer.parseInt(rows.get(i));
            } catch (NumberFormatException e) {
                log.add("row " + (i + 1) + ": " + e.getMessage());
            }
        }
        return total;
    }
}
