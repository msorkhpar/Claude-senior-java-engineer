package practice;

import java.util.List;

/** A user account; email may be null. */
record User(String username, String email, boolean verified, int age) {
}

public final class Eligibility {

    private Eligibility() {
    }

    /** Whether the user is 18 or older. */
    public static boolean isAdult(User u) {
        throw new UnsupportedOperationException("write isAdult");
    }

    /** Whether the email is present and not blank. */
    public static boolean hasEmail(User u) {
        throw new UnsupportedOperationException("write hasEmail");
    }

    /** Usernames of adult, verified users with an email, in order. */
    public static List<String> eligible(List<User> users) {
        throw new UnsupportedOperationException("write eligible");
    }

    /** Usernames of unverified users, in order. */
    public static String[] unverified(List<User> users) {
        throw new UnsupportedOperationException("write unverified");
    }
}
