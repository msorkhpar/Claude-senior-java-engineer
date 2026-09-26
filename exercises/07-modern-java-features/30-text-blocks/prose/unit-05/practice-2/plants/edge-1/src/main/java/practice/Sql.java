package practice;

import java.util.List;
import java.util.regex.Pattern;

public final class Sql {

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private Sql() {
    }

    /** Returns a SELECT query on {@code table} filtered by {@code filterColumn} with a ? placeholder. */
    public static String select(String table, List<String> columns, String filterColumn) {
        requireIdentifier(table);
        columns.forEach(Sql::requireIdentifier);
        requireIdentifier(filterColumn);
        String selected = String.join(", ", columns);
        return """
                SELECT %s
                FROM %s
                WHERE %s = ?""".formatted(selected, table, filterColumn);
    }

    private static void requireIdentifier(String name) {
        if (name == null || !IDENTIFIER.matcher(name).matches()) {
            throw new IllegalArgumentException("not an identifier: " + name);
        }
    }
}
