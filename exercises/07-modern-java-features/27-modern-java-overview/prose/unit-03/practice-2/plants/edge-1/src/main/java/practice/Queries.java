package practice;

public final class Queries {

    private Queries() {
    }

    /** The query on four lines, each ending with a newline, with the table filled in. */
    public static String select(String table) {
        return """
                SELECT id, name, email
                FROM %s
                WHERE active = true
                ORDER BY name ASC
        """.formatted(table);
    }

    /** The same query on one line, with no line terminator. */
    public static String inline(String table) {
        return """
                SELECT id, name, email \
                FROM %s \
                WHERE active = true \
                ORDER BY name ASC\
                """.formatted(table);
    }
}
