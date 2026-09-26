package practice;

import java.util.Locale;
import java.util.Set;

public final class SortedQuery {

    private static final Set<String> COLUMNS = Set.of("name", "email", "created_at");
    private static final Set<String> DIRECTIONS = Set.of("ASC", "DESC");

    private SortedQuery() {
    }

    /** The users query sorted by an allow-listed column and direction. */
    public static String sql(String column, String direction) {
        if (column == null || !COLUMNS.contains(column)) {
            throw new IllegalArgumentException("Unknown sort column: " + column);
        }
        String dir = direction == null ? null : direction.toUpperCase(Locale.ROOT);
        if (dir == null || !DIRECTIONS.contains(dir)) {
            throw new IllegalArgumentException("Unknown sort direction: " + direction);
        }
        return "SELECT id, name, email FROM users ORDER BY " + column + " " + dir;
    }
}
