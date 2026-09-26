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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NameQueryTest {

    private Connection connection;
    /** Every statement and result set the code under test opened (the real objects). */
    private final List<AutoCloseable> opened = new ArrayList<>();
    /** When > 0, the n-th getString call on a result set throws. */
    private int failOnGetString;
    private int getStringCalls;

    @BeforeEach
    void openDatabase() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID());
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE employees (id INT PRIMARY KEY, name VARCHAR(100), department VARCHAR(50))");
            statement.execute("INSERT INTO employees VALUES (1, 'Ada', 'Engineering'), (2, 'Grace', 'Sales'), "
                    + "(3, 'Linus', 'Engineering')");
        }
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        connection.close();
    }

    private Connection lent() {
        return watch(connection, Connection.class);
    }

    private <T> T watch(T target, Class<T> type) {
        InvocationHandler handler = (proxy, method, args) -> {
            if (type == ResultSet.class && method.getName().equals("getString")
                    && ++getStringCalls == failOnGetString) {
                throw new SQLException("disk read failed");
            }
            Object result;
            try {
                result = method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
            if (result instanceof PreparedStatement statement) {
                opened.add(statement);
                return watch(statement, PreparedStatement.class);
            }
            if (result instanceof ResultSet rows) {
                opened.add(rows);
                return watch(rows, ResultSet.class);
            }
            return result;
        };
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler));
    }

    private boolean allClosed() throws SQLException {
        for (AutoCloseable resource : opened) {
            boolean closed = resource instanceof ResultSet rows ? rows.isClosed() : ((Statement) resource).isClosed();
            if (!closed) {
                return false;
            }
        }
        return true;
    }

    @Test
    void returnsTheDepartmentsNames() throws SQLException {
        assertThat(NameQuery.names(lent(), "Engineering")).containsExactly("Ada", "Linus");
    }

    @Test
    void theStatementIsClosedAfterwards() throws SQLException {
        NameQuery.names(lent(), "Engineering");

        assertThat(opened).isNotEmpty();
        assertThat(allClosed()).isTrue();
    }

    @Test
    void theyAreClosedEvenWhenReadingFails() throws SQLException {
        failOnGetString = 2;

        assertThatThrownBy(() -> NameQuery.names(lent(), "Engineering"))
                .isInstanceOf(SQLException.class).hasMessageContaining("disk read failed");
        assertThat(opened).isNotEmpty();
        assertThat(allClosed()).isTrue();
    }

    @Test
    void theCallersConnectionStaysOpen() throws SQLException {
        NameQuery.names(lent(), "Engineering");

        assertThat(connection.isClosed()).isFalse();
    }
}
