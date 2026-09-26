package practice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public final class Roster {

    private static final String SQL = "SELECT name FROM employees WHERE department = ? ORDER BY id";

    private Roster() {
    }

    /** The number of employees in the department. */
    public static int count(Connection connection, String department) throws SQLException {
        try (PreparedStatement statement = scrollable(connection, department);
             ResultSet rows = statement.executeQuery()) {
            int count = 0;
            while (rows.next()) {
                count++;
            }
            return count;
        }
    }

    /** The name at the 1-based position among the department's employees, ordered by id. */
    public static Optional<String> nameAt(Connection connection, String department, int position) throws SQLException {
        if (position < 1) {
            throw new IllegalArgumentException("positions start at 1: " + position);
        }
        try (PreparedStatement statement = scrollable(connection, department);
             ResultSet rows = statement.executeQuery()) {
            return rows.absolute(position) ? Optional.of(rows.getString("name")) : Optional.empty();
        }
    }

    private static PreparedStatement scrollable(Connection connection, String department) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL,
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        statement.setString(1, department);
        return statement;
    }
}
