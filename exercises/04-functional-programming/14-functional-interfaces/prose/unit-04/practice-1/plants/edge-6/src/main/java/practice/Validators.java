package practice;

import java.util.function.Predicate;

public final class Validators {
    private Validators() {
    }

    public static Predicate<String> email() {
        Predicate<String> notNull = s -> s != null;
        Predicate<String> notEmpty = s -> !s.isEmpty();
        Predicate<String> hasDomain = s -> {
            int at = s.indexOf('@');
            return at > 0 && s.indexOf('.') > at + 1;
        };
        return notNull.and(notEmpty).and(hasDomain);
    }

    public static Predicate<String> strongPassword() {
        Predicate<String> minLength = p -> p != null && p.length() >= 8;
        Predicate<String> hasUpper = p -> p.chars().anyMatch(Character::isUpperCase);
        Predicate<String> hasLower = p -> p.chars().anyMatch(Character::isLowerCase);
        Predicate<String> hasDigit = p -> p.chars().anyMatch(Character::isDigit);
        Predicate<String> hasSpecial = p -> p.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
        return minLength.and(hasUpper).and(hasLower).and(hasDigit).and(hasSpecial);
    }
}
