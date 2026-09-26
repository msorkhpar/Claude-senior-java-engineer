package practice;

import java.util.function.Predicate;

public final class Validators {

    private Validators() {
    }

    /** Returns a predicate for a non-empty email with text before the @ and text, then a dot, after it. */
    public static Predicate<String> email() {
        throw new UnsupportedOperationException("write email");
    }

    /** Returns a predicate for 8+ characters with upper, lower, digit and a non-alphanumeric character. */
    public static Predicate<String> strongPassword() {
        throw new UnsupportedOperationException("write strongPassword");
    }
}
