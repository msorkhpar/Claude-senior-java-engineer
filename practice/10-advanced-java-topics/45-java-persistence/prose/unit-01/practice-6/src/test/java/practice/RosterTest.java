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
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RosterTest {

    private Connection connection;
    private final List<String> calls = new ArrayList<>();

    @BeforeEach
    void openDatabase() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID());
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE employees (id INT PRIMARY KEY, name VARCHAR(100), department VARCHAR(50))");
            statement.execute("INSERT INTO employees VALUES (4, 'Barbara', 'Engineering'), (1, 'Ada', 'Engineering'), "
                    + "(2, 'Grace', 'Sales'), (3, 'Linus', 'Engineering')");
        }
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        connection.close();
    }

    /** The real connection, with every call on it and on its statements recorded as "Type.method". */
    private Connection spied() {
        return spy(connection, Connection.class);
    }

    private <T> T spy(T target, Class<T> type) {
        InvocationHandler handler = (proxy, method, args) -> {
            calls.add(type.getSimpleName() + "." + method.getName());
            Object result;
            try {
                result = method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
            if (result instanceof PreparedStatement statement) {
                return spy(statement, PreparedStatement.class);
            }
            if (result instanceof ResultSet rows) {
                return spy(rows, ResultSet.class);
            }
            return result;
        };
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler));
    }

    @Test
    void countsAndPicksByPosition() throws SQLException {
        assertThat(Roster.count(spied(), "Engineering")).isEqualTo(3);
        assertThat(Roster.count(spied(), "Legal")).isZero();
        assertThat(Roster.nameAt(spied(), "Engineering", 1)).isEqualTo(Optional.of("Ada"));
        assertThat(Roster.nameAt(spied(), "Engineering", 2)).isEqualTo(Optional.of("Linus"));
        assertThat(Roster.nameAt(spied(), "Engineering", 3)).isEqualTo(Optional.of("Barbara"));
    }

    @Test
    void countMovesToTheLastRowInsteadOfStepping() throws SQLException {
        Roster.count(spied(), "Engineering");

        assertThat(calls).contains("ResultSet.last").doesNotContain("ResultSet.next");
    }

    @Test
    void aPositionPastTheEndIsEmpty() throws SQLException {
        assertThat(Roster.nameAt(spied(), "Engineering", 4)).isEmpty();
    }

    @Test
    void aPositionBelowOneIsRefused() {
        assertThatThrownBy(() -> Roster.nameAt(spied(), "Engineering", -1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Roster.nameAt(spied(), "Engineering", 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
