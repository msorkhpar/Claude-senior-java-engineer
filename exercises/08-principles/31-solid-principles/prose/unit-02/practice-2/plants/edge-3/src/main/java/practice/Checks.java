package practice;
public final class Checks {
    private Checks() {}
    public interface Validator<T> { boolean isValid(T value); }
    public static final class NonNullValidator implements Validator<String> {
        public boolean isValid(String value) { return value != null; }
    }
    public static final class NonEmptyValidator implements Validator<String> {
        private final Validator<String> delegate;
        public NonEmptyValidator(Validator<String> delegate) { this.delegate = delegate; }
        public boolean isValid(String value) { return delegate.isValid(value) && !value.isEmpty(); }
    }
    public static final class MinLengthValidator implements Validator<String> {
        private final Validator<String> delegate; private final int min;
        public MinLengthValidator(Validator<String> delegate, int min) { this.delegate = delegate; this.min = min; }
        public boolean isValid(String value) { return delegate.isValid(value) && value.codePointCount(0, value.length()) >= min; }
    }
}
