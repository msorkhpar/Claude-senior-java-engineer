package practice;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import practice.ProductSession.Product;

import static org.assertj.core.api.Assertions.assertThat;

class ProductSessionTest {

    private Connection connection;
    private int queries;

    @BeforeEach
    void openDatabase() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID());
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE products (id INT PRIMARY KEY, name VARCHAR(100), price DECIMAL(10,2))");
            statement.execute("INSERT INTO products VALUES (7, 'Pen', 1.50), (1000, 'Desk', 250.00)");
        }
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        connection.close();
    }

    /** The real connection; every query run through it or its statements is counted. */
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
            if (result instanceof Statement statement && !(result instanceof ResultSet)) {
                return counting(statement, Statement.class);
            }
            return result;
        };
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler));
    }

    @Test
    void findingAnIdTwiceGivesTheSameInstance() throws SQLException {
        ProductSession session = new ProductSession(counted());

        Product first = session.find(7).orElseThrow();
        Product second = session.find(7).orElseThrow();

        assertThat(second).isSameAs(first);
        assertThat(first.name()).isEqualTo("Pen");
        assertThat(first.price()).isEqualByComparingTo("1.50");
        assertThat(session.find(8)).isEmpty();
    }

    @Test
    void theSecondFindRunsNoQuery() throws SQLException {
        ProductSession session = new ProductSession(counted());

        session.find(7);
        session.find(7);

        assertThat(queries).isEqualTo(1);
    }

    @Test
    void largeIdsAreCachedByValue() throws SQLException {
        ProductSession session = new ProductSession(counted());

        Product first = session.find(Integer.parseInt("1000")).orElseThrow();
        Product second = session.find(Integer.parseInt("1000")).orElseThrow();

        assertThat(second).isSameAs(first);
    }

    @Test
    void twoSessionsDoNotShareInstances() throws SQLException {
        Product fromFirst = new ProductSession(counted()).find(7).orElseThrow();
        Product fromSecond = new ProductSession(counted()).find(7).orElseThrow();

        assertThat(fromSecond).isNotSameAs(fromFirst);
        assertThat(queries).isEqualTo(2);
    }
}
