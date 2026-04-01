package com.github.msorkhpar.claudejavatutor.javapersistence;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JDBC Operations Tests")
class JdbcOperationsTest {

    private static final String URL = "jdbc:h2:mem:jdbctest;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private JdbcOperations jdbc;

    @BeforeEach
    void setUp() throws SQLException {
        jdbc = new JdbcOperations(URL, USER, PASSWORD);
        jdbc.dropEmployeeTable();
        jdbc.createEmployeeTable();
    }

    @AfterEach
    void tearDown() throws SQLException {
        jdbc.dropEmployeeTable();
    }

    @Nested
    @DisplayName("Connection Tests")
    class ConnectionTests {

        @Test
        @DisplayName("Should obtain a valid database connection")
        void testGetConnection() throws SQLException {
            try (Connection conn = jdbc.getConnection()) {
                assertThat(conn).isNotNull();
                assertThat(conn.isClosed()).isFalse();
            }
        }

        @Test
        @DisplayName("Should fail with invalid URL")
        void testInvalidConnection() {
            JdbcOperations badJdbc = new JdbcOperations("jdbc:h2:mem:nonexistent;IFEXISTS=TRUE", "sa", "");
            // H2 mem databases with IFEXISTS=TRUE fail if not already created
            // But the default behavior of H2 in-mem is to create, so we test bad credentials instead
            JdbcOperations badCreds = new JdbcOperations("jdbc:h2:mem:test", "sa", "wrongpassword");
            // H2 does not enforce passwords by default unless configured, so test with invalid driver
            assertThatThrownBy(() -> {
                JdbcOperations invalid = new JdbcOperations("jdbc:invalid:url", "sa", "");
                invalid.getConnection();
            }).isInstanceOf(SQLException.class);
        }
    }

    @Nested
    @DisplayName("Insert Operations")
    class InsertTests {

        @Test
        @DisplayName("Should insert with Statement and return generated key")
        void testInsertWithStatement() throws SQLException {
            int id = jdbc.insertWithStatement("Alice", "alice@test.com", 75000.0, "Engineering");
            assertThat(id).isGreaterThan(0);

            Optional<JdbcOperations.Employee> found = jdbc.findById(id);
            assertThat(found).isPresent();
            assertThat(found.get().name()).isEqualTo("Alice");
        }

        @Test
        @DisplayName("Should insert with PreparedStatement and return generated key")
        void testInsertWithPreparedStatement() throws SQLException {
            int id = jdbc.insertWithPreparedStatement("Bob", "bob@test.com", 85000.0, "Marketing");
            assertThat(id).isGreaterThan(0);

            Optional<JdbcOperations.Employee> found = jdbc.findById(id);
            assertThat(found).isPresent();
            assertThat(found.get().name()).isEqualTo("Bob");
            assertThat(found.get().email()).isEqualTo("bob@test.com");
            assertThat(found.get().salary()).isEqualTo(85000.0);
            assertThat(found.get().department()).isEqualTo("Marketing");
        }

        @Test
        @DisplayName("Should reject duplicate email on insert")
        void testDuplicateEmail() throws SQLException {
            jdbc.insertWithPreparedStatement("Alice", "dup@test.com", 70000.0, "HR");
            assertThatThrownBy(() ->
                    jdbc.insertWithPreparedStatement("Bob", "dup@test.com", 80000.0, "Sales")
            ).isInstanceOf(SQLException.class);
        }

        @Test
        @DisplayName("Should handle null email on insert")
        void testNullEmailInsert() throws SQLException {
            int id = jdbc.insertWithPreparedStatement("NoEmail", null, 50000.0, "Support");
            assertThat(id).isGreaterThan(0);
            Optional<JdbcOperations.Employee> found = jdbc.findById(id);
            assertThat(found).isPresent();
            assertThat(found.get().email()).isNull();
        }
    }

    @Nested
    @DisplayName("Query Operations")
    class QueryTests {

        @Test
        @DisplayName("Should find employee by ID")
        void testFindById() throws SQLException {
            int id = jdbc.insertWithPreparedStatement("Charlie", "charlie@test.com", 90000.0, "Engineering");
            Optional<JdbcOperations.Employee> result = jdbc.findById(id);

            assertThat(result).isPresent();
            assertThat(result.get().name()).isEqualTo("Charlie");
        }

