package practice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class LockedTransfer {

    private LockedTransfer() {
    }

    /** Locks both rows (lower id first), then moves the amount; false if nothing changed. */
    public static boolean transfer(Connection connection, int fromId, int toId, BigDecimal amount) throws SQLException {
        connection.setAutoCommit(false);
        try {
            Map<Integer, BigDecimal> locked = new HashMap<>();
            try (PreparedStatement lock = connection.prepareStatement(
                    "SELECT id, balance FROM accounts WHERE id = ? FOR UPDATE")) {
                for (int id : new int[] {Math.min(fromId, toId), Math.max(fromId, toId)}) {
                    lock.setInt(1, id);
                    try (ResultSet rows = lock.executeQuery()) {
                        if (rows.next()) {
                            locked.put(id, rows.getBigDecimal("balance"));
                        }
                    }
                }
            }
            if (!locked.containsKey(fromId) || !locked.containsKey(toId)
                    || locked.get(fromId).compareTo(amount) < 0) {
                connection.rollback();
                return false;
            }
            move(connection, fromId, amount.negate());
            move(connection, toId, amount);
            connection.commit();
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private static void move(Connection connection, int id, BigDecimal delta) throws SQLException {
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE accounts SET balance = balance + ? WHERE id = ?")) {
            update.setBigDecimal(1, delta);
            update.setInt(2, id);
            update.executeUpdate();
        }
    }
}
