package practice;

import java.util.List;
import java.util.function.Predicate;

record User(String name, int age, boolean active, String email, boolean emailVerified) {
}

public final class UserFilters {

    public static final Predicate<User> IS_ADULT = user -> user.age() >= 18;

    public static final Predicate<User> IS_ACTIVE = User::active;

    public static final Predicate<User> HAS_VERIFIED_EMAIL =
            User::emailVerified;

    public static final Predicate<User> IS_ELIGIBLE =
            IS_ADULT.and(IS_ACTIVE).and(HAS_VERIFIED_EMAIL);

    private UserFilters() {
    }

    public static List<String> eligibleNames(List<User> users) {
        return users.stream()
                .filter(IS_ELIGIBLE)
                .map(User::name)
                .toList();
    }
}
