package practice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class Bank {

    private Bank() {
    }

    /** Moves the amount in one transaction; true if it moved, false if nothing changed. */
    public static boolean transfer(Connection connection, int fromId, int toId, BigDecimal amount) throws SQLException {
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative: " + amount);
        }
        connection.setAutoCommit(false);
        try (PreparedStatement debit = connection.prepareStatement(
                     "UPDATE accounts SET balance = balance - ? WHERE id = ? AND balance >= ?");
             PreparedStatement credit = connection.prepareStatement(
                     "UPDATE accounts SET balance = balance + ? WHERE id = ?")) {
            debit.setBigDecimal(1, amount);
            debit.setInt(2, fromId);
            debit.setBigDecimal(3, amount);
            if (debit.executeUpdate() == 0) {
                connection.rollback();
                return false;
            }
            credit.setBigDecimal(1, amount);
            credit.setInt(2, toId);
            if (credit.executeUpdate() == 0) {
                connection.rollback();
                return false;
            }
            connection.commit();
            connection.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }
}
