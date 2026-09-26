package practice;

import java.util.function.Predicate;

public final class Validators {

    private Validators() {
    }

    /** Returns a predicate for a non-empty email with text before the @ and text, then a dot, after it. */
    public static Predicate<String> email() {
        Predicate<String> notNull = s -> s != null;
        Predicate<String> notEmpty = s -> !s.isEmpty();
        Predicate<String> hasAtSign = s -> s.contains("@");
        Predicate<String> hasDomain = s -> {
            int at = s.indexOf('@');
            return at > 0 && s.lastIndexOf('.') > at;
        };
        return notNull.and(notEmpty).and(hasAtSign).and(hasDomain);
    }

    /** Returns a predicate for 8+ characters with upper, lower, digit and a non-alphanumeric character. */
    public static Predicate<String> strongPassword() {
        Predicate<String> minLength = p -> p != null && p.length() >= 8;
        Predicate<String> hasUpper = p -> p.chars().anyMatch(Character::isUpperCase);
        Predicate<String> hasLower = p -> p.chars().anyMatch(Character::isLowerCase);
        Predicate<String> hasDigit = p -> p.chars().anyMatch(Character::isDigit);
        Predicate<String> hasSpecial = p -> p.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
        return minLength.and(hasUpper).and(hasLower).and(hasDigit).and(hasSpecial);
    }
}
