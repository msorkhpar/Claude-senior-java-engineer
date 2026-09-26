package practice;

public final class Checks {

    private Checks() {
    }

    public interface Validator<T> {
        boolean isValid(T value);
    }

    /** The base validator: never modified. */
    public static final class NonNullValidator implements Validator<String> {
        public boolean isValid(String value) {
            throw new UnsupportedOperationException("write isValid");
        }
    }

    /** A decorator: adds a rule, and asks the validator it wraps first. */
    public static final class NonEmptyValidator implements Validator<String> {

        public NonEmptyValidator(Validator<String> delegate) {
        }

        public boolean isValid(String value) {
            throw new UnsupportedOperationException("write isValid");
        }
    }

    /** A decorator: adds a minimum length, and asks the validator it wraps first. */
    public static final class MinLengthValidator implements Validator<String> {

        public MinLengthValidator(Validator<String> delegate, int min) {
        }

        public boolean isValid(String value) {
            throw new UnsupportedOperationException("write isValid");
        }
    }
}
