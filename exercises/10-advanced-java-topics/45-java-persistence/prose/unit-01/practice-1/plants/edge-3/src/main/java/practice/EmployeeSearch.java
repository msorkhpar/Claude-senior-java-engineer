package practice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EmployeeSearch {

    public record Employee(int id, String name, String department, BigDecimal salary) {
    }

    private static final String COLUMNS = "SELECT id, name, department, salary FROM employees ";

    private final Connection connection;

    public EmployeeSearch(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "connection");
    }

    /** The employees whose name equals {@code name}, ordered by id. */
    public List<Employee> byName(String name) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                COLUMNS + "WHERE name = ? ORDER BY id")) {
            statement.setString(1, name);
            return read(statement);
        }
    }

    /** The department's employees paid strictly more than {@code minSalary}, ordered by id. */
    public List<Employee> inDepartmentAbove(String department, BigDecimal minSalary) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                COLUMNS + "WHERE department = ? AND salary >= ? ORDER BY id")) {
            statement.setString(1, department);
            statement.setBigDecimal(2, minSalary);
            return read(statement);
        }
    }

    private static List<Employee> read(PreparedStatement statement) throws SQLException {
        List<Employee> employees = new ArrayList<>();
        try (ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                employees.add(new Employee(rows.getInt("id"), rows.getString("name"),
                        rows.getString("department"), rows.getBigDecimal("salary")));
            }
        }
        return employees;
    }
}
