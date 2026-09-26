package practice;

import java.sql.Timestamp;
import java.time.Instant;

public final class JdbcStamps {

    private JdbcStamps() {
    }

    /** The Timestamp to store for {@code instant}; null stays null. */
    public static Timestamp write(Instant instant) {
        throw new UnsupportedOperationException("write write");
    }

    /** The Instant a stored Timestamp holds; null stays null. */
    public static Instant read(Timestamp stamp) {
        throw new UnsupportedOperationException("write read");
    }
}
