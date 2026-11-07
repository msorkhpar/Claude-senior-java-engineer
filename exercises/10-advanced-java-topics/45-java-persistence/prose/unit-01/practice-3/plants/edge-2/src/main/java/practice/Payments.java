package practice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Card payments against wallet(id BIGINT, balance DECIMAL(12,2)): the wallet is debited and a row
 * goes into payment(wallet_id, amount, reference VARCHAR(16)). The connection is lent by the
 * caller, a request handler that goes on using it, in the mode it had, after the payment.
 */
public final class Payments {

    private Payments() {}

    /** Takes the amount from the wallet and records the payment, both or neither. */
    public static void pay(Connection conn, long wallet, BigDecimal amount, String reference) throws SQLException {
        boolean autoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            if (debit(conn, wallet, amount) == 0) throw new SQLException("no wallet " + wallet);
            record(conn, wallet, amount, reference);
            conn.commit();
            conn.setAutoCommit(autoCommit);
        } catch (SQLException | RuntimeException e) {
            conn.rollback();
            throw e;
        }
    }

    /** Takes the amount from the wallet; returns the update count. */
    static int debit(Connection conn, long wallet, BigDecimal amount) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE wallet SET balance = balance - ? WHERE id = ?")) {
            ps.setBigDecimal(1, amount);
            ps.setLong(2, wallet);
            return ps.executeUpdate();
        }
    }

    /** Inserts the payment row. */
    static void record(Connection conn, long wallet, BigDecimal amount, String reference) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO payment (wallet_id, amount, reference) VALUES (?, ?, ?)")) {
            ps.setLong(1, wallet);
            ps.setBigDecimal(2, amount);
            ps.setString(3, reference);
            ps.executeUpdate();
        }
    }
}
