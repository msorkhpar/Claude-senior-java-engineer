package practice;

import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Builds the security report's timeline of logins. Login times reach it as java.util.Date from two
 * places: the gateway logs them with new Date(), and the session store returns them with
 * ResultSet.getTimestamp, which is a java.sql.Timestamp. Both sources record the same login, to the
 * millisecond, so each login usually appears twice.
 */
public final class LoginAudit {

    private LoginAudit() {
    }

    /** Every distinct moment at which a login happened, earliest first, each once. */
    public static List<Instant> distinctMoments(List<Date> loginTimes) {
        return new java.util.LinkedHashSet<>(loginTimes).stream()
                .map(Date::toInstant)
                .sorted()
                .toList();
    }
}
