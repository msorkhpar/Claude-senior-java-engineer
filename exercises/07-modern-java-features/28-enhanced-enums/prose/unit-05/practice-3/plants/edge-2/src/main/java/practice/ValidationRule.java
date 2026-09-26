package practice;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public enum ValidationRule {
    NOT_NULL("Must not be null", Objects::nonNull),
    NOT_BLANK("Must not be blank", v -> !String.valueOf(v).isBlank()),
    POSITIVE_NUMBER("Must be positive", v -> v instanceof Number n && n.doubleValue() > 0),
    VALID_EMAIL("Must be a valid email", v -> String.valueOf(v).matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"));

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
        return validator.test(value) ? Optional.empty() : Optional.of(message);
    }

    /** The messages of every rule {@code value} violates, in the order given. */
    public static List<String> validateAll(Object value, ValidationRule... rules) {
        return Arrays.stream(rules)
                .map(rule -> rule.validate(value))
                .flatMap(Optional::stream)
                .toList();
    }
}
