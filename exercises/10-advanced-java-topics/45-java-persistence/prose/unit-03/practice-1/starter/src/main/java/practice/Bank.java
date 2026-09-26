package practice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public final class Bank {

    private Bank() {
    }

    /** Moves the amount in one transaction; true if it moved, false if nothing changed. */
    public static boolean transfer(Connection connection, int fromId, int toId, BigDecimal amount) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }
}
