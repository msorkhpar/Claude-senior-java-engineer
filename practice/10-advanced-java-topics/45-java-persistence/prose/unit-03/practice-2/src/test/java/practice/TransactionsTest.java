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
import static org.assertj.core.api.Assertions.catchThrowable;

class TransactionsTest {

    private Connection connection;
    /** A second connection: it sees only what was committed. */
    private Connection observer;

    @BeforeEach
    void openDatabase() throws SQLException {
        String url = "jdbc:h2:mem:" + UUID.randomUUID();
        connection = DriverManager.getConnection(url);
        observer = DriverManager.getConnection(url);
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE items (id INT PRIMARY KEY)");
        }
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        observer.close();
        connection.close();
    }

    private static void insert(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO items VALUES (?)")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private List<Integer> committedIds() throws SQLException {
        List<Integer> ids = new ArrayList<>();
        try (Statement statement = observer.createStatement();
             ResultSet rows = statement.executeQuery("SELECT id FROM items ORDER BY id")) {
            while (rows.next()) {
                ids.add(rows.getInt(1));
            }
        }
        return ids;
    }

    /** The real connection, except that rollback() fails. */
    private Connection failingRollback() {
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getName().equals("rollback") && (args == null || args.length == 0)) {
                throw new SQLException("rollback failed");
            }
            try {
                return method.invoke(connection, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                new Class<?>[] {Connection.class}, handler);
    }

    @Test
    void commitsTheWorkAndRestoresAutoCommit() throws SQLException {
        Transactions.inTransaction(connection, c -> {
            insert(c, 1);
            insert(c, 2);
        });

        assertThat(committedIds()).containsExactly(1, 2);
        assertThat(connection.getAutoCommit()).isTrue();
    }

    @Test
    void aFailingRollbackIsSuppressedNotThrown() {
        Throwable thrown = catchThrowable(() -> Transactions.inTransaction(failingRollback(), c -> {
            insert(c, 1);
            throw new SQLException("insert failed");
        }));

        assertThat(thrown).isInstanceOf(SQLException.class).hasMessage("insert failed");
        assertThat(thrown.getSuppressed()).extracting(Throwable::getMessage).containsExactly("rollback failed");
    }

    @Test
    void aRuntimeExceptionAlsoRollsBack() throws SQLException {
        Throwable thrown = catchThrowable(() -> Transactions.inTransaction(connection, c -> {
            insert(c, 1);
            throw new IllegalStateException("bad state");
        }));

        assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("bad state");
        assertThat(committedIds()).isEmpty();
    }

    @Test
    void autoCommitIsRestoredAfterAFailure() throws SQLException {
        Throwable thrown = catchThrowable(() -> Transactions.inTransaction(connection, c -> {
            insert(c, 1);
            throw new SQLException("insert failed");
        }));

        assertThat(thrown).hasMessage("insert failed");
        assertThat(connection.getAutoCommit()).isTrue();
        assertThat(committedIds()).isEmpty();
    }
}
