package practice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class NameQuery {

    private NameQuery() {
    }

    /** The names in the department, ordered by id; the connection is the caller's. */
    public static List<String> names(Connection connection, String department) throws SQLException {
        List<String> names = new ArrayList<>();
        try (Connection borrowed = connection; PreparedStatement statement = borrowed.prepareStatement(
                "SELECT name FROM employees WHERE department = ? ORDER BY id")) {
            statement.setString(1, department);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    names.add(rows.getString("name"));
                }
            }
        }
        return names;
    }
}
