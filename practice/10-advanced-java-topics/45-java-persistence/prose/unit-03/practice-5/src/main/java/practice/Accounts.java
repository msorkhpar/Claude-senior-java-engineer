package practice;

import java.math.BigDecimal;
import java.sql.Connection;
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
        throw new UnsupportedOperationException("TODO");
    }
}
