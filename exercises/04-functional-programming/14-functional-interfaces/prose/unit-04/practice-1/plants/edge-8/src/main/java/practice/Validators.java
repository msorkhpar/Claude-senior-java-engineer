package practice;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class Validators {
    private static final Pattern EMAIL = Pattern.compile("[^@]+@[^@.]+\\..+");

    private Validators() {
    }

    public static Predicate<String> email() {
        Predicate<String> notNull = s -> s != null;
        Predicate<String> shape = s -> EMAIL.matcher(s).matches();
        return notNull.and(shape);
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
