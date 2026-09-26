package practice;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import practice.Catalog.Category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalogTest {

    private Connection connection;
    private int queries;

    @BeforeEach
    void openDatabase() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID());
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE categories (id INT PRIMARY KEY, name VARCHAR(100))");
            statement.execute("CREATE TABLE products (id INT PRIMARY KEY, name VARCHAR(100), category_id INT)");
            statement.execute("INSERT INTO categories VALUES (1, 'Books'), (2, 'Pens')");
            statement.execute("INSERT INTO products VALUES (3, 'Emma', 1), (1, 'Dune', 1), (2, 'Biro', 2)");
        }
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        connection.close();
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
    void loadsACategoryAndItsProducts() throws SQLException {
        Category books = new Catalog(counted()).category(1).orElseThrow();

        assertThat(books.name()).isEqualTo("Books");
        assertThat(books.products()).containsExactly("Dune", "Emma");
    }

    @Test
    void productsAreNotQueriedUntilAsked() throws SQLException {
        new Catalog(counted()).category(1).orElseThrow();

        assertThat(queries).isEqualTo(1);
    }

    @Test
    void productsAreQueriedOnlyOnce() throws SQLException {
        Category books = new Catalog(counted()).category(1).orElseThrow();

        books.products();
        assertThat(books.products()).containsExactly("Dune", "Emma");
        assertThat(queries).isEqualTo(2);
    }

    @Test
    void firstAccessAfterCloseThrows() throws SQLException {
        Catalog catalog = new Catalog(counted());
        Category books = catalog.category(1).orElseThrow();

        catalog.close();

        assertThatThrownBy(books::products).isInstanceOf(Catalog.LazyInitializationException.class);
    }

    @Test
    void productsLoadedBeforeCloseStayReadable() throws SQLException {
        Catalog catalog = new Catalog(counted());
        Category books = catalog.category(1).orElseThrow();
        books.products();

        catalog.close();

        assertThat(books.products()).containsExactly("Dune", "Emma");
        assertThat(connection.isClosed()).isFalse();
    }
}
