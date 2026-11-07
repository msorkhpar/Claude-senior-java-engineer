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
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import practice.MeterImport.Reading;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class MeterImportTest {

    private Connection connection;
    /** Every call the import makes on a statement, as "Kind.method", plus each SQL text it sends. */
    private final List<String> calls = new ArrayList<>();
    private final List<String> sqlTexts = new ArrayList<>();

    @BeforeEach
    void openDatabase() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID());
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE meter_reading (meter VARCHAR(40) NOT NULL, wh BIGINT NOT NULL)");
        }
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        connection.close();
    }

    private static List<Reading> readings(int n) {
        return IntStream.range(0, n).mapToObj(i -> new Reading("feeder-7/m" + (100 + i), 4_000L + 13L * i)).toList();
    }

    private int importAll(List<Reading> readings, int chunkSize) {
        return assertTimeoutPreemptively(Duration.ofSeconds(20),
                () -> MeterImport.importAll(recording(), readings, chunkSize));
    }

    private List<Reading> stored() throws SQLException {
        List<Reading> rows = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT meter, wh FROM meter_reading ORDER BY meter, wh")) {
            while (rs.next()) rows.add(new Reading(rs.getString(1), rs.getLong(2)));
        }
        return rows;
    }

    private long count(String call) {
        return calls.stream().filter(call::equals).count();
    }

    @Test
    void storesEveryReadingAndReportsTheCount() throws SQLException {
        List<Reading> input = readings(12);
        int rows = importAll(input, 4);
        assertThat(rows).isEqualTo(12);
        assertThat(stored()).containsExactlyInAnyOrderElementsOf(input);
    }

    @Test
    void anEmptyNightStoresNothing() throws SQLException {
        assertThat(importAll(List.of(), 50)).isZero();
        assertThat(stored()).isEmpty();
    }

    @Test
    void theDriverIsCalledOncePerChunkNotOncePerReading() {
        importAll(readings(30), 10);
        assertThat(count("PreparedStatement.executeUpdate") + count("Statement.executeUpdate")).isZero();
        assertThat(count("PreparedStatement.executeBatch") + count("Statement.executeBatch")).isEqualTo(3);
    }

    @Test
    void everyReadingIsSentWithTheSameSqlText() throws SQLException {
        List<Reading> input = List.of(new Reading("O'Hare-2", 51), new Reading("Main St 4", 52),
                new Reading("x'); DELETE FROM meter_reading; --", 53), new Reading("Main St 5", 54));
        assertThat(importAll(input, 2)).isEqualTo(4);
        assertThat(stored()).containsExactlyInAnyOrderElementsOf(input);
        assertThat(count("Statement.addBatch")).isZero();
        assertThat(sqlTexts).hasSize(1);
        assertThat(sqlTexts.get(0)).doesNotContain("Main St", "O''Hare", "51");
    }

    @Test
    void aLastShortChunkIsStoredToo() throws SQLException {
        List<Reading> input = readings(11);
        assertThat(importAll(input, 4)).isEqualTo(11);
        assertThat(stored()).containsExactlyInAnyOrderElementsOf(input);
    }

    // --- a connection that records what is done with the statements it hands out ---

    private Connection recording() {
        return wrap(Connection.class, "Connection", connection);
    }

    private <T> T wrap(Class<T> type, String kind, T target) {
        Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {type},
                (self, method, args) -> {
                    calls.add(kind + "." + method.getName());
                    if (args != null && args.length > 0 && args[0] instanceof String sql
                            && (kind.equals("Connection") || method.getName().startsWith("execute")
                                || method.getName().equals("addBatch"))) {
                        sqlTexts.add(sql);
                    }
                    Object result;
                    try {
                        result = method.invoke(target, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                    if (result instanceof PreparedStatement ps) return wrap(PreparedStatement.class, "PreparedStatement", ps);
                    if (result instanceof Statement st && kind.equals("Connection")) return wrap(Statement.class, "Statement", st);
                    return result;
                });
        return type.cast(proxy);
    }
}
