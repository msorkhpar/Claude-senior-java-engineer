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
        throw new UnsupportedOperationException("TODO");
    }
}
