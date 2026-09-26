package practice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public final class Isolation {

    private Isolation() {
    }

    public record Reads(BigDecimal first, BigDecimal second) {
    }

    /** Reads the balance twice in one transaction at the given level, running {@code between} in the middle. */
    public static Reads readTwice(Connection reader, int level, int accountId, Runnable between) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }
}
