package practice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class Isolation {

    private Isolation() {
    }

    public record Reads(BigDecimal first, BigDecimal second) {
    }

    /** Reads the balance twice in one transaction at the given level, running {@code between} in the middle. */
    public static Reads readTwice(Connection reader, int level, int accountId, Runnable between) throws SQLException {
        int previousLevel = reader.getTransactionIsolation();
        boolean previousAutoCommit = reader.getAutoCommit();
        reader.setTransactionIsolation(Math.max(level, Connection.TRANSACTION_READ_COMMITTED));
        reader.setAutoCommit(false);
        try (PreparedStatement statement = reader.prepareStatement("SELECT balance FROM accounts WHERE id = ?")) {
            statement.setInt(1, accountId);
            BigDecimal first = balance(statement);
            between.run();
            BigDecimal second = balance(statement);
            reader.commit();
            return new Reads(first, second);
        } catch (SQLException | RuntimeException e) {
            reader.rollback();
            throw e;
        } finally {
            reader.setAutoCommit(previousAutoCommit);
            reader.setTransactionIsolation(previousLevel);
        }
    }

    private static BigDecimal balance(PreparedStatement statement) throws SQLException {
        try (ResultSet rows = statement.executeQuery()) {
            if (!rows.next()) {
                throw new SQLException("no such account");
            }
            return rows.getBigDecimal(1);
        }
    }
}
