package practice;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public enum ValidationRule {
    NOT_NULL("Must not be null", todo()),
    NOT_BLANK("Must not be blank", todo()),
    POSITIVE_NUMBER("Must be positive", todo()),
    VALID_EMAIL("Must be a valid email", todo());

    private final String message;
    private final Predicate<Object> validator;

    ValidationRule(String message, Predicate<Object> validator) {
        this.message = message;
        this.validator = validator;
    }

    public String message() {
        return message;
    }

    /** Empty when {@code value} satisfies this rule, else this rule's message. */
    public Optional<String> validate(Object value) {
        throw new UnsupportedOperationException("write validate");
    }

    /** The messages of every rule {@code value} violates, in the order given. */
    public static List<String> validateAll(Object value, ValidationRule... rules) {
        throw new UnsupportedOperationException("write validateAll");
    }

    private static Predicate<Object> todo() {
        return v -> {
            throw new UnsupportedOperationException("write this predicate");
        };
    }
}
