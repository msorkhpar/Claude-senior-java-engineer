package practice;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import practice.CategoryReport.CategoryProducts;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryReportTest {

    private Connection connection;
    private int queries;

    @BeforeEach
    void openDatabase() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID());
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE categories (id INT PRIMARY KEY, name VARCHAR(100))");
            statement.execute("CREATE TABLE products (id INT, name VARCHAR(100), category_id INT)");
            statement.execute("INSERT INTO categories VALUES (3, 'Toys'), (1, 'Books'), (2, 'Pens')");
            statement.execute("INSERT INTO products VALUES (3, 'Emma', 1), (4, 'Kite', 3), (1, 'Dune', 1), (2, 'Biro', 2)");
        }
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        connection.close();
    }

    private void addEmptyCategory() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO categories VALUES (4, 'Maps')");
        }
    }

    /** The real connection; every query run through its statements is counted. */
    private Connection counted() {
        return counting(connection, Connection.class);
    }

    private <T> T counting(T target, Class<T> type) {
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getName().startsWith("execute")) {
                queries++;
            }
            Object result;
            try {
                result = method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
            if (result instanceof PreparedStatement statement) {
                return counting(statement, PreparedStatement.class);
            }
            if (result instanceof Statement statement) {
                return counting(statement, Statement.class);
            }
            return result;
        };
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler));
    }

    @Test
    void groupsProductsUnderTheirCategories() throws SQLException {
        assertThat(CategoryReport.load(counted())).containsExactly(
                new CategoryProducts("Books", List.of("Dune", "Emma")),
                new CategoryProducts("Pens", List.of("Biro")),
                new CategoryProducts("Toys", List.of("Kite")));
    }

    @Test
    void runsOneQueryForAllCategories() throws SQLException {
        CategoryReport.load(counted());

        assertThat(queries).isEqualTo(1);
    }

    @Test
    void aCategoryWithoutProductsIsKept() throws SQLException {
        addEmptyCategory();

        List<CategoryProducts> report = CategoryReport.load(counted());

        assertThat(report).extracting(CategoryProducts::category).containsExactly("Books", "Pens", "Toys", "Maps");
    }

    @Test
    void aCategoryWithoutProductsHasAnEmptyList() throws SQLException {
        addEmptyCategory();

        List<CategoryProducts> report = CategoryReport.load(counted());

        assertThat(report).hasSize(4);
        assertThat(report.get(3).products()).isEmpty();
    }
}
