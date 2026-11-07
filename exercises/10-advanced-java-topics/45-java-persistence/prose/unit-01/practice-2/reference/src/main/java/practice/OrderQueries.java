package practice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Queries behind the support screen. Each method is lent the request's connection, which the
 * caller goes on using after the method returns.
 */
public final class OrderQueries {

    /** One open order line as the support screen shows it. */
    public record OpenOrder(long id, String item, int quantity) {}

    static final String OPEN_ORDERS = """
            SELECT id, item, quantity
            FROM orders
            WHERE customer = ? AND status = 'OPEN'
            ORDER BY id
            """;

    private OrderQueries() {}

    /** The customer's open orders, oldest first. */
    public static List<OpenOrder> openOrders(Connection conn, String customer) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(OPEN_ORDERS)) {
            ps.setString(1, customer);
            try (ResultSet rs = ps.executeQuery()) {
                List<OpenOrder> orders = new ArrayList<>();
                while (rs.next()) orders.add(read(rs));
                return orders;
            }
        }
    }

    /** Reads the current row. The quantity column is text written by an old loader. */
    static OpenOrder read(ResultSet rs) throws SQLException {
        return new OpenOrder(rs.getLong("id"), rs.getString("item"), rs.getInt("quantity"));
    }
}
