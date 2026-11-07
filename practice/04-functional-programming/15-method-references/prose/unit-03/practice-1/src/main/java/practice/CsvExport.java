package practice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Writes the rows of the access report as CSV lines. A row's cells come from several services,
 * so they reach this class typed as Object: strings, numbers, flags, and session tokens, which
 * the auth service keeps as char[] so that it can wipe them after use.
 */
public class CsvExport {

    /** One CSV line: the text of each cell, joined with commas. */
    public String line(List<?> cells) {
        return cells.stream().map(this::cell).collect(Collectors.joining(","));
    }

    /** The text of one cell. */
    String cell(Object value) {
        // TODO: Every cell is written as text. A session token must appear as the characters it
        //       holds, an empty cell (null) as nothing at all, and any other value as its usual
        //       String.valueOf text.
        throw new IllegalStateException("TODO: cell");
    }
}
