package practice;

import java.util.List;
import java.util.function.Predicate;

public final class Eligibility {

    /** A user of the service. */
    public record User(String name, int age, boolean active, boolean verifiedEmail) {
    }

    private Eligibility() {
    }

    /** True for an active user, at least 18, with a verified email. */
    public static Predicate<User> isEligible() {
        return user -> user.active() && user.age() >= 18;
    }

    /** The names of the eligible users, in alphabetical order. */
    public static List<String> eligibleNames(List<User> users) {
        return users.stream()
                .filter(isEligible())
                .map(User::name)
                .sorted()
                .toList();
    }
}

