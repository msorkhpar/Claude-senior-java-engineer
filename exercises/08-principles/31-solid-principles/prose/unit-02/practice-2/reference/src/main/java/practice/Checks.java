package practice;

import java.util.Objects;

public final class Checks {

    private Checks() {
    }

    public interface Validator<T> {
        boolean isValid(T value);
    }

    /** The base validator: never modified. */
    public static final class NonNullValidator implements Validator<String> {
        public boolean isValid(String value) {
            return value != null;
        }
    }

    /** A decorator: adds a rule, and asks the validator it wraps first. */
    public static final class NonEmptyValidator implements Validator<String> {
        private final Validator<String> delegate;

        public NonEmptyValidator(Validator<String> delegate) {
            this.delegate = Objects.requireNonNull(delegate);
        }

        public boolean isValid(String value) {
            return delegate.isValid(value) && !value.isEmpty();
        }
    }

    /** A decorator: adds a minimum length, and asks the validator it wraps first. */
    public static final class MinLengthValidator implements Validator<String> {
        private final Validator<String> delegate;
        private final int min;

        public MinLengthValidator(Validator<String> delegate, int min) {
            this.delegate = Objects.requireNonNull(delegate);
            this.min = min;
        }

        public boolean isValid(String value) {
            return delegate.isValid(value) && value.length() >= min;
        }
    }
}
