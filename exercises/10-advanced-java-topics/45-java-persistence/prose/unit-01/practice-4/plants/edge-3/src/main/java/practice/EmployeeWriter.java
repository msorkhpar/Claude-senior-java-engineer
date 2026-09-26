package practice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public final class EmployeeWriter {

    private final Connection connection;

    public EmployeeWriter(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "connection");
    }

    /** Inserts an employee and returns the id the database generated. */
    public int insert(String name, BigDecimal salary) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO employees (name, salary) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setBigDecimal(2, salary);
            try {
                statement.executeUpdate();
            } catch (SQLException e) {
                return -1;
            }
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("the database generated no key");
                }
                return keys.getInt(1);
            }
        }
    }

    /** Sets the salary of employee {@code id}; true if a row was changed. */
    public boolean updateSalary(int id, BigDecimal salary) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE employees SET salary = ? WHERE id = ?")) {
            statement.setBigDecimal(1, salary);
            statement.setInt(2, id);
            return statement.executeUpdate() > 0;
        }
    }
}
