package practice;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class Validators {
    private Validators() {
    }

    public static Predicate<String> email() {
        Predicate<String> notNull = s -> s != null;
        Predicate<String> notEmpty = s -> !s.isEmpty();
        Predicate<String> hasDomain = s -> {
            int at = s.indexOf('@');
            return at > 0 && s.lastIndexOf('.') > at + 1;
        };
        return notNull.and(notEmpty).and(hasDomain);
    }

    private static Predicate<String> has(String regex) {
        Pattern p = Pattern.compile(regex);
        return s -> p.matcher(s).find();
    }

    public static Predicate<String> strongPassword() {
        Predicate<String> minLength = p -> p != null && p.length() >= 8;
        return minLength.and(has("[A-Z]")).and(has("[a-z]")).and(has("[0-9]")).and(has("[^A-Za-z0-9]"));
    }
}
