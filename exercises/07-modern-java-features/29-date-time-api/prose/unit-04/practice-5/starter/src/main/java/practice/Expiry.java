package practice;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public final class Expiry {

    private Expiry() {
    }

    /** issued + ttl, or Instant.MAX when that runs past the end of the timeline. */
    public static Instant expiresAt(Instant issued, Duration ttl) {
        throw new UnsupportedOperationException("write expiresAt");
    }

    /** Whether the token has expired at the clock's current instant. */
    public static boolean isExpired(Instant issued, Duration ttl, Clock clock) {
        throw new UnsupportedOperationException("write isExpired");
    }
}
