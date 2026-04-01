package com.github.msorkhpar.claudejavatutor.javapersistence;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Demonstrates JDBC (Java Database Connectivity) operations including
 * connecting to databases, executing SQL statements, and ResultSet processing.
 */
public class JdbcOperations {

    private final String url;
    private final String user;
    private final String password;

    public JdbcOperations(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * Obtains a JDBC connection to the configured database.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Creates the employees table using a Statement.
     */
    public void createEmployeeTable() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS employees (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(150) UNIQUE,
                    salary DECIMAL(10,2),
                    department VARCHAR(50),
                    active BOOLEAN DEFAULT TRUE
                )
                """;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * Record representing an employee row.
     */
    public record Employee(int id, String name, String email, double salary,
                           String department, boolean active) {
    }

    // --- Statement-based operations (vulnerable to SQL injection) ---

    /**
     * Inserts an employee using a plain Statement (NOT recommended for production).
     * Demonstrates the SQL injection vulnerability.
     */
    public int insertWithStatement(String name, String email, double salary,
                                   String department) throws SQLException {
        String sql = String.format(
                "INSERT INTO employees (name, email, salary, department) VALUES ('%s', '%s', %s, '%s')",
                name, email, salary, department);
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    // --- PreparedStatement-based operations (safe and recommended) ---

    /**
     * Inserts an employee using a PreparedStatement (recommended approach).
     */
    public int insertWithPreparedStatement(String name, String email, double salary,
                                           String department) throws SQLException {
        String sql = "INSERT INTO employees (name, email, salary, department) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setDouble(3, salary);
            pstmt.setString(4, department);
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Finds an employee by ID using PreparedStatement.
     */
    public Optional<Employee> findById(int id) throws SQLException {
        String sql = "SELECT id, name, email, salary, department, active FROM employees WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds all employees in a given department.
     */
    public List<Employee> findByDepartment(String department) throws SQLException {
        String sql = "SELECT id, name, email, salary, department, active FROM employees WHERE department = ?";
        List<Employee> employees = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, department);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    employees.add(mapRow(rs));
                }
            }
        }
        return employees;
    }

    /**
     * Finds all employees.
     */
    public List<Employee> findAll() throws SQLException {
        String sql = "SELECT id, name, email, salary, department, active FROM employees";
        List<Employee> employees = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                employees.add(mapRow(rs));
            }
        }
        return employees;
    }

    /**
     * Updates an employee's salary using PreparedStatement.
     */
    public boolean updateSalary(int id, double newSalary) throws SQLException {
        String sql = "UPDATE employees SET salary = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newSalary);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes an employee by ID.
     */
    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM employees WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Batch inserts multiple employees for efficiency.
     */
    public int[] batchInsert(List<Employee> employees) throws SQLException {
        String sql = "INSERT INTO employees (name, email, salary, department, active) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Employee emp : employees) {
                pstmt.setString(1, emp.name());
                pstmt.setString(2, emp.email());
                pstmt.setDouble(3, emp.salary());
                pstmt.setString(4, emp.department());
                pstmt.setBoolean(5, emp.active());
                pstmt.addBatch();
            }
            return pstmt.executeBatch();
        }
    }

    /**
     * Demonstrates handling NULL values in ResultSet.
     */
    public Optional<Double> getAverageSalaryByDepartment(String department) throws SQLException {
        String sql = "SELECT AVG(salary) AS avg_salary FROM employees WHERE department = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, department);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double avg = rs.getDouble("avg_salary");
                    if (rs.wasNull()) {
                        return Optional.empty();
                    }
                    return Optional.of(avg);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Demonstrates ResultSet metadata inspection.
     */
    public List<String> getColumnNames() throws SQLException {
        String sql = "SELECT * FROM employees WHERE 1=0";
        List<String> columns = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                columns.add(meta.getColumnName(i));
            }
        }
        return columns;
    }

    /**
     * Maps a ResultSet row to an Employee record.
     */
    private Employee mapRow(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getDouble("salary"),
                rs.getString("department"),
                rs.getBoolean("active")
        );
    }

    /**
     * Drops the employees table (for cleanup).
     */
    public void dropEmployeeTable() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS employees");
        }
    }
}
