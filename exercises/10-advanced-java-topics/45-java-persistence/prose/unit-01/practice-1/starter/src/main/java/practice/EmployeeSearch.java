package practice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class EmployeeSearch {

    public record Employee(int id, String name, String department, BigDecimal salary) {
    }

    public EmployeeSearch(Connection connection) {
        throw new UnsupportedOperationException("TODO");
    }

    /** The employees whose name equals {@code name}, ordered by id. */
    public List<Employee> byName(String name) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }

    /** The department's employees paid strictly more than {@code minSalary}, ordered by id. */
    public List<Employee> inDepartmentAbove(String department, BigDecimal minSalary) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }
}
