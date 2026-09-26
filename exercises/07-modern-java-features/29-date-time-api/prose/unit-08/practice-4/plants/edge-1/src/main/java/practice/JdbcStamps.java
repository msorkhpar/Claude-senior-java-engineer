package practice;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;

public final class JdbcStamps {

    private JdbcStamps() {
    }

    /** The Timestamp to store for {@code instant}; null stays null. */
    public static Timestamp write(Instant instant) {
        if (instant == null) {
            return null;
        }
        return new Timestamp(Date.from(instant).getTime());
    }

    /** The Instant a stored Timestamp holds; null stays null. */
    public static Instant read(Timestamp stamp) {
        if (stamp == null) {
            return null;
        }
        return stamp.toInstant();
    }
}