        @Test
        @DisplayName("Should return empty for non-existent ID")
        void testFindByIdNotFound() throws SQLException {
            Optional<JdbcOperations.Employee> result = jdbc.findById(9999);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find employees by department")
        void testFindByDepartment() throws SQLException {
            jdbc.insertWithPreparedStatement("Alice", "alice@test.com", 70000.0, "Engineering");
            jdbc.insertWithPreparedStatement("Bob", "bob@test.com", 80000.0, "Engineering");
            jdbc.insertWithPreparedStatement("Charlie", "charlie@test.com", 60000.0, "Marketing");

            List<JdbcOperations.Employee> engineers = jdbc.findByDepartment("Engineering");
            assertThat(engineers).hasSize(2);
            assertThat(engineers).extracting(JdbcOperations.Employee::name)
                    .containsExactlyInAnyOrder("Alice", "Bob");
        }

        @Test
        @DisplayName("Should return empty list for department with no employees")
        void testFindByDepartmentEmpty() throws SQLException {
            List<JdbcOperations.Employee> result = jdbc.findByDepartment("NonExistent");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find all employees")
        void testFindAll() throws SQLException {
            jdbc.insertWithPreparedStatement("Alice", "alice@test.com", 70000.0, "Engineering");
            jdbc.insertWithPreparedStatement("Bob", "bob@test.com", 80000.0, "Marketing");

            List<JdbcOperations.Employee> all = jdbc.findAll();
            assertThat(all).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when no employees exist")
        void testFindAllEmpty() throws SQLException {
            List<JdbcOperations.Employee> all = jdbc.findAll();
            assertThat(all).isEmpty();
        }
    }

    @Nested
    @DisplayName("Update Operations")
    class UpdateTests {

        @Test
        @DisplayName("Should update employee salary")
        void testUpdateSalary() throws SQLException {
            int id = jdbc.insertWithPreparedStatement("Dave", "dave@test.com", 50000.0, "Sales");
            boolean updated = jdbc.updateSalary(id, 65000.0);

            assertThat(updated).isTrue();
            Optional<JdbcOperations.Employee> found = jdbc.findById(id);
            assertThat(found).isPresent();
            assertThat(found.get().salary()).isEqualTo(65000.0);
        }

        @Test
        @DisplayName("Should return false for updating non-existent employee")
        void testUpdateNonExistent() throws SQLException {
            boolean updated = jdbc.updateSalary(9999, 100000.0);
            assertThat(updated).isFalse();
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteTests {

        @Test
        @DisplayName("Should delete employee by ID")
        void testDeleteById() throws SQLException {
            int id = jdbc.insertWithPreparedStatement("Eve", "eve@test.com", 55000.0, "HR");
            boolean deleted = jdbc.deleteById(id);

            assertThat(deleted).isTrue();
            assertThat(jdbc.findById(id)).isEmpty();
        }

        @Test
        @DisplayName("Should return false for deleting non-existent employee")
        void testDeleteNonExistent() throws SQLException {
            boolean deleted = jdbc.deleteById(9999);
            assertThat(deleted).isFalse();
        }
    }

    @Nested
    @DisplayName("Batch Operations")
    class BatchTests {

        @Test
        @DisplayName("Should batch insert multiple employees")
        void testBatchInsert() throws SQLException {
            List<JdbcOperations.Employee> employees = List.of(
                    new JdbcOperations.Employee(0, "Frank", "frank@test.com", 60000.0, "Engineering", true),
                    new JdbcOperations.Employee(0, "Grace", "grace@test.com", 70000.0, "Marketing", true),
                    new JdbcOperations.Employee(0, "Hank", "hank@test.com", 55000.0, "Sales", false)
            );

            int[] results = jdbc.batchInsert(employees);
            assertThat(results).hasSize(3);

            List<JdbcOperations.Employee> all = jdbc.findAll();
            assertThat(all).hasSize(3);
        }

        @Test
        @DisplayName("Should handle empty batch insert")
        void testEmptyBatchInsert() throws SQLException {
            int[] results = jdbc.batchInsert(List.of());
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("Aggregate and Metadata Operations")
    class AggregateTests {

        @Test
        @DisplayName("Should calculate average salary by department")
        void testAverageSalary() throws SQLException {
            jdbc.insertWithPreparedStatement("Alice", "alice@test.com", 80000.0, "Engineering");
            jdbc.insertWithPreparedStatement("Bob", "bob@test.com", 100000.0, "Engineering");

            Optional<Double> avg = jdbc.getAverageSalaryByDepartment("Engineering");
            assertThat(avg).isPresent();
            assertThat(avg.get()).isEqualTo(90000.0);
        }

        @Test
        @DisplayName("Should return empty for average salary of non-existent department")
        void testAverageSalaryEmpty() throws SQLException {
            Optional<Double> avg = jdbc.getAverageSalaryByDepartment("NonExistent");
            // AVG of no rows is NULL
            assertThat(avg).isEmpty();
        }

        @Test
        @DisplayName("Should retrieve column names from metadata")
        void testGetColumnNames() throws SQLException {
            List<String> columns = jdbc.getColumnNames();
            assertThat(columns).containsExactlyInAnyOrder(
                    "ID", "NAME", "EMAIL", "SALARY", "DEPARTMENT", "ACTIVE"
            );
        }
    }

    @Nested
    @DisplayName("Employee Record Tests")
    class EmployeeRecordTests {

        @Test
        @DisplayName("Should correctly populate Employee record fields")
        void testEmployeeRecordFields() throws SQLException {
            int id = jdbc.insertWithPreparedStatement("Iris", "iris@test.com", 92500.50, "Research");
            Optional<JdbcOperations.Employee> found = jdbc.findById(id);

            assertThat(found).isPresent();
            JdbcOperations.Employee emp = found.get();
            assertThat(emp.id()).isEqualTo(id);
            assertThat(emp.name()).isEqualTo("Iris");
            assertThat(emp.email()).isEqualTo("iris@test.com");
            assertThat(emp.salary()).isEqualTo(92500.50);
            assertThat(emp.department()).isEqualTo("Research");
            assertThat(emp.active()).isTrue();
        }
    }
}
