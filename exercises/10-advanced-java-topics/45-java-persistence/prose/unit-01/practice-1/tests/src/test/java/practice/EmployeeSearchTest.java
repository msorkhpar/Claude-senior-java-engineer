package practice;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import practice.EmployeeSearch.Employee;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeSearchTest {

    private Connection connection;
    private final List<String> sqlSent = new ArrayList<>();

    @BeforeEach
    void openDatabase() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID());
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE employees (id INT PRIMARY KEY, name VARCHAR(100), "
                    + "department VARCHAR(50), salary DECIMAL(10,2))");
        }
        insert(1, "Ada", "Engineering", "90000.00");
        insert(2, "Linus", "Engineering", "80000.00");
        insert(3, "Grace", "Sales", "95000.00");
        insert(4, "O'Brien", "Sales", "70000.00");
        insert(5, "Barbara", "Engineering", "85000.50");
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        connection.close();
    }

    private void insert(int id, String name, String department, String salary) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO employees VALUES (?, ?, ?, ?)")) {
            statement.setInt(1, id);
            statement.setString(2, name);
            statement.setString(3, department);
            statement.setBigDecimal(4, new BigDecimal(salary));
            statement.executeUpdate();
        }
    }

    /** The connection, recording every SQL text a statement is made from or runs. */
    private Connection recording() {
        return record(Connection.class, connection);
    }

    private <T> T record(Class<T> type, T target) {
        Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {type},
                (self, method, args) -> {
                    if (args != null && args.length > 0 && args[0] instanceof String sql) {
                        sqlSent.add(sql);
                    }
                    Object result = call(method, target, args);
                    if (type == Connection.class && result instanceof Statement plain
                            && !(result instanceof PreparedStatement)) {
                        return record(Statement.class, plain);
                    }
                    return result;
                });
        return type.cast(proxy);
    }

    private static Object call(Method method, Object target, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private void noSqlContains(String... values) {
        assertThat(sqlSent).isNotEmpty();
        for (String value : values) {
            assertThat(sqlSent).as("SQL text holding " + value).noneMatch(sql -> sql.contains(value));
        }
    }

    private static List<String> names(List<Employee> employees) {
        return employees.stream().map(Employee::name).toList();
    }

    @Test
    void findsTheDepartmentAboveASalary() throws SQLException {
        List<Employee> found = new EmployeeSearch(recording())
                .inDepartmentAbove("Engineering", new BigDecimal("80000.10"));

        assertThat(names(found)).containsExactly("Ada", "Barbara");
        assertThat(found.get(1).salary()).isEqualByComparingTo("85000.50");
        assertThat(names(new EmployeeSearch(recording()).byName("Grace"))).containsExactly("Grace");
        noSqlContains("Engineering", "80000");
    }

    @Test
    void anInjectionAttemptIsJustAName() throws SQLException {
        String attack = String.join("", "' OR '1'='1");

        assertThat(new EmployeeSearch(recording()).byName(attack)).isEmpty();
        noSqlContains("1'='1", "1''=''1");
    }

    @Test
    void aNameWithAnApostropheIsFound() throws SQLException {
        String name = new StringBuilder("O'").append("Brien").toString();

        assertThat(names(new EmployeeSearch(recording()).byName(name))).containsExactly("O'Brien");
        noSqlContains("Brien");
    }

    @Test
    void aSalaryEqualToTheMinimumIsLeftOut() throws SQLException {
        List<Employee> found = new EmployeeSearch(recording())
                .inDepartmentAbove("Engineering", new BigDecimal("80000.00"));

        assertThat(names(found)).containsExactly("Ada", "Barbara");
        noSqlContains("80000");
    }
}
