package practice;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import practice.OrderQueries.OpenOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class OrderQueriesTest {

    private Connection connection;
    /** The real statements and result sets the method was handed, to check afterwards. */
    private final List<Statement> statements = new ArrayList<>();
    private final List<ResultSet> resultSets = new ArrayList<>();

    @BeforeEach
    void openDatabase() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID());
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE orders (id BIGINT PRIMARY KEY, customer VARCHAR(20), item VARCHAR(40), "
                    + "quantity VARCHAR(10), status VARCHAR(10))");
            st.execute("INSERT INTO orders VALUES "
                    + "(41, 'k-208', 'toner 85A', '2', 'OPEN'), "
                    + "(42, 'k-311', 'desk lamp', '1', 'OPEN'), "
                    + "(57, 'k-208', 'USB-C dock', '1', 'SHIPPED'), "
                    + "(63, 'k-208', 'paper A4', '10', 'OPEN'), "
                    + "(70, 'k-950', 'cable tray', '4', 'OPEN'), "
                    + "(71, 'k-950', 'cable ties', 'a box', 'OPEN')");
        }
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        if (!connection.isClosed()) connection.close();
    }

    private List<OpenOrder> openOrders(String customer) {
        return assertTimeoutPreemptively(Duration.ofSeconds(20),
                () -> OrderQueries.openOrders(lent(), customer));
    }

    private void assertEverythingOpenedIsClosed() throws SQLException {
        assertThat(statements).isNotEmpty();
        for (Statement s : statements) assertThat(s.isClosed()).as("statement closed").isTrue();
        for (ResultSet r : resultSets) assertThat(r.isClosed()).as("result set closed").isTrue();
    }

    @Test
    void returnsTheCustomersOpenOrdersOldestFirst() {
        assertThat(openOrders("k-208")).containsExactly(
                new OpenOrder(41, "toner 85A", 2), new OpenOrder(63, "paper A4", 10));
    }

    @Test
    void aCustomerWithoutOpenOrdersGetsAnEmptyList() {
        assertThat(openOrders("k-404")).isEmpty();
    }

    @Test
    void closesWhatItOpenedOnceTheRowsAreRead() throws SQLException {
        openOrders("k-311");
        assertEverythingOpenedIsClosed();
    }

    @Test
    void theCallerCanKeepUsingItsConnection() throws SQLException {
        openOrders("k-208");
        assertThat(connection.isClosed()).isFalse();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM orders")) {
            rs.next();
            assertThat(rs.getInt(1)).isEqualTo(6);
        }
    }

    @Test
    void aRowThatCannotBeReadStillClosesTheStatement() {
        assertThatThrownBy(() -> openOrders("k-950")).isInstanceOf(SQLException.class);
        assertThat(statements).isNotEmpty();
        for (Statement s : statements) {
            assertThat(isClosed(s)).as("statement closed after the failed read").isTrue();
        }
    }

    private static boolean isClosed(Statement s) {
        try {
            return s.isClosed();
        } catch (SQLException e) {
            throw new AssertionError(e);
        }
    }

    // --- the connection the method is lent: it remembers the statements and result sets it hands out ---

    private Connection lent() {
        return wrap(Connection.class, connection);
    }

    private <T> T wrap(Class<T> type, T target) {
        Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {type},
                (self, method, args) -> {
                    Object result;
                    try {
                        result = method.invoke(target, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                    if (result instanceof PreparedStatement ps) {
                        statements.add(ps);
                        return wrap(PreparedStatement.class, ps);
                    }
                    if (result instanceof Statement st) {
                        statements.add(st);
                        return wrap(Statement.class, st);
                    }
                    if (result instanceof ResultSet rs) {
                        resultSets.add(rs);
                    }
                    return result;
                });
        return type.cast(proxy);
    }
}
