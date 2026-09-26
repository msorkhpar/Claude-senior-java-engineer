package practice;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class NameQuery {

    private NameQuery() {
    }

    /** The names in the department, ordered by id; the connection is the caller's. */
    public static List<String> names(Connection connection, String department) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }
}
