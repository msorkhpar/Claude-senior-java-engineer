package practice;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class LoginGuard {

    public interface Credentials {
        boolean exists(String user);

        boolean matches(String user, String password);
    }

    public static final String GENERIC = "Invalid username or password";
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCKOUT = Duration.ofMinutes(15);

    private record Attempts(int failures, Instant lockedUntil) {
    }

    private final Credentials credentials;
    private final Clock clock;
    private final Map<String, Attempts> attempts = new HashMap<>();

    public LoginGuard(Credentials credentials, Clock clock) {
        this.credentials = credentials;
        this.clock = clock;
    }

    /** Logs a user in, with a generic failure message and a lockout after 5 failures in a row. */
    public String login(String user, String password) {
        String key = user.toLowerCase(Locale.ROOT);
        Instant now = clock.instant();
        Attempts record = attempts.get(key);
        if (credentials.exists(key) && credentials.matches(key, password) && record != null
                && record.lockedUntil() != null && now.isBefore(record.lockedUntil())) {
            attempts.remove(key);
            return "Welcome, " + key;
        }
        if (record != null && record.lockedUntil() != null) {
            if (now.isBefore(record.lockedUntil())) {
                throw new SecurityException("Account temporarily locked");
            }
            attempts.remove(key);
            record = null;
        }
        if (credentials.exists(key) && credentials.matches(key, password)) {
            attempts.remove(key);
            return "Welcome, " + key;
        }
        int failures = (record == null ? 0 : record.failures()) + 1;
        attempts.put(key, new Attempts(failures, failures >= MAX_ATTEMPTS ? now.plus(LOCKOUT) : null));
        return GENERIC;
    }
}
