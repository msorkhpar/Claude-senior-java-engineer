package practice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class Accounts {

    private Accounts() {
    }

    public record Account(int id, String owner, BigDecimal balance, int version) {
    }

    /** Thrown when the row changed (or vanished) since the caller read it. */
    public static final class OptimisticLockException extends Exception {
        public OptimisticLockException(String message) {
            super(message);
        }
    }

    /** Writes the new balance if the row still has the version the caller saw. */
    public static Account update(Connection connection, Account seen, BigDecimal newBalance)
            throws SQLException, OptimisticLockException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE accounts SET balance = ?, version = version + 1 WHERE id = ? AND version = ?")) {
            statement.setBigDecimal(1, newBalance);
            statement.setInt(2, seen.id());
            statement.setInt(3, seen.version());
            if (statement.executeUpdate() == 0) {
                try (PreparedStatement exists = connection.prepareStatement("SELECT 1 FROM accounts WHERE id = ?")) {
                    exists.setInt(1, seen.id());
                    if (!exists.executeQuery().next()) {
                        throw new IllegalStateException("account " + seen.id() + " does not exist");
                    }
                }
                throw new OptimisticLockException("account " + seen.id() + " changed since version " + seen.version());
            }
        }
        return new Account(seen.id(), seen.owner(), newBalance, seen.version() + 1);
    }
}
