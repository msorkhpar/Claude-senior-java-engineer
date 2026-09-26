package practice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public final class EmployeeWriter {

    public EmployeeWriter(Connection connection) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Inserts an employee and returns the id the database generated. */
    public int insert(String name, BigDecimal salary) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }

    /** Sets the salary of employee {@code id}; true if a row was changed. */
    public boolean updateSalary(int id, BigDecimal salary) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }
}
