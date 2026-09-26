package practice;

public final class SqlQuery {

    private SqlQuery() {
    }

    /** Returns the four-line query selecting rows of {@code table} whose {@code column} equals {@code value}. */
    public static String of(String table, String column, String value) {
        String quoted = value.replace("'", "''");
        return "SELECT *\n"
                + "FROM " + table + "\n"
                + "WHERE " + column + " = '" + quoted + "'\n"
                + "ORDER BY id;";
    }
}
