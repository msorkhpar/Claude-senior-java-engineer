package practice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public final class LockedTransfer {

    private LockedTransfer() {
    }

    /** Locks both rows (lower id first), then moves the amount; false if nothing changed. */
    public static boolean transfer(Connection connection, int fromId, int toId, BigDecimal amount) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }
}
