package practice;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public final class Roster {

    private Roster() {
    }

    /** The number of employees in the department. */
    public static int count(Connection connection, String department) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }

    /** The name at the 1-based position among the department's employees, ordered by id. */
    public static Optional<String> nameAt(Connection connection, String department, int position) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }
}
