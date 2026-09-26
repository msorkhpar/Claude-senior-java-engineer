package practice;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LockedTransferTest {

    private Connection connection;
    /** A second connection: it sees only what was committed. */
    private Connection observer;
    /** Each query run through the spied connection: its SQL (upper case) and its first int parameter. */
    private final List<String> queries = new ArrayList<>();

    @BeforeEach
    void openDatabase() throws SQLException {
        String url = "jdbc:h2:mem:" + UUID.randomUUID();
        connection = DriverManager.getConnection(url);
        observer = DriverManager.getConnection(url);
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE accounts (id INT PRIMARY KEY, balance DECIMAL(15,2))");
            statement.execute("INSERT INTO accounts VALUES (1, 100.00), (2, 50.00)");
        }
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        observer.close();
        connection.close();
    }

    private Connection spied() {
        InvocationHandler handler = (proxy, method, args) -> {
            Object result;
            try {
                result = method.invoke(connection, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
            if (result instanceof PreparedStatement statement && method.getName().equals("prepareStatement")) {
                return recording(statement, ((String) args[0]).toUpperCase(Locale.ROOT).replaceAll("\\s+", " ").trim());
            }
            if (result instanceof Statement statement && method.getName().equals("createStatement")) {
                return recording(statement);
            }
            return result;
        };
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                new Class<?>[] {Connection.class}, handler);
    }

    private PreparedStatement recording(PreparedStatement target, String sql) {
        Object[] firstInt = new Object[1];
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getName().equals("setInt") && (Integer) args[0] == 1) {
                firstInt[0] = args[1];
            }
            if (method.getName().equals("executeQuery")) {
                queries.add(sql + " #" + firstInt[0]);
            }
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };
        return (PreparedStatement) Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(),
                new Class<?>[] {PreparedStatement.class}, handler);
    }

    private Statement recording(Statement target) {
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getName().equals("executeQuery")) {
                queries.add(((String) args[0]).toUpperCase(Locale.ROOT).replaceAll("\\s+", " ").trim());
            }
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };
        return (Statement) Proxy.newProxyInstance(Statement.class.getClassLoader(),
                new Class<?>[] {Statement.class}, handler);
    }

    private BigDecimal committedBalance(int id) throws SQLException {
        try (PreparedStatement statement = observer.prepareStatement("SELECT balance FROM accounts WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getBigDecimal(1);
            }
        }
    }

    private List<String> lockedIds() {
        return queries.stream().filter(q -> q.contains("FOR UPDATE")).map(q -> q.substring(q.indexOf('#') + 1)).toList();
    }

    @Test
    void movesMoneyUnderRowLocks() throws SQLException {
        assertThat(LockedTransfer.transfer(spied(), 2, 1, new BigDecimal("30.00"))).isTrue();

        assertThat(committedBalance(1)).isEqualByComparingTo("130.00");
        assertThat(committedBalance(2)).isEqualByComparingTo("20.00");
        assertThat(LockedTransfer.transfer(spied(), 2, 1, new BigDecimal("60.00"))).isFalse();
        assertThat(committedBalance(2)).isEqualByComparingTo("20.00");
        assertThat(LockedTransfer.transfer(spied(), 2, 1, new BigDecimal("20.00"))).isTrue();
        assertThat(committedBalance(1)).isEqualByComparingTo("150.00");
        assertThat(committedBalance(2)).isEqualByComparingTo("0.00");
        assertThat(connection.getAutoCommit()).isTrue();
    }

    @Test
    void locksTheLowerIdFirst() throws SQLException {
        LockedTransfer.transfer(spied(), 2, 1, new BigDecimal("30.00"));

        assertThat(lockedIds()).containsExactly("1", "2");
    }

    @Test
    void theBalanceIsReadUnderTheLock() throws SQLException {
        LockedTransfer.transfer(spied(), 2, 1, new BigDecimal("30.00"));

        assertThat(queries).isNotEmpty().allMatch(q -> q.contains("FOR UPDATE"));
    }

    @Test
    void aMissingAccountChangesNothing() throws SQLException {
        assertThat(LockedTransfer.transfer(spied(), 1, 99, new BigDecimal("30.00"))).isFalse();

        assertThat(committedBalance(1)).isEqualByComparingTo("100.00");
    }
}
