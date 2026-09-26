package practice;

import java.util.Arrays;
import java.util.List;

public final class PasswordLogin {

    @FunctionalInterface
    public interface Authenticator {
        boolean authenticate(char[] password);
    }

    private PasswordLogin() {
    }

    /** Logs the attempt, asks the authenticator, and clears the password on every path. */
    public static boolean login(String user, char[] password, Authenticator authenticator, List<String> log) {
        log.add("Login attempt for user " + user);
        try {
            return authenticator.authenticate(password);
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
