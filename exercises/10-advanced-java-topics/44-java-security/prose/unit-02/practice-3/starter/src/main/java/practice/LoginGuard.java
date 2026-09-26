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

    public LoginGuard(Credentials credentials, Clock clock) {
    }

    /** Logs a user in, with a generic failure message and a lockout after 5 failures in a row. */
    public String login(String user, String password) {
        throw new UnsupportedOperationException("TODO");
    }
}
