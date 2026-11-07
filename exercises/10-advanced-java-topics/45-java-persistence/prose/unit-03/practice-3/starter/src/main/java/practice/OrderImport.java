package practice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.List;

/**
 * Imports the partner shop's orders every night, in one transaction. An order is imported with all
 * of its lines or not at all; an order the database refuses is left out and reported, and the rest
 * of the night's orders are still imported.
 *
 * <p>Tables: {@code orders(id INT PRIMARY KEY)} and
 * {@code order_line(order_id INT NOT NULL, sku VARCHAR(12) NOT NULL)}.
 */
public class OrderImport {

    public record Order(int id, List<String> skus) { }

    /** Imports the night's orders and returns the ids of those imported, in input order. */
    public List<Integer> importAll(Connection c, List<Order> orders) throws SQLException {
        c.setAutoCommit(false);
        List<Integer> imported = new ArrayList<>();
        for (Order order : orders) {
            if (importOne(c, order)) imported.add(order.id());
        }
        c.commit();
        return imported;
    }

    /** Writes one order and its lines. True if the order is in the import, false if it was left out. */
    boolean importOne(Connection c, Order order) throws SQLException {
        // TODO: Write the order and its lines with insertOrder and insertLine. When the database
        //       refuses any of them, the order is left out of the night's import.
        throw new IllegalStateException("TODO: importOne");
    }

    void insertOrder(Connection c, int orderId) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("INSERT INTO orders VALUES (?)")) {
            p.setInt(1, orderId);
            p.executeUpdate();
        }
    }

    void insertLine(Connection c, int orderId, String sku) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("INSERT INTO order_line VALUES (?, ?)")) {
            p.setInt(1, orderId);
            p.setString(2, sku);
            p.executeUpdate();
        }
    }
}
