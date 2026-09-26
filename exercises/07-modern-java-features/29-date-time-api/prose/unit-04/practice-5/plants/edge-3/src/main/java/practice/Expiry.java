package practice;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;

public final class Expiry {

    private Expiry() {
    }

    /** issued + ttl, or Instant.MAX when that runs past the end of the timeline. */
    public static Instant expiresAt(Instant issued, Duration ttl) {
        try {
            return issued.plus(ttl);
        } catch (DateTimeException | ArithmeticException pastTheEnd) {
            // past Instant.MAX, or past the long of epoch seconds itself
            return Instant.MAX;
        }
    }

    /** Whether the token has expired at the clock's current instant. */
    public static boolean isExpired(Instant issued, Duration ttl, Clock clock) {
        return !clock.instant().isBefore(expiresAt(issued, ttl));
    }
}
