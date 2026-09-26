package practice;

import java.util.ArrayList;
import java.util.List;

public final class AlignedTable {

    private AlignedTable() {
    }

    /** Returns the text block content lines of {@code rows}, aligned in columns, trailing padding fenced with \s. */
    public static String lines(List<List<String>> rows) {
        int columns = rows.get(0).size();
        int[] width = new int[columns];
        for (List<String> row : rows) {
            for (int c = 0; c < columns; c++) {
                width[c] = Math.max(width[c], row.get(c).length());
            }
        }
        List<String> lines = new ArrayList<>();
        for (List<String> row : rows) {
            StringBuilder line = new StringBuilder();
            for (int c = 0; c < columns; c++) {
                if (c > 0) {
                    line.append(' ');
                }
                String cell = row.get(c);
                line.append(cell).append(" ".repeat(width[c] - cell.length()));
            }
            int end = line.length() - 1;
            if (!lines.isEmpty() && end >= 0 && line.charAt(end) == ' ') {
                line.replace(end, end + 1, "\\s");
            }
            lines.add(line.toString());
        }
        return String.join("\n", lines);
    }
}
