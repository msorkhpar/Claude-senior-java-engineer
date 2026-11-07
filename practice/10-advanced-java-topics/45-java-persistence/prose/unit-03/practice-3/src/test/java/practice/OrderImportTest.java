package practice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import practice.OrderImport.Order;

import static org.assertj.core.api.Assertions.assertThat;

class OrderImportTest {

    private Connection importer;
    /** A second connection: it sees only what was committed. */
    private Connection observer;

    @BeforeEach
    void openDatabase() throws SQLException {
        String url = "jdbc:h2:mem:" + UUID.randomUUID();
        importer = DriverManager.getConnection(url);
        observer = DriverManager.getConnection(url);
        try (Statement s = observer.createStatement()) {
            s.execute("CREATE TABLE orders (id INT PRIMARY KEY)");
            s.execute("CREATE TABLE order_line (order_id INT NOT NULL, sku VARCHAR(12) NOT NULL)");
        }
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        importer.close();
        observer.close();
    }

    private static Order order(int id, String... skus) {
        return new Order(id, Arrays.asList(skus));
    }

    private List<Integer> importNight(List<Order> orders) throws Exception {
        FutureTask<List<Integer>> task = new FutureTask<>(() -> new OrderImport().importAll(importer, orders));
        Thread t = new Thread(task, "import");
        t.setDaemon(true);
        t.start();
        return task.get(15, TimeUnit.SECONDS);
    }

    private List<Integer> committedOrders() throws SQLException {
        List<Integer> ids = new ArrayList<>();
        try (Statement s = observer.createStatement(); ResultSet r = s.executeQuery("SELECT id FROM orders ORDER BY id")) {
            while (r.next()) ids.add(r.getInt(1));
        }
        return ids;
    }

    private List<String> committedLines(int orderId) throws SQLException {
        List<String> skus = new ArrayList<>();
        try (PreparedStatement p = observer.prepareStatement("SELECT sku FROM order_line WHERE order_id = ? ORDER BY sku")) {
            p.setInt(1, orderId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) skus.add(r.getString(1));
            }
        }
        return skus;
    }

    @Test
    void aCleanNightImportsEveryOrderWithItsLines() throws Exception {
        List<Integer> imported = importNight(List.of(order(301, "BOLT-6", "NUT-6"), order(302, "WASHER-8")));

        assertThat(imported).containsExactly(301, 302);
        assertThat(committedOrders()).containsExactly(301, 302);
        assertThat(committedLines(301)).containsExactly("BOLT-6", "NUT-6");
        assertThat(committedLines(302)).containsExactly("WASHER-8");
    }

    @Test
    void anOrderWithARefusedLineLeavesNoLinesBehind() throws Exception {
        List<Integer> imported = importNight(List.of(order(411, "HINGE-2", "SCREW-4", null, "PLATE-1")));

        assertThat(imported).isEmpty();
        assertThat(committedLines(411)).isEmpty();
    }

    @Test
    void ordersBeforeARefusedOneAreStillImported() throws Exception {
        List<Integer> imported = importNight(List.of(
                order(521, "CLAMP-3"), order(522, "PIPE-15", null), order(523, "VALVE-22")));

        assertThat(imported).containsExactly(521, 523);
        assertThat(committedOrders()).containsExactly(521, 523);
        assertThat(committedLines(521)).containsExactly("CLAMP-3");
        assertThat(committedLines(523)).containsExactly("VALVE-22");
    }

    @Test
    void anOrderWithARefusedLineLeavesNoOrderBehind() throws Exception {
        List<Integer> imported = importNight(List.of(order(631, "TAPE-50", "GLUE-1", "OVERLONG-SKU-NAME"), order(632, "CORD-2")));

        assertThat(imported).containsExactly(632);
        assertThat(committedOrders()).containsExactly(632);
        assertThat(committedLines(631)).isEmpty();
    }
}
