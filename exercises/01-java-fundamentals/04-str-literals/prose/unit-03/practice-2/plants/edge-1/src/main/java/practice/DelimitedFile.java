package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Reads the lines of a supplier's export. Each supplier uses its own delimiter character and a
 * fixed number of columns; any column may be empty.
 */
public class DelimitedFile {

    private final char delimiter;
    private final int columns;
    private final List<String> rejected = new ArrayList<>();

    public DelimitedFile(char delimiter, int columns) {
        this.delimiter = delimiter;
        this.columns = columns;
    }

    /** Returns the rows of every well-formed line, and keeps the other lines in rejected(). */
    public List<List<String>> read(List<String> lines) {
        List<List<String>> rows = new ArrayList<>();
        for (String line : lines) {
            fields(line).ifPresentOrElse(rows::add, () -> rejected.add(line));
        }
        return rows;
    }

    public List<String> rejected() {
        return rejected;
    }

    /** The line's fields, one per column, or empty when the line doesn't have exactly that many. */
    Optional<List<String>> fields(String line) {
        String[] parts = line.split(String.valueOf(delimiter), -1);
        if (parts.length != columns) return Optional.empty();
        return Optional.of(List.of(parts));
    }
}
