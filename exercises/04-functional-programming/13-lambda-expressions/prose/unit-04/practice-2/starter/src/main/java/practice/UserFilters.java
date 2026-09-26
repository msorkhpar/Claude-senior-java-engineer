package practice;

import java.util.List;
import java.util.function.Predicate;

record User(String name, int age, boolean active, String email, boolean emailVerified) {
}

public final class UserFilters {

    public static final Predicate<User> IS_ADULT = user -> {
        throw new UnsupportedOperationException("write IS_ADULT");
    };

    public static final Predicate<User> IS_ACTIVE = user -> {
        throw new UnsupportedOperationException("write IS_ACTIVE");
    };

    public static final Predicate<User> HAS_VERIFIED_EMAIL = user -> {
        throw new UnsupportedOperationException("write HAS_VERIFIED_EMAIL");
    };

    public static final Predicate<User> IS_ELIGIBLE = user -> {
        throw new UnsupportedOperationException("write IS_ELIGIBLE");
    };

    private UserFilters() {
    }

    public static List<String> eligibleNames(List<User> users) {
        throw new UnsupportedOperationException("write eligibleNames");
    }
}
