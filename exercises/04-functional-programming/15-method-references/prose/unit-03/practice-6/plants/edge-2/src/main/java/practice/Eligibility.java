package practice;

import java.util.List;
import java.util.function.Predicate;

/** A user account; email may be null. */
record User(String username, String email, boolean verified, int age) {
}

public final class Eligibility {

    private Eligibility() {
    }

    /** Whether the user is 18 or older. */
    public static boolean isAdult(User u) {
        return u.age() >= 18;
    }

    /** Whether the email is present and not blank. */
    public static boolean hasEmail(User u) {
        return u.email() != null && !u.email().isEmpty();
    }

    /** Usernames of adult, verified users with an email, in order. */
    public static List<String> eligible(List<User> users) {
        Predicate<User> adult = Eligibility::isAdult;
        return users.stream()
                .filter(adult.and(User::verified).and(Eligibility::hasEmail))
                .map(User::username)
                .toList();
    }

    /** Usernames of unverified users, in order. */
    public static String[] unverified(List<User> users) {
        return users.stream()
                .filter(Predicate.not(User::verified))
                .map(User::username)
                .toArray(String[]::new);
    }
}
